# Implementation Plan: Unified Intake & Content-Based Classification

**Branch**: `006-unified-intake-classification` | **Date**: 2026-06-15 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `specs/006-unified-intake-classification/spec.md`

## Summary

파서를 분석의 **유일한 입구**로 만든다. 업로드/경로 두 모드를 모두 받아, 결과는 항상 표준 `data/{source, ddl, analysis}`로 수렴한다. 파일의 표 정의(DDL) vs source 분류는 **파일명/경로가 아니라 내용**으로 한다(`CREATE TABLE/VIEW/INDEX/SEQUENCE` → ddl, `FUNCTION/PROCEDURE/PACKAGE/TRIGGER`·기타 → source). 경로 모드는 로컬 파일을 **하드링크 우선→복사 폴백**으로 `data/`에 반입한다. 한 파일이 파싱/분류 불가여도 전체를 중단하지 않고 그 파일만 격리·보고한다. AST 산출(`data/analysis`)과 언어 문법은 불변.

기술 접근: 입구 직전에 **경량 statement 스캐너**(각 top-level SQL statement의 선행 키워드를 토큰 단위로 식별)로 분류한다 — 전체 파싱 성공 여부와 독립적이라, antlr가 못 읽는 프로시저도 올바르게 source로 보낸다(이게 현재 크래시의 근본 해결).

## Technical Context

**Language/Version**: Java 17 (Temurin)

**Primary Dependencies**: Spring Boot (Web, Tomcat), ANTLR4 생성 문법(C/Java/Oracle/PostgreSQL/Python), Jackson(`Node.toJson`)

**Storage**: 파일시스템 — `data/{source, ddl, analysis}` (`FileStorageService`가 경로 resolve)

**Testing**: JUnit (Maven `src/test`, `mvn test`)

**Target Platform**: JVM 서버(포트 8081), 컨테이너 배포 가능(`uhnpybara/antlr` 이미지)

**Project Type**: 코드 파서 서비스(단일 모듈)

**Performance Goals**: 분류는 입구에 무시 가능한 오버헤드만 추가(경량 키워드 스캔, 파일당 선두 일부만 검사). 파싱 처리량은 기존 유지.

**Constraints**: 한 파일 실패가 전체 run을 중단시키면 안 됨(FR-006); 같은 볼륨에서 경로 반입은 전체 바이트 중복 금지(하드링크, FR-007); AST 노드 스키마·언어 문법 불변(헌법 VI).

**Scale/Scope**: 레거시 코드베이스, 수천 파일 규모. 단일 작업 세트(run당 `data/` 하나, 전량 리셋).

## Constitution Check

*GATE: Phase 0 전 통과 필수. Phase 1 후 재확인.*

헌법 v1.1.0 기준 (antlr `.specify/memory/constitution.md`):

| 원칙 | 판정 | 근거 |
|---|---|---|
| I. Language Strategy Auto-Discovery (분기 0 언어) | ✅ PASS | 분류는 언어 전략과 별개 관심사. 중앙 switch 추가 없음, 전략 레지스트리 불변. |
| II. Automatic Detection over Client Metadata | ✅ 강화 | 분류를 "바이트(내용) 기반"으로 — II의 "바이트가 진실"을 DDL/source 분류로 확장. |
| III. Content-Based Classification (내용=분류) **v1.1.0** | ✅ PASS | 이 기능이 III(개정본)을 구현. (개정은 본 plan에서 처리 완료 — 아래 Note.) |
| IV. Replace-All, Stateless | ✅ PASS | 매 입구 `data/` 전량 리셋 유지(FR-008). |
| V. Streaming-First | ✅ PASS | 진행·입구 보고를 NDJSON 이벤트로(기존 패턴); AST는 `analysis/` 사이드파일. |
| VI. AST JSON Stable Contract | ✅ PASS | `data/analysis` 노드 스키마 불변(FR-009). |

**Constitution amendment (완료)**: 원칙 III을 경로 기반 → 내용 기반으로 개정(**v1.0.0 → v1.1.0**, 근거는 III 본문에 기록). MINOR bump(기존 원칙 재정의, 원칙 삭제 없음). 다운스트림 영향: analyzer(스펙 014)·frontend(스펙 014)에 cross-service로 명시됨.

**GATE 결과: PASS** (위반 없음 → Complexity Tracking 불필요).

## Project Structure

### Documentation (this feature)

```text
specs/006-unified-intake-classification/
├── plan.md              # 이 파일
├── research.md          # Phase 0 (분류 방식·하드링크·섞인파일·실패격리 결정)
├── data-model.md        # Phase 1 (입구 엔티티)
├── quickstart.md        # Phase 1 (검증 시나리오)
├── contracts/           # Phase 1 (입구 엔드포인트·data/ 레이아웃·보고 이벤트 계약)
└── tasks.md             # Phase 2 (/speckit-tasks — 이 명령이 만들지 않음)
```

### Source Code (repository root)

```text
src/main/java/legacymodernizer/parser/
├── controller/
│   └── FileUploadController.java     # /fileUpload, /parsing — 경로 모드 입력에 "범위" 수용 추가
├── service/
│   ├── FileStorageService.java       # data/{source,ddl,analysis} resolve·전량리셋 — 내용기반 분류 + 경로반입(하드링크) 추가
│   ├── (신규) IntakeClassifier.java   # 경량 statement 스캐너: 표정의 vs 프로시저 판정(내용)
│   ├── (신규) SourceMaterializer.java # 경로모드: 선택범위를 data/로 하드링크→복사 폴백 반입
│   └── ParsingOrchestrator.java      # pathMode 분기 제거 → 항상 data/source 기준; 실패격리·보고
├── strategy/                         # 언어 전략(불변)
└── model/                            # Node 등(불변)

src/test/java/legacymodernizer/parser/
├── service/IntakeClassifierTest.java # 내용 분류 단위테스트(표/함수/혼합/미상)
├── service/SourceMaterializerTest.java
└── integration/UnifiedIntakeIT.java  # 업로드·경로 두 모드 → 동일 data/ 산출 검증
```

**Structure Decision**: 기존 단일 모듈 레이아웃 유지. 신규 책임 2개(분류 `IntakeClassifier`, 반입 `SourceMaterializer`)를 `service/`에 응집 — 컨트롤러·전략·모델은 최소 변경. `ParsingOrchestrator`의 `pathMode` 분기 제거가 핵심 정리(DRY).

## Complexity Tracking

> Constitution Check 위반 없음 → 해당 없음.
