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
        // 인라인 주석 제외: 주석과 같은 줄에 코드 토큰이 있으면 그건 그 코드의 주석이지 다음 요소의 주석이 아님
        int ctxLine = ctx.getStart().getLine();
        List<Token> commentTokens = new ArrayList<>();
        for (Token t : hiddenTokens) {
            String text = t.getText().trim();
            if (text.startsWith("--") || text.startsWith("/*") || text.startsWith("//")) {
                // 주석이 구문과 같은 줄이 아니고, 주석 줄에 앞쪽 코드 토큰이 있으면 인라인 주석 → 제외
                int commentLine = t.getLine();
                if (commentLine < ctxLine) {
                    // 이 주석 앞에 같은 줄의 코드 토큰이 있는지 확인
                    boolean isInlineComment = false;
                    int tokenIdx = t.getTokenIndex();
                    for (int i = tokenIdx - 1; i >= 0; i--) {
                        Token prev = tokens.get(i);
                        if (prev.getLine() != commentLine) break;
                        if (prev.getChannel() == Token.DEFAULT_CHANNEL) {
                            isInlineComment = true;
                            break;
                        }
                    }
                    if (isInlineComment) continue; // 인라인 주석은 건너뜀
                }
                commentTokens.add(t);
            }
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
        return initializerText.matches("(?s).*\\.\\w+\\s*\\(.*")
            || initializerText.matches("(?s).*\\b\\w+\\s*\\(.*");
    }

    /**
     * 초기화식에 Java/C의 new Type 패턴이 있는지 판별.
     */
    public static boolean matchesNewInstance(String initializerText) {
        if (initializerText == null) return false;
        return initializerText.matches("(?s).*\\bnew\\s+\\w+.*");
    }

    /**
     * 초기화식에 Python 생성자 호출 패턴(대문자 시작)이 있는지 판별.
     * Python: ClassName(args) = Java의 new ClassName(args)
     */
    public static boolean matchesPythonNewInstance(String initializerText) {
        if (initializerText == null || initializerText.isEmpty()) return false;
        return initializerText.matches("(?s).*\\b[A-Z]\\w*\\s*\\(.*");
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

    /**
     * 초기화식 플래그를 Node에 일괄 적용.
     * @param node            대상 노드
     * @param initializerText 초기화식 텍스트
     * @param pythonMode      true면 Python 방식 (ClassName(args) = new instance)
     */
    public static void applyInitializerFlags(
            legacymodernizer.parser.model.Node node, String initializerText, boolean pythonMode) {
        if (initializerText == null || initializerText.isEmpty()) return;

        boolean hasCall = matchesMethodCall(initializerText);
        node.initializerContainsMethodCall = hasCall ? true : null;

        boolean hasNew = pythonMode
                ? matchesPythonNewInstance(initializerText)
                : matchesNewInstance(initializerText);
        node.initializerContainsNewInstance = hasNew ? true : null;

        if (pythonMode && hasNew && node.variableType == null) {
            String className = extractPythonNewInstanceType(initializerText);
            if (className != null) {
                node.variableType = className;
            }
        }
    }
}
