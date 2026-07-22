package legacymodernizer.parser.recovery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import legacymodernizer.parser.recovery.boundaries.SourceUnit;
import legacymodernizer.parser.recovery.boundaries.UnitKind;
import legacymodernizer.parser.parsing.languages.oracle.OracleSourceUnitLocator;

class OracleSourceUnitLocatorTest {

    @Test
    void locatesStandaloneObjectsAndIgnoresTerminatorsInsideTextAndComments() {
        String source = "-- CREATE PROCEDURE ignored AS BEGIN NULL; END; /\n"
                + "CREATE OR REPLACE PROCEDURE first_proc AS\n"
                + "  text_value VARCHAR2(10) := '/';\n"
                + "BEGIN NULL; END;\n/\n"
                + "CREATE FUNCTION second_fn RETURN NUMBER AS\n"
                + "BEGIN RETURN 1; END;\n/\n"
                + "CREATE OR REPLACE PACKAGE BODY sample_pkg AS\n"
                + "  PROCEDURE nested_proc AS BEGIN NULL; END;\n"
                + "END sample_pkg;\n/\n";

        List<SourceUnit> units = new OracleSourceUnitLocator().locate(source);
        assertEquals(3, units.size());
        assertEquals(List.of(UnitKind.PROCEDURE, UnitKind.FUNCTION, UnitKind.PACKAGE),
                units.stream().map(SourceUnit::kind).toList());
        assertEquals(List.of("first_proc", "second_fn", "sample_pkg"),
                units.stream().map(SourceUnit::name).toList());
        assertTrue(units.stream().allMatch(unit -> unit.boundaryConfidence().equals("EXACT")));
        assertTrue(units.get(0).endOffset() <= units.get(1).startOffset());
        assertTrue(units.get(1).endOffset() <= units.get(2).startOffset());
    }

    @Test
    void fallsBackToFileWhenNoDeclarationIsDiscoverable() {
        List<SourceUnit> units = new OracleSourceUnitLocator().locate("BEGIN NULL; END;\n");
        assertEquals(1, units.size());
        assertEquals(UnitKind.FILE, units.get(0).kind());
        assertEquals("CONSERVATIVE", units.get(0).boundaryConfidence());
    }
}
