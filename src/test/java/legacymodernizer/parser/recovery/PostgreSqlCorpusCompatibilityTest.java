package legacymodernizer.parser.recovery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import legacymodernizer.parser.intake.ParserWorkspace;
import legacymodernizer.parser.intake.SourceIntakeClassifier;
import legacymodernizer.parser.parsing.RawParseResult;
import legacymodernizer.parser.parsing.languages.postgresql.PostgreSqlLanguageModule;
import legacymodernizer.parser.recovery.quality.ParseQualityGate;
import legacymodernizer.parser.recovery.quality.QualityDecision;
import legacymodernizer.parser.recovery.workingcopy.Hashes;
import legacymodernizer.parser.service.ParseProgressTracker;

class PostgreSqlCorpusCompatibilityTest {

    @Test
    void parsesRealPostgreSqlDdlDeterministicallyWithoutModifyingIt() throws Exception {
        String configured = System.getProperty("parser.postgresql.corpus", "");
        Assumptions.assumeTrue(!configured.isBlank(),
                "Set -Dparser.postgresql.corpus for real-corpus validation");
        Path original = Path.of(configured).toAbsolutePath().normalize();
        assertTrue(Files.isRegularFile(original));
        String originalHash = Hashes.sha256(Files.readAllBytes(original));

        ParserWorkspace parserWorkspace = new ParserWorkspace(new SourceIntakeClassifier());
        Path isolatedSource = parserWorkspace.sourceDir()
                .resolve("real-corpus/postgresql/RWIS_postgres_ddl_UPPER.sql");
        Files.createDirectories(isolatedSource.getParent());
        Files.copy(original, isolatedSource, StandardCopyOption.REPLACE_EXISTING);

        PostgreSqlLanguageModule module = new PostgreSqlLanguageModule(parserWorkspace);
        ParseProgressTracker firstTracker = new ParseProgressTracker(
                null, isolatedSource.getFileName().toString());
        ParseProgressTracker secondTracker = new ParseProgressTracker(
                null, isolatedSource.getFileName().toString());
        RawParseResult first = module.parseFile(isolatedSource.toFile(), firstTracker);
        RawParseResult second = module.parseFile(isolatedSource.toFile(), secondTracker);
        QualityDecision decision = new ParseQualityGate().evaluateFirstPass(first);

        Path isolatedAst = parserWorkspace.analysisDir()
                .resolve("real-corpus/postgresql/RWIS_postgres_ddl_UPPER.json");
        Files.createDirectories(isolatedAst.getParent());
        Files.writeString(isolatedAst, first.astJson(), StandardCharsets.UTF_8);

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("source", original.toString().replace('\\', '/'));
        report.put("sourceSha256", originalHash);
        report.put("status", decision.status());
        report.put("accepted", decision.accepted());
        report.put("diagnostics", first.diagnostics().size());
        report.put("antlrRecoveries", first.antlrRecoveries());
        report.put("declarationsDiscovered", first.coverage().declarationsDiscovered());
        report.put("declarationsEmitted", first.coverage().declarationsEmitted());
        report.put("astSha256", Hashes.sha256(first.astJson().getBytes(StandardCharsets.UTF_8)));
        report.put("deterministic", first.astJson().equals(second.astJson()));
        Path reportPath = Path.of("target", "corpus-reports", "postgresql-corpus.json");
        Files.createDirectories(reportPath.getParent());
        new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT)
                .writeValue(reportPath.toFile(), report);

        assertEquals(originalHash, Hashes.sha256(Files.readAllBytes(original)),
                "Original PostgreSQL corpus file changed");
        assertTrue(decision.accepted(), () -> "PostgreSQL corpus was rejected: " + decision.reasons());
        assertEquals(first.astJson(), second.astJson(), "PostgreSQL AST bytes changed on repeat parse");
    }
}
