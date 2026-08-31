package legacymodernizer.parser.parsing.evidence;

import java.util.List;

/** Grammar-owned structure for one ordered call argument. */
public record CallArgumentEvidenceCandidate(
        SourceRangeCandidate range,
        String syntaxKind,
        String literalKind,
        String literalValue,
        String identifier) {

    public CallArgumentEvidenceCandidate {
        if (range == null) {
            throw new IllegalArgumentException("call argument range is required");
        }
        if (!List.of("string_literal", "identifier", "expression")
                .contains(syntaxKind)) {
            throw new IllegalArgumentException("unsupported argument syntaxKind: " + syntaxKind);
        }
        if ("string_literal".equals(syntaxKind)) {
            if (!"string".equals(literalKind) || literalValue == null || identifier != null) {
                throw new IllegalArgumentException(
                        "string literal argument requires only its decoded string value");
            }
        } else if ("identifier".equals(syntaxKind)) {
            if (identifier == null || identifier.isBlank()
                    || literalKind != null || literalValue != null) {
                throw new IllegalArgumentException(
                        "identifier argument requires only its identifier value");
            }
        } else if (literalKind != null || literalValue != null || identifier != null) {
            throw new IllegalArgumentException(
                    "expression argument cannot claim literal or identifier values");
        }
    }

    public static CallArgumentEvidenceCandidate expression(SourceRangeCandidate range) {
        return new CallArgumentEvidenceCandidate(range, "expression", null, null, null);
    }

    public static CallArgumentEvidenceCandidate stringLiteral(
            SourceRangeCandidate range, String value) {
        return new CallArgumentEvidenceCandidate(
                range, "string_literal", "string", value, null);
    }

    public static CallArgumentEvidenceCandidate identifier(
            SourceRangeCandidate range, String value) {
        return new CallArgumentEvidenceCandidate(
                range, "identifier", null, null, value);
    }
}
