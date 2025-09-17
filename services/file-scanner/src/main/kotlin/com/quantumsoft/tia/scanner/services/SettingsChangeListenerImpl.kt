package com.quantumsoft.tia.scanner.services

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy

@Component
class SettingsChangeListenerImpl(
    private val settingsService: SettingsService,
    private val settingsCache: SettingsCache,
    private val settingsChangeNotifier: SettingsChangeNotifier? = null
) : SettingsChangeListener {

    private val logger = LoggerFactory.getLogger(SettingsChangeListenerImpl::class.java)

    @PostConstruct
    fun init() {
        settingsChangeNotifier?.subscribeToChanges(this)
        logger.info("SettingsChangeListener initialized and subscribed to notifications")
    }

    @PreDestroy
    fun shutdown() {
        settingsChangeNotifier?.unsubscribeFromChanges(this)
        logger.info("SettingsChangeListener shutdown and unsubscribed from notifications")
    }

    override suspend fun onSettingChanged(key: String, value: String) = withContext(Dispatchers.IO) {
        try {
            logger.debug("Received setting change notification: $key = $value")
            
            if (value.isEmpty()) {
                // Handle deletion
                settingsCache.remove(key)
                settingsService.invalidateCache(key)
                logger.info("Setting deleted: $key")
            } else {
                // Handle update
                settingsCache.put(key, value)
                settingsService.refreshCache(key, value)
                logger.info("Setting updated: $key = $value")
                
                // Special handling for threshold keys
                if (isThresholdKey(key)) {
                    logger.info("File threshold setting updated: $key = $value")
                    // Additional threshold-specific logic could go here
                }
            }
        } catch (e: Exception) {
            logger.error("Error processing setting change for key: $key", e)
            // Still try to refresh the service cache even if local cache fails
            try {
                if (value.isNotEmpty()) {
                    settingsService.refreshCache(key, value)
                } else {
                    settingsService.invalidateCache(key)
                }
            } catch (ex: Exception) {
                logger.error("Failed to refresh service cache for key: $key", ex)
            }
        }
    }

    suspend fun onBatchSettingsChanged(settings: Map<String, String>) = withContext(Dispatchers.IO) {
        try {
            logger.debug("Processing batch setting changes: ${settings.size} settings")
            
            // Update cache in batch
            settingsCache.putAll(settings)
            
            // Refresh service cache for each setting
            settings.forEach { (key, value) ->
                try {
                    settingsService.refreshCache(key, value)
                } catch (e: Exception) {
                    logger.error("Failed to refresh cache for key: $key", e)
                }
            }
            
            logger.info("Batch setting changes processed: ${settings.size} settings")
        } catch (e: Exception) {
            logger.error("Error processing batch setting changes", e)
        }
    }

    fun isThresholdKey(key: String): Boolean {
        return key.startsWith("scanner.file.threshold")
    }
}