
# Test Specification

This is the comprehensive test specification for @.agent-os/specs/2025-08-28-file-scanner-service-tdd/spec.md

## Test Strategy

### Unit Tests

#### DirectoryScannerTest
```kotlin
class DirectoryScannerTest {
    @Test
    fun `should scan directory recursively with max depth`()
    @Test
    fun `should match files by pattern`()
    @Test
    fun `should handle permission denied gracefully`()
    @Test
    fun `should skip symbolic links when configured`()
    @Test
    fun `should respect scan timeout`()
    @Test
    fun `should emit metrics for scan performance`()
}
```

#### FileValidatorTest
```kotlin
class FileValidatorTest {
    @Test
    fun `should detect ASN1 format by magic bytes`()
    @Test
    fun `should detect CSV format by extension and content`()
    @Test
    fun `should reject files exceeding size limit`()
    @Test
    fun `should calculate correct file hash`()
    @Test
    fun `should detect duplicate files by hash`()
    @Test
    fun `should validate file is readable`()
}
```

#### QueueManagerTest
```kotlin
class QueueManagerTest {
    @Test
    fun `should add message to Redis stream`()
    @Test
    fun `should handle Redis connection failure`()
    @Test
    fun `should respect message priority`()
    @Test
    fun `should move failed messages to DLQ`()
    @Test
    fun `should acknowledge processed messages`()
    @Test
    fun `should implement backpressure when queue full`()
}
```

#### JobSchedulerTest
```kotlin
class JobSchedulerTest {
    @Test
    fun `should execute job on cron schedule`()
    @Test
    fun `should execute job on fixed interval`()
    @Test
    fun `should prevent concurrent execution of same job`()
    @Test
    fun `should handle job failure with retry`()
    @Test
    fun `should update job execution history`()
    @Test
    fun `should reload jobs dynamically`()
}
```

### Integration Tests

#### ScanJobRepositoryIntegrationTest
```kotlin
@DataJpaTest
@TestContainers
class ScanJobRepositoryIntegrationTest {
    @Container
    val postgres = PostgreSQLContainer("postgres:16")
    
    @Test
    fun `should persist and retrieve scan job`()
    @Test
    fun `should enforce unique job names`()
    @Test
    fun `should cascade delete executions`()
    @Test
    fun `should query active jobs efficiently`()
    @Test
    fun `should update job configuration atomically`()
}
```

#### RedisQueueIntegrationTest
```kotlin
@TestContainers
class RedisQueueIntegrationTest {
    @Container
    val redis = GenericContainer("redis:7.4")
    
    @Test
    fun `should handle concurrent producers`()
    @Test
    fun `should maintain message ordering per key`()
    @Test
    fun `should recover from connection loss`()
    @Test
    fun `should distribute messages across consumer group`()
    @Test
    fun `should handle consumer group rebalancing`()
}
```

#### MultiInstanceCoordinationTest
```kotlin
@TestContainers
class MultiInstanceCoordinationTest {
    @Test
    fun `should acquire distributed lock exclusively`()
    @Test
    fun `should release lock on instance shutdown`()
    @Test
    fun `should handle lock expiration and renewal`()
    @Test
    fun `should redistribute work on instance failure`()
    @Test
    fun `should prevent duplicate file processing`()
}
```

### Contract Tests

#### ScanJobApiContractTest
```kotlin
@WebMvcTest(ScanJobController::class)
class ScanJobApiContractTest {
    @Test
    fun `POST should create job with valid configuration`()
    @Test
    fun `POST should reject invalid cron expression`()
    @Test
    fun `GET should return paginated job list`()
    @Test
    fun `PUT should update existing job`()
    @Test
    fun `DELETE should mark job as inactive`()
    @Test
    fun `should validate directory exists and is readable`()
}
```

#### FileStatusApiContractTest
```kotlin
@WebMvcTest(FileStatusController::class)
class FileStatusApiContractTest {
    @Test
    fun `should return files filtered by status`()
    @Test
    fun `should paginate large result sets`()
    @Test
    fun `should return 404 for non-existent file`()
    @Test
    fun `should validate date range parameters`()
}
```

### End-to-End Tests

#### FileDiscoveryE2ETest
```kotlin
@SpringBootTest
@TestContainers
class FileDiscoveryE2ETest {
    @Test
    fun `should discover queue and process file end-to-end`()
    @Test
    fun `should handle job creation through to execution`()
    @Test
    fun `should recover from service restart`()
    @Test
    fun `should scale with multiple instances`()
}
```

### Performance Tests

#### ScannerLoadTest
```kotlin
class ScannerLoadTest {
    @Test
    fun `should handle 1000 files per second`()
    @Test
    fun `should maintain sub-10ms queue latency under load`()
    @Test
    fun `should not exceed 512MB heap under stress`()
    @Test
    fun `should scale linearly with instance count`()
}
```

## Test Data

### Sample Files
- `test-data/asn1/valid-cdr.asn1` - Valid ASN.1 CDR file
- `test-data/asn1/corrupted.asn1` - Corrupted ASN.1 file
- `test-data/csv/valid-records.csv` - Valid CSV file
- `test-data/csv/malformed.csv` - Malformed CSV file
- `test-data/large-file.bin` - File exceeding size limit

### Test Fixtures
```kotlin
object TestFixtures {
    fun validScanJob() = ScanJob(...)
    fun scannedFile() = ScannedFile(...)
    fun jobExecution() = ScanJobExecution(...)
}
```

## Test Execution

### Test Profiles
- `unit` - Fast unit tests only (<5 seconds)
- `integration` - Integration tests with containers (<2 minutes)
- `contract` - API contract tests (<1 minute)
- `e2e` - Full end-to-end tests (<5 minutes)
- `performance` - Load and stress tests (<10 minutes)
- `all` - Complete test suite (<20 minutes)

### CI/CD Pipeline
```yaml
test:
  stage: test
  parallel:
    - unit-tests:
        script: ./gradlew test -Pprofile=unit
    - integration-tests:
        script: ./gradlew test -Pprofile=integration
    - contract-tests:
        script: ./gradlew test -Pprofile=contract
    - mutation-tests:
        script: ./gradlew pitest
```

## Coverage Goals

- Overall line coverage: >90%
- Critical path coverage: 100%
- API endpoint coverage: 100%
- Error handling coverage: 100%
- Integration points: >85%
