package legacymodernizer.parser.parsing.evidence;

import java.util.List;

/** Grammar-owned C preprocessing macro declaration. */
public record MacroEvidenceCandidate(
        String grammarRule,
        SourceRangeCandidate range,
        SourceRangeCandidate nameRange,
        String macroKind,
        String terminalName,
        List<SourceRangeCandidate> parameterRanges,
        boolean variadic,
        SourceRangeCandidate replacementRange) {

    public MacroEvidenceCandidate {
        if (grammarRule == null || grammarRule.isBlank()) {
            throw new IllegalArgumentException("grammarRule is required");
        }
        if (!List.of("object", "function").contains(macroKind)) {
            throw new IllegalArgumentException("unsupported macroKind: " + macroKind);
        }
        if (terminalName == null || terminalName.isBlank()) {
            throw new IllegalArgumentException("macro terminalName is required");
        }
        parameterRanges = List.copyOf(parameterRanges == null ? List.of() : parameterRanges);
        if ("object".equals(macroKind) && (!parameterRanges.isEmpty() || variadic)) {
            throw new IllegalArgumentException("object macro cannot claim parameters");
        }
    }
}
