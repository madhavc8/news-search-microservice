# Multi-stage Docker build for News Search Microservice
# Stage 1: Build the application
FROM maven:3.9.6-openjdk-17-slim AS builder

# Set working directory
WORKDIR /app

# Copy Maven files
COPY pom.xml .
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM openjdk:17-jre-slim

# Create non-root user
RUN groupadd -r newsservice && useradd -r -g newsservice newsservice

# Set working directory
WORKDIR /app

# Copy JAR from builder stage
COPY --from=builder /app/target/news-search-microservice-1.0.0.jar app.jar

# Change ownership
RUN chown -R newsservice:newsservice /app
USER newsservice

# Expose port
EXPOSE 10000

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:10000/api/v1/news/health || exit 1

# Start application
CMD ["java", "-jar", "app.jar"]
