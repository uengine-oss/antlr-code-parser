# Tasks: ANTLR Layered Generic Recovery Platform

**Input**: `spec.md`, `plan.md`, `research.md`, `data-model.md`, and `contracts/`

**Tests are mandatory** and must use isolated data roots.

## Phase 1: Baseline and Contract Lock

- [x] T001 Record initial git status and SHA-256 for user-modified `JavaAstListener.java` and `PythonAstListener.java` in an ignored run report.
- [x] T002 Snapshot operational `D:/work/robo/project/data/analysis` path/hash inventory read-only.
- [x] T003 Create non-customer exact/malformed fixtures under `src/test/resources/recovery/` for all five languages.
- [x] T004 Capture current exact AST goldens and add byte/path comparison tests under `src/test/java/legacymodernizer/parser/contract/`.
- [x] T005 Add deterministic Node JSON contract assertions for property order, omitted nulls, empty children, vocabulary, hierarchy, source ranges, and repeated-run bytes.

## Phase 2: Diagnostics, Quality, and Catalog Foundation

- [x] T006 Implement recovery records under responsibility packages: `boundaries`, `diagnostics`, `quality`, and `evidence`.
- [x] T007 Implement lexer/parser collecting error listeners and deterministic diagnostic ordering under `recovery/diagnostics/`.
- [x] T008 Implement declaration coverage and quality tuple/gate under `recovery/quality/`.
- [x] T009 Implement diagnostics sidecar paths/writer outside `analysis/` and tests.
- [x] T010 Implement `LanguageDefinition`, `LanguageCatalog`, validation, and canonical checksum under `parsing/languages/`.
- [x] T011 Add canonical `src/main/resources/languages/language-catalog.json` with hashes/provenance for Java, Python, C, Oracle, and PostgreSQL.
- [x] T012 Add catalog schema/discovered-module parity tests.

## Phase 3: LanguageModule SPI and Zero-Diff Exact Path

- [x] T013 Define `LanguageModule` parse, coverage, unit-location, context, and rule-registry SPI.
- [x] T014 Implement `LanguageModuleRegistry` with Spring discovery, duplicate claim validation, and shared-extension content probing.
- [x] T015 Migrate Java, Python, C, Oracle, and PostgreSQL strategies to return parse evidence/JSON without changing listeners.
- [x] T016 Implement `ParserSelection` with module registry/catalog lookup and no central language switch.
- [x] T017 Implement `ParseOrchestrator` to validate the internal catalog, evaluate quality, write exact bytes, write sidecars, and report exact/recovered/failure counts.
- [x] T018 Run golden tests and prove exact AST path/byte diff zero before continuing.

## Phase 4: Working Copy and Deterministic Recovery

- [x] T019 Implement SHA-256, immutable `WorkingCopy`, bounded edits, reversible `SourceMap`, and unified diff audit.
- [x] T020 Implement `LayeredRecoveryPipeline` with stable source ordering, exact-sibling protection, file fallback, and proven original-line rebasing.
- [x] T021 Implement common safe normalization registry with no customer/path/literal-specific guards.
- [x] T022 Implement per-module rule registry, deterministic signatures, and promotion metadata.
- [x] T023 Implement Java class/method, Python class/function, C function, Oracle package/routine, and PostgreSQL routine unit locators.
- [x] T024 Implement context reconstruction contracts for headers/wrappers while excluding context-only AST from merged output.
- [x] T025 Add candidate quality-improvement and no-regression validation tests.

## Phase 5: Oracle First Production Recovery

- [x] T026 Implement token-aware Oracle SQL*Plus object splitting outside strings/comments.
- [x] T027 Implement Oracle standalone routine/package-context reparsing and original-line mapping without PACKAGE node creation.
- [x] T028 Implement generic Oracle safe rules for observed lexer/parser signatures, each with positive and negative regression fixtures.
- [x] T029 Run isolated `AMS_procedures.sql`, account for every detected declaration, and store recovery audit/report outside operational data.
- [x] T030 Verify no file-name/sample-literal branch and no original source hash change.

## Phase 6: Repair Agent Boundary

- [x] T031 Implement strict `FailureEnvelope`, canonical checksum, bounded excerpt, and prior validation feedback.
- [x] T032 Implement the Parser-owned `StructuredRepairAgent` with direct model-provider HTTP, forced tool output, byte/token/time bounds, and no Analyzer dependency.
- [x] T033 Complete `PatchProposal` validation coverage for checksum, bounds, ordering, overlap, minimality, no AST/full rewrite, and ambiguity rejection.
- [x] T034 Implement max-three attempt orchestration with validation feedback and Parser-owned reparse/quality adoption.
- [x] T035 Implement repair sidecar with hashes, diffs, attempts, decisions, and final status.
- [x] T036 Add fake-Agent tests for valid, invalid, ambiguous, timeout, budget, and missing-submit outcomes.
- [x] T037 Verify Analyzer contains no Parser repair endpoint, settings, prompts, tests, catalog loader, or cross-service repair reference.

## Phase 7: Internal Catalog and Rule Promotion

- [x] T038 Keep catalog validation inside Parser and leave Analyzer language/role policy unchanged.
- [x] T039 Keep the language catalog as a Parser classpath resource and publish no registry artifact to shared data or Analyzer.
- [x] T040 Implement repair-signature cluster report and reviewed Rule/Grammar candidate exporter.
- [x] T041 Add a synthetic test language module proving no common-engine or Analyzer edit is required.

## Phase 8: Full Regression and End-to-End Validation

- [x] T042 Run Parser unit/contract/integration tests with a fresh isolated `DOCKER_COMPOSE_CONTEXT`.
- [x] T043 Run Analyzer canonical unittest suite.
- [x] T044 Run Java, Python, C, Oracle, and PostgreSQL real corpus into a fresh isolated data root.
- [x] T045 Run Analyzer source→AST ingestion and downstream graph-input construction for that isolated root.
- [x] T046 Compare exact AST paths/bytes against baseline and report diff zero or stop for semantic approval.
- [x] T047 Measure per-language files, lines, errors, stages, recovery rate, unresolved ranges, wall time, and peak memory.
- [x] T048 Test repeated-run determinism and Agent attempt/audit determinism excluding explicitly non-semantic timestamps.

## Phase 9: SpecKit Analyze and Verify

- [x] T049 Trace every FR/SC to implementation and test evidence; resolve omissions.
- [x] T050 Recheck both constitutions and cross-service ownership/contract direction.
- [x] T051 Audit git diff for user WIP preservation and absence of customer-specific branches.
- [x] T052 Re-hash operational `project/data/analysis` and prove it was not modified.
- [x] T053 Produce final implementation, test, normal-diff, recovery, unresolved, performance, and unverified-item report.

## Phase 10: Responsibility and Naming Structure

- [x] T054 Audit the Parser inventory and freeze `structure-audit.md` naming rules, target tree, move map, and protected exceptions.
- [x] T055 Move API, intake, parsing orchestration, language contract, and language-module owners to their target packages without shims, except the documented protected-listener progress type.
- [x] T056 Move recovery records and helpers from generic `model/source/unit/agent/audit` buckets to `boundaries/diagnostics/quality/evidence/workingcopy/repair/reports`.
- [x] T057 Co-locate C, Java, Oracle, PostgreSQL, and Python module/locator/rule files and rename ambiguous internal classes.
- [x] T058 Audit method/variable names in all changed runtime files and remove vague or pattern-only naming where a domain name exists.
- [x] T059 Verify required/missing/forbidden paths, active old imports 0, full/golden/conditional-live tests, and unchanged protected/operational hashes. The live provider call remains explicitly unconfigured and is reported in `verification-report.md`.

## Dependencies

- T001-T005 block all implementation.
- T006-T012 block module migration and recovery.
- T013-T018 block deterministic recovery.
- T019-T025 block Oracle and Agent adoption.
- T026-T030 provide the first real recovery proof.
- T031-T037 block live Parser-owned Agent integration.
- T038-T041 block internal catalog and rule-promotion validation.
- T042-T053 are mandatory completion gates.
