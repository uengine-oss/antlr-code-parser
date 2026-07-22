package legacymodernizer.parser.recovery.boundaries;

import java.nio.charset.StandardCharsets;

import legacymodernizer.parser.recovery.workingcopy.Hashes;

/**
 * 언어별 SourceUnitLocator 4곳에 복제돼 있던 offset→line 계산과 FILE-단위 fallback 의
 * 단일 진실.
 */
public final class UnitBoundaries {

    private UnitBoundaries() {
    }

    /** 1-기반 라인 번호 — offset 이전의 '\n' 개수 + 1. */
    public static int lineOf(String source, int offset) {
        int line = 1;
        for (int index = 0; index < Math.min(offset, source.length()); index++) {
            if (source.charAt(index) == '\n') line++;
        }
        return line;
    }

    /** 단위 경계를 하나도 찾지 못했을 때의 보수적 FILE-단위 fallback. */
    public static SourceUnit fileUnit(String languageId, String source) {
        String identity = languageId + "\nFILE\n" + source;
        return new SourceUnit(Hashes.sha256(identity.getBytes(StandardCharsets.UTF_8)),
                UnitKind.FILE, null, null, 0, source.length(), source.isEmpty() ? 0 : 1,
                source.isEmpty() ? 0 : lineOf(source, source.length() - 1), 0, "CONSERVATIVE");
    }
}
