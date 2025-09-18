#!/bin/bash

# AWS ECS Deployment Script for News Search Microservice
# This script deploys the application to AWS ECS using Fargate

set -e

# Configuration
AWS_REGION=${AWS_REGION:-us-east-1}
AWS_ACCOUNT_ID=${AWS_ACCOUNT_ID}
ECR_REPOSITORY_BACKEND="news-search-microservice"
ECR_REPOSITORY_FRONTEND="news-frontend"
ECS_CLUSTER="news-cluster"
ECS_SERVICE_BACKEND="news-search-backend-service"
ECS_SERVICE_FRONTEND="news-search-frontend-service"
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
    
    if ! command -v aws &> /dev/null; then
        log_error "AWS CLI is not installed"
        exit 1
    fi
    
    if ! command -v docker &> /dev/null; then
        log_error "Docker is not installed"
        exit 1
    fi
    
    if [ -z "$AWS_ACCOUNT_ID" ]; then
        log_error "AWS_ACCOUNT_ID environment variable is not set"
        exit 1
    fi
    
    # Test AWS credentials
    if ! aws sts get-caller-identity &> /dev/null; then
        log_error "AWS credentials not configured or invalid"
        exit 1
    fi
    
    log_success "Prerequisites check passed"
}

# Build and push Docker images
build_and_push_images() {
    log_info "Building and pushing Docker images..."
    
    # Login to ECR
    aws ecr get-login-password --region $AWS_REGION | docker login --username AWS --password-stdin $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com
    
    # Create ECR repositories if they don't exist
    aws ecr describe-repositories --repository-names $ECR_REPOSITORY_BACKEND --region $AWS_REGION 2>/dev/null || \
        aws ecr create-repository --repository-name $ECR_REPOSITORY_BACKEND --region $AWS_REGION
    
    aws ecr describe-repositories --repository-names $ECR_REPOSITORY_FRONTEND --region $AWS_REGION 2>/dev/null || \
        aws ecr create-repository --repository-name $ECR_REPOSITORY_FRONTEND --region $AWS_REGION
    
    # Build backend
    log_info "Building backend application..."
    mvn clean package -DskipTests
    docker build -t $ECR_REPOSITORY_BACKEND .
    docker tag $ECR_REPOSITORY_BACKEND:latest $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/$ECR_REPOSITORY_BACKEND:latest
    docker push $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/$ECR_REPOSITORY_BACKEND:latest
    
    # Build frontend
    log_info "Building frontend application..."
    cd frontend
    npm install
    npm run build
    docker build -t $ECR_REPOSITORY_FRONTEND .
    docker tag $ECR_REPOSITORY_FRONTEND:latest $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/$ECR_REPOSITORY_FRONTEND:latest
    docker push $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/$ECR_REPOSITORY_FRONTEND:latest
    cd ..
    
    log_success "Images built and pushed successfully"
}

# Create ECS task definition
create_task_definition() {
    log_info "Creating ECS task definition..."
    
    cat > task-definition-backend.json << EOF
{
  "family": "news-search-backend",
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["FARGATE"],
  "cpu": "512",
  "memory": "1024",
  "executionRoleArn": "arn:aws:iam::$AWS_ACCOUNT_ID:role/ecsTaskExecutionRole",
  "taskRoleArn": "arn:aws:iam::$AWS_ACCOUNT_ID:role/ecsTaskRole",
  "containerDefinitions": [
    {
      "name": "news-search-backend",
      "image": "$AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/$ECR_REPOSITORY_BACKEND:latest",
      "portMappings": [
        {
          "containerPort": 8080,
          "protocol": "tcp"
        }
      ],
      "environment": [
        {
          "name": "SPRING_PROFILES_ACTIVE",
          "value": "prod"
        },
        {
          "name": "NEWS_API_KEY",
          "value": "$NEWS_API_KEY"
        },
        {
          "name": "JAVA_OPTS",
          "value": "-Xmx512m -Xms256m"
        }
      ],
      "logConfiguration": {
        "logDriver": "awslogs",
        "options": {
          "awslogs-group": "/ecs/news-search-backend",
          "awslogs-region": "$AWS_REGION",
          "awslogs-stream-prefix": "ecs"
        }
      },
      "healthCheck": {
        "command": ["CMD-SHELL", "curl -f http://localhost:8080/api/v1/news/health || exit 1"],
        "interval": 30,
        "timeout": 5,
        "retries": 3,
        "startPeriod": 60
      }
    }
  ]
}
EOF

    # Register task definition
    aws ecs register-task-definition --cli-input-json file://task-definition-backend.json --region $AWS_REGION
    
    log_success "Task definition created"
}

# Create ECS cluster and service
create_ecs_resources() {
    log_info "Creating ECS cluster and services..."
    
    # Create cluster
    aws ecs create-cluster --cluster-name $ECS_CLUSTER --region $AWS_REGION 2>/dev/null || true
    
    # Create log group
    aws logs create-log-group --log-group-name /ecs/news-search-backend --region $AWS_REGION 2>/dev/null || true
    
    # Get default VPC and subnets
    VPC_ID=$(aws ec2 describe-vpcs --filters "Name=isDefault,Values=true" --query 'Vpcs[0].VpcId' --output text --region $AWS_REGION)
    SUBNET_IDS=$(aws ec2 describe-subnets --filters "Name=vpc-id,Values=$VPC_ID" --query 'Subnets[*].SubnetId' --output text --region $AWS_REGION)
    SECURITY_GROUP_ID=$(aws ec2 describe-security-groups --filters "Name=vpc-id,Values=$VPC_ID" "Name=group-name,Values=default" --query 'SecurityGroups[0].GroupId' --output text --region $AWS_REGION)
    
    # Create service
    aws ecs create-service \
        --cluster $ECS_CLUSTER \
        --service-name $ECS_SERVICE_BACKEND \
        --task-definition news-search-backend \
        --desired-count 2 \
        --launch-type FARGATE \
        --network-configuration "awsvpcConfiguration={subnets=[$SUBNET_IDS],securityGroups=[$SECURITY_GROUP_ID],assignPublicIp=ENABLED}" \
        --region $AWS_REGION 2>/dev/null || \
    aws ecs update-service \
        --cluster $ECS_CLUSTER \
        --service $ECS_SERVICE_BACKEND \
        --force-new-deployment \
        --region $AWS_REGION
    
    log_success "ECS resources created/updated"
}

# Wait for deployment
wait_for_deployment() {
    log_info "Waiting for deployment to complete..."
    
    aws ecs wait services-stable --cluster $ECS_CLUSTER --services $ECS_SERVICE_BACKEND --region $AWS_REGION
    
    log_success "Deployment completed successfully"
}

# Get service information
get_service_info() {
    log_info "Getting service information..."
    
    # Get task ARNs
    TASK_ARNS=$(aws ecs list-tasks --cluster $ECS_CLUSTER --service-name $ECS_SERVICE_BACKEND --query 'taskArns' --output text --region $AWS_REGION)
    
    if [ -n "$TASK_ARNS" ]; then
        # Get task details
        aws ecs describe-tasks --cluster $ECS_CLUSTER --tasks $TASK_ARNS --query 'tasks[0].attachments[0].details[?name==`networkInterfaceId`].value' --output text --region $AWS_REGION | while read ENI_ID; do
            if [ -n "$ENI_ID" ]; then
                PUBLIC_IP=$(aws ec2 describe-network-interfaces --network-interface-ids $ENI_ID --query 'NetworkInterfaces[0].Association.PublicIp' --output text --region $AWS_REGION)
                if [ "$PUBLIC_IP" != "None" ] && [ -n "$PUBLIC_IP" ]; then
                    echo "Service URL: http://$PUBLIC_IP:8080/api/v1/news/health"
                    echo "API Documentation: http://$PUBLIC_IP:8080/swagger-ui.html"
                fi
            fi
        done
    fi
}

# Main execution
main() {
    log_info "Starting AWS ECS deployment for News Search Microservice..."
    
    check_prerequisites
    build_and_push_images
    create_task_definition
    create_ecs_resources
    wait_for_deployment
    get_service_info
    
    log_success "AWS ECS deployment completed successfully!"
    log_info "Your News Search Microservice is now running on AWS ECS"
}

# Execute main function
main "$@"
