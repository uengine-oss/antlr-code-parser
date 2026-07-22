package legacymodernizer.parser.language;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import legacymodernizer.parser.parsing.RawParseResult;
import legacymodernizer.parser.parsing.languages.LanguageModule;
import legacymodernizer.parser.parsing.languages.LanguageModuleRegistry;
import legacymodernizer.parser.recovery.candidates.RepairProfile;
import legacymodernizer.parser.recovery.diagnostics.DiagnosticPhase;
import legacymodernizer.parser.recovery.diagnostics.ParseDiagnostic;
import legacymodernizer.parser.recovery.localization.ContextSlice;
import legacymodernizer.parser.recovery.localization.ErrorSpanLocator;
import legacymodernizer.parser.recovery.localization.SliceLevel;
import legacymodernizer.parser.recovery.localization.SliceSyntax;
import legacymodernizer.parser.recovery.quality.DeclarationCoverage;
import legacymodernizer.parser.service.ParseProgressTracker;

/**
 * Spec 012 SC-007 rehearsal: a brand-new language registers through the public SPI alone.
 * This test defines a toy language module in test scope — zero production files are added or
 * modified — and proves registration, detection, recovery defaults (slice syntax, repair
 * profile), and Parser-owned localization all work through interface defaults.
 */
class NewLanguageOnboardingRehearsalTest {

    /** Minimal new language: extension .toy, one FILE unit, trivial "AST". */
    private static final class ToyLanguageModule implements LanguageModule {
        @Override
        public RawParseResult parseFile(File file, ParseProgressTracker tracker) throws Exception {
            String source = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            String ast = "{\"type\":\"FILE\",\"name\":\"" + file.getName()
                    + "\",\"startLine\":1,\"endLine\":" + Math.max(1, source.lines().count())
                    + ",\"children\":[]}";
            return new RawParseResult("toy", "toy-grammar-1", "file", "0".repeat(64), ast,
                    List.of(), 0, DeclarationCoverage.unknown(), 0);
        }

        @Override
        public String languageId() {
            return "toy";
        }

        @Override
        public Set<String> parseExtensions() {
            return Set.of(".toy");
        }
    }

    @Test
    void newLanguageRegistersAndInheritsRecoveryDefaultsWithoutCoreChanges() {
        LanguageModule toy = new ToyLanguageModule();
        LanguageModuleRegistry registry = new LanguageModuleRegistry(List.of(toy));

        assertEquals(toy, registry.require("toy"));
        assertEquals(List.of(toy), registry.candidates(".toy"));
        assertTrue(registry.supportedExtensions().contains(".toy"));

        // Recovery SPI defaults come from the interface, not from Core edits.
        assertEquals(SliceSyntax.generic(), toy.sliceSyntax());
        assertEquals(RepairProfile.empty(), toy.repairProfile());
        assertEquals(Set.of("common-safe", "toy"), toy.recoveryRuleSets());
        assertEquals(1, toy.locateUnits("hello\nworld\n").size());

        // Parser-owned localization works for the new language with zero language code.
        ErrorSpanLocator locator = new ErrorSpanLocator();
        String source = "alpha beta;\ngamma delta epsilon;\nzeta;\n";
        ParseDiagnostic diagnostic = new ParseDiagnostic(DiagnosticPhase.PARSER, "ERROR",
                "ANTLR_PARSER_SYNTAX", "mismatched input 'delta'", 2, 6, "delta", "{';'}",
                List.of("statement"), "");
        int anchor = locator.anchorOffset(source, 1, diagnostic.line(), diagnostic.column());
        ContextSlice slice = locator.slice(source, toy.sliceSyntax(), anchor, SliceLevel.L1);
        assertNotNull(slice.sliceSha256());
        assertTrue(slice.text().contains("gamma delta epsilon;"));
        assertTrue(slice.length() <= SliceLevel.L1.maxChars());
    }
}
