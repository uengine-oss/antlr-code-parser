package legacymodernizer.parser.antlr;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.misc.Interval;

/**
 * ANTLR 파서 공통 유틸리티
 * - 모든 리스너/비지터에서 공유
 */
public class ParserUtils {
    
    /**
     * 단일 컨텍스트의 원본 텍스트 추출 (공백·줄바꿈 유지).
     * ctx.getText()는 토큰을 공백 없이 이어붙이므로 사용 금지.
     */
    public static String getOriginalText(ParserRuleContext ctx, TokenStream tokens) {
        if (ctx == null) return null;
        return sliceOriginalText(ctx.getStart(), ctx.getStop(), tokens);
    }

    /**
     * 두 컨텍스트 구간의 원본 텍스트 추출 (first.start ~ last.stop).
     * 사이의 콤마·공백·주석까지 모두 보존.
     */
    public static String getOriginalText(ParserRuleContext first, ParserRuleContext last, TokenStream tokens) {
        if (first == null || last == null) return null;
        return sliceOriginalText(first.getStart(), last.getStop(), tokens);
    }

    /**
     * TokenStream 우선, 실패 시 CharStream 인덱스로 복구.
     */
    private static String sliceOriginalText(Token start, Token stop, TokenStream tokens) {
        if (start == null || stop == null) return null;

        if (tokens instanceof CommonTokenStream) {
            try {
                return ((CommonTokenStream) tokens).getText(start, stop);
            } catch (Exception ignored) {
                // CharStream 대체로 넘어감
            }
        }

        CharStream input = start.getInputStream();
        if (input == null) return null;

        int startIdx = Math.min(start.getStartIndex(), stop.getStopIndex());
        int stopIdx  = Math.max(start.getStartIndex(), stop.getStopIndex());
        int lastIdx  = input.size() - 1;
        if (stopIdx > lastIdx) stopIdx = lastIdx;
        if (startIdx < 0) startIdx = 0;
        if (startIdx > stopIdx) return null;

        try {
            return input.getText(new Interval(startIdx, stopIdx));
        } catch (Exception ignored) {
            return null;
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // 주석 토큰 판별 (다중 언어 공용)
    // ═══════════════════════════════════════════════════════════════════

    /**
     * 주석 토큰 여부 판정: 블록 `/{@code *…*}/`, 라인 `--`, `//`.
     * (현재 지원 언어: PL/SQL, PostgreSQL, Java, C, Python 주석은 별도 채널 처리)
     */
    public static boolean isCommentToken(Token t) {
        if (t == null) return false;
        String text = t.getText();
        if (text == null) return false;
        String trimmed = text.trim();
        return trimmed.startsWith("/*") || trimmed.startsWith("--") || trimmed.startsWith("//");
    }

    /**
     * 주석이 standalone(같은 줄에 앞선 코드 토큰이 없는)인지 판정.
     * 인라인 trailing 주석(`x := 1; -- note`)을 걸러낼 때 사용.
     */
    public static boolean isStandaloneComment(Token commentToken, TokenStream tokens) {
        if (commentToken == null || tokens == null) return true;
        int commentLine = commentToken.getLine();
        int tokenIdx = commentToken.getTokenIndex();
        for (int i = tokenIdx - 1; i >= 0; i--) {
            Token prev = tokens.get(i);
            if (prev.getLine() != commentLine) return true;
            if (prev.getChannel() == Token.DEFAULT_CHANNEL) return false;
        }
        return true;
    }

    /**
     * 주석 토큰을 `"라인번호: 내용"` 포맷으로 StringBuilder에 append.
     * 여러 줄 블록 주석은 각 줄마다 번호 붙음.
     */
    private static void appendCommentWithLineNumbers(StringBuilder sb, Token commentToken) {
        int line = commentToken.getLine();
        String[] lines = commentToken.getText().split("\n", -1);
        for (int j = 0; j < lines.length; j++) {
            if (j > 0) sb.append("\n");
            sb.append(line + j).append(": ").append(lines[j]);
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // 헤더(선언부) 주석 수집
    // ═══════════════════════════════════════════════════════════════════

    /**
     * 함수/프로시저/트리거 선언부의 standalone 주석을 모아 헤더 문서로 반환.
     *
     * <p>수집 정책:
     * <ul>
     *   <li>블록 주석(`/{@code *…*}/`): {@code bodyStartKeyword} 이전 전 범위에서 수집.
     *       DECLARE 구간 중간에 삽입되는 다단 문서 블록까지 포괄.</li>
     *   <li>라인 주석(`--`, `//`): 시그니처 직후의 서문만 헤더로 인정하기 위해
     *       {@code declStartKeywords} (예: "AS", "IS") 직후 첫 코드 토큰 라인
     *       <em>이전</em>에 있는 것만 수집. 이 제한을 두지 않으면 DECLARE 내부
     *       CURSOR SQL 주석까지 헤더에 섞여 들어감.</li>
     *   <li>인라인 trailing 주석(같은 줄에 코드 토큰이 선행)은 항상 제외.</li>
     *   <li>{@code bodyStartKeyword}가 null이거나 못 찾으면 ctx 전체 범위.</li>
     *   <li>{@code declStartKeywords}가 비면 라인 주석에도 위치 제한 없음.</li>
     * </ul>
     *
     * <p>예시: PL/SQL은 {@code collectHeaderComments(ctx, tokens, "BEGIN", "AS", "IS")}.
     */
    public static String collectHeaderComments(
            ParserRuleContext ctx, CommonTokenStream tokens,
            String bodyStartKeyword, String... declStartKeywords) {
        if (ctx == null || tokens == null) return null;

        int start = ctx.getStart().getTokenIndex();
        int stop  = ctx.getStop().getTokenIndex();

        int scanEnd = findKeywordIndex(tokens, start, stop, bodyStartKeyword);
        if (scanEnd < 0) scanEnd = stop;

        int lineCommentCutoff = findLineCommentCutoff(tokens, start, scanEnd, declStartKeywords);

        StringBuilder sb = new StringBuilder();
        for (int i = start + 1; i < scanEnd; i++) {
            Token t = tokens.get(i);
            if (t.getChannel() != Token.HIDDEN_CHANNEL) continue;
            if (!isCommentToken(t)) continue;
            if (!isStandaloneComment(t, tokens)) continue;

            // 라인 주석은 선언부 서문 영역(cutoff 이전)만 허용
            if (isLineComment(t) && lineCommentCutoff > 0 && t.getLine() >= lineCommentCutoff) {
                continue;
            }

            if (sb.length() > 0) sb.append("\n");
            appendCommentWithLineNumbers(sb, t);
        }
        return sb.length() > 0 ? sb.toString() : null;
    }

    /** default-channel 토큰 중 주어진 키워드를 찾아 인덱스 반환 (없으면 -1). */
    private static int findKeywordIndex(
            TokenStream tokens, int from, int to, String keyword) {
        if (keyword == null) return -1;
        for (int i = from; i <= to; i++) {
            Token t = tokens.get(i);
            if (t.getChannel() == Token.DEFAULT_CHANNEL
                    && keyword.equalsIgnoreCase(t.getText())) {
                return i;
            }
        }
        return -1;
    }

    /**
     * declStart 키워드(예: AS/IS) 직후 첫 코드 토큰의 라인을 반환.
     * 이 라인 이후의 라인 주석은 헤더가 아닌 statement 내부 주석으로 간주.
     * declStart를 못 찾으면 -1 (제한 없음).
     */
    private static int findLineCommentCutoff(
            TokenStream tokens, int start, int scanEnd, String[] declStartKeywords) {
        if (declStartKeywords == null || declStartKeywords.length == 0) return -1;

        boolean seenDeclStart = false;
        for (int i = start + 1; i < scanEnd; i++) {
            Token t = tokens.get(i);
            if (t.getChannel() != Token.DEFAULT_CHANNEL) continue;
            if (!seenDeclStart) {
                for (String kw : declStartKeywords) {
                    if (kw.equalsIgnoreCase(t.getText())) {
                        seenDeclStart = true;
                        break;
                    }
                }
            } else {
                return t.getLine();
            }
        }
        return -1;
    }

    private static boolean isLineComment(Token t) {
        String trimmed = t.getText().trim();
        return trimmed.startsWith("--") || trimmed.startsWith("//");
    }

    /**
     * 선행 주석 추출 (여러 줄, 블록 주석 모두 포함)
     * 
     * 구문 직전의 모든 주석을 추출 (줄바꿈 포함)
     * - 한 줄 주석: -- (PL/SQL, PostgreSQL)
     * - 블록 주석: 슬래시별표 형식 (모든 언어)
     * - Java 주석: // (Java)
     * - 여러 줄 주석: 연속된 주석 모두 포함
     * 
     * @param ctx 파싱 컨텍스트
     * @param tokens 토큰 스트림
     * @return 선행 주석 텍스트 (라인번호 포함, 없으면 null)
     */
    public static String getLeadingComment(ParserRuleContext ctx, CommonTokenStream tokens) {
        if (ctx == null || ctx.getStart() == null || tokens == null) return null;
        
        List<Token> hiddenTokens = tokens.getHiddenTokensToLeft(
            ctx.getStart().getTokenIndex(), Token.HIDDEN_CHANNEL
        );
        
        if (hiddenTokens == null || hiddenTokens.isEmpty()) return null;
        
        // 주석 토큰 필터링 + standalone 판정 (인라인 trailing 주석 제외)
        int ctxLine = ctx.getStart().getLine();
        List<Token> commentTokens = new ArrayList<>();
        for (Token t : hiddenTokens) {
            if (!isCommentToken(t)) continue;
            if (t.getLine() < ctxLine && !isStandaloneComment(t, tokens)) continue;
            commentTokens.add(t);
        }
        
        if (commentTokens.isEmpty()) return null;

        // 구문 바로 직전의 주석만 포함 (주석 끝과 구문 시작 사이에 빈 줄 2줄 이상이면 관련 없는 주석으로 판단)
        // commentTokens는 역순: index 0이 구문에 가장 가까운 주석
        int ctxStartLine = ctx.getStart().getLine();

        // 구문에 가장 가까운 주석의 끝 라인 확인
        Token closestComment = commentTokens.get(0);
        int closestCommentEndLine = closestComment.getLine();
        String closestText = closestComment.getText();
        closestCommentEndLine += closestText.split("\n", -1).length - 1;

        // 주석 끝과 구문 시작 사이에 빈 줄이 2줄 이상이면 관련 없는 주석
        if (ctxStartLine - closestCommentEndLine > 2) {
            return null;
        }

        // 연속된 주석만 포함 (주석 간 빈 줄 2줄 이상이면 분리)
        List<Token> relevantComments = new ArrayList<>();
        relevantComments.add(commentTokens.get(0));
        for (int i = 1; i < commentTokens.size(); i++) {
            Token prev = commentTokens.get(i - 1); // 구문에 더 가까운 주석
            Token curr = commentTokens.get(i);      // 구문에서 더 먼 주석
            int currEndLine = curr.getLine() + curr.getText().split("\n", -1).length - 1;
            if (prev.getLine() - currEndLine > 2) {
                break; // 이 주석은 이전 블록의 주석
            }
            relevantComments.add(curr);
        }

        Token firstComment = relevantComments.get(relevantComments.size() - 1);
        Token lastComment = relevantComments.get(0);
        
        CharStream input = ctx.getStart().getInputStream();
        if (input == null) return null;
        
        int startIndex = firstComment.getStartIndex();
        int stopIndex = lastComment.getStopIndex();
        
        // 유효한 범위인지 확인
        if (startIndex > stopIndex) {
            return null;
        }
        
        String commentText;
        try {
            commentText = input.getText(new Interval(startIndex, stopIndex));
        } catch (Exception e) {
            // 범위 오류 발생 시 null 반환
            return null;
        }
        if (commentText == null || commentText.trim().isEmpty()) return null;
        
        // 라인번호 추가
        int commentStartLine = firstComment.getLine();
        String[] lines = commentText.split("\n", -1);
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            if (i > 0) result.append("\n");
            result.append(commentStartLine + i).append(": ").append(lines[i]);
        }
        
        return result.toString();
    }

    /**
     * 구문 뒤쪽(같은 줄)에 있는 인라인 주석을 추출한다.
     * 예: SUB_STATUS_ACTIVE, (주석) -> 이 주석을 찾음
     */
    public static String getTrailingComment(ParserRuleContext ctx, CommonTokenStream tokens) {
        if (ctx == null || ctx.getStop() == null || tokens == null) return null;

        int stopTokenIndex = ctx.getStop().getTokenIndex();
        int stopLine = ctx.getStop().getLine();

        // stop 토큰 오른쪽의 hidden 토큰을 탐색
        List<Token> hiddenTokens = tokens.getHiddenTokensToRight(
                stopTokenIndex, Token.HIDDEN_CHANNEL
        );

        if (hiddenTokens == null || hiddenTokens.isEmpty()) return null;

        // 같은 줄에 있는 주석만 찾기
        for (Token t : hiddenTokens) {
            if (t.getLine() != stopLine) break; // 다른 줄이면 중단
            String text = t.getText().trim();
            if (text.startsWith("/*") || text.startsWith("//") || text.startsWith("--")) {
                return stopLine + ": " + text;
            }
        }

        return null;
    }

    /**
     * leading 주석과 trailing 주석을 합쳐서 반환한다.
     * leading이 있으면 leading 우선, 없으면 trailing 사용.
     */
    public static String getComment(ParserRuleContext ctx, CommonTokenStream tokens) {
        String leading = getLeadingComment(ctx, tokens);
        if (leading != null) return leading;
        return getTrailingComment(ctx, tokens);
    }

    /**
     * 시그니처 추출: 시작부터 특정 토큰(예: {, IS, AS) 직전까지
     */
    public static String extractSignature(ParserRuleContext ctx, TokenStream tokens, String... endTokens) {
        if (ctx == null || tokens == null) return null;
        
        int startCharIndex = ctx.getStart().getStartIndex();
        int endCharIndex = -1;
        
        int startTokenIndex = ctx.getStart().getTokenIndex();
        int stopTokenIndex = ctx.getStop().getTokenIndex();
        
        // endToken 찾기
        for (int i = startTokenIndex; i <= stopTokenIndex; i++) {
            Token token = tokens.get(i);
            for (String endToken : endTokens) {
                if (token.getText().equalsIgnoreCase(endToken)) {
                    // 이전 토큰의 끝 위치
                    if (i > startTokenIndex) {
                        endCharIndex = tokens.get(i - 1).getStopIndex();
                    }
                    break;
                }
            }
            if (endCharIndex >= 0) break;
        }
        
        if (endCharIndex < startCharIndex) return null;
        
        CharStream input = ctx.getStart().getInputStream();
        if (input == null) return null;
        
        try {
            return input.getText(new Interval(startCharIndex, endCharIndex)).trim();
        } catch (Exception e) {
            // 범위 오류 발생 시 null 반환
            return null;
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // 초기화식 패턴 판별 (Java/Python/C 공용)
    // ═══════════════════════════════════════════════════════════════════

    /**
     * 초기화식에 메서드/함수 호출 패턴이 있는지 판별.
     * Java: .getX(, getInstance(  Python: func(  C: func(
     */
    public static boolean matchesMethodCall(String initializerText) {
        if (initializerText == null || initializerText.isEmpty()) return false;
        return initializerText.matches("(?s).*\\b\\w+\\s*\\(.*");
    }

    private static final java.util.regex.Pattern PYTHON_CTOR_PATTERN =
            java.util.regex.Pattern.compile("^([A-Z]\\w*)\\s*\\(");

    /**
     * Python 초기화식에서 생성자 클래스명 추출.
     * "StatsService(db)" → "StatsService"
     */
    public static String extractPythonNewInstanceType(String text) {
        if (text == null || text.isEmpty()) return null;
        java.util.regex.Matcher m = PYTHON_CTOR_PATTERN.matcher(text);
        return m.find() ? m.group(1) : null;
    }

    // ═══════════════════════════════════════════════════════════════════
    // 식별자 분리 (schema.name → schema, name)
    // ═══════════════════════════════════════════════════════════════════

    /**
     * "schema.name" 형식을 두 조각으로 분리.
     * 점이 없으면 schema=null, name=fullName.
     */
    public static String[] extractSchemaAndName(String fullName) {
        if (fullName == null) return new String[]{null, null};
        if (fullName.contains(".")) {
            String[] parts = fullName.split("\\.", 2);
            return new String[]{parts[0], parts[1]};
        }
        return new String[]{null, fullName};
    }

    /**
     * 초기화식에서 호출 메서드/함수명 추출.
     * "obj.getService()" → "getService", "calculate(x)" → "calculate"
     */
    public static String extractCallName(String initializerText) {
        if (initializerText == null || initializerText.isEmpty()) return null;
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("(?:.*\\.)?\\b(\\w+)\\s*\\(")
                .matcher(initializerText);
        return m.find() ? m.group(1) : null;
    }

    /**
     * 초기화식 텍스트에 함수/메서드 호출이 있으면 parent의 자식으로 FUNCTION_CALL 노드를 emit한다.
     *
     * <p>표현식 핸들러가 닿지 않는 위치 전용 — 그 외 위치의 호출은 각 언어 표현식 핸들러
     * (Java {@code enterMethodInvocation}, C {@code enterPostfixExpression},
     * Python {@code enterTrailer})가 직접 emit하므로 이 함수를 쓰면 중복된다. 사용처:
     * <ul>
     *   <li>C 전역변수 초기화식 — 파일 스코프라 함수 내부 호출 핸들러가 안 잡음.</li>
     *   <li>PL/pgSQL 문장 표현식 — visitor가 표현식 하위트리를 순회하지 않음.</li>
     * </ul>
     *
     * @param parent          emit된 FUNCTION_CALL의 부모
     * @param initializerText 초기화식 텍스트
     * @param startLine       emit 노드 시작 라인
     * @param endLine         emit 노드 끝 라인
     */
    public static void emitInitializerCall(
            legacymodernizer.parser.model.Node parent, String initializerText,
            int startLine, int endLine) {
        if (parent == null || initializerText == null || initializerText.isEmpty()) return;
        if (!matchesMethodCall(initializerText)) return;
        legacymodernizer.parser.model.Node call = new legacymodernizer.parser.model.Node(
                "FUNCTION_CALL", extractCallName(initializerText), startLine, parent);
        call.endLine = endLine;
    }
}
