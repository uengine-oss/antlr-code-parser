# Shared server data root

## Goal

브라우저 업로드 서버 모드에서 Parser와 Analyzer가 Workspace가 지정한 동일한
`ROBO_DATA_DIR`를 사용한다. 테스트의 `parser.data.root`와 CLI의 명시적 data root는 보존한다.

## Requirements

- `parser.data.root` system property는 테스트 격리를 위해 최우선이다.
- 그 다음 `ROBO_DATA_DIR`를 shared server workspace로 사용한다.
- 명시된 `ROBO_DATA_DIR`가 빈 값이면 추측 폴백하지 않고 실패한다.
- shared root가 없을 때만 기존 Docker context와 sibling `data` 기본값을 사용한다.
- 업로드·파싱은 같은 resolved root의 source·ddl·analysis를 사용한다.
