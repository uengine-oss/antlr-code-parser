# 공유 계약: 의미 제어 흐름 AST 노드 어휘 + 노드 분류

**소유(생산자)**: `antlr-code-parser` (spec 007 — 이번 라운드는 **C 언어만**)
**소비자**: `robo-data-analyzer` (spec 054), `robo-data-frontend` (spec 055)
**버전**: 2.0 (2026-07-15). 이 파일이 세 저장소의 유일한 진실이다. 각 저장소 spec/plan은 값을 중복
정의하지 않고 이 파일을 참조한다. 값이 바뀌면 세 저장소 spec을 함께 갱신한다.

> 이 작업의 **동기**를 먼저 밝힌다: 프레임워크 rules/examples 3분할 분석의 정확도가 떨어질 때(반복·환각)
> 함수를 결정론적으로 쪼개 조각별 분석 후 병합하려면 **자를 기준 구조**가 필요하다. 그 구조가 제어문
> AST다. 즉 제어문 AST는 **폴백 분할 전용 뼈대**이며 그 자체가 목적이 아니다. LLM 분석에는 **절대**
> 참여하지 않는다.

## 0. 이번 라운드 범위 (C-first)

- **파서**: 제어문 AST emit은 **C 언어만** 구현한다(spec 007). Java/Python은 정확도 개선이 확인된 뒤 별도
  후속 spec(008 등)에서 리스너만 추가한다.
- **분석기/프론트**: 언어 무관 로직이므로 이번에 완성한다. 아래 §1 어휘는 세 언어 전부를 정의하되, C만
  "지금 구현", Java/Python은 "예약(reserved)"으로 표시한다. 소비자는 예약 타입도 미리 인식하도록 만들어
  두어(추가 시 파서 리스너만 손대면 되도록) DRY를 지킨다.

## 1. 노드 타입 어휘 (기존 토큰은 불변, 순수 additive)

| type 문자열 | 의미 | C | Java | Python | 부모가 될 수 있는 대상 | 자식 |
|---|---|---|---|---|---|---|
| `IF` | 조건 분기 시작 | ✅구현 | ✅구현 | ✅구현 | FUNCTION, 다른 제어문, `ELSE`(else-if 중첩) | 본문 노드, `ELSE` |
| `ELSE` | else/elif 분기 | ✅구현 | ✅구현 | ✅구현 | `IF` | 본문 노드, (Java/C) 중첩 `IF` |
| `LOOP` | for/while/do (구분 없음) | ✅구현 | ✅구현 | ✅구현 | FUNCTION, 다른 제어문 | 본문 노드 |
| `SWITCH` | switch 블록 | ✅구현 | ✅구현 | — | FUNCTION, 다른 제어문 | `CASE` |
| `CASE` | switch의 case/default 그룹 | ✅구현 | ✅구현 | — | `SWITCH` | 본문 노드 |
| `TRY` | try 블록(+finally 문장) | — | ✅구현 | ✅구현 | FUNCTION, 다른 제어문 | 본문 노드, `CATCH` |
| `CATCH` | catch/except 절 | — | ✅구현 | ✅구현 | `TRY` | 본문 노드 |

<!-- Java/Python 구현: 2026-07-16, Java20/Python AstListener + 픽스처 검증
     (Hello.java: IF2·ELSE2·LOOP2·SWITCH1·CASE3(default 포함)·TRY1·CATCH1 /
      sample.py: IF1·ELSE2(elif+else)·LOOP2·TRY1·CATCH1 — 기대치 전항 일치).
     Python loop/try-else 절과 match 문은 범위 밖(리스너 주석에 명시). -->


- C는 `TRY`/`CATCH` 없음(언어에 구문 없음). Python은 `SWITCH`/`CASE` 없음.
- 기존 토큰(`FUNCTION_CALL`, `NEW_INSTANCE`, `IMPORT`, `INCLUDE`, `FUNCTION` 등) 그대로 두고 추가만 한다.

## 2. 노드 분류 — 세 저장소가 공유하는 단일 진실

모든 노드는 정확히 두 부류다. **회색지대 없음.** 분류는 role(=`type` → ontology role) **한 곳**에서만
결정하고, 모든 소비자가 이 분류를 참조한다(소비자마다 개별 `if type==IF` 분기 금지 = 땜빵 금지).

### 2-A. 분석 대상 (analysis targets)

- **대상**: `FUNCTION` / `METHOD` (그리고 DBMS의 `PROCEDURE`/`FUNCTION`/`TRIGGER` 등 루틴).
- LLM 3분할 분석을 받는다. summary·domain_terms·rules/examples·검색색인·health·component 대상.

### 2-B. 구조 노드 (structural / auxiliary)

- **대상**: `FUNCTION_CALL`, `NEW_INSTANCE`, `IMPORT`/`INCLUDE`, `IF`/`ELSE`/`LOOP`/`SWITCH`/`CASE`/`TRY`/`CATCH`.
- 그래프에는 **그려지되**(기본 숨김), **LLM 분석·보강에는 절대 불참**한다.
- 용도: (a) 폴백 분할 뼈대, (b) 호출/생성/import의 CALLS/READS/WRITES 연결(기존 internal 역할),
  (c) 프론트 "숨긴 요소 모두 보기"에서 함수 아래 구조 표시.
- **현황 주의**: `FUNCTION_CALL`/`NEW_INSTANCE`/`IMPORT`는 **현재 그래프에 저장되지 않고**(호출 연결용
  transient) 소비 후 버려진다. 이번 스펙부터 **구조 노드로 그래프에 보존**한다(단, 아래 필드 계약 준수).

## 3. 구조 노드 필드 계약 — "LLM 전에 알 수 있는 것만"

경계 규칙: **"LLM/AI를 쓰기 전, AST(step2 load_ast) 단계에서 결정론적으로 알 수 있는가?"** → 예면 채우고,
아니오면 채우지 않는다. 이는 `data_models/graph_nodes.py::CodeNode`의 기존 필드 그룹 주석과 정확히 일치한다
(`load_ast 가 채움` vs `analyze_code 이 채움`).

| 채운다 (AST 단계, 결정론) | 채우지 않는다 (LLM 산물) |
|---|---|
| `type`/`label`, `name`(식별자) | `summary` |
| `start_line`, `end_line` | `domain_terms` |
| **`code_text`** (자기 라인 범위 원문 슬라이스) | `logical_name` |
| `file_path`, **`owner_id`**(소속), `package` | `search_text` |
| `parent_id` (PARENT_OF 구조 부모) | `stereotype` |
| `comment`, `return_type`, `params`, `annotations`(해당 시) | `embedding` |
| (internal 노드) CALLS/READS/WRITES 기여 — **소유자 = 가장 가까운 FUNCTION/METHOD** | `rules`/`examples`/`questions` |

→ 구조 노드는 곧 **`summary` 등 LLM 필드가 기본값(None)으로 남는 `CodeNode`**다. step6는
`analysis_targets`만 순회하므로 구조 노드에 도달하는 보강 코드 경로가 **존재하지 않는다**(막는 게 아니라
구조적으로 불가능 = 근본 DRY).

## 4. 소유(owner) 책임 분리 — 파서는 만들지 않는다

파서 JSON은 순수 구조 트리(children[])다. "이 FUNCTION_CALL은 어느 함수 소속인가"(owner routine)를
파서가 표시하지 않는다. **robo-data-analyzer가 트리를 하강하며 두 컨텍스트를 분리 스레딩한다**:

- `structural_parent_id` — PARENT_OF 생성용, 바로 위 노드(제어문으로 내려가면 IF로 바뀜).
- `owner_routine_id` — CALLS/READS/WRITES 귀속용, 가장 가까운 FUNCTION/METHOD(제어문을 지나쳐도 불변).

```text
checkout ─PARENT_OF→ IF ─PARENT_OF→ FUNCTION_CALL(save_order)   # 구조(structural_parent)
checkout ─CALLS────────────────────→ save_order                 # 의미(owner_routine)
IF ─CALLS→ save_order   ← 절대 금지
```

## 5. 하위 호환성

- 기존 토큰의 emit 조건·필드는 변경하지 않는다.
- 제어문이 없는 기존 함수의 AST/분석 결과는 이 변경 이후에도 **100% 동일**(회귀 0).
- 제어문이 있는 함수는, FUNCTION 직속이던 FUNCTION_CALL/NEW_INSTANCE가 제어문 노드 아래로 한 단계 더
  중첩될 수 있다. **소실은 회귀(금지), 재중첩은 의도.**

## 6. 소비자(Analyzer) 준수 의무 요약 (상세는 spec 054)

- `role_of()`가 제어문 7타입을 인식하도록 매핑 추가 → `_walk_framework`가 이들과 그 자식까지 재귀
  (현재는 미지정 role → 자식까지 통째 스킵되는 회귀 지점).
- 구조 노드는 `graph_nodes`에만, `analysis_targets`에는 넣지 않는다.
- 검색/요약/health/component/분석순서(cycle·round) 계산은 `analysis_targets`만 순회한다(전수 감사).
- CALLS/READS/WRITES의 소유자는 항상 가장 가까운 FUNCTION/METHOD(§4).
