package legacymodernizer.parser.antlr.plsql;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;

import legacymodernizer.parser.parsing.AntlrParseHarness;
import legacymodernizer.parser.model.Node;
import legacymodernizer.parser.antlr.ListenerHelper;
import legacymodernizer.parser.antlr.ParserUtils;
import legacymodernizer.parser.service.ParseProgressTracker;

/**
 * PL/SQL 파일 분석을 위한 커스텀 리스너
 * - 프로시저/함수/트리거/패키지 구조 추출
 * - 통일된 속성명 사용 (Node 클래스 참조)
 */
public class PlSqlAstListener extends PlSqlParserBaseListener
        implements AntlrParseHarness.AstListener {
    private final ListenerHelper h;

    public Node getRoot() {
        return h.getRoot();
    }

    public PlSqlAstListener(CommonTokenStream tokens, ParseProgressTracker tracker) {
        this.h = new ListenerHelper(tokens, tracker);
    }

    public void setFileInfo(String fileName, String filePath) {
        h.setFileInfo(fileName, filePath);
    }

    @Override
    public void enterEveryRule(ParserRuleContext ctx) {
        h.checkProgress(ctx);
    }

    /**
     * 파라미터 목록을 원본 그대로 추출 (공백·콤마·줄바꿈·주석 보존).
     */
    private String extractParameters(java.util.List<PlSqlParser.ParameterContext> params) {
        if (params == null || params.isEmpty()) return null;
        return ParserUtils.getOriginalText(params.get(0), params.get(params.size() - 1), h.getTokens());
    }

    /**
     * PROCEDURE/FUNCTION/TRIGGER/PACKAGE_BODY 의 파라미터 목록을 자식 PARAMETER 노드로 emit.
     *
     * Analyzer 측 책임 분리: ANTLR 가 grammar 차원에서 이름·타입·mode·default 를 정확히 추출.
     * 자식 노드 추가만으로 PARENT_OF / REFERENCES / INIT_BY 매칭이 자동 작동.
     */
    private void emitParameters(java.util.List<PlSqlParser.ParameterContext> params, Node parent) {
        if (params == null || params.isEmpty() || parent == null) return;
        for (PlSqlParser.ParameterContext p : params) {
            if (p.parameter_name() == null) continue;
            String pname = p.parameter_name().getText();
            int sLine = p.getStart().getLine();
            int eLine = p.getStop().getLine();
            Node paramNode = new Node("PARAMETER", pname, sLine, parent);
            paramNode.endLine = eLine;
            if (p.type_spec() != null) {
                paramNode.variableType = p.type_spec().getText();
            }
            // PL/SQL mode: IN / OUT / IN OUT / INOUT — modifiers 필드 재사용.
            // PL/SQL 기본값은 IN — 키워드 명시 없으면 IN 으로 채움 (정보 손실 방지).
            StringBuilder mode = new StringBuilder();
            if (p.IN() != null && !p.IN().isEmpty()) mode.append("IN");
            if (p.OUT() != null && !p.OUT().isEmpty()) {
                if (mode.length() > 0) mode.append(" ");
                mode.append("OUT");
            }
            if (p.INOUT() != null && !p.INOUT().isEmpty()) {
                if (mode.length() > 0) mode.append(" ");
                mode.append("INOUT");
            }
            paramNode.modifiers = mode.length() > 0 ? mode.toString() : "IN";
            // default value (`name TYPE := default`)
            if (p.default_value_part() != null) {
                paramNode.initValue = ParserUtils.getOriginalText(
                    p.default_value_part(), p.default_value_part(), h.getTokens()
                );
            }
        }
    }

    // ========================================
    // Procedure/Function/Trigger/Package
    // ========================================

    @Override
    public void enterCreate_procedure_body(PlSqlParser.Create_procedure_bodyContext ctx) {
        String fullName = ctx.procedure_name() != null ? ctx.procedure_name().getText() : null;
        String[] parts = ParserUtils.extractSchemaAndName(fullName);

        Node node = h.enterStatement("PROCEDURE", parts[1], ctx.getStart().getLine());
        node.schema = parts[0];
        node.parameters = extractParameters(ctx.parameter());
        node.signature = ParserUtils.extractSignature(ctx, h.getTokens(), "IS", "AS");
        emitParameters(ctx.parameter(), node);
    }

    @Override
    public void exitCreate_procedure_body(PlSqlParser.Create_procedure_bodyContext ctx) {
        Node node = h.exitStatementWithChildDedupe("PROCEDURE", ctx.getStop().getLine(), ctx);
        h.attachHeaderComment(node, ctx, "BEGIN", "AS", "IS");
    }

    @Override
    public void enterCreate_function_body(PlSqlParser.Create_function_bodyContext ctx) {
        String fullName = ctx.function_name() != null ? ctx.function_name().getText() : null;
        String[] parts = ParserUtils.extractSchemaAndName(fullName);

        Node node = h.enterStatement("FUNCTION", parts[1], ctx.getStart().getLine());
        node.schema = parts[0];
        node.parameters = extractParameters(ctx.parameter());

        if (ctx.type_spec() != null) {
            node.returnType = ctx.type_spec().getText();
        }

        node.signature = ParserUtils.extractSignature(ctx, h.getTokens(), "IS", "AS");
        emitParameters(ctx.parameter(), node);
    }

    @Override
    public void exitCreate_function_body(PlSqlParser.Create_function_bodyContext ctx) {
        Node node = h.exitStatementWithChildDedupe("FUNCTION", ctx.getStop().getLine(), ctx);
        h.attachHeaderComment(node, ctx, "BEGIN", "AS", "IS");
    }

    @Override
    public void enterPackage_obj_spec(PlSqlParser.Package_obj_specContext ctx) {
        String nodeType = packageObjSpecNodeType(ctx);
        if (nodeType == null) return;
        h.enterStatement(nodeType, ctx.getStart().getLine());
    }

    @Override
    public void exitPackage_obj_spec(PlSqlParser.Package_obj_specContext ctx) {
        String nodeType = packageObjSpecNodeType(ctx);
        if (nodeType == null) return;
        h.exitStatementWithChildDedupe(nodeType, ctx.getStop().getLine(), ctx);
    }

    /** enter/exit 쌍이 같은 판정을 재계산해야 하므로 한 곳에 둔다 — null 은 FUNCTION/PROCEDURE 스킵. */
    private static String packageObjSpecNodeType(PlSqlParser.Package_obj_specContext ctx) {
        String text = ctx.getText().toUpperCase().trim();
        if (text.startsWith("FUNCTION") || text.startsWith("PROCEDURE")) return null;
        return text.contains("CONSTANT") ? "CONSTANT_FIELD" : "PACKAGE_VARIABLE";
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

        Node node = h.enterStatement(memberType, name, ctx.getStart().getLine());
        node.returnType = returnType;
        node.parameters = parameters;
        node.signature = ParserUtils.extractSignature(ctx, h.getTokens(), "IS", "AS");
        // package body 의 function_body / procedure_body 자식의 parameter 자식 노드 추가
        if (ctx.function_body() != null) {
            emitParameters(ctx.function_body().parameter(), node);
        } else if (ctx.procedure_body() != null) {
            emitParameters(ctx.procedure_body().parameter(), node);
        }
    }

    @Override
    public void exitPackage_obj_body(PlSqlParser.Package_obj_bodyContext ctx) {
        String memberType = ctx.function_body() != null ? "FUNCTION" : "PROCEDURE";
        Node node = h.exitStatementWithChildDedupe(memberType, ctx.getStop().getLine(), ctx);
        h.attachHeaderComment(node, ctx, "BEGIN", "AS", "IS");
    }

    @Override
    public void enterCreate_trigger(PlSqlParser.Create_triggerContext ctx) {
        String fullName = ctx.trigger_name() != null ? ctx.trigger_name().getText() : null;
        String[] parts = ParserUtils.extractSchemaAndName(fullName);

        Node node = h.enterStatement("TRIGGER", parts[1], ctx.getStart().getLine());
        node.schema = parts[0];
    }

    @Override
    public void exitCreate_trigger(PlSqlParser.Create_triggerContext ctx) {
        Node node = h.exitStatementWithChildDedupe("TRIGGER", ctx.getStop().getLine(), ctx);
        h.attachHeaderComment(node, ctx, "BEGIN", "AS", "IS");
    }

    @Override
    public void enterTrigger_block(PlSqlParser.Trigger_blockContext ctx) {
        h.enterStatement("TRIGGER_BLOCK", ctx.getStart().getLine());
    }

    @Override
    public void exitTrigger_block(PlSqlParser.Trigger_blockContext ctx) {
        h.exitStatementWithChildDedupe("TRIGGER_BLOCK", ctx.getStop().getLine(), ctx);
    }

    // ========================================
    // DECLARE/ASSIGNMENT/RETURN
    // ========================================

    @Override
    public void enterSeq_of_declare_specs(PlSqlParser.Seq_of_declare_specsContext ctx) {
        h.enterStatement("DECLARE", ctx.getStart().getLine());
    }

    @Override
    public void exitSeq_of_declare_specs(PlSqlParser.Seq_of_declare_specsContext ctx) {
        h.exitStatementWithChildDedupe("DECLARE", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterVariable_declaration(PlSqlParser.Variable_declarationContext ctx) {
        // DECLARE 블록 안 변수만 VARIABLE 노드로 생성 (package_obj_spec 은 기존 PACKAGE_VARIABLE 처리 유지)
        if (!(ctx.getParent() instanceof PlSqlParser.Declare_specContext)) return;

        String varName = ctx.identifier() != null ? ctx.identifier().getText() : null;
        Node v = h.addLeafStatement("VARIABLE", varName, ctx.getStart().getLine(), ctx.getStop().getLine());
        if (ctx.type_spec() != null) {
            v.variableType = ctx.type_spec().getText();
        }
        // 초기화 표현식 (`:= 우변`) 텍스트만 보존 — 식별자 추출/플래그는 Analyzer 책임
        if (ctx.default_value_part() != null && ctx.default_value_part().expression() != null) {
            v.initValue = ctx.default_value_part().expression().getText();
        }
    }

    @Override
    public void enterAssignment_statement(PlSqlParser.Assignment_statementContext ctx) {
        h.enterStatement("ASSIGNMENT", ctx.getStart().getLine());
        // 좌·우변 텍스트는 노드 원문으로 보존 — 식별자 추출/플래그는 Analyzer 책임
    }

    @Override
    public void exitAssignment_statement(PlSqlParser.Assignment_statementContext ctx) {
        h.exitStatementWithChildDedupe("ASSIGNMENT", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterReturn_statement(PlSqlParser.Return_statementContext ctx) {
        h.enterStatement("RETURN", ctx.getStart().getLine());
        // 반환식은 노드 텍스트로 보존 — Analyzer 가 식별자 분석
    }

    @Override
    public void exitReturn_statement(PlSqlParser.Return_statementContext ctx) {
        h.exitStatementWithChildDedupe("RETURN", ctx.getStop().getLine(), ctx);
    }

    // ========================================
    // DML: INSERT/UPDATE/DELETE/MERGE/SELECT
    // ========================================

    @Override
    public void enterQuery_block(PlSqlParser.Query_blockContext ctx) {
        h.enterStatement("SELECT", ctx.getStart().getLine());
    }

    @Override
    public void exitQuery_block(PlSqlParser.Query_blockContext ctx) {
        h.exitStatementWithChildDedupe("SELECT", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterInsert_statement(PlSqlParser.Insert_statementContext ctx) {
        h.enterStatement("INSERT", ctx.getStart().getLine());
    }

    @Override
    public void exitInsert_statement(PlSqlParser.Insert_statementContext ctx) {
        h.exitStatementWithChildDedupe("INSERT", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterUpdate_statement(PlSqlParser.Update_statementContext ctx) {
        h.enterStatement("UPDATE", ctx.getStart().getLine());
    }

    @Override
    public void exitUpdate_statement(PlSqlParser.Update_statementContext ctx) {
        h.exitStatementWithChildDedupe("UPDATE", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterDelete_statement(PlSqlParser.Delete_statementContext ctx) {
        h.enterStatement("DELETE", ctx.getStart().getLine());
    }

    @Override
    public void exitDelete_statement(PlSqlParser.Delete_statementContext ctx) {
        h.exitStatementWithChildDedupe("DELETE", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterMerge_statement(PlSqlParser.Merge_statementContext ctx) {
        h.enterStatement("MERGE", ctx.getStart().getLine());
    }

    @Override
    public void exitMerge_statement(PlSqlParser.Merge_statementContext ctx) {
        h.exitStatementWithChildDedupe("MERGE", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterMerge_update_clause(PlSqlParser.Merge_update_clauseContext ctx) {
        h.enterStatement("UPDATE", ctx.getStart().getLine());
    }

    @Override
    public void exitMerge_update_clause(PlSqlParser.Merge_update_clauseContext ctx) {
        h.exitStatementWithChildDedupe("UPDATE", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterMerge_insert_clause(PlSqlParser.Merge_insert_clauseContext ctx) {
        h.enterStatement("INSERT", ctx.getStart().getLine());
    }

    @Override
    public void exitMerge_insert_clause(PlSqlParser.Merge_insert_clauseContext ctx) {
        h.exitStatementWithChildDedupe("INSERT", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterMerge_update_delete_part(PlSqlParser.Merge_update_delete_partContext ctx) {
        h.enterStatement("DELETE", ctx.getStart().getLine());
    }

    @Override
    public void exitMerge_update_delete_part(PlSqlParser.Merge_update_delete_partContext ctx) {
        h.exitStatementWithChildDedupe("DELETE", ctx.getStop().getLine(), ctx);
    }

    // ========================================
    // 제어 흐름: IF/ELSIF/ELSE/LOOP
    // ========================================

    @Override
    public void enterIf_statement(PlSqlParser.If_statementContext ctx) {
        h.enterStatement("IF", ctx.getStart().getLine());
        // condition 텍스트는 노드 원문에 포함 — 플래그/식별자는 Analyzer 책임
    }

    @Override
    public void exitIf_statement(PlSqlParser.If_statementContext ctx) {
        h.exitStatementWithChildDedupe("IF", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterElsif_part(PlSqlParser.Elsif_partContext ctx) {
        h.enterStatement("ELSIF", ctx.getStart().getLine());
        // condition 은 노드 원문 — Analyzer 책임
    }

    @Override
    public void exitElsif_part(PlSqlParser.Elsif_partContext ctx) {
        h.exitStatementWithChildDedupe("ELSIF", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterElse_part(PlSqlParser.Else_partContext ctx) {
        h.enterStatement("ELSE", ctx.getStart().getLine());
    }

    @Override
    public void exitElse_part(PlSqlParser.Else_partContext ctx) {
        h.exitStatementWithChildDedupe("ELSE", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterLoop_statement(PlSqlParser.Loop_statementContext ctx) {
        h.enterStatement("LOOP", ctx.getStart().getLine());
    }

    @Override
    public void exitLoop_statement(PlSqlParser.Loop_statementContext ctx) {
        h.exitStatementWithChildDedupe("LOOP", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterSingle_column_for_loop(PlSqlParser.Single_column_for_loopContext ctx) {
        h.enterStatement("LOOP", ctx.getStart().getLine());
    }

    @Override
    public void exitSingle_column_for_loop(PlSqlParser.Single_column_for_loopContext ctx) {
        h.exitStatementWithChildDedupe("LOOP", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterMulti_column_for_loop(PlSqlParser.Multi_column_for_loopContext ctx) {
        h.enterStatement("LOOP", ctx.getStart().getLine());
    }

    @Override
    public void exitMulti_column_for_loop(PlSqlParser.Multi_column_for_loopContext ctx) {
        h.exitStatementWithChildDedupe("LOOP", ctx.getStop().getLine(), ctx);
    }

    // ========================================
    // EXCEPTION
    // ========================================

    @Override
    public void enterException_handler(PlSqlParser.Exception_handlerContext ctx) {
        // 첫 WHEN 만 EXCEPTION 노드 push — 이후 WHEN 들은 같은 EXCEPTION 의 형제로 들어옴.
        // EXCEPTION 종료는 exitBody 에서 (peek 가 EXCEPTION 일 때 pop).
        if (!"EXCEPTION".equals(h.getNodeStack().peek().type)) {
            h.enterStatement("EXCEPTION", ctx.getStart().getLine() - 1);
        }
    }

    @Override
    public void exitBody(PlSqlParser.BodyContext ctx) {
        // BEGIN..END 블록에 EXCEPTION 절이 있으면 peek = EXCEPTION → pop.
        // EXCEPTION 절이 없으면 peek 미스매치 → no-op.
        h.exitStatementWithChildDedupe("EXCEPTION", ctx.getStop().getLine(), null);
    }

    @Override
    public void enterSeq_of_statements(PlSqlParser.Seq_of_statementsContext ctx) {
        if (isImplicitTryBlock(ctx)) {
            int beginLine = ((PlSqlParser.BodyContext) ctx.getParent()).BEGIN().getSymbol().getLine();
            h.enterStatement("TRY", beginLine);
        }
    }

    @Override
    public void exitSeq_of_statements(PlSqlParser.Seq_of_statementsContext ctx) {
        if (isImplicitTryBlock(ctx)) {
            h.exitStatementWithChildDedupe("TRY", ctx.getStop().getLine(), ctx);
        }
    }

    /** BEGIN..END 예외처리 블록(자체 BEGIN 텍스트 없이 Body→Statement 로 감싸인 문장열) = TRY 로 마킹. */
    private static boolean isImplicitTryBlock(PlSqlParser.Seq_of_statementsContext ctx) {
        return !ctx.getText().contains("BEGIN")
                && ctx.getParent() instanceof PlSqlParser.BodyContext
                && ctx.getParent().getParent() instanceof PlSqlParser.StatementContext;
    }

    // ========================================
    // CALL
    // ========================================

    @Override
    public void enterCall_statement(PlSqlParser.Call_statementContext ctx) {
        if (isRaiseCall(ctx)) return;
        String name = ctx.routine_name().isEmpty() ? null : ctx.routine_name(0).getText();
        h.enterStatement("CALL", name, ctx.getStart().getLine());
    }

    @Override
    public void exitCall_statement(PlSqlParser.Call_statementContext ctx) {
        if (isRaiseCall(ctx)) return;
        h.exitStatementWithChildDedupe("CALL", ctx.getStop().getLine(), ctx);
    }

    /** enter/exit 쌍이 같은 판정을 재계산해야 하므로 RAISE 감지를 한 곳에 둔다. */
    private static boolean isRaiseCall(PlSqlParser.Call_statementContext ctx) {
        return !ctx.routine_name().isEmpty()
                && ctx.routine_name(0).getText().toUpperCase().contains("RAISE");
    }

    // ========================================
    // CURSOR
    // ========================================

    @Override
    public void enterCursor_declaration(PlSqlParser.Cursor_declarationContext ctx) {
        String name = ctx.identifier() != null ? ctx.identifier().getText() : null;
        h.enterStatement("CURSOR_VARIABLE", name, ctx.getStart().getLine());
    }

    @Override
    public void exitCursor_declaration(PlSqlParser.Cursor_declarationContext ctx) {
        h.exitStatementWithChildDedupe("CURSOR_VARIABLE", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterOpen_statement(PlSqlParser.Open_statementContext ctx) {
        h.enterStatement("OPEN_CURSOR", ctx.getStart().getLine());
    }

    @Override
    public void exitOpen_statement(PlSqlParser.Open_statementContext ctx) {
        h.exitStatementWithChildDedupe("OPEN_CURSOR", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterFetch_statement(PlSqlParser.Fetch_statementContext ctx) {
        h.enterStatement("FETCH", ctx.getStart().getLine());
    }

    @Override
    public void exitFetch_statement(PlSqlParser.Fetch_statementContext ctx) {
        h.exitStatementWithChildDedupe("FETCH", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterClose_statement(PlSqlParser.Close_statementContext ctx) {
        h.enterStatement("CLOSE_CURSOR", ctx.getStart().getLine());
    }

    @Override
    public void exitClose_statement(PlSqlParser.Close_statementContext ctx) {
        h.exitStatementWithChildDedupe("CLOSE_CURSOR", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterOpen_for_statement(PlSqlParser.Open_for_statementContext ctx) {
        h.enterStatement("OPEN_CURSOR", ctx.getStart().getLine());
    }

    @Override
    public void exitOpen_for_statement(PlSqlParser.Open_for_statementContext ctx) {
        h.exitStatementWithChildDedupe("OPEN_CURSOR", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterExit_statement(PlSqlParser.Exit_statementContext ctx) {
        h.enterStatement("EXIT", ctx.getStart().getLine());
    }

    @Override
    public void exitExit_statement(PlSqlParser.Exit_statementContext ctx) {
        h.exitStatementWithChildDedupe("EXIT", ctx.getStop().getLine(), ctx);
    }

    // ========================================
    // CTE/JOIN/EXECUTE/COMMIT
    // ========================================

    @Override
    public void enterSubquery_factoring_clause(PlSqlParser.Subquery_factoring_clauseContext ctx) {
        h.enterStatement("CTE", ctx.getStart().getLine());
    }

    @Override
    public void exitSubquery_factoring_clause(PlSqlParser.Subquery_factoring_clauseContext ctx) {
        h.exitStatementWithChildDedupe("CTE", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterJoin_clause(PlSqlParser.Join_clauseContext ctx) {
        h.enterStatement("JOIN", ctx.getStart().getLine());
    }

    @Override
    public void exitJoin_clause(PlSqlParser.Join_clauseContext ctx) {
        h.exitStatementWithChildDedupe("JOIN", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterSubquery_operation_part(PlSqlParser.Subquery_operation_partContext ctx) {
        h.enterStatement(setOperationType(ctx), ctx.getStart().getLine());
    }

    @Override
    public void exitSubquery_operation_part(PlSqlParser.Subquery_operation_partContext ctx) {
        h.exitStatementWithChildDedupe(setOperationType(ctx), ctx.getStop().getLine(), ctx);
    }

    /** UNION ALL / UNION / INTERSECT / MINUS 노드 타입을 grammar context 에서 추출.
     *  ctx.getText() 는 토큰을 공백 없이 이어붙이므로 "UNIONALL" 형태로 비교한다. */
    private static String setOperationType(PlSqlParser.Subquery_operation_partContext ctx) {
        String t = ctx.getText().toUpperCase();
        if (t.contains("UNIONALL")) return "UNION_ALL";
        if (t.contains("UNION")) return "UNION";
        if (t.contains("INTERSECT")) return "INTERSECT";
        if (t.contains("MINUS")) return "MINUS";
        return "SET_OPERATION";
    }

    @Override
    public void enterExecute_immediate(PlSqlParser.Execute_immediateContext ctx) {
        h.enterStatement("EXECUTE_IMMEDIATE", ctx.getStart().getLine());
    }

    @Override
    public void exitExecute_immediate(PlSqlParser.Execute_immediateContext ctx) {
        h.exitStatementWithChildDedupe("EXECUTE_IMMEDIATE", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterCommit_statement(PlSqlParser.Commit_statementContext ctx) {
        h.enterStatement("COMMIT", ctx.getStart().getLine());
    }

    @Override
    public void exitCommit_statement(PlSqlParser.Commit_statementContext ctx) {
        h.exitStatementWithChildDedupe("COMMIT", ctx.getStop().getLine(), ctx);
    }

}
