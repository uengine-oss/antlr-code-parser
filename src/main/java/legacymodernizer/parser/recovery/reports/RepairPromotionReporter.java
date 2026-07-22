package legacymodernizer.parser.recovery.reports;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeSet;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import legacymodernizer.parser.intake.ParserWorkspace;
import legacymodernizer.parser.recovery.workingcopy.Hashes;

@Component
public final class RepairPromotionReporter {

    public static final int MINIMUM_OCCURRENCES = 2;
    private static final String REPORT_NAME = "promotion-candidates.json";

    private final ParserWorkspace parserWorkspace;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
            .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);

    public RepairPromotionReporter(ParserWorkspace parserWorkspace) {
        this.parserWorkspace = parserWorkspace;
    }

    /** Writes only a non-empty review report; exact/no-candidate runs leave no repair residue. */
    public Optional<Path> writeIfCandidates() throws IOException {
        List<JsonNode> audits = new ArrayList<>();
        Path repairRoot = parserWorkspace.repairsDir();
        if (Files.isDirectory(repairRoot)) {
            try (var files = Files.walk(repairRoot)) {
                for (Path path : files.filter(Files::isRegularFile)
                        .filter(value -> value.toString().endsWith(".repair.json"))
                        .sorted().toList()) {
                    audits.add(objectMapper.readTree(path.toFile()));
                }
            }
        }
        RepairPromotionReport report = buildReport(audits);
        Path output = repairRoot.resolve("review").resolve(REPORT_NAME);
        if (report.candidates().isEmpty()) {
            Files.deleteIfExists(output);
            return Optional.empty();
        }
        Files.createDirectories(output.getParent());
        Files.writeString(output, objectMapper.writeValueAsString(report), StandardCharsets.UTF_8);
        return Optional.of(output);
    }

    public RepairPromotionReport buildReport(List<JsonNode> audits) {
        Map<String, MutableCandidate> grouped = new LinkedHashMap<>();
        List<JsonNode> orderedAudits = new ArrayList<>(audits == null ? List.of() : audits);
        orderedAudits.sort(Comparator.comparing(audit -> audit.path("sourcePath").asText()));
        for (JsonNode audit : orderedAudits) {
            String language = audit.path("language").asText("unknown");
            String grammar = audit.path("grammarRevision").asText("unknown");
            String sourcePath = audit.path("sourcePath").asText();
            for (JsonNode unit : audit.path("units")) {
                if (!unit.path("accepted").asBoolean()) continue;
                JsonNode successful = successfulRepairAttempt(unit.path("attempts"));
                if (successful == null) continue;
                String stage = successful.path("stage").asText();
                String ruleId = successful.path("ruleId").asText(stage);
                String unitKind = unit.path("unit").path("kind").asText("UNKNOWN");
                List<String> diagnosticCodes = diagnosticCodes(unit.path("attempts"), successful);
                String editShape = normalizedEditShape(successful.path("diff").asText());
                String signatureText = String.join("\n", language, grammar, unitKind, stage,
                        ruleId, String.join(",", diagnosticCodes), editShape);
                String signature = Hashes.sha256(signatureText.getBytes(StandardCharsets.UTF_8));
                MutableCandidate candidate = grouped.computeIfAbsent(signature,
                        ignored -> new MutableCandidate(signature, language, grammar, unitKind,
                                stage, ruleId, diagnosticCodes));
                candidate.sourcePaths.add(sourcePath);
                candidate.unitIds.add(unit.path("unit").path("unitId").asText());
                candidate.occurrences++;
            }
        }

        List<RepairPromotionReport.Candidate> candidates = grouped.values().stream()
                .filter(candidate -> candidate.occurrences >= MINIMUM_OCCURRENCES)
                .sorted(Comparator.comparing(candidate -> candidate.signature))
                .map(MutableCandidate::freeze)
                .toList();
        return new RepairPromotionReport("1.0.0", MINIMUM_OCCURRENCES, candidates);
    }

    private static JsonNode successfulRepairAttempt(JsonNode attempts) {
        if (!attempts.isArray()) return null;
        JsonNode successful = null;
        for (JsonNode attempt : attempts) {
            String stage = attempt.path("stage").asText();
            if (("SAFE_RULE".equals(stage) || "REPAIR_AGENT".equals(stage))
                    && !attempt.path("diff").asText().isBlank()
                    && attempt.path("qualityReasons").isArray()
                    && attempt.path("qualityReasons").isEmpty()) {
                successful = attempt;
            }
        }
        return successful;
    }

    private static List<String> diagnosticCodes(JsonNode attempts, JsonNode successful) {
        TreeSet<String> codes = new TreeSet<>();
        for (JsonNode attempt : attempts) {
            if (attempt == successful) break;
            for (JsonNode diagnostic : attempt.path("diagnostics")) {
                String code = diagnostic.path("code").asText();
                if (!code.isBlank()) codes.add(code);
            }
        }
        return List.copyOf(codes);
    }

    private static String normalizedEditShape(String diff) {
        return diff.lines()
                .filter(line -> !line.startsWith("--- ") && !line.startsWith("+++ "))
                .map(line -> line.replaceAll("@@ offset [0-9]+,[0-9]+", "@@ offset #,#"))
                .reduce((left, right) -> left + "\n" + right)
                .orElse("");
    }

    private static final class MutableCandidate {
        private final String signature;
        private final String language;
        private final String grammar;
        private final String unitKind;
        private final String stage;
        private final String ruleId;
        private final List<String> diagnosticCodes;
        private final TreeSet<String> sourcePaths = new TreeSet<>();
        private final TreeSet<String> unitIds = new TreeSet<>();
        private int occurrences;

        private MutableCandidate(String signature, String language, String grammar,
                                 String unitKind, String stage, String ruleId,
                                 List<String> diagnosticCodes) {
            this.signature = signature;
            this.language = language;
            this.grammar = grammar;
            this.unitKind = unitKind;
            this.stage = stage;
            this.ruleId = ruleId;
            this.diagnosticCodes = diagnosticCodes;
        }

        private RepairPromotionReport.Candidate freeze() {
            List<String> reviewOptions = "REPAIR_AGENT".equals(stage)
                    ? List.of("RECOVERY_RULE", "PINNED_GRAMMAR_PATCH")
                    : List.of("RULE_REGRESSION_EXPANSION", "PINNED_GRAMMAR_PATCH");
            var fixture = new RepairPromotionReport.RegressionFixtureTemplate(
                    language, grammar, unitKind, diagnosticCodes, true, true, true);
            return new RepairPromotionReport.Candidate(signature, language, grammar,
                    unitKind, stage, ruleId, diagnosticCodes, occurrences,
                    List.copyOf(sourcePaths), List.copyOf(unitIds), reviewOptions, fixture);
        }
    }
}
