# Enterprise ETL Engine (TIA ETL System)

> Telecom-focused ETL pipeline with pluggable parsers and a Spring Boot file-scanner service.

## Overview

The project processes telecom data files (ASN.1 and CSV) and is structured as:
- **Parser API (`core/parser-api`)**: Kotlin interfaces and models for parser plugins.
- **File Scanner (`services/file-scanner`)**: Spring Boot service that schedules directory scans, validates files, queues work in Redis, and tracks job metadata in PostgreSQL.
- **ZTE ASN.1 Parser (`parsers/zte-asn1-parser`)**: Parser implementation for ZXUN CG CDR files built on the Parser API.

## Current Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    Web Console (React)                  │
├─────────────────────────────────────────────────────────┤
│                   Job Manager Service                   │
├──────────────┬────────────────┬─────────────────────────┤
│ File Scanner │ Parser         │ Monitoring              │
│   Service    │ Orchestrator   │   Service               │
├──────────────┴────────────────┴─────────────────────────┤
│            Redis Queue & Distributed Locks              │
├─────────────────────────────────────────────────────────┤
│     PostgreSQL          │        ClickHouse             │
└─────────────────────────────────────────────────────────┘
```

## Prerequisites

- Java 21+
- Docker & Docker Compose
- PostgreSQL 16+, ClickHouse (optional for parsers), Redis 7+
- Bash-compatible shell (examples use Git Bash on Windows)

## Quick Start (local)

1) Clone and enter the repo
```bash
git clone https://github.com/diorwave/Enterprise-ETL-Engine.git
cd Enterprise-ETL-Engine
```

2) Start infrastructure
```bash
docker-compose -f deployment/docker-compose.yml up -d
```

3) (Optional) Prepare a local override for the File Scanner
```bash
cp services/file-scanner/src/main/resources/application-local.yml.example \
   services/file-scanner/src/main/resources/application-local.yml
# Export custom DB/Redis values if not using defaults
export DB_URL="jdbc:postgresql://localhost:5432/tia_etl"
export DB_USER="tia_user"
export DB_PASSWORD="tia_password"
export REDIS_HOST="localhost"
export REDIS_PORT="6379"
```

4) Build everything
```bash
./gradlew clean build
```

5) Run the File Scanner service
```bash
./gradlew :services:file-scanner:bootRun
```
Swagger UI: http://localhost:8081/file-scanner/swagger-ui.html  
Health: http://localhost:8081/file-scanner/actuator/health

6) Build parser artifacts (optional)
```bash
./gradlew :core:parser-api:publishToMavenLocal
./gradlew :parsers:zte-asn1-parser:build
```
The ZTE parser JAR will be under `parsers/zte-asn1-parser/build/libs/`.

## Configuration

Key settings for the File Scanner live in `services/file-scanner/src/main/resources/application.yml`:
- `spring.datasource` / `spring.data.redis`: database and Redis endpoints.
- `scanner.scan.*`: timeouts, concurrent scan limits, and file size limits.
- `scanner.queue.*`: batching and lock timeouts for queued work.
- `management` / `springdoc`: actuator and OpenAPI exposure.

Liquibase migrations for the scanner run automatically on startup.

## Building Parsers

- Implement `DataParser` (and optionally `ParserLifecycle`) from `core/parser-api`.
- Package as a JAR; the scanner can load parser plugins from your preferred location.
- For local development, consume the API via `mavenLocal()` after running `publishToMavenLocal`.

## Testing

```bash
# All unit tests
./gradlew test

# Module-specific tests
./gradlew :core:parser-api:test
./gradlew :services:file-scanner:test
./gradlew :parsers:zte-asn1-parser:test
```

## Documentation

- `docs/file-scanner-description.md` — high-level notes on the scanner.
- `deployment/README.md` — infrastructure services.
- Module READMEs inside `core/parser-api` and `parsers/zte-asn1-parser`.

## Contributing

Issues and PRs are welcome. Please open tickets with clear reproduction steps or context. Primary maintainer: **diorwave (Dior)**.

## License

Proprietary. Contact the maintainer for usage or distribution questions.

## Repository

https://github.com/diorwave/Enterprise-ETL-Engine
