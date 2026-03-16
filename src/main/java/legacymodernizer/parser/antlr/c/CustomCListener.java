package legacymodernizer.parser.antlr.c;

import java.util.List;
import java.util.Stack;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

import legacymodernizer.parser.antlr.Node;
import legacymodernizer.parser.antlr.ParserUtils;
import legacymodernizer.parser.service.ParseProgressTracker;

/**
 * C 파일 분석을 위한 커스텀 리스너
 * - #include는 전처리기 지시문(HIDDEN 채널)이므로 별도 추출
 * - struct/union → STRUCT 노드
 * - enum → ENUM 노드
 * - functionDefinition → FUNCTION 노드
 * - 전역 declaration → GLOBAL_VARIABLE 또는 TYPEDEF 노드
 * - 지역 declaration (블록 내) → VARIABLE 노드
 * - 함수 호출 → FUNCTION_CALL 노드 (postfixExpression 기반)
 */
public class CustomCListener extends CParserBaseListener {

    private CommonTokenStream tokens;
    private Stack<Node> nodeStack = new Stack<>();
    private Node root = new Node("FILE", 0, null);
    private ParseProgressTracker progressTracker;

    public Node getRoot() {
        return root;
    }

    public void setFileInfo(String fileName, String filePath) {
        root.fileName = fileName;
        root.filePath = filePath;
    }

    public CustomCListener(CommonTokenStream tokens) {
        this.tokens = tokens;
        nodeStack.push(root);
        extractIncludes();
    }

    public CustomCListener(CommonTokenStream tokens, ParseProgressTracker tracker) {
        this(tokens);
        this.progressTracker = tracker;
    }

    @Override
    public void enterEveryRule(ParserRuleContext ctx) {
        if (progressTracker != null && ctx.getStart() != null) {
            progressTracker.checkLine(ctx.getStart().getLine());
        }
    }

    // ========================================
    // #include 추출 (HIDDEN 채널에서)
    // ========================================

    private void extractIncludes() {
        tokens.fill();
        for (Token token : tokens.getTokens()) {
            if (token.getType() == CLexer.Directive) {
                String text = token.getText().trim();
                if (text.startsWith("#include") || text.startsWith("# include")) {
                    String includeName = text.replaceFirst("^#\\s*include\\s*", "").trim();
                    Node node = new Node("INCLUDE", includeName, token.getLine(), root);
                    node.endLine = token.getLine();
                }
            }
        }
    }

    // ========================================
    // 노드 생성/종료
    // ========================================

    private Node enterStatement(String type, String name, int line) {
        Node node = new Node(type, name, line, nodeStack.peek());
        nodeStack.push(node);
        return node;
    }

    private void exitStatement(String type, int line, ParserRuleContext ctx) {
        if (!nodeStack.isEmpty() && nodeStack.peek().type.equals(type)) {
            Node node = nodeStack.pop();
            node.endLine = line;
            if (ctx != null) {
                node.comment = ParserUtils.getLeadingComment(ctx, tokens);
            }
        }
    }

    // ========================================
    // struct / union
    // ========================================

    @Override
    public void enterStructOrUnionSpecifier(CParser.StructOrUnionSpecifierContext ctx) {
        // 이름이 있고 본문({})이 있는 struct/union만 노드로 생성
        if (ctx.Identifier() == null) return;
        // 본문이 없으면 (forward declaration 또는 타입 참조) 무시
        if (ctx.memberDeclarationList() == null) return;

        String structOrUnion = ctx.structOrUnion().getText(); // "struct" or "union"
        String type = structOrUnion.equals("union") ? "UNION" : "STRUCT";
        String name = ctx.Identifier().getText();

        Node node = enterStatement(type, name, ctx.getStart().getLine());
        node.signature = ParserUtils.extractSignature(ctx, tokens, "{");
    }

    @Override
    public void exitStructOrUnionSpecifier(CParser.StructOrUnionSpecifierContext ctx) {
        if (ctx.Identifier() == null) return;
        if (ctx.memberDeclarationList() == null) return;

        String structOrUnion = ctx.structOrUnion().getText();
        String type = structOrUnion.equals("union") ? "UNION" : "STRUCT";

        if (!nodeStack.isEmpty() && nodeStack.peek().type.equals(type)) {
            Node node = nodeStack.peek();
            node.comment = ParserUtils.getLeadingComment(ctx, tokens);
            exitStatement(type, ctx.getStop().getLine(), ctx);
            propagateClassName(node, node.name);
        }
    }

    // ========================================
    // enum
    // ========================================

    @Override
    public void enterEnumSpecifier(CParser.EnumSpecifierContext ctx) {
        if (ctx.Identifier() == null) return;
        if (ctx.enumeratorList() == null) return;

        String name = ctx.Identifier().getText();
        Node node = enterStatement("ENUM", name, ctx.getStart().getLine());
        node.signature = ParserUtils.extractSignature(ctx, tokens, "{");
    }

    @Override
    public void exitEnumSpecifier(CParser.EnumSpecifierContext ctx) {
        if (ctx.Identifier() == null) return;
        if (ctx.enumeratorList() == null) return;

        exitStatement("ENUM", ctx.getStop().getLine(), ctx);
    }

    // ========================================
    // 함수 정의
    // ========================================

    @Override
    public void enterFunctionDefinition(CParser.FunctionDefinitionContext ctx) {
        String name = null;
        String returnType = null;

        // 함수 이름 추출: declarator → directDeclarator → Identifier
        if (ctx.declarator() != null && ctx.declarator().directDeclarator() != null) {
            CParser.DirectDeclaratorContext dd = ctx.declarator().directDeclarator();
            if (dd.Identifier() != null) {
                name = dd.Identifier().getText();
            }
        }

        // 리턴 타입 추출: declarationSpecifiers
        if (ctx.declarationSpecifiers() != null) {
            returnType = ctx.declarationSpecifiers().getText();
        }

        Node node = enterStatement("FUNCTION", name, ctx.getStart().getLine());
        node.signature = ParserUtils.extractSignature(ctx, tokens, "{");
        node.returnType = returnType;

        // 파라미터 추출
        if (ctx.declarator() != null && ctx.declarator().directDeclarator() != null) {
            CParser.DirectDeclaratorContext dd = ctx.declarator().directDeclarator();
            // directDeclarator에서 parameterTypeList 찾기
            if (dd.parameterTypeList() != null && !dd.parameterTypeList().isEmpty()) {
                CParser.ParameterTypeListContext params = dd.parameterTypeList(0);
                node.parameters = ParserUtils.getOriginalText(params, tokens);
            }
        }

        // storage class (static, extern 등) 추출
        if (ctx.declarationSpecifiers() != null) {
            StringBuilder modifiers = new StringBuilder();
            for (CParser.DeclarationSpecifierContext ds : ctx.declarationSpecifiers().declarationSpecifier()) {
                if (ds.storageClassSpecifier() != null) {
                    if (modifiers.length() > 0) modifiers.append(" ");
                    modifiers.append(ds.storageClassSpecifier().getText());
                }
            }
            if (modifiers.length() > 0) {
                node.modifiers = modifiers.toString();
            }
        }
    }

    @Override
    public void exitFunctionDefinition(CParser.FunctionDefinitionContext ctx) {
        exitStatement("FUNCTION", ctx.getStop().getLine(), ctx);
    }

    // ========================================
    // 전역 선언 (변수, typedef)
    // ========================================

    @Override
    public void enterDeclaration(CParser.DeclarationContext ctx) {
        if (ctx.declarationSpecifiers() == null) return;

        // 블록 내부의 declaration은 VARIABLE, 전역은 GLOBAL_VARIABLE 또는 TYPEDEF
        boolean isGlobal = isGlobalScope();

        // typedef 여부 확인
        boolean isTypedef = false;
        StringBuilder typeBuilder = new StringBuilder();
        StringBuilder modifierBuilder = new StringBuilder();

        for (CParser.DeclarationSpecifierContext ds : ctx.declarationSpecifiers().declarationSpecifier()) {
            if (ds.storageClassSpecifier() != null) {
                if (ds.storageClassSpecifier().getText().equals("typedef")) {
                    isTypedef = true;
                } else {
                    if (modifierBuilder.length() > 0) modifierBuilder.append(" ");
                    modifierBuilder.append(ds.storageClassSpecifier().getText());
                }
            } else if (ds.typeSpecifier() != null) {
                if (typeBuilder.length() > 0) typeBuilder.append(" ");
                typeBuilder.append(ds.typeSpecifier().getText());
            } else if (ds.typeQualifier() != null) {
                if (modifierBuilder.length() > 0) modifierBuilder.append(" ");
                modifierBuilder.append(ds.typeQualifier().getText());
            }
        }

        // initDeclaratorList에서 이름 추출
        if (ctx.initDeclaratorList() != null) {
            for (CParser.InitDeclaratorContext initDecl : ctx.initDeclaratorList().initDeclarator()) {
                String name = extractDeclaratorName(initDecl.declarator());
                if (name == null) continue;

                String nodeType;
                if (isTypedef) {
                    nodeType = "TYPEDEF";
                } else if (isGlobal) {
                    nodeType = "GLOBAL_VARIABLE";
                } else {
                    nodeType = "VARIABLE";
                }

                Node node = enterStatement(nodeType, name, ctx.getStart().getLine());
                node.fieldType = typeBuilder.length() > 0 ? typeBuilder.toString() : null;
                if (modifierBuilder.length() > 0) {
                    node.modifiers = modifierBuilder.toString();
                }

                // 즉시 닫기 (declaration은 한 줄)
                node.endLine = ctx.getStop().getLine();
                node.comment = ParserUtils.getLeadingComment(ctx, tokens);
                nodeStack.pop();
            }
        } else if (!isTypedef && typeBuilder.length() > 0) {
            // initDeclaratorList 없는 선언 (ex: struct 정의만)은 무시
        }
    }

    // ========================================
    // 함수 호출 (postfixExpression에서 '(' 감지)
    // ========================================

    @Override
    public void enterPostfixExpression(CParser.PostfixExpressionContext ctx) {
        // postfixExpression: (primaryExpression | ...) ( '[' ... | '(' argumentExpressionList? ')' | ... )*
        // 함수 호출 패턴: Identifier '(' ... ')'
        if (ctx.primaryExpression() == null) return;

        // '(' 토큰이 있는지 확인 (함수 호출)
        boolean hasFunctionCall = false;
        for (int i = 0; i < ctx.getChildCount(); i++) {
            if (ctx.getChild(i).getText().equals("(") && i > 0) {
                hasFunctionCall = true;
                break;
            }
        }
        if (!hasFunctionCall) return;

        // FUNCTION 노드 내부에서만 함수 호출 추출
        if (!isInsideFunction()) return;

        String name = null;
        if (ctx.primaryExpression().Identifier() != null) {
            name = ctx.primaryExpression().Identifier().getText();
        }

        Node node = enterStatement("FUNCTION_CALL", name, ctx.getStart().getLine());
        node.endLine = ctx.getStop().getLine();
    }

    @Override
    public void exitPostfixExpression(CParser.PostfixExpressionContext ctx) {
        exitStatement("FUNCTION_CALL", ctx.getStop().getLine(), ctx);
    }

    // ========================================
    // struct 멤버 필드
    // ========================================

    @Override
    public void enterMemberDeclaration(CParser.MemberDeclarationContext ctx) {
        // STRUCT/UNION 내부에서만
        if (!isInsideType("STRUCT") && !isInsideType("UNION")) return;
        if (ctx.specifierQualifierList() == null) return;
        if (ctx.memberDeclaratorList() == null) return;

        String fieldType = ParserUtils.getOriginalText(ctx.specifierQualifierList(), tokens);

        for (CParser.MemberDeclaratorContext md : ctx.memberDeclaratorList().memberDeclarator()) {
            String name = null;
            if (md.declarator() != null) {
                name = extractDeclaratorName(md.declarator());
            }

            Node node = new Node("FIELD", name, ctx.getStart().getLine(), nodeStack.peek());
            node.fieldType = fieldType;
            node.endLine = ctx.getStop().getLine();
            node.comment = ParserUtils.getLeadingComment(ctx, tokens);
        }
    }

    // ========================================
    // enum 상수
    // ========================================

    @Override
    public void enterEnumerator(CParser.EnumeratorContext ctx) {
        if (!isInsideType("ENUM")) return;

        String name = ctx.enumerationConstant() != null ? ctx.enumerationConstant().getText() : null;
        Node node = new Node("ENUM_CONSTANT", name, ctx.getStart().getLine(), nodeStack.peek());
        node.endLine = ctx.getStop().getLine();
        node.comment = ParserUtils.getLeadingComment(ctx, tokens);
    }

    // ========================================
    // 유틸리티
    // ========================================

    /**
     * declarator에서 이름(Identifier) 추출
     */
    private String extractDeclaratorName(CParser.DeclaratorContext ctx) {
        if (ctx == null) return null;
        CParser.DirectDeclaratorContext dd = ctx.directDeclarator();
        if (dd == null) return null;
        // 중첩 declarator (포인터 등)
        if (dd.declarator() != null) {
            return extractDeclaratorName(dd.declarator());
        }
        if (dd.Identifier() != null) {
            return dd.Identifier().getText();
        }
        return null;
    }

    /**
     * 현재 스코프가 전역(FILE 직접 하위)인지 확인
     */
    private boolean isGlobalScope() {
        if (nodeStack.isEmpty()) return true;
        Node current = nodeStack.peek();
        return current.type.equals("FILE");
    }

    /**
     * 현재 FUNCTION 내부에 있는지 확인
     */
    private boolean isInsideFunction() {
        for (int i = nodeStack.size() - 1; i >= 0; i--) {
            if (nodeStack.get(i).type.equals("FUNCTION")) return true;
        }
        return false;
    }

    /**
     * 특정 타입의 노드 내부에 있는지 확인
     */
    private boolean isInsideType(String type) {
        for (int i = nodeStack.size() - 1; i >= 0; i--) {
            if (nodeStack.get(i).type.equals(type)) return true;
        }
        return false;
    }

    /**
     * 클래스명(구조체명)을 모든 자식 노드에 재귀적으로 전파
     */
    private void propagateClassName(Node node, String className) {
        for (Node child : node.children) {
            child.className = className;
            propagateClassName(child, className);
        }
    }

    // ========================================
    // 디버깅용
    // ========================================

    public void printTree(Node node, String indent) {
        StringBuilder info = new StringBuilder();
        info.append(indent).append(node.type);
        if (node.name != null) info.append(" [").append(node.name).append("]");
        if (node.modifiers != null) info.append(" {").append(node.modifiers).append("}");
        if (node.fieldType != null) info.append(" type:").append(node.fieldType);
        if (node.returnType != null) info.append(" returns:").append(node.returnType);
        info.append(" (").append(node.startLine).append("-").append(node.endLine).append(")");

        System.out.println(info.toString());
        for (Node child : node.children) {
            printTree(child, indent + "  ");
        }
    }

    public void printStructure() {
        printTree(root, "");
    }
}
