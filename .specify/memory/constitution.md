# ANTLR Code Parser Constitution

<!--
  Principles the antlr-code-parser codebase ALREADY embodies, derived 2026-06-15 by
  reverse-engineering the implementation. Every new spec/plan's "Constitution Check" must
  comply. Amending a principle requires bumping the version below with a rationale.
-->

## Core Principles

### I. Language Strategy Auto-Discovery (분기 0 언어 추가)

Each language is a Spring `@Component` implementing `TargetParserStrategy`, auto-discovered via
List injection. `LanguageDetector` builds the extension→strategy map **in reverse** from each
strategy's `getTargetExtensions()`. Adding a language MUST be a new strategy class only — no
edits to a central router/switch.

**Rationale**: a switch on language is where parser-routing bugs breed; the registry pattern
keeps each language self-contained.

### II. Automatic Detection over Client Metadata (감지 우선)

The live endpoints determine language/dialect **automatically** (file extension + weighted
`.sql` dialect scoring, Oracle vs PostgreSQL). Request metadata such as `target` / `strategy` /
`nameCase` is NOT used to drive parsing. New behavior MUST NOT reintroduce trusting unverified
client-declared language.

**Rationale**: callers mislabel files; the bytes are the source of truth. (Vestigial metadata
fields exist in the contract but are inert — see specs 002/003.)

### III. Path-Based Classification & Structure Preservation (경로=분류)

A file is DDL iff its path begins with `ddl/`; everything else is source. The uploaded folder
structure is preserved 1:1 across `source/`, `ddl/`, and `analysis/`. Classification MUST stay
path-derived, not a separate flag.

**Rationale**: one rule (the path) means no ambiguity and no per-file metadata to drift.

### IV. Replace-All, Stateless Uploads (전량 교체)

Each upload wipes the prior `source/` `ddl/` `analysis/` and stores fresh. There is no
incremental merge or multi-tenant store. Specs MUST treat an upload as a full reset.

**Rationale**: a single working set keeps the parser stateless and reproducible per request.

### V. Streaming-First, Out-of-Band Results (스트림 우선)

Parse progress is delivered as an NDJSON stream (`message` / `complete` / `error`, plus a
structured `detected` event). The AST JSON itself is written to `analysis/` and is **never**
returned in the HTTP response. New long-running operations MUST follow this stream + side-file
pattern.

**Rationale**: large parses exceed request/response limits; streaming keeps the client informed
without buffering megabytes.

### VI. AST JSON Is a Stable Downstream Contract (계약 안정성)

`Node.toJson()` (field order via `@JsonPropertyOrder`, nulls dropped via `@JsonInclude(NON_NULL)`)
is the **input contract consumed by robo-data-analyzer step2 (load_ast)**. Any change to node
field names, the node-type vocabulary, or the `analysis/` layout MUST flag the analyzer as
affected.

**Rationale**: this is the one boundary another service depends on; silent shape changes break
the analyzer's graph.

## Cross-Service Contracts

- **↑ Input — frontend/clients**: `POST /antlr/fileUpload` (multipart) + `POST /antlr/parsing`.
- **↓ Output — robo-data-analyzer**: `analysis/<path>.json` in the `Node` schema (the boundary
  that ripples downstream; see spec 004 here ↔ analyzer spec 004 "AST Loading").

A change that does not touch the AST JSON shape or the parse/upload endpoints is internal.

## Governance

New features go through `/speckit-specify → /speckit-plan`; each `plan.md` MUST include a
Constitution Check verifying the principles above. A principle is amended only by editing this
file and bumping the version, with the rationale recorded.

**Version**: 1.0.0 | **Ratified**: 2026-06-15 | **Last Amended**: 2026-06-15
