# Contributing to Enterprise ETL Engine

Thank you for considering a contribution. This document summarizes how to work on the repository, run checks, and open changes safely.

## Project scope
- Telecom-focused ETL platform with pluggable parsers.
- Modules: `core/parser-api`, `services/file-scanner`, `parsers/zte-asn1-parser`.
- Primary maintainer: **diorwave (Dior)**.

## Prerequisites
- Java 21, Kotlin 2.0.21
- Docker + Docker Compose (for integration/e2e tests and local infra)
- Git and Gradle wrapper (`./gradlew`)

## Setup
```bash
./gradlew clean build        # full build
docker-compose -f deployment/docker-compose.yml up -d  # start infra
```

Optional local overrides for the scanner:
```bash
cp services/file-scanner/src/main/resources/application-local.yml.example \
   services/file-scanner/src/main/resources/application-local.yml
```

## Building and testing
- All modules: `./gradlew clean build`
- Targeted:
  - Parser API: `./gradlew :core:parser-api:test`
  - File Scanner: `./gradlew :services:file-scanner:test -Pprofile=unit|integration|e2e|all`
  - ZTE Parser: `./gradlew :parsers:zte-asn1-parser:test` (integration: `:integrationTest`)
- Integration/E2E tests use Testcontainers; ensure Docker is running.
- Coverage: enabled for parser modules; JaCoCo is currently disabled in file-scanner due to Java compatibility.

## Coding guidelines
- Follow Kotlin/Java style in `.editorconfig` and `.agent-os/standards/*`.
- Keep changes small and focused; prefer modular commits per module or feature.
- Add or update tests alongside functional changes.
- Avoid committing large or sensitive datasets; redact telecom PII.

## Git and commits
- Branch naming suggestion: `feat/<topic>`, `fix/<issue>`, `chore/<area>`.
- Commit messages: imperative, clear scope (e.g., `Add queue metrics endpoint`).
- If touching build or workflows, note rationale in the commit body.

## Pull requests
- Describe motivation, scope, and testing performed.
- Checklist:
  - [ ] Unit tests (or note why not)
  - [ ] Integration/E2E when relevant
  - [ ] Docs/config updated if behavior changes
  - [ ] No secrets or sample customer data added

## Dependencies and publishing
- Parser API and ZTE parser can publish to GitHub Packages; set `GITHUB_ACTOR` / `GITHUB_TOKEN` when needed.
- Keep dependency upgrades in separate commits; run security scans when modifying `parsers/zte-asn1-parser` (OWASP Dependency Check is configured).

## Reporting issues
- Include context, reproduction steps, expected vs. actual behavior, and relevant logs (sanitized).

## Contact
- For questions or access requests, reach out to **diorwave (Dior)** via the repository issue tracker or listed maintainer contacts.
