package legacymodernizer.parser.parsing;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import legacymodernizer.parser.model.Node;
import legacymodernizer.parser.recovery.diagnostics.CollectingAntlrErrorListener;
import legacymodernizer.parser.recovery.diagnostics.CountingErrorStrategy;
import legacymodernizer.parser.recovery.diagnostics.DiagnosticPhase;
import legacymodernizer.parser.recovery.diagnostics.ParseDiagnostic;
import legacymodernizer.parser.service.ParseProgressTracker;

/**
 * 5개 언어 모듈이 동일하게 반복하던 ANTLR 배선(오류 리스너 부착 → 파싱 → AST walk →
 * 좌표 rebase → 진단 병합)의 단일 진실. 언어별로는 lexer/parser/listener 팩토리와
 * entry rule 호출만 남긴다.
 */
public final class AntlrParseHarness {

    private AntlrParseHarness() {
    }

    /** 언어별 AST 리스너의 공통 계약 — harness 가 walk·rebase 하기 위한 최소 표면. */
    public interface AstListener extends ParseTreeListener {
        Node getRoot();

        void setFileInfo(String fileName, String filePath);

        /** Walk가 끝난 뒤 언어 grammar가 요구하는 소유권·범위를 확정한다. */
        default void finalizeAst() {
        }
    }

    /** 공용 배선의 결과 — coverage 계산 등 언어별 마무리를 위해 parser/tree/listener 를 노출. */
    public record Harnessed<P extends Parser, L extends AstListener>(
            P parser, ParserRuleContext tree, L listener, String astJson,
            List<ParseDiagnostic> diagnostics, int recoveries) {
    }

    public static <P extends Parser, L extends AstListener> Harnessed<P, L> run(
            String source, String fileName, String filePath, int lineOffset,
            ParseProgressTracker tracker,
            Function<CharStream, Lexer> lexerFactory,
            Function<CommonTokenStream, P> parserFactory,
            Function<P, ? extends ParserRuleContext> entryRule,
            BiFunction<CommonTokenStream, ParseProgressTracker, L> listenerFactory) {
        CharStream chars = CharStreams.fromString(source);
        Lexer lexer = lexerFactory.apply(chars);
        CollectingAntlrErrorListener lexerErrors = new CollectingAntlrErrorListener(
                DiagnosticPhase.LEXER, source);
        lexer.removeErrorListeners();
        lexer.addErrorListener(lexerErrors);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        P parser = parserFactory.apply(tokens);
        CollectingAntlrErrorListener parserErrors = new CollectingAntlrErrorListener(
                DiagnosticPhase.PARSER, source);
        CountingErrorStrategy errorStrategy = new CountingErrorStrategy();
        parser.removeErrorListeners();
        parser.addErrorListener(parserErrors);
        parser.setErrorHandler(errorStrategy);

        ParserRuleContext tree = entryRule.apply(parser);
        L listener = listenerFactory.apply(tokens, tracker);
        listener.setFileInfo(fileName, filePath);
        new ParseTreeWalker().walk(listener, tree);
        listener.finalizeAst();
        AstCoordinates.rebaseChildren(listener.getRoot(), lineOffset);

        String astJson = listener.getRoot().toJson();
        List<ParseDiagnostic> diagnostics = new ArrayList<>();
        lexerErrors.diagnostics().stream()
                .map(diagnostic -> AstCoordinates.rebase(diagnostic, lineOffset))
                .forEach(diagnostics::add);
        parserErrors.diagnostics().stream()
                .map(diagnostic -> AstCoordinates.rebase(diagnostic, lineOffset))
                .forEach(diagnostics::add);
        return new Harnessed<>(parser, tree, listener, astJson, diagnostics,
                errorStrategy.recoveryCount());
    }
}
