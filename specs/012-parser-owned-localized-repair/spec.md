# Feature Specification: Parser-Owned Localized Repair Platform

**Feature Branch**: `012-parser-owned-localized-repair`

**Created**: 2026-07-22

**Status**: Draft — approved scope from user directive (2026-07-22 session); implementation in progress

**Input**: Replace the full-unit Agent transfer with Parser-owned error localization, add a
language-agnostic grammar-guided minimal-edit engine, orchestrate repairs as dependency-aware
waves, and gate every adoption behind deterministic semantic-safety checks — while keeping the
established Node JSON contract byte-stable for valid inputs and keeping the Analyzer untouched.

## Supersedes / corrections to prior specs

This feature is the single source of truth for the repair pipeline. It explicitly corrects
overclaims found in prior specs (verified against code and real corpus on 2026-07-22):

- **009-layered-parser-recovery** claimed a *bounded* FailureEnvelope. The implementation sends
  the entire failed unit (up to 65,536 chars) in `sourceExcerpt` and allows edits across
  `0..unitText.length()` (`FailureEnvelopeFactory` lines 25/39/66/75). Superseded by FR-020..026.
- **011-live-gpu-repair-agent** is marked "Completed" on the strength of a 115-char synthetic
  fixture. On the real AMS corpus the Agent was called 21 times with **0 adopted patches**
  (reject reasons: EXPECTED_TEXT_MISMATCH 8, AMBIGUOUS_PROPOSAL 4, NO_OP_EDIT 2,
  MISSING_SUBMIT 1; 3 size skips; 10 units left REVIEW_REQUIRED). The live-GPU flow works as
  plumbing but its repair effectiveness claim is superseded by this spec's success criteria.
- **010** stream/quality work is retained; its stream event names are a frozen compatibility
  contract here (FR-060).

`unitStatuses.RECOVERED_VALIDATED = 20(+3 SAFE)` in the AMS sidecar means units salvaged by
minimal-unit exact parsing after file-level failure — **not** Agent repairs.

## Non-goals

- No changes to robo-data-analyzer (no tracked file, no new consumer logic).
- No new AST Node types (e.g. UNRESOLVED). Diagnostics/repair state live in Parser sidecars only.
- No automatic promotion of Agent successes into grammar or rules.
- No guarantee of full automatic repair: uncertain repairs end as REVIEW_REQUIRED, honestly.
- No per-language hand-written rule piles; language-specific repair knowledge is limited to a
  small optional RepairProfile per language module.

## Fixed Node JSON contract (baseline)

For valid inputs, all of the following are frozen: file name/location, root structure, field
names/types, null/empty-array/omission conventions, the existing Node type set, parent/child
hierarchy, child order, node names and casing, start/end line coordinates, ID rules, Analyzer
semantics, and determinism across reruns.

**Node type inventory (provenance: 175 real AST files from `target/test-data/full-corpus-live-agent/analysis`, extracted 2026-07-22):**

ASSIGNMENT, CALL, CASE, CATCH, CLASS, CLOSE_CURSOR, COMMIT, CONSTANT_FIELD, CURSOR_VARIABLE,
DECLARE, DEFINE, DELETE, ELSE, ELSIF, ENUM, ENUM_CONSTANT, EXCEPTION, EXECUTE_IMMEDIATE, EXIT,
FETCH, FIELD, FILE, FUNCTION, FUNCTION_CALL, GLOBAL_VARIABLE, IF, IMPORT, INCLUDE, INSERT,
INTERFACE, JOIN, LOOP, MEMBER, MERGE, METHOD, NEW_INSTANCE, OPEN_CURSOR, PARAMETER, PROCEDURE,
RETURN, SELECT, STRUCT, SWITCH, TRIGGER, TRIGGER_BLOCK, TRY, TYPEDEF, UNION, UNION_ALL, UPDATE

(50 types; additionally BEGIN and UPDATE appear in golden fixtures.) This inventory was produced
by the current dirty worktree; SC-002 requires a diff-0 cross-check against the clean baseline
clone `D:\work\robo\.audit\antlr-code-parser-head-f0d21ba` on the same valid corpus before the
inventory is declared authoritative. The byte-level golden contract test
(`AstJsonGoldenContractTest`) remains the per-language gate.

## Functional Requirements

### A. Parser-owned localization (replaces Agent self-localization)

- **FR-020**: The Parser MUST compute, for every failed unit, one or more *error spans* from
  ANTLR diagnostics (offending token, expected token set, rule stack), expanded only through
  deterministic levels: L0 offending token ± few tokens → L1 enclosing statement/expression →
  L2 statement + related declaration summary → L3 enclosing block. L4 means "stop automatic
  repair; REVIEW_REQUIRED".
- **FR-021**: The default Agent input is the **minimal slice** (lowest level that forms a
  syntactically completable region), never the full unit or file. Full-unit transmission is
  removed as a default path; no configuration may silently re-enable it.
- **FR-022**: Every slice carries three offset frames (file, unit, slice) plus the snapshot
  SHA-256 of the working copy it was cut from, and the expected text at the edit anchor.
- **FR-023**: The Agent has no authority to widen a slice. Requests for more context are
  answered by the Parser choosing the next level (max L3).
- **FR-024**: Patch proposals MUST be bound to absolute unit offsets + snapshot hash;
  stale-snapshot or expected-text-mismatch proposals are rejected before any reparse.
- **FR-025**: Per-request evidence MUST record source chars sent, prompt tokens (when the
  provider reports them), slice level, and the slice/unit size ratio.
- **FR-026**: ANTLR's reported error position MAY be later than the true root cause; the
  localizer MUST consider string/comment/bracket/BEGIN-END boundaries when choosing L1..L3
  (documented heuristic, deterministic, testable).

### B. Grammar-guided deterministic candidate engine (language-agnostic)

- **FR-030**: A Core engine MUST generate bounded single-token candidates (insert/delete/replace)
  from the ANTLR expected-token set at the offending position, rank them by edit cost, and
  evaluate each candidate by strict reparse of the affected unit within a time budget.
- **FR-031**: The engine uses only language-agnostic runtime facts (expected tokens, offending
  token, rule stack) plus an optional per-language RepairProfile (edit-cost weights, forbidden
  edits, dialect-specific allowed tokens). No language conditionals in Core.
- **FR-032**: Candidate evaluation MUST be deterministic: same input → same chosen candidate.
- **FR-033**: The engine runs before any Agent call; the Agent is invoked only when no
  deterministic candidate passes the gates.

### C. Adoption gate (semantic safety tiers)

- **FR-040**: Hard-reject edits (never auto-adopted): identifier, literal, operator changes;
  control-flow/call/return changes; DML/DDL kind, table/column, JOIN/predicate changes;
  transaction/exception/dynamic-SQL changes; multiple equally-plausible candidates; unclear
  root-cause range.
- **FR-041**: Auto-accept is limited to token-stream-preserving edits (BOM/encoding/whitespace
  normalization) → RECOVERED_SAFE.
- **FR-042**: Token-stream-changing but grammar-mandated edits (e.g. dialect keyword removal)
  require: strict full-unit reparse with 0 lexer/parser errors and 0 ANTLR recoveries, plus
  token/AST fingerprint delta check → RECOVERED_VALIDATED; otherwise REVIEW_REQUIRED.
- **FR-043**: Reparse success alone is never reported as semantic preservation. Result grades
  are Parser sidecar states, never AST node types.
- **FR-044**: With the Agent disabled or unreachable, the deterministic path MUST function
  end-to-end (fail-closed; valid-input path calls the Agent 0 times).

### D. Dependency-aware wave orchestration

- **FR-050**: Raw diagnostics MUST be grouped by independence (different files / different
  non-nested units / non-overlapping spans on the same snapshot). Only independent groups run
  in parallel; same-block, overlapping, or post-unclosed-delimiter diagnostics are sequential.
- **FR-051**: After each wave the working copy is re-snapshotted and fully reparsed; next-wave
  diagnostics are recomputed (cascade elimination). Max 3 waves.
- **FR-052**: Termination guards: no diagnostics, max waves, repeated first-error fingerprint
  (oscillation), empty applied-patch set (no progress), repeated patch hash.
- **FR-053**: Concurrency MUST be governed by a token-budget weighted semaphore with
  backpressure and per-request timeout/cancellation — not a fixed thread count.

### E. Language onboarding SPI

- **FR-055**: A new language is added as one LanguageModule with: identity, detection, grammar
  provenance, parser factory + entry rules, SourceUnitLocator, AstAdapter/Listener, contract
  corpus; optionally ErrorSpanLocator specialization, RepairProfile, SemanticValidator. Zero
  Core edits (SC-007 rehearsal proves it).
- **FR-056**: A language without a SemanticValidator limits auto-accept to FR-041 scope.

### F. Compatibility, evidence, governance

- **FR-060**: Existing stream event names/shapes and sidecar schemas stay backward compatible;
  additions are new optional fields only.
- **FR-061**: Original inputs are never modified; original-file and inventory hashes are
  verified before/after every run.
- **FR-062**: Grammar files stay pinned (upstream URL/commit, SHA-256, license, tool/runtime
  version, entry rule); no automatic upstream tracking. Grammar promotion and RepairProfile
  promotion are separate, human-approved processes.
- **FR-063**: The Agent adapter stays provider-independent (SGLang chat-template/topK vs
  OpenAI-style reasoning_effort are mutually exclusive option sets, enforced at construction).

## Success Criteria

- **SC-001**: `.\mvnw.cmd test` fully green (no failures/errors) on the final worktree.
- **SC-002**: Valid-corpus Node JSON diff 0 vs clean baseline f0d21ba (audit clone) across all
  five languages, byte-compare per file.
- **SC-003**: On the real AMS corpus rerun: no Agent request contains a full unit by default;
  per-request chars/tokens and slice ratios are reported; every adoption/rejection has an exact
  reason; final lexer/parser errors and ANTLR recoveries reported per unit; original inventory
  hash unchanged.
- **SC-004**: Deterministic-only mode (Agent off) completes the whole corpus safely and
  resolves at least the delimiter/BOM/dialect-token class of failures.
- **SC-005**: Multi-language validation: real corpus runs for Java, Python, C, PostgreSQL and
  Oracle (분석대상모음 projects), not Oracle alone.
- **SC-006**: Hard-reject evidence: a test corpus of semantically dangerous candidate edits is
  demonstrably rejected (identifier/literal/operator/transaction changes).
- **SC-007**: New-language onboarding rehearsal: one language added (or simulated via module
  isolation test) with zero Core package modifications.
- **SC-008**: GPU concurrency measured with real prompts (1→2→4→8→16→32), reported as latency,
  throughput and token counts; thread count is not the control variable.
- **SC-009**: README/spec/plan/tasks/code/verification report agree; final report separates
  검증됨 / 실패함 / 미검증 / REVIEW_REQUIRED.
