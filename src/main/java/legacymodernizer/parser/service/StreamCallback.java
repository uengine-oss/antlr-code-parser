package legacymodernizer.parser.service;

/**
 * 스트림 파싱 진행 상황 콜백 인터페이스
 * 
 * 파싱 진행 중 실시간으로 메시지를 전송하기 위한 콜백입니다.
 */
@FunctionalInterface
public interface StreamCallback {
    
    /**
     * 메시지 전송
     * 
     * @param type    메시지 타입 (message, error, complete)
     * @param content 메시지 내용
     */
    void send(String type, String content);
    
    /**
     * 일반 메시지 전송 (편의 메서드)
     */
    default void message(String content) {
        send("message", content);
    }
    
    /**
     * 에러 메시지 전송 (편의 메서드)
     */
    default void error(String content) {
        send("error", content);
    }
    
    /**
     * 완료 신호 전송 (편의 메서드)
     */
    default void complete() {
        send("complete", null);
    }
}

