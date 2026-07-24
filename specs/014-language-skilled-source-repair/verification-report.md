# Verification Report: Language-Skilled Verified Source Repair

## Result

The Parser now supplies the repair Agent with the common repair Skill and exactly
one Parser-selected language Skill. Accepted localized edits are reparsed by
ANTLR and can be applied to the selected original source only when the complete
file recovery, source hash, charset round trip, and atomic-write gates pass.
Source application remains disabled by default.

The Parser, grammar, AST schema, and JSON output contract were not replaced.

## Skill validation

The following packages passed the canonical `quick_validate.py` validator:

- `common-syntax-repair`
- `java-syntax-repair`
- `python-syntax-repair`
- `c-syntax-repair`
- `oracle-syntax-repair`
- `postgresql-syntax-repair`

The runtime catalog rejects missing, malformed, oversized, and unsafe language
identifiers. Captured-request tests prove that an Oracle request contains the
common and Oracle Skills and does not contain the PostgreSQL Skill.

## Automated tests

Focused repair, Skill, source-application, workspace-origin, and end-to-end
tests passed: 21 tests, 0 failures, 1 live test skipped when credentials were
not supplied.

The full Maven suite passed:

- Tests run: 115
- Failures: 0
- Errors: 0
- Skipped: 8

The source-application tests cover disabled, applied, stale-source, lossy
charset, partial recovery, no-op, UTF-8, and MS949 behavior. The integration
test starts with malformed Oracle source, performs localized recovery, reparses
the complete repaired file, applies it to the external selected original, and
then proves that the modified original parses as `EXACT`.

## Water corpus

Command:

```powershell
.\mvnw.cmd `
  '-Dparser.full.corpus=D:\work\robo\분석대상모음\dbms\oracle\수자원' `
  '-Dparser.full.report.name=water-skill-repair-summary.json' `
  '-Dtest=FullCorpusRecoveryTest' test
```

Result:

- Intake source files: 117
- Oracle parser files: 38
- Source lines: 10,935
- AST files: 38
- `EXACT`: 38
- Lexer errors: 0
- Parser errors: 0
- ANTLR recoveries: 0
- Agent calls: 0
- Original corpus unchanged: true

The UTF-8 JSON report parses successfully and preserves the Korean corpus path.
Because every supported Oracle source was already `EXACT`, no repair or source
write was needed for the water corpus.

## Live GPU check

The Parser reused the Analyzer runtime contract without copying its secret:

- Config: `qwen36_sglang_local`
- Provider: SGLang
- Model: `frentis-ai-model`
- Thinking: disabled
- Top-k: 1
- API key source: Analyzer `.env` `ROBO_LLM_API_KEY`, mapped only into the test
  process environment

The authenticated live test passed in 4.455 seconds. The GPU Agent returned a
bounded edit for the malformed Oracle procedure, the proposal passed snapshot
and edit validation, and the complete unit reparse was accepted as
`RECOVERED_VALIDATED`. The test also proves that the accepted attempt has
`stage=REPAIR_AGENT`, a non-empty edit and diff, request evidence, and a verified
full repaired source.

The fixture on disk remained byte-identical because this test exercises the
default read-only mode. The separate source-application integration test proves
the opt-in external-original write path.

## Source-application switch

The explicit opt-in is:

```powershell
$env:PARSER_REPAIR_APPLY_TO_SOURCE = "true"
```

With the switch absent or false, behavior is read-only. Uploaded files are
applied only to their workspace copy; path intake records and targets the exact
external original selected by the user.

## Worktree preservation

The pre-existing `src/main/resources/logback-spring.xml` modification and
untracked `specs/013-platform-console-charset/` work were preserved and were not
folded into this feature.
