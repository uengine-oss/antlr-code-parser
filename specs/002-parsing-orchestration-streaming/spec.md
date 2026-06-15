# Feature Specification: Parsing Orchestration & NDJSON Streaming

**Feature Branch**: `002-parsing-orchestration-streaming`

**Created**: 2026-06-15

**Status**: Backfilled (reverse-engineered)

**Input**: Existing feature — `POST /antlr/parsing` (application/json, 메타데이터만): 이전에 업로드된 소스를 파일별로 알맞은 언어 파서로 라우팅·파싱하고, 진행 상황을 NDJSON(type: message/complete/error)으로 실시간 스트리밍하며, AST JSON 은 응답이 아닌 `analysis/` 폴더에 source 구조 그대로 저장. 30분 타임아웃.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - 업로드된 소스를 파싱하고 진행 상황을 실시간으로 본다 (Priority: P1)

분석가가 `POST /antlr/fileUpload` 로 소스 묶음(여러 언어 혼재 가능)을 올린 뒤, 별도 파일 없이 `POST /antlr/parsing` 을 메타데이터만으로 호출한다. 서버는 업로드된 `data/source` 를 자동 감지·파싱하면서, 시작/파일별 시작·중간진행·완료/전체완료를 NDJSON 한 줄씩 흘려보낸다. AST JSON 자체는 응답으로 오지 않고 `analysis/` 에 저장된다.

**Why this priority**: robo-data-analyzer 파이프라인의 진입점. 이 흐름이 동작하지 않으면 후속 분석(catalog→graph)이 시작될 수 없는 핵심 MVP.

**Independent Test**: 소스 N개 업로드 후 `/antlr/parsing` 을 빈 본문으로 호출 → NDJSON 스트림에 `message` 들이 흐르고 마지막에 `complete` 가 오며, `analysis/` 에 source 와 동일한 폴더 구조의 `.json` 이 생성됨을 확인.

**Acceptance Scenarios**:

1. **Given** `data/source` 에 지원 확장자 파일이 존재, **When** `POST /antlr/parsing` 을 메타데이터만으로 호출, **Then** `application/x-ndjson` 스트림으로 `message` 들이 실시간 도착하고 종료 시 단일 `complete` 가 전송된다.
2. **Given** 파싱이 성공적으로 끝남, **When** 스트림 종료, **Then** AST JSON 은 응답 본문에 포함되지 않고 `analysis/{상대경로}.json` 로 저장된다(source 구조 미러).
3. **Given** Java·SQL 등 여러 언어가 섞인 소스, **When** 파싱, **Then** 각 파일이 확장자(및 `.sql` 방언 점수)로 판별되어 알맞은 파서로 라우팅된다.

---

### User Story 2 - 로컬 폴더를 직접 파싱한다 (경로 모드) (Priority: P2)

데스크톱(Electron) 사용자는 업로드 없이 분석할 로컬 폴더 경로를 본문 `project_root` 로 전달한다. 서버는 업로드된 `source/` 대신 그 폴더를 직접 감지·파싱한다.

**Why this priority**: 대용량/로컬 코드베이스에서 업로드 왕복을 없애 주는 보조 경로. 핵심 흐름(P1)이 있으면 없어도 제품은 동작하므로 P2.

**Independent Test**: 본문 `{"project_root":"<로컬경로>"}` 로 호출 → 첫 메시지에 "📁 로컬 경로 분석: …" 이 포함되고 해당 폴더 기준으로 파싱·저장됨을 확인.

**Acceptance Scenarios**:

1. **Given** 유효한 로컬 폴더 경로, **When** `project_root` 를 포함해 호출, **Then** 업로드된 source 대신 그 경로를 파싱하며 `analysis/` 를 매 실행마다 새로 비우고 채운다.

---

### User Story 3 - 일부 파일이 실패해도 나머지는 끝까지 파싱한다 (Priority: P3)

한 파일에서 파싱 오류가 나도 전체가 중단되지 않고, 해당 파일만 `error` 로 알린 뒤 나머지 파일을 계속 처리하고 성공/실패 집계를 마지막 메시지로 요약한다.

**Why this priority**: 레거시 소스의 부분 실패는 흔하므로 견고성이 중요하지만, 정상 경로(P1) 이후의 품질 요건이라 P3.

**Independent Test**: 의도적으로 깨진 파일 1개를 섞어 호출 → 그 파일에 대한 `error` 라인이 오고, 다른 파일은 정상 처리되며 마지막에 "성공 X개, 실패 Y개" 요약이 온다.

**Acceptance Scenarios**:

1. **Given** 파일 중 하나가 파싱 불가, **When** 파싱 진행, **Then** 해당 파일에 대해 `error` 이벤트가 전송되지만 스트림은 계속되고 종료 시 `complete` 가 온다.

---

### Edge Cases

- 소스 디렉토리(또는 `project_root`)가 존재하지 않으면? → `error` 이벤트("소스 디렉토리 없음: …") 후 스트림이 에러로 종료된다.
- 지원 확장자 파일이 하나도 없으면? → "⚠️ 지원하는 확장자의 파싱 대상 파일이 없습니다." 메시지 후 정상 `complete` 로 종료(에러 아님).
- `.sql` 파일이 방언 마커가 전혀 없으면(순수 ANSI)? → 점수 동점/0 시 oracle 기본으로 라우팅(`DetectionResult` 로 노출되어 호출자가 덮어쓸 수 있음).
- 파일이 UTF-8 로 못 읽히면? → EUC-KR 로 폴백해 라인 수를 센다(둘 다 실패 시 0).
- 30분을 초과하는 초대용량 작업? → emitter 타임아웃(30분)으로 스트림이 종료된다.
- 개별 NDJSON 라인 전송 중 클라이언트 연결 끊김(IOException)? → 해당 전송만 경고 로깅하고 파싱은 계속된다.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST `POST /antlr/parsing` 를 `application/json` 본문(메타데이터만, 파일 없음)으로 받아들이고, 응답을 `application/x-ndjson` 스트림으로 반환해야 한다.
- **FR-002**: System MUST 파싱 대상을 자동 결정해야 한다 — 본문에 `project_root` 가 있으면 그 로컬 폴더를, 없으면 업로드된 `data/source` 를 파싱한다(본문의 target/strategy/nameCase 등은 라우팅에 사용하지 않음).
- **FR-003**: System MUST 각 소스 파일을 확장자(및 여러 전략이 주장하는 `.sql` 은 내용 방언 점수)로 판별해 알맞은 언어 파서 전략으로 라우팅해야 한다.
- **FR-004**: System MUST 파싱 결과 AST JSON 을 응답에 포함하지 않고 `analysis/` 아래에 source 와 동일한 폴더 구조로(확장자만 `.json` 으로 치환) 저장해야 한다.
- **FR-005**: System MUST 매 파싱 실행 시작 시 `analysis/` 를 비우고 새로 생성해야 한다(경로 모드에도 적용).
- **FR-006**: System MUST 진행 상황을 NDJSON 이벤트로 실시간 전송해야 한다. 이벤트 타입은 `message`(진행), `complete`(전체 완료), `error`(에러)이며, 보조적으로 구조화된 `detected` 이벤트(감지 결과)도 전송한다.
- **FR-007**: System MUST 파일별 진행을 알려야 한다 — 파일 시작("📄 [i/N] … 파싱 시작"), 일정 라인 간격(기본 500라인)마다 중간 진행, 파일 완료("✅ [i/N] …").
- **FR-008**: System MUST 개별 파일 실패를 격리해야 한다 — 한 파일 오류는 `error` 이벤트로 알리되 나머지 파일 파싱을 계속하고, 종료 시 성공/실패/총 라인 수를 요약 메시지로 보낸다.
- **FR-009**: System MUST 소스/대상 디렉토리가 없으면 `error` 이벤트를 보내고 스트림을 에러로 종료해야 한다.
- **FR-010**: System MUST 스트림 emitter 에 30분 타임아웃을 적용해야 한다(대용량 대비).
- **FR-011**: System MUST 정상 종료 시 정확히 하나의 `complete` 이벤트를 전송하고 emitter 를 완료해야 한다.
- **FR-012**: System MUST 파싱을 요청 스레드와 분리된 워커에서 비동기 실행해 스트림을 점진적으로 흘려보내야 한다.

### Key Entities *(include if feature involves data)*

- **NDJSON 이벤트**: 한 줄(객체)+개행으로 구성. `type` 필드로 구분.
  - `message`: 사람이 읽는 진행 텍스트(`content` 포함). 시작/파일별 시작·중간·완료/요약.
  - `complete`: 전체 파싱 정상 종료 신호(`content` 없음).
  - `error`: 디렉토리 부재·개별 파일 실패 등(`content` = 에러 메시지).
  - `detected`(보조): 감지 결과 JSON(`target`, `strategy`, `sqlDialect`, `targets`) — 프론트가 sourceType/strategy 를 자동 설정하도록.
- **파일별 라우팅(DetectionResult)**: `source/`(또는 `project_root`)를 훑어 파일→파서 전략 매핑을 만든다. 확장자 1차 + `.sql` 방언 점수(Oracle vs PostgreSQL) 2차. 미지원 확장자는 스킵.
- **analysis/ 출력 레이아웃**: source/ 구조를 그대로 미러. `source/user/UserService.java` → `analysis/user/UserService.json`. 매 실행마다 통째로 재생성.
- **진행 추적기(ParseProgressTracker)**: 파일 단위로 직전 알림 + 간격(기본 500라인)을 넘으면 "📍 {파일} - {n}라인까지 파싱 중..." 을 보낸다(정확히 N라인마다가 아니라 기준 초과 시점).

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 업로드된 N개 파일을 파싱하면, 지원 확장자 파일 각각에 대해 `analysis/` 에 대응하는 `.json` 이 source 구조 그대로 생성된다(성공 파일 100%).
- **SC-002**: 응답 본문 어디에도 AST JSON 내용이 포함되지 않는다(스트림은 message/complete/error/detected 이벤트만).
- **SC-003**: 파일 중 일부가 실패해도 나머지 파일 파싱이 끝까지 진행되고 단일 `complete` 로 종료되며, 성공/실패 집계가 마지막 메시지에 정확히 표기된다.
- **SC-004**: 30분 이내에 끝나는 작업은 스트림이 정상 `complete` 로 닫히고, 첫 진행 메시지가 호출 직후 점진적으로 도착한다(블로킹 일괄 응답 아님).
- **SC-005**: Java·Oracle·PostgreSQL 등 혼재 소스에서 각 파일이 올바른 파서로 라우팅된다(잘못된 파서 라우팅 0건).

## Assumptions

- `POST /antlr/parsing` 호출 전에 `POST /antlr/fileUpload` 로 소스가 이미 저장되어 있다(경로 모드 제외). 두 단계는 분리되어 있다.
- 본문의 `target`/`strategy`/`nameCase` 는 README 의 과거 계약 잔재이며, 실제 라우팅은 본문이 아니라 자동 감지로 결정된다(본문에서 실제로 읽는 키는 `project_root` 뿐).
- 저장 루트는 `data/{source, ddl, analysis}` 구조를 따른다(FileStorageService 관리).
- 텍스트 인코딩은 UTF-8 우선, 실패 시 EUC-KR 폴백(레거시 한글 소스 대비).
- 지원 언어/확장자는 등록된 파서 전략(Java, Oracle PL/SQL, PostgreSQL, C, Python 등)의 합집합에서 동적으로 결정된다.
- 본 스펙은 파싱 오케스트레이션·스트리밍 계약에 한정한다. 개별 문법(ANTLR grammar)·AST 노드 직렬화 세부는 범위 밖.
