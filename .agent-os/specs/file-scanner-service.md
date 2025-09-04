# File Scanner Service Specification

## Overview
The File Scanner Service is responsible for monitoring configured directories, discovering files matching specified patterns, and queuing them for processing. It coordinates with multiple service instances using Redis for distributed locking and ensures exactly-once processing guarantees.

## Service Architecture

### Core Components

#### 1. Directory Scanner
- **Purpose**: Recursively scans directories for files matching configured patterns
- **Technology**: Kotlin coroutines for async I/O operations
- **Features**:
  - Support for multiple file patterns (regex)
  - Recursive and non-recursive scanning options
  - File modification time tracking
  - Size and count limits per scan

#### 2. Job Scheduler
- **Purpose**: Manages and executes scanning jobs based on configured schedules
- **Technology**: Spring Scheduler with Quartz for complex cron expressions
- **Features**:
  - Cron-based scheduling
  - Fixed interval scheduling
  - On-demand scanning via API
  - Job pause/resume capabilities

#### 3. File Queue Manager
- **Purpose**: Manages the Redis queue of discovered files
- **Technology**: Spring Data Redis with Lettuce client
- **Features**:
  - FIFO queue implementation
  - Distributed locking for multi-instance coordination
  - Dead letter queue for failed files
  - Priority queue support for urgent files

#### 4. Processing Status Tracker
- **Purpose**: Tracks file processing status in PostgreSQL
- **Technology**: Spring Data JPA with Hibernate
- **Features**:
  - File processing history
  - Duplicate detection
  - Retry management
  - Audit trail

## Data Models

### JobConfiguration
```kotlin
data class JobConfiguration(
    val id: UUID,
    val name: String,
    val description: String?,
    val sourceDirectory: String,
    val filePattern: String, // Regex pattern
    val recursive: Boolean = true,
    val scanInterval: ScanInterval,
    val parserId: String,
    val afterProcessing: ProcessingAction,
    val priority: Int = 0,
    val maxFilesPerScan: Int = 1000,
    val maxFileSizeBytes: Long = 1_073_741_824, // 1GB
    val isActive: Boolean = true,
    val createdAt: Instant,
    val updatedAt: Instant,
    val lastScanAt: Instant?,
    val metadata: Map<String, String> = emptyMap()
)

sealed class ScanInterval {
    data class Cron(val expression: String) : ScanInterval()
    data class Fixed(val interval: Duration) : ScanInterval()
    object Manual : ScanInterval()
}

enum class ProcessingAction {
    KEEP,           // Keep file in place
    MOVE,          // Move to processed directory
    DELETE,        // Delete after successful processing
    ARCHIVE        // Compress and archive
}
```

### FileDiscovery
```kotlin
data class FileDiscovery(
    val id: UUID,
    val jobId: UUID,
    val filePath: String,
    val fileName: String,
    val fileSize: Long,
    val lastModified: Instant,
    val discoveredAt: Instant,
    val checksum: String?, // MD5 or SHA-256
    val status: DiscoveryStatus,
    val priority: Int,
    val retryCount: Int = 0,
    val maxRetries: Int = 3,
    val processingStartedAt: Instant?,
    val processingCompletedAt: Instant?,
    val errorMessage: String?,
    val metadata: Map<String, String> = emptyMap()
)

enum class DiscoveryStatus {
    DISCOVERED,     // File found, not yet queued
    QUEUED,        // In Redis queue
    PROCESSING,    // Being processed
    COMPLETED,     // Successfully processed
    FAILED,        // Processing failed
    SKIPPED,       // Skipped (duplicate, too large, etc.)
    RETRY_SCHEDULED // Will be retried
}
```

### ScanResult
```kotlin
data class ScanResult(
    val jobId: UUID,
    val scanId: UUID,
    val startTime: Instant,
    val endTime: Instant,
    val duration: Duration,
    val filesDiscovered: Int,
    val filesQueued: Int,
    val filesSkipped: Int,
    val totalSizeBytes: Long,
    val errors: List<ScanError>,
    val statistics: ScanStatistics
)

data class ScanError(
    val path: String,
    val errorType: ErrorType,
    val message: String,
    val timestamp: Instant
)

data class ScanStatistics(
    val averageFileSize: Long,
    val largestFile: FileInfo?,
    val oldestFile: FileInfo?,
    val newestFile: FileInfo?,
    val filesByExtension: Map<String, Int>
)
```

## REST API Endpoints

### Job Management

#### Create Job
```
POST /api/v1/jobs
Content-Type: application/json

{
  "name": "Orange ASN.1 Scanner",
  "sourceDirectory": "/data/orange/incoming",
  "filePattern": ".*\\.asn1$",
  "recursive": true,
  "scanInterval": {
    "type": "cron",
    "expression": "0 */15 * * * *"
  },
  "parserId": "orange-asn1-parser",
  "afterProcessing": "ARCHIVE",
  "priority": 10
}

Response: 201 Created
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Orange ASN.1 Scanner",
  ...
}
```

#### List Jobs
```
GET /api/v1/jobs?active=true&page=0&size=20&sort=priority,desc

Response: 200 OK
{
  "content": [...],
  "totalElements": 15,
  "totalPages": 1,
  "number": 0
}
```

#### Update Job
```
PUT /api/v1/jobs/{jobId}
PATCH /api/v1/jobs/{jobId}
```

#### Delete Job
```
DELETE /api/v1/jobs/{jobId}
```

#### Trigger Manual Scan
```
POST /api/v1/jobs/{jobId}/scan

Response: 202 Accepted
{
  "scanId": "650e8400-e29b-41d4-a716-446655440001",
  "status": "INITIATED",
  "message": "Scan job queued"
}
```

### File Discovery

#### List Discovered Files
```
GET /api/v1/discoveries?jobId={jobId}&status=QUEUED&page=0&size=50

Response: 200 OK
{
  "content": [...],
  "totalElements": 234,
  "totalPages": 5,
  "number": 0
}
```

#### Get File Discovery Details
```
GET /api/v1/discoveries/{discoveryId}
```

#### Retry Failed File
```
POST /api/v1/discoveries/{discoveryId}/retry

Response: 202 Accepted
```

#### Skip File
```
POST /api/v1/discoveries/{discoveryId}/skip

Response: 200 OK
```

### Monitoring & Health

#### Service Health
```
GET /health

Response: 200 OK
{
  "status": "UP",
  "components": {
    "redis": { "status": "UP" },
    "postgres": { "status": "UP" },
    "diskSpace": { 
      "status": "UP",
      "details": {
        "free": 107374182400,
        "threshold": 10737418240
      }
    }
  }
}
```

#### Service Metrics
```
GET /metrics

Response: 200 OK
{
  "filesScanned": 15234,
  "filesQueued": 14892,
  "filesProcessed": 14001,
  "filesFailed": 342,
  "averageScanTime": "PT2.5S",
  "queueDepth": 891,
  "activeJobs": 8
}
```

#### Scan History
```
GET /api/v1/scan-history?jobId={jobId}&from=2024-01-01&to=2024-01-31

Response: 200 OK
[
  {
    "scanId": "...",
    "jobId": "...",
    "startTime": "2024-01-15T10:00:00Z",
    "endTime": "2024-01-15T10:02:30Z",
    "filesDiscovered": 234,
    "filesQueued": 230,
    "status": "COMPLETED"
  }
]
```

## Integration Points

### Redis Integration

#### Queue Operations
```kotlin
interface FileQueueService {
    suspend fun enqueue(discovery: FileDiscovery): Boolean
    suspend fun dequeue(): FileDiscovery?
    suspend fun peek(): FileDiscovery?
    suspend fun size(): Long
    suspend fun clear(jobId: UUID)
    suspend fun moveToDeadLetter(discovery: FileDiscovery)
}
```

#### Distributed Locking
```kotlin
interface DistributedLockService {
    suspend fun tryLock(key: String, duration: Duration): Boolean
    suspend fun unlock(key: String): Boolean
    suspend fun extend(key: String, duration: Duration): Boolean
    suspend fun isLocked(key: String): Boolean
}
```

### PostgreSQL Integration

#### Repositories
```kotlin
interface JobConfigurationRepository : JpaRepository<JobConfiguration, UUID> {
    fun findByIsActiveTrue(): List<JobConfiguration>
    fun findByNameIgnoreCase(name: String): Optional<JobConfiguration>
    
    @Query("SELECT j FROM JobConfiguration j WHERE j.lastScanAt < :threshold")
    fun findJobsNeedingScanning(@Param("threshold") threshold: Instant): List<JobConfiguration>
}

interface FileDiscoveryRepository : JpaRepository<FileDiscovery, UUID> {
    fun findByJobIdAndStatus(jobId: UUID, status: DiscoveryStatus): List<FileDiscovery>
    fun existsByFilePathAndChecksum(filePath: String, checksum: String): Boolean
    
    @Query("SELECT COUNT(f) FROM FileDiscovery f WHERE f.status = :status")
    fun countByStatus(@Param("status") status: DiscoveryStatus): Long
}
```

### Event Publishing

#### Events
```kotlin
sealed class ScannerEvent {
    data class JobCreated(val job: JobConfiguration) : ScannerEvent()
    data class JobUpdated(val job: JobConfiguration) : ScannerEvent()
    data class JobDeleted(val jobId: UUID) : ScannerEvent()
    data class ScanStarted(val jobId: UUID, val scanId: UUID) : ScannerEvent()
    data class ScanCompleted(val result: ScanResult) : ScannerEvent()
    data class FileDiscovered(val discovery: FileDiscovery) : ScannerEvent()
    data class FileQueued(val discovery: FileDiscovery) : ScannerEvent()
    data class FileFailed(val discovery: FileDiscovery, val error: String) : ScannerEvent()
}
```

#### WebSocket Updates
```kotlin
@Component
class ScannerWebSocketHandler {
    fun sendJobUpdate(jobId: UUID, update: JobUpdate)
    fun sendScanProgress(scanId: UUID, progress: ScanProgress)
    fun sendQueueStatus(status: QueueStatus)
}
```

## Configuration

### Application Properties
```yaml
file-scanner:
  scan:
    thread-pool-size: 10
    max-concurrent-scans: 5
    file-buffer-size: 8192
    checksum-algorithm: SHA-256
    enable-checksum: true
    
  queue:
    redis-key-prefix: "tia:scanner:queue:"
    lock-timeout: PT5M
    max-queue-size: 10000
    batch-size: 100
    
  retry:
    max-attempts: 3
    initial-delay: PT30S
    max-delay: PT5M
    multiplier: 2.0
    
  cleanup:
    enabled: true
    older-than-days: 30
    run-at-startup: false
    cron: "0 0 2 * * *"
    
  monitoring:
    metrics-enabled: true
    health-check-interval: PT30S
    alert-threshold-queue-size: 5000
    alert-threshold-failed-files: 100
```

## Security Considerations

### Authentication & Authorization
- Keycloak integration for OAuth2/OIDC
- Role-based access control:
  - `ADMIN`: Full access to all operations
  - `OPERATOR`: Create/update jobs, trigger scans
  - `VIEWER`: Read-only access to jobs and discoveries

### File Access Security
- Validate directory paths (prevent directory traversal)
- Check file permissions before queuing
- Sanitize file names in API responses
- Implement file size limits
- Virus scanning integration (optional)

## Performance Requirements

### Scanning Performance
- Scan 10,000 files in < 30 seconds
- Support directories with 1M+ files
- Memory usage < 512MB per scan
- CPU usage < 50% during scanning

### Queue Performance
- Enqueue/dequeue operations < 10ms
- Support 100,000 queued items
- Batch operations for efficiency
- Redis memory optimization

### API Performance
- Response time < 200ms for queries
- Support 100 concurrent API requests
- Pagination for large result sets
- Response caching where appropriate

## Error Handling

### Retry Strategy
```kotlin
class ExponentialBackoffRetry(
    private val maxAttempts: Int = 3,
    private val initialDelay: Duration = Duration.ofSeconds(30),
    private val maxDelay: Duration = Duration.ofMinutes(5),
    private val multiplier: Double = 2.0
) {
    fun calculateDelay(attempt: Int): Duration {
        val delay = initialDelay.multipliedBy(multiplier.pow(attempt - 1).toLong())
        return if (delay > maxDelay) maxDelay else delay
    }
}
```

### Error Recovery
- Automatic recovery from Redis disconnection
- PostgreSQL connection pooling with retry
- Graceful degradation when dependencies fail
- Circuit breaker pattern for external services

## Testing Requirements

### Unit Tests
```kotlin
@Test
fun `should discover files matching pattern`() {
    // Given
    val job = createTestJob(filePattern = ".*\\.csv$")
    val scanner = DirectoryScanner(job)
    
    // When
    val files = scanner.scan("/test/data")
    
    // Then
    assertThat(files).hasSize(5)
    assertThat(files).allMatch { it.endsWith(".csv") }
}
```

### Integration Tests
```kotlin
@SpringBootTest
@Testcontainers
class FileScannerIntegrationTest {
    @Container
    val redis = GenericContainer("redis:7-alpine")
    
    @Container
    val postgres = PostgreSQLContainer("postgres:15")
    
    @Test
    fun `should complete full scan workflow`() {
        // Test complete scanning workflow
    }
}
```

### Performance Tests
```kotlin
@Test
fun `should handle large directory efficiently`() {
    // Create directory with 100,000 files
    // Measure scanning time and memory usage
    // Assert performance requirements are met
}
```

## Deployment Considerations

### Docker Configuration
```dockerfile
FROM openjdk:17-slim
COPY target/file-scanner-*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Kubernetes Resources
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: file-scanner
spec:
  replicas: 3
  template:
    spec:
      containers:
      - name: file-scanner
        image: quantum-soft-dev/file-scanner:latest
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
```

### Health Probes
```yaml
livenessProbe:
  httpGet:
    path: /health/liveness
    port: 8081
  initialDelaySeconds: 30
  periodSeconds: 10
  
readinessProbe:
  httpGet:
    path: /health/readiness
    port: 8081
  initialDelaySeconds: 20
  periodSeconds: 5
```

## Monitoring & Observability

### Metrics
- Files scanned per minute
- Queue depth over time
- Processing success/failure rates
- Scan duration percentiles
- API response time histograms

### Logging
```kotlin
@Component
class ScannerLogger {
    fun logScanStarted(job: JobConfiguration, scanId: UUID)
    fun logFileDiscovered(file: File, job: JobConfiguration)
    fun logFileQueued(discovery: FileDiscovery)
    fun logScanCompleted(result: ScanResult)
    fun logError(error: Exception, context: Map<String, Any>)
}
```

### Distributed Tracing
- OpenTelemetry integration
- Trace ID propagation
- Span creation for key operations
- Performance bottleneck identification

## Migration & Upgrade

### Database Migrations
```sql
-- V1__create_job_configuration.sql
CREATE TABLE job_configuration (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    source_directory VARCHAR(1000) NOT NULL,
    file_pattern VARCHAR(500) NOT NULL,
    -- additional columns
);

-- V2__create_file_discovery.sql
CREATE TABLE file_discovery (
    id UUID PRIMARY KEY,
    job_id UUID NOT NULL REFERENCES job_configuration(id),
    file_path VARCHAR(2000) NOT NULL,
    -- additional columns
);
```

### Version Compatibility
- API versioning with `/api/v1` prefix
- Backward compatibility for 2 major versions
- Graceful migration path for breaking changes
- Feature flags for gradual rollout

## Future Enhancements

### Phase 2 Features
- Cloud storage support (S3, Azure Blob)
- Machine learning for pattern detection
- Predictive scanning based on historical data
- Multi-tenancy support
- Advanced file filtering (size, date, content)

### Phase 3 Features
- Distributed scanning across multiple nodes
- Real-time file watching (inotify/FSEvents)
- Compression/decompression support
- Encryption at rest and in transit
- Advanced scheduling with dependencies

## Acceptance Criteria

1. **Functional Requirements**
   - [ ] Service can scan configured directories
   - [ ] Files matching patterns are queued in Redis
   - [ ] Multiple instances coordinate via distributed locking
   - [ ] Job scheduling works with cron and fixed intervals
   - [ ] API endpoints return correct responses
   - [ ] Failed files are retried with backoff
   - [ ] Processing status is tracked in PostgreSQL

2. **Non-Functional Requirements**
   - [ ] Service starts in < 30 seconds
   - [ ] Memory usage stays below 1GB
   - [ ] Can handle 100,000 files per directory
   - [ ] API response time < 200ms
   - [ ] 99.9% uptime during operation
   - [ ] Comprehensive logging and monitoring
   - [ ] Security requirements are met

3. **Integration Requirements**
   - [ ] Integrates with Redis for queuing
   - [ ] Integrates with PostgreSQL for persistence
   - [ ] Publishes events for other services
   - [ ] WebSocket updates work correctly
   - [ ] Health checks report accurate status
   - [ ] Metrics are exposed for Prometheus

4. **Documentation Requirements**
   - [ ] OpenAPI specification complete
   - [ ] README with setup instructions
   - [ ] Configuration guide
   - [ ] Troubleshooting guide
   - [ ] Performance tuning guide