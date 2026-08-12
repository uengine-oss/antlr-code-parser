# Implementation Plan: PL/SQL MERGE 인라인 대상 보존

1. 첫 실패 좌표와 Oracle 정본 문법을 계약으로 고정한다.
2. 인라인 target 및 다중 물리 객체 반대 사례를 parser test로 고정한다.
3. MERGE grammar를 공통 `dml_table_expression_clause`에 연결한다.
4. listener는 최상위 query block의 단일 물리 객체만 WRITE로 승격한다.
5. 국소/전체 parser 테스트 후 실제 물 코퍼스를 재파싱해 격리와 DBLINK를 전수 감사한다.

