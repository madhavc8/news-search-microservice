# Multi-stage Docker build for News Search Microservice
# Stage 1: Build the application
FROM maven:3.9.4-openjdk-17-slim AS builder

# Set working directory
WORKDIR /app

# Copy pom.xml and download dependencies (for better layer caching)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Stage 2: Create the runtime image
FROM openjdk:17-jre-slim

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Create non-root user for security
RUN groupadd -r newsservice && useradd -r -g newsservice newsservice

# Set working directory
WORKDIR /app

# Copy the built JAR from builder stage
COPY --from=builder /app/target/news-search-microservice-*.jar app.jar

# Change ownership to non-root user
RUN chown -R newsservice:newsservice /app

# Switch to non-root user
USER newsservice

# Expose port 8080
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/api/v1/news/health || exit 1

# Environment variables for configuration
ENV SPRING_PROFILES_ACTIVE=prod
ENV SERVER_PORT=8080
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar app.jar"]

# Labels for metadata
LABEL maintainer="News Service Team <support@newsservice.com>"
LABEL version="1.0.0"
LABEL description="Production-ready News Search Microservice"
LABEL org.opencontainers.image.source="https://github.com/newsservice/news-search-microservice"
