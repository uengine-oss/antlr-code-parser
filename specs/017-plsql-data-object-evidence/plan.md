# Implementation Plan: PL/SQL 데이터 객체 구문 증거

1. 실제 오좌표와 nested/schema/link/alias 반대 사례를 parser contract test로 고정한다.
2. optional AST value object와 JSON 순서를 추가한다.
3. PL/SQL listener에서 물리 tableview와 명시 qualified column만 grammar tree로 수집한다.
4. 기존 DML 구조·범위·부모 및 golden JSON 회귀를 검증한다.
5. Analyzer AST loader와 공통 데이터 객체 사용 증거 계약을 연결한다.
6. spec 131 C-028에서 grammar-owned unqualified identifier evidence v2를 추가하고 Analyzer의
   SQL tokenizer source replay를 제거한다. 물리 owner 결정은 Analyzer의 DDL/query-scope 유일성
   검증에 남긴다.
