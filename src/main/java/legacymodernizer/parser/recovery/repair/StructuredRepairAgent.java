package legacymodernizer.parser.recovery.repair;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Component
public final class StructuredRepairAgent implements RepairAgent {

    static final String SUBMIT_FUNCTION = "submit_parser_repair";
    static final int MAX_REQUEST_BYTES = 262_144;
    static final int MAX_RESPONSE_BYTES = 131_072;
    private static final String SYSTEM_PROMPT_RESOURCE =
            "/recovery/repair-agent-system-prompt.txt";

    private final boolean enabled;
    private final URI chatCompletionsEndpoint;
    private final String apiKey;
    private final String model;
    private final Duration timeout;
    private final int maxOutputTokens;
    private final String reasoningEffort;
    private final Boolean thinkingEnabled;
    private final Integer topK;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String systemPrompt;
    /** FR-025: prompt token count of the most recent proposal, when the provider reports it. */
    private volatile Integer lastPromptTokens;

    public StructuredRepairAgent() {
        this.enabled = booleanSetting("parser.repair.agent.enabled",
                "PARSER_REPAIR_AGENT_ENABLED", false);
        String apiBase = setting("parser.repair.agent.api.base",
                "PARSER_REPAIR_AGENT_API_BASE", "");
        this.model = setting("parser.repair.agent.model",
                "PARSER_REPAIR_AGENT_MODEL", "");
        this.apiKey = setting("parser.repair.agent.api.key",
                "PARSER_REPAIR_AGENT_API_KEY", "");
        this.timeout = Duration.ofSeconds(longSetting(
                "parser.repair.agent.timeout.seconds",
                "PARSER_REPAIR_AGENT_TIMEOUT_SECONDS", 120));
        this.maxOutputTokens = intSetting("parser.repair.agent.max.output.tokens",
                "PARSER_REPAIR_AGENT_MAX_OUTPUT_TOKENS", 2048);
        this.reasoningEffort = optionalReasoningEffortSetting(
                "parser.repair.agent.reasoning.effort",
                "PARSER_REPAIR_AGENT_REASONING_EFFORT");
        this.thinkingEnabled = optionalBooleanSetting(
                "parser.repair.agent.thinking.enabled",
                "PARSER_REPAIR_AGENT_THINKING_ENABLED");
        this.topK = optionalPositiveIntSetting(
                "parser.repair.agent.top.k", "PARSER_REPAIR_AGENT_TOP_K");
        requireExclusiveProviderOptions(reasoningEffort, thinkingEnabled, topK);
        if (enabled && apiBase.isBlank()) {
            throw new RepairAgentException("REPAIR_AGENT_API_BASE_REQUIRED");
        }
        if (enabled && model.isBlank()) {
            throw new RepairAgentException("REPAIR_AGENT_MODEL_REQUIRED");
        }
        this.chatCompletionsEndpoint = apiBase.isBlank() ? null : chatCompletionsEndpoint(apiBase);
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        this.objectMapper = new ObjectMapper()
                .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        this.systemPrompt = loadSystemPrompt();
    }

    StructuredRepairAgent(boolean enabled, URI chatCompletionsEndpoint, String apiKey,
                          String model, Duration timeout, int maxOutputTokens,
                          HttpClient httpClient, ObjectMapper objectMapper,
                          String systemPrompt) {
        this(enabled, chatCompletionsEndpoint, apiKey, model, timeout, maxOutputTokens,
                null, null, null, httpClient, objectMapper, systemPrompt);
    }

    StructuredRepairAgent(boolean enabled, URI chatCompletionsEndpoint, String apiKey,
                          String model, Duration timeout, int maxOutputTokens,
                          Boolean thinkingEnabled, HttpClient httpClient,
                          ObjectMapper objectMapper, String systemPrompt) {
        this(enabled, chatCompletionsEndpoint, apiKey, model, timeout, maxOutputTokens,
                null, thinkingEnabled, null, httpClient, objectMapper, systemPrompt);
    }

    StructuredRepairAgent(boolean enabled, URI chatCompletionsEndpoint, String apiKey,
                          String model, Duration timeout, int maxOutputTokens,
                          String reasoningEffort, Boolean thinkingEnabled, Integer topK,
                          HttpClient httpClient,
                          ObjectMapper objectMapper, String systemPrompt) {
        this.enabled = enabled;
        this.chatCompletionsEndpoint = chatCompletionsEndpoint;
        this.apiKey = apiKey == null ? "" : apiKey;
        this.model = model;
        this.timeout = timeout;
        this.maxOutputTokens = maxOutputTokens;
        requireExclusiveProviderOptions(reasoningEffort, thinkingEnabled, topK);
        this.reasoningEffort = reasoningEffort;
        this.thinkingEnabled = thinkingEnabled;
        this.topK = topK;
        this.httpClient = httpClient;
        this.objectMapper = objectMapper.enable(
                DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        this.systemPrompt = systemPrompt;
    }

    @Override
    public boolean enabled() {
        return enabled;
    }

    @Override
    public Integer lastPromptTokens() {
        return lastPromptTokens;
    }

    @Override
    public PatchProposal propose(FailureEnvelope envelope) {
        if (!enabled) throw new RepairAgentException("REPAIR_AGENT_DISABLED");
        if (chatCompletionsEndpoint == null) {
            throw new RepairAgentException("REPAIR_AGENT_API_BASE_REQUIRED");
        }
        if (model == null || model.isBlank()) {
            throw new RepairAgentException("REPAIR_AGENT_MODEL_REQUIRED");
        }
        try {
            byte[] requestBody = objectMapper.writeValueAsBytes(buildRequest(envelope));
            if (requestBody.length > MAX_REQUEST_BYTES) {
                throw new RepairAgentException("REPAIR_AGENT_REQUEST_TOO_LARGE");
            }
            HttpRequest.Builder request = HttpRequest.newBuilder(chatCompletionsEndpoint)
                    .timeout(timeout)
                    .header("Content-Type", "application/json; charset=utf-8")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofByteArray(requestBody));
            if (!apiKey.isBlank()) request.header("Authorization", "Bearer " + apiKey);

            HttpResponse<byte[]> response = httpClient.send(request.build(),
                    HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new RepairAgentException("REPAIR_AGENT_HTTP_" + response.statusCode());
            }
            if (response.body().length > MAX_RESPONSE_BYTES) {
                throw new RepairAgentException("REPAIR_AGENT_RESPONSE_TOO_LARGE");
            }
            return parseProposal(response.body());
        } catch (RepairAgentException error) {
            throw error;
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            throw new RepairAgentException("REPAIR_AGENT_INTERRUPTED", error);
        } catch (Exception error) {
            throw new RepairAgentException(
                    "REPAIR_AGENT_UNAVAILABLE:" + error.getClass().getSimpleName(), error);
        }
    }

    private ObjectNode buildRequest(FailureEnvelope envelope) throws IOException {
        ObjectNode request = objectMapper.createObjectNode();
        request.put("model", model);
        if (reasoningEffort == null) {
            request.put("temperature", 0.0);
            request.put("max_tokens", maxOutputTokens);
        } else {
            request.put("reasoning_effort", reasoningEffort);
            request.put("max_completion_tokens", maxOutputTokens);
        }
        request.put("parallel_tool_calls", false);
        if (thinkingEnabled != null) {
            request.putObject("chat_template_kwargs")
                    .put("enable_thinking", thinkingEnabled);
        }
        if (topK != null) request.put("top_k", topK);

        ArrayNode messages = request.putArray("messages");
        messages.addObject().put("role", "system").put("content", systemPrompt);
        messages.addObject().put("role", "user").put("content",
                objectMapper.writeValueAsString(envelope));

        ObjectNode function = request.putArray("tools").addObject()
                .put("type", "function").putObject("function");
        function.put("name", SUBMIT_FUNCTION);
        function.put("description",
                "Submit one bounded parser repair proposal for Parser validation.");
        function.set("parameters", proposalSchema());
        function.put("strict", true);

        ObjectNode toolChoice = request.putObject("tool_choice");
        toolChoice.put("type", "function");
        toolChoice.putObject("function").put("name", SUBMIT_FUNCTION);
        return request;
    }

    private ObjectNode proposalSchema() {
        ObjectNode schema = objectMapper.createObjectNode();
        schema.put("type", "object");
        schema.put("additionalProperties", false);
        schema.putArray("required").add("schemaVersion").add("failureEnvelopeHash")
                .add("edits").add("rationale").add("confidence").add("ambiguities");
        ObjectNode properties = schema.putObject("properties");
        properties.putObject("schemaVersion").put("type", "string").put("const", "1.0.0");
        properties.putObject("failureEnvelopeHash").put("type", "string");
        ObjectNode edits = properties.putObject("edits");
        edits.put("type", "array");
        // minItems 0: the prompt instructs "no edits + ambiguity" for honest abstention,
        // so an empty edit list must be schema-legal (2nd adversarial audit, 2026-07-22).
        edits.put("minItems", 0);
        edits.put("maxItems", 64);
        ObjectNode edit = edits.putObject("items");
        edit.put("type", "object");
        edit.put("additionalProperties", false);
        edit.putArray("required").add("startOffset").add("endOffset")
                .add("expectedText").add("replacement").add("reason");
        ObjectNode editProperties = edit.putObject("properties");
        editProperties.putObject("startOffset").put("type", "integer").put("minimum", 0);
        editProperties.putObject("endOffset").put("type", "integer").put("minimum", 0);
        editProperties.putObject("expectedText").put("type", "string")
                .put("maxLength", 16_384);
        editProperties.putObject("replacement").put("type", "string")
                .put("maxLength", 16_384);
        editProperties.putObject("reason").put("type", "string")
                .put("minLength", 1).put("maxLength", 512);
        properties.putObject("rationale").put("type", "string")
                .put("minLength", 1).put("maxLength", 2_048);
        properties.putObject("confidence").put("type", "number")
                .put("minimum", 0).put("maximum", 1);
        ObjectNode ambiguities = properties.putObject("ambiguities");
        ambiguities.put("type", "array");
        ambiguities.putObject("items").put("type", "string");
        return schema;
    }

    private PatchProposal parseProposal(byte[] responseBody) throws IOException {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode promptTokens = root.path("usage").path("prompt_tokens");
        lastPromptTokens = promptTokens.isIntegralNumber() ? promptTokens.asInt() : null;
        JsonNode choices = root.path("choices");
        if (!choices.isArray() || choices.size() != 1) {
            throw new RepairAgentException("REPAIR_AGENT_INVALID_CHOICE_COUNT");
        }
        JsonNode toolCalls = choices.get(0).path("message").path("tool_calls");
        if (!toolCalls.isArray() || toolCalls.size() != 1) {
            throw new RepairAgentException("REPAIR_AGENT_MISSING_SUBMIT");
        }
        JsonNode function = toolCalls.get(0).path("function");
        if (!SUBMIT_FUNCTION.equals(function.path("name").asText())) {
            throw new RepairAgentException("REPAIR_AGENT_WRONG_SUBMIT_FUNCTION");
        }
        JsonNode rawArguments = function.get("arguments");
        if (rawArguments == null || !rawArguments.isTextual()) {
            throw new RepairAgentException("REPAIR_AGENT_INVALID_ARGUMENTS");
        }
        JsonNode proposal = objectMapper.readTree(rawArguments.asText());
        return objectMapper.treeToValue(proposal, PatchProposal.class);
    }

    /** FR-063: OpenAI-style and SGLang-style options are mutually exclusive per provider. */
    private static void requireExclusiveProviderOptions(String reasoningEffort,
            Boolean thinkingEnabled, Integer topK) {
        if (reasoningEffort != null && (thinkingEnabled != null || topK != null)) {
            throw new RepairAgentException("REPAIR_AGENT_PROVIDER_OPTIONS_CONFLICT");
        }
    }

    private static URI chatCompletionsEndpoint(String apiBase) {
        String normalized = apiBase.trim().replaceAll("/+$", "");
        if (!normalized.endsWith("/chat/completions")) normalized += "/chat/completions";
        return URI.create(normalized);
    }

    private static String loadSystemPrompt() {
        try (InputStream input = StructuredRepairAgent.class
                .getResourceAsStream(SYSTEM_PROMPT_RESOURCE)) {
            if (input == null) {
                throw new RepairAgentException("REPAIR_AGENT_PROMPT_MISSING");
            }
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException error) {
            throw new RepairAgentException("REPAIR_AGENT_PROMPT_UNREADABLE", error);
        }
    }

    private static String setting(String property, String environment, String fallback) {
        String propertyValue = System.getProperty(property);
        if (propertyValue != null && !propertyValue.isBlank()) return propertyValue.trim();
        String environmentValue = System.getenv(environment);
        return environmentValue == null || environmentValue.isBlank()
                ? fallback : environmentValue.trim();
    }

    private static boolean booleanSetting(String property, String environment, boolean fallback) {
        return Boolean.parseBoolean(setting(property, environment, Boolean.toString(fallback)));
    }

    private static Boolean optionalBooleanSetting(String property, String environment) {
        String value = setting(property, environment, "");
        if (value.isBlank()) return null;
        if ("true".equalsIgnoreCase(value)) return true;
        if ("false".equalsIgnoreCase(value)) return false;
        throw new RepairAgentException("REPAIR_AGENT_THINKING_SETTING_INVALID");
    }

    private static Integer optionalPositiveIntSetting(String property, String environment) {
        String value = setting(property, environment, "");
        if (value.isBlank()) return null;
        try {
            int parsed = Integer.parseInt(value);
            if (parsed > 0) return parsed;
        } catch (NumberFormatException ignored) {
            // Report one stable configuration error below.
        }
        throw new RepairAgentException("REPAIR_AGENT_TOP_K_SETTING_INVALID");
    }

    private static String optionalReasoningEffortSetting(String property, String environment) {
        String value = setting(property, environment, "");
        if (value.isBlank()) return null;
        String normalized = value.trim().toLowerCase();
        return switch (normalized) {
            case "none", "minimal", "low", "medium", "high", "xhigh", "max" -> normalized;
            default -> throw new RepairAgentException(
                    "REPAIR_AGENT_REASONING_EFFORT_INVALID");
        };
    }

    private static long longSetting(String property, String environment, long fallback) {
        try {
            long parsedSetting = Long.parseLong(setting(property, environment, Long.toString(fallback)));
            return parsedSetting > 0 ? parsedSetting : fallback;
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static int intSetting(String property, String environment, int fallback) {
        try {
            int parsedSetting = Integer.parseInt(setting(property, environment, Integer.toString(fallback)));
            return parsedSetting > 0 ? parsedSetting : fallback;
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }
}
