# Implementation Plan: Live GPU Repair Agent

1. Add an opt-in chat-template thinking flag to `StructuredRepairAgent`; omit the provider-
   specific field when it is not configured.
2. Strengthen `AgentTextEdit` with `expectedText` and validate it against `sourceExcerpt`.
3. Add line-start offsets to `FailureEnvelope` and carry prior edits plus diagnostics through
   `PriorAttempt`.
4. Update the forced tool schema and system prompt to state zero-based, end-exclusive offset
   rules and retry behavior.
5. Update deterministic tests, then run the full suite.
6. Run the conditional live test against the supplied SGLang GPU configuration and retain only
   the durable verification report, not temporary source/run directories.
7. Update README, the specification index, and the meeting report.

## Rollback

The new provider field is opt-in and the AST contract is untouched. Rollback consists of
removing the new envelope/edit evidence fields and optional request field; exact parsing is
independent of them.

