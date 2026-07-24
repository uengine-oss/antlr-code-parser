# Implementation Plan: Language-Skilled Verified Source Repair

**Spec**: [spec.md](spec.md) | **Created**: 2026-07-24

## Technical Context

- Java 17, Spring Boot 3.3, ANTLR 4.13.2, Jackson, Java `HttpClient`
- Existing `StructuredRepairAgent`, `FailureEnvelope`, three-level Agent ladder,
  `PatchProposalValidator`, `WorkingCopy`, and `parseUnit` quality gates
- No new framework, sidecar, parser grammar, AST schema, endpoint, or analyzer dependency

## Constitution Check

- Language detection remains content/extension based; skills consume the detected
  `languageId` and do not trust client metadata.
- Language skill lookup is convention based (`<languageId>-syntax-repair`) with no central
  language switch, preserving zero-Core language onboarding.
- AST JSON and streaming contracts remain unchanged.
- Source write-back is an internal, explicit opt-in operation after the existing accepted
  recovery decision and does not alter replace-all upload semantics.

## Design

```text
FailureEnvelope
  -> RepairSkillCatalog
       -> common-syntax-repair/SKILL.md
       -> <languageId>-syntax-repair/SKILL.md
  -> RepairPromptAssembler
  -> existing StructuredRepairAgent tool call
  -> existing PatchProposalValidator + semantic gates
  -> WorkingCopy + parseUnit/full-file reparse
  -> RecoveryOutcome.repairedSource (only fully accepted direct edits)
  -> VerifiedSourceRepairApplier (opt-in, hash-bound, same charset, atomic replace)
```

### Skill loading

- Load classpath resources by fixed convention, validate a safe language identifier, parse
  strict YAML frontmatter containing only `name` and `description`, and cache immutable skills.
- Append skills inside explicit XML-like boundaries after the stable base prompt.
- Treat all skill text as instructions; never include source snippets or corpus-specific names.

### Repaired-source propagation

- Carry the accepted working text through deterministic-engine and Agent results.
- For unit repairs, convert each accepted repaired unit into one non-overlapping file edit and
  materialize it only when the assembled file status is fully recovered.
- Context reconstruction without a direct text edit may recover AST but cannot produce a
  write-back candidate.

### Source application

- Default disabled.
- Re-read bytes immediately before writing; compare SHA-256 with the recovery snapshot.
- Decode strictly with the existing codec and refuse its lossy fallback.
- Encode using the detected charset, write a unique sibling temporary file, then move with
  `ATOMIC_MOVE + REPLACE_EXISTING`, falling back to `REPLACE_EXISTING` only when atomic move is
  unsupported.
- Emit an explicit progress event after a confirmed write.

## Verification

1. Skill validator for all six packages.
2. Unit tests for catalog validation and language-isolated prompt assembly.
3. Existing HTTP capture test proving the composed prompt reaches the GPU request unchanged.
4. Unit tests for repaired-source propagation and verified application.
5. Focused recovery suite, then full Maven suite.
6. Water corpus rerun and live GPU run when credentials are present.

## Rollback

- Set `PARSER_REPAIR_APPLY_TO_SOURCE=false` (default) to disable all writes.
- Remove Skill assembler injection to return to the byte-identical previous base prompt.
- No data migration or grammar rollback is required.
