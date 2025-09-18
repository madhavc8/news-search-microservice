# 🌐 News Search Microservice - Server Deployment Guide

## 🚀 **SERVER DEPLOYMENT OPTIONS**

This guide covers deploying the News Search Microservice to various server environments.

---

## 🏗️ **DEPLOYMENT ARCHITECTURE**

```
┌─────────────────────────────────────────────────────┐
│                  CLOUD DEPLOYMENT                   │
├─────────────────────────────────────────────────────┤
│  Internet → Load Balancer → Application Servers     │
│                              ↓                      │
│            Database ← → Cache ← → Monitoring        │
└─────────────────────────────────────────────────────┘
```

---

## ☁️ **OPTION 1: AWS DEPLOYMENT**

### **1.1 AWS ECS (Elastic Container Service)**

#### **Prerequisites:**
- AWS CLI installed and configured
- Docker images pushed to ECR
- ECS cluster created

#### **Deployment Commands:**
```bash
# 1. Build and push Docker images
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin <account-id>.dkr.ecr.us-east-1.amazonaws.com

# Build images
docker build -t news-search-microservice .
docker build -t news-frontend ./frontend

# Tag images
docker tag news-search-microservice:latest <account-id>.dkr.ecr.us-east-1.amazonaws.com/news-search-microservice:latest
docker tag news-frontend:latest <account-id>.dkr.ecr.us-east-1.amazonaws.com/news-frontend:latest

# Push images
docker push <account-id>.dkr.ecr.us-east-1.amazonaws.com/news-search-microservice:latest
docker push <account-id>.dkr.ecr.us-east-1.amazonaws.com/news-frontend:latest

# 2. Deploy to ECS
aws ecs update-service --cluster news-cluster --service news-search-service --force-new-deployment
```

#### **ECS Task Definition (task-definition.json):**
```json
{
  "family": "news-search-microservice",
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["FARGATE"],
  "cpu": "512",
  "memory": "1024",
  "executionRoleArn": "arn:aws:iam::<account-id>:role/ecsTaskExecutionRole",
  "containerDefinitions": [
    {
      "name": "news-search-backend",
      "image": "<account-id>.dkr.ecr.us-east-1.amazonaws.com/news-search-microservice:latest",
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
          "value": "ccaf5d41cc5140c984818c344edcc14d"
        }
      ],
      "logConfiguration": {
        "logDriver": "awslogs",
        "options": {
          "awslogs-group": "/ecs/news-search-microservice",
          "awslogs-region": "us-east-1",
          "awslogs-stream-prefix": "ecs"
        }
      }
    }
  ]
}
```

### **1.2 AWS EC2 Deployment**

#### **Server Setup Commands:**
```bash
# Connect to EC2 instance
ssh -i your-key.pem ec2-user@your-ec2-ip

# Install Docker
sudo yum update -y
sudo yum install -y docker
sudo service docker start
sudo usermod -a -G docker ec2-user

# Install Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/download/1.29.2/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Clone repository
git clone https://github.com/your-repo/news-search-microservice.git
cd news-search-microservice

# Set environment variables
export NEWS_API_KEY=ccaf5d41cc5140c984818c344edcc14d

# Deploy
docker-compose -f docker-compose.prod.yml up -d
```

---

## 🔵 **OPTION 2: AZURE DEPLOYMENT**

### **2.1 Azure Container Instances**

#### **Deployment Commands:**
```bash
# Login to Azure
az login

# Create resource group
az group create --name news-search-rg --location eastus

# Create container registry
az acr create --resource-group news-search-rg --name newsearchregistry --sku Basic

# Build and push images
az acr build --registry newsearchregistry --image news-search-microservice .
az acr build --registry newsearchregistry --image news-frontend ./frontend

# Deploy container instances
az container create \
  --resource-group news-search-rg \
  --name news-search-backend \
  --image newsearchregistry.azurecr.io/news-search-microservice:latest \
  --cpu 1 \
  --memory 2 \
  --ports 8080 \
  --environment-variables SPRING_PROFILES_ACTIVE=prod NEWS_API_KEY=ccaf5d41cc5140c984818c344edcc14d \
  --dns-name-label news-search-api

az container create \
  --resource-group news-search-rg \
  --name news-search-frontend \
  --image newsearchregistry.azurecr.io/news-frontend:latest \
  --cpu 0.5 \
  --memory 1 \
  --ports 80 \
  --dns-name-label news-search-app
```

### **2.2 Azure App Service**

#### **Deployment Commands:**
```bash
# Create App Service Plan
az appservice plan create --name news-search-plan --resource-group news-search-rg --sku B1 --is-linux

# Create Web App for Backend
az webapp create --resource-group news-search-rg --plan news-search-plan --name news-search-api --deployment-container-image-name newsearchregistry.azurecr.io/news-search-microservice:latest

# Configure environment variables
az webapp config appsettings set --resource-group news-search-rg --name news-search-api --settings SPRING_PROFILES_ACTIVE=prod NEWS_API_KEY=ccaf5d41cc5140c984818c344edcc14d

# Create Web App for Frontend
az webapp create --resource-group news-search-rg --plan news-search-plan --name news-search-app --deployment-container-image-name newsearchregistry.azurecr.io/news-frontend:latest
```

---

## 🟢 **OPTION 3: GOOGLE CLOUD DEPLOYMENT**

### **3.1 Google Cloud Run**

#### **Deployment Commands:**
```bash
# Authenticate with Google Cloud
gcloud auth login
gcloud config set project your-project-id

# Build and push images
gcloud builds submit --tag gcr.io/your-project-id/news-search-microservice .
gcloud builds submit --tag gcr.io/your-project-id/news-frontend ./frontend

# Deploy to Cloud Run
gcloud run deploy news-search-backend \
  --image gcr.io/your-project-id/news-search-microservice \
  --platform managed \
  --region us-central1 \
  --allow-unauthenticated \
  --port 8080 \
  --set-env-vars SPRING_PROFILES_ACTIVE=prod,NEWS_API_KEY=ccaf5d41cc5140c984818c344edcc14d

gcloud run deploy news-search-frontend \
  --image gcr.io/your-project-id/news-frontend \
  --platform managed \
  --region us-central1 \
  --allow-unauthenticated \
  --port 80
```

### **3.2 Google Kubernetes Engine (GKE)**

#### **Kubernetes Manifests:**

**backend-deployment.yaml:**
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: news-search-backend
spec:
  replicas: 2
  selector:
    matchLabels:
      app: news-search-backend
  template:
    metadata:
      labels:
        app: news-search-backend
    spec:
      containers:
      - name: backend
        image: gcr.io/your-project-id/news-search-microservice:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: NEWS_API_KEY
          value: "ccaf5d41cc5140c984818c344edcc14d"
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "500m"
---
apiVersion: v1
kind: Service
metadata:
  name: news-search-backend-service
spec:
  selector:
    app: news-search-backend
  ports:
  - port: 8080
    targetPort: 8080
  type: LoadBalancer
```

#### **Deployment Commands:**
```bash
# Create GKE cluster
gcloud container clusters create news-search-cluster --num-nodes=2

# Get cluster credentials
gcloud container clusters get-credentials news-search-cluster

# Deploy applications
kubectl apply -f backend-deployment.yaml
kubectl apply -f frontend-deployment.yaml

# Check status
kubectl get pods
kubectl get services
```

---

## 🐙 **OPTION 4: DIGITAL OCEAN DEPLOYMENT**

### **4.1 Digital Ocean Droplet**

#### **Server Setup:**
```bash
# Create droplet via DO console or CLI
doctl compute droplet create news-search-server \
  --size s-2vcpu-2gb \
  --image docker-20-04 \
  --region nyc1 \
  --ssh-keys your-ssh-key-id

# Connect to droplet
ssh root@your-droplet-ip

# Clone and deploy
git clone https://github.com/your-repo/news-search-microservice.git
cd news-search-microservice
export NEWS_API_KEY=ccaf5d41cc5140c984818c344edcc14d
docker-compose -f docker-compose.prod.yml up -d
```

### **4.2 Digital Ocean App Platform**

#### **app.yaml:**
```yaml
name: news-search-microservice
services:
- name: backend
  source_dir: /
  github:
    repo: your-username/news-search-microservice
    branch: main
  run_command: java -jar target/news-search-microservice-1.0.0.jar
  environment_slug: java
  instance_count: 1
  instance_size_slug: basic-xxs
  envs:
  - key: SPRING_PROFILES_ACTIVE
    value: prod
  - key: NEWS_API_KEY
    value: ccaf5d41cc5140c984818c344edcc14d
  http_port: 8080
- name: frontend
  source_dir: /frontend
  github:
    repo: your-username/news-search-microservice
    branch: main
  run_command: npm start
  environment_slug: node-js
  instance_count: 1
  instance_size_slug: basic-xxs
  http_port: 3000
```

---

## 🔧 **OPTION 5: VPS/DEDICATED SERVER**

### **5.1 Ubuntu Server Setup**

#### **Server Preparation:**
```bash
# Connect to server
ssh user@your-server-ip

# Update system
sudo apt update && sudo apt upgrade -y

# Install Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sh get-docker.sh
sudo usermod -aG docker $USER

# Install Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/download/1.29.2/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Install Java and Maven (if building on server)
sudo apt install -y openjdk-17-jdk maven

# Install Node.js
curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
sudo apt-get install -y nodejs
```

#### **Deployment:**
```bash
# Clone repository
git clone https://github.com/your-repo/news-search-microservice.git
cd news-search-microservice

# Build applications
mvn clean package -DskipTests
cd frontend && npm install && npm run build && cd ..

# Set environment variables
export NEWS_API_KEY=ccaf5d41cc5140c984818c344edcc14d

# Deploy with Docker Compose
docker-compose -f docker-compose.prod.yml up -d

# Setup reverse proxy (Nginx)
sudo apt install -y nginx
sudo cp nginx/production.conf /etc/nginx/sites-available/news-search
sudo ln -s /etc/nginx/sites-available/news-search /etc/nginx/sites-enabled/
sudo nginx -t && sudo systemctl reload nginx
```

---

## 🔒 **SECURITY CONFIGURATION**

### **SSL/TLS Setup (Let's Encrypt):**
```bash
# Install Certbot
sudo apt install -y certbot python3-certbot-nginx

# Get SSL certificate
sudo certbot --nginx -d your-domain.com

# Auto-renewal
sudo crontab -e
# Add: 0 12 * * * /usr/bin/certbot renew --quiet
```

### **Firewall Configuration:**
```bash
# Configure UFW
sudo ufw allow ssh
sudo ufw allow 80
sudo ufw allow 443
sudo ufw enable
```

---

## 📊 **MONITORING SETUP**

### **Production Monitoring:**
```bash
# Deploy monitoring stack
docker-compose -f docker-compose.monitoring.yml up -d

# Access dashboards
# Prometheus: http://your-server:9090
# Grafana: http://your-server:3000 (admin/admin123)
```

---

## 🚀 **RECOMMENDED DEPLOYMENT COMMAND**

### **For immediate server deployment, choose one:**

#### **AWS ECS:**
```bash
aws ecs update-service --cluster news-cluster --service news-search-service --force-new-deployment
```

#### **Google Cloud Run:**
```bash
gcloud run deploy news-search-backend --image gcr.io/your-project-id/news-search-microservice --platform managed --region us-central1 --allow-unauthenticated
```

#### **VPS/Dedicated Server:**
```bash
ssh user@your-server-ip
git clone https://github.com/your-repo/news-search-microservice.git
cd news-search-microservice
export NEWS_API_KEY=ccaf5d41cc5140c984818c344edcc14d
docker-compose -f docker-compose.prod.yml up -d
```

---

## 🎯 **POST-DEPLOYMENT VERIFICATION**

After deployment, verify these endpoints:
- **Health**: `https://your-domain/api/v1/news/health`
- **API Docs**: `https://your-domain/swagger-ui.html`
- **Frontend**: `https://your-domain`
- **Monitoring**: `https://your-domain:9090` (Prometheus)

**Your microservice is now deployed and running on a production server!** 🌐
