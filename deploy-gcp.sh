#!/bin/bash

# Google Cloud Platform Deployment Script for News Search Microservice
# This script deploys the application to Google Cloud Run

set -e

# Configuration
PROJECT_ID=${PROJECT_ID}
REGION=${REGION:-us-central1}
SERVICE_NAME_BACKEND="news-search-backend"
SERVICE_NAME_FRONTEND="news-search-frontend"
NEWS_API_KEY=${NEWS_API_KEY:-ccaf5d41cc5140c984818c344edcc14d}

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

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

# Check prerequisites
check_prerequisites() {
    log_info "Checking prerequisites..."
    
    if ! command -v gcloud &> /dev/null; then
        log_error "Google Cloud SDK is not installed"
        exit 1
    fi
    
    if ! command -v docker &> /dev/null; then
        log_error "Docker is not installed"
        exit 1
    fi
    
    if [ -z "$PROJECT_ID" ]; then
        log_error "PROJECT_ID environment variable is not set"
        exit 1
    fi
    
    # Test gcloud authentication
    if ! gcloud auth list --filter=status:ACTIVE --format="value(account)" | head -n 1 &> /dev/null; then
        log_error "Google Cloud authentication required. Run: gcloud auth login"
        exit 1
    fi
    
    # Set project
    gcloud config set project $PROJECT_ID
    
    log_success "Prerequisites check passed"
}

# Enable required APIs
enable_apis() {
    log_info "Enabling required Google Cloud APIs..."
    
    gcloud services enable cloudbuild.googleapis.com
    gcloud services enable run.googleapis.com
    gcloud services enable containerregistry.googleapis.com
    
    log_success "APIs enabled"
}

# Build and push images
build_and_push_images() {
    log_info "Building and pushing Docker images to Google Container Registry..."
    
    # Configure Docker for GCR
    gcloud auth configure-docker
    
    # Build backend
    log_info "Building backend application..."
    mvn clean package -DskipTests
    
    gcloud builds submit --tag gcr.io/$PROJECT_ID/$SERVICE_NAME_BACKEND .
    
    # Build frontend
    log_info "Building frontend application..."
    cd frontend
    npm install
    npm run build
    
    gcloud builds submit --tag gcr.io/$PROJECT_ID/$SERVICE_NAME_FRONTEND .
    cd ..
    
    log_success "Images built and pushed successfully"
}

# Deploy to Cloud Run
deploy_to_cloud_run() {
    log_info "Deploying to Google Cloud Run..."
    
    # Deploy backend
    log_info "Deploying backend service..."
    gcloud run deploy $SERVICE_NAME_BACKEND \
        --image gcr.io/$PROJECT_ID/$SERVICE_NAME_BACKEND \
        --platform managed \
        --region $REGION \
        --allow-unauthenticated \
        --port 8080 \
        --memory 1Gi \
        --cpu 1 \
        --concurrency 80 \
        --max-instances 10 \
        --set-env-vars SPRING_PROFILES_ACTIVE=prod,NEWS_API_KEY=$NEWS_API_KEY,JAVA_OPTS="-Xmx512m -Xms256m" \
        --timeout 300
    
    # Deploy frontend
    log_info "Deploying frontend service..."
    gcloud run deploy $SERVICE_NAME_FRONTEND \
        --image gcr.io/$PROJECT_ID/$SERVICE_NAME_FRONTEND \
        --platform managed \
        --region $REGION \
        --allow-unauthenticated \
        --port 80 \
        --memory 512Mi \
        --cpu 1 \
        --concurrency 100 \
        --max-instances 5 \
        --timeout 60
    
    log_success "Services deployed to Cloud Run"
}

# Configure custom domain (optional)
configure_domain() {
    local domain=$1
    
    if [ -n "$domain" ]; then
        log_info "Configuring custom domain: $domain"
        
        # Map domain to backend service
        gcloud run domain-mappings create \
            --service $SERVICE_NAME_BACKEND \
            --domain api.$domain \
            --region $REGION
        
        # Map domain to frontend service
        gcloud run domain-mappings create \
            --service $SERVICE_NAME_FRONTEND \
            --domain $domain \
            --region $REGION
        
        log_success "Domain mapping created. Please configure DNS records:"
        log_info "Add CNAME record: api.$domain -> ghs.googlehosted.com"
        log_info "Add CNAME record: $domain -> ghs.googlehosted.com"
    fi
}

# Get service URLs
get_service_urls() {
    log_info "Getting service URLs..."
    
    BACKEND_URL=$(gcloud run services describe $SERVICE_NAME_BACKEND --region $REGION --format 'value(status.url)')
    FRONTEND_URL=$(gcloud run services describe $SERVICE_NAME_FRONTEND --region $REGION --format 'value(status.url)')
    
    echo ""
    log_success "Deployment completed! Service URLs:"
    echo "Backend API: $BACKEND_URL/api/v1/news/health"
    echo "API Documentation: $BACKEND_URL/swagger-ui.html"
    echo "Frontend Application: $FRONTEND_URL"
    echo ""
    
    # Test backend health
    log_info "Testing backend health..."
    if curl -f -s "$BACKEND_URL/api/v1/news/health" > /dev/null; then
        log_success "Backend service is healthy!"
    else
        log_warning "Backend service health check failed"
    fi
}

# Setup monitoring (optional)
setup_monitoring() {
    log_info "Setting up monitoring and logging..."
    
    # Create log-based metrics
    gcloud logging metrics create news_search_requests \
        --description="News Search API Requests" \
        --log-filter='resource.type="cloud_run_revision" AND resource.labels.service_name="'$SERVICE_NAME_BACKEND'"' \
        --project $PROJECT_ID 2>/dev/null || true
    
    gcloud logging metrics create news_search_errors \
        --description="News Search API Errors" \
        --log-filter='resource.type="cloud_run_revision" AND resource.labels.service_name="'$SERVICE_NAME_BACKEND'" AND severity>=ERROR' \
        --project $PROJECT_ID 2>/dev/null || true
    
    log_success "Monitoring configured"
}

# Create Cloud Scheduler job for health checks
create_health_check_job() {
    log_info "Creating health check job..."
    
    BACKEND_URL=$(gcloud run services describe $SERVICE_NAME_BACKEND --region $REGION --format 'value(status.url)')
    
    gcloud scheduler jobs create http news-search-health-check \
        --schedule="*/5 * * * *" \
        --uri="$BACKEND_URL/api/v1/news/health" \
        --http-method=GET \
        --description="Health check for News Search Microservice" \
        --project $PROJECT_ID 2>/dev/null || \
    gcloud scheduler jobs update http news-search-health-check \
        --schedule="*/5 * * * *" \
        --uri="$BACKEND_URL/api/v1/news/health" \
        --http-method=GET \
        --project $PROJECT_ID
    
    log_success "Health check job created"
}

# Main execution
main() {
    local custom_domain=$1
    
    log_info "Starting Google Cloud Platform deployment for News Search Microservice..."
    
    check_prerequisites
    enable_apis
    build_and_push_images
    deploy_to_cloud_run
    
    if [ -n "$custom_domain" ]; then
        configure_domain $custom_domain
    fi
    
    setup_monitoring
    create_health_check_job
    get_service_urls
    
    log_success "Google Cloud Platform deployment completed successfully!"
    log_info "Your News Search Microservice is now running on Google Cloud Run"
    
    echo ""
    echo "Next steps:"
    echo "1. Configure your frontend to use the backend URL"
    echo "2. Set up custom domain DNS records (if applicable)"
    echo "3. Configure monitoring alerts in Google Cloud Console"
    echo "4. Review logs in Google Cloud Logging"
}

# Show usage
show_usage() {
    echo "Usage: $0 [custom-domain]"
    echo ""
    echo "Environment variables required:"
    echo "  PROJECT_ID - Google Cloud Project ID"
    echo "  NEWS_API_KEY - NewsAPI.org API key (optional, defaults to demo key)"
    echo "  REGION - Google Cloud region (optional, defaults to us-central1)"
    echo ""
    echo "Example:"
    echo "  export PROJECT_ID=my-project-123"
    echo "  export NEWS_API_KEY=your-api-key"
    echo "  $0 example.com"
}

# Check if help is requested
if [ "$1" = "--help" ] || [ "$1" = "-h" ]; then
    show_usage
    exit 0
fi

# Execute main function
main "$@"
