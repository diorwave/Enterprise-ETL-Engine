# File Threshold and Dynamic Settings System Specification

## Overview
Implement a dynamic settings system with file threshold management to control the maximum number of files in the processing queue. The system must support database storage, distributed updates, and real-time configuration changes without service restarts.

## Requirements

### Functional Requirements
1. **Dynamic Settings Management**
   - Store configuration settings in PostgreSQL database
   - Support key-value structure for flexible settings
   - Provide CRUD operations for settings management
   - Initialize default settings on first deployment

2. **File Threshold Control**
   - Limit maximum files in Redis queue
   - Configurable threshold per job or globally
   - Handle threshold exceeded scenarios gracefully
   - Support backpressure mechanisms

3. **Distributed Settings Synchronization**
   - Propagate setting changes to all service instances
   - Use Redis pub/sub for change notifications
   - Cache settings locally with TTL
   - Handle network partitions gracefully

### Non-Functional Requirements
- **Performance**: Settings updates < 100ms propagation
- **Reliability**: Settings persist across restarts
- **Scalability**: Support 100+ service instances
- **Testability**: Full TDD implementation with 80%+ coverage

## Technical Design

### Database Schema
```sql
CREATE TABLE system_settings (
    id UUID PRIMARY KEY,
    setting_key VARCHAR(255) UNIQUE NOT NULL,
    setting_value TEXT NOT NULL,
    value_type VARCHAR(50) NOT NULL,
    description TEXT,
    last_modified TIMESTAMP NOT NULL,
    modified_by VARCHAR(255),
    version BIGINT DEFAULT 0
);

CREATE INDEX idx_settings_key ON system_settings(setting_key);
```

### Key Components

#### 1. SystemSettings Entity
```kotlin
@Entity
@Table(name = "system_settings")
data class SystemSettings(
    @Id
    val id: UUID = UUID.randomUUID(),
    
    @Column(unique = true, nullable = false)
    val settingKey: String,
    
    @Column(nullable = false)
    val settingValue: String,
    
    @Enumerated(EnumType.STRING)
    val valueType: SettingType,
    
    val description: String? = null,
    
    @Column(nullable = false)
    val lastModified: Instant = Instant.now(),
    
    val modifiedBy: String? = null,
    
    @Version
    val version: Long = 0
)

enum class SettingType {
    STRING, INTEGER, LONG, BOOLEAN, JSON
}
```

#### 2. SettingsService
```kotlin
interface SettingsService {
    suspend fun getSetting(key: String): String?
    suspend fun getSettingAs<T>(key: String, type: Class<T>): T?
    suspend fun setSetting(key: String, value: Any, type: SettingType)
    suspend fun deleteSetting(key: String)
    suspend fun getAllSettings(): Map<String, String>
}
```

#### 3. SettingsChangeNotifier
```kotlin
interface SettingsChangeNotifier {
    suspend fun notifyChange(key: String, value: String)
    fun subscribeToChanges(listener: SettingsChangeListener)
}
```

#### 4. FileThresholdValidator
```kotlin
@Component
class FileThresholdValidator(
    private val settingsService: SettingsService
) {
    companion object {
        const val FILE_THRESHOLD_KEY = "scanner.file.threshold"
        const val DEFAULT_THRESHOLD = 10000
    }
    
    suspend fun canEnqueueFile(currentQueueSize: Int): Boolean {
        val threshold = settingsService.getSettingAs(
            FILE_THRESHOLD_KEY, 
            Int::class.java
        ) ?: DEFAULT_THRESHOLD
        
        return currentQueueSize < threshold
    }
    
    suspend fun getThreshold(): Int {
        return settingsService.getSettingAs(
            FILE_THRESHOLD_KEY,
            Int::class.java
        ) ?: DEFAULT_THRESHOLD
    }
}
```

### Redis Integration

#### Pub/Sub Channels
- `settings:changes` - For broadcasting setting changes
- `settings:sync` - For requesting full sync

#### Message Format
```json
{
  "action": "UPDATE",
  "key": "scanner.file.threshold",
  "value": "5000",
  "timestamp": "2025-08-31T10:00:00Z",
  "source": "instance-1"
}
```

### API Endpoints

```yaml
/api/v1/settings:
  get:
    description: Get all settings
    responses:
      200:
        schema:
          type: object
          additionalProperties:
            type: string
  
  post:
    description: Create or update a setting
    requestBody:
      schema:
        type: object
        properties:
          key: string
          value: string
          type: string
    responses:
      200:
        description: Setting updated

/api/v1/settings/{key}:
  get:
    description: Get specific setting
    parameters:
      - name: key
        in: path
        required: true
    responses:
      200:
        schema:
          type: object
          properties:
            key: string
            value: string
            type: string
  
  delete:
    description: Delete a setting
    parameters:
      - name: key
        in: path
        required: true
    responses:
      204:
        description: Setting deleted
```

## Implementation Strategy

### Phase 1: Core Settings Infrastructure
1. Create database schema with Liquibase
2. Implement SystemSettings entity and repository
3. Build SettingsService with caching
4. Add REST API endpoints

### Phase 2: Distribution Mechanism
1. Implement Redis pub/sub integration
2. Build SettingsChangeNotifier
3. Create SettingsChangeListener
4. Add local cache with TTL

### Phase 3: File Threshold Feature
1. Implement FileThresholdValidator
2. Integrate with QueueManager
3. Add backpressure handling
4. Implement metrics collection

### Phase 4: Testing & Monitoring
1. Unit tests for all components
2. Integration tests with Testcontainers
3. E2E tests for settings propagation
4. Performance testing under load

## Testing Requirements

### Unit Tests
- SettingsService CRUD operations
- FileThresholdValidator logic
- Cache invalidation mechanisms
- Message serialization/deserialization

### Integration Tests
- Database persistence
- Redis pub/sub communication
- Settings synchronization
- API endpoint functionality

### E2E Tests
- Complete settings update flow
- Multi-instance synchronization
- Threshold enforcement during scanning
- Failover scenarios

## Success Criteria
1. Settings changes propagate to all instances within 100ms
2. File threshold prevents queue overflow
3. System handles Redis failures gracefully
4. All tests pass with 80%+ coverage
5. No service restarts required for configuration changes