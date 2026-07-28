# ANTLR Code Parser Dockerfile
# Spring Boot + ANTLR4

FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app
COPY pom.xml .
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests -B

# Runtime stage (멀티플랫폼 지원)
FROM eclipse-temurin:17-jre

WORKDIR /app
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

# Copy the built jar
COPY --from=builder /app/target/*.jar app.jar

# Expose port
EXPOSE 8081

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl --fail --silent http://localhost:8081/actuator/health >/dev/null || exit 1

# Run the application with increased memory for ANTLR parsing
ENTRYPOINT ["java", "-Xms512m", "-Xmx4096m", "-jar", "app.jar"]
