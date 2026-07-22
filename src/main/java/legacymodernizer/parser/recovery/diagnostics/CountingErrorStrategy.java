package legacymodernizer.parser.recovery.diagnostics;

import org.antlr.v4.runtime.DefaultErrorStrategy;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Token;

public final class CountingErrorStrategy extends DefaultErrorStrategy {

    private int recoveryCount;

    @Override
    public void recover(Parser recognizer, RecognitionException exception) {
        recoveryCount++;
        super.recover(recognizer, exception);
    }

    @Override
    public Token recoverInline(Parser recognizer) throws RecognitionException {
        recoveryCount++;
        return super.recoverInline(recognizer);
    }

    public int recoveryCount() {
        return recoveryCount;
    }
}
