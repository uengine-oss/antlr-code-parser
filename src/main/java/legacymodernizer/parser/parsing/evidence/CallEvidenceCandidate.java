package legacymodernizer.parser.parsing.evidence;

import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

/** Grammar-owned call boundaries. Resolution and naming remain Analyzer responsibilities. */
public record CallEvidenceCandidate(
        String grammarRule,
        SourceRangeCandidate callRange,
        SourceRangeCandidate calleeRange,
        SourceRangeCandidate receiverRange,
        String calleeKind,
        String terminalName,
        List<SourceRangeCandidate> argumentRanges,
        List<ScopeEvidenceCandidate> scopePath) {

    public CallEvidenceCandidate {
        if (grammarRule == null || grammarRule.isBlank()) {
            throw new IllegalArgumentException("grammarRule is required");
        }
        if (!List.of("named", "constructor", "expression").contains(calleeKind)) {
            throw new IllegalArgumentException("unsupported calleeKind: " + calleeKind);
        }
        if (!"expression".equals(calleeKind)
                && (terminalName == null || terminalName.isBlank())) {
            throw new IllegalArgumentException(calleeKind + " callee requires terminalName");
        }
        argumentRanges = List.copyOf(argumentRanges == null ? List.of() : argumentRanges);
        scopePath = List.copyOf(scopePath == null ? List.of() : scopePath);
    }

    public static CallEvidenceCandidate fromTokens(
            String grammarRule, Token callStart, Token callStop,
            Token calleeStart, Token calleeStop,
            String calleeKind, String terminalName,
            List<? extends ParserRuleContext> arguments) {
        return new CallEvidenceCandidate(grammarRule,
                range(callStart, callStop), range(calleeStart, calleeStop), null,
                calleeKind, terminalName,
                arguments == null ? List.of() : arguments.stream()
                        .map(argument -> range(argument.getStart(), argument.getStop()))
                        .toList(), List.of());
    }

    public CallEvidenceCandidate withStructuralContext(
            SourceRangeCandidate receiver,
            List<ScopeEvidenceCandidate> lexicalScopePath) {
        return new CallEvidenceCandidate(grammarRule, callRange, calleeRange, receiver,
                calleeKind, terminalName, argumentRanges, lexicalScopePath);
    }

    private static SourceRangeCandidate range(Token start, Token stop) {
        if (start == null || stop == null) {
            throw new IllegalArgumentException("ANTLR token boundary is required");
        }
        return new SourceRangeCandidate(start.getStartIndex(), stop.getStopIndex() + 1);
    }
}
