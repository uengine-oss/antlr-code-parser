# Plan: Shared server data root

1. data-root 선택을 입력이 명시된 순수 resolver로 분리한다.
2. test root → `ROBO_DATA_DIR` → Docker context → sibling data 순서로 결정한다.
3. 각 우선순위와 blank fail-closed를 단위 테스트한다.
4. Workspace가 ANTLR과 Analyzer에 같은 절대경로를 주입하는 통합 계약과 연결한다.
5. 실제 쇼핑몰 upload→parse→analyze run에서 경로와 결과를 검증한다.
