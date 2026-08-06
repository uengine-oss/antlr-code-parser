# Tasks: 구조 구문 AST 생산자 계약

- [x] P016-01 parser/analyzer의 source pattern scanner와 AST statement consumer 전수 목록 작성
      (2026-08-05 — analyzer 75개 정규식 지점 19파일 분류: AST 이동/analyzer 유지/embedded owner.
      analyzer `_walk_framework`가 미지 role을 조용히 버리는 소비자 결함도 확인)
- [x] P016-02 지원 언어별 source occurrence↔현재 AST red fixture 작성
      (`StructuralStatementAstContractTest` — C/Java/Python/PLSQL 12 tests, red 7건 실측 후 green)
- [x] P016-03 Node 표현 필드와 canonical statement vocabulary 계약 확정
      (`Node.expression/target/operator` 추가, @JsonPropertyOrder·golden PROPERTY_ORDER 동기,
      exact source 보존은 `ParserUtils.getExactSourceText` — Java WS skip 으로 토큰 결합이
      `x + 1`→`x+1` 훼손되는 실결함을 잡고 CharStream 직접 슬라이스로 통일)
- [x] P016-04 C/Java/Python listener RETURN·종료·ASSIGNMENT 배선
      (C: jumpStatement→RETURN/BREAK/CONTINUE/GOTO + statement-level ASSIGNMENT + IF/SWITCH/
      LOOP(test-only)/CASE expression; Java: RETURN/THROW/BREAK/CONTINUE + ASSIGNMENT + 조건;
      Python: RETURN/RAISE/BREAK/CONTINUE + routine 내 ASSIGNMENT + 조건. 전부 leaf emit 으로
      기존 FUNCTION_CALL 수·부모 보존. for 머리 대입은 grammar 문맥으로 제외 — TA-102 정합)
- [ ] P016-05 PL/SQL/PostgreSQL 기존 emit과 새 공통 계약 정합
      (완료: PLSQL RETURN/ASSIGNMENT/IF/ELSIF/LOOP expression·target·operator 필드,
      PG RETURN/ASSIGNMENT/IF/ELSIF/WHILE/CASE/WHEN 필드. **미완**: PLSQL EXIT/CONTINUE/RAISE
      노드 emit 여부 — analyzer DBMS 소비자 영향 검토 후 결정; PLSQL CASE/WHEN 노드 미emit 확인)
- [ ] P016-06 기존 CALL/control/DML node 소실·부모 회귀 0 검증
      (parser 전체 132 tests green + golden diff 전수 검토는 통과; 실제 corpus 재파싱 대조는 미실행)
- [ ] P016-07 analyzer reader/ontology/context/evidence를 AST-only 소비로 전환
- [ ] P016-08 analyzer legacy token/regex scanner와 중복 ontology keyword 삭제
- [ ] P016-09 parser Maven 전체 회귀 + analyzer unittest/prompt hash 통과
- [ ] P016-10 실제 corpus 재파싱 source↔AST 전수 대조 및 cold wall/정확도/토큰 보고

각 task는 구현→국소 테스트→반대 사례→양끝 통합→전체 회귀→diff 감사까지 끝나야 체크한다.
