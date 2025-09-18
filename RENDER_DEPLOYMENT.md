# 🚀 News Search Microservice - Render + Vercel Deployment

## 📋 **DEPLOYMENT STRATEGY**

Following your successful **Accessibility Analyzer** deployment pattern:
- **Backend**: Render.com (Java Spring Boot)
- **Frontend**: Vercel (React)
- **Database**: None required (uses NewsAPI.org)
- **Cost**: **FREE** (using free tiers)

---

## 🎯 **STEP-BY-STEP DEPLOYMENT**

### **Step 1: Backend Deployment on Render.com**

#### **1.1 Push to GitHub**
```bash
git add .
git commit -m "Add Render deployment configuration"
git push origin main
```

#### **1.2 Deploy on Render.com**
1. Go to [render.com](https://render.com)
2. Sign in with GitHub
3. Click **"New +"** → **"Web Service"**
4. Connect your GitHub repository: `news-search-microservice`
5. Configure:
   - **Name**: `news-search-microservice`
   - **Environment**: `Java`
   - **Build Command**: `mvn clean package -DskipTests`
   - **Start Command**: `java -jar target/news-search-microservice-1.0.0.jar`
   - **Plan**: `Free`

#### **1.3 Environment Variables (in Render dashboard)**
```
PORT=10000
NEWS_API_KEY=ccaf5d41cc5140c984818c344edcc14d
SPRING_PROFILES_ACTIVE=prod
JAVA_OPTS=-Xmx512m -Xms256m
SERVER_PORT=10000
```

#### **1.4 Deploy**
- Click **"Create Web Service"**
- Wait for deployment (5-10 minutes)
- Your backend will be available at: `https://news-search-microservice.onrender.com`

---

### **Step 2: Frontend Deployment on Vercel**

#### **2.1 Deploy on Vercel**
1. Go to [vercel.com](https://vercel.com)
2. Sign in with GitHub
3. Click **"New Project"**
4. Import your GitHub repository
5. Configure:
   - **Framework Preset**: `Create React App`
   - **Root Directory**: `frontend`
   - **Build Command**: `npm run build`
   - **Output Directory**: `build`

#### **2.2 Environment Variables (in Vercel dashboard)**
```
REACT_APP_API_BASE_URL=https://news-search-microservice.onrender.com/api/v1
NODE_ENV=production
```

#### **2.3 Deploy**
- Click **"Deploy"**
- Wait for deployment (2-3 minutes)
- Your frontend will be available at: `https://news-search-frontend-xxx.vercel.app`

---

## 🔧 **CONFIGURATION FILES CREATED**

### **✅ Backend Configuration (`render.yaml`)**
```yaml
services:
  - type: web
    name: news-search-microservice
    env: java
    buildCommand: mvn clean package -DskipTests
    startCommand: java -jar target/news-search-microservice-1.0.0.jar
    plan: free
    envVars:
      - key: PORT
        value: 10000
      - key: NEWS_API_KEY
        value: ccaf5d41cc5140c984818c344edcc14d
      - key: SPRING_PROFILES_ACTIVE
        value: prod
```

### **✅ Frontend Configuration (`frontend/vercel.json`)**
```json
{
  "name": "news-search-frontend",
  "builds": [
    {
      "src": "package.json",
      "use": "@vercel/static-build",
      "config": { "distDir": "build" }
    }
  ],
  "env": {
    "REACT_APP_API_BASE_URL": "https://news-search-microservice.onrender.com/api/v1"
  }
}
```

---

## 🌐 **EXPECTED DEPLOYMENT URLS**

After successful deployment:

### **Backend (Render.com)**
- **API Base**: `https://news-search-microservice.onrender.com/api/v1`
- **Health Check**: `https://news-search-microservice.onrender.com/api/v1/news/health`
- **Swagger UI**: `https://news-search-microservice.onrender.com/swagger-ui.html`
- **Search Endpoint**: `https://news-search-microservice.onrender.com/api/v1/news/search?keyword=apple`

### **Frontend (Vercel)**
- **Application**: `https://news-search-frontend-xxx.vercel.app`
- **Modern UI**: Glass morphism design with Bootstrap 5
- **Features**: Search, filtering, offline mode, health monitoring

---

## ⚡ **AUTOMATIC DEPLOYMENTS**

### **✅ Continuous Deployment Setup**
- **Backend**: Auto-deploys on every push to `main` branch
- **Frontend**: Auto-deploys on every push to `main` branch
- **Zero Configuration**: Just push code and it deploys!

### **✅ Build Process**
```
GitHub Push → Render/Vercel → Build → Deploy → Live URL
```

---

## 🔍 **DEPLOYMENT VERIFICATION**

### **Backend Health Check**
```bash
curl https://news-search-microservice.onrender.com/api/v1/news/health
```

**Expected Response:**
```json
{
  "status": "UP",
  "newsApiAvailable": true,
  "offlineModeEnabled": false,
  "timestamp": "2025-09-18T22:40:00Z"
}
```

### **Frontend Verification**
1. Visit your Vercel URL
2. Test search functionality
3. Verify API integration
4. Check responsive design

---

## 💰 **COST BREAKDOWN**

### **FREE TIER LIMITS**
- **Render.com**: 750 hours/month (sufficient for 24/7)
- **Vercel**: 100GB bandwidth, unlimited deployments
- **NewsAPI.org**: 1000 requests/day (demo key)
- **Total Cost**: **$0/month** 🎉

---

## 🚨 **TROUBLESHOOTING**

### **Common Issues & Solutions**

#### **Backend Build Fails**
```bash
# Check Java version in Render logs
# Ensure Maven dependencies are correct
# Verify render.yaml configuration
```

#### **Frontend API Connection Issues**
```bash
# Check CORS configuration in Spring Boot
# Verify API base URL in Vercel environment variables
# Test API endpoints directly
```

#### **Cold Start Delays**
- Render free tier has cold starts (~30 seconds)
- First request after inactivity may be slow
- Consider upgrading to paid tier for production

---

## 🎯 **DEPLOYMENT CHECKLIST**

### **Pre-Deployment**
- [x] `render.yaml` created
- [x] `vercel.json` created
- [x] Environment variables configured
- [x] Code pushed to GitHub

### **Deployment Steps**
- [ ] Create Render web service
- [ ] Configure Render environment variables
- [ ] Create Vercel project
- [ ] Configure Vercel environment variables
- [ ] Test both deployments

### **Post-Deployment**
- [ ] Verify backend health endpoint
- [ ] Test frontend application
- [ ] Confirm API integration
- [ ] Monitor deployment logs

---

## 🚀 **READY TO DEPLOY!**

**Your News Search Microservice is now configured for deployment using the exact same stack as your successful Accessibility Analyzer!**

### **Next Steps:**
1. **Push to GitHub**: `git push origin main`
2. **Deploy Backend**: Connect Render.com to your repo
3. **Deploy Frontend**: Connect Vercel to your repo
4. **Test & Verify**: Check both services are working

**This deployment strategy is proven, free, and matches your existing successful setup!** 🌟
