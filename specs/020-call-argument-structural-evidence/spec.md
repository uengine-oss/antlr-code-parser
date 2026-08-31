# Feature Specification: 호출 인자 구조 증거

**Status**: Complete
**Created**: 2026-08-31
**Affected consumer**: `D:\work\robo\project\robo-data-analyzer`

## 1. 목적

grammar가 이미 구분한 call argument의 순서·범위와 직접 문자열 literal 또는 직접 identifier 여부를
Parser common evidence에 보존한다. Analyzer의 runtime wrapper binding이 source 정규식·문자열 분할·
괄호 세기 없이 이 구조 사실만 소비하도록 생산자 계약을 닫는다.

## 2. 버전 계약

- 기존 sealed common evidence `2.0.0`은 변경하지 않는다. `call.payload.argumentRanges`는 range 배열이다.
- 새 structural frontend 출력은 `2.1.0`이다. call payload는 `argumentRanges` 대신 `arguments`를 쓴다.
- `2.1.0 arguments`의 각 원소는 `range`, `syntaxKind`, `literalKind`, `literalValue`, `identifier`를
  정확히 한 번 가진다. 이름이 다른 호환 alias나 두 필드 동시 발행은 금지한다.
- Analyzer는 sealed `2.0.0`과 current `2.1.0`을 버전별 exact schema로 소비한다.

## 3. 요구사항

- FR-001: 모든 call argument는 grammar 순서의 정확한 half-open source range를 보존한다.
- FR-002: 직접 문자열 literal은 `syntaxKind=string_literal`, `literalKind=string`, decoded
  `literalValue`, `identifier=null`로 발행한다.
- FR-003: 직접 identifier는 `syntaxKind=identifier`, `identifier=<grammar text>`, literal 필드 null로
  발행한다.
- FR-004: 그 밖의 식은 `syntaxKind=expression`과 나머지 value 필드 null로 보존한다. 괄호로 감싼
  identifier를 직접 identifier로 축약하지 않는다.
- FR-005: C의 인접 문자열 literal과 표준 escape는 grammar token을 기준으로 decode한다. 유효하게
  decode할 수 없는 literal은 값을 추측하지 않고 expression으로 보존한다.
- FR-006: Java·Python·Oracle·PostgreSQL 등 아직 literal/identifier 세분화를 구현하지 않은 frontend도
  argument range를 `expression`으로 손실 없이 발행한다.
- FR-007: Parser는 runtime wrapper, internal/external, framework 분류나 최종 binding을 결정하지 않는다.
- FR-008: 기존 call occurrence, nested call, parent, callee/receiver/scope와 exact-once population을
  바꾸지 않는다.

## 4. 비범위

- 문자열 내용이 실제 callable인지 판단
- wrapper contract 선택 또는 framework 의미 해석
- identifier의 symbol/type/linkage resolution
- source text 재파싱 fallback

## 5. 수락 기준

- C direct string, adjacent string, escaped string, direct identifier, grouped/expression fixture 통과
- nested call의 안쪽/바깥쪽 occurrence와 argument ownership 보존
- 모든 structural frontend의 `2.1.0` exact contract와 기존 `2.0.0` Analyzer replay 통과
- Analyzer wrapper 정상·누락·종류 불일치·internal·partial-upload external 반례 통과
- Parser 전체 Maven test와 Analyzer 영향 회귀 통과
