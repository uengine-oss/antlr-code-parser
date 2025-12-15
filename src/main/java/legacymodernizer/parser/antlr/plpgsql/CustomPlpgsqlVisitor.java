package legacymodernizer.parser.antlr.plpgsql;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import java.util.HashMap;
import java.util.Map;
import legacymodernizer.parser.antlr.Node;

/**
 * PL/pgSQL Parse Tree를 Node 트리로 변환하는 Visitor
 */
public class CustomPlpgsqlVisitor extends PlpgsqlParserBaseVisitor<Node> {

    private Node parentNode;  // PostgreSQL의 CREATE_FUNCTION 노드
    private int baseLineNumber;  // $$ 시작 라인 번호
    private CommonTokenStream tokens;
    private Node currentBlockNode;
    private Map<String, CursorInfo> activeCursors = new HashMap<>();  // 커서 이름 -> 커서 정보 매핑
    
    /**
     * 커서 정보 저장 클래스
     */
    private static class CursorInfo {
        Node cursorNode;      // CURSOR 노드
        Node previousBlock;   // 이전 블록 노드
        
        CursorInfo(Node cursorNode, Node previousBlock) {
            this.cursorNode = cursorNode;
            this.previousBlock = previousBlock;
        }
    }

    public CustomPlpgsqlVisitor(Node parentNode, int baseLineNumber, CommonTokenStream tokens) {
        this.parentNode = parentNode;
        this.baseLineNumber = baseLineNumber;
        this.tokens = tokens;
    }

    /**
     * 실제 라인 번호 계산 (PL/pgSQL 내부 라인 + 베이스 라인)
     */
    private int getActualLineNumber(ParserRuleContext ctx) {
        return baseLineNumber + ctx.getStart().getLine();
    }
    
    /**
     * 실제 라인 번호 계산 (Token용)
     */
    private int getActualLineNumber(Token token) {
        return baseLineNumber + token.getLine();
    }
    
    /**
     * 실제 끝 라인 번호 계산 (PL/pgSQL 내부 라인 + 베이스 라인)
     */
    private int getActualEndLineNumber(ParserRuleContext ctx) {
        return baseLineNumber + ctx.getStop().getLine();
    }

    /**
     * 노드 생성 헬퍼
     */
    private Node createNode(String type, ParserRuleContext ctx, Node parent) {
        int startLine = getActualLineNumber(ctx);
        Node node = new Node(type, startLine, parent);
        node.endLine = getActualEndLineNumber(ctx);

        return node;
    }

    @Override
    public Node visitPlpgsqlBlock(PlpgsqlParser.PlpgsqlBlockContext ctx) {
        // 최초 호출 시 currentBlockNode가 null이면 parentNode로 초기화
        if (currentBlockNode == null) {
        currentBlockNode = parentNode;
        }

        Node previousBlock = currentBlockNode;
        
        // DECLARE 섹션은 항상 현재 블록의 자식으로 (BEGIN과 형제 관계)
        if (ctx.declareSection() != null) {
            visit(ctx.declareSection());
        }

        // BEGIN 블록 노드 생성
        Node beginNode = null;
        
        // BEGIN 키워드의 위치를 찾아서 노드 생성
        if (ctx.BEGIN() != null) {
            int beginStartLine = getActualLineNumber(ctx.BEGIN().getSymbol());
            int beginEndLine = getActualEndLineNumber(ctx);
            
            beginNode = new Node("BEGIN", beginStartLine, previousBlock);
            beginNode.endLine = beginEndLine;
            
            // BEGIN 블록 내부의 statements만 처리
            currentBlockNode = beginNode;
            
        if (ctx.statementList() != null) {
            visitStatementList(ctx.statementList());
        }

            // EXCEPTION 섹션도 BEGIN 내부
        if (ctx.exceptionSection() != null) {
            visit(ctx.exceptionSection());
        }

            currentBlockNode = previousBlock;
        } else {
            // BEGIN이 없는 경우 (최상위 블록) - statementList만 처리
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
        // DECLARE 키워드의 실제 줄 번호 사용
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
        // VARIABLE_DECLARATION과 INITIAL_VALUE 노드를 만들지 않음
        // 필요하면 변수명 정보만 저장하는 방식으로 변경 가능
    }

    @Override
    public Node visitStatementList(PlpgsqlParser.StatementListContext ctx) {
        if (ctx.statement() == null) return null;

        for (PlpgsqlParser.StatementContext stmtCtx : ctx.statement()) {
            visit(stmtCtx);
        }

        return null;
    }

    @Override
    public Node visitAssignmentStmt(PlpgsqlParser.AssignmentStmtContext ctx) {
        Node assignNode = createNode("ASSIGNMENT", ctx, currentBlockNode);
        return assignNode;
    }

    @Override
    public Node visitInsertStmt(PlpgsqlParser.InsertStmtContext ctx) {
        Node insertNode = createNode("INSERT", ctx, currentBlockNode);
        return insertNode;
    }

    @Override
    public Node visitUpdateStmt(PlpgsqlParser.UpdateStmtContext ctx) {
        Node updateNode = createNode("UPDATE", ctx, currentBlockNode);
        return updateNode;
    }

    @Override
    public Node visitDeleteStmt(PlpgsqlParser.DeleteStmtContext ctx) {
        Node deleteNode = createNode("DELETE", ctx, currentBlockNode);
        return deleteNode;
    }

    @Override
    public Node visitRaiseStmt(PlpgsqlParser.RaiseStmtContext ctx) {

        Node raiseNode = createNode("NOTICE", ctx, currentBlockNode);
        return raiseNode;
    }

    @Override
    public Node visitReturnStmt(PlpgsqlParser.ReturnStmtContext ctx) {
        String returnType = "RETURN";
        if (ctx.NEXT() != null) {
            returnType = "RETURN_NEXT";
        } else if (ctx.QUERY() != null) {
            returnType = "RETURN_QUERY";
        }
        Node returnNode = createNode(returnType, ctx, currentBlockNode);
        return returnNode;
    }

    // ========== 커서 관련 문법 ==========
    
    /**
     * 커서 이름 추출 헬퍼 메서드
     */
    private String extractCursorName(ParserRuleContext ctx) {
        if (ctx == null) return null;
        
        // OPEN, CLOSE, FETCH 문에서 커서 이름 찾기
        // 간단한 방법: 첫 번째 식별자가 커서 이름일 가능성이 높음
        // OPEN cursor_name FOR ... 또는 CLOSE cursor_name 형태
        if (ctx.getChildCount() >= 2) {
            String cursorName = ctx.getChild(1).getText();
            return cursorName;
        }
        
        return null;
    }

    @Override
    public Node visitOpenCursorStmt(PlpgsqlParser.OpenCursorStmtContext ctx) {
        String cursorName = extractCursorName(ctx);
        
        // CURSOR 노드 생성
        Node cursorNode = createNode("CURSOR", ctx, currentBlockNode);
        
        // 현재 블록을 저장하고 CURSOR 블록으로 전환
        Node previousBlock = currentBlockNode;
        
        if (cursorName != null) {
            activeCursors.put(cursorName, new CursorInfo(cursorNode, previousBlock));
        }
        
        // currentBlockNode를 CURSOR 노드로 변경하여 이후 statement들이 자식으로 추가되도록 함
        currentBlockNode = cursorNode;
        
        return cursorNode;
    }

    @Override
    public Node visitCloseCursorStmt(PlpgsqlParser.CloseCursorStmtContext ctx) {
        String cursorName = extractCursorName(ctx);
        
        // 활성 커서에서 제거하고 CURSOR 노드의 endLine 업데이트 및 블록 복원
        if (cursorName != null && activeCursors.containsKey(cursorName)) {
            CursorInfo cursorInfo = activeCursors.get(cursorName);
            cursorInfo.cursorNode.endLine = getActualEndLineNumber(ctx);
            
            // 이전 블록으로 복원
            currentBlockNode = cursorInfo.previousBlock;
            
            activeCursors.remove(cursorName);
        }
        
        // CLOSE는 별도 노드를 만들지 않고 CURSOR에 포함됨
        return null;
    }

    @Override
    public Node visitFetchStmt(PlpgsqlParser.FetchStmtContext ctx) {
        return createNode("FETCH", ctx, currentBlockNode);
    }

    @Override
    public Node visitSelectIntoStmt(PlpgsqlParser.SelectIntoStmtContext ctx) {
        Node selectNode = createNode("SELECT", ctx, currentBlockNode);
        return selectNode;
    }

    @Override
    public Node visitIfStmt(PlpgsqlParser.IfStmtContext ctx) {
        Node ifNode = createNode("IF", ctx, currentBlockNode);
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
                
                // ELSIF 키워드의 실제 라인 번호 사용
                int elsifStartLine = getActualLineNumber(ctx.ELSIF(i).getSymbol());
                int elsifEndLine = getActualEndLineNumber(elsifStmtList);

            Node elsifNode = new Node("ELSIF", elsifStartLine, ifNode);
            elsifNode.endLine = elsifEndLine;

            currentBlockNode = elsifNode;
            visitStatementList(elsifStmtList);
            }
        }

        // ELSE 절
        if (ctx.ELSE() != null) {
            PlpgsqlParser.StatementListContext elseStmtList = ctx.statementList(ctx.statementList().size() - 1);

            // ELSE 키워드의 라인 번호 사용
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
        Node exitNode = createNode("EXIT", ctx, currentBlockNode);
        return exitNode;
    }

    @Override
    public Node visitContinueStmt(PlpgsqlParser.ContinueStmtContext ctx) {
        Node continueNode = createNode("CONTINUE", ctx, currentBlockNode);
        return continueNode;
    }

    @Override
    public Node visitSetStmt(PlpgsqlParser.SetStmtContext ctx) {
        Node setNode = createNode("SET", ctx, currentBlockNode);
        return setNode;
    }

    @Override
    public Node visitExecuteStmt(PlpgsqlParser.ExecuteStmtContext ctx) {
        Node executeNode = createNode("EXECUTE", ctx, currentBlockNode);
        return executeNode;
    }

    @Override
    public Node visitPerformStmt(PlpgsqlParser.PerformStmtContext ctx) {
        Node performNode = createNode("PERFORM", ctx, currentBlockNode);
        return performNode;
    }
    
    @Override
    public Node visitCommitStmt(PlpgsqlParser.CommitStmtContext ctx) {
        Node commitNode = createNode("COMMIT", ctx, currentBlockNode);
        return commitNode;
    }
    
    @Override
    public Node visitRollbackStmt(PlpgsqlParser.RollbackStmtContext ctx) {
        Node rollbackNode = createNode("ROLLBACK", ctx, currentBlockNode);
        return rollbackNode;
    }

    @Override
    public Node visitNestedBlock(PlpgsqlParser.NestedBlockContext ctx) {
        Node previousBlock = currentBlockNode;
        
        // DECLARE 섹션 처리 (있는 경우)
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

        // BEGIN 블록 생성
        int beginStartLine = getActualLineNumber(ctx.BEGIN().getSymbol());
        int beginEndLine = getActualEndLineNumber(ctx);
        
        Node beginNode = new Node("BEGIN", beginStartLine, currentBlockNode);
        beginNode.endLine = beginEndLine;
        
        // BEGIN 블록 내부 처리
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
        Node exceptionNode = createNode("EXCEPTION", ctx, currentBlockNode);

        // for (PlpgsqlParser.ExceptionHandlerContext handlerCtx : ctx.exceptionHandlerList().exceptionHandler()) {
        //     Node handlerNode = createNode("EXCEPTION_HANDLER", handlerCtx, exceptionNode);
        //     Node previousBlock = currentBlockNode;
        //     currentBlockNode = handlerNode;

        //     visitStatementList(handlerCtx.statementList());

        //     currentBlockNode = previousBlock;
        // }

        return exceptionNode;
    }
    
    @Override
    public Node visitCaseStmt(PlpgsqlParser.CaseStmtContext ctx) {
        Node caseNode = createNode("CASE", ctx, currentBlockNode);
        Node previousBlock = currentBlockNode;
        currentBlockNode = caseNode;
        
        // WHEN 절들
        int whenCount = ctx.whenClauseList().whenClause().size();
        for (int i = 0; i < whenCount; i++) {
            PlpgsqlParser.StatementListContext whenStmtList = ctx.whenClauseList().whenClause(i).statementList();
            int whenStartLine = getActualLineNumber(ctx.whenClauseList().whenClause(i));
            int whenEndLine = getActualEndLineNumber(whenStmtList);
            
            Node whenNode = new Node("WHEN", whenStartLine, caseNode);
            whenNode.endLine = whenEndLine;
            
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
        Node assertNode = createNode("ASSERT", ctx, currentBlockNode);
        return assertNode;
    }
    
    @Override
    public Node visitNullStmt(PlpgsqlParser.NullStmtContext ctx) {
        Node nullNode = createNode("NULL", ctx, currentBlockNode);
        return nullNode;
    }
    
    @Override
    public Node visitGetDiagnosticsStmt(PlpgsqlParser.GetDiagnosticsStmtContext ctx) {
        Node diagNode = createNode("GET_DIAGNOSTICS", ctx, currentBlockNode);
        return diagNode;
    }
    
    @Override
    public Node visitCreateTempTableStmt(PlpgsqlParser.CreateTempTableStmtContext ctx) {
        Node createTableNode = createNode("CREATE_TEMP_TABLE", ctx, currentBlockNode);
        return createTableNode;
    }
    
    @Override
    public Node visitCteStmt(PlpgsqlParser.CteStmtContext ctx) {
        Node cteNode = createNode("CTE", ctx, currentBlockNode);
        return cteNode;
    }
    
    @Override
    public Node visitSqlGenericStmt(PlpgsqlParser.SqlGenericStmtContext ctx) {
        // CALL 문 처리
        if (ctx.CALL() != null) {
            Node callNode = createNode("CALL", ctx, currentBlockNode);
            return callNode;
        }
        
        // 구문 텍스트 확인
        String stmtText = ctx.getText().toUpperCase();
        
        // CREATE INDEX 문 처리
        if (stmtText.startsWith("CREATEINDEX") || stmtText.contains("CREATEINDEX")) {
            Node createIndexNode = createNode("CREATE_INDEX", ctx, currentBlockNode);
            return createIndexNode;
        }
        
        // DROP TABLE 문 처리
        if (stmtText.startsWith("DROPTABLE") || stmtText.contains("DROPTABLE")) {
            Node dropTableNode = createNode("DROP_TABLE", ctx, currentBlockNode);
            return dropTableNode;
        }
        
        Node sqlNode = createNode("SQL_GENERIC", ctx, currentBlockNode);
        return sqlNode;
    }
}

