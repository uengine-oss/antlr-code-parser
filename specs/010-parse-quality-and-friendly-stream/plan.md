# Implementation Plan

## Boundaries

- Parser owns parsing, recovery, quality evidence, sidecars, and NDJSON events.
- Analyzer remains an unchanged consumer and is used only for read-only compatibility checks.
- Operational `project/data` is not a test target. All runs use a unique root below `target/` or a
  test temporary directory.

## Design

1. Replace the vague two-string stream callback with `api/stream/ParseEventSink` and the immutable
   `ParseStreamEvent` wire contract.
2. Keep `type/content` while adding optional structured progress fields.
3. Have `ParseOrchestrator` emit named lifecycle events and quality-aware file results.
4. Return anti-buffering headers from the parsing endpoint and serialize one event per NDJSON line.
5. Make repair-promotion output optional when there are no candidates.
6. Add isolated shopmall quality verification against the maintained answer key and isolated
   malformed-fixture semantic comparisons.
7. Run focused tests, the full Parser suite, normal shopmall verification, and residue audit.

## Frozen Contracts

- Node JSON is not edited.
- Generated ANTLR code and AST listener semantics are not edited for stream work.
- Existing `detected`, `message`, `error`, `quality-summary`, and `complete` event types remain
  recognizable; new fields are additive.

