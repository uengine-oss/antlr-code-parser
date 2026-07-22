# Data Model: ANTLR Layered Generic Recovery Platform

## LanguageCatalog

- `schemaVersion`: semantic version of the Parser-internal catalog schema.
- `catalogVersion`: content version.
- `parserBuild`: parser artifact/build identity.
- `antlrRuntimeVersion`: runtime version.
- `languages[]`: ordered `LanguageDefinition` entries.
- `catalogSha256`: checksum of canonical catalog content excluding this field.

## LanguageDefinition

- `id`, `aliases`, and `family`.
- `parseExtensions`: lowercase dot-prefixed extensions that Parser currently parses.
- `sharedExtension`: whether Parser content probes must arbitrate a parse extension.
- `entryRules`: whole-file and unit-kind rule names.
- `unitKinds`: supported recovery boundaries.
- `emittedNodeTypes`: frozen Node types this module can emit.
- `routineNodeTypes`: existing routine types emitted by this Parser module.
- `grammar`: local hashes and optional upstream repo/commit.
- `recoveryRuleSets`: ordered rule-set identifiers.

## SourceUnit

- `unitId`: deterministic hash of language, file hash, kind, original range, and ordinal.
- `kind`: FILE, CLASS, METHOD, FUNCTION, PROCEDURE, PACKAGE, TRIGGER, or FRAGMENT.
- `name`: discovered name when safely available.
- `parentUnitId`: enclosing unit or null.
- `startOffset`, `endOffset`, `startLine`, `endLine`: original coordinates.
- `ordinal`: stable source order.
- `boundaryConfidence`: EXACT or CONSERVATIVE.

## UnitParseContext

- `contextId`: deterministic language-owned context strategy identifier.
- `sourceText`: parse-only wrapper/header text; never serialized as a Node.
- `originalUnitStartLine`: coordinate rebase anchor for the failed unit.
- `unitStartOffset`, `unitEndOffset`: failed-unit range inside the context candidate.

A module may return more than one context candidate in deterministic order. For example, C
can try the original preprocessor branch and then a line-count-preserving alternate branch.
Oracle package-member context wraps only the failed member and discards wrapper-only AST.

## ParseAttempt

- `attemptId`, `unitId`, `stage`, `ruleId`, `attemptNumber`.
- `sourceSha256`, `workingSha256`, `grammarRevision`.
- `diagnostics[]`, `coverage`, `antlrRecoveryCount`.
- `elapsedMillis`, `nodeCount`, `qualityDecision`.

## ParseDiagnostic

- `phase`: LEXER, PARSER, COVERAGE, VALIDATION, AGENT, or SYSTEM.
- `severity`, `code`, `message`.
- `originalLine`, `originalColumn`, `workingLine`, `workingColumn`.
- `offendingToken`, `expectedTokens`, `ruleStack`, `tokenWindow`.
- `unitId`, `recoverable`.

## CoverageEvidence

- `declarationsDiscovered`, `declarationsEmitted`.
- `byKind`: discovered/emitted counts per kind.
- `missingUnits[]`: deterministic unit ids/ranges.
- `structuralChecks[]`: named pass/fail invariants.

## QualityDecision

- `status`: EXACT, RECOVERED_SAFE, RECOVERED_VALIDATED, REVIEW_REQUIRED, PARTIAL, UNRESOLVED, or FAILED.
- `accepted`: whether the attempt may contribute AST.
- `qualityTuple`: ordered values used for deterministic comparison.
- `reasons[]`: stable reason codes and messages.

The quality tuple is compared lexicographically in this direction: fatal/system errors, unmappable ranges, missing declaration count, parser error count, lexer error count, structural failure count, edit breadth, edit count. Lower is better. Exact output is never replaced by a recovered candidate.

## WorkingCopy and SourceMap

- `originalSha256`, `workingSha256`, `text`.
- `edits[]`: ordered non-overlapping `TextEdit` values.
- `sourceMap`: piecewise mappings from working offsets/lines to original coordinates.

`TextEdit` contains original-relative `startOffset`, `endOffset`, `replacement`, `ruleId`, and `reason`. Original files are never writable through this model.

## FailureEnvelope

- Contract and source identity: schema version, language/module, grammar, file/unit hashes and original range.
- Failure evidence: exact diagnostics, expected tokens, rule stack, token window, coverage, prior attempts.
- Bounded source: failed-unit excerpt only, with absolute original coordinates.
- Constraints: allowed range, maximum changed characters/lines, forbidden outputs, remaining attempts.

## PatchProposal

- `proposalId`, `failureEnvelopeHash`.
- `edits[]`: ordered range replacements inside the unit.
- `rationale`, `confidence`, `ambiguities[]`.
- No AST field and no complete source-file field are permitted.

## RepairAudit

- Original/working hashes, source unit, attempts, proposals, validation outcomes.
- Accepted proposal/rule or null.
- Unified diff and source map summary.
- Final sidecar status and promotion signature.

Rule-based edits carried into context reconstruction retain the same ordered edits, unified
diff, and source map. A separate deterministic promotion report clusters successful signatures
and emits reviewable rule/grammar candidate metadata plus a regression-fixture template. It
never changes a grammar or enables a rule automatically.

## State Transitions

```text
FIRST_PASS
  ├─ exact ───────────────────────────────> EXACT
  └─ failed quality gate -> UNIT_ISOLATION
       ├─ exact unit reparse ─────────────> RECOVERED_VALIDATED
       └─ remaining -> SAFE_RULES -> LANGUAGE_RULES -> CONTEXT
            ├─ validated ─────────────────> RECOVERED_SAFE/VALIDATED
            └─ remaining -> AGENT (0..3 when configured)
                 ├─ validated unambiguous > RECOVERED_VALIDATED
                 ├─ ambiguous ────────────> REVIEW_REQUIRED
                 ├─ unavailable/disabled ─> REVIEW_REQUIRED
                 └─ exhausted ────────────> UNRESOLVED/FAILED
```

Statuses are sidecar values and never Node types.
