package com.quantumsoft.tia.scanner.services

interface SettingsCache {
    suspend fun get(key: String): String?
    suspend fun put(key: String, value: String)
    suspend fun putAll(entries: Map<String, String>)
    suspend fun remove(key: String)
    suspend fun clear()
    suspend fun getAll(): Map<String, String>
    suspend fun containsKey(key: String): Boolean
    suspend fun size(): Int
    suspend fun computeIfAbsent(key: String, compute: suspend () -> String): String
    fun getStatistics(): CacheStatistics
}

data class CacheStatistics(
    val size: Int,
    val hitCount: Long,
    val missCount: Long,
    val evictionCount: Long
) {
    val hitRate: Double
        get() = if (hitCount + missCount > 0) {
            hitCount.toDouble() / (hitCount + missCount)
        } else {
            0.0
        }
}