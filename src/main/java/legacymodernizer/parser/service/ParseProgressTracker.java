package legacymodernizer.parser.service;

/**
 * 파싱 진행 상황 추적기
 * 
 * N라인 간격으로 진행 상황을 콜백으로 전달합니다.
 * 정확히 N라인마다가 아니라, N라인 기준을 넘었을 때 현재 라인을 전달합니다.
 * 
 * 예: 500라인 기준
 *   - 523라인 → "523라인까지 파싱 중..."
 *   - 1047라인 → "1047라인까지 파싱 중..."
 */
public class ParseProgressTracker {
    
    /** 기본 알림 간격 (라인 수) */
    public static final int DEFAULT_LINE_INTERVAL = 500;
    
    private final StreamCallback callback;
    private final String fileName;
    private final int lineInterval;
    private int lastNotifiedLine = 0;
    
    public ParseProgressTracker(StreamCallback callback, String fileName) {
        this(callback, fileName, DEFAULT_LINE_INTERVAL);
    }
    
    public ParseProgressTracker(StreamCallback callback, String fileName, int lineInterval) {
        this.callback = callback;
        this.fileName = fileName;
        this.lineInterval = lineInterval;
    }
    
    /**
     * 현재 라인을 체크하고, 기준을 넘었으면 알림 전송
     *
     * @param currentLine 현재 파싱 중인 라인 번호
     */
    public void checkLine(int currentLine) {
        if (callback == null) return;

        if (currentLine >= lastNotifiedLine + lineInterval) {
            callback.message(String.format("📍 %s - %d라인까지 파싱 중...", fileName, currentLine));
            lastNotifiedLine = currentLine;
        }
    }

    /**
     * 파싱 시작 알림
     *
     * @param totalLines 파일 총 라인 수 (알 수 있는 경우)
     */
    public void start(int totalLines) {
        if (callback == null) return;

        if (totalLines > 0) {
            callback.message(String.format("📄 %s 파싱 시작... (총 %,d라인)", fileName, totalLines));
        } else {
            callback.message(String.format("📄 %s 파싱 시작...", fileName));
        }
    }

    /**
     * 파싱 시작 알림 (라인 수 없이)
     */
    public void start() {
        start(0);
    }

    /**
     * 파싱 완료 알림
     *
     * @param totalLines 처리된 총 라인 수
     */
    public void complete(int totalLines) {
        if (callback == null) return;

        callback.message(String.format("✅ %s 완료 (%,d라인)", fileName, totalLines));
    }

    /**
     * 파싱 에러 알림
     *
     * @param errorMessage 에러 메시지
     * @param line 에러 발생 라인 (0이면 라인 정보 없음)
     */
    public void error(String errorMessage, int line) {
        if (callback == null) return;

        if (line > 0) {
            callback.error(String.format("❌ %s 파싱 실패: %s (%d라인)", fileName, errorMessage, line));
        } else {
            callback.error(String.format("❌ %s 파싱 실패: %s", fileName, errorMessage));
        }
    }

    /**
     * 마지막으로 알림을 보낸 라인 반환
     */
    public int getLastNotifiedLine() {
        return lastNotifiedLine;
    }

    /**
     * 콜백 반환
     */
    public StreamCallback getCallback() {
        return callback;
    }
}

