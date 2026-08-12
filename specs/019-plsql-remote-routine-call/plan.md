# Implementation Plan: PL/SQL 원격 함수 호출 증거

1. source DBLINK 토큰과 AST 물리 객체 좌표의 잔차를 종류별로 전수 분류한다.
2. 원격 함수/sequence 반대 사례를 parser test로 고정한다.
3. grammar가 이미 구분한 link+function_argument 관문에서만 CALL을 생성한다.
4. 물 코퍼스를 재파싱하고 source→AST→Analyzer CALL 소비를 대조한다.

