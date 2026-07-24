package legacymodernizer.parser.recovery.repair;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.net.httpserver.HttpServer;

class StructuredRepairAgentTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private HttpServer server;

    @AfterEach
    void stopServer() {
        if (server != null) server.stop(0);
    }

    @Test
    void sendsOnlyTheBoundedFailureEnvelopeAndParsesOneSubmitToolCall() throws Exception {
        FailureEnvelope envelope = envelope();
        PatchProposal expected = new PatchProposal("1.0.0", envelope.failureEnvelopeHash(),
                List.of(new AgentTextEdit(6, 8, "AS", "  ",
                        "Remove invalid Oracle alias keyword")),
                "One unambiguous syntax-only edit", 0.98, List.of());
        ObjectNode response = objectMapper.createObjectNode();
        ObjectNode function = response.putArray("choices").addObject()
                .putObject("message").putArray("tool_calls").addObject()
                .putObject("function");
        function.put("name", StructuredRepairAgent.SUBMIT_FUNCTION);
        function.put("arguments", objectMapper.writeValueAsString(expected));

        AtomicReference<String> capturedBody = new AtomicReference<>();
        AtomicReference<String> capturedAuthorization = new AtomicReference<>();
        URI endpoint = startServer(200, objectMapper.writeValueAsString(response),
                capturedBody, capturedAuthorization);
        StructuredRepairAgent agent = agent(endpoint);

        PatchProposal actual = agent.propose(envelope);

        assertEquals(expected, actual);
        assertEquals("Bearer test-token", capturedAuthorization.get());
        JsonNode request = objectMapper.readTree(capturedBody.get());
        assertEquals("repair-model", request.path("model").asText());
        assertEquals(512, request.path("max_tokens").asInt());
        assertFalse(request.path("parallel_tool_calls").asBoolean(true));
        assertFalse(request.path("chat_template_kwargs")
                .path("enable_thinking").asBoolean(true));
        assertEquals(1, request.path("top_k").asInt());
        assertEquals(StructuredRepairAgent.SUBMIT_FUNCTION,
                request.path("tool_choice").path("function").path("name").asText());
        assertEquals(StructuredRepairAgent.SUBMIT_FUNCTION,
                request.path("tools").get(0).path("function").path("name").asText());
        assertTrue(request.path("tools").get(0).path("function").path("strict").asBoolean());
        String systemPrompt = request.path("messages").get(0).path("content").asText();
        assertTrue(systemPrompt.contains("Return one bounded syntax repair tool call."));
        assertTrue(systemPrompt.contains("name=\"common-syntax-repair\""));
        assertTrue(systemPrompt.contains("name=\"oracle-syntax-repair\""));
        assertFalse(systemPrompt.contains("name=\"postgresql-syntax-repair\""));
        String userEnvelope = request.path("messages").get(1).path("content").asText();
        assertEquals(envelope.sourceExcerpt(),
                objectMapper.readTree(userEnvelope).path("sourceExcerpt").asText());
        assertFalse(capturedBody.get().toLowerCase().contains("analyzer"));
    }

    @Test
    void rejectsMissingSubmitToolAndProviderHttpFailureExplicitly() throws Exception {
        AtomicReference<String> request = new AtomicReference<>();
        URI missingSubmit = startServer(200,
                "{\"choices\":[{\"message\":{\"content\":\"plain text\"}}]}",
                request, new AtomicReference<>());
        RepairAgentException missing = assertThrows(RepairAgentException.class,
                () -> agent(missingSubmit).propose(envelope()));
        assertEquals("REPAIR_AGENT_MISSING_SUBMIT", missing.getMessage());

        server.stop(0);
        server = null;
        URI unavailable = startServer(503, "{\"error\":\"unavailable\"}",
                new AtomicReference<>(), new AtomicReference<>());
        RepairAgentException failed = assertThrows(RepairAgentException.class,
                () -> agent(unavailable).propose(envelope()));
        assertEquals("REPAIR_AGENT_HTTP_503", failed.getMessage());
    }

    @Test
    void enforcesResponseByteBudgetAndRequestTimeout() throws Exception {
        URI oversized = startServer(200,
                "x".repeat(StructuredRepairAgent.MAX_RESPONSE_BYTES + 1),
                new AtomicReference<>(), new AtomicReference<>());
        assertEquals("REPAIR_AGENT_RESPONSE_TOO_LARGE",
                assertThrows(RepairAgentException.class,
                        () -> agent(oversized).propose(envelope())).getMessage());

        server.stop(0);
        server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        server.createContext("/v1/chat/completions", exchange -> {
            try {
                Thread.sleep(250);
                exchange.sendResponseHeaders(200, 0);
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
            } finally {
                exchange.close();
            }
        });
        server.start();
        URI delayed = URI.create("http://127.0.0.1:" + server.getAddress().getPort()
                + "/v1/chat/completions");

        RepairAgentException timeout = assertThrows(RepairAgentException.class,
                () -> agent(delayed, Duration.ofMillis(50)).propose(envelope()));
        assertTrue(timeout.getMessage().startsWith("REPAIR_AGENT_UNAVAILABLE:HttpTimeoutException"));
    }

    private StructuredRepairAgent agent(URI endpoint) {
        return agent(endpoint, Duration.ofSeconds(2));
    }

    private StructuredRepairAgent agent(URI endpoint, Duration timeout) {
        return new StructuredRepairAgent(true, endpoint, "test-token", "repair-model",
                timeout, 512, null, false, 1, HttpClient.newHttpClient(),
                new ObjectMapper(), "Return one bounded syntax repair tool call.");
    }

    private URI startServer(int status, String response,
                            AtomicReference<String> capturedBody,
                            AtomicReference<String> capturedAuthorization) throws IOException {
        server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        server.createContext("/v1/chat/completions", exchange -> {
            capturedBody.set(new String(exchange.getRequestBody().readAllBytes(),
                    StandardCharsets.UTF_8));
            capturedAuthorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
            byte[] body = response.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(status, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
        return URI.create("http://127.0.0.1:" + server.getAddress().getPort()
                + "/v1/chat/completions");
    }

    private static FailureEnvelope envelope() {
        String source = "TABLE AS A";
        return new FailureEnvelope("2.0.0", "a".repeat(64), "oracle", "grammar-1",
                "b".repeat(64), "c".repeat(64), "unit-1", "PROCEDURE",
                100, 110, 5, 5,
                "L1", 0, source.length(), 100, 100 + source.length(), "d".repeat(64), "",
                List.of(new DiagnosticEvidence("PARSER", "ANTLR_SYNTAX_ERROR",
                        "Unexpected AS", 5, 7, 7, 9, "AS", "identifier",
                        List.of("select_statement"), source)),
                new CoverageEvidence(1, 0, List.of("alias_proc"), false), List.of(),
                "unit=unit-1; kind=PROCEDURE; name=alias_proc", source,
                List.of(0), List.of(new SourceTokenEvidence(6, 8, "AS")),
                new RepairConstraints(0, source.length(), 16, 2, 3,
                        List.of("AST", "NODE_JSON", "FULL_UNIT_REWRITE")));
    }
}
