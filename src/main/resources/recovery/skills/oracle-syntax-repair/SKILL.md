---
name: oracle-syntax-repair
description: Repair bounded Oracle SQL and PL/SQL syntax failures involving DECLARE/BEGIN/EXCEPTION/END blocks, statement terminators, calls, cursors, packages, triggers, and MERGE structure. Use only when the Parser-detected languageId is oracle.
---

# Oracle Syntax Repair

## Apply Oracle structure

- Follow `DECLARE? BEGIN ... EXCEPTION ... END [name];` and routine
  `IS|AS declarations BEGIN ... EXCEPTION ... END [name];` structure.
- When declarations are followed by executable statements or `EXCEPTION` with no `BEGIN`, insert
  `BEGIN` at the unique declaration/body boundary. Absence of `BEGIN` is not by itself a reason
  to abstain.
- Insert the opener on an existing line when necessary to preserve line count.
- Match named `END` labels only to an already declared enclosing unit.
- Preserve `/` client terminators and distinguish them from PL/SQL grammar tokens.
- Never change schema, table, column, alias, variable, cursor, routine, literal, predicate,
  transaction, dynamic SQL, or MERGE target names for automatic repair.

## Examples

**Good:** Before: `IS v NUMBER; v := 1; EXCEPTION ... END;`. The declaration ends at
`v NUMBER;`; insert only `BEGIN ` immediately before `v := 1;`.

**Good:** Before: `IS v NUMBER; EXCEPTION WHEN OTHERS THEN NULL; END;`. Insert only `BEGIN `
immediately before `EXCEPTION` when that is the unique body boundary.

**Bad:** Replace a `MERGE INTO` target, join predicate, column, or alias with a guessed name.

**Bad:** Add `NULL;`, a handler, a `COMMIT`, or an `END IF` that was not grammar-mandated.

**Abstain:** More than one enclosing block could own `EXCEPTION` or `END`, or the required
declaration boundary lies outside the excerpt.
