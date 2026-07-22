package legacymodernizer.parser.recovery.repair;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

class PatchProposalValidatorTest {

    private final PatchProposalValidator validator = new PatchProposalValidator();

    @Test
    void acceptsOnlyOrderedBoundedMinimalTextEdits() {
        FailureEnvelope envelope = envelope("TABLE AS A");
        PatchProposal proposal = proposal(envelope,
                List.of(new AgentTextEdit(6, 8, "AS", "  ",
                        "Remove invalid alias keyword")));

        var edits = validator.validate(envelope, proposal);

        assertEquals(1, edits.size());
        assertEquals(6, edits.get(0).startOffset());
        assertEquals(8, edits.get(0).endOffset());
    }

    @Test
    void rejectsWholeExcerptReplacementEvenWhenTheExcerptIsShort() {
        FailureEnvelope envelope = envelope("TABLE AS A");
        PatchProposal proposal = proposal(envelope, List.of(new AgentTextEdit(
                0, envelope.sourceExcerpt().length(), envelope.sourceExcerpt(),
                "SELECT 1", "Replace everything")));

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> validator.validate(envelope, proposal));

        assertEquals("AGENT_FULL_EXCERPT_REWRITE_FORBIDDEN", error.getMessage());
    }

    @Test
    void reanchorsExcerptOffsetsOntoTheUnitFrame() {
        FailureEnvelope envelope = envelope("TABLE AS A", 40);
        PatchProposal proposal = proposal(envelope,
                List.of(new AgentTextEdit(6, 8, "AS", "  ", "Remove invalid alias keyword")));

        var edits = validator.validate(envelope, proposal);

        assertEquals(46, edits.get(0).startOffset(), "unit frame = slice start + excerpt offset");
        assertEquals(48, edits.get(0).endOffset());
    }

    @Test
    void rejectsNodeJsonDisguisedAsReplacementText() {
        FailureEnvelope envelope = envelope("TABLE AS A");
        PatchProposal proposal = proposal(envelope, List.of(new AgentTextEdit(6, 8, "AS",
                "{\"type\":\"QUERY\",\"startLine\":1,\"children\":[]}",
                "Return an AST instead of source")));

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> validator.validate(envelope, proposal));

        assertEquals("AGENT_AST_PAYLOAD_FORBIDDEN", error.getMessage());
    }

    @Test
    void rejectsOverlappingAndOutOfRangeEdits() {
        FailureEnvelope envelope = envelope("TABLE AS A");
        PatchProposal overlapping = proposal(envelope, List.of(
                new AgentTextEdit(2, 5, "BLE", "", "first"),
                new AgentTextEdit(4, 6, "E ", "", "overlap")));
        PatchProposal escaped = proposal(envelope, List.of(
                new AgentTextEdit(0, envelope.sourceExcerpt().length() + 1,
                        "", "", "outside")));

        assertEquals("AGENT_EDIT_OUT_OF_RANGE_OR_OVERLAPPING",
                assertThrows(IllegalArgumentException.class,
                        () -> validator.validate(envelope, overlapping)).getMessage());
        assertEquals("AGENT_EDIT_OUT_OF_RANGE_OR_OVERLAPPING",
                assertThrows(IllegalArgumentException.class,
                        () -> validator.validate(envelope, escaped)).getMessage());
    }

    @Test
    void reanchorsWrongOffsetsWhenExpectedTextOccursExactlyOnce() {
        FailureEnvelope envelope = envelope("TABLE AS A");
        PatchProposal proposal = proposal(envelope, List.of(
                new AgentTextEdit(9, 10, "AS", "  ", "wrong offset, unique text")));

        var edits = validator.validate(envelope, proposal);

        assertEquals(6, edits.get(0).startOffset(), "unique expectedText wins over offsets");
        assertEquals(8, edits.get(0).endOffset());
    }

    @Test
    void rejectsWrongOffsetsWhenExpectedTextIsAmbiguousOrAbsent() {
        FailureEnvelope envelope = envelope("TABLE AS A");
        PatchProposal ambiguous = proposal(envelope, List.of(
                new AgentTextEdit(0, 1, "A", "", "A occurs three times")));
        PatchProposal absent = proposal(envelope, List.of(
                new AgentTextEdit(0, 1, "ZZ", "", "not in excerpt")));

        assertEquals("AGENT_EXPECTED_TEXT_MISMATCH",
                assertThrows(IllegalArgumentException.class,
                        () -> validator.validate(envelope, ambiguous)).getMessage());
        assertEquals("AGENT_EXPECTED_TEXT_MISMATCH",
                assertThrows(IllegalArgumentException.class,
                        () -> validator.validate(envelope, absent)).getMessage());
    }

    @Test
    void rejectsAnEditThatDoesNotChangeTheSelectedText() {
        FailureEnvelope envelope = envelope("TABLE AS A");
        PatchProposal proposal = proposal(envelope, List.of(
                new AgentTextEdit(9, 10, "A", "A", "no change")));

        assertEquals("AGENT_NO_OP_EDIT",
                assertThrows(IllegalArgumentException.class,
                        () -> validator.validate(envelope, proposal)).getMessage());
    }

    private static PatchProposal proposal(FailureEnvelope envelope, List<AgentTextEdit> edits) {
        return new PatchProposal("1.0.0", envelope.failureEnvelopeHash(), edits,
                "Minimal syntax-only repair", 0.95, List.of());
    }

    private static FailureEnvelope envelope(String source) {
        return envelope(source, 0);
    }

    private static FailureEnvelope envelope(String source, int sliceUnitStartOffset) {
        return new FailureEnvelope("2.0.0", "a".repeat(64), "oracle", "grammar-1",
                "b".repeat(64), "c".repeat(64), "unit-1", "PROCEDURE",
                100, 100 + sliceUnitStartOffset + source.length(), 5, 5,
                "L1", sliceUnitStartOffset, sliceUnitStartOffset + source.length(),
                100 + sliceUnitStartOffset, 100 + sliceUnitStartOffset + source.length(),
                "d".repeat(64), "", List.of(),
                new CoverageEvidence(1, 0, List.of("alias_proc"), false), List.of(),
                "unit=unit-1; kind=PROCEDURE; name=alias_proc", source,
                List.of(0), List.of(new SourceTokenEvidence(6, 8, "AS")),
                new RepairConstraints(0, source.length(), 64, 4, 3,
                        List.of("AST", "NODE_JSON", "FULL_UNIT_REWRITE")));
    }
}
