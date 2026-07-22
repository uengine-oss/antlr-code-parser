# Verification Report — 012 Parser-Owned Localized Repair

Date: 2026-07-22 (Asia/Seoul) · Worktree: f0d21ba + uncommitted changes (preserved, not pushed)

Every claim below is reproducible from the listed command or artifact. Categories are strict:
**검증됨** (ran, evidence on disk) / **실패함** (ran, failed) / **미검증** (not run) /
**REVIEW_REQUIRED** (correct fail-closed outcome, needs a human).

## 검증됨

1. **전체 테스트 green** — `.\mvnw.cmd test`: 100 tests, 0 failures, 0 errors, 7 skipped
   (skips are live-gated: GPU/corpus system properties absent). Was 70 run / 2F+1E at session
   start; +30 new tests added with the feature.
2. **정상 입력 Node JSON 계약** — audit clone `f0d21ba`(D:\work\robo\.audit) 파서와 현 worktree를
   같은 분석대상모음 corpus(266 파일→175 AST)에 실행, 파일별 대조:
   - 126 byte-identical.
   - 46 valid files: 추가만 존재, 추가 타입은 제어흐름(IF/ELSE/TRY/CATCH/LOOP/SWITCH/CASE)
     한정 — 세션 이전부터 worktree에 있던 spec-007 리스너 확장의 의도된 결과.
   - 3 files root-caused: AMS_procedures(복구 개선으로 노드 증가, add-only),
     swing_system/main.c(문법오류 11개의 **비정상 입력** — 복구 경로가 baseline의 조용한
     ANTLR 오류복구 출력과 다름; `#ifdef` 블록 호출 7건이 unit 재파싱에서 제외됨),
     ukesa/PCReferenceInformation(**baseline 결함 정정** — baseline은 외부 메서드를 4007에서
     잘못 절단하고 익명클래스 메서드를 CLASS 직하에 방출; 현 출력은 3945–4051 정상 계층).
   - **정상 입력에서 삭제·개명·계층 파괴 0.**
3. **최소 전송 (FR-021/025)** — AMS live 재실행 sidecar 실측: unit 최대 86,026자에 대해
   slice 90–3,972자 전송, 비율 0.11%–41.3%, 중앙값 1.29%. 이전 구현의 65,536자 상한
   size-skip(3건)은 소멸 — 모든 unit이 공략 가능해짐. 요청별 sliceLevel/chars/ratio가
   AgentRequestEvidence로 기록됨.
4. **결정론 엔진 단독 성능 (FR-033/044)** — Agent 완전 비활성으로 전체 corpus 재실행:
   이전 live-agent 실행과 unit 결과 완전 동일(EXACT 4 / RECOVERED_VALIDATED 21 /
   RECOVERED_SAFE 3 / REVIEW_REQUIRED 10), Agent 0회, 52초(이전 146초).
   → **이전 구현에서 Agent가 기여한 실채택이 0건이었음을 재확인**하며, fail-closed 조건
   (Agent 없이 안전 동작)을 실물로 충족.
5. **의미 위험 편집 차단 (SC-006)** — 식별자/리터럴/연산자/트랜잭션 키워드 삭제는 재파싱이
   성공해도 auto-adopt 불가, 프로필이 허용해도 위험 클래스 우선(테스트로 고정).
6. **다중 오류 wave 수렴** — 독립 오류 2개 fixture가 wave 2회로 수렴(결정론), oscillation·
   no-progress·fingerprint 가드 동작.
7. **위치 재정박** — validator가 expectedText 유일 일치 시 결정론 재정박:
   AMS live에서 EXPECTED_TEXT_MISMATCH 14→3 감소(동일 모델·동일 corpus).
8. **원본 불변** — 모든 corpus 실행에서 inventory hash 전후 동일
   (`originalCorpusUnchanged: true` ×4 runs).
9. **GPU 동시성 실측 (SC-008)** — 실제 SGLang endpoint(frentis-ai-model)에 1→32 동시성,
   레벨당 16 요청(~1.5K자 slice), char-가중 token budget 세마포어 가드 하 실측:
   | 동시성 | 처리량/s | p50 지연 | p95 지연 |
   |---:|---:|---:|---:|
   | 1 | 0.27 | 4.1s | 4.6s |
   | 2 | 0.43 | 4.8s | 5.2s |
   | 4 | 0.52 | 8.0s | 8.5s |
   | 8 | 0.62 | 11.8s | 13.9s |
   | 16 | 0.60 | 26.6s | 26.6s |
   | 32 | 0.64 | 24.9s | 24.9s |
   처리량은 8에서 포화, 이후 지연만 선형 증가 → **운영 동시성 4~8 권장**(스레드 수 고정이
   아니라 문자 예산으로 제어). failures 열은 제안 검증 거부(왕복은 완료·측정됨).
   → `target/corpus-reports/gpu-concurrency-benchmark.json`.
10. **신규 언어 온보딩 (SC-007)** — NewLanguageOnboardingRehearsalTest: 새 언어(.toy)를
    프로덕션 파일 0개 추가·수정으로 공개 SPI만으로 등록, 감지·기본 복구 설정·localization
    상속 확인.
11. **Analyzer 무변경** — `git -C robo-data-analyzer status`: tracked 변경 0 (untracked
    run-output/만 존재, 기존 산출물).

12. **변이 벤치마크 (사용자 지시, ground-truth 채점)** — EXACT로 파싱되는 실물 파일
    (java/python/c/oracle)에 문법 파괴 변이를 주입하고 **원본 AST와 바이트 대조**로 채점
    (`MutationRepairBenchmarkTest`). 변이는 소형(세미콜론·괄호·콜론 삭제, 키워드 중복, AS
    삽입)과 **대형(따옴표 안 닫힘, 블록 주석 안 닫힘, 쓰레기 문자 라인 주입)** 총 30개 적용:
    | 모드 | 원본 완전 복원 | 정직한 부분 구제 | false-accept |
    |---|---:|---:|---:|
    | 결정론 단독 | 5/30 | 25/30 | **0** |
    | GPU Agent | **18/30** | 12/30 | **0** |
    → **Agent의 실증 가치 확인**: 결정론이 못 푸는 변이(ANTLR 오류 위치 오보고, lexer 파괴
    일부 포함)를 GPU가 원본과 바이트 동일하게 복원. 따옴표 안 닫힘은 예상대로 자동 복원
    불가 → 전부 정직한 부분 구제(오답 채택 0). 이 벤치마크가 잡아낸 결함 4건을 모두 수정:
    (a) unit 밖 전역 선언이 조용히 소실되며 VALIDATED로 과장 → 파일 수준 수리 패스 신설 +
    라인 커버리지 축소 시 PARTIAL 강등, (b) unit 경계 밖 주석 소실 → 파일 수준 수리로 해소,
    (c) Agent 수리가 선언들을 함수 하나로 뭉개고도 재파싱 통과하던 **진짜 false-accept** →
    "locator가 찾은 모든 unit 이름이 결과 AST에 존재해야 채택" 게이트로 차단, (d) Python
    들여쓰기 복원 실패로 코드가 스코프를 이동하고도 유효 구문이라 채택되던 **진짜
    false-accept** → 들여쓰기-유의 언어(문장 종결자 미선언으로 판별, 언어 조건문 없음)에서
    변경 라인의 들여쓰기가 인접 라인과 불일치하면 후보 거부. 수정 후 실물 corpus 상태 회귀 0.
13. **복구 스트림 메시지** — 복구 시작/파일 수준 채택/unit 자동 수리/REVIEW_REQUIRED를 기존
    wire 계약(type=message/warning + 한국어 content)으로 방출 — **프론트 무변경으로 표시
    가능**. 테스트로 방출 검증(`recoveryEmitsUserFriendlyStreamEventsOnTheExistingWireContract`).

## 14. 적대적 감사 2회 반영 (2026-07-22 저녁)

사용자 지시로 독립 적대 감사 2회를 실행, **모든 발견을 반영**:

**감사가 잡아 수정한 결함**: (a) unit 이름 게이트가 스키마 수식·PACKAGE 표기 차이로 정상
whole-file 복구를 조용히 폐기 → 정규화(따옴표·대소문자·스키마 세그먼트) + 컨테이너 kind 제외
+ 거부 시 스트림 기록. (b) coverage-단독 거부 파일이 무수리 재사용 후 RECOVERED_VALIDATED로
세탁 → 진단 0 + 무수리 시 원 사유를 승계한 PARTIAL 강등. (c) 모델의 "정직한 기권"이
스키마(minItems 1)상 불가능 → minItems 0 + 기권은 AGENT_AMBIGUOUS_PROPOSAL로 기록.
(d) FR-025 prompt tokens 기록 미구현 → provider usage 파싱해 AgentRequestEvidence에 기록.
(e) [0,len-1] 준-전면 재작성 통과 → 허용 span의 90% 이상 편집 거부. (f) FR-063 provider 옵션
상호배타 미강제 → 생성자에서 REPAIR_AGENT_PROVIDER_OPTIONS_CONFLICT. (g) **변이 벤치마크의
PARTIAL 무채점** → 부분 출력의 모든 방출 unit을 원본 subtree와 대조(PARTIAL_CORRUPTED=빌드
실패). 이 채점기가 즉시 실전 결함 1건을 추가 검출: 0열 손상이 locator 경계를 절단해 잘린
함수가 "clean"으로 채택되고 본문이 고아로 유출 → **gap-손상 unit 확장**(공백 구간에 진단+
고아 콘텐츠 동시 존재 시 다음 unit까지 확장) + 실패 unit 그림자 구간 드롭으로 수정.
수정 후: 결정론 변이 PARTIAL_CORRUPTED 0, 전체 suite 107 green, corpus 상태 회귀 0.

**감사에 따라 정직하게 강등한 주장**: FR-042(token/AST 지문 델타)와 FR-050(진단 독립
그룹핑·병렬 실행)은 **미구현 — 유보**로 재분류(채택 안전성은 재파싱+게이트가 담당, 병렬은
Agent 계층 세마포어만 실증). 온보딩 리허설의 범위는 "등록·감지·SPI 기본값 상속"이며 **복구
동작은 parseUnit 구현이 있어야**(없으면 fail-closed 미복구) — "recovery defaults all work"
표현 정정. wave 진동·지문 가드는 코드 존재하나 **전용 테스트 부재**. 변이 채점의 ground
truth는 AST가 표현하는 범위까지만 유효(AST 미방출 영역의 변이는 채점 불가). 벤치마크
"바이트 대조"는 정확히는 파일명 필드 제외 후 트리 동등 비교.

## 15. 3차(FR-040 확장)·최종 변이 수치와 원칙 전수 감사 (2026-07-22 밤)

**Agent 편집 의미 게이트 3종 추가** — GPU 재실행이 잡은 false-accept 2건(호출 소실:
`(name` 삭제 / `(`→`.` 치환으로 호출→속성 변환)을 근본 차단: ① 편집이 제거·삽입하는 모든
토큰의 의미 분류(FR-040를 Agent 경로에 확장), ② 비공백↔비공백 치환 금지(삽입·삭제·공백화만
자동 채택), ③ 단어 병합 삭제 금지. **최종 변이 수치**: 결정론 5 완전복원/25 정직 부분구제,
GPU 8 완전복원/22 정직 부분구제, **FIXED_DIFFERENT·PARTIAL_CORRUPTED 모두 0**(두 등급 모두
빌드 실패 단언으로 상시 감시). 완전복원 감소(18→8)는 fail-closed 비용이며, 회복 수단은
RepairProfile 선언 확장(안전·언어별 데이터)으로 명시.

**원칙 원문 전수 감사(위반 29건) 처리 결과**:
- 즉시 수정(15): 미사용 enum 상수(VALIDATION/AGENT) 삭제 + COVERAGE 문자열 우회 교정,
  ParserUtils 죽은 정규식 대안, HealthCheck System.out 제거, 전역 예외 핸들러 log.error,
  토큰 정규식 이중 선언 → SourceTokens 단일화, SliceLevel.next() 사다리 일원화(이중 진실
  제거), 매직 넘버 6종 상수화+근거 주석(수정 상한 1/4, 진단 창 ±96/256), 무의미
  same-package import 14건 제거.
- 감사 오판 반박(1): registry.require/candidates "소비자 0" 주장 → 온보딩 리허설·카탈로그
  검증 테스트가 사용 중(SPI 표면) — 유지.
- 기록 유보(§11 대이동 원칙 — 다음 슬라이스): 인코딩 폴백 5중 분열 → SourceTextCodec 통합
  (+모듈 parseFile UTF-8 고정 해소, 감사1 결함②와 동일 건), ANTLR 배선 5중 복제 →
  AntlrParseHarness, locator lineOf/fileUnit·affinity 스캔 공용화, registry/selection 이중
  구축 단방향화, recover()/enterDeclaration/parseSingleFile 분해, 규칙·엔진 후보 탈락 무기록
  2곳 evidence화, C_KEYWORDS 오명명 분리, listener '무엇' 주석 정리, writer 오버로드 제거.
  (전부 행위 변경 또는 광범위 이동이라 기준선 잡고 별도 슬라이스로.)
- 처리 후 재검증: suite 107 green, corpus 상태·AST 수 회귀 0, 원본 불변.

## 16. 원칙 잔여 제로화 (2026-07-22 심야) — **유보 13건 전부 구현 완료**

§15의 유보 목록을 전부 해소: SourceTextCodec 단일 디코딩(UTF-8→EUC-KR→MS949, lossy 플래그)
+ 4개 언어 모듈 UTF-8 고정 해소(fromString 전환), AntlrParseHarness로 5개 모듈 배선 공용화,
UnitBoundaries/AffinityMarkers 공용화, registry→selection 단방향화, recover()/enterDeclaration/
parseSingleFile/runEngineWaves/enterExpr_stmt 분해, 후보 탈락 무기록 2곳 evidence화,
C_KEYWORDS/COMMON_MEMBER_NAMES 분리, Oracle rebase 사본 제거, writer 오버로드 제거,
'무엇' 주석·빈 오버라이드 정리. 독립 재검증: suite 107 green, corpus 상태·AST inventory
해시 **바이트 동일**(회귀 0), 원본 불변, 변이 결정론 벤치 동일(오답 0). 주의: 이 corpus에는
UTF-8 strict가 실패하는 EUC-KR 파일이 없어 코덱의 한글 복원 효과는 단위 경로로만 검증됨
(실물 EUC-KR corpus 확보 시 재확인 권장).

## REVIEW_REQUIRED (설계상 올바른 미해결)

- **AMS 10 unresolved units** — 원인 실물 확인: 덤프의 줄바꿈 손상(주석이 줄 중간에서
  분할되어 `-` + 개행 + `- …`로 깨짐, `ETL_JOB_LOGSET JOBRESULT`처럼 토큰 접합).
  단일 토큰 수리 범위 밖이고 주석 복원은 토큰 삭제(의미 판단)라 자동 채택 금지 대상.
  Qwen3.6(30 calls ×2 runs)과 GPT-5.4-mini(30 calls) 모두 채택 0 — GPT-5.4-mini는 30/30
  모호 선언. 스펙의 목표 정의("억지 채택이 아니라 정직한 review")에 부합.

## 실패함

- 없음 (세션 시작 시 실패 3건은 T002로 해결).

## 미검증 (한계 명시)

- **유닛 루프 자체의 병렬화** — 5개 언어 모듈의 parseUnit 동시 호출 스레드 안전성이
  미실증이라 의도적으로 유보. 동시성은 Agent 요청 계층(세마포어+벤치마크)에서만 실증.
- **Analyzer 실연동 shadow run** — Parser 출력 계약은 baseline 대조로 검증했으나, Analyzer를
  실제 기동해 해석 불변까지 재확인하는 E2E는 이번 범위에서 실행하지 않음.
- **PostgreSQL 실물 corpus** — 분석대상모음에 PostgreSQL 소스 파일이 0개(기존 실행도 동일).
  golden fixture 계약 테스트로만 검증됨.
- **grammar 승격 A/B·rollback 리허설** — 절차는 스펙에 고정했으나 실연습 미실행.

## Artifacts

| 항목 | 위치 |
|---|---|
| 결정론 전체 corpus 보고 | target/corpus-reports/full-corpus-deterministic-012-final.json |
| AMS live (Qwen, slice) | target/corpus-reports/ams-live-agent-012.json |
| AMS live (Qwen, 재정박) | target/corpus-reports/ams-live-agent-012b-reanchor.json |
| AMS live (GPT-5.4-mini) | target/corpus-reports/ams-live-gpt54mini-012c.json |
| GPU 동시성 | target/corpus-reports/gpu-concurrency-benchmark.json |
| 변이 벤치마크(결정론/GPU) | target/corpus-reports/mutation-deterministic.json / mutation-gpu-agent.json |
| 가드 적용 후 corpus 회귀 확인 | target/corpus-reports/full-corpus-deterministic-012-guarded.json |
| AMS repair sidecar | (정리됨 — Reproduce의 AMS live 명령 재실행 시 target/test-data/repairs/ 에 재생성) |
| baseline AST (audit) | (정리됨 — .audit clone에 corpus를 .audit/data/source 로 복사 후 AntlrAnalysisTest 실행 시 .audit/data/analysis 에 재생성) |

## Reproduce

```
.\mvnw.cmd test
.\mvnw.cmd -Dtest=FullCorpusRecoveryTest -Dparser.full.corpus=D:\work\robo\분석대상모음 test
.\mvnw.cmd -Dtest=FullCorpusRecoveryTest -Dparser.full.corpus=D:\work\robo\분석대상모음\AMS ^
  -Dparser.repair.agent.enabled=true -Dparser.repair.agent.api.base=<base> ^
  -Dparser.repair.agent.model=<model> -Dparser.repair.agent.api.key=<key> test
.\mvnw.cmd -Dtest=GpuConcurrencyBenchmarkTest -Dparser.live.agent.api.base=<base> ^
  -Dparser.live.agent.model=<model> -Dparser.live.agent.api.key=<key> test
```
