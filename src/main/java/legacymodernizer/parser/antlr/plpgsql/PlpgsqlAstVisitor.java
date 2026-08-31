package legacymodernizer.parser.antlr.plpgsql;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import java.util.HashMap;
import java.util.Map;
import legacymodernizer.parser.model.Node;
import legacymodernizer.parser.antlr.ParserUtils;

/**
 * PL/pgSQL Parse Tree를 Node 트리로 변환하는 Visitor
 * - 통일된 속성명 사용 (Node 클래스 참조)
 */
public class PlpgsqlAstVisitor extends PlpgsqlParserBaseVisitor<Node> {

    private Node parentNode;
    private int baseLineNumber;
    private CommonTokenStream tokens;
    private Node currentBlockNode;
    private Map<String, CursorInfo> activeCursors = new HashMap<>();
    
    private static class CursorInfo {
        Node cursorNode;
        Node previousBlock;
        
        CursorInfo(Node cursorNode, Node previousBlock) {
            this.cursorNode = cursorNode;
            this.previousBlock = previousBlock;
        }
    }

    public PlpgsqlAstVisitor(Node parentNode, int baseLineNumber, CommonTokenStream tokens) {
        this.parentNode = parentNode;
        this.baseLineNumber = baseLineNumber;
        this.tokens = tokens;
    }

    // ========================================
    // 유틸리티 메서드
    // ========================================

    private int getActualLineNumber(ParserRuleContext ctx) {
        return baseLineNumber + ctx.getStart().getLine();
    }
    
    private int getActualLineNumber(Token token) {
        return baseLineNumber + token.getLine();
    }
    
    private int getActualEndLineNumber(ParserRuleContext ctx) {
        return baseLineNumber + ctx.getStop().getLine();
    }

    private Node createNode(String type, ParserRuleContext ctx, Node parent) {
        return createNode(type, null, ctx, parent);
    }
    
    private Node createNode(String type, String name, ParserRuleContext ctx, Node parent) {
        int startLine = getActualLineNumber(ctx);
        Node node = new Node(type, name, startLine, parent);
        node.endLine = getActualEndLineNumber(ctx);
        return node;
    }

    // ========================================
    // 블록 처리
    // ========================================

    @Override
    public Node visitPlpgsqlBlock(PlpgsqlParser.PlpgsqlBlockContext ctx) {
        if (currentBlockNode == null) {
            currentBlockNode = parentNode;
        }

        Node previousBlock = currentBlockNode;
        
        if (ctx.declareSection() != null) {
            visit(ctx.declareSection());
        }

        Node beginNode = null;
        
        if (ctx.BEGIN() != null) {
            int beginStartLine = getActualLineNumber(ctx.BEGIN().getSymbol());
            int beginEndLine = getActualEndLineNumber(ctx);
            
            beginNode = new Node("BEGIN", beginStartLine, previousBlock);
            beginNode.endLine = beginEndLine;
            
            currentBlockNode = beginNode;
            
            if (ctx.statementList() != null) {
                visitStatementList(ctx.statementList());
            }

            if (ctx.exceptionSection() != null) {
                visit(ctx.exceptionSection());
            }

            currentBlockNode = previousBlock;
        } else {
            if (ctx.statementList() != null) {
                visitStatementList(ctx.statementList());
            }
            
            if (ctx.exceptionSection() != null) {
                visit(ctx.exceptionSection());
            }
        }
        
        return beginNode;
    }

    @Override
    public Node visitDeclareSection(PlpgsqlParser.DeclareSectionContext ctx) {
        int declStartLine = getActualLineNumber(ctx.DECLARE().getSymbol());
        int declEndLine = getActualEndLineNumber(ctx.declarationList().declaration(ctx.declarationList().declaration().size() - 1));
        
        Node declareNode = new Node("DECLARE", declStartLine, currentBlockNode);
        declareNode.endLine = declEndLine;

        for (PlpgsqlParser.DeclarationContext declCtx : ctx.declarationList().declaration()) {
            visitDeclaration(declCtx, declareNode);
        }

        return declareNode;
    }

    private void visitDeclaration(PlpgsqlParser.DeclarationContext ctx, Node parent) {
        // DECLARE 블록 안 변수마다 VARIABLE 노드 생성
        if (ctx.Identifier() == null || ctx.Identifier().isEmpty()) return;
        String varName = ctx.Identifier(0).getText();
        Node v = new Node("VARIABLE", varName, getActualLineNumber(ctx.Identifier(0).getSymbol()), parent);
        v.endLine = getActualEndLineNumber(ctx);
        if (ctx.dataType() != null) {
            v.variableType = ctx.dataType().getText();
        }
        // 초기화 표현식 (`:= 우변` 또는 `DEFAULT 우변`) 텍스트 보존 — 식별자 추출은 Analyzer 책임
        if (ctx.expression() != null) {
            v.initValue = ParserUtils.getOriginalText(ctx.expression(), tokens);
        }
    }

    @Override
    public Node visitStatementList(PlpgsqlParser.StatementListContext ctx) {
        if (ctx.statement() == null) return null;

        for (PlpgsqlParser.StatementContext stmtCtx : ctx.statement()) {
            visit(stmtCtx);
        }

        return null;
    }

    // ========================================
    // DML
    // ========================================

    @Override
    public Node visitAssignmentStmt(PlpgsqlParser.AssignmentStmtContext ctx) {
        Node node = createNode("ASSIGNMENT", ctx, currentBlockNode);
        // 좌변·연산자·우변 원문을 명시 필드로 보존 — downstream 이 소스를 재파싱하지
        // 않는다(spec 016 FR-003). 기존 initializer-call 자식 emit 은 그대로 유지.
        node.target = ParserUtils.getExactSourceText(ctx.variableRef());
        node.operator = ctx.ASSIGN() != null ? ctx.ASSIGN().getText() : null;
        if (ctx.expression() != null) {
            node.expression = ParserUtils.getExactSourceText(ctx.expression());
            ParserUtils.emitInitializerCall(
                    node, ctx.expression().getText(), node.startLine, node.endLine);
        }
        return node;
    }

    @Override
    public Node visitInsertStmt(PlpgsqlParser.InsertStmtContext ctx) {
        return createNode("INSERT", ctx, currentBlockNode);
    }

    @Override
    public Node visitUpdateStmt(PlpgsqlParser.UpdateStmtContext ctx) {
        return createNode("UPDATE", ctx, currentBlockNode);
    }

    @Override
    public Node visitDeleteStmt(PlpgsqlParser.DeleteStmtContext ctx) {
        return createNode("DELETE", ctx, currentBlockNode);
    }

    @Override
    public Node visitRaiseStmt(PlpgsqlParser.RaiseStmtContext ctx) {
        return createNode("NOTICE", ctx, currentBlockNode);
    }

    @Override
    public Node visitReturnStmt(PlpgsqlParser.ReturnStmtContext ctx) {
        String returnType = "RETURN";
        if (ctx.NEXT() != null) {
            returnType = "RETURN_NEXT";
        } else if (ctx.QUERY() != null) {
            returnType = "RETURN_QUERY";
        }
        Node node = createNode(returnType, ctx, currentBlockNode);
        if (ctx.expression() != null) {
            // 반환식 원문을 expression 필드로 보존 — downstream 이 소스를 재파싱하지
            // 않는다(spec 016 FR-003). 기존 initializer-call 자식 emit 은 그대로 유지.
            node.expression = ParserUtils.getExactSourceText(ctx.expression());
            ParserUtils.emitInitializerCall(
                    node, ctx.expression().getText(), node.startLine, node.endLine);
        }
        return node;
    }

    // ========================================
    // 커서
    // ========================================
    
    private String extractCursorName(ParserRuleContext ctx) {
        if (ctx == null) return null;
        if (ctx.getChildCount() >= 2) {
            return ctx.getChild(1).getText();
        }
        return null;
    }

    @Override
    public Node visitOpenCursorStmt(PlpgsqlParser.OpenCursorStmtContext ctx) {
        String cursorName = extractCursorName(ctx);
        
        Node cursorNode = createNode("CURSOR", cursorName, ctx, currentBlockNode);
        Node previousBlock = currentBlockNode;
        
        if (cursorName != null) {
            activeCursors.put(cursorName, new CursorInfo(cursorNode, previousBlock));
        }
        
        currentBlockNode = cursorNode;
        
        return cursorNode;
    }

    @Override
    public Node visitCloseCursorStmt(PlpgsqlParser.CloseCursorStmtContext ctx) {
        String cursorName = extractCursorName(ctx);
        
        if (cursorName != null && activeCursors.containsKey(cursorName)) {
            CursorInfo cursorInfo = activeCursors.get(cursorName);
            cursorInfo.cursorNode.endLine = getActualEndLineNumber(ctx);
            currentBlockNode = cursorInfo.previousBlock;
            activeCursors.remove(cursorName);
        }
        
        return null;
    }

    @Override
    public Node visitFetchStmt(PlpgsqlParser.FetchStmtContext ctx) {
        return createNode("FETCH", ctx, currentBlockNode);
    }

    @Override
    public Node visitSelectIntoStmt(PlpgsqlParser.SelectIntoStmtContext ctx) {
        return createNode("SELECT", ctx, currentBlockNode);
    }

    // ========================================
    // 제어 흐름
    // ========================================

    @Override
    public Node visitIfStmt(PlpgsqlParser.IfStmtContext ctx) {
        Node ifNode = createNode("IF", ctx, currentBlockNode);
        // IF 조건 expression(0) 의 호출을 FUNCTION_CALL 노드로 emit
        if (ctx.expression() != null && !ctx.expression().isEmpty()) {
            // 조건식 원문 보존 (spec 016 FR-003)
            ifNode.expression = ParserUtils.getExactSourceText(ctx.expression(0));
            ParserUtils.emitInitializerCall(
                    ifNode, ctx.expression(0).getText(), ifNode.startLine, ifNode.endLine);
        }
        Node previousBlock = currentBlockNode;
        currentBlockNode = ifNode;

        // THEN 절
        if (ctx.statementList() != null && !ctx.statementList().isEmpty()) {
            visitStatementList(ctx.statementList(0));
        }

        // ELSIF 절들
        int elsifCount = ctx.expression().size() - 1;
        if (elsifCount > 0 && ctx.ELSIF() != null) {
            for (int i = 0; i < elsifCount; i++) {
                PlpgsqlParser.StatementListContext elsifStmtList = ctx.statementList(i + 1);

                int elsifStartLine = getActualLineNumber(ctx.ELSIF(i).getSymbol());
                int elsifEndLine = getActualEndLineNumber(elsifStmtList);

                Node elsifNode = new Node("ELSIF", elsifStartLine, ifNode);
                elsifNode.endLine = elsifEndLine;
                // 조건식 원문 보존 (spec 016 FR-003)
                elsifNode.expression = ParserUtils.getExactSourceText(ctx.expression(i + 1));
                // ELSIF 조건 expression(i+1) 의 호출을 FUNCTION_CALL 노드로 emit
                ParserUtils.emitInitializerCall(
                        elsifNode, ctx.expression(i + 1).getText(),
                        elsifNode.startLine, elsifNode.endLine);

                currentBlockNode = elsifNode;
                visitStatementList(elsifStmtList);
            }
        }

        // ELSE 절
        if (ctx.ELSE() != null) {
            PlpgsqlParser.StatementListContext elseStmtList = ctx.statementList(ctx.statementList().size() - 1);

            int elseStartLine = getActualLineNumber(ctx.ELSE().getSymbol());
            int elseEndLine = getActualEndLineNumber(elseStmtList);

            Node elseNode = new Node("ELSE", elseStartLine, ifNode);
            elseNode.endLine = elseEndLine;

            currentBlockNode = elseNode;
            visitStatementList(elseStmtList);
        }

        currentBlockNode = previousBlock;
        return ifNode;
    }

    @Override
    public Node visitLoopStmt(PlpgsqlParser.LoopStmtContext ctx) {
        Node loopNode = createNode("LOOP", ctx, currentBlockNode);
        Node previousBlock = currentBlockNode;
        currentBlockNode = loopNode;

        visitStatementList(ctx.statementList());

        currentBlockNode = previousBlock;
        return loopNode;
    }

    @Override
    public Node visitWhileStmt(PlpgsqlParser.WhileStmtContext ctx) {
        Node whileNode = createNode("WHILE", ctx, currentBlockNode);
        // 판정절 원문 보존 (spec 016 FR-003)
        whileNode.expression = ParserUtils.getExactSourceText(ctx.expression());
        Node previousBlock = currentBlockNode;
        currentBlockNode = whileNode;

        visitStatementList(ctx.statementList());

        currentBlockNode = previousBlock;
        return whileNode;
    }

    @Override
    public Node visitForStmt(PlpgsqlParser.ForStmtContext ctx) {
        Node forNode = createNode("FOR", ctx, currentBlockNode);
        Node previousBlock = currentBlockNode;
        currentBlockNode = forNode;

        visitStatementList(ctx.statementList());

        currentBlockNode = previousBlock;
        return forNode;
    }

    @Override
    public Node visitForeachStmt(PlpgsqlParser.ForeachStmtContext ctx) {
        Node foreachNode = createNode("FOREACH", ctx, currentBlockNode);
        Node previousBlock = currentBlockNode;
        currentBlockNode = foreachNode;

        visitStatementList(ctx.statementList());

        currentBlockNode = previousBlock;
        return foreachNode;
    }

    @Override
    public Node visitExitStmt(PlpgsqlParser.ExitStmtContext ctx) {
        return createNode("EXIT", ctx, currentBlockNode);
    }

    @Override
    public Node visitContinueStmt(PlpgsqlParser.ContinueStmtContext ctx) {
        return createNode("CONTINUE", ctx, currentBlockNode);
    }

    // ========================================
    // 기타 문장
    // ========================================

    @Override
    public Node visitSetStmt(PlpgsqlParser.SetStmtContext ctx) {
        return createNode("SET", ctx, currentBlockNode);
    }

    @Override
    public Node visitExecuteStmt(PlpgsqlParser.ExecuteStmtContext ctx) {
        return createNode("EXECUTE", ctx, currentBlockNode);
    }

    @Override
    public Node visitPerformStmt(PlpgsqlParser.PerformStmtContext ctx) {
        return createNode("PERFORM", ctx, currentBlockNode);
    }
    
    @Override
    public Node visitCommitStmt(PlpgsqlParser.CommitStmtContext ctx) {
        return createNode("COMMIT", ctx, currentBlockNode);
    }
    
    @Override
    public Node visitRollbackStmt(PlpgsqlParser.RollbackStmtContext ctx) {
        return createNode("ROLLBACK", ctx, currentBlockNode);
    }

    @Override
    public Node visitNestedBlock(PlpgsqlParser.NestedBlockContext ctx) {
        Node previousBlock = currentBlockNode;
        
        if (ctx.declarationList() != null) {
            PlpgsqlParser.DeclarationListContext declList = ctx.declarationList();

            int declStartLine;
            if (ctx.DECLARE() != null) {
                declStartLine = getActualLineNumber(ctx.DECLARE().getSymbol());
            } else {
                declStartLine = getActualLineNumber(declList);
            }
            int declEndLine = getActualEndLineNumber(declList.declaration(declList.declaration().size() - 1));

            Node declareNode = new Node("DECLARE", declStartLine, currentBlockNode);
            declareNode.endLine = declEndLine;

            for (PlpgsqlParser.DeclarationContext declCtx : declList.declaration()) {
                visitDeclaration(declCtx, declareNode);
            }
        }

        int beginStartLine = getActualLineNumber(ctx.BEGIN().getSymbol());
        int beginEndLine = getActualEndLineNumber(ctx);
        
        Node beginNode = new Node("BEGIN", beginStartLine, currentBlockNode);
        beginNode.endLine = beginEndLine;
        
        currentBlockNode = beginNode;
        
        visitStatementList(ctx.statementList());

        if (ctx.exceptionSection() != null) {
            visitExceptionSection(ctx.exceptionSection());
        }
        
        currentBlockNode = previousBlock;
        return beginNode;
    }

    @Override
    public Node visitExceptionSection(PlpgsqlParser.ExceptionSectionContext ctx) {
        return createNode("EXCEPTION", ctx, currentBlockNode);
    }
    
    @Override
    public Node visitCaseStmt(PlpgsqlParser.CaseStmtContext ctx) {
        Node caseNode = createNode("CASE", ctx, currentBlockNode);
        // selector 식 원문 보존 (spec 016 FR-003) — searched CASE 는 null.
        caseNode.expression = ParserUtils.getExactSourceText(ctx.expression());
        Node previousBlock = currentBlockNode;
        currentBlockNode = caseNode;

        // WHEN 절들
        int whenCount = ctx.whenClauseList().whenClause().size();
        for (int i = 0; i < whenCount; i++) {
            PlpgsqlParser.WhenClauseContext when = ctx.whenClauseList().whenClause(i);
            PlpgsqlParser.StatementListContext whenStmtList = when.statementList();
            int whenStartLine = getActualLineNumber(when);
            int whenEndLine = getActualEndLineNumber(whenStmtList);

            Node whenNode = new Node("WHEN", whenStartLine, caseNode);
            whenNode.endLine = whenEndLine;
            // WHEN 판정식 원문 보존 (spec 016 FR-003)
            whenNode.expression = ParserUtils.getExactSourceText(when.expression());

            currentBlockNode = whenNode;
            visitStatementList(whenStmtList);
        }
        
        // ELSE 절
        if (ctx.ELSE() != null) {
            PlpgsqlParser.StatementListContext elseStmtList = ctx.statementList();
            int elseStartLine = getActualLineNumber(ctx.ELSE().getSymbol());
            int elseEndLine = getActualEndLineNumber(elseStmtList);
            
            Node elseNode = new Node("ELSE", elseStartLine, caseNode);
            elseNode.endLine = elseEndLine;
            
            currentBlockNode = elseNode;
            visitStatementList(elseStmtList);
        }
        
        currentBlockNode = previousBlock;
        return caseNode;
    }
    
    @Override
    public Node visitAssertStmt(PlpgsqlParser.AssertStmtContext ctx) {
        return createNode("ASSERT", ctx, currentBlockNode);
    }
    
    @Override
    public Node visitNullStmt(PlpgsqlParser.NullStmtContext ctx) {
        return createNode("NULL", ctx, currentBlockNode);
    }
    
    @Override
    public Node visitGetDiagnosticsStmt(PlpgsqlParser.GetDiagnosticsStmtContext ctx) {
        return createNode("GET_DIAGNOSTICS", ctx, currentBlockNode);
    }
    
    @Override
    public Node visitCreateTempTableStmt(PlpgsqlParser.CreateTempTableStmtContext ctx) {
        return createNode("CREATE_TEMP_TABLE", ctx, currentBlockNode);
    }
    
    @Override
    public Node visitCteStmt(PlpgsqlParser.CteStmtContext ctx) {
        return createNode("CTE", ctx, currentBlockNode);
    }
    
    @Override
    public Node visitFunctionCall(PlpgsqlParser.FunctionCallContext ctx) {
        java.util.List<PlpgsqlParser.CallableNamePartContext> path =
                ctx.callableName().callableNamePart();
        String functionName = path.get(path.size() - 1).getText();
        return createNode("CALL", functionName, ctx, currentBlockNode);
    }
    
    @Override
    public Node visitSqlGenericStmt(PlpgsqlParser.SqlGenericStmtContext ctx) {
        // CALL 문 처리
        if (ctx.CALL() != null) {
            String name = null;
            // 호출 대상 이름 추출 시도
            if (ctx.getChildCount() > 1) {
                name = ctx.getChild(1).getText();
            }
            return createNode("CALL", name, ctx, currentBlockNode);
        }
        
        String stmtText = ctx.getText().toUpperCase();
        
        if (stmtText.contains("CREATEINDEX")) {
            return createNode("CREATE_INDEX", ctx, currentBlockNode);
        }

        if (stmtText.contains("DROPTABLE")) {
            return createNode("DROP_TABLE", ctx, currentBlockNode);
        }
        
        return createNode("SQL_GENERIC", ctx, currentBlockNode);
    }
}
