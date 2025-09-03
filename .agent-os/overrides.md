# TIA ETL System - Agent OS Overrides

## IMPORTANT: Configuration Overrides

This file defines critical overrides for Agent OS commands when working with the TIA ETL System project.

### Technology Stack Override
- **IGNORE**: `.agent-os/standards/tech-stack.md` (Ruby on Rails config)
- **USE**: `.agent-os/product/tech-stack.md` (Kotlin/Spring Boot config)

### Language & Framework
- **Language**: Kotlin 2.0.21 (NOT Ruby)
- **Framework**: Spring Boot 3.3.5 (NOT Rails)
- **Build Tool**: Gradle with Kotlin DSL (NOT Bundler)
- **Frontend**: React 18+ with TypeScript (same as default)

### Code Generation Rules
When using Agent OS commands:

1. **For `/create-spec`**: 
   - Follow standard spec creation process
   - Tech requirements should reference Kotlin/Spring Boot

2. **For `/execute-tasks`**:
   - Use `.agent-os/instructions/kotlin/execute-tasks.md`
   - Generate Kotlin code following `.agent-os/standards/kotlin-conventions.md`
   - Use templates from `.agent-os/templates/kotlin/`

3. **For `/create-service`**:
   - Use `.agent-os/instructions/kotlin/create-service.md`
   - Generate Spring Boot microservice structure
   - Include Gradle build configuration

### Database & Infrastructure
- **Primary DB**: PostgreSQL 16+ with JPA/Hibernate
- **Time-series DB**: ClickHouse 24.11
- **Cache/Queue**: Redis 7.4+
- **Migrations**: Liquibase (NOT Rails migrations)
- **ORM**: Spring Data JPA (NOT Active Record)

### Testing Framework
- **Unit Tests**: JUnit 5 + MockK
- **Integration Tests**: Testcontainers
- **Performance Tests**: Gatling
- **Frontend Tests**: Jest + React Testing Library

### Service Architecture
All services follow this pattern:
```
services/{service-name}/
├── build.gradle.kts
├── src/main/kotlin/com/tia/etl/{service-name}/
│   ├── Application.kt
│   ├── config/
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── domain/
│   └── mapper/
└── src/test/kotlin/
```

### API Conventions
- Base path: `/api/v1/`
- REST endpoints with Spring annotations
- Coroutines for async operations
- OpenAPI 3.1 documentation

### File Paths
When referencing files:
- Config: `.agent-os/product/` (project-specific)
- Standards: `.agent-os/standards/kotlin-conventions.md`
- Templates: `.agent-os/templates/kotlin/`
- Instructions: `.agent-os/instructions/kotlin/`

### Command Adaptations

#### Original Command → Adapted Behavior
- `/create-spec` → Uses Kotlin/Spring context
- `/execute-tasks` → Generates Kotlin code
- `/create-service` → Creates Spring Boot service
- `/plan-product` → Considers Java/Kotlin ecosystem

### Checklist for Agent OS Usage
Before executing any Agent OS command:
- [ ] Verify using product tech-stack, not global
- [ ] Ensure Kotlin/Spring Boot context
- [ ] Check for service-specific requirements
- [ ] Follow microservice architecture patterns
- [ ] Use appropriate Kotlin idioms

### Do NOT Generate
- Ruby code
- Rails models/controllers
- ERB templates
- Bundler configurations
- RSpec tests

### DO Generate
- Kotlin classes with Spring annotations
- Gradle build configurations
- JPA entities
- Spring REST controllers
- JUnit/MockK tests
- Liquibase migrations

## Quick Reference

| Component | Use | Don't Use |
|-----------|-----|-----------|
| Language | Kotlin 2.0.21 | Ruby |
| Framework | Spring Boot 3.3.5 | Rails |
| Build | Gradle | Bundler |
| ORM | Spring Data JPA | Active Record |
| Testing | JUnit + MockK | RSpec |
| Migrations | Liquibase | Rails Migrations |
| API | Spring REST | Rails API |

## Notes
- This project is for the Government of Liberia
- Processing telecom data from Orange and MTN
- High-performance ETL with plugin architecture
- Microservices deployed in Docker containers