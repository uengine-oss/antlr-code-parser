# Implementation Plan: Parser-Owned Localized Repair Platform

**Spec**: [spec.md](spec.md) · **Created**: 2026-07-22 · **Baseline**: worktree on f0d21ba (dirty, preserved)

## Current flow (verified 2026-07-22)

`LayeredRecoveryPipeline.recover()` runs per failed file, sequentially per unit:

1. Exact reuse of untouched children (no diagnostics in unit range)
2. Minimal-unit exact reparse (`module.parseUnit`)
3. `RecoveryRule` proposals (only Utf8BomRule + OracleTableAliasAsRule exist)
4. Context reconstruction candidates
5. Agent ×3 with `FailureEnvelopeFactory` sending the **entire unitText** (≤65,536 chars),
   edit range `0..unitText.length()` — the defect this feature removes

Adoption = `QualityDecision.accepted && strictlyBetter && children nonempty`. No wave loop, no
cross-unit parallelism, no token-level semantic classification of edits.

## Target architecture

```
recovery/
├─ localization/            NEW  Parser-owned error spans and slices
│  ├─ ErrorSpan             (root-cause span, diagnostics group, confidence)
│  ├─ ContextSlice          (level L0..L3, text, file/unit/slice offsets, snapshotSha, anchors)
│  ├─ ErrorSpanLocator      Core deterministic locator (token stream + bracket/string/BEGIN-END scan)
│  └─ SliceBudget           (max chars per level, stop→L4/REVIEW_REQUIRED)
├─ candidates/              NEW  deterministic grammar-guided minimal edits
│  ├─ TokenEditCandidate    (insert/delete/replace one token, cost, provenance)
│  ├─ GrammarGuidedEditEngine  (expected-token set → ranked candidates → reparse eval)
│  └─ EditClassification    (SAFE_WHITESPACE / STRUCTURAL_TOKEN / IDENTIFIER / LITERAL /
│                            OPERATOR / CONTROL / TRANSACTION → gate tier)
├─ repair/                  CHANGED
│  ├─ FailureEnvelope v2    sourceExcerpt := ContextSlice text only; 3-frame offsets + hash
│  ├─ FailureEnvelopeFactory  builds from ContextSlice, never from full unitText
│  └─ PatchProposalValidator  validates in slice frame, maps to unit frame, stale-hash reject
├─ orchestration/           NEW  wave loop + concurrency
│  ├─ RepairWavePlanner     groups diagnostics into independent groups (file/unit/non-overlap)
│  ├─ RepairWaveRunner      ≤3 waves, re-snapshot + full reparse between waves,
│  │                        fingerprint/no-progress/oscillation termination
│  └─ TokenBudgetSemaphore  weighted by prompt chars, backpressure, timeout, cancellation
└─ (existing) rules/ quality/ workingcopy/ evidence/ reports/ diagnostics/ unchanged contracts
```

`LayeredRecoveryPipeline` keeps its public signature (`recover(...)`) and its stages 1–4;
stage 5 is replaced by: localization → deterministic candidates → (only if none pass) Agent with
minimal slice → gate. The wave loop wraps stages for units with multiple diagnostic groups.

### LanguageModule SPI additions (default-methods only — zero breakage for existing modules)

- `default ErrorSpanLocator errorSpanLocator()` → Core locator
- `default RepairProfile repairProfile()` → empty profile (no weights, no extra forbidden edits)
- `default Optional<SemanticValidator> semanticValidator()` → empty (limits auto-accept tier)

### Adoption gate decision table (deterministic)

| Edit classification | Tier |
|---|---|
| BOM/encoding/whitespace, token stream unchanged | auto-accept → RECOVERED_SAFE |
| Single structural token (delimiter, dialect keyword) from expected set, strict reparse 0 errors + 0 ANTLR recoveries, fingerprint delta only at edit site | RECOVERED_VALIDATED |
| identifier / literal / operator / control-flow / transaction / dynamic-SQL tokens | hard reject → REVIEW_REQUIRED |
| ≥2 equally ranked candidates, unclear root cause, L4 | REVIEW_REQUIRED |

Fingerprint = token-type sequence + identifier/literal multiset outside the edit span; computed
from the language lexer, no parser needed.

## Design decisions

- **D1**: Slice levels are computed from the ANTLR token stream (already produced by first
  parse), not by re-lexing; string/comment/bracket balance decides L1..L3 growth. CPCT+ is not
  ported (LR-only); only its ranking principles (min cost, parses-further-wins) are reused.
- **D2**: Envelope schemaVersion bumps to 2.0.0; prompt tells the model offsets are
  slice-relative; validator re-anchors via expectedText + 3-frame offsets + snapshot hash.
- **D3**: Deterministic engine runs before Agent (FR-033); Agent input = same slice the engine
  used, plus rejected-candidate feedback.
- **D4**: Cross-unit Agent calls run in parallel under TokenBudgetSemaphore; within a unit,
  groups run sequentially unless provably independent (non-overlapping spans, no unclosed
  delimiter before the later span).
- **D5**: Existing sidecar/stream schemas keep their fields; new data (slice level, chars sent,
  ratio, classification, wave index) are additive optional fields.
- **D6**: `StructuredRepairAgent` constructor keeps provider-independence; SGLang options
  (thinkingEnabled/topK) and OpenAI reasoningEffort remain mutually exclusive at the config
  layer (documented; enforced with a construction-time check).

## Verification map (spec SC → concrete command)

- SC-001: `.\mvnw.cmd test`
- SC-002: run audit-clone parser & worktree parser on the same valid corpus → per-file byte diff
- SC-003/004: `FullCorpusRecoveryTest` with `-Dparser.full.corpus=분석대상모음` (+ Agent env for
  live run), then inspect `target/corpus-reports/*.json` + repair sidecars
- SC-005: corpus includes shopmall(Java·PostgreSQL 등), AMS(Oracle), swing_system, rwis, ukesa…
- SC-006: gate unit tests with adversarial candidate fixtures
- SC-007: onboarding rehearsal — module-isolation test proving Core-untouched registration
- SC-008: live GPU benchmark script, concurrency 1→32, report tokens/latency/throughput
