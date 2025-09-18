pipeline {
    agent any
    
    environment {
        MAVEN_OPTS = '-Dmaven.repo.local=.m2/repository'
        DOCKER_IMAGE = 'news-search-microservice'
        DOCKER_TAG = "${BUILD_NUMBER}"
        NEWS_API_KEY = credentials('news-api-key')
    }
    
    tools {
        maven 'Maven-3.9.4'
        jdk 'OpenJDK-17'
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
                script {
                    env.GIT_COMMIT_SHORT = sh(
                        script: "git rev-parse --short HEAD",
                        returnStdout: true
                    ).trim()
                }
            }
        }
        
        stage('Build') {
            steps {
                sh 'mvn clean compile'
            }
        }
        
        stage('Unit Tests') {
            steps {
                sh 'mvn test'
            }
            post {
                always {
                    publishTestResults testResultsPattern: 'target/surefire-reports/*.xml'
                    publishCoverage adapters: [
                        jacocoAdapter('target/site/jacoco/jacoco.xml')
                    ], sourceFileResolver: sourceFiles('STORE_LAST_BUILD')
                }
            }
        }
        
        stage('Integration Tests') {
            steps {
                sh 'mvn verify -P integration-tests'
            }
            post {
                always {
                    publishTestResults testResultsPattern: 'target/failsafe-reports/*.xml'
                }
            }
        }
        
        stage('BDD Tests') {
            steps {
                sh 'mvn test -Dtest=CucumberTestRunner'
            }
            post {
                always {
                    publishHTML([
                        allowMissing: false,
                        alwaysLinkToLastBuild: true,
                        keepAll: true,
                        reportDir: 'target/cucumber-reports',
                        reportFiles: 'index.html',
                        reportName: 'Cucumber BDD Report'
                    ])
                }
            }
        }
        
        stage('Code Quality Analysis') {
            parallel {
                stage('SonarQube Analysis') {
                    when {
                        anyOf {
                            branch 'main'
                            branch 'develop'
                        }
                    }
                    steps {
                        withSonarQubeEnv('SonarQube') {
                            sh 'mvn sonar:sonar'
                        }
                    }
                }
                
                stage('Security Scan') {
                    steps {
                        sh 'mvn org.owasp:dependency-check-maven:check'
                    }
                    post {
                        always {
                            publishHTML([
                                allowMissing: false,
                                alwaysLinkToLastBuild: true,
                                keepAll: true,
                                reportDir: 'target',
                                reportFiles: 'dependency-check-report.html',
                                reportName: 'OWASP Dependency Check Report'
                            ])
                        }
                    }
                }
            }
        }
        
        stage('Package') {
            steps {
                sh 'mvn package -DskipTests'
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }
        
        stage('Docker Build') {
            steps {
                script {
                    def image = docker.build("${DOCKER_IMAGE}:${DOCKER_TAG}")
                    docker.withRegistry('https://registry.hub.docker.com', 'docker-hub-credentials') {
                        image.push()
                        image.push('latest')
                    }
                }
            }
        }
        
        stage('Security Scan - Docker Image') {
            steps {
                script {
                    sh "docker run --rm -v /var/run/docker.sock:/var/run/docker.sock aquasec/trivy:latest image ${DOCKER_IMAGE}:${DOCKER_TAG}"
                }
            }
        }
        
        stage('Deploy to Staging') {
            when {
                branch 'develop'
            }
            steps {
                script {
                    sh """
                        docker-compose -f docker-compose.staging.yml down
                        docker-compose -f docker-compose.staging.yml up -d
                    """
                }
            }
        }
        
        stage('Smoke Tests') {
            when {
                branch 'develop'
            }
            steps {
                script {
                    sleep(time: 30, unit: 'SECONDS') // Wait for service to start
                    sh '''
                        curl -f http://localhost:8080/api/v1/news/health || exit 1
                        curl -f "http://localhost:8080/api/v1/news/search?keyword=test" || exit 1
                    '''
                }
            }
        }
        
        stage('Performance Tests') {
            when {
                branch 'develop'
            }
            steps {
                script {
                    sh '''
                        # Run JMeter performance tests
                        jmeter -n -t performance-tests/news-search-load-test.jmx -l target/jmeter-results.jtl
                    '''
                }
            }
            post {
                always {
                    publishHTML([
                        allowMissing: false,
                        alwaysLinkToLastBuild: true,
                        keepAll: true,
                        reportDir: 'target',
                        reportFiles: 'jmeter-report.html',
                        reportName: 'JMeter Performance Report'
                    ])
                }
            }
        }
        
        stage('Deploy to Production') {
            when {
                branch 'main'
            }
            steps {
                script {
                    input message: 'Deploy to Production?', ok: 'Deploy'
                    
                    // Blue-Green Deployment
                    sh """
                        # Deploy to green environment
                        docker-compose -f docker-compose.prod.yml up -d --scale news-search-microservice=2
                        
                        # Health check
                        sleep 60
                        curl -f http://localhost:8080/api/v1/news/health || exit 1
                        
                        # Switch traffic (this would typically involve load balancer configuration)
                        echo "Production deployment completed successfully"
                    """
                }
            }
        }
    }
    
    post {
        always {
            cleanWs()
        }
        
        success {
            emailext (
                subject: "✅ Pipeline Success: ${env.JOB_NAME} - ${env.BUILD_NUMBER}",
                body: """
                    <h2>Build Successful</h2>
                    <p><strong>Job:</strong> ${env.JOB_NAME}</p>
                    <p><strong>Build Number:</strong> ${env.BUILD_NUMBER}</p>
                    <p><strong>Git Commit:</strong> ${env.GIT_COMMIT_SHORT}</p>
                    <p><strong>Duration:</strong> ${currentBuild.durationString}</p>
                    <p><a href="${env.BUILD_URL}">View Build Details</a></p>
                """,
                to: "${env.CHANGE_AUTHOR_EMAIL}",
                mimeType: 'text/html'
            )
        }
        
        failure {
            emailext (
                subject: "❌ Pipeline Failed: ${env.JOB_NAME} - ${env.BUILD_NUMBER}",
                body: """
                    <h2>Build Failed</h2>
                    <p><strong>Job:</strong> ${env.JOB_NAME}</p>
                    <p><strong>Build Number:</strong> ${env.BUILD_NUMBER}</p>
                    <p><strong>Git Commit:</strong> ${env.GIT_COMMIT_SHORT}</p>
                    <p><strong>Failed Stage:</strong> ${env.STAGE_NAME}</p>
                    <p><a href="${env.BUILD_URL}">View Build Details</a></p>
                    <p><a href="${env.BUILD_URL}console">View Console Output</a></p>
                """,
                to: "${env.CHANGE_AUTHOR_EMAIL}",
                mimeType: 'text/html'
            )
        }
    }
}
