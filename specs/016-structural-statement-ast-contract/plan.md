# Implementation Plan: 구조 구문 AST 생산자 계약

## 1. 책임 경계

```text
source bytes -> language grammar/context -> language listener -> stable AST JSON
            -> analyzer reader/ontology role -> routine/branch evidence -> Rule/graph
```

언어 문법은 listener가 소유한다. analyzer는 canonical AST role과 좌표/표현만 소비한다.

## 2. 구현 순서

1. parser와 analyzer의 관련 dirty diff, Node schema, 모든 AST 소비자를 전수 조사한다.
2. source occurrence 대비 현재 AST 누락을 언어별 red contract test로 고정한다.
3. 공통 Node 표현 필드를 확정하고 parser 004/vocabulary 계약을 갱신한다.
4. C, Java, Python, PL/SQL/PostgreSQL listener를 grammar context 기반으로 구현한다.
5. analyzer ontology/reader/context/branch evidence를 AST-only 소비로 전환한다.
6. source↔AST 완전성 0-delta 뒤 analyzer legacy scanner와 return keyword 계약을 삭제한다.
7. parser/analyzer 전체 회귀, 실제 corpus 재파싱, cold 성능·정확도 검증을 수행한다.

## 3. 영향 파일 후보

- parser: `Node.java`, `ListenerHelper.java`, 언어별 `*AstListener.java`, AST vocabulary/spec/tests
- analyzer: `ontology_roles.yaml`, step2 readers, `AnalysisContext`, `control_subtree`,
  `branch_inventory`, Rule evidence, 관련 tests/docs

후보 목록은 전수 caller/consumer 조사 전 확정 목록으로 간주하지 않는다.

## 4. Constitution Check

- Parser VI: AST JSON shape는 analyzer 계약과 함께 변경한다.
- Analyzer I: 구조 사실은 LLM 0이되 업무 의미를 추론하지 않는다.
- Analyzer III/VIII: AST statement vocabulary와 role의 단일 진실을 둔다.
- No Silent Failure: AST contract 누락을 source fallback으로 숨기지 않는다.

## 5. WIP 보호

현재 parser의 C switch/case ownership 변경은 사용자 WIP다. reset/checkout/clean하지 않고,
listener 수정 전 diff를 다시 읽어 충돌 없이 통합한다. `.specify/feature.json`의 기존 활성 기능도
이번 문서 생성만으로 변경하지 않는다.
