# Contract: Unified Intake Endpoints & Data Layout

파서가 외부(프론트·analyzer)에 노출하는 경계. 변경/유지 항목을 명시.

## C1. `POST /antlr/fileUpload` (업로드 모드 — 기존, 호환 유지)

- 입력: multipart 파일들(상대경로명) + 선택적 metadata.
- 동작: 파일을 `data/`로 저장. **변경**: 저장 시 `data/source`/`data/ddl` 분류를 **파일명 `ddl/` 접두가 아니라 내용 기반**(R1)으로. `ddl/` 접두는 역호환 힌트로만 허용(내용이 우선).
- 응답: 기존 형태 유지(`files`/`ddlFiles`/`nontargetFiles`) + (추가) 분류는 내용 기준임을 반영.

## C2. `POST /antlr/parsing` (경로 모드 포함 — 변경)

- 입력(JSON):
  ```json
  {
    "project_root": "<로컬 폴더 절대경로>",   // path 모드
    "scope": ["rel/path/a.sql", "rel/dir/"],  // (신규) 포함 범위; 비면 전체
    "ddl_dir": "...", "target": "...", "..." : "(기존 메타, inert)"
  }
  ```
- 동작(변경):
  1. `project_root`가 있으면 **`scope`의 파일을 `data/source`로 반입**(하드링크→복사, R2). `pathMode` 제자리 읽기 **제거** — 항상 `data/` 기준.
  2. `data/`의 각 파일을 **내용 기반 분류**(R1) → `data/ddl`(표 정의, 섞인 파일은 파생)·`data/source`(원본).
  3. 각 파일 파스 → `data/analysis/<상대경로>.json`(노드 스키마 **불변**).
  4. 파일 실패는 격리·`skipped` 이벤트(R4), 전체 중단 없음.
- 출력(스트림, NDJSON — 헌법 V):
  - `message` (기존), `complete` (기존), `error` (입력 전체 무효 시),
  - **(신규)** `skipped` `{type:"skipped", file, stage, reason}`,
  - **(신규)** 요약은 `complete` payload에 `{ddlCount, sourceCount, skippedCount, materializedVia}` 포함.

## C3. Data Layout (다운스트림 계약 — analyzer가 의존)

```
data/source/   원본 1:1 (AST 입력)
data/ddl/      표 정의만 (analyzer sqlglot 입력)  ← 불변식: 프로시저 0
data/analysis/ AST JSON (노드 스키마 불변, 헌법 VI)
```

- **analyzer (스펙 014)**: 이 세 폴더만 소비. `project_root` 경로 모드 의존 제거.
- **frontend (스펙 014)**: `scope`(C2)로 선택 범위 전달.

## 호환성·불변

- AST 노드 스키마/`analysis/` 레이아웃 **불변**(헌법 VI) — analyzer step2 무영향.
- `/fileUpload`·`/parsing` 엔드포인트 시그니처는 **추가 호환**(신규 `scope`·`skipped`만 추가, 기존 필드 유지).
- 언어 문법·전략 레지스트리 **불변**(헌법 I).
