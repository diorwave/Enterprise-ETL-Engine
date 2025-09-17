package com.quantumsoft.tia.scanner.services

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.connection.Message
import org.springframework.data.redis.connection.MessageListener
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.concurrent.CopyOnWriteArrayList
import jakarta.annotation.PostConstruct

@Component
class SettingsChangeNotifierImpl(
    private val redisTemplate: RedisTemplate<String, String>,
    private val objectMapper: ObjectMapper,
    private val messageListenerContainer: RedisMessageListenerContainer
) : SettingsChangeNotifier, MessageListener {

    private val logger = LoggerFactory.getLogger(SettingsChangeNotifierImpl::class.java)
    private val listeners = CopyOnWriteArrayList<SettingsChangeListener>()
    
    @Value("\${spring.application.instance-id:instance-1}")
    private lateinit var instanceId: String
    
    companion object {
        private const val SETTINGS_CHANGE_CHANNEL = "settings:changes"
        private const val SETTINGS_SYNC_CHANNEL = "settings:sync"
    }
    
    @PostConstruct
    fun init() {
        // Subscribe to Redis channels for settings changes
        messageListenerContainer.addMessageListener(this, ChannelTopic(SETTINGS_CHANGE_CHANNEL))
        messageListenerContainer.addMessageListener(this, ChannelTopic(SETTINGS_SYNC_CHANNEL))
        logger.info("Subscribed to Redis settings channels")
    }

    override suspend fun notifyChange(key: String, value: String) = withContext(Dispatchers.IO) {
        val action = if (value.isEmpty()) "DELETE" else "UPDATE"
        val message = SettingsChangeMessage(
            action = action,
            key = key,
            value = value,
            timestamp = Instant.now().toString(),
            source = instanceId
        )
        
        val jsonMessage = objectMapper.writeValueAsString(message)
        
        // Publish to Redis channel
        redisTemplate.convertAndSend(SETTINGS_CHANGE_CHANNEL, jsonMessage)
        logger.debug("Published setting change: $key = $value")
        
        // Notify local listeners
        notifyLocalListeners(key, value)
    }

    override fun subscribeToChanges(listener: SettingsChangeListener) {
        listeners.add(listener)
        logger.debug("Added settings change listener: ${listener.javaClass.simpleName}")
    }

    override fun unsubscribeFromChanges(listener: SettingsChangeListener) {
        listeners.remove(listener)
        logger.debug("Removed settings change listener: ${listener.javaClass.simpleName}")
    }

    override fun onMessage(message: Message, pattern: ByteArray?) {
        val channel = String(message.channel)
        val body = String(message.body)
        
        logger.debug("Received message on channel $channel: $body")
        
        when (channel) {
            SETTINGS_CHANGE_CHANNEL -> handleSettingsChange(body)
            SETTINGS_SYNC_CHANNEL -> handleSyncRequest(body)
        }
    }
    
    fun handleMessage(messageBody: String) {
        handleSettingsChange(messageBody)
    }
    
    fun handleSyncResponse(messageBody: String) {
        try {
            val syncResponse = objectMapper.readValue(messageBody, SyncResponseMessage::class.java)
            if (syncResponse.source != instanceId) {
                // Apply all settings from sync response
                kotlinx.coroutines.runBlocking {
                    syncResponse.settings.forEach { (key, value) ->
                        notifyLocalListeners(key, value)
                    }
                }
                logger.info("Applied ${syncResponse.settings.size} settings from sync response")
            }
        } catch (e: Exception) {
            logger.error("Failed to process sync response", e)
        }
    }
    
    private fun handleSettingsChange(messageBody: String) {
        try {
            val changeMessage = objectMapper.readValue(messageBody, SettingsChangeMessage::class.java)
            
            // Ignore messages from this instance (already handled locally)
            if (changeMessage.source == instanceId) {
                return
            }
            
            // Notify local listeners
            kotlinx.coroutines.runBlocking {
                notifyLocalListeners(changeMessage.key, changeMessage.value)
            }
            
            logger.debug("Processed setting change from ${changeMessage.source}: ${changeMessage.key}")
        } catch (e: Exception) {
            logger.error("Failed to process settings change message", e)
        }
    }
    
    private fun handleSyncRequest(messageBody: String) {
        try {
            val syncRequest = objectMapper.readValue(messageBody, SyncRequestMessage::class.java)
            
            // Ignore our own sync requests
            if (syncRequest.source == instanceId) {
                return
            }
            
            // TODO: Implement sync response (would need access to SettingsService)
            logger.debug("Received sync request from ${syncRequest.source}")
        } catch (e: Exception) {
            logger.error("Failed to process sync request", e)
        }
    }
    
    private suspend fun notifyLocalListeners(key: String, value: String) = withContext(Dispatchers.IO) {
        listeners.forEach { listener ->
            try {
                listener.onSettingChanged(key, value)
            } catch (e: Exception) {
                logger.error("Error notifying listener ${listener.javaClass.simpleName}", e)
            }
        }
    }
    
    suspend fun requestFullSync() = withContext(Dispatchers.IO) {
        val message = SyncRequestMessage(
            action = "SYNC_REQUEST",
            timestamp = Instant.now().toString(),
            source = instanceId
        )
        
        val jsonMessage = objectMapper.writeValueAsString(message)
        redisTemplate.convertAndSend(SETTINGS_SYNC_CHANNEL, jsonMessage)
        logger.info("Requested full settings sync")
    }
    
    // For testing
    internal fun getListeners(): List<SettingsChangeListener> = listeners.toList()
}

data class SettingsChangeMessage(
    val action: String,
    val key: String,
    val value: String,
    val timestamp: String,
    val source: String
)

data class SyncRequestMessage(
    val action: String,
    val timestamp: String,
    val source: String
)

data class SyncResponseMessage(
    val action: String,
    val settings: Map<String, String>,
    val source: String
)