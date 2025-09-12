# Parser API Development Guide

## Quick Start for Developers

### Building the Library

```bash
# Basic build (without strict coverage)
./gradlew :core:parser-api:buildForDev

# Release build with all artifacts
./gradlew :core:parser-api:releaseBuild

# Build with strict coverage verification
./gradlew :core:parser-api:testWithCoverage
```

### Running Tests

```bash
# Run all tests
./gradlew :core:parser-api:test

# Run tests with coverage report
./gradlew :core:parser-api:jacocoTestReport

# Run tests with coverage verification
./gradlew :core:parser-api:testWithCoverage
```

### Publishing (CI/CD)

The library is automatically published to GitHub Packages when:

1. **Push to main branch** with changes in `core/parser-api/`
2. **Tags** matching `parser-api-v*` pattern
3. **Manual workflow dispatch** with version bump selection

### Local Development

#### Setup GitHub Packages Authentication

1. Create a Personal Access Token with `read:packages` permission
2. Copy `gradle.properties.template` to `gradle.properties`
3. Fill in your GitHub credentials:

```properties
gpr.user=your-github-username
gpr.key=your-personal-access-token
```

#### Building JARs

The build creates three JAR files:

- **Main JAR**: `parser-api-{version}.jar` - Contains compiled classes
- **Sources JAR**: `parser-api-{version}-sources.jar` - Contains source code
- **Javadoc JAR**: `parser-api-{version}-javadoc.jar` - Contains API documentation

### Project Structure

```
core/parser-api/
├── src/main/kotlin/           # Source code
│   └── com/tia/etl/parser/api/
├── src/test/kotlin/           # Unit tests
├── build.gradle.kts           # Build configuration
├── gradle.properties          # Version and settings
├── README.md                  # Usage documentation
├── CHANGELOG.md               # Version history
└── DEVELOPMENT.md            # This file
```

### Gradle Tasks

#### Build Tasks
- `buildForDev` - Development build without strict coverage
- `releaseBuild` - Production build with all artifacts
- `jar` - Create main JAR
- `sourcesJar` - Create sources JAR  
- `javadocJar` - Create documentation JAR

#### Test Tasks
- `test` - Run unit tests
- `testWithCoverage` - Run tests with coverage verification
- `jacocoTestReport` - Generate coverage report
- `jacocoTestCoverageVerification` - Verify coverage thresholds

#### Publishing Tasks
- `publishingCheck` - Verify ready for publishing
- `publish` - Publish to GitHub Packages
- `publishToMavenLocal` - Install to local Maven repository

### Code Quality Standards

#### Coverage Requirements (for CI)
- **Instruction Coverage**: ≥50%
- **Branch Coverage**: ≥40%

#### Code Style
- Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful names for classes, methods, and variables
- Add KDoc comments for public APIs
- Keep functions small and focused

#### Testing Guidelines
- Write unit tests for all public APIs
- Use descriptive test names
- Test both success and failure scenarios
- Use MockK for mocking dependencies
- Aim for comprehensive coverage of business logic

### Making Changes

#### Adding New Features

1. **Design First**: Update interfaces and data models
2. **Test Driven**: Write tests before implementation
3. **Documentation**: Update KDoc comments
4. **Examples**: Add usage examples in README
5. **Changelog**: Record changes in CHANGELOG.md

#### Version Bump Strategy

The CI/CD automatically determines version bumps:

- **BREAKING CHANGE** in commit message → Major version bump
- **feat:** prefix in commit message → Minor version bump  
- **fix:** prefix in commit message → Patch version bump
- Any other changes → Patch version bump

#### Commit Message Format

Use [Conventional Commits](https://www.conventionalcommits.org/):

```
feat: add validation for parser configuration
fix: handle null values in processing context
docs: update API documentation
test: add coverage for error scenarios
BREAKING CHANGE: remove deprecated method
```

### Troubleshooting

#### Build Issues

```bash
# Clean and rebuild
./gradlew :core:parser-api:clean :core:parser-api:build

# Check dependency issues
./gradlew :core:parser-api:dependencies

# Run with debug output
./gradlew :core:parser-api:build --info --stacktrace
```

#### Publishing Issues

1. **Authentication**: Verify GitHub token has correct permissions
2. **Version Format**: Ensure version follows semantic versioning (X.Y.Z)
3. **Network**: Check connection to GitHub Packages
4. **Permissions**: Ensure write access to repository

#### Test Coverage Issues

```bash
# View detailed coverage report
open core/parser-api/build/reports/jacoco/test/html/index.html

# Run specific test class
./gradlew :core:parser-api:test --tests "ProcessingResultTest"

# Run with verbose logging
./gradlew :core:parser-api:test --info
```

### IDE Configuration

#### IntelliJ IDEA

1. Import project using Gradle
2. Set Project SDK to Java 21
3. Enable Kotlin plugin
4. Configure code style to follow Kotlin conventions

#### VS Code

1. Install Kotlin extension
2. Install Gradle extension
3. Configure Java to use JDK 21

### Contributing

1. **Fork** the repository
2. **Branch** from main: `git checkout -b feature/my-feature`
3. **Test** your changes: `./gradlew :core:parser-api:test`
4. **Build** release: `./gradlew :core:parser-api:releaseBuild`
5. **Commit** with conventional format
6. **Push** and create Pull Request

### Release Process

#### Automatic Release (Recommended)

1. Push changes to main branch
2. CI automatically builds and tests
3. Version is calculated based on commit messages
4. Tagged and published to GitHub Packages
5. GitHub Release created with changelog

#### Manual Release

```bash
# Set version in gradle.properties
echo "version=1.1.0" > core/parser-api/gradle.properties

# Build and test
./gradlew :core:parser-api:releaseBuild

# Publish (requires authentication)
./gradlew :core:parser-api:publish

# Create git tag
git tag parser-api-v1.1.0
git push origin parser-api-v1.1.0
```

### Monitoring

After release, monitor:

1. **GitHub Actions** - Build and publish status
2. **GitHub Packages** - Published artifacts
3. **GitHub Releases** - Release notes and downloads
4. **Issues** - Bug reports and feature requests

### Support

- **Issues**: Create GitHub issues for bugs or features
- **Discussions**: Use GitHub Discussions for questions
- **Documentation**: Check README.md and code comments
- **Examples**: See example parser implementation