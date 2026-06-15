# Feature Specification: File Upload & Storage

**Feature Branch**: `001-file-upload-storage`

**Created**: 2026-06-15

**Status**: Backfilled (reverse-engineered)

**Input**: 기존 구현 코드(FileUploadController · FileStorageService · WebConfig)로부터 역설계하여 사후 작성한 명세.

## User Scenarios & Testing *(mandatory)*

<!-- 실제 동작에서 도출한 우선순위 사용자 시나리오 -->

### User Story 1 - 소스 프로젝트 전체 업로드 (Priority: P1)

사용자(robo-data-analyzer 또는 데스크톱 앱)가 레거시 프로젝트 폴더를 골라 `POST /antlr/fileUpload` 멀티파트 요청으로 한 번에 올린다. 각 파일은 폴더 구조가 담긴 상대경로를 파일명으로 가지며, 서버는 이를 그대로 보존해 저장한다. 별도의 언어 지정은 필요 없다.

**Why this priority**: 이 업로드가 이후 모든 파싱/분석의 입구다. 이것 없이는 어떤 분석도 시작할 수 없으므로 최우선이다.

**Independent Test**: 폴더 picker로 여러 소스 파일을 올리고, 응답 JSON의 `files`에 모든 대상 파일이 상대경로와 내용으로 담겨 돌아오는지, 그리고 디스크 `data/source/` 아래에 동일 폴더 구조로 저장됐는지 확인하면 검증된다.

**Acceptance Scenarios**:

1. **Given** `user/UserService.java`, `order/OrderController.java` 두 파일을 가진 멀티파트 요청, **When** `POST /antlr/fileUpload` 호출, **Then** 두 파일이 `data/source/user/...`, `data/source/order/...`로 저장되고 응답 `files`에 `{fileName, fileContent}`로 반환된다.
2. **Given** 지원 확장자가 아닌 파일(예: `README.md`)이 섞인 요청, **When** 업로드, **Then** 해당 파일은 `data/source/`에 저장은 되지만 응답에서 `files`가 아닌 `nontargetFiles`로 분류된다.
3. **Given** metadata 파트의 `targetFolder` 값이 주어지고 폴더 없는 단일 파일명, **When** 업로드, **Then** 그 파일만 `source/{targetFolder}/...`로 prefix가 적용된다.

---

### User Story 2 - DDL 파일 자동 분류 (Priority: P1)

사용자가 스키마 정의(DDL) 파일을 `ddl/` 경로 접두로 같은 요청에 함께 올린다. 서버는 경로만으로 이를 DDL로 자동 분류해 소스와 분리 저장한다.

**Why this priority**: 분석기는 소스와 DDL을 구분해 소비한다. 잘못 섞이면 다운스트림 분석이 깨지므로 P1.

**Independent Test**: `ddl/schema.sql`을 포함해 업로드한 뒤, 응답 `ddlFiles`에만 등장하고 `data/ddl/schema.sql`(접두 `ddl/` 제거)로 저장됐는지 확인.

**Acceptance Scenarios**:

1. **Given** `ddl/tables/user.sql` 파일, **When** 업로드, **Then** `data/ddl/tables/user.sql`로 저장되고 응답 `ddlFiles`에 `fileName="ddl/tables/user.sql"`로 반환된다.
2. **Given** `ddl/` 접두가 없는 `.sql` 파일, **When** 업로드, **Then** DDL이 아닌 소스로 취급된다(분류는 오로지 경로 접두 기준).

---

### User Story 3 - 재업로드 시 전체 교체 (Priority: P2)

사용자가 같은 프로젝트를 다시 업로드하면, 직전 업로드의 잔재(이전 소스·DDL·분석 결과)가 모두 사라지고 새 업로드만 남아야 한다.

**Why this priority**: stale 파일이 남으면 분석 결과가 오염된다. 깨끗한 재시작 보장이 데이터 정합성의 핵심이라 P2.

**Independent Test**: A 세트 업로드 후 더 적은 B 세트를 업로드하고, A에만 있던 파일과 이전 분석 결과가 `data/`에서 사라졌는지 확인.

**Acceptance Scenarios**:

1. **Given** 이전 업로드로 `data/source`, `data/ddl`, `data/analysis`에 파일이 존재, **When** 새 업로드, **Then** 세 디렉토리가 먼저 비워진 뒤 새 파일만 저장된다.

---

### Edge Cases

- 빈 요청(`files` 없음/0개) → 400, `{"error":"files 필수"}`.
- 빈 파일·파일명 없는 파트 → 조용히 건너뜀(skip).
- metadata JSON 파싱 실패 → 400, `{"error":"metadata JSON 파싱 실패"}`.
- 파일 크기 제한 초과 → 413(Payload Too Large), 최대 파일 100MB / 요청 500MB 안내 메시지.
- UTF-8로 못 읽는 파일 → EUC-KR → MS949 순으로 재시도, 모두 실패 시 내용 `"[binary file]"`로 반환(저장은 정상).
- 경로 구분자 `\`(윈도우) → `/`로 정규화 후 분류·저장.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST `POST /antlr/fileUpload`를 `multipart/form-data`로 제공하며, `files` 파트(파일 배열)와 선택적 `metadata` 파트(JSON 문자열)를 받는다.
- **FR-002**: System MUST 파일명에 담긴 상대경로의 폴더 구조를 보존하여 저장한다.
- **FR-003**: System MUST 상대경로가 `ddl/`로 시작하는 파일을 DDL로 분류해 `data/ddl/` 아래(접두 제거)에 저장하고, 그 외 모든 파일은 `data/source/`에 저장한다.
- **FR-004**: System MUST 새 업로드 시작 시 `data/source`, `data/ddl`, `data/analysis`를 모두 비운 뒤 저장한다(전체 교체 의미론).
- **FR-005**: System MUST 저장 파일을 응답 JSON에 `{fileName, fileContent}` 형태로 반환하되 세 리스트로 나눈다 — 지원 확장자 소스는 `files`, DDL은 `ddlFiles`, 지원 확장자가 아닌 소스는 `nontargetFiles`.
- **FR-006**: System MUST 소스/비소스 구분을 자동 감지된 지원 확장자 집합으로 판정한다(호출자가 언어를 지정하지 않음).
- **FR-007**: System MUST metadata의 `targetFolder`가 있고 파일명에 폴더가 없는 단일 파일일 때만 해당 prefix를 적용한다(DDL 및 이미 폴더가 있는 파일은 제외).
- **FR-008**: System MUST 파일 내용을 UTF-8로 읽고 실패 시 EUC-KR·MS949로 폴백하며, 모두 실패하면 `"[binary file]"`로 표기한다.
- **FR-009**: System MUST 잘못된 요청(파일 없음, metadata 파싱 실패)에 400, 크기 초과에 413을 반환한다.
- **FR-010**: System MUST 모든 origin의 CORS 요청을 허용한다(실제 CORS 통제는 API Gateway가 담당).

### Key Entities *(include if feature involves data)*

- **Uploaded File**: 하나의 업로드 결과 항목. 속성 `fileName`(상대경로, `/` 정규화), `fileContent`(텍스트 또는 `[binary file]`).
- **분류(Classification)**: 경로 접두로 결정 — `ddl/` 시작 → **DDL**, 그 외 → **Source**. Source 중 지원 확장자면 `files`, 아니면 `nontargetFiles`.
- **저장 레이아웃**: `data/source/`(소스 원본), `data/ddl/`(DDL 원본, 접두 제거), `data/analysis/`(파싱 결과 JSON, source 구조 미러). BASE_DIR은 `DOCKER_COMPOSE_CONTEXT` env 또는 `{작업디렉토리 상위}/data`로 결정.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 폴더 구조를 포함한 N개 파일 업로드 후, 디스크에 저장된 상대경로가 입력 상대경로와 100% 일치한다.
- **SC-002**: `ddl/` 접두 파일은 100% `ddlFiles`로, 그 외는 `files`/`nontargetFiles`로 분류되어 교차 오분류가 0건이다.
- **SC-003**: 재업로드 후 이전 업로드에만 존재하던 파일과 분석 산출물이 0건 잔존한다.
- **SC-004**: 잘못된 요청에 대해 명확한 한국어 오류 메시지와 올바른 HTTP 상태(400/413)가 반환된다.

## Assumptions

- 파일 크기 제한(파일 100MB / 요청 500MB)은 코드의 오류 메시지·README에 명시돼 있으나, 저장소에 `application.yml`/`.properties`가 없어 실제 Spring multipart 한도가 설정 파일로 강제되는지는 확인되지 않았다(외부 설정/기본값 의존 가정).
- 호출자는 언어/방언을 지정하지 않으며, 분류·파싱 라우팅은 서버 자동 감지에 위임된다.
- metadata는 선택이며 현재 코드가 실제로 읽는 키는 `targetFolder` 하나뿐이다(README가 언급하는 `strategy`/`target`/`nameCase` 및 `OpenAI-Api-Key` 헤더는 이 업로드 경로에서 사용되지 않음).
- 단일 사용자(분석기 1 클라이언트) 순차 업로드를 전제로 한 공유 `data/` 디렉토리 모델이다(동시 업로드 격리는 범위 밖).
- 텍스트 인코딩 폴백 대상은 한국어 레거시(EUC-KR/MS949)를 가정한다.
