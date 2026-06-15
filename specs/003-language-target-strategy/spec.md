# Feature Specification: Language/Target Parser Strategy & Detection

**Feature Branch**: `003-language-target-strategy`

**Created**: 2026-06-15

**Status**: Backfilled (reverse-engineered)

**Input**: User description: "전략 패턴으로 target(Java / Oracle PL/SQL / PostgreSQL / C / Python)별 알맞은 ANTLR lexer/parser 를 고르고, 파일 확장자 + .sql 방언 점수로 언어·방언을 자동 감지한다. 메타데이터 {strategy, target, nameCase} 로 구동되는 것으로 문서화돼 있으나 실제 코드는 자동 감지로 진화."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - 혼합 소스에서 파일별 올바른 파서 자동 선택 (Priority: P1)

분석가가 Java·C·Python·PL/SQL 파일이 섞인 레거시 프로젝트 폴더를 던지면, 호출자가 언어를 지정하지 않아도 시스템이 파일마다 알맞은 ANTLR 파서를 골라 AST JSON 을 만든다.

**Why this priority**: 이 기능의 본질. 파일별 올바른 전략 선택이 안 되면 파싱 자체가 무의미하다. 단독으로 MVP 가치 제공.

**Independent Test**: `.java`·`.c`·`.py` 가 섞인 디렉토리를 `detect()` 에 넣고, 각 파일이 해당 언어 전략(java/c/python)으로 라우팅되며 미지원 확장자는 스킵되는지 확인.

**Acceptance Scenarios**:

1. **Given** `.java`·`.c`·`.py` 파일이 든 폴더, **When** 감지를 실행, **Then** 각 파일이 확장자 기준으로 JavaParserStrategy/CParserStrategy/PythonParserStrategy 에 1:1 매핑된다.
2. **Given** `.txt`·`.md` 같은 미지원 확장자, **When** 감지를 실행, **Then** 그 파일은 `fileStrategies` 에서 제외(스킵)되어 파싱 대상이 아니다.
3. **Given** 단 하나의 전략만 주장하는 확장자(`.pks` 등), **When** 감지를 실행, **Then** 방언 판별 없이 oracle 로 즉시 확정된다.

---

### User Story 2 - 모호한 .sql 파일의 방언 자동 판별 (Priority: P2)

`.sql` 은 Oracle PL/SQL 과 PostgreSQL 두 전략이 모두 주장하는 모호한 확장자다. 시스템이 파일 내용의 방언 특유 마커를 점수화해 프로젝트 단위로 한 방언을 결정한다.

**Why this priority**: 실무 DDL/프로시저는 거의 `.sql` 로 들어오므로 방언 오판은 파싱 실패로 직결. P1 라우팅 위에서 동작하는 핵심 보강.

**Independent Test**: `VARCHAR2`·`SYSDATE`·`FROM DUAL` 이 든 `.sql` 묶음 → oracle, `$$`·`plpgsql`·`RAISE NOTICE` 가 든 묶음 → postgresql 로 판정되는지 점수 로그로 확인.

**Acceptance Scenarios**:

1. **Given** Oracle 마커가 우세한 `.sql` 들, **When** 감지를 실행, **Then** 프로젝트 전체 `.sql` 이 oracle(PlSqlParserStrategy)로 일괄 라우팅된다.
2. **Given** PostgreSQL 마커가 우세한 `.sql` 들, **When** 감지를 실행, **Then** postgresql(PostgreSqlParserStrategy)로 일괄 라우팅된다.
3. **Given** 마커가 전혀 없는 순수 ANSI `.sql`(점수 0 또는 동점), **When** 감지를 실행, **Then** 레거시 기본값 oracle 로 폴백한다.

---

### User Story 3 - 감지 결과로 파이프라인 strategy·대표 언어 산출 (Priority: P3)

프론트엔드/robo 파이프라인이 `sourceType` 과 `strategy(framework|dbms)` 를 스스로 채우도록, 감지 결과가 대표 언어와 파이프라인 전략을 함께 노출한다.

**Why this priority**: 파싱 자체에는 불필요하나 후속 분석 파이프라인 연동에 필요. 부가 가치 계층.

**Independent Test**: framework 언어(java/c/python)가 하나라도 있으면 `strategy=framework`, SQL 만이면 `dbms`, 비어 있으면 `framework` 가 반환되고 `primaryTarget` 이 최다 파일 타입인지 확인.

**Acceptance Scenarios**:

1. **Given** java 5개 + oracle 2개, **When** 감지를 실행, **Then** `strategy=framework`, `primaryTarget=java`.
2. **Given** oracle `.sql` 만, **When** 감지를 실행, **Then** `strategy=dbms`, `primaryTarget=oracle`.

---

### Edge Cases

- 소스 디렉토리 탐색(Files.walk) 실패 시 → RuntimeException 으로 명확히 중단.
- `.sql` 방언 판별용 파일 읽기가 UTF-8·EUC-KR 모두 실패 → 그 파일은 점수 0 으로 건너뛰고 나머지로 합산.
- 같은 `.sql` 모음에 oracle·postgres 마커가 동점 → oracle 기본(레거시 우선).
- 지원 확장자가 0개인 빈/무관 폴더 → `detectedTargets` 빈 집합, `strategy=framework`, `primaryTarget=null`.
- C 는 파일별 파싱 전 `prepare()` 로 `.c/.h` 전체에서 typedef/매크로를 선수집해야 정확 — 이 준비 단계가 누락되면 사용자 타입 오인식.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: 시스템은 호출자가 언어/target 을 지정하지 않아도 파일별로 적절한 파서 전략을 자동 선택해야 한다(파싱 endpoint 는 요청 body 의 언어 지정을 무시).
- **FR-002**: 각 전략은 `getSupportedTargetType()`(예: java/oracle/postgresql/c/python)과 `getTargetExtensions()`(담당 확장자)을 선언해야 하며, 감지기는 이 값에서 확장자→전략 라우팅 맵을 역으로 구성해야 한다(DRY).
- **FR-003**: 한 확장자를 단일 전략만 주장하면 방언 판별 없이 즉시 확정해야 한다.
- **FR-004**: 둘 이상 전략이 같은 확장자(`.sql`)를 주장하면 파일 내용의 방언 특유 마커를 가중치 점수로 합산해 프로젝트 단위로 한 방언을 결정해야 한다.
- **FR-005**: Oracle 마커(VARCHAR2·DBMS_*·NVL·SYSDATE·FROM DUAL·PACKAGE BODY·EXCEPTION WHEN·MERGE INTO·ROWNUM·CONNECT BY·IS/AS BEGIN)와 PostgreSQL 마커($$·LANGUAGE plpgsql·SERIAL·RETURNS·RAISE NOTICE·::cast·now()·TEXT)를 각자 가중치로 점수화해야 한다.
- **FR-006**: 방언 점수가 동점이거나 모두 0이면 oracle 로 폴백해야 한다.
- **FR-007**: 지원하지 않는 확장자의 파일은 파싱 대상에서 스킵해야 한다.
- **FR-008**: 감지 결과는 파일별 전략, 감지된 target 집합, `.sql` 방언, 대표 언어(primaryTarget), 파이프라인 strategy(framework/dbms)를 구조화해 노출해야 한다.
- **FR-009**: 파이프라인 strategy 는 framework 언어가 하나라도 있으면 framework, SQL(oracle/postgresql)만이면 dbms, 비어 있으면 framework 로 도출해야 한다.
- **FR-010**: 대표 언어는 선택된 strategy 분류(framework vs dbms)에 속하는 타입 중 파일 수가 가장 많은 것으로 산출해야 한다.
- **FR-011**: PL/SQL 파싱은 대소문자 무관 렉싱을 위해 입력 스트림을 강제 대문자화(`CaseChangingCharStream(stream, upper=true)`)해야 하며, `getText()` 원문은 보존해야 한다.
- **FR-012**: 방언 판별용 파일 읽기는 UTF-8 우선, 실패 시 EUC-KR 로 폴백하고, 둘 다 실패하면 해당 파일을 빈 내용으로 건너뛰어야 한다(레거시 인코딩 대비).
- **FR-013**: C 전략은 단일 파일 파싱 전 `prepare()` 단계에서 소스 전체의 typedef/struct/enum 타입명과 `#define` 매크로 상수를 선수집해 파서에 등록해야 한다.

### Key Entities *(include if feature involves data)*

- **TargetParserStrategy**: 전략 인터페이스. `parseFileWithStream(file, outputPath, tracker)`, `getSupportedTargetType()`, `getTargetExtensions()`, 선택적 `prepare()`. Spring `@Component` 로 등록되어 List 주입 → 감지기가 자동 발견.
- **JavaParserStrategy**: type `java`, 확장자 `{.java}`, Java20Lexer/Parser.
- **CParserStrategy**: type `c`, 확장자 `{.c, .h}`, CLexer/CParser. `prepare()` 로 typedef·매크로 선수집(README 미기재 target).
- **PythonParserStrategy**: type `python`, 확장자 `{.py}`, PythonLexer/Parser (README 미기재 target).
- **PlSqlParserStrategy**: type `oracle`, 확장자 `{.sql, .pks, .pkb, .prc, .fnc}`, PlSqlLexer/Parser + CaseChangingCharStream(upper).
- **PostgreSqlParserStrategy**: type `postgresql`, 확장자 `{.sql}`, PostgreSQLLexer/Parser.
- **target 값**: 코드 실제 타입 = `java` / `oracle` / `postgresql` / `c` / `python`. README 매핑표는 `oracle`+`plsql`, `postgresql`+`postgres` 별칭을 적었으나 코드의 `getSupportedTargetType()` 은 단일 정식값(`oracle`, `postgresql`)만 반환한다.
- **LanguageDetector**: 확장자 1차 라우팅 맵(extToStrategies) + 타입→전략 맵(typeToStrategy)을 구성. `.sql` 모호성은 마커 점수(Marker = {정규식, 가중치})로 해소. DBMS_TYPES = {oracle, postgresql}.
- **DetectionResult**: `{fileStrategies, detectedTargets, sqlDialect, primaryTarget, strategy}`.
- **nameCase 옵션**: 메타데이터상 `original` / `uppercase` / `lowercase` 로 문서화. 실제 코드에서는 파싱 경로가 이 값을 읽지 않으며, 대소문자 처리는 PL/SQL 의 하드코딩된 강제 대문자화로만 존재(아래 Assumptions 참조).

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 5개 지원 언어(java/oracle/postgresql/c/python)의 단일 확장자 파일은 100% 올바른 전략으로 라우팅된다.
- **SC-002**: Oracle 우세 `.sql` 묶음과 PostgreSQL 우세 `.sql` 묶음이 각각 정확한 방언으로 100% 분류된다.
- **SC-003**: 마커 부재(순수 ANSI) `.sql` 은 항상 oracle 로 결정론적 폴백한다.
- **SC-004**: 미지원 확장자 파일은 0건 파싱 시도(전량 스킵)된다.
- **SC-005**: 호출자가 target 을 전혀 지정하지 않은 요청이 정상 파싱을 완수한다(자동 감지만으로 동작).

## Assumptions

- README 의 `🔧 지원 Target` 표는 Java/Oracle/PostgreSQL 3개만 적지만, **실제 코드에는 C·Python 전략이 추가로 존재**한다(README 누락). 본 스펙은 코드 기준 5개 언어를 정식으로 다룬다.
- README 표는 `oracle`/`plsql`, `postgresql`/`postgres` 를 동의 target 값으로 표기하나, 코드의 `getSupportedTargetType()` 은 `oracle`·`postgresql` 단일값만 반환한다. `plsql`/`postgres` 별칭은 코드에 매핑이 없다(문서-코드 불일치).
- 메타데이터 `{strategy, target, nameCase}` 는 README 기준이며, **현재 코드의 파싱 endpoint(`POST /antlr/parsing`)는 body 의 `target`·`strategy`·`nameCase` 를 무시**하고 LanguageDetector 의 자동 감지로 대체한다(body 에서는 선택적 `project_root` 만 사용). 업로드 endpoint 도 `targetFolder` 만 읽는다.
- `nameCase` 의 `original/uppercase/lowercase` 분기는 코드에서 구현되지 않았다. 실제 대소문자 처리는 PL/SQL 전략의 고정 `CaseChangingCharStream(stream, upper=true)` 한 곳뿐이며 메타데이터로 제어되지 않는다.
- 방언 점수와 마커 가중치는 휴리스틱이며, 마커가 없거나 동점인 경계에서는 의도적으로 oracle 레거시 기본을 택한다.
- 전략은 Spring `@Component` 로 자동 등록되어 List 주입되므로, 새 언어 추가는 전략 클래스 하나 추가로 끝난다(감지기/오케스트레이터 수정 불필요).
