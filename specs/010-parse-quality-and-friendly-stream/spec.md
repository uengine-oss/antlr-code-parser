# Feature Specification: Parse Quality Proof and Friendly Progress Stream

**Created**: 2026-07-21  
**Status**: Completed for stream/quality-gate scope — semantic-preservation claims narrowed by [012-parser-owned-localized-repair](../012-parser-owned-localized-repair/spec.md) (ShopmallRecoverySemanticTest is a limited projection comparison, not general semantic proof)  
**Scope**: `antlr-code-parser` only. Analyzer is read-only and unchanged.

## Goal

Prove that the existing shopping-mall C domain is represented faithfully in AST JSON, prove that
recovery preserves meaning on isolated malformed copies, and make the existing NDJSON progress
stream understandable without changing the frozen AST JSON contract.

## User Stories

### US1 — Trust the normal shopping-mall parse (P1)

An operator can parse the 12 C/H files in an isolated workspace and see evidence that every known
function, source range, control structure, and representative cross-function call is present in the
generated AST.

**Acceptance**

1. All 124 source-defined functions in the maintained shopmall answer key exist exactly once.
2. Function names and start/end lines match the source-derived answer key.
3. Representative calls and control-node counts are compared; every mismatch is reported, never
   hidden by a file-level success count.
4. Original source hashes do not change and all AST JSON files are valid.

### US2 — Prove recovery preserves meaning (P1)

An operator can run deliberately malformed copies in an isolated data root and compare accepted
recovery ASTs with the normal baseline by a semantic projection that ignores only the deliberately
damaged syntax.

**Acceptance**

1. Tests never write operational `project/data`.
2. Every fault records the exact failed range and diagnostic.
3. A recovered result is accepted only when expected declarations, names, ordering, calls, control
   nodes, and original coordinates remain equivalent to the baseline expectation.
4. If equivalence cannot be established, the result is review-required/unresolved and is not
   presented as successful.

### US3 — Understand parsing progress immediately (P1)

An operator sees friendly Korean messages and stable structured fields for preparation, detection,
per-file progress, exact/recovered/review-required results, and the final run summary.

**Acceptance**

1. Existing top-level `type` and `content` fields and the terminal `complete` event remain compatible.
2. Events add optional `schemaVersion`, `event`, `phase`, `status`, `current`, `total`, `percent`,
   `file`, `language`, `line`, `quality`, and `counts` fields.
3. Per-file completion names the quality outcome rather than saying only “complete”.
4. The final message reports exact, recovered, partial, review/unresolved/failed, AST file, and line
   counts in plain Korean.
5. NDJSON is emitted incrementally with anti-buffering response headers.

### US4 — Leave only useful evidence (P2)

Normal exact runs do not create misleading repair artifacts. A file-level repair audit exists only
when recovery units were attempted. A promotion-candidate report exists only when at least one
reviewable recurring candidate exists.

## Requirements

- **FR-001**: AST file names, paths, fields, field order, types, node vocabulary, hierarchy, child
  order, names, case, source coordinates, and deterministic bytes remain frozen.
- **FR-002**: Analyzer code and configuration MUST NOT be changed.
- **FR-003**: Legacy NDJSON consumers that read only `type/content` MUST continue to work.
- **FR-004**: Stream event construction MUST use a named Parser API contract, not ad-hoc maps or
  JSON strings in orchestration code.
- **FR-005**: Exact inputs MUST produce diagnostics but MUST NOT produce file repair audits.
- **FR-006**: Empty promotion reports MUST NOT be written or advertised in the stream.
- **FR-007**: All new quality tests MUST use isolated temporary data roots.
- **FR-008**: Source mutation count MUST be zero.

## Success Criteria

- **SC-001**: Shopmall function coverage is 124/124 with zero duplicate or missing function nodes.
- **SC-002**: Shopmall AST JSON validity is 12/12 and normal quality status is EXACT for 12/12.
- **SC-003**: Every accepted malformed-fixture recovery passes its declared semantic projection;
  every non-equivalent result is rejected or requires review.
- **SC-004**: Stream contract tests prove ordered run-start → detection → file events → run-summary
  → single complete, with zero error events for the normal shopmall run.
- **SC-005**: Parser regression suite and frozen AST golden tests remain green.
