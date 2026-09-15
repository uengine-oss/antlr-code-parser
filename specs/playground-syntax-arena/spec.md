# File-level grammar syntax arena

## Intent

The independently designed Analyzer needs body conditions, returns, exceptions,
calls and lexical scopes. Existing callable syntax contains declarations only.
User authorized whole-playground service redesign; product originals remain untouched.

## Design and impact

Add `evidence.syntaxArena` with schema `antlr-syntax-arena/v1`. Convert the already
parsed ANTLR tree once per file into an ordered flat arena: rule/token kind,
code-point range and ordered child IDs. Do not repeat source text or body trees
per callable. Shared conversion has no language-name switch. JavaLanguageModule
is the initial producer; other modules are explicitly not integrated yet.
Record parser/lexer ATN fingerprint; this identifies grammar automata, not the
entire frontend implementation. Existing facts remain version 2.1.0; the additive
arena has its own required schema and status. Analyzer must explicitly opt into
this contract and must not reconstruct a missing arena from source text.

Exact parse yields an arena; recovery yields `unavailable` with an explicit reason,
not a guessed tree. EOF is not a source token. Empty source has a valid zero-length
root. Invalid coordinates abort rather than clamp. Full source still owns comments
and whitespace; hidden-channel tokens are not part of the grammar tree.

## Tasks and acceptance

1. Shared serializer and Java wiring, using the same parser invocation.
2. Verify source spans, ordered children, unique ownership, grammar fingerprint,
   no duplicated source text, nested expression/return/throw/lambda nodes.
3. Analyzer immutable arena reader validates links/ranges/ownership and status.
4. Real HTTP reparse of both synthetic inputs, then consumer read-back and mutations.

This extends the playground data contract only. No graph migration or product
consumer change. Whole Analyzer operations/rules/UI remain incomplete.

## Verification

SemanticEvidenceIrContractTest 27/27 passed, including empty input and malformed
return with missing semicolon. The empty-input test first exposed a null stop
token for an epsilon rule; zero-width grammar ranges now handle that case.
Rebuilt and restarted the verified playground process. Real HTTP ingestion and
Analyzer read-back produced 403/478 syntax nodes for Staffing/ContextCases and
verified return/if/throw/lambda ranges. Removing root links from each real artifact
was rejected by the Analyzer. Analyzer unittest: 31 passed. Full scale, all-language
coverage, recovered-source mappings and common operation lowering remain unverified.
