package legacymodernizer.parser.service;

/**
 * 파싱 진행 상황 추적기.
 *
 * <p>N라인 간격을 넘을 때마다 진행 메시지를 콜백으로 전달한다(정확히 N라인마다가 아니라
 * 기준을 넘은 시점의 현재 라인). 예: 500라인 기준 → 523라인에서 "523라인까지 파싱 중...".
 */
public class ParseProgressTracker {

    /** 진행 알림 간격 (라인 수) — 직전 알림 + 이 값을 넘으면 콜백. */
    public static final int DEFAULT_LINE_INTERVAL = 500;

    private final StreamCallback callback;
    private final String fileName;
    private int lastNotifiedLine = 0;

    public ParseProgressTracker(StreamCallback callback, String fileName) {
        this.callback = callback;
        this.fileName = fileName;
    }

    /** 현재 라인이 직전 알림 + 간격을 넘었으면 진행 메시지 전송. */
    public void checkLine(int currentLine) {
        if (callback == null) return;

        if (currentLine >= lastNotifiedLine + DEFAULT_LINE_INTERVAL) {
            callback.message(String.format("📍 %s - %d라인까지 파싱 중...", fileName, currentLine));
            lastNotifiedLine = currentLine;
        }
    }
}
