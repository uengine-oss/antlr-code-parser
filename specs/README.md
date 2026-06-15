# Specifications — ANTLR Code Parser

> **Backfilled** on 2026-06-15 by reverse-engineering the existing codebase using the
> [GitHub Spec Kit](https://github.com/github/spec-kit) format. Each `spec.md` was derived from
> the actual Spring Boot controllers, services, strategies, and the `Node` AST model — **not**
> from a prior design doc. The README was found to be substantially stale; specs follow the
> **code** (see "Discrepancies" below).

## What this is

antlr-code-parser (Spring Boot, Java 17, ANTLR 4.13.2) collects legacy source + DDL files,
parses them with per-language ANTLR grammars (Java / Oracle PL/SQL / PostgreSQL / C / Python),
and writes **AST JSON** to `analysis/` — the input contract for robo-data-analyzer.

## How to use these specs

- **Onboarding**: read in numbered order.
- **Change planning**: update the relevant `spec.md` first; the spec is the contract.
- **New features**: `/speckit-specify → /speckit-plan → /speckit-tasks → /speckit-implement`.
  The constitution at [`.specify/memory/constitution.md`](../.specify/memory/constitution.md)
  states the 6 principles every new plan's Constitution Check must satisfy.

## Feature index

| # | Spec | One-liner |
|---|------|-----------|
| 001 | [File Upload & Storage](001-file-upload-storage/spec.md) | `POST /antlr/fileUpload` multipart; path-based ddl/ classification; replace-all storage |
| 002 | [Parsing Orchestration & NDJSON Streaming](002-parsing-orchestration-streaming/spec.md) | `POST /antlr/parsing`; per-file routing; NDJSON progress; AST → `analysis/` |
| 003 | [Language/Target Parser Strategy & Detection](003-language-target-strategy/spec.md) | auto-discovered strategies; extension + `.sql` dialect-score detection |
| 004 | [AST Node Model & JSON Output Contract](004-ast-node-json-output/spec.md) | `Node.toJson()` shape → the contract robo-data-analyzer step2 reads |
| 005 | [Health Check, CORS & Web Configuration](005-health-cors-config/spec.md) | `GET /` health, CORS, global exception handling |

## Cross-service contract

- **↓ robo-data-analyzer** reads `analysis/<path>.json` in the `Node` schema. Owned by **spec 004**
  here; consumed by the analyzer's spec 004 ("AST Loading → Code Structure Graph"). This is the
  one boundary that ripples downstream.

## Discrepancies corrected against code (notable — README was stale)

Specs follow the **code**, not the README:

- **001** — response has a **third** list `nontargetFiles` (README shows only `files`/`ddlFiles`).
  The controller **ignores** README's `strategy`/`target`/`nameCase` metadata + `OpenAI-Api-Key`
  header — it reads only `metadata.targetFolder`.
- **002** — `POST /antlr/parsing` reads **only** `project_root`; README's `{strategy,target,nameCase}`
  body is dead. Undocumented **`project_root` local-path mode** (Electron) and a structured
  **`detected`** NDJSON event exist.
- **003** — README's target table omits **C and Python** strategies (both present). Aliases
  `plsql`/`postgres` have **no mapping** in code (only `oracle`/`postgresql`). `nameCase` is
  **unimplemented** (only PL/SQL's hardcoded upper-casing exists).
- **004** — the `Node` model lives in `…/parser/model/Node.java` (not `antlr/`). Actual JSON fields:
  `type, name, signature, modifiers, annotations, returnType, parameters, genericType, extendsType,
  implementsTypes, variableType, initValue, schema, moduleName, fileName, filePath, packageName,
  comment, startLine, endLine, children` (nulls dropped; `parent` ignored).
- **005** — there is **no `application.yml`**; port 8081 comes only from docker mapping (Spring
  likely listens on 8080 inside the container). The Dockerfile healthcheck hits `/actuator/health`,
  which is **not implemented** (only `GET /`). The 100MB/500MB limits are a hardcoded message
  string, not enforced config.

Specs are intentionally short (~95-140 lines each) — they describe the contract, not the code.
