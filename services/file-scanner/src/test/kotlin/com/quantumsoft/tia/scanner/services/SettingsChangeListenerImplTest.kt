package com.quantumsoft.tia.scanner.services

import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.Logger

@ExtendWith(MockKExtension::class)
@DisplayName("SettingsChangeListener Tests")
class SettingsChangeListenerImplTest {

    @MockK
    private lateinit var settingsService: SettingsService

    @MockK
    private lateinit var settingsCache: SettingsCache

    @MockK
    private lateinit var logger: Logger

    private lateinit var listener: SettingsChangeListenerImpl

    @BeforeEach
    fun setUp() {
        clearAllMocks()
        listener = SettingsChangeListenerImpl(settingsService, settingsCache)
    }

    @Test
    fun `should handle setting update notification`() = runTest {
        // Given
        val key = "test.setting"
        val value = "new-value"
        
        coEvery { settingsCache.put(key, value) } just Runs
        coEvery { settingsService.refreshCache(key, value) } just Runs

        // When
        listener.onSettingChanged(key, value)

        // Then
        coVerify { settingsCache.put(key, value) }
        coVerify { settingsService.refreshCache(key, value) }
    }

    @Test
    fun `should handle setting deletion notification`() = runTest {
        // Given
        val key = "deleted.setting"
        val value = ""
        
        coEvery { settingsCache.remove(key) } just Runs
        coEvery { settingsService.invalidateCache(key) } just Runs

        // When
        listener.onSettingChanged(key, value)

        // Then
        coVerify { settingsCache.remove(key) }
        coVerify { settingsService.invalidateCache(key) }
    }

    @Test
    fun `should update local cache on setting change`() = runTest {
        // Given
        val key = "cache.update"
        val value = "cached-value"
        
        coEvery { settingsCache.put(key, value) } just Runs
        coEvery { settingsService.refreshCache(key, value) } just Runs

        // When
        listener.onSettingChanged(key, value)

        // Then
        coVerify { settingsCache.put(key, value) }
    }

    @Test
    fun `should handle batch setting updates`() = runTest {
        // Given
        val updates = mapOf(
            "setting1" to "value1",
            "setting2" to "value2",
            "setting3" to "value3"
        )
        
        coEvery { settingsCache.putAll(updates) } just Runs
        coEvery { settingsService.refreshCache(any(), any()) } just Runs

        // When
        listener.onBatchSettingsChanged(updates)

        // Then
        coVerify { settingsCache.putAll(updates) }
        updates.forEach { (key, value) ->
            coVerify { settingsService.refreshCache(key, value) }
        }
    }

    @Test
    fun `should register with notifier on initialization`() = runTest {
        // Given
        val notifier = mockk<SettingsChangeNotifier>()
        every { notifier.subscribeToChanges(any()) } just Runs
        
        // When
        val listenerWithNotifier = SettingsChangeListenerImpl(
            settingsService, 
            settingsCache, 
            notifier
        )
        listenerWithNotifier.init() // Call init explicitly in test

        // Then
        verify { notifier.subscribeToChanges(listenerWithNotifier) }
    }

    @Test
    fun `should handle exceptions gracefully`() = runTest {
        // Given
        val key = "error.setting"
        val value = "error-value"
        
        coEvery { settingsCache.put(key, value) } throws RuntimeException("Cache error")
        coEvery { settingsService.refreshCache(key, value) } just Runs

        // When & Then - Should not throw
        listener.onSettingChanged(key, value)
        
        // Service refresh should still be called
        coVerify { settingsService.refreshCache(key, value) }
    }

    @Test
    fun `should process special setting keys`() = runTest {
        // Given
        val thresholdKey = "scanner.file.threshold"
        val thresholdValue = "5000"
        
        coEvery { settingsCache.put(thresholdKey, thresholdValue) } just Runs
        coEvery { settingsService.refreshCache(thresholdKey, thresholdValue) } just Runs

        // When
        listener.onSettingChanged(thresholdKey, thresholdValue)

        // Then
        assertThat(listener.isThresholdKey(thresholdKey)).isTrue()
        coVerify { settingsCache.put(thresholdKey, thresholdValue) }
    }

    @Test
    fun `should identify threshold-related keys`() = runTest {
        // Given & When & Then
        assertThat(listener.isThresholdKey("scanner.file.threshold")).isTrue()
        assertThat(listener.isThresholdKey("scanner.file.threshold.max")).isTrue()
        assertThat(listener.isThresholdKey("scanner.other.setting")).isFalse()
        assertThat(listener.isThresholdKey("threshold.something")).isFalse()
    }

    @Test
    fun `should handle concurrent updates`() = runTest {
        // Given
        val updates = (1..10).map { "key$it" to "value$it" }
        
        coEvery { settingsCache.put(any(), any()) } just Runs
        coEvery { settingsService.refreshCache(any(), any()) } just Runs

        // When - Simulate concurrent updates
        val jobs = updates.map { (key, value) ->
            launch {
                listener.onSettingChanged(key, value)
            }
        }
        jobs.joinAll()

        // Then
        updates.forEach { (key, value) ->
            coVerify { settingsCache.put(key, value) }
            coVerify { settingsService.refreshCache(key, value) }
        }
    }

    @Test
    fun `should cleanup on shutdown`() = runTest {
        // Given
        val notifier = mockk<SettingsChangeNotifier>()
        every { notifier.subscribeToChanges(any()) } just Runs
        every { notifier.unsubscribeFromChanges(any()) } just Runs
        
        val listenerWithNotifier = SettingsChangeListenerImpl(
            settingsService, 
            settingsCache, 
            notifier
        )

        // When
        listenerWithNotifier.shutdown()

        // Then
        verify { notifier.unsubscribeFromChanges(listenerWithNotifier) }
    }
}
