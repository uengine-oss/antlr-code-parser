# Phase 0 Research: Unified Intake & Content-Based Classification

스펙의 미해결 설계 질문 4개를 결정한다. 각 항목: Decision / Rationale / Alternatives.

## R1. 표 정의 vs source 분류를 무엇으로 판정하나 (FR-003·004·005)

**Decision**: 입구 단계의 **경량 statement 스캐너**. 각 `.sql`을 top-level statement 경계로 나눠, 각 statement의 선행 키워드를 토큰 단위로 식별한다 — `CREATE [OR REPLACE] [GLOBAL TEMPORARY] {TABLE|VIEW|MATERIALIZED VIEW|INDEX|SEQUENCE}` → 표 정의(DDL), `CREATE [OR REPLACE] {FUNCTION|PROCEDURE|PACKAGE|PACKAGE BODY|TRIGGER|TYPE}` → source, 그 외 → source(기본). 주석/문자열 리터럴은 토크나이즈 시 무시. 비-`.sql`(.java/.py 등)은 항상 source.

**Rationale**:
- **파싱 성공 여부와 독립** — antlr가 못 읽는 프로시저(현재 크래시 원인)도 키워드만으로 source로 올바르게 보낸다. "분류를 전체 파스에 의존"시키면, 파스가 깨지는 바로 그 프로시저를 분류하지 못해 문제가 재발한다.
- **싸다** — statement 선두만 검사. 입구 오버헤드 무시 가능.
- **섞인 파일 자연 처리** — statement 단위라 한 파일의 표/프로시저를 각각 분류(R3).
- 헌법 II("바이트가 진실")·III(내용 기반)와 정합.

**Alternatives considered**:
- *antlr 전체 파스트리 재사용*: 정확하지만 분류가 파스 성공에 묶임 → 프로시저 파스 실패 시 분류 불가(현 사고 재현). 또 입구가 전체 파스에 결합되어 무겁다. **기각**.
- *파일명/폴더(`ddl/`) 기준*: 현행 방식. 프로젝트마다 취약, 섞인 파일 불가 — 본 기능이 대체하려는 대상. (역호환 힌트로만 잔존, 진실 아님.) **기각**.

## R2. 경로 모드 로컬 파일을 data/로 어떻게 반입하나 (FR-002·007)

**Decision**: `java.nio.file.Files.createLink`(하드링크) 우선 시도 → 실패(다른 볼륨/파일시스템 미지원/권한) 시 `Files.copy`로 폴백. 대상은 `data/source/<상대경로>`(원본 폴더 구조 보존). 매 run 전 `data/` 전량 리셋(기존 정책).

**Rationale**: 같은 드라이브에서 하드링크는 디스크 0 추가·즉시. 폴백 복사로 안전성 보장. 경로 모드의 "업로드 없음" 이점을 유지하면서 표준 `data/`로 수렴.

**Alternatives**: *심볼릭 링크* — Windows 권한/정책·이식성 문제로 기각. *항상 복사* — 대용량에서 디스크 비용, 기각(폴백으로만 사용).

## R3. 한 파일에 표+프로시저가 섞이면 (FR-005, US5)

**Decision**: 원본 파일은 그대로 `data/source`에 보존(1:1, AST 파싱 입력). R1 스캐너가 그 파일에서 **표 정의 statement만 추출**해 `data/ddl/<상대경로>`(또는 `<파일>.ddl.sql`)에 **파생 산출물**로 기록. 즉 표 정의는 ddl 세트에, 원본(프로시저 포함)은 source 세트에 — 둘 다 존재.

**Rationale**: 섞인 파일은 통째로 한 바구니에 못 넣는다(헌법 III 개정의 동기). 파생 ddl 산출 + 원본 보존이 "표 정의는 DDL 소비자에게, 프로시저는 AST 파서에게" 둘 다 만족. analyzer의 sqlglot은 깨끗한 ddl만, antlr 파스는 원본 source를 본다.

**Alternatives**: *통째 라우팅(파일 단위)* — 단일종류 파일엔 충분하나 섞인 파일에서 표 또는 프로시저 한쪽을 잃음. 기각(US5 미충족). *분류 안 하고 analyzer resilience에만 의존* — 근본이 아님(입구가 더러운 채로 남음). 기각.

## R4. 한 파일 실패를 어떻게 격리·보고하나 (FR-006, US4)

**Decision**: 파일 단위 `try/catch`. 분류/반입/파스 실패 시 그 파일을 건너뛰고, NDJSON 스트림에 **구조화된 `skipped` 이벤트**(`{file, stage, reason}`)를 emit한 뒤 다음 파일 계속. run 종료 시 입구 보고(FR-010)에 건너뛴 수 포함. 단, **입력 전체가 무효**(경로 없음/빈 입력 등)면 기존대로 즉시 오류.

**Rationale**: 헌법 V(스트리밍 보고)와 정합이며 IV(no silent failure)를 **위반하지 않음** — 건너뜀을 "조용히 빈 결과"가 아니라 명시 이벤트로 알린다. 레거시 코드의 이상 파일 하나가 전체를 가라앉히지 않게 한다. (analyzer는 이 격리에 기대어 IV의 raise 정책을 그대로 유지 — analyzer 스펙 014.)

**Alternatives**: *기존처럼 즉시 raise* — 한 파일이 전체 중단(현 사고). 기각. *조용히 skip* — 헌법 IV 위반. 기각.

## 결론 — NEEDS CLARIFICATION 0
4개 설계 질문 모두 결정됨. Phase 1(data-model·contracts·quickstart) 진행 가능.
