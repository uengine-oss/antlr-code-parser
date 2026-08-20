package legacymodernizer.parser.parsing.languages.c;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

import legacymodernizer.parser.parsing.evidence.ConditionalCompilationEvidence;
import legacymodernizer.parser.parsing.evidence.ConditionalCompilationEvidence.ConditionalRegionCandidate;
import legacymodernizer.parser.parsing.evidence.ConditionalCompilationEvidence.Presence;
import legacymodernizer.parser.parsing.evidence.ConditionalCompilationEvidence.PresenceSpan;
import legacymodernizer.parser.parsing.evidence.SourceRangeCandidate;

/** Builds conditional presence exclusively from preprocessing-grammar listener events. */
public final class CConditionalCompilationAnalyzer {

    public enum Kind { IF, IFDEF, IFNDEF, ELIF, ELSE, ENDIF }

    public record Directive(
            Kind kind,
            SourceRangeCandidate range,
            String canonicalText,
            Long constantValue) {
        public Directive {
            if (kind == null || range == null || canonicalText == null) {
                throw new IllegalArgumentException("conditional directive fields are required");
            }
        }
    }

    private CConditionalCompilationAnalyzer() {
    }

    public static ConditionalCompilationEvidence analyze(
            int sourceLength, List<Directive> directives) {
        List<PresenceSpan> spans = new ArrayList<>();
        List<ConditionalRegionCandidate> regions = new ArrayList<>();
        ArrayDeque<Frame> stack = new ArrayDeque<>();
        int cursor = 0;

        for (Directive directive : directives) {
            int start = directive.range().startOffset();
            int end = directive.range().endOffset();
            Presence directivePresence = currentPresence(stack);
            addSpan(spans, cursor, start, directivePresence);
            addSpan(spans, start, end, directivePresence);
            switch (directive.kind()) {
                case IF, IFDEF, IFNDEF -> {
                    Reachability parent = currentReachability(stack);
                    Evaluation evaluation = directive.kind() == Kind.IF
                            ? Evaluation.of(directive.constantValue()) : Evaluation.unknown();
                    Reachability branch = fromEvaluation(evaluation);
                    stack.push(new Frame(start, directive.canonicalText(), parent,
                            combine(parent, branch), evaluation.mayBeTrue(),
                            evaluation.isDefinitelyTrue(), evaluation.isUnknown()));
                }
                case ELIF -> {
                    if (!stack.isEmpty()) stack.peek().enterElif(directive);
                    else regions.add(malformedDirective(directive));
                }
                case ELSE -> {
                    if (!stack.isEmpty()) stack.peek().enterElse();
                    else regions.add(malformedDirective(directive));
                }
                case ENDIF -> {
                    if (!stack.isEmpty()) {
                        Frame completed = stack.pop();
                        regions.add(new ConditionalRegionCandidate(
                                new SourceRangeCandidate(completed.groupStart, end),
                                completed.firstCondition,
                                effectiveRegionPresence(completed)));
                    } else regions.add(malformedDirective(directive));
                }
            }
            cursor = end;
        }
        addSpan(spans, cursor, sourceLength, currentPresence(stack));
        while (!stack.isEmpty()) {
            Frame unterminated = stack.removeLast();
            regions.add(new ConditionalRegionCandidate(
                    new SourceRangeCandidate(unterminated.groupStart, sourceLength),
                    unterminated.firstCondition,
                    new Presence("unknown", unterminated.firstCondition,
                            "malformed_directive_structure")));
        }
        return new ConditionalCompilationEvidence(spans, regions);
    }

    private static Presence effectiveRegionPresence(Frame completed) {
        if (completed.parent == Reachability.INACTIVE) {
            return new Presence("inactive", completed.firstCondition, "enclosing_inactive");
        }
        if (completed.parent == Reachability.CONDITIONAL) {
            return new Presence("conditional", completed.firstCondition,
                    "inherited_missing_build_configuration");
        }
        if (completed.unknownEncountered) {
            return new Presence("conditional", completed.firstCondition,
                    "missing_build_configuration");
        }
        return new Presence("active", completed.firstCondition, "constant_evaluated");
    }

    private static ConditionalRegionCandidate malformedDirective(Directive directive) {
        return new ConditionalRegionCandidate(directive.range(), directive.canonicalText(),
                new Presence("unknown", directive.canonicalText(),
                        "malformed_directive_structure"));
    }

    private static void addSpan(
            List<PresenceSpan> spans, int start, int end, Presence presence) {
        if (end > start) spans.add(new PresenceSpan(new SourceRangeCandidate(start, end), presence));
    }

    private static Reachability currentReachability(ArrayDeque<Frame> stack) {
        return stack.isEmpty() ? Reachability.ACTIVE : stack.peek().current;
    }

    private static Presence currentPresence(ArrayDeque<Frame> stack) {
        if (stack.isEmpty()) return Presence.active();
        Frame frame = stack.peek();
        return switch (frame.current) {
            case ACTIVE -> new Presence("active", frame.currentCondition, "constant_evaluated");
            case INACTIVE -> new Presence("inactive", frame.currentCondition, "constant_evaluated");
            case CONDITIONAL -> new Presence("conditional", frame.currentCondition,
                    "missing_build_configuration");
        };
    }

    private static Reachability fromEvaluation(Evaluation evaluation) {
        if (evaluation.isDefinitelyTrue()) return Reachability.ACTIVE;
        if (!evaluation.mayBeTrue()) return Reachability.INACTIVE;
        return Reachability.CONDITIONAL;
    }

    private static Reachability combine(Reachability parent, Reachability branch) {
        if (parent == Reachability.INACTIVE || branch == Reachability.INACTIVE) {
            return Reachability.INACTIVE;
        }
        if (parent == Reachability.ACTIVE && branch == Reachability.ACTIVE) {
            return Reachability.ACTIVE;
        }
        return Reachability.CONDITIONAL;
    }

    private enum Reachability { ACTIVE, INACTIVE, CONDITIONAL }

    private record Evaluation(boolean known, long value) {
        private static Evaluation of(Long value) {
            return value == null ? unknown() : new Evaluation(true, value);
        }
        private static Evaluation unknown() { return new Evaluation(false, 0); }
        private boolean isDefinitelyTrue() { return known && value != 0; }
        private boolean mayBeTrue() { return !known || value != 0; }
        private boolean isUnknown() { return !known; }
    }

    private static final class Frame {
        private final int groupStart;
        private final String firstCondition;
        private final Reachability parent;
        private boolean priorMayMatch;
        private boolean priorDefinitelyMatched;
        private boolean unknownEncountered;
        private Reachability current;
        private String currentCondition;

        private Frame(int groupStart, String firstCondition, Reachability parent,
                      Reachability current, boolean priorMayMatch,
                      boolean priorDefinitelyMatched, boolean unknownEncountered) {
            this.groupStart = groupStart;
            this.firstCondition = firstCondition;
            this.parent = parent;
            this.current = current;
            this.priorMayMatch = priorMayMatch;
            this.priorDefinitelyMatched = priorDefinitelyMatched;
            this.unknownEncountered = unknownEncountered;
            this.currentCondition = firstCondition;
        }

        private void enterElif(Directive directive) {
            Evaluation evaluation = Evaluation.of(directive.constantValue());
            Reachability local;
            if (priorDefinitelyMatched || !evaluation.mayBeTrue()) local = Reachability.INACTIVE;
            else if (priorMayMatch || evaluation.isUnknown()) local = Reachability.CONDITIONAL;
            else local = Reachability.ACTIVE;
            current = combine(parent, local);
            priorDefinitelyMatched = priorDefinitelyMatched
                    || (!priorMayMatch && evaluation.isDefinitelyTrue());
            priorMayMatch = priorMayMatch || evaluation.mayBeTrue();
            unknownEncountered |= evaluation.isUnknown();
            currentCondition = directive.canonicalText();
        }

        private void enterElse() {
            Reachability local = priorDefinitelyMatched ? Reachability.INACTIVE
                    : priorMayMatch ? Reachability.CONDITIONAL : Reachability.ACTIVE;
            current = combine(parent, local);
            currentCondition = "#else of (" + firstCondition + ")";
        }
    }
}
