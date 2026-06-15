# Phase 1 Data Model: Unified Intake & Content-Based Classification

입구가 다루는 개념 엔티티. (런타임 자료구조 — 영속 DB 아님; 파일시스템 `data/`가 산출 저장소.)

## Entity: IntakeRequest (입구 요청)

하나의 분석 입력. 두 형태 중 하나.

| 필드 | 타입 | 설명 | 검증 |
|---|---|---|---|
| `mode` | enum(`upload`, `path`) | 입력 모드 | 둘 중 하나 필수 |
| `files` | 멀티파트 파일[] | (upload) 상대경로명 + 바이트 | mode=upload면 필수 |
| `projectRoot` | 경로 | (path) 로컬 폴더 절대경로 | mode=path면 필수·존재·읽기가능 |
| `scope` | 상대경로[] | (path) 반입할 포함 파일 목록(프론트 014가 결정) | 비면 "전체"로 해석 |

**상태 전이**: `received → classified → materialized → parsed → done`. 단계별 파일 실패는 그 파일을 `skipped`로 표시(요청은 계속).

## Entity: ClassifiedFile (분류된 파일)

입구가 본 각 파일의 판정 결과.

| 필드 | 타입 | 설명 |
|---|---|---|
| `relativePath` | 경로 | `data/` 기준 상대경로(원본 구조 보존) |
| `kind` | enum(`ddl`, `source`) | 내용 기반 판정(R1) |
| `derivedDdl` | 경로? | 섞인 파일에서 추출한 표 정의 산출물 위치(R3, 있을 때) |
| `dialectHint` | enum(`oracle`,`postgresql`,`null`) | `.sql` 방언 점수 결과(기존 LanguageDetector) |

**규칙**:
- `CREATE TABLE/VIEW/MATERIALIZED VIEW/INDEX/SEQUENCE` 선언 → `ddl`.
- `CREATE FUNCTION/PROCEDURE/PACKAGE[ BODY]/TRIGGER/TYPE` 및 기타 코드 → `source`.
- 섞인 `.sql` → 원본은 `source`, 추출 표 정의는 `derivedDdl`(→ `data/ddl`).
- 미상/판정 불가 내용 → `source`(기본, 누락 금지).

## Entity: StandardDataSet (표준 데이터 세트)

입구의 유일 산출이자 다운스트림 유일 입력.

```
data/
├── source/   # 원본 파일 1:1(AST 파스 입력) — 모든 종류 포함
├── ddl/      # 표 정의만(파생 포함) — analyzer sqlglot 입력
└── analysis/ # AST JSON(노드 스키마 불변) — analyzer step2 입력
```

**불변식**: `ddl/`에는 표 정의 외 내용이 없다(SC-003). 매 run 전량 리셋(헌법 IV).

## Entity: IntakeReport (입구 보고)

호출자에게 "무슨 일이 있었는지" 요약(FR-010).

| 필드 | 타입 | 설명 |
|---|---|---|
| `ddlCount` | int | ddl로 분류된 수 |
| `sourceCount` | int | source로 분류된 수 |
| `skipped` | `{file, stage, reason}[]` | 건너뛴 파일(R4) — 비어도 명시 |
| `materializedVia` | enum(`hardlink`,`copy`,`mixed`) | (path) 반입 방식 |

스트림으로 `skipped` 이벤트는 발생 즉시, 요약은 `complete` 직전에 전달(헌법 V).
