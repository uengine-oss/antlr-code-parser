# Antlr-Server API 스펙

## 서버 정보

| 항목 | 값 |
|---|---|
| Base URL | `http://localhost:8081` |
| 파일 크기 제한 | 3MB |

---

## 공통

### 헤더

| 헤더 | 필수 | 설명 |
|---|---|---|
| `Session-UUID` | ✅ | 세션 고유 식별자 |

### 성공/실패 구분

- **200 OK** → 성공
- **400 Bad Request** → 실패 (에러 메시지 포함)

---

## 저장 구조

```
data/{sessionUUID}/{projectName}/
├── ddl/                    ← DDL 파일
└── {systemName}/           ← 시스템별 폴더
    ├── src/                ← 소스 파일
    └── analysis/           ← 파싱 결과 JSON
```

---

## 1. 파일 업로드

```
POST /fileUpload
Content-Type: multipart/form-data
```

### Request

| 파트 | 타입 | 설명 |
|---|---|---|
| `metadata` | JSON string | 메타데이터 |
| `files` | File[] | 업로드 파일 |

**metadata 예시:**
```json
{
  "target": "oracle",
  "projectName": "은행시스템",
  "systems": [
    { "name": "계좌", "sp": ["입금.sql", "출금.sql"] },
    { "name": "고객", "sp": ["조회.sql"] }
  ],
  "ddl": ["테이블.sql"]
}
```

| 필드 | 필수 | 설명 |
|---|---|---|
| target | ✅ | `oracle`, `postgresql`, `java` |
| projectName | ✅ | 프로젝트명 |
| systems | ✅ | 시스템별 파일 목록 |
| systems[].name | ✅ | 시스템명 |
| systems[].sp | ✅ | 소스 파일명 배열 |
| ddl | ❌ | DDL 파일명 배열 |

### Response

**성공 (200):**
```json
{
  "projectName": "은행시스템",
  "systemFiles": [
    { "system": "계좌", "fileName": "입금.sql", "fileContent": "CREATE OR REPLACE..." },
    { "system": "계좌", "fileName": "출금.sql", "fileContent": "..." }
  ],
  "ddlFiles": [
    { "fileName": "테이블.sql", "fileContent": "CREATE TABLE..." }
  ]
}
```

**실패 (400):**
```json
{ "error": "에러 메시지" }
```

| 에러 | 원인 |
|---|---|
| 세션 정보 없음 | Session-UUID 헤더 누락 |
| metadata 형식 오류 | JSON 파싱 실패 |
| target 필수 | target 누락 |
| projectName 필수 | projectName 누락 |
| systems 필수 | systems 누락 |
| 파일 필수 | files 누락 |
| 유효한 파일 없음 | 모든 파일이 비어있음 |
| 지원하지 않는 target | 잘못된 target 값 |
| 파일 없음: {파일명} | metadata에 있는 파일이 files에 없음 |

---

## 2. 파싱 (ANTLR 분석)

```
POST /parsing
Content-Type: application/json
```

### Request

```json
{
  "target": "oracle",
  "projectName": "은행시스템",
  "systems": [
    { "name": "계좌", "sp": ["입금.sql", "출금.sql"] },
    { "name": "고객", "sp": ["조회.sql"] }
  ]
}
```

| 필드 | 필수 | 설명 |
|---|---|---|
| target | ✅ | `oracle`, `postgresql`, `java` |
| projectName | ✅ | 프로젝트명 |
| systems | ✅ | 파싱할 파일 목록 |

### Response

**성공 (200):**
```json
{
  "projectName": "은행시스템",
  "files": [
    { "system": "계좌", "fileName": "입금.sql", "analysisResult": "{...}" },
    { "system": "계좌", "fileName": "출금.sql", "analysisResult": "{...}" }
  ]
}
```

**실패 (400):**
```json
{ "error": "에러 메시지" }
```

| 에러 | 원인 |
|---|---|
| 세션 정보 없음 | Session-UUID 헤더 누락 |
| target 필수 | target 누락 |
| projectName 필수 | projectName 누락 |
| systems 필수 | systems 누락 |
| 지원하지 않는 target | 잘못된 target 값 |
| 파일 없음: {시스템}/{파일} | 업로드 안 된 파일 |
| 파싱 실패: {시스템}/{파일} | ANTLR 파싱 오류 |

---

## 지원 Target

| target | 설명 |
|---|---|
| oracle | Oracle PL/SQL |
| postgresql | PostgreSQL |
| java | Java |

---

## 흐름 예시

```
1. 프론트: Session-UUID 생성
       ↓
2. POST /fileUpload
   - 파일들 업로드
   - 서버가 저장
   - 파일 내용 반환
       ↓
3. POST /parsing  
   - 파싱 요청
   - 서버가 ANTLR 파싱
   - 분석 결과 반환
```
