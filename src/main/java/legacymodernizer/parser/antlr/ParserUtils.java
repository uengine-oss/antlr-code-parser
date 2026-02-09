package legacymodernizer.parser.antlr;

import org.antlr.v4.runtime.CharStream;
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
     * CharStream에서 원본 텍스트 추출 (공백, 줄바꿈 유지)
     */
    public static String getOriginalText(ParserRuleContext ctx) {
        if (ctx == null || ctx.getStart() == null || ctx.getStop() == null) return null;
        CharStream input = ctx.getStart().getInputStream();
        if (input == null) return null;
        return input.getText(new Interval(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex()));
    }
    
    /**
     * 라인번호가 포함된 코드 추출
     * 예: "64: public class OrderReadHelper {\n65:     private static final..."
     */
    public static String getCodeWithLineNumbers(ParserRuleContext ctx) {
        String code = getOriginalText(ctx);
        if (code == null) return null;
        
        int startLine = ctx.getStart().getLine();
        String[] lines = code.split("\n", -1);
        StringBuilder result = new StringBuilder();
        
        for (int i = 0; i < lines.length; i++) {
            if (i > 0) result.append("\n");
            result.append(startLine + i).append(": ").append(lines[i]);
        }
        
        return result.toString();
    }
    
    /**
     * 라인번호가 포함된 코드 추출 (baseLineNumber 오프셋 적용)
     * PL/pgSQL 등 내장 블록에서 사용
     */
    public static String getCodeWithLineNumbers(ParserRuleContext ctx, int baseLineNumber) {
        String code = getOriginalText(ctx);
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
        
        String code = input.getText(new Interval(startToken.getStartIndex(), endToken.getStopIndex()));
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
        
        return input.getText(new Interval(startCharIndex, endCharIndex)).trim();
    }
}

