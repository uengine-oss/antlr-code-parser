package legacymodernizer.parser.recovery.localization;

import java.util.List;

/**
 * Language lexical facts the slice scanner needs to avoid cutting a slice inside a string,
 * a comment, or an unbalanced bracket. Owned by the LanguageModule; Core stays language-free.
 */
public record SliceSyntax(
        List<String> lineCommentPrefixes,
        List<String> blockCommentOpeners,
        List<String> blockCommentClosers,
        List<Character> stringQuotes,
        boolean backslashEscapesInStrings,
        boolean doubledQuoteEscapesInStrings,
        boolean tripleQuotedStrings,
        List<Character> statementTerminators) {

    public SliceSyntax {
        if (blockCommentOpeners.size() != blockCommentClosers.size()) {
            throw new IllegalArgumentException("SLICE_SYNTAX_BLOCK_COMMENT_PAIR_MISMATCH");
        }
        lineCommentPrefixes = List.copyOf(lineCommentPrefixes);
        blockCommentOpeners = List.copyOf(blockCommentOpeners);
        blockCommentClosers = List.copyOf(blockCommentClosers);
        stringQuotes = List.copyOf(stringQuotes);
        statementTerminators = List.copyOf(statementTerminators);
    }

    public static SliceSyntax generic() {
        return new SliceSyntax(List.of("--", "//", "#"), List.of("/*"), List.of("*/"),
                List.of('\'', '"'), true, true, false, List.of(';'));
    }

    public static SliceSyntax sql() {
        return new SliceSyntax(List.of("--"), List.of("/*"), List.of("*/"),
                List.of('\'', '"'), false, true, false, List.of(';'));
    }

    public static SliceSyntax cFamily() {
        return new SliceSyntax(List.of("//"), List.of("/*"), List.of("*/"),
                List.of('\'', '"'), true, false, false, List.of(';'));
    }

    public static SliceSyntax python() {
        return new SliceSyntax(List.of("#"), List.of(), List.of(),
                List.of('\'', '"'), true, false, true, List.of());
    }
}
