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
{"schemaVersion":"1.1.0","type":"message","event":"run_started","content":"🧭 코드 구조 파악을 준비하고 있어요","phase":"PREPARING","status":"RUNNING"}
{"schemaVersion":"1.1.0","type":"detected","event":"language_detected","content":"{...}","phase":"DETECTION","status":"COMPLETED","language":"java"}
{"schemaVersion":"1.1.0","type":"message","event":"file_started","content":"📄 [1/5] user/UserService.java — JAVA 320라인을 읽고 있어요","current":1,"total":5,"percent":0,"file":"user/UserService.java","language":"java"}
{"schemaVersion":"1.1.0","type":"message","event":"file_result","content":"✅ [1/5] user/UserService.java — 문법 오류 없이 정확히 파싱됐어요","quality":"EXACT"}
{"schemaVersion":"1.1.0","type":"warning","event":"file_skipped","content":"⏭️ 분석 대상에서 제외했어요: ..."}
{"schemaVersion":"1.1.0","type":"message","event":"run_completed","content":"🎉 파싱 완료 — 정확 5 · 복구 0 · 부분 0 · 검토 필요 0 · 미해결 0 · 실패 0 · AST 5개","percent":100,"counts":{"exact":5,"recovered":0}}
{"schemaVersion":"1.1.0","type":"complete","event":"complete","phase":"COMPLETED","status":"COMPLETED","percent":100}
```
**호환 계약**: 기존 `type/content` 는 유지됩니다. `event`, `phase`, `status`, `current`,
`total`, `percent`, `file`, `language`, `line`, `quality`, `counts` 는 UI가 문자열을 다시
해석하지 않고 진행률과 품질을 표시하도록 추가된 선택 필드입니다. 정상 종료에는
`complete` 가 정확히 한 번 옵니다.

> ⚠️ AST JSON 내용은 응답에 포함되지 않습니다 — `analysis/` 폴더에 저장됩니다.

---

## 🧭 DDL 구분 (경로가 아니라 내용 기반)

`SourceIntakeClassifier` 가 **파일 내용**으로 분류합니다:
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

| Target | 언어 모듈 | target 값 | 확장자 |
|--------|------------|-----------|--------|
| Java | JavaLanguageModule | `java` | `.java` |
| C | CLanguageModule | `c` | `.c .h` |
| Python | PythonLanguageModule | `python` | `.py` |
| Oracle PL/SQL | OracleLanguageModule | `oracle` | `.sql .pks .pkb .prc .fnc` |
| PostgreSQL | PostgreSqlLanguageModule | `postgresql` | `.sql` |

> `.sql` 은 Oracle/PostgreSQL 둘 다 주장하므로, 프로젝트 단위 방언 마커 점수로 1회 결정합니다(동점/0이면 oracle 기본 — `ParserSelection`).

### 새 언어 추가 순서

새 언어는 중앙 `switch`에 덧붙이지 않습니다. 해당 언어 폴더에 Spring `@Component`인
`LanguageModule` 구현체를 추가하면 `LanguageModuleRegistry`가 자동 등록합니다.

1. `antlr-grammars/`에 검토·고정한 Lexer/Parser `.g4`와 출처를 둡니다.
2. 생성 Parser는 `src/main/java/.../antlr/<language>/`에, AST Listener는 같은 언어 경계에 둡니다.
3. `parsing/languages/<language>/`에 `<Language>LanguageModule`과 필요한
   `<Language>SourceUnitLocator`를 추가합니다.
4. 확장자, 공용 확장자 우선순위, 내용 감지 점수와 DBMS/framework 계열을 모듈에서 선언합니다.
5. (선택) `sliceSyntax()`로 주석·문자열·문장 종결자 규칙을, `repairProfile()`로 삭제 허용
   구조 키워드를 선언합니다. 선언하지 않으면 보수적 기본값이 적용되고 자동 복구 범위만
   좁아집니다(fail-closed).
6. `languages/language-catalog.json`에 entry rule, 생성 Node 타입, Grammar SHA-256,
   복구 rule-set을 등록하고 catalog checksum을 갱신합니다.
7. 정상 AST golden test, 문법 오류·최소 단위 복구 test, 기존 Node JSON 불변 test를 추가한 뒤
   `mvn clean test`를 실행합니다. Core 수정 0으로 등록되는지는
   `NewLanguageOnboardingRehearsalTest` 패턴으로 확인합니다.

공통 오류 진단, 품질 판정, 최대 3회 재시도, Repair Agent, 감사 JSON과 NDJSON 스트림은
언어별로 다시 만들지 않습니다. 새 언어 모듈은 문법 실행·AST 변환·최소 복구 단위만 책임집니다.

---

## 🛠️ 실제 GPU Repair Agent

정상 파일은 Agent를 호출하지 않습니다. 실패 unit은 먼저 **결정론 grammar-guided 엔진**이
공략합니다 — ANTLR의 extraneous/missing/mismatched 신호와 기대 토큰 집합으로 단일 토큰
후보를 만들고, wave당 전체 unit 재파싱으로 평가해 유일한 strict 생존자만 채택합니다
(최대 3 wave, 의미 위험 토큰 변경은 재파싱이 성공해도 차단).

엔진이 실패한 경우에만 Agent를 호출하며, 이때도 unit 전체가 아니라 **Parser가 좁힌 bounded
slice**(L1 문장→L2 +선언 헤더→L3 캡 윈도우, 최대 4,000자)만 전송합니다. 제안은
`expectedText + offset + snapshot hash`로 검증하고(excerpt 내 유일 일치 시 결정론 재정박),
전체 unit strict 재파싱이 엄격히 개선된 경우에만 AST에 채택됩니다. 모든 Agent 호출은
문자수 가중 token-budget 세마포어로 동시성이 제한됩니다. 원본 파일은 수정하지 않습니다.

사내 SGLang GPU를 사용하는 Parser 프로세스에는 다음 환경변수를 전달합니다.

```powershell
$env:PARSER_REPAIR_AGENT_ENABLED="true"
$env:PARSER_REPAIR_AGENT_API_BASE="http://ai-server.dream-flow.com:30000/v1"
$env:PARSER_REPAIR_AGENT_MODEL="frentis-ai-model"
$env:PARSER_REPAIR_AGENT_API_KEY="<secret>"
$env:PARSER_REPAIR_AGENT_THINKING_ENABLED="false"   # SGLang 전용
$env:PARSER_REPAIR_AGENT_TOP_K="1"                  # SGLang 전용
$env:PARSER_REPAIR_AGENT_TIMEOUT_SECONDS="600"
```

OpenAI 계열 모델(GPT-5.4-mini 등)은 SGLang 전용 옵션 대신
`PARSER_REPAIR_AGENT_REASONING_EFFORT`(none/low/…)를 사용합니다. 두 옵션군은 provider별로
상호 배타적으로 설정합니다. 동시 Agent 트래픽은
`parser.repair.agent.budget.chars`(기본 200,000자) 예산으로 제한됩니다.

API 키는 저장소나 문서에 기록하지 않습니다. 설정이 없거나 서버 응답·편집 범위·재파싱이
검증을 통과하지 못하면 해당 단위는 `REVIEW_REQUIRED`로 남고 다음 파일 처리는 계속됩니다.

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
├── api/                            ← HTTP·NDJSON 경계
│   ├── FileUploadController.java · HealthCheckController.java · WebConfig.java
│   └── stream/                     ← ParseEventSink · ParseStreamEvent
├── intake/                         ← ParserWorkspace · SourceIntakeClassifier
├── parsing/                        ← ParseOrchestrator · ParserSelection · RawParseResult
│   └── languages/                  ← 공통 등록 + c/java/oracle/postgresql/python 모듈
├── recovery/                       ← 실패 진단·최소단위·규칙·품질·Repair Agent·감사
├── service/ParseProgressTracker.java ← 보호된 Listener 호환을 위한 유일한 임시 예외
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
2. **DDL 구분 = 내용 기반**(`SourceIntakeClassifier`), 경로 아님.
3. **2단계 처리**: 업로드 → 파싱 분리. 경로 모드는 업로드 생략(`project_root`).
4. **파싱 결과**: 응답에 없고 `analysis/` 폴더에 저장.
5. **파일 대체**: 업로드 시 기존 파일 모두 삭제 후 새로 저장.
6. **파싱 스트림**: NDJSON 실시간 진행(타임아웃 30분).
7. **파일 크기 제한**: 코드에 multipart 한도 설정이 **없어 Spring 기본**(파일 1MB / 요청 10MB)이 적용됩니다. 큰 파일을 받으려면 `application.yml` 에 `spring.servlet.multipart.max-file-size/max-request-size` 를 추가해야 합니다.

---

## 📚 참고
- [ANTLR 4](https://github.com/antlr/antlr4)
- [Spring Boot 3.3.0](https://docs.spring.io/spring-boot/docs/3.3.0/reference/html/)
