package legacymodernizer.parser.parsing.languages;

import java.util.List;

public record LanguageCatalog(
        String schemaVersion,
        String catalogVersion,
        String parserBuild,
        String antlrRuntimeVersion,
        List<LanguageDefinition> languages,
        String catalogSha256) {
}
