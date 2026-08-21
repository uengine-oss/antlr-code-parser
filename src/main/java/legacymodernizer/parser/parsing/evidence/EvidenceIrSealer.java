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
import legacymodernizer.parser.parsing.evidence.CallableEvidenceExtraction.CallBindingCandidate;
import legacymodernizer.parser.parsing.evidence.CallableEvidenceExtraction.CallableCandidate;
import legacymodernizer.parser.recovery.workingcopy.Hashes;

/**
 * Seals listener-owned syntax boundaries against the exact decoded source.
 * This class validates ranges and hashes; it never searches or reparses source text.
 */
public final class EvidenceIrSealer {

    private static final String VERSION = "1.1.0";
    private static final String SYMBOL_VERSION = "1.2.0";
    private static final String CALL_BINDING_VERSION = "1.3.0";
    private static final String ID_DOMAIN = "robo-evidence-v1";
    private static final String BINDING_ID_DOMAIN = "robo-call-binding-v1";
    private static final String COMPATIBILITY_ID_DOMAIN = "robo-call-compatibility-v1";
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
                conditional, true, null, null, null, null, null);
    }

    public static String sealExact(Node root, byte[] rawSource,
                                   SourceTextCodec.DecodedText decoded,
                                   String sourceId,
                                   String parseStatus,
                                   List<CallEvidenceCandidate> calls,
                                   ConditionalCompilationEvidence conditional,
                                   MacroEvidenceExtraction macros) {
        return seal(root, rawSource, decoded, sourceId, parseStatus, calls,
                conditional, true, macros, null, null, null, null);
    }

    public static String sealExact(Node root, byte[] rawSource,
                                   SourceTextCodec.DecodedText decoded,
                                   String sourceId,
                                   String parseStatus,
                                   List<CallEvidenceCandidate> calls,
                                   ImportEvidenceExtraction imports) {
        return seal(root, rawSource, decoded, sourceId, parseStatus, calls,
                ConditionalCompilationEvidence.NONE, true, null, imports, null, null, null);
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
                conditional, true, macros, null, configuredPreprocessing, null, null);
    }

    public static String sealExact(Node root, byte[] rawSource,
                                   SourceTextCodec.DecodedText decoded,
                                   String sourceId,
                                   String parseStatus,
                                   List<CallEvidenceCandidate> calls,
                                   ConditionalCompilationEvidence conditional,
                                   MacroEvidenceExtraction macros,
                                   ImportEvidenceExtraction imports,
                                   ConfiguredPreprocessingEvidence configuredPreprocessing) {
        return seal(root, rawSource, decoded, sourceId, parseStatus, calls,
                conditional, true, macros, imports, configuredPreprocessing, null, null);
    }

    public static String sealExact(Node root, byte[] rawSource,
                                   SourceTextCodec.DecodedText decoded,
                                   String sourceId,
                                   String parseStatus,
                                   List<CallEvidenceCandidate> calls,
                                   ConditionalCompilationEvidence conditional,
                                   MacroEvidenceExtraction macros,
                                   ImportEvidenceExtraction imports,
                                   ConfiguredPreprocessingEvidence configuredPreprocessing,
                                   SymbolEvidenceExtraction symbols) {
        return seal(root, rawSource, decoded, sourceId, parseStatus, calls,
                conditional, true, macros, imports, configuredPreprocessing, symbols, null);
    }

    public static String sealExact(Node root, byte[] rawSource,
                                   SourceTextCodec.DecodedText decoded,
                                   String sourceId,
                                   String parseStatus,
                                   List<CallEvidenceCandidate> calls,
                                   ConditionalCompilationEvidence conditional,
                                   MacroEvidenceExtraction macros,
                                   ImportEvidenceExtraction imports,
                                   ConfiguredPreprocessingEvidence configuredPreprocessing,
                                   SymbolEvidenceExtraction symbols,
                                   CallableEvidenceExtraction callables) {
        return seal(root, rawSource, decoded, sourceId, parseStatus, calls,
                conditional, true, macros, imports, configuredPreprocessing,
                symbols, callables);
    }

    private static String seal(Node root, byte[] rawSource,
                               SourceTextCodec.DecodedText decoded,
                               String sourceId,
                               String parseStatus,
                               List<CallEvidenceCandidate> calls,
                               ConditionalCompilationEvidence conditional,
                               boolean callSupported,
                               MacroEvidenceExtraction macros,
                               ImportEvidenceExtraction imports,
                               ConfiguredPreprocessingEvidence configuredPreprocessing,
                               SymbolEvidenceExtraction symbols,
                               CallableEvidenceExtraction callables) {
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
            List<String> unresolvedSymbolFactIds = new ArrayList<>();
            List<String> partialCallBindingFactIds = new ArrayList<>();
            List<String> unavailableCallableFactIds = new ArrayList<>();
            List<String> configurationDependentCallableFactIds = new ArrayList<>();
            Map<SourceRangeCandidate, String> symbolDefinitionFactIds = new HashMap<>();
            Map<SourceRangeCandidate, SymbolDefinitionEvidenceCandidate> symbolDefinitions =
                    new HashMap<>();
            List<CallableCandidate> emittedCallables = callables == null
                    ? List.of() : callables.callables();
            List<CallBindingCandidate> emittedBindings = callables == null
                    ? List.of() : callables.bindings();
            if (callables != null && emittedBindings.size() != emittedCalls.size()) {
                throw new IllegalStateException(
                        "call binding population must equal call fact population");
            }
            Map<SourceRangeCandidate, CallableCandidate> callableByNameRange = new HashMap<>();
            List<String> callableIds = precomputedFactIdList(
                    emittedCallables.stream().map(CallableCandidate::range).toList(),
                    normalizedSourceId, decodedHash, "callable");
            Map<SourceRangeCandidate, String> callableFactIds = new HashMap<>();
            Map<SourceRangeCandidate, String> macroFactIds = precomputedFactIds(
                    macros == null ? List.of()
                            : macros.candidates().stream()
                                    .map(MacroEvidenceCandidate::range).toList(),
                    normalizedSourceId, decodedHash, "macro");
            for (int callableIndex = 0; callableIndex < emittedCallables.size();
                    callableIndex++) {
                CallableCandidate callable = emittedCallables.get(callableIndex);
                if (callableByNameRange.put(callable.nameRange(), callable) != null) {
                    throw new IllegalStateException(
                            "duplicate callable name range: " + callable.nameRange());
                }
                callableFactIds.put(callable.nameRange(), callableIds.get(callableIndex));
            }
            String legacyJson = root.toJson();
            if (legacyJson.isEmpty() || legacyJson.charAt(legacyJson.length() - 1) != '}') {
                throw new IllegalStateException("FILE root did not serialize as a JSON object");
            }
            StringWriter output = new StringWriter(legacyJson.length() + source.length());
            output.write(legacyJson, 0, legacyJson.length() - 1);
            output.write(",\"evidence\":");
            try (JsonGenerator generator = JSON.getFactory().createGenerator(output)) {
                generator.writeStartObject();
                generator.writeStringField("version", callables != null
                        ? CALL_BINDING_VERSION
                        : symbols == null ? VERSION : SYMBOL_VERSION);
                generator.writeStringField("sourceId", normalizedSourceId);
                generator.writeStringField("rawSourceSha256", Hashes.sha256(rawSource));
                generator.writeStringField("decodedTextSha256", decodedHash);
                generator.writeStringField("decodedText", source);
                generator.writeStringField("sourceEncoding", decoded.charset());
                generator.writeStringField("decodeStatus", decoded.lossy() ? "lossy" : "exact");
                generator.writeStringField("positionEncoding", "unicode-code-point");
                generator.writeStringField("rangeConvention", "half-open");
                generator.writeStringField("rangeEncoding", "char-offset-length");
                boolean unresolvedSymbols = symbols != null
                        && symbols.unresolvedLookups() > 0;
                generator.writeStringField("parseStatus",
                        (unresolvedRegions > 0 || unresolvedSymbols)
                                && "exact".equals(effectiveParseStatus)
                                        ? "partial" : effectiveParseStatus);
                if (configuredPreprocessing != null) {
                    writeConfiguredPreprocessing(generator, configuredPreprocessing);
                }

                generator.writeArrayFieldStart("facts");
                for (int callIndex = 0; callIndex < emittedCalls.size(); callIndex++) {
                    CallEvidenceCandidate call = emittedCalls.get(callIndex);
                    CallBindingCandidate binding = callables == null
                            ? null : emittedBindings.get(callIndex);
                    ObjectNode fact = sealCall(call, binding, normalizedSourceId,
                            decodedHash, index, ordinals, grammarRules, presences,
                            conditional.presenceAt(call.callRange().startOffset()),
                            callableByNameRange, callableFactIds, macroFactIds);
                    if (binding != null && ("configuration_dependent".equals(binding.status())
                            || "unsupported".equals(binding.status()))) {
                        partialCallBindingFactIds.add(fact.path("factId").asText());
                    }
                    generator.writeTree(fact);
                }
                if (callables != null) {
                    for (CallableCandidate callable : emittedCallables) {
                        ObjectNode fact = sealCallable(callable, normalizedSourceId,
                                decodedHash, index, ordinals, grammarRules, presences,
                                conditional.presenceAt(callable.range().startOffset()));
                        if ("unavailable".equals(callable.compatibilityStatus())) {
                            unavailableCallableFactIds.add(fact.path("factId").asText());
                        }
                        if ("configuration_dependent".equals(
                                callable.definitionStatus())) {
                            configurationDependentCallableFactIds.add(
                                    fact.path("factId").asText());
                        }
                        generator.writeTree(fact);
                    }
                }
                if (imports != null) {
                    for (ImportEvidenceCandidate candidate : imports.candidates()) {
                        generator.writeTree(sealImport(candidate, normalizedSourceId,
                                decodedHash, index, ordinals, grammarRules, presences,
                                conditional.presenceAt(candidate.range().startOffset())));
                    }
                }
                if (macros != null) {
                    for (MacroEvidenceCandidate macro : macros.candidates()) {
                        generator.writeTree(sealMacro(macro, normalizedSourceId, decodedHash,
                                index, ordinals, grammarRules, presences,
                                conditional.presenceAt(macro.range().startOffset())));
                    }
                }
                if (symbols != null) {
                    for (SymbolDefinitionEvidenceCandidate definition : symbols.definitions()) {
                        ObjectNode fact = sealSymbolDefinition(definition, normalizedSourceId,
                                decodedHash, index, ordinals, grammarRules, presences,
                                conditional.presenceAt(definition.range().startOffset()));
                        String previous = symbolDefinitionFactIds.put(
                                definition.range(), fact.path("factId").asText());
                        SymbolDefinitionEvidenceCandidate previousDefinition =
                                symbolDefinitions.put(definition.range(), definition);
                        if (previous != null || previousDefinition != null) {
                            throw new IllegalStateException(
                                    "duplicate C symbol definition range: " + definition.range());
                        }
                        generator.writeTree(fact);
                    }
                    for (SymbolLookupEvidenceCandidate lookup : symbols.lookups()) {
                        ObjectNode fact = sealSymbolLookup(lookup, normalizedSourceId,
                                decodedHash, index, ordinals, grammarRules, presences,
                                conditional.presenceAt(lookup.range().startOffset()),
                                symbolDefinitionFactIds, symbolDefinitions);
                        if ("unresolved".equals(lookup.resolutionStatus())) {
                            unresolvedSymbolFactIds.add(fact.path("factId").asText());
                        }
                        generator.writeTree(fact);
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
                List<String> completenessKinds = callables == null ? KINDS : List.of(
                        "call", "call_binding", "callable", "import", "symbol",
                        "literal", "assignment", "parameter", "macro",
                        "embedded_language", "conditional_region");
                for (String kind : completenessKinds) {
                    int emitted = "call".equals(kind) ? emittedCalls.size()
                            : "call_binding".equals(kind) ? emittedBindings.size()
                            : "callable".equals(kind) ? emittedCallables.size()
                            : "import".equals(kind) && imports != null
                                    ? imports.candidates().size()
                            : "macro".equals(kind) && macros != null
                                    ? macros.candidates().size()
                            : "symbol".equals(kind) && symbols != null
                                    ? symbols.definitions().size() + symbols.lookups().size()
                            : "conditional_region".equals(kind) ? conditional.regions().size() : 0;
                    int unresolved = "macro".equals(kind) && macros != null
                            ? macros.explicitlyUnresolved()
                            : "import".equals(kind) && imports != null
                                    ? imports.explicitlyUnresolved() : 0;
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
                    List<String> importReasons = new ArrayList<>();
                    if (imports != null) {
                        if (decoded.lossy()) importReasons.add("insufficient_lossy_decode");
                        importReasons.addAll(imports.reasons());
                    }
                    String status = "call".equals(kind)
                            ? !callSupported ? "unsupported"
                                    : !callReasons.isEmpty() ? "partial" : "complete"
                            : "macro".equals(kind)
                                    ? macros == null ? "unsupported"
                                            : !macroReasons.isEmpty() ? "partial" : "complete"
                            : "import".equals(kind)
                                    ? imports == null ? "unsupported"
                                            : !importReasons.isEmpty() ? "partial" : "complete"
                            : "symbol".equals(kind)
                                    ? symbols == null ? "unsupported"
                                            : !unresolvedSymbolFactIds.isEmpty()
                                                    ? "partial" : "complete"
                            : "call_binding".equals(kind)
                                    ? callables == null ? "unsupported"
                                            : !partialCallBindingFactIds.isEmpty()
                                                    ? "partial" : "complete"
                            : "callable".equals(kind)
                                    ? callables == null ? "unsupported"
                                            : !unavailableCallableFactIds.isEmpty()
                                                    || !configurationDependentCallableFactIds
                                                            .isEmpty()
                                                    ? "partial" : "complete"
                            : "conditional_region".equals(kind) ? "complete" : "unsupported";
                    List<String> reasons = "call".equals(kind)
                            ? callReasons : "macro".equals(kind) ? macroReasons
                            : "import".equals(kind) ? importReasons
                            : "call_binding".equals(kind)
                                    && !partialCallBindingFactIds.isEmpty()
                                    ? callBindingCompletenessReasons(emittedBindings)
                            : "callable".equals(kind)
                                    ? callableCompletenessReasons(
                                            unavailableCallableFactIds,
                                            configurationDependentCallableFactIds)
                            : "symbol".equals(kind) && !unresolvedSymbolFactIds.isEmpty()
                                    ? List.of("insufficient_type_name_environment")
                                    : List.of();
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
                    if ("symbol".equals(kind) && !unresolvedSymbolFactIds.isEmpty()) {
                        generator.writeArrayFieldStart("unresolvedFactIds");
                        for (String factId : unresolvedSymbolFactIds) {
                            generator.writeString(factId);
                        }
                        generator.writeEndArray();
                    }
                    if ("call_binding".equals(kind)
                            && !partialCallBindingFactIds.isEmpty()) {
                        generator.writeArrayFieldStart("unresolvedFactIds");
                        for (String factId : partialCallBindingFactIds) {
                            generator.writeString(factId);
                        }
                        generator.writeEndArray();
                    }
                    if ("callable".equals(kind)
                            && (!unavailableCallableFactIds.isEmpty()
                                    || !configurationDependentCallableFactIds.isEmpty())) {
                        generator.writeArrayFieldStart("unresolvedFactIds");
                        java.util.LinkedHashSet<String> factIds =
                                new java.util.LinkedHashSet<>(unavailableCallableFactIds);
                        factIds.addAll(configurationDependentCallableFactIds);
                        for (String factId : factIds) {
                            generator.writeString(factId);
                        }
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
                conditional, callSupported, null, null, null, null, null);
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
                                       CallBindingCandidate binding,
                                       String sourceId,
                                       String decodedHash,
                                       CodePointIndex index,
                                       Map<String, Integer> ordinals,
                                       Map<String, Integer> grammarRules,
                                       Map<Presence, Integer> presences,
                                       Presence factPresence,
                                       Map<SourceRangeCandidate, CallableCandidate> callables,
                                       Map<SourceRangeCandidate, String> callableFactIds,
                                       Map<SourceRangeCandidate, String> macroFactIds) {
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
        if (binding != null && !candidate.callRange().equals(binding.callRange())) {
            throw new IllegalArgumentException(
                    "call binding range does not match its call fact");
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
        if (binding != null) {
            ObjectNode encoded = payload.putObject("binding");
            encoded.put("adapterSchema", binding.adapterSchema());
            encoded.put("status", binding.status());
            encoded.put("resolutionMode", binding.resolutionMode());

            CallableCandidate declaration = null;
            String declarationFactId = null;
            String bindingKey = null;
            String compatibilityKey = null;
            String compatibilityScope = null;
            if (binding.declarationNameRange() != null) {
                declaration = callables.get(binding.declarationNameRange());
                declarationFactId = callableFactIds.get(binding.declarationNameRange());
                if (declaration == null || declarationFactId == null) {
                    throw new IllegalStateException(
                            "call binding references a missing callable declaration");
                }
                if (!binding.adapterSchema().equals(declaration.adapterSchema())
                        || !binding.targetScope().equals(declaration.targetScope())
                        || !java.util.Objects.equals(
                                binding.configurationId(), declaration.configurationId())) {
                    throw new IllegalStateException(
                            "call binding contradicts its callable declaration");
                }
                boolean directDefinition = "direct_definition".equals(
                        binding.resolutionMode());
                boolean compatibleDefinition = "compatible_definition".equals(
                        binding.resolutionMode());
                if (directDefinition
                        && (!("definition".equals(declaration.role()))
                                || !("exact".equals(declaration.definitionStatus())))) {
                    throw new IllegalStateException(
                            "direct call binding must reference an exact definition");
                }
                if (compatibleDefinition
                        && (!("declaration".equals(declaration.role()))
                                || "unavailable".equals(
                                        declaration.compatibilityStatus()))) {
                    throw new IllegalStateException(
                            "compatible call binding must reference an exact declaration type");
                }
                bindingKey = bindingKey(declaration, sourceId);
                compatibilityKey = compatibilityKey(declaration, sourceId);
                compatibilityScope = declaration.compatibilityScope();
                if (compatibleDefinition && compatibilityKey == null) {
                    throw new IllegalStateException(
                            "bound callable has no compatibility identity");
                }
            }
            putNullable(encoded, "declarationFactId", declarationFactId);
            putNullable(encoded, "bindingKey", bindingKey);
            putNullable(encoded, "compatibilityKey", compatibilityKey);
            putNullable(encoded, "compatibilityScope", compatibilityScope);
            encoded.put("targetScope", binding.targetScope());
            encoded.put("dispatch", binding.dispatch());
            putNullable(encoded, "configurationId", binding.configurationId());
            encoded.putNull("externalIdentityKey");
            ArrayNode candidateFactIds = encoded.putArray("candidateFactIds");
            for (SourceRangeCandidate range : binding.candidateMacroRanges()) {
                String factId = macroFactIds.get(range);
                if (factId == null) {
                    throw new IllegalStateException(
                            "call binding references a missing macro fact");
                }
                candidateFactIds.add(factId);
            }
            for (SourceRangeCandidate range : binding.candidateCallableRanges()) {
                String factId = callableFactIds.get(range);
                if (factId == null) {
                    throw new IllegalStateException(
                            "call binding references a missing callable fact");
                }
                candidateFactIds.add(factId);
            }
            encoded.put("provenance", binding.provenance());
            putNullable(encoded, "reason", binding.reason());
        }
        return fact;
    }

    private static ObjectNode sealCallable(CallableCandidate candidate,
                                           String sourceId,
                                           String decodedHash,
                                           CodePointIndex index,
                                           Map<String, Integer> ordinals,
                                           Map<String, Integer> grammarRules,
                                           Map<Presence, Integer> presences,
                                           Presence factPresence) {
        index.requireValid(candidate.range());
        index.requireSubrange(candidate.range(), candidate.nameRange(), "callable name");
        index.requireValid(candidate.visibilityRange());
        if (candidate.visibilityStartOffset() < candidate.nameRange().endOffset()
                || candidate.visibilityStartOffset()
                        > candidate.visibilityRange().endOffset()) {
            throw new IllegalArgumentException("invalid callable visibility boundary");
        }
        if (candidate.astNodeRange() != null) {
            index.requireValid(candidate.astNodeRange());
            if (!candidate.range().equals(candidate.astNodeRange())) {
                throw new IllegalArgumentException(
                        "callable definition range must equal its AST node range");
            }
        }

        String ordinalKey = "callable\0" + candidate.range().startOffset()
                + "\0" + candidate.range().endOffset();
        int ordinal = ordinals.merge(ordinalKey, 1, Integer::sum) - 1;
        ObjectNode fact = JSON.createObjectNode();
        fact.put("factId", factId(sourceId, decodedHash, "callable",
                candidate.range(), ordinal));
        fact.put("kind", "callable");
        fact.set("range", index.rangeJson(candidate.range()));
        fact.put("grammarRuleRef", reference(grammarRules, candidate.grammarRule()));
        fact.put("presenceRef", reference(presences, factPresence));

        ObjectNode payload = fact.putObject("payload");
        payload.put("role", candidate.role());
        payload.set("nameRange", index.rangeJson(candidate.nameRange()));
        payload.put("adapterSchema", candidate.adapterSchema());
        payload.put("bindingKey", bindingKey(candidate, sourceId));
        putNullable(payload, "compatibilityKey", compatibilityKey(candidate, sourceId));
        payload.put("compatibilityStatus", candidate.compatibilityStatus());
        payload.put("compatibilityScope", candidate.compatibilityScope());
        payload.put("definitionStatus", candidate.definitionStatus());
        payload.put("targetScope", candidate.targetScope());
        putNullable(payload, "configurationId", candidate.configurationId());
        if (candidate.astNodeRange() == null) payload.putNull("astNodeRange");
        else payload.set("astNodeRange", index.rangeJson(candidate.astNodeRange()));
        payload.set("visibilityRange", index.rangeJson(candidate.visibilityRange()));
        payload.put("visibilityStartOffset", candidate.visibilityStartOffset());
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

    private static ObjectNode sealSymbolDefinition(
            SymbolDefinitionEvidenceCandidate candidate,
            String sourceId,
            String decodedHash,
            CodePointIndex index,
            Map<String, Integer> ordinals,
            Map<String, Integer> grammarRules,
            Map<Presence, Integer> presences,
            Presence factPresence) {
        index.requireValid(candidate.range());
        index.requireValid(candidate.scopeRange());
        index.requireSubrange(candidate.scopeRange(), candidate.range(),
                "symbol definition");
        String ordinalKey = "symbol\0" + candidate.range().startOffset()
                + "\0" + candidate.range().endOffset();
        int ordinal = ordinals.merge(ordinalKey, 1, Integer::sum) - 1;
        ObjectNode fact = JSON.createObjectNode();
        fact.put("factId", factId(sourceId, decodedHash, "symbol",
                candidate.range(), ordinal));
        fact.put("kind", "symbol");
        fact.set("range", index.rangeJson(candidate.range()));
        fact.put("grammarRuleRef", reference(grammarRules, candidate.grammarRule()));
        fact.put("presenceRef", reference(presences, factPresence));
        ObjectNode payload = fact.putObject("payload");
        payload.put("role", "definition");
        payload.put("symbolKind", candidate.symbolKind());
        payload.put("scopeKind", candidate.scopeKind());
        payload.set("scopeRange", index.rangeJson(candidate.scopeRange()));
        payload.put("visibilityStartOffset", candidate.visibilityStartOffset());
        return fact;
    }

    private static ObjectNode sealSymbolLookup(
            SymbolLookupEvidenceCandidate candidate,
            String sourceId,
            String decodedHash,
            CodePointIndex index,
            Map<String, Integer> ordinals,
            Map<String, Integer> grammarRules,
            Map<Presence, Integer> presences,
            Presence factPresence,
            Map<SourceRangeCandidate, String> definitionFactIds,
            Map<SourceRangeCandidate, SymbolDefinitionEvidenceCandidate> definitions) {
        index.requireValid(candidate.range());
        String ordinalKey = "symbol\0" + candidate.range().startOffset()
                + "\0" + candidate.range().endOffset();
        int ordinal = ordinals.merge(ordinalKey, 1, Integer::sum) - 1;
        ObjectNode fact = JSON.createObjectNode();
        fact.put("factId", factId(sourceId, decodedHash, "symbol",
                candidate.range(), ordinal));
        fact.put("kind", "symbol");
        fact.set("range", index.rangeJson(candidate.range()));
        fact.put("grammarRuleRef", reference(grammarRules, candidate.grammarRule()));
        fact.put("presenceRef", reference(presences, factPresence));
        ObjectNode payload = fact.putObject("payload");
        payload.put("role", "lookup");
        payload.put("lookupKind", "type_name");
        payload.put("parserDecision", candidate.parserDecision());
        payload.put("resolutionStatus", candidate.resolutionStatus());
        payload.put("provenance", candidate.provenance());
        if (candidate.definitionRange() == null) {
            payload.putNull("definitionFactId");
        } else {
            String definitionFactId = definitionFactIds.get(candidate.definitionRange());
            SymbolDefinitionEvidenceCandidate definition =
                    definitions.get(candidate.definitionRange());
            if (definitionFactId == null || definition == null
                    || candidate.range().startOffset() < definition.visibilityStartOffset()
                    || candidate.range().startOffset() >= definition.scopeRange().endOffset()) {
                throw new IllegalStateException(
                        "C symbol lookup does not resolve to a visible definition");
            }
            payload.put("definitionFactId", definitionFactId);
        }
        if (candidate.configuredEvidenceId() == null) {
            payload.putNull("configuredEvidenceId");
        } else {
            payload.put("configuredEvidenceId", candidate.configuredEvidenceId());
        }
        ArrayNode contexts = payload.putArray("predicateContexts");
        candidate.predicateContexts().forEach(contexts::add);
        return fact;
    }

    private static ObjectNode sealImport(ImportEvidenceCandidate candidate,
                                         String sourceId,
                                         String decodedHash,
                                         CodePointIndex index,
                                         Map<String, Integer> ordinals,
                                         Map<String, Integer> grammarRules,
                                         Map<Presence, Integer> presences,
                                         Presence factPresence) {
        index.requireValid(candidate.range());
        String ordinalKey = "import\0" + candidate.range().startOffset()
                + "\0" + candidate.range().endOffset();
        int ordinal = ordinals.merge(ordinalKey, 1, Integer::sum) - 1;
        ObjectNode fact = JSON.createObjectNode();
        fact.put("factId", factId(sourceId, decodedHash, "import", candidate.range(), ordinal));
        fact.put("kind", "import");
        fact.set("range", index.rangeJson(candidate.range()));
        fact.put("grammarRuleRef", reference(grammarRules, candidate.grammarRule()));
        fact.put("presenceRef", reference(presences, factPresence));
        ObjectNode payload = fact.putObject("payload");
        payload.put("directiveKind", candidate.directiveKind());
        ArrayNode entries = payload.putArray("entries");
        for (ImportBindingCandidate entry : candidate.entries()) {
            index.requireSubrange(candidate.range(), entry.targetRange(), "import target");
            ObjectNode encoded = entries.addObject();
            encoded.put("importKind", entry.importKind());
            encoded.put("targetKind", entry.targetKind());
            encoded.set("targetRange", index.rangeJson(entry.targetRange()));
            ArrayNode components = encoded.putArray("pathComponentRanges");
            for (SourceRangeCandidate component : entry.pathComponentRanges()) {
                index.requireSubrange(candidate.range(), component, "import path component");
                components.add(index.rangeJson(component));
            }
            if (entry.memberRange() == null) encoded.putNull("memberRange");
            else {
                index.requireSubrange(candidate.range(), entry.memberRange(), "import member");
                encoded.set("memberRange", index.rangeJson(entry.memberRange()));
            }
            if (entry.aliasRange() == null) encoded.putNull("aliasRange");
            else {
                index.requireSubrange(candidate.range(), entry.aliasRange(), "import alias");
                encoded.set("aliasRange", index.rangeJson(entry.aliasRange()));
            }
            encoded.put("relativeLevel", entry.relativeLevel());
            encoded.put("wildcard", entry.wildcard());
            encoded.put("locality", entry.locality());
        }
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

    private static List<String> precomputedFactIdList(
            List<SourceRangeCandidate> ranges,
            String sourceId,
            String decodedHash,
            String kind) {
        Map<SourceRangeCandidate, Integer> ordinals = new HashMap<>();
        List<String> result = new ArrayList<>(ranges.size());
        for (SourceRangeCandidate range : ranges) {
            int ordinal = ordinals.merge(range, 1, Integer::sum) - 1;
            result.add(factId(sourceId, decodedHash, kind, range, ordinal));
        }
        return List.copyOf(result);
    }

    private static List<String> callableCompletenessReasons(
            List<String> unavailableCompatibility,
            List<String> configurationDependentDefinitions) {
        List<String> reasons = new ArrayList<>(2);
        if (!unavailableCompatibility.isEmpty()) {
            reasons.add("insufficient_callable_type_compatibility");
        }
        if (!configurationDependentDefinitions.isEmpty()) {
            reasons.add("insufficient_inline_definition_configuration");
        }
        return List.copyOf(reasons);
    }

    private static List<String> callBindingCompletenessReasons(
            List<CallBindingCandidate> bindings) {
        boolean configurationDependent = bindings.stream()
                .anyMatch(item -> "configuration_dependent".equals(item.status()));
        boolean unsupported = bindings.stream()
                .anyMatch(item -> "unsupported".equals(item.status()));
        List<String> reasons = new ArrayList<>(2);
        if (configurationDependent) {
            reasons.add("insufficient_call_binding_configuration");
        }
        if (unsupported) reasons.add("insufficient_call_binding_capability");
        return List.copyOf(reasons);
    }

    private static Map<SourceRangeCandidate, String> precomputedFactIds(
            List<SourceRangeCandidate> ranges,
            String sourceId,
            String decodedHash,
            String kind) {
        List<String> ids = precomputedFactIdList(ranges, sourceId, decodedHash, kind);
        Map<SourceRangeCandidate, String> result = new HashMap<>();
        for (int index = 0; index < ranges.size(); index++) {
            if (result.put(ranges.get(index), ids.get(index)) != null) {
                throw new IllegalStateException(
                        "duplicate " + kind + " range cannot be referenced exactly: "
                                + ranges.get(index));
            }
        }
        return result;
    }

    private static String bindingKey(CallableCandidate candidate, String sourceId) {
        String sourceScope = "source_file".equals(candidate.targetScope())
                ? sourceId : "";
        String tuple = String.join("\0", BINDING_ID_DOMAIN,
                candidate.adapterSchema(), candidate.targetScope(), sourceScope,
                candidate.terminalName());
        return sha256(tuple.getBytes(StandardCharsets.UTF_8));
    }

    private static String compatibilityKey(
            CallableCandidate candidate, String sourceId) {
        if (candidate.compatibilityMaterial() == null) return null;
        String sourceScope = "source_file".equals(candidate.compatibilityScope())
                ? sourceId : "";
        String tuple = String.join("\0", COMPATIBILITY_ID_DOMAIN,
                candidate.adapterSchema(), candidate.compatibilityScope(), sourceScope,
                candidate.compatibilityMaterial());
        return sha256(tuple.getBytes(StandardCharsets.UTF_8));
    }

    private static void putNullable(ObjectNode object, String field, String value) {
        if (value == null) object.putNull(field);
        else object.put(field, value);
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
