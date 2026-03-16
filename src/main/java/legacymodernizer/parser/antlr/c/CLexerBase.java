package legacymodernizer.parser.antlr.c;

import org.antlr.v4.runtime.*;

/**
 * CLexer Base 클래스
 * 원본 grammars-v4에서는 gcc 전처리기를 실행하지만,
 * 우리 프로젝트에서는 전처리 없이 직접 파싱하므로 단순화
 */
public abstract class CLexerBase extends Lexer {

    public CLexerBase(CharStream input) {
        super(input);
    }
}
