package legacymodernizer.parser.antlr.plsql;

import java.util.Stack;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.TokenStream;

import legacymodernizer.parser.antlr.Node;
import legacymodernizer.parser.antlr.ParserUtils;
import legacymodernizer.parser.service.ParseProgressTracker;

/**
 * PL/SQL 파일 분석을 위한 커스텀 리스너
 * - 프로시저/함수/트리거/패키지 구조 추출
 * - 통일된 속성명 사용 (Node 클래스 참조)
 */
public class CustomPlSqlListener extends PlSqlParserBaseListener {
    private TokenStream tokens;
    private Stack<Node> nodeStack = new Stack<>();
    private Node root = new Node("FILE", 0, null);
    private ParseProgressTracker progressTracker;

    public Node getRoot() {
        return root;
    }

    public CustomPlSqlListener(TokenStream tokens) {
        this.tokens = tokens;
        nodeStack.push(root);
    }
    
    public CustomPlSqlListener(TokenStream tokens, ParseProgressTracker tracker) {
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
    // 유틸리티 메서드
    // ========================================
    
    /**
     * 스키마.이름 형태에서 분리
     */
    private String[] extractSchemaAndName(String fullName) {
        if (fullName == null) return new String[]{null, null};
        if (fullName.contains(".")) {
            String[] parts = fullName.split("\\.", 2);
            return new String[]{parts[0], parts[1]};
        }
        return new String[]{null, fullName};
    }
    
    /**
     * 파라미터 목록을 문자열로 추출
     */
    private String extractParameters(java.util.List<PlSqlParser.ParameterContext> params) {
        if (params == null || params.isEmpty()) return null;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < params.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(params.get(i).getText());
        }
        return sb.toString();
    }

    private Node enterStatement(String type, int line) {
        return enterStatement(type, null, line);
    }
    
    private Node enterStatement(String type, String name, int line) {
        Node node = new Node(type, name, line, nodeStack.peek());
        nodeStack.push(node);
        return node;
    }

    private void exitStatement(String type, int line) {
        exitStatement(type, line, null);
    }
    
    private void exitStatement(String type, int line, ParserRuleContext ctx) {
        if (nodeStack.isEmpty()) return;
        Node node = nodeStack.pop();
        node.endLine = line;
        // 동일 범위 중복 자식 제거
        if (node.children != null && !node.children.isEmpty()) {
            node.children.removeIf(child -> child.startLine == node.startLine && child.endLine == node.endLine);
        }
        if (ctx != null && tokens instanceof CommonTokenStream) {
            node.comment = ParserUtils.getLeadingComment(ctx, (CommonTokenStream) tokens);
        }
    }

    // ========================================
    // Procedure/Function/Trigger/Package
    // ========================================
    
    @Override
    public void enterCreate_procedure_body(PlSqlParser.Create_procedure_bodyContext ctx) {
        String fullName = ctx.procedure_name() != null ? ctx.procedure_name().getText() : null;
        String[] parts = extractSchemaAndName(fullName);
        
        Node node = enterStatement("PROCEDURE", parts[1], ctx.getStart().getLine());
        node.schema = parts[0];
        node.parameters = extractParameters(ctx.parameter());
        node.signature = ParserUtils.extractSignature(ctx, tokens, "IS", "AS");
    }

    @Override
    public void exitCreate_procedure_body(PlSqlParser.Create_procedure_bodyContext ctx) {
        exitStatement("PROCEDURE", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterCreate_function_body(PlSqlParser.Create_function_bodyContext ctx) {
        String fullName = ctx.function_name() != null ? ctx.function_name().getText() : null;
        String[] parts = extractSchemaAndName(fullName);
        
        Node node = enterStatement("FUNCTION", parts[1], ctx.getStart().getLine());
        node.schema = parts[0];
        node.parameters = extractParameters(ctx.parameter());
        
        if (ctx.type_spec() != null) {
            node.returnType = ctx.type_spec().getText();
        }
        
        node.signature = ParserUtils.extractSignature(ctx, tokens, "IS", "AS");
    }

    @Override
    public void exitCreate_function_body(PlSqlParser.Create_function_bodyContext ctx) {
        exitStatement("FUNCTION", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterPackage_obj_spec(PlSqlParser.Package_obj_specContext ctx) {
        String text = ctx.getText().toUpperCase().trim();
        if (text.startsWith("FUNCTION") || text.startsWith("PROCEDURE")) {
            return;
        }
        enterStatement("PACKAGE_VARIABLE", ctx.getStart().getLine());
    }
    
    @Override
    public void exitPackage_obj_spec(PlSqlParser.Package_obj_specContext ctx) {
        String text = ctx.getText().toUpperCase().trim();
        if (text.startsWith("FUNCTION") || text.startsWith("PROCEDURE")) {
            return;
        }
        exitStatement("PACKAGE_VARIABLE", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterPackage_obj_body(PlSqlParser.Package_obj_bodyContext ctx) {
        String memberType = ctx.function_body() != null ? "FUNCTION" : "PROCEDURE";
        
        String name = null;
        String returnType = null;
        String parameters = null;
        
        if (ctx.function_body() != null) {
            PlSqlParser.Function_bodyContext funcCtx = ctx.function_body();
            if (funcCtx.identifier() != null) {
                name = funcCtx.identifier().getText();
            }
            if (funcCtx.type_spec() != null) {
                returnType = funcCtx.type_spec().getText();
            }
            parameters = extractParameters(funcCtx.parameter());
        } else if (ctx.procedure_body() != null) {
            PlSqlParser.Procedure_bodyContext procCtx = ctx.procedure_body();
            if (procCtx.identifier() != null) {
                name = procCtx.identifier().getText();
            }
            parameters = extractParameters(procCtx.parameter());
        }
        
        Node node = enterStatement(memberType, name, ctx.getStart().getLine());
        node.returnType = returnType;
        node.parameters = parameters;
        node.signature = ParserUtils.extractSignature(ctx, tokens, "IS", "AS");
    }

    @Override
    public void exitPackage_obj_body(PlSqlParser.Package_obj_bodyContext ctx) {
        String memberType = ctx.function_body() != null ? "FUNCTION" : "PROCEDURE";
        exitStatement(memberType, ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterCreate_trigger(PlSqlParser.Create_triggerContext ctx) {
        String fullName = ctx.trigger_name() != null ? ctx.trigger_name().getText() : null;
        String[] parts = extractSchemaAndName(fullName);
        
        Node node = enterStatement("TRIGGER", parts[1], ctx.getStart().getLine());
        node.schema = parts[0];
    }
    
    @Override
    public void exitCreate_trigger(PlSqlParser.Create_triggerContext ctx) {
        exitStatement("TRIGGER", ctx.getStop().getLine(), ctx);
    }
    
    @Override
    public void enterTrigger_block(PlSqlParser.Trigger_blockContext ctx) {
        enterStatement("TRIGGER_BLOCK", ctx.getStart().getLine());
    }
    
    @Override
    public void exitTrigger_block(PlSqlParser.Trigger_blockContext ctx) {
        exitStatement("TRIGGER_BLOCK", ctx.getStop().getLine(), ctx);
    }

    // ========================================
    // DECLARE/ASSIGNMENT/RETURN
    // ========================================
    
    @Override
    public void enterSeq_of_declare_specs(PlSqlParser.Seq_of_declare_specsContext ctx) {
        enterStatement("DECLARE", ctx.getStart().getLine());
    }

    @Override
    public void exitSeq_of_declare_specs(PlSqlParser.Seq_of_declare_specsContext ctx) {
        exitStatement("DECLARE", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterAssignment_statement(PlSqlParser.Assignment_statementContext ctx) {
        enterStatement("ASSIGNMENT", ctx.getStart().getLine());
    }

    @Override
    public void exitAssignment_statement(PlSqlParser.Assignment_statementContext ctx) {
        exitStatement("ASSIGNMENT", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterReturn_statement(PlSqlParser.Return_statementContext ctx) {
        enterStatement("RETURN", ctx.getStart().getLine());
    }

    @Override
    public void exitReturn_statement(PlSqlParser.Return_statementContext ctx) {
        exitStatement("RETURN", ctx.getStop().getLine(), ctx);
    }

    // ========================================
    // DML: INSERT/UPDATE/DELETE/MERGE/SELECT
    // ========================================

    @Override
    public void enterQuery_block(PlSqlParser.Query_blockContext ctx) {
        enterStatement("SELECT", ctx.getStart().getLine());
    }
    
    @Override
    public void exitQuery_block(PlSqlParser.Query_blockContext ctx) {
        exitStatement("SELECT", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterInsert_statement(PlSqlParser.Insert_statementContext ctx) {
        enterStatement("INSERT", ctx.getStart().getLine());
    }

    @Override
    public void exitInsert_statement(PlSqlParser.Insert_statementContext ctx) {
        exitStatement("INSERT", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterUpdate_statement(PlSqlParser.Update_statementContext ctx) {
        enterStatement("UPDATE", ctx.getStart().getLine());
    }

    @Override
    public void exitUpdate_statement(PlSqlParser.Update_statementContext ctx) {
        exitStatement("UPDATE", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterDelete_statement(PlSqlParser.Delete_statementContext ctx) {
        enterStatement("DELETE", ctx.getStart().getLine());
    }

    @Override
    public void exitDelete_statement(PlSqlParser.Delete_statementContext ctx) {
        exitStatement("DELETE", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterMerge_statement(PlSqlParser.Merge_statementContext ctx) {
        enterStatement("MERGE", ctx.getStart().getLine());
    }

    @Override
    public void exitMerge_statement(PlSqlParser.Merge_statementContext ctx) {
        exitStatement("MERGE", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterMerge_update_clause(PlSqlParser.Merge_update_clauseContext ctx) {
        enterStatement("UPDATE", ctx.getStart().getLine());
    }

    @Override
    public void exitMerge_update_clause(PlSqlParser.Merge_update_clauseContext ctx) {
        exitStatement("UPDATE", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterMerge_insert_clause(PlSqlParser.Merge_insert_clauseContext ctx) {
        enterStatement("INSERT", ctx.getStart().getLine());
    }

    @Override
    public void exitMerge_insert_clause(PlSqlParser.Merge_insert_clauseContext ctx) {
        exitStatement("INSERT", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterMerge_update_delete_part(PlSqlParser.Merge_update_delete_partContext ctx) {
        enterStatement("DELETE", ctx.getStart().getLine());
    }

    @Override
    public void exitMerge_update_delete_part(PlSqlParser.Merge_update_delete_partContext ctx) {
        exitStatement("DELETE", ctx.getStop().getLine(), ctx);
    }

    // ========================================
    // 제어 흐름: IF/ELSIF/ELSE/LOOP
    // ========================================
    
    @Override
    public void enterIf_statement(PlSqlParser.If_statementContext ctx) {
        enterStatement("IF", ctx.getStart().getLine());
    }

    @Override
    public void exitIf_statement(PlSqlParser.If_statementContext ctx) {
        exitStatement("IF", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterElsif_part(PlSqlParser.Elsif_partContext ctx) {
        enterStatement("ELSIF", ctx.getStart().getLine());
    }

    @Override
    public void exitElsif_part(PlSqlParser.Elsif_partContext ctx) {
        exitStatement("ELSIF", ctx.getStop().getLine(), ctx);
    }
    
    @Override
    public void enterElse_part(PlSqlParser.Else_partContext ctx) {
        enterStatement("ELSE", ctx.getStart().getLine());
    }

    @Override
    public void exitElse_part(PlSqlParser.Else_partContext ctx) {
        exitStatement("ELSE", ctx.getStop().getLine(), ctx);
    }
    
    @Override
    public void enterLoop_statement(PlSqlParser.Loop_statementContext ctx) {
        enterStatement("LOOP", ctx.getStart().getLine());
    }

    @Override
    public void exitLoop_statement(PlSqlParser.Loop_statementContext ctx) {
        exitStatement("LOOP", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterSingle_column_for_loop(PlSqlParser.Single_column_for_loopContext ctx) {
        enterStatement("LOOP", ctx.getStart().getLine());
    }
    
    @Override
    public void exitSingle_column_for_loop(PlSqlParser.Single_column_for_loopContext ctx) {
        exitStatement("LOOP", ctx.getStop().getLine(), ctx);
    }
    
    @Override
    public void enterMulti_column_for_loop(PlSqlParser.Multi_column_for_loopContext ctx) {
        enterStatement("LOOP", ctx.getStart().getLine());
    }
    
    @Override
    public void exitMulti_column_for_loop(PlSqlParser.Multi_column_for_loopContext ctx) {
        exitStatement("LOOP", ctx.getStop().getLine(), ctx);
    }

    // ========================================
    // EXCEPTION
    // ========================================
    
    @Override
    public void enterException_handler(PlSqlParser.Exception_handlerContext ctx) {
        if (!"EXCEPTION".equals(nodeStack.peek().type)) {
            enterStatement("EXCEPTION", ctx.getStart().getLine() - 1);
        }
    }
    
    @Override
    public void exitException_handler(PlSqlParser.Exception_handlerContext ctx) {
        // no-op
    }
    
    @Override
    public void exitBody(PlSqlParser.BodyContext ctx) {
        if (!nodeStack.isEmpty() && "EXCEPTION".equals(nodeStack.peek().type)) {
            Node node = nodeStack.pop();
            node.endLine = ctx.getStop().getLine();
            // 동일 범위 중복 자식 제거
            if (node.children != null && !node.children.isEmpty()) {
                node.children.removeIf(child -> child.startLine == node.startLine && child.endLine == node.endLine);
            }
        }
    }

    @Override
    public void enterSeq_of_statements(PlSqlParser.Seq_of_statementsContext ctx) {
        String text = ctx.getText();
        if (!text.contains("BEGIN") && 
            ctx.getParent() instanceof PlSqlParser.BodyContext && 
            ctx.getParent().getParent() instanceof PlSqlParser.StatementContext) {
            PlSqlParser.BodyContext bodyCtx = (PlSqlParser.BodyContext) ctx.getParent();
            int beginLine = bodyCtx.BEGIN().getSymbol().getLine();
            enterStatement("TRY", beginLine);
        }
    }

    @Override
    public void exitSeq_of_statements(PlSqlParser.Seq_of_statementsContext ctx) {
        String text = ctx.getText();
        if (!text.contains("BEGIN") && 
            ctx.getParent() instanceof PlSqlParser.BodyContext && 
            ctx.getParent().getParent() instanceof PlSqlParser.StatementContext) {
            exitStatement("TRY", ctx.getStop().getLine(), ctx);
        }
    }
    
    // ========================================
    // CALL
    // ========================================
    
    @Override
    public void enterCall_statement(PlSqlParser.Call_statementContext ctx) {
        String name = null;
        if (!ctx.routine_name().isEmpty()) {
            PlSqlParser.Routine_nameContext routineName = ctx.routine_name(0);
            if (routineName.getText().toUpperCase().contains("RAISE")) {
                return;
            }
            name = routineName.getText();
        }
        enterStatement("CALL", name, ctx.getStart().getLine());
    }
    
    @Override
    public void exitCall_statement(PlSqlParser.Call_statementContext ctx) {
        if (!ctx.routine_name().isEmpty()) {
            PlSqlParser.Routine_nameContext routineName = ctx.routine_name(0);
            if (routineName.getText().toUpperCase().contains("RAISE")) {
                return;
            }
        }
        exitStatement("CALL", ctx.getStop().getLine(), ctx);
    }

    // ========================================
    // CURSOR
    // ========================================
    
    @Override
    public void enterCursor_declaration(PlSqlParser.Cursor_declarationContext ctx) {
        String name = ctx.identifier() != null ? ctx.identifier().getText() : null;
        enterStatement("CURSOR_VARIABLE", name, ctx.getStart().getLine());
    }
    
    @Override
    public void exitCursor_declaration(PlSqlParser.Cursor_declarationContext ctx) {
        exitStatement("CURSOR_VARIABLE", ctx.getStop().getLine(), ctx);
    }
    
    @Override
    public void enterOpen_statement(PlSqlParser.Open_statementContext ctx) {
        enterStatement("OPEN_CURSOR", ctx.getStart().getLine());
    }
    
    @Override
    public void exitOpen_statement(PlSqlParser.Open_statementContext ctx) {
        exitStatement("OPEN_CURSOR", ctx.getStop().getLine(), ctx);
    }
    
    @Override
    public void enterFetch_statement(PlSqlParser.Fetch_statementContext ctx) {
        enterStatement("FETCH", ctx.getStart().getLine());
    }
    
    @Override
    public void exitFetch_statement(PlSqlParser.Fetch_statementContext ctx) {
        exitStatement("FETCH", ctx.getStop().getLine(), ctx);
    }
    
    @Override
    public void enterClose_statement(PlSqlParser.Close_statementContext ctx) {
        enterStatement("CLOSE_CURSOR", ctx.getStart().getLine());
    }
    
    @Override
    public void exitClose_statement(PlSqlParser.Close_statementContext ctx) {
        exitStatement("CLOSE_CURSOR", ctx.getStop().getLine(), ctx);
    }
    
    @Override
    public void enterOpen_for_statement(PlSqlParser.Open_for_statementContext ctx) {
        enterStatement("OPEN_CURSOR", ctx.getStart().getLine());
    }
    
    @Override
    public void exitOpen_for_statement(PlSqlParser.Open_for_statementContext ctx) {
        exitStatement("OPEN_CURSOR", ctx.getStop().getLine(), ctx);
    }
    
    @Override
    public void enterExit_statement(PlSqlParser.Exit_statementContext ctx) {
        enterStatement("EXIT", ctx.getStart().getLine());
    }
    
    @Override
    public void exitExit_statement(PlSqlParser.Exit_statementContext ctx) {
        exitStatement("EXIT", ctx.getStop().getLine(), ctx);
    }

    // ========================================
    // CTE/JOIN/EXECUTE/COMMIT
    // ========================================

    @Override
    public void enterSubquery_factoring_clause(PlSqlParser.Subquery_factoring_clauseContext ctx) {
        enterStatement("CTE", ctx.getStart().getLine());
    }
    
    @Override
    public void exitSubquery_factoring_clause(PlSqlParser.Subquery_factoring_clauseContext ctx) {
        exitStatement("CTE", ctx.getStop().getLine(), ctx);
    }
    
    @Override
    public void enterJoin_clause(PlSqlParser.Join_clauseContext ctx) {
        enterStatement("JOIN", ctx.getStart().getLine());
    }
    
    @Override
    public void exitJoin_clause(PlSqlParser.Join_clauseContext ctx) {
        exitStatement("JOIN", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterSubquery_operation_part(PlSqlParser.Subquery_operation_partContext ctx) {
        String t = ctx.getText().toUpperCase();
        String type = t.contains("UNIONALL") ? "UNION_ALL"
                : t.contains("UNION") ? "UNION"
                : t.contains("INTERSECT") ? "INTERSECT"
                : t.contains("MINUS") ? "MINUS"
                : "SET_OPERATION";
        enterStatement(type, ctx.getStart().getLine());
    }

    @Override
    public void exitSubquery_operation_part(PlSqlParser.Subquery_operation_partContext ctx) {
        String t = ctx.getText().toUpperCase();
        String type = t.contains("UNION ALL") ? "UNION_ALL"
                : t.contains("UNION") ? "UNION"
                : t.contains("INTERSECT") ? "INTERSECT"
                : t.contains("MINUS") ? "MINUS"
                : "SET_OPERATION";
        exitStatement(type, ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterExecute_immediate(PlSqlParser.Execute_immediateContext ctx) {
        enterStatement("EXECUTE_IMMEDIATE", ctx.getStart().getLine());
    }
    
    @Override
    public void exitExecute_immediate(PlSqlParser.Execute_immediateContext ctx) {
        exitStatement("EXECUTE_IMMEDIATE", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterCommit_statement(PlSqlParser.Commit_statementContext ctx) {
        enterStatement("COMMIT", ctx.getStart().getLine());
    }
    
    @Override
    public void exitCommit_statement(PlSqlParser.Commit_statementContext ctx) {
        exitStatement("COMMIT", ctx.getStop().getLine(), ctx);
    }

    // // ========================================
    // // DDL: CREATE/ALTER/COMMENT
    // // ========================================
    
    // @Override
    // public void enterCreate_table(PlSqlParser.Create_tableContext ctx) {
    //     String name = null;
    //     if (ctx.table_name() != null) {
    //         name = ctx.table_name().getText();
    //     }
    //     enterStatement("CREATE_TABLE", name, ctx.getStart().getLine());
    // }
    
    // @Override
    // public void exitCreate_table(PlSqlParser.Create_tableContext ctx) {
    //     exitStatement("CREATE_TABLE", ctx.getStop().getLine(), ctx);
    // }
    
    // @Override
    // public void enterAlter_table(PlSqlParser.Alter_tableContext ctx) {
    //     String name = null;
    //     if (ctx.tableview_name() != null && ctx.tableview_name().getText() != null) {
    //         name = ctx.tableview_name().getText();
    //     }
    //     enterStatement("ALTER_TABLE", name, ctx.getStart().getLine());
    // }
    
    // @Override
    // public void exitAlter_table(PlSqlParser.Alter_tableContext ctx) {
    //     exitStatement("ALTER_TABLE", ctx.getStop().getLine(), ctx);
    // }
    
    // @Override
    // public void enterComment_on_column(PlSqlParser.Comment_on_columnContext ctx) {
    //     String name = null;
    //     if (ctx.column_name() != null) {
    //         name = ctx.column_name().getText();
    //     }
    //     enterStatement("COMMENT_ON_COLUMN", name, ctx.getStart().getLine());
    // }
    
    // @Override
    // public void exitComment_on_column(PlSqlParser.Comment_on_columnContext ctx) {
    //     exitStatement("COMMENT_ON_COLUMN", ctx.getStop().getLine(), ctx);
    // }
    
    // @Override
    // public void enterComment_on_table(PlSqlParser.Comment_on_tableContext ctx) {
    //     String name = null;
    //     if (ctx.tableview_name() != null && ctx.tableview_name().getText() != null) {
    //         name = ctx.tableview_name().getText();
    //     }
    //     enterStatement("COMMENT_ON_TABLE", name, ctx.getStart().getLine());
    // }
    
    // @Override
    // public void exitComment_on_table(PlSqlParser.Comment_on_tableContext ctx) {
    //     exitStatement("COMMENT_ON_TABLE", ctx.getStop().getLine(), ctx);
    // }
    
    // @Override
    // public void enterCreate_index(PlSqlParser.Create_indexContext ctx) {
    //     String name = null;
    //     if (ctx.index_name() != null) {
    //         name = ctx.index_name().getText();
    //     }
    //     enterStatement("CREATE_INDEX", name, ctx.getStart().getLine());
    // }
    
    // @Override
    // public void exitCreate_index(PlSqlParser.Create_indexContext ctx) {
    //     exitStatement("CREATE_INDEX", ctx.getStop().getLine(), ctx);
    // }

    // ========================================
    // 디버깅용
    // ========================================
    
    public void printTree(Node node, String indent) {
        StringBuilder info = new StringBuilder();
        info.append(indent).append(node.type);
        if (node.name != null) info.append(" [").append(node.name).append("]");
        if (node.schema != null) info.append(" schema:").append(node.schema);
        if (node.returnType != null) info.append(" returns:").append(node.returnType);
        if (node.parameters != null) info.append(" params:").append(node.parameters);
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
