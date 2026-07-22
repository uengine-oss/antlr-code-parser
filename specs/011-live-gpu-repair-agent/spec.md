# Feature Specification: Live GPU Repair Agent

**Status**: Plumbing verified on a synthetic fixture only — effectiveness claim superseded by [012-parser-owned-localized-repair](../012-parser-owned-localized-repair/spec.md)  
**Date**: 2026-07-22

> **Correction (2026-07-22)**: "Completed" referred to a 115-char synthetic Oracle fixture. On the
> real AMS corpus the Agent was called 21 times with 0 adopted patches (EXPECTED_TEXT_MISMATCH 8,
> AMBIGUOUS_PROPOSAL 4, NO_OP_EDIT 2, MISSING_SUBMIT 1; 3 size skips; 10 units REVIEW_REQUIRED).
> `RECOVERED_VALIDATED` unit counts in sidecars are minimal-unit exact parses, not Agent repairs.

## Purpose

Connect the Parser-owned repair boundary to the already supplied OpenAI-compatible SGLang
GPU service and prove a real failed parse can be repaired without changing the source file or
the established AST Node JSON contract.

## Frozen contracts

- Analyzer code and Analyzer runtime behavior remain unchanged.
- AST JSON paths, root shape, fields, types, node names, hierarchy, ordering, coordinates,
  IDs, semantics, and determinism remain unchanged for existing accepted inputs.
- An Agent proposal is never trusted directly. It is accepted only after bounded-edit
  validation and a strictly better ANTLR reparse.
- The original source file is never modified.

## Requirements

- **FR-001**: The repair client MUST optionally send the SGLang/Qwen chat-template setting
  that disables free-form thinking before a forced tool call.
- **FR-002**: Every proposed edit MUST bind an exact expected source substring in addition to
  its zero-based, end-exclusive offsets.
- **FR-003**: The validator MUST reject an edit when its expected substring does not equal the
  source excerpt at the supplied offsets.
- **FR-004**: The failure envelope MUST expose source line start offsets so an Agent can map
  line/column diagnostics to excerpt offsets without guessing.
- **FR-005**: Every retry MUST include the previous edit, resulting diagnostics, and exact
  validation reasons.
- **FR-006**: Provider failures, malformed tool results, bad offsets, and non-improving edits
  MUST remain review-required and MUST NOT alter the source or emit an accepted AST.
- **FR-007**: A live test against the supplied GPU service MUST demonstrate: first parse
  rejected, one bounded tool proposal received, proposal validated, ANTLR reparse accepted,
  final `RECOVERED_VALIDATED`, audit written, original hash unchanged.
- **FR-008**: README MUST document new-language onboarding and the Parser-owned GPU Agent
  settings without introducing an Analyzer dependency.

## Acceptance scenarios

1. Given the Oracle fixture containing `FROM APP_TABLE AS A`, the live GPU Agent removes only
   `AS`, the Parser reparses successfully, and the original fixture hash is unchanged.
2. Given a proposal whose offsets select text different from `expectedText`, validation fails
   with `AGENT_EXPECTED_TEXT_MISMATCH` before any reparse.
3. Given a failed first proposal, the next envelope contains its edits and resulting parser
   diagnostics.
4. Given Agent configuration is absent, ordinary exact parsing remains unchanged and recovery
   ends explicitly at review-required.
