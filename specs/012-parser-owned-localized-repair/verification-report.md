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
| AMS repair sidecar | target/test-data/repairs/AMS_procedures.sql.repair.json |
| baseline AST (audit) | D:\work\robo\.audit\data\analysis\ |

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
