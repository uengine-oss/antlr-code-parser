package legacymodernizer.parser.parsing.languages;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

@Component
public final class LanguageModuleRegistry {

    private final Map<String, LanguageModule> modulesById = new LinkedHashMap<>();
    private final Map<String, List<LanguageModule>> modulesByExtension = new LinkedHashMap<>();

    public LanguageModuleRegistry(List<LanguageModule> modules) {
        modules.stream().sorted(java.util.Comparator.comparing(LanguageModule::languageId))
                .forEach(module -> {
                    String id = module.languageId().toLowerCase(Locale.ROOT);
                    if (modulesById.put(id, module) != null) {
                        throw new IllegalStateException("Duplicate language module id: " + id);
                    }
                    for (String extension : module.parseExtensions()) {
                        modulesByExtension.computeIfAbsent(extension.toLowerCase(Locale.ROOT),
                                ignored -> new ArrayList<>()).add(module);
                    }
                });
    }

    public List<LanguageModule> modules() {
        return List.copyOf(modulesById.values());
    }

    public LanguageModule require(String id) {
        LanguageModule module = modulesById.get(id.toLowerCase(Locale.ROOT));
        if (module == null) throw new IllegalArgumentException("Unknown language module: " + id);
        return module;
    }

    public List<LanguageModule> candidates(String extension) {
        return List.copyOf(modulesByExtension.getOrDefault(
                extension.toLowerCase(Locale.ROOT), List.of()));
    }

    public Set<String> supportedExtensions() {
        return Set.copyOf(modulesByExtension.keySet());
    }
}
