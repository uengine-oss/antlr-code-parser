# Final Verification Report

**Date**: 2026-07-21  
**Scope**: `antlr-code-parser` implementation; `robo-data-analyzer` read-only validation only

## Outcome

ANTLR 4.13.2 and the current listener/Node JSON contract remain the core parser. Failed input is
handled inside Parser by exact minimal-unit retry, generic safe rules, language rules, language
context reconstruction, and finally an optional structured Repair Agent. Exact files bypass all
recovery work and retain their prior AST path and bytes.

## Fixed Contract

- Golden AST changed relative paths: **0**.
- Golden AST changed bytes: **0**.
- Corpus runs 2 and 3 AST inventory SHA-256:
  `c545d2834e5c7281130d9ca9a5d884b0c90f1f9bce051fb3b0bf26360d7fa8c0`.
- Repeat semantic diagnostics SHA-256:
  `4152cc11d705a7e9fd399754a6335d49e233cad5db1aba19235286713788d78c`.
- Repeat semantic repair-audit SHA-256:
  `73f225ed479c8150ca3a6e35d2ee69e9188a65a8b846cf2e978366d7225ca03c`.
- All 21 compared semantic/count/status fields matched between repeat runs.
- Diagnostics, repair evidence, source maps, and Agent history remain outside `analysis/`.

## Full Corpus

Input: `D:/work/robo/분석대상모음`  
Isolated output: `target/test-data`  
Original inventory before/after:
`bd2df17208f169c2ad60b65def1fd0ad425b6ca19a9166fdd4d561dcd5de68ad`

| Language | Files | Lines | Lexer errors | Parser errors | ANTLR recoveries | File result |
|---|---:|---:|---:|---:|---:|---|
| Java | 94 | 25,454 | 0 | 0 | 0 | 94 EXACT |
| Python | 18 | 1,088 | 0 | 0 | 0 | 18 EXACT |
| C | 53 | 60,546 | 0 | 1 | 2 | 52 EXACT, 1 RECOVERED_VALIDATED |
| Oracle | 10 | 19,032 | 4 | 1 | 1 | 9 EXACT, 1 PARTIAL |
| PostgreSQL procedural source | 0 | 0 | 0 | 0 | 0 | corpus contains DDL, not procedural source |
| **Total Parser files** | **175** | **106,120** | **4** | **2** | **3** | **175 AST files** |

The normal content classifier routed five DDL files to `ddl/`, including the available
PostgreSQL corpus. A separate isolated compatibility run parsed
`RWIS_postgres_ddl_UPPER.sql` directly with the PostgreSQL ANTLR module: status EXACT,
diagnostics 0, recoveries 0, deterministic AST SHA-256
`1caea37943f2d199c4a3dcc5ca66b90ae19314437aee8ac462263863b6682533`, and unchanged original
SHA-256 `a16d312240c867ea99753ccace9052d0aa337d2d0d4c25d5532994ee73bce7db`.

## Recovery and Unresolved Units

AMS file boundaries: **33/33 accounted**.

- 20 `RECOVERED_VALIDATED`.
- 3 `RECOVERED_SAFE`.
- 10 `REVIEW_REQUIRED` because the real Agent was explicitly disabled.
- Actual Agent calls: **0**; explicit disabled-Agent skips: **10**.
- Original AMS SHA-256 remained
  `e464a0ede0716a2343c23522b55abfbae3fff95113933d34a02374578828619c`.

| Procedure | Lines | Final stage |
|---|---:|---|
| `"AMS_MRT"."SP_MIG_TO_MRT_TRANS_MRT"` | 6-2491 | REPAIR_AGENT_SKIPPED |
| `"AMS_MIG"."SP_TD_AB00023"` | 2690-5120 | REPAIR_AGENT_SKIPPED |
| `"AMS_MIG"."SP_TD_AB00023_P2"` | 5127-6540 | REPAIR_AGENT_SKIPPED |
| `"AMS_MIG"."SP_TD_AB00028"` | 6784-7131 | REPAIR_AGENT_SKIPPED |
| `"AMS_MIG"."SP_TD_AB00035_BAK"` | 7520-7878 | REPAIR_AGENT_SKIPPED |
| `"AMS_MIG"."SP_TD_AB00038"` | 9522-9734 | REPAIR_AGENT_SKIPPED |
| `"AMS_MIG"."SP_TD_AB00048"` | 10105-10343 | REPAIR_AGENT_SKIPPED |
| `"AMS_MIG"."SP_TM_AB00001"` | 10825-11286 | REPAIR_AGENT_SKIPPED |
| `"AMS_MRT"."SP_MIG_TO_MRT_TRANS_MRT"` | 13997-15319 | REPAIR_AGENT_SKIPPED |
| `"AMS_MRT"."SP_MIG_TO_MRT_TRANS_MRT_20250902"` | 15323-16461 | REPAIR_AGENT_SKIPPED |

Each row retains exact diagnostics and `REPAIR_AGENT_DISABLED`; none creates PACKAGE,
UNRESOLVED, or other invented AST nodes.

## Performance

| Run | Wall time | Peak heap | Exact first pass | Exact processing | Exact overhead |
|---|---:|---:|---:|---:|---:|
| 2 | 78.337s | 4,993,445,888 B | 69.823s | 69.860s | 37ms / 0.053% |
| 3 | 89.109s | 5,052,426,752 B | 78.315s | 78.377s | 62ms / 0.079% |

Both are below the 20% exact-path overhead limit.

## Analyzer Read-Only Compatibility

- Analyzer source/config/spec changes made by this feature: **0**.
- Parser-repair/catalog contamination references in Analyzer: **0**.
- Analyzer canonical suite: **272 passed, 5 conditionally skipped, 0 failed**.
- Analyzer loaded **176 AST files**, including the isolated real PostgreSQL AST.
- Graph inputs constructed: **17,074 nodes**, **54,607 edges**, 18,735 call sites, and 1,355 import references.
- PostgreSQL corpus AST alone produced 4,006 code nodes, one module, and one package container.
- One empty-code warning belongs to the intentionally empty `library_system/src/__init__.py`
  module (source exists and is zero bytes), not an ingestion failure.

## Test Execution

- Final Parser suite: **63 tests, 0 failures, 0 errors, 4 conditional skips**.
- The three corpus-conditioned skips were run separately with real paths and passed:
  full corpus twice, AMS Oracle corpus, and PostgreSQL DDL corpus.
- The remaining conditional skip is the explicitly unconfigured live model-provider call.
- Analyzer canonical suite: **272 tests, 0 failures, 5 conditional skips**.

## Structure and Safety Audit

- Required responsibility paths missing: **0**.
- Forbidden moved paths present: **0**.
- Active imports of superseded packages: **0**.
- Public type/file mismatches: **0**.
- Package/path mismatches: **0**.
- Customer/file/sample-specific runtime branches: **0**.
- Java listener SHA-256 preserved:
  `9e8b580b321900c9c567f56751016a4b03ca4f05417b761d56f4647fac26d31e`.
- Python listener SHA-256 preserved:
  `724ef75ed2bb9157f9161fd27db373762c23c05c0be887f9cbb2af8ea13d3884`.
- Operational `project/data/analysis`: 12 files, 539,475 bytes; baseline/final canonical
  inventory SHA-256 both
  `ca3a3ebd76fee1406b718942b131e51daf74a1b610a85c08c37154b126db2840`.

## Explicitly Unverified External Item

A live model-provider Agent call was not made because no provider URL/model/key was explicitly
configured and such a call may incur cost. The conditional live test remains available.
Deterministic fake-provider tests cover successful adoption, rejection, ambiguity, retry
feedback, timeout, budget, checksum, bounds, overlap, full rewrite, and missing-submit behavior.
