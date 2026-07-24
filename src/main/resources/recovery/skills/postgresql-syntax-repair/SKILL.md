---
name: postgresql-syntax-repair
description: Repair bounded PostgreSQL SQL and PL/pgSQL syntax failures involving dollar quotes, function bodies, DECLARE/BEGIN/EXCEPTION/END blocks, casts, statements, and delimiters. Use only when the Parser-detected languageId is postgresql.
---

# PostgreSQL Syntax Repair

## Apply PostgreSQL structure

- Balance ordinary quotes separately from matching `$tag$ ... $tag$` delimiters.
- Preserve the existing dollar-quote tag exactly; do not replace it with `$$` unless both ends
  already prove that exact tag.
- Follow PL/pgSQL `DECLARE ... BEGIN ... EXCEPTION ... END` structure without importing Oracle
  slash-terminator rules.
- Insert a uniquely missing delimiter, comma, semicolon, colon, or block marker only.
- Preserve casts, operators, function volatility, `LANGUAGE`, identifiers, literals, predicates,
  table/column names, and conflict targets.

## Examples

**Good:** A function body opens with `$body$` and the final delimiter is uniquely absent. Insert
only `$body$` at the body boundary.

**Good:** A call has one unmatched `(` before its semicolon. Insert only `)`.

**Bad:** Change `::type`, `LANGUAGE plpgsql`, an `ON CONFLICT` target, or a dollar-quote tag by
guessing.

**Abstain:** Nested dynamic SQL contains an unmatched quote whose end is outside the excerpt.
