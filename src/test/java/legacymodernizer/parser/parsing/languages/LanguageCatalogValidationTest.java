package legacymodernizer.parser.parsing.languages;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import legacymodernizer.parser.recovery.workingcopy.Hashes;
import legacymodernizer.parser.intake.ParserWorkspace;
import legacymodernizer.parser.intake.SourceIntakeClassifier;
import legacymodernizer.parser.parsing.RawParseResult;
import legacymodernizer.parser.parsing.languages.c.CLanguageModule;
import legacymodernizer.parser.parsing.languages.java.JavaLanguageModule;
import legacymodernizer.parser.parsing.languages.oracle.OracleLanguageModule;
import legacymodernizer.parser.parsing.languages.postgresql.PostgreSqlLanguageModule;
import legacymodernizer.parser.parsing.languages.python.PythonLanguageModule;
import legacymodernizer.parser.service.ParseProgressTracker;

class LanguageCatalogValidationTest {

    @BeforeAll
    static void requireIsolatedDataRoot() {
        assertTrue(System.getProperty("parser.data.root", "").replace('\\', '/')
                .contains("/target/test-data"));
    }

    @Test
    void catalogMatchesDiscoveredModulesAndPinnedGrammarFiles() throws Exception {
        ParserWorkspace workspace = new ParserWorkspace(new SourceIntakeClassifier());
        LanguageCatalogValidator validator = new LanguageCatalogValidator();
        LanguageCatalog catalog = validator.load();
        assertEquals("1.0.0", catalog.schemaVersion());
        assertEquals("4.13.2", catalog.antlrRuntimeVersion());
        assertTrue(catalog.catalogSha256().matches("[0-9a-f]{64}"));

        List<LanguageModule> modules = productionModules(workspace);
        Map<String, LanguageModule> byId = new LinkedHashMap<>();
        modules.forEach(module -> byId.put(module.languageId(), module));
        assertEquals(byId.keySet(), Set.copyOf(catalog.languages().stream().map(LanguageDefinition::id).toList()));

        for (LanguageDefinition language : catalog.languages()) {
            assertEquals(byId.get(language.id()).parseExtensions(), Set.copyOf(language.parseExtensions()),
                    "Parser extension capability drift: " + language.id());
            assertEquals(byId.get(language.id()).recoveryRuleSets(),
                    Set.copyOf(language.recoveryRuleSets()),
                    "Recovery rule-set capability drift: " + language.id());
            assertFalse(language.emittedNodeTypes().isEmpty());
            for (LanguageDefinition.GrammarFile grammarFile : language.grammar().files()) {
                Path path = Path.of(grammarFile.path());
                assertTrue(Files.isRegularFile(path), "Missing pinned grammar: " + path);
                assertEquals(grammarFile.sha256(), Hashes.sha256(Files.readAllBytes(path)),
                        "Grammar changed without catalog update: " + path);
            }
        }

        assertEquals(catalog, validator.validateModules(modules));
        System.out.println("catalogSha256=" + catalog.catalogSha256());
    }

    @Test
    void rejectsUnsupportedCatalogMajorBeforeModuleUse() {
        ParserWorkspace workspace = new ParserWorkspace(new SourceIntakeClassifier());
        LanguageCatalogValidator validator = new LanguageCatalogValidator();
        LanguageCatalog current = validator.load();
        LanguageCatalog unsupported = new LanguageCatalog("2.0.0", current.catalogVersion(),
                current.parserBuild(), current.antlrRuntimeVersion(), current.languages(), "");

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> validator.validateModules(unsupported, productionModules(workspace)));

        assertTrue(error.getMessage().startsWith("Unsupported language catalog schema"));
    }

    @Test
    void syntheticLanguageNeedsOnlyOneModuleAndOneCatalogEntry() {
        ParserWorkspace workspace = new ParserWorkspace(new SourceIntakeClassifier());
        LanguageCatalogValidator validator = new LanguageCatalogValidator();
        LanguageCatalog current = validator.load();
        LanguageDefinition syntheticDefinition = new LanguageDefinition(
                "synthetic", List.of("synthetic-lang"), "framework",
                List.of(".synthetic"), false, Map.of("FILE", "file"),
                List.of("FILE"), List.of("FILE"), List.of(),
                current.languages().get(0).grammar(), List.of("common-safe", "synthetic"));
        List<LanguageDefinition> definitions = new ArrayList<>(current.languages());
        definitions.add(syntheticDefinition);
        LanguageCatalog extendedCatalog = new LanguageCatalog(current.schemaVersion(),
                current.catalogVersion(), current.parserBuild(), current.antlrRuntimeVersion(),
                List.copyOf(definitions), "");

        LanguageModule syntheticModule = new LanguageModule() {
            @Override public RawParseResult parseFile(File file, ParseProgressTracker tracker) {
                throw new UnsupportedOperationException("Not needed for registration test");
            }
            @Override public String languageId() { return "synthetic"; }
            @Override public Set<String> parseExtensions() { return Set.of(".synthetic"); }
        };
        List<LanguageModule> modules = new ArrayList<>(productionModules(workspace));
        modules.add(syntheticModule);

        assertEquals(extendedCatalog,
                validator.validateModules(extendedCatalog, List.copyOf(modules)));
        assertEquals(syntheticModule,
                new LanguageModuleRegistry(List.copyOf(modules)).require("synthetic"));
    }

    private static List<LanguageModule> productionModules(ParserWorkspace workspace) {
        return List.of(new JavaLanguageModule(workspace), new PythonLanguageModule(workspace),
                new CLanguageModule(workspace), new OracleLanguageModule(workspace),
                new PostgreSqlLanguageModule(workspace));
    }
}
