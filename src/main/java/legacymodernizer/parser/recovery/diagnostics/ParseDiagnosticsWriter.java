package legacymodernizer.parser.recovery.diagnostics;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import legacymodernizer.parser.recovery.diagnostics.DiagnosticPhase;
import legacymodernizer.parser.recovery.quality.QualityDecision;
import legacymodernizer.parser.parsing.RawParseResult;
import legacymodernizer.parser.recovery.evidence.RecoveryOutcome;
import legacymodernizer.parser.intake.ParserWorkspace;

@Component
public final class ParseDiagnosticsWriter {

    private final ParserWorkspace parserWorkspace;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
            .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);

    public ParseDiagnosticsWriter(ParserWorkspace parserWorkspace) {
        this.parserWorkspace = parserWorkspace;
    }

    public Path write(Path sourceFile, Path sourceRoot,
                      RawParseResult firstPass, QualityDecision decision) throws IOException {
        return write(sourceFile, sourceRoot, firstPass,
                new RecoveryOutcome(decision.accepted() ? firstPass.astJson() : null,
                        decision, List.of(), 0, 0, decision.accepted() ? 0 : 1));
    }

    public Path write(Path sourceFile, Path sourceRoot,
                      RawParseResult firstPass, RecoveryOutcome recovery) throws IOException {
        return write(sourceFile, sourceRoot, firstPass, recovery, firstPass.elapsedMillis());
    }

    public Path write(Path sourceFile, Path sourceRoot,
                      RawParseResult firstPass, RecoveryOutcome recovery,
                      long processingElapsedMillis) throws IOException {
        String relative = sourceRoot.relativize(sourceFile).toString().replace('\\', '/');
        Path output = parserWorkspace.diagnosticsDir().resolve(relative + ".parse.json");
        Files.createDirectories(output.getParent());

        int lexerErrors = (int) firstPass.diagnostics().stream()
                .filter(diagnostic -> diagnostic.phase() == DiagnosticPhase.LEXER).count();
        int parserErrors = (int) firstPass.diagnostics().stream()
                .filter(diagnostic -> diagnostic.phase() == DiagnosticPhase.PARSER).count();
        int agentAttempts = (int) recovery.units().stream()
                .flatMap(unit -> unit.attempts().stream())
                .filter(attempt -> "REPAIR_AGENT".equals(attempt.stage())).count();
        ParseDiagnosticsSidecar sidecar = new ParseDiagnosticsSidecar(
                "1.1.0",
                relative,
                firstPass.language(),
                firstPass.sourceSha256(),
                firstPass.grammarRevision(),
                recovery.decision().status(),
                new ParseDiagnosticsSidecar.FirstPassEvidence(
                        firstPass.entryRule(), firstPass.diagnostics(), firstPass.antlrRecoveries(),
                        firstPass.coverage(), firstPass.elapsedMillis(), recovery.decision().qualityTuple(),
                        recovery.decision().reasons()),
                recovery.units(),
                new ParseDiagnosticsSidecar.Summary(
                        lexerErrors, parserErrors, firstPass.antlrRecoveries(),
                        firstPass.coverage().declarationsDiscovered(),
                        firstPass.coverage().declarationsEmitted(), agentAttempts,
                        firstPass.elapsedMillis(),
                        Math.max(firstPass.elapsedMillis(), processingElapsedMillis)));
        Files.writeString(output, objectMapper.writeValueAsString(sidecar), StandardCharsets.UTF_8);
        return output;
    }
}
