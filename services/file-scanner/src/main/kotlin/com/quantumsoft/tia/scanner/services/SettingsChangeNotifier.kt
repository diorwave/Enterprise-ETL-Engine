package com.quantumsoft.tia.scanner.services

interface SettingsChangeNotifier {
    suspend fun notifyChange(key: String, value: String)
    fun subscribeToChanges(listener: SettingsChangeListener)
    fun unsubscribeFromChanges(listener: SettingsChangeListener)
}

interface SettingsChangeListener {
    suspend fun onSettingChanged(key: String, value: String)
}