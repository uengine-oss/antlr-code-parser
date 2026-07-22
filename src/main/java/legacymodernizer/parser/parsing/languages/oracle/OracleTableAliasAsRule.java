package legacymodernizer.parser.parsing.languages.oracle;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import legacymodernizer.parser.parsing.RawParseResult;
import legacymodernizer.parser.recovery.boundaries.SourceUnit;
import legacymodernizer.parser.recovery.rules.RecoveryRule;
import legacymodernizer.parser.recovery.rules.RecoveryRuleProposal;
import legacymodernizer.parser.recovery.workingcopy.TextEdit;
import legacymodernizer.parser.parsing.languages.oracle.OracleSourceUnitLocator;

@Component
public final class OracleTableAliasAsRule implements RecoveryRule {

    private static final String IDENTIFIER = "[A-Za-z_][A-Za-z0-9_$#]*";
    private static final Pattern TABLE_ALIAS = Pattern.compile(
            "(?i)\\b(?:FROM|JOIN|UPDATE|MERGE[\\t ]+INTO)[\\t ]+"
            + IDENTIFIER + "(?:[\\t ]*\\.[\\t ]*" + IDENTIFIER + ")?"
            + "([\\t ]+AS[\\t ]+)(?=" + IDENTIFIER + "\\b)");
    private static final Pattern COMMA_ALIAS = Pattern.compile(
            "(?i),[\\t ]*" + IDENTIFIER + "(?:[\\t ]*\\.[\\t ]*" + IDENTIFIER + ")?"
            + "([\\t ]+AS[\\t ]+)(?=" + IDENTIFIER + "\\b)");
    private static final Pattern PAREN_ALIAS = Pattern.compile(
            "(?i)\\)([\\t ]+AS[\\t ]+)(?=" + IDENTIFIER + "\\b)");

    @Override
    public String id() {
        return "oracle.remove-table-alias-as.v1";
    }

    @Override
    public String ruleSetId() {
        return "oracle";
    }

    @Override
    public Set<String> languages() {
        return Set.of("oracle");
    }

    @Override
    public RecoveryRuleProposal propose(String source, SourceUnit unit, RawParseResult failedAttempt) {
        String text = source == null ? "" : source;
        if (!hasAliasDiagnostic(text, unit, failedAttempt)) return RecoveryRuleProposal.none(id());

        String masked = OracleSourceUnitLocator.sanitizeForStructure(text);
        List<TextEdit> edits = new ArrayList<>();
        addMatches(masked, TABLE_ALIAS, edits);
        addMatches(masked, COMMA_ALIAS, edits);
        addDerivedTableMatches(masked, edits);
        edits.sort(Comparator.comparingInt(TextEdit::startOffset));
        if (edits.isEmpty()) return RecoveryRuleProposal.none(id());
        return new RecoveryRuleProposal(id(), true, false, edits,
                "Removing AS from a table alias preserves the alias while matching Oracle syntax.");
    }

    private void addMatches(String source, Pattern pattern, List<TextEdit> edits) {
        Matcher matcher = pattern.matcher(source);
        while (matcher.find()) addEdit(matcher.start(1), matcher.end(1), edits);
    }

    private void addDerivedTableMatches(String source, List<TextEdit> edits) {
        Matcher matcher = PAREN_ALIAS.matcher(source);
        while (matcher.find()) {
            int open = matchingOpenParenthesis(source, matcher.start());
            if (open < 0) continue;
            String body = source.substring(open + 1, matcher.start()).stripLeading();
            if (body.regionMatches(true, 0, "SELECT", 0, 6)
                    || body.regionMatches(true, 0, "WITH", 0, 4)) {
                addEdit(matcher.start(1), matcher.end(1), edits);
            }
        }
    }

    private void addEdit(int start, int end, List<TextEdit> edits) {
        boolean duplicate = edits.stream().anyMatch(edit -> edit.startOffset() == start
                && edit.endOffset() == end);
        if (!duplicate) {
            edits.add(new TextEdit(start, end, " ".repeat(end - start), id(),
                    "Oracle table aliases do not use AS"));
        }
    }

    private static int matchingOpenParenthesis(String source, int closeOffset) {
        int depth = 0;
        for (int index = closeOffset; index >= 0; index--) {
            char current = source.charAt(index);
            if (current == ')') depth++;
            else if (current == '(' && --depth == 0) return index;
        }
        return -1;
    }

    private static boolean hasAliasDiagnostic(String source, SourceUnit unit,
                                              RawParseResult failedAttempt) {
        if (failedAttempt == null) return false;
        return failedAttempt.diagnostics().stream().anyMatch(diagnostic -> {
            int relativeLine = diagnostic.line() - unit.startLine() + 1;
            if (relativeLine < 1) return false;
            int lineStart = lineStart(source, relativeLine);
            if (lineStart < 0) return false;
            int tokenOffset = Math.min(source.length(), lineStart + Math.max(0, diagnostic.column()));
            int cursor = tokenOffset;
            while (cursor > lineStart && (source.charAt(cursor - 1) == ' '
                    || source.charAt(cursor - 1) == '\t')) cursor--;
            if (cursor - lineStart < 2 || !source.regionMatches(true, cursor - 2, "AS", 0, 2)) {
                return false;
            }
            int before = cursor - 3;
            return before < lineStart || !Character.isLetterOrDigit(source.charAt(before));
        });
    }

    private static int lineStart(String source, int requestedLine) {
        if (requestedLine == 1) return 0;
        int line = 1;
        for (int index = 0; index < source.length(); index++) {
            if (source.charAt(index) == '\n' && ++line == requestedLine) return index + 1;
        }
        return -1;
    }
}
