package legacymodernizer.parser.recovery.reports;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import legacymodernizer.parser.intake.ParserWorkspace;
import legacymodernizer.parser.intake.SourceIntakeClassifier;

class RepairPromotionReporterTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private final RepairPromotionReporter reporter = new RepairPromotionReporter(
            new ParserWorkspace(new SourceIntakeClassifier()));

    @Test
    void clustersRepeatedSuccessfulRepairsWithoutAutomaticPromotion() throws Exception {
        JsonNode first = audit("a.sql", "unit-a", 10);
        JsonNode second = audit("b.sql", "unit-b", 900);

        RepairPromotionReport forward = reporter.buildReport(List.of(first, second));
        RepairPromotionReport reversed = reporter.buildReport(List.of(second, first));

        assertEquals(forward, reversed);
        assertEquals(1, forward.candidates().size());
        RepairPromotionReport.Candidate candidate = forward.candidates().get(0);
        assertEquals(2, candidate.occurrenceCount());
        assertEquals(List.of("a.sql", "b.sql"), candidate.sourcePaths());
        assertEquals(List.of("unit-a", "unit-b"), candidate.unitIds());
        assertEquals(List.of("ANTLR_PARSER_SYNTAX"), candidate.diagnosticCodes());
        assertTrue(candidate.reviewOptions().contains("RULE_REGRESSION_EXPANSION"));
        assertTrue(candidate.reviewOptions().contains("PINNED_GRAMMAR_PATCH"));
        assertTrue(candidate.regressionFixtureTemplate().automaticPromotionForbidden());
        assertTrue(candidate.regressionFixtureTemplate().reviewerMustSupplyMinimalSource());
        assertTrue(candidate.regressionFixtureTemplate().expectedAstGoldenRequired());
    }

    @Test
    void doesNotPromoteASingleOccurrence() throws Exception {
        RepairPromotionReport report = reporter.buildReport(List.of(audit("one.sql", "one", 1)));
        assertTrue(report.candidates().isEmpty());
    }

    private JsonNode audit(String sourcePath, String unitId, int offset) throws Exception {
        String diff = "--- a/" + sourcePath + "#" + unitId + "\n"
                + "+++ b/" + sourcePath + "#" + unitId + "\n"
                + "@@ offset " + offset + "," + (offset + 4)
                + " rule oracle.remove-table-alias-as.v1 @@\n"
                + "- AS \\n+   \n";
        var audit = mapper.createObjectNode();
        audit.put("sourcePath", sourcePath);
        audit.put("language", "oracle");
        audit.put("grammarRevision", "PlSql");
        var unit = audit.putArray("units").addObject();
        unit.put("accepted", true);
        unit.putObject("unit").put("unitId", unitId).put("kind", "PROCEDURE");
        var failed = unit.putArray("attempts").addObject();
        failed.put("stage", "MINIMAL_UNIT_EXACT");
        failed.putArray("qualityReasons").add("PARSER_ERRORS");
        failed.putArray("diagnostics").addObject().put("code", "ANTLR_PARSER_SYNTAX");
        var successful = unit.withArray("attempts").addObject();
        successful.put("stage", "SAFE_RULE");
        successful.put("ruleId", "oracle.remove-table-alias-as.v1");
        successful.put("diff", diff);
        successful.putArray("qualityReasons");
        successful.putArray("diagnostics");
        return audit;
    }
}
