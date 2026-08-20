package legacymodernizer.parser.contract;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import legacymodernizer.parser.intake.ParserWorkspace;
import legacymodernizer.parser.intake.SourceIntakeClassifier;
import legacymodernizer.parser.service.ParseProgressTracker;
import legacymodernizer.parser.parsing.languages.c.CLanguageModule;
import legacymodernizer.parser.parsing.languages.java.JavaLanguageModule;
import legacymodernizer.parser.parsing.languages.oracle.OracleLanguageModule;
import legacymodernizer.parser.parsing.languages.postgresql.PostgreSqlLanguageModule;
import legacymodernizer.parser.parsing.languages.python.PythonLanguageModule;
import legacymodernizer.parser.parsing.languages.LanguageModule;

class AstJsonGoldenContractTest {

    private static final ObjectMapper JSON = new ObjectMapper();
    // spec 016: 구조 statement 의 expression 필드 추가 — analyzer step2(load_ast)가
    // 영향받는 계약 변경이며 golden 은 같은 변경에서 재생성했다 (parser 헌법 VI).
    private static final List<String> PROPERTY_ORDER = List.of(
            "type", "name", "signature", "modifiers", "annotations", "returnType",
            "parameters", "genericType", "extendsType", "implementsTypes", "variableType",
            "initValue", "target", "operator", "expression", "dataObjectEvidenceVersion",
            "dataObjectReferences",
            "qualifiedColumnReferences", "unqualifiedIdentifierReferences",
            "schema", "moduleName",
            "fileName", "filePath", "packageName", "comment", "startLine", "endLine",
            "children", "evidence");
    private static final Map<String, String> FIXTURES = new LinkedHashMap<>();

    static {
        FIXTURES.put("java", "Sample.java");
        FIXTURES.put("python", "sample.py");
        FIXTURES.put("c", "sample.c");
        FIXTURES.put("oracle", "sample.prc");
        FIXTURES.put("postgresql", "sample.sql");
    }

    @BeforeAll
    static void requireIsolatedDataRoot() {
        String configured = System.getProperty("parser.data.root", "").replace('\\', '/');
        assertTrue(configured.contains("/target/test-data"),
                "Golden tests must run only under target/test-data: " + configured);
    }

    @Test
    void exactParserOutputMatchesCurrentByteGoldenForEveryLanguage() throws Exception {
        ParserWorkspace storage = new ParserWorkspace(new SourceIntakeClassifier());
        Map<String, LanguageModule> modules = new LinkedHashMap<>();
        modules.put("java", new JavaLanguageModule(storage));
        modules.put("python", new PythonLanguageModule(storage));
        modules.put("c", new CLanguageModule(storage));
        modules.put("oracle", new OracleLanguageModule(storage));
        modules.put("postgresql", new PostgreSqlLanguageModule(storage));

        Path sourceRoot = storage.sourceDir();
        Path outputRoot = Path.of("target", "golden-contract-actual").toAbsolutePath();
        Files.createDirectories(sourceRoot);
        Files.createDirectories(outputRoot);

        for (Map.Entry<String, String> entry : FIXTURES.entrySet()) {
            String language = entry.getKey();
            String fileName = entry.getValue();
            Path source = sourceRoot.resolve(Path.of("golden", language, fileName));
            copyResource("/recovery/exact/" + language + "/" + fileName, source);

            LanguageModule module = modules.get(language);
            module.prepareProjectContext();
            Path actual = outputRoot.resolve(language + ".json");
            module.writeAstFile(source.toFile(), actual.toString(),
                    new ParseProgressTracker(null, fileName));

            Path repeated = outputRoot.resolve(language + "-repeated.json");
            module.writeAstFile(source.toFile(), repeated.toString(),
                    new ParseProgressTracker(null, fileName));
            assertArrayEquals(Files.readAllBytes(actual), Files.readAllBytes(repeated),
                    "Repeated AST JSON is not deterministic for " + language);
            assertFixedNodeShape(JSON.readTree(actual.toFile()), true);

            if (Boolean.getBoolean("parser.generateGoldens")) {
                Path generated = Path.of("target", "generated-goldens", language + ".json");
                Files.createDirectories(generated.getParent());
                Files.copy(actual, generated, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                continue;
            }

            try (InputStream expected = AstJsonGoldenContractTest.class.getResourceAsStream(
                    "/recovery/golden/" + language + ".json")) {
                assertTrue(expected != null, "Missing golden for " + language);
                assertArrayEquals(withoutResourceTerminator(expected.readAllBytes()), Files.readAllBytes(actual),
                        "AST JSON byte contract changed for " + language);
            }
        }
    }

    private static void copyResource(String resource, Path destination) throws IOException {
        Files.createDirectories(destination.getParent());
        try (InputStream input = AstJsonGoldenContractTest.class.getResourceAsStream(resource)) {
            if (input == null) {
                throw new IOException("Missing test resource: " + resource);
            }
            Files.copy(input, destination, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static byte[] withoutResourceTerminator(byte[] bytes) {
        int length = bytes.length;
        if (length > 0 && bytes[length - 1] == '\n') {
            length--;
            if (length > 0 && bytes[length - 1] == '\r') {
                length--;
            }
        }
        return java.util.Arrays.copyOf(bytes, length);
    }

    private static void assertFixedNodeShape(JsonNode node, boolean fileRoot) {
        assertTrue(node.isObject(), "Every AST node must be an object");
        assertTrue(node.hasNonNull("type") && node.get("type").isTextual(), "type must be text");
        assertTrue(node.has("startLine") && node.get("startLine").isIntegralNumber(),
                "startLine must be an integer");
        assertTrue(node.has("endLine") && node.get("endLine").isIntegralNumber(),
                "endLine must be an integer");
        assertTrue(node.has("children") && node.get("children").isArray(),
                "children must always be an array");

        List<String> fields = new ArrayList<>();
        node.fieldNames().forEachRemaining(fields::add);
        int previous = -1;
        for (String field : fields) {
            assertTrue(PROPERTY_ORDER.contains(field), "Unknown Node JSON property: " + field);
            assertTrue(fileRoot || !"evidence".equals(field),
                    "Only the FILE root may own an evidence envelope");
            int current = PROPERTY_ORDER.indexOf(field);
            assertTrue(current > previous, "Node JSON property order changed at: " + field);
            assertTrue(!node.get(field).isNull(), "Null properties must be omitted: " + field);
            previous = current;
        }
        node.get("children").forEach(child -> assertFixedNodeShape(child, false));
    }
}
