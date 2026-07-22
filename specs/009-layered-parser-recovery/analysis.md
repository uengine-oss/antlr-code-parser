# SpecKit Analysis: ANTLR Layered Generic Recovery Platform

**Final verification date**: 2026-07-21

## Result

All fixed-contract, ownership, recovery, structure, and deterministic test requirements are
implemented. The only environment-dependent check not executed is a billable live Repair Agent
provider call; disabled-provider behavior and fake-provider success/failure/budget/timeout paths
are verified. This is reported explicitly and does not weaken Parser-side proposal validation.

## Requirement Traceability

| Requirements | Implementation evidence | Test/report evidence | Result |
|---|---|---|---|
| FR-001..004 fixed AST contract | exact fast path in `ParseOrchestrator`; recovery artifacts under `diagnostics/` and `repairs/` | `AstJsonGoldenContractTest`; repeat corpus AST hash | PASS |
| FR-005..009 diagnostics/quality | collecting listeners, `CountingErrorStrategy`, coverage counters, `ParseQualityGate`, diagnostics sidecar v1.1 | malformed fixtures and `ParseDiagnosticsQualityTest` | PASS |
| FR-010..014 modules/catalog | `LanguageModule`, Spring registry, catalog validator and pinned local grammar hashes | `LanguageCatalogValidationTest`, mixed-SQL selection, synthetic module | PASS |
| FR-015..022 layered recovery | immutable working copies, source maps, deterministic unit locators, ordered rules/contexts, Oracle member context | working-copy, context, cross-language, Oracle unit/package and AMS tests | PASS |
| FR-023..028 Repair Agent | `StructuredRepairAgent`, forced tool response, `FailureEnvelope`, proposal validator, max-three pipeline | fake provider, invalid/ambiguous/timeout/budget/missing-submit tests | PASS; live call not configured |
| FR-029..034 operations | status-aware progress, isolated roots, corpus reports, promotion report | corpus runs 2/3, AMS and PostgreSQL reports, Analyzer read-only ingestion | PASS |
| FR-035..039 structure/naming | responsibility packages and co-located language modules | required/forbidden/import/type/package audits and full suite | PASS |

## Success Criteria

| Criterion | Evidence | Result |
|---|---|---|
| SC-001 | exact golden relative paths and bytes unchanged; corpus run 2/3 AST SHA-256 identical | PASS |
| SC-002 | all malformed fixtures are non-EXACT with collected diagnostics | PASS |
| SC-003 | AMS 33/33 units accounted: 23 recovered, 10 review-required | PASS |
| SC-004 | original corpus and AMS/PostgreSQL hashes unchanged; edits bounded by validator/source map | PASS |
| SC-005 | synthetic catalog/module test requires no common-engine or Analyzer change | PASS |
| SC-006 | unchanged Analyzer built 17,074 graph nodes and 54,607 graph-input edges from 176 ASTs, including real PostgreSQL DDL AST | PASS |
| SC-007 | corpus elapsed 78.337s/89.109s, peak heap 4,993,445,888/5,052,426,752 bytes; exact overhead 0.053%/0.079% | PASS |
| SC-008 | ten unresolved AMS procedures have exact names, line ranges, diagnostics, and disabled-Agent reason; no invented node | PASS |
| SC-009 | required missing 0, forbidden present 0, active old imports 0, type/file mismatch 0, package/path mismatch 0 | PASS |

## Ownership and Contract Findings

1. Parser alone owns diagnostics, working copies, recovery rules, Agent calls, proposal
   validation, reparsing, adoption, AST serialization, and recovery sidecars.
2. Analyzer remains an unchanged AST JSON consumer. Its canonical 272-test suite passed and its
   read-only ingestion constructed graph inputs without Parser repair logic or catalog files.
3. `PROCEDURE`, `QUERY`, and similar values remain listener-emitted Node types. Recovery statuses
   never become Node types, and Oracle package context creates no PACKAGE AST node.
4. ANTLR 4.13.2 and the current pinned grammar files remain in place. No grammar refresh is
   justified without a separate pinned A/B candidate that preserves every exact golden byte.
5. No runtime branch contains AMS, RWIS, file-name, customer, or sample-literal conditions.

## Remaining External Verification

A live model-provider invocation requires explicit provider URL/model/key and may incur cost.
No credential was inferred or consumed. When configured, run the conditional live test on a
non-customer fixture and retain its audit sidecar; all acceptance authority remains in Parser.
