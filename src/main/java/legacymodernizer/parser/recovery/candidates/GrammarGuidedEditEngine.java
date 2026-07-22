package legacymodernizer.parser.recovery.candidates;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import legacymodernizer.parser.recovery.diagnostics.ParseDiagnostic;
import legacymodernizer.parser.recovery.localization.ErrorSpanLocator;

/**
 * Language-agnostic single-token repair candidates derived from ANTLR's own recovery signals
 * (spec 012 FR-030..033). The engine only generates; strict reparse evaluation and the
 * uniqueness/ambiguity decision belong to the pipeline.
 *
 * ANTLR message forms used (DefaultErrorStrategy):
 *   "extraneous input 'X' expecting ..."  → delete X
 *   "missing 'X' at ..."                  → insert X before the offending position
 *   "mismatched input 'X' expecting {..}" → replace X with each expected literal
 *   "no viable alternative"               → delete a profile-declared structural keyword at or
 *                                           immediately before the offending token, nothing else
 */
public final class GrammarGuidedEditEngine {

    private static final Pattern EXTRANEOUS = Pattern.compile("^extraneous input '(.+?)'");
    private static final Pattern MISSING = Pattern.compile("^missing '(.+?)' at ");
    private static final Pattern MISMATCHED = Pattern.compile("^mismatched input '(.+?)'");
    private static final Pattern QUOTED_LITERAL = Pattern.compile("'([^']+)'");
    private static final int MAX_REPLACEMENTS = 8;

    private final ErrorSpanLocator locator = new ErrorSpanLocator();

    public List<TokenEditCandidate> generate(String unitText, int unitStartLine,
                                             ParseDiagnostic diagnostic,
                                             RepairProfile profile) {
        String message = diagnostic.message() == null ? "" : diagnostic.message();
        int anchor = locator.anchorOffset(unitText, unitStartLine,
                diagnostic.line(), diagnostic.column());
        List<TokenEditCandidate> candidates = new ArrayList<>();

        Matcher extraneous = EXTRANEOUS.matcher(message);
        if (extraneous.find()) {
            String token = extraneous.group(1);
            int start = tokenStart(unitText, anchor, token);
            if (start >= 0) {
                int end = deletionEnd(unitText, start, token);
                candidates.add(new TokenEditCandidate(TokenEditCandidate.Kind.DELETE,
                        start, end, unitText.substring(start, end), "",
                        EditClassification.forDeletion(token,
                                profile.deletableStructuralKeywords()),
                        1, "ANTLR extraneous input '" + token + "'"));
            }
            return List.copyOf(candidates);
        }

        Matcher missing = MISSING.matcher(message);
        if (missing.find()) {
            String token = missing.group(1);
            candidates.add(new TokenEditCandidate(TokenEditCandidate.Kind.INSERT,
                    anchor, anchor, "", insertionText(unitText, anchor, token),
                    EditClassification.forInsertion(token),
                    1, "ANTLR missing '" + token + "'"));
            return List.copyOf(candidates);
        }

        if (message.startsWith("no viable alternative")) {
            for (int[] span : noViableDeletionSpans(unitText, anchor,
                    diagnostic.offendingToken())) {
                String token = unitText.substring(span[0], span[1]).trim();
                EditClassification classification = EditClassification.forDeletion(
                        token, profile.deletableStructuralKeywords());
                if (classification != EditClassification.STRUCTURAL_TOKEN) continue;
                int end = deletionEnd(unitText, span[0], unitText.substring(span[0], span[1]));
                candidates.add(new TokenEditCandidate(TokenEditCandidate.Kind.DELETE,
                        span[0], end, unitText.substring(span[0], end), "",
                        classification, 2,
                        "ANTLR no viable alternative; profile keyword '" + token + "'"));
            }
            return List.copyOf(candidates);
        }

        Matcher mismatched = MISMATCHED.matcher(message);
        if (mismatched.find()) {
            String token = mismatched.group(1);
            int start = tokenStart(unitText, anchor, token);
            if (start < 0) return List.of();
            int end = start + token.length();
            int cost = 2;
            for (String literal : expectedLiterals(diagnostic.expectedTokens())) {
                if (literal.equals(token)) continue;
                candidates.add(new TokenEditCandidate(TokenEditCandidate.Kind.REPLACE,
                        start, end, token, literal,
                        replacementClassification(token, literal),
                        cost++, "ANTLR mismatched input '" + token + "' → '" + literal + "'"));
                if (candidates.size() >= MAX_REPLACEMENTS) break;
            }
        }
        return List.copyOf(candidates);
    }

    /** Replacing is as risky as deleting the old token plus inserting the new one. */
    private static EditClassification replacementClassification(String oldToken,
                                                                String newToken) {
        EditClassification deletion = EditClassification.forDeletion(oldToken, Set.of());
        EditClassification insertion = EditClassification.forInsertion(newToken);
        if (!deletion.autoAdoptable()) return deletion;
        if (!insertion.autoAdoptable()) return insertion;
        return EditClassification.STRUCTURAL_TOKEN;
    }

    private static Set<String> expectedLiterals(String expectedTokens) {
        Set<String> literals = new LinkedHashSet<>();
        if (expectedTokens == null) return literals;
        Matcher matcher = QUOTED_LITERAL.matcher(expectedTokens);
        while (matcher.find()) literals.add(matcher.group(1));
        return literals;
    }

    /**
     * For "no viable alternative": the offending token span plus the word immediately before
     * it. Only profile-declared structural keywords among these ever become candidates.
     */
    private static List<int[]> noViableDeletionSpans(String unitText, int anchor,
                                                     String offendingToken) {
        List<int[]> spans = new ArrayList<>();
        int offendingStart = offendingToken == null ? anchor
                : Math.max(0, Math.min(anchor, unitText.length()));
        int offendingEnd = offendingToken == null ? offendingStart
                : Math.min(unitText.length(), offendingStart + offendingToken.length());
        if (offendingEnd > offendingStart) {
            spans.add(new int[]{offendingStart, offendingEnd});
        }
        int cursor = offendingStart;
        while (cursor > 0 && Character.isWhitespace(unitText.charAt(cursor - 1))) cursor--;
        int previousEnd = cursor;
        while (cursor > 0 && isWordCharacter(unitText.charAt(cursor - 1))) cursor--;
        if (previousEnd > cursor) {
            spans.add(new int[]{cursor, previousEnd});
        }
        return spans;
    }

    private static boolean isWordCharacter(char character) {
        return Character.isLetterOrDigit(character) || character == '_' || character == '$'
                || character == '#';
    }

    /** Finds the offending token at/near the anchor; diagnostics columns are token starts. */
    private static int tokenStart(String unitText, int anchor, String token) {
        if (unitText.startsWith(token, anchor)) return anchor;
        int windowStart = Math.max(0, anchor - token.length());
        int found = unitText.indexOf(token, windowStart);
        return found >= 0 && found <= anchor + 1 ? found : -1;
    }

    /** Deleting a token also swallows one following space so 'A  B' does not stay 'A  '. */
    private static int deletionEnd(String unitText, int start, String token) {
        int end = start + token.length();
        if (end < unitText.length() && unitText.charAt(end) == ' ') end++;
        return end;
    }

    private static String insertionText(String unitText, int anchor, String token) {
        boolean needsLeadingSpace = anchor > 0
                && Character.isLetterOrDigit(unitText.charAt(anchor - 1))
                && Character.isLetterOrDigit(token.charAt(0));
        boolean needsTrailingSpace = anchor < unitText.length()
                && Character.isLetterOrDigit(unitText.charAt(anchor))
                && Character.isLetterOrDigit(token.charAt(token.length() - 1));
        return (needsLeadingSpace ? " " : "") + token + (needsTrailingSpace ? " " : "");
    }
}
