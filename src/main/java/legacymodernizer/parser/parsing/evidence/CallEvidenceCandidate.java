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
        List<CallArgumentEvidenceCandidate> arguments,
        List<ScopeEvidenceCandidate> scopePath,
        List<SourceRangeCandidate> calleePathRanges,
        SourceRangeCandidate databaseLinkRange) {

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
        arguments = List.copyOf(arguments == null ? List.of() : arguments);
        scopePath = List.copyOf(scopePath == null ? List.of() : scopePath);
        calleePathRanges = List.copyOf(
                calleePathRanges == null ? List.of() : calleePathRanges);
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
                        .map(argument -> CallArgumentEvidenceCandidate.expression(
                                range(argument.getStart(), argument.getStop())))
                        .toList(), List.of(), List.of(), null);
    }

    public static CallEvidenceCandidate fromStructuredArguments(
            String grammarRule, Token callStart, Token callStop,
            Token calleeStart, Token calleeStop,
            String calleeKind, String terminalName,
            List<CallArgumentEvidenceCandidate> arguments) {
        return new CallEvidenceCandidate(grammarRule,
                range(callStart, callStop), range(calleeStart, calleeStop), null,
                calleeKind, terminalName, arguments, List.of(), List.of(), null);
    }

    public CallEvidenceCandidate withStructuralContext(
            SourceRangeCandidate receiver,
            List<ScopeEvidenceCandidate> lexicalScopePath) {
        return new CallEvidenceCandidate(grammarRule, callRange, calleeRange, receiver,
                calleeKind, terminalName, arguments, lexicalScopePath,
                calleePathRanges, databaseLinkRange);
    }

    public CallEvidenceCandidate withCalleeStructure(
            List<SourceRangeCandidate> pathRanges,
            SourceRangeCandidate linkRange) {
        return new CallEvidenceCandidate(grammarRule, callRange, calleeRange, receiverRange,
                calleeKind, terminalName, arguments, scopePath,
                pathRanges, linkRange);
    }

    private static SourceRangeCandidate range(Token start, Token stop) {
        if (start == null || stop == null) {
            throw new IllegalArgumentException("ANTLR token boundary is required");
        }
        return new SourceRangeCandidate(start.getStartIndex(), stop.getStopIndex() + 1);
    }
}
