---
name: java-syntax-repair
description: Repair bounded Java syntax failures involving delimiters, statement terminators, declarations, generics, lambdas, and try/catch/finally structure. Use only when the Parser-detected languageId is java.
---

# Java Syntax Repair

## Apply Java structure

- Balance `()`, `[]`, `{}`, and generic `<>` only when ownership is unique.
- Insert a missing `;`, `,`, `)`, `]`, or `}` only at the parser-proven boundary.
- Keep annotations attached to their existing declaration.
- Keep `try`, `catch`, `finally`, lambda arrows, and switch labels unchanged unless a missing
  delimiter alone explains the failure.
- Do not invent imports, classes, methods, return statements, exception handlers, or bodies.

## Examples

**Good:** `invoke(value;` has one unmatched call parenthesis. Insert `)` before `;`.

**Good:** `List<String values` is uniquely missing `>` before the variable name. Insert `>`.

**Bad:** Change `catch (IOException e)` to another exception type or add a new `catch` block.

**Abstain:** A `}` could close either a nested lambda block or its containing method.
