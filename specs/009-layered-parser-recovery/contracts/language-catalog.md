# Parser-internal Language Catalog v1

## Location and ownership

- Canonical build resource: `src/main/resources/languages/language-catalog.json`.
- Owner: antlr-code-parser.
- Consumer: antlr-code-parser module discovery and validation only.

Parser validates that every discovered production `LanguageModule` has exactly one catalog entry and that every entry has a module. It performs this check in memory before AST parsing and writes no shared artifact.

## Required shape

```json
{
  "schemaVersion": "1.0.0",
  "catalogVersion": "1.0.0",
  "parserBuild": "0.0.1-SNAPSHOT",
  "antlrRuntimeVersion": "4.13.2",
  "languages": [
    {
      "id": "java",
      "aliases": [],
      "family": "framework",
      "parseExtensions": [".java"],
      "sharedExtension": false,
      "entryRules": {"FILE": "start_", "CLASS": "classDeclaration", "METHOD": "methodDeclaration"},
      "unitKinds": ["FILE", "CLASS", "METHOD"],
      "emittedNodeTypes": ["FILE", "CLASS", "METHOD"],
      "routineNodeTypes": ["METHOD"],
      "grammar": {"files": [{"path": "antlr-grammars/Java20Parser.g4", "sha256": "..."}]},
      "recoveryRuleSets": ["common-safe", "java"]
    }
  ],
  "catalogSha256": "..."
}
```

The complete production resource may list more emitted node types than this shortened example.

## Validation

- Parser supports schema major `1` only.
- Unsupported major: fail before parsing.
- Compatible minor: validate all required fields and unique ids/extensions.
- Parser build or content changes update `catalogVersion` or checksum.
- Parser detection uses `parseExtensions`; no Analyzer capability or semantic role is declared here.
- Analyzer remains unchanged and consumes only the established AST JSON contract.

## Frozen semantics

The catalog describes what current listeners emit; it does not rename nodes. In particular, PostgreSQL may declare `PROCEDURE` as its current routine node type, and Oracle package boundary support does not imply a PACKAGE AST node.
