package legacymodernizer.parser.service;

import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * 파일 내용으로 종류를 판정한다 — 표 정의(DDL) vs 소스(프로시저/함수/기타 코드).
 *
 * <p>헌법 III(내용=분류, v1.1.0): 파일명·폴더 경로가 아니라 <b>무엇을 선언하는지</b>로 판정한다.
 * 전체 파싱과 독립적인 경량 키워드 스캔이라, 파서가 못 읽는 프로시저도 정확히 {@code SOURCE} 로
 * 보낸다 — 프로시저가 표 정의(DDL) 파서(analyzer sqlglot)에 닿아 run 이 죽던 사고를 근원 차단.
 *
 * <p>판정 규칙(.sql 한정; 그 외 확장자는 항상 SOURCE):
 * <ul>
 *   <li>프로시저 선언(FUNCTION/PROCEDURE/PACKAGE/TRIGGER/TYPE)이 하나라도 있으면 → SOURCE
 *       (표 정의가 섞여 있어도 SOURCE — 프로시저를 DDL 파서에 넘기지 않는다).</li>
 *   <li>프로시저 없이 표 정의(TABLE/VIEW/INDEX/SEQUENCE)만 있으면 → DDL.</li>
 *   <li>둘 다 없으면 → SOURCE(기본 — 누락하지 않는다).</li>
 * </ul>
 */
@Slf4j
@Service
public class IntakeClassifier {

    public enum Kind { DDL, SOURCE }

    /** 표 정의 선언. */
    private static final Pattern DDL_DECL = Pattern.compile(
            "(?im)^\\s*CREATE\\s+(OR\\s+REPLACE\\s+)?(GLOBAL\\s+TEMPORARY\\s+)?"
            + "(MATERIALIZED\\s+VIEW|TABLE|VIEW|UNIQUE\\s+INDEX|INDEX|SEQUENCE)\\b");

    /** 프로시저/함수/패키지/트리거/타입 선언 = 프로그램 코드. */
    private static final Pattern PROC_DECL = Pattern.compile(
            "(?im)^\\s*CREATE\\s+(OR\\s+REPLACE\\s+)?(EDITIONABLE\\s+|NONEDITIONABLE\\s+)?"
            + "(FUNCTION|PROCEDURE|PACKAGE(\\s+BODY)?|TRIGGER|TYPE(\\s+BODY)?)\\b");

    /**
     * 파일 내용으로 종류를 판정. 절대 예외를 던지지 않는다(판정 불가 시 SOURCE).
     *
     * @param fileName 상대경로/파일명 (확장자 판별용)
     * @param content  파일 텍스트 (null 허용)
     */
    public Kind classify(String fileName, String content) {
        if (content == null || fileName == null || !fileName.toLowerCase().endsWith(".sql")) {
            return Kind.SOURCE;
        }
        boolean hasProc = PROC_DECL.matcher(content).find();
        boolean hasDdl = DDL_DECL.matcher(content).find();
        // 혼합(표+프로시저)은 안전하게 SOURCE — 표 정의 DDL 추출은 후속 US5.
        if (hasProc && hasDdl) {
            log.warn("[분류] 혼합 파일(표+프로시저) → SOURCE(표 정의 추출은 후속): {}", fileName);
        }
        // DDL = 표 정의가 있고 프로시저가 없을 때만. 그 외 전부 SOURCE.
        return (hasDdl && !hasProc) ? Kind.DDL : Kind.SOURCE;
    }
}
