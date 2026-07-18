# Defect Fix: C 매크로 선치환이 initValue 심볼을 소실시켜 INIT_BY 누락

**Created**: 2026-07-16 · **Status**: Implemented · **Repo**: antlr-code-parser (owner)

## 증상 (analyzer 측 관측, 원문 정답지 대조)

shop_mall 재분석 그래프의 INIT_BY 가 기대 7건 중 1건만 생성(6건 누락). 누락 전부
"배열 size 가 **헤더(#include) 매크로**인 전역변수"(예: `char g_sqlbuf[LEN_SQL];`).
ast.json 증거: `code_text="...[LEN_SQL]..."`(원문) vs `init_value="[2048]"`(전개됨).
유일 생존 1건(`g_trace_stack[TRACE_MAX]`)은 TRACE_MAX 가 **.c 자기파일** 정의라 살아남음.

## 근본원인

`CParserStrategy.preprocessSource()` 2단계가 파싱 전에 소스에서 매크로 상수를 숫자로
전면 치환한다. 수집기(`collectMacroConstants`)가 **.h 파일만** 순회하므로 헤더 매크로만
치환되는 비대칭까지 설명됨. 치환된 토큰 스트림에서 `extractArraySize` 가
`getOriginalText` 를 불러도 이미 `[2048]` — analyzer `reference_linker`(계약: "antlr 가
size 표현식을 initValue 에 **원문 그대로** 통합 출력") 가 심볼을 매칭할 수 없다.

REFERENCES(1,418건)가 무결한 이유: analyzer 는 참조 스캔에 파서 토큰이 아니라
**원본 소스 파일**을 직접 읽는다 — 오염 표면은 파서 산출 initValue 뿐.

## 수정

2단계 매크로 치환과 그 수집기를 제거한다(1단계 조건부컴파일 처리는 유지).
ANTLR C 문법은 `char x[LEN_SQL + 1]` 의 식별자 상수식을 문법적으로 정상 파싱하므로
치환은 파싱 필요조건이 아니며, 원문 훼손(심볼 소실)만 남는 코드다. 소비처 전수 확인:
`collectedMacroConstants`/`collectMacroConstants`/치환 루프는 `CParserStrategy` 내부에서만
사용 — 외부 영향 0.

## 검증 (원문 독립 정답지, goal-verify/)

- 재파싱+재분석 후 `verify_symbol_refs.py`: INIT_BY 기대 7/7, 심볼 우주 276·HAS_MEMBER 42검사·
  REFERENCES 1,418(누락0·과잉0) 회귀 없음.
- `verify_relationships.py`(CALLS 662·제어구조 124함수)·`diff_runner.py`(함수124·테이블22·FK11)·
  `e2e_correctness.py` 14 AC 회귀 없음 — 치환 제거가 파싱을 깨지 않음의 증거.
