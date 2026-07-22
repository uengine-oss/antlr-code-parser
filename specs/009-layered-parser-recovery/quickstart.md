# Quickstart: Verification Scenarios

All commands must set `DOCKER_COMPOSE_CONTEXT` to a newly created isolated data directory. Never point tests at `D:/work/robo/project/data`.

## 1. Fixed-contract golden test

1. Copy only committed, non-customer fixtures into isolated `source/`.
2. Run the baseline exact parser and capture AST paths plus SHA-256.
3. Run the layered parser twice.
4. Assert path set and bytes are identical for every first-pass exact file.
5. Assert diagnostics exist outside `analysis/`.

## 2. False-success detection

Parse fixtures containing one lexer error, one parser recovery, and one missing-declaration case. Assert all are non-EXACT and each sidecar identifies a bounded source position and reason.

## 3. Oracle minimal-unit recovery

Parse an isolated copy of `AMS_procedures.sql`. Verify:

- the original hash before and after is identical;
- declaration candidates are in deterministic source order;
- exact candidates bypass transforms;
- only failed candidates run rules/Agent;
- every declaration candidate has a final status;
- accepted AST line numbers reference original lines;
- no PACKAGE or UNRESOLVED Node is introduced.

## 4. Agent boundary

With a fake adapter, test valid minimal, out-of-range, full-rewrite, AST-producing, ambiguous, and no-submit responses. With an explicitly configured model provider, run one bounded non-customer fixture and retain the audit sidecar. The call originates from the Java Parser and never routes through Analyzer.

## 5. Internal language catalog

Validate the internal language catalog, then verify accepted AST with the existing Analyzer unchanged. Test unsupported major, checksum mismatch, module drift, and a test module entry. Parser must fail before parsing when its internal catalog is invalid.

## 6. Full-corpus validation

Use a fresh isolated run root, intake `D:/work/robo/분석대상모음`, parse every Parser-routed
Java/Python/C/Oracle source, and then point Analyzer at the isolated data root. Record
per-language counts, errors, recovery stages, unresolved ranges, AST hashes, graph input counts,
wall time, and peak memory. Exercise the available PostgreSQL DDL as described below.

The current corpus contains PostgreSQL DDL but no PostgreSQL procedural source file. Keep the
normal content-based intake behavior (DDL stays in `ddl/`) and additionally run
`PostgreSqlCorpusCompatibilityTest` against `rwis/ddl/RWIS_postgres_ddl_UPPER.sql` to prove the
PostgreSQL ANTLR module's exact, deterministic AST and unchanged-source behavior. Analyzer
compatibility is then checked against that isolated AST together with the normal corpus ASTs.

## 7. Final audit

- `git diff` confirms user WIP listeners were not overwritten.
- Operational `project/data/analysis` hash snapshot is unchanged.
- Normal AST diff is zero.
- Every unresolved unit is bounded and reported.
- No sample/customer-specific conditional exists.
- Grammar provenance and A/B evidence are recorded.
