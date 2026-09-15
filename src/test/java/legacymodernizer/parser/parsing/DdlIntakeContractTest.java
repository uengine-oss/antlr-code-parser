package legacymodernizer.parser.parsing;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import legacymodernizer.parser.intake.ParserWorkspace;
import legacymodernizer.parser.api.stream.ParseStreamEvent;

@SpringBootTest
class DdlIntakeContractTest {
    @Autowired private ParseOrchestrator orchestrator;
    @Autowired private ParserWorkspace workspace;
    @TempDir Path input;

    @Test
    void ddlOnlyIntakeActuallyParsesAndPublishesScopedEvidence() throws Exception {
        assertTrue(System.getProperty("parser.data.root", "").replace('\\', '/')
                .contains("/target/test-data"));
        String text = "CREATE TABLE IF NOT EXISTS hr.staff (id integer PRIMARY KEY, name text);";
        Files.writeString(input.resolve("schema.sql"), text);
        var events = new ArrayList<ParseStreamEvent>();
        orchestrator.parse(input.toString(), events::add);
        Path artifact = workspace.analysisDir().resolve("ddl/schema.json");
        assertTrue(Files.isRegularFile(artifact), "intake count is not proof of a DDL artifact");
        var evidence = new ObjectMapper().readTree(artifact.toFile()).path("evidence");
        assertEquals("ddl/schema.sql", evidence.path("sourceId").asText());
        assertEquals(text, evidence.path("decodedText").asText());
        assertEquals("exact", evidence.path("syntaxArena").path("status").asText());
        assertEquals(text, Files.readString(input.resolve("schema.sql")));
    }

    @Test
    void ambiguousOutputLayoutIsRejectedBeforeAnyArtifactIsWritten() throws Exception {
        Files.createDirectories(input.resolve("ddl"));
        Files.writeString(input.resolve("ddl/schema.java"), "class schema {}\n");
        Files.writeString(input.resolve("schema.sql"), "CREATE TABLE IF NOT EXISTS hr.staff (id integer);\n");
        var events = new ArrayList<ParseStreamEvent>();
        assertThrows(IllegalArgumentException.class, () -> orchestrator.parse(input.toString(), events::add));
        try (var outputs = Files.walk(workspace.analysisDir())) {
            assertEquals(0, outputs.filter(Files::isRegularFile).count());
        }
    }
}
