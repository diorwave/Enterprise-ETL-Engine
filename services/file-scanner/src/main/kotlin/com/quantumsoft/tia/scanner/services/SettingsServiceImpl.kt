package com.quantumsoft.tia.scanner.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.quantumsoft.tia.scanner.entities.SystemSettings
import com.quantumsoft.tia.scanner.entities.SettingType
import com.quantumsoft.tia.scanner.repositories.SystemSettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.Instant

@Service
class SettingsServiceImpl(
    private val repository: SystemSettingsRepository,
    private val redisTemplate: RedisTemplate<String, String>,
    private val objectMapper: ObjectMapper,
    private val settingsChangeNotifier: SettingsChangeNotifier
) : SettingsService {

    private val logger = LoggerFactory.getLogger(SettingsServiceImpl::class.java)
    
    companion object {
        private const val CACHE_PREFIX = "settings:"
        private val CACHE_TTL = Duration.ofMinutes(5)
    }

    override suspend fun getSetting(key: String): String? = withContext(Dispatchers.IO) {
        // Try to get from cache first
        val cacheKey = "$CACHE_PREFIX$key"
        val cachedValue = redisTemplate.opsForValue().get(cacheKey)
        
        if (cachedValue != null) {
            logger.debug("Found setting in cache: $key")
            return@withContext cachedValue
        }
        
        // If not in cache, get from database
        val setting = repository.findBySettingKey(key)
        if (setting.isPresent) {
            val value = setting.get().settingValue
            // Cache the value
            redisTemplate.opsForValue().set(cacheKey, value, CACHE_TTL)
            logger.debug("Cached setting from database: $key")
            return@withContext value
        }
        
        logger.debug("Setting not found: $key")
        null
    }

    @Suppress("UNCHECKED_CAST")
    override suspend fun <T> getSettingAs(key: String, type: Class<T>): T? = withContext(Dispatchers.IO) {
        val value = getSetting(key) ?: return@withContext null
        
        try {
            when (type) {
                String::class.java -> value as T
                Int::class.java, Integer::class.java -> value.toInt() as T
                Long::class.java, java.lang.Long::class.java -> value.toLong() as T
                Boolean::class.java, java.lang.Boolean::class.java -> value.toBoolean() as T
                else -> {
                    // Try to deserialize as JSON
                    objectMapper.readValue(value, type)
                }
            }
        } catch (e: Exception) {
            logger.error("Failed to convert setting value for key: $key to type: ${type.simpleName}", e)
            throw e
        }
    }

    @Transactional
    override suspend fun setSetting(key: String, value: Any, type: SettingType) = withContext(Dispatchers.IO) {
        val stringValue = when (value) {
            is String -> value
            is Int, is Long, is Boolean -> value.toString()
            else -> objectMapper.writeValueAsString(value)
        }
        
        val existingSetting = repository.findBySettingKey(key)
        
        val settings = if (existingSetting.isPresent) {
            // Update existing setting
            val current = existingSetting.get()
            val updated = current.copy(
                settingValue = stringValue,
                valueType = type,
                lastModified = Instant.now()
            )
            repository.save(updated)
            logger.info("Updated setting: $key")
        } else {
            // Create new setting
            val newSetting = SystemSettings(
                settingKey = key,
                settingValue = stringValue,
                valueType = type,
                lastModified = Instant.now()
            )
            repository.save(newSetting)
            logger.info("Created new setting: $key")
        }
        
        // Invalidate cache and notify change
        invalidateCache(key)
        settingsChangeNotifier.notifyChange(key, stringValue)
    }

    @Transactional
    override suspend fun deleteSetting(key: String) = withContext(Dispatchers.IO) {
        repository.deleteBySettingKey(key)
        invalidateCache(key)
        settingsChangeNotifier.notifyChange(key, "")
        logger.info("Deleted setting: $key")
    }

    override suspend fun getAllSettings(): Map<String, String> = withContext(Dispatchers.IO) {
        repository.findAll().associate { it.settingKey to it.settingValue }
    }

    override suspend fun getSettings(keys: List<String>): Map<String, String> = withContext(Dispatchers.IO) {
        repository.findBySettingKeyIn(keys).associate { it.settingKey to it.settingValue }
    }

    override suspend fun refreshCache(key: String, value: String) = withContext(Dispatchers.IO) {
        val cacheKey = "$CACHE_PREFIX$key"
        redisTemplate.delete(cacheKey)
        redisTemplate.opsForValue().set(cacheKey, value, CACHE_TTL)
        logger.debug("Refreshed cache for setting: $key")
    }

    override suspend fun invalidateCache(key: String) = withContext(Dispatchers.IO) {
        val cacheKey = "$CACHE_PREFIX$key"
        redisTemplate.delete(cacheKey)
        logger.debug("Invalidated cache for setting: $key")
    }
}