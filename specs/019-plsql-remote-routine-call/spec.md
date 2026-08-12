# Feature Specification: PL/SQL 원격 함수 호출 증거

**Status**: Draft  
**Created**: 2026-08-11  
**Affected consumer**: `D:\work\robo\project\robo-data-analyzer`

## 1. 목적

표현식 안의 `routine@dblink(args)`를 CALL로 보존하되,
`sequence.NEXTVAL@dblink`는 호출로 창작하지 않는다.

## 2. 확인된 첫 오좌표

`KOMAIN_SPCM_KMARF_VERIFICATION.sql:430`의
`KWMS_DATE_CHANGE@DAON('B','M',10,OBSDHM)`는 grammar가 함수 인자와 DBLINK를 정확히
식별하지만 listener가 statement형 `call_statement`만 CALL로 만든다. 따라서 원격 함수 호출
9건이 CALL 그래프에서 누락된다. 같은 코퍼스의 `EM_TRAN_PR.NEXTVAL@SMS` 등 33좌표는
sequence 참조이므로 CALL이 아니다.

## 3. 요구사항

- FR-001: `general_element_part`에 DBLINK와 함수 인자가 모두 있으면 CALL을 만든다.
- FR-002: CALL 이름은 수식 이름과 `@dblink`를 보존하고 인자 값은 포함하지 않는다.
- FR-003: DBLINK만 있고 함수 인자가 없는 sequence/일반 요소는 CALL을 만들지 않는다.
- FR-004: 일반 내장 함수까지 새 CALL로 확장하지 않는다. 이번 계약은 원격 링크가 명시된
  호출만 다룬다.
- FR-005: 기존 statement CALL의 노드 수와 이름은 바꾸지 않는다.

## 4. 수락 기준

- 원격 함수 2회와 sequence NEXTVAL 2회 fixture에서 CALL은 정확히 2개다.
- 물 코퍼스의 독립 source DBLINK 토큰 980건(고유 좌표 955)은 물리 객체 913좌표,
  원격 함수 9좌표, sequence 33좌표로 설명되고 고유 좌표 분류 잔차가 0이다.
- parser 전체 회귀와 Analyzer DBMS CALL 소비 계약을 통과한다.
