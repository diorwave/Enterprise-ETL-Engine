# Deployment Configuration

## Quick Start

Start all infrastructure services:

```bash
docker-compose up -d
```

## Services

### PostgreSQL
- **Version**: 16 Alpine
- **Root User**: postgres / postgres_root_password
- **Databases**:
  - `tia_etl` - Main ETL database (user: tia_user / tia_password)
  - `keycloak` - Keycloak database (user: keycloak / keycloak_password)
- **Port**: 5432
- **Init Script**: `postgres/init.sql`

### ClickHouse
- **Version**: 24.11
- **Database**: tia_etl
- **User**: tia_user / tia_password
- **Ports**: 
  - HTTP: 8123
  - Native: 9000
- **Init Script**: `clickhouse/init.sql`

### Redis
- **Version**: 7.4 Alpine
- **Port**: 6379
- **Persistence**: AOF enabled

### Keycloak
- **Version**: 26.0
- **Admin**: admin / admin
- **Database**: PostgreSQL (keycloak database)
- **Port**: 8080
- **Mode**: Development (for local)

## Health Checks

All services include health checks. Check status:

```bash
docker-compose ps
```

## Data Persistence

Data is persisted in Docker volumes:
- `postgres_data` - PostgreSQL data
- `clickhouse_data` - ClickHouse data  
- `redis_data` - Redis AOF files

## Troubleshooting

View logs:
```bash
docker-compose logs -f [service_name]
```

Restart service:
```bash
docker-compose restart [service_name]
```

Reset all data:
```bash
docker-compose down -v
docker-compose up -d
```