# Technical Specification

This is the technical specification for the spec detailed in @.agent-os/specs/2025-08-28-file-scanner-service-tdd/spec.md

## Technical Requirements

### TDD Implementation Strategy

- **Test Framework Setup**
  - JUnit 5 for test execution and assertions
  - MockK for Kotlin-friendly mocking
  - AssertJ for fluent assertions
  - Testcontainers for integration tests (Redis, PostgreSQL)
  - WireMock for external service mocking
  - Test fixtures and builders for test data

- **Test Categories**
  - Unit Tests: Isolated component testing with mocks
  - Integration Tests: Database and Redis interaction testing
  - Contract Tests: API endpoint validation
  - End-to-End Tests: Full workflow validation
  - Performance Tests: Load and stress testing

- **TDD Workflow**
  - Write failing test first (Red)
  - Implement minimal code to pass (Green)
  - Refactor for clean code (Refactor)
  - Commit after each cycle
  - Maintain test coverage >90%

### Core Components (Test-First Development)

- **Directory Scanner Component**
  - Test scenarios: empty dirs, nested dirs, symlinks, permissions
  - Mock file system for unit tests
  - Real file system for integration tests
  - Pattern matching test cases for various formats

- **File Validator Component**
  - Test file format detection with sample files
  - Mock file access for permission testing
  - Hash calculation verification tests
  - Size limit boundary testing

- **Queue Manager Component**
  - Test Redis operations with Testcontainers
  - Mock Redis failures for resilience testing
  - Consumer group behavior tests
  - Message ordering and priority tests

- **Job Scheduler Component**
  - Mock time for schedule testing
  - Test cron expression evaluation
  - Concurrent execution tests
  - Job state transition tests

### Testing Infrastructure

- **Test Data Management**
  - Test fixtures for CDR files (ASN.1, CSV samples)
  - Database test data with Liquibase
  - Redis test data setup/teardown
  - File system test structure creation

- **Test Environments**
  - Unit: In-memory, fully mocked
  - Integration: Testcontainers for dependencies
  - Contract: MockMvc for API testing
  - E2E: Docker Compose test environment

### Performance Requirements (Test-Verified)

- Load tests: Verify >1000 files/second scanning
- Latency tests: Ensure <10ms p99 queue insertion
- Memory tests: Confirm <512MB heap usage
- Concurrency tests: Validate multi-instance coordination

### Test Coverage Requirements

- Line coverage: >90%
- Branch coverage: >85%
- Mutation testing score: >75%
- Critical path coverage: 100%

## External Dependencies

- **Testcontainers** (1.19.3) - Integration testing with real services
  - **Justification:** Provides reliable, reproducible integration tests with actual Redis and PostgreSQL instances

- **MockK** (1.13.8) - Kotlin mocking framework
  - **Justification:** Native Kotlin support with coroutine mocking capabilities essential for testing async operations

- **AssertJ** (3.24.2) - Fluent assertions library
  - **Justification:** Provides readable, comprehensive assertions that make tests self-documenting

- **WireMock** (3.3.1) - HTTP service virtualization
  - **Justification:** Enables testing of external service integrations without dependencies

- **Pitest** (1.15.3) - Mutation testing framework
  - **Justification:** Ensures test quality by verifying tests actually catch bugs

- **ArchUnit** (1.2.1) - Architecture testing
  - **Justification:** Enforces architectural constraints through automated tests