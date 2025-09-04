# API Specification

This is the API specification for the spec detailed in @.agent-os/specs/2025-08-28-file-scanner-service-tdd/spec.md

## Endpoints

All endpoints will be developed test-first with comprehensive contract tests.

### Scan Job Management

#### GET /api/v1/scanner/jobs

**Purpose:** List all configured scan jobs with pagination
**Parameters:** 
- page (int, optional): Page number, default 0
- size (int, optional): Page size, default 20
- active (boolean, optional): Filter by active status
- sort (string, optional): Sort field and direction

**Response:**
```json
{
  "content": [{
    "id": "uuid",
    "name": "string",
    "sourceDirectory": "string",
    "filePattern": "string",
    "scanInterval": {
      "type": "CRON|FIXED",
      "value": "string"
    },
    "isActive": "boolean",
    "lastExecution": "ISO-8601",
    "nextExecution": "ISO-8601",
    "statistics": {
      "totalFiles": "number",
      "successRate": "number"
    }
  }],
  "totalElements": "number",
  "totalPages": "number",
  "page": "number",
  "size": "number"
}
```

**Test Scenarios:**
- Valid pagination parameters
- Invalid page/size values
- Empty result set
- Sorting validation
- Authorization checks

**Errors:** 
- 400 Bad Request - Invalid parameters
- 401 Unauthorized - Missing authentication
- 500 Internal Server Error - Database unavailable

#### POST /api/v1/scanner/jobs

**Purpose:** Create a new scan job configuration
**Body:**
```json
{
  "name": "string (required, unique)",
  "description": "string (optional)",
  "sourceDirectory": "string (required, must exist)",
  "filePattern": "string (required, valid regex)",
  "scanInterval": {
    "type": "CRON|FIXED (required)",
    "value": "string (required, valid cron or ISO-8601 duration)"
  },
  "maxFileSizeMb": "number (optional, 1-10240)",
  "recursiveScan": "boolean (optional, default true)",
  "maxDepth": "number (optional, 1-100)",
  "priority": "number (optional, 0-10)",
  "parserId": "string (required, must exist)"
}
```

**Response:** 201 Created with created job object

**Test Scenarios:**
- Valid job creation
- Duplicate name rejection
- Invalid directory path
- Invalid cron expression
- Invalid file pattern regex
- Missing required fields
- Boundary value testing

**Errors:** 
- 400 Bad Request - Validation failure
- 409 Conflict - Duplicate name
- 422 Unprocessable Entity - Invalid directory/parser

#### PUT /api/v1/scanner/jobs/{jobId}

**Purpose:** Update existing scan job configuration
**Parameters:** jobId (UUID, required)
**Body:** Same as POST (all fields optional)

**Response:** 200 OK with updated job object

**Test Scenarios:**
- Partial update
- Full update
- Update non-existent job
- Update with invalid data
- Concurrent update handling

**Errors:** 
- 400 Bad Request - Invalid data
- 404 Not Found - Job doesn't exist
- 409 Conflict - Optimistic lock failure

#### DELETE /api/v1/scanner/jobs/{jobId}

**Purpose:** Delete a scan job (soft delete)
**Parameters:** jobId (UUID, required)

**Response:** 204 No Content

**Test Scenarios:**
- Delete existing job
- Delete non-existent job
- Delete job with active scans
- Delete job with history

**Errors:** 
- 404 Not Found - Job doesn't exist
- 409 Conflict - Job has active scans

### Scan Operations

#### POST /api/v1/scanner/jobs/{jobId}/scan

**Purpose:** Trigger immediate scan for a specific job
**Parameters:** jobId (UUID, required)
**Body:** 
```json
{
  "force": "boolean (optional, bypass schedule check)"
}
```

**Response:** 202 Accepted
```json
{
  "executionId": "uuid",
  "status": "STARTED",
  "message": "Scan initiated",
  "estimatedDuration": "number (seconds)"
}
```

**Test Scenarios:**
- Trigger valid scan
- Trigger with job already running
- Trigger inactive job
- Force scan override

**Errors:** 
- 404 Not Found - Job doesn't exist
- 409 Conflict - Scan already running
- 503 Service Unavailable - Scanner at capacity

### File Status

#### GET /api/v1/scanner/files

**Purpose:** Query scanned files with comprehensive filters

**Parameters:**
- jobId (UUID, optional): Filter by job
- status (string[], optional): Filter by multiple statuses
- from (ISO-8601, optional): Start date
- to (ISO-8601, optional): End date
- filePattern (string, optional): File name pattern
- minSize (long, optional): Minimum file size
- maxSize (long, optional): Maximum file size
- page (int): Page number
- size (int): Page size

**Test Scenarios:**
- Complex filter combinations
- Date range validation
- Large result set pagination
- Performance with multiple filters

### Monitoring

#### GET /api/v1/scanner/metrics

**Purpose:** Get detailed scanner service metrics

**Response:**
```json
{
  "scanning": {
    "activeJobs": "number",
    "runningScans": "number",
    "scanRate": "number (files/second)"
  },
  "queue": {
    "depth": "number",
    "throughput": "number (messages/second)",
    "deadLetterCount": "number"
  },
  "performance": {
    "averageScanDuration": "number (ms)",
    "p95ScanDuration": "number (ms)",
    "failureRate": "number (percentage)"
  },
  "cluster": {
    "instanceCount": "number",
    "healthyInstances": "number",
    "totalCapacity": "number (files/second)"
  }
}
```

**Test Scenarios:**
- Metrics calculation accuracy
- Performance under load
- Cache effectiveness
- Real-time updates

## API Testing Strategy

### Contract Tests
```kotlin
@Test
fun `should validate API contract with OpenAPI spec`()
@Test
fun `should maintain backward compatibility`()
@Test
fun `should handle content negotiation`()
@Test
fun `should validate CORS headers`()
```

### Security Tests
```kotlin
@Test
fun `should enforce authentication on all endpoints`()
@Test
fun `should validate authorization for operations`()
@Test
fun `should prevent SQL injection`()
@Test
fun `should sanitize user input`()
@Test
fun `should rate limit API calls`()
```

### Error Handling Tests
```kotlin
@Test
fun `should return consistent error format`()
@Test
fun `should not leak sensitive information`()
@Test
fun `should handle database connection loss`()
@Test
fun `should handle Redis unavailability`()
```