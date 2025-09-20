# ZTE ASN.1 Parser

Kotlin/Spring Boot parser for ZXUN CG ASN.1 CDR files. Implements the Parser API from `core/parser-api` and can be packaged as a plugin for the Enterprise ETL Engine.

Repository: https://github.com/diorwave/Enterprise-ETL-Engine

## Features

- ASN.1 decoding for ZXUN CG CDRs (PGW/SGW records in `pgw_r8_new` spec).
- Batch inserts to ClickHouse via JDBC.
- Validation, metrics, and health endpoints (Spring Boot Actuator).
- Testcontainers-based integration tests for ClickHouse/PostgreSQL.

## Build & Test

```bash
./gradlew :parsers:zte-asn1-parser:clean :parsers:zte-asn1-parser:build
./gradlew :parsers:zte-asn1-parser:test
# Optional integration tests (require Docker)
./gradlew :parsers:zte-asn1-parser:integrationTest
```

JAR output: `parsers/zte-asn1-parser/build/libs/`.

## Using as a Plugin

1) Build the JAR (see above).  
2) Point the engine/file-scanner to the JAR location (e.g., a shared parsers directory).  
3) Ensure ClickHouse/PostgreSQL credentials are available to the parser if it writes directly to databases.

Minimal usage snippet:
```kotlin
val parser = ZteAsn1ParserImpl()
val result = parser.process(processingContext.copy(inputFile = file))
println("Processed ${result.recordsProcessed} records, status=${result.status}")
```

## Configuration Hints

- ClickHouse URL/credentials are read from Spring Boot properties when running the parser as a standalone app.
- Default metrics: Prometheus endpoint via Actuator.
- Adjust batch size and file size limits in your deployment config to match expected traffic.

## Support & Contributions

- Open issues/PRs in the main repository with logs and sample data (sanitized).
- Maintainer: diorwave (Dior).

## License

Proprietary. Contact the maintainer for usage and distribution questions.