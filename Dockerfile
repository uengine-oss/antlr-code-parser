# ANTLR Code Parser Dockerfile
# Spring Boot + ANTLR4

FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app
COPY pom.xml .
COPY src ./src
# 문법 원본도 넣는다 (2026-10-22) — 카탈로그 테스트가 `antlr-grammars/*.g4` 의
# sha256 핀을 **실제 파일과** 맞춘다. 빌더 단계에만 들어가고 런타임 이미지에는
# 안 실린다.
COPY antlr-grammars ./antlr-grammars

# Build the application
#
# ⚠ **카탈로그·계약 테스트는 빌드에서 돈다** (2026-10-22).
#
# 전에는 `-DskipTests` 로 전부 건너뛰었다. 그 사이에 **두 달 동안 세 가지가
# 조용히 깨져 있었다**:
#   · 문법 파일 네 개의 sha256 핀이 카탈로그와 어긋났다(2026-08 변경 뒤로)
#   · `core.autocrlf` 때문에 테스트 입력이 CRLF 로 깔려 골든이 전부 틀어졌다
#   · Boot 4 이식에서 `EvidenceIrSealer` 가 죽어 **Java 파싱이 안 됐다**
#     (`writeTree()` → `UnsupportedOperationException`)
# 마지막 것은 **설치본까지 나갔고**, 사용자가 코드 분석을 돌려서야 드러났다.
#
# 전부 돌리면 말뭉치 테스트까지 끌려와 느리다. 그래서 **바이트 계약을 지키는
# 것만** 돌린다 — 약 3분 30초다. 이 셋이 제품의 출력 모양을 고정한다.
RUN mvn -B clean package       -Dtest='LanguageCatalogValidationTest,*ContractTest'       -DfailIfNoTests=false -Dsurefire.failIfNoSpecifiedTests=false

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
