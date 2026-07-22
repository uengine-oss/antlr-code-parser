package legacymodernizer.parser.recovery;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import legacymodernizer.parser.recovery.quality.DeclarationCoverage;
import legacymodernizer.parser.recovery.diagnostics.DiagnosticPhase;
import legacymodernizer.parser.recovery.diagnostics.ParseDiagnostic;
import legacymodernizer.parser.parsing.RawParseResult;
import legacymodernizer.parser.recovery.boundaries.SourceUnit;
import legacymodernizer.parser.recovery.boundaries.UnitKind;
import legacymodernizer.parser.parsing.languages.oracle.OracleTableAliasAsRule;

class OracleTableAliasAsRuleTest {

    @Test
    void ignoresAliasesInsideStringsAndComments() {
        String source = "BEGIN\n"
                + "  v_text := 'FROM STRING_TABLE AS S';\n"
                + "  -- FROM COMMENT_TABLE AS C\n"
                + "  /* JOIN BLOCK_TABLE AS B */\n"
                + "  SELECT A.ID INTO v_id FROM REAL_TABLE AS A, OTHER_TABLE AS B,\n"
                + "    (SELECT ID FROM THIRD_TABLE) AS C;\n"
                + "END;";
        SourceUnit unit = new SourceUnit("unit", UnitKind.PROCEDURE, "sample", null,
                0, source.length(), 1, 7, 0, "EXACT");
        String failureLine = "  SELECT A.ID INTO v_id FROM REAL_TABLE AS A, OTHER_TABLE AS B,";
        ParseDiagnostic diagnostic = new ParseDiagnostic(DiagnosticPhase.PARSER, "ERROR", "syntax",
                "unexpected alias", 5, failureLine.indexOf("A, OTHER"), "A", null, List.of(), "");
        RawParseResult failed = new RawParseResult("oracle", "test", "sql_script", "hash", "{}",
                List.of(diagnostic), 1, DeclarationCoverage.unknown(), 0);

        var proposal = new OracleTableAliasAsRule().propose(source, unit, failed);

        assertEquals(3, proposal.edits().size());
        for (var edit : proposal.edits()) {
            assertEquals(" AS ", source.substring(edit.startOffset(), edit.endOffset()));
            assertEquals(edit.endOffset() - edit.startOffset(), edit.replacement().length());
        }
    }
}
