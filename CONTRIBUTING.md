# Contributing to TIA ETL System

## Development Process

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## Development Setup

1. Install prerequisites:
   - JDK 21
   - Docker & Docker Compose
   - Node.js 20 LTS

2. Clone repository:
```bash
git clone https://github.com/quantum-soft-dev/tia-etl-system.git
cd tia-etl-system
```

3. Start infrastructure:
```bash
docker-compose -f deployment/docker-compose.yml up -d
```

4. Build project:
```bash
./gradlew build
```

## Code Style

- Follow Kotlin coding conventions
- Use ktlint for formatting
- Write tests for new features
- Update documentation

## Commit Messages

Follow conventional commits:
- `feat:` New feature
- `fix:` Bug fix
- `docs:` Documentation changes
- `style:` Code style changes
- `refactor:` Code refactoring
- `test:` Test additions/changes
- `chore:` Build/auxiliary changes

## Testing

Run tests before submitting PR:
```bash
./gradlew test
```

## Agent OS

Use Agent OS for code generation:
```bash
claude -p "Use /create-spec for [component]"
claude -p "Use /execute-tasks to implement [component]"
```