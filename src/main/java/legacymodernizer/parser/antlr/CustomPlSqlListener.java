package legacymodernizer.parser.antlr;

import java.util.Stack;
import legacymodernizer.parser.antlr.plsql.PlSqlParserBaseListener;
import legacymodernizer.parser.antlr.plsql.PlSqlParser;
import org.antlr.v4.runtime.TokenStream;

public class CustomPlSqlListener extends PlSqlParserBaseListener {
    @SuppressWarnings("unused")
    private TokenStream tokens;
    private Stack<Node> nodeStack = new Stack<>();
    private Node root = new Node("ROOT", 0, null); // 루트 노드

    public Node getRoot() {
        return root;
    }

    public CustomPlSqlListener(TokenStream tokens) {
        this.tokens = tokens;
        nodeStack.push(root); // 초기 상태에서 루트 노드를 스택에 푸시
    }

    private void enterStatement(String statementType, int line) {
        Node currentNode = new Node(statementType, line, nodeStack.peek());
        nodeStack.push(currentNode);
        // System.out.println("Enter " + statementType + " Statement Line: " + line);
    }

    private void exitStatement(String statementType, int line) {
        Node node = nodeStack.pop();
        node.endLine = line;
        // System.out.println("Exit " + statementType + " Statement Line: " + line);
    }

    @Override
    public void enterCreate_procedure_body(PlSqlParser.Create_procedure_bodyContext ctx) {
        enterStatement("CREATE_PROCEDURE_BODY", ctx.getStart().getLine());
    }

    @Override
    public void exitCreate_procedure_body(PlSqlParser.Create_procedure_bodyContext ctx) {
        exitStatement("CREATE_PROCEDURE_BODY", ctx.getStop().getLine());
    }

    @Override
    public void enterCreate_package(PlSqlParser.Create_packageContext ctx) {
        enterStatement("PACKAGE_SPEC", ctx.getStart().getLine());
    }

    @Override
    public void exitCreate_package(PlSqlParser.Create_packageContext ctx) {
        exitStatement("PACKAGE_SPEC", ctx.getStop().getLine());
    }

    @Override
    public void enterCreate_package_body(PlSqlParser.Create_package_bodyContext ctx) {
        enterStatement("PACKAGE_BODY", ctx.getStart().getLine());
    }

    @Override
    public void exitCreate_package_body(PlSqlParser.Create_package_bodyContext ctx) {
        exitStatement("PACKAGE_BODY", ctx.getStop().getLine());
    }

    @Override
    public void enterAssignment_statement(PlSqlParser.Assignment_statementContext ctx) {
        enterStatement("ASSIGNMENT", ctx.getStart().getLine());
    }

    @Override
    public void exitAssignment_statement(PlSqlParser.Assignment_statementContext ctx) {
        exitStatement("ASSIGNMENT", ctx.getStop().getLine());
    }

    @Override
    public void enterIf_statement(PlSqlParser.If_statementContext ctx) {
        enterStatement("IF", ctx.getStart().getLine());
    }

    @Override
    public void exitIf_statement(PlSqlParser.If_statementContext ctx) {
        exitStatement("IF", ctx.getStop().getLine());
    }

    @Override
    public void enterInsert_statement(PlSqlParser.Insert_statementContext ctx) {
        enterStatement("INSERT", ctx.getStart().getLine());
    }

    @Override
    public void exitInsert_statement(PlSqlParser.Insert_statementContext ctx) {
        exitStatement("INSERT", ctx.getStop().getLine());
    }

    @Override
    public void enterSelect_statement(PlSqlParser.Select_statementContext ctx) {
        enterStatement("SELECT", ctx.getStart().getLine());
    }

    @Override
    public void exitSelect_statement(PlSqlParser.Select_statementContext ctx) {
        exitStatement("SELECT", ctx.getStop().getLine());
    }

    @Override
    public void enterUpdate_statement(PlSqlParser.Update_statementContext ctx) {
        enterStatement("UPDATE", ctx.getStart().getLine());
    }

    @Override
    public void exitUpdate_statement(PlSqlParser.Update_statementContext ctx) {
        exitStatement("UPDATE", ctx.getStop().getLine());
    }

    @Override
    public void enterExecute_immediate(PlSqlParser.Execute_immediateContext ctx) {
        enterStatement("EXECUTE_IMMDDIATE", ctx.getStart().getLine());
    }

    @Override
    public void exitExecute_immediate(PlSqlParser.Execute_immediateContext ctx) {
        exitStatement("EXECUTE_IMMDDIATE", ctx.getStop().getLine());
    }

    @Override
    public void enterMerge_statement(PlSqlParser.Merge_statementContext ctx) {
        enterStatement("MERGE", ctx.getStart().getLine());
    }

    @Override
    public void exitMerge_statement(PlSqlParser.Merge_statementContext ctx) {
        exitStatement("MERGE", ctx.getStop().getLine());
    }

    @Override
    public void enterDelete_statement(PlSqlParser.Delete_statementContext ctx) {
        enterStatement("DELETE", ctx.getStart().getLine());
    }

    @Override
    public void exitDelete_statement(PlSqlParser.Delete_statementContext ctx) {
        exitStatement("DELETE", ctx.getStop().getLine());
    }

    @Override
    public void enterLoop_statement(PlSqlParser.Loop_statementContext ctx) {
        enterStatement("WHILE", ctx.getStart().getLine());
    }

    @Override
    public void exitLoop_statement(PlSqlParser.Loop_statementContext ctx) {
        exitStatement("WHILE", ctx.getStop().getLine());
    }

    @Override
    public void enterSeq_of_declare_specs(PlSqlParser.Seq_of_declare_specsContext ctx) {
        enterStatement("DECLARE", ctx.getStart().getLine());
    }

    @Override
    public void exitSeq_of_declare_specs(PlSqlParser.Seq_of_declare_specsContext ctx) {
        exitStatement("DECLARE", ctx.getStop().getLine());
    }

    @Override
    public void enterReturn_statement(PlSqlParser.Return_statementContext ctx) {
        enterStatement("RETURN", ctx.getStart().getLine());
    }

    @Override
    public void exitReturn_statement(PlSqlParser.Return_statementContext ctx) {
        exitStatement("RETURN", ctx.getStop().getLine());
    }

    @Override
    public void enterException_handler(PlSqlParser.Exception_handlerContext ctx) {
        enterStatement("EXCEPTION", ctx.getStart().getLine());
    }
    
    @Override
    public void exitException_handler(PlSqlParser.Exception_handlerContext ctx) {
        exitStatement("EXCEPTION", ctx.getStop().getLine());
    }


    @Override
    public void enterFunction_body(PlSqlParser.Function_bodyContext ctx) {
        enterStatement("BODY", ctx.getStart().getLine());
    }

    @Override
    public void exitFunction_body(PlSqlParser.Function_bodyContext ctx) {
        exitStatement("BODY", ctx.getStop().getLine());
    }

    @Override
    public void enterProcedure_body(PlSqlParser.Procedure_bodyContext ctx) {
        enterStatement("BODY", ctx.getStart().getLine());
    }

    @Override
    public void exitProcedure_body(PlSqlParser.Procedure_bodyContext ctx) {
        exitStatement("BODY", ctx.getStop().getLine());
    }


    @Override
    public void enterSeq_of_statements(PlSqlParser.Seq_of_statementsContext ctx) {
        String text = ctx.getText();
        // 프로시저의 본문인 경우 (BodyContext의 자식이면서 Create_procedure_bodyContext의 손자)
        if (ctx.getParent() instanceof PlSqlParser.BodyContext && 
            ctx.getParent().getParent() instanceof PlSqlParser.Create_procedure_bodyContext) {
            enterStatement("BODY", ctx.getStart().getLine());
        }
        // BEGIN-END 블록의 내용인 경우
        else if (!text.contains("BEGIN") && 
                 ctx.getParent() instanceof PlSqlParser.BodyContext && 
                 ctx.getParent().getParent() instanceof PlSqlParser.StatementContext) {
            
            enterStatement("TRY", ctx.getStart().getLine());
        }
    }
    
    @Override
    public void exitSeq_of_statements(PlSqlParser.Seq_of_statementsContext ctx) {
        
        String text = ctx.getText();
        // 프로시저의 본문인 경우
        if (ctx.getParent() instanceof PlSqlParser.BodyContext && 
            ctx.getParent().getParent() instanceof PlSqlParser.Create_procedure_bodyContext) {
            exitStatement("BODY", ctx.getStop().getLine());
        }
        // BEGIN-END 블록의 내용인 경우
        else if (!text.contains("BEGIN") && 
                 ctx.getParent() instanceof PlSqlParser.BodyContext && 
                 ctx.getParent().getParent() instanceof PlSqlParser.StatementContext) {
            
            exitStatement("TRY", ctx.getStop().getLine());
        }
    }
    
    @Override
    public void enterCall_statement(PlSqlParser.Call_statementContext ctx) {
        String statementType = "CALL";
        if (!ctx.routine_name().isEmpty()) {
            PlSqlParser.Routine_nameContext routineName = ctx.routine_name(0); 
            if (routineName.getText().toUpperCase().contains("RAISE")) {
                // statementType = "RAISE";
                return;
            }
        }
        enterStatement(statementType, ctx.getStart().getLine());
    }
    
    @Override
    public void exitCall_statement(PlSqlParser.Call_statementContext ctx) {
        String statementType = "CALL";
        if (!ctx.routine_name().isEmpty()) {
            PlSqlParser.Routine_nameContext routineName = ctx.routine_name(0);
            if (routineName.getText().toUpperCase().contains("RAISE")) {
                // statementType = "RAISE";
                return;
            }
        }
        exitStatement(statementType, ctx.getStop().getLine());
    }

    @Override
    public void enterPackage_obj_spec(PlSqlParser.Package_obj_specContext ctx) {
        enterStatement("PROCEDURE_SPEC", ctx.getStart().getLine());
    }

    @Override
    public void exitPackage_obj_spec(PlSqlParser.Package_obj_specContext ctx) {
        exitStatement("PROCEDURE_SPEC", ctx.getStop().getLine());
    }

    @Override
    public void enterPackage_obj_body(PlSqlParser.Package_obj_bodyContext ctx) {
        String memberType = ctx.function_body() != null ? "FUNCTION" : "PROCEDURE";
        enterStatement(memberType, ctx.getStart().getLine());
    }
    
    @Override
    public void exitPackage_obj_body(PlSqlParser.Package_obj_bodyContext ctx) {
        String memberType = ctx.function_body() != null ? "FUNCTION" : "PROCEDURE";
        exitStatement(memberType, ctx.getStop().getLine());
    }

    // 트리 구조를 출력하는 메서드 (디버깅 목적)
    public void printTree(Node node, String indent) {
        System.out.println(indent + node.type + " (" + node.startLine + ", " + node.endLine + ")");
        for (Node child : node.children) {
            printTree(child, indent + "  ");
        }
    }

    // 트리 구조 출력을 위한 메서드 호출 예시
    public void printStructure() {
        printTree(root, "");
    }
}