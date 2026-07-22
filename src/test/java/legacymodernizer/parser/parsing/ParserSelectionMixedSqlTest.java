package legacymodernizer.parser.parsing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

import legacymodernizer.parser.intake.ParserWorkspace;
import legacymodernizer.parser.intake.SourceIntakeClassifier;
import legacymodernizer.parser.parsing.languages.LanguageModuleRegistry;
import legacymodernizer.parser.parsing.languages.oracle.OracleLanguageModule;
import legacymodernizer.parser.parsing.languages.postgresql.PostgreSqlLanguageModule;

class ParserSelectionMixedSqlTest {

    @Test
    void resolvesSharedSqlExtensionPerFileInAMixedDbmsProject() throws Exception {
        ParserWorkspace workspace = new ParserWorkspace(new SourceIntakeClassifier());
        Path root = workspace.sourceDir().resolve("mixed-dbms-selection");
        Files.createDirectories(root);
        Path oracle = root.resolve("oracle.sql");
        Path postgresql = root.resolve("postgresql.sql");
        Files.writeString(oracle, "CREATE OR REPLACE PROCEDURE app.work AS\n"
                + "  value VARCHAR2(10);\nBEGIN DBMS_OUTPUT.PUT_LINE(value); END;\n/\n",
                StandardCharsets.UTF_8);
        Files.writeString(postgresql, "CREATE OR REPLACE FUNCTION app.work() RETURNS integer AS $$\n"
                + "BEGIN RETURN 1; END;\n$$ LANGUAGE plpgsql;\n", StandardCharsets.UTF_8);

        ParserSelection selection = new ParserSelection(new LanguageModuleRegistry(List.of(
                new OracleLanguageModule(workspace), new PostgreSqlLanguageModule(workspace))));
        ParserSelection.DetectionResult result = selection.detect(root);

        assertEquals("oracle", result.modulesByFile().get(oracle).languageId());
        assertEquals("postgresql", result.modulesByFile().get(postgresql).languageId());
        assertEquals(java.util.Set.of("oracle", "postgresql"), result.detectedTargets());
        assertNull(result.sqlDialect(), "The legacy singular dialect field is null for mixed SQL");
    }
}
