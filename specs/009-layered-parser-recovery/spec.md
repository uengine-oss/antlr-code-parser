# Feature Specification: ANTLR Layered Generic Recovery Platform

**Feature Branch**: `009-layered-parser-recovery`

**Created**: 2026-07-21

**Status**: Partially implemented — superseded in part by [012-parser-owned-localized-repair](../012-parser-owned-localized-repair/spec.md)

> **Correction (2026-07-22)**: The "bounded" FailureEnvelope described here was implemented as a
> full-unit transfer (entire unitText up to 65,536 chars, edit range 0..length). Localization and
> minimal-slice transfer are redefined and owned by spec 012.

**Input**: Preserve the existing successful Node JSON contract while recovering only failed minimal language units through deterministic rules and, as a last resort, a constrained Repair Agent.

## User Scenarios & Testing

### User Story 1 - Preserve every successful AST (Priority: P1)

As an analyzer operator, I can reparse an already supported, valid project and receive byte-identical Node JSON at the same paths so that no downstream meaning changes.

**Why this priority**: Recovery is unacceptable if it changes known-good output.

**Independent Test**: Run the current and new parser on the same valid Java, Python, C, Oracle, and PostgreSQL fixtures and compare relative paths plus SHA-256 hashes of every AST JSON file.

**Acceptance Scenarios**:

1. **Given** a file that passes the quality gate on the first parse, **When** the recovery platform parses it, **Then** no recovery stage runs and its AST JSON bytes and path equal the golden baseline.
2. **Given** the same valid input and configuration, **When** it is parsed repeatedly, **Then** output file order, child order, field order, values, and bytes are deterministic.
3. **Given** diagnostics are enabled, **When** a valid file is parsed, **Then** diagnostics are written outside `analysis/` and the AST schema is unchanged.

---

### User Story 2 - Detect false success precisely (Priority: P1)

As a maintainer, I can see lexer errors, parser errors, ANTLR recovery, declaration coverage, exact failure ranges, and quality status for each file without reading stderr.

**Why this priority**: The current parser counts recovered/partial parses as success when ANTLR does not throw.

**Independent Test**: Parse a fixture with a known lexer error and a fixture with a recoverable parser error; assert deterministic diagnostics and a non-EXACT quality result while keeping sidecars out of AST JSON.

**Acceptance Scenarios**:

1. **Given** ANTLR reports a syntax error but returns a parse tree, **When** quality is evaluated, **Then** the file is not reported as exact success.
2. **Given** source declarations are visible but absent from emitted AST, **When** coverage is evaluated, **Then** the missing declaration ranges are reported.
3. **Given** a diagnostic event, **When** it is serialized, **Then** it contains language, grammar revision, file hash, line, column, offending token, expected tokens, rule stack, and source window where available.

---

### User Story 3 - Recover only failed minimal units (Priority: P1)

As a user who drops in unfamiliar legacy code, I receive valid AST for independently recoverable classes, methods, functions, procedures, packages, or top-level fragments even when another unit in the same file is malformed.

**Why this priority**: File-wide retry or rewrite increases risk and maintenance cost.

**Independent Test**: Parse `AMS_procedures.sql`; verify standalone procedure candidates are isolated, exact candidates remain untouched, recoverable candidates are reparsed, and unresolved candidates have precise diagnostics.

**Acceptance Scenarios**:

1. **Given** a failed whole-file parse, **When** a language module can identify declaration boundaries, **Then** only failed minimal units enter recovery.
2. **Given** exact units and failed units in one file, **When** results are assembled, **Then** exact units retain source order and values and only accepted recovered units are merged.
3. **Given** a candidate changes text outside its unit range, **When** validation runs, **Then** it is rejected.

---

### User Story 4 - Add languages without common-engine branches (Priority: P2)

As a parser maintainer, I can support another language by adding a `LanguageModule` and internal catalog entry without editing common orchestration or Analyzer.

**Why this priority**: This is the primary control on long-term maintenance fatigue.

**Independent Test**: Register a test language module through component discovery and a test catalog entry; verify detection, diagnostics, and quality routing without modifying common-engine source.

**Acceptance Scenarios**:

1. **Given** two modules share an extension such as `.sql`, **When** detection runs, **Then** module-owned content probes resolve the module deterministically or report ambiguity.
2. **Given** a catalog version unsupported by Parser, **When** parsing starts, **Then** Parser fails explicitly before creating AST output.
3. **Given** a new module and matching catalog entry, **When** Parser starts, **Then** discovery and validation succeed without changing common orchestration or Analyzer.

---

### User Story 5 - Use a constrained Repair Agent only as a last resort (Priority: P2)

As an operator, I can let a Repair Agent propose a minimal syntax patch for the remaining failed range, while Parser retains all authority to validate or reject it.

**Why this priority**: Some vendor and legacy syntax cannot be maintained economically as one-off grammar branches, but semantic ambiguity must never be auto-adopted.

**Independent Test**: Use a deterministic fake Repair Agent to propose one valid minimal patch, one out-of-range patch, one full rewrite, and one ambiguous patch; only the valid, quality-improving proposal is accepted.

**Acceptance Scenarios**:

1. **Given** deterministic rules have not recovered a unit, **When** Agent repair is enabled, **Then** it receives only a precise `FailureEnvelope` and parse/patch/validate tool contract.
2. **Given** an Agent proposal, **When** it changes the original file, rewrites the whole unit, emits AST, exceeds its range, or lacks a unique validated interpretation, **Then** it is rejected.
3. **Given** three unsuccessful attempts, **When** recovery stops, **Then** the unit is `REVIEW_REQUIRED` or `UNRESOLVED` in sidecars only and no new AST node type is emitted.

---

### User Story 6 - Promote repeated repairs into maintained rules (Priority: P3)

As a maintainer, I can cluster successful repairs by normalized error signature and promote recurring patterns into reviewed rules or pinned grammar patches with regression tests.

**Independent Test**: Feed repeated successful repair audit records and verify a deterministic candidate report is produced without automatically changing a rule or grammar.

## Edge Cases

- A lexer error has no parser rule stack or expected-token set.
- A parser error occurs before a declaration boundary can be identified.
- Nested declarations overlap or a delimiter appears inside a string/comment.
- A source file uses UTF-8, EUC-KR, or MS949 and a working copy must preserve line mapping.
- Several language modules claim the same extension and content probes tie.
- Recovery removes parser errors but declaration coverage or structural invariants regress.
- A repair changes line count; emitted line numbers must still map to original source lines.
- The configured model provider is unavailable, budget-exhausted, or returns no submit tool result.
- A grammar update produces fewer syntax errors but changes valid AST JSON.
- The file name or content resembles a known corpus sample; no sample-specific branch is permitted.

## Requirements

### Fixed Output Contract

- **FR-001**: For first-pass quality-gate success, the system MUST preserve AST JSON relative file name, storage location, root structure, property names, property order, property types, omitted nulls, empty arrays, node vocabulary, hierarchy, child order, names, case, line information, and deterministic bytes.
- **FR-002**: The system MUST NOT change Analyzer meaning or the existing Analyzer-generated identity rules.
- **FR-003**: The system MUST NOT implement PostgreSQL FUNCTION relabeling, an Oracle PACKAGE AST node, or an `UNRESOLVED` AST node without explicit user approval.
- **FR-004**: Diagnostics, recovery status, hashes, mappings, diffs, and Agent history MUST be separate sidecars and MUST NOT be inserted into Node JSON.

### Diagnostics and Quality

- **FR-005**: Every lexer and parser MUST use explicit collecting error listeners; default stderr reporting MUST NOT be the source of truth.
- **FR-006**: A parse attempt MUST record syntax errors, ANTLR recovery evidence, entry rule, grammar/runtime revision, elapsed time, source hash, and unit range.
- **FR-007**: Each language module MUST provide deterministic declaration discovery and AST coverage evidence appropriate to its supported unit kinds.
- **FR-008**: A quality gate MUST distinguish `EXACT`, `RECOVERED_SAFE`, `RECOVERED_VALIDATED`, `REVIEW_REQUIRED`, `PARTIAL`, `UNRESOLVED`, and `FAILED` in diagnostics only.
- **FR-009**: First-pass `EXACT` MUST require zero collected lexer/parser errors and all module-declared structural/coverage invariants.

### Language Modules and Catalog

- **FR-010**: Common orchestration MUST depend on a `LanguageModule` SPI discovered through Spring components, not language switches.
- **FR-011**: A module MUST declare language id, aliases, Parser parse extensions, shared-extension probes, entry rules, grammar provenance, recoverable unit kinds, emitted node types, routine labels, and available rule sets. Distinct capability sets MUST remain explicit rather than being widened into a behavior-changing union.
- **FR-012**: Parser MUST validate a deterministic versioned internal language catalog against its discovered modules and MUST NOT require Analyzer changes or publish Parser registry files into shared data.
- **FR-013**: Catalog schema major versions MUST fail closed when unsupported; compatible minor additions MAY be accepted after validation.
- **FR-014**: Grammar sources MUST record an upstream repository and commit or an explicit local provenance hash. Updates MUST be pinned and pass golden A/B tests before adoption.

### Layered Recovery

- **FR-015**: The original source MUST be read-only. All normalization and repair MUST occur on a per-attempt working copy.
- **FR-016**: Failed files MUST be isolated to the smallest deterministically discoverable unit among class, method, function, procedure, package, or top-level fragment.
- **FR-017**: Recovery order MUST be first-pass exact parse, minimal-unit exact reparse, common safe normalization, language rule registry, context reconstruction, then Repair Agent.
- **FR-018**: Rules MUST be keyed by generic syntax/error signatures and language/module capabilities, never customer, path, file name, or literal sample content.
- **FR-019**: Every changed working copy MUST retain original/recovered SHA-256, an ordered edit list, a reversible source map, and a unified-diff audit record.
- **FR-020**: A recovered AST MUST map all reported source ranges back to original line coordinates before serialization.
- **FR-021**: A candidate MUST improve the quality tuple without regressing already exact units, declaration coverage, source ordering, or structural invariants.
- **FR-022**: Oracle MUST be the first production module for minimal-unit recovery and MUST exercise `AMS_procedures.sql` without adding an Oracle PACKAGE node.

### Repair Agent

- **FR-023**: Parser MUST own repair orchestration, Agent invocation, validation, and acceptance. A configured Agent may return only a typed patch proposal.
- **FR-024**: `FailureEnvelope` MUST include language/module, grammar provenance, file and unit hashes, original line range, exact errors, expected tokens, rule stack, token window, bounded source excerpt, AST coverage, prior attempts, and constraints.
- **FR-025**: Agent attempts MUST be limited to three per unit and bounded by run budget/configuration.
- **FR-026**: Agent output MUST be an ordered minimal edit list plus rationale/confidence; direct AST, original-file mutation, whole-file rewrite, and edits outside the failed unit MUST be rejected.
- **FR-027**: Parser MUST reparse and quality-check every Agent proposal. Ambiguous or merely error-suppressing proposals MUST NOT be auto-adopted.
- **FR-028**: The configured Repair Agent MUST enforce structured output, request budgets, timeouts, and no source-filesystem authority; Parser remains the sole acceptance authority.

### Operations and Validation

- **FR-029**: Parse progress MUST distinguish exact, recovered, review-required, unresolved, and failed counts; no recovered parse may be silently counted as exact.
- **FR-030**: Tests MUST use isolated temporary data roots and MUST NOT delete or overwrite operational `project/data/analysis`.
- **FR-031**: The current user modifications to `JavaAstListener.java` and `PythonAstListener.java` MUST be preserved.
- **FR-032**: Java, Python, C, Oracle, and PostgreSQL corpora under `D:/work/robo/분석대상모음` MUST be exercised from original source through AST and Analyzer.
- **FR-033**: Repeated successful repair signatures MUST produce reviewable Rule/Grammar promotion candidates and regression fixtures; promotion MUST never be automatic.
- **FR-034**: The final report MUST contain normal JSON diff count, per-language file/line/error/recovery statistics, unresolved ranges, Agent attempts, Analyzer ingestion result, and performance comparison.

### Responsibility and Naming Structure

- **FR-035**: Runtime packages MUST be organized by the product responsibilities `api`, `intake`, `parsing`, and `recovery`; generic catch-all packages MUST NOT own new code.
- **FR-036**: Language-specific parser, source-boundary, and deterministic-rule code MUST have one discoverable home under `parsing/languages/<language>`.
- **FR-037**: The active runtime tree MUST contain no moved-package compatibility shims and no active imports of the superseded `service`, root `language`, `recovery/model`, `recovery/source`, `recovery/unit`, `recovery/agent`, or `recovery/audit` paths.
- **FR-038**: Package, file, class, method, and variable names MUST state domain responsibility; vague names require evidence or replacement according to `structure-audit.md`.
- **FR-039**: Structural moves MUST preserve AST/sidecar serialized names, public HTTP behavior, protected listener WIP, and all golden bytes.

### Key Entities

- **LanguageModule**: Self-contained parser capability, declaration locator, quality evidence provider, and recovery rules for one language/dialect.
- **LanguageCatalog**: Versioned Parser-internal registry of syntax ownership, parse extensions, emitted node types, routine labels, and pinned grammar provenance.
- **ParseAttempt**: Immutable evidence for one source/unit and one recovery stage.
- **ParseDiagnostic**: A collected lexer/parser/coverage/validation issue with precise source coordinates.
- **QualityDecision**: Deterministic status and reasons derived from attempt evidence.
- **SourceUnit**: Minimal recoverable original range and its kind/name/parent/source order.
- **WorkingCopy**: Original-preserving text plus edit history and original-coordinate source map.
- **RecoveryRule**: Versioned deterministic transformation guarded by error signature and invariants.
- **FailureEnvelope**: Bounded, exact context sent to a Repair Agent.
- **PatchProposal**: Typed Agent response containing minimal edits, not AST.
- **RepairAudit**: Attempts, diffs, hashes, decisions, and validation results stored outside AST.

## Success Criteria

- **SC-001**: All first-pass exact golden fixtures produce zero changed AST JSON files and zero changed relative AST paths.
- **SC-002**: Every intentionally malformed fixture is reported as non-EXACT with at least one precise diagnostic; false exact-success count is zero.
- **SC-003**: On `AMS_procedures.sql`, every declaration boundary that can be identified is accounted for as exact, recovered, review-required, or unresolved; no declaration silently disappears.
- **SC-004**: No accepted recovery edit exists outside its minimal unit and no source file hash changes.
- **SC-005**: A test language module can be added without changing common recovery orchestration or Analyzer.
- **SC-006**: Analyzer loads all accepted AST through the unchanged Node JSON contract with deterministic graph input counts.
- **SC-007**: Full-corpus parse wall time and peak memory are reported; exact-file overhead is measured and kept below 20% unless a documented evidence-based exception is approved.
- **SC-008**: All unresolved units have exact bounded ranges and reasons; no failure is represented by an invented AST node.
- **SC-009**: Target-tree verification reports required paths missing 0, forbidden moved paths 0, and active old imports 0, excluding only the documented frozen `antlr` and `model/Node` contracts.

## Assumptions

- ANTLR 4.13.2 remains the runtime/tool baseline unless pinned A/B evidence justifies a change.
- Existing generated parsers and listeners define the current output semantics; recovery wraps them rather than replacing their listener behavior.
- Agent availability is optional at runtime. Its absence yields an explicit review-required outcome, never silent success.
- Oracle package semantics and PostgreSQL function labeling remain frozen pending separate approval.
