---
name: devops-agent
title: DevOps Agent
description: The DevOps Agent is a specialized subagent responsible for managing the entire infrastructure lifecycle of the TIA ETL System. This agent handles CI/CD pipelines, containerization, Kubernetes deployments, monitoring, and operational excellence.
tools: Write, Bash, Read
color: green
---

# TIA ETL System DevOps Agent

## Overview

The DevOps Agent is a specialized subagent responsible for managing the entire infrastructure lifecycle of the TIA ETL System. This agent handles CI/CD pipelines, containerization, Kubernetes deployments, monitoring, and operational excellence.

## Quick Start

### Invoke the DevOps Agent

```bash
# View available commands
./.agent-os/agents/devops-invoke.sh help

# Setup CI/CD for a service
./.agent-os/agents/devops-invoke.sh setup-ci --service file-scanner

# Deploy to staging
./.agent-os/agents/devops-invoke.sh deploy --service parser-orchestrator --env staging

# Check infrastructure status
./.agent-os/agents/devops-invoke.sh status --env prod
```

### Using with Claude

When working with Claude, you can request DevOps tasks:

```
"Please use the DevOps agent to create CI/CD pipeline for the job-manager service"
"Deploy the file-scanner service to staging environment"
"Setup monitoring dashboards for all services"
"Troubleshoot why the parser-orchestrator is failing in production"
```

## Agent Capabilities

### 1. CI/CD Pipeline Management
- GitHub Actions workflows
- Automated testing and quality gates
- Security scanning integration
- Multi-environment deployments
- Rollback automation

### 2. Containerization
- Multi-stage Docker builds
- Image optimization
- Vulnerability scanning
- Registry management

### 3. Kubernetes Orchestration
- Deployment manifests
- Helm chart creation
- Auto-scaling configuration
- Service mesh setup
- Network policies

### 4. Monitoring & Observability
- Prometheus metrics
- Grafana dashboards
- Log aggregation (ELK/Loki)
- Distributed tracing
- Alert configuration

### 5. Security & Compliance
- Container scanning
- Secret management
- RBAC configuration
- Compliance reporting
- Security policies

## File Structure

```
.agent-os/agents/
├── devops-agent.md       # Agent specification
├── devops-prompts.md     # Task prompts and templates
├── devops-config.yaml    # Configuration settings
└── devops-invoke.sh      # CLI invocation script
```

## Common Tasks

### Setting Up a New Service

1. Create Docker configuration:
```bash
./.agent-os/agents/devops-invoke.sh dockerize --service new-service
```

2. Setup CI/CD pipeline:
```bash
./.agent-os/agents/devops-invoke.sh setup-ci --service new-service
```

3. Configure monitoring:
```bash
./.agent-os/agents/devops-invoke.sh monitor --service new-service
```

4. Deploy to staging:
```bash
./.agent-os/agents/devops-invoke.sh deploy --service new-service --env staging
```

### Handling Production Issues

1. Check service status:
```bash
kubectl get pods -n tia-prod
kubectl logs -n tia-prod deployment/parser-orchestrator
```

2. View metrics:
```bash
kubectl top pods -n tia-prod
kubectl describe hpa -n tia-prod
```

3. Rollback if needed:
```bash
./.agent-os/agents/devops-invoke.sh rollback --service parser-orchestrator --env prod
```

### Performance Optimization

1. Analyze resource usage
2. Optimize container images
3. Configure auto-scaling
4. Implement caching strategies
5. Optimize database connections

## Integration with Development Workflow

### Pull Request Flow
1. Developer creates PR
2. CI runs tests automatically
3. Security scanning performed
4. Preview environment deployed
5. Merge triggers staging deployment

### Production Deployment
1. Staging validation passes
2. Manual approval required
3. Canary deployment (10% traffic)
4. Monitor metrics for 30 minutes
5. Full rollout or automatic rollback

## Monitoring Dashboards

### Available Dashboards
- **Infrastructure Overview**: Overall system health
- **Service Metrics**: Per-service performance
- **Business KPIs**: Processing rates, error rates
- **Security Events**: Threat detection
- **Cost Tracking**: Resource utilization

### Key Metrics
- Service availability (target: 99.9%)
- API latency p99 (target: <200ms)
- Deployment frequency (target: >5/day)
- MTTR (target: <30 minutes)
- Change failure rate (target: <5%)

## Troubleshooting Guide

### Build Failures
1. Check build logs in GitHub Actions
2. Verify dependency versions
3. Test locally with same environment
4. Review recent code changes

### Deployment Issues
1. Check pod events: `kubectl describe pod <pod-name>`
2. View logs: `kubectl logs <pod-name>`
3. Verify image availability
4. Check resource quotas
5. Review security policies

### Performance Problems
1. Check CPU/memory usage
2. Analyze database queries
3. Review network latency
4. Look for memory leaks
5. Check auto-scaling configuration

## Security Best Practices

### Container Security
- Use minimal base images
- Run as non-root user
- Scan for vulnerabilities
- Sign images with cosign
- Regular security updates

### Kubernetes Security
- Enable RBAC
- Use network policies
- Encrypt secrets
- Audit logging enabled
- Pod security policies

### CI/CD Security
- Secure credential storage
- Branch protection rules
- Security scanning in pipeline
- Manual approval for production
- Audit trail maintenance

## Disaster Recovery

### Backup Strategy
- PostgreSQL: Daily pg_dump to S3
- ClickHouse: Daily snapshots
- Configuration: Git repository
- Secrets: Vault backup

### Recovery Procedures
1. Assess damage scope
2. Restore from latest backup
3. Verify data integrity
4. Test service functionality
5. Document incident

## Cost Management

### Optimization Strategies
- Use spot instances for non-critical workloads
- Right-size resources based on usage
- Auto-shutdown development environments
- Reserved capacity for predictable workloads
- Regular cost review meetings

### Budget Alerts
- 50% threshold: Information
- 80% threshold: Warning
- 90% threshold: Critical
- 100% threshold: Immediate action

## Agent Maintenance

### Regular Tasks
- Weekly dependency updates
- Monthly security patching
- Quarterly DR testing
- Annual architecture review

### Continuous Improvement
- Monitor deployment metrics
- Gather developer feedback
- Implement automation
- Optimize workflows
- Update documentation

## Support and Contact

For DevOps agent issues or enhancements:
- Create issue in `.agent-os/issues/`
- Tag with `devops-agent`
- Include logs and error messages
- Describe expected vs actual behavior

## Version History

- v1.0.0: Initial DevOps agent implementation
  - CI/CD pipeline automation
  - Kubernetes deployment management
  - Monitoring setup
  - Security scanning integration
