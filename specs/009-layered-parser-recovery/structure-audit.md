# Responsibility and Naming Audit

## Decision

The Parser tree must reveal responsibility before a file is opened. Top-level packages name product responsibilities, and each language keeps its parser module, source-unit locator, and language-specific deterministic rules in one folder.

## Naming Rules

1. New runtime code belongs under `api`, `intake`, `parsing`, or `recovery`.
2. Generic buckets such as `model`, `service`, `source`, `unit`, `agent`, `audit`, `utils`, `helpers`, `common`, `manager`, and `data` are not destinations for new recovery code.
3. Class names state domain responsibility: `ParserWorkspace`, `ParserSelection`, `ParseOrchestrator`, `LayeredRecoveryPipeline`, and `StructuredRepairAgent`.
4. Language implementations are `*LanguageModule`, not strategies. Each is co-located under `parsing/languages/<language>` with its locator and language rules.
5. `*Writer` is reserved for one named serialized artifact. `*Registry` is reserved for validated lookup/discovery ownership.
6. Variables use domain vocabulary such as `module`, `sourceUnit`, `parseAttempt`, `qualityDecision`, `repairProposal`, `workingCopy`, and `parserWorkspace`.
7. Serialized AST and sidecar property names are contracts and are not renamed by structural refactoring.

## Runtime Tree

```text
legacymodernizer/parser/
|-- ParserApplication.java
|-- api/
|   |-- FileUploadController.java
|   |-- HealthCheckController.java
|   |-- stream/
|   |   |-- ParseEventSink.java
|   |   `-- ParseStreamEvent.java
|   `-- WebConfig.java
|-- intake/
|   |-- ParserWorkspace.java
|   `-- SourceIntakeClassifier.java
|-- parsing/
|   |-- AstCoordinates.java
|   |-- ParseOrchestrator.java
|   |-- ParserSelection.java
|   |-- RawParseResult.java
|   |-- boundaries/
|   `-- languages/
|       |-- AntlrLanguageModuleSupport.java
|       |-- LanguageCatalog.java
|       |-- LanguageCatalogValidator.java
|       |-- LanguageDefinition.java
|       |-- LanguageModule.java
|       |-- LanguageModuleRegistry.java
|       |-- c/
|       |-- java/
|       |-- oracle/
|       |-- postgresql/
|       `-- python/
|-- recovery/
|   |-- LayeredRecoveryPipeline.java
|   |-- boundaries/
|   |-- diagnostics/
|   |-- evidence/
|   |-- quality/
|   |-- repair/
|   |-- reports/
|   |-- rules/
|   `-- workingcopy/
|-- antlr/
|-- model/Node.java
`-- service/ParseProgressTracker.java
```

## Completed Move Map

| Former location/name | Current location/name | Responsibility |
|---|---|---|
| `controller` + `config` | `api` | HTTP and web boundary |
| vague two-string `StreamCallback` | `api/stream/ParseEventSink` + `ParseStreamEvent` | typed, additive NDJSON lifecycle contract |
| `FileStorageService` | `intake/ParserWorkspace` | Parser workspace paths and intake materialization |
| `IntakeClassifier` | `intake/SourceIntakeClassifier` | content-based source/DDL classification |
| `LanguageDetector` | `parsing/ParserSelection` | deterministic language-module selection |
| `ParsingOrchestrator` | `parsing/ParseOrchestrator` | file-level parse workflow |
| `service/strategy/*ParserStrategy` | `parsing/languages/<language>/*LanguageModule` | one discoverable language home |
| `RecoveryCoordinator` | `recovery/LayeredRecoveryPipeline` | ordered minimal-unit recovery |
| `recovery/model` | responsibility-specific recovery packages | eliminate generic model ownership |
| `recovery/source` | `recovery/workingcopy` | immutable attempt text and source maps |
| `recovery/unit` | language folders plus `recovery/boundaries` | language locators and shared coordinate contracts |
| `recovery/agent` | `recovery/repair` | Parser-owned repair proposal boundary |
| `recovery/audit` | `recovery/reports` | persisted repair evidence |

`parsing/boundaries` owns the comment/string-safe structural masker shared by Java and C unit
locators. `recovery/boundaries` owns the language-neutral failed-unit and unit-context contracts;
the two folders therefore have distinct parsing and recovery responsibilities.

## Frozen Exceptions

- `antlr/**` stays in place because generated parsers and listeners define the frozen AST output behavior.
- `model/Node.java` stays in place because it is the existing serialized AST contract.
- `service/ParseProgressTracker.java` stays temporarily because the protected Java/Python listeners import that exact type. Moving it would change their recorded hashes. No other runtime file may remain under `service`, and no new code may be added there.

## Verification

- required responsibility packages missing: 0;
- obsolete moved paths present: 0;
- active imports of former moved packages: 0;
- public type/file mismatches: 0;
- package/path mismatches: 0;
- compatibility shims: 0;
- protected listener hashes unchanged;
- Parser full suite and golden byte tests pass;
- full-corpus source and operational hashes unchanged.
