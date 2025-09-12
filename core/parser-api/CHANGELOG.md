# Changelog

All notable changes to the TIA ETL Parser API will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- CI/CD pipeline for automated publishing to GitHub Packages
- Comprehensive documentation with usage examples
- Gradle publishing configuration with Maven metadata

### Changed
- Enhanced README with installation and authentication instructions
- Improved build configuration with reproducible builds

## [1.0.0] - 2025-08-28

### Added
- Initial release of the Parser API
- Core interfaces:
  - `DataParser` - Main parsing interface
  - `ParserLifecycle` - Optional lifecycle management
- Data models:
  - `ParserMetadata` - Parser identification and capabilities
  - `ProcessingContext` - Runtime context for parsers
  - `ProcessingResult` - Success/failure results with statistics
  - `ValidationResult` - File validation outcomes
  - `JobConfiguration` - Job-specific configuration
  - `TableSchema` - Target database schema definition
- Exception types:
  - `ParserException` - Base parser exception
  - `ValidationException` - Validation-specific exception
- Example parser implementation for testing and reference
- Comprehensive unit tests with 90%+ coverage
- Thread-safe design for concurrent processing
- Coroutine support for async operations

### Features
- **Plugin Architecture**: Dynamic parser loading and discovery
- **Configuration Schema**: Typed configuration validation
- **Database Integration**: Built-in ClickHouse and PostgreSQL support  
- **Error Handling**: Structured error reporting and logging
- **Performance**: Batch processing with configurable sizes
- **Monitoring**: Processing metrics and statistics
- **Validation**: Pre-processing file validation
- **Lifecycle Management**: Optional initialization and cleanup hooks

### Technical Details
- **Language**: Kotlin 2.0.21
- **JVM Target**: Java 21
- **Dependencies**: SLF4J for logging
- **Testing**: JUnit 5, MockK, AssertJ
- **Build Tool**: Gradle with Kotlin DSL
- **Package Format**: Standard JAR with sources and Javadoc

### Documentation
- Complete API documentation with KDoc
- Usage examples for common scenarios
- Testing guidelines and best practices
- Deployment instructions
- Performance optimization tips

---

## Release Notes Format

Each release will include:

### Added
- New features and capabilities

### Changed  
- Modifications to existing functionality
- Improvements and enhancements

### Deprecated
- Features marked for removal in future versions

### Removed
- Deleted features and breaking changes

### Fixed
- Bug fixes and corrections

### Security
- Security-related changes and patches

---

## Version Naming Convention

- **Major version** (X.0.0): Breaking API changes
- **Minor version** (X.Y.0): New features, backwards compatible
- **Patch version** (X.Y.Z): Bug fixes, backwards compatible

## Release Tags

- Format: `parser-api-vX.Y.Z`
- Example: `parser-api-v1.0.0`

## Backwards Compatibility

The Parser API maintains backwards compatibility within major versions:

- **1.x.x**: All versions are compatible
- **2.x.x**: Breaking changes from 1.x.x (when introduced)
- **Deprecation**: Features are deprecated for one minor version before removal

## Migration Guides

When breaking changes are introduced, migration guides will be provided:

- **From 1.x to 2.x**: (Future, if needed)
- Step-by-step migration instructions
- Code examples showing before/after
- Timeline for deprecated feature removal

## Support Policy

- **Current version**: Full support with new features and bug fixes
- **Previous major version**: Security fixes and critical bug fixes for 1 year
- **Older versions**: Community support only

## Contributing to Changelog

When contributing changes:

1. Add entries to `[Unreleased]` section
2. Use the appropriate category (Added, Changed, Fixed, etc.)
3. Include issue/PR numbers when relevant
4. Follow the established format and style
5. Move entries to versioned section upon release