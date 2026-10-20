# ANTLR Code Parser Dockerfile
# Spring Boot + ANTLR4

FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app
COPY pom.xml .
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests -B

# Runtime stage (멀티플랫폼 지원)
# ⚠ 베이스를 `17-jre` 가 아니라 **`17-jre-noble`** 로 둔다 (2026-10-20 · done15 §298).
#
# `eclipse-temurin:17-jre` 는 지금 **pebble**(Go 실행 관리자)를 품고 있고,
# 그 안의 Go stdlib 1.27.1 이 **치명 2 · 고위험 9** 를 올린다. 우리 코드와
# 무관하고 **우리가 고칠 수 없는** 것이라 ISS-39 로 묶어 둔 것이었다.
#
# 2026-10-20 에 베이스 태그를 나란히 재 보고 **그 전제가 틀렸다는 것을 알았다**:
#     eclipse-temurin:17-jre          **2C  9H**   (pebble)
#     eclipse-temurin:17-jre-alpine    0C  3H
#     eclipse-temurin:17-jre-noble    **0C  0H**   ← 이것
#     eclipse-temurin:17-jre-jammy     0C  0H
#
# noble = Ubuntu 24.04 LTS · glibc 라 **지금 베이스와 같은 계열**이다
# (alpine 은 musl 이라 갈아타기 위험이 있고, 그만큼 걸러지지도 않는다).
# ⚠ **태그를 고정한 이유** — `17-jre` 는 최신 Ubuntu 를 따라가면서 pebble 을
#   들였다. 고정하지 않으면 **다음 굽기에 조용히 되돌아간다**.
FROM eclipse-temurin:17-jre-noble

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
    CMD curl --fail --silent http://localhost:8081/ >/dev/null || exit 1

# Run the application with increased memory for ANTLR parsing
ENTRYPOINT ["java", "-Xms512m", "-Xmx4096m", "-jar", "app.jar"]
