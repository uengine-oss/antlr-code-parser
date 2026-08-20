package legacymodernizer.parser.parsing.evidence;

import java.nio.charset.StandardCharsets;
import java.io.StringWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
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
                conditional, true, null, null);
    }

    public static String sealExact(Node root, byte[] rawSource,
                                   SourceTextCodec.DecodedText decoded,
                                   String sourceId,
                                   String parseStatus,
                                   List<CallEvidenceCandidate> calls,
                                   ConditionalCompilationEvidence conditional,
                                   MacroEvidenceExtraction macros) {
        return seal(root, rawSource, decoded, sourceId, parseStatus, calls,
                conditional, true, macros, null);
    }

    public static String sealExact(Node root, byte[] rawSource,
                                   SourceTextCodec.DecodedText decoded,
                                   String sourceId,
                                   String parseStatus,
                                   List<CallEvidenceCandidate> calls,
                                   ConditionalCompilationEvidence conditional,
                                   MacroEvidenceExtraction macros,
                                   ConfiguredPreprocessingEvidence configuredPreprocessing) {
        return seal(root, rawSource, decoded, sourceId, parseStatus, calls,
                conditional, true, macros, configuredPreprocessing);
    }

    private static String seal(Node root, byte[] rawSource,
                               SourceTextCodec.DecodedText decoded,
                               String sourceId,
                               String parseStatus,
                               List<CallEvidenceCandidate> calls,
                               ConditionalCompilationEvidence conditional,
                               boolean callSupported,
                               MacroEvidenceExtraction macros,
                               ConfiguredPreprocessingEvidence configuredPreprocessing) {
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
                if (configuredPreprocessing != null) {
                    writeConfiguredPreprocessing(generator, configuredPreprocessing);
                }

                generator.writeArrayFieldStart("facts");
                for (CallEvidenceCandidate call : emittedCalls) {
                    generator.writeTree(sealCall(call, normalizedSourceId, decodedHash, index,
                            ordinals, grammarRules, presences,
                            conditional.presenceAt(call.callRange().startOffset())));
                }
                if (macros != null) {
                    for (MacroEvidenceCandidate macro : macros.candidates()) {
                        generator.writeTree(sealMacro(macro, normalizedSourceId, decodedHash,
                                index, ordinals, grammarRules, presences,
                                conditional.presenceAt(macro.range().startOffset())));
                    }
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
                            : "macro".equals(kind) && macros != null
                                    ? macros.candidates().size()
                            : "conditional_region".equals(kind) ? conditional.regions().size() : 0;
                    int unresolved = "macro".equals(kind) && macros != null
                            ? macros.explicitlyUnresolved() : 0;
                    List<String> callReasons = new ArrayList<>();
                    if (decoded.lossy()) callReasons.add("insufficient_lossy_decode");
                    if (!"exact".equals(parseStatus)) {
                        callReasons.add("insufficient_parser_recovery");
                    }
                    if (!unresolvedScopeFactIds.isEmpty()) {
                        callReasons.add("insufficient_missing_build_configuration");
                    }
                    List<String> macroReasons = new ArrayList<>();
                    if (macros != null) {
                        if (decoded.lossy()) {
                            macroReasons.add("insufficient_lossy_decode");
                        }
                        macroReasons.addAll(macros.reasons());
                    }
                    String status = "call".equals(kind)
                            ? !callSupported ? "unsupported"
                                    : !callReasons.isEmpty() ? "partial" : "complete"
                            : "macro".equals(kind)
                                    ? macros == null ? "unsupported"
                                            : !macroReasons.isEmpty() ? "partial" : "complete"
                            : "conditional_region".equals(kind) ? "complete" : "unsupported";
                    List<String> reasons = "call".equals(kind)
                            ? callReasons : "macro".equals(kind) ? macroReasons : List.of();
                    generator.writeStartObject();
                    generator.writeStringField("kind", kind);
                    generator.writeStringField("status", status);
                    if (!reasons.isEmpty()) {
                        generator.writeStringField("reason", reasons.get(0));
                        generator.writeArrayFieldStart("reasons");
                        for (String reason : reasons) generator.writeString(reason);
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
                conditional, callSupported, null, null);
    }

    private static void writeConfiguredPreprocessing(
            JsonGenerator generator,
            ConfiguredPreprocessingEvidence configured) throws Exception {
        generator.writeObjectFieldStart("configuredPreprocessing");
        generator.writeStringField("version", configured.version());
        generator.writeStringField("status", configured.status());
        generator.writeStringField("trust", configured.trust());

        var build = configured.build();
        generator.writeObjectFieldStart("build");
        generator.writeStringField("status",
                build.status().name().toLowerCase(Locale.ROOT));
        generator.writeNumberField("population", build.population());
        generator.writeNumberField("emitted", build.emitted());
        generator.writeNumberField("explicitlyUnresolved", build.explicitlyUnresolved());
        writeStringArray(generator, "commandOccurrenceIds", build.commandOccurrenceIds());
        writeStringArray(generator, "emittedCommandOccurrenceIds",
                build.emittedCommandOccurrenceIds());
        writeStringArray(generator, "unresolvedCommandOccurrenceIds",
                build.unresolvedCommandOccurrenceIds());
        writeStringArray(generator, "unresolvedEvidenceIds",
                build.unresolvedEvidenceIds());
        writeStringArray(generator, "reasons", build.unresolvedReasons());
        generator.writeEndObject();

        var trace = configured.trace();
        generator.writeObjectFieldStart("trace");
        generator.writeStringField("status", trace.status());
        generator.writeNumberField("population", trace.population());
        generator.writeNumberField("emitted", trace.emitted());
        generator.writeNumberField("explicitlyUnresolved", trace.explicitlyUnresolved());
        writeStringArray(generator, "evidenceIds", trace.evidenceIds());
        writeStringArray(generator, "emittedEvidenceIds", trace.emittedEvidenceIds());
        writeStringArray(generator, "unresolvedEvidenceIds", trace.unresolvedEvidenceIds());
        writeStringArray(generator, "reasons", trace.reasons());
        generator.writeEndObject();
        generator.writeEndObject();
    }

    private static void writeStringArray(JsonGenerator generator, String field,
                                         List<String> values) throws Exception {
        generator.writeArrayFieldStart(field);
        for (String value : values) {
            generator.writeString(value);
        }
        generator.writeEndArray();
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
        payload.put("calleeKind", candidate.calleeKind());
        if (candidate.terminalName() == null) payload.putNull("terminalName");
        else payload.put("terminalName", candidate.terminalName());
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

    private static ObjectNode sealMacro(MacroEvidenceCandidate candidate,
                                        String sourceId,
                                        String decodedHash,
                                        CodePointIndex index,
                                        Map<String, Integer> ordinals,
                                        Map<String, Integer> grammarRules,
                                        Map<Presence, Integer> presences,
                                        Presence factPresence) {
        index.requireValid(candidate.range());
        index.requireSubrange(candidate.range(), candidate.nameRange(), "macro name");
        for (SourceRangeCandidate parameter : candidate.parameterRanges()) {
            index.requireSubrange(candidate.range(), parameter, "macro parameter");
        }
        if (candidate.replacementRange() != null) {
            index.requireSubrange(candidate.range(), candidate.replacementRange(),
                    "macro replacement");
        }

        String ordinalKey = "macro\0" + candidate.range().startOffset()
                + "\0" + candidate.range().endOffset();
        int ordinal = ordinals.merge(ordinalKey, 1, Integer::sum) - 1;
        ObjectNode fact = JSON.createObjectNode();
        fact.put("factId", factId(sourceId, decodedHash, "macro", candidate.range(), ordinal));
        fact.put("kind", "macro");
        fact.set("range", index.rangeJson(candidate.range()));
        fact.put("grammarRuleRef", reference(grammarRules, candidate.grammarRule()));
        fact.put("presenceRef", reference(presences, factPresence));

        ObjectNode payload = fact.putObject("payload");
        payload.set("nameRange", index.rangeJson(candidate.nameRange()));
        payload.put("macroKind", candidate.macroKind());
        payload.put("terminalName", candidate.terminalName());
        ArrayNode parameters = payload.putArray("parameterRanges");
        for (SourceRangeCandidate parameter : candidate.parameterRanges()) {
            parameters.add(index.rangeJson(parameter));
        }
        payload.put("variadic", candidate.variadic());
        if (candidate.replacementRange() == null) payload.putNull("replacementRange");
        else payload.set("replacementRange", index.rangeJson(candidate.replacementRange()));
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

        private void requireSubrange(SourceRangeCandidate outer,
                                     SourceRangeCandidate inner,
                                     String field) {
            requireValid(inner);
            if (inner.startOffset() < outer.startOffset()
                    || inner.endOffset() > outer.endOffset()) {
                throw new IllegalArgumentException(field + " range is outside fact range");
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
