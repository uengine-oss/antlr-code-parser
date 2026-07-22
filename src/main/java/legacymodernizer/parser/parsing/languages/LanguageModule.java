package legacymodernizer.parser.parsing.languages;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.Optional;

import legacymodernizer.parser.recovery.boundaries.SourceUnit;
import legacymodernizer.parser.recovery.boundaries.UnitKind;
import legacymodernizer.parser.recovery.boundaries.UnitParseRequest;
import legacymodernizer.parser.recovery.boundaries.UnitParseContext;
import legacymodernizer.parser.parsing.RawParseResult;
import legacymodernizer.parser.recovery.workingcopy.Hashes;
import legacymodernizer.parser.service.ParseProgressTracker;

public interface LanguageModule {

    default void prepareProjectContext() {
    }

    RawParseResult parseFile(File file, ParseProgressTracker tracker) throws Exception;

    default void writeAstFile(File file, String outputPath,
                              ParseProgressTracker tracker) throws Exception {
        RawParseResult parseAttempt = parseFile(file, tracker);
        Files.writeString(Path.of(outputPath), parseAttempt.astJson(), StandardCharsets.UTF_8);
    }

    String languageId();

    Set<String> parseExtensions();

    default String languageFamily() {
        return "framework";
    }

    default int contentAffinity(String source) {
        return 0;
    }

    default int sharedExtensionPriority() {
        return 0;
    }

    default Set<String> recoveryRuleSets() {
        return Set.of("common-safe", languageId());
    }

    default legacymodernizer.parser.recovery.localization.SliceSyntax sliceSyntax() {
        return legacymodernizer.parser.recovery.localization.SliceSyntax.generic();
    }

    default legacymodernizer.parser.recovery.candidates.RepairProfile repairProfile() {
        return legacymodernizer.parser.recovery.candidates.RepairProfile.empty();
    }

    default List<SourceUnit> locateUnits(String source) {
        String text = source == null ? "" : source;
        int lineCount = text.isEmpty() ? 0 : (int) text.lines().count();
        String id = Hashes.sha256((languageId() + "\n" + text)
                .getBytes(StandardCharsets.UTF_8));
        return List.of(new SourceUnit(id, UnitKind.FILE, null, null,
                0, text.length(), text.isEmpty() ? 0 : 1, lineCount, 0, "CONSERVATIVE"));
    }

    default List<SourceUnit> locateRecoveryUnits(String source, RawParseResult failedParse) {
        return locateUnits(source);
    }

    default boolean supportsUnitParsing() {
        return false;
    }

    default RawParseResult parseUnit(UnitParseRequest request, ParseProgressTracker tracker) throws Exception {
        throw new UnsupportedOperationException("Minimal-unit parsing is not implemented for "
                + languageId());
    }

    default Optional<UnitParseContext> reconstructUnitContext(String fileSource, SourceUnit unit) {
        return Optional.empty();
    }

    default Optional<UnitParseContext> reconstructUnitContext(
            String fileSource, SourceUnit unit, String unitSource) {
        return reconstructUnitContext(fileSource, unit);
    }

    default List<UnitParseContext> reconstructUnitContexts(
            String fileSource, SourceUnit unit, String unitSource) {
        return reconstructUnitContext(fileSource, unit, unitSource)
                .map(List::of).orElseGet(List::of);
    }
}
