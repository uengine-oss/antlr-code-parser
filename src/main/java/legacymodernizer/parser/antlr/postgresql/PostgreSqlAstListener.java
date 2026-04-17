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
    private boolean plpgsqlLogErrors = false;

    public Node getRoot() {
        return h.getRoot();
    }

    public PostgreSqlAstListener(TokenStream tokens) {
        this.h = new ListenerHelper((CommonTokenStream) tokens, null);
    }

    public PostgreSqlAstListener(TokenStream tokens, ParseProgressTracker tracker) {
        this.h = new ListenerHelper((CommonTokenStream) tokens, tracker);
    }

    @Override
    public void enterEveryRule(ParserRuleContext ctx) {
        h.checkProgress(ctx);
    }

    // ========================================
    // 노드 생성/종료
    // ========================================

    private Node enterStatement(String type, int line) {
        return enterStatement(type, null, line);
    }

    private Node enterStatement(String type, String name, int line) {
        Node node = new Node(type, name, line, h.getNodeStack().peek());
        h.getNodeStack().push(node);
        return node;
    }

    private void exitStatement(String type, int line) {
        exitStatement(type, line, null);
    }

    private void exitStatement(String type, int line, ParserRuleContext ctx) {
        if (h.getNodeStack().isEmpty()) return;
        Node node = h.getNodeStack().pop();
        node.endLine = line;
        if (ctx != null) {
            node.comment = ParserUtils.getLeadingComment(ctx, h.getTokens());
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
     * PostgreSQL 함수 파라미터 목록 추출
     */
    private String extractPostgreSQLParameters(PostgreSQLParser.Func_args_with_defaultsContext funcArgs) {
        if (funcArgs == null || funcArgs.func_args_with_defaults_list() == null) return null;

        java.util.List<PostgreSQLParser.Func_arg_with_defaultContext> args =
            funcArgs.func_args_with_defaults_list().func_arg_with_default();

        if (args == null || args.isEmpty()) return null;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(args.get(i).getText());
        }
        return sb.toString();
    }

    /**
     * PostgreSQL 함수 리턴 타입 추출
     */
    private String extractPostgreSQLReturnType(PostgreSQLParser.Func_returnContext funcReturn) {
        if (funcReturn == null) return null;
        return funcReturn.getText();
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
        String[] parts = extractSchemaAndName(fullName);
        String name = parts[1];

        Node node = enterStatement("PROCEDURE", name, ctx.getStart().getLine());
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
        exitStatement("PROCEDURE", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterDostmt(PostgreSQLParser.DostmtContext ctx) {
        enterStatement("DO", ctx.getStart().getLine());

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
        exitStatement("DO", ctx.getStop().getLine(), ctx);
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

            parser.removeErrorListeners();
            parser.addErrorListener(new BaseErrorListener() {
                @Override
                public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                                      int line, int charPositionInLine, String msg,
                                      RecognitionException e) {
                    if (plpgsqlLogErrors) {
                        System.err.println("PL/pgSQL Parse Error at line " + (adjustedBaseLineNumber + line)
                                     + ":" + charPositionInLine + " - " + msg);
                    }
                }
            });

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
        enterStatement("SET", ctx.getStart().getLine());
    }

    @Override
    public void exitVariablesetstmt(PostgreSQLParser.VariablesetstmtContext ctx) {
        exitStatement("SET", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterVariableresetstmt(PostgreSQLParser.VariableresetstmtContext ctx) {
        enterStatement("RESET", ctx.getStart().getLine());
    }

    @Override
    public void exitVariableresetstmt(PostgreSQLParser.VariableresetstmtContext ctx) {
        exitStatement("RESET", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterDropstmt(PostgreSQLParser.DropstmtContext ctx) {
        enterStatement("DROP", ctx.getStart().getLine());
    }

    @Override
    public void exitDropstmt(PostgreSQLParser.DropstmtContext ctx) {
        exitStatement("DROP", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterDroprolestmt(PostgreSQLParser.DroprolestmtContext ctx) {
        enterStatement("DROP_ROLE", ctx.getStart().getLine());
    }

    @Override
    public void exitDroprolestmt(PostgreSQLParser.DroprolestmtContext ctx) {
        exitStatement("DROP_ROLE", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterCreatestmt(PostgreSQLParser.CreatestmtContext ctx) {
        enterStatement("CREATE_TABLE", ctx.getStart().getLine());
    }

    @Override
    public void exitCreatestmt(PostgreSQLParser.CreatestmtContext ctx) {
        exitStatement("CREATE_TABLE", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterAltertablestmt(PostgreSQLParser.AltertablestmtContext ctx) {
        enterStatement("ALTER_TABLE", ctx.getStart().getLine());
    }

    @Override
    public void exitAltertablestmt(PostgreSQLParser.AltertablestmtContext ctx) {
        exitStatement("ALTER_TABLE", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterAlterrolestmt(PostgreSQLParser.AlterrolestmtContext ctx) {
        enterStatement("ALTER_ROLE", ctx.getStart().getLine());
    }

    @Override
    public void exitAlterrolestmt(PostgreSQLParser.AlterrolestmtContext ctx) {
        exitStatement("ALTER_ROLE", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterAlterdatabasestmt(PostgreSQLParser.AlterdatabasestmtContext ctx) {
        enterStatement("ALTER_DATABASE", ctx.getStart().getLine());
    }

    @Override
    public void exitAlterdatabasestmt(PostgreSQLParser.AlterdatabasestmtContext ctx) {
        exitStatement("ALTER_DATABASE", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterAlterfunctionstmt(PostgreSQLParser.AlterfunctionstmtContext ctx) {
        enterStatement("ALTER_FUNCTION", ctx.getStart().getLine());
    }

    @Override
    public void exitAlterfunctionstmt(PostgreSQLParser.AlterfunctionstmtContext ctx) {
        exitStatement("ALTER_FUNCTION", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterIndexstmt(PostgreSQLParser.IndexstmtContext ctx) {
        enterStatement("CREATE_INDEX", ctx.getStart().getLine());
    }

    @Override
    public void exitIndexstmt(PostgreSQLParser.IndexstmtContext ctx) {
        exitStatement("CREATE_INDEX", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterCreateopfamilystmt(PostgreSQLParser.CreateopfamilystmtContext ctx) {
        enterStatement("CREATE_OPERATOR_FAMILY", ctx.getStart().getLine());
    }

    @Override
    public void exitCreateopfamilystmt(PostgreSQLParser.CreateopfamilystmtContext ctx) {
        exitStatement("CREATE_OPERATOR_FAMILY", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterAlteropfamilystmt(PostgreSQLParser.AlteropfamilystmtContext ctx) {
        enterStatement("ALTER_OPERATOR_FAMILY", ctx.getStart().getLine());
    }

    @Override
    public void exitAlteropfamilystmt(PostgreSQLParser.AlteropfamilystmtContext ctx) {
        exitStatement("ALTER_OPERATOR_FAMILY", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterDropopfamilystmt(PostgreSQLParser.DropopfamilystmtContext ctx) {
        enterStatement("DROP_OPERATOR_FAMILY", ctx.getStart().getLine());
    }

    @Override
    public void exitDropopfamilystmt(PostgreSQLParser.DropopfamilystmtContext ctx) {
        exitStatement("DROP_OPERATOR_FAMILY", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterCreateopclassstmt(PostgreSQLParser.CreateopclassstmtContext ctx) {
        enterStatement("CREATE_OPERATOR_CLASS", ctx.getStart().getLine());
    }

    @Override
    public void exitCreateopclassstmt(PostgreSQLParser.CreateopclassstmtContext ctx) {
        exitStatement("CREATE_OPERATOR_CLASS", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterDropopclassstmt(PostgreSQLParser.DropopclassstmtContext ctx) {
        enterStatement("DROP_OPERATOR_CLASS", ctx.getStart().getLine());
    }

    @Override
    public void exitDropopclassstmt(PostgreSQLParser.DropopclassstmtContext ctx) {
        exitStatement("DROP_OPERATOR_CLASS", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterCreateschemastmt(PostgreSQLParser.CreateschemastmtContext ctx) {
        enterStatement("CREATE_SCHEMA", ctx.getStart().getLine());
    }

    @Override
    public void exitCreateschemastmt(PostgreSQLParser.CreateschemastmtContext ctx) {
        exitStatement("CREATE_SCHEMA", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterCreateseqstmt(PostgreSQLParser.CreateseqstmtContext ctx) {
        enterStatement("CREATE_SEQUENCE", ctx.getStart().getLine());
    }

    @Override
    public void exitCreateseqstmt(PostgreSQLParser.CreateseqstmtContext ctx) {
        exitStatement("CREATE_SEQUENCE", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterCreatetrigstmt(PostgreSQLParser.CreatetrigstmtContext ctx) {
        enterStatement("CREATE_TRIGGER", ctx.getStart().getLine());
    }

    @Override
    public void exitCreatetrigstmt(PostgreSQLParser.CreatetrigstmtContext ctx) {
        exitStatement("CREATE_TRIGGER", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterRulestmt(PostgreSQLParser.RulestmtContext ctx) {
        enterStatement("CREATE_RULE", ctx.getStart().getLine());
    }

    @Override
    public void exitRulestmt(PostgreSQLParser.RulestmtContext ctx) {
        exitStatement("CREATE_RULE", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterCreatedbstmt(PostgreSQLParser.CreatedbstmtContext ctx) {
        enterStatement("CREATE_DATABASE", ctx.getStart().getLine());
    }

    @Override
    public void exitCreatedbstmt(PostgreSQLParser.CreatedbstmtContext ctx) {
        exitStatement("CREATE_DATABASE", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterCreateuserstmt(PostgreSQLParser.CreateuserstmtContext ctx) {
        enterStatement("CREATE_USER", ctx.getStart().getLine());
    }

    @Override
    public void exitCreateuserstmt(PostgreSQLParser.CreateuserstmtContext ctx) {
        exitStatement("CREATE_USER", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterCreaterolestmt(PostgreSQLParser.CreaterolestmtContext ctx) {
        enterStatement("CREATE_ROLE", ctx.getStart().getLine());
    }

    @Override
    public void exitCreaterolestmt(PostgreSQLParser.CreaterolestmtContext ctx) {
        exitStatement("CREATE_ROLE", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterCreategroupstmt(PostgreSQLParser.CreategroupstmtContext ctx) {
        enterStatement("CREATE_GROUP", ctx.getStart().getLine());
    }

    @Override
    public void exitCreategroupstmt(PostgreSQLParser.CreategroupstmtContext ctx) {
        exitStatement("CREATE_GROUP", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterViewstmt(PostgreSQLParser.ViewstmtContext ctx) {
        enterStatement("CREATE_VIEW", ctx.getStart().getLine());
    }

    @Override
    public void exitViewstmt(PostgreSQLParser.ViewstmtContext ctx) {
        exitStatement("CREATE_VIEW", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterTruncatestmt(PostgreSQLParser.TruncatestmtContext ctx) {
        enterStatement("TRUNCATE", ctx.getStart().getLine());
    }

    @Override
    public void exitTruncatestmt(PostgreSQLParser.TruncatestmtContext ctx) {
        exitStatement("TRUNCATE", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterCopystmt(PostgreSQLParser.CopystmtContext ctx) {
        enterStatement("COPY", ctx.getStart().getLine());
    }

    @Override
    public void exitCopystmt(PostgreSQLParser.CopystmtContext ctx) {
        exitStatement("COPY", ctx.getStop().getLine(), ctx);
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

        enterStatement(defineType, ctx.getStart().getLine());
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

        exitStatement(defineType, ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterRenamestmt(PostgreSQLParser.RenamestmtContext ctx) {
        enterStatement("RENAME", ctx.getStart().getLine());
    }

    @Override
    public void exitRenamestmt(PostgreSQLParser.RenamestmtContext ctx) {
        exitStatement("RENAME", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterAlterownerstmt(PostgreSQLParser.AlterownerstmtContext ctx) {
        enterStatement("ALTER_OWNER", ctx.getStart().getLine());
    }

    @Override
    public void exitAlterownerstmt(PostgreSQLParser.AlterownerstmtContext ctx) {
        exitStatement("ALTER_OWNER", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterAlterobjectschemastmt(PostgreSQLParser.AlterobjectschemastmtContext ctx) {
        enterStatement("ALTER_SCHEMA", ctx.getStart().getLine());
    }

    @Override
    public void exitAlterobjectschemastmt(PostgreSQLParser.AlterobjectschemastmtContext ctx) {
        exitStatement("ALTER_SCHEMA", ctx.getStop().getLine(), ctx);
    }

    // ========================================
    // DML
    // ========================================

    @Override
    public void enterSelectstmt(PostgreSQLParser.SelectstmtContext ctx) {
        if (insideInsert || insideExplain) return;
        enterStatement("SELECT", ctx.getStart().getLine());
    }

    @Override
    public void exitSelectstmt(PostgreSQLParser.SelectstmtContext ctx) {
        if (insideInsert || insideExplain) return;
        exitStatement("SELECT", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterInsertstmt(PostgreSQLParser.InsertstmtContext ctx) {
        insideInsert = true;
        enterStatement("INSERT", ctx.getStart().getLine());
    }

    @Override
    public void exitInsertstmt(PostgreSQLParser.InsertstmtContext ctx) {
        exitStatement("INSERT", ctx.getStop().getLine(), ctx);
        insideInsert = false;
    }

    @Override
    public void enterUpdatestmt(PostgreSQLParser.UpdatestmtContext ctx) {
        enterStatement("UPDATE", ctx.getStart().getLine());
    }

    @Override
    public void exitUpdatestmt(PostgreSQLParser.UpdatestmtContext ctx) {
        exitStatement("UPDATE", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterDeletestmt(PostgreSQLParser.DeletestmtContext ctx) {
        enterStatement("DELETE", ctx.getStart().getLine());
    }

    @Override
    public void exitDeletestmt(PostgreSQLParser.DeletestmtContext ctx) {
        exitStatement("DELETE", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterMergestmt(PostgreSQLParser.MergestmtContext ctx) {
        enterStatement("MERGE", ctx.getStart().getLine());
    }

    @Override
    public void exitMergestmt(PostgreSQLParser.MergestmtContext ctx) {
        exitStatement("MERGE", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterMerge_insert_clause(PostgreSQLParser.Merge_insert_clauseContext ctx) {
        enterStatement("MERGE_INSERT", ctx.getStart().getLine());
    }

    @Override
    public void exitMerge_insert_clause(PostgreSQLParser.Merge_insert_clauseContext ctx) {
        exitStatement("MERGE_INSERT", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterMerge_update_clause(PostgreSQLParser.Merge_update_clauseContext ctx) {
        enterStatement("MERGE_UPDATE", ctx.getStart().getLine());
    }

    @Override
    public void exitMerge_update_clause(PostgreSQLParser.Merge_update_clauseContext ctx) {
        exitStatement("MERGE_UPDATE", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterMerge_delete_clause(PostgreSQLParser.Merge_delete_clauseContext ctx) {
        enterStatement("MERGE_DELETE", ctx.getStart().getLine());
    }

    @Override
    public void exitMerge_delete_clause(PostgreSQLParser.Merge_delete_clauseContext ctx) {
        exitStatement("MERGE_DELETE", ctx.getStop().getLine(), ctx);
    }

    // ========================================
    // DCL
    // ========================================

    @Override
    public void enterGrantstmt(PostgreSQLParser.GrantstmtContext ctx) {
        enterStatement("GRANT", ctx.getStart().getLine());
    }

    @Override
    public void exitGrantstmt(PostgreSQLParser.GrantstmtContext ctx) {
        exitStatement("GRANT", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterRevokestmt(PostgreSQLParser.RevokestmtContext ctx) {
        enterStatement("REVOKE", ctx.getStart().getLine());
    }

    @Override
    public void exitRevokestmt(PostgreSQLParser.RevokestmtContext ctx) {
        exitStatement("REVOKE", ctx.getStop().getLine(), ctx);
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

        enterStatement(transactionType, ctx.getStart().getLine());
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

        exitStatement(transactionType, ctx.getStop().getLine(), ctx);
    }

    // ========================================
    // Utility
    // ========================================

    @Override
    public void enterAnalyzestmt(PostgreSQLParser.AnalyzestmtContext ctx) {
        enterStatement("ANALYZE", ctx.getStart().getLine());
    }

    @Override
    public void exitAnalyzestmt(PostgreSQLParser.AnalyzestmtContext ctx) {
        exitStatement("ANALYZE", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterVacuumstmt(PostgreSQLParser.VacuumstmtContext ctx) {
        enterStatement("VACUUM", ctx.getStart().getLine());
    }

    @Override
    public void exitVacuumstmt(PostgreSQLParser.VacuumstmtContext ctx) {
        exitStatement("VACUUM", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterExplainstmt(PostgreSQLParser.ExplainstmtContext ctx) {
        insideExplain = true;
        enterStatement("EXPLAIN", ctx.getStart().getLine());
    }

    @Override
    public void exitExplainstmt(PostgreSQLParser.ExplainstmtContext ctx) {
        exitStatement("EXPLAIN", ctx.getStop().getLine(), ctx);
        insideExplain = false;
    }

    @Override
    public void enterPreparestmt(PostgreSQLParser.PreparestmtContext ctx) {
        enterStatement("PREPARE", ctx.getStart().getLine());
    }

    @Override
    public void exitPreparestmt(PostgreSQLParser.PreparestmtContext ctx) {
        exitStatement("PREPARE", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterExecutestmt(PostgreSQLParser.ExecutestmtContext ctx) {
        enterStatement("EXECUTE", ctx.getStart().getLine());
    }

    @Override
    public void exitExecutestmt(PostgreSQLParser.ExecutestmtContext ctx) {
        exitStatement("EXECUTE", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterLockstmt(PostgreSQLParser.LockstmtContext ctx) {
        enterStatement("LOCK", ctx.getStart().getLine());
    }

    @Override
    public void exitLockstmt(PostgreSQLParser.LockstmtContext ctx) {
        exitStatement("LOCK", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterReindexstmt(PostgreSQLParser.ReindexstmtContext ctx) {
        enterStatement("REINDEX", ctx.getStart().getLine());
    }

    @Override
    public void exitReindexstmt(PostgreSQLParser.ReindexstmtContext ctx) {
        exitStatement("REINDEX", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterClusterstmt(PostgreSQLParser.ClusterstmtContext ctx) {
        enterStatement("CLUSTER", ctx.getStart().getLine());
    }

    @Override
    public void exitClusterstmt(PostgreSQLParser.ClusterstmtContext ctx) {
        exitStatement("CLUSTER", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterCommentstmt(PostgreSQLParser.CommentstmtContext ctx) {
        enterStatement("COMMENT", ctx.getStart().getLine());
    }

    @Override
    public void exitCommentstmt(PostgreSQLParser.CommentstmtContext ctx) {
        exitStatement("COMMENT", ctx.getStop().getLine(), ctx);
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
