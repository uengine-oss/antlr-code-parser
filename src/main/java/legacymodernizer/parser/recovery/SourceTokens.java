package legacymodernizer.parser.recovery;

import java.util.regex.Pattern;

/**
 * 단일 진실: Agent에게 보여주는 토큰 좌표(FailureEnvelopeFactory)와 Agent 편집을 심사하는
 * 토큰 분해(LayeredRecoveryPipeline)는 같은 어휘 규칙을 써야 한다 — 한쪽만 바뀌면 모델이
 * 본 토큰과 게이트가 심사한 토큰이 어긋난다.
 */
public final class SourceTokens {

    public static final Pattern PATTERN = Pattern.compile(
            "[\\p{L}_$#][\\p{L}\\p{N}_$#]*|\\p{N}+(?:\\.\\p{N}+)?|\\S");

    private SourceTokens() {
    }
}
