# Claude Code Context - TIA ETL System

## Project Overview

Building an ETL platform for the Government of Liberia to process telecommunications data from Orange and MTN operators. The system handles ASN.1 and CSV files for tax control and service quality monitoring.

## Agent OS Commands

- Use `/create-spec` to create specifications for new components
- Use `/execute-tasks` to implement features from specifications
- Reference `.agent-os/product/` for project documentation
- Follow standards in `.agent-os/product/tech-stack.md`

## Architecture Guidelines

### Service Architecture
```
services/
├── file-scanner/         # Scans directories, queues files
├── parser-orchestrator/  # Loads parsers, processes files
├── job-manager/         # CRUD for jobs, scheduling
└── monitoring/          # Metrics, alerts, real-time updates
```

### Parser Plugin Architecture
```kotlin
interface DataParser {
    fun getMetadata(): ParserMetadata
    fun process(context: ProcessingContext): ProcessingResult
}

// Context provides connections and config
data class ProcessingContext(
    val file: File,
    val clickHouseConnection: Connection,
    val postgresConnection: Connection,
    val jobConfig: JobConfiguration,
    val logger: Logger
)
```

## Technology Stack

### Backend Requirements
- **Kotlin 2.0.21** with coroutines for async operations
- **Spring Boot 3.3.5** for REST APIs and DI
- **MapStruct 1.6.3** for DTO mappings
- **JPA + Liquibase 4.30.0** for PostgreSQL
- **ClickHouse JDBC 0.7.1** for batch inserts
- **Redis** for distributed locks and queues

### Frontend Requirements
- **React 18+** with TypeScript
- **Material-UI** for components
- **Redux Toolkit** for state
- **React Query** for server state
- **Socket.io** for WebSocket
- **AG-Grid** for data tables

## Code Generation Rules

### Kotlin Services
```kotlin
@Service
class FileScanner(
    private val redisTemplate: RedisTemplate<String, String>,
    private val jobRepository: JobRepository
) {
    // Use coroutines for async operations
    suspend fun scanDirectory(job: JobConfiguration) {
        coroutineScope {
            // Implementation
        }
    }
    
    // Use data classes for DTOs
    data class ScanResult(
        val filesFound: Int,
        val filesQueued: Int,
        val errors: List<String>
    )
}
```

### React Components
```typescript
interface DashboardProps {
    jobs: Job[];
    metrics: ProcessingMetrics;
}

export const Dashboard: FC<DashboardProps> = ({ jobs, metrics }) => {
    const { data, loading } = useQuery(['metrics'], fetchMetrics);
    
    return (
        <Container>
            <Grid container spacing={3}>
                {/* Use Material-UI components */}
            </Grid>
        </Container>
    );
};
```

## Critical Implementation Details

### File Processing Flow
1. **Scanner** finds files matching patterns
2. **Redis Queue** stores file paths with locks
3. **Orchestrator** loads appropriate parser JAR
4. **Parser** processes file and loads to ClickHouse
5. **PostgreSQL** stores processing status and audit

### Error Handling Strategy
- All services implement health checks
- PostgreSQL/ClickHouse/Redis unavailable → service pauses
- Exponential backoff for retries
- Dead letter queue for failed files
- Critical errors trigger dashboard alerts

### Multi-Instance Coordination
```kotlin
// Redis lock for file processing
fun tryAcquireLock(fileName: String): Boolean {
    return redisTemplate.opsForValue()
        .setIfAbsent("lock:$fileName", instanceId, Duration.ofMinutes(5))
}
```

### Job Configuration
```kotlin
data class JobConfiguration(
    val id: UUID,
    val name: String,
    val sourceDirectory: String,
    val filePattern: Regex,
    val scanInterval: ScanInterval, // Cron or Duration
    val parserId: String,
    val afterProcessing: ProcessingAction,
    val isActive: Boolean
)

sealed class ScanInterval {
    data class Cron(val expression: String) : ScanInterval()
    data class Fixed(val interval: Duration) : ScanInterval()
}
```

## Testing Requirements

- **Unit tests** for all business logic (80% coverage)
- **Integration tests** with Testcontainers
- **Performance tests** for large file processing
- **E2E tests** for critical user flows

## Security Considerations

- **Keycloak** for authentication and authorization
- **Roles**: Admin, Operator, Viewer
- **Audit logging** for all operations
- **TLS** for all communications

## Performance Targets

- Process >100 files/hour
- Handle files with millions of records
- API response <200ms
- Dashboard updates <100ms
- Service memory <2GB each

## Development Workflow

1. Create specification: `/create-spec service-name`
2. Review spec in `.agent-os/specs/`
3. Implement with: `/execute-tasks`
4. Write tests alongside implementation
5. Document APIs with OpenAPI

## Agent OS Configuration Override
When using Agent OS commands:
- **CRITICAL**: Always check `.agent-os/overrides.md` first
- Use Kotlin/Spring Boot templates from `.agent-os/templates/kotlin/`
- Follow `.agent-os/standards/kotlin-conventions.md`
- Execute tasks using `.agent-os/instructions/kotlin/execute-tasks.md`
- Generate services with `.agent-os/instructions/kotlin/create-service.md`

## Common Patterns

### Health Check
```kotlin
@RestController
class HealthController {
    @GetMapping("/health")
    fun health(): ResponseEntity<HealthStatus> {
        // Check PostgreSQL, ClickHouse, Redis
        return ResponseEntity.ok(HealthStatus.UP)
    }
}
```

### Metrics Collection
```kotlin
@Component
class MetricsCollector(
    private val meterRegistry: MeterRegistry
) {
    fun recordProcessing(fileName: String, duration: Duration) {
        meterRegistry.timer("file.processing")
            .record(duration)
    }
}
```

## Current Development Focus

Check `.agent-os/product/roadmap.md` for current phase and priorities.

## Important Notes

- Parser JARs are loaded dynamically from `/opt/tia/parsers/`
- Logs rotate daily, critical logs also go to PostgreSQL
- All datetime handling should use UTC
- File paths should handle both Windows and Linux
- Always implement proper resource cleanup