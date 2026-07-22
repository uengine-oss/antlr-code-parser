package legacymodernizer.parser.parsing.languages;

import java.util.List;
import java.util.Map;

public record LanguageDefinition(
        String id,
        List<String> aliases,
        String family,
        List<String> parseExtensions,
        boolean sharedExtension,
        Map<String, String> entryRules,
        List<String> unitKinds,
        List<String> emittedNodeTypes,
        List<String> routineNodeTypes,
        GrammarDescriptor grammar,
        List<String> recoveryRuleSets) {

    public record GrammarDescriptor(
            String provenance,
            String upstreamRepository,
            String upstreamCommit,
            List<GrammarFile> files) {
    }

    public record GrammarFile(String path, String sha256) {
    }
}
