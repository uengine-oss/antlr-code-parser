package legacymodernizer.parser.recovery;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import legacymodernizer.parser.intake.ParserWorkspace;
import legacymodernizer.parser.intake.SourceIntakeClassifier;
import legacymodernizer.parser.parsing.RawParseResult;
import legacymodernizer.parser.parsing.languages.LanguageModule;
import legacymodernizer.parser.parsing.languages.c.CLanguageModule;
import legacymodernizer.parser.parsing.languages.java.JavaLanguageModule;
import legacymodernizer.parser.parsing.languages.oracle.OracleLanguageModule;
import legacymodernizer.parser.parsing.languages.python.PythonLanguageModule;
import legacymodernizer.parser.recovery.evidence.RecoveryOutcome;
import legacymodernizer.parser.recovery.quality.ParseQualityGate;
import legacymodernizer.parser.recovery.quality.QualityDecision;
import legacymodernizer.parser.recovery.repair.RepairAgent;
import legacymodernizer.parser.recovery.repair.StructuredRepairAgent;
import legacymodernizer.parser.recovery.rules.RecoveryRuleRegistry;
import legacymodernizer.parser.service.ParseProgressTracker;

/**
 * Mutation benchmark (user directive 2026-07-22): take real files that parse EXACT, break
 * their grammar on purpose, and grade repairs against the untouched original AST — the
 * original is the ground truth, so semantic preservation and false accepts are measurable.
 *
 * Grades per mutation:
 *   FIXED_EXACT        repaired AST byte-equals the original AST (perfect repair)
 *   FIXED_DIFFERENT    pipeline adopted an AST that differs from the original (false accept!)
 *   REVIEW_REQUIRED    fail-closed, left for a human (safe)
 *   UNRESOLVED         no AST produced (safe but uncovered)
 *   NOT_BROKEN         the mutation did not actually break the grammar (excluded from rates)
 *
 * Enabled with -Dparser.full.corpus=<dir>; add the live agent properties to grade the Agent
 * tier on the cases the deterministic engine leaves behind.
 */
class MutationRepairBenchmarkTest {

    private static final int FILES_PER_LANGUAGE =
            Integer.getInteger("parser.mutation.files.per.language", 4);
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeAll
    static void requireIsolatedDataRoot() {
        assertTrue(System.getProperty("parser.data.root", "").replace('\\', '/')
                .contains("/target/test-data"));
    }

    private record Mutation(String name, String before, String after) {
        /** Applies mid-file when possible; tolerates CRLF sources ("\n" patterns match "\r\n"). */
        String apply(String source) {
            for (String[] pair : variants()) {
                int index = source.indexOf(pair[0], source.length() / 3);
                if (index < 0) index = source.indexOf(pair[0]);
                if (index >= 0) {
                    return source.substring(0, index) + pair[1]
                            + source.substring(index + pair[0].length());
                }
            }
            return null;
        }

        private List<String[]> variants() {
            if (!before.endsWith("\n")) {
                return List.<String[]>of(new String[]{before, after});
            }
            String beforeCrlf = before.substring(0, before.length() - 1) + "\r\n";
            String afterCrlf = after.endsWith("\n")
                    ? after.substring(0, after.length() - 1) + "\r\n" : after;
            return List.of(new String[]{beforeCrlf, afterCrlf}, new String[]{before, after});
        }
    }

    private static List<Mutation> mutations(String language) {
        // "unclose-*" and "garbage-*" are the heavy lexer-level breakages: an unterminated
        // string/comment swallows everything after it, garbage characters break the lexer.
        return switch (language) {
            case "java", "c" -> List.of(
                    new Mutation("delete-semicolon", ";\n", "\n"),
                    new Mutation("delete-closing-paren", ");\n", ";\n"),
                    new Mutation("duplicate-if-keyword", "if (", "if if ("),
                    new Mutation("unclose-string", "\");\n", ";\n"),
                    new Mutation("unclose-block-comment", "*/", "  "),
                    new Mutation("garbage-line", "\n    ", "\n@#$%^&~`?? "));
            case "python" -> List.of(
                    new Mutation("delete-def-colon", ":\n", "\n"),
                    new Mutation("delete-closing-paren", ")\n", "\n"),
                    new Mutation("unclose-string", "\")\n", ")\n"),
                    new Mutation("garbage-line", "\n    ", "\n@#$%^&~`?? "));
            case "oracle" -> List.of(
                    new Mutation("delete-semicolon", ";\n", "\n"),
                    new Mutation("insert-alias-as", " FROM ", " FROM DUAL_T AS "),
                    new Mutation("duplicate-select-keyword", "SELECT ", "SELECT SELECT "),
                    new Mutation("unclose-string", "';\n", ";\n"),
                    new Mutation("garbage-line", "\n  ", "\n@#$%^&~`?? "));
            default -> List.of();
        };
    }

    @Test
    void deliberatelyBrokenRealFilesAreRepairedBackToTheOriginalAst() throws Exception {
        String configured = System.getProperty("parser.full.corpus");
        Assumptions.assumeTrue(configured != null && !configured.isBlank());
        Path corpus = Path.of(configured).toAbsolutePath().normalize();
        Assumptions.assumeTrue(Files.isDirectory(corpus));

        ParserWorkspace storage = new ParserWorkspace(new SourceIntakeClassifier());
        Map<String, LanguageModule> modules = new LinkedHashMap<>();
        modules.put("java", new JavaLanguageModule(storage));
        modules.put("python", new PythonLanguageModule(storage));
        modules.put("c", new CLanguageModule(storage));
        modules.put("oracle", new OracleLanguageModule(storage));
        Map<String, String> extensions = Map.of(
                "java", ".java", "python", ".py", "c", ".c", "oracle", ".prc");

        ParseQualityGate gate = new ParseQualityGate();
        RepairAgent agent = new StructuredRepairAgent();
        LayeredRecoveryPipeline pipeline = new LayeredRecoveryPipeline(
                gate, new RecoveryRuleRegistry(List.of()), agent);
        ParseProgressTracker tracker = new ParseProgressTracker(null, "mutation");

        List<Map<String, Object>> rows = new ArrayList<>();
        Map<String, Integer> totals = new LinkedHashMap<>();
        for (Map.Entry<String, LanguageModule> entry : modules.entrySet()) {
            String language = entry.getKey();
            LanguageModule module = entry.getValue();
            List<Path> candidates = sampleFiles(corpus, extensions.get(language));
            int used = 0;
            for (Path sourcePath : candidates) {
                if (used >= FILES_PER_LANGUAGE) break;
                String original = Files.readString(sourcePath, StandardCharsets.UTF_8);
                Path cleanCopy = stage(storage, language, sourcePath.getFileName() + ".clean",
                        original);
                RawParseResult cleanParse = module.parseFile(cleanCopy.toFile(), tracker);
                if (!gate.evaluateFirstPass(cleanParse).accepted()) continue; // need EXACT truth
                used++;
                for (Mutation mutation : mutations(language)) {
                    String mutated = mutation.apply(original);
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("language", language);
                    row.put("file", corpus.relativize(sourcePath).toString().replace('\\', '/'));
                    row.put("mutation", mutation.name());
                    if (mutated == null) {
                        row.put("grade", "MUTATION_NOT_APPLICABLE");
                        rows.add(row);
                        continue;
                    }
                    Path broken = stage(storage, language,
                            sourcePath.getFileName() + "." + mutation.name(), mutated);
                    RawParseResult first = module.parseFile(broken.toFile(), tracker);
                    QualityDecision decision = gate.evaluateFirstPass(first);
                    String grade;
                    if (decision.accepted()) {
                        grade = "NOT_BROKEN";
                    } else {
                        RecoveryOutcome outcome = pipeline.recover(module, broken,
                                storage.sourceDir(), first, decision, tracker);
                        String status = outcome.decision().status().name();
                        row.put("status", status);
                        boolean fullyAdopted = status.startsWith("RECOVERED")
                                || "EXACT".equals(status);
                        if (outcome.astJson() == null) {
                            grade = status;
                        } else if (!fullyAdopted) {
                            // Honest partial salvage: broken unit stays flagged, rest emitted.
                            grade = "PARTIAL_SALVAGE";
                        } else {
                            grade = switch (compareAst(cleanParse.astJson(), outcome.astJson())) {
                                case IDENTICAL -> "FIXED_EXACT";
                                case COMMENT_ONLY -> "FIXED_COMMENT_LOSS";
                                case COORD_ONLY -> "FIXED_COORD_DRIFT";
                                case DIFFERENT -> "FIXED_DIFFERENT";
                            };
                            if (!"FIXED_EXACT".equals(grade)) {
                                Path diffDir = Path.of("target", "corpus-reports", "mutation-diffs");
                                Files.createDirectories(diffDir);
                                String stem = sourcePath.getFileName() + "." + mutation.name();
                                Files.writeString(diffDir.resolve(stem + ".clean.json"),
                                        cleanParse.astJson(), StandardCharsets.UTF_8);
                                Files.writeString(diffDir.resolve(stem + ".repaired.json"),
                                        outcome.astJson(), StandardCharsets.UTF_8);
                            }
                        }
                    }
                    row.put("grade", grade);
                    totals.merge(grade, 1, Integer::sum);
                    rows.add(row);
                }
            }
        }

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("agentEnabled", agent.enabled());
        report.put("filesPerLanguage", FILES_PER_LANGUAGE);
        report.put("totals", totals);
        report.put("rows", rows);
        String name = System.getProperty("parser.mutation.report.name",
                "mutation-repair-benchmark.json");
        Path output = Path.of("target", "corpus-reports", name);
        Files.createDirectories(output.getParent());
        Files.writeString(output, mapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(report), StandardCharsets.UTF_8);
        System.out.println("MUTATION totals=" + totals + " -> " + output);
        assertTrue(totals.getOrDefault("FIXED_DIFFERENT", 0) == 0,
                "false accept detected: a repair was adopted that differs from the original AST");
    }

    private enum AstMatch { IDENTICAL, COMMENT_ONLY, COORD_ONLY, DIFFERENT }

    /**
     * Compares ASTs ignoring the file-name fields (clean vs mutated copies differ by name).
     * COMMENT_ONLY means all code structure matches and only comment metadata was lost —
     * a recovery-fidelity gap (unit boundaries exclude leading comments), not a wrong repair.
     */
    private AstMatch compareAst(String cleanAstJson, String repairedAstJson) throws Exception {
        var clean = (com.fasterxml.jackson.databind.node.ObjectNode) mapper.readTree(cleanAstJson);
        var repaired = (com.fasterxml.jackson.databind.node.ObjectNode) mapper.readTree(repairedAstJson);
        Stream.of(clean, repaired).forEach(node -> {
            node.remove("name");
            node.remove("fileName");
            node.remove("filePath");
        });
        if (clean.equals(repaired)) return AstMatch.IDENTICAL;
        stripComments(clean);
        stripComments(repaired);
        if (clean.equals(repaired)) return AstMatch.COMMENT_ONLY;
        stripCoordinates(clean);
        stripCoordinates(repaired);
        // Equivalent repair at a shifted position (e.g. ')' restored one line early).
        return clean.equals(repaired) ? AstMatch.COORD_ONLY : AstMatch.DIFFERENT;
    }

    private static void stripCoordinates(com.fasterxml.jackson.databind.JsonNode node) {
        if (node instanceof com.fasterxml.jackson.databind.node.ObjectNode object) {
            object.remove("startLine");
            object.remove("endLine");
        }
        node.path("children").forEach(MutationRepairBenchmarkTest::stripCoordinates);
    }

    private static void stripComments(com.fasterxml.jackson.databind.JsonNode node) {
        if (node instanceof com.fasterxml.jackson.databind.node.ObjectNode object) {
            object.remove("comment");
        }
        node.path("children").forEach(MutationRepairBenchmarkTest::stripComments);
    }

    private static Path stage(ParserWorkspace storage, String language, String fileName,
                              String content) throws Exception {
        Path path = storage.sourceDir().resolve("mutation/" + language + "/" + fileName);
        Files.createDirectories(path.getParent());
        Files.writeString(path, content, StandardCharsets.UTF_8);
        return path;
    }

    private static List<Path> sampleFiles(Path corpus, String extension) throws Exception {
        try (Stream<Path> walk = Files.walk(corpus)) {
            return walk.filter(Files::isRegularFile)
                    .filter(path -> path.toString().toLowerCase().endsWith(extension))
                    .filter(path -> {
                        try {
                            long size = Files.size(path);
                            return size > 400 && size < 60_000;
                        } catch (Exception error) {
                            return false;
                        }
                    })
                    .sorted()
                    .limit(FILES_PER_LANGUAGE * 3L)
                    .toList();
        }
    }
}
