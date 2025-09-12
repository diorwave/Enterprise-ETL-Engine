# Parser API (Enterprise ETL Engine)

Lightweight Kotlin interfaces and models for building parser plugins used by the Enterprise ETL Engine.

Repository: https://github.com/diorwave/Enterprise-ETL-Engine

## Getting Started

Build and publish to your local Maven repository:
```bash
./gradlew :core:parser-api:clean :core:parser-api:publishToMavenLocal
```

Consume from another Gradle project:
```kotlin
repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("com.quantum.etl:parser-api:1.0.0")
}
```

## Key Interfaces & Models

- `DataParser`: main interface for processing input files.
- `ParserLifecycle`: optional lifecycle hooks for initialization/cleanup.
- `ParserMetadata`, `ProcessingContext`, `ProcessingResult`, `ValidationResult`, `JobConfiguration`, `TableSchema`: core data structures used by the engine and plugins.

## Development Workflow

1. Implement `DataParser` (and optionally `ParserLifecycle`).
2. Add validation and error handling that returns `ProcessingResult` rather than throwing.
3. Write unit tests for metadata, validation, and processing paths.
4. Package your parser as a JAR and load it where the engine expects plugins.

## Testing

```bash
./gradlew :core:parser-api:test
./gradlew :core:parser-api:jacocoTestReport   # coverage report
```

## Documentation

Use KDoc in the source or generate HTML docs:
```bash
./gradlew :core:parser-api:dokkaHtml
```

## Support

- Open issues/PRs in the main repository.
- Maintainer: diorwave (Dior).

## License

Proprietary. Contact the maintainer for usage and distribution questions.