package legacymodernizer.parser.api.stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

class ParseStreamEventTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void keepsLegacyFieldsAndAddsStructuredProgress() throws Exception {
        Map<String, Integer> counts = new LinkedHashMap<>();
        counts.put("exact", 3);
        counts.put("recovered", 1);
        ParseStreamEvent event = ParseStreamEvent.builder("message", "file_result")
                .content("복구 후 재검증까지 통과했어요")
                .phase("RECOVERY").status("COMPLETED")
                .current(4).total(12).percent(33)
                .file("shop_mall/order/order.c").language("c")
                .quality("RECOVERED_VALIDATED").counts(counts).build();

        JsonNode json = mapper.readTree(mapper.writeValueAsBytes(event));
        assertEquals("1.1.0", json.path("schemaVersion").asText());
        assertEquals("message", json.path("type").asText());
        assertEquals("복구 후 재검증까지 통과했어요", json.path("content").asText());
        assertEquals("file_result", json.path("event").asText());
        assertEquals(33, json.path("percent").asInt());
        assertEquals(1, json.path("counts").path("recovered").asInt());
        assertFalse(json.has("line"), "Unused optional fields must not add wire noise");
        assertThrows(UnsupportedOperationException.class,
                () -> event.counts().put("failed", 1));
    }

    @Test
    void emitsMinimalCompatibleTerminalEvent() throws Exception {
        JsonNode json = mapper.readTree(mapper.writeValueAsBytes(ParseStreamEvent.complete()));
        assertEquals("complete", json.path("type").asText());
        assertEquals("complete", json.path("event").asText());
        assertEquals(100, json.path("percent").asInt());
        assertTrue(json.path("content").isMissingNode());
    }
}

