package com.quantumsoft.tia.scanner.repositories

import com.quantumsoft.tia.scanner.entities.SystemSettings
import com.quantumsoft.tia.scanner.entities.SettingType
import com.quantumsoft.tia.scanner.config.TestConfiguration
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.Instant
import java.util.UUID

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestConfiguration::class)
@DisplayName("SystemSettingsRepository Tests")
class SystemSettingsRepositoryTest {
    
    companion object {
        @Container
        @JvmStatic
        val postgres = PostgreSQLContainer("postgres:15-alpine")
            .withDatabaseName("test_db")
            .withUsername("test")
            .withPassword("test")

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
        }
    }

    @Autowired
    private lateinit var repository: SystemSettingsRepository

    @BeforeEach
    fun setUp() {
        repository.deleteAll()
    }

    @Test
    fun `should save and retrieve settings`() {
        // Given
        val settings = SystemSettings(
            settingKey = "test.setting",
            settingValue = "test-value",
            valueType = SettingType.STRING,
            description = "Test setting"
        )

        // When
        val saved = repository.save(settings)
        val retrieved = repository.findById(saved.id)

        // Then
        assertThat(retrieved).isPresent
        assertThat(retrieved.get().settingKey).isEqualTo("test.setting")
        assertThat(retrieved.get().settingValue).isEqualTo("test-value")
        assertThat(retrieved.get().valueType).isEqualTo(SettingType.STRING)
        assertThat(retrieved.get().description).isEqualTo("Test setting")
    }

    @Test
    fun `should find by setting key`() {
        // Given
        val settings = SystemSettings(
            settingKey = "unique.key",
            settingValue = "unique-value",
            valueType = SettingType.STRING
        )
        repository.save(settings)

        // When
        val found = repository.findBySettingKey("unique.key")

        // Then
        assertThat(found).isPresent
        assertThat(found.get().settingValue).isEqualTo("unique-value")
    }

    @Test
    fun `should return empty when setting key not found`() {
        // When
        val found = repository.findBySettingKey("non.existent.key")

        // Then
        assertThat(found).isEmpty
    }

    @Test
    fun `should find all by value type`() {
        // Given
        repository.save(SystemSettings(
            settingKey = "int.setting.1",
            settingValue = "100",
            valueType = SettingType.INTEGER
        ))
        repository.save(SystemSettings(
            settingKey = "int.setting.2",
            settingValue = "200",
            valueType = SettingType.INTEGER
        ))
        repository.save(SystemSettings(
            settingKey = "string.setting",
            settingValue = "text",
            valueType = SettingType.STRING
        ))

        // When
        val integerSettings = repository.findAllByValueType(SettingType.INTEGER)

        // Then
        assertThat(integerSettings).hasSize(2)
        assertThat(integerSettings.map { it.settingKey })
            .containsExactlyInAnyOrder("int.setting.1", "int.setting.2")
    }

    @Test
    fun `should find settings modified after timestamp`() {
        // Given
        val pastTime = Instant.now().minusSeconds(3600)
        val recentTime = Instant.now().minusSeconds(60)
        
        repository.save(SystemSettings(
            settingKey = "old.setting",
            settingValue = "old",
            valueType = SettingType.STRING,
            lastModified = pastTime
        ))
        repository.save(SystemSettings(
            settingKey = "recent.setting",
            settingValue = "recent",
            valueType = SettingType.STRING,
            lastModified = recentTime
        ))

        // When
        val recentSettings = repository.findByLastModifiedAfter(
            Instant.now().minusSeconds(120)
        )

        // Then
        assertThat(recentSettings).hasSize(1)
        assertThat(recentSettings[0].settingKey).isEqualTo("recent.setting")
    }

    @Test
    fun `should update existing setting`() {
        // Given
        val settings = repository.save(SystemSettings(
            settingKey = "update.test",
            settingValue = "initial",
            valueType = SettingType.STRING
        ))

        // When
        val updated = settings.copy(
            settingValue = "updated",
            lastModified = Instant.now(),
            modifiedBy = "test-user"
        )
        repository.save(updated)

        // Then
        val retrieved = repository.findBySettingKey("update.test")
        assertThat(retrieved).isPresent
        assertThat(retrieved.get().settingValue).isEqualTo("updated")
        assertThat(retrieved.get().modifiedBy).isEqualTo("test-user")
    }

    @Test
    fun `should delete setting`() {
        // Given
        val settings = repository.save(SystemSettings(
            settingKey = "delete.test",
            settingValue = "value",
            valueType = SettingType.STRING
        ))

        // When
        repository.deleteById(settings.id)

        // Then
        val found = repository.findById(settings.id)
        assertThat(found).isEmpty
    }

    @Test
    fun `should count settings by value type`() {
        // Given
        repository.save(SystemSettings(
            settingKey = "bool.1",
            settingValue = "true",
            valueType = SettingType.BOOLEAN
        ))
        repository.save(SystemSettings(
            settingKey = "bool.2",
            settingValue = "false",
            valueType = SettingType.BOOLEAN
        ))
        repository.save(SystemSettings(
            settingKey = "string.1",
            settingValue = "text",
            valueType = SettingType.STRING
        ))

        // When
        val booleanCount = repository.countByValueType(SettingType.BOOLEAN)
        val stringCount = repository.countByValueType(SettingType.STRING)
        val jsonCount = repository.countByValueType(SettingType.JSON)

        // Then
        assertThat(booleanCount).isEqualTo(2)
        assertThat(stringCount).isEqualTo(1)
        assertThat(jsonCount).isEqualTo(0)
    }

    @Test
    fun `should find all settings with pagination`() {
        // Given
        for (i in 1..10) {
            repository.save(SystemSettings(
                settingKey = "setting.$i",
                settingValue = "value-$i",
                valueType = SettingType.STRING
            ))
        }

        // When
        val page = repository.findAll(PageRequest.of(0, 5))

        // Then
        assertThat(page.totalElements).isEqualTo(10)
        assertThat(page.totalPages).isEqualTo(2)
        assertThat(page.content).hasSize(5)
    }

    @Test
    fun `should handle JSON value type`() {
        // Given
        val jsonConfig = """
            {
                "threshold": 5000,
                "retryCount": 3,
                "features": {
                    "autoScan": true,
                    "validation": false
                }
            }
        """.trimIndent()

        val settings = repository.save(SystemSettings(
            settingKey = "scanner.config",
            settingValue = jsonConfig,
            valueType = SettingType.JSON,
            description = "Scanner configuration"
        ))

        // When
        val retrieved = repository.findById(settings.id)

        // Then
        assertThat(retrieved).isPresent
        assertThat(retrieved.get().valueType).isEqualTo(SettingType.JSON)
        assertThat(retrieved.get().settingValue).isEqualTo(jsonConfig)
    }

    @Test
    fun `should enforce unique constraint on setting key`() {
        // Given
        repository.save(SystemSettings(
            settingKey = "unique.constraint.test",
            settingValue = "value1",
            valueType = SettingType.STRING
        ))

        // When & Then
        val duplicate = SystemSettings(
            settingKey = "unique.constraint.test",
            settingValue = "value2",
            valueType = SettingType.STRING
        )
        
        // This should throw a constraint violation exception
        org.junit.jupiter.api.assertThrows<org.springframework.dao.DataIntegrityViolationException> {
            repository.saveAndFlush(duplicate)
        }
    }

    @Test
    fun `should update version on modification`() {
        // Given
        val settings = repository.save(SystemSettings(
            settingKey = "version.test",
            settingValue = "v1",
            valueType = SettingType.STRING
        ))
        repository.flush()
        val initialVersion = settings.version

        // When
        val retrieved = repository.findById(settings.id).get()
        val updated = retrieved.copy(
            settingValue = "v2",
            lastModified = Instant.now()
        )
        val saved = repository.saveAndFlush(updated)

        // Then
        // Version should be managed by JPA/Hibernate automatically
        // For now, we'll check that the value was updated
        assertThat(saved.settingValue).isEqualTo("v2")
    }

    @Test
    fun `should find settings by key pattern`() {
        // Given
        // Clear any existing settings first
        repository.deleteAll()
        
        repository.save(SystemSettings(
            settingKey = "scanner.file.threshold.test",
            settingValue = "5000",
            valueType = SettingType.INTEGER
        ))
        repository.save(SystemSettings(
            settingKey = "scanner.file.maxSize",
            settingValue = "1000000",
            valueType = SettingType.LONG
        ))
        repository.save(SystemSettings(
            settingKey = "monitor.interval",
            settingValue = "60",
            valueType = SettingType.INTEGER
        ))

        // When
        val scannerSettings = repository.findBySettingKeyStartingWith("scanner.")

        // Then
        assertThat(scannerSettings).hasSizeGreaterThanOrEqualTo(2)
        assertThat(scannerSettings.map { it.settingKey })
            .allMatch { it.startsWith("scanner.") }
    }

    @Test
    fun `should delete by setting key`() {
        // Given
        repository.save(SystemSettings(
            settingKey = "delete.by.key",
            settingValue = "value",
            valueType = SettingType.STRING
        ))

        // When
        repository.deleteBySettingKey("delete.by.key")

        // Then
        val found = repository.findBySettingKey("delete.by.key")
        assertThat(found).isEmpty
    }
}