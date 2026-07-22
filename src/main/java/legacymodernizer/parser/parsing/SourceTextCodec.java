package legacymodernizer.parser.parsing;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * byte[]→String 디코딩의 단일 진실 — UTF-8 → EUC-KR → MS949 strict 순서 시도.
 * 전부 실패하면 UTF-8 lenient(대체문자)로 살리되 lossy=true 로 호출자에게 알린다.
 */
public final class SourceTextCodec {

    private static final List<Charset> CANDIDATES = List.of(
            StandardCharsets.UTF_8, Charset.forName("EUC-KR"), Charset.forName("MS949"));

    private SourceTextCodec() {
    }

    /** 디코딩 결과 — 실제 사용 charset 과 손실 여부를 함께 반환한다. */
    public record DecodedText(String text, String charset, boolean lossy) {
    }

    public static DecodedText decode(byte[] bytes) {
        for (Charset charset : CANDIDATES) {
            try {
                String text = charset.newDecoder()
                        .onMalformedInput(CodingErrorAction.REPORT)
                        .onUnmappableCharacter(CodingErrorAction.REPORT)
                        .decode(ByteBuffer.wrap(bytes)).toString();
                return new DecodedText(text, charset.name(), false);
            } catch (CharacterCodingException strictFailure) {
                // 다음 후보 인코딩 시도.
            }
        }
        return new DecodedText(new String(bytes, StandardCharsets.UTF_8),
                StandardCharsets.UTF_8.name(), true);
    }
}
