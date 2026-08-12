# Feature Specification: PL/SQL 데이터 객체 구문 증거

**Status**: Draft  
**Created**: 2026-08-10  
**Affected consumer**: `D:\work\robo\project\robo-data-analyzer`

## 1. 목적

PL/SQL grammar가 이미 식별한 물리 데이터 객체, schema, database link, alias와
alias-qualified column을 DML AST에 손실 없이 보존한다. Analyzer가 SQL 원문 정규식으로
테이블과 컬럼 소유권을 다시 추측하지 않도록 구문 생산자 책임을 확정한다.

## 2. 확인된 첫 오좌표

`PRC_INSERT_RDD01DD_TB_WATERSUISUL.sql:239-245`에서 `A`는 `RDIB2EN_TB`, `B`는
`RDD01DD_TB`다. 현 AST SELECT 노드는 범위와 자식만 제공하므로 Analyzer prompt에는 두
bare table만 전달된다. 그 결과 `B.VAL`의 실제 소유 테이블을 모델이 다시 추론하며 일부
table insight와 최종 table description에서 `VAL`이 `RDIB2EN_TB` 소유로 오염됐다.

## 3. 요구사항

- FR-001: SELECT/INSERT/UPDATE/DELETE/MERGE AST 노드는 grammar가 소유한 물리 객체를
  `dataObjectReferences`로 제공한다.
- FR-002: 각 객체 증거는 `rawReference`, `schema`, `name`, `databaseLink`, `alias`,
  `access(READ|WRITE)`, `startLine`을 보존한다. 없는 값은 창작하지 않는다.
- FR-003: 객체 alias 또는 객체명으로 명시 수식된 컬럼만
  `qualifiedColumnReferences(rawReference, qualifier, name, startLine)`로 제공한다.
- FR-004: 중첩 query의 객체·컬럼은 가장 가까운 SELECT 노드가 소유한다. 바깥 노드에
  중복 복제하지 않는다.
- FR-005: INSERT target, UPDATE/DELETE target, MERGE target은 WRITE다. SELECT FROM의
  물리 객체는 READ다. 의미 추론이 필요한 암묵 read/write는 parser가 만들지 않는다.
- FR-006: derived table, collection expression, 동적 SQL처럼 물리 `tableview_name`이
  없는 문맥은 객체를 추정하지 않는다.
- FR-007: 기존 AST 노드 type, 범위, parent, CALL/control/DML 수를 바꾸지 않는다.
- FR-008: 새 필드는 optional이며 비어 있으면 기존 JSON shape를 유지한다.
- FR-009: unquoted identifier는 대소문자를 같은 식별자로 비교하고 quoted identifier는
  내부 대소문자를 보존해 서로 다른 alias를 교차 귀속하지 않는다.
- FR-010: DML target을 바깥 범위로 참조하는 correlated subquery의 명시 qualifier는
  nested SELECT에서 버리지 않고 미귀속 qualified-column 증거로 보존한다.

## 4. 비범위

- Catalog object ID 결정, synonym 해소, search_path/current schema 적용
- alias 없는 컬럼의 테이블 소유권 추론
- 동적 SQL 문자열 파싱
- 샘플 조회와 업무 의미 생성

## 5. 수락 기준

- schema/link/alias가 있는 SELECT와 INSERT/UPDATE/DELETE/MERGE fixture가 정확한 구문
  증거를 낸다.
- self join과 nested query에서 소유 노드·중복·누락이 정확하다.
- `A.SUB_TAGSN`, `A.TAG_GUBUN`, `A.BNB_CODE`, `B.VAL`, `B.TAGSN`, `B.LOG_TIME`가
  첫 오좌표의 정확한 alias에 귀속된다.
- parser Maven 전체 회귀와 Analyzer consumer 계약 테스트를 통과한다.

## 6. 외부 정본 확인

- Oracle SQL Reference, [Database Object Names and Qualifiers](https://docs.oracle.com/en/database/oracle/oracle-database/26/sqlrf/Database-Object-Names-and-Qualifiers.html):
  unquoted identifier는 대소문자를 구분하지 않고 uppercase로 해석하며 quoted identifier는
  대소문자를 구분한다.
- Oracle SQL Reference, [Using Subqueries](https://docs.oracle.com/en/database/oracle/oracle-database/26/sqlrf/Using-Subqueries.html):
  nested subquery는 바깥 statement/query block의 table 또는 alias 컬럼을 명시 참조할 수 있다.
