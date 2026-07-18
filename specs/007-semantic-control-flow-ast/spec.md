# Feature Specification: C 언어 의미 제어 흐름 AST 노드 (C-first 파일럿)

**Feature Branch**: `007-semantic-control-flow-ast`

**Created**: 2026-07-15

**Status**: Draft

**Input**: robo-data-analyzer의 rules/examples 폴백 분할이 자를 기준 구조를 필요로 한다. 그 구조가 함수
본문의 제어문(if/else, loop, switch/case) AST다. 상위 계약: `contracts/ast-node-vocabulary.md`.
근거 문서: `docs/핸드오프/2026-07-15-framework-의미AST-규칙예시-폴백-스트림-핸드오프.md`.

## 동기 (왜 이 뼈대가 필요한가)

이 기능 자체는 "그래프를 함수 밑으로 확장"하는 게 목적이 아니다. **프레임워크 rules/examples 3분할
분석의 정확도가 떨어질 때**(같은 규칙 수십 회 반복 등) 함수를 결정론적으로 쪼개 조각별로 분석하고 병합해
정확도를 올리려는 것이고, 그러려면 **어디서 자를지 기준이 되는 구조**가 있어야 한다. 그 구조가 제어문
AST다. 따라서 이 노드들은 **폴백 분할 전용 뼈대**이며 LLM 분석에는 참여하지 않는다(그 규칙은 소비자인
robo-data-analyzer spec 054가 강제).

## 배경과 현재 사실 (조사 완료)

- 현재 C 리스너(`CAstListener.java`)는 함수 자식으로 `FUNCTION_CALL`만 emit한다(`postfixExpression` 경유).
  `if/else/switch/loop`는 전혀 노드화되지 않아, 함수 본문의 분기 구조가 AST JSON에서 사라진다.
- PL/SQL 리스너는 이미 `IF/ELSIF/ELSE/LOOP/EXCEPTION/TRY`를 `ListenerHelper.enterStatement(type, line)` /
  `exitStatementWithChildDedupe(type, line, ctx)` 패턴으로 노드화한다(`PlSqlAstListener.java:358-453`). C는
  이 정책에서 빠져 있을 뿐, **새 메커니즘은 필요 없다** — 같은 헬퍼를 재사용한다.
- C의 if/switch는 grammar 규칙 `selectionStatement` 하나에서, for/while/do는 `iterationStatement` 하나에서
  나온다. 해당 생성 context 클래스(`SelectionStatementContext`, `IterationStatementContext`)는 이미 커밋된
  `src/main/java/legacymodernizer/parser/antlr/c/CParser.java`에 존재한다.
- ANTLR `.g4` 재생성이 필요 없다 — `antlr4-maven-plugin`의 `sourceDirectory`가 실존하지 않는 빈
  디렉터리를 가리켜 사실상 no-op이고, `antlr-grammars/*.g4`는 참고 사본이다. 필요한 생성 코드가 이미
  리포지토리에 커밋돼 있으므로 **리스너 전용 변경**이다.
- `Node.java`는 `type`이 자유 문자열이고 children[]만 있는 순수 구조 트리다. "owner routine" 개념은 파서에
  없고, 이번에도 만들지 않는다(트리 중첩만 정확하면 analyzer가 스스로 복원 — `contracts/`).
- **Java/Python은 이번 범위 밖**이다. C로 폴백 정확도 개선을 확인한 뒤 후속 spec에서 리스너를 추가한다.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - C 함수 안 제어문이 AST에 중첩으로 남는다 (Priority: P1)

C 함수에 if/else, for/while/do, switch/case가 있으면 파서가 만든 AST JSON에 해당 제어문이 FUNCTION 자식
노드로, 실제 소스 중첩 그대로 나타난다. 지금은 통째로 사라진다.

**Why this priority**: analyzer의 폴백 분할이 이 구조에 의존한다. 파서가 뼈대를 안 만들면 분할이 불가능하다.

**Independent Test**: 중첩 if/else, loop, switch/case가 있는 C fixture를 파싱해 생성 AST JSON에서 해당
노드가 정확한 부모 아래, 정확한 start/end line으로 존재하는지 확인.

**Acceptance Scenarios**:

1. **Given** `if (조건) { A(); } else { B(); }`, **When** 파싱, **Then** 함수 아래 `IF` 노드가 있고 그
   자식으로 `A` 호출의 `FUNCTION_CALL`이, `IF`의 자식(또는 형제)로 `ELSE` 노드가 있고 그 자식으로 `B`
   호출의 `FUNCTION_CALL`이 있다.
2. **Given** 중첩 if (`if A { if B { ... } }`), **When** 파싱, **Then** 바깥 `IF` 아래 안쪽 `IF`가 중첩된다.
3. **Given** for/while/do 루프, **When** 파싱, **Then** 각각 `LOOP` 노드가 생성되고 본문의 호출/중첩
   제어문이 `LOOP` 자식으로 들어간다.
4. **Given** switch/case, **When** 파싱, **Then** `SWITCH` 노드 아래 각 case(및 default) 그룹이 `CASE`
   자식 노드로 들어간다.
5. **Given** `else if` 체인, **When** 파싱, **Then** 별도 `ELSEIF` 타입 없이 `ELSE` 안에 중첩된 `IF`로
   표현된다(문법 구조 그대로, 인위적 평탄화 없음).

---

### User Story 2 - 기존 FUNCTION_CALL이 제어문 아래로 내려가도 소실되지 않는다 (Priority: P1)

제어문 노드 도입으로 기존 FUNCTION 직속이던 `FUNCTION_CALL`이 이제 `IF`/`LOOP` 아래로 한 단계 더 중첩된다.
이 위치 이동 자체는 의도된 동작이지만, 노드가 누락되는 회귀는 없어야 한다.

**Why this priority**: 파서 출력이 회귀 없이 완전해야 analyzer의 owner routine 복원·CALLS 연결이 성립한다.

**Independent Test**: 제어문 없는 기존 fixture는 노드 수·구조가 변경 전후 동일(diff 0), 제어문 있는
fixture는 `FUNCTION_CALL` 총 개수가 도입 전 예상값과 동일.

**Acceptance Scenarios**:

1. **Given** 제어문이 없는 기존 C fixture, **When** 파싱, **Then** 노드 수·타입·중첩이 이전과 100% 동일.
2. **Given** if 안의 함수 호출, **When** 파싱, **Then** 해당 `FUNCTION_CALL`은 여전히 AST에 존재하며 부모가
   FUNCTION에서 `IF`로 바뀐 것만 차이다(소실 0).

---

### Edge Cases

- **do-while/while/for 구분 없음**: 세 형태 모두 `LOOP` 한 타입으로 emit(PL/SQL 선례). 종류 구분은 후속.
- **switch의 default 그룹**: 값 리터럴 유무와 무관하게 `CASE`로 emit.
- **if/switch가 같은 `selectionStatement`에서 나옴**: `ctx.If()`/`ctx.Switch()` 토큰 accessor로 구분해 각각
  `IF`/`SWITCH`로 emit. C에 try/catch 없으므로 `TRY`/`CATCH`는 생성하지 않는다.
- **빈 본문(`if (x);`)**: `IF` 노드는 만들되 자식이 없을 수 있다(빈 children[], 오류 아님).
- **`goto`/`break`/`continue`/`return`**: 이번 범위에서 별도 노드를 만들지 않는다(제어 흐름 분기의 절단선이
  아니라 단일 문장 — 폴백 분할 기준이 아님).

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: C 리스너에서 `selectionStatement` 진입/이탈 시, if 형태는 `IF`(+else 절이 있으면 `ELSE`)를,
  switch 형태는 `SWITCH`+각 case/default 그룹의 `CASE`를 `ListenerHelper.enterStatement` /
  `exitStatementWithChildDedupe` 패턴으로 emit해야 한다.
- **FR-002**: C 리스너에서 `iterationStatement`(for/while/do)에 대해 `LOOP` 노드를 emit해야 한다.
- **FR-003**: C에 대해 `TRY`/`CATCH` 노드를 생성하지 않아야 한다(언어에 구문 없음).
- **FR-004**: 새 제어문 노드의 `startLine`/`endLine`은 해당 grammar context의 실제 시작/끝 토큰 라인과
  일치해야 한다(대략치 금지).
- **FR-005**: 중첩 구조(if 안 if, loop 안 if 등)가 리스너 스택에 정확히 반영되어, AST children[] 중첩이
  실제 소스 중첩과 일치해야 한다.
- **FR-006**: 기존 `FUNCTION_CALL`/`INCLUDE`/`DEFINE`/`ENUM` 등 emit 로직은 변경하지 않는다(그 노드들이
  이제 제어문 아래로 중첩될 뿐, emit 트리거·필드는 그대로).
- **FR-007**: 제어문이 전혀 없는 기존 fixture/함수는 AST 노드 수·타입·구조가 이번 변경 이전과 완전히
  동일해야 한다(회귀 0).
- **FR-008**: 새 타입(`IF`,`ELSE`,`LOOP`,`SWITCH`,`CASE`)이 `contracts/ast-node-vocabulary.md`와
  `specs/004-ast-node-json-output/spec.md`의 어휘 목록에 순수 추가로 문서화되어야 한다.
- **FR-009**: C fixture 회귀 테스트(중첩 if/else, loop, switch/case를 모두 포함)가 `src/test/`에 추가되어야
  한다.

### Key Entities

- **IF / ELSE / LOOP / SWITCH / CASE**: `contracts/ast-node-vocabulary.md` §1 정의. C는 이 5개만 구현
  (TRY/CATCH 없음). 모두 `Node.java`의 기존 JSON 스키마를 그대로 쓰고 신규 필드는 없다.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: C fixture 테스트가 통과하며, 중첩 if/else·loop·switch/case의 AST 노드 타입·부모관계·
  start/end line이 기대값과 100% 일치한다.
- **SC-002**: 기존 004 fixture 및 `src/test/` 전체 테스트가 회귀 없이 통과한다(제어문 없는 소스의 노드 수
  diff 0).
- **SC-003**: 제어문 도입 후에도 같은 함수 안 `FUNCTION_CALL` 총 개수가 도입 전과 동일(부모만 바뀌고 소실
  0) — C fixture에서 확인.
- **SC-004**: `contracts/ast-node-vocabulary.md`와 `specs/004-.../spec.md`가 새 토큰을 반영한다.

## Assumptions

- ANTLR `.g4` 재생성 불필요(필요한 생성 context 클래스가 이미 커밋됨).
- owner routine은 파서가 만들지 않는다(analyzer가 트리 하강으로 복원 — `contracts/` §4).
- for/while/do, case/default의 하위 구분은 이번 범위 밖(별도 후속).
- Java/Python 리스너는 이번 범위 밖. C로 폴백 정확도 개선 확인 후 후속 spec에서 추가.
