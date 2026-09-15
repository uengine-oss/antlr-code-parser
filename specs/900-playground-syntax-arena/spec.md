# Playground PostgreSQL syntax arena

Scope: cloned playground parser only; preserve product originals and Java implementation.
Intent: Analyzer must derive DBMS child structure from the same ANTLR parse, with source
provenance and explicit unsupported/recovered states. Reuse SyntaxArenaSealer for the
PostgreSQL module, without a second parse or regex extraction. Nested dollar-quoted
PL/pgSQL remains outer grammar text here; this is not its nested syntax representation.

DoD: exact CREATE TABLE input produces deterministic arena with createstmt/columnDef
rules and correct quoted/Unicode source slices. Recovered input gives unavailable arena,
not exact nodes. Existing SemanticEvidenceIrContractTest suite and PostgreSQL data-object
tests pass. Rebuild isolated host and inspect actual HTTP DDL artifact before Analyzer use.

Actual HTTP finding: intake counted 1 DDL but ParseOrchestrator scanned only source/,
so the completed run emitted just 3 Java artifacts. Include source/ and ddl/ in one
detection population. DDL output goes to analysis/ddl; DDL source identity uses ddl/
prefix. Preserve input bytes and reject any output-path collision before parsing,
rather than overwriting another artifact. A Spring integration test must prove a
DDL-only intake produces the exact arena and does not escape analysis/. Legacy
source output layout stays as observed; collisions require a future layout contract.

Actual Analyzer arena validation then rejected PostgreSQL optional empty rules:
typename span ended at 169, while its epsilon opt_array_bounds anchored at next token 170.
No characters were consumed. Define empty-rule anchors within their parent's closed
boundary; clip only epsilon zero-width anchors, never token/nonempty ranges. Test all
PostgreSQL child containment, not just token slices, plus existing Java contracts.

Verified: 37 targeted tests pass (29 common IR, 5 PostgreSQL data-object, 1 mixed SQL,
2 DDL intake/collision). Real localhost HTTP intake now yields 3 Java + 1 DDL artifacts.
Analyzer validates DDL source hash and all 563 arena nodes. Three CREATE TABLE statements
contain 10 columns; a separate ALTER contains the 11th column declaration, preserved but
not applied by the current Analyzer. Existing Java 11 routines/6 calls and control-flow
verification also pass. Evidence under services/_runs/parser-intake and analyzer-dbms.
