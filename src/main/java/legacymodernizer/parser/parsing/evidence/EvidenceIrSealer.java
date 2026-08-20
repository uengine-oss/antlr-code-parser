package legacymodernizer.parser.parsing.evidence;

import java.nio.charset.StandardCharsets;
import java.io.StringWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.core.JsonGenerator;

import legacymodernizer.parser.model.Node;
import legacymodernizer.parser.parsing.SourceTextCodec;
import legacymodernizer.parser.parsing.evidence.ConditionalCompilationEvidence.ConditionalRegionCandidate;
import legacymodernizer.parser.parsing.evidence.ConditionalCompilationEvidence.Presence;
import legacymodernizer.parser.recovery.workingcopy.Hashes;

/**
 * Seals listener-owned syntax boundaries against the exact decoded source.
 * This class validates ranges and hashes; it never searches or reparses source text.
 */
public final class EvidenceIrSealer {

    private static final String VERSION = "1.0.0";
    private static final String ID_DOMAIN = "robo-evidence-v1";
    private static final List<String> KINDS = List.of(
            "call", "import", "symbol", "literal", "assignment", "parameter",
            "macro", "embedded_language", "conditional_region");
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final ThreadLocal<MessageDigest> SHA_256 = ThreadLocal.withInitial(() -> {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException(impossible);
        }
    });

    private EvidenceIrSealer() {
    }

    public static String sealExact(Node root, byte[] rawSource,
                                   SourceTextCodec.DecodedText decoded,
                                   String sourceId,
                                   String parseStatus,
                                   List<CallEvidenceCandidate> calls) {
        return sealExact(root, rawSource, decoded, sourceId, parseStatus, calls,
                ConditionalCompilationEvidence.NONE, true);
    }

    public static String sealExact(Node root, byte[] rawSource,
                                   SourceTextCodec.DecodedText decoded,
                                   String sourceId,
                                   String parseStatus,
                                   List<CallEvidenceCandidate> calls,
                                   ConditionalCompilationEvidence conditional) {
        return seal(root, rawSource, decoded, sourceId, parseStatus, calls,
                conditional, true);
    }

    private static String seal(Node root, byte[] rawSource,
                               SourceTextCodec.DecodedText decoded,
                               String sourceId,
                               String parseStatus,
                               List<CallEvidenceCandidate> calls,
                               ConditionalCompilationEvidence conditional,
                               boolean callSupported) {
        try {
            String source = decoded.text();
            CodePointIndex index = new CodePointIndex(source);
            String normalizedSourceId = normalizeSourceId(sourceId);
            String decodedHash = sha256(source.getBytes(StandardCharsets.UTF_8));
            String effectiveParseStatus = decoded.lossy() ? "unresolved" : parseStatus;

            long unresolvedRegions = conditional.unresolvedRegionCount();
            List<CallEvidenceCandidate> emittedCalls = calls == null ? List.of() : calls;
            Map<String, Integer> ordinals = new HashMap<>();
            Map<String, Integer> grammarRules = new LinkedHashMap<>();
            Map<Presence, Integer> presences = new LinkedHashMap<>();
            List<String> unresolvedScopeFactIds = new ArrayList<>();
            String legacyJson = root.toJson();
            if (legacyJson.isEmpty() || legacyJson.charAt(legacyJson.length() - 1) != '}') {
                throw new IllegalStateException("FILE root did not serialize as a JSON object");
            }
            StringWriter output = new StringWriter(legacyJson.length() + source.length());
            output.write(legacyJson, 0, legacyJson.length() - 1);
            output.write(",\"evidence\":");
            try (JsonGenerator generator = JSON.getFactory().createGenerator(output)) {
                generator.writeStartObject();
                generator.writeStringField("version", VERSION);
                generator.writeStringField("sourceId", normalizedSourceId);
                generator.writeStringField("rawSourceSha256", Hashes.sha256(rawSource));
                generator.writeStringField("decodedTextSha256", decodedHash);
                generator.writeStringField("decodedText", source);
                generator.writeStringField("sourceEncoding", decoded.charset());
                generator.writeStringField("decodeStatus", decoded.lossy() ? "lossy" : "exact");
                generator.writeStringField("positionEncoding", "unicode-code-point");
                generator.writeStringField("rangeConvention", "half-open");
                generator.writeStringField("rangeEncoding", "char-offset-length");
                generator.writeStringField("parseStatus", unresolvedRegions > 0
                        && "exact".equals(effectiveParseStatus)
                                ? "partial" : effectiveParseStatus);

                generator.writeArrayFieldStart("facts");
                for (CallEvidenceCandidate call : emittedCalls) {
                    generator.writeTree(sealCall(call, normalizedSourceId, decodedHash, index,
                            ordinals, grammarRules, presences,
                            conditional.presenceAt(call.callRange().startOffset())));
                }
                for (ConditionalRegionCandidate region : conditional.regions()) {
                    ObjectNode sealedRegion = sealConditionalRegion(region, normalizedSourceId,
                            decodedHash, index, ordinals, grammarRules, presences);
                    generator.writeTree(sealedRegion);
                    if ("conditional".equals(region.presence().status())
                            || "unknown".equals(region.presence().status())) {
                        unresolvedScopeFactIds.add(sealedRegion.path("factId").asText());
                    }
                }
                generator.writeEndArray();

                generator.writeArrayFieldStart("grammarRules");
                for (String grammarRule : grammarRules.keySet()) {
                    generator.writeString(grammarRule);
                }
                generator.writeEndArray();
                generator.writeArrayFieldStart("presences");
                for (Presence presence : presences.keySet()) {
                    generator.writeTree(presenceJson(presence));
                }
                generator.writeEndArray();

                generator.writeArrayFieldStart("completeness");
                for (String kind : KINDS) {
                    int emitted = "call".equals(kind) ? emittedCalls.size()
                            : "conditional_region".equals(kind) ? conditional.regions().size() : 0;
                    int unresolved = 0;
                    List<String> callReasons = new ArrayList<>();
                    if (decoded.lossy()) callReasons.add("insufficient_lossy_decode");
                    if (!"exact".equals(parseStatus)) {
                        callReasons.add("insufficient_parser_recovery");
                    }
                    if (!unresolvedScopeFactIds.isEmpty()) {
                        callReasons.add("insufficient_missing_build_configuration");
                    }
                    String status = "call".equals(kind)
                            ? !callSupported ? "unsupported"
                                    : !callReasons.isEmpty() ? "partial" : "complete"
                            : "conditional_region".equals(kind) ? "complete" : "unsupported";
                    generator.writeStartObject();
                    generator.writeStringField("kind", kind);
                    generator.writeStringField("status", status);
                    if ("call".equals(kind) && callSupported && !callReasons.isEmpty()) {
                        generator.writeStringField("reason", callReasons.get(0));
                        generator.writeArrayFieldStart("reasons");
                        for (String reason : callReasons) generator.writeString(reason);
                        generator.writeEndArray();
                    }
                    if ("call".equals(kind) && callSupported
                            && !unresolvedScopeFactIds.isEmpty()) {
                        generator.writeArrayFieldStart("unresolvedScopeFactIds");
                        for (String factId : unresolvedScopeFactIds) generator.writeString(factId);
                        generator.writeEndArray();
                    }
                    generator.writeNumberField("population", emitted + unresolved);
                    generator.writeNumberField("emitted", emitted);
                    generator.writeNumberField("explicitlyUnresolved", unresolved);
                    generator.writeEndObject();
                }
                generator.writeEndArray();
                generator.writeEndObject();
            }
            output.write('}');
            return output.toString();
        } catch (Exception failure) {
            throw new IllegalStateException("semantic evidence sealing failed", failure);
        }
    }

    public static String sealExact(Node root, byte[] rawSource,
                                   SourceTextCodec.DecodedText decoded,
                                   String sourceId,
                                   String parseStatus,
                                   List<CallEvidenceCandidate> calls,
                                   ConditionalCompilationEvidence conditional,
                                   boolean callSupported) {
        return seal(root, rawSource, decoded, sourceId, parseStatus, calls,
                conditional, callSupported);
    }

    private static ObjectNode sealCall(CallEvidenceCandidate candidate,
                                       String sourceId,
                                       String decodedHash,
                                       CodePointIndex index,
                                       Map<String, Integer> ordinals,
                                       Map<String, Integer> grammarRules,
                                       Map<Presence, Integer> presences,
                                       Presence factPresence) {
        index.requireValid(candidate.callRange());
        index.requireValid(candidate.calleeRange());
        if (candidate.calleeRange().startOffset() < candidate.callRange().startOffset()
                || candidate.calleeRange().endOffset() > candidate.callRange().endOffset()) {
            throw new IllegalArgumentException("callee range is outside call range");
        }
        for (SourceRangeCandidate argument : candidate.argumentRanges()) {
            index.requireValid(argument);
            if (argument.startOffset() < candidate.callRange().startOffset()
                    || argument.endOffset() > candidate.callRange().endOffset()) {
                throw new IllegalArgumentException("argument range is outside call range");
            }
        }

        String ordinalKey = "call\0" + candidate.callRange().startOffset()
                + "\0" + candidate.callRange().endOffset();
        int ordinal = ordinals.merge(ordinalKey, 1, Integer::sum) - 1;
        ObjectNode fact = JSON.createObjectNode();
        fact.put("factId", factId(sourceId, decodedHash, "call",
                candidate.callRange(), ordinal));
        fact.put("kind", "call");
        fact.set("range", index.rangeJson(candidate.callRange()));
        fact.put("grammarRuleRef", reference(grammarRules, candidate.grammarRule()));
        fact.put("presenceRef", reference(presences, factPresence));

        ObjectNode payload = fact.putObject("payload");
        payload.set("calleeRange", index.rangeJson(candidate.calleeRange()));
        ArrayNode arguments = payload.putArray("argumentRanges");
        for (SourceRangeCandidate argument : candidate.argumentRanges()) {
            arguments.add(index.rangeJson(argument));
        }
        return fact;
    }

    private static ObjectNode sealConditionalRegion(ConditionalRegionCandidate candidate,
                                                     String sourceId,
                                                     String decodedHash,
                                                     CodePointIndex index,
                                                     Map<String, Integer> ordinals,
                                                     Map<String, Integer> grammarRules,
                                                     Map<Presence, Integer> presences) {
        index.requireValid(candidate.range());
        String ordinalKey = "conditional_region\0" + candidate.range().startOffset()
                + "\0" + candidate.range().endOffset();
        int ordinal = ordinals.merge(ordinalKey, 1, Integer::sum) - 1;
        ObjectNode fact = JSON.createObjectNode();
        fact.put("factId", factId(sourceId, decodedHash, "conditional_region",
                candidate.range(), ordinal));
        fact.put("kind", "conditional_region");
        fact.set("range", index.rangeJson(candidate.range()));
        fact.put("grammarRuleRef", reference(grammarRules, "Directive"));
        fact.put("presenceRef", reference(presences, candidate.presence()));
        ObjectNode payload = fact.putObject("payload");
        payload.put("condition", candidate.condition());
        return fact;
    }

    private static <T> int reference(Map<T, Integer> table, T value) {
        Integer existing = table.get(value);
        if (existing != null) return existing;
        int reference = table.size();
        table.put(value, reference);
        return reference;
    }

    private static ObjectNode presenceJson(Presence presence) {
        ObjectNode result = JSON.createObjectNode();
        result.put("status", presence.status());
        if (presence.condition() == null) result.putNull("condition");
        else result.put("condition", presence.condition());
        result.putNull("configurationId");
        result.put("provenance", presence.provenance());
        return result;
    }

    private static String factId(String sourceId, String decodedHash, String kind,
                                 SourceRangeCandidate range, int ordinal) {
        String rangeText = range.startOffset() + ":"
                + (range.endOffset() - range.startOffset());
        String tuple = String.join("\0", ID_DOMAIN, sourceId, decodedHash, kind,
                rangeText, Integer.toString(ordinal));
        return sha256(tuple.getBytes(StandardCharsets.UTF_8));
    }

    private static String normalizeSourceId(String sourceId) {
        if (sourceId == null || sourceId.isBlank()) {
            throw new IllegalArgumentException("sourceId is required");
        }
        String normalized = sourceId.replace('\\', '/');
        if (normalized.startsWith("/") || normalized.matches("^[A-Za-z]:/.*")) {
            throw new IllegalArgumentException("sourceId must be relative: " + sourceId);
        }
        return normalized;
    }

    private static String sha256(byte[] bytes) {
        MessageDigest digest = SHA_256.get();
        digest.reset();
        return java.util.HexFormat.of().formatHex(digest.digest(bytes));
    }

    private static final class CodePointIndex {
        private final int[] codePoints;

        private CodePointIndex(String source) {
            this.codePoints = source.codePoints().toArray();
        }

        private void requireValid(SourceRangeCandidate range) {
            if (range.endOffset() > codePoints.length) {
                throw new IllegalArgumentException("range exceeds decoded source: " + range);
            }
        }

        private ObjectNode rangeJson(SourceRangeCandidate range) {
            ObjectNode result = JSON.createObjectNode();
            result.put("charOffset", range.startOffset());
            result.put("charLength", range.endOffset() - range.startOffset());
            return result;
        }
    }
}
