---
name: python-syntax-repair
description: Repair bounded Python syntax failures involving delimiters, colons, commas, calls, collections, and string boundaries while treating indentation as semantic structure. Use only when the Parser-detected languageId is python.
---

# Python Syntax Repair

## Apply Python structure

- Balance `()`, `[]`, and `{}` and repair a uniquely missing `:`, `,`, or closing quote.
- Preserve physical lines and existing leading indentation.
- Treat `INDENT` and `DEDENT` changes as control-flow changes; do not auto-reindent a block.
- Do not add `pass`, `return`, imports, exception handlers, function parameters, or expressions.
- Do not change names, literal values, operators, decorators, or call targets.

## Examples

**Good:** `result = call(value` ends with a uniquely unmatched `(`. Insert only `)`.

**Good:** `def load(value)` is immediately followed by its existing body and uniquely lacks `:`.
Insert only `:`.

**Bad:** Add `pass` to make an empty suite parse or move an existing statement into a block.

**Abstain:** The repair requires changing leading spaces or tabs on any existing code line.
