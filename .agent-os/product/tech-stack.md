# Tech Stack - TIA ETL System

## Context

Technical stack for TIA ETL System - government telecom data processing platform for Liberia.

## Core Technologies

### Language & Framework
- **Language**: Kotlin 2.0.21
- **JVM**: Java 21 LTS (Corretto/OpenJDK)
- **Framework**: Spring Boot 3.3.5
- **Build Tool**: Gradle 8.5+ with Kotlin DSL
- **Async**: Kotlin Coroutines 1.9.0

### Spring Ecosystem
- **Spring Web**: REST APIs
- **Spring Data JPA**: PostgreSQL integration
- **Spring Cloud Stream**: Event processing (2024.0.0)
- **Spring Security**: Authentication & Authorization
- **Spring Actuator**: Health checks and metrics

## Data Layer

### Databases
- **PostgreSQL 16+**: Configuration, job management, audit logs
- **ClickHouse 24.11**: Telecom data storage with monthly partitioning
- **Redis 7.4+**: Distributed locks and job queue

### Database Tools
- **Liquibase 4.30.0**: PostgreSQL schema migrations
- **MapStruct 1.6.3**: DTO mappings
- **HikariCP**: Connection pooling (included with Spring Boot)
- **ClickHouse JDBC 0.7.1**: Native driver for batch inserts
## Parser Architecture

### Plugin System
- **JAR Loading**: URLClassLoader for dynamic loading
- **Interface**: Standardized DataParser API
- **File Formats**: ASN.1 (BER/DER), CSV, extensible
- **Libraries**:
  - Bouncy Castle 1.78.1: ASN.1 processing
  - Apache Commons CSV 1.12.0: CSV parsing
  - Jackson 2.18.2: JSON processing

## Web Console

### Frontend Stack
- **Framework**: React 18.3+
- **Language**: TypeScript 5.7+
- **Build**: Vite 6.0+
- **Package Manager**: npm 10+

### UI Components
- **Component Library**: Material-UI (MUI) 6.2+
- **Data Grid**: AG-Grid 33.0+ for tables
- **Charts**: Recharts 2.15+ for visualizations
- **Icons**: Material Icons

### State & Communication
- **State Management**: Redux Toolkit 2.5+
- **Server State**: TanStack Query 5.62+
- **WebSocket**: Socket.io-client 4.8+
- **HTTP Client**: Axios 1.7+

### Styling
- **CSS Framework**: TailwindCSS 3.4+
- **CSS-in-JS**: Emotion 11.14+ (with MUI)

## Security & Authentication

### Keycloak Integration
- **Version**: 26.0+
- **SSO**: Single Sign-On
- **OAuth 2.0**: Token-based auth
- **JWT**: Access tokens
- **Roles**: Admin, Operator, Viewer

### Security Features
- **TLS**: HTTPS everywhere
- **CORS**: Configured for Web Console
- **Rate Limiting**: API protection
- **Audit Logging**: All user actions
## Infrastructure

### Container Platform
- **Docker**: 27.0+ for containerization
- **Docker Compose**: 2.30+ for local development
- **Base Images**: Alpine Linux for size optimization

### Service Communication
- **REST**: Synchronous APIs
- **Redis Queue**: Asynchronous job processing
- **WebSocket**: Real-time updates

## Monitoring & Logging

### Logging
- **Framework**: Logback 1.5.12 with SLF4J 2.0.16
- **Log Rotation**: Daily rotation with size limits
- **Critical Logs**: PostgreSQL storage
- **Log Levels**: Configurable per service

### Metrics & Health
- **Micrometer**: Metrics collection (included with Spring Boot)
- **Spring Actuator**: Health endpoints
- **Custom Metrics**: Processing statistics

### Alerting
- **Dashboard Alerts**: WebSocket notifications
- **Alert Types**:
  - Service unavailability
  - High error rates
  - Missing files
  - Processing delays

## Development Tools

### IDE & Extensions
- **IDE**: IntelliJ IDEA 2024.3
- **Kotlin Plugin**: Latest stable
- **Database Tools**: DataGrip or IDE database tools

### Code Quality
- **Linting**: Detekt 1.23+ for Kotlin
- **Formatting**: ktlint 12.1+
- **Testing**: JUnit 5.11.3, MockK 1.13.13, Testcontainers 1.20.4

### API Documentation
- **OpenAPI**: 3.1 specification
- **Swagger UI**: Interactive API docs (SpringDoc 2.7.0)

## Testing Strategy

### Backend Testing
- **Unit Tests**: JUnit 5.11.3 + MockK 1.13.13
- **Integration Tests**: Testcontainers 1.20.4
- **Performance Tests**: Gatling 3.13.1
- **Assertion Library**: AssertJ 3.26.3

### Frontend Testing
- **Unit Tests**: Jest 30.0+ + React Testing Library 16.1+
- **E2E Tests**: Playwright 1.49+
- **Component Tests**: Storybook 8.5+

## Deployment

### Environment
- **Target**: On-premise deployment
- **OS**: Linux (Ubuntu 24.04 LTS / RHEL 9)
- **Java Runtime**: Docker containers with Java 21

### Configuration
- **Config Management**: Spring Cloud Config
- **Environment Variables**: Docker env files
- **Secrets**: External file mounts

## Performance Requirements

- **File Processing**: >100 files/hour
- **Record Throughput**: >100k records/minute
- **API Response**: <200ms p95
- **Dashboard Update**: <100ms latency
- **Memory per Service**: <2GB

## Version Summary

| Component | Version |
|-----------|---------|
| Kotlin | 2.0.21 |
| Spring Boot | 3.3.5 |
| PostgreSQL | 16+ |
| ClickHouse | 24.11 |
| Redis | 7.4+ |
| Keycloak | 26.0+ |
| React | 18.3+ |
| TypeScript | 5.7+ |