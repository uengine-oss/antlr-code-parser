# Implementation Plan: 호출 인자 구조 증거

## 1. 데이터 흐름

```text
source bytes
  -> language grammar argument context
  -> CallArgumentEvidenceCandidate
  -> common evidence 2.1.0 call.payload.arguments
  -> Analyzer strict decoder/projection
  -> runtime wrapper contract
  -> existing language semantic adapter
  -> exact-once internal/external/unresolved outcome
```

Parser는 구조만 생산하고 Analyzer가 wrapper 의미와 최종 binding을 소유한다.

## 2. 구현 순서

1. 보호 중인 Parser WIP와 Analyzer 2.0 decoder/consumer를 전수 대조한다.
2. `2.1.0 arguments` wire와 `2.0.0` replay 호환을 문서·contract test로 고정한다.
3. Parser 공통 candidate/sealer와 C grammar-owned literal/identifier 추출을 정합한다.
4. Analyzer decoder/projection/context에 구조 인자를 손실 없이 전달한다.
5. wrapper 계약을 이름 집합이 아니라 `wrapper + callee_from` exact map으로 검증한다.
6. 유효 target만 기존 language adapter에 넘기고, 부족/불일치는 명시 상태로 닫는다.
7. 양 저장소 집중·전체 회귀와 실제 12건 deterministic replay를 수행한다.

## 3. Constitution Check

- Parser VI: 버전이 붙은 AST JSON 변경이며 Analyzer 양끝을 같은 slice에서 검증한다.
- Analyzer II/VIII: Parser는 문법 사실, Analyzer adapter/core는 binding을 소유한다.
- Analyzer III: wrapper occurrence마다 한 결과를 보존하고 partial upload는 external이다.
- Analyzer XII: `argumentRanges`에 숨은 값을 싣지 않고 `arguments`라는 한 이름으로 표현한다.

## 4. WIP 보호

시작 시 존재한 Parser의 7개 호출 인자 관련 변경은 출처 미확정 사용자 WIP로 보존한다. 같은 책임의
검증된 계약으로 정합하되 unrelated 파일을 reset/checkout/clean하지 않고, diff를 전부 재검토한 뒤
허용 목록만 stage한다.
