# Verification Report: Live GPU Repair Agent

**Date**: 2026-07-22  
**Result**: PASS

## What was actually verified

The Parser directly called the supplied internal SGLang endpoint with model
`frentis-ai-model`. No fake provider or Analyzer execution path was used for the final proof.
The fixture was synthetic Oracle PL/SQL and contained the invalid table alias form
`FROM APP_TABLE AS A`.

The accepted run proved this sequence:

1. Initial ANTLR parse rejected the unit.
2. The Parser sent one bounded `FailureEnvelope` to the real GPU model.
3. The model returned one forced `submit_parser_repair` tool call.
4. The Parser verified the envelope hash, exact `expectedText`, offsets, edit bounds, and
   line-count preservation.
5. The Parser applied the edit to an in-memory working copy only.
6. ANTLR reparsed the candidate and the quality gate accepted it.
7. Final status was `RECOVERED_VALIDATED`; the original source SHA-256 remained
   `9ae537bbc8120cd876da6bbbac5cb378ffeec8334b7165adf43a9cf335b3ab6f`.
8. The audit recorded `agentAttempts=1` and `accepted=true`.

## Failures found before the passing run

| Observation | Root cause | Resolution |
|---|---|---|
| `REPAIR_AGENT_MISSING_SUBMIT` | Qwen thinking consumed the 2,048-token response budget before the forced tool result | opt-in `chat_template_kwargs.enable_thinking=false` |
| Correct explanation but wrong edit offset | the envelope exposed the source but not exact neighboring token coordinates | add `diagnosticWindowTokens` and line-start offsets |
| Repeated rejected edit | prior Agent edits and their resulting diagnostics were not fully returned on retry | carry proposed edits, resulting diagnostics, and validation reasons |
| Huge/no-op tool fields | the tool schema did not declare field lengths and no-op edits were not rejected early | schema bounds plus `AGENT_NO_OP_EDIT` |
| Non-deterministic local sampling | the Parser did not forward the internal SGLang `top_k=1` setting | opt-in `PARSER_REPAIR_AGENT_TOP_K=1` |

All failed proposals were rejected as `REVIEW_REQUIRED`; no failed run changed the original
fixture or emitted an accepted AST.

## Regression evidence

- Parser suites: **27**.
- Tests: **70 total, 67 passed, 3 path-conditioned, 0 failures, 0 errors**.
- The two shopmall real-path tests were rerun after the clean suite and passed.
- The complete `RepairAgentRecoveryTest` class was rerun with the real GPU configuration;
  all six methods passed, including the live method.
- The three remaining conditional corpus tests are the same large Oracle/PostgreSQL/full-corpus
  path gates already executed and reported by SDD 009.
- AST Node JSON production code and its frozen output contract were not changed by SDD 011.
- Analyzer tracked changes from this work: **0**.

## Runtime settings used

- `PARSER_REPAIR_AGENT_ENABLED=true`
- `PARSER_REPAIR_AGENT_API_BASE=<internal OpenAI-compatible SGLang /v1 endpoint>`
- `PARSER_REPAIR_AGENT_MODEL=frentis-ai-model`
- `PARSER_REPAIR_AGENT_THINKING_ENABLED=false`
- `PARSER_REPAIR_AGENT_TOP_K=1`
- `PARSER_REPAIR_AGENT_TIMEOUT_SECONDS=600`

The API key was read from the existing local secret configuration for the test and was not
written to source, reports, logs, or this specification.

