# News Search Microservice - Deployment Guide

This guide provides comprehensive instructions for building and deploying the News Search Microservice using CI/CD pipelines and Docker containers.

## 🏗️ CI/CD Pipeline Overview

Our CI/CD pipeline is implemented using Jenkins and includes the following stages:

### Continuous Integration (CI)
1. **Checkout** - Source code retrieval
2. **Build** - Maven compilation
3. **Unit Tests** - JUnit and Mockito tests
4. **Integration Tests** - End-to-end API testing
5. **BDD Tests** - Cucumber behavior-driven tests
6. **Code Quality Analysis** - SonarQube and OWASP security scans
7. **Package** - JAR file creation
8. **Docker Build** - Container image creation

### Continuous Deployment (CD)
1. **Security Scan** - Docker image vulnerability scanning
2. **Deploy to Staging** - Automated staging deployment
3. **Smoke Tests** - Basic functionality verification
4. **Performance Tests** - JMeter load testing
5. **Deploy to Production** - Manual approval + blue-green deployment

## 📋 Prerequisites

### Required Software
- **Java 17** or higher
- **Maven 3.9.4** or higher
- **Node.js 18** or higher
- **Docker** and **Docker Compose**
- **Git**
- **Jenkins** (for CI/CD pipeline)

### Environment Variables
```bash
export NEWS_API_KEY=your_newsapi_key_here
export DOCKER_REGISTRY=your_docker_registry
export GRAFANA_PASSWORD=your_grafana_password
export REDIS_PASSWORD=your_redis_password
```

## 🚀 Quick Deployment

### Option 1: Automated Deployment Script

#### Linux/macOS:
```bash
# Make script executable
chmod +x deploy.sh

# Deploy to development
./deploy.sh dev

# Deploy to staging
./deploy.sh staging

# Deploy to production
./deploy.sh prod
```

#### Windows:
```cmd
# Deploy to development
deploy.bat dev

# Deploy to staging
deploy.bat staging

# Deploy to production
deploy.bat prod
```

### Option 2: Manual Docker Deployment

#### Development Environment:
```bash
# Build and start services
docker-compose up -d

# View logs
docker-compose logs -f

# Stop services
docker-compose down
```

#### Staging Environment:
```bash
docker-compose -f docker-compose.staging.yml up -d
```

#### Production Environment:
```bash
docker-compose -f docker-compose.prod.yml up -d
```

## 🏭 Jenkins CI/CD Pipeline Setup

### 1. Jenkins Configuration

#### Required Plugins:
- Pipeline
- Docker Pipeline
- Maven Integration
- JUnit
- Jacoco
- HTML Publisher
- Email Extension

#### Credentials Setup:
```
news-api-key: NewsAPI.org API key
docker-hub-credentials: Docker Hub login
sonarqube-token: SonarQube authentication token
```

### 2. Pipeline Configuration

Create a new Pipeline job in Jenkins with the following settings:

- **Pipeline Definition**: Pipeline script from SCM
- **SCM**: Git
- **Repository URL**: Your Git repository URL
- **Script Path**: `Jenkinsfile`

### 3. Webhook Configuration

Configure Git webhooks to trigger builds automatically:
- **Payload URL**: `http://your-jenkins-url/github-webhook/`
- **Content Type**: `application/json`
- **Events**: Push, Pull Request

## 🐳 Docker Container Details

### Backend Container (Spring Boot)
- **Base Image**: `openjdk:17-jre-slim`
- **Port**: 8080
- **Health Check**: `/api/v1/news/health`
- **Memory Limit**: 512MB (production)
- **Security**: Non-root user execution

### Frontend Container (React + Nginx)
- **Base Image**: `nginx:alpine`
- **Port**: 80/443
- **Health Check**: `/health`
- **Features**: Gzip compression, security headers, API proxy

### Key Features:
- **Multi-stage builds** for optimized image sizes
- **Health checks** for container orchestration
- **Non-root user execution** for security
- **Resource limits** for production stability
- **Logging configuration** for monitoring

## 🏢 Environment-Specific Configurations

### Development (`docker-compose.yml`)
- Single replica
- Debug logging enabled
- Development API key fallback
- Port 8080 exposed

### Staging (`docker-compose.staging.yml`)
- Staging-specific configuration
- Reduced resource allocation
- Faster health checks
- Separate network

### Production (`docker-compose.prod.yml`)
- Multiple replicas (2)
- Resource limits and reservations
- Production logging levels
- Load balancer configuration
- Monitoring stack (Prometheus + Grafana)
- Redis for caching

## 📊 Monitoring and Observability

### Prometheus Metrics
- Application metrics via `/actuator/prometheus`
- JVM metrics (memory, GC, threads)
- HTTP request metrics
- Custom business metrics

### Grafana Dashboards
- Application performance dashboard
- Infrastructure monitoring
- Error rate and latency tracking
- Business KPI visualization

### Health Checks
- **Backend**: `http://localhost:8080/api/v1/news/health`
- **Frontend**: `http://localhost:80/health`
- **Prometheus**: `http://localhost:9090/-/healthy`
- **Grafana**: `http://localhost:3000/api/health`

## 🔧 Build Commands

### Backend Build
```bash
# Clean and compile
mvn clean compile

# Run tests
mvn test

# Package application
mvn package -DskipTests

# Build Docker image
docker build -t news-search-microservice:latest .
```

### Frontend Build
```bash
cd frontend

# Install dependencies
npm ci

# Run tests
npm test -- --coverage --watchAll=false

# Build production bundle
npm run build

# Build Docker image
docker build -t news-frontend:latest .
```

## 🧪 Testing Strategy

### Unit Tests
- **Framework**: JUnit 5 + Mockito
- **Coverage**: >80% target
- **Location**: `src/test/java`
- **Command**: `mvn test`

### Integration Tests
- **Framework**: Spring Boot Test
- **Database**: H2 in-memory
- **Location**: `src/test/java`
- **Command**: `mvn verify -P integration-tests`

### BDD Tests
- **Framework**: Cucumber
- **Features**: `src/test/resources/features`
- **Step Definitions**: `src/test/java/bdd`
- **Command**: `mvn test -Dtest=CucumberTestRunner`

### Performance Tests
- **Tool**: JMeter
- **Test Plans**: `performance-tests/`
- **Metrics**: Response time, throughput, error rate

## 🔐 Security Considerations

### Container Security
- Non-root user execution
- Minimal base images
- Regular security updates
- Vulnerability scanning with Trivy

### Application Security
- Input validation
- Security headers
- API key protection
- HTTPS enforcement (production)

### Network Security
- Container network isolation
- Firewall rules
- Load balancer SSL termination

## 📝 Deployment Checklist

### Pre-deployment
- [ ] All tests passing
- [ ] Code quality gates met
- [ ] Security scans clean
- [ ] Environment variables configured
- [ ] Database migrations ready
- [ ] Monitoring configured

### Deployment
- [ ] Build successful
- [ ] Container images pushed
- [ ] Services deployed
- [ ] Health checks passing
- [ ] Smoke tests successful

### Post-deployment
- [ ] Application accessible
- [ ] API endpoints working
- [ ] Monitoring data flowing
- [ ] Logs being collected
- [ ] Performance metrics normal

## 🚨 Troubleshooting

### Common Issues

#### Container Won't Start
```bash
# Check logs
docker-compose logs news-search-microservice

# Check health
docker exec -it news-search-microservice curl localhost:8080/api/v1/news/health
```

#### API Not Responding
```bash
# Test connectivity
curl -v http://localhost:8080/api/v1/news/health

# Check network
docker network ls
docker network inspect windsurf-project_news-network
```

#### High Memory Usage
```bash
# Check container stats
docker stats

# Adjust JVM settings
export JAVA_OPTS="-Xmx256m -Xms128m"
```

### Log Locations
- **Application Logs**: `./logs/`
- **Docker Logs**: `docker-compose logs`
- **Nginx Logs**: `/var/log/nginx/`

## 📞 Support

For deployment issues or questions:
- Check the troubleshooting section above
- Review application logs
- Consult the monitoring dashboards
- Contact the development team

## 🔄 Rollback Procedure

### Quick Rollback
```bash
# Stop current deployment
docker-compose down

# Deploy previous version
docker-compose up -d news-search-microservice:previous-tag

# Verify rollback
curl http://localhost:8080/api/v1/news/health
```

### Database Rollback
```bash
# Run database migration rollback
mvn flyway:undo

# Or restore from backup
# (specific commands depend on your database setup)
```

---

## 📊 Deployment Metrics

Track these metrics for successful deployments:
- **Deployment Frequency**: Daily/Weekly
- **Lead Time**: < 1 hour
- **MTTR**: < 30 minutes
- **Change Failure Rate**: < 5%
- **Availability**: > 99.9%
