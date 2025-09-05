#!/bin/bash

# DevOps Agent Invocation Script
# Usage: ./devops-invoke.sh <command> [options]

set -e

AGENT_NAME="devops-agent"
CONFIG_FILE=".agent-os/agents/devops-config.yaml"
PROMPTS_FILE=".agent-os/agents/devops-prompts.md"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to show help
show_help() {
    cat << EOF
DevOps Agent for TIA ETL System

Usage: $0 <command> [options]

Commands:
    setup-ci        Create CI/CD pipelines for a service
    dockerize       Create Docker configuration for a service
    deploy          Deploy service to an environment
    rollback        Rollback a deployment
    monitor         Setup monitoring for a service
    troubleshoot    Debug infrastructure issues
    backup          Create backups of databases
    restore         Restore from backup
    security-scan   Run security scans
    optimize        Optimize resource usage
    status          Show infrastructure status
    help            Show this help message

Options:
    --service NAME      Service name
    --env ENV          Environment (dev/staging/prod)
    --version VERSION  Version to deploy/rollback to

Examples:
    $0 setup-ci --service file-scanner
    $0 deploy --service parser-orchestrator --env staging
    $0 rollback --service job-manager --env prod --version v1.2.3
    $0 monitor --service monitoring-service
    $0 status --env prod

EOF
}

# Function to setup CI/CD pipeline
setup_ci() {
    local service=$1
    log_info "Setting up CI/CD pipeline for $service..."
    
    # Create GitHub Actions workflow
    mkdir -p .github/workflows
    
    cat > .github/workflows/${service}.yml << 'EOF'
name: CI/CD Pipeline - ${service}

on:
  push:
    branches: [ main, develop ]
    paths:
      - 'services/${service}/**'
      - '.github/workflows/${service}.yml'
  pull_request:
    branches: [ main ]
    paths:
      - 'services/${service}/**'

env:
  REGISTRY: ghcr.io
  IMAGE_NAME: quantum-soft-dev/tia-etl/${service}

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      
      - name: Cache Gradle dependencies
        uses: actions/cache@v3
        with:
          path: ~/.gradle/caches
          key: gradle-${{ hashFiles('**/*.gradle*') }}
      
      - name: Run tests
        run: |
          cd services/${service}
          ./gradlew test
      
      - name: Generate test report
        uses: dorny/test-reporter@v1
        if: success() || failure()
        with:
          name: Test Results
          path: services/${service}/build/test-results/test/*.xml
          reporter: java-junit

  build:
    needs: test
    runs-on: ubuntu-latest
    if: github.event_name == 'push'
    
    steps:
      - uses: actions/checkout@v4
      
      - name: Set up Docker Buildx
        uses: docker/setup-buildx-action@v3
      
      - name: Log in to GitHub Container Registry
        uses: docker/login-action@v3
        with:
          registry: ${{ env.REGISTRY }}
          username: ${{ github.actor }}
          password: ${{ secrets.GITHUB_TOKEN }}
      
      - name: Extract metadata
        id: meta
        uses: docker/metadata-action@v5
        with:
          images: ${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}
          tags: |
            type=ref,event=branch
            type=ref,event=pr
            type=semver,pattern={{version}}
            type=sha
      
      - name: Build and push Docker image
        uses: docker/build-push-action@v5
        with:
          context: services/${service}
          push: true
          tags: ${{ steps.meta.outputs.tags }}
          labels: ${{ steps.meta.outputs.labels }}
          cache-from: type=gha
          cache-to: type=gha,mode=max

  security-scan:
    needs: build
    runs-on: ubuntu-latest
    
    steps:
      - name: Run Trivy vulnerability scanner
        uses: aquasecurity/trivy-action@master
        with:
          image-ref: ${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}:${{ github.sha }}
          format: 'sarif'
          output: 'trivy-results.sarif'
      
      - name: Upload Trivy results to GitHub Security
        uses: github/codeql-action/upload-sarif@v2
        with:
          sarif_file: 'trivy-results.sarif'

  deploy-staging:
    needs: [build, security-scan]
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    environment: staging
    
    steps:
      - uses: actions/checkout@v4
      
      - name: Deploy to staging
        run: |
          # Deploy using kubectl or ArgoCD
          echo "Deploying to staging..."
          kubectl set image deployment/${service} ${service}=${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}:${{ github.sha }} -n tia-staging
          kubectl rollout status deployment/${service} -n tia-staging

  deploy-production:
    needs: deploy-staging
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    environment: production
    
    steps:
      - uses: actions/checkout@v4
      
      - name: Deploy to production
        run: |
          # Deploy using kubectl or ArgoCD with canary
          echo "Deploying to production with canary..."
          # Implement canary deployment logic here
EOF
    
    log_success "CI/CD pipeline created for $service"
}

# Function to dockerize a service
dockerize() {
    local service=$1
    log_info "Creating Docker configuration for $service..."
    
    cat > services/${service}/Dockerfile << 'EOF'
# Multi-stage build for Kotlin Spring Boot application
FROM gradle:8.5-jdk17 AS builder

WORKDIR /app

# Copy gradle files for dependency caching
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle ./gradle

# Download dependencies
RUN gradle dependencies --no-daemon

# Copy source code
COPY src ./src

# Build application
RUN gradle bootJar --no-daemon

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

# Install dumb-init for proper signal handling
RUN apk add --no-cache dumb-init

# Create non-root user
RUN addgroup -g 1000 appuser && \
    adduser -D -u 1000 -G appuser appuser

WORKDIR /app

# Copy jar from builder
COPY --from=builder --chown=appuser:appuser /app/build/libs/*.jar app.jar

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1

USER appuser

EXPOSE 8080

ENTRYPOINT ["dumb-init", "--"]
CMD ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
EOF
    
    log_success "Docker configuration created for $service"
}

# Function to deploy a service
deploy() {
    local service=$1
    local environment=$2
    local version=${3:-latest}
    
    log_info "Deploying $service version $version to $environment..."
    
    # Check if deployment exists
    if kubectl get deployment $service -n tia-$environment &>/dev/null; then
        # Update existing deployment
        kubectl set image deployment/$service $service=ghcr.io/quantum-soft-dev/tia-etl/$service:$version -n tia-$environment
        kubectl rollout status deployment/$service -n tia-$environment
    else
        # Create new deployment
        kubectl apply -f k8s/$environment/$service.yaml
    fi
    
    log_success "$service deployed to $environment"
}

# Function to rollback a deployment
rollback() {
    local service=$1
    local environment=$2
    local version=$3
    
    log_info "Rolling back $service in $environment to version $version..."
    
    if [ -z "$version" ]; then
        # Rollback to previous version
        kubectl rollout undo deployment/$service -n tia-$environment
    else
        # Rollback to specific version
        kubectl set image deployment/$service $service=ghcr.io/quantum-soft-dev/tia-etl/$service:$version -n tia-$environment
    fi
    
    kubectl rollout status deployment/$service -n tia-$environment
    
    log_success "$service rolled back in $environment"
}

# Function to setup monitoring
monitor() {
    local service=$1
    log_info "Setting up monitoring for $service..."
    
    # Create ServiceMonitor for Prometheus
    cat > k8s/monitoring/servicemonitor-${service}.yaml << EOF
apiVersion: monitoring.coreos.com/v1
kind: ServiceMonitor
metadata:
  name: ${service}
  namespace: monitoring
spec:
  selector:
    matchLabels:
      app: ${service}
  endpoints:
  - port: metrics
    interval: 30s
    path: /actuator/prometheus
EOF
    
    # Create Grafana dashboard
    cat > k8s/monitoring/dashboard-${service}.json << EOF
{
  "dashboard": {
    "title": "${service} Dashboard",
    "panels": [
      {
        "title": "Request Rate",
        "targets": [
          {
            "expr": "rate(http_server_requests_seconds_count{service=\"${service}\"}[5m])"
          }
        ]
      },
      {
        "title": "Response Time",
        "targets": [
          {
            "expr": "histogram_quantile(0.95, rate(http_server_requests_seconds_bucket{service=\"${service}\"}[5m]))"
          }
        ]
      },
      {
        "title": "Error Rate",
        "targets": [
          {
            "expr": "rate(http_server_requests_seconds_count{service=\"${service}\",status=~\"5..\"}[5m])"
          }
        ]
      }
    ]
  }
}
EOF
    
    kubectl apply -f k8s/monitoring/servicemonitor-${service}.yaml
    
    log_success "Monitoring setup completed for $service"
}

# Function to show infrastructure status
show_status() {
    local environment=$1
    
    log_info "Infrastructure status for $environment environment:"
    
    echo ""
    echo "=== Deployments ==="
    kubectl get deployments -n tia-$environment
    
    echo ""
    echo "=== Pods ==="
    kubectl get pods -n tia-$environment
    
    echo ""
    echo "=== Services ==="
    kubectl get services -n tia-$environment
    
    echo ""
    echo "=== Ingress ==="
    kubectl get ingress -n tia-$environment
    
    echo ""
    echo "=== HPA ==="
    kubectl get hpa -n tia-$environment
    
    echo ""
    echo "=== Recent Events ==="
    kubectl get events -n tia-$environment --sort-by='.lastTimestamp' | head -20
}

# Parse command line arguments
COMMAND=$1
shift

SERVICE=""
ENVIRONMENT=""
VERSION=""

while [[ $# -gt 0 ]]; do
    case $1 in
        --service)
            SERVICE="$2"
            shift 2
            ;;
        --env)
            ENVIRONMENT="$2"
            shift 2
            ;;
        --version)
            VERSION="$2"
            shift 2
            ;;
        *)
            log_error "Unknown option: $1"
            show_help
            exit 1
            ;;
    esac
done

# Execute command
case $COMMAND in
    setup-ci)
        [ -z "$SERVICE" ] && { log_error "Service name required"; exit 1; }
        setup_ci "$SERVICE"
        ;;
    dockerize)
        [ -z "$SERVICE" ] && { log_error "Service name required"; exit 1; }
        dockerize "$SERVICE"
        ;;
    deploy)
        [ -z "$SERVICE" ] && { log_error "Service name required"; exit 1; }
        [ -z "$ENVIRONMENT" ] && { log_error "Environment required"; exit 1; }
        deploy "$SERVICE" "$ENVIRONMENT" "$VERSION"
        ;;
    rollback)
        [ -z "$SERVICE" ] && { log_error "Service name required"; exit 1; }
        [ -z "$ENVIRONMENT" ] && { log_error "Environment required"; exit 1; }
        rollback "$SERVICE" "$ENVIRONMENT" "$VERSION"
        ;;
    monitor)
        [ -z "$SERVICE" ] && { log_error "Service name required"; exit 1; }
        monitor "$SERVICE"
        ;;
    status)
        [ -z "$ENVIRONMENT" ] && ENVIRONMENT="prod"
        show_status "$ENVIRONMENT"
        ;;
    help)
        show_help
        ;;
    *)
        log_error "Unknown command: $COMMAND"
        show_help
        exit 1
        ;;
esac