---
name: common-syntax-repair
description: Propose bounded, minimal syntax repairs from parser diagnostics while preserving program meaning. Use for every failed Java, Python, C, Oracle, or PostgreSQL parser unit before applying a language-specific repair skill.
---

# Common Syntax Repair

## Diagnose

1. Treat the Parser diagnostic as evidence, not necessarily the root-cause location.
2. Trace unmatched delimiters, quotes, block markers, and statement terminators backward inside
   `sourceExcerpt`.
3. Select the smallest edit at the root cause. Do not clean up adjacent style or formatting.
4. Use exact token offsets from `diagnosticWindowTokens`; never estimate an offset.
5. Preserve the existing line count and line-ending style.

## Propose

- Prefer one insertion or deletion of grammar-mandated structural text.
- Copy `expectedText` byte-for-character from the selected excerpt range.
- Leave identifiers, literals, operators, calls, predicates, table/column names, transactions,
  and business statements unchanged.
- Return an empty edit list with explicit ambiguities when two root causes or placements remain
  plausible.

## Examples

**Good:** The parser reports an error at `;`, but the unique unmatched token is an earlier `(`.
Insert only the missing `)` before `;`.

**Good:** A block opener is uniquely required between declarations and the first executable or
exception section. Insert only that opener at the grammar boundary.

**Bad:** Replace the complete excerpt with a newly formatted version that happens to parse.

**Bad:** Rename a table, column, variable, method, function, or type to make a later rule match.

**Abstain:** Two different unmatched openers could own the same closing token, or the apparent
cause lies outside `sourceExcerpt`.
