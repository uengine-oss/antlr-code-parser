# Contract: Verified Source Application

## Configuration

```text
System property: parser.repair.apply.to.source
Environment:     PARSER_REPAIR_APPLY_TO_SOURCE
Default:         false
```

## Preconditions

All must hold:

1. Application is explicitly enabled.
2. Recovery has an accepted AST and status `RECOVERED_SAFE` or `RECOVERED_VALIDATED`.
3. Recovery contains a non-null repaired source produced by direct accepted edits.
4. Current source bytes match `originalFileSha256`.
5. Existing bytes decode without loss through `SourceTextCodec`.
6. Re-encoding the repaired text with that charset is possible.

## Effects

- Write only the resolved source file supplied to the parse operation.
- Use a unique sibling temporary file and replace the source atomically when supported.
- Preserve unrelated bytes semantically through same-charset encoding and minimal text edits.
- Return an explicit result and emit a source-repaired progress event.

## Failure behavior

- Disabled, absent repair, no-op, stale hash, lossy decode, invalid encoding, or non-accepted
  recovery leaves the source untouched.
- Temporary files are deleted on failed application.
- Application failure does not relabel a parse as exact; it is reported separately.
