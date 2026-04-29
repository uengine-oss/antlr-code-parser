package legacymodernizer.parser.antlr.postgresql;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import legacymodernizer.parser.model.Node;
import legacymodernizer.parser.antlr.ListenerHelper;
import legacymodernizer.parser.antlr.ParserUtils;
import legacymodernizer.parser.antlr.plpgsql.PlpgsqlAstVisitor;
import legacymodernizer.parser.antlr.plpgsql.PlpgsqlLexer;
import legacymodernizer.parser.antlr.plpgsql.PlpgsqlParser;
import legacymodernizer.parser.service.ParseProgressTracker;

/**
 * PostgreSQL 파일 분석을 위한 커스텀 리스너
 * - DDL/DML/DCL 구조 추출
 * - 통일된 속성명 사용 (Node 클래스 참조)
 */
public class PostgreSqlAstListener extends PostgreSQLParserBaseListener {
    private final ListenerHelper h;
    private boolean insideInsert = false;
    private boolean insideExplain = false;

    public Node getRoot() {
        return h.getRoot();
    }

    public PostgreSqlAstListener(CommonTokenStream tokens, ParseProgressTracker tracker) {
        this.h = new ListenerHelper(tokens, tracker);
    }

    public void setFileInfo(String fileName, String filePath) {
        h.setFileInfo(fileName, filePath);
    }

    @Override
    public void enterEveryRule(ParserRuleContext ctx) {
        h.checkProgress(ctx);
    }

    // ========================================
    // 유틸리티 메서드
    // ========================================

    /**
     * PostgreSQL 함수 파라미터 목록을 원본 그대로 추출 (공백·콤마·주석 보존).
     */
    private String extractPostgreSQLParameters(PostgreSQLParser.Func_args_with_defaultsContext funcArgs) {
        if (funcArgs == null || funcArgs.func_args_with_defaults_list() == null) return null;
        java.util.List<PostgreSQLParser.Func_arg_with_defaultContext> args =
            funcArgs.func_args_with_defaults_list().func_arg_with_default();
        if (args == null || args.isEmpty()) return null;
        return ParserUtils.getOriginalText(args.get(0), args.get(args.size() - 1), h.getTokens());
    }

    /**
     * PostgreSQL 함수 리턴 타입을 원본 그대로 추출 (SETOF/TABLE(...) 등 공백 보존).
     */
    private String extractPostgreSQLReturnType(PostgreSQLParser.Func_returnContext funcReturn) {
        return ParserUtils.getOriginalText(funcReturn, h.getTokens());
    }

    // ========================================
    // CREATE FUNCTION / DO
    // ========================================

    @Override
    public void enterCreatefunctionstmt(PostgreSQLParser.CreatefunctionstmtContext ctx) {
        // 함수명 추출
        String fullName = null;
        if (ctx.func_name() != null) {
            fullName = ctx.func_name().getText();
        }

        // 스키마와 이름 분리
        String[] parts = ParserUtils.extractSchemaAndName(fullName);
        String name = parts[1];

        Node node = h.enterStatement("PROCEDURE", name, ctx.getStart().getLine());
        node.schema = parts[0];

        // 파라미터 추출
        if (ctx.func_args_with_defaults() != null) {
            node.parameters = extractPostgreSQLParameters(ctx.func_args_with_defaults());
        }

        // 리턴 타입 추출
        if (ctx.func_return() != null) {
            node.returnType = extractPostgreSQLReturnType(ctx.func_return());
        }

        // 시그니처 추출 (AS $$ 이전까지)
        int dollarLineNumber = findDollarStringLine(ctx);
        node.signature = extractSignatureUntil(ctx, dollarLineNumber);

        if (dollarLineNumber > 0) {
            String plpgsqlCode = extractDollarQuotedString(ctx);
            if (plpgsqlCode != null && !plpgsqlCode.trim().isEmpty()) {
                int leadingNewlines = countRemovedLeadingLines(plpgsqlCode);
                int adjustedBaseLineNumber = dollarLineNumber + leadingNewlines - 1;
                parsePlpgsqlBlock(plpgsqlCode.trim(), adjustedBaseLineNumber);
            }
        }
    }

    private String extractSignatureUntil(PostgreSQLParser.CreatefunctionstmtContext ctx, int dollarLine) {
        if (ctx == null || h.getTokens() == null || dollarLine <= 0) return null;

        StringBuilder sb = new StringBuilder();
        int startIndex = ctx.getStart().getTokenIndex();
        int stopIndex = ctx.getStop().getTokenIndex();

        for (int i = startIndex; i <= stopIndex; i++) {
            Token token = h.getTokens().get(i);
            if (token.getLine() >= dollarLine && token.getText().startsWith("$")) {
                break;
            }
            sb.append(token.getText());
            if (i < stopIndex) sb.append(" ");
        }

        return sb.toString().trim();
    }

    private int findDollarStringLine(PostgreSQLParser.CreatefunctionstmtContext ctx) {
        int startIndex = ctx.getStart().getTokenIndex();
        int stopIndex = ctx.getStop().getTokenIndex();

        for (int i = startIndex; i <= stopIndex; i++) {
            Token token = h.getTokens().get(i);
            if (token.getText().startsWith("$") && token.getText().endsWith("$")) {
                return token.getLine();
            }
        }
        return -1;
    }

    @Override
    public void exitCreatefunctionstmt(PostgreSQLParser.CreatefunctionstmtContext ctx) {
        h.exitStatement("PROCEDURE", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterDostmt(PostgreSQLParser.DostmtContext ctx) {
        h.enterStatement("DO", ctx.getStart().getLine());

        int dollarLineNumber = findDollarStringLineForDo(ctx);

        if (dollarLineNumber > 0) {
            String plpgsqlCode = extractDollarQuotedStringForDo(ctx);
            if (plpgsqlCode != null && !plpgsqlCode.trim().isEmpty()) {
                int leadingNewlines = countRemovedLeadingLines(plpgsqlCode);
                int adjustedBaseLineNumber = dollarLineNumber + leadingNewlines - 1;
                parsePlpgsqlBlock(plpgsqlCode.trim(), adjustedBaseLineNumber);
            }
        }
    }

    @Override
    public void exitDostmt(PostgreSQLParser.DostmtContext ctx) {
        h.exitStatement("DO", ctx.getStop().getLine(), ctx);
    }

    private int findDollarStringLineForDo(PostgreSQLParser.DostmtContext ctx) {
        int startIndex = ctx.getStart().getTokenIndex();
        int stopIndex = ctx.getStop().getTokenIndex();

        for (int i = startIndex; i <= stopIndex; i++) {
            Token token = h.getTokens().get(i);
            if (token.getText().startsWith("$") && token.getText().endsWith("$")) {
                return token.getLine();
            }
        }
        return -1;
    }

    private String extractDollarQuotedStringForDo(PostgreSQLParser.DostmtContext ctx) {
        if (ctx.dostmt_opt_list() != null) {
            for (PostgreSQLParser.Dostmt_opt_itemContext optItem : ctx.dostmt_opt_list().dostmt_opt_item()) {
                if (optItem.sconst() != null) {
                    return extractFromSconst(optItem.sconst());
                }
            }
        }
        return null;
    }

    private String extractDollarQuotedString(PostgreSQLParser.CreatefunctionstmtContext ctx) {
        PostgreSQLParser.Createfunc_opt_listContext optList = ctx.createfunc_opt_list();
        if (optList == null) return null;

        for (PostgreSQLParser.Createfunc_opt_itemContext optItem : optList.createfunc_opt_item()) {
            if (optItem.AS() != null && optItem.func_as() != null) {
                PostgreSQLParser.Func_asContext funcAs = optItem.func_as();
                if (funcAs.def != null) {
                    return extractFromSconst(funcAs.def);
                }
            }
        }
        return null;
    }

    private String extractFromSconst(PostgreSQLParser.SconstContext sconstCtx) {
        if (sconstCtx == null || sconstCtx.anysconst() == null) return null;

        PostgreSQLParser.AnysconstContext anysconst = sconstCtx.anysconst();
        if (anysconst.BeginDollarStringConstant() == null) return null;

        StringBuilder content = new StringBuilder();
        for (TerminalNode dollarText : anysconst.DollarText()) {
            content.append(dollarText.getText());
        }
        return content.toString();
    }

    private int countRemovedLeadingLines(String text) {
        int count = 0;
        int i = 0;

        while (i < text.length() && Character.isWhitespace(text.charAt(i))) {
            char c = text.charAt(i);
            if (c == '\r') {
                count++;
                i++;
                if (i < text.length() && text.charAt(i) == '\n') {
                    i++;
                }
            } else if (c == '\n') {
                count++;
                i++;
            } else {
                i++;
            }
        }
        return count;
    }

    private void parsePlpgsqlBlock(String plpgsqlCode, int baseLineNumber) {
        try {
            CharStream input = CharStreams.fromString(plpgsqlCode);
            PlpgsqlLexer lexer = new PlpgsqlLexer(input);
            CommonTokenStream plTokens = new CommonTokenStream(lexer);
            PlpgsqlParser parser = new PlpgsqlParser(plTokens);

            int adjustedBaseLineNumber = baseLineNumber;

            // PL/pgSQL 파싱 오류는 silently skip — outer PostgreSQL 파싱은 계속.
            parser.removeErrorListeners();

            ParseTree tree = parser.plpgsqlBlock();

            PlpgsqlAstVisitor visitor = new PlpgsqlAstVisitor(
                h.getNodeStack().peek(),
                adjustedBaseLineNumber,
                plTokens
            );
            visitor.visit(tree);

        } catch (Exception e) {
            System.err.println("Error parsing PL/pgSQL: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ========================================
    // DDL
    // ========================================

    @Override
    public void enterVariablesetstmt(PostgreSQLParser.VariablesetstmtContext ctx) {
        h.enterStatement("SET", ctx.getStart().getLine());
    }

    @Override
    public void exitVariablesetstmt(PostgreSQLParser.VariablesetstmtContext ctx) {
        h.exitStatement("SET", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterVariableresetstmt(PostgreSQLParser.VariableresetstmtContext ctx) {
        h.enterStatement("RESET", ctx.getStart().getLine());
    }

    @Override
    public void exitVariableresetstmt(PostgreSQLParser.VariableresetstmtContext ctx) {
        h.exitStatement("RESET", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterDropstmt(PostgreSQLParser.DropstmtContext ctx) {
        h.enterStatement("DROP", ctx.getStart().getLine());
    }

    @Override
    public void exitDropstmt(PostgreSQLParser.DropstmtContext ctx) {
        h.exitStatement("DROP", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterDroprolestmt(PostgreSQLParser.DroprolestmtContext ctx) {
        h.enterStatement("DROP_ROLE", ctx.getStart().getLine());
    }

    @Override
    public void exitDroprolestmt(PostgreSQLParser.DroprolestmtContext ctx) {
        h.exitStatement("DROP_ROLE", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterCreatestmt(PostgreSQLParser.CreatestmtContext ctx) {
        h.enterStatement("CREATE_TABLE", ctx.getStart().getLine());
    }

    @Override
    public void exitCreatestmt(PostgreSQLParser.CreatestmtContext ctx) {
        h.exitStatement("CREATE_TABLE", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterAltertablestmt(PostgreSQLParser.AltertablestmtContext ctx) {
        h.enterStatement("ALTER_TABLE", ctx.getStart().getLine());
    }

    @Override
    public void exitAltertablestmt(PostgreSQLParser.AltertablestmtContext ctx) {
        h.exitStatement("ALTER_TABLE", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterAlterrolestmt(PostgreSQLParser.AlterrolestmtContext ctx) {
        h.enterStatement("ALTER_ROLE", ctx.getStart().getLine());
    }

    @Override
    public void exitAlterrolestmt(PostgreSQLParser.AlterrolestmtContext ctx) {
        h.exitStatement("ALTER_ROLE", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterAlterdatabasestmt(PostgreSQLParser.AlterdatabasestmtContext ctx) {
        h.enterStatement("ALTER_DATABASE", ctx.getStart().getLine());
    }

    @Override
    public void exitAlterdatabasestmt(PostgreSQLParser.AlterdatabasestmtContext ctx) {
        h.exitStatement("ALTER_DATABASE", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterAlterfunctionstmt(PostgreSQLParser.AlterfunctionstmtContext ctx) {
        h.enterStatement("ALTER_FUNCTION", ctx.getStart().getLine());
    }

    @Override
    public void exitAlterfunctionstmt(PostgreSQLParser.AlterfunctionstmtContext ctx) {
        h.exitStatement("ALTER_FUNCTION", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterIndexstmt(PostgreSQLParser.IndexstmtContext ctx) {
        h.enterStatement("CREATE_INDEX", ctx.getStart().getLine());
    }

    @Override
    public void exitIndexstmt(PostgreSQLParser.IndexstmtContext ctx) {
        h.exitStatement("CREATE_INDEX", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterCreateopfamilystmt(PostgreSQLParser.CreateopfamilystmtContext ctx) {
        h.enterStatement("CREATE_OPERATOR_FAMILY", ctx.getStart().getLine());
    }

    @Override
    public void exitCreateopfamilystmt(PostgreSQLParser.CreateopfamilystmtContext ctx) {
        h.exitStatement("CREATE_OPERATOR_FAMILY", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterAlteropfamilystmt(PostgreSQLParser.AlteropfamilystmtContext ctx) {
        h.enterStatement("ALTER_OPERATOR_FAMILY", ctx.getStart().getLine());
    }

    @Override
    public void exitAlteropfamilystmt(PostgreSQLParser.AlteropfamilystmtContext ctx) {
        h.exitStatement("ALTER_OPERATOR_FAMILY", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterDropopfamilystmt(PostgreSQLParser.DropopfamilystmtContext ctx) {
        h.enterStatement("DROP_OPERATOR_FAMILY", ctx.getStart().getLine());
    }

    @Override
    public void exitDropopfamilystmt(PostgreSQLParser.DropopfamilystmtContext ctx) {
        h.exitStatement("DROP_OPERATOR_FAMILY", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterCreateopclassstmt(PostgreSQLParser.CreateopclassstmtContext ctx) {
        h.enterStatement("CREATE_OPERATOR_CLASS", ctx.getStart().getLine());
    }

    @Override
    public void exitCreateopclassstmt(PostgreSQLParser.CreateopclassstmtContext ctx) {
        h.exitStatement("CREATE_OPERATOR_CLASS", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterDropopclassstmt(PostgreSQLParser.DropopclassstmtContext ctx) {
        h.enterStatement("DROP_OPERATOR_CLASS", ctx.getStart().getLine());
    }

    @Override
    public void exitDropopclassstmt(PostgreSQLParser.DropopclassstmtContext ctx) {
        h.exitStatement("DROP_OPERATOR_CLASS", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterCreateschemastmt(PostgreSQLParser.CreateschemastmtContext ctx) {
        h.enterStatement("CREATE_SCHEMA", ctx.getStart().getLine());
    }

    @Override
    public void exitCreateschemastmt(PostgreSQLParser.CreateschemastmtContext ctx) {
        h.exitStatement("CREATE_SCHEMA", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterCreateseqstmt(PostgreSQLParser.CreateseqstmtContext ctx) {
        h.enterStatement("CREATE_SEQUENCE", ctx.getStart().getLine());
    }

    @Override
    public void exitCreateseqstmt(PostgreSQLParser.CreateseqstmtContext ctx) {
        h.exitStatement("CREATE_SEQUENCE", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterCreatetrigstmt(PostgreSQLParser.CreatetrigstmtContext ctx) {
        h.enterStatement("CREATE_TRIGGER", ctx.getStart().getLine());
    }

    @Override
    public void exitCreatetrigstmt(PostgreSQLParser.CreatetrigstmtContext ctx) {
        h.exitStatement("CREATE_TRIGGER", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterRulestmt(PostgreSQLParser.RulestmtContext ctx) {
        h.enterStatement("CREATE_RULE", ctx.getStart().getLine());
    }

    @Override
    public void exitRulestmt(PostgreSQLParser.RulestmtContext ctx) {
        h.exitStatement("CREATE_RULE", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterCreatedbstmt(PostgreSQLParser.CreatedbstmtContext ctx) {
        h.enterStatement("CREATE_DATABASE", ctx.getStart().getLine());
    }

    @Override
    public void exitCreatedbstmt(PostgreSQLParser.CreatedbstmtContext ctx) {
        h.exitStatement("CREATE_DATABASE", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterCreateuserstmt(PostgreSQLParser.CreateuserstmtContext ctx) {
        h.enterStatement("CREATE_USER", ctx.getStart().getLine());
    }

    @Override
    public void exitCreateuserstmt(PostgreSQLParser.CreateuserstmtContext ctx) {
        h.exitStatement("CREATE_USER", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterCreaterolestmt(PostgreSQLParser.CreaterolestmtContext ctx) {
        h.enterStatement("CREATE_ROLE", ctx.getStart().getLine());
    }

    @Override
    public void exitCreaterolestmt(PostgreSQLParser.CreaterolestmtContext ctx) {
        h.exitStatement("CREATE_ROLE", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterCreategroupstmt(PostgreSQLParser.CreategroupstmtContext ctx) {
        h.enterStatement("CREATE_GROUP", ctx.getStart().getLine());
    }

    @Override
    public void exitCreategroupstmt(PostgreSQLParser.CreategroupstmtContext ctx) {
        h.exitStatement("CREATE_GROUP", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterViewstmt(PostgreSQLParser.ViewstmtContext ctx) {
        h.enterStatement("CREATE_VIEW", ctx.getStart().getLine());
    }

    @Override
    public void exitViewstmt(PostgreSQLParser.ViewstmtContext ctx) {
        h.exitStatement("CREATE_VIEW", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterTruncatestmt(PostgreSQLParser.TruncatestmtContext ctx) {
        h.enterStatement("TRUNCATE", ctx.getStart().getLine());
    }

    @Override
    public void exitTruncatestmt(PostgreSQLParser.TruncatestmtContext ctx) {
        h.exitStatement("TRUNCATE", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterCopystmt(PostgreSQLParser.CopystmtContext ctx) {
        h.enterStatement("COPY", ctx.getStart().getLine());
    }

    @Override
    public void exitCopystmt(PostgreSQLParser.CopystmtContext ctx) {
        h.exitStatement("COPY", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterDefinestmt(PostgreSQLParser.DefinestmtContext ctx) {
        String defineType = "DEFINE";
        String secondToken = ctx.getChild(1).getText().toUpperCase();

        if (secondToken.equals("AGGREGATE")) {
            defineType = "CREATE_AGGREGATE";
        } else if (secondToken.equals("OPERATOR")) {
            defineType = "CREATE_OPERATOR";
        } else if (secondToken.equals("TYPE")) {
            defineType = "CREATE_TYPE";
        }

        h.enterStatement(defineType, ctx.getStart().getLine());
    }

    @Override
    public void exitDefinestmt(PostgreSQLParser.DefinestmtContext ctx) {
        String defineType = "DEFINE";
        String secondToken = ctx.getChild(1).getText().toUpperCase();

        if (secondToken.equals("AGGREGATE")) {
            defineType = "CREATE_AGGREGATE";
        } else if (secondToken.equals("OPERATOR")) {
            defineType = "CREATE_OPERATOR";
        } else if (secondToken.equals("TYPE")) {
            defineType = "CREATE_TYPE";
        }

        h.exitStatement(defineType, ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterRenamestmt(PostgreSQLParser.RenamestmtContext ctx) {
        h.enterStatement("RENAME", ctx.getStart().getLine());
    }

    @Override
    public void exitRenamestmt(PostgreSQLParser.RenamestmtContext ctx) {
        h.exitStatement("RENAME", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterAlterownerstmt(PostgreSQLParser.AlterownerstmtContext ctx) {
        h.enterStatement("ALTER_OWNER", ctx.getStart().getLine());
    }

    @Override
    public void exitAlterownerstmt(PostgreSQLParser.AlterownerstmtContext ctx) {
        h.exitStatement("ALTER_OWNER", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterAlterobjectschemastmt(PostgreSQLParser.AlterobjectschemastmtContext ctx) {
        h.enterStatement("ALTER_SCHEMA", ctx.getStart().getLine());
    }

    @Override
    public void exitAlterobjectschemastmt(PostgreSQLParser.AlterobjectschemastmtContext ctx) {
        h.exitStatement("ALTER_SCHEMA", ctx.getStop().getLine(), ctx);
    }

    // ========================================
    // DML
    // ========================================

    @Override
    public void enterSelectstmt(PostgreSQLParser.SelectstmtContext ctx) {
        if (insideInsert || insideExplain) return;
        h.enterStatement("SELECT", ctx.getStart().getLine());
    }

    @Override
    public void exitSelectstmt(PostgreSQLParser.SelectstmtContext ctx) {
        if (insideInsert || insideExplain) return;
        h.exitStatement("SELECT", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterInsertstmt(PostgreSQLParser.InsertstmtContext ctx) {
        insideInsert = true;
        h.enterStatement("INSERT", ctx.getStart().getLine());
    }

    @Override
    public void exitInsertstmt(PostgreSQLParser.InsertstmtContext ctx) {
        h.exitStatement("INSERT", ctx.getStop().getLine(), ctx);
        insideInsert = false;
    }

    @Override
    public void enterUpdatestmt(PostgreSQLParser.UpdatestmtContext ctx) {
        h.enterStatement("UPDATE", ctx.getStart().getLine());
    }

    @Override
    public void exitUpdatestmt(PostgreSQLParser.UpdatestmtContext ctx) {
        h.exitStatement("UPDATE", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterDeletestmt(PostgreSQLParser.DeletestmtContext ctx) {
        h.enterStatement("DELETE", ctx.getStart().getLine());
    }

    @Override
    public void exitDeletestmt(PostgreSQLParser.DeletestmtContext ctx) {
        h.exitStatement("DELETE", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterMergestmt(PostgreSQLParser.MergestmtContext ctx) {
        h.enterStatement("MERGE", ctx.getStart().getLine());
    }

    @Override
    public void exitMergestmt(PostgreSQLParser.MergestmtContext ctx) {
        h.exitStatement("MERGE", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterMerge_insert_clause(PostgreSQLParser.Merge_insert_clauseContext ctx) {
        h.enterStatement("MERGE_INSERT", ctx.getStart().getLine());
    }

    @Override
    public void exitMerge_insert_clause(PostgreSQLParser.Merge_insert_clauseContext ctx) {
        h.exitStatement("MERGE_INSERT", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterMerge_update_clause(PostgreSQLParser.Merge_update_clauseContext ctx) {
        h.enterStatement("MERGE_UPDATE", ctx.getStart().getLine());
    }

    @Override
    public void exitMerge_update_clause(PostgreSQLParser.Merge_update_clauseContext ctx) {
        h.exitStatement("MERGE_UPDATE", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterMerge_delete_clause(PostgreSQLParser.Merge_delete_clauseContext ctx) {
        h.enterStatement("MERGE_DELETE", ctx.getStart().getLine());
    }

    @Override
    public void exitMerge_delete_clause(PostgreSQLParser.Merge_delete_clauseContext ctx) {
        h.exitStatement("MERGE_DELETE", ctx.getStop().getLine(), ctx);
    }

    // ========================================
    // DCL
    // ========================================

    @Override
    public void enterGrantstmt(PostgreSQLParser.GrantstmtContext ctx) {
        h.enterStatement("GRANT", ctx.getStart().getLine());
    }

    @Override
    public void exitGrantstmt(PostgreSQLParser.GrantstmtContext ctx) {
        h.exitStatement("GRANT", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterRevokestmt(PostgreSQLParser.RevokestmtContext ctx) {
        h.enterStatement("REVOKE", ctx.getStart().getLine());
    }

    @Override
    public void exitRevokestmt(PostgreSQLParser.RevokestmtContext ctx) {
        h.exitStatement("REVOKE", ctx.getStop().getLine(), ctx);
    }

    // ========================================
    // TCL
    // ========================================

    @Override
    public void enterTransactionstmt(PostgreSQLParser.TransactionstmtContext ctx) {
        String transactionType = "TRANSACTION";
        String firstToken = ctx.getStart().getText().toUpperCase();

        if (firstToken.equals("BEGIN") || firstToken.equals("START")) {
            transactionType = "BEGIN";
        } else if (firstToken.equals("COMMIT") || firstToken.equals("END")) {
            transactionType = "COMMIT";
        } else if (firstToken.equals("ROLLBACK") || firstToken.equals("ABORT")) {
            transactionType = "ROLLBACK";
        } else if (firstToken.equals("SAVEPOINT")) {
            transactionType = "SAVEPOINT";
        } else if (firstToken.equals("RELEASE")) {
            transactionType = "RELEASE";
        } else if (firstToken.equals("PREPARE")) {
            transactionType = "PREPARE_TRANSACTION";
        }

        h.enterStatement(transactionType, ctx.getStart().getLine());
    }

    @Override
    public void exitTransactionstmt(PostgreSQLParser.TransactionstmtContext ctx) {
        String firstToken = ctx.getStart().getText().toUpperCase();
        String transactionType = "TRANSACTION";

        if (firstToken.equals("BEGIN") || firstToken.equals("START")) {
            transactionType = "BEGIN";
        } else if (firstToken.equals("COMMIT") || firstToken.equals("END")) {
            transactionType = "COMMIT";
        } else if (firstToken.equals("ROLLBACK") || firstToken.equals("ABORT")) {
            transactionType = "ROLLBACK";
        } else if (firstToken.equals("SAVEPOINT")) {
            transactionType = "SAVEPOINT";
        } else if (firstToken.equals("RELEASE")) {
            transactionType = "RELEASE";
        } else if (firstToken.equals("PREPARE")) {
            transactionType = "PREPARE_TRANSACTION";
        }

        h.exitStatement(transactionType, ctx.getStop().getLine(), ctx);
    }

    // ========================================
    // Utility
    // ========================================

    @Override
    public void enterAnalyzestmt(PostgreSQLParser.AnalyzestmtContext ctx) {
        h.enterStatement("ANALYZE", ctx.getStart().getLine());
    }

    @Override
    public void exitAnalyzestmt(PostgreSQLParser.AnalyzestmtContext ctx) {
        h.exitStatement("ANALYZE", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterVacuumstmt(PostgreSQLParser.VacuumstmtContext ctx) {
        h.enterStatement("VACUUM", ctx.getStart().getLine());
    }

    @Override
    public void exitVacuumstmt(PostgreSQLParser.VacuumstmtContext ctx) {
        h.exitStatement("VACUUM", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterExplainstmt(PostgreSQLParser.ExplainstmtContext ctx) {
        insideExplain = true;
        h.enterStatement("EXPLAIN", ctx.getStart().getLine());
    }

    @Override
    public void exitExplainstmt(PostgreSQLParser.ExplainstmtContext ctx) {
        h.exitStatement("EXPLAIN", ctx.getStop().getLine(), ctx);
        insideExplain = false;
    }

    @Override
    public void enterPreparestmt(PostgreSQLParser.PreparestmtContext ctx) {
        h.enterStatement("PREPARE", ctx.getStart().getLine());
    }

    @Override
    public void exitPreparestmt(PostgreSQLParser.PreparestmtContext ctx) {
        h.exitStatement("PREPARE", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterExecutestmt(PostgreSQLParser.ExecutestmtContext ctx) {
        h.enterStatement("EXECUTE", ctx.getStart().getLine());
    }

    @Override
    public void exitExecutestmt(PostgreSQLParser.ExecutestmtContext ctx) {
        h.exitStatement("EXECUTE", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterLockstmt(PostgreSQLParser.LockstmtContext ctx) {
        h.enterStatement("LOCK", ctx.getStart().getLine());
    }

    @Override
    public void exitLockstmt(PostgreSQLParser.LockstmtContext ctx) {
        h.exitStatement("LOCK", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterReindexstmt(PostgreSQLParser.ReindexstmtContext ctx) {
        h.enterStatement("REINDEX", ctx.getStart().getLine());
    }

    @Override
    public void exitReindexstmt(PostgreSQLParser.ReindexstmtContext ctx) {
        h.exitStatement("REINDEX", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterClusterstmt(PostgreSQLParser.ClusterstmtContext ctx) {
        h.enterStatement("CLUSTER", ctx.getStart().getLine());
    }

    @Override
    public void exitClusterstmt(PostgreSQLParser.ClusterstmtContext ctx) {
        h.exitStatement("CLUSTER", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterCommentstmt(PostgreSQLParser.CommentstmtContext ctx) {
        h.enterStatement("COMMENT", ctx.getStart().getLine());
    }

    @Override
    public void exitCommentstmt(PostgreSQLParser.CommentstmtContext ctx) {
        h.exitStatement("COMMENT", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterCase_expr(PostgreSQLParser.Case_exprContext ctx) {
        // SQL CASE 표현식은 노드로 만들지 않음
    }

    @Override
    public void exitCase_expr(PostgreSQLParser.Case_exprContext ctx) {
        // SQL CASE 표현식은 노드로 만들지 않음
    }
}
