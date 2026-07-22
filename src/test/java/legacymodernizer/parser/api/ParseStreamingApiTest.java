package legacymodernizer.parser.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import legacymodernizer.parser.intake.ParserWorkspace;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ParseStreamingApiTest {

    @Autowired private TestRestTemplate rest;
    @Autowired private ParserWorkspace workspace;
    @TempDir Path inputRoot;

    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeAll
    static void requireIsolatedDataRoot() {
        assertTrue(System.getProperty("parser.data.root", "").replace('\\', '/')
                .contains("/target/test-data"));
    }

    @Test
    void streamsFriendlyOrderedNdjsonWithCompatibilityFieldsAndNoBufferingHeaders() throws Exception {
        Files.writeString(inputRoot.resolve("cart.c"),
                "int cart_total(int price, int qty) { return price * qty; }\n",
                StandardCharsets.UTF_8);
        workspace.intakeFromPath(inputRoot);

        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        var response = rest.exchange("/antlr/parsing", HttpMethod.POST,
                new HttpEntity<>(Map.of(), requestHeaders), String.class);

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getHeaders().getContentType().toString()
                .startsWith("application/x-ndjson"));
        assertTrue(response.getHeaders().getCacheControl().contains("no-cache"));
        assertEquals("no", response.getHeaders().getFirst("X-Accel-Buffering"));

        List<JsonNode> events = new ArrayList<>();
        for (String line : response.getBody().split("\\R")) {
            if (!line.isBlank()) events.add(mapper.readTree(line));
        }
        assertFalse(events.isEmpty());
        assertEquals("run_started", events.get(0).path("event").asText());
        assertTrue(events.stream().allMatch(event -> event.hasNonNull("type")));
        assertTrue(events.stream().filter(event -> "message".equals(event.path("type").asText()))
                .allMatch(event -> event.hasNonNull("content")));
        assertTrue(events.stream().anyMatch(event -> "file_result".equals(event.path("event").asText())
                && "EXACT".equals(event.path("quality").asText())
                && event.path("percent").asInt() == 100));
        assertTrue(events.stream().anyMatch(event -> "run_completed".equals(event.path("event").asText())
                && event.path("counts").path("exact").asInt() == 1));
        assertEquals(1, events.stream()
                .filter(event -> "complete".equals(event.path("type").asText())).count());
        assertEquals("complete", events.get(events.size() - 1).path("type").asText());
        assertFalse(events.stream().anyMatch(event -> "repair_promotion_candidates"
                .equals(event.path("event").asText())));
    }
}

