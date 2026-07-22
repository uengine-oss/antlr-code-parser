# Tasks: Parser-Owned Localized Repair Platform

Evidence rule: a task is complete only with the listed proof. 검증됨/실패함/미검증 구분 보고.
Detailed evidence: [verification-report.md](verification-report.md).

## Phase 0 — Footing

- [x] T001 Reproduce known test failure (`mvnw test`: 70 run, F2/E1/S6) — 2026-07-22 12:55 KST
- [x] T002 Fix `StructuredRepairAgentTest` helper to 12-arg constructor (reasoningEffort=null);
      suite green 3/3, full suite green — 2026-07-22 12:56 KST
- [x] T003 Extract real Node type inventory (50 types from 175 AST files) → spec.md
- [x] T004 Baseline contract cross-check vs audit clone f0d21ba on the full 분석대상모음 corpus:
      175 common AST files → 126 byte-identical, 46 valid files differ ONLY by intended
      control-flow additions (IF/ELSE/TRY/CATCH/LOOP/SWITCH/CASE — inherited spec-007 listener
      extension), 3 remaining diffs root-caused (AMS recovery gains; swing_system main.c is an
      invalid input where the recovery path differs from baseline's silent ANTLR-recovered
      output; ukesa PCReferenceInformation was a baseline hierarchy bug — enclosing method
      truncated at 4007, anonymous methods floated under CLASS — fixed by current output).
      **Zero removals/renames on valid inputs.**

## Phase 1 — Localization & minimal transfer

- [x] T010 `recovery/localization/` (SliceSyntax, SliceLevel, ContextSlice, ErrorSpanLocator)
      + per-language SliceSyntax overrides. Tests: ErrorSpanLocatorTest 11/11.
- [x] T011 FailureEnvelope schema 2.0.0: slice-based excerpt, 3-frame offsets + slice hash,
      read-only declarationHeader; DiagnosticEvidence offsets renamed to excerpt frame;
      contract fixture v2 (v1.1 removed); validator maps slice→unit frame.
- [x] T012 Pipeline ladder L1→L2→L3 per Agent retry; size-skip removed (86,026-char units now
      served as 90–3,972-char slices, median transfer ratio 1.3%); AgentRequestEvidence records
      chars/ratio per request. Test: FailureEnvelopeSliceTest.

## Phase 2 — Deterministic candidates & gate

- [x] T020 `recovery/candidates/` GrammarGuidedEditEngine driven by ANTLR's own signals
      (extraneous/missing/mismatched + profile-gated no-viable-alternative deletion).
      Tests: GrammarGuidedEditEngineTest 8/8 incl. determinism.
- [x] T021 EditClassification tiers; risk-class keywords rejected even when profile-listed.
      SC-006 adversarial tests: identifier/literal/operator/transaction deletions never
      auto-adoptable.
- [x] T022 Engine-before-Agent ordering; ambiguity (≥2 strict survivors) → REVIEW_REQUIRED
      without Agent. DeterministicEngineRecoveryTest: Oracle alias AS repaired with Agent
      disabled; agent-path tests pinned via empty-profile module.

## Phase 3 — Waves, parallelism, real-corpus verification

- [x] T030 Engine wave loop ≤3 with fingerprint/no-progress/oscillation guards and
      parses-further ranking (CPCT+ principle). Test: two independent dialect errors converge
      across waves deterministically.
- [x] T031 TokenBudgetSemaphore (char-weighted, backpressure, timeout) gating all Agent calls;
      TokenBudgetSemaphoreTest 4/4. **Deliberately deferred**: parallelizing the per-unit
      pipeline loop itself — thread-safety of all five language modules under concurrent
      parseUnit is unproven; concurrency is exercised at the Agent-request layer (T034).
- [x] T032 Full-corpus deterministic-only rerun (106,120 lines, 175 files, java/python/c/
      oracle; corpus has no postgresql source files — same as baseline run): **identical unit
      outcomes to the previous live-agent run (EXACT 4 / RECOVERED 24 / REVIEW_REQUIRED 10)
      with 0 Agent calls and 52s vs 146s.** Original inventory unchanged.
      Report: target/corpus-reports/full-corpus-deterministic-012-final.json
- [x] T033 AMS live reruns: Qwen3.6 (×2) and GPT-5.4-mini (×1). Slice transfer proven
      (90–3,972 chars vs units up to 86,026; ratios 0.11%–41%, median 1.3%). Validator
      unique-match re-anchoring cut EXPECTED_TEXT_MISMATCH 14→3. Final: 0 adoptions — all 30
      calls per run end rejected or model-declared ambiguous. Root cause of all 10 unresolved
      units: **the AMS dump has line-wrapping corruption** (comments split across lines,
      tokens glued like `ETL_JOB_LOGSET JOBRESULT`) — multi-token, semantically ambiguous
      repairs. REVIEW_REQUIRED is the correct fail-closed outcome per spec (not a target of
      forced adoption). Reports: ams-live-agent-012.json / -012b-reanchor.json /
      ams-live-gpt54mini-012c.json
- [x] T034 GPU concurrency benchmark 1→32 with real prompts under the token budget →
      target/corpus-reports/gpu-concurrency-benchmark.json

## Phase 4 — Maintainability & docs

- [x] T040 Onboarding rehearsal: NewLanguageOnboardingRehearsalTest registers a new language
      through the public SPI with zero production-file changes; recovery defaults inherited.
- [x] T041 Specs 009/010/011 annotated with correction headers; CLAUDE.md points to 012;
      README updated (language onboarding, sidecar states, Agent config, commands).
- [x] T042 Verification report with 검증됨/실패함/미검증 separation; Analyzer tracked-diff 0;
      final full suite green.
