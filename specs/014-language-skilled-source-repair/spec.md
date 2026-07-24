# Feature Specification: Language-Skilled Verified Source Repair

**Feature Branch**: `main` (dirty worktree preserved; feature isolated under spec 014)

**Created**: 2026-07-24

**Status**: Approved by user for implementation

**Input**: When ANTLR reports a syntax error, give the existing Java Repair Agent concise
common and language-specific skills, let it diagnose the root cause and propose only the
smallest relevant edit, reparse the temporary result, and optionally apply a fully verified
repair to the original source.

## User Scenarios & Testing

### User Story 1 - Language-aware minimal repair (Priority: P1)

As a parser operator, I want an Oracle, PostgreSQL, Java, Python, or C failure to receive
instructions specific to that language so the Agent can make the same bounded structural
repair a human would make without rewriting unrelated code.

**Independent Test**: Capture the outgoing Agent request for each language. It contains the
common skill and exactly one matching language skill, and never contains another language's
skill.

**Acceptance Scenarios**:

1. **Given** Oracle PL/SQL with a uniquely missing `BEGIN` before `EXCEPTION`, **When** the
   Oracle skill is assembled, **Then** it permits the grammar-mandated insertion and forbids
   abstaining merely because `BEGIN` is absent.
2. **Given** a proposed table, column, identifier, literal, predicate, or operator change,
   **When** the existing Java gates evaluate it, **Then** it is rejected even if reparsing
   succeeds.
3. **Given** a diagnostic whose true cause is an earlier unclosed delimiter, **When** the
   bounded slice contains that cause, **Then** the Agent is instructed to edit the cause rather
   than the later diagnostic token.

### User Story 2 - Verified original-source application (Priority: P1)

As a corpus maintainer, I want a successfully repaired source to be written back only when the
entire file is accepted and explicit source-application mode is enabled.

**Independent Test**: Repair a temporary malformed source, verify the original remains
unchanged with the mode off, then enable the mode and verify an atomic same-encoding write of
only the accepted change.

**Acceptance Scenarios**:

1. **Given** an accepted repair and `PARSER_REPAIR_APPLY_TO_SOURCE=true`, **When** the source
   still matches the parsed snapshot hash, **Then** write the repaired source atomically.
2. **Given** `PARTIAL`, `REVIEW_REQUIRED`, a stale source hash, lossy decoding, or application
   mode off, **When** application is considered, **Then** do not modify the source.
3. **Given** UTF-8, EUC-KR, or MS949 input, **When** a repair is applied, **Then** retain the
   detected charset, BOM text if present, and existing line endings outside the edit.

### User Story 3 - Portable Skill packages (Priority: P2)

As a maintainer, I want each repair skill to be a valid, concise `SKILL.md` package so it can
be versioned, reviewed, tested, and reused independently of a particular model provider.

**Independent Test**: Validate all six skill directories with the skill validation script and
run prompt-contract tests that reject malformed frontmatter, missing skills, path traversal,
and conflicting instructions.

## Edge Cases

- Unknown or unsafe `languageId` values must not become classpath paths.
- Missing or malformed common/language skills must fail closed as `REVIEW_REQUIRED`.
- A repair that makes one unit clean but leaves another unit unresolved must not be written.
- Concurrent modification between parsing and application must be detected by byte hash.
- A no-op repaired source must not rewrite the original file.

## Functional Requirements

- **FR-001**: The base system prompt MUST be provider-neutral and MUST be combined with
  `common-syntax-repair` followed by `<languageId>-syntax-repair`.
- **FR-002**: The repository MUST provide common, Java, Python, C, Oracle, and PostgreSQL
  repair skills with good, bad, and abstention examples.
- **FR-003**: Java MUST select the skill from the Parser-detected `languageId`; the model MUST
  NOT select, discover, or widen skills.
- **FR-004**: Skills MUST request the smallest root-cause edit and MUST forbid whole-excerpt
  rewrites and unrelated cleanup.
- **FR-005**: Existing envelope-hash, expected-text, offset, edit-risk, strict-improvement,
  full reparse, AST-child, and coverage gates remain authoritative.
- **FR-006**: A fully accepted direct text repair MUST expose the materialized repaired source
  separately from AST JSON and audit evidence.
- **FR-007**: Original-source application MUST be explicit and default off. Configuration is
  `parser.repair.apply.to.source` / `PARSER_REPAIR_APPLY_TO_SOURCE`.
- **FR-008**: Application MUST verify the original byte SHA-256, reject lossy decoding, encode
  with the detected charset, write a sibling temporary file, and atomically replace where the
  filesystem supports it.
- **FR-009**: `PARTIAL`, `REVIEW_REQUIRED`, `UNRESOLVED`, failed, context-only salvage, and
  no-op outcomes MUST never modify source.
- **FR-010**: ANTLR grammars, language routing, AST Node schema, API endpoints, and Analyzer
  contracts MUST remain unchanged.
- **FR-011**: Tool-call output remains the baseline transport in this feature; structured
  output mode may be evaluated separately only after an exact-server A/B benchmark.

## Success Criteria

- **SC-001**: All six skill directories pass `quick_validate.py`.
- **SC-002**: Prompt-contract tests prove common + exactly one matching language skill.
- **SC-003**: A golden Oracle missing-`BEGIN` example is present and is not contradicted by an
  abstention rule.
- **SC-004**: Source-application tests cover off, success, stale hash, lossy input, partial,
  no-op, and charset preservation paths.
- **SC-005**: `.\mvnw.cmd test` passes with no existing regression.
- **SC-006**: The water corpus rerun reports its exact parser/lexer/recovery result; GPU testing
  is reported separately if credentials are unavailable.

## Superseded Contract

Spec 012 FR-061 ("original inputs are never modified") remains the default behavior. This
feature adds a user-authorized, explicit opt-in write-back path after full verification; it
does not silently change the default upload/parse contract.
