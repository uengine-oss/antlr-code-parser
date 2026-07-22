package legacymodernizer.parser.recovery.repair;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import legacymodernizer.parser.recovery.boundaries.SourceUnit;
import legacymodernizer.parser.recovery.boundaries.UnitKind;
import legacymodernizer.parser.recovery.diagnostics.DiagnosticPhase;
import legacymodernizer.parser.recovery.diagnostics.ParseDiagnostic;
import legacymodernizer.parser.recovery.localization.ContextSlice;
import legacymodernizer.parser.recovery.localization.ErrorSpanLocator;
import legacymodernizer.parser.recovery.localization.SliceLevel;
import legacymodernizer.parser.recovery.localization.SliceSyntax;
import legacymodernizer.parser.recovery.quality.DeclarationCoverage;
import legacymodernizer.parser.parsing.RawParseResult;

/** Spec 012 FR-021/025: the Agent request never carries the full unit, only the slice. */
class FailureEnvelopeSliceTest {

    @Test
    void largeUnitEnvelopeCarriesOnlyTheBoundedSlice() {
        StringBuilder body = new StringBuilder(
                "CREATE OR REPLACE PROCEDURE BULK_LOAD(P IN NUMBER) IS\nBEGIN\n");
        for (int line = 0; line < 800; line++) {
            body.append("  V").append(line).append(" := ").append(line).append(";\n");
        }
        body.append("  SELECT 1 INTO V0 FROM DUAL T AS X;\n");
        int errorLine = 2 + 800 + 1; // unit-relative line of the SELECT
        body.append("END;\n");
        String unitText = body.toString();
        assertTrue(unitText.length() > 10_000, "fixture must be a large unit");

        SourceUnit unit = new SourceUnit("unit-big", UnitKind.PROCEDURE, "BULK_LOAD", null,
                0, unitText.length(), 1, errorLine + 2, 0, "CONSERVATIVE");
        ParseDiagnostic diagnostic = new ParseDiagnostic(DiagnosticPhase.PARSER, "ERROR",
                "ANTLR_PARSER_SYNTAX", "extraneous input 'AS'", errorLine,
                unitLineColumn(unitText, errorLine, "AS X"), "AS", "identifier",
                List.of("table_ref"), "DUAL T AS X");
        RawParseResult parse = new RawParseResult("oracle", "grammar-1", "sql_script",
                "0".repeat(64), "{}", List.of(diagnostic), 1, DeclarationCoverage.unknown(), 5);

        ErrorSpanLocator locator = new ErrorSpanLocator();
        int anchor = locator.anchorOffset(unitText, unit.startLine(),
                diagnostic.line(), diagnostic.column());
        ContextSlice slice = locator.slice(unitText, SliceSyntax.sql(), anchor, SliceLevel.L1);

        FailureEnvelope envelope = new FailureEnvelopeFactory().create(
                "oracle", "f".repeat(64), unit, unitText, slice, parse, List.of(), 3);

        assertEquals(slice.text(), envelope.sourceExcerpt());
        assertNotEquals(unitText, envelope.sourceExcerpt());
        assertTrue(envelope.sourceExcerpt().length() <= SliceLevel.L1.maxChars(),
                "excerpt must respect the slice budget");
        assertTrue(envelope.sourceExcerpt().contains("AS X"), "error site must be inside");
        assertTrue((double) envelope.sourceExcerpt().length() / unitText.length() < 0.10,
                "transfer ratio must collapse for large units");
        assertEquals("L1", envelope.sliceLevel());
        assertEquals(slice.unitStartOffset(), envelope.sliceUnitStartOffset());
        assertEquals(unit.startOffset() + slice.unitStartOffset(),
                envelope.sliceFileStartOffset());
        assertEquals(0, envelope.constraints().allowedStartOffset());
        assertEquals(envelope.sourceExcerpt().length(),
                envelope.constraints().allowedEndOffset());
        // Diagnostic offsets are excerpt-relative and point at the offending token.
        DiagnosticEvidence evidence = envelope.diagnostics().get(0);
        assertEquals("AS", envelope.sourceExcerpt().substring(
                evidence.excerptStartOffset(), evidence.excerptEndOffset()));
    }

    private static int unitLineColumn(String unitText, int line, String needle) {
        String[] lines = unitText.split("\n", -1);
        return lines[line - 1].indexOf(needle);
    }
}
