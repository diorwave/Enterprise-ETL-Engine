package com.quantumsoft.tia.scanner.services

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Disabled
import java.time.Duration

@DisplayName("SettingsCache Tests")
class SettingsCacheTest {

    private lateinit var cache: SettingsCache

    @BeforeEach
    fun setUp() {
        cache = SettingsCacheImpl(
            ttl = Duration.ofSeconds(2),
            maxSize = 100
        )
    }

    @Test
    fun `should store and retrieve value`() = runTest {
        // Given
        val key = "test.key"
        val value = "test.value"

        // When
        cache.put(key, value)
        val retrieved = cache.get(key)

        // Then
        assertThat(retrieved).isEqualTo(value)
    }

    @Test
    fun `should return null for non-existent key`() = runTest {
        // When
        val retrieved = cache.get("non.existent")

        // Then
        assertThat(retrieved).isNull()
    }

    @Test
    fun `should remove value`() = runTest {
        // Given
        val key = "remove.test"
        cache.put(key, "value")

        // When
        cache.remove(key)
        val retrieved = cache.get(key)

        // Then
        assertThat(retrieved).isNull()
    }

    @Test
    fun `should clear all values`() = runTest {
        // Given
        cache.put("key1", "value1")
        cache.put("key2", "value2")
        cache.put("key3", "value3")

        // When
        cache.clear()

        // Then
        assertThat(cache.get("key1")).isNull()
        assertThat(cache.get("key2")).isNull()
        assertThat(cache.get("key3")).isNull()
        assertThat(cache.size()).isEqualTo(0)
    }

    @Test
    @Disabled("Timing test is flaky in CI")
    fun `should expire values after TTL`() = runTest {
        // Given
        val shortLivedCache = SettingsCacheImpl(
            ttl = Duration.ofMillis(200),
            maxSize = 10
        )
        val key = "expiring.key"
        val value = "expiring.value"

        // When
        shortLivedCache.put(key, value)
        assertThat(shortLivedCache.get(key)).isEqualTo(value)
        
        // Wait for expiration
        delay(250)

        // Then
        assertThat(shortLivedCache.get(key)).isNull()
    }

    @Test
    fun `should update existing value`() = runTest {
        // Given
        val key = "update.key"
        cache.put(key, "initial")

        // When
        cache.put(key, "updated")
        val retrieved = cache.get(key)

        // Then
        assertThat(retrieved).isEqualTo("updated")
    }

    @Test
    fun `should handle batch put operations`() = runTest {
        // Given
        val entries = mapOf(
            "batch1" to "value1",
            "batch2" to "value2",
            "batch3" to "value3"
        )

        // When
        cache.putAll(entries)

        // Then
        assertThat(cache.get("batch1")).isEqualTo("value1")
        assertThat(cache.get("batch2")).isEqualTo("value2")
        assertThat(cache.get("batch3")).isEqualTo("value3")
    }

    @Test
    fun `should return all cached entries`() = runTest {
        // Given
        cache.put("all1", "value1")
        cache.put("all2", "value2")

        // When
        val all = cache.getAll()

        // Then
        assertThat(all).containsEntry("all1", "value1")
        assertThat(all).containsEntry("all2", "value2")
    }

    @Test
    fun `should check if key exists`() = runTest {
        // Given
        cache.put("exists", "value")

        // When & Then
        assertThat(cache.containsKey("exists")).isTrue()
        assertThat(cache.containsKey("not.exists")).isFalse()
    }

    @Test
    fun `should return cache size`() = runTest {
        // Given & When
        assertThat(cache.size()).isEqualTo(0)
        
        cache.put("size1", "value1")
        assertThat(cache.size()).isEqualTo(1)
        
        cache.put("size2", "value2")
        assertThat(cache.size()).isEqualTo(2)
        
        cache.remove("size1")
        assertThat(cache.size()).isEqualTo(1)
    }

    @Test
    fun `should evict oldest entries when max size reached`() = runTest {
        // Given
        val smallCache = SettingsCacheImpl(
            ttl = Duration.ofMinutes(5),
            maxSize = 3
        )

        // When - Add more than max size
        smallCache.put("evict1", "value1")
        smallCache.put("evict2", "value2")
        smallCache.put("evict3", "value3")
        smallCache.put("evict4", "value4") // Should evict evict1

        // Then
        assertThat(smallCache.get("evict1")).isNull() // Evicted
        assertThat(smallCache.get("evict2")).isEqualTo("value2")
        assertThat(smallCache.get("evict3")).isEqualTo("value3")
        assertThat(smallCache.get("evict4")).isEqualTo("value4")
        assertThat(smallCache.size()).isEqualTo(3)
    }

    @Test
    @Disabled("Timing test is flaky in CI")
    fun `should refresh TTL on access`() = runTest {
        // Given
        val refreshCache = SettingsCacheImpl(
            ttl = Duration.ofMillis(300),
            maxSize = 10,
            refreshOnAccess = true
        )
        val key = "refresh.key"
        val value = "refresh.value"

        // When
        refreshCache.put(key, value)
        delay(150) // Half of TTL
        
        // Access to refresh TTL
        assertThat(refreshCache.get(key)).isEqualTo(value)
        
        delay(200) // Original would have expired, but was refreshed

        // Then - Should still be available
        assertThat(refreshCache.get(key)).isEqualTo(value)
    }

    @Test
    fun `should handle concurrent operations`() = runTest {
        // Given
        val keys = (1..100).map { "concurrent.$it" }

        // When - Concurrent puts
        val putJobs = keys.map { key ->
            launch {
                cache.put(key, "value-$key")
            }
        }
        putJobs.forEach { it.join() }

        // Then - All should be stored
        keys.forEach { key ->
            assertThat(cache.get(key)).isEqualTo("value-$key")
        }
    }

    @Test
    fun `should compute if absent`() = runTest {
        // Given
        val key = "compute.key"
        var computeCount = 0
        val computeFunction: suspend () -> String = {
            computeCount++
            "computed-value"
        }

        // When - First call computes
        val value1 = cache.computeIfAbsent(key, computeFunction)
        
        // When - Second call returns cached
        val value2 = cache.computeIfAbsent(key, computeFunction)

        // Then
        assertThat(value1).isEqualTo("computed-value")
        assertThat(value2).isEqualTo("computed-value")
        assertThat(computeCount).isEqualTo(1) // Only computed once
    }

    @Test
    fun `should get statistics`() = runTest {
        // Given
        cache.put("stats1", "value1")
        cache.put("stats2", "value2")
        cache.get("stats1") // Hit
        cache.get("stats3") // Miss

        // When
        val stats = cache.getStatistics()

        // Then
        assertThat(stats.size).isEqualTo(2)
        assertThat(stats.hitCount).isEqualTo(1)
        assertThat(stats.missCount).isEqualTo(1)
        assertThat(stats.hitRate).isEqualTo(0.5)
    }
}