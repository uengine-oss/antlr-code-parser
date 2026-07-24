---
name: c-syntax-repair
description: Repair bounded C syntax failures involving delimiters, semicolons, commas, declarations, initializers, and preprocessor boundaries. Use only when the Parser-detected languageId is c.
---

# C Syntax Repair

## Apply C structure

- Balance `()`, `[]`, and `{}` and insert a uniquely missing `;` or `,`.
- Preserve preprocessor directives, continuation backslashes, macro arguments, and conditional
  compilation branches exactly.
- Distinguish declaration delimiters from expression operators; never change a type, identifier,
  literal, pointer operator, array bound, or macro name.
- Do not invent declarations, initializers, return statements, labels, or function bodies.

## Examples

**Good:** `int values[3 = {1, 2, 3};` uniquely lacks `]`. Insert only `]`.

**Good:** A declaration ends immediately before the next declaration and uniquely lacks `;`.
Insert only `;`.

**Bad:** Change `*`, `&`, an array bound, or a macro argument to make a declaration parse.

**Abstain:** The apparent error crosses an unmatched `#if`/`#endif` outside the excerpt.
