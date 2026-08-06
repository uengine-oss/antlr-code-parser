# Feature Specification: 구조 구문 AST 생산자 계약

**Status**: Draft  
**Created**: 2026-08-05  
**Affected consumer**: `D:\work\robo\project\robo-data-analyzer`

## 1. 목적

ANTLR grammar가 이미 식별하는 RETURN, THROW/RAISE, BREAK/CONTINUE/GOTO/EXIT, CALL,
ASSIGNMENT, 제어 조건을 언어별 listener가 AST JSON으로 완전하게 제공한다. analyzer가
source 정규식·첫 토큰·괄호 수로 동일 구문을 다시 파싱하지 않도록 생산자 책임을 확정한다.

이 spec은 007의 “goto/break/continue/return은 범위 밖” 결정을 후속 범위에서 대체한다.
007의 역사와 기존 C switch/case dirty WIP는 보존한다.

## 2. 현재 확인 사실

- C 실제 AST corpus에서 `RETURN` node는 0개다.
- C/Java/Python grammar에는 return context가 있지만 custom listener emit이 없다.
- PL/SQL listener는 `RETURN`과 `ASSIGNMENT`를 emit한다.
- 언어별 parser 출력 계약이 불균일해 analyzer가 source를 재탐색하고 있다.

## 3. 요구사항

- FR-001: parser가 공식 지원하는 각 언어는 grammar가 구분하는 구조 statement를 AST node로 emit한다.
- FR-002: node는 canonical type, 정확한 start/end line, 실제 구조 parent를 보존한다.
- FR-003: downstream이 source를 다시 파싱하지 않도록 필요한 표현을 명시 필드로 보존한다.
  최소 후보는 `expression`(RETURN/THROW/RAISE/조건), `target`과 `operator`(ASSIGNMENT)다.
  최종 필드는 Node 전체 소비자 조사와 contract test 뒤 확정한다.
- FR-004: value-less return은 RETURN node로 남고 expression만 비어야 한다.
- FR-005: multiline/inline/nested statement도 grammar context 범위 그대로 한 node로 남아야 한다.
- FR-006: throw/raise와 normal return을 같은 의미로 합치지 않는다.
- FR-007: 새 언어는 listener + vocabulary + analyzer consumer test가 함께 없으면 지원 완료가 아니다.
- FR-008: analyzer 호환을 위한 source regex fallback을 parser나 consumer에 추가하지 않는다.
- FR-009: 기존 FUNCTION_CALL, control node, DML node의 수·부모·범위를 소실시키지 않는다.
- FR-010: AST JSON shape 변경은 parser 헌법 VI에 따라 analyzer 영향을 명시하고 양끝을 함께 검증한다.

## 4. 비범위

- 상태코드·반환값의 업무 의미 추론
- 위치별 값 추적 또는 객체 상태 추적
- embedded SQL 문자열과 외부 문서의 host-language 의미 해석
- 특정 고객 함수명·코드값·접두 기반 노드 생성

## 5. 수락 기준

- 지원 언어별 정상·빈값·multiline·inline·nested RETURN fixture 통과
- throw/raise, break/continue/goto/exit의 언어별 해당 구문 fixture 통과
- assignment와 call의 source occurrence↔AST missing/extra/wrong-parent 0
- 기존 AST node 소실 0
- 실제 corpus 재파싱 후 analyzer가 AST-only로 동일하거나 더 완전한 obligation/evidence 생성
- parser 전체 Maven test와 analyzer 전체 unittest/prompt hash 통과
- AST 파일 크기·parse wall·analyzer wall/input token 전후 보고

## 6. 실패와 종료

- grammar가 구분하지 못하는 구문은 regex 추정으로 통과시키지 않고 지원 한계로 기록한다.
- 한 언어 통과를 전 언어 완료로 일반화하지 않는다.
- parser와 analyzer 이중 생산 기간을 무기한 두지 않는다. 양끝 전환 뒤 legacy scanner를 삭제한다.
