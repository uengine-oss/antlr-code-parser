package legacymodernizer.parser.recovery.localization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ErrorSpanLocatorTest {

    private final ErrorSpanLocator locator = new ErrorSpanLocator();

    private static final String ORACLE_UNIT = String.join("\n",
            "CREATE OR REPLACE PROCEDURE ORDER_TOTAL(P_ID IN NUMBER) IS",
            "  V_TOTAL NUMBER := 0;",
            "BEGIN",
            "  -- semicolon inside comment; must not split",
            "  SELECT SUM(AMOUNT)",
            "    INTO V_TOTAL",
            "    FROM ORDERS O AS X",
            "   WHERE O.ID = P_ID;",
            "  UPDATE T SET NOTE = 'text with ; semicolon' WHERE ID = P_ID;",
            "END;",
            "");

    private int anchorAt(String needle) {
        int offset = ORACLE_UNIT.indexOf(needle);
        assertTrue(offset >= 0, "fixture must contain " + needle);
        return offset;
    }

    @Test
    void anchorOffsetMapsFileLineAndColumnIntoUnitOffsets() {
        // unit starts at file line 10; diagnostic on file line 16 (unit line 7), column 4
        int anchor = locator.anchorOffset(ORACLE_UNIT, 10, 16, 4);
        assertEquals("FROM", ORACLE_UNIT.substring(anchor, anchor + 4));
    }

    @Test
    void level0IsTheDiagnosticLineWindowOnly() {
        ContextSlice slice = locator.slice(ORACLE_UNIT, SliceSyntax.sql(),
                anchorAt("AS X"), SliceLevel.L0);
        assertTrue(slice.text().contains("FROM ORDERS O AS X"));
        assertFalse(slice.text().contains("BEGIN"));
        assertFalse(slice.text().contains("UPDATE"));
        assertTrue(slice.length() <= SliceLevel.L0.maxChars());
        assertEquals("", slice.headerText());
    }

    @Test
    void level1CoversTheWholeStatementBoundedByRealTerminators() {
        ContextSlice slice = locator.slice(ORACLE_UNIT, SliceSyntax.sql(),
                anchorAt("AS X"), SliceLevel.L1);
        assertTrue(slice.text().contains("SELECT SUM(AMOUNT)"));
        assertTrue(slice.text().contains("WHERE O.ID = P_ID;"));
        assertFalse(slice.text().contains("UPDATE"), "next statement must stay out");
        assertEquals(ORACLE_UNIT.substring(slice.unitStartOffset(), slice.unitEndOffset()),
                slice.text());
    }

    @Test
    void semicolonsInsideCommentsAndStringsNeverSplitStatements() {
        ContextSlice fromComment = locator.slice(ORACLE_UNIT, SliceSyntax.sql(),
                anchorAt("SELECT SUM"), SliceLevel.L1);
        assertTrue(fromComment.text().contains("-- semicolon inside comment"),
                "comment semicolon must not terminate the statement scan");
        ContextSlice fromString = locator.slice(ORACLE_UNIT, SliceSyntax.sql(),
                anchorAt("NOTE ="), SliceLevel.L1);
        assertTrue(fromString.text().contains("'text with ; semicolon'"));
        assertTrue(fromString.text().trim().endsWith("WHERE ID = P_ID;"));
    }

    @Test
    void level2AddsThePreviousStatementAndReadOnlyHeader() {
        ContextSlice slice = locator.slice(ORACLE_UNIT, SliceSyntax.sql(),
                anchorAt("NOTE ="), SliceLevel.L2);
        assertTrue(slice.text().contains("SELECT SUM(AMOUNT)"), "previous statement included");
        String declaration = "CREATE OR REPLACE PROCEDURE ORDER_TOTAL";
        assertTrue(slice.headerText().startsWith(declaration)
                        || (slice.unitStartOffset() == 0 && slice.text().startsWith(declaration)),
                "declaration context must be present exactly once (header or slice body)");
    }

    @Test
    void level3ProvidesHeaderWhenTheSliceCannotReachTheUnitStart() {
        StringBuilder body = new StringBuilder("CREATE OR REPLACE PROCEDURE BIG(P IN NUMBER) IS\nBEGIN\n");
        for (int line = 0; line < 400; line++) body.append("  V").append(line).append(" := ").append(line).append(";\n");
        body.append("END;\n");
        String unit = body.toString();
        ContextSlice slice = locator.slice(unit, SliceSyntax.sql(),
                unit.indexOf("V399"), SliceLevel.L3);
        assertTrue(slice.unitStartOffset() > 0, "budget must keep the slice away from unit start");
        assertTrue(slice.headerText().startsWith("CREATE OR REPLACE PROCEDURE BIG"));
        assertTrue(slice.length() <= SliceLevel.L3.maxChars());
    }

    @Test
    void unbalancedBracketsExtendTheStatementUntilBalanced() {
        String unit = "CALL LOG(A,\n  B); FINISH(C);\n";
        ContextSlice slice = locator.slice(unit, SliceSyntax.sql(),
                unit.indexOf("LOG"), SliceLevel.L1);
        assertTrue(slice.text().contains("B);"), "must extend past newline to close bracket");
    }

    @Test
    void budgetsAreEnforcedAndSlicesStayContiguous() {
        StringBuilder big = new StringBuilder();
        for (int line = 0; line < 500; line++) big.append("  V").append(line).append(" := ").append(line).append(";\n");
        String unit = big.toString();
        int anchor = unit.indexOf("V250");
        for (SliceLevel level : SliceLevel.values()) {
            ContextSlice slice = locator.slice(unit, SliceSyntax.sql(), anchor, level);
            assertTrue(slice.length() <= level.maxChars(), level + " over budget");
            assertEquals(unit.substring(slice.unitStartOffset(), slice.unitEndOffset()),
                    slice.text());
            assertTrue(slice.unitStartOffset() <= anchor && anchor <= slice.unitEndOffset(),
                    level + " must contain the anchor");
        }
    }

    @Test
    void sameInputAlwaysProducesTheSameSlice() {
        ContextSlice first = locator.slice(ORACLE_UNIT, SliceSyntax.sql(),
                anchorAt("AS X"), SliceLevel.L1);
        ContextSlice second = locator.slice(ORACLE_UNIT, SliceSyntax.sql(),
                anchorAt("AS X"), SliceLevel.L1);
        assertEquals(first, second);
    }

    @Test
    void pythonWithoutTerminatorsFallsBackToLineWindows() {
        String unit = "def total(items):\n    s = 0\n    for i in items:\n        s += i\n    return s\n";
        ContextSlice slice = locator.slice(unit, SliceSyntax.python(),
                unit.indexOf("for i"), SliceLevel.L1);
        assertTrue(slice.text().contains("for i in items:"));
        assertTrue(slice.length() <= SliceLevel.L1.maxChars());
    }

    @Test
    void sliceOffsetsMapBackToUnitOffsets() {
        ContextSlice slice = locator.slice(ORACLE_UNIT, SliceSyntax.sql(),
                anchorAt("AS X"), SliceLevel.L1);
        int asInSlice = slice.text().indexOf("AS X");
        assertEquals(anchorAt("AS X"), slice.toUnitOffset(asInSlice));
    }
}
