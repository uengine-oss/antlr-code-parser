package legacymodernizer.parser.parsing.evidence;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.Vocabulary;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

/** Shared conversion of ANTLR grammar contexts into exact structural evidence. */
public final class GrammarStructureEvidence {

    private static final Set<String> SOURCELESS_LAYOUT_TOKENS = Set.of(
            "LINE_BREAK", "INDENT", "DEDENT");

    @FunctionalInterface
    public interface ScopeClassifier {
        String classify(ParserRuleContext context);
    }

    private final Vocabulary vocabulary;
    private final String[] ruleNames;
    private final int sourceLength;

    public GrammarStructureEvidence(
            CommonTokenStream tokens, Vocabulary vocabulary, String[] ruleNames) {
        if (tokens == null || vocabulary == null || ruleNames == null) {
            throw new IllegalArgumentException("grammar evidence dependencies are required");
        }
        this.vocabulary = vocabulary;
        this.ruleNames = ruleNames.clone();
        this.sourceLength = tokens.getTokenSource().getInputStream().size();
    }

    public SourceRangeCandidate range(ParserRuleContext context) {
        if (context == null) throw new IncompleteGrammarEvidence();
        return range(context.getStart(), context.getStop());
    }

    public SourceRangeCandidate range(Token start, Token stop) {
        int endOffset = stop != null && stop.getType() == Token.EOF
                ? sourceLength : stop == null ? -1 : stop.getStopIndex() + 1;
        if (start == null || stop == null || start.getStartIndex() < 0
                || endOffset < start.getStartIndex() || endOffset > sourceLength) {
            throw new IncompleteGrammarEvidence(
                    "invalid token range start=" + (start == null ? "null" : start)
                            + ", stop=" + (stop == null ? "null" : stop));
        }
        return new SourceRangeCandidate(start.getStartIndex(), endOffset);
    }

    public SyntaxComponentCandidate component(ParserRuleContext context) {
        SourceRangeCandidate contextRange = range(context);
        int ruleIndex = context.getRuleIndex();
        if (ruleIndex < 0 || ruleIndex >= ruleNames.length
                || ruleNames[ruleIndex] == null || ruleNames[ruleIndex].isBlank()) {
            throw new IncompleteGrammarEvidence("missing grammar rule name");
        }
        List<SyntaxTokenCandidate> directTokens = new ArrayList<>();
        List<SyntaxComponentCandidate> children = new ArrayList<>();
        if (context.children != null) {
            for (ParseTree child : context.children) {
                if (child instanceof ParserRuleContext childContext) {
                    if (hasSourceBackedRange(childContext)) {
                        children.add(component(childContext));
                    }
                } else if (child instanceof TerminalNode terminal) {
                    Token token = terminal.getSymbol();
                    String tokenKind = vocabulary.getSymbolicName(token.getType());
                    if (tokenKind == null || tokenKind.isBlank()) {
                        throw new IncompleteGrammarEvidence(
                                "missing symbolic token name: " + token);
                    }
                    if (token.getStopIndex() < token.getStartIndex()
                            && SOURCELESS_LAYOUT_TOKENS.contains(tokenKind)) {
                        continue;
                    }
                    directTokens.add(new SyntaxTokenCandidate(tokenKind, range(token, token)));
                }
            }
        }
        return new SyntaxComponentCandidate(
                ruleNames[ruleIndex], contextRange, directTokens, children);
    }

    private static boolean hasSourceBackedRange(ParserRuleContext context) {
        Token start = context == null ? null : context.getStart();
        Token stop = context == null ? null : context.getStop();
        return start != null && stop != null && start.getStartIndex() >= 0
                && stop.getStopIndex() >= start.getStartIndex();
    }

    public List<ScopeEvidenceCandidate> scopePath(
            ParserRuleContext context, ScopeClassifier classifier) {
        if (classifier == null) throw new IllegalArgumentException("scope classifier is required");
        List<ScopeEvidenceCandidate> nested = new ArrayList<>();
        ParserRuleContext cursor = context;
        while (cursor != null) {
            String kind = classifier.classify(cursor);
            if (kind != null) {
                nested.add(0, new ScopeEvidenceCandidate(kind, range(cursor)));
            }
            cursor = cursor.getParent();
        }
        List<ScopeEvidenceCandidate> result = new ArrayList<>(nested.size() + 1);
        result.add(new ScopeEvidenceCandidate(
                "translation_unit", new SourceRangeCandidate(0, sourceLength)));
        result.addAll(nested);
        return List.copyOf(result);
    }

    /** A recovered or synthetic grammar node has no exact source-backed representation. */
    public static final class IncompleteGrammarEvidence extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public IncompleteGrammarEvidence() {
        }

        public IncompleteGrammarEvidence(String message) {
            super(message);
        }
    }
}
