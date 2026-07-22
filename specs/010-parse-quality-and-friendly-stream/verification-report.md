# Verification Report: Shopmall Parse Quality and Friendly Stream

**Date**: 2026-07-21  
**Scope**: `antlr-code-parser`; Analyzer read-only/unchanged  
**Test root**: `target/test-data` only

## Verdict

The current shopping-mall C domain parses correctly at the maintained AST contract level. The
normal run is 12/12 EXACT, all 124 expected functions are represented exactly once with matching
source ranges, all expected calls are present, and IF/LOOP/SWITCH/CASE counts match. An isolated,
deliberately malformed copy of the real `payment/promotion.c` recovered through alternate C
preprocessor context and produced the same function/call/control semantic projection as its exact
baseline. Neither the operational original nor the malformed test copy changed.

## Normal shopmall evidence

| Measure | Result |
|---|---:|
| C/H inputs | 12 |
| EXACT diagnostics | 12 / 12 |
| Valid AST JSON | 12 / 12 |
| AST nodes | 5,275 |
| Expected functions | 124 |
| Actual unique functions | 124 |
| Missing / duplicate functions | 0 / 0 |
| File or start/end-line mismatches | 0 |
| Missing expected calls | 0 |
| IF/LOOP/SWITCH/CASE mismatches | 0 |
| Source hash changes | 0 |
| Exact-run `.repair.json` files | 0 |
| Empty promotion reports | 0 |

The frozen C AST vocabulary represents both `for` and `while` as `LOOP`; therefore the answer-key
`for + while` total is compared with AST `LOOP`. No Node type or AST serialization was changed.

## Malformed-copy recovery evidence

1. Copy the real `payment/promotion.c` into the isolated test root.
2. Add a deliberately invalid active vendor/preprocessor branch and a valid alternate branch near
   `calc_discount`; create a line-aligned exact baseline copy.
3. Confirm first-pass quality is rejected.
4. Recover only the failed C function through `c.alternate-preprocessor-branches.v1`.
5. Confirm final status is `RECOVERED_VALIDATED`.
6. Compare ordered semantic projections of FUNCTION, FUNCTION_CALL, IF, LOOP, SWITCH, CASE, and
   ELSE nodes with the exact baseline: equal.
7. Confirm original and malformed-copy SHA-256 values are unchanged.

An arbitrary broken statement that cannot be proven equivalent is still PARTIAL,
REVIEW_REQUIRED, UNRESOLVED, or FAILED; it is not mislabeled as exact success.

## Stream evidence

- Typed contract: `api/stream/ParseStreamEvent` and `ParseEventSink`.
- Legacy `type/content` retained; structured lifecycle/progress/quality fields are additive.
- Relative-path processing order is deterministic.
- Per-file result distinguishes EXACT, recovered, partial, review-required, unresolved, and failed.
- Final human message reports all quality buckets, AST count, and line count.
- HTTP response includes `Cache-Control: no-cache, no-transform` and
  `X-Accel-Buffering: no`.
- Integration test confirms ordered NDJSON and exactly one terminal `complete` event.

## Regression and isolation

- Clean full Parser suite: **68 tests, 64 passed, 4 conditional, 0 failures, 0 errors**.
- The two path-gated shopmall tests were then rerun with real paths and passed, leaving the suite
  inventory at **68 tests, 4 remaining conditional**.
- Analyzer tracked changes caused by this work: **0**. Its only status entry remains the pre-existing
  untracked `run-output/` directory.
- Listening Parser test/service processes after verification: **0**.
- Custom `isolated-shopmall-*` and `shopmall-live-*` residue after cleanup: **0**.

## Repair artifact decision

`diagnostics/*.parse.json` remains the always-on quality evidence. A file-level
`repairs/*.repair.json` is written only when recovery units were actually attempted. The recurring
promotion report is written and streamed only when it contains at least one reviewable candidate.
Exact runs therefore do not create misleading repair files.

