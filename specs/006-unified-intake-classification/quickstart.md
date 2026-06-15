# Quickstart: Unified Intake & Content-Based Classification 검증

이 기능이 끝까지 동작함을 증명하는 실행 시나리오. (구현 코드 아님 — 검증 가이드.)

## 전제

- antlr 서비스 기동(포트 8081): `mvn spring-boot:run` (또는 컨테이너).
- 테스트 픽스처: 표 정의 `.sql`(CREATE TABLE)과 프로시저 `.sql`(CREATE FUNCTION/PROCEDURE)이 **섞인** 로컬 폴더. (실측 케이스: `D:\다운로드\rwis` — 표 16 + 함수/프로시저 9, 한 폴더 혼재.)

## 시나리오 1 — 경로 모드, 프로시저 포함 폴더가 중단 없이 완료 (SC-002, US2)

```
POST /antlr/parsing   { "project_root": "<rwis 경로>" }
```
- 기대: NDJSON에 `message` 흐름 → `complete`. **`error`로 중단되지 않음**(과거: 함수 파일에서 ParseError로 전체 중단).
- 확인: `data/ddl`에 표 정의만 존재(함수/프로시저 파일 없음), `data/source`에 원본 전부, `data/analysis`에 AST 생성.

## 시나리오 2 — 내용 기반 분류 정확성 (SC-003, US2)

- 함수 파일(`FN_*.sql`, `CREATE FUNCTION ... RETURN ... IS BEGIN ...`)을 넣고 입구 실행.
- 확인: 그 파일이 **`data/source`에만** 나타나고 `data/ddl`에는 없다. 폴더에 `ddl/` 규칙이 없어도 동일.

## 시나리오 3 — 두 모드 동일 산출 (SC-001, US1)

- 같은 파일 집합을 (a) `/fileUpload`로 업로드, (b) `/parsing` `project_root`로 경로 모드 실행.
- 확인: 두 경우의 `data/{source, ddl, analysis}` 내용이 동일.

## 시나리오 4 — 한 파일 실패 격리 (SC-004, US4)

- 정상 파일들 사이에 깨진 `.sql`(파스 불가) 하나를 섞어 입구 실행.
- 확인: `complete`로 완료. 스트림에 `skipped {file, stage, reason}` 1건. 정상 파일은 모두 `data/`에 처리됨.

## 시나리오 5 — 섞인 파일 (SC-007, US5)

- `CREATE TABLE` + `CREATE PROCEDURE`가 한 파일에 든 `.sql` 입구 실행.
- 확인: 원본은 `data/source`에 보존, 표 정의는 `data/ddl`에 파생 산출. run 완료.

## 시나리오 6 — 경로 반입 비용 (SC-006, US3)

- `data/`와 같은 볼륨의 큰 폴더를 경로 모드로 실행.
- 확인: `complete` payload `materializedVia="hardlink"`, 디스크 사용량이 폴더 크기만큼 증가하지 않음. 다른 볼륨이면 `"copy"`로 폴백·정상 완료.

## 다운스트림 연계 확인 (참고)

- analyzer(스펙 014)를 `data/`만으로 실행 → 그래프 생성(이 입구 산출이 충분함을 입증). frontend(스펙 014) `scope`로 일부 제외 → `data/`에 그만큼만 반입됨.
