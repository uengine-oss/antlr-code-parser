# Tasks: Platform-Aware Console Charset

## User Story 1 - 읽을 수 있는 콘솔 로그

- [x] T001 [US1] `src/main/resources/logback-spring.xml`에서 명시 설정 → JVM 문자셋 → UTF-8 순서로 콘솔 charset을 결정한다.
- [x] T002 [US1] Windows 코퍼스 테스트 콘솔과 Surefire XML에서 한국어 경로 및 로그가 보존되는지 검증한다.

## User Story 2 - UTF-8 산출물 회귀 금지

- [x] T003 [US2] 원본 해시 불변과 AST/diagnostic JSON strict UTF-8·JSON 전수 검증을 실행한다.
- [x] T004 [US2] 전체 `mvnw.cmd test`를 실행하고 Git diff를 재감사한다.

## Completion Evidence

- 코퍼스 요약 보고서와 Surefire XML
- strict UTF-8/JSON 검사 결과
- Maven 전체 테스트 결과

## Verification

- `water-final-delivery.json`: Oracle 38/38 `EXACT`, lexer/parser/recovery 오류 0, 원본 실행 중 불변.
- Surefire XML: `분석대상모음`, `수자원`, 시스템별 한국어 경로와 `[경로 반입]` 로그 보존.
- AST/diagnostic JSON 76개: JSON 오류 0, U+FFFD 대체 문자 0.
- 전체 Maven: 107개 실행, 실패 0, 오류 0, 건너뜀 8, `BUILD SUCCESS`.
