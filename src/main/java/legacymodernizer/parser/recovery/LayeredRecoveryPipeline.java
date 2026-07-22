package legacymodernizer.parser.recovery;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import legacymodernizer.parser.parsing.languages.LanguageModule;
import legacymodernizer.parser.recovery.repair.FailureEnvelope;
import legacymodernizer.parser.recovery.repair.FailureEnvelopeFactory;
import legacymodernizer.parser.recovery.repair.PatchProposal;
import legacymodernizer.parser.recovery.repair.PatchProposalValidator;
import legacymodernizer.parser.recovery.repair.RepairAgent;
import legacymodernizer.parser.recovery.repair.RepairAgentException;
import legacymodernizer.parser.recovery.quality.QualityDecision;
import legacymodernizer.parser.recovery.quality.QualityStatus;
import legacymodernizer.parser.parsing.RawParseResult;
import legacymodernizer.parser.recovery.diagnostics.ParseDiagnostic;
import legacymodernizer.parser.recovery.candidates.GrammarGuidedEditEngine;
import legacymodernizer.parser.recovery.candidates.TokenEditCandidate;
import legacymodernizer.parser.recovery.evidence.AgentRequestEvidence;
import legacymodernizer.parser.recovery.evidence.RecoveryAttemptEvidence;
import legacymodernizer.parser.recovery.evidence.RecoveryOutcome;
import legacymodernizer.parser.recovery.localization.ContextSlice;
import legacymodernizer.parser.recovery.localization.ErrorSpanLocator;
import legacymodernizer.parser.recovery.localization.SliceLevel;
import legacymodernizer.parser.recovery.localization.SliceSyntax;
import legacymodernizer.parser.recovery.orchestration.TokenBudgetSemaphore;
import legacymodernizer.parser.recovery.boundaries.SourceUnit;
import legacymodernizer.parser.recovery.boundaries.UnitKind;
import legacymodernizer.parser.recovery.boundaries.UnitParseRequest;
import legacymodernizer.parser.recovery.boundaries.UnitParseContext;
import legacymodernizer.parser.recovery.evidence.UnitRecoveryEvidence;
import legacymodernizer.parser.recovery.quality.ParseQualityGate;
import legacymodernizer.parser.recovery.rules.RecoveryRule;
import legacymodernizer.parser.recovery.rules.RecoveryRuleProposal;
import legacymodernizer.parser.recovery.rules.RecoveryRuleRegistry;
import legacymodernizer.parser.recovery.workingcopy.WorkingCopy;
import legacymodernizer.parser.recovery.workingcopy.Hashes;
import legacymodernizer.parser.recovery.workingcopy.TextEdit;
import legacymodernizer.parser.service.ParseProgressTracker;

@Service
public final class LayeredRecoveryPipeline {

    private final ParseQualityGate qualityGate;
    private final RecoveryRuleRegistry ruleRegistry;
    private final RepairAgent repairAgent;
    private final FailureEnvelopeFactory envelopeFactory = new FailureEnvelopeFactory();
    private final PatchProposalValidator proposalValidator = new PatchProposalValidator();
    private final GrammarGuidedEditEngine editEngine = new GrammarGuidedEditEngine();
    // Shared across all files/units of this pipeline instance so concurrent Agent traffic is
    // bounded by prompt characters in flight, not by thread count (FR-053).
    private final TokenBudgetSemaphore agentBudget = new TokenBudgetSemaphore(
            Long.getLong("parser.repair.agent.budget.chars", 200_000L));
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public LayeredRecoveryPipeline(ParseQualityGate qualityGate, RecoveryRuleRegistry ruleRegistry,
                               RepairAgent repairAgent) {
        this.qualityGate = qualityGate;
        this.ruleRegistry = ruleRegistry;
        this.repairAgent = repairAgent;
    }

    public LayeredRecoveryPipeline(ParseQualityGate qualityGate, RecoveryRuleRegistry ruleRegistry) {
        this(qualityGate, ruleRegistry, RepairAgent.disabled());
    }

    public RecoveryOutcome recover(LanguageModule module, Path sourceFile, Path sourceRoot,
                                   RawParseResult firstPass, QualityDecision firstDecision,
                                   ParseProgressTracker tracker) throws Exception {
        if (firstDecision.status() == QualityStatus.EXACT) {
            return new RecoveryOutcome(firstPass.astJson(), firstDecision, List.of(), 0, 0, 0);
        }
        if (!module.supportsUnitParsing()) {
            return unresolved(firstDecision, "MINIMAL_UNIT_PARSER_UNAVAILABLE");
        }

        String source = readText(sourceFile);
        String relativePath = sourceRoot.relativize(sourceFile).toString().replace('\\', '/');
        String fileSha256 = Hashes.sha256(Files.readAllBytes(sourceFile));

        // File-level repair runs BEFORE unit salvage: a defect outside every unit boundary
        // (e.g. a broken global declaration) can only be fixed here, and a whole-file repair
        // preserves full fidelity — comments and non-unit regions included. Mutation
        // benchmarking surfaced both gaps (2026-07-22).
        tracker.repairProgress("repair_started",
                "🛠 문법 오류가 발견되어 자동 복구를 시작해요");
        RecoveryOutcome wholeFile = repairWholeFile(module, sourceFile, relativePath,
                fileSha256, source, firstPass, firstDecision, tracker);
        if (wholeFile != null) {
            tracker.repairProgress("repair_whole_file_adopted",
                    "✅ 파일 전체를 원문 그대로 보존하며 복구했어요");
            return wholeFile;
        }

        List<SourceUnit> units = module.locateRecoveryUnits(source, firstPass);
        if (units.size() == 1 && units.get(0).kind() == UnitKind.FILE) {
            return unresolved(firstDecision, "MINIMAL_UNIT_BOUNDARY_UNAVAILABLE");
        }

        ObjectNode firstRoot = (ObjectNode) objectMapper.readTree(firstPass.astJson());
        List<JsonNode> firstChildren = elements(firstRoot.path("children"));
        // A column-0 defect can fool a text locator into ending a unit early; the severed
        // body then parses as top-level orphans just past the boundary and the truncated
        // unit alone reparses "clean" (mutation grading, 2026-07-22). Signature: a diagnostic
        // in the gap after a unit PLUS first-pass children inside that gap. Expand such units
        // to the next unit so the damage stays inside and fails or repairs honestly.
        units = expandUnitsCutByGapDamage(units, firstPass, firstChildren, source);
        List<JsonNode> acceptedChildren = new ArrayList<>();
        List<UnitRecoveryEvidence> evidence = new ArrayList<>();
        int reused = 0;
        int recovered = 0;
        int unresolved = 0;
        boolean usedSafeRule = false;
        boolean hasReviewRequired = false;
        java.util.Set<String> failedUnitIds = new java.util.HashSet<>();

        for (SourceUnit unit : units) {
            List<JsonNode> reusable = childrenWithin(firstChildren, unit);
            boolean intersectsError = firstPass.diagnostics().stream()
                    .anyMatch(diagnostic -> diagnostic.line() >= unit.startLine()
                            && diagnostic.line() <= unit.endLine());
            if (!intersectsError && reusableIsStructurallyPlausible(reusable, unit)) {
                acceptedChildren.addAll(reusable);
                reused++;
                evidence.add(new UnitRecoveryEvidence(unit, QualityStatus.EXACT, true,
                        reusable.size(), List.of()));
                continue;
            }

            String unitText = source.substring(unit.startOffset(), unit.endOffset());
            RawParseResult attempt = module.parseUnit(
                    new UnitParseRequest(unitText, sourceFile.getFileName().toString(), relativePath, unit),
                    tracker);
            QualityDecision decision = qualityGate.evaluateFirstPass(attempt);
            List<JsonNode> unitChildren = elements(objectMapper.readTree(attempt.astJson()).path("children"));
            List<RecoveryAttemptEvidence> attempts = new ArrayList<>();
            RecoveryAttemptEvidence attemptEvidence = new RecoveryAttemptEvidence(
                    "MINIMAL_UNIT_EXACT", 1, attempt.sourceSha256(), attempt.diagnostics(),
                    attempt.antlrRecoveries(), attempt.coverage(), attempt.elapsedMillis(), decision.reasons(),
                    decision.qualityTuple(), attempt.sourceSha256(), null, null);
            attempts.add(attemptEvidence);
            if (decision.accepted() && !unitChildren.isEmpty()) {
                acceptedChildren.addAll(unitChildren);
                recovered++;
                evidence.add(new UnitRecoveryEvidence(unit, QualityStatus.RECOVERED_VALIDATED,
                        true, unitChildren.size(), List.copyOf(attempts)));
                continue;
            }

            boolean acceptedByRule = false;
            int attemptNumber = 1;
            List<ContextSourceCandidate> contextSources = new ArrayList<>();
            contextSources.add(new ContextSourceCandidate(unitText, null, null));
            for (RecoveryRule rule : ruleRegistry.forModule(module)) {
                RecoveryRuleProposal proposal = rule.propose(unitText, unit, attempt);
                if (!proposal.safe() || proposal.ambiguous() || proposal.edits().isEmpty()) continue;

                WorkingCopy workingCopy;
                try {
                    workingCopy = WorkingCopy.exact(unitText).applyOriginalEdits(proposal.edits());
                } catch (IllegalArgumentException invalidRuleEdit) {
                    continue;
                }
                if (!workingCopy.sourceMap().preservesLineCount()) continue;

                RawParseResult candidate = module.parseUnit(
                        new UnitParseRequest(workingCopy.workingText(), sourceFile.getFileName().toString(),
                                relativePath, unit), tracker);
                QualityDecision candidateDecision = qualityGate.evaluateFirstPass(candidate);
                List<JsonNode> candidateChildren = elements(
                        objectMapper.readTree(candidate.astJson()).path("children"));
                attempts.add(new RecoveryAttemptEvidence(
                        "SAFE_RULE", ++attemptNumber, attempt.sourceSha256(), candidate.diagnostics(),
                        candidate.antlrRecoveries(), candidate.coverage(), candidate.elapsedMillis(),
                        candidateDecision.reasons(), candidateDecision.qualityTuple(),
                        workingCopy.workingSha256(), proposal.ruleId(),
                        workingCopy.unifiedDiff(relativePath + "#" + unit.unitId()),
                        workingCopy.edits(), workingCopy.sourceMap().summary()));

                contextSources.add(new ContextSourceCandidate(
                        workingCopy.workingText(), proposal.ruleId(), workingCopy));

                if (candidateDecision.accepted()
                        && isStrictlyBetter(candidateDecision, decision)
                        && !candidateChildren.isEmpty()) {
                    acceptedChildren.addAll(candidateChildren);
                    recovered++;
                    usedSafeRule = true;
                    evidence.add(new UnitRecoveryEvidence(unit, QualityStatus.RECOVERED_SAFE,
                            true, candidateChildren.size(), List.copyOf(attempts)));
                    acceptedByRule = true;
                    break;
                }
            }

            boolean acceptedByContext = false;
            if (!acceptedByRule) {
                for (ContextSourceCandidate contextSource : contextSources) {
                    try {
                        for (UnitParseContext context : module.reconstructUnitContexts(
                                source, unit, contextSource.sourceText())) {
                            RawParseResult candidate = module.parseUnit(
                                    new UnitParseRequest(context.sourceText(),
                                            sourceFile.getFileName().toString(), relativePath, unit,
                                            context.leadingContextLines()), tracker);
                            QualityDecision candidateDecision = qualityGate.evaluateFirstPass(candidate);
                            List<JsonNode> candidateChildren = childrenWithin(elements(
                                    objectMapper.readTree(candidate.astJson()).path("children")), unit);
                            attempts.add(new RecoveryAttemptEvidence(
                                    "CONTEXT_RECONSTRUCTION", ++attemptNumber, attempt.sourceSha256(),
                                    candidate.diagnostics(), candidate.antlrRecoveries(),
                                    candidate.coverage(), candidate.elapsedMillis(),
                                    candidateDecision.reasons(), candidateDecision.qualityTuple(),
                                    candidate.sourceSha256(), contextRuleId(context, contextSource),
                                    contextSource.workingCopy() == null ? null
                                            : contextSource.workingCopy().unifiedDiff(
                                                    relativePath + "#" + unit.unitId()),
                                    contextSource.workingCopy() == null ? List.of()
                                            : contextSource.workingCopy().edits(),
                                    contextSource.workingCopy() == null ? null
                                            : contextSource.workingCopy().sourceMap().summary()));
                            if (candidateDecision.accepted()
                                    && isStrictlyBetter(candidateDecision, decision)
                                    && !candidateChildren.isEmpty()) {
                                acceptedChildren.addAll(candidateChildren);
                                recovered++;
                                usedSafeRule |= contextSource.ruleId() != null;
                                evidence.add(new UnitRecoveryEvidence(unit,
                                        contextSource.ruleId() == null
                                                ? QualityStatus.RECOVERED_VALIDATED
                                                : QualityStatus.RECOVERED_SAFE,
                                        true,
                                        candidateChildren.size(), List.copyOf(attempts)));
                                acceptedByContext = true;
                                break;
                            }
                        }
                    } catch (Exception contextFailure) {
                        String reason = "CONTEXT_RECONSTRUCTION_FAILED:"
                                + contextFailure.getClass().getSimpleName();
                        attempts.add(new RecoveryAttemptEvidence(
                                "CONTEXT_RECONSTRUCTION", ++attemptNumber,
                                attempt.sourceSha256(), attempt.diagnostics(),
                                attempt.antlrRecoveries(), attempt.coverage(), 0,
                                List.of(reason), decision.qualityTuple(),
                                attempt.sourceSha256(), reason, null));
                    }
                    if (acceptedByContext) break;
                }
            }

            // Deterministic grammar-guided repair runs before any Agent call (FR-033) as a
            // wave loop (FR-050..052): each wave fixes the first root cause on the current
            // working text, then reparses so cascading diagnostics disappear. Waves accept
            // strict improvements; the unit is adopted only when a wave ends completely clean.
            // Two equally good survivors in one wave are an ambiguity → REVIEW_REQUIRED
            // without consulting the Agent (FR-040).
            EngineWaveResult engineResult = !acceptedByRule && !acceptedByContext
                    && !attempt.diagnostics().isEmpty()
                    ? runEngineWaves(module, unit, unitText, sourceFile, relativePath,
                            attempt, decision, attempts, attemptNumber, tracker)
                    : new EngineWaveResult(null, false, attemptNumber);
            attemptNumber = engineResult.attemptNumber();
            boolean acceptedByEngine = false;
            boolean engineAmbiguous = engineResult.ambiguous();
            if (!acceptedByRule && !acceptedByContext && engineResult.cleanChildren() != null) {
                acceptedChildren.addAll(engineResult.cleanChildren());
                recovered++;
                evidence.add(new UnitRecoveryEvidence(unit, QualityStatus.RECOVERED_VALIDATED,
                        true, engineResult.cleanChildren().size(), List.copyOf(attempts)));
                acceptedByEngine = true;
                tracker.repairProgress("repair_unit_engine_adopted",
                        "✅ " + displayName(unit) + " 구간의 문법 오류를 자동으로 고쳤어요");
            }

            boolean acceptedByAgent = false;
            boolean unitReviewRequired = engineAmbiguous;
            if (!acceptedByRule && !acceptedByContext && !acceptedByEngine && !engineAmbiguous) {
                if (!repairAgent.enabled()) {
                    attempts.add(agentSkippedEvidence(++attemptNumber, attempt, decision,
                            "REPAIR_AGENT_DISABLED"));
                    unitReviewRequired = true;
                } else {
                    AgentLadderResult ladder = runAgentLadder(module, unit, unitText, sourceFile,
                            relativePath, fileSha256, attempt, decision, attempts, attemptNumber,
                            tracker);
                    attemptNumber = ladder.attemptNumber();
                    unitReviewRequired |= ladder.reviewRequired();
                    if (ladder.children() != null) {
                        acceptedChildren.addAll(ladder.children());
                        recovered++;
                        evidence.add(new UnitRecoveryEvidence(unit,
                                QualityStatus.RECOVERED_VALIDATED, true,
                                ladder.children().size(), List.copyOf(attempts)));
                        acceptedByAgent = true;
                    } else {
                        unitReviewRequired = true;
                    }
                }
            }
            if (!acceptedByRule && !acceptedByContext && !acceptedByEngine && !acceptedByAgent) {
                unresolved++;
                hasReviewRequired |= unitReviewRequired;
                failedUnitIds.add(unit.unitId());
                evidence.add(new UnitRecoveryEvidence(unit,
                        unitReviewRequired ? QualityStatus.REVIEW_REQUIRED : QualityStatus.UNRESOLVED,
                        false, 0, List.copyOf(attempts)));
                tracker.repairReviewRequired("repair_unit_review_required",
                        "⚠️ " + displayName(unit)
                                + " 구간은 자동 복구가 불확실해 검토 대상으로 남겼어요");
            }
        }

        // Shadow of every failed unit: from its start to the next unit's start. A defect at
        // column 0 can shrink the locator's unit boundary AND make ANTLR error recovery
        // scatter the unit's real body as top-level orphans just past that boundary — those
        // orphans masquerade as valid non-unit content (mutation grading caught a Python def
        // body hoisted to file scope, 2026-07-22). Nothing inside a failed unit's shadow is
        // trustworthy.
        List<int[]> failedShadows = new ArrayList<>();
        for (int index = 0; index < units.size(); index++) {
            if (!failedUnitIds.contains(units.get(index).unitId())) continue;
            int shadowStart = units.get(index).startLine();
            int shadowEnd = index + 1 < units.size()
                    ? units.get(index + 1).startLine() - 1 : Integer.MAX_VALUE;
            failedShadows.add(new int[]{shadowStart, Math.max(shadowStart, shadowEnd)});
        }

        int droppedOutsideUnits = 0;
        for (JsonNode child : firstChildren) {
            int start = child.path("startLine").asInt(-1);
            int end = child.path("endLine").asInt(start);
            boolean overlapsUnit = units.stream().anyMatch(unit -> start <= unit.endLine()
                    && end >= unit.startLine());
            boolean intersectsError = firstPass.diagnostics().stream().anyMatch(diagnostic ->
                    diagnostic.line() >= start && diagnostic.line() <= end)
                    || failedShadows.stream().anyMatch(shadow ->
                            start <= shadow[1] && end >= shadow[0]);
            if (!overlapsUnit && start > 0) {
                if (!intersectsError) {
                    acceptedChildren.add(child);
                } else {
                    // Content outside every unit (e.g. C globals) whose region carries the
                    // error: dropping it silently while reporting a fully validated recovery
                    // would hide data loss — the file must honestly grade as PARTIAL.
                    droppedOutsideUnits++;
                }
            }
        }

        acceptedChildren.sort(Comparator
                .comparingInt((JsonNode child) -> child.path("startLine").asInt(Integer.MAX_VALUE))
                .thenComparingInt(child -> child.path("endLine").asInt(Integer.MAX_VALUE)));
        ArrayNode children = objectMapper.createArrayNode();
        acceptedChildren.forEach(children::add);
        firstRoot.set("children", children);

        boolean hasAst = !acceptedChildren.isEmpty();
        // Parse-independent loss guard: lines the (error-recovered) first pass attributed to
        // some node must still be attributed after salvage. ANTLR can mangle content near an
        // error into a neighbouring node; when unit reparse then rebuilds that node correctly,
        // the mangled-in lines silently vanish — found by mutation benchmarking (2026-07-22).
        java.util.BitSet coveredBefore = lineCoverage(firstChildren);
        java.util.BitSet coveredAfter = lineCoverage(acceptedChildren);
        coveredBefore.andNot(coveredAfter);
        int lostLines = coveredBefore.cardinality();
        // A first pass with zero diagnostics was rejected for non-syntax reasons (e.g.
        // coverage); reusing every unit untouched reproduces the identical content, and
        // relabelling it as a validated recovery would launder that rejection (adversarial
        // audit, 2026-07-22). Files whose diagnostics all lay outside units keep the
        // established salvage semantics.
        boolean launderedReuse = recovered == 0 && !usedSafeRule
                && firstPass.diagnostics().isEmpty();
        boolean fullyRecovered = unresolved == 0 && droppedOutsideUnits == 0 && lostLines == 0
                && !launderedReuse;
        QualityStatus status = fullyRecovered && hasAst
                ? usedSafeRule ? QualityStatus.RECOVERED_SAFE : QualityStatus.RECOVERED_VALIDATED
                : hasAst ? QualityStatus.PARTIAL
                : hasReviewRequired ? QualityStatus.REVIEW_REQUIRED : QualityStatus.UNRESOLVED;
        List<String> reasons = new ArrayList<>();
        if (fullyRecovered) {
            reasons.add("MINIMAL_UNIT_RECOVERY");
        }
        if (unresolved > 0) reasons.add("UNRESOLVED_UNITS=" + unresolved);
        if (droppedOutsideUnits > 0) {
            reasons.add("DROPPED_NON_UNIT_REGIONS=" + droppedOutsideUnits);
        }
        if (lostLines > 0) reasons.add("COVERAGE_SHRUNK_LINES=" + lostLines);
        if (launderedReuse) {
            reasons.add("CONTENT_UNCHANGED_FROM_REJECTED_FIRST_PASS");
            reasons.addAll(firstDecision.reasons());
        }
        QualityDecision finalDecision = new QualityDecision(status, hasAst,
                List.of(0, unresolved, 0, 0, 0, 0, 0), reasons);
        return new RecoveryOutcome(hasAst ? objectMapper.writeValueAsString(firstRoot) : null,
                finalDecision, List.copyOf(evidence), reused, recovered, unresolved);
    }

    /**
     * Whole-file repair pass: deterministic engine waves, then the Agent slice ladder, on the
     * complete source. Adoption requires a clean strict reparse of the entire file, so a
     * success preserves everything — comments, globals, regions no unit covers. Returns null
     * when nothing is adopted; unit salvage then proceeds unchanged, and the attempts made
     * here are intentionally not recorded as a unit so sidecar unit counts stay comparable.
     */
    private RecoveryOutcome repairWholeFile(LanguageModule module, Path sourceFile,
            String relativePath, String fileSha256, String source, RawParseResult firstPass,
            QualityDecision firstDecision, ParseProgressTracker tracker) throws Exception {
        if (firstPass.diagnostics().isEmpty()) {
            return null;
        }
        int lineCount = (int) source.lines().count();
        SourceUnit fileUnit = new SourceUnit(fileSha256, UnitKind.FILE,
                sourceFile.getFileName().toString(), null, 0, source.length(),
                1, Math.max(1, lineCount), 0, "EXACT");
        List<RecoveryAttemptEvidence> attempts = new ArrayList<>();
        EngineWaveResult engine = runEngineWaves(module, fileUnit, source, sourceFile,
                relativePath, firstPass, firstDecision, attempts, 0, tracker);
        List<JsonNode> children = engine.cleanChildren();
        if (children == null && !engine.ambiguous() && repairAgent.enabled()) {
            AgentLadderResult ladder = runAgentLadder(module, fileUnit, source, sourceFile,
                    relativePath, fileSha256, firstPass, firstDecision, attempts,
                    engine.attemptNumber(), tracker);
            children = ladder.children();
        }
        if (children == null || children.isEmpty()) {
            return null;
        }
        // Structural invariant from the text-scanning unit locator (parse-independent): a
        // repaired whole file must still contain every named routine the locator sees in the
        // source. Catches "repairs" that merge or swallow declarations yet reparse cleanly
        // (found by mutation benchmarking, 2026-07-22).
        if (!containsEveryLocatedUnit(module, source, children)) {
            tracker.repairProgress("repair_whole_file_rejected",
                    "⚠️ 파일 전체 복구안이 구조 보존 확인을 통과하지 못해 폐기하고 구간 복구로 전환해요");
            return null;
        }
        ObjectNode root = (ObjectNode) objectMapper.readTree(firstPass.astJson());
        ArrayNode array = objectMapper.createArrayNode();
        children.forEach(array::add);
        root.set("children", array);
        QualityDecision decision = new QualityDecision(QualityStatus.RECOVERED_VALIDATED, true,
                List.of(0, 0, 0, 0, 0, 0, 0), List.of("WHOLE_FILE_RECOVERY"));
        UnitRecoveryEvidence fileEvidence = new UnitRecoveryEvidence(fileUnit,
                QualityStatus.RECOVERED_VALIDATED, true, children.size(),
                List.copyOf(attempts));
        return new RecoveryOutcome(objectMapper.writeValueAsString(root), decision,
                List.of(fileEvidence), 0, 1, 0);
    }

    /**
     * Indentation guard for indentation-significant languages (declared via an empty
     * statement-terminator set in SliceSyntax): a changed line whose leading whitespace
     * matches neither adjacent nonblank line may silently move code between scopes even
     * though the reparse succeeds - found by mutation benchmarking (2026-07-22).
     */
    private static boolean indentationSafe(SliceSyntax syntax, WorkingCopy workingCopy) {
        if (!syntax.statementTerminators().isEmpty()) return true;
        String[] before = workingCopy.originalText().split("\n", -1);
        String[] after = workingCopy.workingText().split("\n", -1);
        if (before.length != after.length) return false;
        for (int index = 0; index < after.length; index++) {
            if (before[index].equals(after[index]) || after[index].isBlank()) continue;
            int indent = leadingWhitespace(after[index]);
            Integer previous = adjacentIndent(after, index, -1);
            Integer next = adjacentIndent(after, index, +1);
            // No blanket indent==0 pass: dedenting to column 0 moves code out of its scope
            // (mutation grading caught the engine hoisting an if out of a def, 2026-07-22).
            boolean matches = (previous != null && indent == previous)
                    || (next != null && indent == next)
                    || (previous == null && next == null);
            if (!matches) return false;
        }
        return true;
    }

    private static Integer adjacentIndent(String[] lines, int from, int step) {
        for (int index = from + step; index >= 0 && index < lines.length; index += step) {
            if (!lines[index].isBlank()) return leadingWhitespace(lines[index]);
        }
        return null;
    }

    private static int leadingWhitespace(String line) {
        int count = 0;
        while (count < line.length() && (line.charAt(count) == ' ' || line.charAt(count) == '\t')) {
            count++;
        }
        return count;
    }

    /** 스트림 메시지용 사람 친화 구간 이름 — unit 이름이 없으면 라인 범위로 안내한다. */
    private static String displayName(SourceUnit unit) {
        if (unit.name() != null && !unit.name().isBlank()) return unit.name();
        return unit.startLine() + "~" + unit.endLine() + "행";
    }

    private static List<SourceUnit> expandUnitsCutByGapDamage(List<SourceUnit> units,
            RawParseResult firstPass, List<JsonNode> firstChildren, String source) {
        List<SourceUnit> adjusted = new ArrayList<>(units.size());
        int sourceLines = (int) source.lines().count();
        for (int index = 0; index < units.size(); index++) {
            SourceUnit unit = units.get(index);
            int gapStart = unit.endLine() + 1;
            int gapEnd = index + 1 < units.size()
                    ? units.get(index + 1).startLine() - 1 : sourceLines;
            boolean damagedGap = gapEnd >= gapStart && firstPass.diagnostics().stream()
                    .anyMatch(d -> d.line() >= gapStart && d.line() <= gapEnd);
            boolean orphanedContent = damagedGap && firstChildren.stream().anyMatch(child -> {
                int start = child.path("startLine").asInt(-1);
                int end = child.path("endLine").asInt(start);
                return start >= gapStart && end <= gapEnd;
            });
            if (orphanedContent) {
                int newEndOffset = index + 1 < units.size()
                        ? units.get(index + 1).startOffset() : source.length();
                adjusted.add(new SourceUnit(unit.unitId(), unit.kind(), unit.name(),
                        unit.parentUnitId(), unit.startOffset(), newEndOffset,
                        unit.startLine(), gapEnd, unit.ordinal(), "GAP_DAMAGE_EXPANDED"));
            } else {
                adjusted.add(unit);
            }
        }
        return List.copyOf(adjusted);
    }

    /** Union of start..end line ranges claimed by top-level children. */
    private static java.util.BitSet lineCoverage(List<JsonNode> children) {
        java.util.BitSet covered = new java.util.BitSet();
        for (JsonNode child : children) {
            int start = child.path("startLine").asInt(-1);
            int end = child.path("endLine").asInt(start);
            if (start > 0 && end >= start) covered.set(start, end + 1);
        }
        return covered;
    }

    /** Every named unit the locator finds in the source must appear as a named node. */
    private static boolean containsEveryLocatedUnit(LanguageModule module, String source,
                                                    List<JsonNode> children) {
        java.util.Set<String> emittedNames = new java.util.HashSet<>();
        collectNames(children, emittedNames);
        for (SourceUnit unit : module.locateUnits(source)) {
            if (unit.name() == null || unit.name().isBlank()) continue;
            // Container boundaries (package/file/fragment) are located for slicing but many
            // listeners intentionally emit only their members, never a node named after the
            // container — requiring them here silently killed whole-file repair for the most
            // common Oracle shapes (adversarial audit, 2026-07-22).
            if (unit.kind() == UnitKind.PACKAGE || unit.kind() == UnitKind.FILE
                    || unit.kind() == UnitKind.FRAGMENT) {
                continue;
            }
            String located = normalizeUnitName(unit.name());
            // Listeners store schema-qualified names without the schema, so the last dot
            // segment must count as a match ("SCH"."PROC" ↔ PROC).
            String lastSegment = located.substring(located.lastIndexOf('.') + 1);
            if (!emittedNames.contains(located) && !emittedNames.contains(lastSegment)) {
                return false;
            }
        }
        return true;
    }

    private static String normalizeUnitName(String name) {
        return name.replace("\"", "").trim().toUpperCase(java.util.Locale.ROOT);
    }

    private static void collectNames(List<JsonNode> nodes, java.util.Set<String> names) {
        for (JsonNode node : nodes) {
            String name = node.path("name").asText(null);
            if (name != null) names.add(normalizeUnitName(name));
            List<JsonNode> nested = elements(node.path("children"));
            if (!nested.isEmpty()) collectNames(nested, names);
        }
    }

    /** Outcome of the Agent slice ladder for one unit (or the whole file). */
    private record AgentLadderResult(
            List<JsonNode> children, int attemptNumber, boolean reviewRequired) {
    }

    /**
     * L1→L2→L3 slice ladder against the Agent (spec 012 FR-020..023): the Parser localizes,
     * the Agent proposes within the slice, adoption requires a strictly better clean reparse
     * of the full text.
     */
    private AgentLadderResult runAgentLadder(LanguageModule module, SourceUnit unit,
            String unitText, Path sourceFile, String relativePath, String fileSha256,
            RawParseResult attempt, QualityDecision decision,
            List<RecoveryAttemptEvidence> attempts, int attemptNumber,
            ParseProgressTracker tracker) throws Exception {
        ErrorSpanLocator spanLocator = new ErrorSpanLocator();
        SliceSyntax sliceSyntax = module.sliceSyntax();
        int anchor = attempt.diagnostics().isEmpty() ? 0
                : spanLocator.anchorOffset(unitText, unit.startLine(),
                        attempt.diagnostics().get(0).line(),
                        attempt.diagnostics().get(0).column());
        boolean reviewRequired = false;
        // 사다리 순회의 단일 진실은 SliceLevel.next()다(별도 배열 선언 금지).
        SliceLevel level = SliceLevel.L1;
        for (int agentCall = 1; agentCall <= 3 && level != null;
                agentCall++, level = level.next()) {
            PatchProposal agentProposal = null;
            ContextSlice slice = spanLocator.slice(unitText, sliceSyntax, anchor, level);
            try {
                FailureEnvelope envelope = envelopeFactory.create(
                        module.languageId(), fileSha256, unit, unitText, slice,
                        attempt, attempts, 4 - agentCall);
                agentProposal = proposeWithinBudget(envelope);
                // FR-040 applies to Agent edits too: every token the edit removes or inserts
                // must be structurally neutral. A reparse-clean paren fix that swallows
                // "(name" deletes a call — mutation grading caught exactly that (2026-07-22).
                requireSemanticallyNeutralEdits(agentProposal, module.repairProfile());
                AgentRequestEvidence requestEvidence = AgentRequestEvidence.of(
                        slice.level().name(), slice.length(), unitText.length(),
                        repairAgent.lastPromptTokens());
                List<TextEdit> validatedEdits =
                        proposalValidator.validate(envelope, agentProposal);
                requireNoTokenMerge(unitText, validatedEdits);
                WorkingCopy workingCopy = WorkingCopy.exact(unitText)
                        .applyOriginalEdits(validatedEdits);
                if (!workingCopy.sourceMap().preservesLineCount()) {
                    throw new IllegalArgumentException("AGENT_LINE_COUNT_CHANGED");
                }
                if (!indentationSafe(module.sliceSyntax(), workingCopy)) {
                    throw new IllegalArgumentException("AGENT_INDENTATION_RISK");
                }
                RawParseResult candidate = module.parseUnit(
                        new UnitParseRequest(workingCopy.workingText(),
                                sourceFile.getFileName().toString(), relativePath, unit),
                        tracker);
                QualityDecision candidateDecision = qualityGate.evaluateFirstPass(candidate);
                List<JsonNode> candidateChildren = elements(
                        objectMapper.readTree(candidate.astJson()).path("children"));
                attempts.add(new RecoveryAttemptEvidence(
                        "REPAIR_AGENT", ++attemptNumber, attempt.sourceSha256(),
                        candidate.diagnostics(), candidate.antlrRecoveries(),
                        candidate.coverage(), candidate.elapsedMillis(),
                        candidateDecision.reasons(), candidateDecision.qualityTuple(),
                        workingCopy.workingSha256(), "REPAIR_AGENT",
                        workingCopy.unifiedDiff(relativePath + "#" + unit.unitId()),
                        workingCopy.edits(), workingCopy.sourceMap().summary(),
                        requestEvidence));
                if (candidateDecision.accepted()
                        && isStrictlyBetter(candidateDecision, decision)
                        && !candidateChildren.isEmpty()) {
                    return new AgentLadderResult(candidateChildren, attemptNumber, reviewRequired);
                }
            } catch (RepairAgentException | IllegalArgumentException error) {
                attempts.add(agentFailureEvidence(++attemptNumber, attempt, decision,
                        boundedReason(error.getMessage()), agentProposal));
                reviewRequired = true;
            }
        }
        return new AgentLadderResult(null, attemptNumber, true);
    }

    /** Outcome of the deterministic wave loop for one unit. */
    private record EngineWaveResult(
            List<JsonNode> cleanChildren, boolean ambiguous, int attemptNumber) {
    }

    private static final int MAX_ENGINE_WAVES = 3;

    /**
     * Applies at most {@link #MAX_ENGINE_WAVES} unique strict-improving single-token repairs,
     * reparsing between waves so cascading diagnostics are recomputed. Termination guards:
     * clean parse, ambiguity, no adoptable candidate, no strict improvement, repeated
     * first-error fingerprint (oscillation), wave limit.
     */
    private EngineWaveResult runEngineWaves(LanguageModule module, SourceUnit unit,
            String unitText, Path sourceFile, String relativePath, RawParseResult firstAttempt,
            QualityDecision firstDecision, List<RecoveryAttemptEvidence> attempts,
            int attemptNumber, ParseProgressTracker tracker) throws Exception {
        String workingText = unitText;
        RawParseResult workingParse = firstAttempt;
        QualityDecision workingDecision = firstDecision;
        java.util.Set<String> seenFingerprints = new java.util.HashSet<>();
        List<TextEdit> adoptedEdits = new ArrayList<>();
        for (int wave = 1; wave <= MAX_ENGINE_WAVES; wave++) {
            if (workingParse.diagnostics().isEmpty()) break;
            ParseDiagnostic first = workingParse.diagnostics().get(0);
            String fingerprint = first.line() + ":" + first.column() + ":"
                    + first.code() + ":" + first.offendingToken();
            if (!seenFingerprints.add(fingerprint)) break;

            List<TokenEditCandidate> generated = editEngine.generate(
                    workingText, unit.startLine(), first, module.repairProfile());
            RawParseResult bestParse = null;
            QualityDecision bestDecision = null;
            String bestWorkingText = null;
            WorkingCopy bestCopy = null;
            int survivors = 0;
            for (TokenEditCandidate candidate : generated) {
                if (!candidate.autoAdoptable()) continue;
                WorkingCopy workingCopy;
                try {
                    workingCopy = WorkingCopy.exact(workingText)
                            .applyOriginalEdits(List.of(candidate.toTextEdit()));
                } catch (IllegalArgumentException invalidCandidate) {
                    continue;
                }
                if (!workingCopy.sourceMap().preservesLineCount()
                        || !indentationSafe(module.sliceSyntax(), workingCopy)) {
                    continue;
                }
                RawParseResult candidateParse = module.parseUnit(
                        new UnitParseRequest(workingCopy.workingText(),
                                sourceFile.getFileName().toString(), relativePath, unit),
                        tracker);
                QualityDecision candidateDecision = qualityGate.evaluateFirstPass(candidateParse);
                attempts.add(new RecoveryAttemptEvidence(
                        "GRAMMAR_GUIDED", ++attemptNumber, workingParse.sourceSha256(),
                        candidateParse.diagnostics(), candidateParse.antlrRecoveries(),
                        candidateParse.coverage(), candidateParse.elapsedMillis(),
                        candidateDecision.reasons(), candidateDecision.qualityTuple(),
                        workingCopy.workingSha256(), candidate.provenance(),
                        workingCopy.unifiedDiff(relativePath + "#" + unit.unitId()),
                        workingCopy.edits(), workingCopy.sourceMap().summary()));
                boolean candidateClean = candidateParse.diagnostics().isEmpty()
                        && candidateParse.antlrRecoveries() == 0
                        && candidateDecision.accepted();
                // "no viable alternative" reports a single diagnostic per parse, so count
                // alone cannot show progress — a strictly later first error does (CPCT+
                // parses-further principle, plan D1). Line counts are preserved by every
                // candidate, so (line, column) stays comparable across texts.
                boolean strictImprovement = candidateClean
                        || candidateParse.diagnostics().size() < workingParse.diagnostics().size()
                        || (!candidateParse.diagnostics().isEmpty()
                                && laterThan(candidateParse.diagnostics().get(0), first));
                if (!strictImprovement) continue;
                survivors++;
                if (bestParse == null || betterParse(candidateParse, bestParse)) {
                    bestParse = candidateParse;
                    bestDecision = candidateDecision;
                    bestWorkingText = workingCopy.workingText();
                    bestCopy = workingCopy;
                }
            }
            boolean cleanBest = bestParse != null && bestParse.diagnostics().isEmpty()
                    && bestParse.antlrRecoveries() == 0 && bestDecision.accepted();
            if (survivors > 1 && !cleanBest) {
                attempts.add(new RecoveryAttemptEvidence(
                        "GRAMMAR_GUIDED", ++attemptNumber, workingParse.sourceSha256(),
                        workingParse.diagnostics(), workingParse.antlrRecoveries(),
                        workingParse.coverage(), 0,
                        List.of("GRAMMAR_GUIDED_AMBIGUOUS:" + survivors),
                        workingDecision.qualityTuple(), workingParse.sourceSha256(),
                        "GRAMMAR_GUIDED_AMBIGUOUS", null));
                return new EngineWaveResult(null, true, attemptNumber);
            }
            if (bestParse == null) break;
            workingText = bestWorkingText;
            workingParse = bestParse;
            workingDecision = bestDecision;
            adoptedEdits.addAll(bestCopy.edits());
            if (cleanBest && survivors > 1) {
                // Multiple improvements but exactly one reached a clean parse — not ambiguous.
                break;
            }
        }
        boolean clean = !adoptedEdits.isEmpty() && workingParse.diagnostics().isEmpty()
                && workingParse.antlrRecoveries() == 0 && workingDecision.accepted()
                && isStrictlyBetter(workingDecision, firstDecision);
        if (!clean) return new EngineWaveResult(null, false, attemptNumber);
        List<JsonNode> children = elements(
                objectMapper.readTree(workingParse.astJson()).path("children"));
        return new EngineWaveResult(children.isEmpty() ? null : children, false, attemptNumber);
    }

    private static final java.util.regex.Pattern EDIT_TOKEN = SourceTokens.PATTERN;

    /** FR-040 for the Agent path: reject edits whose removed/inserted tokens are not
     * structurally neutral (identifiers, literals, operators, risk keywords). */
    private static void requireSemanticallyNeutralEdits(
            PatchProposal proposal,
            legacymodernizer.parser.recovery.candidates.RepairProfile profile) {
        for (legacymodernizer.parser.recovery.repair.AgentTextEdit edit : proposal.edits()) {
            requireNeutralTokens(edit.expectedText(), profile, true);
            requireNeutralTokens(edit.replacement(), profile, false);
            // Pure-punctuation substitutions can still flip meaning ("(" → "." turns a call
            // into an attribute access, mutation grading 2026-07-22). Only insertions,
            // deletions and whitespace-blanking are auto-adoptable; substitutions are a
            // human call.
            boolean removesText = edit.expectedText() != null && !edit.expectedText().isBlank();
            boolean insertsText = edit.replacement() != null && !edit.replacement().isBlank();
            if (removesText && insertsText) {
                throw new IllegalArgumentException("AGENT_SUBSTITUTION_REQUIRES_REVIEW");
            }
        }
    }

    /** Deleting a span that sat between two word characters merges them into a brand-new
     * identifier ("getLogger(" minus "(" → "getLoggername"); such edits need a human. */
    private static void requireNoTokenMerge(String unitText, List<TextEdit> edits) {
        for (TextEdit edit : edits) {
            if (!edit.replacement().isBlank()) continue;
            if (edit.replacement().isEmpty()
                    && edit.startOffset() > 0 && edit.endOffset() < unitText.length()
                    && isWordCharacter(unitText.charAt(edit.startOffset() - 1))
                    && isWordCharacter(unitText.charAt(edit.endOffset()))) {
                throw new IllegalArgumentException("AGENT_TOKEN_MERGE_REQUIRES_REVIEW");
            }
        }
    }

    private static boolean isWordCharacter(char character) {
        return Character.isLetterOrDigit(character) || character == '_' || character == '$'
                || character == '#';
    }

    private static void requireNeutralTokens(String text,
            legacymodernizer.parser.recovery.candidates.RepairProfile profile,
            boolean deletion) {
        if (text == null || text.isBlank()) return;
        java.util.regex.Matcher matcher = EDIT_TOKEN.matcher(text);
        while (matcher.find()) {
            String token = matcher.group();
            boolean neutral = deletion
                    ? legacymodernizer.parser.recovery.candidates.EditClassification
                            .forDeletion(token, profile.deletableStructuralKeywords())
                            .autoAdoptable()
                    : legacymodernizer.parser.recovery.candidates.EditClassification
                            .forInsertion(token).autoAdoptable();
            if (!neutral) {
                throw new IllegalArgumentException("AGENT_SEMANTIC_TOKEN_EDIT:" + token);
            }
        }
    }

    /** Gates one Agent HTTP request by prompt-character weight; timeout becomes a normal
     * REPAIR_AGENT failure so the unit ends as REVIEW_REQUIRED, never a hang. */
    private PatchProposal proposeWithinBudget(FailureEnvelope envelope) {
        long weight = envelope.sourceExcerpt().length() + envelope.declarationHeader().length();
        try {
            if (!agentBudget.tryAcquire(weight, java.time.Duration.ofSeconds(
                    Long.getLong("parser.repair.agent.budget.wait.seconds", 180L)))) {
                throw new RepairAgentException("REPAIR_AGENT_BUDGET_TIMEOUT");
            }
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new RepairAgentException("REPAIR_AGENT_BUDGET_INTERRUPTED");
        }
        try {
            return repairAgent.propose(envelope);
        } finally {
            agentBudget.release(weight);
        }
    }

    private static boolean laterThan(ParseDiagnostic candidate, ParseDiagnostic baseline) {
        if (candidate.line() != baseline.line()) return candidate.line() > baseline.line();
        return candidate.column() > baseline.column();
    }

    /** Clean beats dirty; then fewer diagnostics; then a later first error; then fewer recoveries. */
    private static boolean betterParse(RawParseResult candidate, RawParseResult best) {
        boolean candidateClean = candidate.diagnostics().isEmpty();
        boolean bestClean = best.diagnostics().isEmpty();
        if (candidateClean != bestClean) return candidateClean;
        if (candidateClean) return candidate.antlrRecoveries() < best.antlrRecoveries();
        if (candidate.diagnostics().size() != best.diagnostics().size()) {
            return candidate.diagnostics().size() < best.diagnostics().size();
        }
        ParseDiagnostic candidateFirst = candidate.diagnostics().get(0);
        ParseDiagnostic bestFirst = best.diagnostics().get(0);
        if (laterThan(candidateFirst, bestFirst)) return true;
        if (laterThan(bestFirst, candidateFirst)) return false;
        return candidate.antlrRecoveries() < best.antlrRecoveries();
    }

    private RecoveryOutcome unresolved(QualityDecision firstDecision, String reason) {
        QualityDecision decision = new QualityDecision(QualityStatus.UNRESOLVED, false,
                firstDecision.qualityTuple(), List.of(reason));
        return new RecoveryOutcome(null, decision, List.of(), 0, 0, 1);
    }

    private static List<JsonNode> childrenWithin(List<JsonNode> children, SourceUnit unit) {
        return children.stream().filter(child -> {
            int start = child.path("startLine").asInt(-1);
            int end = child.path("endLine").asInt(start);
            return start >= unit.startLine() && end <= unit.endLine();
        }).toList();
    }

    private static boolean reusableIsStructurallyPlausible(List<JsonNode> children, SourceUnit unit) {
        if (children.isEmpty()) return false;
        if (unit.kind() == UnitKind.PACKAGE) return true;
        if (children.size() != 1) return false;
        String type = children.get(0).path("type").asText();
        return switch (unit.kind()) {
            case CLASS -> Set.of("CLASS", "INTERFACE", "ENUM", "RECORD").contains(type);
            case METHOD -> Set.of("METHOD", "CONSTRUCTOR").contains(type);
            case FUNCTION -> Set.of("FUNCTION", "PROCEDURE").contains(type);
            case PROCEDURE -> type.equals("PROCEDURE");
            case TRIGGER -> type.equals("TRIGGER");
            case FILE, FRAGMENT -> false;
            case PACKAGE -> true;
        };
    }

    private static List<JsonNode> elements(JsonNode array) {
        List<JsonNode> elements = new ArrayList<>();
        if (array.isArray()) array.forEach(elements::add);
        return elements;
    }

    private static boolean isStrictlyBetter(QualityDecision candidate, QualityDecision baseline) {
        int length = Math.min(candidate.qualityTuple().size(), baseline.qualityTuple().size());
        for (int index = 0; index < length; index++) {
            int comparison = Integer.compare(candidate.qualityTuple().get(index),
                    baseline.qualityTuple().get(index));
            if (comparison != 0) return comparison < 0;
        }
        return candidate.qualityTuple().size() < baseline.qualityTuple().size();
    }

    private static RecoveryAttemptEvidence agentFailureEvidence(
            int attemptNumber, RawParseResult attempt, QualityDecision decision, String reason,
            PatchProposal proposal) {
        return new RecoveryAttemptEvidence("REPAIR_AGENT", attemptNumber, attempt.sourceSha256(),
                attempt.diagnostics(), attempt.antlrRecoveries(), attempt.coverage(), 0,
                List.of(reason), decision.qualityTuple(), attempt.sourceSha256(), reason, null,
                proposalEdits(proposal), null);
    }

    private static List<TextEdit> proposalEdits(PatchProposal proposal) {
        if (proposal == null || proposal.edits() == null) return List.of();
        return proposal.edits().stream()
                .filter(edit -> edit != null && edit.startOffset() >= 0
                        && edit.endOffset() >= edit.startOffset())
                .map(edit -> new TextEdit(edit.startOffset(), edit.endOffset(),
                        edit.replacement(), "REPAIR_AGENT", edit.reason()))
                .toList();
    }

    private static RecoveryAttemptEvidence agentSkippedEvidence(
            int attemptNumber, RawParseResult attempt, QualityDecision decision, String reason) {
        return new RecoveryAttemptEvidence("REPAIR_AGENT_SKIPPED", attemptNumber,
                attempt.sourceSha256(), attempt.diagnostics(), attempt.antlrRecoveries(),
                attempt.coverage(), 0, List.of(reason), decision.qualityTuple(),
                attempt.sourceSha256(), reason, null);
    }

    private static String boundedReason(String reason) {
        if (reason == null || reason.isBlank()) return "REPAIR_AGENT_FAILURE";
        return reason.length() <= 256 ? reason : reason.substring(0, 256);
    }

    private static String contextRuleId(UnitParseContext context,
                                        ContextSourceCandidate sourceCandidate) {
        return sourceCandidate.ruleId() == null ? context.contextId()
                : context.contextId() + "+" + sourceCandidate.ruleId();
    }

    private record ContextSourceCandidate(
            String sourceText, String ruleId, WorkingCopy workingCopy) {
    }

    private static String readText(Path path) throws Exception {
        byte[] bytes = Files.readAllBytes(path);
        for (String charset : List.of("UTF-8", "EUC-KR", "MS949")) {
            try {
                return Charset.forName(charset).newDecoder().decode(java.nio.ByteBuffer.wrap(bytes)).toString();
            } catch (Exception ignored) {
                // Try the next supported legacy encoding.
            }
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
