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
     * 원본 텍스트 추출 (공백, 줄바꿈 유지)
     * TokenStream을 우선 사용 (범위 오류 방지), 실패 시 CharStream 시도
     * 
     * 비교 결과:
     * - TokenStream.getText(): 원본 그대로, 범위 오류 방지 ✅ 우선 사용
     * - CharStream.getText(Interval): 원본 그대로, 범위 오류 가능 ⚠️ 대체로 사용
     * - ctx.getText(): 공백이 모두 사라짐 ❌ 완전히 제거됨
     */
    public static String getOriginalText(ParserRuleContext ctx, TokenStream tokens) {
        if (ctx == null) return null;
        
        // 1. TokenStream 우선 사용 (범위 오류 방지, 원본 그대로)
        if (tokens instanceof CommonTokenStream) {
            try {
                return ((CommonTokenStream) tokens).getText(ctx);
            } catch (Exception e) {
                // TokenStream 실패 시 CharStream 시도
            }
        }
        
        // 2. CharStream 대체 시도 (TokenStream 실패 시)
        if (ctx.getStart() != null && ctx.getStop() != null) {
            CharStream input = ctx.getStart().getInputStream();
            if (input != null) {
                int startIndex = ctx.getStart().getStartIndex();
                int stopIndex = ctx.getStop().getStopIndex();
                
                // 범위가 잘못된 경우 처리
                if (startIndex > stopIndex) {
                    int temp = startIndex;
                    startIndex = stopIndex;
                    stopIndex = temp;
                }
                
                try {
                    // 토큰 인덱스가 파일 범위를 벗어나는지 확인
                    int fileSize = input.size();
                    if (stopIndex >= fileSize) {
                        stopIndex = fileSize - 1;
                    }
                    if (startIndex < 0) {
                        startIndex = 0;
                    }
                    if (startIndex <= stopIndex) {
                        return input.getText(new Interval(startIndex, stopIndex));
                    }
                } catch (Exception e) {
                    // 최종 실패
                }
            }
        }
        
        return null;
    }
    
    /**
     * 디버깅용: 세 가지 방법으로 텍스트 추출하여 비교
     * - CharStream: 원본 그대로
     * - ctx.getText(): 토큰 연결
     * - TokenStream: 토큰 기반 (숨겨진 채널 포함)
     */
    public static String compareTextExtraction(ParserRuleContext ctx, TokenStream tokens) {
        if (ctx == null) return null;
        
        StringBuilder result = new StringBuilder();
        result.append("=== 텍스트 추출 방법 비교 ===\n\n");
        
        // 1. CharStream 방식 (원본 그대로)
        try {
            if (ctx.getStart() != null && ctx.getStop() != null) {
                CharStream input = ctx.getStart().getInputStream();
                if (input != null) {
                    int startIndex = ctx.getStart().getStartIndex();
                    int stopIndex = ctx.getStop().getStopIndex();
                    if (startIndex <= stopIndex) {
                        String charStreamText = input.getText(new Interval(startIndex, stopIndex));
                        result.append("[1] CharStream.getText():\n");
                        result.append("길이: ").append(charStreamText.length()).append("\n");
                        result.append("내용: ").append(escapeForDisplay(charStreamText)).append("\n\n");
                    }
                }
            }
        } catch (Exception e) {
            result.append("[1] CharStream.getText(): 실패 - ").append(e.getMessage()).append("\n\n");
        }
        
        // 2. ctx.getText() 방식
        try {
            String ctxText = ctx.getText();
            result.append("[2] ctx.getText():\n");
            result.append("길이: ").append(ctxText.length()).append("\n");
            result.append("내용: ").append(escapeForDisplay(ctxText)).append("\n\n");
        } catch (Exception e) {
            result.append("[2] ctx.getText(): 실패 - ").append(e.getMessage()).append("\n\n");
        }
        
        // 3. TokenStream 방식
        if (tokens instanceof CommonTokenStream) {
            try {
                CommonTokenStream cts = (CommonTokenStream) tokens;
                int startTokenIndex = ctx.getStart().getTokenIndex();
                int stopTokenIndex = ctx.getStop().getTokenIndex();
                String tokenStreamText = cts.getText(ctx);
                result.append("[3] TokenStream.getText():\n");
                result.append("토큰 범위: ").append(startTokenIndex).append(" ~ ").append(stopTokenIndex).append("\n");
                result.append("길이: ").append(tokenStreamText.length()).append("\n");
                result.append("내용: ").append(escapeForDisplay(tokenStreamText)).append("\n\n");
            } catch (Exception e) {
                result.append("[3] TokenStream.getText(): 실패 - ").append(e.getMessage()).append("\n\n");
            }
        }
        
        return result.toString();
    }
    
    private static String escapeForDisplay(String text) {
        if (text == null) return "null";
        return text
            .replace("\\", "\\\\")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
    }
    
    /**
     * 라인번호가 포함된 코드 추출 (TokenStream 필수)
     * 예: "64: public class OrderReadHelper {\n65:     private static final..."
     */
    public static String getCodeWithLineNumbers(ParserRuleContext ctx, TokenStream tokens) {
        if (ctx == null) return null;
        
        String code = getOriginalText(ctx, tokens);
        if (code == null) return null;
        
        return formatCodeWithLineNumbers(code, ctx.getStart().getLine());
    }
    
    private static String formatCodeWithLineNumbers(String code, int startLine) {
        if (code == null) return null;
        
        String[] lines = code.split("\n", -1);
        StringBuilder result = new StringBuilder();
        
        for (int i = 0; i < lines.length; i++) {
            if (i > 0) result.append("\n");
            result.append(startLine + i).append(": ").append(lines[i]);
        }
        
        return result.toString();
    }
    
    /**
     * 라인번호가 포함된 코드 추출 (baseLineNumber 오프셋 적용, TokenStream 필수)
     * PL/pgSQL 등 내장 블록에서 사용
     */
    public static String getCodeWithLineNumbers(ParserRuleContext ctx, int baseLineNumber, TokenStream tokens) {
        if (ctx == null) return null;
        
        String code = getOriginalText(ctx, tokens);
        if (code == null) return null;
        
        int startLine = baseLineNumber + ctx.getStart().getLine();
        String[] lines = code.split("\n", -1);
        StringBuilder result = new StringBuilder();
        
        for (int i = 0; i < lines.length; i++) {
            if (i > 0) result.append("\n");
            result.append(startLine + i).append(": ").append(lines[i]);
        }
        
        return result.toString();
    }
    
    /**
     * Token 범위 기반 코드 추출 (라인번호 포함)
     * EXCEPTION 블록처럼 여러 컨텍스트를 합쳐야 할 때 사용
     */
    public static String getCodeWithLineNumbers(Token startToken, Token endToken) {
        if (startToken == null || endToken == null) return null;
        CharStream input = startToken.getInputStream();
        if (input == null) return null;
        
        int startIndex = startToken.getStartIndex();
        int stopIndex = endToken.getStopIndex();
        
        // 범위가 잘못된 경우 처리
        if (startIndex > stopIndex) {
            int temp = startIndex;
            startIndex = stopIndex;
            stopIndex = temp;
        }
        
        String code;
        try {
            // 토큰 인덱스가 파일 범위를 벗어나는지 확인
            // (파일 크기 문제가 아니라, 파싱 중 토큰 인덱스 계산 오류로 인한 범위 초과 방지)
            int fileSize = input.size();
            if (stopIndex >= fileSize) {
                stopIndex = fileSize - 1;
            }
            if (startIndex < 0) {
                startIndex = 0;
            }
            if (startIndex > stopIndex) {
                return null;
            }
            
            code = input.getText(new Interval(startIndex, stopIndex));
        } catch (Exception e) {
            // 범위 오류 발생 시 최소한 가능한 범위만이라도 추출 시도
            try {
                int fileSize = input.size();
                if (startIndex >= 0 && startIndex < fileSize) {
                    int safeStopIndex = Math.min(stopIndex, fileSize - 1);
                    if (safeStopIndex >= startIndex) {
                        code = input.getText(new Interval(startIndex, safeStopIndex));
                    } else {
                        return null;
                    }
                } else {
                    return null;
                }
            } catch (Exception e2) {
                return null;
            }
        }
        
        if (code == null) return null;
        
        int startLine = startToken.getLine();
        String[] lines = code.split("\n", -1);
        StringBuilder result = new StringBuilder();
        
        for (int i = 0; i < lines.length; i++) {
            if (i > 0) result.append("\n");
            result.append(startLine + i).append(": ").append(lines[i]);
        }
        
        return result.toString();
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
        
        // 주석 토큰만 필터 (공백/줄바꿈 제외)
        // getHiddenTokensToLeft는 역순으로 반환되므로, 첫 번째가 가장 가까운 주석
        List<Token> commentTokens = new ArrayList<>();
        for (Token t : hiddenTokens) {
            String text = t.getText().trim();
            // PL/SQL, PostgreSQL, Java 모두 지원
            if (text.startsWith("--") || text.startsWith("/*") || text.startsWith("//")) {
                commentTokens.add(t);
            }
        }
        
        if (commentTokens.isEmpty()) return null;
        
        // 첫 번째 주석부터 마지막 주석까지 모두 포함
        // 역순이므로 마지막이 첫 번째 주석, 첫 번째가 마지막 주석
        Token firstComment = commentTokens.get(commentTokens.size() - 1);
        Token lastComment = commentTokens.get(0);
        
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
     * 인라인 주석 추출 (같은 라인에 있는 주석만)
     * 
     * 구문 끝 토큰과 같은 라인에 있는 주석을 추출
     * 예: product_id VARCHAR(20) PRIMARY KEY,  -- 제품 ID
     * 
     * @param ctx 파싱 컨텍스트
     * @param tokens 토큰 스트림
     * @return 인라인 주석 텍스트 (주석 기호 제거, 없으면 null)
     */
    public static String getTrailingComment(ParserRuleContext ctx, CommonTokenStream tokens) {
        if (ctx == null || ctx.getStop() == null || tokens == null) return null;
        
        Token stopToken = ctx.getStop();
        int stopLine = stopToken.getLine();
        
        List<Token> hiddenTokens = tokens.getHiddenTokensToRight(
            stopToken.getTokenIndex(), Token.HIDDEN_CHANNEL
        );
        
        if (hiddenTokens == null || hiddenTokens.isEmpty()) return null;
        
        // 같은 라인에 있는 첫 번째 주석만 추출
        for (Token t : hiddenTokens) {
            String text = t.getText().trim();
            
            // 주석인지 확인 (PostgreSQL/Oracle 모두 -- 또는 /* */)
            if (text.startsWith("--") || text.startsWith("/*")) {
                // 같은 라인인지 확인
                if (t.getLine() == stopLine) {
                    // 주석 기호 제거하고 반환
                    if (text.startsWith("--")) {
                        return text.substring(2).trim();
                    } else if (text.startsWith("/*")) {
                        String comment = text.substring(2);
                        if (comment.endsWith("*/")) {
                            comment = comment.substring(0, comment.length() - 2);
                        }
                        return comment.trim();
                    }
                } else {
                    // 다른 라인이면 중단 (다음 구문의 주석일 수 있음)
                    break;
                }
            }
        }
        
        return null;
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
}


