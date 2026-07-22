# Contract: Parser Repair Proposal v1

## Ownership

- Parser builds `FailureEnvelope`, calls the adapter, validates edits, reparses, and decides adoption.
- The configured Repair Agent returns only a typed `PatchProposal`; it has no AST authority.
- Parser never modifies the original source.

## Request

`FailureEnvelope` includes only one failed minimal unit and precise evidence. The envelope checksum binds any proposal to that exact evidence. Source outside the unit is excluded except for a small read-only context header declared by the language module.

## Response

```json
{
  "schemaVersion": "1.0.0",
  "failureEnvelopeHash": "...",
  "edits": [
    {"startOffset": 10, "endOffset": 10, "replacement": " ", "reason": "..."}
  ],
  "rationale": "...",
  "confidence": 0.0,
  "ambiguities": []
}
```

Offsets are relative to the failed unit. Edits must be ordered, non-overlapping, within bounds, and under configured line/character limits.

## Mandatory rejection

- Envelope checksum mismatch.
- More than three attempts for a unit.
- Edit outside the failed unit or overlapping edits.
- Complete file/unit rewrite beyond configured minimality limits.
- Any AST/tree/Node JSON payload.
- Parser quality tuple does not improve.
- Exact sibling unit changes or coverage decreases.
- Original coordinate source map is incomplete.
- Proposal declares ambiguity or validation yields more than one plausible result.

## Operational failure

Timeout, network failure, budget exhaustion, invalid structured output, or missing submit tool is recorded and returns control to Parser as `REVIEW_REQUIRED`; it never becomes exact success.
