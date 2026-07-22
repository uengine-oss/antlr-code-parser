# Implementation Plan: ANTLR Layered Generic Recovery Platform

**Branch**: `009-layered-parser-recovery` | **Date**: 2026-07-21 | **Spec**: [spec.md](./spec.md)

## Summary

Wrap the current ANTLR grammars and listeners in a zero-diff exact path that collects diagnostics and applies a deterministic quality gate. Only failed files are split into language-declared minimal units and retried through exact unit parsing, deterministic rules, context reconstruction, and finally a constrained Parser-owned Repair Agent. Preserve normal Node JSON bytes and keep diagnostics and repairs outside `analysis/`. Analyzer remains an unchanged consumer of accepted AST JSON only.

## Technical Context

**Language/Version**: Java 17

**Primary Dependencies**: Spring Boot 3.3, ANTLR runtime/tool 4.13.2, Jackson, and JDK `HttpClient` for explicitly configured structured model-provider calls

**Storage**: Isolated filesystem roots containing `source/`, `analysis/`, `diagnostics/`, and `repairs/`

**Testing**: JUnit 5/Spring Boot tests, byte-level AST goldens, deterministic fake-Agent tests, and an isolated full-corpus runner; downstream compatibility is verified read-only

**Target Platform**: JVM Parser service on Windows/Linux containers

**Project Type**: JVM parsing service with a frozen AST JSON consumer boundary

**Performance Goals**: Exact-file wall-time overhead below 20%; no Agent call for exact files; at most three Agent calls per failed minimal unit

**Constraints**: Node JSON contract and listener semantics frozen; original sources read-only; no customer-specific rules; no operational data mutation; protected Java/Python listener WIP untouched

**Scale/Scope**: Five current languages, tens of thousands of corpus lines, mixed legacy/vendor syntax, and file/minimal-unit audit artifacts

## Constitution Check

| Parser constitution principle | Result | Design evidence |
|---|---|---|
| I. Language Module Auto-Discovery | PASS | `LanguageModule` is Spring-discovered; common recovery has no language switch. |
| II. Automatic Detection | PASS | Catalog extensions plus module-owned content probes; no client language metadata is required. |
| III. Content-Based Classification | PASS | Intake remains content-based; shared `.sql` ownership is resolved by module probes. |
| IV. Replace-All, Stateless | PASS | Runtime outputs remain per run; sidecars are reproducible artifacts. |
| V. Streaming-First | PASS | Progress reports exact, recovered, partial, and failed status. |
| VI. Stable AST JSON | PASS | The exact path writes existing listener bytes; new metadata stays outside `analysis/`. |

The established Analyzer boundary is unchanged: it consumes accepted AST JSON and does not participate in Parser recovery.

## Project Structure

### Documentation

```text
specs/009-layered-parser-recovery/
|-- spec.md
|-- plan.md
|-- research.md
|-- data-model.md
|-- quickstart.md
|-- structure-audit.md
|-- verification-report.md
|-- contracts/
|   |-- diagnostics-sidecar.md
|   |-- language-catalog.md
|   `-- repair-proposal.md
|-- checklists/requirements.md
`-- tasks.md
```

### Parser Source

```text
src/main/java/legacymodernizer/parser/
|-- api/                         # HTTP, streaming, and web configuration
|-- intake/                      # source classification and Parser workspace
|-- parsing/
|   |-- ParseOrchestrator.java
|   |-- ParserSelection.java
|   |-- RawParseResult.java
|   |-- boundaries/
|   `-- languages/
|       |-- LanguageModule.java
|       |-- LanguageModuleRegistry.java
|       |-- LanguageCatalog.java
|       |-- LanguageCatalogValidator.java
|       |-- c/
|       |-- java/
|       |-- oracle/
|       |-- postgresql/
|       `-- python/
|-- recovery/
|   |-- LayeredRecoveryPipeline.java
|   |-- boundaries/
|   |-- diagnostics/
|   |-- evidence/
|   |-- quality/
|   |-- repair/
|   |-- reports/
|   |-- rules/
|   `-- workingcopy/
|-- antlr/                       # frozen generated parsers/listeners
|-- model/Node.java              # frozen AST contract
`-- service/ParseProgressTracker.java # temporary protected-listener boundary

src/main/resources/
|-- languages/language-catalog.json
`-- recovery/repair-agent-system-prompt.txt
```

`service/ParseProgressTracker.java` is the only temporary legacy-package exception because the protected Java/Python listeners import that exact type. It cannot move without changing their recorded hashes. No new code may be added under `service`.

## Implementation Phases

### Phase 0 - Baseline and contract lock

- Snapshot dirty-worktree and protected-listener hashes.
- Snapshot operational `project/data/analysis` read-only.
- Build valid and malformed fixtures for all five languages.
- Capture exact AST paths and byte-level goldens.

### Phase 1 - Diagnostics and internal catalog

- Collect lexer/parser errors explicitly while preserving the listener path.
- Evaluate declaration coverage and deterministic quality tuples.
- Validate the versioned Parser-internal language catalog against discovered modules.
- Write deterministic diagnostic sidecars outside `analysis/`.

### Phase 2 - Minimal-unit deterministic recovery

- Discover token-aware language units with file fallback.
- Retry only failed units and protect exact siblings.
- Apply immutable working-copy edits with reversible source maps and diffs.
- Run common safe rules, language rules, and context reconstruction in fixed order.
- Rebase accepted AST ranges to original source coordinates.

### Phase 3 - Parser-owned structured Repair Agent

- Build a bounded `FailureEnvelope` containing exact diagnostics, range, excerpt, coverage, prior attempts, and validation feedback.
- Call an explicitly configured model-provider API directly from Java with a forced structured `submit_parser_repair` tool.
- Accept only typed minimal edits bound to the envelope checksum.
- Reparse and quality-check every proposal; retry only the failed unit up to three calls.
- Reject ambiguity, AST output, whole rewrites, out-of-range edits, and non-improving candidates.

### Phase 4 - Rule promotion

- Cluster repeated successful repair signatures.
- Produce reviewable rule or pinned-grammar candidates and regression fixtures.
- Never promote a rule or grammar automatically.

### Phase 5 - Responsibility-based structure and naming

- Keep HTTP boundaries under `api`, workspace intake under `intake`, parsing/module ownership under `parsing`, and recovery under `recovery`.
- Co-locate each language module, source-unit locator, and language-specific rule under `parsing/languages/<language>`.
- Remove moved-package compatibility shims and stale imports.
- Preserve the protected listener hashes and public HTTP behavior.

### Phase 6 - Analyze and verify

- Run focused, contract, integration, and full Parser tests in isolated roots.
- Parse the full Parser-routed Java/Python/C/Oracle corpus twice and run the available real
  PostgreSQL DDL through its ANTLR module in a dedicated isolated compatibility test.
- Compare exact AST path/byte inventories and repeated-run determinism.
- Record unresolved ranges, Agent attempts, performance, and source/operational hashes.
- Verify the unchanged Analyzer can consume accepted AST JSON read-only; do not change Analyzer code or configuration.

## Compatibility Strategy

- Exact first-pass files use the same entry rule, listener, `setFileInfo`, and raw `toJson` string.
- Sidecars never reside under `analysis/`.
- Recovered files may assemble accepted unit JSON only after every range maps to original coordinates.
- Existing filenames, paths, fields, field order, types, null/empty representation, Node vocabulary, hierarchy, child order, names, case, line information, identity meaning, and determinism remain frozen.
- Any proposal requiring new or renamed Node semantics stops for explicit user approval.

## Complexity Tracking

No constitution exception is required. Parser owns failure evidence, direct Agent invocation, repair proposals, reparsing, validation, and acceptance. Analyzer owns none of those responsibilities.
