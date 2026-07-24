# Data Model: Language-Skilled Verified Source Repair

## RepairSkill

- `name`: skill directory name from frontmatter
- `description`: concise activation/domain summary
- `body`: complete Markdown instructions after frontmatter
- `resourcePath`: immutable classpath provenance

## ComposedRepairPrompt

- Base system contract
- Common repair skill
- Exactly one Parser-selected language skill
- Stable ordering and separators for cacheability and testability

## RecoveryOutcome additions

- `originalFileSha256`: byte hash of the parsed source snapshot
- `repairedSource`: complete materialized source only for a fully accepted direct text repair;
  otherwise null

## SourceApplicationResult

- `status`: `DISABLED`, `NO_REPAIR`, `APPLIED`, `STALE_SOURCE`, `LOSSY_SOURCE`,
  `NO_CHANGE`
- `path`: resolved source path
- `beforeSha256` / `afterSha256`: populated without exposing source content
- `charset`: detected charset for applied results
