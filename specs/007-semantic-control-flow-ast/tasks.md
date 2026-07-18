# Tasks: C 언어 의미 제어 흐름 AST 노드 (C-first)

**Input**: `spec.md`, `plan.md`, `contracts/ast-node-vocabulary.md`
**Tests**: 포함 (회귀 0·소실 0을 명시 요구하므로 필수).

## Phase 1: Setup

- [ ] T001 `src/test/resources/fixtures/007-control-flow/sample.c` 작성 — 2단계 중첩 if/else, for·while·do
      루프 각 1개, switch/case(default 포함), 단일 라인 if(`if(x) f();`), 제어문 없는 함수 1개를 모두 포함.
      각 제어문 안에 식별 가능한 `FUNCTION_CALL`을 넣어 소실 검증이 가능하게 한다.

## Phase 2: Foundational — 회귀 기준선

- [ ] T002 변경 전 `mvn test` 통과 확인, 기존 004 fixture의 AST JSON 출력을 스냅샷으로 저장
      (`target/test-baseline/` 등). 이후 diff로 회귀를 기계적으로 확인.

**Checkpoint**: 베이스라인 확보.

---

## Phase 3: User Story 1 — 제어문이 AST에 중첩으로 남는다 (P1) 🎯 MVP

### Tests (먼저 작성, FAIL 확인)

- [ ] T003 [US1] `src/test/java/legacymodernizer/parser/antlr/c/CControlFlowAstTest.java` — `sample.c`
      파싱 후 assert: (a) `IF`/`ELSE`/`LOOP`/`SWITCH`/`CASE` 노드 존재·부모관계·start/end line,
      (b) 중첩 if가 부모 IF 아래 중첩, (c) else-if가 `ELSE` 안 `IF`로 표현, (d) switch default가 `CASE`,
      (e) `TRY`/`CATCH`가 생성되지 않음.

### Implementation

- [ ] T004 [US1] `CAstListener.java`에 `enterSelectionStatement`/`exitSelectionStatement` 추가 —
      `ctx.If()`/`ctx.Switch()` accessor로 if/switch 판별, if는 `IF`(+else 절 `ELSE`), switch는
      `SWITCH`+case 그룹별 `CASE` emit. 실제 `CParser.SelectionStatementContext` accessor를 코드로 확인.
- [ ] T005 [P] [US1] `CAstListener.java`에 `enterIterationStatement`/`exitIterationStatement` 추가 —
      `LOOP` emit.
- [ ] T006 [US1] else/case 경계 라인 정확성 조정 — C grammar의 `Else()` 토큰/case 라벨 위치를 실제
      context로 확인해 `ELSE`/`CASE`의 start line을 정확히 잡는다.
- [ ] T007 [US1] T003 통과 확인, 실패 시 중첩/라인 수정.

**Checkpoint**: US1 독립 검증 — C fixture 통과.

---

## Phase 4: User Story 2 — 기존 노드 소실 없음 (P1)

### Tests

- [ ] T008 [P] [US2] 기존 004 fixture 재파싱 → T002 baseline과 byte-diff 0 assert(회귀 스냅샷 테스트).
- [ ] T009 [P] [US2] `sample.c`에서 `FUNCTION_CALL` 총 개수를 재귀 카운트해, fixture 작성 시 명시한
      예상값과 일치 assert(부모가 제어문으로 바뀌어도 소실 0).

### Implementation

- [ ] T010 [US2] T008/T009 실패 시 진단 — 대개 `nodeStack.peek()` 참조 시점 오류 또는
      `exitStatementWithChildDedupe`의 자식 dedupe가 정상 자식을 지운 경우. 방문 순서를 grammar와 대조해 수정.

**Checkpoint**: US1 + US2 통과 — 소실 없는 온전한 트리.

---

## Phase 5: Polish & 문서

- [ ] T011 [P] `contracts/ast-node-vocabulary.md`, `specs/004-.../spec.md` 새 토큰 반영 재검토.
- [ ] T012 `mvn test` 전체 통과 확인(회귀 포함).
- [ ] T013 analyzer 작업(같은 작업자)에 `contracts/ast-node-vocabulary.md` 확정 공유 — spec 054의
      `_walk_framework`/`ontology_roles.yaml` 착수 신호.

## Dependencies & Execution Order

- Setup(T001) → Foundational(T002) → US1(T003-T007) → US2(T008-T010) → Polish(T011-T013).
- T005는 T004와 다른 grammar rule이라 병렬 가능. T008/T009는 서로 병렬.
- 커밋은 사용자 요청 시에만(이 저장소는 현재 clean).

## Java/Python (범위 밖 — 후속 참고)

C 폴백 정확도 개선 확인 후 별도 spec에서 리스너만 추가한다. 어휘/필드 계약은 이미
`contracts/ast-node-vocabulary.md`에 세 언어 전부 정의돼 있으므로(Java/Python은 🔒예약), 추가 시 리스너
핸들러만 구현하면 analyzer/frontend는 그대로 동작한다.
