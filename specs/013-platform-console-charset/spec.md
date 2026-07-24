# Feature Specification: Platform-Aware Console Charset

**Feature Branch**: `main`

**Created**: 2026-07-24

**Status**: Approved

**Input**: Windows MS949 Maven/Surefire 실행에서 UTF-8 Logback 콘솔 바이트가 깨져 보이지만 UTF-8 JSON 산출물은 보존해야 한다.

## User Scenarios & Testing

### User Story 1 - 읽을 수 있는 콘솔 로그 (Priority: P1)

Windows와 UTF-8 터미널에서 파서 운영자가 파일 경로와 한국어 로그를 손상 없이 읽는다.

**Independent Test**: 한국어 경로가 포함된 Oracle 코퍼스를 Maven/Surefire로 실행하고 콘솔 및 Surefire XML에서 모지바케가 없는지 검사한다.

**Acceptance Scenarios**:

1. **Given** JVM 기본 문자셋이 MS949인 Windows 테스트 프로세스, **When** 파서가 한국어 파일 경로를 콘솔에 기록하면, **Then** 캡처된 로그에 원문 한국어가 유지된다.
2. **Given** UTF-8 환경, **When** 동일 로그를 출력하면, **Then** UTF-8 출력이 유지된다.

### User Story 2 - UTF-8 산출물 회귀 금지 (Priority: P1)

콘솔 문자셋을 환경에 맞춰도 AST, 진단, 수리 보고서는 계속 UTF-8로 저장된다.

**Independent Test**: 코퍼스 파싱 후 모든 JSON을 strict UTF-8과 JSON 파서로 읽고 원본 해시 불변을 확인한다.

## Edge Cases

- `logging.charset.console`을 명시한 배포 환경은 명시값이 최우선이어야 한다.
- JVM 기본 문자셋을 구할 수 없는 환경은 UTF-8로 폴백해야 한다.
- 파일 로그와 JSON 직렬화 문자셋은 콘솔 설정에 종속되면 안 된다.

## Requirements

### Functional Requirements

- **FR-001**: 콘솔 appender는 `logging.charset.console` 명시값을 우선 사용해야 한다.
- **FR-002**: 명시값이 없으면 현재 JVM의 `file.encoding`을 사용하고, 값이 없으면 UTF-8로 폴백해야 한다.
- **FR-003**: 파일/JSON 출력은 계속 UTF-8이어야 한다.
- **FR-004**: 고객명·코퍼스명·특정 한국어 문자열을 제품 설정에 하드코딩하면 안 된다.

## Success Criteria

- **SC-001**: Windows MS949 Maven/Surefire 코퍼스 실행 로그의 대표 한국어 경로가 원문과 일치한다.
- **SC-002**: 생성 JSON 전수가 strict UTF-8 및 JSON 검증을 통과한다.
- **SC-003**: 전체 Maven 테스트가 통과한다.

## Assumptions

- Logback의 `charset`은 JVM이 캡처하는 콘솔 문자셋과 일치해야 한다.
- 파일 기반 산출물의 기존 `StandardCharsets.UTF_8` 계약은 변경하지 않는다.
