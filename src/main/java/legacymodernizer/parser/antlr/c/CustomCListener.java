package legacymodernizer.parser.antlr.c;

import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    /**
     * typedef struct { ... } Name; 패턴에서 Name을 임시 보관.
     * enterDeclaration이 먼저 실행되어 이름을 저장하면,
     * 이후 enterStructOrUnionSpecifier / enterEnumSpecifier 에서 사용한다.
     */
    private String pendingTypedefName = null;

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
                // #define 대문자 상수를 DEFINE으로 추출
                if (text.startsWith("#define") || text.startsWith("# define")) {
                    Matcher m = Pattern
                        .compile("^#\\s*define\\s+([A-Z_][A-Z0-9_]*)\\s+([-+]?(?:0[xX][0-9a-fA-F]+|\\d+(?:\\.\\d+)?))")
                        .matcher(text);
                    if (m.find()) {
                        Node node = new Node("DEFINE", m.group(1), token.getLine(), root);
                        node.endLine = token.getLine();
                        node.variableType = m.group(2);
                    }
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
                node.comment = ParserUtils.getComment(ctx, tokens);
            }
        }
    }

    // ========================================
    // struct / union
    // ========================================

    @Override
    public void enterStructOrUnionSpecifier(CParser.StructOrUnionSpecifierContext ctx) {
        // 본문이 없으면 (forward declaration 또는 타입 참조) 무시
        if (ctx.memberDeclarationList() == null) return;

        String structOrUnion = ctx.structOrUnion().getText(); // "struct" or "union"
        String type = structOrUnion.equals("union") ? "UNION" : "STRUCT";

        String name;
        if (ctx.Identifier() != null) {
            // Named struct: struct Subscriber { ... };
            name = ctx.Identifier().getText();
        } else if (pendingTypedefName != null) {
            // Anonymous typedef struct: typedef struct { ... } Subscriber;
            name = pendingTypedefName;
        } else {
            return; // 이름 없는 익명 struct (typedef도 아님) → 무시
        }

        Node node = enterStatement(type, name, ctx.getStart().getLine());
        node.signature = ParserUtils.extractSignature(ctx, tokens, "{");
    }

    @Override
    public void exitStructOrUnionSpecifier(CParser.StructOrUnionSpecifierContext ctx) {
        if (ctx.memberDeclarationList() == null) return;

        String structOrUnion = ctx.structOrUnion().getText();
        String type = structOrUnion.equals("union") ? "UNION" : "STRUCT";

        if (!nodeStack.isEmpty() && nodeStack.peek().type.equals(type)) {
            Node node = nodeStack.peek();
            node.comment = ParserUtils.getComment(ctx, tokens);
            exitStatement(type, ctx.getStop().getLine(), ctx);
            propagateModuleName(node, node.name);
        }
        // typedef struct 처리 완료 시 pending 이름 초기화
        pendingTypedefName = null;
    }

    // ========================================
    // enum
    // ========================================

    @Override
    public void enterEnumSpecifier(CParser.EnumSpecifierContext ctx) {
        if (ctx.enumeratorList() == null) return; // body 없으면 무시

        String name;
        if (ctx.Identifier() != null) {
            // Named enum: enum UsageType { ... };
            name = ctx.Identifier().getText();
        } else if (pendingTypedefName != null) {
            // Anonymous typedef enum: typedef enum { ... } UsageType;
            name = pendingTypedefName;
        } else {
            return; // 이름 없는 익명 enum → 무시
        }

        Node node = enterStatement("ENUM", name, ctx.getStart().getLine());
        node.signature = ParserUtils.extractSignature(ctx, tokens, "{");
    }

    @Override
    public void exitEnumSpecifier(CParser.EnumSpecifierContext ctx) {
        if (ctx.enumeratorList() == null) return;

        exitStatement("ENUM", ctx.getStop().getLine(), ctx);
        // typedef enum 처리 완료 시 pending 이름 초기화
        pendingTypedefName = null;
    }

    // ========================================
    // 함수 정의
    // ========================================

    @Override
    public void enterFunctionDefinition(CParser.FunctionDefinitionContext ctx) {
        // .h 파일에서는 함수 정의 노드를 생성하지 않음 (중복 방지)
        if (isHeaderFile()) return;

        String name = null;

        // 함수 이름 추출: declarator → directDeclarator → Identifier
        if (ctx.declarator() != null && ctx.declarator().directDeclarator() != null) {
            CParser.DirectDeclaratorContext dd = ctx.declarator().directDeclarator();
            if (dd.Identifier() != null) {
                name = dd.Identifier().getText();
            }
        }

        // returnType, modifiers 분리 추출
        String returnType = extractReturnType(ctx.declarationSpecifiers());
        String modifiers = extractModifiers(ctx.declarationSpecifiers());

        // 포인터 * 확인 (declarator에 pointer가 있으면 리턴 타입에 포함)
        if (ctx.declarator() != null && !ctx.declarator().pointer().isEmpty()) {
            returnType = returnType + " *";
        }

        Node node = enterStatement("FUNCTION", name, ctx.getStart().getLine());
        node.signature = ParserUtils.extractSignature(ctx, tokens, "{");
        node.returnType = returnType != null ? returnType.trim() : null;
        if (modifiers != null) {
            node.modifiers = modifiers;
        }

        // 파라미터 추출
        if (ctx.declarator() != null && ctx.declarator().directDeclarator() != null) {
            CParser.DirectDeclaratorContext dd = ctx.declarator().directDeclarator();
            if (dd.parameterTypeList() != null && !dd.parameterTypeList().isEmpty()) {
                CParser.ParameterTypeListContext params = dd.parameterTypeList(0);
                node.parameters = ParserUtils.getOriginalText(params, tokens);
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
        boolean hasStructOrEnumWithBody = false;

        for (CParser.DeclarationSpecifierContext ds : ctx.declarationSpecifiers().declarationSpecifier()) {
            if (ds.storageClassSpecifier() != null && ds.storageClassSpecifier().getText().equals("typedef")) {
                isTypedef = true;
            }
            if (ds.typeSpecifier() != null) {
                CParser.TypeSpecifierContext ts = ds.typeSpecifier();
                if (ts.structOrUnionSpecifier() != null
                        && ts.structOrUnionSpecifier().memberDeclarationList() != null) {
                    hasStructOrEnumWithBody = true;
                }
                if (ts.enumSpecifier() != null
                        && ts.enumSpecifier().enumeratorList() != null) {
                    hasStructOrEnumWithBody = true;
                }
            }
        }

        // returnType/modifiers를 공통 메서드로 추출
        String variableType = extractReturnType(ctx.declarationSpecifiers());
        String modifierStr = extractModifiers(ctx.declarationSpecifiers());
        // typedef는 modifiers에서 제외 (별도 처리)
        if (modifierStr != null) {
            modifierStr = modifierStr.replace("typedef", "").trim();
            if (modifierStr.isEmpty()) modifierStr = null;
        }

        // typedef struct { ... } Name; / typedef enum { ... } Name; 패턴:
        // TYPEDEF 노드를 만들지 않고, 이름만 저장하여 struct/enum 핸들러에서 사용
        if (isTypedef && hasStructOrEnumWithBody) {
            if (ctx.initDeclaratorList() != null) {
                // typedef struct { ... } Name; → initDeclaratorList에서 이름 추출
                for (CParser.InitDeclaratorContext initDecl : ctx.initDeclaratorList().initDeclarator()) {
                    pendingTypedefName = extractDeclaratorName(initDecl.declarator());
                    break;
                }
            } else {
                // ANTLR이 typedef 이름을 typeSpecifier(typedefName)로 파싱한 경우
                // declarationSpecifiers의 마지막 typedefName에서 이름 추출
                List<CParser.DeclarationSpecifierContext> specs = ctx.declarationSpecifiers().declarationSpecifier();
                for (int i = specs.size() - 1; i >= 0; i--) {
                    CParser.DeclarationSpecifierContext ds = specs.get(i);
                    if (ds.typeSpecifier() != null && ds.typeSpecifier().typedefName() != null) {
                        pendingTypedefName = ds.typeSpecifier().typedefName().getText();
                        break;
                    }
                }
            }
            if (pendingTypedefName != null) {
                return; // TYPEDEF 노드 생성 안 함 → struct/enum 핸들러가 처리
            }
        }

        // initDeclaratorList에서 이름 추출
        if (ctx.initDeclaratorList() != null) {
            for (CParser.InitDeclaratorContext initDecl : ctx.initDeclaratorList().initDeclarator()) {
                String name = extractDeclaratorName(initDecl.declarator());
                if (name == null) continue;

                // 함수 프로토타입 감지: declarator에 parameterTypeList가 있으면 함수 선언
                boolean isFunctionPrototype = false;
                String parameters = null;
                if (initDecl.declarator() != null && initDecl.declarator().directDeclarator() != null) {
                    CParser.DirectDeclaratorContext dd = initDecl.declarator().directDeclarator();
                    if (dd.parameterTypeList() != null && !dd.parameterTypeList().isEmpty()) {
                        isFunctionPrototype = true;
                        parameters = ParserUtils.getOriginalText(dd.parameterTypeList(0), tokens);
                    }
                }

                // 포인터 확인
                String actualVariableType = variableType;
                if (initDecl.declarator() != null && !initDecl.declarator().pointer().isEmpty()) {
                    actualVariableType = (actualVariableType != null ? actualVariableType : "") + " *";
                    actualVariableType = actualVariableType.trim();
                }

                boolean isConst = actualVariableType != null && actualVariableType.contains("const");
                String nodeType;
                if (isTypedef) {
                    nodeType = "TYPEDEF";
                } else if (isFunctionPrototype && isGlobal) {
                    if (isHeaderFile()) continue;
                    nodeType = "FUNCTION";
                } else if (isConst) {
                    nodeType = "CONSTANT_FIELD";
                } else if (isGlobal) {
                    nodeType = "GLOBAL_VARIABLE";
                } else {
                    nodeType = "VARIABLE";
                }

                Node node = enterStatement(nodeType, name, ctx.getStart().getLine());
                node.variableType = actualVariableType;
                if (isFunctionPrototype) {
                    node.returnType = actualVariableType;
                    node.parameters = parameters;
                    node.variableType = null;
                }
                if (modifierStr != null) {
                    node.modifiers = modifierStr;
                }
                // 초기화식 플래그: = 오른쪽에 함수 호출이 있는지 판별
                if (!isFunctionPrototype && !isTypedef && initDecl.initializer() != null) {
                    String initText = ParserUtils.getOriginalText(initDecl.initializer(), tokens);
                    if (initText != null && initText.matches("(?s).*\\w+\\s*\\(.*")) {
                        node.initializerContainsMethodCall = true;
                    }
                }

                // 즉시 닫기 (declaration은 한 줄)
                node.endLine = ctx.getStop().getLine();
                node.comment = ParserUtils.getComment(ctx, tokens);
                nodeStack.pop();
            }
        } else if (!isTypedef && variableType != null) {
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

        // 함수 호출 이름 추출: '->' 또는 '.' 멤버 접근 시 실제 호출 대상 추출
        // 예: svc->handler(req, reply) → name="handler", printf("hello") → name="printf"
        String name = null;
        for (int j = 0; j < ctx.getChildCount(); j++) {
            if (ctx.getChild(j).getText().equals("(") && j > 0) {
                // 멤버 접근 호출: svc->handler(), obj.func()
                if (j >= 2) {
                    String op = ctx.getChild(j - 2).getText();
                    if (".".equals(op) || "->".equals(op)) {
                        name = ctx.getChild(j - 1).getText();
                        break;
                    }
                }
                // 일반 호출: printf(), init_fn()
                if (ctx.primaryExpression().Identifier() != null) {
                    name = ctx.primaryExpression().Identifier().getText();
                }
                break;
            }
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

        String variableType = ParserUtils.getOriginalText(ctx.specifierQualifierList(), tokens);

        for (CParser.MemberDeclaratorContext md : ctx.memberDeclaratorList().memberDeclarator()) {
            String name = null;
            if (md.declarator() != null) {
                name = extractDeclaratorName(md.declarator());
            }

            Node node = new Node("MEMBER", name, ctx.getStart().getLine(), nodeStack.peek());
            node.variableType = variableType;
            node.endLine = ctx.getStop().getLine();
            node.comment = ParserUtils.getComment(ctx, tokens);
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
        node.comment = ParserUtils.getComment(ctx, tokens);
    }

    // ========================================
    // 유틸리티
    // ========================================

    /**
     * declarationSpecifiers에서 returnType 추출
     * storage class(static, extern 등)를 제외한 타입 지정자만 추출하며
     * 원본 공백을 유지 (struct tm 등)
     */
    private String extractReturnType(CParser.DeclarationSpecifiersContext specs) {
        if (specs == null) return null;
        StringBuilder sb = new StringBuilder();
        for (CParser.DeclarationSpecifierContext ds : specs.declarationSpecifier()) {
            if (ds.storageClassSpecifier() != null) continue; // static, extern 등 제외
            if (ds.typeSpecifier() != null) {
                if (sb.length() > 0) sb.append(" ");
                sb.append(ParserUtils.getOriginalText(ds.typeSpecifier(), tokens));
            } else if (ds.typeQualifier() != null) {
                if (sb.length() > 0) sb.append(" ");
                sb.append(ds.typeQualifier().getText());
            } else if (ds.functionSpecifier() != null) {
                // WINAPI 등 calling convention은 returnType에 포함
                if (sb.length() > 0) sb.append(" ");
                sb.append(ds.functionSpecifier().getText());
            }
        }
        return sb.length() > 0 ? sb.toString() : null;
    }

    /**
     * declarationSpecifiers에서 modifiers 추출
     * storage class(static, extern 등)만 추출
     */
    private String extractModifiers(CParser.DeclarationSpecifiersContext specs) {
        if (specs == null) return null;
        StringBuilder sb = new StringBuilder();
        for (CParser.DeclarationSpecifierContext ds : specs.declarationSpecifier()) {
            if (ds.storageClassSpecifier() != null) {
                if (sb.length() > 0) sb.append(" ");
                sb.append(ds.storageClassSpecifier().getText());
            }
        }
        return sb.length() > 0 ? sb.toString() : null;
    }

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
     * 현재 파일이 헤더(.h) 파일인지 확인
     */
    private boolean isHeaderFile() {
        return root.fileName != null && root.fileName.endsWith(".h");
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
     * 모듈명(구조체명 등)을 모든 자식 노드에 재귀적으로 전파
     */
    private void propagateModuleName(Node node, String moduleName) {
        for (Node child : node.children) {
            child.moduleName = moduleName;
            propagateModuleName(child, moduleName);
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
        if (node.variableType != null) info.append(" type:").append(node.variableType);
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
