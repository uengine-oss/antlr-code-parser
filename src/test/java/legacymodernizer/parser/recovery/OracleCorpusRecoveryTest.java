package legacymodernizer.parser.recovery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import legacymodernizer.parser.recovery.quality.QualityDecision;
import legacymodernizer.parser.parsing.RawParseResult;
import legacymodernizer.parser.recovery.evidence.RecoveryOutcome;
import legacymodernizer.parser.recovery.quality.ParseQualityGate;
import legacymodernizer.parser.parsing.languages.oracle.OracleTableAliasAsRule;
import legacymodernizer.parser.recovery.rules.RecoveryRuleRegistry;
import legacymodernizer.parser.recovery.rules.Utf8BomRule;
import legacymodernizer.parser.recovery.workingcopy.Hashes;
import legacymodernizer.parser.intake.ParserWorkspace;
import legacymodernizer.parser.intake.SourceIntakeClassifier;
import legacymodernizer.parser.service.ParseProgressTracker;
import legacymodernizer.parser.parsing.languages.oracle.OracleLanguageModule;

class OracleCorpusRecoveryTest {

    @Test
    void measuresRealOracleCorpusWithoutModifyingIt() throws Exception {
        String configured = System.getProperty("parser.oracle.corpus", "");
        Assumptions.assumeTrue(!configured.isBlank(), "Set -Dparser.oracle.corpus for real-corpus validation");
        Path original = Path.of(configured).toAbsolutePath().normalize();
        assertTrue(Files.isRegularFile(original));
        String originalHash = Hashes.sha256(Files.readAllBytes(original));

        ParserWorkspace storage = new ParserWorkspace(new SourceIntakeClassifier());
        Path isolated = storage.sourceDir().resolve("real-corpus/oracle/AMS_procedures.sql");
        Files.createDirectories(isolated.getParent());
        Files.copy(original, isolated, StandardCopyOption.REPLACE_EXISTING);

        OracleLanguageModule module = new OracleLanguageModule(storage);
        ParseQualityGate gate = new ParseQualityGate();
        RawParseResult first = module.parseFile(isolated.toFile(),
                new ParseProgressTracker(null, isolated.getFileName().toString()));
        QualityDecision firstDecision = gate.evaluateFirstPass(first);
        RecoveryOutcome outcome = new LayeredRecoveryPipeline(gate, new RecoveryRuleRegistry(
                java.util.List.of(new Utf8BomRule(), new OracleTableAliasAsRule()))).recover(
                module, isolated, storage.sourceDir(), first, firstDecision,
                new ParseProgressTracker(null, isolated.getFileName().toString()));

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("source", original.toString().replace('\\', '/'));
        report.put("sourceSha256", originalHash);
        report.put("firstPassStatus", firstDecision.status());
        report.put("firstPassDiagnostics", first.diagnostics().size());
        report.put("firstPassRecoveries", first.antlrRecoveries());
        report.put("finalStatus", outcome.decision().status());
        report.put("detectedUnits", outcome.units().size());
        report.put("exactReusedUnits", outcome.exactReusedUnits());
        report.put("recoveredUnits", outcome.recoveredUnits());
        report.put("unresolvedUnits", outcome.unresolvedUnits());
        report.put("units", outcome.units());
        Path reportPath = Path.of("target", "corpus-reports", "ams-recovery.json");
        Files.createDirectories(reportPath.getParent());
        new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT).writeValue(reportPath.toFile(), report);

        assertEquals(originalHash, Hashes.sha256(Files.readAllBytes(original)), "Original corpus file changed");
        assertTrue(outcome.units().size() >= 1);
        System.out.printf("AMS units=%d reused=%d recovered=%d unresolved=%d status=%s%n",
                outcome.units().size(), outcome.exactReusedUnits(), outcome.recoveredUnits(),
                outcome.unresolvedUnits(), outcome.decision().status());
    }
}
