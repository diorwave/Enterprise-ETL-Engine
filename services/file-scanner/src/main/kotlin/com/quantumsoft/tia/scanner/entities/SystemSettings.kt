package com.quantumsoft.tia.scanner.entities

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "system_settings")
data class SystemSettings(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID = UUID.randomUUID(),
    
    @Column(name = "setting_key", unique = true, nullable = false)
    val settingKey: String,
    
    @Column(name = "setting_value", nullable = false, columnDefinition = "TEXT")
    val settingValue: String,
    
    @Enumerated(EnumType.STRING)
    @Column(name = "value_type", nullable = false, length = 50)
    val valueType: SettingType,
    
    @Column(columnDefinition = "TEXT")
    val description: String? = null,
    
    @Column(name = "last_modified", nullable = false)
    val lastModified: Instant = Instant.now(),
    
    @Column(name = "modified_by")
    val modifiedBy: String? = null,
    
    @Version
    @Column(nullable = false)
    val version: Long = 0
)

enum class SettingType {
    STRING,
    INTEGER,
    LONG,
    BOOLEAN,
    JSON
}