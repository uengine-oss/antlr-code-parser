# Expression call evidence

## Intent and scope

The integrated playground needs complete structural calls before Analyzer binding,
context, rules and examples can be trusted. User authorization is recorded in
`../../../../analyzer/specs/003-integrated-playground/spec.md`.
Only this playground clone changes; product originals and Architect are excluded.

## Evidence and plan

`JavaAstListener.enterMethodInvocation` handles statement grammar calls. Java20
also embeds calls in `primaryNoNewArray` and `pNNA`. The real Staffing.java intake
reported exact parsing but omitted the return-expression call. Fix extraction in
the grammar adapter, never by reparsing source text in Analyzer.

Preserve the evidence schema and legacy AST. Emit each invocation once, with the
invocation's own closing parenthesis as its end (excluding subsequent chain tails).
For chained invocations preserve the preceding primary expression as receiver.
Method references, parenthesized values and field/array access are not calls.

## Acceptance and tasks

- Reproduce missing return/nested/initializer calls in a real Java module test.
- Test chained calls, receiver and argument ranges, constructor coexistence,
  statement calls and non-call expressions, including Unicode before offsets.
- Implement grammar-owned extraction and run focused evidence regression tests.
- Rebuild the isolated parser, repeat HTTP intake and inspect emitted JSON.
- Connect Analyzer consumer separately; parser tests alone do not prove integration.

## Constitution check

Language routing, intake classification, upload lifecycle, NDJSON and JSON schema
remain unchanged. Only evidence population is corrected. Existing product results
are not migrated; playground inputs must be reparsed before downstream analysis.

## Status

Implemented grammar-owned expression-call evidence in JavaAstListener. Focused
regression failed before the fix (expected 8 calls, actual 2), then passed with
exact invocation, receiver and argument ranges. SemanticEvidenceIrContractTest:
24 tests passed, zero failures/errors/skips. No claim of full parser-suite coverage.

Real HTTP intake after isolated restart: Staffing.java produced both constructor
and return-expression call with exact source slices. Receipt:
`../_runs/parser-intake/call-evidence-check.json` relative to service clone root.
Analyzer consumption and business semantics remain unverified.
