package com.quantumsoft.tia.scanner.services

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

@Component
class SettingsCacheImpl(
    @Value("\${settings.cache.ttl:300}") ttlSeconds: Long = 300,
    @Value("\${settings.cache.max-size:1000}") private val maxSize: Int = 1000,
    @Value("\${settings.cache.refresh-on-access:true}") private val refreshOnAccess: Boolean = true
) : SettingsCache {

    constructor(ttl: Duration, maxSize: Int, refreshOnAccess: Boolean = false) : this(
        ttl.seconds,
        maxSize,
        refreshOnAccess
    )

    private val logger = LoggerFactory.getLogger(SettingsCacheImpl::class.java)
    private val cache = ConcurrentHashMap<String, CacheEntry>()
    private val mutex = Mutex()
    private val ttl = Duration.ofSeconds(ttlSeconds)
    
    // Statistics
    private val hitCount = AtomicLong(0)
    private val missCount = AtomicLong(0)
    private val evictionCount = AtomicLong(0)

    private data class CacheEntry(
        val value: String,
        var expireAt: Instant
    )

    override suspend fun get(key: String): String? = mutex.withLock {
        val entry = cache[key]
        
        return if (entry != null && !isExpired(entry)) {
            hitCount.incrementAndGet()
            if (refreshOnAccess) {
                // Refresh TTL on access
                entry.expireAt = Instant.now().plus(ttl)
            }
            entry.value
        } else {
            missCount.incrementAndGet()
            if (entry != null) {
                // Remove expired entry
                cache.remove(key)
            }
            null
        }
    }

    override suspend fun put(key: String, value: String) = mutex.withLock {
        // Check if we need to evict entries
        if (cache.size >= maxSize && !cache.containsKey(key)) {
            evictOldest()
        }
        
        cache[key] = CacheEntry(value, Instant.now().plus(ttl))
        logger.debug("Cached setting: $key")
    }

    override suspend fun putAll(entries: Map<String, String>) = mutex.withLock {
        entries.forEach { (key, value) ->
            // Check size for each entry
            if (cache.size >= maxSize && !cache.containsKey(key)) {
                evictOldest()
            }
            cache[key] = CacheEntry(value, Instant.now().plus(ttl))
        }
        logger.debug("Cached ${entries.size} settings")
    }

    override suspend fun remove(key: String) = mutex.withLock {
        cache.remove(key)
        logger.debug("Removed setting from cache: $key")
    }

    override suspend fun clear() = mutex.withLock {
        cache.clear()
        logger.info("Cache cleared")
    }

    override suspend fun getAll(): Map<String, String> = mutex.withLock {
        // Clean expired entries first
        cleanExpired()
        
        return cache.mapValues { it.value.value }
    }

    override suspend fun containsKey(key: String): Boolean = mutex.withLock {
        val entry = cache[key]
        return entry != null && !isExpired(entry)
    }

    override suspend fun size(): Int = mutex.withLock {
        cleanExpired()
        return cache.size
    }

    override suspend fun computeIfAbsent(key: String, compute: suspend () -> String): String {
        // First check without lock to avoid deadlock
        val existing = getInternal(key)
        if (existing != null) {
            return existing
        }
        
        // Now lock and check again (double-check pattern)
        return mutex.withLock {
            val entry = cache[key]
            if (entry != null && !isExpired(entry)) {
                hitCount.incrementAndGet()
                entry.value
            } else {
                val computed = compute()
                cache[key] = CacheEntry(computed, Instant.now().plus(ttl))
                computed
            }
        }
    }
    
    private suspend fun getInternal(key: String): String? = mutex.withLock {
        val entry = cache[key]
        if (entry != null && !isExpired(entry)) {
            hitCount.incrementAndGet()
            entry.value
        } else {
            missCount.incrementAndGet()
            null
        }
    }

    override fun getStatistics(): CacheStatistics {
        return CacheStatistics(
            size = cache.size,
            hitCount = hitCount.get(),
            missCount = missCount.get(),
            evictionCount = evictionCount.get()
        )
    }

    private fun isExpired(entry: CacheEntry): Boolean {
        return Instant.now().isAfter(entry.expireAt)
    }

    private fun cleanExpired() {
        val now = Instant.now()
        val expired = cache.entries.filter { now.isAfter(it.value.expireAt) }
        expired.forEach { cache.remove(it.key) }
        
        if (expired.isNotEmpty()) {
            logger.debug("Cleaned ${expired.size} expired entries from cache")
        }
    }

    private fun evictOldest() {
        // Find the oldest entry (earliest expiration)
        val oldest = cache.entries.minByOrNull { it.value.expireAt }
        if (oldest != null) {
            cache.remove(oldest.key)
            evictionCount.incrementAndGet()
            logger.debug("Evicted oldest cache entry: ${oldest.key}")
        }
    }
}