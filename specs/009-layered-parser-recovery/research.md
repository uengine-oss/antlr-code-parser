# Research: ANTLR Layered Generic Recovery Platform

## Decision 1: Keep ANTLR 4.13.2 and the current listeners

**Decision**: Keep ANTLR as the core parser and retain 4.13.2 for this feature.

**Rationale**: Current grammars/listeners already encode the Node JSON contract. Runtime upgrade alone does not make grammars tolerant, while grammar replacement can alter parse trees and listener events. Valid-output compatibility is more valuable than blind currency.

**Rejected**: Replacing ANTLR with Tree-sitter/CAST/Understand; blindly copying latest grammars; changing to SLL-only parsing. Each either changes output semantics, reduces supported database syntax, or fails current corpus A/B checks.

## Decision 2: Add quality evidence around the existing exact path

**Decision**: First-pass parse continues to use the same grammar entry rule and listener. Explicit error listeners and a quality gate observe it. If exact, existing listener JSON is written unchanged.

**Rationale**: This creates a zero-diff fast path and fixes current false-success reporting without redesigning listeners.

## Decision 3: Recover by smallest language-declared unit

**Decision**: `LanguageModule` supplies a token-aware unit locator and entry-rule adapters. Common orchestration handles attempts, ordering, validation, source maps, and audit.

**Rationale**: Unit boundaries are language-specific, but retry policy is not. This avoids a central language switch and avoids reparsing/rebuilding unaffected units.

**Oracle first**: Use standalone SQL*Plus terminator lines and declaration-start probes outside strings/comments to isolate procedures/functions/triggers/packages. Preserve current emitted Node vocabulary; packages remain a source-boundary concept unless separately approved as an AST node.

## Decision 4: Use deterministic recovery before an Agent

**Decision**: Ordered stages are exact unit parse, common safe normalization, language rules, bounded context reconstruction, then Agent.

**Rationale**: Deterministic transforms are reproducible, cheap, reviewable, and promotable. Agent repair is reserved for residue that cannot be economically covered by stable rules.

## Decision 5: Agent proposes edits; Parser decides

**Decision**: Parser owns `FailureEnvelope`, attempt limits, working copy, edit-bound validation, Repair Agent invocation, reparse, quality comparison, and adoption.

**Rationale**: Parser repair is part of source-to-AST production. Keeping the invocation boundary in Parser prevents Analyzer from acquiring parsing responsibilities and keeps syntax acceptance under one owner.

**Failure behavior**: Missing model-provider configuration, exhausted budget, timeout, or invalid patch becomes explicit `REVIEW_REQUIRED`; the exact path continues for other files.

## Decision 6: Validate one versioned internal language catalog

**Decision**: Parser resources contain the canonical module catalog. Parser validates it against discovered modules in memory and does not publish a cross-service artifact. Analyzer continues to consume AST JSON without Parser registry logic.

**Rationale**: Parser extensions, module ids, emitted node types, routine types, and grammar provenance have one Parser-owned source. Analyzer semantics remain independently owned because AST JSON is the only integration contract.

**Compatibility**: Major mismatch fails closed. Missing required module metadata fails before parsing.

## Decision 7: Sidecars mirror source paths outside analysis

**Decision**:

```text
data/
├── analysis/<relative-without-extension>.json
├── diagnostics/<relative-with-extension>.parse.json
└── repairs/<relative-with-extension>.repair.json
```

**Rationale**: Existing Analyzer AST traversal remains clean and the fixed AST filename/location contract remains intact.

## Decision 8: Preserve lines through reversible edits

**Decision**: Every edit stores original and working offsets. Accepted AST ranges are translated through a piecewise source map back to original 1-based line coordinates. Prefer line-preserving normalization; reject unmappable ranges.

## Decision 9: Grammar provenance is pinned, not refreshed automatically

**Decision**: The internal language catalog records grammar file hashes and upstream commit when known. A grammar update is a reviewed candidate requiring valid-corpus JSON diff zero plus failure-corpus quality improvement.

## Corpus observations driving the design

- Java and Python current corpora parse without collected syntax errors.
- C succeeds on all but one observed file with the current project-context type registration; a blind upstream grammar performed worse.
- Oracle `AMS_procedures.sql` contains many standalone procedure declarations; whole-file parsing does not emit their bodies reliably. Object splitting recovers a substantial subset, proving minimal-unit recovery is useful.
- Oracle package input parses but current listener flattens package contents and emits no PACKAGE node; changing this is semantic and excluded.
- PostgreSQL `CREATE FUNCTION` currently emits a `PROCEDURE` node; changing the label is excluded.

## Alternatives retained for later review

- Approved semantic evolution of Oracle PACKAGE nodes or PostgreSQL FUNCTION labels.
- Grammar fork refresh after a pinned, reproducible A/B report demonstrates zero normal-output drift.
