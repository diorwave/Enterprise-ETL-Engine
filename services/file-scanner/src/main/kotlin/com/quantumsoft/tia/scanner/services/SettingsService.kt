package com.quantumsoft.tia.scanner.services

import com.quantumsoft.tia.scanner.entities.SettingType

interface SettingsService {
    suspend fun getSetting(key: String): String?
    suspend fun <T> getSettingAs(key: String, type: Class<T>): T?
    suspend fun setSetting(key: String, value: Any, type: SettingType)
    suspend fun deleteSetting(key: String)
    suspend fun getAllSettings(): Map<String, String>
    suspend fun getSettings(keys: List<String>): Map<String, String>
    suspend fun refreshCache(key: String, value: String)
    suspend fun invalidateCache(key: String)
}