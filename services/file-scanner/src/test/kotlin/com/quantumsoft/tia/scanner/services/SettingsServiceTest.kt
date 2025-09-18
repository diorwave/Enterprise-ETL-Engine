package com.quantumsoft.tia.scanner.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.quantumsoft.tia.scanner.entities.SystemSettings
import com.quantumsoft.tia.scanner.entities.SettingType
import com.quantumsoft.tia.scanner.repositories.SystemSettingsRepository
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ValueOperations
import java.time.Duration
import java.time.Instant
import java.util.*

@ExtendWith(MockKExtension::class)
@DisplayName("SettingsService Tests")
class SettingsServiceTest {

    @MockK
    private lateinit var repository: SystemSettingsRepository

    @MockK
    private lateinit var redisTemplate: RedisTemplate<String, String>

    @MockK
    private lateinit var valueOperations: ValueOperations<String, String>

    @MockK
    private lateinit var objectMapper: ObjectMapper

    @MockK
    private lateinit var settingsChangeNotifier: SettingsChangeNotifier

    @InjectMockKs
    private lateinit var service: SettingsServiceImpl

    @BeforeEach
    fun setUp() {
        clearAllMocks()
        every { redisTemplate.opsForValue() } returns valueOperations
    }

    @Test
    fun `should get setting from cache when available`() = runTest {
        // Given
        val key = "test.setting"
        val cachedValue = "cached-value"
        every { valueOperations.get("settings:$key") } returns cachedValue

        // When
        val result = service.getSetting(key)

        // Then
        assertThat(result).isEqualTo(cachedValue)
        verify(exactly = 0) { repository.findBySettingKey(any()) }
    }

    @Test
    fun `should get setting from database when not cached`() = runTest {
        // Given
        val key = "test.setting"
        val dbValue = "db-value"
        val settings = SystemSettings(
            settingKey = key,
            settingValue = dbValue,
            valueType = SettingType.STRING
        )
        
        every { valueOperations.get("settings:$key") } returns null
        every { repository.findBySettingKey(key) } returns Optional.of(settings)
        every { valueOperations.set("settings:$key", dbValue, Duration.ofMinutes(5)) } answers { nothing }

        // When
        val result = service.getSetting(key)

        // Then
        assertThat(result).isEqualTo(dbValue)
        verify { repository.findBySettingKey(key) }
        verify { valueOperations.set("settings:$key", dbValue, Duration.ofMinutes(5)) }
    }

    @Test
    fun `should return null when setting not found`() = runTest {
        // Given
        val key = "non.existent"
        every { valueOperations.get("settings:$key") } returns null
        every { repository.findBySettingKey(key) } returns Optional.empty()

        // When
        val result = service.getSetting(key)

        // Then
        assertThat(result).isNull()
    }

    @Test
    fun `should get setting as Integer`() = runTest {
        // Given
        val key = "max.retries"
        val value = "5"
        every { valueOperations.get("settings:$key") } returns value

        // When
        val result = service.getSettingAs(key, Int::class.java)

        // Then
        assertThat(result).isEqualTo(5)
    }

    @Test
    fun `should get setting as Boolean`() = runTest {
        // Given
        val key = "feature.enabled"
        val value = "true"
        every { valueOperations.get("settings:$key") } returns value

        // When
        val result = service.getSettingAs(key, Boolean::class.java)

        // Then
        assertThat(result).isEqualTo(true)
    }

    @Test
    fun `should get setting as Long`() = runTest {
        // Given
        val key = "max.file.size"
        val value = "10000000000"
        every { valueOperations.get("settings:$key") } returns value

        // When
        val result = service.getSettingAs(key, Long::class.java)

        // Then
        assertThat(result).isEqualTo(10000000000L)
    }

    @Test
    fun `should get setting as JSON object`() = runTest {
        // Given
        val key = "config.json"
        val jsonValue = """{"threshold": 5000, "enabled": true}"""
        val expectedObject = mapOf("threshold" to 5000, "enabled" to true)
        
        every { valueOperations.get("settings:$key") } returns jsonValue
        every { objectMapper.readValue(jsonValue, Map::class.java) } returns expectedObject

        // When
        val result = service.getSettingAs(key, Map::class.java)

        // Then
        assertThat(result).isEqualTo(expectedObject)
    }

    @Test
    fun `should create new setting`() = runTest {
        // Given
        val key = "new.setting"
        val value = "new-value"
        val type = SettingType.STRING
        val newSettings = SystemSettings(
            settingKey = key,
            settingValue = value,
            valueType = type
        )
        
        every { repository.findBySettingKey(key) } returns Optional.empty()
        every { repository.save(any()) } returns newSettings
        every { redisTemplate.delete("settings:$key") } returns true
        coEvery { settingsChangeNotifier.notifyChange(key, value) } answers { nothing }

        // When
        service.setSetting(key, value, type)

        // Then
        verify { repository.save(match { 
            it.settingKey == key && it.settingValue == value && it.valueType == type 
        }) }
        verify { redisTemplate.delete("settings:$key") }
        coVerify { settingsChangeNotifier.notifyChange(key, value) }
    }

    @Test
    fun `should update existing setting`() = runTest {
        // Given
        val key = "existing.setting"
        val oldValue = "old-value"
        val newValue = "new-value"
        val type = SettingType.STRING
        val existingSettings = SystemSettings(
            id = UUID.randomUUID(),
            settingKey = key,
            settingValue = oldValue,
            valueType = type
        )
        val updatedSettings = existingSettings.copy(settingValue = newValue)
        
        every { repository.findBySettingKey(key) } returns Optional.of(existingSettings)
        every { repository.save(any()) } returns updatedSettings
        every { redisTemplate.delete("settings:$key") } returns true
        coEvery { settingsChangeNotifier.notifyChange(key, newValue) } answers { nothing }

        // When
        service.setSetting(key, newValue, type)

        // Then
        verify { repository.save(match { 
            it.id == existingSettings.id && it.settingValue == newValue 
        }) }
        verify { redisTemplate.delete("settings:$key") }
        coVerify { settingsChangeNotifier.notifyChange(key, newValue) }
    }

    @Test
    fun `should delete setting`() = runTest {
        // Given
        val key = "delete.me"
        every { repository.deleteBySettingKey(key) } answers { nothing }
        every { redisTemplate.delete("settings:$key") } returns true
        coEvery { settingsChangeNotifier.notifyChange(key, "") } answers { nothing }

        // When
        service.deleteSetting(key)

        // Then
        verify { repository.deleteBySettingKey(key) }
        verify { redisTemplate.delete("settings:$key") }
        coVerify { settingsChangeNotifier.notifyChange(key, "") }
    }

    @Test
    fun `should get all settings`() = runTest {
        // Given
        val settings = listOf(
            SystemSettings(
                settingKey = "key1",
                settingValue = "value1",
                valueType = SettingType.STRING
            ),
            SystemSettings(
                settingKey = "key2",
                settingValue = "value2",
                valueType = SettingType.STRING
            )
        )
        every { repository.findAll() } returns settings

        // When
        val result = service.getAllSettings()

        // Then
        assertThat(result).containsExactlyInAnyOrderEntriesOf(
            mapOf("key1" to "value1", "key2" to "value2")
        )
    }

    @Test
    fun `should handle integer value conversion`() = runTest {
        // Given
        val key = "threshold"
        val value = 5000
        val type = SettingType.INTEGER
        
        every { repository.findBySettingKey(key) } returns Optional.empty()
        every { repository.save(any()) } answers { firstArg() }
        every { redisTemplate.delete("settings:$key") } returns true
        coEvery { settingsChangeNotifier.notifyChange(key, "5000") } answers { nothing }

        // When
        service.setSetting(key, value, type)

        // Then
        verify { repository.save(match { 
            it.settingValue == "5000" && it.valueType == SettingType.INTEGER 
        }) }
    }

    @Test
    fun `should handle boolean value conversion`() = runTest {
        // Given
        val key = "enabled"
        val value = true
        val type = SettingType.BOOLEAN
        
        every { repository.findBySettingKey(key) } returns Optional.empty()
        every { repository.save(any()) } answers { firstArg() }
        every { redisTemplate.delete("settings:$key") } returns true
        coEvery { settingsChangeNotifier.notifyChange(key, "true") } answers { nothing }

        // When
        service.setSetting(key, value, type)

        // Then
        verify { repository.save(match { 
            it.settingValue == "true" && it.valueType == SettingType.BOOLEAN 
        }) }
    }

    @Test
    fun `should handle JSON object conversion`() = runTest {
        // Given
        val key = "config"
        val value = mapOf("threshold" to 5000, "enabled" to true)
        val jsonString = """{"threshold":5000,"enabled":true}"""
        val type = SettingType.JSON
        
        every { objectMapper.writeValueAsString(value) } returns jsonString
        every { repository.findBySettingKey(key) } returns Optional.empty()
        every { repository.save(any()) } answers { firstArg() }
        every { redisTemplate.delete("settings:$key") } returns true
        coEvery { settingsChangeNotifier.notifyChange(key, jsonString) } answers { nothing }

        // When
        service.setSetting(key, value, type)

        // Then
        verify { repository.save(match { 
            it.settingValue == jsonString && it.valueType == SettingType.JSON 
        }) }
    }

    @Test
    fun `should throw exception for invalid type conversion`() = runTest {
        // Given
        val key = "invalid"
        val value = "not-a-number"
        every { valueOperations.get("settings:$key") } returns value

        // When & Then
        assertThrows<NumberFormatException> {
            service.getSettingAs(key, Int::class.java)
        }
    }

    @Test
    fun `should refresh cache when setting is updated`() = runTest {
        // Given
        val key = "refresh.test"
        val value = "new-value"
        
        every { redisTemplate.delete("settings:$key") } returns true
        every { valueOperations.set("settings:$key", value, Duration.ofMinutes(5)) } answers { nothing }

        // When
        service.refreshCache(key, value)

        // Then
        verify { redisTemplate.delete("settings:$key") }
        verify { valueOperations.set("settings:$key", value, Duration.ofMinutes(5)) }
    }

    @Test
    fun `should invalidate cache for key`() = runTest {
        // Given
        val key = "invalidate.test"
        every { redisTemplate.delete("settings:$key") } returns true

        // When
        service.invalidateCache(key)

        // Then
        verify { redisTemplate.delete("settings:$key") }
    }

    @Test
    fun `should get multiple settings by keys`() = runTest {
        // Given
        val keys = listOf("key1", "key2", "key3")
        val settings = listOf(
            SystemSettings(
                settingKey = "key1",
                settingValue = "value1",
                valueType = SettingType.STRING
            ),
            SystemSettings(
                settingKey = "key2",
                settingValue = "value2",
                valueType = SettingType.STRING
            )
        )
        
        every { repository.findBySettingKeyIn(keys) } returns settings

        // When
        val result = service.getSettings(keys)

        // Then
        assertThat(result).hasSize(2)
        assertThat(result).containsExactlyInAnyOrderEntriesOf(
            mapOf("key1" to "value1", "key2" to "value2")
        )
    }
}