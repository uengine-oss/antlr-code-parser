package legacymodernizer.parser.antlr.postgresql;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link PlpgsqlParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface PlpgsqlParserVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link PlpgsqlParser#plpgsqlBlock}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPlpgsqlBlock(PlpgsqlParser.PlpgsqlBlockContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlpgsqlParser#label}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLabel(PlpgsqlParser.LabelContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlpgsqlParser#declareSection}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDeclareSection(PlpgsqlParser.DeclareSectionContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlpgsqlParser#declarationList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDeclarationList(PlpgsqlParser.DeclarationListContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlpgsqlParser#declaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDeclaration(PlpgsqlParser.DeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlpgsqlParser#dataType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDataType(PlpgsqlParser.DataTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlpgsqlParser#statementList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatementList(PlpgsqlParser.StatementListContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlpgsqlParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatement(PlpgsqlParser.StatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlpgsqlParser#assignmentStmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignmentStmt(PlpgsqlParser.AssignmentStmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlpgsqlParser#variableRef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariableRef(PlpgsqlParser.VariableRefContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlpgsqlParser#selectIntoStmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelectIntoStmt(PlpgsqlParser.SelectIntoStmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlpgsqlParser#selectList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelectList(PlpgsqlParser.SelectListContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlpgsqlParser#selectItem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelectItem(PlpgsqlParser.SelectItemContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlpgsqlParser#variableList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariableList(PlpgsqlParser.VariableListContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlpgsqlParser#fromClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFromClause(PlpgsqlParser.FromClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlpgsqlParser#tableRef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableRef(PlpgsqlParser.TableRefContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlpgsqlParser#joinClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJoinClause(PlpgsqlParser.JoinClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlpgsqlParser#joinType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJoinType(PlpgsqlParser.JoinTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlpgsqlParser#whereClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhereClause(PlpgsqlParser.WhereClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlpgsqlParser#performStmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPerformStmt(PlpgsqlParser.PerformStmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlpgsqlParser#executeStmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExecuteStmt(PlpgsqlParser.ExecuteStmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlpgsqlParser#insertStmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInsertStmt(PlpgsqlParser.InsertStmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlpgsqlParser#columnList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumnList(PlpgsqlParser.ColumnListContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlpgsqlParser#expressionList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpressionList(PlpgsqlParser.ExpressionListContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlpgsqlParser#updateStmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUpdateStmt(PlpgsqlParser.UpdateStmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlpgsqlParser#assignmentList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignmentList(PlpgsqlParser.AssignmentListContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlpgsqlParser#columnAssignment}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumnAssignment(PlpgsqlParser.ColumnAssignmentContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlpgsqlParser#deleteStmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDeleteStmt(PlpgsqlParser.DeleteStmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlpgsqlParser#setStmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSetStmt(PlpgsqlParser.SetStmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlpgsqlParser#ifStmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIfStmt(PlpgsqlParser.IfStmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlpgsqlParser#caseStmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCaseStmt(PlpgsqlParser.CaseStmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlpgsqlParser#whenClauseList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhenClauseList(PlpgsqlParser.WhenClauseListContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlpgsqlParser#whenClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhenClause(PlpgsqlParser.WhenClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlpgsqlParser#loopStmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLoopStmt(PlpgsqlParser.LoopStmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlpgsqlParser#whileStmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhileStmt(PlpgsqlParser.WhileStmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlpgsqlParser#forStmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitForStmt(PlpgsqlParser.ForStmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlpgsqlParser#forQuerySource}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitForQuerySource(PlpgsqlParser.ForQuerySourceContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlpgsqlParser#foreachStmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitForeachStmt(PlpgsqlParser.ForeachStmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlpgsqlParser#exitStmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExitStmt(PlpgsqlParser.ExitStmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlpgsqlParser#continueStmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitContinueStmt(PlpgsqlParser.ContinueStmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlpgsqlParser#returnStmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReturnStmt(PlpgsqlParser.ReturnStmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlpgsqlParser#raiseStmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRaiseStmt(PlpgsqlParser.RaiseStmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlpgsqlParser#raiseOptionList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRaiseOptionList(PlpgsqlParser.RaiseOptionListContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlpgsqlParser#raiseOption}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRaiseOption(PlpgsqlParser.RaiseOptionContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlpgsqlParser#assertStmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssertStmt(PlpgsqlParser.AssertStmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlpgsqlParser#nullStmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNullStmt(PlpgsqlParser.NullStmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlpgsqlParser#commitStmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCommitStmt(PlpgsqlParser.CommitStmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlpgsqlParser#rollbackStmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRollbackStmt(PlpgsqlParser.RollbackStmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlpgsqlParser#nestedBlock}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNestedBlock(PlpgsqlParser.NestedBlockContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlpgsqlParser#exceptionSection}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExceptionSection(PlpgsqlParser.ExceptionSectionContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlpgsqlParser#exceptionHandlerList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExceptionHandlerList(PlpgsqlParser.ExceptionHandlerListContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlpgsqlParser#exceptionHandler}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExceptionHandler(PlpgsqlParser.ExceptionHandlerContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlpgsqlParser#exceptionConditionList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExceptionConditionList(PlpgsqlParser.ExceptionConditionListContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlpgsqlParser#exceptionCondition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExceptionCondition(PlpgsqlParser.ExceptionConditionContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlpgsqlParser#getDiagnosticsStmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGetDiagnosticsStmt(PlpgsqlParser.GetDiagnosticsStmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlpgsqlParser#diagnosticsItemList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDiagnosticsItemList(PlpgsqlParser.DiagnosticsItemListContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlpgsqlParser#diagnosticsItem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDiagnosticsItem(PlpgsqlParser.DiagnosticsItemContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlpgsqlParser#diagnosticsOption}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDiagnosticsOption(PlpgsqlParser.DiagnosticsOptionContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlpgsqlParser#openCursorStmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpenCursorStmt(PlpgsqlParser.OpenCursorStmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlpgsqlParser#closeCursorStmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCloseCursorStmt(PlpgsqlParser.CloseCursorStmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlpgsqlParser#fetchStmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFetchStmt(PlpgsqlParser.FetchStmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlpgsqlParser#selectStmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelectStmt(PlpgsqlParser.SelectStmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlpgsqlParser#withClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWithClause(PlpgsqlParser.WithClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlpgsqlParser#cteList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCteList(PlpgsqlParser.CteListContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlpgsqlParser#cte}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCte(PlpgsqlParser.CteContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlpgsqlParser#selectQuery}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelectQuery(PlpgsqlParser.SelectQueryContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlpgsqlParser#groupByClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGroupByClause(PlpgsqlParser.GroupByClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlpgsqlParser#havingClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHavingClause(PlpgsqlParser.HavingClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlpgsqlParser#orderByClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOrderByClause(PlpgsqlParser.OrderByClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlpgsqlParser#orderByItem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOrderByItem(PlpgsqlParser.OrderByItemContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlpgsqlParser#limitClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLimitClause(PlpgsqlParser.LimitClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlpgsqlParser#parameterList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParameterList(PlpgsqlParser.ParameterListContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlpgsqlParser#parameter}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParameter(PlpgsqlParser.ParameterContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlpgsqlParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpression(PlpgsqlParser.ExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlpgsqlParser#whenExprClauseList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhenExprClauseList(PlpgsqlParser.WhenExprClauseListContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlpgsqlParser#whenExprClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhenExprClause(PlpgsqlParser.WhenExprClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlpgsqlParser#arrayExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArrayExpression(PlpgsqlParser.ArrayExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlpgsqlParser#specialVariable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSpecialVariable(PlpgsqlParser.SpecialVariableContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlpgsqlParser#functionCall}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionCall(PlpgsqlParser.FunctionCallContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlpgsqlParser#functionCallArgs}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionCallArgs(PlpgsqlParser.FunctionCallArgsContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlpgsqlParser#windowFunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWindowFunction(PlpgsqlParser.WindowFunctionContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlpgsqlParser#windowSpec}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWindowSpec(PlpgsqlParser.WindowSpecContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlpgsqlParser#cteStmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCteStmt(PlpgsqlParser.CteStmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlpgsqlParser#sqlGenericStmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSqlGenericStmt(PlpgsqlParser.SqlGenericStmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlpgsqlParser#literal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLiteral(PlpgsqlParser.LiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlpgsqlParser#createTempTableStmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateTempTableStmt(PlpgsqlParser.CreateTempTableStmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlpgsqlParser#createTableColumnDefList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateTableColumnDefList(PlpgsqlParser.CreateTableColumnDefListContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlpgsqlParser#createTableColumnDef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateTableColumnDef(PlpgsqlParser.CreateTableColumnDefContext ctx);
	/**
	 * Visit a parse tree produced by {@link PlpgsqlParser#onCommitClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOnCommitClause(PlpgsqlParser.OnCommitClauseContext ctx);
}