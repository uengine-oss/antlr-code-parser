# Robo Analyzer — ANTLR Code Parser

> **다양한 언어(Java, C, Python, Oracle PL/SQL, PostgreSQL)의 소스를 수집하고 ANTLR로 파싱해 AST JSON을 생성하는 Spring Boot 백엔드.** 언어는 호출자가 지정하지 않고 **자동 감지**한다(확장자 + `.sql` 방언 마커 점수).

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.0-6DB33F?style=flat&logo=spring-boot)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-007396?style=flat&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![ANTLR](https://img.shields.io/badge/ANTLR-4.13.2-FF6600?style=flat)](https://www.antlr.org/)

---

## 📤 API 스펙

### 1. 파일 업로드 (`POST /antlr/fileUpload`)

파일을 서버에 업로드·저장합니다. **기존 파일은 모두 삭제되고 새로 업로드된 파일로 대체됩니다.**

#### 요청
| 항목 | 값 |
|------|-----|
| Content-Type | `multipart/form-data` |

| Part | 타입 | 설명 |
|------|------|------|
| `metadata` | JSON string | 선택. **`targetFolder`** 만 사용 |
| `files` | File[] | 업로드 파일들(파일명에 상대경로 포함) |

> metadata는 `targetFolder`(선택)만 읽습니다. **언어/전략은 보내지 않습니다 — 자동 감지**합니다.

#### 응답
```json
{
  "files":         [ {"fileName": "user/UserService.java", "fileContent": "..."} ],
  "ddlFiles":      [ {"fileName": "schema.sql", "fileContent": "CREATE TABLE..."} ],
  "nontargetFiles":[ {"fileName": "README.md", "fileContent": "..."} ]
}
```
- `files`: 파싱 대상 소스, `ddlFiles`: 표 정의 SQL, `nontargetFiles`: 대상 외 파일.

---

### 2. 파싱 요청 (`POST /antlr/parsing`)

업로드된(또는 경로 모드의) 파일을 ANTLR로 파싱해 AST JSON을 생성하고, 진행 상황을 NDJSON 스트림으로 전달합니다.

#### 요청
| 항목 | 값 |
|------|-----|
| Content-Type | `application/json` |

```json
{ "project_root": "C:/path/to/source" }   // 선택
```
- **`project_root` 가 있으면**: 로컬 폴더를 직접 반입·파싱(Electron **경로 모드**).
- **없으면**: 업로드된 `data/source` 를 파싱(브라우저 업로드 모드).
- 그 외 필드는 무시됩니다(**언어 자동 감지**).

#### 응답
- Content-Type: `application/x-ndjson` (NDJSON 스트림), 타임아웃 30분.

```
{"type":"message","content":"🔎 언어 자동 감지..."}
{"type":"detected","target":"java","strategy":"framework","sqlDialect":null,"targets":[...]}
{"type":"message","content":"📄 [1/5] user/UserService.java 파싱 시작..."}
{"type":"skipped","content":"..."}
{"type":"message","content":"🎉 파싱 완료! ..."}
{"type":"complete"}
```
**이벤트 타입**: `message`(진행) · `detected`(감지 결과 `{target,strategy,sqlDialect,targets}`) · `skipped`(대상 외) · `error` · `complete`.

> ⚠️ AST JSON 내용은 응답에 포함되지 않습니다 — `analysis/` 폴더에 저장됩니다.

---

## 🧭 DDL 구분 (경로가 아니라 내용 기반)

`IntakeClassifier` 가 **파일 내용**으로 분류합니다:
- `.sql` 안에 표 정의(CREATE TABLE/VIEW/INDEX/SEQUENCE)만 있고 프로시저(FUNCTION/PROCEDURE/PACKAGE/TRIGGER/TYPE)가 없으면 → **DDL**.
- 그 외 `.sql` 및 `.sql` 아닌 소스 → **SOURCE**.
- `ddl/` 접두 경로는 역호환 힌트로만 쓰고 분류 전 제거됩니다(경로로 판단하지 않음).

---

## 📁 저장 구조

저장 베이스는 프로세스 작업 디렉토리의 상위 폴더 밑 `data/` (또는 env `DOCKER_COMPOSE_CONTEXT`).

```
data/
  ├── source/     ← 소스 파일 (원본 폴더 구조 유지)
  ├── ddl/        ← DDL 파일
  └── analysis/   ← 파싱 결과 AST JSON (source 와 동일 구조)
```

> 업로드 시 기존 `source/`·`ddl/`·`analysis/` 내용은 모두 삭제되고 대체됩니다.

---

## 🔧 지원 Target (자동 감지)

| Target | 전략 클래스 | target 값 | 확장자 |
|--------|------------|-----------|--------|
| Java | JavaParserStrategy | `java` | `.java` |
| C | CParserStrategy | `c` | `.c .h` |
| Python | PythonParserStrategy | `python` | `.py` |
| Oracle PL/SQL | PlSqlParserStrategy | `oracle` | `.sql .pks .pkb .prc .fnc` |
| PostgreSQL | PostgreSqlParserStrategy | `postgresql` | `.sql` |

> `.sql` 은 Oracle/PostgreSQL 둘 다 주장하므로, 프로젝트 단위 방언 마커 점수로 1회 결정합니다(동점/0이면 oracle 기본 — `LanguageDetector`).

---

## 🚀 빠른 시작

### 요구사항
- JDK 17+
- Maven 3.8+

### 빌드 & 실행
```bash
mvn clean install -Dmaven.test.skip=true
mvn spring-boot:run
```
> 코드에 `server.port` 설정이 없어 **기본 포트 8080**으로 뜹니다. 컨테이너 배포(`docker-compose.yml`/`Dockerfile`)는 **8081**로 매핑·노출합니다. 헬스체크는 `GET /` → 본문 `OK`.

### API 테스트 (로컬 8080 기준)
```bash
curl http://localhost:8080/                                  # 헬스체크 → OK
curl -X POST http://localhost:8080/antlr/fileUpload \
  -F 'metadata={"targetFolder":"myproj"}' \
  -F "files=@Main.java;filename=Main.java"
curl -X POST http://localhost:8080/antlr/parsing \
  -H "Content-Type: application/json" -d '{}'                # 업로드 모드(자동 감지)
```

---

## 📂 프로젝트 구조

```
src/main/java/legacymodernizer/parser/
├── ParserApplication.java          ← Spring Boot 진입점
├── controller/
│   ├── FileUploadController.java   ← /antlr/fileUpload, /antlr/parsing
│   └── HealthCheckController.java  ← GET / → "OK"
├── service/
│   ├── FileStorageService.java     ← 파일 저장/반입(업로드·경로 모드)
│   ├── IntakeClassifier.java       ← DDL/SOURCE 내용 기반 분류
│   ├── LanguageDetector.java       ← 언어/방언 자동 감지
│   ├── ParsingOrchestrator.java    ← 파일별 자동 라우팅·파싱·스트림
│   ├── ParseProgressTracker.java
│   ├── StreamCallback.java
│   └── strategy/
│       ├── TargetParserStrategy.java · AbstractParserStrategy.java
│       ├── JavaParserStrategy · CParserStrategy · PythonParserStrategy
│       └── PlSqlParserStrategy · PostgreSqlParserStrategy
├── model/
│   └── Node.java                   ← AST 노드 (toJson 직렬화)
├── antlr/                          ← 생성된 파서(커밋됨): java/ c/ python/ plsql/ postgresql/ plpgsql/
└── config/
    └── WebConfig.java              ← CORS, 예외 처리
```
> 문법 원본(`.g4`)은 repo 루트 `antlr-grammars/`, 생성 파서는 `src/.../antlr/**` 에 사전 생성·커밋되어 있습니다.

---

## 🔑 핵심 포인트

1. **언어 자동 감지**: 호출자가 언어를 지정하지 않음(확장자 + `.sql` 방언 점수).
2. **DDL 구분 = 내용 기반**(`IntakeClassifier`), 경로 아님.
3. **2단계 처리**: 업로드 → 파싱 분리. 경로 모드는 업로드 생략(`project_root`).
4. **파싱 결과**: 응답에 없고 `analysis/` 폴더에 저장.
5. **파일 대체**: 업로드 시 기존 파일 모두 삭제 후 새로 저장.
6. **파싱 스트림**: NDJSON 실시간 진행(타임아웃 30분).
7. **파일 크기 제한**: 코드에 multipart 한도 설정이 **없어 Spring 기본**(파일 1MB / 요청 10MB)이 적용됩니다. 큰 파일을 받으려면 `application.yml` 에 `spring.servlet.multipart.max-file-size/max-request-size` 를 추가해야 합니다.

---

## 📚 참고
- [ANTLR 4](https://github.com/antlr/antlr4)
- [Spring Boot 3.3.0](https://docs.spring.io/spring-boot/docs/3.3.0/reference/html/)
