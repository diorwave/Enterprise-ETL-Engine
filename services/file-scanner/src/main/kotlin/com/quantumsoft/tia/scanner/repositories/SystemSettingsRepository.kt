package com.quantumsoft.tia.scanner.repositories

import com.quantumsoft.tia.scanner.entities.SystemSettings
import com.quantumsoft.tia.scanner.entities.SettingType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.Optional
import java.util.UUID

@Repository
interface SystemSettingsRepository : JpaRepository<SystemSettings, UUID> {
    
    fun findBySettingKey(settingKey: String): Optional<SystemSettings>
    
    fun findAllByValueType(valueType: SettingType): List<SystemSettings>
    
    fun findByLastModifiedAfter(timestamp: Instant): List<SystemSettings>
    
    fun findBySettingKeyStartingWith(prefix: String): List<SystemSettings>
    
    fun countByValueType(valueType: SettingType): Long
    
    @Modifying
    @Transactional
    @Query("DELETE FROM SystemSettings s WHERE s.settingKey = :key")
    fun deleteBySettingKey(@Param("key") settingKey: String)
    
    @Query("SELECT s FROM SystemSettings s WHERE s.settingKey IN :keys")
    fun findBySettingKeyIn(@Param("keys") keys: List<String>): List<SystemSettings>
    
    @Query("SELECT s.settingKey FROM SystemSettings s")
    fun findAllSettingKeys(): List<String>
    
    @Modifying
    @Transactional
    @Query("UPDATE SystemSettings s SET s.settingValue = :value, s.lastModified = :timestamp, s.modifiedBy = :modifiedBy WHERE s.settingKey = :key")
    fun updateValue(
        @Param("key") settingKey: String,
        @Param("value") settingValue: String,
        @Param("timestamp") lastModified: Instant,
        @Param("modifiedBy") modifiedBy: String?
    ): Int
}