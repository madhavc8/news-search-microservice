@echo off
REM News Search Microservice Deployment Script for Windows
REM This script handles building and deploying the application locally

setlocal enabledelayedexpansion

REM Configuration
set PROJECT_NAME=news-search-microservice
set DOCKER_IMAGE=news-search-microservice
set FRONTEND_IMAGE=news-frontend
set ENVIRONMENT=%1
if "%ENVIRONMENT%"=="" set ENVIRONMENT=dev
if "%NEWS_API_KEY%"=="" set NEWS_API_KEY=ccaf5d41cc5140c984818c344edcc14d

REM Colors (limited in Windows batch)
set INFO=[INFO]
set SUCCESS=[SUCCESS]
set WARNING=[WARNING]
set ERROR=[ERROR]

echo %INFO% Starting News Search Microservice Deployment...
echo %INFO% Environment: %ENVIRONMENT%

REM Check prerequisites
echo %INFO% Checking prerequisites...

where docker >nul 2>nul
if %errorlevel% neq 0 (
    echo %ERROR% Docker is not installed or not in PATH
    exit /b 1
)

docker info >nul 2>nul
if %errorlevel% neq 0 (
    echo %ERROR% Docker is not running
    exit /b 1
)

where docker-compose >nul 2>nul
if %errorlevel% neq 0 (
    echo %ERROR% Docker Compose is not installed or not in PATH
    exit /b 1
)

where mvn >nul 2>nul
if %errorlevel% neq 0 (
    echo %ERROR% Maven is not installed or not in PATH
    exit /b 1
)

where node >nul 2>nul
if %errorlevel% neq 0 (
    echo %ERROR% Node.js is not installed or not in PATH
    exit /b 1
)

echo %SUCCESS% All prerequisites are met!

REM Clean up existing containers
echo %INFO% Cleaning up existing containers...
docker-compose -f docker-compose.yml down --remove-orphans >nul 2>nul
docker-compose -f docker-compose.staging.yml down --remove-orphans >nul 2>nul
docker-compose -f docker-compose.prod.yml down --remove-orphans >nul 2>nul

REM Remove old images
docker rmi %DOCKER_IMAGE%:latest >nul 2>nul
docker rmi %FRONTEND_IMAGE%:latest >nul 2>nul

echo %SUCCESS% Environment cleaned up!

REM Run tests
echo %INFO% Running backend tests...
call mvn clean test
if %errorlevel% neq 0 (
    echo %ERROR% Backend tests failed
    exit /b 1
)

echo %INFO% Running frontend tests...
cd frontend
call npm test -- --coverage --watchAll=false
if %errorlevel% neq 0 (
    echo %ERROR% Frontend tests failed
    cd ..
    exit /b 1
)
cd ..

echo %SUCCESS% All tests passed!

REM Build backend
echo %INFO% Building backend application...
call mvn clean package -DskipTests
if %errorlevel% neq 0 (
    echo %ERROR% Backend build failed
    exit /b 1
)

docker build -t %DOCKER_IMAGE%:latest .
if %errorlevel% neq 0 (
    echo %ERROR% Backend Docker build failed
    exit /b 1
)

echo %SUCCESS% Backend built successfully!

REM Build frontend
echo %INFO% Building frontend application...
cd frontend

call npm ci
if %errorlevel% neq 0 (
    echo %ERROR% Frontend dependency installation failed
    cd ..
    exit /b 1
)

call npm run build
if %errorlevel% neq 0 (
    echo %ERROR% Frontend build failed
    cd ..
    exit /b 1
)

docker build -t %FRONTEND_IMAGE%:latest .
if %errorlevel% neq 0 (
    echo %ERROR% Frontend Docker build failed
    cd ..
    exit /b 1
)

cd ..
echo %SUCCESS% Frontend built successfully!

REM Deploy based on environment
if "%ENVIRONMENT%"=="dev" (
    echo %INFO% Deploying to local development environment...
    docker-compose up -d
) else if "%ENVIRONMENT%"=="staging" (
    echo %INFO% Deploying to staging environment...
    docker-compose -f docker-compose.staging.yml up -d
) else if "%ENVIRONMENT%"=="prod" (
    echo %INFO% Deploying to production environment...
    set /p CONFIRM="Are you sure you want to deploy to production? (y/N): "
    if /i not "!CONFIRM!"=="y" (
        echo %WARNING% Production deployment cancelled.
        exit /b 0
    )
    docker-compose -f docker-compose.prod.yml up -d
) else (
    echo %ERROR% Unknown environment: %ENVIRONMENT%
    echo Usage: deploy.bat [dev^|staging^|prod]
    exit /b 1
)

if %errorlevel% neq 0 (
    echo %ERROR% Deployment failed
    exit /b 1
)

echo %INFO% Waiting for services to start...
timeout /t 30 /nobreak >nul

REM Health check
echo %INFO% Performing health checks...
set MAX_ATTEMPTS=10
set ATTEMPT=1

:healthcheck
curl -f -s http://localhost:8080/api/v1/news/health >nul 2>nul
if %errorlevel% equ 0 (
    echo %SUCCESS% Backend service is healthy!
    goto :healthcheck_done
) else (
    echo %WARNING% Backend health check failed (attempt %ATTEMPT%/%MAX_ATTEMPTS%^)
    if %ATTEMPT% geq %MAX_ATTEMPTS% (
        echo %ERROR% Backend service failed to start properly
        echo %INFO% Showing recent logs...
        docker-compose logs --tail=50
        exit /b 1
    )
    timeout /t 10 /nobreak >nul
    set /a ATTEMPT+=1
    goto :healthcheck
)

:healthcheck_done

REM Test API endpoint
curl -f -s "http://localhost:8080/api/v1/news/search?keyword=test" >nul 2>nul
if %errorlevel% equ 0 (
    echo %SUCCESS% API endpoint is working!
) else (
    echo %WARNING% API endpoint test failed
)

echo.
echo %SUCCESS% Deployment completed successfully!
echo.
echo Service URLs:
echo   Backend API: http://localhost:8080/api/v1/news
echo   Swagger UI:  http://localhost:8080/swagger-ui.html
echo   Frontend:    http://localhost:80 (if deployed^)
echo   Health:      http://localhost:8080/api/v1/news/health
echo.
echo To view logs: docker-compose logs -f
echo To stop services: docker-compose down
echo.

endlocal
