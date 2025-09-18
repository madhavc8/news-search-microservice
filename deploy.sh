#!/bin/bash

# News Search Microservice Deployment Script
# This script handles building and deploying the application locally

set -e  # Exit on any error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
PROJECT_NAME="news-search-microservice"
DOCKER_IMAGE="news-search-microservice"
FRONTEND_IMAGE="news-frontend"
ENVIRONMENT=${1:-dev}
NEWS_API_KEY=${NEWS_API_KEY:-ccaf5d41cc5140c984818c344edcc14d}

# Functions
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

check_prerequisites() {
    log_info "Checking prerequisites..."
    
    # Check if Docker is installed and running
    if ! command -v docker &> /dev/null; then
        log_error "Docker is not installed. Please install Docker first."
        exit 1
    fi
    
    if ! docker info &> /dev/null; then
        log_error "Docker is not running. Please start Docker first."
        exit 1
    fi
    
    # Check if Docker Compose is installed
    if ! command -v docker-compose &> /dev/null; then
        log_error "Docker Compose is not installed. Please install Docker Compose first."
        exit 1
    fi
    
    # Check if Maven is installed
    if ! command -v mvn &> /dev/null; then
        log_error "Maven is not installed. Please install Maven first."
        exit 1
    fi
    
    # Check if Node.js is installed
    if ! command -v node &> /dev/null; then
        log_error "Node.js is not installed. Please install Node.js first."
        exit 1
    fi
    
    log_success "All prerequisites are met!"
}

clean_environment() {
    log_info "Cleaning up existing containers and images..."
    
    # Stop and remove existing containers
    docker-compose -f docker-compose.yml down --remove-orphans 2>/dev/null || true
    docker-compose -f docker-compose.staging.yml down --remove-orphans 2>/dev/null || true
    docker-compose -f docker-compose.prod.yml down --remove-orphans 2>/dev/null || true
    
    # Remove old images
    docker rmi ${DOCKER_IMAGE}:latest 2>/dev/null || true
    docker rmi ${FRONTEND_IMAGE}:latest 2>/dev/null || true
    
    # Clean up dangling images
    docker image prune -f
    
    log_success "Environment cleaned up!"
}

run_tests() {
    log_info "Running tests..."
    
    # Run backend tests
    log_info "Running backend tests..."
    mvn clean test
    
    # Run frontend tests
    log_info "Running frontend tests..."
    cd frontend
    npm test -- --coverage --watchAll=false
    cd ..
    
    log_success "All tests passed!"
}

build_backend() {
    log_info "Building backend application..."
    
    # Build the Spring Boot application
    mvn clean package -DskipTests
    
    # Build Docker image
    docker build -t ${DOCKER_IMAGE}:latest .
    
    log_success "Backend built successfully!"
}

build_frontend() {
    log_info "Building frontend application..."
    
    cd frontend
    
    # Install dependencies
    npm ci
    
    # Build React application
    npm run build
    
    # Build Docker image
    docker build -t ${FRONTEND_IMAGE}:latest .
    
    cd ..
    
    log_success "Frontend built successfully!"
}

deploy_local() {
    log_info "Deploying to local environment..."
    
    # Set environment variables
    export NEWS_API_KEY=${NEWS_API_KEY}
    export DOCKER_IMAGE=${DOCKER_IMAGE}
    export FRONTEND_IMAGE=${FRONTEND_IMAGE}
    
    # Start services
    docker-compose up -d
    
    # Wait for services to be ready
    log_info "Waiting for services to start..."
    sleep 30
    
    # Health check
    check_health
    
    log_success "Local deployment completed!"
}

deploy_staging() {
    log_info "Deploying to staging environment..."
    
    export NEWS_API_KEY=${NEWS_API_KEY}
    export DOCKER_IMAGE=${DOCKER_IMAGE}
    export FRONTEND_IMAGE=${FRONTEND_IMAGE}
    
    docker-compose -f docker-compose.staging.yml up -d
    
    log_info "Waiting for staging services to start..."
    sleep 45
    
    check_health
    
    log_success "Staging deployment completed!"
}

deploy_production() {
    log_info "Deploying to production environment..."
    
    # Confirmation prompt
    read -p "Are you sure you want to deploy to production? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        log_warning "Production deployment cancelled."
        exit 0
    fi
    
    export NEWS_API_KEY=${NEWS_API_KEY}
    export DOCKER_IMAGE=${DOCKER_IMAGE}
    export FRONTEND_IMAGE=${FRONTEND_IMAGE}
    
    docker-compose -f docker-compose.prod.yml up -d
    
    log_info "Waiting for production services to start..."
    sleep 60
    
    check_health
    
    log_success "Production deployment completed!"
}

check_health() {
    log_info "Performing health checks..."
    
    # Check backend health
    local backend_health_url="http://localhost:8080/api/v1/news/health"
    local max_attempts=10
    local attempt=1
    
    while [ $attempt -le $max_attempts ]; do
        if curl -f -s $backend_health_url > /dev/null; then
            log_success "Backend service is healthy!"
            break
        else
            log_warning "Backend health check failed (attempt $attempt/$max_attempts)"
            if [ $attempt -eq $max_attempts ]; then
                log_error "Backend service failed to start properly"
                show_logs
                exit 1
            fi
            sleep 10
            ((attempt++))
        fi
    done
    
    # Check frontend health (if deployed)
    if curl -f -s http://localhost:80/health > /dev/null 2>&1; then
        log_success "Frontend service is healthy!"
    else
        log_warning "Frontend service health check failed or not deployed"
    fi
    
    # Test API endpoint
    if curl -f -s "http://localhost:8080/api/v1/news/search?keyword=test" > /dev/null; then
        log_success "API endpoint is working!"
    else
        log_warning "API endpoint test failed"
    fi
}

show_logs() {
    log_info "Showing recent logs..."
    docker-compose logs --tail=50
}

show_status() {
    log_info "Service Status:"
    docker-compose ps
    
    log_info "Docker Images:"
    docker images | grep -E "(news-search|news-frontend)"
    
    log_info "Network Status:"
    docker network ls | grep news
}

cleanup() {
    log_info "Cleaning up resources..."
    docker-compose down --remove-orphans
    docker system prune -f
    log_success "Cleanup completed!"
}

show_help() {
    echo "News Search Microservice Deployment Script"
    echo ""
    echo "Usage: $0 [ENVIRONMENT] [COMMAND]"
    echo ""
    echo "Environments:"
    echo "  dev        Deploy to development environment (default)"
    echo "  staging    Deploy to staging environment"
    echo "  prod       Deploy to production environment"
    echo ""
    echo "Commands:"
    echo "  build      Build applications only"
    echo "  test       Run tests only"
    echo "  deploy     Build and deploy (default)"
    echo "  status     Show service status"
    echo "  logs       Show service logs"
    echo "  cleanup    Clean up resources"
    echo "  help       Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0 dev deploy    # Deploy to development"
    echo "  $0 staging       # Deploy to staging"
    echo "  $0 prod deploy   # Deploy to production"
    echo "  $0 dev build     # Build only"
    echo "  $0 dev test      # Run tests only"
}

# Main execution
main() {
    local command=${2:-deploy}
    
    case $command in
        build)
            check_prerequisites
            build_backend
            build_frontend
            ;;
        test)
            check_prerequisites
            run_tests
            ;;
        deploy)
            check_prerequisites
            clean_environment
            run_tests
            build_backend
            build_frontend
            
            case $ENVIRONMENT in
                dev)
                    deploy_local
                    ;;
                staging)
                    deploy_staging
                    ;;
                prod)
                    deploy_production
                    ;;
                *)
                    log_error "Unknown environment: $ENVIRONMENT"
                    show_help
                    exit 1
                    ;;
            esac
            ;;
        status)
            show_status
            ;;
        logs)
            show_logs
            ;;
        cleanup)
            cleanup
            ;;
        help)
            show_help
            ;;
        *)
            log_error "Unknown command: $command"
            show_help
            exit 1
            ;;
    esac
}

# Script entry point
if [ $# -eq 0 ]; then
    main dev deploy
else
    main "$@"
fi
