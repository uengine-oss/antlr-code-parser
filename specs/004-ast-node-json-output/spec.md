# Feature Specification: AST Node Model & JSON Output Contract

**Feature Branch**: `004-ast-node-json-output`

**Created**: 2026-06-15

**Status**: Backfilled (reverse-engineered)

**Input**: User description: "기존 antlr-code-parser의 `Node` AST 모델과 그 JSON 직렬화(`toJson`)를 역공학으로 문서화. 언어별 AST 리스너/비지터가 노드를 만들고 `analysis/<path>.json` 으로 기록한다. 이 JSON 은 robo-data-analyzer step2(load_ast)가 소비하는 입력 계약이다."

> **Cross-service contract — consumed by robo-data-analyzer step2 (spec 004 there).**
> 이 문서가 정의하는 JSON 모양(필드명·노드 타입 어휘·출력 경로)은 두 서비스 사이의 경계 계약이다. 필드명/노드 타입을 바꾸면 다운스트림 analyzer의 `load_ast` 가 깨진다.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - analyzer step2가 AST JSON 을 안정적으로 로드 (Priority: P1)

robo-data-analyzer 의 step2(`load_ast`)는 antlr-code-parser 가 만든 `analysis/<path>.json` 파일을 읽어 각 노드의 `type`, `name`, `startLine`/`endLine`, `children` 를 순회하며 호출 그래프·심볼 카탈로그를 구성한다. analyzer 는 antlr-code-parser 의 내부 구현을 모르고 오직 이 JSON 모양만 안다.

**Why this priority**: 이것이 본 기능의 존재 이유다. JSON 모양이 곧 서비스 간 계약이며, 필드명 하나만 어긋나도 분석 파이프라인 전체가 멈춘다.

**Independent Test**: 알려진 Java/PL/SQL 소스 한 개를 파싱해 나온 `analysis/.../X.json` 을 analyzer step2 로직(또는 동등한 JSON 파서)에 먹여 루트 `FILE` 노드부터 자식 트리를 끝까지 순회하고, `type`/`name`/`startLine`/`children` 가 누락 없이 읽히는지 확인.

**Acceptance Scenarios**:

1. **Given** 파싱된 Java 파일의 JSON, **When** analyzer 가 루트 노드를 읽으면, **Then** `type=="FILE"` 이고 `fileName`/`filePath`/`packageName` 과 `children` 배열을 얻는다.
2. **Given** 어떤 노드든, **When** analyzer 가 노드를 읽으면, **Then** `type` 과 `startLine`/`endLine` 은 항상 존재하고, 값이 없는 선택 필드(`signature`, `comment`, `schema` 등)는 키 자체가 생략돼 있다.
3. **Given** 메서드 본문에 호출이 있는 소스, **When** analyzer 가 트리를 내려가면, **Then** `FUNCTION_CALL`/`NEW_INSTANCE` 노드를 `name`(호출 대상)으로 식별할 수 있다.

---

### User Story 2 - 모든 언어가 동일한 노드 스키마로 출력 (Priority: P2)

Java, C, Python, PL/SQL, PostgreSQL, PL/pgSQL 6개 언어 리스너/비지터가 서로 다른 ANTLR 베이스 클래스를 상속하지만, 모두 같은 `Node` 클래스를 채워 같은 필드명으로 직렬화한다. 언어별 노드 타입 어휘(vocabulary)만 다르고 필드 스키마는 동일하다.

**Why this priority**: 단일 스키마 덕분에 analyzer 는 언어별 분기 없이 한 가지 로더로 모든 JSON 을 처리한다. 스키마 통일이 깨지면 analyzer 가 언어별 특수처리를 떠안게 된다.

**Independent Test**: 각 언어 샘플 1개씩 파싱 → 모든 JSON 의 키 집합이 `@JsonPropertyOrder` 가 정의한 필드명 부분집합인지 확인(낯선 키 0개).

**Acceptance Scenarios**:

1. **Given** 임의 언어의 JSON, **When** 키를 수집하면, **Then** 모든 키가 정의된 16개 필드명 집합 안에 든다.
2. **Given** Java 와 PL/SQL JSON, **When** 둘을 비교하면, **Then** 필드명은 동일하고 오직 `type` 값 어휘만 다르다.

---

### User Story 3 - 출력이 source/ 구조를 미러링 (Priority: P3)

파싱 결과 JSON 은 `analysis/` 아래에 `source/` 의 디렉토리 구조를 그대로 미러로 저장되며, 확장자만 `.json` 으로 치환된다(`src/a/B.java` → `analysis/a/B.json`). analyzer 는 이 경로 규칙으로 원본 소스와 분석 JSON 을 짝지을 수 있다.

**Why this priority**: 경로 미러링이 원본↔분석 매핑의 암묵 계약이다. 본 노드 스키마와는 독립적인 보조 계약이라 P3.

**Independent Test**: 중첩 디렉토리 소스 트리를 파싱 → `analysis/` 트리가 `source/` 트리와 1:1(확장자 제외) 대응하는지 확인.

**Acceptance Scenarios**:

1. **Given** `source/pkg/Foo.java`, **When** 파싱하면, **Then** `analysis/pkg/Foo.json` 이 생성된다.

---

### Edge Cases

- null 값 필드는 어떻게 되나? → `@JsonInclude(NON_NULL)` 로 직렬화에서 키 자체가 빠진다. analyzer 는 키 부재를 "값 없음"으로 다뤄야 한다(빈 문자열/null 가정 금지).
- 자식이 없는 노드의 `children` 은? → 빈 배열 `[]` 로 항상 존재(컬렉션은 NON_NULL 대상이지만 빈 ArrayList 로 초기화돼 직렬화됨).
- `parent` 참조는? → `@JsonIgnore` 로 직렬화 제외(순환 방지). 트리는 오직 하향 `children` 으로만 표현.
- 확장자 없는 파일은? → `lastIndexOf('.')` 가 0 이하면 원본명에 `.json` 을 덧붙인다.
- 파싱 실패/빈 파일은? → 최소한 루트 `FILE` 노드는 생성되어 빈 `children` 으로 기록된다(다운스트림이 항상 루트를 기대할 수 있게).
- `final` Java 필드는? → `FIELD` 가 아니라 `CONSTANT_FIELD` 로 type 이 승격된다(analyzer 어휘에 포함 필요).

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: 시스템은 모든 언어 파싱 결과를 단일 `Node` 모델로 표현하고, 그 직렬화 형태(필드명·순서)는 모든 언어에서 동일해야 한다(MUST).
- **FR-002**: 모든 노드는 `type`(string), `startLine`(int), `endLine`(int) 를 항상 가져야 한다(MUST). 이들은 다운스트림의 필수 키다.
- **FR-003**: 루트 노드의 `type` 은 `"FILE"` 이어야 하며 `fileName`, `filePath`, `packageName` 을 보유해야 한다(MUST). `packageName` 은 경로 디렉토리에서 유도되되 언어별 패키지 선언(Java `package`)으로 덮어쓸 수 있다.
- **FR-004**: null 인 선택 필드는 JSON 에서 생략되어야 한다(MUST, `@JsonInclude(NON_NULL)`). 빈 키를 출력하면 안 된다.
- **FR-005**: 트리 구조는 `children` 배열로만 표현하고, 부모 역참조(`parent`)는 직렬화에서 제외해야 한다(MUST). 순환 참조 금지.
- **FR-006**: 직렬화 필드 순서는 `type, name, signature, modifiers, annotations, returnType, parameters, genericType, extendsType, implementsTypes, variableType, initValue, schema, moduleName, fileName, filePath, packageName, comment, startLine, endLine, children` 로 고정되어야 한다(`@JsonPropertyOrder`). (순서는 가독성용이며 의미 계약은 키 이름이다.)
- **FR-007**: 결과 JSON 은 `analysis/` 아래에 `source/` 구조를 미러링하며 확장자만 `.json` 으로 치환해 저장해야 한다(MUST).
- **FR-008**: 시스템은 언어별 노드-타입 어휘를 `type` 문자열로 emit 해야 한다(아래 Key Entities 의 어휘 목록). 다운스트림이 의존하므로 기존 토큰을 임의로 개명하면 안 된다(MUST).
- **FR-009**: 호출/생성 의미는 `FUNCTION_CALL`(호출 대상명=`name`) 과 `NEW_INSTANCE`(생성 타입명=`name`) 노드로 구분해 emit 해야 한다(MUST). Java `new` 는 두 노드를 형제로 동시 emit 한다(구현체 추적 + 생성자 호출 추적).
- **FR-010**: 주석은 `comment` 필드에 `"라인번호: 내용"` 포맷으로 부착되어야 한다(있을 때만; standalone/leading 정책은 ParserUtils 참조).

### Key Entities *(include if feature involves data)*

- **Node (AST 노드 / JSON 객체)**: 모든 언어 공통의 단일 직렬화 단위. `legacymodernizer.parser.model.Node`. `toJson()` 은 Jackson `ObjectMapper`(NON_NULL) 로 직렬화한다. **실제 JSON 필드명**(= `@JsonPropertyOrder` 순서, 이것이 계약):
  - 공통/필수: **`type`**, **`name`**, **`startLine`**, **`endLine`** — `type`/`startLine`/`endLine` 은 항상 존재, `name` 은 익명 노드에서 생략 가능.
  - 선언부: **`signature`**, **`modifiers`**, **`annotations`**, **`returnType`**, **`parameters`**, **`genericType`** — 함수/메서드/클래스 선언 메타.
  - 상속/구현(주로 Java): **`extendsType`**, **`implementsTypes`**.
  - 변수/필드: **`variableType`**, **`initValue`**(초기화 표현식 텍스트).
  - 스키마(주로 PL/SQL·PostgreSQL): **`schema`**.
  - 소속 모듈: **`moduleName`**(클래스/구조체명이 자식들에 재귀 전파됨).
  - 파일(루트 `FILE` 노드 전용): **`fileName`**, **`filePath`**, **`packageName`**.
  - 주석: **`comment`**(`"라인번호: 내용"` 포맷).
  - 트리: **`children`**(자식 Node 배열, 항상 존재·빈 배열 가능).
  - 직렬화 제외: `parent`(`@JsonIgnore`).
- **노드 타입 어휘 (`type` 값)** — 언어별 리스너/비지터가 emit 하는 실제 문자열:
  - 공통: `FILE`(루트), `FUNCTION_CALL`, `NEW_INSTANCE`.
  - Java(`JavaAstListener`): `IMPORT`, `CLASS`, `INTERFACE`, `METHOD`, `FIELD`, `CONSTANT_FIELD`(final 필드), `FUNCTION_CALL`, `NEW_INSTANCE`, `IF`, `ELSE`, `LOOP`, `SWITCH`, `CASE`, `TRY`, `CATCH`(spec 007 추가, 순수 additive).
  - C(`CAstListener`): `INCLUDE`, `DEFINE`, `ENUM`, `ENUM_CONSTANT`, `FUNCTION`, `MEMBER`, `FUNCTION_CALL`, `IF`, `ELSE`, `LOOP`, `SWITCH`, `CASE`(spec 007 추가; C는 `TRY`/`CATCH` 없음).
  - Python(`PythonAstListener`): `IMPORT`, `CLASS`, `FUNCTION_CALL`, `NEW_INSTANCE`, `IF`, `ELSE`, `LOOP`, `TRY`, `CATCH`(등, spec 007 추가; Python은 `SWITCH`/`CASE` 없음 — 언어에 해당 구문 없음).
  - PL/SQL(`PlSqlAstListener`): `PARAMETER`, `PROCEDURE`, `FUNCTION`, `TRIGGER`, `TRIGGER_BLOCK`, `DECLARE`, `VARIABLE`, `ASSIGNMENT`, `RETURN`, `SELECT`, `INSERT`, `UPDATE`, `DELETE`, `MERGE`, `IF`, `ELSIF`, `ELSE`, `LOOP`, `EXCEPTION`, `TRY`, `CALL`, `CURSOR_VARIABLE`, `OPEN_CURSOR`, `FETCH`, `CLOSE_CURSOR`, `EXIT`, `CTE`, `JOIN`, `EXECUTE_IMMEDIATE`, `COMMIT` 등.
  - PostgreSQL(`PostgreSqlAstListener`): `PARAMETER`, `PROCEDURE`, `DO`, DDL/DML 다수 (`CREATE_TABLE`, `ALTER_TABLE`, `CREATE_INDEX`, `CREATE_TRIGGER`, `CREATE_VIEW`, `CREATE_SCHEMA`, `CREATE_SEQUENCE`, `SELECT`, `INSERT`, `UPDATE`, `DELETE`, `MERGE`/`MERGE_INSERT`/`MERGE_UPDATE`/`MERGE_DELETE`, `GRANT`, `REVOKE`, `SET`, `RESET`, `DROP`, `TRUNCATE`, `COPY`, `COMMENT` 등 다수).
  - PL/pgSQL(`PlpgsqlAstVisitor`): `BEGIN`, `DECLARE`, `VARIABLE`, `ELSIF`, `ELSE`, `WHEN` 등.
- **출력 경로 (analysis/ 미러)**: `analysis/<source 상대경로의 확장자만 .json>` — `source/` 트리를 1:1 미러링. analyzer 가 원본↔분석을 짝짓는 보조 계약. 작성 주체는 언어별 `*ParserStrategy` (`Files.writeString(outputPath, listener.getRoot().toJson(), UTF_8)`), 경로 산출은 `ParsingOrchestrator.parseSingleFile`.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: robo-data-analyzer step2(`load_ast`)가 antlr-code-parser 가 만든 JSON 을 코드 변경 없이 100% 로드한다(필드명 불일치로 인한 파싱 실패 0건).
- **SC-002**: 6개 언어 모든 출력 JSON 의 키 집합이 정의된 16개 필드명(+`children`)의 부분집합이다(미정의 키 0개).
- **SC-003**: 모든 노드에서 `type`, `startLine`, `endLine` 누락 0건이며, 루트는 항상 `type=="FILE"`.
- **SC-004**: `source/` 의 모든 파일이 `analysis/` 에 동일 상대경로(.json)로 1:1 대응한다.

## Assumptions

- 다운스트림 계약의 진실 원천(source of truth)은 `Node.java` 의 `@JsonPropertyOrder`/`@JsonInclude` 와 언어별 리스너의 `type` 문자열 리터럴이다(본 문서는 이를 역공학한 것).
- analyzer 는 키 부재를 "값 없음"으로 해석하며, 생략된 선택 필드에 대해 빈 문자열/null 을 가정하지 않는다.
- 노드-타입 어휘는 추가될 수 있으나(신규 statement), 기존 토큰의 개명/제거는 호환성 파괴로 간주된다.
- `comment` 의 `"라인번호: 내용"` 포맷과 `children` 의 항상-존재(빈 배열 포함)는 analyzer 가 의존하는 안정 동작이다.
- Jackson 라이브러리가 직렬화 엔진이며, 직렬화 의미(필드 순서/포함 정책)는 어노테이션으로 고정돼 라이브러리 버전 간 동일하다고 가정한다.
