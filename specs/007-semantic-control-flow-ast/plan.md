# Implementation Plan: C 언어 의미 제어 흐름 AST 노드 (C-first)

**Branch**: `007-semantic-control-flow-ast` | **Date**: 2026-07-15 | **Spec**: [spec.md](./spec.md)

## Summary

C 리스너(`CAstListener.java`)에 `IF`/`ELSE`/`LOOP`/`SWITCH`/`CASE` 노드를 PL/SQL 리스너와 동일한
`ListenerHelper.enterStatement()` / `exitStatementWithChildDedupe()` 패턴으로 추가한다. 문법 재생성 없이
기존 커밋된 `CParser.java`의 `SelectionStatementContext`/`IterationStatementContext`만 사용하는 리스너 전용
변경이다. 기존 `FUNCTION_CALL` emit은 건드리지 않고, 그것이 새 제어문 노드의 자식으로 자연 중첩되는지만
확인한다. **Java/Python은 이번 범위 밖.**

## Technical Context

**Language/Version**: Java 17+, Spring Boot, ANTLR4 런타임(커밋된 생성 파서 사용)
**Primary Dependencies**: `legacymodernizer.parser.antlr.c.*`, `ListenerHelper`, `ParserUtils`
**Storage**: N/A (AST를 `data/analysis/*.json`으로 출력)
**Testing**: Maven `mvn test` (JUnit) + C fixture 신규
**Project Type**: 기존 서비스 확장(단일 프로젝트, 포트/엔드포인트 변경 없음)
**Performance**: 파싱 속도 저하 없음(리스너 콜백 O(1) 노드 생성/스택 push-pop)
**Constraints**: spec 004 기존 필드/토큰 하위호환(FR-008), 순수 additive
**Scale/Scope**: C 리스너 1파일 + fixture 테스트 1세트 + 계약/004 문서 갱신

## Constitution Check

- **원칙 VI (AST JSON은 안정적 다운스트림 계약)**: 새 노드 타입 추가는 analyzer에 영향 →
  `contracts/ast-node-vocabulary.md`(세 저장소 공유) + spec 054/055를 함께 관리. **PASS**.
- **FR-008 (기존 토큰 삭제/개명 금지)**: 순수 추가. **PASS**.
- 스트리밍/CORS 등 타 원칙 무관. **N/A**.

## Project Structure

```text
specs/007-semantic-control-flow-ast/
├── spec.md
├── plan.md                          # 이 문서
├── contracts/ast-node-vocabulary.md # 세 저장소 공유 계약 (어휘 + 노드 분류 + 필드 계약)
└── tasks.md

src/main/java/legacymodernizer/parser/antlr/
├── ListenerHelper.java              # 변경 없음(기존 메서드 재사용)
└── c/CAstListener.java              # + selectionStatement / iterationStatement 핸들러

src/test/java/legacymodernizer/parser/antlr/c/
└── CControlFlowAstTest.java         # 신규

src/test/resources/fixtures/007-control-flow/
└── sample.c                         # 신규 fixture

specs/004-ast-node-json-output/spec.md  # 새 토큰 반영(이미 갱신됨 — 재검토만)
```

**Structure Decision**: `CAstListener.java`에 핸들러 메서드만 추가하는 최소 변경. `Node.java`/
`ListenerHelper.java`는 필드·시그니처 변경 없이 재사용.

## C 언어 매핑 (Phase 1 설계 결론)

| grammar rule / context | 판별 | emit 타입 | 비고 |
|---|---|---|---|
| `selectionStatement` (if 형태) | `ctx.If() != null` | `IF` (+else 절 있으면 `ELSE`) | else-if는 `ELSE` 안 중첩 `IF`로 자연 표현 |
| `selectionStatement` (switch 형태) | `ctx.Switch() != null` | `SWITCH` → 각 case 그룹 `CASE` | default도 `CASE` |
| `iterationStatement` (for/while/do) | — | `LOOP` | 종류 구분 없음 |
| — | — | (TRY/CATCH 생성 안 함) | C에 구문 없음 |

핸들러는 `h.enterStatement("TYPE", ctx.getStart().getLine())` /
`h.exitStatementWithChildDedupe("TYPE", ctx.getStop().getLine(), ctx)` 패턴 그대로 재사용
(`PlSqlAstListener.java:358-453`과 동일). `else`/`case` 경계 표현은 C grammar에서 `selectionStatement`의
`Else()` 토큰 위치·`labeledStatement`/`blockItem` 구조를 확인해 정확한 라인으로 emit한다(구현 시 실제
context accessor를 코드로 확인).

## owner routine 결정

파서는 "소유 함수" 필드를 만들지 않는다. AST JSON은 순수 구조 트리이고, analyzer가 트리를 하강하며
가장 가까운 FUNCTION 조상을 owner routine으로 복원한다(`contracts/ast-node-vocabulary.md` §4). 파서가
검증할 것은 **중첩이 정확한가** 하나뿐이다.

## 위험과 완화

- **`else`/`case` 경계의 grammar 표현 불확실**: C grammar에서 else 절과 case 라벨이 별도 rule인지
  `selectionStatement`/`labeledStatement` 안에 있는지 구현 시 실제 `CParser.java` context accessor로 확인.
  잘못 잡으면 `ELSE`/`CASE` 라인이 틀어짐 → fixture 테스트가 잡는다.
- **`exitStatementWithChildDedupe`의 자식 dedupe 부작용**: 부모와 동일 라인 범위 자식을 제거하는 로직이
  단일 라인 if(`if(x) y();`)에서 정상 자식까지 지울 위험 → fixture에 단일 라인 if를 포함해 검증.

## Complexity Tracking

*Constitution Check 위반 없음 — 비워둔다.*
