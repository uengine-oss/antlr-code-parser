package legacymodernizer.parser.antlr.plsql;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;

import legacymodernizer.parser.parsing.AntlrParseHarness;
import legacymodernizer.parser.model.DatabaseLinkComponent;
import legacymodernizer.parser.model.DataObjectReference;
import legacymodernizer.parser.model.Node;
import legacymodernizer.parser.model.QualifiedColumnReference;
import legacymodernizer.parser.model.UnqualifiedIdentifierReference;
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
        Node node = h.enterStatement("ASSIGNMENT", ctx.getStart().getLine());
        // 좌변·연산자·우변 원문을 명시 필드로 보존 — downstream 이 소스를 재파싱하지
        // 않는다(spec 016 FR-003). 식별자 의미 해석은 계속 Analyzer 책임이다.
        node.target = ParserUtils.getExactSourceText(
                ctx.general_element() != null ? ctx.general_element() : ctx.bind_variable());
        node.operator = ctx.ASSIGN_OP() != null ? ctx.ASSIGN_OP().getText() : null;
        node.expression = ParserUtils.getExactSourceText(ctx.expression());
    }

    @Override
    public void exitAssignment_statement(PlSqlParser.Assignment_statementContext ctx) {
        h.exitStatementWithChildDedupe("ASSIGNMENT", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterReturn_statement(PlSqlParser.Return_statementContext ctx) {
        Node node = h.enterStatement("RETURN", ctx.getStart().getLine());
        // 반환식 원문을 expression 필드로 보존 — downstream 이 소스를 재파싱하지
        // 않는다(spec 016 FR-003). 값 없는 RETURN 은 expression null (FR-004).
        node.expression = ParserUtils.getExactSourceText(ctx.expression());
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
        Node node = h.enterStatement("SELECT", ctx.getStart().getLine());
        List<DataObjectReference> references = new ArrayList<>();
        for (PlSqlParser.Table_ref_auxContext tableRef : descendants(ctx, PlSqlParser.Table_ref_auxContext.class)) {
            if (nearestAncestor(tableRef, PlSqlParser.Query_blockContext.class) != ctx) continue;
            PlSqlParser.Tableview_nameContext table = physicalTable(tableRef);
            if (table == null) continue;
            addObject(references, objectReference(table, tableRef.table_alias(), "READ"));
        }
        attachEvidence(
                node, references, qualifiedColumns(ctx, visibleQualifiers(ctx), true),
                unqualifiedIdentifiers(ctx, true));
    }

    @Override
    public void exitQuery_block(PlSqlParser.Query_blockContext ctx) {
        h.exitStatementWithChildDedupe("SELECT", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterInsert_statement(PlSqlParser.Insert_statementContext ctx) {
        Node node = h.enterStatement("INSERT", ctx.getStart().getLine());
        List<DataObjectReference> references = new ArrayList<>();
        for (PlSqlParser.Insert_into_clauseContext into : descendants(ctx, PlSqlParser.Insert_into_clauseContext.class)) {
            if (nearestAncestor(into, PlSqlParser.Insert_statementContext.class) != ctx) continue;
            addObject(references, objectReference(into.general_table_ref(), "WRITE"));
        }
        attachEvidence(
                node, references, qualifiedColumns(ctx, qualifiers(references), false),
                unqualifiedIdentifiers(ctx, false));
    }

    @Override
    public void exitInsert_statement(PlSqlParser.Insert_statementContext ctx) {
        h.exitStatementWithChildDedupe("INSERT", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterUpdate_statement(PlSqlParser.Update_statementContext ctx) {
        Node node = h.enterStatement("UPDATE", ctx.getStart().getLine());
        List<DataObjectReference> references = new ArrayList<>();
        addObject(references, objectReference(ctx.general_table_ref(), "WRITE"));
        attachEvidence(
                node, references, qualifiedColumns(ctx, qualifiers(references), false),
                unqualifiedIdentifiers(ctx, false));
    }

    @Override
    public void exitUpdate_statement(PlSqlParser.Update_statementContext ctx) {
        h.exitStatementWithChildDedupe("UPDATE", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterDelete_statement(PlSqlParser.Delete_statementContext ctx) {
        Node node = h.enterStatement("DELETE", ctx.getStart().getLine());
        List<DataObjectReference> references = new ArrayList<>();
        addObject(references, objectReference(ctx.general_table_ref(), "WRITE"));
        attachEvidence(
                node, references, qualifiedColumns(ctx, qualifiers(references), false),
                unqualifiedIdentifiers(ctx, false));
    }

    @Override
    public void exitDelete_statement(PlSqlParser.Delete_statementContext ctx) {
        h.exitStatementWithChildDedupe("DELETE", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterMerge_statement(PlSqlParser.Merge_statementContext ctx) {
        Node node = h.enterStatement("MERGE", ctx.getStart().getLine());
        List<DataObjectReference> references = new ArrayList<>();
        addObject(references, objectReference(
                ctx.dml_table_expression_clause(), ctx.table_alias(), "WRITE"));
        if (ctx.selected_tableview() != null && ctx.selected_tableview().tableview_name() != null) {
            addObject(references, objectReference(
                    ctx.selected_tableview().tableview_name(),
                    ctx.selected_tableview().table_alias(),
                    "READ"));
        }
        attachEvidence(
                node, references, qualifiedColumns(ctx, qualifiers(references), false),
                unqualifiedIdentifiers(ctx, false));
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

    private static void attachEvidence(
            Node node,
            List<DataObjectReference> objects,
            List<QualifiedColumnReference> columns,
            List<UnqualifiedIdentifierReference> identifiers) {
        node.dataObjectEvidenceVersion = 3;
        if (!objects.isEmpty()) node.dataObjectReferences = new ArrayList<>(objects);
        if (!columns.isEmpty()) node.qualifiedColumnReferences = new ArrayList<>(columns);
        if (!identifiers.isEmpty()) {
            node.unqualifiedIdentifierReferences = new ArrayList<>(identifiers);
        }
    }

    private static void addObject(List<DataObjectReference> references, DataObjectReference candidate) {
        if (candidate == null) return;
        String key = String.join("\u0000",
                candidate.rawReference == null ? "" : candidate.rawReference,
                candidate.alias == null ? "" : candidate.alias,
                candidate.access == null ? "" : candidate.access,
                Integer.toString(candidate.startLine));
        for (DataObjectReference existing : references) {
            String existingKey = String.join("\u0000",
                    existing.rawReference == null ? "" : existing.rawReference,
                    existing.alias == null ? "" : existing.alias,
                    existing.access == null ? "" : existing.access,
                    Integer.toString(existing.startLine));
            if (existingKey.equals(key)) return;
        }
        references.add(candidate);
    }

    private static DataObjectReference objectReference(
            PlSqlParser.General_table_refContext ctx, String access) {
        if (ctx == null || ctx.dml_table_expression_clause() == null) return null;
        return objectReference(ctx.dml_table_expression_clause(), ctx.table_alias(), access);
    }

    private static DataObjectReference objectReference(
            PlSqlParser.Dml_table_expression_clauseContext dml,
            PlSqlParser.Table_aliasContext alias,
            String access) {
        if (dml == null) return null;
        if (dml.tableview_name() != null) {
            return objectReference(dml.tableview_name(), alias, access);
        }
        if (dml.select_statement() == null) return null;

        // Oracle permits an updatable inline view as a DML target. The physical write target is
        // syntax-decidable only when the outer target query block contains one physical object.
        // A join needs key-preservation/schema semantics, so fail closed instead of marking every
        // table as written.
        List<PlSqlParser.Query_blockContext> blocks =
                descendants(dml.select_statement(), PlSqlParser.Query_blockContext.class);
        if (blocks.isEmpty()) return null;
        PlSqlParser.Query_blockContext outer = blocks.get(0);
        List<DataObjectReference> physical = new ArrayList<>();
        for (PlSqlParser.Table_ref_auxContext tableRef
                : descendants(outer, PlSqlParser.Table_ref_auxContext.class)) {
            if (nearestAncestor(tableRef, PlSqlParser.Query_blockContext.class) != outer) continue;
            PlSqlParser.Tableview_nameContext table = physicalTable(tableRef);
            if (table != null) addObject(physical, objectReference(table, null, access));
        }
        if (physical.size() != 1) return null;
        DataObjectReference reference = physical.get(0);
        if (alias != null) reference.alias = alias.getText();
        return reference;
    }

    private static DataObjectReference objectReference(
            PlSqlParser.Tableview_nameContext table,
            PlSqlParser.Table_aliasContext alias,
            String access) {
        if (table == null || table.identifier() == null) return null;
        DataObjectReference reference = new DataObjectReference();
        reference.rawReference = ParserUtils.getExactSourceText(table);
        if (table.id_expression() == null) {
            reference.name = table.identifier().getText();
        } else {
            reference.schema = table.identifier().getText();
            reference.name = table.id_expression().getText();
        }
        if (isQuoted(reference.schema)) reference.schemaQuoted = true;
        if (isQuoted(reference.name)) reference.nameQuoted = true;
        if (table.link_name() != null) {
            reference.databaseLink = table.link_name().getText();
            reference.databaseLinkComponents = databaseLinkComponents(table.link_name());
        }
        if (alias != null) reference.alias = alias.getText();
        reference.access = access;
        reference.startLine = table.getStart().getLine();
        return reference;
    }

    private static ArrayList<DatabaseLinkComponent> databaseLinkComponents(
            PlSqlParser.Link_nameContext link) {
        ArrayList<DatabaseLinkComponent> components = new ArrayList<>();
        components.add(databaseLinkComponent("database", link.database().getText()));
        for (PlSqlParser.DomainContext domain : link.domain()) {
            components.add(databaseLinkComponent("domain", domain.getText()));
        }
        if (link.connection_qualifier() != null) {
            components.add(databaseLinkComponent(
                    "connection_qualifier", link.connection_qualifier().getText()));
        }
        return components;
    }

    private static DatabaseLinkComponent databaseLinkComponent(String role, String name) {
        DatabaseLinkComponent component = new DatabaseLinkComponent();
        component.role = role;
        component.name = name;
        if (isQuoted(name)) component.nameQuoted = true;
        return component;
    }

    private static PlSqlParser.Tableview_nameContext physicalTable(
            PlSqlParser.Table_ref_auxContext owner) {
        for (PlSqlParser.Dml_table_expression_clauseContext candidate
                : descendants(owner, PlSqlParser.Dml_table_expression_clauseContext.class)) {
            if (nearestAncestor(candidate, PlSqlParser.Table_ref_auxContext.class) != owner) continue;
            if (candidate.tableview_name() != null) return candidate.tableview_name();
        }
        return null;
    }

    /** Visible physical aliases include the current query and correlated outer query scopes. */
    private static Set<String> visibleQualifiers(PlSqlParser.Query_blockContext ctx) {
        Set<String> result = new HashSet<>();
        for (PlSqlParser.Query_blockContext scope = ctx; scope != null;
                scope = nearestAncestor(scope, PlSqlParser.Query_blockContext.class)) {
            List<DataObjectReference> references = new ArrayList<>();
            for (PlSqlParser.Table_ref_auxContext tableRef
                    : descendants(scope, PlSqlParser.Table_ref_auxContext.class)) {
                if (nearestAncestor(tableRef, PlSqlParser.Query_blockContext.class) != scope) continue;
                PlSqlParser.Tableview_nameContext table = physicalTable(tableRef);
                if (table != null) addObject(references, objectReference(table, tableRef.table_alias(), "READ"));
            }
            result.addAll(qualifiers(references));
        }
        // A SELECT nested in UPDATE/DELETE/MERGE can contain a correlated reference to the
        // enclosing DML target even though that target is not owned by a Query_block. Preserve
        // the explicit qualifier on the nested SELECT; the Analyzer will leave it unbound to
        // the local SELECT objects instead of guessing ownership.
        for (ParseTree current = ctx.getParent(); current != null; current = current.getParent()) {
            List<DataObjectReference> references = new ArrayList<>();
            if (current instanceof PlSqlParser.Update_statementContext) {
                PlSqlParser.Update_statementContext update =
                        (PlSqlParser.Update_statementContext) current;
                addObject(references, objectReference(update.general_table_ref(), "WRITE"));
            } else if (current instanceof PlSqlParser.Delete_statementContext) {
                PlSqlParser.Delete_statementContext delete =
                        (PlSqlParser.Delete_statementContext) current;
                addObject(references, objectReference(delete.general_table_ref(), "WRITE"));
            } else if (current instanceof PlSqlParser.Merge_statementContext) {
                PlSqlParser.Merge_statementContext merge =
                        (PlSqlParser.Merge_statementContext) current;
                addObject(references, objectReference(
                        merge.dml_table_expression_clause(), merge.table_alias(), "WRITE"));
            }
            result.addAll(qualifiers(references));
        }
        return result;
    }

    private static Set<String> qualifiers(List<DataObjectReference> references) {
        Set<String> result = new HashSet<>();
        for (DataObjectReference reference : references) {
            if (reference.alias != null) result.add(canonical(reference.alias));
            if (reference.name != null) result.add(canonical(reference.name));
        }
        return result;
    }

    private static List<QualifiedColumnReference> qualifiedColumns(
            ParserRuleContext owner, Set<String> visible, boolean queryOwned) {
        List<QualifiedColumnReference> result = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();
        for (PlSqlParser.General_elementContext element
                : descendants(owner, PlSqlParser.General_elementContext.class)) {
            PlSqlParser.Query_blockContext nearestQuery = nearestAncestor(
                    element, PlSqlParser.Query_blockContext.class);
            if (queryOwned) {
                if (nearestQuery != owner) continue;
            } else if (nearestQuery != null) {
                continue;
            }
            List<PlSqlParser.General_element_partContext> parts = flattenedParts(element);
            if (parts.size() < 2) continue;
            PlSqlParser.General_element_partContext last = parts.get(parts.size() - 1);
            if (last.id_expression() == null || !last.function_argument().isEmpty()) continue;
            List<String> names = new ArrayList<>();
            boolean usable = true;
            for (PlSqlParser.General_element_partContext part : parts) {
                if (part.id_expression() == null) {
                    usable = false;
                    break;
                }
                names.add(part.id_expression().getText());
            }
            if (!usable) continue;
            String qualifier = String.join(".", names.subList(0, names.size() - 1));
            String localQualifier = names.get(names.size() - 2);
            if (!visible.contains(canonical(qualifier))
                    && !visible.contains(canonical(localQualifier))) continue;
            String raw = ParserUtils.getExactSourceText(element);
            String key = raw + "\u0000" + element.getStart().getLine();
            if (!seen.add(key)) continue;
            QualifiedColumnReference reference = new QualifiedColumnReference();
            reference.rawReference = raw;
            reference.qualifier = qualifier;
            reference.name = names.get(names.size() - 1);
            if (isQuoted(reference.name)) reference.nameQuoted = true;
            reference.startLine = element.getStart().getLine();
            result.add(reference);
        }
        return result;
    }

    private static List<UnqualifiedIdentifierReference> unqualifiedIdentifiers(
            ParserRuleContext owner, boolean queryOwned) {
        List<ParserRuleContext> candidates = new ArrayList<>();
        candidates.addAll(descendants(owner, PlSqlParser.General_elementContext.class));
        candidates.addAll(descendants(owner, PlSqlParser.Column_nameContext.class));
        candidates.sort(Comparator.comparingInt(ctx -> ctx.getStart().getTokenIndex()));

        List<UnqualifiedIdentifierReference> result = new ArrayList<>();
        Set<Integer> seenStarts = new LinkedHashSet<>();
        for (ParserRuleContext candidate : candidates) {
            if (!ownedBy(candidate, owner, queryOwned)) continue;
            String name;
            if (candidate instanceof PlSqlParser.General_elementContext) {
                PlSqlParser.General_elementContext element =
                        (PlSqlParser.General_elementContext) candidate;
                // ``A.B`` recursively contains the prefix ``A`` as a direct child. It is part
                // of the qualified reference, not a second unqualified occurrence. A value
                // inside a function argument is separated by expression contexts and remains.
                if (element.getParent() instanceof PlSqlParser.General_elementContext) continue;
                List<PlSqlParser.General_element_partContext> parts = flattenedParts(element);
                if (parts.size() != 1) continue;
                PlSqlParser.General_element_partContext part = parts.get(0);
                if (part.id_expression() == null || !part.function_argument().isEmpty()) continue;
                name = part.id_expression().getText();
            } else {
                PlSqlParser.Column_nameContext column =
                        (PlSqlParser.Column_nameContext) candidate;
                if (column.identifier() == null || !column.id_expression().isEmpty()) continue;
                name = column.identifier().getText();
            }
            int start = candidate.getStart().getTokenIndex();
            if (!seenStarts.add(start)) continue;
            UnqualifiedIdentifierReference reference = new UnqualifiedIdentifierReference();
            reference.rawReference = ParserUtils.getExactSourceText(candidate);
            reference.name = name;
            if (isQuoted(name)) reference.nameQuoted = true;
            reference.startLine = candidate.getStart().getLine();
            result.add(reference);
        }
        return result;
    }

    private static boolean ownedBy(
            ParserRuleContext candidate, ParserRuleContext owner, boolean queryOwned) {
        PlSqlParser.Query_blockContext nearestQuery = nearestAncestor(
                candidate, PlSqlParser.Query_blockContext.class);
        return queryOwned ? nearestQuery == owner : nearestQuery == null;
    }

    private static List<PlSqlParser.General_element_partContext> flattenedParts(
            PlSqlParser.General_elementContext ctx) {
        List<PlSqlParser.General_element_partContext> result = new ArrayList<>();
        if (ctx.general_element() != null) result.addAll(flattenedParts(ctx.general_element()));
        result.addAll(ctx.general_element_part());
        return result;
    }

    @Override
    public void enterGeneral_element_part(PlSqlParser.General_element_partContext ctx) {
        if (ctx.link_name() == null || ctx.function_argument().isEmpty()) return;
        h.enterStatement("CALL", remoteRoutineName(ctx), ctx.getStart().getLine());
    }

    @Override
    public void exitGeneral_element_part(PlSqlParser.General_element_partContext ctx) {
        if (ctx.link_name() == null || ctx.function_argument().isEmpty()) return;
        h.exitStatementWithChildDedupe("CALL", ctx.getStop().getLine(), ctx);
    }

    private static String remoteRoutineName(PlSqlParser.General_element_partContext ctx) {
        PlSqlParser.General_elementContext element = nearestAncestor(
                ctx, PlSqlParser.General_elementContext.class);
        List<String> names = new ArrayList<>();
        if (element != null) {
            for (PlSqlParser.General_element_partContext part : flattenedParts(element)) {
                if (part.id_expression() != null) names.add(part.id_expression().getText());
                if (part == ctx) break;
            }
        }
        if (names.isEmpty() && ctx.id_expression() != null) {
            names.add(ctx.id_expression().getText());
        }
        return String.join(".", names) + "@" + ctx.link_name().getText();
    }

    private static String canonical(String value) {
        if (value == null) return "";
        String trimmed = value.trim();
        if (trimmed.length() >= 2 && trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
            return "Q:" + trimmed.substring(1, trimmed.length() - 1).replace("\"\"", "\"");
        }
        return "U:" + trimmed.toUpperCase(Locale.ROOT);
    }

    private static boolean isQuoted(String value) {
        String trimmed = value == null ? "" : value.trim();
        return trimmed.length() >= 2 && trimmed.startsWith("\"") && trimmed.endsWith("\"");
    }

    private static <T extends ParserRuleContext> T nearestAncestor(
            ParseTree node, Class<T> type) {
        for (ParseTree current = node.getParent(); current != null; current = current.getParent()) {
            if (type.isInstance(current)) return type.cast(current);
        }
        return null;
    }

    private static <T extends ParserRuleContext> List<T> descendants(ParseTree root, Class<T> type) {
        List<T> result = new ArrayList<>();
        collectDescendants(root, type, result);
        return result;
    }

    private static <T extends ParserRuleContext> void collectDescendants(
            ParseTree root, Class<T> type, List<T> result) {
        for (int i = 0; i < root.getChildCount(); i++) {
            ParseTree child = root.getChild(i);
            if (type.isInstance(child)) result.add(type.cast(child));
            collectDescendants(child, type, result);
        }
    }

    // ========================================
    // 제어 흐름: IF/ELSIF/ELSE/LOOP
    // ========================================

    @Override
    public void enterIf_statement(PlSqlParser.If_statementContext ctx) {
        // 조건식 원문 보존 (spec 016 FR-003)
        h.enterStatement("IF", ctx.getStart().getLine())
                .expression = ParserUtils.getExactSourceText(ctx.condition());
        // condition 텍스트는 노드 원문에 포함 — 플래그/식별자는 Analyzer 책임
    }

    @Override
    public void exitIf_statement(PlSqlParser.If_statementContext ctx) {
        h.exitStatementWithChildDedupe("IF", ctx.getStop().getLine(), ctx);
    }

    @Override
    public void enterElsif_part(PlSqlParser.Elsif_partContext ctx) {
        // 조건식 원문 보존 (spec 016 FR-003)
        h.enterStatement("ELSIF", ctx.getStart().getLine())
                .expression = ParserUtils.getExactSourceText(ctx.condition());
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
        // WHILE 판정절만 조건으로 보존 — FOR cursor 파라미터는 도달 기계장치다(TA-102).
        h.enterStatement("LOOP", ctx.getStart().getLine())
                .expression = ParserUtils.getExactSourceText(ctx.condition());
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
        recoverImplicitTableFunctionReturns(ctx);
    }

    /**
     * Recover a valueless table-function {@code RETURN;} swallowed as a SELECT alias.
     *
     * <p>Some legacy Oracle-compatible sources terminate a {@code RETURN TABLE} query as
     * {@code SELECT ... FROM object RETURN;} without a separate semicolon before
     * {@code RETURN}. The permissive grammar accepts RETURN as a non-reserved table alias,
     * so the query consumes the token and no executable RETURN node is emitted. Recovery is
     * limited to an active table function, an exact {@code RETURN ;} token pair inside its
     * body, and a missing RETURN node at that physical line.
     */
    private void recoverImplicitTableFunctionReturns(PlSqlParser.BodyContext ctx) {
        Node function = activeTableFunction();
        if (function == null) return;

        List<Token> tokens = h.getTokens().getTokens();
        int start = ctx.getStart().getTokenIndex();
        int stop = ctx.getStop().getTokenIndex();
        for (int index = start; index <= stop && index < tokens.size(); index++) {
            Token token = tokens.get(index);
            if (token.getType() != PlSqlLexer.RETURN) continue;
            Token next = nextDefaultToken(tokens, index + 1, stop);
            if (next == null || !";".equals(next.getText())) continue;
            int line = token.getLine();
            if (containsNodeAtLine(function, "RETURN", line)) continue;

            Token previous = previousDefaultToken(tokens, index - 1, start);
            int queryEndLine = previous == null ? line : previous.getLine();
            trimAbsorbedReturn(function, line, queryEndLine);
            Node recovered = new Node("RETURN", null, line, function);
            recovered.endLine = line;
        }
    }

    private Node activeTableFunction() {
        for (int index = h.getNodeStack().size() - 1; index >= 0; index--) {
            Node candidate = h.getNodeStack().get(index);
            if ("FUNCTION".equals(candidate.type)) {
                return "TABLE".equalsIgnoreCase(candidate.returnType) ? candidate : null;
            }
        }
        return null;
    }

    private static Token nextDefaultToken(List<Token> tokens, int start, int stop) {
        for (int index = start; index <= stop && index < tokens.size(); index++) {
            Token token = tokens.get(index);
            if (token.getChannel() == Token.DEFAULT_CHANNEL) return token;
        }
        return null;
    }

    private static Token previousDefaultToken(List<Token> tokens, int start, int stop) {
        for (int index = start; index >= stop && index >= 0; index--) {
            Token token = tokens.get(index);
            if (token.getChannel() == Token.DEFAULT_CHANNEL) return token;
        }
        return null;
    }

    private static boolean containsNodeAtLine(Node node, String type, int line) {
        if (type.equals(node.type) && node.startLine == line) return true;
        for (Node child : node.children) {
            if (containsNodeAtLine(child, type, line)) return true;
        }
        return false;
    }

    private static void trimAbsorbedReturn(Node node, int returnLine, int queryEndLine) {
        if (node.startLine < returnLine && node.endLine >= returnLine
                && isQueryNode(node.type)) {
            node.endLine = Math.max(node.startLine, queryEndLine);
            if (node.dataObjectReferences != null) {
                for (DataObjectReference reference : node.dataObjectReferences) {
                    if ("RETURN".equalsIgnoreCase(reference.alias)) reference.alias = null;
                }
            }
        }
        for (Node child : node.children) {
            trimAbsorbedReturn(child, returnLine, queryEndLine);
        }
    }

    private static boolean isQueryNode(String type) {
        return "SELECT".equals(type) || "UNION".equals(type)
                || "UNION_ALL".equals(type) || "INTERSECT".equals(type)
                || "MINUS".equals(type) || "SET_OPERATION".equals(type);
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
        // 조건식 원문 보존 (spec 016 FR-003) — `EXIT WHEN <조건>` 의 조건은 커서 루프의
        // 실제 탈출 판정이다. 조건 없는 `LOOP` 은 자기 조건이 없으므로, 이걸 버리면
        // 반복 종료 조건이 AST 어디에도 남지 않는다(실측: rwis EXIT 5건 전부 유실).
        // `enterElsif_part` 와 같은 계약이며, 무조건 `EXIT` 는 빈 값이 사실이다.
        Node node = h.enterStatement("EXIT", ctx.getStart().getLine());
        if (ctx.condition() != null) {
            node.expression = ParserUtils.getExactSourceText(ctx.condition());
        }
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

    @Override
    public void enterRollback_statement(PlSqlParser.Rollback_statementContext ctx) {
        h.enterStatement("ROLLBACK", ctx.getStart().getLine());
    }

    @Override
    public void exitRollback_statement(PlSqlParser.Rollback_statementContext ctx) {
        h.exitStatementWithChildDedupe("ROLLBACK", ctx.getStop().getLine(), ctx);
    }

}
