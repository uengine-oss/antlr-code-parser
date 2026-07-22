package legacymodernizer.parser.parsing.languages;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 공유 확장자(.sql) 모듈들이 복제하던 contentAffinity 가중 스캔의 단일 진실 —
 * 마커 정의는 각 모듈이 소유하고, 여기서는 점수 집계만 공용화한다.
 */
public final class AffinityMarkers {

    private AffinityMarkers() {
    }

    /** 방언 지문 하나 — 등장 횟수마다 weight 를 더한다. */
    public record Marker(Pattern pattern, int weight) {
    }

    public static int score(List<Marker> markers, String source) {
        int score = 0;
        for (Marker marker : markers) {
            Matcher matcher = marker.pattern().matcher(source);
            while (matcher.find()) score += marker.weight();
        }
        return score;
    }
}
