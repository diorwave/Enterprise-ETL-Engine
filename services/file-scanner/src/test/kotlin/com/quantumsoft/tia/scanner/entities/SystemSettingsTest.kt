package com.quantumsoft.tia.scanner.entities

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import java.time.Instant
import java.util.UUID

@DisplayName("SystemSettings Entity Tests")
class SystemSettingsTest {

    @Test
    fun `should create SystemSettings with all fields`() {
        // Given
        val id = UUID.randomUUID()
        val settingKey = "scanner.file.threshold"
        val settingValue = "5000"
        val valueType = SettingType.INTEGER
        val description = "Maximum files in queue"
        val modifiedBy = "admin"
        val lastModified = Instant.now()

        // When
        val settings = SystemSettings(
            id = id,
            settingKey = settingKey,
            settingValue = settingValue,
            valueType = valueType,
            description = description,
            lastModified = lastModified,
            modifiedBy = modifiedBy,
            version = 1L
        )

        // Then
        assertThat(settings.id).isEqualTo(id)
        assertThat(settings.settingKey).isEqualTo(settingKey)
        assertThat(settings.settingValue).isEqualTo(settingValue)
        assertThat(settings.valueType).isEqualTo(valueType)
        assertThat(settings.description).isEqualTo(description)
        assertThat(settings.lastModified).isEqualTo(lastModified)
        assertThat(settings.modifiedBy).isEqualTo(modifiedBy)
        assertThat(settings.version).isEqualTo(1L)
    }

    @Test
    fun `should create SystemSettings with default values`() {
        // Given
        val settingKey = "test.setting"
        val settingValue = "test-value"
        val valueType = SettingType.STRING

        // When
        val settings = SystemSettings(
            settingKey = settingKey,
            settingValue = settingValue,
            valueType = valueType
        )

        // Then
        assertThat(settings.id).isNotNull()
        assertThat(settings.settingKey).isEqualTo(settingKey)
        assertThat(settings.settingValue).isEqualTo(settingValue)
        assertThat(settings.valueType).isEqualTo(valueType)
        assertThat(settings.description).isNull()
        assertThat(settings.lastModified).isNotNull()
        assertThat(settings.modifiedBy).isNull()
        assertThat(settings.version).isEqualTo(0L)
    }

    @Test
    fun `should support all SettingType values`() {
        // Given & When & Then
        assertThat(SettingType.values()).containsExactly(
            SettingType.STRING,
            SettingType.INTEGER,
            SettingType.LONG,
            SettingType.BOOLEAN,
            SettingType.JSON
        )
    }

    @Test
    fun `should handle JSON value type`() {
        // Given
        val jsonValue = """{"threshold": 5000, "enabled": true}"""
        
        // When
        val settings = SystemSettings(
            settingKey = "scanner.config",
            settingValue = jsonValue,
            valueType = SettingType.JSON
        )

        // Then
        assertThat(settings.settingValue).isEqualTo(jsonValue)
        assertThat(settings.valueType).isEqualTo(SettingType.JSON)
    }

    @Test
    fun `should handle boolean value type`() {
        // Given & When
        val settings = SystemSettings(
            settingKey = "feature.enabled",
            settingValue = "true",
            valueType = SettingType.BOOLEAN
        )

        // Then
        assertThat(settings.settingValue).isEqualTo("true")
        assertThat(settings.valueType).isEqualTo(SettingType.BOOLEAN)
    }

    @Test
    fun `should handle integer value type`() {
        // Given & When
        val settings = SystemSettings(
            settingKey = "max.retries",
            settingValue = "3",
            valueType = SettingType.INTEGER
        )

        // Then
        assertThat(settings.settingValue).isEqualTo("3")
        assertThat(settings.valueType).isEqualTo(SettingType.INTEGER)
    }

    @Test
    fun `should handle long value type`() {
        // Given & When
        val settings = SystemSettings(
            settingKey = "max.file.size",
            settingValue = "10000000000",
            valueType = SettingType.LONG
        )

        // Then
        assertThat(settings.settingValue).isEqualTo("10000000000")
        assertThat(settings.valueType).isEqualTo(SettingType.LONG)
    }

    @Test
    fun `should be equal when all fields are same`() {
        // Given
        val id = UUID.randomUUID()
        val lastModified = Instant.now()
        val settings1 = SystemSettings(
            id = id,
            settingKey = "key1",
            settingValue = "value1",
            valueType = SettingType.STRING,
            lastModified = lastModified
        )
        val settings2 = SystemSettings(
            id = id,
            settingKey = "key1",
            settingValue = "value1",
            valueType = SettingType.STRING,
            lastModified = lastModified
        )

        // When & Then
        assertThat(settings1).isEqualTo(settings2)
        assertThat(settings1.hashCode()).isEqualTo(settings2.hashCode())
    }

    @Test
    fun `should not be equal when id is different`() {
        // Given
        val settings1 = SystemSettings(
            settingKey = "key",
            settingValue = "value",
            valueType = SettingType.STRING
        )
        val settings2 = SystemSettings(
            settingKey = "key",
            settingValue = "value",
            valueType = SettingType.STRING
        )

        // When & Then
        assertThat(settings1).isNotEqualTo(settings2)
        assertThat(settings1.hashCode()).isNotEqualTo(settings2.hashCode())
    }

    @Test
    fun `should handle null description`() {
        // Given & When
        val settings = SystemSettings(
            settingKey = "test.key",
            settingValue = "test.value",
            valueType = SettingType.STRING,
            description = null
        )

        // Then
        assertThat(settings.description).isNull()
    }

    @Test
    fun `should handle null modifiedBy`() {
        // Given & When
        val settings = SystemSettings(
            settingKey = "test.key",
            settingValue = "test.value",
            valueType = SettingType.STRING,
            modifiedBy = null
        )

        // Then
        assertThat(settings.modifiedBy).isNull()
    }
}
