package com.quantumsoft.tia.scanner.validators

import com.quantumsoft.tia.scanner.entities.SettingType
import com.quantumsoft.tia.scanner.exceptions.ThresholdExceededException
import com.quantumsoft.tia.scanner.services.SettingsService
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
@DisplayName("FileThresholdValidator Tests")
class FileThresholdValidatorTest {

    @MockK
    private lateinit var settingsService: SettingsService

    private lateinit var validator: FileThresholdValidator

    @BeforeEach
    fun setUp() {
        clearAllMocks()
        validator = FileThresholdValidator(settingsService)
    }

    @Test
    fun `should allow enqueue when queue size is below threshold`() = runTest {
        // Given
        val currentQueueSize = 100
        val threshold = 500
        coEvery { settingsService.getSettingAs("scanner.file.threshold", Int::class.java) } returns threshold

        // When
        val canEnqueue = validator.canEnqueueFile(currentQueueSize)

        // Then
        assertThat(canEnqueue).isTrue()
    }

    @Test
    fun `should reject enqueue when queue size equals threshold`() = runTest {
        // Given
        val currentQueueSize = 500
        val threshold = 500
        coEvery { settingsService.getSettingAs("scanner.file.threshold", Int::class.java) } returns threshold

        // When
        val canEnqueue = validator.canEnqueueFile(currentQueueSize)

        // Then
        assertThat(canEnqueue).isFalse()
    }

    @Test
    fun `should reject enqueue when queue size exceeds threshold`() = runTest {
        // Given
        val currentQueueSize = 600
        val threshold = 500
        coEvery { settingsService.getSettingAs("scanner.file.threshold", Int::class.java) } returns threshold

        // When
        val canEnqueue = validator.canEnqueueFile(currentQueueSize)

        // Then
        assertThat(canEnqueue).isFalse()
    }

    @Test
    fun `should use default threshold when setting not found`() = runTest {
        // Given
        val currentQueueSize = 9000
        coEvery { settingsService.getSettingAs("scanner.file.threshold", Int::class.java) } returns null

        // When
        val canEnqueue = validator.canEnqueueFile(currentQueueSize)

        // Then
        assertThat(canEnqueue).isTrue() // Default is 10000
    }

    @Test
    fun `should get current threshold value`() = runTest {
        // Given
        val threshold = 5000
        coEvery { settingsService.getSettingAs("scanner.file.threshold", Int::class.java) } returns threshold

        // When
        val result = validator.getThreshold()

        // Then
        assertThat(result).isEqualTo(threshold)
    }

    @Test
    fun `should get default threshold when not configured`() = runTest {
        // Given
        coEvery { settingsService.getSettingAs("scanner.file.threshold", Int::class.java) } returns null

        // When
        val result = validator.getThreshold()

        // Then
        assertThat(result).isEqualTo(FileThresholdValidator.DEFAULT_THRESHOLD)
    }

    @Test
    fun `should update threshold setting`() = runTest {
        // Given
        val newThreshold = 7500
        coEvery { settingsService.setSetting("scanner.file.threshold", newThreshold, SettingType.INTEGER) } just Runs

        // When
        validator.setThreshold(newThreshold)

        // Then
        coVerify { settingsService.setSetting("scanner.file.threshold", newThreshold, SettingType.INTEGER) }
    }

    @Test
    fun `should validate threshold before enqueue`() = runTest {
        // Given
        val currentQueueSize = 100
        val threshold = 500
        coEvery { settingsService.getSettingAs("scanner.file.threshold", Int::class.java) } returns threshold

        // When
        validator.validateThreshold(currentQueueSize)

        // Then - Should not throw
    }

    @Test
    fun `should throw exception when threshold exceeded`() = runTest {
        // Given
        val currentQueueSize = 600
        val threshold = 500
        coEvery { settingsService.getSettingAs("scanner.file.threshold", Int::class.java) } returns threshold

        // When & Then
        val exception = assertThrows<ThresholdExceededException> {
            validator.validateThreshold(currentQueueSize)
        }
        assertThat(exception.message).contains("threshold")
        assertThat(exception.currentSize).isEqualTo(currentQueueSize)
        assertThat(exception.threshold).isEqualTo(threshold)
    }

    @Test
    fun `should calculate threshold utilization percentage`() = runTest {
        // Given
        val currentQueueSize = 750
        val threshold = 1000
        coEvery { settingsService.getSettingAs("scanner.file.threshold", Int::class.java) } returns threshold

        // When
        val utilization = validator.getThresholdUtilization(currentQueueSize)

        // Then
        assertThat(utilization).isEqualTo(75.0)
    }

    @Test
    fun `should handle zero threshold gracefully`() = runTest {
        // Given
        val currentQueueSize = 100
        coEvery { settingsService.getSettingAs("scanner.file.threshold", Int::class.java) } returns 0

        // When
        val utilization = validator.getThresholdUtilization(currentQueueSize)

        // Then
        assertThat(utilization).isEqualTo(100.0) // Max utilization
    }

    @Test
    fun `should check if threshold is enabled`() = runTest {
        // Given
        coEvery { settingsService.getSettingAs("scanner.file.threshold.enabled", Boolean::class.java) } returns true

        // When
        val enabled = validator.isThresholdEnabled()

        // Then
        assertThat(enabled).isTrue()
    }

    @Test
    fun `should default to enabled when setting not found`() = runTest {
        // Given
        coEvery { settingsService.getSettingAs("scanner.file.threshold.enabled", Boolean::class.java) } returns null

        // When
        val enabled = validator.isThresholdEnabled()

        // Then
        assertThat(enabled).isTrue() // Default is enabled
    }

    @Test
    fun `should get remaining capacity`() = runTest {
        // Given
        val currentQueueSize = 300
        val threshold = 1000
        coEvery { settingsService.getSettingAs("scanner.file.threshold", Int::class.java) } returns threshold

        // When
        val remaining = validator.getRemainingCapacity(currentQueueSize)

        // Then
        assertThat(remaining).isEqualTo(700)
    }

    @Test
    fun `should return zero when over capacity`() = runTest {
        // Given
        val currentQueueSize = 1200
        val threshold = 1000
        coEvery { settingsService.getSettingAs("scanner.file.threshold", Int::class.java) } returns threshold

        // When
        val remaining = validator.getRemainingCapacity(currentQueueSize)

        // Then
        assertThat(remaining).isEqualTo(0)
    }

    @Test
    fun `should check if near threshold`() = runTest {
        // Given
        val currentQueueSize = 920
        val threshold = 1000
        coEvery { settingsService.getSettingAs("scanner.file.threshold", Int::class.java) } returns threshold

        // When
        val nearThreshold = validator.isNearThreshold(currentQueueSize, 90.0)

        // Then
        assertThat(nearThreshold).isTrue() // 92% > 90%
    }

    @Test
    fun `should validate batch enqueue`() = runTest {
        // Given
        val currentQueueSize = 400
        val batchSize = 50
        val threshold = 500
        coEvery { settingsService.getSettingAs("scanner.file.threshold", Int::class.java) } returns threshold

        // When
        val canEnqueue = validator.canEnqueueBatch(currentQueueSize, batchSize)

        // Then
        assertThat(canEnqueue).isTrue() // 400 + 50 = 450 < 500
    }

    @Test
    fun `should reject batch that would exceed threshold`() = runTest {
        // Given
        val currentQueueSize = 460
        val batchSize = 50
        val threshold = 500
        coEvery { settingsService.getSettingAs("scanner.file.threshold", Int::class.java) } returns threshold

        // When
        val canEnqueue = validator.canEnqueueBatch(currentQueueSize, batchSize)

        // Then
        assertThat(canEnqueue).isFalse() // 460 + 50 = 510 > 500
    }
}