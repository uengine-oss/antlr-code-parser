package legacymodernizer.parser.parsing.evidence;

import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

/** Grammar-owned call boundaries. Resolution and naming remain Analyzer responsibilities. */
public record CallEvidenceCandidate(
        String grammarRule,
        SourceRangeCandidate callRange,
        SourceRangeCandidate calleeRange,
        List<SourceRangeCandidate> argumentRanges) {

    public CallEvidenceCandidate {
        if (grammarRule == null || grammarRule.isBlank()) {
            throw new IllegalArgumentException("grammarRule is required");
        }
        argumentRanges = List.copyOf(argumentRanges == null ? List.of() : argumentRanges);
    }

    public static CallEvidenceCandidate fromTokens(
            String grammarRule, Token callStart, Token callStop,
            Token calleeStart, Token calleeStop,
            List<? extends ParserRuleContext> arguments) {
        return new CallEvidenceCandidate(grammarRule,
                range(callStart, callStop), range(calleeStart, calleeStop),
                arguments == null ? List.of() : arguments.stream()
                        .map(argument -> range(argument.getStart(), argument.getStop()))
                        .toList());
    }

    private static SourceRangeCandidate range(Token start, Token stop) {
        if (start == null || stop == null) {
            throw new IllegalArgumentException("ANTLR token boundary is required");
        }
        return new SourceRangeCandidate(start.getStartIndex(), stop.getStopIndex() + 1);
    }
}
