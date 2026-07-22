package legacymodernizer.parser.recovery.candidates;

import java.util.Locale;
import java.util.Set;

/**
 * Semantic-risk class of a single-token edit (spec 012 FR-040/041/042). Only
 * SAFE_WHITESPACE and STRUCTURAL_TOKEN edits may ever be adopted automatically; everything
 * else is hard-rejected regardless of whether the reparse succeeds.
 */
public enum EditClassification {
    SAFE_WHITESPACE,
    STRUCTURAL_TOKEN,
    IDENTIFIER,
    LITERAL,
    OPERATOR,
    CONTROL_FLOW,
    TRANSACTION,
    DATA_STATEMENT;

    private static final Set<String> TRANSACTION_KEYWORDS = Set.of(
            "COMMIT", "ROLLBACK", "SAVEPOINT");
    private static final Set<String> CONTROL_KEYWORDS = Set.of(
            "IF", "ELSE", "ELSIF", "ELIF", "LOOP", "WHILE", "FOR", "RETURN", "EXIT", "GOTO",
            "RAISE", "THROW", "BREAK", "CONTINUE", "CASE", "WHEN", "TRY", "CATCH", "EXCEPTION",
            "FINALLY");
    private static final Set<String> DATA_KEYWORDS = Set.of(
            "SELECT", "INSERT", "UPDATE", "DELETE", "MERGE", "CREATE", "ALTER", "DROP",
            "TRUNCATE", "GRANT", "REVOKE", "JOIN", "WHERE", "SET", "VALUES");

    public boolean autoAdoptable() {
        return this == SAFE_WHITESPACE || this == STRUCTURAL_TOKEN;
    }

    /**
     * Classifies a token being deleted from real source. Alphabetic tokens are identifiers
     * unless the language profile declares them deletable structural keywords — lexically an
     * Oracle alias {@code AS} and a column named {@code AS_OF} are indistinguishable.
     */
    public static EditClassification forDeletion(String token, Set<String> structuralKeywords) {
        return classify(token, structuralKeywords, false);
    }

    /**
     * Classifies a token being inserted. The token text comes from the grammar's own expected
     * set ("missing 'X'"), so keywords are grammar-mandated scaffolding, not invented code —
     * but risk-class keywords and anything identifier-like stay non-adoptable.
     */
    public static EditClassification forInsertion(String token) {
        return classify(token, Set.of(), true);
    }

    private static EditClassification classify(String token, Set<String> structuralKeywords,
                                               boolean grammarMandated) {
        if (token == null || token.isBlank()) return SAFE_WHITESPACE;
        String trimmed = token.trim();
        String upper = trimmed.toUpperCase(Locale.ROOT);
        if (trimmed.startsWith("'") || trimmed.startsWith("\"")
                || trimmed.chars().allMatch(Character::isDigit)) {
            return LITERAL;
        }
        if (TRANSACTION_KEYWORDS.contains(upper)) return TRANSACTION;
        if (CONTROL_KEYWORDS.contains(upper)) return CONTROL_FLOW;
        if (DATA_KEYWORDS.contains(upper)) return DATA_STATEMENT;
        boolean word = trimmed.chars().allMatch(character ->
                Character.isLetterOrDigit(character) || character == '_' || character == '$'
                        || character == '#');
        if (word) {
            if (structuralKeywords.contains(upper)) return STRUCTURAL_TOKEN;
            return grammarMandated ? STRUCTURAL_TOKEN : IDENTIFIER;
        }
        boolean operator = trimmed.chars().allMatch(character ->
                "+-*/=<>!%&|^~".indexOf(character) >= 0);
        if (operator) return OPERATOR;
        return STRUCTURAL_TOKEN;
    }
}
