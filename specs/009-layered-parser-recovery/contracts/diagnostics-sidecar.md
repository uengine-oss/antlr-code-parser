# Contract: Parse Diagnostics Sidecar v1.1

## Location

For source `<data-root>/source/a/b/Foo.sql`, write:

`<data-root>/diagnostics/a/b/Foo.sql.parse.json`

The source extension remains in the sidecar name to avoid collisions. The AST remains `<data-root>/analysis/a/b/Foo.json`.

## Required fields

```json
{
  "schemaVersion": "1.1.0",
  "sourcePath": "a/b/Foo.sql",
  "language": "oracle",
  "sourceSha256": "...",
  "grammarRevision": "...",
  "status": "EXACT",
  "firstPass": {},
  "units": [],
  "summary": {
    "lexerErrors": 0,
    "parserErrors": 0,
    "antlrRecoveries": 0,
    "declarationsDiscovered": 0,
    "declarationsEmitted": 0,
    "agentAttempts": 0,
    "elapsedMillis": 0,
    "processingElapsedMillis": 0
  }
}
```

Diagnostics and arrays are deterministically ordered by original offset, phase, code, and attempt. Messages are evidence, not AST semantics.

`elapsedMillis` measures the first ANTLR parse. `processingElapsedMillis` measures the
Parser-owned parse, quality gate, and recovery decision before sidecar/audit file I/O. Their
difference is the recovery-layer overhead used by the full-corpus performance report.

## Status rules

- `EXACT`: first pass meets every gate and no edits occurred.
- `RECOVERED_SAFE`: deterministic reviewed-safe transformation passed all gates.
- `RECOVERED_VALIDATED`: isolated/context/Agent candidate passed all gates.
- `REVIEW_REQUIRED`: a plausible but ambiguous candidate exists or Agent is unavailable.
- `PARTIAL`: accepted independent units exist while other units remain unresolved.
- `UNRESOLVED`: bounded unit failed all allowed attempts.
- `FAILED`: system, I/O, catalog, or unbounded failure prevents a trustworthy result.

No status creates or changes a Node JSON node.
