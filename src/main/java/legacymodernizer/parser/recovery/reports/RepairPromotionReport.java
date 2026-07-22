package legacymodernizer.parser.recovery.reports;

import java.util.List;

public record RepairPromotionReport(
        String schemaVersion,
        int minimumOccurrences,
        List<Candidate> candidates) {

    public record Candidate(
            String signatureSha256,
            String language,
            String grammarRevision,
            String unitKind,
            String successfulStage,
            String ruleId,
            List<String> diagnosticCodes,
            int occurrenceCount,
            List<String> sourcePaths,
            List<String> unitIds,
            List<String> reviewOptions,
            RegressionFixtureTemplate regressionFixtureTemplate) {
    }

    public record RegressionFixtureTemplate(
            String language,
            String grammarRevision,
            String unitKind,
            List<String> diagnosticCodes,
            boolean reviewerMustSupplyMinimalSource,
            boolean expectedAstGoldenRequired,
            boolean automaticPromotionForbidden) {
    }
}
