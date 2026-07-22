package legacymodernizer.parser.parsing;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import legacymodernizer.parser.parsing.languages.LanguageModule;
import legacymodernizer.parser.parsing.languages.LanguageModuleRegistry;
import lombok.extern.slf4j.Slf4j;

/** Detects a language module from extension and module-owned content affinity. */
@Slf4j
@Component
public class ParserSelection {

    // 모듈 색인(id 중복 검사·extension→modules 맵)의 단일 진실은 registry — 여기서는 소비만 한다.
    private final LanguageModuleRegistry registry;
    private final Map<String, String> familyByType = new LinkedHashMap<>();

    public ParserSelection(LanguageModuleRegistry registry) {
        this.registry = registry;
        for (LanguageModule module : registry.modules()) {
            familyByType.put(module.languageId().toLowerCase(Locale.ROOT), module.languageFamily());
        }
        log.info("Language modules initialized: extensions={} targets={}",
                registry.supportedExtensions(), familyByType.keySet());
    }

    public Set<String> supportedExtensions() {
        return registry.supportedExtensions();
    }

    public List<LanguageModule> modules() {
        return registry.modules();
    }

    public record DetectionResult(
            Map<Path, LanguageModule> modulesByFile,
            Set<String> detectedTargets,
            String sqlDialect,
            String primaryTarget,
            String analysisStrategy) {
    }

    public DetectionResult detect(Path sourceDir) {
        List<Path> files;
        try (var walk = Files.walk(sourceDir)) {
            files = walk.filter(Files::isRegularFile).sorted().toList();
        } catch (IOException exception) {
            throw new RuntimeException("Cannot scan source directory: " + sourceDir, exception);
        }

        Map<Path, LanguageModule> modulesByFile = new LinkedHashMap<>();
        Set<String> detectedTargets = new LinkedHashSet<>();
        Set<String> sqlDialects = new LinkedHashSet<>();
        Map<String, Integer> countByType = new LinkedHashMap<>();
        for (Path file : files) {
            String extension = extensionOf(file);
            List<LanguageModule> candidates = registry.candidates(extension);
            if (candidates.isEmpty()) continue;
            LanguageModule selected = candidates.size() == 1
                    ? candidates.get(0) : resolveAmbiguous(candidates, file);
            if (selected == null) continue;
            modulesByFile.put(file, selected);
            String type = selected.languageId();
            if (".sql".equals(extension)) sqlDialects.add(type);
            detectedTargets.add(type);
            countByType.merge(type, 1, Integer::sum);
        }

        String sqlDialect = sqlDialects.size() == 1 ? sqlDialects.iterator().next() : null;
        String analysisStrategy = deriveAnalysisStrategy(detectedTargets);
        String primaryTarget = derivePrimaryTarget(countByType, analysisStrategy);
        log.info("Language detection: targets={} primary={} strategy={} sqlDialect={} parsed={}/{}",
                detectedTargets, primaryTarget, analysisStrategy, sqlDialect,
                modulesByFile.size(), files.size());
        return new DetectionResult(
                Collections.unmodifiableMap(new LinkedHashMap<>(modulesByFile)),
                Collections.unmodifiableSet(new LinkedHashSet<>(detectedTargets)),
                sqlDialect, primaryTarget, analysisStrategy);
    }

    private String deriveAnalysisStrategy(Set<String> targets) {
        if (targets.isEmpty()) return "framework";
        boolean framework = targets.stream()
                .anyMatch(target -> !"dbms".equals(familyByType.get(target)));
        return framework ? "framework" : "dbms";
    }

    private String derivePrimaryTarget(Map<String, Integer> countByType, String analysisStrategy) {
        boolean dbms = "dbms".equals(analysisStrategy);
        return countByType.entrySet().stream()
                .filter(entry -> "dbms".equals(familyByType.get(entry.getKey())) == dbms)
                .sorted(Comparator.<Map.Entry<String, Integer>>comparingInt(Map.Entry::getValue)
                        .reversed().thenComparing(Map.Entry::getKey))
                .map(Map.Entry::getKey)
                .findFirst().orElse(null);
    }

    private LanguageModule resolveAmbiguous(List<LanguageModule> candidates, Path file) {
        LanguageModule selected = null;
        int selectedScore = Integer.MIN_VALUE;
        int selectedPriority = Integer.MIN_VALUE;
        String source = readText(file);
        for (LanguageModule candidate : candidates) {
            int score = candidate.contentAffinity(source);
            int priority = candidate.sharedExtensionPriority();
            if (selected == null || score > selectedScore
                    || (score == selectedScore && priority > selectedPriority)
                    || (score == selectedScore && priority == selectedPriority
                        && candidate.languageId().compareTo(selected.languageId()) < 0)) {
                selected = candidate;
                selectedScore = score;
                selectedPriority = priority;
            }
        }
        log.debug("Shared extension resolution: file={} candidates={} selected={} score={}",
                file,
                candidates.stream().map(LanguageModule::languageId).toList(),
                selected == null ? null : selected.languageId(), selectedScore);
        return selected;
    }

    private static String extensionOf(Path file) {
        String name = file.getFileName().toString().toLowerCase(Locale.ROOT);
        int dot = name.lastIndexOf('.');
        return dot < 0 ? "" : name.substring(dot);
    }

    private static String readText(Path file) {
        try {
            return SourceTextCodec.decode(Files.readAllBytes(file)).text();
        } catch (Exception readFailure) {
            log.warn("Cannot read shared-extension candidate: {}", file);
            return "";
        }
    }
}
