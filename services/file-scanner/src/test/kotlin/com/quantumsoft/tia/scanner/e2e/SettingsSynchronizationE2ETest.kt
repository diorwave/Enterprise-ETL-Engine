package com.quantumsoft.tia.scanner.e2e

import com.quantumsoft.tia.scanner.entities.SettingType
import com.quantumsoft.tia.scanner.services.SettingsService
import com.quantumsoft.tia.scanner.services.SettingsChangeNotifier
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.core.ParameterizedTypeReference
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@DisplayName("Settings Synchronization E2E Tests")
class SettingsSynchronizationE2ETest : BaseE2ETest() {

    @Autowired
    private lateinit var settingsService: SettingsService
    
    @Autowired
    private lateinit var settingsChangeNotifier: SettingsChangeNotifier
    
    @Autowired
    private lateinit var redisTemplate: RedisTemplate<String, String>
    
    @Autowired
    private lateinit var restTemplate: TestRestTemplate
    
    @LocalServerPort
    private var port: Int = 0
    
    @BeforeEach
    fun setUp() {
        // Clear all settings before each test
        runBlocking {
            settingsService.getAllSettings().keys.forEach { key ->
                settingsService.deleteSetting(key)
            }
        }
    }
    
    @Test
    fun `should synchronize settings across instances via Redis pub-sub`() = runTest {
        // Given - Create a setting via service
        val settingKey = "test.sync.setting"
        val initialValue = "initialValue"
        val updatedValue = "updatedValue"
        
        settingsService.setSetting(settingKey, initialValue, SettingType.STRING)
        
        // Verify initial value
        val initial = settingsService.getSetting(settingKey)
        assertThat(initial).isEqualTo(initialValue)
        
        // When - Update setting via REST API (simulating another instance)
        val url = "http://localhost:$port/api/v1/settings/$settingKey"
        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
        }
        val request = HttpEntity(mapOf(
            "value" to updatedValue,
            "type" to "STRING"
        ), headers)
        
        val response = restTemplate.exchange(
            url,
            HttpMethod.PUT,
            request,
            String::class.java
        )
        
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        
        // Wait for synchronization
        delay(500)
        
        // Then - Verify the setting is synchronized
        val synchronized = settingsService.getSetting(settingKey)
        assertThat(synchronized).isEqualTo(updatedValue)
    }
    
    @Test
    fun `should propagate setting deletion across instances`() = runTest {
        // Given - Create a setting
        val settingKey = "test.delete.setting"
        settingsService.setSetting(settingKey, "toBeDeleted", SettingType.STRING)
        
        // Verify it exists
        assertThat(settingsService.getSetting(settingKey)).isNotNull()
        
        // When - Delete via REST API
        val url = "http://localhost:$port/api/v1/settings/$settingKey"
        val response = restTemplate.exchange(
            url,
            HttpMethod.DELETE,
            null,
            Void::class.java
        )
        
        assertThat(response.statusCode).isEqualTo(HttpStatus.NO_CONTENT)
        
        // Wait for synchronization
        delay(500)
        
        // Then - Verify deletion is synchronized
        val deleted = settingsService.getSetting(settingKey)
        assertThat(deleted).isNull()
    }
    
    @Test
    fun `should handle batch settings updates`() = runTest {
        // Given - Create multiple settings
        val settings = mapOf(
            "batch.setting1" to "value1",
            "batch.setting2" to "value2",
            "batch.setting3" to "value3"
        )
        
        settings.forEach { (key, value) ->
            settingsService.setSetting(key, value, SettingType.STRING)
        }
        
        // When - Update all settings via batch endpoint
        val url = "http://localhost:$port/api/v1/settings/batch"
        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
        }
        
        val updates = settings.map { (key, _) ->
            mapOf(
                "key" to key,
                "value" to "updated_$key",
                "type" to "STRING"
            )
        }
        
        val request = HttpEntity(updates, headers)
        val response = restTemplate.exchange(
            url,
            HttpMethod.PUT,
            request,
            object : ParameterizedTypeReference<Map<String, String>>() {}
        )
        
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        
        // Wait for synchronization
        delay(500)
        
        // Then - Verify all settings are updated
        settings.keys.forEach { key ->
            val value = settingsService.getSetting(key)
            assertThat(value).isEqualTo("updated_$key")
        }
    }
    
    @Test
    fun `should synchronize file threshold setting changes`() = runTest {
        // Given - Set initial threshold
        val thresholdKey = "scanner.file.threshold"
        val initialThreshold = 1000
        val updatedThreshold = 5000
        
        settingsService.setSetting(thresholdKey, initialThreshold, SettingType.INTEGER)
        
        // Verify initial value
        val initial = settingsService.getSettingAs(thresholdKey, Int::class.java)
        assertThat(initial).isEqualTo(initialThreshold)
        
        // When - Update threshold via REST API
        val url = "http://localhost:$port/api/v1/settings/$thresholdKey"
        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
        }
        val request = HttpEntity(mapOf(
            "value" to updatedThreshold.toString(),
            "type" to "INTEGER"
        ), headers)
        
        val response = restTemplate.exchange(
            url,
            HttpMethod.PUT,
            request,
            String::class.java
        )
        
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        
        // Wait for synchronization
        delay(500)
        
        // Then - Verify threshold is synchronized
        val synchronized = settingsService.getSettingAs(thresholdKey, Int::class.java)
        assertThat(synchronized).isEqualTo(updatedThreshold)
    }
    
    @Test
    fun `should handle concurrent settings updates`() = runTest {
        // Given - Create a setting
        val settingKey = "test.concurrent.setting"
        settingsService.setSetting(settingKey, "initial", SettingType.STRING)
        
        val updateCount = 10
        val latch = CountDownLatch(updateCount)
        val errors = mutableListOf<Exception>()
        
        // When - Perform concurrent updates
        val threads = (1..updateCount).map { i ->
            Thread {
                try {
                    val url = "http://localhost:$port/api/v1/settings/$settingKey"
                    val headers = HttpHeaders().apply {
                        contentType = MediaType.APPLICATION_JSON
                    }
                    val request = HttpEntity(mapOf(
                        "value" to "concurrent_$i",
                        "type" to "STRING"
                    ), headers)
                    
                    restTemplate.exchange(
                        url,
                        HttpMethod.PUT,
                        request,
                        String::class.java
                    )
                } catch (e: Exception) {
                    errors.add(e)
                } finally {
                    latch.countDown()
                }
            }
        }
        
        threads.forEach { it.start() }
        
        // Wait for all updates to complete
        assertThat(latch.await(10, TimeUnit.SECONDS)).isTrue()
        
        // Then - Verify no errors and final value is set
        assertThat(errors).isEmpty()
        
        delay(1000) // Wait for final synchronization
        
        val finalValue = settingsService.getSetting(settingKey)
        assertThat(finalValue).isNotNull()
        assertThat(finalValue).startsWith("concurrent_")
    }
    
    @Test
    fun `should maintain cache consistency after Redis restart`() = runTest {
        // Given - Create settings
        val settings = mapOf(
            "cache.test1" to "value1",
            "cache.test2" to "value2"
        )
        
        settings.forEach { (key, value) ->
            settingsService.setSetting(key, value, SettingType.STRING)
        }
        
        // When - Clear Redis cache (simulate restart)
        redisTemplate.connectionFactory?.connection?.serverCommands()?.flushAll()
        
        // Wait a moment
        delay(500)
        
        // Then - Settings should still be available from database
        settings.forEach { (key, expectedValue) ->
            val value = settingsService.getSetting(key)
            assertThat(value).isEqualTo(expectedValue)
        }
        
        // And cache should be repopulated
        val cacheKey = "tia:settings:cache.test1"
        val cachedValue = redisTemplate.opsForValue().get(cacheKey)
        assertThat(cachedValue).isEqualTo("value1")
    }
}