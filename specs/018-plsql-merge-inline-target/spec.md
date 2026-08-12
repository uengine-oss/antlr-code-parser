# Feature Specification: PL/SQL MERGE 인라인 대상 보존

**Status**: Draft  
**Created**: 2026-08-11  
**Affected consumer**: `D:\work\robo\project\robo-data-analyzer`

## 1. 목적

Oracle `MERGE INTO (subquery)`의 유효한 `dml_table_expression_clause`를 파싱하고,
대상 인라인 뷰 안의 단일 물리 객체와 database link를 AST에 손실 없이 보존한다.

## 2. 확인된 첫 오좌표

`KOMAIN_SPCM_FCO_HDAPS.sql:77`의
`MERGE INTO (SELECT * FROM DUBMMRF WHERE ...) A`에서 기존 grammar가 `INTO` 뒤에
`tableview_name`만 허용한다. 파서가 `(`에서 실패해 해당 프로시저 전체가
`REVIEW_REQUIRED`로 격리되고, 이후 DBLINK를 포함한 모든 구조 증거가 사라진다.

## 3. 외부 계약

Oracle SQL Quick Reference의 `MERGE` 문법은 `INTO dml_table_expression_clause`를 사용하며,
해당 공통 절은 `(subquery [subquery_restriction_clause])`를 허용한다.

## 4. 요구사항

- FR-001: MERGE target은 `dml_table_expression_clause` 전체를 허용한다.
- FR-002: 직접 table/view target의 기존 AST와 WRITE 증거는 바뀌지 않는다.
- FR-003: 인라인 target의 최상위 query block에 물리 객체가 정확히 하나면 그 객체를
  WRITE로 보존하고, target alias와 DBLINK를 그대로 보존한다.
- FR-004: 인라인 target의 SELECT 읽기 증거도 삭제하지 않는다. 대상 행 선택과 물리
  변경이라는 서로 다른 사실을 각각 보존한다.
- FR-005: 최상위 target query block에 물리 객체가 여러 개면 어느 객체가 실제 변경되는지
  추측하지 않고 WRITE를 만들지 않는다.
- FR-006: 동적 객체명, synonym 해소, 원격 DB metadata는 추측하지 않는다.
- FR-007: parser 전체 회귀와 실제 보존 corpus 재파싱에서 기존 6개 격리 루틴이
  정확 파싱되고 DBLINK 증거가 유지되어야 한다.

## 5. 비범위

- 다중 테이블 인라인 뷰의 key-preserved write target 추론
- DBLINK 원격 객체의 runtime 존재 여부 확인
- 분석 LLM을 이용한 SQL 구문 복구

## 6. 수락 기준

- 단일 로컬/DBLINK 인라인 target fixture가 MERGE, SELECT, WRITE/READ 증거를 낸다.
- 다중 테이블 인라인 target은 물리 WRITE를 창작하지 않는다.
- 물 코퍼스 43개가 parser 격리 없이 처리되거나, 남은 격리는 이 문법과 무관한 정확한
  최초 오좌표로 분리된다.

