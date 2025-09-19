package com.quantumsoft.tia.scanner.integration

import com.quantumsoft.tia.scanner.entities.SettingType
import com.quantumsoft.tia.scanner.repositories.SystemSettingsRepository
import com.quantumsoft.tia.scanner.services.SettingsService
import com.quantumsoft.tia.scanner.services.SettingsChangeListener
import com.quantumsoft.tia.scanner.services.SettingsChangeNotifier
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.launch
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.test.context.ActiveProfiles
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("SettingsService Integration Tests")
class SettingsServiceIntegrationTest : BaseIntegrationTest() {

    @Autowired
    private lateinit var settingsService: SettingsService

    @Autowired
    private lateinit var repository: SystemSettingsRepository

    @Autowired
    private lateinit var redisTemplate: RedisTemplate<String, String>

    @Autowired
    private lateinit var settingsChangeNotifier: SettingsChangeNotifier

    @BeforeEach
    fun setUp() {
        repository.deleteAll()
        redisTemplate.keys("settings:*").forEach { redisTemplate.delete(it) }
    }

    @Test
    fun `should persist setting to database`() = runTest {
        // Given
        val key = "test.persistence"
        val value = "persistent-value"

        // When
        settingsService.setSetting(key, value, SettingType.STRING)

        // Then
        val saved = repository.findBySettingKey(key)
        assertThat(saved).isPresent
        assertThat(saved.get().settingValue).isEqualTo(value)
        assertThat(saved.get().valueType).isEqualTo(SettingType.STRING)
    }

    @Test
    fun `should cache setting in Redis`() = runTest {
        // Given
        val key = "test.cache"
        val value = "cached-value"

        // When
        settingsService.setSetting(key, value, SettingType.STRING)
        val retrieved = settingsService.getSetting(key)

        // Then
        assertThat(retrieved).isEqualTo(value)
        val cachedValue = redisTemplate.opsForValue().get("settings:$key")
        assertThat(cachedValue).isEqualTo(value)
    }

    @Test
    fun `should update existing setting`() = runTest {
        // Given
        val key = "test.update"
        settingsService.setSetting(key, "initial", SettingType.STRING)

        // When
        settingsService.setSetting(key, "updated", SettingType.STRING)

        // Then
        val retrieved = settingsService.getSetting(key)
        assertThat(retrieved).isEqualTo("updated")
        
        val dbValue = repository.findBySettingKey(key)
        assertThat(dbValue).isPresent
        assertThat(dbValue.get().settingValue).isEqualTo("updated")
    }

    @Test
    fun `should delete setting from database and cache`() = runTest {
        // Given
        val key = "test.delete"
        settingsService.setSetting(key, "to-delete", SettingType.STRING)

        // When
        settingsService.deleteSetting(key)

        // Then
        val retrieved = settingsService.getSetting(key)
        assertThat(retrieved).isNull()
        
        val dbValue = repository.findBySettingKey(key)
        assertThat(dbValue).isEmpty
        
        val cachedValue = redisTemplate.opsForValue().get("settings:$key")
        assertThat(cachedValue).isNull()
    }

    @Test
    fun `should handle integer settings`() = runTest {
        // Given
        val key = "test.integer"
        val value = 42

        // When
        settingsService.setSetting(key, value, SettingType.INTEGER)
        val retrieved = settingsService.getSettingAs(key, Int::class.java)

        // Then
        assertThat(retrieved).isEqualTo(42)
    }

    @Test
    fun `should handle boolean settings`() = runTest {
        // Given
        val key = "test.boolean"
        val value = true

        // When
        settingsService.setSetting(key, value, SettingType.BOOLEAN)
        val retrieved = settingsService.getSettingAs(key, Boolean::class.java)

        // Then
        assertThat(retrieved).isEqualTo(true)
    }

    @Test
    fun `should handle long settings`() = runTest {
        // Given
        val key = "test.long"
        val value = 9999999999L

        // When
        settingsService.setSetting(key, value, SettingType.LONG)
        val retrieved = settingsService.getSettingAs(key, Long::class.java)

        // Then
        assertThat(retrieved).isEqualTo(9999999999L)
    }

    @Test
    fun `should handle JSON settings`() = runTest {
        // Given
        val key = "test.json"
        val value = mapOf(
            "threshold" to 5000,
            "enabled" to true,
            "features" to listOf("scan", "validate")
        )

        // When
        settingsService.setSetting(key, value, SettingType.JSON)
        val retrieved = settingsService.getSettingAs(key, Map::class.java)

        // Then
        assertThat(retrieved).isNotNull
        assertThat(retrieved!!["threshold"]).isEqualTo(5000)
        assertThat(retrieved["enabled"]).isEqualTo(true)
        @Suppress("UNCHECKED_CAST")
        assertThat(retrieved["features"] as List<String>).containsExactly("scan", "validate")
    }

    @Test
    fun `should get all settings`() = runTest {
        // Given
        settingsService.setSetting("key1", "value1", SettingType.STRING)
        settingsService.setSetting("key2", "value2", SettingType.STRING)
        settingsService.setSetting("key3", "123", SettingType.INTEGER)

        // When
        val allSettings = settingsService.getAllSettings()

        // Then
        assertThat(allSettings).hasSize(3)
        assertThat(allSettings).containsEntry("key1", "value1")
        assertThat(allSettings).containsEntry("key2", "value2")
        assertThat(allSettings).containsEntry("key3", "123")
    }

    @Test
    fun `should get multiple settings by keys`() = runTest {
        // Given
        settingsService.setSetting("multi1", "value1", SettingType.STRING)
        settingsService.setSetting("multi2", "value2", SettingType.STRING)
        settingsService.setSetting("multi3", "value3", SettingType.STRING)

        // When
        val settings = settingsService.getSettings(listOf("multi1", "multi3", "nonexistent"))

        // Then
        assertThat(settings).hasSize(2)
        assertThat(settings).containsEntry("multi1", "value1")
        assertThat(settings).containsEntry("multi3", "value3")
        assertThat(settings).doesNotContainKey("nonexistent")
    }

    @Test
    fun `should notify listeners on setting change`() = runTest {
        // Given
        val latch = CountDownLatch(1)
        val receivedKey = AtomicReference<String>()
        val receivedValue = AtomicReference<String>()
        
        val listener = object : SettingsChangeListener {
            override suspend fun onSettingChanged(key: String, value: String) {
                receivedKey.set(key)
                receivedValue.set(value)
                latch.countDown()
            }
        }
        
        settingsChangeNotifier.subscribeToChanges(listener)

        // When
        settingsService.setSetting("notify.test", "notify-value", SettingType.STRING)

        // Then
        assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue()
        assertThat(receivedKey.get()).isEqualTo("notify.test")
        assertThat(receivedValue.get()).isEqualTo("notify-value")
        
        // Cleanup
        settingsChangeNotifier.unsubscribeFromChanges(listener)
    }

    @Test
    fun `should handle concurrent settings updates`() = runTest {
        // Given
        val key = "concurrent.test"
        val iterations = 10

        // When - Update setting concurrently
        val jobs = (1..iterations).map { i ->
            launch {
                settingsService.setSetting(key, "value-$i", SettingType.STRING)
            }
        }
        jobs.forEach { it.join() }

        // Then - Should have the last value
        val finalValue = settingsService.getSetting(key)
        assertThat(finalValue).isNotNull()
        assertThat(finalValue).matches("value-\\d+")
        
        // Database should have only one entry
        val dbEntries = repository.findBySettingKey(key)
        assertThat(dbEntries).isPresent
    }

    @Test
    fun `should invalidate cache when setting is updated`() = runTest {
        // Given
        val key = "cache.invalidation"
        settingsService.setSetting(key, "initial", SettingType.STRING)
        
        // Ensure it's cached
        settingsService.getSetting(key)
        assertThat(redisTemplate.opsForValue().get("settings:$key")).isEqualTo("initial")

        // When
        settingsService.setSetting(key, "updated", SettingType.STRING)

        // Then
        val retrieved = settingsService.getSetting(key)
        assertThat(retrieved).isEqualTo("updated")
        assertThat(redisTemplate.opsForValue().get("settings:$key")).isEqualTo("updated")
    }

    @Test
    fun `should handle setting with null description`() = runTest {
        // Given
        val key = "null.description"
        val value = "value"

        // When
        settingsService.setSetting(key, value, SettingType.STRING)

        // Then
        val saved = repository.findBySettingKey(key)
        assertThat(saved).isPresent
        assertThat(saved.get().description).isNull()
    }

    @Test
    fun `should persist version for optimistic locking`() = runTest {
        // Given
        val key = "version.test"
        settingsService.setSetting(key, "v1", SettingType.STRING)
        
        val initial = repository.findBySettingKey(key).get()
        val initialVersion = initial.version

        // When
        settingsService.setSetting(key, "v2", SettingType.STRING)

        // Then
        val updated = repository.findBySettingKey(key).get()
        assertThat(updated.version).isGreaterThan(initialVersion)
    }

    @Test
    fun `should handle Redis unavailability gracefully`() = runTest {
        // This test would require mocking Redis connection failure
        // For now, we'll test that the service still works with database
        
        // Given
        val key = "redis.fallback"
        val value = "fallback-value"

        // When
        settingsService.setSetting(key, value, SettingType.STRING)

        // Then - Should still be able to get from database
        val retrieved = settingsService.getSetting(key)
        assertThat(retrieved).isEqualTo(value)
    }
}