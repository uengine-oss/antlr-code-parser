# Feature Specification: Health Check, CORS & Web Configuration

**Feature Branch**: `005-health-cors-config`

**Created**: 2026-06-15

**Status**: Backfilled (reverse-engineered)

**Input**: Reverse-engineered from existing code — `HealthCheckController.java` (`GET /`), `WebConfig.java` (CORS mappings + `GlobalExceptionHandler`), `ParserApplication.java`, and deployment artifacts (`docker-compose.yml`, `Dockerfile`) exposing port 8081. 모든 요청이 통과하는 cross-cutting infra 관심사.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - 서비스 생존 확인 (Health Check) (Priority: P1)

운영자 또는 상위 API Gateway가 antlr-code-parser 서비스가 살아있고 요청을 받을 준비가 되었는지 확인한다. 루트 경로(`GET /`)로 요청하면 즉시 `200 OK` 응답을 받는다.

**Why this priority**: 헬스 체크가 없으면 Gateway·오케스트레이터가 서비스 가용성을 판단할 수 없어, 죽은 인스턴스로 트래픽을 보내거나 배포 검증이 불가능하다. 가장 기초적인 운영 안전망이다.

**Independent Test**: 서비스를 띄운 뒤 `curl http://localhost:8081/` 를 호출해 `200` 과 본문 `OK` 가 반환되는지 확인하면 단독 검증된다.

**Acceptance Scenarios**:

1. **Given** 서비스가 정상 기동된 상태, **When** `GET /` 를 호출하면, **Then** HTTP `200` 과 본문 `"OK"` 를 반환한다.
2. **Given** 서비스가 기동되지 않은 상태, **When** `GET /` 를 호출하면, **Then** 연결 실패(응답 없음)로 비가용 상태가 드러난다.

---

### User Story 2 - 브라우저/Gateway 교차 출처 요청 허용 (CORS) & 일관된 오류 응답 (Priority: P2)

프런트엔드(다른 origin)나 API Gateway에서 보낸 교차 출처 요청이 차단되지 않고 처리되며, 처리 중 발생한 오류는 항상 일관된 JSON 형태로 반환된다.

**Why this priority**: P1 헬스 체크가 동작해야 의미가 있으므로 그다음 우선순위다. CORS가 막히면 브라우저 클라이언트가 호출 자체를 못 하고, 오류 포맷이 제각각이면 호출자가 실패를 안정적으로 해석할 수 없다.

**Independent Test**: 임의 origin 헤더를 붙여 preflight(`OPTIONS`)와 실제 요청을 보내 CORS 헤더가 응답에 포함되는지, 그리고 잘못된 요청을 보내 `{"detail": ...}` / `{"error": ...}` JSON 오류가 오는지 확인하면 단독 검증된다.

**Acceptance Scenarios**:

1. **Given** 다른 origin에서 온 요청, **When** 어떤 경로(`/**`)로든 요청하면, **Then** 모든 origin 패턴(`*`)·메서드(GET/POST/PUT/DELETE/OPTIONS/PATCH)·헤더가 허용된다.
2. **Given** 핸들러가 `ResponseStatusException` 을 던지는 상황, **When** 그 요청이 처리되면, **Then** 해당 상태 코드와 `{"detail": <사유>}` JSON 을 반환한다.
3. **Given** 업로드 파일이 너무 큰 상황(`MaxUploadSizeExceededException`), **When** 요청이 처리되면, **Then** `413 Payload Too Large` 와 파일 크기 초과 안내 메시지를 반환한다.

---

### Edge Cases

- 정의되지 않은 경로 호출 시: 매핑된 컨트롤러가 없으면 Spring 기본 처리 또는 `GlobalExceptionHandler`의 일반 `Exception` 핸들러로 떨어져 `500` + `{"detail": "Unexpected error: ..."}` 를 반환한다.
- `ResponseStatusException` 의 상태 코드가 표준 `HttpStatus` 로 해석되지 않을 때: `500 INTERNAL_SERVER_ERROR` 로 폴백한다.
- `MultipartException` 중 크기 초과가 아닌 경우: `400 Bad Request` + 업로드 처리 오류 메시지를 반환한다.
- 예외 메시지가 `null` 일 때: 빈 문자열로 치환해 NPE 없이 JSON을 구성한다.
- `allowCredentials(true)` 와 origin 패턴 `*` 조합: 실제 Origin을 echo하는 방식으로 동작한다(와일드카드 리터럴 미사용).

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: 시스템은 `GET /` 요청에 대해 HTTP `200` 과 본문 `"OK"` 를 반환하는 헬스 체크 엔드포인트를 제공해야 한다.
- **FR-002**: 시스템은 모든 경로(`/**`)에 대해 CORS 매핑을 적용해야 한다.
- **FR-003**: 시스템은 모든 origin 패턴(`allowedOriginPatterns("*")`)을 허용해야 한다. (CORS는 상위 API Gateway에서 통합 관리한다는 전제)
- **FR-004**: 시스템은 CORS 허용 메서드로 `GET, POST, PUT, DELETE, OPTIONS, PATCH` 를 허용해야 한다.
- **FR-005**: 시스템은 모든 요청 헤더(`allowedHeaders("*")`)와 노출 헤더(`exposedHeaders("*")`)를 허용하고, 자격 증명(`allowCredentials(true)`)을 허용하며, preflight 캐시(`maxAge`)를 3600초로 설정해야 한다.
- **FR-006**: 시스템은 전역 예외 처리(`@ControllerAdvice`)를 통해 모든 요청의 오류를 일관된 JSON 형태로 변환해야 한다.
- **FR-007**: 시스템은 `ResponseStatusException` 을 해당 상태 코드와 `{"detail": <사유 또는 메시지>}` 로 반환하고, 해석 불가한 상태 코드는 `500` 으로 폴백해야 한다.
- **FR-008**: 시스템은 `MaxUploadSizeExceededException` 을 `413 Payload Too Large` 와 파일 크기 초과 안내 메시지로 반환해야 한다.
- **FR-009**: 시스템은 그 외 `MultipartException` 을 `400 Bad Request` 와 업로드 오류 메시지로 반환해야 한다.
- **FR-010**: 시스템은 처리되지 않은 일반 `Exception` 을 `500` 과 `{"detail": "Unexpected error: ..."}` 로 반환해야 한다.
- **FR-011**: 시스템은 HTTP 포트 `8081` 에서 서비스를 노출해야 한다. (배포 시 docker 포트 매핑으로 지정)

### Key Entities *(include if feature involves data)*

- **헬스 체크 엔드포인트**: `GET /` — 의존성 점검 없이 단순 생존 신호(`200 OK` / `"OK"`)를 반환하는 진입점.
- **CORS 정책**: 허용 origin 패턴(`*`), 허용 메서드(GET/POST/PUT/DELETE/OPTIONS/PATCH), 허용·노출 헤더(`*`), 자격 증명 허용, preflight 캐시 3600초. 적용 범위 `/**`.
- **전역 예외 처리(GlobalExceptionHandler)**: 요청 처리 중 발생하는 예외를 상태 코드 + `{"detail"|"error": ...}` JSON 으로 표준화하는 cross-cutting 핸들러.
- **서비스 포트**: `8081` — 모든 외부 요청의 진입 포트. (코드 내 `application.yml` 미존재; 배포 아티팩트에서 지정)

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: `GET /` 호출의 100% 가 `200 OK` + 본문 `"OK"` 를 반환한다(서비스 기동 상태 기준).
- **SC-002**: 임의 origin에서 온 요청이 CORS로 인해 차단되지 않고 100% 처리된다.
- **SC-003**: 처리 중 발생한 모든 예외가 일관된 JSON(`detail` 또는 `error` 키)으로 반환되어, 호출자가 비정형 스택트레이스를 받지 않는다.
- **SC-004**: 파일 크기 초과 오류가 `413` 으로, 기타 멀티파트 오류가 `400` 으로 정확히 구분되어 반환된다.

## Assumptions

- CORS의 실제 정책(origin 화이트리스트 등)은 상위 **API Gateway**에서 통합 관리하며, 본 서비스는 의도적으로 모든 origin을 허용한다(코드 주석 명시).
- 포트 `8081` 은 소스 내 `application.yml`/`properties` 가 아니라 배포 아티팩트(`docker-compose.yml`, `Dockerfile EXPOSE`)에서 결정된다 — Spring 기본 포트 8080을 컨테이너 매핑으로 8081에 노출한다는 전제.
- 헬스 체크는 다운스트림 의존성(DB·파일시스템 등)을 점검하지 않는 단순 liveness 신호로 충분하다.
- 예외 메시지에 언급된 파일 크기 한도(최대 파일 100MB / 요청 500MB)는 안내 문구이며, 실제 강제 한도는 별도 설정에 의존한다(본 spec 범위 밖).
