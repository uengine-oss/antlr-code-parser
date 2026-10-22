package legacymodernizer.parser.parsing.languages;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.antlr.v4.runtime.RuntimeMetaData;
import org.springframework.stereotype.Service;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import legacymodernizer.parser.recovery.workingcopy.CanonicalJson;
import legacymodernizer.parser.recovery.workingcopy.Hashes;

@Service
public final class LanguageCatalogValidator {

    public static final String RESOURCE = "/languages/language-catalog.json";
    private static final Pattern SEMANTIC_VERSION = Pattern.compile("[0-9]+\\.[0-9]+\\.[0-9]+");
    private static final Pattern LANGUAGE_ID = Pattern.compile("[a-z][a-z0-9-]*");
    private static final Pattern SHA256 = Pattern.compile("[0-9a-f]{64}");

    private final ObjectMapper objectMapper = JsonMapper.builder()
            .enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
            .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)
            .build();
    private LanguageCatalog cachedCatalog;

    public synchronized LanguageCatalog load() {
        if (cachedCatalog == null) cachedCatalog = loadResource();
        return cachedCatalog;
    }

    public LanguageCatalog validateModules(List<LanguageModule> modules) {
        return validateModules(load(), modules);
    }

    LanguageCatalog validateModules(LanguageCatalog catalog, List<LanguageModule> modules) {
        validateCatalog(catalog);
        if (modules == null) throw new IllegalStateException("Language modules are missing");

        Map<String, LanguageModule> discoveredById = new HashMap<>();
        for (LanguageModule module : modules) {
            if (module == null) throw new IllegalStateException("Null language module");
            String moduleId = normalizedId(module.languageId());
            if (discoveredById.put(moduleId, module) != null) {
                throw new IllegalStateException("Duplicate language module: " + moduleId);
            }
        }

        Set<String> declaredIds = new TreeSet<>(catalog.languages().stream()
                .map(LanguageDefinition::id).toList());
        Set<String> discoveredIds = new TreeSet<>(discoveredById.keySet());
        if (!discoveredIds.equals(declaredIds)) {
            throw new IllegalStateException("Language module/catalog mismatch: modules="
                    + discoveredIds + ", catalog=" + declaredIds);
        }

        for (LanguageDefinition definition : catalog.languages()) {
            LanguageModule module = discoveredById.get(definition.id());
            Set<String> moduleExtensions = new TreeSet<>(module.parseExtensions().stream()
                    .map(extension -> extension.toLowerCase(Locale.ROOT)).toList());
            if (!moduleExtensions.equals(new TreeSet<>(definition.parseExtensions()))) {
                throw new IllegalStateException("Parse extension drift for " + definition.id());
            }
            if (!definition.family().equals(module.languageFamily())) {
                throw new IllegalStateException("Language family drift for " + definition.id());
            }
            if (!new TreeSet<>(module.recoveryRuleSets())
                    .equals(new TreeSet<>(definition.recoveryRuleSets()))) {
                throw new IllegalStateException("Recovery rule-set drift for " + definition.id());
            }
        }
        return catalog;
    }

    private LanguageCatalog loadResource() {
        try (InputStream input = LanguageCatalogValidator.class.getResourceAsStream(RESOURCE)) {
            if (input == null) {
                throw new IllegalStateException("Language catalog resource missing: " + RESOURCE);
            }
            LanguageCatalog resourceCatalog = objectMapper.readValue(input, LanguageCatalog.class);
            validateCatalog(resourceCatalog);
            if (resourceCatalog.catalogSha256() == null
                    || !SHA256.matcher(resourceCatalog.catalogSha256()).matches()) {
                throw new IllegalStateException("Language catalog checksum is missing or invalid");
            }
            String computedChecksum = checksum(resourceCatalog);
            if (!resourceCatalog.catalogSha256().equals(computedChecksum)) {
                throw new IllegalStateException("Language catalog checksum mismatch: declared="
                        + resourceCatalog.catalogSha256() + ", computed=" + computedChecksum);
            }
            return new LanguageCatalog(resourceCatalog.schemaVersion(),
                    resourceCatalog.catalogVersion(), resourceCatalog.parserBuild(),
                    resourceCatalog.antlrRuntimeVersion(),
                    List.copyOf(resourceCatalog.languages()), computedChecksum);
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot load language catalog", exception);
        }
    }

    private void validateCatalog(LanguageCatalog catalog) {
        if (catalog == null || !isSupportedSchema(catalog.schemaVersion())) {
            throw new IllegalStateException("Unsupported language catalog schema: "
                    + (catalog == null ? null : catalog.schemaVersion()));
        }
        if (!isSemanticVersion(catalog.catalogVersion())) {
            throw new IllegalStateException("Invalid language catalog version: "
                    + catalog.catalogVersion());
        }
        if (isBlank(catalog.parserBuild())) {
            throw new IllegalStateException("Parser build is missing from language catalog");
        }
        if (!RuntimeMetaData.VERSION.equals(catalog.antlrRuntimeVersion())) {
            throw new IllegalStateException("ANTLR runtime/catalog mismatch: runtime="
                    + RuntimeMetaData.VERSION + ", catalog=" + catalog.antlrRuntimeVersion());
        }
        if (catalog.languages() == null || catalog.languages().isEmpty()) {
            throw new IllegalStateException("Language catalog has no languages");
        }

        Set<String> idsAndAliases = new HashSet<>();
        Map<String, List<String>> languageIdsByExtension = new HashMap<>();
        Map<String, Boolean> sharedByLanguage = new HashMap<>();
        for (LanguageDefinition definition : catalog.languages()) {
            validateDefinition(definition, idsAndAliases);
            sharedByLanguage.put(definition.id(), definition.sharedExtension());
            for (String extension : definition.parseExtensions()) {
                languageIdsByExtension.computeIfAbsent(extension, ignored -> new java.util.ArrayList<>())
                        .add(definition.id());
            }
        }
        languageIdsByExtension.forEach((extension, languageIds) -> {
            if (languageIds.size() > 1 && languageIds.stream()
                    .anyMatch(languageId -> !sharedByLanguage.get(languageId))) {
                throw new IllegalStateException("Shared extension is not declared by every module: "
                        + extension + " -> " + languageIds);
            }
        });
    }

    private void validateDefinition(LanguageDefinition definition, Set<String> idsAndAliases) {
        if (definition == null || !validLanguageId(definition.id())
                || !idsAndAliases.add(definition.id())) {
            throw new IllegalStateException("Blank, invalid, or duplicate language id: "
                    + (definition == null ? null : definition.id()));
        }
        if (definition.aliases() == null) {
            throw new IllegalStateException("Aliases missing: " + definition.id());
        }
        for (String alias : definition.aliases()) {
            if (!validLanguageId(alias) || !idsAndAliases.add(alias)) {
                throw new IllegalStateException("Invalid or duplicate language alias: " + alias);
            }
        }
        if (isBlank(definition.family())) {
            throw new IllegalStateException("Language family missing: " + definition.id());
        }
        validateExtensions(definition.id(), definition.parseExtensions());
        requireNonEmpty(definition.id(), "entry rules", definition.entryRules());
        if (!definition.entryRules().containsKey("FILE")) {
            throw new IllegalStateException("FILE entry rule missing: " + definition.id());
        }
        requireNonEmpty(definition.id(), "unit kinds", definition.unitKinds());
        for (String unitKind : definition.unitKinds()) {
            if (!definition.entryRules().containsKey(unitKind)) {
                throw new IllegalStateException("Entry rule missing for " + definition.id()
                        + " unit " + unitKind);
            }
        }
        requireNonEmpty(definition.id(), "emitted node types", definition.emittedNodeTypes());
        if (definition.routineNodeTypes() == null
                || !new HashSet<>(definition.emittedNodeTypes())
                    .containsAll(definition.routineNodeTypes())) {
            throw new IllegalStateException("Routine node types are not emitted by "
                    + definition.id());
        }
        requireNonEmpty(definition.id(), "recovery rule sets", definition.recoveryRuleSets());
        validateGrammar(definition);
    }

    private void validateGrammar(LanguageDefinition definition) {
        LanguageDefinition.GrammarDescriptor grammar = definition.grammar();
        if (grammar == null || isBlank(grammar.provenance())
                || grammar.files() == null || grammar.files().isEmpty()) {
            throw new IllegalStateException("Grammar provenance missing: " + definition.id());
        }
        Set<String> grammarPaths = new HashSet<>();
        for (LanguageDefinition.GrammarFile grammarFile : grammar.files()) {
            if (grammarFile == null || isBlank(grammarFile.path())
                    || !grammarPaths.add(grammarFile.path())
                    || grammarFile.sha256() == null
                    || !SHA256.matcher(grammarFile.sha256()).matches()) {
                throw new IllegalStateException("Invalid grammar file for " + definition.id());
            }
        }
    }

    private void validateExtensions(String languageId, List<String> extensions) {
        if (extensions == null || extensions.isEmpty()) {
            throw new IllegalStateException("Parse extensions missing: " + languageId);
        }
        Set<String> unique = new HashSet<>();
        for (String extension : extensions) {
            if (extension == null || !extension.matches("\\.[a-z0-9][a-z0-9+_-]*")
                    || !unique.add(extension)) {
                throw new IllegalStateException("Invalid or duplicate extension for "
                        + languageId + ": " + extension);
            }
        }
    }

    /**
     * 카탈로그의 지문.
     *
     * ⚠ **키 순서를 우리가 정한다** (2026-10-22). 전에는 매퍼의
     * `SORT_PROPERTIES_ALPHABETICALLY` 에 기대 정렬된 JSON 이 나오는 것을
     * 전제로 지문을 떴다. Boot 4(Jackson 3)로 옮기자 그 기능이 record 에
     * 적용되지 않아 **선언 순서**로 나왔고, 내용은 한 글자도 안 바뀌었는데
     * 지문만 달라져 `checksum mismatch` 로 파싱이 통째로 멈췄다(실측).
     *
     * 지문이 **직렬화기의 기본값에 매달려 있으면** 라이브러리가 움직일 때마다
     * 같은 일이 난다. 그래서 여기서 **직접 정렬한다** — 배열은 순서가 데이터
     * 자체이므로 그대로 두고, 객체 키만 재귀로 정렬한다.
     */
    private String checksum(LanguageCatalog catalog) {
        JsonNode tree = objectMapper.valueToTree(catalog);
        ((ObjectNode) tree).remove("catalogSha256");
        return CanonicalJson.sha256(objectMapper, tree);
    }


    private static boolean isSupportedSchema(String version) {
        return isSemanticVersion(version) && version.startsWith("1.");
    }

    private static boolean isSemanticVersion(String version) {
        return version != null && SEMANTIC_VERSION.matcher(version).matches();
    }

    private static boolean validLanguageId(String id) {
        return id != null && LANGUAGE_ID.matcher(id).matches();
    }

    private static String normalizedId(String id) {
        if (id == null || id.isBlank()) throw new IllegalStateException("Blank language module id");
        return id.toLowerCase(Locale.ROOT);
    }

    private static boolean isBlank(String text) {
        return text == null || text.isBlank();
    }

    private static void requireNonEmpty(String languageId, String field, Map<?, ?> values) {
        if (values == null || values.isEmpty()) {
            throw new IllegalStateException(field + " missing: " + languageId);
        }
    }

    private static void requireNonEmpty(String languageId, String field, List<?> values) {
        if (values == null || values.isEmpty()) {
            throw new IllegalStateException(field + " missing: " + languageId);
        }
    }
}
