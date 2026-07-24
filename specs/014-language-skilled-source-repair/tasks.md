# Tasks: Language-Skilled Verified Source Repair

## Phase 1 - Contracts

- [x] T001 Record user scenarios, safety boundary, and opt-in source-write contract
- [x] T002 Record architecture, data model, verification, and rollback

## Phase 2 - Skills

- [x] T003 Initialize and write common, Java, Python, C, Oracle, PostgreSQL `SKILL.md` packages
- [x] T004 Validate all Skill packages with `quick_validate.py`
- [x] T005 Add malformed/missing/path-traversal Skill catalog tests

## Phase 3 - Agent prompt assembly

- [x] T006 Implement immutable classpath `RepairSkillCatalog`
- [x] T007 Implement deterministic `RepairPromptAssembler`
- [x] T008 Integrate prompt assembly into `StructuredRepairAgent`
- [x] T009 Prove common + exactly one language skill in captured requests

## Phase 4 - Verified source materialization

- [x] T010 Propagate accepted working text through engine and Agent outcomes
- [x] T011 Materialize non-overlapping accepted unit edits only for full recovery
- [x] T012 Add `VerifiedSourceRepairApplier` with opt-in, hash, charset, and atomic-write gates
- [x] T013 Integrate application after audit evidence is written
- [x] T014 Test disabled, applied, stale, lossy, partial, no-op, and charset paths

## Phase 5 - Verification

- [x] T015 Run focused repair/skill/source-application tests
- [x] T016 Run the full Maven test suite
- [x] T017 Rerun the water corpus and record exact results
- [x] T018 Run authenticated live GPU validation using the Analyzer `.env` secret without exposing it
- [x] T019 Audit final diff, preserve user WIP, and write verification report
