package legacymodernizer.parser.service;

import legacymodernizer.parser.api.stream.ParseEventSink;
import legacymodernizer.parser.api.stream.ParseStreamEvent;

/**
 * 파싱 진행 상황 추적기.
 *
 * <p>N라인 간격을 넘을 때마다 진행 메시지를 콜백으로 전달한다(정확히 N라인마다가 아니라
 * 기준을 넘은 시점의 현재 라인). 예: 500라인 기준 → 523라인에서 "523라인까지 파싱 중...".
 */
public class ParseProgressTracker {

    /** 진행 알림 간격 (라인 수) — 직전 알림 + 이 값을 넘으면 콜백. */
    public static final int DEFAULT_LINE_INTERVAL = 500;

    private final ParseEventSink eventSink;
    private final String fileName;
    private final String language;
    private final int fileIndex;
    private final int totalFiles;
    private final int totalLines;
    private int lastNotifiedLine = 0;

    public ParseProgressTracker(ParseEventSink eventSink, String fileName) {
        this(eventSink, fileName, null, 0, 0, 0);
    }

    public ParseProgressTracker(ParseEventSink eventSink, String fileName, String language,
                                int fileIndex, int totalFiles, int totalLines) {
        this.eventSink = eventSink;
        this.fileName = fileName;
        this.language = language;
        this.fileIndex = fileIndex;
        this.totalFiles = totalFiles;
        this.totalLines = totalLines;
    }

    /** 현재 라인이 직전 알림 + 간격을 넘었으면 진행 메시지 전송. */
    public void checkLine(int currentLine) {
        if (eventSink == null) return;

        if (currentLine >= lastNotifiedLine + DEFAULT_LINE_INTERVAL) {
            int percent = overallPercent(currentLine);
            eventSink.emit(ParseStreamEvent.builder("message", "file_progress")
                    .content(String.format("🔎 %s — %,d라인까지 구조를 읽고 있어요", fileName, currentLine))
                    .phase("PARSING").status("RUNNING")
                    .current(fileIndex > 0 ? fileIndex : null)
                    .total(totalFiles > 0 ? totalFiles : null)
                    .percent(percent >= 0 ? percent : null)
                    .file(fileName).language(language).line(currentLine).build());
            lastNotifiedLine = currentLine;
        }
    }

    /** 복구 진행 상황을 사용자 친화 문구로 전송 — 기존 wire 계약(type=message)만 사용. */
    public void repairProgress(String event, String content) {
        if (eventSink == null) return;
        eventSink.emit(legacymodernizer.parser.api.stream.ParseStreamEvent
                .builder("message", event)
                .content(content).phase("RECOVERY").status("RUNNING")
                .file(fileName).language(language).build());
    }

    /** 자동 복구가 불확실해 사람 검토로 남긴 경우 — warning 타입으로 구분 표시 가능. */
    public void repairReviewRequired(String event, String content) {
        if (eventSink == null) return;
        eventSink.emit(legacymodernizer.parser.api.stream.ParseStreamEvent
                .builder("warning", event)
                .content(content).phase("RECOVERY").status("REVIEW_REQUIRED")
                .file(fileName).language(language).build());
    }

    private int overallPercent(int currentLine) {
        if (fileIndex <= 0 || totalFiles <= 0 || totalLines <= 0) return -1;
        double completedFiles = fileIndex - 1;
        double currentFile = Math.min(0.99, Math.max(0.0, (double) currentLine / totalLines));
        return (int) Math.floor(((completedFiles + currentFile) / totalFiles) * 100.0);
    }
}
