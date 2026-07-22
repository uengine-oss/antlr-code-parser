package legacymodernizer.parser.recovery.reports;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import legacymodernizer.parser.parsing.RawParseResult;
import legacymodernizer.parser.recovery.evidence.RecoveryOutcome;
import legacymodernizer.parser.intake.ParserWorkspace;

@Component
public final class RepairAuditWriter {

    private final ParserWorkspace parserWorkspace;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
            .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);

    public RepairAuditWriter(ParserWorkspace parserWorkspace) {
        this.parserWorkspace = parserWorkspace;
    }

    public Path write(Path sourceFile, Path sourceRoot,
                      RawParseResult firstPass, RecoveryOutcome outcome) throws IOException {
        if (outcome.units().isEmpty()) return null;
        String relative = sourceRoot.relativize(sourceFile).toString().replace('\\', '/');
        Path output = parserWorkspace.repairsDir().resolve(relative + ".repair.json");
        Files.createDirectories(output.getParent());
        int agentAttempts = (int) outcome.units().stream()
                .flatMap(unit -> unit.attempts().stream())
                .filter(attempt -> "REPAIR_AGENT".equals(attempt.stage()))
                .count();
        RepairAuditSidecar sidecar = new RepairAuditSidecar(
                "1.0.0", relative, firstPass.sourceSha256(), firstPass.language(),
                firstPass.grammarRevision(), outcome.decision().status(),
                outcome.exactReusedUnits(), outcome.recoveredUnits(),
                outcome.unresolvedUnits(), agentAttempts, outcome.units());
        Files.writeString(output, objectMapper.writeValueAsString(sidecar), StandardCharsets.UTF_8);
        return output;
    }
}
