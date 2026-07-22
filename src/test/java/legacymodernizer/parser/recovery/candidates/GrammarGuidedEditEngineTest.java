package legacymodernizer.parser.recovery.candidates;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import legacymodernizer.parser.recovery.diagnostics.DiagnosticPhase;
import legacymodernizer.parser.recovery.diagnostics.ParseDiagnostic;

class GrammarGuidedEditEngineTest {

    private final GrammarGuidedEditEngine engine = new GrammarGuidedEditEngine();

    private static ParseDiagnostic diagnostic(String message, int line, int column,
                                              String offending, String expected) {
        return new ParseDiagnostic(DiagnosticPhase.PARSER, "ERROR", "ANTLR_PARSER_SYNTAX",
                message, line, column, offending, expected, List.of("statement"), "");
    }

    @Test
    void extraneousProfileKeywordBecomesAnAdoptableDeletion() {
        String unit = "SELECT 1 FROM T AS X;";
        List<TokenEditCandidate> candidates = engine.generate(unit, 1,
                diagnostic("extraneous input 'AS' expecting ID", 1, unit.indexOf("AS"),
                        "AS", "{ID}"),
                new RepairProfile(Set.of("AS")));

        assertEquals(1, candidates.size());
        TokenEditCandidate candidate = candidates.get(0);
        assertEquals(TokenEditCandidate.Kind.DELETE, candidate.kind());
        assertTrue(candidate.autoAdoptable(), "profile keyword deletion is structural");
        assertEquals("AS ", candidate.expectedText(), "deletion swallows one trailing space");
        assertEquals("SELECT 1 FROM T X;", apply(unit, candidate));
    }

    @Test
    void extraneousAlphabeticTokenWithoutProfileIsNeverAutoAdoptable() {
        String unit = "SELECT 1 FROM T AS X;";
        List<TokenEditCandidate> candidates = engine.generate(unit, 1,
                diagnostic("extraneous input 'AS' expecting ID", 1, unit.indexOf("AS"),
                        "AS", "{ID}"),
                RepairProfile.empty());

        assertEquals(EditClassification.IDENTIFIER, candidates.get(0).classification());
        assertFalse(candidates.get(0).autoAdoptable(),
                "lexically AS could be a column name; without profile evidence it is data");
    }

    @Test
    void missingSemicolonBecomesAnInsertionBeforeTheOffendingToken() {
        String unit = "V1 := 1\nV2 := 2;";
        List<TokenEditCandidate> candidates = engine.generate(unit, 1,
                diagnostic("missing ';' at 'V2'", 2, 0, "V2", "{';'}"),
                RepairProfile.empty());

        assertEquals(1, candidates.size());
        TokenEditCandidate candidate = candidates.get(0);
        assertEquals(TokenEditCandidate.Kind.INSERT, candidate.kind());
        assertTrue(candidate.autoAdoptable());
        assertEquals("V1 := 1\n;V2 := 2;", apply(unit, candidate));
    }

    @Test
    void mismatchedInputProposesOnlyExpectedLiteralReplacements() {
        String unit = "IF X > 0 THAN RETURN; END IF;";
        List<TokenEditCandidate> candidates = engine.generate(unit, 1,
                diagnostic("mismatched input 'THAN' expecting {'THEN', ';'}", 1,
                        unit.indexOf("THAN"), "THAN", "{'THEN', ';'}"),
                RepairProfile.empty());

        assertEquals(2, candidates.size());
        assertTrue(candidates.stream().allMatch(candidate ->
                candidate.kind() == TokenEditCandidate.Kind.REPLACE));
        assertEquals(List.of("THEN", ";"),
                candidates.stream().map(TokenEditCandidate::replacement).toList());
    }

    @Test
    void semanticallyDangerousTokensAreHardRejectedEvenWhenGrammarSuggestsThem() {
        // SC-006 adversarial cases: identifier, literal, operator, transaction keywords.
        String unit = "TOTAL := PRICE + 10; COMMIT;";
        assertFalse(engine.generate(unit, 1,
                        diagnostic("extraneous input 'PRICE' expecting ID", 1,
                                unit.indexOf("PRICE"), "PRICE", "{ID}"),
                        RepairProfile.empty())
                .get(0).autoAdoptable(), "identifier deletion must never auto-adopt");
        assertFalse(engine.generate(unit, 1,
                        diagnostic("extraneous input '10' expecting ID", 1,
                                unit.indexOf("10"), "10", "{ID}"),
                        RepairProfile.empty())
                .get(0).autoAdoptable(), "literal deletion must never auto-adopt");
        assertFalse(engine.generate(unit, 1,
                        diagnostic("extraneous input '+' expecting ';'", 1,
                                unit.indexOf("+"), "+", "{';'}"),
                        RepairProfile.empty())
                .get(0).autoAdoptable(), "operator deletion must never auto-adopt");
        assertFalse(engine.generate(unit, 1,
                        diagnostic("extraneous input 'COMMIT' expecting ID", 1,
                                unit.indexOf("COMMIT"), "COMMIT", "{ID}"),
                        new RepairProfile(Set.of("COMMIT")))
                .get(0).autoAdoptable(),
                "risk-class keywords stay rejected even if a profile tries to allow them");
    }

    @Test
    void noViableAlternativeYieldsNoDeterministicCandidateWithoutProfile() {
        assertTrue(engine.generate("SELECT FROM WHERE;", 1,
                diagnostic("no viable alternative at input 'SELECT FROM'", 1, 0,
                        "SELECT", "{ID}"),
                RepairProfile.empty()).isEmpty());
    }

    @Test
    void noViableAlternativeDeletesOnlyAProfileKeywordAdjacentToTheError() {
        String unit = "SELECT A.ID INTO v_id FROM APP_TABLE AS A;";
        List<TokenEditCandidate> candidates = engine.generate(unit, 1,
                diagnostic("no viable alternative at input '... AS A'", 1,
                        unit.lastIndexOf("A;"), "A", null),
                new RepairProfile(Set.of("AS")));

        assertEquals(1, candidates.size(), "offending identifier 'A' itself must be excluded");
        TokenEditCandidate candidate = candidates.get(0);
        assertEquals(TokenEditCandidate.Kind.DELETE, candidate.kind());
        assertTrue(candidate.autoAdoptable());
        assertEquals("SELECT A.ID INTO v_id FROM APP_TABLE A;", apply(unit, candidate));
    }

    @Test
    void generationIsDeterministic() {
        String unit = "IF X > 0 THAN RETURN; END IF;";
        ParseDiagnostic diagnostic = diagnostic(
                "mismatched input 'THAN' expecting {'THEN', ';'}", 1,
                unit.indexOf("THAN"), "THAN", "{'THEN', ';'}");
        assertEquals(engine.generate(unit, 1, diagnostic, RepairProfile.empty()),
                engine.generate(unit, 1, diagnostic, RepairProfile.empty()));
    }

    private static String apply(String unit, TokenEditCandidate candidate) {
        return unit.substring(0, candidate.unitStartOffset())
                + candidate.replacement()
                + unit.substring(candidate.unitEndOffset());
    }
}
