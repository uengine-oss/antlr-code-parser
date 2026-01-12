# Robo Analter ANTLR Code Parser

> **robo_analter를 위한 ANTLR 코드 파서 - 다양한 언어(Java, Oracle PL/SQL, PostgreSQL 등)의 소스 파일을 수집하고 ANTLR로 파싱하여 AST JSON을 제공하는 Spring Boot 기반 백엔드**

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.0-6DB33F?style=flat&logo=spring-boot)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-007396?style=flat&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![ANTLR](https://img.shields.io/badge/ANTLR-4.13.1-FF6600?style=flat)](https://www.antlr.org/)

---

## 📤 API 스펙

### 1. 파일 업로드 (`POST /antlr/fileUpload`)

파일을 서버에 업로드하고 저장합니다. **기존 파일은 모두 삭제되고 새로 업로드된 파일로 대체됩니다.**

#### 요청

| 항목 | 값 |
|------|-----|
| **Content-Type** | `multipart/form-data` |

| Header | 설명 |
|--------|------|
| `Accept-Language` | 언어 설정 (선택, 예: `ko`) |
| `OpenAI-Api-Key` | OpenAI API 키 (선택) |

| Part | 타입 | 설명 |
|------|------|------|
| `metadata` | JSON string | 파싱 설정 |
| `files` | File[] | 업로드할 파일들 (파일명에 상대경로 포함) |

**metadata 형식:**
```json
{
  "strategy": "framework",    // "framework" | "dbms"
  "target": "java",           // "java" | "oracle" | "postgresql"
  "nameCase": "original"      // "original" | "uppercase" | "lowercase"
}
```

**파일명 형식:**
```
{상대경로}/{파일명}

예시:
user/UserService.java        ← 소스 파일 → source/user/UserService.java
order/OrderController.java   ← 소스 파일 → source/order/OrderController.java
ddl/schema.sql               ← DDL 파일  → ddl/schema.sql
ddl/tables/user.sql          ← DDL 파일  → ddl/tables/user.sql
```

> **DDL 구분**: 경로가 `ddl/...`로 시작하면 DDL 파일로 자동 분류

#### 응답

```json
{
  "files": [
    {"fileName": "user/UserService.java", "fileContent": "package user;..."},
    {"fileName": "order/OrderController.java", "fileContent": "package order;..."}
  ],
  "ddlFiles": [
    {"fileName": "ddl/schema.sql", "fileContent": "CREATE TABLE..."},
    {"fileName": "ddl/tables/user.sql", "fileContent": "CREATE TABLE users..."}
  ]
}
```

---

### 2. 파싱 요청 (`POST /antlr/parsing`)

업로드된 파일들을 ANTLR로 파싱하여 AST JSON을 생성합니다. 진행 상황을 실시간으로 NDJSON 스트림으로 전달합니다.

#### 요청

| 항목 | 값 |
|------|-----|
| **Content-Type** | `application/json` |

| Header | 설명 |
|--------|------|
| `Accept-Language` | 언어 설정 (선택, 예: `ko`) |

```json
{
  "strategy": "framework",
  "target": "java",
  "nameCase": "original"
}
```

#### 응답

- **Content-Type**: `application/x-ndjson` (NDJSON 스트림)
- **타임아웃**: 30분 (대용량 파일 대비)

**응답 형식 (NDJSON - 줄바꿈으로 구분)**
```
{"type": "message", "content": "🚀 파싱을 시작합니다. (총 5개 파일)"}
{"type": "message", "content": "📄 [1/5] user/UserService.java 파싱 시작... (523라인)"}
{"type": "message", "content": "📍 user/UserService.java - 523라인까지 파싱 중..."}
{"type": "message", "content": "✅ [1/5] user/UserService.java 완료 (523라인)"}
{"type": "message", "content": "🎉 파싱 완료! 총 5개 파일, 2,450라인 처리됨"}
{"type": "complete"}
```

**타입**
- `message`: 진행 상황 메시지
- `complete`: 파싱 완료
- `error`: 에러 발생 시

> ⚠️ **파싱 결과(AST JSON 내용)는 응답에 포함되지 않습니다.** 파싱 결과는 서버의 `analysis/` 폴더에 저장됩니다.

---

## 📁 저장 구조

```
data/
  ├── source/                 ← 소스 파일 (원본 폴더 구조 유지)
  │   ├── user/
  │   │   └── UserService.java
  │   └── order/
  │       └── OrderController.java
  ├── ddl/                    ← DDL 파일 (원본 폴더 구조 유지)
  │   ├── schema.sql
  │   └── tables/
  │       └── user.sql
  └── analysis/               ← 파싱 결과 JSON (source와 동일 구조)
      ├── user/
      │   └── UserService.json
      └── order/
          └── OrderController.json
```

> **주의**: 파일 업로드 시 기존 `source/`, `ddl/`, `analysis/` 폴더 내용은 모두 삭제되고 새로 업로드된 파일로 대체됩니다.

---

## 📊 전체 흐름

```
┌─────────────────────────────────────────────────────────────────┐
│                        프론트엔드                                │
├─────────────────────────────────────────────────────────────────┤
│  1. 사용자가 파일/폴더 선택                                       │
│  2. 파일 경로를 상대경로로 설정                                   │
│     - 소스 파일: {상대경로}/{파일명}                              │
│     - DDL 파일:  ddl/{상대경로}/{파일명}                         │
│  3. FormData 구성                                                │
│     - metadata: JSON 문자열                                       │
│     - files: 모든 파일 (filename에 경로 포함)                      │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│              POST /antlr/fileUpload (multipart/form-data)        │
├─────────────────────────────────────────────────────────────────┤
│  ⚠️ 기존 파일 모두 삭제 후 새로 저장                              │
│  서버가 filename 경로로 소스/DDL 파일 자동 구분                    │
│  (ddl/로 시작하면 DDL → ddl/ 폴더에 저장)                         │
│  (그 외 → source/ 폴더에 저장)                                    │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                      응답 (JSON)                                 │
├─────────────────────────────────────────────────────────────────┤
│  {                                                               │
│    "files": [...],      // 소스 파일 (fileName, fileContent)     │
│    "ddlFiles": [...]    // DDL 파일 (fileName, fileContent)      │
│  }                                                               │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│              POST /antlr/parsing (application/json)              │
├─────────────────────────────────────────────────────────────────┤
│  { "strategy": "framework", "target": "java", "nameCase": "original" }│
│  (파일 없이 메타데이터만 전송)                                      │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│              응답 (NDJSON 스트림 - 실시간 진행 상황)              │
├─────────────────────────────────────────────────────────────────┤
│  {"type": "message", "content": "🚀 파싱을 시작합니다..."}        │
│  {"type": "message", "content": "📄 UserService.java 파싱..."}    │
│  {"type": "complete"}                                           │
│  (파싱 결과는 analysis/ 폴더에 저장됨)                            │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🔧 지원 Target

| Target | 전략 클래스 | target 값 | 파서 |
|--------|------------|-----------|------|
| **Java** | JavaParserStrategy | `java` | Java20Lexer/Parser |
| **Oracle** | PlSqlParserStrategy | `oracle`, `plsql` | PlSqlLexer/Parser |
| **PostgreSQL** | PostgreSqlParserStrategy | `postgresql`, `postgres` | PostgreSQLLexer/Parser |

---

## 🚀 빠른 시작

### 요구사항

- JDK 17+
- Maven 3.8+

### 빌드 & 실행

```bash
# 빌드
mvn clean install -Dmaven.test.skip=true

# 실행
mvn spring-boot:run
```

서버: `http://localhost:8081`

### API 테스트

```bash
# 헬스체크
curl http://localhost:8081/

# 파일 업로드
curl -X POST http://localhost:8081/antlr/fileUpload \
  -F 'metadata={"strategy":"framework","target":"java","nameCase":"original"}' \
  -F "files=@Main.java;filename=Main.java"

# 파싱 (NDJSON 스트림 응답)
curl -X POST http://localhost:8081/antlr/parsing \
  -H "Content-Type: application/json" \
  -d '{"strategy":"framework","target":"java","nameCase":"original"}'
```

---

## 📂 프로젝트 구조

```
src/main/java/legacymodernizer/parser/
├── ParserApplication.java          ← Spring Boot 진입점
├── controller/
│   ├── FileUploadController.java   ← REST API (/antlr/fileUpload, /antlr/parsing)
│   └── HealthCheckController.java  ← 헬스체크 API (GET /)
├── service/
│   ├── FileParserService.java      ← 파일 저장/파싱 공통 로직
│   └── parsing/
│       ├── TargetParserStrategy.java     ← 전략 인터페이스
│       ├── ParserStrategyFactory.java    ← 전략 팩토리
│       ├── JavaParserStrategy.java       ← Java 파싱
│       ├── PlSqlParserStrategy.java      ← Oracle PL/SQL 파싱
│       └── PostgreSqlParserStrategy.java ← PostgreSQL 파싱
├── antlr/
│   ├── Node.java                   ← AST 노드 (toJson 직렬화)
│   ├── java/                       ← Java ANTLR 파일
│   ├── plsql/                      ← Oracle PL/SQL ANTLR 파일
│   └── postgresql/                 ← PostgreSQL ANTLR 파일
└── config/
    └── WebConfig.java              ← CORS, 예외 처리
```

---

## 🔑 핵심 포인트

1. **DDL 구분**: 별도 필드가 아닌 파일 경로로 자동 구분 (`ddl/`로 시작하면 DDL)
2. **폴더 구조 유지**: 업로드된 폴더 구조가 그대로 유지됨
3. **2단계 처리**: 업로드 → 파싱이 분리 (파싱은 메타데이터만 전송)
4. **파싱 결과**: 응답에 포함되지 않고 `analysis/` 폴더에 저장
5. **파일 대체**: 업로드 시 기존 파일 모두 삭제 후 새로 저장
6. **파싱 스트림**: 파싱 API는 NDJSON 스트림으로 진행 상황을 실시간 전달 (타임아웃 30분)
7. **파일 크기 제한**: 최대 파일 크기 100MB, 최대 요청 크기 500MB

---

## 📚 참고

- [ANTLR 4 공식 문서](https://github.com/antlr/antlr4)
- [Spring Boot 레퍼런스](https://docs.spring.io/spring-boot/docs/3.3.0/reference/html/)
