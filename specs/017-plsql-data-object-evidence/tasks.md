# Tasks: PL/SQL 데이터 객체 구문 증거

- [x] P017-01 첫 오좌표 source→기존 AST→실제 prompt의 정보 손실 경계 확정
- [x] P017-02 SELECT schema/link/alias/qualified-column red fixture
- [x] P017-03 DML target 및 nested-query 소유권 red fixture
- [x] P017-04 optional AST evidence model과 listener 구현
- [x] P017-05 parser 국소·전체 회귀 — 145 tests, failure/error 0, skipped 8
- [x] P017-06 Analyzer consumer 계약 검증 — quoted alias 대소문자 분리와 correlated outer qualifier 보존 포함
- [ ] P017-07 실제 보존 corpus replay 검증 — analyzer 재실행 상한 0이라 미실행
- [x] P017-08 spec 131 C-028 evidence v2 unqualified identifier listener 계약과 Analyzer consumer
  연결 — owner/nested/function/alias/repeated occurrence 계약, 전체 157 tests·failure/error 0·skipped 8
