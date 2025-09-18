package com.quantumsoft.tia.scanner.services

import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ListOperations
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import java.time.Instant

@ExtendWith(MockKExtension::class)
@DisplayName("SettingsChangeNotifier Tests")
class SettingsChangeNotifierTest {

    @MockK
    private lateinit var redisTemplate: RedisTemplate<String, String>

    @MockK
    private lateinit var listOperations: ListOperations<String, String>

    @MockK
    private lateinit var objectMapper: ObjectMapper

    @MockK
    private lateinit var messageListenerContainer: RedisMessageListenerContainer

    private lateinit var notifier: SettingsChangeNotifierImpl

    @BeforeEach
    fun setUp() {
        clearAllMocks()
        every { redisTemplate.opsForList() } returns listOperations
        
        // Initialize the notifier with mocked dependencies
        notifier = SettingsChangeNotifierImpl(redisTemplate, objectMapper, messageListenerContainer)
        
        // Set instanceId using reflection
        val instanceIdField = SettingsChangeNotifierImpl::class.java.getDeclaredField("instanceId")
        instanceIdField.isAccessible = true
        instanceIdField.set(notifier, "instance-1")
    }

    @Test
    fun `should publish setting change notification`() = runTest {
        // Given
        val key = "test.setting"
        val value = "test-value"
        val instanceId = "instance-1"
        val timestamp = Instant.now()
        
        val expectedMessage = mapOf(
            "action" to "UPDATE",
            "key" to key,
            "value" to value,
            "timestamp" to timestamp.toString(),
            "source" to instanceId
        )
        val jsonMessage = """{"action":"UPDATE","key":"$key","value":"$value"}"""
        
        every { objectMapper.writeValueAsString(any()) } returns jsonMessage
        every { redisTemplate.convertAndSend(any<String>(), any()) } returns 1L

        // When
        notifier.notifyChange(key, value)

        // Then
        verify { redisTemplate.convertAndSend("settings:changes", jsonMessage) }
    }

    @Test
    fun `should notify deletion when value is empty`() = runTest {
        // Given
        val key = "delete.setting"
        val value = ""
        val expectedAction = "DELETE"
        
        val jsonMessage = """{"action":"DELETE","key":"$key","value":""}"""
        every { objectMapper.writeValueAsString(any()) } returns jsonMessage
        every { redisTemplate.convertAndSend(any<String>(), any()) } returns 1L

        // When
        notifier.notifyChange(key, value)

        // Then
        verify { 
            objectMapper.writeValueAsString(any<SettingsChangeMessage>())
        }
        verify { redisTemplate.convertAndSend("settings:changes", jsonMessage) }
    }

    @Test
    fun `should subscribe listener to changes`() = runTest {
        // Given
        val listener = mockk<SettingsChangeListener>()
        
        // When
        notifier.subscribeToChanges(listener)

        // Then
        assertThat(notifier.getListeners()).contains(listener)
    }

    @Test
    fun `should unsubscribe listener from changes`() = runTest {
        // Given
        val listener = mockk<SettingsChangeListener>()
        notifier.subscribeToChanges(listener)

        // When
        notifier.unsubscribeFromChanges(listener)

        // Then
        assertThat(notifier.getListeners()).doesNotContain(listener)
    }

    @Test
    fun `should notify all subscribed listeners`() = runTest {
        // Given
        val key = "broadcast.setting"
        val value = "broadcast-value"
        val listener1 = mockk<SettingsChangeListener>()
        val listener2 = mockk<SettingsChangeListener>()
        
        coEvery { listener1.onSettingChanged(any(), any()) } answers { nothing }
        coEvery { listener2.onSettingChanged(any(), any()) } answers { nothing }
        every { objectMapper.writeValueAsString(any()) } returns "{}"
        every { redisTemplate.convertAndSend(any<String>(), any()) } returns 1L
        
        notifier.subscribeToChanges(listener1)
        notifier.subscribeToChanges(listener2)

        // When
        notifier.notifyChange(key, value)

        // Then
        coVerify { listener1.onSettingChanged(key, value) }
        coVerify { listener2.onSettingChanged(key, value) }
    }

    @Test
    fun `should handle listener exception gracefully`() = runTest {
        // Given
        val key = "error.setting"
        val value = "error-value"
        val failingListener = mockk<SettingsChangeListener>()
        val workingListener = mockk<SettingsChangeListener>()
        
        coEvery { failingListener.onSettingChanged(any(), any()) } throws RuntimeException("Listener error")
        coEvery { workingListener.onSettingChanged(any(), any()) } answers { nothing }
        every { objectMapper.writeValueAsString(any()) } returns "{}"
        every { redisTemplate.convertAndSend(any<String>(), any()) } returns 1L
        
        notifier.subscribeToChanges(failingListener)
        notifier.subscribeToChanges(workingListener)

        // When
        notifier.notifyChange(key, value)

        // Then
        coVerify { failingListener.onSettingChanged(key, value) }
        coVerify { workingListener.onSettingChanged(key, value) }
    }

    @Test
    fun `should process incoming Redis message`() = runTest {
        // Given
        val key = "incoming.setting"
        val value = "incoming-value"
        val listener = mockk<SettingsChangeListener>()
        val message = """{"action":"UPDATE","key":"$key","value":"$value","source":"other-instance"}"""
        
        coEvery { listener.onSettingChanged(any(), any()) } answers { nothing }
        every { 
            objectMapper.readValue(message, SettingsChangeMessage::class.java) 
        } returns SettingsChangeMessage(
            action = "UPDATE",
            key = key,
            value = value,
            timestamp = Instant.now().toString(),
            source = "other-instance"
        )
        
        notifier.subscribeToChanges(listener)

        // When
        notifier.handleMessage(message)

        // Then
        coVerify { listener.onSettingChanged(key, value) }
    }

    @Test
    fun `should ignore messages from same instance`() = runTest {
        // Given
        val key = "ignore.setting"
        val value = "ignore-value"
        val listener = mockk<SettingsChangeListener>()
        val message = """{"action":"UPDATE","key":"$key","value":"$value","source":"instance-1"}"""
        
        coEvery { listener.onSettingChanged(any(), any()) } answers { nothing }
        every { 
            objectMapper.readValue(message, SettingsChangeMessage::class.java) 
        } returns SettingsChangeMessage(
            action = "UPDATE",
            key = key,
            value = value,
            timestamp = Instant.now().toString(),
            source = "instance-1" // Same as current instance
        )
        
        notifier.subscribeToChanges(listener)

        // When
        notifier.handleMessage(message)

        // Then
        coVerify(exactly = 0) { listener.onSettingChanged(any(), any()) }
    }

    @Test
    fun `should request full sync`() = runTest {
        // Given
        val syncMessage = """{"action":"SYNC_REQUEST","source":"instance-1"}"""
        every { objectMapper.writeValueAsString(any()) } returns syncMessage
        every { redisTemplate.convertAndSend(any<String>(), any()) } returns 1L

        // When
        notifier.requestFullSync()

        // Then
        verify { redisTemplate.convertAndSend("settings:sync", syncMessage) }
    }

    @Test
    fun `should handle sync response`() = runTest {
        // Given
        val settings = mapOf(
            "setting1" to "value1",
            "setting2" to "value2"
        )
        val listener = mockk<SettingsChangeListener>()
        val jsonSettings = """{"setting1":"value1","setting2":"value2"}"""
        val syncMessage = """{"action":"SYNC_RESPONSE","settings":$jsonSettings}"""
        
        coEvery { listener.onSettingChanged(any(), any()) } answers { nothing }
        every { objectMapper.writeValueAsString(settings) } returns jsonSettings
        every { 
            objectMapper.readValue(syncMessage, SyncResponseMessage::class.java) 
        } returns SyncResponseMessage(
            action = "SYNC_RESPONSE",
            settings = settings,
            source = "other-instance"
        )
        
        notifier.subscribeToChanges(listener)

        // When
        notifier.handleSyncResponse(syncMessage)

        // Then
        coVerify { listener.onSettingChanged("setting1", "value1") }
        coVerify { listener.onSettingChanged("setting2", "value2") }
    }

    @Test
    fun `should not notify unsubscribed listeners`() = runTest {
        // Given
        val key = "unsubscribed.setting"
        val value = "unsubscribed-value"
        val listener = mockk<SettingsChangeListener>()
        
        coEvery { listener.onSettingChanged(any(), any()) } answers { nothing }
        every { objectMapper.writeValueAsString(any()) } returns "{}"
        every { redisTemplate.convertAndSend(any<String>(), any()) } returns 1L
        
        notifier.subscribeToChanges(listener)
        notifier.unsubscribeFromChanges(listener)

        // When
        notifier.notifyChange(key, value)

        // Then
        coVerify(exactly = 0) { listener.onSettingChanged(any(), any()) }
    }
}

data class SettingsChangeMessage(
    val action: String,
    val key: String,
    val value: String,
    val timestamp: String,
    val source: String
)

data class SyncResponseMessage(
    val action: String,
    val settings: Map<String, String>,
    val source: String
)