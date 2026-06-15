# Tasks: Unified Intake & Content-Based Classification

**Feature**: 006-unified-intake-classification | **Branch**: `006-unified-intake-classification`
**Input**: plan.md · research.md · data-model.md · contracts/intake-contract.md · quickstart.md

테스트 포함(사용자 요청: 단위/통합). 경로는 `antlr-code-parser/` 기준.

---

## Phase 1: Setup

- [ ] T001 빌드 전제 확인 — JDK 17 하드링크(`java.nio.file.Files.createLink`)·statement 토크나이즈에 신규 의존 불필요함을 `pom.xml`에서 확인(추가 의존 없음).

## Phase 2: Foundational (모든 스토리의 선행 — 분류 엔진)

- [ ] T002 `data/{source, ddl, analysis}` 경로 resolve 단일화 점검 — `src/main/java/legacymodernizer/parser/service/FileStorageService.java`의 `sourceDir()/ddlDir()/analysisDir()`가 단일 진실인지 확인·정리.
- [ ] T003 [P] `IntakeClassifier` 신규 — statement 경계 분할 + 선행 키워드 식별(`CREATE [OR REPLACE]...{TABLE|VIEW|MATERIALIZED VIEW|INDEX|SEQUENCE}`→ddl / `{FUNCTION|PROCEDURE|PACKAGE[ BODY]|TRIGGER|TYPE}`·기타→source, 주석/문자열 무시) in `src/main/java/legacymodernizer/parser/service/IntakeClassifier.java` (research R1).
- [ ] T004 [P] `IntakeClassifier` 단위테스트 — 표/함수/프로시저/혼합/미상/비-sql 케이스 in `src/test/java/legacymodernizer/parser/service/IntakeClassifierTest.java`.

**Checkpoint**: 분류 엔진이 내용으로 종류를 판정·테스트 통과 → 스토리들 착수 가능.

---

## Phase 3: User Story 1 - 입구 하나, 표준 출력 폴더 하나 (P1)

**Goal**: 업로드·경로 두 모드 모두 결과가 `data/{source, ddl, analysis}`로 수렴.
**Independent Test**: 같은 파일을 업로드/경로로 각각 입구에 넣어 동일 `data/` 산출.

- [ ] T005 [US1] `/parsing` 경로모드를 "제자리 읽기"에서 "`data/`로 반입 후 처리"로 전환 — `src/main/java/legacymodernizer/parser/service/ParsingOrchestrator.java`의 `pathMode`/`sourceBase` 분기 제거, 항상 `data/source` 기준.
- [ ] T006 [US1] `FileStorageService.uploadFiles`가 분류를 `IntakeClassifier`로 위임(파일명 `ddl/` 접두는 역호환 힌트로만) — `src/main/java/legacymodernizer/parser/service/FileStorageService.java`.
- [ ] T007 [US1] 통합테스트 — 동일 입력의 업로드 vs 경로 모드가 동일 `data/` 산출 검증 in `src/test/java/legacymodernizer/parser/integration/UnifiedIntakeIT.java`.

**Checkpoint**: 두 모드 산출 동일 → analyzer가 `data/`만 봐도 됨.

---

## Phase 4: User Story 2 - 프로시저는 표 정의 파서에 닿지 않는다 (P1) ★크래시 해결

**Goal**: `data/ddl`엔 표 정의만, 프로시저/함수는 `data/source`로.
**Independent Test**: 함수 포함 폴더 → 함수가 source로 분류, 전체 run 완료.

- [ ] T008 [US2] 분류 결과 라우팅 — `ddl` 종류는 `data/ddl/<상대경로>`, `source`는 `data/source/<상대경로>`로 배치 in `src/main/java/legacymodernizer/parser/service/FileStorageService.java`.
- [ ] T009 [US2] 통합테스트 — `CREATE FUNCTION/PROCEDURE` .sql이 `data/ddl`에 없고 `data/source`에만 있으며 run이 완료됨 검증(quickstart 시나리오 1·2) in `src/test/java/legacymodernizer/parser/integration/UnifiedIntakeIT.java`.

**Checkpoint**: 프로시저 포함 프로젝트가 중단 없이 완료(현재 크래시 해소).

---

## Phase 5: User Story 3 - 로컬 파일을 싸게 반입 (P2)

**Goal**: 경로모드 반입을 하드링크 우선→복사 폴백으로.
**Independent Test**: 같은 볼륨 폴더 반입 시 전체 중복 없음, 불가 시 복사 폴백.

- [ ] T010 [P] [US3] `SourceMaterializer` 신규 — `scope` 파일을 `data/source/<상대경로>`로 `Files.createLink`→실패 시 `Files.copy` in `src/main/java/legacymodernizer/parser/service/SourceMaterializer.java` (research R2).
- [ ] T011 [US3] `/parsing` 경로모드가 `SourceMaterializer`로 반입하도록 연결 — `src/main/java/legacymodernizer/parser/service/ParsingOrchestrator.java`.
- [ ] T012 [P] [US3] `SourceMaterializer` 단위테스트(하드링크/복사 폴백) in `src/test/java/legacymodernizer/parser/service/SourceMaterializerTest.java`.

---

## Phase 6: User Story 4 - 한 파일이 깨져도 입구가 멈추지 않는다 (P2)

**Goal**: 파일 단위 실패 격리 + `skipped` 이벤트, 전체 중단 없음.
**Independent Test**: 깨진 파일 1개 섞여도 run 완료 + skipped 보고.

- [ ] T013 [US4] 파일 단위 try/catch로 분류/반입/파스 실패 격리 in `src/main/java/legacymodernizer/parser/service/ParsingOrchestrator.java` (research R4).
- [ ] T014 [US4] NDJSON `skipped {file, stage, reason}` 이벤트 emit + 입력 전체 무효 시는 기존대로 즉시 오류 — `src/main/java/legacymodernizer/parser/service/ParsingOrchestrator.java`.
- [ ] T015 [US4] 통합테스트 — 깨진 파일 1개 포함 시 `complete` 도달 + `skipped` 1건 + 정상 파일 처리 검증(quickstart 시나리오 4).

---

## Phase 7: User Story 5 - 섞인 파일의 표 정의도 잡아낸다 (P3)

**Goal**: 한 파일의 표 정의는 `data/ddl` 파생, 원본은 `data/source` 보존.
**Independent Test**: 표+프로시저 혼합 .sql → 표 정의가 ddl 세트에 존재, run 완료.

- [ ] T016 [US5] `IntakeClassifier`에 statement 단위 추출 추가 — 혼합 파일에서 표 정의 statement만 모아 파생 `.ddl.sql` 생성, 원본은 source 보존 in `src/main/java/legacymodernizer/parser/service/IntakeClassifier.java` (research R3).
- [ ] T017 [US5] 혼합 파일 처리를 라우팅에 연결(원본→source, 파생→ddl) in `src/main/java/legacymodernizer/parser/service/FileStorageService.java`.
- [ ] T018 [P] [US5] 단위/통합테스트 — 혼합 .sql 분류·파생 검증 in `IntakeClassifierTest.java` + `UnifiedIntakeIT.java`.

---

## Phase 8: Polish & Cross-Cutting

- [ ] T019 [P] 데드 청소 — 제거된 `pathMode`/제자리 읽기 잔재·옛 로그·미사용 분기 통째 폐기(원칙: 한 번 수정=영구) in `ParsingOrchestrator.java`·`FileUploadController.java`.
- [ ] T020 입구 요약 보고 — `complete` payload에 `{ddlCount, sourceCount, skippedCount, materializedVia}` 포함(data-model IntakeReport, FR-010) in `ParsingOrchestrator.java`.
- [ ] T021 [P] `/parsing` 본문에 `scope` 수용(contracts C2) — `src/main/java/legacymodernizer/parser/controller/FileUploadController.java`.
- [ ] T022 quickstart 시나리오 1~6 수동 검증 + AST 노드 스키마(`data/analysis`) 불변 확인(헌법 VI).

---

## Dependencies & 실행 순서

- **Setup(T001) → Foundational(T002-T004) → 스토리들**.
- **US1(T005-T007)·US2(T008-T009)** = P1, 그래프 복구 핵심. Foundational 후 착수, US1→US2 순(US2가 US1의 라우팅에 의존).
- **US3(T010-T012)·US4(T013-T015)** = P2, US1/US2 후 독립.
- **US5(T016-T018)** = P3, 분류 엔진(T003) 확장.
- **Polish(T019-T022)** = 마지막.

## Parallel 기회

- T003·T004(분류 엔진+테스트), T010·T012(materializer+테스트), T018, T019·T021 은 서로 다른 파일이라 `[P]` 병렬 가능.

## MVP 범위

**Foundational + US1 + US2** (T001-T009) = **그래프 복구 MVP** — 프로시저 포함 프로젝트가 중단 없이 분석되고 두 모드가 data/로 수렴. US3~5는 효율·견고·엣지 보강.
