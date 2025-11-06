package legacymodernizer.parser.antlr.postgresql;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue"})
public class PlpgsqlParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.13.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		DECLARE=1, BEGIN=2, END=3, EXCEPTION=4, IF=5, THEN=6, ELSIF=7, ELSE=8, 
		CASE=9, WHEN=10, LOOP=11, WHILE=12, FOR=13, FOREACH=14, EXIT=15, CONTINUE=16, 
		RETURN=17, RAISE=18, NEXT=19, QUERY=20, CREATE=21, DROP=22, ALTER=23, 
		TRUNCATE=24, ANALYZE=25, VACUUM=26, EXPLAIN=27, LOCK=28, REINDEX=29, CLUSTER=30, 
		COMMENT=31, TABLE=32, TEMP=33, TEMPORARY=34, SELECT=35, INSERT=36, UPDATE=37, 
		DELETE=38, PERFORM=39, EXECUTE=40, CALL=41, INTO=42, FROM=43, WHERE=44, 
		SET=45, VALUES=46, RETURNING=47, TO=48, DISTINCT=49, ALL=50, GROUP=51, 
		HAVING=52, ORDER=53, LIMIT=54, OFFSET=55, ASC=56, DESC=57, NULLS=58, FIRST=59, 
		LAST=60, WITH=61, BETWEEN=62, UNION=63, INTERSECT=64, EXCEPT=65, EXISTS=66, 
		CAST=67, EXTRACT=68, COALESCE=69, NULLIF=70, GREATEST=71, LEAST=72, POSITION=73, 
		OVER=74, PARTITION=75, COMMIT=76, ROLLBACK=77, SAVEPOINT=78, START=79, 
		TRANSACTION=80, WORK=81, INTEGER=82, BIGINT=83, SMALLINT=84, NUMERIC=85, 
		DECIMAL=86, REAL=87, DOUBLE=88, VARCHAR=89, CHAR=90, TEXT=91, BOOLEAN=92, 
		DATE=93, TIME=94, TIMESTAMP=95, TIMESTAMPTZ=96, INTERVAL=97, RECORD=98, 
		JSON=99, JSONB=100, UUID=101, BYTEA=102, MONEY=103, IN=104, REVERSE=105, 
		BY=106, ARRAY=107, SLICE=108, JOIN=109, LEFT=110, RIGHT=111, SUBSTRING=112, 
		INNER=113, OUTER=114, FULL=115, CROSS=116, ON=117, NOTICE=118, WARNING=119, 
		INFO=120, DEBUG=121, LOG=122, SQLSTATE=123, ERRCODE=124, MESSAGE=125, 
		DETAIL=126, HINT=127, GET=128, DIAGNOSTICS=129, STACKED=130, CURRENT=131, 
		AS=132, IS=133, NULL=134, NOT=135, AND=136, OR=137, CONSTANT=138, DEFAULT=139, 
		USING=140, STRICT=141, ZONE=142, VOLATILE=143, STABLE=144, IMMUTABLE=145, 
		LANGUAGE=146, CALLED=147, SECURITY=148, DEFINER=149, INVOKER=150, ASSERT=151, 
		OPEN=152, CLOSE=153, FETCH=154, MOVE=155, FOUND=156, CURSOR=157, ALIAS=158, 
		ROWS=159, PRESERVE=160, TRUE=161, FALSE=162, PERCENT=163, TYPE=164, ROWTYPE=165, 
		DOLLAR=166, ASSIGN=167, CONCAT=168, EQ=169, NEQ=170, LT=171, LTE=172, 
		GT=173, GTE=174, PLUS=175, MINUS=176, STAR=177, SLASH=178, CARET=179, 
		TYPECAST=180, LIKE=181, ILIKE=182, SIMILAR=183, ANY=184, SOME=185, SEMI=186, 
		COMMA=187, DOT=188, LPAREN=189, RPAREN=190, LBRACK=191, RBRACK=192, LABEL_START=193, 
		LABEL_END=194, IntegerLiteral=195, NumericLiteral=196, StringLiteral=197, 
		DollarQuotedString=198, Identifier=199, QuotedIdentifier=200, LineComment=201, 
		BlockComment=202, WS=203;
	public static final int
		RULE_plpgsqlBlock = 0, RULE_label = 1, RULE_declareSection = 2, RULE_declarationList = 3, 
		RULE_declaration = 4, RULE_dataType = 5, RULE_statementList = 6, RULE_statement = 7, 
		RULE_assignmentStmt = 8, RULE_variableRef = 9, RULE_selectIntoStmt = 10, 
		RULE_selectList = 11, RULE_selectItem = 12, RULE_variableList = 13, RULE_fromClause = 14, 
		RULE_tableRef = 15, RULE_joinClause = 16, RULE_joinType = 17, RULE_whereClause = 18, 
		RULE_performStmt = 19, RULE_executeStmt = 20, RULE_insertStmt = 21, RULE_columnList = 22, 
		RULE_expressionList = 23, RULE_updateStmt = 24, RULE_assignmentList = 25, 
		RULE_columnAssignment = 26, RULE_deleteStmt = 27, RULE_setStmt = 28, RULE_ifStmt = 29, 
		RULE_caseStmt = 30, RULE_whenClauseList = 31, RULE_whenClause = 32, RULE_loopStmt = 33, 
		RULE_whileStmt = 34, RULE_forStmt = 35, RULE_forQuerySource = 36, RULE_foreachStmt = 37, 
		RULE_exitStmt = 38, RULE_continueStmt = 39, RULE_returnStmt = 40, RULE_raiseStmt = 41, 
		RULE_raiseOptionList = 42, RULE_raiseOption = 43, RULE_assertStmt = 44, 
		RULE_nullStmt = 45, RULE_commitStmt = 46, RULE_rollbackStmt = 47, RULE_nestedBlock = 48, 
		RULE_exceptionSection = 49, RULE_exceptionHandlerList = 50, RULE_exceptionHandler = 51, 
		RULE_exceptionConditionList = 52, RULE_exceptionCondition = 53, RULE_getDiagnosticsStmt = 54, 
		RULE_diagnosticsItemList = 55, RULE_diagnosticsItem = 56, RULE_diagnosticsOption = 57, 
		RULE_openCursorStmt = 58, RULE_closeCursorStmt = 59, RULE_fetchStmt = 60, 
		RULE_selectStmt = 61, RULE_withClause = 62, RULE_cteList = 63, RULE_cte = 64, 
		RULE_selectQuery = 65, RULE_groupByClause = 66, RULE_havingClause = 67, 
		RULE_orderByClause = 68, RULE_orderByItem = 69, RULE_limitClause = 70, 
		RULE_parameterList = 71, RULE_parameter = 72, RULE_expression = 73, RULE_whenExprClauseList = 74, 
		RULE_whenExprClause = 75, RULE_arrayExpression = 76, RULE_specialVariable = 77, 
		RULE_functionCall = 78, RULE_functionCallArgs = 79, RULE_windowFunction = 80, 
		RULE_windowSpec = 81, RULE_cteStmt = 82, RULE_sqlGenericStmt = 83, RULE_literal = 84, 
		RULE_createTempTableStmt = 85, RULE_createTableColumnDefList = 86, RULE_createTableColumnDef = 87, 
		RULE_onCommitClause = 88;
	private static String[] makeRuleNames() {
		return new String[] {
			"plpgsqlBlock", "label", "declareSection", "declarationList", "declaration", 
			"dataType", "statementList", "statement", "assignmentStmt", "variableRef", 
			"selectIntoStmt", "selectList", "selectItem", "variableList", "fromClause", 
			"tableRef", "joinClause", "joinType", "whereClause", "performStmt", "executeStmt", 
			"insertStmt", "columnList", "expressionList", "updateStmt", "assignmentList", 
			"columnAssignment", "deleteStmt", "setStmt", "ifStmt", "caseStmt", "whenClauseList", 
			"whenClause", "loopStmt", "whileStmt", "forStmt", "forQuerySource", "foreachStmt", 
			"exitStmt", "continueStmt", "returnStmt", "raiseStmt", "raiseOptionList", 
			"raiseOption", "assertStmt", "nullStmt", "commitStmt", "rollbackStmt", 
			"nestedBlock", "exceptionSection", "exceptionHandlerList", "exceptionHandler", 
			"exceptionConditionList", "exceptionCondition", "getDiagnosticsStmt", 
			"diagnosticsItemList", "diagnosticsItem", "diagnosticsOption", "openCursorStmt", 
			"closeCursorStmt", "fetchStmt", "selectStmt", "withClause", "cteList", 
			"cte", "selectQuery", "groupByClause", "havingClause", "orderByClause", 
			"orderByItem", "limitClause", "parameterList", "parameter", "expression", 
			"whenExprClauseList", "whenExprClause", "arrayExpression", "specialVariable", 
			"functionCall", "functionCallArgs", "windowFunction", "windowSpec", "cteStmt", 
			"sqlGenericStmt", "literal", "createTempTableStmt", "createTableColumnDefList", 
			"createTableColumnDef", "onCommitClause"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'DECLARE'", "'BEGIN'", "'END'", "'EXCEPTION'", "'IF'", "'THEN'", 
			"'ELSIF'", "'ELSE'", "'CASE'", "'WHEN'", "'LOOP'", "'WHILE'", "'FOR'", 
			"'FOREACH'", "'EXIT'", "'CONTINUE'", "'RETURN'", "'RAISE'", "'NEXT'", 
			"'QUERY'", "'CREATE'", "'DROP'", "'ALTER'", "'TRUNCATE'", "'ANALYZE'", 
			"'VACUUM'", "'EXPLAIN'", "'LOCK'", "'REINDEX'", "'CLUSTER'", "'COMMENT'", 
			"'TABLE'", "'TEMP'", "'TEMPORARY'", "'SELECT'", "'INSERT'", "'UPDATE'", 
			"'DELETE'", "'PERFORM'", "'EXECUTE'", "'CALL'", "'INTO'", "'FROM'", "'WHERE'", 
			"'SET'", "'VALUES'", "'RETURNING'", "'TO'", "'DISTINCT'", "'ALL'", "'GROUP'", 
			"'HAVING'", "'ORDER'", "'LIMIT'", "'OFFSET'", "'ASC'", "'DESC'", "'NULLS'", 
			"'FIRST'", "'LAST'", "'WITH'", "'BETWEEN'", "'UNION'", "'INTERSECT'", 
			"'EXCEPT'", "'EXISTS'", "'CAST'", "'EXTRACT'", "'COALESCE'", "'NULLIF'", 
			"'GREATEST'", "'LEAST'", "'POSITION'", "'OVER'", "'PARTITION'", "'COMMIT'", 
			"'ROLLBACK'", "'SAVEPOINT'", "'START'", "'TRANSACTION'", "'WORK'", null, 
			"'BIGINT'", "'SMALLINT'", "'NUMERIC'", "'DECIMAL'", "'REAL'", null, "'VARCHAR'", 
			"'CHAR'", "'TEXT'", null, "'DATE'", "'TIME'", "'TIMESTAMP'", "'TIMESTAMPTZ'", 
			"'INTERVAL'", "'RECORD'", "'JSON'", "'JSONB'", "'UUID'", "'BYTEA'", "'MONEY'", 
			"'IN'", "'REVERSE'", "'BY'", "'ARRAY'", "'SLICE'", "'JOIN'", "'LEFT'", 
			"'RIGHT'", "'SUBSTRING'", "'INNER'", "'OUTER'", "'FULL'", "'CROSS'", 
			"'ON'", "'NOTICE'", "'WARNING'", "'INFO'", "'DEBUG'", "'LOG'", "'SQLSTATE'", 
			"'ERRCODE'", "'MESSAGE'", "'DETAIL'", "'HINT'", "'GET'", "'DIAGNOSTICS'", 
			"'STACKED'", "'CURRENT'", "'AS'", "'IS'", "'NULL'", "'NOT'", "'AND'", 
			"'OR'", "'CONSTANT'", "'DEFAULT'", "'USING'", "'STRICT'", "'ZONE'", "'VOLATILE'", 
			"'STABLE'", "'IMMUTABLE'", "'LANGUAGE'", "'CALLED'", "'SECURITY'", "'DEFINER'", 
			"'INVOKER'", "'ASSERT'", "'OPEN'", "'CLOSE'", "'FETCH'", "'MOVE'", "'FOUND'", 
			"'CURSOR'", "'ALIAS'", "'ROWS'", "'PRESERVE'", "'TRUE'", "'FALSE'", "'%'", 
			"'TYPE'", "'ROWTYPE'", "'$'", "':='", "'||'", "'='", null, "'<'", "'<='", 
			"'>'", "'>='", "'+'", "'-'", "'*'", "'/'", "'^'", "'::'", "'LIKE'", "'ILIKE'", 
			"'SIMILAR'", "'ANY'", "'SOME'", "';'", "','", "'.'", "'('", "')'", "'['", 
			"']'", "'<<'", "'>>'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "DECLARE", "BEGIN", "END", "EXCEPTION", "IF", "THEN", "ELSIF", 
			"ELSE", "CASE", "WHEN", "LOOP", "WHILE", "FOR", "FOREACH", "EXIT", "CONTINUE", 
			"RETURN", "RAISE", "NEXT", "QUERY", "CREATE", "DROP", "ALTER", "TRUNCATE", 
			"ANALYZE", "VACUUM", "EXPLAIN", "LOCK", "REINDEX", "CLUSTER", "COMMENT", 
			"TABLE", "TEMP", "TEMPORARY", "SELECT", "INSERT", "UPDATE", "DELETE", 
			"PERFORM", "EXECUTE", "CALL", "INTO", "FROM", "WHERE", "SET", "VALUES", 
			"RETURNING", "TO", "DISTINCT", "ALL", "GROUP", "HAVING", "ORDER", "LIMIT", 
			"OFFSET", "ASC", "DESC", "NULLS", "FIRST", "LAST", "WITH", "BETWEEN", 
			"UNION", "INTERSECT", "EXCEPT", "EXISTS", "CAST", "EXTRACT", "COALESCE", 
			"NULLIF", "GREATEST", "LEAST", "POSITION", "OVER", "PARTITION", "COMMIT", 
			"ROLLBACK", "SAVEPOINT", "START", "TRANSACTION", "WORK", "INTEGER", "BIGINT", 
			"SMALLINT", "NUMERIC", "DECIMAL", "REAL", "DOUBLE", "VARCHAR", "CHAR", 
			"TEXT", "BOOLEAN", "DATE", "TIME", "TIMESTAMP", "TIMESTAMPTZ", "INTERVAL", 
			"RECORD", "JSON", "JSONB", "UUID", "BYTEA", "MONEY", "IN", "REVERSE", 
			"BY", "ARRAY", "SLICE", "JOIN", "LEFT", "RIGHT", "SUBSTRING", "INNER", 
			"OUTER", "FULL", "CROSS", "ON", "NOTICE", "WARNING", "INFO", "DEBUG", 
			"LOG", "SQLSTATE", "ERRCODE", "MESSAGE", "DETAIL", "HINT", "GET", "DIAGNOSTICS", 
			"STACKED", "CURRENT", "AS", "IS", "NULL", "NOT", "AND", "OR", "CONSTANT", 
			"DEFAULT", "USING", "STRICT", "ZONE", "VOLATILE", "STABLE", "IMMUTABLE", 
			"LANGUAGE", "CALLED", "SECURITY", "DEFINER", "INVOKER", "ASSERT", "OPEN", 
			"CLOSE", "FETCH", "MOVE", "FOUND", "CURSOR", "ALIAS", "ROWS", "PRESERVE", 
			"TRUE", "FALSE", "PERCENT", "TYPE", "ROWTYPE", "DOLLAR", "ASSIGN", "CONCAT", 
			"EQ", "NEQ", "LT", "LTE", "GT", "GTE", "PLUS", "MINUS", "STAR", "SLASH", 
			"CARET", "TYPECAST", "LIKE", "ILIKE", "SIMILAR", "ANY", "SOME", "SEMI", 
			"COMMA", "DOT", "LPAREN", "RPAREN", "LBRACK", "RBRACK", "LABEL_START", 
			"LABEL_END", "IntegerLiteral", "NumericLiteral", "StringLiteral", "DollarQuotedString", 
			"Identifier", "QuotedIdentifier", "LineComment", "BlockComment", "WS"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "PlpgsqlParser.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public PlpgsqlParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@SuppressWarnings("CheckReturnValue")
	public static class PlpgsqlBlockContext extends ParserRuleContext {
		public TerminalNode BEGIN() { return getToken(PlpgsqlParser.BEGIN, 0); }
		public StatementListContext statementList() {
			return getRuleContext(StatementListContext.class,0);
		}
		public TerminalNode END() { return getToken(PlpgsqlParser.END, 0); }
		public List<LabelContext> label() {
			return getRuleContexts(LabelContext.class);
		}
		public LabelContext label(int i) {
			return getRuleContext(LabelContext.class,i);
		}
		public DeclareSectionContext declareSection() {
			return getRuleContext(DeclareSectionContext.class,0);
		}
		public ExceptionSectionContext exceptionSection() {
			return getRuleContext(ExceptionSectionContext.class,0);
		}
		public TerminalNode SEMI() { return getToken(PlpgsqlParser.SEMI, 0); }
		public PlpgsqlBlockContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_plpgsqlBlock; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).enterPlpgsqlBlock(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).exitPlpgsqlBlock(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PlpgsqlParserVisitor ) return ((PlpgsqlParserVisitor<? extends T>)visitor).visitPlpgsqlBlock(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PlpgsqlBlockContext plpgsqlBlock() throws RecognitionException {
		PlpgsqlBlockContext _localctx = new PlpgsqlBlockContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_plpgsqlBlock);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(179);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LABEL_START) {
				{
				setState(178);
				label();
				}
			}

			setState(182);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==DECLARE) {
				{
				setState(181);
				declareSection();
				}
			}

			setState(184);
			match(BEGIN);
			setState(185);
			statementList();
			setState(187);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==EXCEPTION) {
				{
				setState(186);
				exceptionSection();
				}
			}

			setState(189);
			match(END);
			setState(191);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LABEL_START) {
				{
				setState(190);
				label();
				}
			}

			setState(194);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==SEMI) {
				{
				setState(193);
				match(SEMI);
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class LabelContext extends ParserRuleContext {
		public TerminalNode LABEL_START() { return getToken(PlpgsqlParser.LABEL_START, 0); }
		public TerminalNode Identifier() { return getToken(PlpgsqlParser.Identifier, 0); }
		public TerminalNode LABEL_END() { return getToken(PlpgsqlParser.LABEL_END, 0); }
		public LabelContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_label; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).enterLabel(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).exitLabel(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PlpgsqlParserVisitor ) return ((PlpgsqlParserVisitor<? extends T>)visitor).visitLabel(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LabelContext label() throws RecognitionException {
		LabelContext _localctx = new LabelContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_label);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(196);
			match(LABEL_START);
			setState(197);
			match(Identifier);
			setState(198);
			match(LABEL_END);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class DeclareSectionContext extends ParserRuleContext {
		public TerminalNode DECLARE() { return getToken(PlpgsqlParser.DECLARE, 0); }
		public DeclarationListContext declarationList() {
			return getRuleContext(DeclarationListContext.class,0);
		}
		public DeclareSectionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_declareSection; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).enterDeclareSection(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).exitDeclareSection(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PlpgsqlParserVisitor ) return ((PlpgsqlParserVisitor<? extends T>)visitor).visitDeclareSection(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DeclareSectionContext declareSection() throws RecognitionException {
		DeclareSectionContext _localctx = new DeclareSectionContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_declareSection);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(200);
			match(DECLARE);
			setState(201);
			declarationList();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class DeclarationListContext extends ParserRuleContext {
		public List<DeclarationContext> declaration() {
			return getRuleContexts(DeclarationContext.class);
		}
		public DeclarationContext declaration(int i) {
			return getRuleContext(DeclarationContext.class,i);
		}
		public DeclarationListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_declarationList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).enterDeclarationList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).exitDeclarationList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PlpgsqlParserVisitor ) return ((PlpgsqlParserVisitor<? extends T>)visitor).visitDeclarationList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DeclarationListContext declarationList() throws RecognitionException {
		DeclarationListContext _localctx = new DeclarationListContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_declarationList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(204); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(203);
				declaration();
				}
				}
				setState(206); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==Identifier );
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class DeclarationContext extends ParserRuleContext {
		public List<TerminalNode> Identifier() { return getTokens(PlpgsqlParser.Identifier); }
		public TerminalNode Identifier(int i) {
			return getToken(PlpgsqlParser.Identifier, i);
		}
		public DataTypeContext dataType() {
			return getRuleContext(DataTypeContext.class,0);
		}
		public TerminalNode SEMI() { return getToken(PlpgsqlParser.SEMI, 0); }
		public TerminalNode CONSTANT() { return getToken(PlpgsqlParser.CONSTANT, 0); }
		public TerminalNode NOT() { return getToken(PlpgsqlParser.NOT, 0); }
		public TerminalNode NULL() { return getToken(PlpgsqlParser.NULL, 0); }
		public TerminalNode ASSIGN() { return getToken(PlpgsqlParser.ASSIGN, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode DEFAULT() { return getToken(PlpgsqlParser.DEFAULT, 0); }
		public TerminalNode CURSOR() { return getToken(PlpgsqlParser.CURSOR, 0); }
		public SelectStmtContext selectStmt() {
			return getRuleContext(SelectStmtContext.class,0);
		}
		public TerminalNode IS() { return getToken(PlpgsqlParser.IS, 0); }
		public TerminalNode FOR() { return getToken(PlpgsqlParser.FOR, 0); }
		public TerminalNode LPAREN() { return getToken(PlpgsqlParser.LPAREN, 0); }
		public ParameterListContext parameterList() {
			return getRuleContext(ParameterListContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(PlpgsqlParser.RPAREN, 0); }
		public TerminalNode ALIAS() { return getToken(PlpgsqlParser.ALIAS, 0); }
		public TerminalNode DOLLAR() { return getToken(PlpgsqlParser.DOLLAR, 0); }
		public TerminalNode IntegerLiteral() { return getToken(PlpgsqlParser.IntegerLiteral, 0); }
		public DeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_declaration; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).enterDeclaration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).exitDeclaration(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PlpgsqlParserVisitor ) return ((PlpgsqlParserVisitor<? extends T>)visitor).visitDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DeclarationContext declaration() throws RecognitionException {
		DeclarationContext _localctx = new DeclarationContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_declaration);
		int _la;
		try {
			setState(246);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,11,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(208);
				match(Identifier);
				setState(210);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==CONSTANT) {
					{
					setState(209);
					match(CONSTANT);
					}
				}

				setState(212);
				dataType(0);
				setState(215);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NOT) {
					{
					setState(213);
					match(NOT);
					setState(214);
					match(NULL);
					}
				}

				setState(221);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case ASSIGN:
					{
					setState(217);
					match(ASSIGN);
					setState(218);
					expression(0);
					}
					break;
				case DEFAULT:
					{
					setState(219);
					match(DEFAULT);
					setState(220);
					expression(0);
					}
					break;
				case SEMI:
					break;
				default:
					break;
				}
				setState(223);
				match(SEMI);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(225);
				match(Identifier);
				setState(226);
				match(CURSOR);
				setState(231);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LPAREN) {
					{
					setState(227);
					match(LPAREN);
					setState(228);
					parameterList();
					setState(229);
					match(RPAREN);
					}
				}

				setState(233);
				_la = _input.LA(1);
				if ( !(_la==FOR || _la==IS) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(234);
				selectStmt();
				setState(235);
				match(SEMI);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(237);
				match(Identifier);
				setState(238);
				match(ALIAS);
				setState(239);
				match(FOR);
				setState(243);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case Identifier:
					{
					setState(240);
					match(Identifier);
					}
					break;
				case DOLLAR:
					{
					setState(241);
					match(DOLLAR);
					setState(242);
					match(IntegerLiteral);
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(245);
				match(SEMI);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class DataTypeContext extends ParserRuleContext {
		public TerminalNode INTEGER() { return getToken(PlpgsqlParser.INTEGER, 0); }
		public TerminalNode BIGINT() { return getToken(PlpgsqlParser.BIGINT, 0); }
		public TerminalNode SMALLINT() { return getToken(PlpgsqlParser.SMALLINT, 0); }
		public TerminalNode NUMERIC() { return getToken(PlpgsqlParser.NUMERIC, 0); }
		public TerminalNode LPAREN() { return getToken(PlpgsqlParser.LPAREN, 0); }
		public List<TerminalNode> IntegerLiteral() { return getTokens(PlpgsqlParser.IntegerLiteral); }
		public TerminalNode IntegerLiteral(int i) {
			return getToken(PlpgsqlParser.IntegerLiteral, i);
		}
		public TerminalNode RPAREN() { return getToken(PlpgsqlParser.RPAREN, 0); }
		public TerminalNode COMMA() { return getToken(PlpgsqlParser.COMMA, 0); }
		public TerminalNode DECIMAL() { return getToken(PlpgsqlParser.DECIMAL, 0); }
		public TerminalNode REAL() { return getToken(PlpgsqlParser.REAL, 0); }
		public TerminalNode DOUBLE() { return getToken(PlpgsqlParser.DOUBLE, 0); }
		public TerminalNode VARCHAR() { return getToken(PlpgsqlParser.VARCHAR, 0); }
		public TerminalNode CHAR() { return getToken(PlpgsqlParser.CHAR, 0); }
		public TerminalNode TEXT() { return getToken(PlpgsqlParser.TEXT, 0); }
		public TerminalNode BOOLEAN() { return getToken(PlpgsqlParser.BOOLEAN, 0); }
		public TerminalNode DATE() { return getToken(PlpgsqlParser.DATE, 0); }
		public TerminalNode TIME() { return getToken(PlpgsqlParser.TIME, 0); }
		public TerminalNode TIMESTAMP() { return getToken(PlpgsqlParser.TIMESTAMP, 0); }
		public TerminalNode WITH() { return getToken(PlpgsqlParser.WITH, 0); }
		public TerminalNode Identifier() { return getToken(PlpgsqlParser.Identifier, 0); }
		public TerminalNode ZONE() { return getToken(PlpgsqlParser.ZONE, 0); }
		public TerminalNode TIMESTAMPTZ() { return getToken(PlpgsqlParser.TIMESTAMPTZ, 0); }
		public TerminalNode INTERVAL() { return getToken(PlpgsqlParser.INTERVAL, 0); }
		public TerminalNode RECORD() { return getToken(PlpgsqlParser.RECORD, 0); }
		public TerminalNode JSON() { return getToken(PlpgsqlParser.JSON, 0); }
		public TerminalNode JSONB() { return getToken(PlpgsqlParser.JSONB, 0); }
		public TerminalNode UUID() { return getToken(PlpgsqlParser.UUID, 0); }
		public TerminalNode BYTEA() { return getToken(PlpgsqlParser.BYTEA, 0); }
		public TerminalNode MONEY() { return getToken(PlpgsqlParser.MONEY, 0); }
		public TerminalNode PERCENT() { return getToken(PlpgsqlParser.PERCENT, 0); }
		public TerminalNode TYPE() { return getToken(PlpgsqlParser.TYPE, 0); }
		public TerminalNode ROWTYPE() { return getToken(PlpgsqlParser.ROWTYPE, 0); }
		public DataTypeContext dataType() {
			return getRuleContext(DataTypeContext.class,0);
		}
		public TerminalNode ARRAY() { return getToken(PlpgsqlParser.ARRAY, 0); }
		public TerminalNode LBRACK() { return getToken(PlpgsqlParser.LBRACK, 0); }
		public TerminalNode RBRACK() { return getToken(PlpgsqlParser.RBRACK, 0); }
		public DataTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dataType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).enterDataType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).exitDataType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PlpgsqlParserVisitor ) return ((PlpgsqlParserVisitor<? extends T>)visitor).visitDataType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DataTypeContext dataType() throws RecognitionException {
		return dataType(0);
	}

	private DataTypeContext dataType(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		DataTypeContext _localctx = new DataTypeContext(_ctx, _parentState);
		DataTypeContext _prevctx = _localctx;
		int _startState = 10;
		enterRecursionRule(_localctx, 10, RULE_dataType, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(319);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case INTEGER:
				{
				setState(249);
				match(INTEGER);
				}
				break;
			case BIGINT:
				{
				setState(250);
				match(BIGINT);
				}
				break;
			case SMALLINT:
				{
				setState(251);
				match(SMALLINT);
				}
				break;
			case NUMERIC:
				{
				setState(252);
				match(NUMERIC);
				setState(260);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,13,_ctx) ) {
				case 1:
					{
					setState(253);
					match(LPAREN);
					setState(254);
					match(IntegerLiteral);
					setState(257);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==COMMA) {
						{
						setState(255);
						match(COMMA);
						setState(256);
						match(IntegerLiteral);
						}
					}

					setState(259);
					match(RPAREN);
					}
					break;
				}
				}
				break;
			case DECIMAL:
				{
				setState(262);
				match(DECIMAL);
				setState(270);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,15,_ctx) ) {
				case 1:
					{
					setState(263);
					match(LPAREN);
					setState(264);
					match(IntegerLiteral);
					setState(267);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==COMMA) {
						{
						setState(265);
						match(COMMA);
						setState(266);
						match(IntegerLiteral);
						}
					}

					setState(269);
					match(RPAREN);
					}
					break;
				}
				}
				break;
			case REAL:
				{
				setState(272);
				match(REAL);
				}
				break;
			case DOUBLE:
				{
				setState(273);
				match(DOUBLE);
				}
				break;
			case VARCHAR:
				{
				setState(274);
				match(VARCHAR);
				setState(278);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,16,_ctx) ) {
				case 1:
					{
					setState(275);
					match(LPAREN);
					setState(276);
					match(IntegerLiteral);
					setState(277);
					match(RPAREN);
					}
					break;
				}
				}
				break;
			case CHAR:
				{
				setState(280);
				match(CHAR);
				setState(284);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,17,_ctx) ) {
				case 1:
					{
					setState(281);
					match(LPAREN);
					setState(282);
					match(IntegerLiteral);
					setState(283);
					match(RPAREN);
					}
					break;
				}
				}
				break;
			case TEXT:
				{
				setState(286);
				match(TEXT);
				}
				break;
			case BOOLEAN:
				{
				setState(287);
				match(BOOLEAN);
				}
				break;
			case DATE:
				{
				setState(288);
				match(DATE);
				}
				break;
			case TIME:
				{
				setState(289);
				match(TIME);
				setState(293);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,18,_ctx) ) {
				case 1:
					{
					setState(290);
					match(LPAREN);
					setState(291);
					match(IntegerLiteral);
					setState(292);
					match(RPAREN);
					}
					break;
				}
				}
				break;
			case TIMESTAMP:
				{
				setState(295);
				match(TIMESTAMP);
				setState(299);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,19,_ctx) ) {
				case 1:
					{
					setState(296);
					match(LPAREN);
					setState(297);
					match(IntegerLiteral);
					setState(298);
					match(RPAREN);
					}
					break;
				}
				setState(304);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,20,_ctx) ) {
				case 1:
					{
					setState(301);
					match(WITH);
					setState(302);
					match(Identifier);
					setState(303);
					match(ZONE);
					}
					break;
				}
				}
				break;
			case TIMESTAMPTZ:
				{
				setState(306);
				match(TIMESTAMPTZ);
				}
				break;
			case INTERVAL:
				{
				setState(307);
				match(INTERVAL);
				}
				break;
			case RECORD:
				{
				setState(308);
				match(RECORD);
				}
				break;
			case JSON:
				{
				setState(309);
				match(JSON);
				}
				break;
			case JSONB:
				{
				setState(310);
				match(JSONB);
				}
				break;
			case UUID:
				{
				setState(311);
				match(UUID);
				}
				break;
			case BYTEA:
				{
				setState(312);
				match(BYTEA);
				}
				break;
			case MONEY:
				{
				setState(313);
				match(MONEY);
				}
				break;
			case Identifier:
				{
				setState(314);
				match(Identifier);
				setState(317);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,21,_ctx) ) {
				case 1:
					{
					setState(315);
					match(PERCENT);
					setState(316);
					_la = _input.LA(1);
					if ( !(_la==TYPE || _la==ROWTYPE) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					}
					break;
				}
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			_ctx.stop = _input.LT(-1);
			setState(329);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,24,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new DataTypeContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_dataType);
					setState(321);
					if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
					setState(322);
					match(ARRAY);
					setState(325);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,23,_ctx) ) {
					case 1:
						{
						setState(323);
						match(LBRACK);
						setState(324);
						match(RBRACK);
						}
						break;
					}
					}
					} 
				}
				setState(331);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,24,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class StatementListContext extends ParserRuleContext {
		public List<StatementContext> statement() {
			return getRuleContexts(StatementContext.class);
		}
		public StatementContext statement(int i) {
			return getRuleContext(StatementContext.class,i);
		}
		public StatementListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_statementList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).enterStatementList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).exitStatementList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PlpgsqlParserVisitor ) return ((PlpgsqlParserVisitor<? extends T>)visitor).visitStatementList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StatementListContext statementList() throws RecognitionException {
		StatementListContext _localctx = new StatementListContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_statementList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(335);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 2305882561565948454L) != 0) || ((((_la - 76)) & ~0x3f) == 0 && ((1L << (_la - 76)) & 292733975779082243L) != 0) || ((((_la - 151)) & ~0x3f) == 0 && ((1L << (_la - 151)) & 848822976643087L) != 0)) {
				{
				{
				setState(332);
				statement();
				}
				}
				setState(337);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class StatementContext extends ParserRuleContext {
		public AssignmentStmtContext assignmentStmt() {
			return getRuleContext(AssignmentStmtContext.class,0);
		}
		public SelectIntoStmtContext selectIntoStmt() {
			return getRuleContext(SelectIntoStmtContext.class,0);
		}
		public PerformStmtContext performStmt() {
			return getRuleContext(PerformStmtContext.class,0);
		}
		public ExecuteStmtContext executeStmt() {
			return getRuleContext(ExecuteStmtContext.class,0);
		}
		public InsertStmtContext insertStmt() {
			return getRuleContext(InsertStmtContext.class,0);
		}
		public UpdateStmtContext updateStmt() {
			return getRuleContext(UpdateStmtContext.class,0);
		}
		public DeleteStmtContext deleteStmt() {
			return getRuleContext(DeleteStmtContext.class,0);
		}
		public SetStmtContext setStmt() {
			return getRuleContext(SetStmtContext.class,0);
		}
		public IfStmtContext ifStmt() {
			return getRuleContext(IfStmtContext.class,0);
		}
		public CaseStmtContext caseStmt() {
			return getRuleContext(CaseStmtContext.class,0);
		}
		public LoopStmtContext loopStmt() {
			return getRuleContext(LoopStmtContext.class,0);
		}
		public WhileStmtContext whileStmt() {
			return getRuleContext(WhileStmtContext.class,0);
		}
		public ForStmtContext forStmt() {
			return getRuleContext(ForStmtContext.class,0);
		}
		public ForeachStmtContext foreachStmt() {
			return getRuleContext(ForeachStmtContext.class,0);
		}
		public ExitStmtContext exitStmt() {
			return getRuleContext(ExitStmtContext.class,0);
		}
		public ContinueStmtContext continueStmt() {
			return getRuleContext(ContinueStmtContext.class,0);
		}
		public ReturnStmtContext returnStmt() {
			return getRuleContext(ReturnStmtContext.class,0);
		}
		public RaiseStmtContext raiseStmt() {
			return getRuleContext(RaiseStmtContext.class,0);
		}
		public AssertStmtContext assertStmt() {
			return getRuleContext(AssertStmtContext.class,0);
		}
		public NullStmtContext nullStmt() {
			return getRuleContext(NullStmtContext.class,0);
		}
		public NestedBlockContext nestedBlock() {
			return getRuleContext(NestedBlockContext.class,0);
		}
		public GetDiagnosticsStmtContext getDiagnosticsStmt() {
			return getRuleContext(GetDiagnosticsStmtContext.class,0);
		}
		public OpenCursorStmtContext openCursorStmt() {
			return getRuleContext(OpenCursorStmtContext.class,0);
		}
		public CloseCursorStmtContext closeCursorStmt() {
			return getRuleContext(CloseCursorStmtContext.class,0);
		}
		public FetchStmtContext fetchStmt() {
			return getRuleContext(FetchStmtContext.class,0);
		}
		public CommitStmtContext commitStmt() {
			return getRuleContext(CommitStmtContext.class,0);
		}
		public RollbackStmtContext rollbackStmt() {
			return getRuleContext(RollbackStmtContext.class,0);
		}
		public CreateTempTableStmtContext createTempTableStmt() {
			return getRuleContext(CreateTempTableStmtContext.class,0);
		}
		public CteStmtContext cteStmt() {
			return getRuleContext(CteStmtContext.class,0);
		}
		public SqlGenericStmtContext sqlGenericStmt() {
			return getRuleContext(SqlGenericStmtContext.class,0);
		}
		public StatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_statement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).enterStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).exitStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PlpgsqlParserVisitor ) return ((PlpgsqlParserVisitor<? extends T>)visitor).visitStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StatementContext statement() throws RecognitionException {
		StatementContext _localctx = new StatementContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_statement);
		try {
			setState(368);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,26,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(338);
				assignmentStmt();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(339);
				selectIntoStmt();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(340);
				performStmt();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(341);
				executeStmt();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(342);
				insertStmt();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(343);
				updateStmt();
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(344);
				deleteStmt();
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(345);
				setStmt();
				}
				break;
			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(346);
				ifStmt();
				}
				break;
			case 10:
				enterOuterAlt(_localctx, 10);
				{
				setState(347);
				caseStmt();
				}
				break;
			case 11:
				enterOuterAlt(_localctx, 11);
				{
				setState(348);
				loopStmt();
				}
				break;
			case 12:
				enterOuterAlt(_localctx, 12);
				{
				setState(349);
				whileStmt();
				}
				break;
			case 13:
				enterOuterAlt(_localctx, 13);
				{
				setState(350);
				forStmt();
				}
				break;
			case 14:
				enterOuterAlt(_localctx, 14);
				{
				setState(351);
				foreachStmt();
				}
				break;
			case 15:
				enterOuterAlt(_localctx, 15);
				{
				setState(352);
				exitStmt();
				}
				break;
			case 16:
				enterOuterAlt(_localctx, 16);
				{
				setState(353);
				continueStmt();
				}
				break;
			case 17:
				enterOuterAlt(_localctx, 17);
				{
				setState(354);
				returnStmt();
				}
				break;
			case 18:
				enterOuterAlt(_localctx, 18);
				{
				setState(355);
				raiseStmt();
				}
				break;
			case 19:
				enterOuterAlt(_localctx, 19);
				{
				setState(356);
				assertStmt();
				}
				break;
			case 20:
				enterOuterAlt(_localctx, 20);
				{
				setState(357);
				nullStmt();
				}
				break;
			case 21:
				enterOuterAlt(_localctx, 21);
				{
				setState(358);
				nestedBlock();
				}
				break;
			case 22:
				enterOuterAlt(_localctx, 22);
				{
				setState(359);
				getDiagnosticsStmt();
				}
				break;
			case 23:
				enterOuterAlt(_localctx, 23);
				{
				setState(360);
				openCursorStmt();
				}
				break;
			case 24:
				enterOuterAlt(_localctx, 24);
				{
				setState(361);
				closeCursorStmt();
				}
				break;
			case 25:
				enterOuterAlt(_localctx, 25);
				{
				setState(362);
				fetchStmt();
				}
				break;
			case 26:
				enterOuterAlt(_localctx, 26);
				{
				setState(363);
				commitStmt();
				}
				break;
			case 27:
				enterOuterAlt(_localctx, 27);
				{
				setState(364);
				rollbackStmt();
				}
				break;
			case 28:
				enterOuterAlt(_localctx, 28);
				{
				setState(365);
				createTempTableStmt();
				}
				break;
			case 29:
				enterOuterAlt(_localctx, 29);
				{
				setState(366);
				cteStmt();
				}
				break;
			case 30:
				enterOuterAlt(_localctx, 30);
				{
				setState(367);
				sqlGenericStmt();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class AssignmentStmtContext extends ParserRuleContext {
		public VariableRefContext variableRef() {
			return getRuleContext(VariableRefContext.class,0);
		}
		public TerminalNode ASSIGN() { return getToken(PlpgsqlParser.ASSIGN, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode SEMI() { return getToken(PlpgsqlParser.SEMI, 0); }
		public AssignmentStmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_assignmentStmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).enterAssignmentStmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).exitAssignmentStmt(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PlpgsqlParserVisitor ) return ((PlpgsqlParserVisitor<? extends T>)visitor).visitAssignmentStmt(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AssignmentStmtContext assignmentStmt() throws RecognitionException {
		AssignmentStmtContext _localctx = new AssignmentStmtContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_assignmentStmt);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(370);
			variableRef();
			setState(371);
			match(ASSIGN);
			setState(372);
			expression(0);
			setState(373);
			match(SEMI);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class VariableRefContext extends ParserRuleContext {
		public List<TerminalNode> Identifier() { return getTokens(PlpgsqlParser.Identifier); }
		public TerminalNode Identifier(int i) {
			return getToken(PlpgsqlParser.Identifier, i);
		}
		public List<TerminalNode> DOT() { return getTokens(PlpgsqlParser.DOT); }
		public TerminalNode DOT(int i) {
			return getToken(PlpgsqlParser.DOT, i);
		}
		public List<TerminalNode> QuotedIdentifier() { return getTokens(PlpgsqlParser.QuotedIdentifier); }
		public TerminalNode QuotedIdentifier(int i) {
			return getToken(PlpgsqlParser.QuotedIdentifier, i);
		}
		public VariableRefContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_variableRef; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).enterVariableRef(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).exitVariableRef(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PlpgsqlParserVisitor ) return ((PlpgsqlParserVisitor<? extends T>)visitor).visitVariableRef(this);
			else return visitor.visitChildren(this);
		}
	}

	public final VariableRefContext variableRef() throws RecognitionException {
		VariableRefContext _localctx = new VariableRefContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_variableRef);
		try {
			int _alt;
			setState(391);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case Identifier:
				enterOuterAlt(_localctx, 1);
				{
				setState(375);
				match(Identifier);
				setState(380);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,27,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(376);
						match(DOT);
						setState(377);
						match(Identifier);
						}
						} 
					}
					setState(382);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,27,_ctx);
				}
				}
				break;
			case QuotedIdentifier:
				enterOuterAlt(_localctx, 2);
				{
				setState(383);
				match(QuotedIdentifier);
				setState(388);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,28,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(384);
						match(DOT);
						setState(385);
						match(QuotedIdentifier);
						}
						} 
					}
					setState(390);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,28,_ctx);
				}
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class SelectIntoStmtContext extends ParserRuleContext {
		public TerminalNode SELECT() { return getToken(PlpgsqlParser.SELECT, 0); }
		public SelectListContext selectList() {
			return getRuleContext(SelectListContext.class,0);
		}
		public TerminalNode INTO() { return getToken(PlpgsqlParser.INTO, 0); }
		public VariableListContext variableList() {
			return getRuleContext(VariableListContext.class,0);
		}
		public TerminalNode SEMI() { return getToken(PlpgsqlParser.SEMI, 0); }
		public WithClauseContext withClause() {
			return getRuleContext(WithClauseContext.class,0);
		}
		public TerminalNode STRICT() { return getToken(PlpgsqlParser.STRICT, 0); }
		public FromClauseContext fromClause() {
			return getRuleContext(FromClauseContext.class,0);
		}
		public WhereClauseContext whereClause() {
			return getRuleContext(WhereClauseContext.class,0);
		}
		public GroupByClauseContext groupByClause() {
			return getRuleContext(GroupByClauseContext.class,0);
		}
		public HavingClauseContext havingClause() {
			return getRuleContext(HavingClauseContext.class,0);
		}
		public OrderByClauseContext orderByClause() {
			return getRuleContext(OrderByClauseContext.class,0);
		}
		public LimitClauseContext limitClause() {
			return getRuleContext(LimitClauseContext.class,0);
		}
		public SelectIntoStmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_selectIntoStmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).enterSelectIntoStmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).exitSelectIntoStmt(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PlpgsqlParserVisitor ) return ((PlpgsqlParserVisitor<? extends T>)visitor).visitSelectIntoStmt(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SelectIntoStmtContext selectIntoStmt() throws RecognitionException {
		SelectIntoStmtContext _localctx = new SelectIntoStmtContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_selectIntoStmt);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(394);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==WITH) {
				{
				setState(393);
				withClause();
				}
			}

			setState(396);
			match(SELECT);
			setState(397);
			selectList();
			setState(398);
			match(INTO);
			setState(400);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==STRICT) {
				{
				setState(399);
				match(STRICT);
				}
			}

			setState(402);
			variableList();
			setState(404);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==FROM) {
				{
				setState(403);
				fromClause();
				}
			}

			setState(407);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==WHERE) {
				{
				setState(406);
				whereClause();
				}
			}

			setState(410);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==GROUP) {
				{
				setState(409);
				groupByClause();
				}
			}

			setState(413);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==HAVING) {
				{
				setState(412);
				havingClause();
				}
			}

			setState(416);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ORDER) {
				{
				setState(415);
				orderByClause();
				}
			}

			setState(419);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LIMIT) {
				{
				setState(418);
				limitClause();
				}
			}

			setState(421);
			match(SEMI);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class SelectListContext extends ParserRuleContext {
		public TerminalNode STAR() { return getToken(PlpgsqlParser.STAR, 0); }
		public List<SelectItemContext> selectItem() {
			return getRuleContexts(SelectItemContext.class);
		}
		public SelectItemContext selectItem(int i) {
			return getRuleContext(SelectItemContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(PlpgsqlParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(PlpgsqlParser.COMMA, i);
		}
		public SelectListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_selectList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).enterSelectList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).exitSelectList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PlpgsqlParserVisitor ) return ((PlpgsqlParserVisitor<? extends T>)visitor).visitSelectList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SelectListContext selectList() throws RecognitionException {
		SelectListContext _localctx = new SelectListContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_selectList);
		int _la;
		try {
			setState(432);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case STAR:
				enterOuterAlt(_localctx, 1);
				{
				setState(423);
				match(STAR);
				}
				break;
			case CASE:
			case EXISTS:
			case CAST:
			case COALESCE:
			case NULLIF:
			case GREATEST:
			case LEAST:
			case POSITION:
			case INTERVAL:
			case ARRAY:
			case LEFT:
			case RIGHT:
			case SUBSTRING:
			case SQLSTATE:
			case NULL:
			case NOT:
			case FOUND:
			case TRUE:
			case FALSE:
			case LPAREN:
			case IntegerLiteral:
			case NumericLiteral:
			case StringLiteral:
			case DollarQuotedString:
			case Identifier:
			case QuotedIdentifier:
				enterOuterAlt(_localctx, 2);
				{
				setState(424);
				selectItem();
				setState(429);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(425);
					match(COMMA);
					setState(426);
					selectItem();
					}
					}
					setState(431);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class SelectItemContext extends ParserRuleContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode Identifier() { return getToken(PlpgsqlParser.Identifier, 0); }
		public TerminalNode AS() { return getToken(PlpgsqlParser.AS, 0); }
		public SelectItemContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_selectItem; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).enterSelectItem(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).exitSelectItem(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PlpgsqlParserVisitor ) return ((PlpgsqlParserVisitor<? extends T>)visitor).visitSelectItem(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SelectItemContext selectItem() throws RecognitionException {
		SelectItemContext _localctx = new SelectItemContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_selectItem);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(434);
			expression(0);
			setState(439);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==AS || _la==Identifier) {
				{
				setState(436);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==AS) {
					{
					setState(435);
					match(AS);
					}
				}

				setState(438);
				match(Identifier);
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class VariableListContext extends ParserRuleContext {
		public List<VariableRefContext> variableRef() {
			return getRuleContexts(VariableRefContext.class);
		}
		public VariableRefContext variableRef(int i) {
			return getRuleContext(VariableRefContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(PlpgsqlParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(PlpgsqlParser.COMMA, i);
		}
		public VariableListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_variableList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).enterVariableList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).exitVariableList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PlpgsqlParserVisitor ) return ((PlpgsqlParserVisitor<? extends T>)visitor).visitVariableList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final VariableListContext variableList() throws RecognitionException {
		VariableListContext _localctx = new VariableListContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_variableList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(441);
			variableRef();
			setState(446);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(442);
				match(COMMA);
				setState(443);
				variableRef();
				}
				}
				setState(448);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FromClauseContext extends ParserRuleContext {
		public TerminalNode FROM() { return getToken(PlpgsqlParser.FROM, 0); }
		public TableRefContext tableRef() {
			return getRuleContext(TableRefContext.class,0);
		}
		public List<JoinClauseContext> joinClause() {
			return getRuleContexts(JoinClauseContext.class);
		}
		public JoinClauseContext joinClause(int i) {
			return getRuleContext(JoinClauseContext.class,i);
		}
		public FromClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fromClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).enterFromClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).exitFromClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PlpgsqlParserVisitor ) return ((PlpgsqlParserVisitor<? extends T>)visitor).visitFromClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FromClauseContext fromClause() throws RecognitionException {
		FromClauseContext _localctx = new FromClauseContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_fromClause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(449);
			match(FROM);
			setState(450);
			tableRef();
			setState(454);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (((((_la - 109)) & ~0x3f) == 0 && ((1L << (_la - 109)) & 215L) != 0) || _la==COMMA) {
				{
				{
				setState(451);
				joinClause();
				}
				}
				setState(456);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class TableRefContext extends ParserRuleContext {
		public List<TerminalNode> Identifier() { return getTokens(PlpgsqlParser.Identifier); }
		public TerminalNode Identifier(int i) {
			return getToken(PlpgsqlParser.Identifier, i);
		}
		public List<TerminalNode> DOT() { return getTokens(PlpgsqlParser.DOT); }
		public TerminalNode DOT(int i) {
			return getToken(PlpgsqlParser.DOT, i);
		}
		public TerminalNode AS() { return getToken(PlpgsqlParser.AS, 0); }
		public List<TerminalNode> QuotedIdentifier() { return getTokens(PlpgsqlParser.QuotedIdentifier); }
		public TerminalNode QuotedIdentifier(int i) {
			return getToken(PlpgsqlParser.QuotedIdentifier, i);
		}
		public FunctionCallContext functionCall() {
			return getRuleContext(FunctionCallContext.class,0);
		}
		public TerminalNode WITH() { return getToken(PlpgsqlParser.WITH, 0); }
		public TerminalNode LPAREN() { return getToken(PlpgsqlParser.LPAREN, 0); }
		public SelectStmtContext selectStmt() {
			return getRuleContext(SelectStmtContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(PlpgsqlParser.RPAREN, 0); }
		public TableRefContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tableRef; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).enterTableRef(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).exitTableRef(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PlpgsqlParserVisitor ) return ((PlpgsqlParserVisitor<? extends T>)visitor).visitTableRef(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TableRefContext tableRef() throws RecognitionException {
		TableRefContext _localctx = new TableRefContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_tableRef);
		int _la;
		try {
			setState(505);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,55,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(457);
				match(Identifier);
				setState(462);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==DOT) {
					{
					{
					setState(458);
					match(DOT);
					setState(459);
					match(Identifier);
					}
					}
					setState(464);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(469);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==AS || _la==Identifier) {
					{
					setState(466);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==AS) {
						{
						setState(465);
						match(AS);
						}
					}

					setState(468);
					match(Identifier);
					}
				}

				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(471);
				match(QuotedIdentifier);
				setState(476);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==DOT) {
					{
					{
					setState(472);
					match(DOT);
					setState(473);
					match(QuotedIdentifier);
					}
					}
					setState(478);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(483);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==AS || _la==Identifier) {
					{
					setState(480);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==AS) {
						{
						setState(479);
						match(AS);
						}
					}

					setState(482);
					match(Identifier);
					}
				}

				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(485);
				functionCall();
				setState(488);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==WITH) {
					{
					setState(486);
					match(WITH);
					setState(487);
					match(Identifier);
					}
				}

				setState(494);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==AS || _la==Identifier) {
					{
					setState(491);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==AS) {
						{
						setState(490);
						match(AS);
						}
					}

					setState(493);
					match(Identifier);
					}
				}

				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(496);
				match(LPAREN);
				setState(497);
				selectStmt();
				setState(498);
				match(RPAREN);
				setState(503);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==AS || _la==Identifier) {
					{
					setState(500);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==AS) {
						{
						setState(499);
						match(AS);
						}
					}

					setState(502);
					match(Identifier);
					}
				}

				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class JoinClauseContext extends ParserRuleContext {
		public TerminalNode COMMA() { return getToken(PlpgsqlParser.COMMA, 0); }
		public TableRefContext tableRef() {
			return getRuleContext(TableRefContext.class,0);
		}
		public TerminalNode JOIN() { return getToken(PlpgsqlParser.JOIN, 0); }
		public JoinTypeContext joinType() {
			return getRuleContext(JoinTypeContext.class,0);
		}
		public TerminalNode ON() { return getToken(PlpgsqlParser.ON, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode USING() { return getToken(PlpgsqlParser.USING, 0); }
		public TerminalNode LPAREN() { return getToken(PlpgsqlParser.LPAREN, 0); }
		public ColumnListContext columnList() {
			return getRuleContext(ColumnListContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(PlpgsqlParser.RPAREN, 0); }
		public JoinClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_joinClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).enterJoinClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).exitJoinClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PlpgsqlParserVisitor ) return ((PlpgsqlParserVisitor<? extends T>)visitor).visitJoinClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final JoinClauseContext joinClause() throws RecognitionException {
		JoinClauseContext _localctx = new JoinClauseContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_joinClause);
		int _la;
		try {
			setState(523);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case COMMA:
				enterOuterAlt(_localctx, 1);
				{
				setState(507);
				match(COMMA);
				setState(508);
				tableRef();
				}
				break;
			case JOIN:
			case LEFT:
			case RIGHT:
			case INNER:
			case FULL:
			case CROSS:
				enterOuterAlt(_localctx, 2);
				{
				setState(510);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (((((_la - 110)) & ~0x3f) == 0 && ((1L << (_la - 110)) & 107L) != 0)) {
					{
					setState(509);
					joinType();
					}
				}

				setState(512);
				match(JOIN);
				setState(513);
				tableRef();
				setState(521);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case ON:
					{
					setState(514);
					match(ON);
					setState(515);
					expression(0);
					}
					break;
				case USING:
					{
					setState(516);
					match(USING);
					setState(517);
					match(LPAREN);
					setState(518);
					columnList();
					setState(519);
					match(RPAREN);
					}
					break;
				case LOOP:
				case WHERE:
				case RETURNING:
				case GROUP:
				case HAVING:
				case ORDER:
				case LIMIT:
				case UNION:
				case INTERSECT:
				case EXCEPT:
				case JOIN:
				case LEFT:
				case RIGHT:
				case INNER:
				case FULL:
				case CROSS:
				case SEMI:
				case COMMA:
				case RPAREN:
					break;
				default:
					break;
				}
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class JoinTypeContext extends ParserRuleContext {
		public TerminalNode INNER() { return getToken(PlpgsqlParser.INNER, 0); }
		public TerminalNode LEFT() { return getToken(PlpgsqlParser.LEFT, 0); }
		public TerminalNode OUTER() { return getToken(PlpgsqlParser.OUTER, 0); }
		public TerminalNode RIGHT() { return getToken(PlpgsqlParser.RIGHT, 0); }
		public TerminalNode FULL() { return getToken(PlpgsqlParser.FULL, 0); }
		public TerminalNode CROSS() { return getToken(PlpgsqlParser.CROSS, 0); }
		public JoinTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_joinType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).enterJoinType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).exitJoinType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PlpgsqlParserVisitor ) return ((PlpgsqlParserVisitor<? extends T>)visitor).visitJoinType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final JoinTypeContext joinType() throws RecognitionException {
		JoinTypeContext _localctx = new JoinTypeContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_joinType);
		int _la;
		try {
			setState(539);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case INNER:
				enterOuterAlt(_localctx, 1);
				{
				setState(525);
				match(INNER);
				}
				break;
			case LEFT:
				enterOuterAlt(_localctx, 2);
				{
				setState(526);
				match(LEFT);
				setState(528);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==OUTER) {
					{
					setState(527);
					match(OUTER);
					}
				}

				}
				break;
			case RIGHT:
				enterOuterAlt(_localctx, 3);
				{
				setState(530);
				match(RIGHT);
				setState(532);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==OUTER) {
					{
					setState(531);
					match(OUTER);
					}
				}

				}
				break;
			case FULL:
				enterOuterAlt(_localctx, 4);
				{
				setState(534);
				match(FULL);
				setState(536);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==OUTER) {
					{
					setState(535);
					match(OUTER);
					}
				}

				}
				break;
			case CROSS:
				enterOuterAlt(_localctx, 5);
				{
				setState(538);
				match(CROSS);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class WhereClauseContext extends ParserRuleContext {
		public TerminalNode WHERE() { return getToken(PlpgsqlParser.WHERE, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public WhereClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_whereClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).enterWhereClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).exitWhereClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PlpgsqlParserVisitor ) return ((PlpgsqlParserVisitor<? extends T>)visitor).visitWhereClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final WhereClauseContext whereClause() throws RecognitionException {
		WhereClauseContext _localctx = new WhereClauseContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_whereClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(541);
			match(WHERE);
			setState(542);
			expression(0);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class PerformStmtContext extends ParserRuleContext {
		public TerminalNode PERFORM() { return getToken(PlpgsqlParser.PERFORM, 0); }
		public SelectListContext selectList() {
			return getRuleContext(SelectListContext.class,0);
		}
		public TerminalNode SEMI() { return getToken(PlpgsqlParser.SEMI, 0); }
		public FromClauseContext fromClause() {
			return getRuleContext(FromClauseContext.class,0);
		}
		public WhereClauseContext whereClause() {
			return getRuleContext(WhereClauseContext.class,0);
		}
		public PerformStmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_performStmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).enterPerformStmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).exitPerformStmt(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PlpgsqlParserVisitor ) return ((PlpgsqlParserVisitor<? extends T>)visitor).visitPerformStmt(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PerformStmtContext performStmt() throws RecognitionException {
		PerformStmtContext _localctx = new PerformStmtContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_performStmt);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(544);
			match(PERFORM);
			setState(545);
			selectList();
			setState(547);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==FROM) {
				{
				setState(546);
				fromClause();
				}
			}

			setState(550);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==WHERE) {
				{
				setState(549);
				whereClause();
				}
			}

			setState(552);
			match(SEMI);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ExecuteStmtContext extends ParserRuleContext {
		public TerminalNode EXECUTE() { return getToken(PlpgsqlParser.EXECUTE, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode SEMI() { return getToken(PlpgsqlParser.SEMI, 0); }
		public TerminalNode INTO() { return getToken(PlpgsqlParser.INTO, 0); }
		public VariableListContext variableList() {
			return getRuleContext(VariableListContext.class,0);
		}
		public TerminalNode USING() { return getToken(PlpgsqlParser.USING, 0); }
		public ExpressionListContext expressionList() {
			return getRuleContext(ExpressionListContext.class,0);
		}
		public ExecuteStmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_executeStmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).enterExecuteStmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).exitExecuteStmt(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PlpgsqlParserVisitor ) return ((PlpgsqlParserVisitor<? extends T>)visitor).visitExecuteStmt(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExecuteStmtContext executeStmt() throws RecognitionException {
		ExecuteStmtContext _localctx = new ExecuteStmtContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_executeStmt);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(554);
			match(EXECUTE);
			setState(555);
			expression(0);
			setState(558);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==INTO) {
				{
				setState(556);
				match(INTO);
				setState(557);
				variableList();
				}
			}

			setState(562);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==USING) {
				{
				setState(560);
				match(USING);
				setState(561);
				expressionList();
				}
			}

			setState(564);
			match(SEMI);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class InsertStmtContext extends ParserRuleContext {
		public TerminalNode INSERT() { return getToken(PlpgsqlParser.INSERT, 0); }
		public List<TerminalNode> INTO() { return getTokens(PlpgsqlParser.INTO); }
		public TerminalNode INTO(int i) {
			return getToken(PlpgsqlParser.INTO, i);
		}
		public List<TerminalNode> Identifier() { return getTokens(PlpgsqlParser.Identifier); }
		public TerminalNode Identifier(int i) {
			return getToken(PlpgsqlParser.Identifier, i);
		}
		public TerminalNode VALUES() { return getToken(PlpgsqlParser.VALUES, 0); }
		public List<TerminalNode> LPAREN() { return getTokens(PlpgsqlParser.LPAREN); }
		public TerminalNode LPAREN(int i) {
			return getToken(PlpgsqlParser.LPAREN, i);
		}
		public List<ExpressionListContext> expressionList() {
			return getRuleContexts(ExpressionListContext.class);
		}
		public ExpressionListContext expressionList(int i) {
			return getRuleContext(ExpressionListContext.class,i);
		}
		public List<TerminalNode> RPAREN() { return getTokens(PlpgsqlParser.RPAREN); }
		public TerminalNode RPAREN(int i) {
			return getToken(PlpgsqlParser.RPAREN, i);
		}
		public List<TerminalNode> SEMI() { return getTokens(PlpgsqlParser.SEMI); }
		public TerminalNode SEMI(int i) {
			return getToken(PlpgsqlParser.SEMI, i);
		}
		public List<TerminalNode> DOT() { return getTokens(PlpgsqlParser.DOT); }
		public TerminalNode DOT(int i) {
			return getToken(PlpgsqlParser.DOT, i);
		}
		public ColumnListContext columnList() {
			return getRuleContext(ColumnListContext.class,0);
		}
		public List<TerminalNode> COMMA() { return getTokens(PlpgsqlParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(PlpgsqlParser.COMMA, i);
		}
		public TerminalNode RETURNING() { return getToken(PlpgsqlParser.RETURNING, 0); }
		public VariableListContext variableList() {
			return getRuleContext(VariableListContext.class,0);
		}
		public InsertStmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_insertStmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).enterInsertStmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).exitInsertStmt(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PlpgsqlParserVisitor ) return ((PlpgsqlParserVisitor<? extends T>)visitor).visitInsertStmt(this);
			else return visitor.visitChildren(this);
		}
	}

	public final InsertStmtContext insertStmt() throws RecognitionException {
		InsertStmtContext _localctx = new InsertStmtContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_insertStmt);
		int _la;
		try {
			int _alt;
			setState(628);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,75,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(566);
				match(INSERT);
				setState(567);
				match(INTO);
				setState(568);
				match(Identifier);
				setState(573);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==DOT) {
					{
					{
					setState(569);
					match(DOT);
					setState(570);
					match(Identifier);
					}
					}
					setState(575);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(580);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LPAREN) {
					{
					setState(576);
					match(LPAREN);
					setState(577);
					columnList();
					setState(578);
					match(RPAREN);
					}
				}

				setState(582);
				match(VALUES);
				setState(583);
				match(LPAREN);
				setState(584);
				expressionList();
				setState(585);
				match(RPAREN);
				setState(593);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(586);
					match(COMMA);
					setState(587);
					match(LPAREN);
					setState(588);
					expressionList();
					setState(589);
					match(RPAREN);
					}
					}
					setState(595);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(602);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==RETURNING) {
					{
					setState(596);
					match(RETURNING);
					setState(597);
					expressionList();
					setState(600);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==INTO) {
						{
						setState(598);
						match(INTO);
						setState(599);
						variableList();
						}
					}

					}
				}

				setState(604);
				match(SEMI);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(606);
				match(INSERT);
				setState(607);
				match(INTO);
				setState(608);
				match(Identifier);
				setState(613);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,72,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(609);
						match(DOT);
						setState(610);
						match(Identifier);
						}
						} 
					}
					setState(615);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,72,_ctx);
				}
				setState(620);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,73,_ctx) ) {
				case 1:
					{
					setState(616);
					match(LPAREN);
					setState(617);
					columnList();
					setState(618);
					match(RPAREN);
					}
					break;
				}
				setState(623); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(622);
					_la = _input.LA(1);
					if ( _la <= 0 || (_la==SEMI) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					}
					}
					setState(625); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & -2L) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & -1L) != 0) || ((((_la - 128)) & ~0x3f) == 0 && ((1L << (_la - 128)) & -288230376151711745L) != 0) || ((((_la - 192)) & ~0x3f) == 0 && ((1L << (_la - 192)) & 4095L) != 0) );
				setState(627);
				match(SEMI);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ColumnListContext extends ParserRuleContext {
		public List<TerminalNode> Identifier() { return getTokens(PlpgsqlParser.Identifier); }
		public TerminalNode Identifier(int i) {
			return getToken(PlpgsqlParser.Identifier, i);
		}
		public List<TerminalNode> COMMA() { return getTokens(PlpgsqlParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(PlpgsqlParser.COMMA, i);
		}
		public ColumnListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_columnList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).enterColumnList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).exitColumnList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PlpgsqlParserVisitor ) return ((PlpgsqlParserVisitor<? extends T>)visitor).visitColumnList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ColumnListContext columnList() throws RecognitionException {
		ColumnListContext _localctx = new ColumnListContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_columnList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(630);
			match(Identifier);
			setState(635);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(631);
				match(COMMA);
				setState(632);
				match(Identifier);
				}
				}
				setState(637);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ExpressionListContext extends ParserRuleContext {
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(PlpgsqlParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(PlpgsqlParser.COMMA, i);
		}
		public ExpressionListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expressionList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).enterExpressionList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).exitExpressionList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PlpgsqlParserVisitor ) return ((PlpgsqlParserVisitor<? extends T>)visitor).visitExpressionList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExpressionListContext expressionList() throws RecognitionException {
		ExpressionListContext _localctx = new ExpressionListContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_expressionList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(638);
			expression(0);
			setState(643);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(639);
				match(COMMA);
				setState(640);
				expression(0);
				}
				}
				setState(645);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class UpdateStmtContext extends ParserRuleContext {
		public TerminalNode UPDATE() { return getToken(PlpgsqlParser.UPDATE, 0); }
		public List<TerminalNode> Identifier() { return getTokens(PlpgsqlParser.Identifier); }
		public TerminalNode Identifier(int i) {
			return getToken(PlpgsqlParser.Identifier, i);
		}
		public TerminalNode SET() { return getToken(PlpgsqlParser.SET, 0); }
		public AssignmentListContext assignmentList() {
			return getRuleContext(AssignmentListContext.class,0);
		}
		public TerminalNode SEMI() { return getToken(PlpgsqlParser.SEMI, 0); }
		public WithClauseContext withClause() {
			return getRuleContext(WithClauseContext.class,0);
		}
		public List<TerminalNode> DOT() { return getTokens(PlpgsqlParser.DOT); }
		public TerminalNode DOT(int i) {
			return getToken(PlpgsqlParser.DOT, i);
		}
		public TerminalNode FROM() { return getToken(PlpgsqlParser.FROM, 0); }
		public TableRefContext tableRef() {
			return getRuleContext(TableRefContext.class,0);
		}
		public WhereClauseContext whereClause() {
			return getRuleContext(WhereClauseContext.class,0);
		}
		public TerminalNode RETURNING() { return getToken(PlpgsqlParser.RETURNING, 0); }
		public ExpressionListContext expressionList() {
			return getRuleContext(ExpressionListContext.class,0);
		}
		public TerminalNode AS() { return getToken(PlpgsqlParser.AS, 0); }
		public List<JoinClauseContext> joinClause() {
			return getRuleContexts(JoinClauseContext.class);
		}
		public JoinClauseContext joinClause(int i) {
			return getRuleContext(JoinClauseContext.class,i);
		}
		public TerminalNode INTO() { return getToken(PlpgsqlParser.INTO, 0); }
		public VariableListContext variableList() {
			return getRuleContext(VariableListContext.class,0);
		}
		public UpdateStmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_updateStmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).enterUpdateStmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).exitUpdateStmt(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PlpgsqlParserVisitor ) return ((PlpgsqlParserVisitor<? extends T>)visitor).visitUpdateStmt(this);
			else return visitor.visitChildren(this);
		}
	}

	public final UpdateStmtContext updateStmt() throws RecognitionException {
		UpdateStmtContext _localctx = new UpdateStmtContext(_ctx, getState());
		enterRule(_localctx, 48, RULE_updateStmt);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(647);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==WITH) {
				{
				setState(646);
				withClause();
				}
			}

			setState(649);
			match(UPDATE);
			setState(650);
			match(Identifier);
			setState(655);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==DOT) {
				{
				{
				setState(651);
				match(DOT);
				setState(652);
				match(Identifier);
				}
				}
				setState(657);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(662);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==AS || _la==Identifier) {
				{
				setState(659);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==AS) {
					{
					setState(658);
					match(AS);
					}
				}

				setState(661);
				match(Identifier);
				}
			}

			setState(664);
			match(SET);
			setState(665);
			assignmentList();
			setState(674);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==FROM) {
				{
				setState(666);
				match(FROM);
				setState(667);
				tableRef();
				setState(671);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (((((_la - 109)) & ~0x3f) == 0 && ((1L << (_la - 109)) & 215L) != 0) || _la==COMMA) {
					{
					{
					setState(668);
					joinClause();
					}
					}
					setState(673);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
			}

			setState(677);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==WHERE) {
				{
				setState(676);
				whereClause();
				}
			}

			setState(685);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==RETURNING) {
				{
				setState(679);
				match(RETURNING);
				setState(680);
				expressionList();
				setState(683);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==INTO) {
					{
					setState(681);
					match(INTO);
					setState(682);
					variableList();
					}
				}

				}
			}

			setState(687);
			match(SEMI);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class AssignmentListContext extends ParserRuleContext {
		public List<ColumnAssignmentContext> columnAssignment() {
			return getRuleContexts(ColumnAssignmentContext.class);
		}
		public ColumnAssignmentContext columnAssignment(int i) {
			return getRuleContext(ColumnAssignmentContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(PlpgsqlParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(PlpgsqlParser.COMMA, i);
		}
		public AssignmentListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_assignmentList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).enterAssignmentList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).exitAssignmentList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PlpgsqlParserVisitor ) return ((PlpgsqlParserVisitor<? extends T>)visitor).visitAssignmentList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AssignmentListContext assignmentList() throws RecognitionException {
		AssignmentListContext _localctx = new AssignmentListContext(_ctx, getState());
		enterRule(_localctx, 50, RULE_assignmentList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(689);
			columnAssignment();
			setState(694);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(690);
				match(COMMA);
				setState(691);
				columnAssignment();
				}
				}
				setState(696);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ColumnAssignmentContext extends ParserRuleContext {
		public List<TerminalNode> Identifier() { return getTokens(PlpgsqlParser.Identifier); }
		public TerminalNode Identifier(int i) {
			return getToken(PlpgsqlParser.Identifier, i);
		}
		public TerminalNode EQ() { return getToken(PlpgsqlParser.EQ, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public List<TerminalNode> DOT() { return getTokens(PlpgsqlParser.DOT); }
		public TerminalNode DOT(int i) {
			return getToken(PlpgsqlParser.DOT, i);
		}
		public ColumnAssignmentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_columnAssignment; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).enterColumnAssignment(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).exitColumnAssignment(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PlpgsqlParserVisitor ) return ((PlpgsqlParserVisitor<? extends T>)visitor).visitColumnAssignment(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ColumnAssignmentContext columnAssignment() throws RecognitionException {
		ColumnAssignmentContext _localctx = new ColumnAssignmentContext(_ctx, getState());
		enterRule(_localctx, 52, RULE_columnAssignment);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(697);
			match(Identifier);
			setState(702);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==DOT) {
				{
				{
				setState(698);
				match(DOT);
				setState(699);
				match(Identifier);
				}
				}
				setState(704);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(705);
			match(EQ);
			setState(706);
			expression(0);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class DeleteStmtContext extends ParserRuleContext {
		public TerminalNode DELETE() { return getToken(PlpgsqlParser.DELETE, 0); }
		public TerminalNode FROM() { return getToken(PlpgsqlParser.FROM, 0); }
		public List<TerminalNode> Identifier() { return getTokens(PlpgsqlParser.Identifier); }
		public TerminalNode Identifier(int i) {
			return getToken(PlpgsqlParser.Identifier, i);
		}
		public TerminalNode SEMI() { return getToken(PlpgsqlParser.SEMI, 0); }
		public WithClauseContext withClause() {
			return getRuleContext(WithClauseContext.class,0);
		}
		public List<TerminalNode> DOT() { return getTokens(PlpgsqlParser.DOT); }
		public TerminalNode DOT(int i) {
			return getToken(PlpgsqlParser.DOT, i);
		}
		public TerminalNode USING() { return getToken(PlpgsqlParser.USING, 0); }
		public List<TableRefContext> tableRef() {
			return getRuleContexts(TableRefContext.class);
		}
		public TableRefContext tableRef(int i) {
			return getRuleContext(TableRefContext.class,i);
		}
		public WhereClauseContext whereClause() {
			return getRuleContext(WhereClauseContext.class,0);
		}
		public TerminalNode RETURNING() { return getToken(PlpgsqlParser.RETURNING, 0); }
		public ExpressionListContext expressionList() {
			return getRuleContext(ExpressionListContext.class,0);
		}
		public TerminalNode AS() { return getToken(PlpgsqlParser.AS, 0); }
		public List<TerminalNode> COMMA() { return getTokens(PlpgsqlParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(PlpgsqlParser.COMMA, i);
		}
		public TerminalNode INTO() { return getToken(PlpgsqlParser.INTO, 0); }
		public VariableListContext variableList() {
			return getRuleContext(VariableListContext.class,0);
		}
		public DeleteStmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_deleteStmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).enterDeleteStmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).exitDeleteStmt(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PlpgsqlParserVisitor ) return ((PlpgsqlParserVisitor<? extends T>)visitor).visitDeleteStmt(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DeleteStmtContext deleteStmt() throws RecognitionException {
		DeleteStmtContext _localctx = new DeleteStmtContext(_ctx, getState());
		enterRule(_localctx, 54, RULE_deleteStmt);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(709);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==WITH) {
				{
				setState(708);
				withClause();
				}
			}

			setState(711);
			match(DELETE);
			setState(712);
			match(FROM);
			setState(713);
			match(Identifier);
			setState(718);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==DOT) {
				{
				{
				setState(714);
				match(DOT);
				setState(715);
				match(Identifier);
				}
				}
				setState(720);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(725);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==AS || _la==Identifier) {
				{
				setState(722);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==AS) {
					{
					setState(721);
					match(AS);
					}
				}

				setState(724);
				match(Identifier);
				}
			}

			setState(736);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==USING) {
				{
				setState(727);
				match(USING);
				setState(728);
				tableRef();
				setState(733);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(729);
					match(COMMA);
					setState(730);
					tableRef();
					}
					}
					setState(735);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
			}

			setState(739);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==WHERE) {
				{
				setState(738);
				whereClause();
				}
			}

			setState(747);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==RETURNING) {
				{
				setState(741);
				match(RETURNING);
				setState(742);
				expressionList();
				setState(745);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==INTO) {
					{
					setState(743);
					match(INTO);
					setState(744);
					variableList();
					}
				}

				}
			}

			setState(749);
			match(SEMI);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class SetStmtContext extends ParserRuleContext {
		public TerminalNode SET() { return getToken(PlpgsqlParser.SET, 0); }
		public TerminalNode Identifier() { return getToken(PlpgsqlParser.Identifier, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode SEMI() { return getToken(PlpgsqlParser.SEMI, 0); }
		public TerminalNode TO() { return getToken(PlpgsqlParser.TO, 0); }
		public TerminalNode EQ() { return getToken(PlpgsqlParser.EQ, 0); }
		public TerminalNode FROM() { return getToken(PlpgsqlParser.FROM, 0); }
		public TerminalNode CURRENT() { return getToken(PlpgsqlParser.CURRENT, 0); }
		public SetStmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_setStmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).enterSetStmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).exitSetStmt(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PlpgsqlParserVisitor ) return ((PlpgsqlParserVisitor<? extends T>)visitor).visitSetStmt(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SetStmtContext setStmt() throws RecognitionException {
		SetStmtContext _localctx = new SetStmtContext(_ctx, getState());
		enterRule(_localctx, 56, RULE_setStmt);
		int _la;
		try {
			setState(762);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,98,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(751);
				match(SET);
				setState(752);
				match(Identifier);
				setState(753);
				_la = _input.LA(1);
				if ( !(_la==TO || _la==EQ) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(754);
				expression(0);
				setState(755);
				match(SEMI);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(757);
				match(SET);
				setState(758);
				match(Identifier);
				setState(759);
				match(FROM);
				setState(760);
				match(CURRENT);
				setState(761);
				match(SEMI);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class IfStmtContext extends ParserRuleContext {
		public List<TerminalNode> IF() { return getTokens(PlpgsqlParser.IF); }
		public TerminalNode IF(int i) {
			return getToken(PlpgsqlParser.IF, i);
		}
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public List<TerminalNode> THEN() { return getTokens(PlpgsqlParser.THEN); }
		public TerminalNode THEN(int i) {
			return getToken(PlpgsqlParser.THEN, i);
		}
		public List<StatementListContext> statementList() {
			return getRuleContexts(StatementListContext.class);
		}
		public StatementListContext statementList(int i) {
			return getRuleContext(StatementListContext.class,i);
		}
		public TerminalNode END() { return getToken(PlpgsqlParser.END, 0); }
		public TerminalNode SEMI() { return getToken(PlpgsqlParser.SEMI, 0); }
		public List<TerminalNode> ELSIF() { return getTokens(PlpgsqlParser.ELSIF); }
		public TerminalNode ELSIF(int i) {
			return getToken(PlpgsqlParser.ELSIF, i);
		}
		public TerminalNode ELSE() { return getToken(PlpgsqlParser.ELSE, 0); }
		public IfStmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_ifStmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).enterIfStmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).exitIfStmt(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PlpgsqlParserVisitor ) return ((PlpgsqlParserVisitor<? extends T>)visitor).visitIfStmt(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IfStmtContext ifStmt() throws RecognitionException {
		IfStmtContext _localctx = new IfStmtContext(_ctx, getState());
		enterRule(_localctx, 58, RULE_ifStmt);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(764);
			match(IF);
			setState(765);
			expression(0);
			setState(766);
			match(THEN);
			setState(767);
			statementList();
			setState(775);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==ELSIF) {
				{
				{
				setState(768);
				match(ELSIF);
				setState(769);
				expression(0);
				setState(770);
				match(THEN);
				setState(771);
				statementList();
				}
				}
				setState(777);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(780);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ELSE) {
				{
				setState(778);
				match(ELSE);
				setState(779);
				statementList();
				}
			}

			setState(782);
			match(END);
			setState(783);
			match(IF);
			setState(784);
			match(SEMI);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class CaseStmtContext extends ParserRuleContext {
		public List<TerminalNode> CASE() { return getTokens(PlpgsqlParser.CASE); }
		public TerminalNode CASE(int i) {
			return getToken(PlpgsqlParser.CASE, i);
		}
		public WhenClauseListContext whenClauseList() {
			return getRuleContext(WhenClauseListContext.class,0);
		}
		public TerminalNode END() { return getToken(PlpgsqlParser.END, 0); }
		public TerminalNode SEMI() { return getToken(PlpgsqlParser.SEMI, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode ELSE() { return getToken(PlpgsqlParser.ELSE, 0); }
		public StatementListContext statementList() {
			return getRuleContext(StatementListContext.class,0);
		}
		public CaseStmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_caseStmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).enterCaseStmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).exitCaseStmt(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PlpgsqlParserVisitor ) return ((PlpgsqlParserVisitor<? extends T>)visitor).visitCaseStmt(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CaseStmtContext caseStmt() throws RecognitionException {
		CaseStmtContext _localctx = new CaseStmtContext(_ctx, getState());
		enterRule(_localctx, 60, RULE_caseStmt);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(786);
			match(CASE);
			setState(788);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (((((_la - 9)) & ~0x3f) == 0 && ((1L << (_la - 9)) & -720575940379279359L) != 0) || ((((_la - 73)) & ~0x3f) == 0 && ((1L << (_la - 73)) & 6918655906817245185L) != 0) || ((((_la - 156)) & ~0x3f) == 0 && ((1L << (_la - 156)) & 34643206209633L) != 0)) {
				{
				setState(787);
				expression(0);
				}
			}

			setState(790);
			whenClauseList();
			setState(793);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ELSE) {
				{
				setState(791);
				match(ELSE);
				setState(792);
				statementList();
				}
			}

			setState(795);
			match(END);
			setState(796);
			match(CASE);
			setState(797);
			match(SEMI);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class WhenClauseListContext extends ParserRuleContext {
		public List<WhenClauseContext> whenClause() {
			return getRuleContexts(WhenClauseContext.class);
		}
		public WhenClauseContext whenClause(int i) {
			return getRuleContext(WhenClauseContext.class,i);
		}
		public WhenClauseListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_whenClauseList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).enterWhenClauseList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).exitWhenClauseList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PlpgsqlParserVisitor ) return ((PlpgsqlParserVisitor<? extends T>)visitor).visitWhenClauseList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final WhenClauseListContext whenClauseList() throws RecognitionException {
		WhenClauseListContext _localctx = new WhenClauseListContext(_ctx, getState());
		enterRule(_localctx, 62, RULE_whenClauseList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(800); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(799);
				whenClause();
				}
				}
				setState(802); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==WHEN );
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class WhenClauseContext extends ParserRuleContext {
		public TerminalNode WHEN() { return getToken(PlpgsqlParser.WHEN, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode THEN() { return getToken(PlpgsqlParser.THEN, 0); }
		public StatementListContext statementList() {
			return getRuleContext(StatementListContext.class,0);
		}
		public WhenClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_whenClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).enterWhenClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).exitWhenClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PlpgsqlParserVisitor ) return ((PlpgsqlParserVisitor<? extends T>)visitor).visitWhenClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final WhenClauseContext whenClause() throws RecognitionException {
		WhenClauseContext _localctx = new WhenClauseContext(_ctx, getState());
		enterRule(_localctx, 64, RULE_whenClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(804);
			match(WHEN);
			setState(805);
			expression(0);
			setState(806);
			match(THEN);
			setState(807);
			statementList();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class LoopStmtContext extends ParserRuleContext {
		public List<TerminalNode> LOOP() { return getTokens(PlpgsqlParser.LOOP); }
		public TerminalNode LOOP(int i) {
			return getToken(PlpgsqlParser.LOOP, i);
		}
		public StatementListContext statementList() {
			return getRuleContext(StatementListContext.class,0);
		}
		public TerminalNode END() { return getToken(PlpgsqlParser.END, 0); }
		public TerminalNode SEMI() { return getToken(PlpgsqlParser.SEMI, 0); }
		public List<LabelContext> label() {
			return getRuleContexts(LabelContext.class);
		}
		public LabelContext label(int i) {
			return getRuleContext(LabelContext.class,i);
		}
		public LoopStmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_loopStmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).enterLoopStmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).exitLoopStmt(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PlpgsqlParserVisitor ) return ((PlpgsqlParserVisitor<? extends T>)visitor).visitLoopStmt(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LoopStmtContext loopStmt() throws RecognitionException {
		LoopStmtContext _localctx = new LoopStmtContext(_ctx, getState());
		enterRule(_localctx, 66, RULE_loopStmt);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(810);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LABEL_START) {
				{
				setState(809);
				label();
				}
			}

			setState(812);
			match(LOOP);
			setState(813);
			statementList();
			setState(814);
			match(END);
			setState(815);
			match(LOOP);
			setState(817);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LABEL_START) {
				{
				setState(816);
				label();
				}
			}

			setState(819);
			match(SEMI);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class WhileStmtContext extends ParserRuleContext {
		public TerminalNode WHILE() { return getToken(PlpgsqlParser.WHILE, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public List<TerminalNode> LOOP() { return getTokens(PlpgsqlParser.LOOP); }
		public TerminalNode LOOP(int i) {
			return getToken(PlpgsqlParser.LOOP, i);
		}
		public StatementListContext statementList() {
			return getRuleContext(StatementListContext.class,0);
		}
		public TerminalNode END() { return getToken(PlpgsqlParser.END, 0); }
		public TerminalNode SEMI() { return getToken(PlpgsqlParser.SEMI, 0); }
		public List<LabelContext> label() {
			return getRuleContexts(LabelContext.class);
		}
		public LabelContext label(int i) {
			return getRuleContext(LabelContext.class,i);
		}
		public WhileStmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_whileStmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).enterWhileStmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).exitWhileStmt(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PlpgsqlParserVisitor ) return ((PlpgsqlParserVisitor<? extends T>)visitor).visitWhileStmt(this);
			else return visitor.visitChildren(this);
		}
	}

	public final WhileStmtContext whileStmt() throws RecognitionException {
		WhileStmtContext _localctx = new WhileStmtContext(_ctx, getState());
		enterRule(_localctx, 68, RULE_whileStmt);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(822);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LABEL_START) {
				{
				setState(821);
				label();
				}
			}

			setState(824);
			match(WHILE);
			setState(825);
			expression(0);
			setState(826);
			match(LOOP);
			setState(827);
			statementList();
			setState(828);
			match(END);
			setState(829);
			match(LOOP);
			setState(831);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LABEL_START) {
				{
				setState(830);
				label();
				}
			}

			setState(833);
			match(SEMI);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ForStmtContext extends ParserRuleContext {
		public TerminalNode FOR() { return getToken(PlpgsqlParser.FOR, 0); }
		public TerminalNode Identifier() { return getToken(PlpgsqlParser.Identifier, 0); }
		public TerminalNode IN() { return getToken(PlpgsqlParser.IN, 0); }
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public List<TerminalNode> DOT() { return getTokens(PlpgsqlParser.DOT); }
		public TerminalNode DOT(int i) {
			return getToken(PlpgsqlParser.DOT, i);
		}
		public List<TerminalNode> LOOP() { return getTokens(PlpgsqlParser.LOOP); }
		public TerminalNode LOOP(int i) {
			return getToken(PlpgsqlParser.LOOP, i);
		}
		public StatementListContext statementList() {
			return getRuleContext(StatementListContext.class,0);
		}
		public TerminalNode END() { return getToken(PlpgsqlParser.END, 0); }
		public TerminalNode SEMI() { return getToken(PlpgsqlParser.SEMI, 0); }
		public List<LabelContext> label() {
			return getRuleContexts(LabelContext.class);
		}
		public LabelContext label(int i) {
			return getRuleContext(LabelContext.class,i);
		}
		public TerminalNode REVERSE() { return getToken(PlpgsqlParser.REVERSE, 0); }
		public TerminalNode BY() { return getToken(PlpgsqlParser.BY, 0); }
		public ForQuerySourceContext forQuerySource() {
			return getRuleContext(ForQuerySourceContext.class,0);
		}
		public ForStmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_forStmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).enterForStmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).exitForStmt(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PlpgsqlParserVisitor ) return ((PlpgsqlParserVisitor<? extends T>)visitor).visitForStmt(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ForStmtContext forStmt() throws RecognitionException {
		ForStmtContext _localctx = new ForStmtContext(_ctx, getState());
		enterRule(_localctx, 70, RULE_forStmt);
		int _la;
		try {
			setState(877);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,114,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(836);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LABEL_START) {
					{
					setState(835);
					label();
					}
				}

				setState(838);
				match(FOR);
				setState(839);
				match(Identifier);
				setState(840);
				match(IN);
				setState(842);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==REVERSE) {
					{
					setState(841);
					match(REVERSE);
					}
				}

				setState(844);
				expression(0);
				setState(845);
				match(DOT);
				setState(846);
				match(DOT);
				setState(847);
				expression(0);
				setState(850);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==BY) {
					{
					setState(848);
					match(BY);
					setState(849);
					expression(0);
					}
				}

				setState(852);
				match(LOOP);
				setState(853);
				statementList();
				setState(854);
				match(END);
				setState(855);
				match(LOOP);
				setState(857);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LABEL_START) {
					{
					setState(856);
					label();
					}
				}

				setState(859);
				match(SEMI);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(862);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LABEL_START) {
					{
					setState(861);
					label();
					}
				}

				setState(864);
				match(FOR);
				setState(865);
				match(Identifier);
				setState(866);
				match(IN);
				setState(867);
				forQuerySource();
				setState(868);
				match(LOOP);
				setState(869);
				statementList();
				setState(870);
				match(END);
				setState(871);
				match(LOOP);
				setState(873);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LABEL_START) {
					{
					setState(872);
					label();
					}
				}

				setState(875);
				match(SEMI);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ForQuerySourceContext extends ParserRuleContext {
		public TerminalNode EXECUTE() { return getToken(PlpgsqlParser.EXECUTE, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode SELECT() { return getToken(PlpgsqlParser.SELECT, 0); }
		public SelectListContext selectList() {
			return getRuleContext(SelectListContext.class,0);
		}
		public FromClauseContext fromClause() {
			return getRuleContext(FromClauseContext.class,0);
		}
		public WhereClauseContext whereClause() {
			return getRuleContext(WhereClauseContext.class,0);
		}
		public GroupByClauseContext groupByClause() {
			return getRuleContext(GroupByClauseContext.class,0);
		}
		public HavingClauseContext havingClause() {
			return getRuleContext(HavingClauseContext.class,0);
		}
		public OrderByClauseContext orderByClause() {
			return getRuleContext(OrderByClauseContext.class,0);
		}
		public LimitClauseContext limitClause() {
			return getRuleContext(LimitClauseContext.class,0);
		}
		public ForQuerySourceContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_forQuerySource; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).enterForQuerySource(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).exitForQuerySource(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PlpgsqlParserVisitor ) return ((PlpgsqlParserVisitor<? extends T>)visitor).visitForQuerySource(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ForQuerySourceContext forQuerySource() throws RecognitionException {
		ForQuerySourceContext _localctx = new ForQuerySourceContext(_ctx, getState());
		enterRule(_localctx, 72, RULE_forQuerySource);
		int _la;
		try {
			setState(902);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case EXECUTE:
				enterOuterAlt(_localctx, 1);
				{
				setState(879);
				match(EXECUTE);
				setState(880);
				expression(0);
				}
				break;
			case SELECT:
				enterOuterAlt(_localctx, 2);
				{
				setState(881);
				match(SELECT);
				setState(882);
				selectList();
				setState(884);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==FROM) {
					{
					setState(883);
					fromClause();
					}
				}

				setState(887);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==WHERE) {
					{
					setState(886);
					whereClause();
					}
				}

				setState(890);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==GROUP) {
					{
					setState(889);
					groupByClause();
					}
				}

				setState(893);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==HAVING) {
					{
					setState(892);
					havingClause();
					}
				}

				setState(896);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ORDER) {
					{
					setState(895);
					orderByClause();
					}
				}

				setState(899);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LIMIT) {
					{
					setState(898);
					limitClause();
					}
				}

				}
				break;
			case CASE:
			case EXISTS:
			case CAST:
			case COALESCE:
			case NULLIF:
			case GREATEST:
			case LEAST:
			case POSITION:
			case INTERVAL:
			case ARRAY:
			case LEFT:
			case RIGHT:
			case SUBSTRING:
			case SQLSTATE:
			case NULL:
			case NOT:
			case FOUND:
			case TRUE:
			case FALSE:
			case LPAREN:
			case IntegerLiteral:
			case NumericLiteral:
			case StringLiteral:
			case DollarQuotedString:
			case Identifier:
			case QuotedIdentifier:
				enterOuterAlt(_localctx, 3);
				{
				setState(901);
				expression(0);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ForeachStmtContext extends ParserRuleContext {
		public TerminalNode FOREACH() { return getToken(PlpgsqlParser.FOREACH, 0); }
		public TerminalNode Identifier() { return getToken(PlpgsqlParser.Identifier, 0); }
		public TerminalNode IN() { return getToken(PlpgsqlParser.IN, 0); }
		public TerminalNode ARRAY() { return getToken(PlpgsqlParser.ARRAY, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public List<TerminalNode> LOOP() { return getTokens(PlpgsqlParser.LOOP); }
		public TerminalNode LOOP(int i) {
			return getToken(PlpgsqlParser.LOOP, i);
		}
		public StatementListContext statementList() {
			return getRuleContext(StatementListContext.class,0);
		}
		public TerminalNode END() { return getToken(PlpgsqlParser.END, 0); }
		public TerminalNode SEMI() { return getToken(PlpgsqlParser.SEMI, 0); }
		public List<LabelContext> label() {
			return getRuleContexts(LabelContext.class);
		}
		public LabelContext label(int i) {
			return getRuleContext(LabelContext.class,i);
		}
		public TerminalNode SLICE() { return getToken(PlpgsqlParser.SLICE, 0); }
		public TerminalNode IntegerLiteral() { return getToken(PlpgsqlParser.IntegerLiteral, 0); }
		public ForeachStmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_foreachStmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).enterForeachStmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).exitForeachStmt(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PlpgsqlParserVisitor ) return ((PlpgsqlParserVisitor<? extends T>)visitor).visitForeachStmt(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ForeachStmtContext foreachStmt() throws RecognitionException {
		ForeachStmtContext _localctx = new ForeachStmtContext(_ctx, getState());
		enterRule(_localctx, 74, RULE_foreachStmt);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(905);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LABEL_START) {
				{
				setState(904);
				label();
				}
			}

			setState(907);
			match(FOREACH);
			setState(908);
			match(Identifier);
			setState(911);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==SLICE) {
				{
				setState(909);
				match(SLICE);
				setState(910);
				match(IntegerLiteral);
				}
			}

			setState(913);
			match(IN);
			setState(914);
			match(ARRAY);
			setState(915);
			expression(0);
			setState(916);
			match(LOOP);
			setState(917);
			statementList();
			setState(918);
			match(END);
			setState(919);
			match(LOOP);
			setState(921);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LABEL_START) {
				{
				setState(920);
				label();
				}
			}

			setState(923);
			match(SEMI);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ExitStmtContext extends ParserRuleContext {
		public TerminalNode EXIT() { return getToken(PlpgsqlParser.EXIT, 0); }
		public TerminalNode SEMI() { return getToken(PlpgsqlParser.SEMI, 0); }
		public TerminalNode Identifier() { return getToken(PlpgsqlParser.Identifier, 0); }
		public TerminalNode WHEN() { return getToken(PlpgsqlParser.WHEN, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public ExitStmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_exitStmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).enterExitStmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).exitExitStmt(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PlpgsqlParserVisitor ) return ((PlpgsqlParserVisitor<? extends T>)visitor).visitExitStmt(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExitStmtContext exitStmt() throws RecognitionException {
		ExitStmtContext _localctx = new ExitStmtContext(_ctx, getState());
		enterRule(_localctx, 76, RULE_exitStmt);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(925);
			match(EXIT);
			setState(927);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==Identifier) {
				{
				setState(926);
				match(Identifier);
				}
			}

			setState(931);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==WHEN) {
				{
				setState(929);
				match(WHEN);
				setState(930);
				expression(0);
				}
			}

			setState(933);
			match(SEMI);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ContinueStmtContext extends ParserRuleContext {
		public TerminalNode CONTINUE() { return getToken(PlpgsqlParser.CONTINUE, 0); }
		public TerminalNode SEMI() { return getToken(PlpgsqlParser.SEMI, 0); }
		public TerminalNode Identifier() { return getToken(PlpgsqlParser.Identifier, 0); }
		public TerminalNode WHEN() { return getToken(PlpgsqlParser.WHEN, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public ContinueStmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_continueStmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).enterContinueStmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).exitContinueStmt(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PlpgsqlParserVisitor ) return ((PlpgsqlParserVisitor<? extends T>)visitor).visitContinueStmt(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ContinueStmtContext continueStmt() throws RecognitionException {
		ContinueStmtContext _localctx = new ContinueStmtContext(_ctx, getState());
		enterRule(_localctx, 78, RULE_continueStmt);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(935);
			match(CONTINUE);
			setState(937);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==Identifier) {
				{
				setState(936);
				match(Identifier);
				}
			}

			setState(941);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==WHEN) {
				{
				setState(939);
				match(WHEN);
				setState(940);
				expression(0);
				}
			}

			setState(943);
			match(SEMI);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ReturnStmtContext extends ParserRuleContext {
		public TerminalNode RETURN() { return getToken(PlpgsqlParser.RETURN, 0); }
		public TerminalNode SEMI() { return getToken(PlpgsqlParser.SEMI, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode NEXT() { return getToken(PlpgsqlParser.NEXT, 0); }
		public TerminalNode QUERY() { return getToken(PlpgsqlParser.QUERY, 0); }
		public SelectStmtContext selectStmt() {
			return getRuleContext(SelectStmtContext.class,0);
		}
		public TerminalNode EXECUTE() { return getToken(PlpgsqlParser.EXECUTE, 0); }
		public TerminalNode USING() { return getToken(PlpgsqlParser.USING, 0); }
		public ExpressionListContext expressionList() {
			return getRuleContext(ExpressionListContext.class,0);
		}
		public ReturnStmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_returnStmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).enterReturnStmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).exitReturnStmt(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PlpgsqlParserVisitor ) return ((PlpgsqlParserVisitor<? extends T>)visitor).visitReturnStmt(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ReturnStmtContext returnStmt() throws RecognitionException {
		ReturnStmtContext _localctx = new ReturnStmtContext(_ctx, getState());
		enterRule(_localctx, 80, RULE_returnStmt);
		int _la;
		try {
			setState(970);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,131,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(945);
				match(RETURN);
				setState(947);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (((((_la - 9)) & ~0x3f) == 0 && ((1L << (_la - 9)) & -720575940379279359L) != 0) || ((((_la - 73)) & ~0x3f) == 0 && ((1L << (_la - 73)) & 6918655906817245185L) != 0) || ((((_la - 156)) & ~0x3f) == 0 && ((1L << (_la - 156)) & 34643206209633L) != 0)) {
					{
					setState(946);
					expression(0);
					}
				}

				setState(949);
				match(SEMI);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(950);
				match(RETURN);
				setState(951);
				match(NEXT);
				setState(952);
				expression(0);
				setState(953);
				match(SEMI);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(955);
				match(RETURN);
				setState(956);
				match(QUERY);
				setState(957);
				selectStmt();
				setState(958);
				match(SEMI);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(960);
				match(RETURN);
				setState(961);
				match(QUERY);
				setState(962);
				match(EXECUTE);
				setState(963);
				expression(0);
				setState(966);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==USING) {
					{
					setState(964);
					match(USING);
					setState(965);
					expressionList();
					}
				}

				setState(968);
				match(SEMI);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class RaiseStmtContext extends ParserRuleContext {
		public TerminalNode RAISE() { return getToken(PlpgsqlParser.RAISE, 0); }
		public TerminalNode SEMI() { return getToken(PlpgsqlParser.SEMI, 0); }
		public TerminalNode StringLiteral() { return getToken(PlpgsqlParser.StringLiteral, 0); }
		public TerminalNode Identifier() { return getToken(PlpgsqlParser.Identifier, 0); }
		public TerminalNode USING() { return getToken(PlpgsqlParser.USING, 0); }
		public RaiseOptionListContext raiseOptionList() {
			return getRuleContext(RaiseOptionListContext.class,0);
		}
		public TerminalNode NOTICE() { return getToken(PlpgsqlParser.NOTICE, 0); }
		public TerminalNode WARNING() { return getToken(PlpgsqlParser.WARNING, 0); }
		public TerminalNode INFO() { return getToken(PlpgsqlParser.INFO, 0); }
		public TerminalNode DEBUG() { return getToken(PlpgsqlParser.DEBUG, 0); }
		public TerminalNode LOG() { return getToken(PlpgsqlParser.LOG, 0); }
		public TerminalNode EXCEPTION() { return getToken(PlpgsqlParser.EXCEPTION, 0); }
		public TerminalNode COMMA() { return getToken(PlpgsqlParser.COMMA, 0); }
		public ExpressionListContext expressionList() {
			return getRuleContext(ExpressionListContext.class,0);
		}
		public RaiseStmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_raiseStmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).enterRaiseStmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).exitRaiseStmt(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PlpgsqlParserVisitor ) return ((PlpgsqlParserVisitor<? extends T>)visitor).visitRaiseStmt(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RaiseStmtContext raiseStmt() throws RecognitionException {
		RaiseStmtContext _localctx = new RaiseStmtContext(_ctx, getState());
		enterRule(_localctx, 82, RULE_raiseStmt);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(972);
			match(RAISE);
			setState(974);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==EXCEPTION || ((((_la - 118)) & ~0x3f) == 0 && ((1L << (_la - 118)) & 31L) != 0)) {
				{
				setState(973);
				_la = _input.LA(1);
				if ( !(_la==EXCEPTION || ((((_la - 118)) & ~0x3f) == 0 && ((1L << (_la - 118)) & 31L) != 0)) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
			}

			setState(982);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case StringLiteral:
				{
				setState(976);
				match(StringLiteral);
				setState(979);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMA) {
					{
					setState(977);
					match(COMMA);
					setState(978);
					expressionList();
					}
				}

				}
				break;
			case Identifier:
				{
				setState(981);
				match(Identifier);
				}
				break;
			case USING:
			case SEMI:
				break;
			default:
				break;
			}
			setState(986);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==USING) {
				{
				setState(984);
				match(USING);
				setState(985);
				raiseOptionList();
				}
			}

			setState(988);
			match(SEMI);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class RaiseOptionListContext extends ParserRuleContext {
		public List<RaiseOptionContext> raiseOption() {
			return getRuleContexts(RaiseOptionContext.class);
		}
		public RaiseOptionContext raiseOption(int i) {
			return getRuleContext(RaiseOptionContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(PlpgsqlParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(PlpgsqlParser.COMMA, i);
		}
		public RaiseOptionListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_raiseOptionList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).enterRaiseOptionList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).exitRaiseOptionList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PlpgsqlParserVisitor ) return ((PlpgsqlParserVisitor<? extends T>)visitor).visitRaiseOptionList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RaiseOptionListContext raiseOptionList() throws RecognitionException {
		RaiseOptionListContext _localctx = new RaiseOptionListContext(_ctx, getState());
		enterRule(_localctx, 84, RULE_raiseOptionList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(990);
			raiseOption();
			setState(995);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(991);
				match(COMMA);
				setState(992);
				raiseOption();
				}
				}
				setState(997);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class RaiseOptionContext extends ParserRuleContext {
		public TerminalNode EQ() { return getToken(PlpgsqlParser.EQ, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode MESSAGE() { return getToken(PlpgsqlParser.MESSAGE, 0); }
		public TerminalNode DETAIL() { return getToken(PlpgsqlParser.DETAIL, 0); }
		public TerminalNode HINT() { return getToken(PlpgsqlParser.HINT, 0); }
		public TerminalNode ERRCODE() { return getToken(PlpgsqlParser.ERRCODE, 0); }
		public TerminalNode SQLSTATE() { return getToken(PlpgsqlParser.SQLSTATE, 0); }
		public RaiseOptionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_raiseOption; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).enterRaiseOption(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).exitRaiseOption(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PlpgsqlParserVisitor ) return ((PlpgsqlParserVisitor<? extends T>)visitor).visitRaiseOption(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RaiseOptionContext raiseOption() throws RecognitionException {
		RaiseOptionContext _localctx = new RaiseOptionContext(_ctx, getState());
		enterRule(_localctx, 86, RULE_raiseOption);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(998);
			_la = _input.LA(1);
			if ( !(((((_la - 123)) & ~0x3f) == 0 && ((1L << (_la - 123)) & 31L) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(999);
			match(EQ);
			setState(1000);
			expression(0);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class AssertStmtContext extends ParserRuleContext {
		public TerminalNode ASSERT() { return getToken(PlpgsqlParser.ASSERT, 0); }
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode SEMI() { return getToken(PlpgsqlParser.SEMI, 0); }
		public TerminalNode COMMA() { return getToken(PlpgsqlParser.COMMA, 0); }
		public AssertStmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_assertStmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).enterAssertStmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).exitAssertStmt(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PlpgsqlParserVisitor ) return ((PlpgsqlParserVisitor<? extends T>)visitor).visitAssertStmt(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AssertStmtContext assertStmt() throws RecognitionException {
		AssertStmtContext _localctx = new AssertStmtContext(_ctx, getState());
		enterRule(_localctx, 88, RULE_assertStmt);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1002);
			match(ASSERT);
			setState(1003);
			expression(0);
			setState(1006);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COMMA) {
				{
				setState(1004);
				match(COMMA);
				setState(1005);
				expression(0);
				}
			}

			setState(1008);
			match(SEMI);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class NullStmtContext extends ParserRuleContext {
		public TerminalNode NULL() { return getToken(PlpgsqlParser.NULL, 0); }
		public TerminalNode SEMI() { return getToken(PlpgsqlParser.SEMI, 0); }
		public NullStmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_nullStmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).enterNullStmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).exitNullStmt(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PlpgsqlParserVisitor ) return ((PlpgsqlParserVisitor<? extends T>)visitor).visitNullStmt(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NullStmtContext nullStmt() throws RecognitionException {
		NullStmtContext _localctx = new NullStmtContext(_ctx, getState());
		enterRule(_localctx, 90, RULE_nullStmt);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1010);
			match(NULL);
			setState(1011);
			match(SEMI);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class CommitStmtContext extends ParserRuleContext {
		public TerminalNode COMMIT() { return getToken(PlpgsqlParser.COMMIT, 0); }
		public TerminalNode SEMI() { return getToken(PlpgsqlParser.SEMI, 0); }
		public TerminalNode WORK() { return getToken(PlpgsqlParser.WORK, 0); }
		public TerminalNode TRANSACTION() { return getToken(PlpgsqlParser.TRANSACTION, 0); }
		public CommitStmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_commitStmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).enterCommitStmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).exitCommitStmt(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PlpgsqlParserVisitor ) return ((PlpgsqlParserVisitor<? extends T>)visitor).visitCommitStmt(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CommitStmtContext commitStmt() throws RecognitionException {
		CommitStmtContext _localctx = new CommitStmtContext(_ctx, getState());
		enterRule(_localctx, 92, RULE_commitStmt);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1013);
			match(COMMIT);
			setState(1015);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==TRANSACTION || _la==WORK) {
				{
				setState(1014);
				_la = _input.LA(1);
				if ( !(_la==TRANSACTION || _la==WORK) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
			}

			setState(1017);
			match(SEMI);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class RollbackStmtContext extends ParserRuleContext {
		public TerminalNode ROLLBACK() { return getToken(PlpgsqlParser.ROLLBACK, 0); }
		public TerminalNode SEMI() { return getToken(PlpgsqlParser.SEMI, 0); }
		public TerminalNode TO() { return getToken(PlpgsqlParser.TO, 0); }
		public TerminalNode Identifier() { return getToken(PlpgsqlParser.Identifier, 0); }
		public TerminalNode WORK() { return getToken(PlpgsqlParser.WORK, 0); }
		public TerminalNode TRANSACTION() { return getToken(PlpgsqlParser.TRANSACTION, 0); }
		public TerminalNode SAVEPOINT() { return getToken(PlpgsqlParser.SAVEPOINT, 0); }
		public RollbackStmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_rollbackStmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).enterRollbackStmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).exitRollbackStmt(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PlpgsqlParserVisitor ) return ((PlpgsqlParserVisitor<? extends T>)visitor).visitRollbackStmt(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RollbackStmtContext rollbackStmt() throws RecognitionException {
		RollbackStmtContext _localctx = new RollbackStmtContext(_ctx, getState());
		enterRule(_localctx, 94, RULE_rollbackStmt);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1019);
			match(ROLLBACK);
			setState(1021);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==TRANSACTION || _la==WORK) {
				{
				setState(1020);
				_la = _input.LA(1);
				if ( !(_la==TRANSACTION || _la==WORK) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
			}

			setState(1028);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==TO) {
				{
				setState(1023);
				match(TO);
				setState(1025);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==SAVEPOINT) {
					{
					setState(1024);
					match(SAVEPOINT);
					}
				}

				setState(1027);
				match(Identifier);
				}
			}

			setState(1030);
			match(SEMI);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class NestedBlockContext extends ParserRuleContext {
		public TerminalNode BEGIN() { return getToken(PlpgsqlParser.BEGIN, 0); }
		public StatementListContext statementList() {
			return getRuleContext(StatementListContext.class,0);
		}
		public TerminalNode END() { return getToken(PlpgsqlParser.END, 0); }
		public TerminalNode SEMI() { return getToken(PlpgsqlParser.SEMI, 0); }
		public List<LabelContext> label() {
			return getRuleContexts(LabelContext.class);
		}
		public LabelContext label(int i) {
			return getRuleContext(LabelContext.class,i);
		}
		public TerminalNode DECLARE() { return getToken(PlpgsqlParser.DECLARE, 0); }
		public DeclarationListContext declarationList() {
			return getRuleContext(DeclarationListContext.class,0);
		}
		public ExceptionSectionContext exceptionSection() {
			return getRuleContext(ExceptionSectionContext.class,0);
		}
		public NestedBlockContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_nestedBlock; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).enterNestedBlock(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).exitNestedBlock(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PlpgsqlParserVisitor ) return ((PlpgsqlParserVisitor<? extends T>)visitor).visitNestedBlock(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NestedBlockContext nestedBlock() throws RecognitionException {
		NestedBlockContext _localctx = new NestedBlockContext(_ctx, getState());
		enterRule(_localctx, 96, RULE_nestedBlock);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1033);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LABEL_START) {
				{
				setState(1032);
				label();
				}
			}

			setState(1036);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==DECLARE) {
				{
				setState(1035);
				match(DECLARE);
				}
			}

			setState(1039);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==Identifier) {
				{
				setState(1038);
				declarationList();
				}
			}

			setState(1041);
			match(BEGIN);
			setState(1042);
			statementList();
			setState(1044);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==EXCEPTION) {
				{
				setState(1043);
				exceptionSection();
				}
			}

			setState(1046);
			match(END);
			setState(1048);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LABEL_START) {
				{
				setState(1047);
				label();
				}
			}

			setState(1050);
			match(SEMI);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ExceptionSectionContext extends ParserRuleContext {
		public TerminalNode EXCEPTION() { return getToken(PlpgsqlParser.EXCEPTION, 0); }
		public ExceptionHandlerListContext exceptionHandlerList() {
			return getRuleContext(ExceptionHandlerListContext.class,0);
		}
		public ExceptionSectionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_exceptionSection; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).enterExceptionSection(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).exitExceptionSection(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PlpgsqlParserVisitor ) return ((PlpgsqlParserVisitor<? extends T>)visitor).visitExceptionSection(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExceptionSectionContext exceptionSection() throws RecognitionException {
		ExceptionSectionContext _localctx = new ExceptionSectionContext(_ctx, getState());
		enterRule(_localctx, 98, RULE_exceptionSection);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1052);
			match(EXCEPTION);
			setState(1053);
			exceptionHandlerList();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ExceptionHandlerListContext extends ParserRuleContext {
		public List<ExceptionHandlerContext> exceptionHandler() {
			return getRuleContexts(ExceptionHandlerContext.class);
		}
		public ExceptionHandlerContext exceptionHandler(int i) {
			return getRuleContext(ExceptionHandlerContext.class,i);
		}
		public ExceptionHandlerListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_exceptionHandlerList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).enterExceptionHandlerList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).exitExceptionHandlerList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PlpgsqlParserVisitor ) return ((PlpgsqlParserVisitor<? extends T>)visitor).visitExceptionHandlerList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExceptionHandlerListContext exceptionHandlerList() throws RecognitionException {
		ExceptionHandlerListContext _localctx = new ExceptionHandlerListContext(_ctx, getState());
		enterRule(_localctx, 100, RULE_exceptionHandlerList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1056); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(1055);
				exceptionHandler();
				}
				}
				setState(1058); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==WHEN );
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ExceptionHandlerContext extends ParserRuleContext {
		public TerminalNode WHEN() { return getToken(PlpgsqlParser.WHEN, 0); }
		public ExceptionConditionListContext exceptionConditionList() {
			return getRuleContext(ExceptionConditionListContext.class,0);
		}
		public TerminalNode THEN() { return getToken(PlpgsqlParser.THEN, 0); }
		public StatementListContext statementList() {
			return getRuleContext(StatementListContext.class,0);
		}
		public ExceptionHandlerContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_exceptionHandler; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).enterExceptionHandler(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).exitExceptionHandler(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PlpgsqlParserVisitor ) return ((PlpgsqlParserVisitor<? extends T>)visitor).visitExceptionHandler(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExceptionHandlerContext exceptionHandler() throws RecognitionException {
		ExceptionHandlerContext _localctx = new ExceptionHandlerContext(_ctx, getState());
		enterRule(_localctx, 102, RULE_exceptionHandler);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1060);
			match(WHEN);
			setState(1061);
			exceptionConditionList();
			setState(1062);
			match(THEN);
			setState(1063);
			statementList();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ExceptionConditionListContext extends ParserRuleContext {
		public List<ExceptionConditionContext> exceptionCondition() {
			return getRuleContexts(ExceptionConditionContext.class);
		}
		public ExceptionConditionContext exceptionCondition(int i) {
			return getRuleContext(ExceptionConditionContext.class,i);
		}
		public List<TerminalNode> OR() { return getTokens(PlpgsqlParser.OR); }
		public TerminalNode OR(int i) {
			return getToken(PlpgsqlParser.OR, i);
		}
		public ExceptionConditionListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_exceptionConditionList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).enterExceptionConditionList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).exitExceptionConditionList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PlpgsqlParserVisitor ) return ((PlpgsqlParserVisitor<? extends T>)visitor).visitExceptionConditionList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExceptionConditionListContext exceptionConditionList() throws RecognitionException {
		ExceptionConditionListContext _localctx = new ExceptionConditionListContext(_ctx, getState());
		enterRule(_localctx, 104, RULE_exceptionConditionList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1065);
			exceptionCondition();
			setState(1070);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==OR) {
				{
				{
				setState(1066);
				match(OR);
				setState(1067);
				exceptionCondition();
				}
				}
				setState(1072);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ExceptionConditionContext extends ParserRuleContext {
		public TerminalNode Identifier() { return getToken(PlpgsqlParser.Identifier, 0); }
		public TerminalNode SQLSTATE() { return getToken(PlpgsqlParser.SQLSTATE, 0); }
		public TerminalNode StringLiteral() { return getToken(PlpgsqlParser.StringLiteral, 0); }
		public ExceptionConditionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_exceptionCondition; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).enterExceptionCondition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).exitExceptionCondition(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PlpgsqlParserVisitor ) return ((PlpgsqlParserVisitor<? extends T>)visitor).visitExceptionCondition(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExceptionConditionContext exceptionCondition() throws RecognitionException {
		ExceptionConditionContext _localctx = new ExceptionConditionContext(_ctx, getState());
		enterRule(_localctx, 106, RULE_exceptionCondition);
		try {
			setState(1076);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case Identifier:
				enterOuterAlt(_localctx, 1);
				{
				setState(1073);
				match(Identifier);
				}
				break;
			case SQLSTATE:
				enterOuterAlt(_localctx, 2);
				{
				setState(1074);
				match(SQLSTATE);
				setState(1075);
				match(StringLiteral);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class GetDiagnosticsStmtContext extends ParserRuleContext {
		public TerminalNode GET() { return getToken(PlpgsqlParser.GET, 0); }
		public TerminalNode DIAGNOSTICS() { return getToken(PlpgsqlParser.DIAGNOSTICS, 0); }
		public DiagnosticsItemListContext diagnosticsItemList() {
			return getRuleContext(DiagnosticsItemListContext.class,0);
		}
		public TerminalNode SEMI() { return getToken(PlpgsqlParser.SEMI, 0); }
		public TerminalNode CURRENT() { return getToken(PlpgsqlParser.CURRENT, 0); }
		public TerminalNode STACKED() { return getToken(PlpgsqlParser.STACKED, 0); }
		public GetDiagnosticsStmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_getDiagnosticsStmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).enterGetDiagnosticsStmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).exitGetDiagnosticsStmt(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PlpgsqlParserVisitor ) return ((PlpgsqlParserVisitor<? extends T>)visitor).visitGetDiagnosticsStmt(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GetDiagnosticsStmtContext getDiagnosticsStmt() throws RecognitionException {
		GetDiagnosticsStmtContext _localctx = new GetDiagnosticsStmtContext(_ctx, getState());
		enterRule(_localctx, 108, RULE_getDiagnosticsStmt);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1078);
			match(GET);
			setState(1080);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==STACKED || _la==CURRENT) {
				{
				setState(1079);
				_la = _input.LA(1);
				if ( !(_la==STACKED || _la==CURRENT) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
			}

			setState(1082);
			match(DIAGNOSTICS);
			setState(1083);
			diagnosticsItemList();
			setState(1084);
			match(SEMI);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class DiagnosticsItemListContext extends ParserRuleContext {
		public List<DiagnosticsItemContext> diagnosticsItem() {
			return getRuleContexts(DiagnosticsItemContext.class);
		}
		public DiagnosticsItemContext diagnosticsItem(int i) {
			return getRuleContext(DiagnosticsItemContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(PlpgsqlParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(PlpgsqlParser.COMMA, i);
		}
		public DiagnosticsItemListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_diagnosticsItemList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).enterDiagnosticsItemList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).exitDiagnosticsItemList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PlpgsqlParserVisitor ) return ((PlpgsqlParserVisitor<? extends T>)visitor).visitDiagnosticsItemList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DiagnosticsItemListContext diagnosticsItemList() throws RecognitionException {
		DiagnosticsItemListContext _localctx = new DiagnosticsItemListContext(_ctx, getState());
		enterRule(_localctx, 110, RULE_diagnosticsItemList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1086);
			diagnosticsItem();
			setState(1091);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(1087);
				match(COMMA);
				setState(1088);
				diagnosticsItem();
				}
				}
				setState(1093);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class DiagnosticsItemContext extends ParserRuleContext {
		public VariableRefContext variableRef() {
			return getRuleContext(VariableRefContext.class,0);
		}
		public TerminalNode EQ() { return getToken(PlpgsqlParser.EQ, 0); }
		public DiagnosticsOptionContext diagnosticsOption() {
			return getRuleContext(DiagnosticsOptionContext.class,0);
		}
		public DiagnosticsItemContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_diagnosticsItem; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).enterDiagnosticsItem(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).exitDiagnosticsItem(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PlpgsqlParserVisitor ) return ((PlpgsqlParserVisitor<? extends T>)visitor).visitDiagnosticsItem(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DiagnosticsItemContext diagnosticsItem() throws RecognitionException {
		DiagnosticsItemContext _localctx = new DiagnosticsItemContext(_ctx, getState());
		enterRule(_localctx, 112, RULE_diagnosticsItem);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1094);
			variableRef();
			setState(1095);
			match(EQ);
			setState(1096);
			diagnosticsOption();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class DiagnosticsOptionContext extends ParserRuleContext {
		public TerminalNode Identifier() { return getToken(PlpgsqlParser.Identifier, 0); }
		public DiagnosticsOptionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_diagnosticsOption; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).enterDiagnosticsOption(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).exitDiagnosticsOption(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PlpgsqlParserVisitor ) return ((PlpgsqlParserVisitor<? extends T>)visitor).visitDiagnosticsOption(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DiagnosticsOptionContext diagnosticsOption() throws RecognitionException {
		DiagnosticsOptionContext _localctx = new DiagnosticsOptionContext(_ctx, getState());
		enterRule(_localctx, 114, RULE_diagnosticsOption);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1098);
			match(Identifier);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class OpenCursorStmtContext extends ParserRuleContext {
		public TerminalNode OPEN() { return getToken(PlpgsqlParser.OPEN, 0); }
		public VariableRefContext variableRef() {
			return getRuleContext(VariableRefContext.class,0);
		}
		public TerminalNode SEMI() { return getToken(PlpgsqlParser.SEMI, 0); }
		public TerminalNode LPAREN() { return getToken(PlpgsqlParser.LPAREN, 0); }
		public ExpressionListContext expressionList() {
			return getRuleContext(ExpressionListContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(PlpgsqlParser.RPAREN, 0); }
		public TerminalNode FOR() { return getToken(PlpgsqlParser.FOR, 0); }
		public SelectStmtContext selectStmt() {
			return getRuleContext(SelectStmtContext.class,0);
		}
		public OpenCursorStmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_openCursorStmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).enterOpenCursorStmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).exitOpenCursorStmt(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PlpgsqlParserVisitor ) return ((PlpgsqlParserVisitor<? extends T>)visitor).visitOpenCursorStmt(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OpenCursorStmtContext openCursorStmt() throws RecognitionException {
		OpenCursorStmtContext _localctx = new OpenCursorStmtContext(_ctx, getState());
		enterRule(_localctx, 116, RULE_openCursorStmt);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1100);
			match(OPEN);
			setState(1101);
			variableRef();
			setState(1106);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LPAREN) {
				{
				setState(1102);
				match(LPAREN);
				setState(1103);
				expressionList();
				setState(1104);
				match(RPAREN);
				}
			}

			setState(1110);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==FOR) {
				{
				setState(1108);
				match(FOR);
				setState(1109);
				selectStmt();
				}
			}

			setState(1112);
			match(SEMI);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class CloseCursorStmtContext extends ParserRuleContext {
		public TerminalNode CLOSE() { return getToken(PlpgsqlParser.CLOSE, 0); }
		public VariableRefContext variableRef() {
			return getRuleContext(VariableRefContext.class,0);
		}
		public TerminalNode SEMI() { return getToken(PlpgsqlParser.SEMI, 0); }
		public CloseCursorStmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_closeCursorStmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).enterCloseCursorStmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).exitCloseCursorStmt(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PlpgsqlParserVisitor ) return ((PlpgsqlParserVisitor<? extends T>)visitor).visitCloseCursorStmt(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CloseCursorStmtContext closeCursorStmt() throws RecognitionException {
		CloseCursorStmtContext _localctx = new CloseCursorStmtContext(_ctx, getState());
		enterRule(_localctx, 118, RULE_closeCursorStmt);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1114);
			match(CLOSE);
			setState(1115);
			variableRef();
			setState(1116);
			match(SEMI);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FetchStmtContext extends ParserRuleContext {
		public TerminalNode FETCH() { return getToken(PlpgsqlParser.FETCH, 0); }
		public VariableRefContext variableRef() {
			return getRuleContext(VariableRefContext.class,0);
		}
		public TerminalNode INTO() { return getToken(PlpgsqlParser.INTO, 0); }
		public VariableListContext variableList() {
			return getRuleContext(VariableListContext.class,0);
		}
		public TerminalNode SEMI() { return getToken(PlpgsqlParser.SEMI, 0); }
		public FetchStmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fetchStmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).enterFetchStmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).exitFetchStmt(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PlpgsqlParserVisitor ) return ((PlpgsqlParserVisitor<? extends T>)visitor).visitFetchStmt(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FetchStmtContext fetchStmt() throws RecognitionException {
		FetchStmtContext _localctx = new FetchStmtContext(_ctx, getState());
		enterRule(_localctx, 120, RULE_fetchStmt);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1118);
			match(FETCH);
			setState(1119);
			variableRef();
			setState(1120);
			match(INTO);
			setState(1121);
			variableList();
			setState(1122);
			match(SEMI);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class SelectStmtContext extends ParserRuleContext {
		public List<SelectQueryContext> selectQuery() {
			return getRuleContexts(SelectQueryContext.class);
		}
		public SelectQueryContext selectQuery(int i) {
			return getRuleContext(SelectQueryContext.class,i);
		}
		public WithClauseContext withClause() {
			return getRuleContext(WithClauseContext.class,0);
		}
		public List<TerminalNode> UNION() { return getTokens(PlpgsqlParser.UNION); }
		public TerminalNode UNION(int i) {
			return getToken(PlpgsqlParser.UNION, i);
		}
		public List<TerminalNode> INTERSECT() { return getTokens(PlpgsqlParser.INTERSECT); }
		public TerminalNode INTERSECT(int i) {
			return getToken(PlpgsqlParser.INTERSECT, i);
		}
		public List<TerminalNode> EXCEPT() { return getTokens(PlpgsqlParser.EXCEPT); }
		public TerminalNode EXCEPT(int i) {
			return getToken(PlpgsqlParser.EXCEPT, i);
		}
		public List<TerminalNode> ALL() { return getTokens(PlpgsqlParser.ALL); }
		public TerminalNode ALL(int i) {
			return getToken(PlpgsqlParser.ALL, i);
		}
		public SelectStmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_selectStmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).enterSelectStmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).exitSelectStmt(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PlpgsqlParserVisitor ) return ((PlpgsqlParserVisitor<? extends T>)visitor).visitSelectStmt(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SelectStmtContext selectStmt() throws RecognitionException {
		SelectStmtContext _localctx = new SelectStmtContext(_ctx, getState());
		enterRule(_localctx, 122, RULE_selectStmt);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1125);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==WITH) {
				{
				setState(1124);
				withClause();
				}
			}

			setState(1127);
			selectQuery();
			setState(1135);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (((((_la - 63)) & ~0x3f) == 0 && ((1L << (_la - 63)) & 7L) != 0)) {
				{
				{
				setState(1128);
				_la = _input.LA(1);
				if ( !(((((_la - 63)) & ~0x3f) == 0 && ((1L << (_la - 63)) & 7L) != 0)) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(1130);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ALL) {
					{
					setState(1129);
					match(ALL);
					}
				}

				setState(1132);
				selectQuery();
				}
				}
				setState(1137);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class WithClauseContext extends ParserRuleContext {
		public TerminalNode WITH() { return getToken(PlpgsqlParser.WITH, 0); }
		public CteListContext cteList() {
			return getRuleContext(CteListContext.class,0);
		}
		public WithClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_withClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).enterWithClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).exitWithClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PlpgsqlParserVisitor ) return ((PlpgsqlParserVisitor<? extends T>)visitor).visitWithClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final WithClauseContext withClause() throws RecognitionException {
		WithClauseContext _localctx = new WithClauseContext(_ctx, getState());
		enterRule(_localctx, 124, RULE_withClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1138);
			match(WITH);
			setState(1139);
			cteList();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class CteListContext extends ParserRuleContext {
		public List<CteContext> cte() {
			return getRuleContexts(CteContext.class);
		}
		public CteContext cte(int i) {
			return getRuleContext(CteContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(PlpgsqlParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(PlpgsqlParser.COMMA, i);
		}
		public CteListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_cteList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).enterCteList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).exitCteList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PlpgsqlParserVisitor ) return ((PlpgsqlParserVisitor<? extends T>)visitor).visitCteList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CteListContext cteList() throws RecognitionException {
		CteListContext _localctx = new CteListContext(_ctx, getState());
		enterRule(_localctx, 126, RULE_cteList);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1141);
			cte();
			setState(1146);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,157,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1142);
					match(COMMA);
					setState(1143);
					cte();
					}
					} 
				}
				setState(1148);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,157,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class CteContext extends ParserRuleContext {
		public TerminalNode Identifier() { return getToken(PlpgsqlParser.Identifier, 0); }
		public TerminalNode AS() { return getToken(PlpgsqlParser.AS, 0); }
		public List<TerminalNode> LPAREN() { return getTokens(PlpgsqlParser.LPAREN); }
		public TerminalNode LPAREN(int i) {
			return getToken(PlpgsqlParser.LPAREN, i);
		}
		public SelectStmtContext selectStmt() {
			return getRuleContext(SelectStmtContext.class,0);
		}
		public List<TerminalNode> RPAREN() { return getTokens(PlpgsqlParser.RPAREN); }
		public TerminalNode RPAREN(int i) {
			return getToken(PlpgsqlParser.RPAREN, i);
		}
		public ColumnListContext columnList() {
			return getRuleContext(ColumnListContext.class,0);
		}
		public CteContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_cte; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).enterCte(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).exitCte(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PlpgsqlParserVisitor ) return ((PlpgsqlParserVisitor<? extends T>)visitor).visitCte(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CteContext cte() throws RecognitionException {
		CteContext _localctx = new CteContext(_ctx, getState());
		enterRule(_localctx, 128, RULE_cte);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1149);
			match(Identifier);
			setState(1154);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LPAREN) {
				{
				setState(1150);
				match(LPAREN);
				setState(1151);
				columnList();
				setState(1152);
				match(RPAREN);
				}
			}

			setState(1156);
			match(AS);
			setState(1157);
			match(LPAREN);
			setState(1158);
			selectStmt();
			setState(1159);
			match(RPAREN);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class SelectQueryContext extends ParserRuleContext {
		public TerminalNode SELECT() { return getToken(PlpgsqlParser.SELECT, 0); }
		public SelectListContext selectList() {
			return getRuleContext(SelectListContext.class,0);
		}
		public TerminalNode DISTINCT() { return getToken(PlpgsqlParser.DISTINCT, 0); }
		public TerminalNode ALL() { return getToken(PlpgsqlParser.ALL, 0); }
		public FromClauseContext fromClause() {
			return getRuleContext(FromClauseContext.class,0);
		}
		public WhereClauseContext whereClause() {
			return getRuleContext(WhereClauseContext.class,0);
		}
		public GroupByClauseContext groupByClause() {
			return getRuleContext(GroupByClauseContext.class,0);
		}
		public HavingClauseContext havingClause() {
			return getRuleContext(HavingClauseContext.class,0);
		}
		public OrderByClauseContext orderByClause() {
			return getRuleContext(OrderByClauseContext.class,0);
		}
		public LimitClauseContext limitClause() {
			return getRuleContext(LimitClauseContext.class,0);
		}
		public TerminalNode ON() { return getToken(PlpgsqlParser.ON, 0); }
		public TerminalNode LPAREN() { return getToken(PlpgsqlParser.LPAREN, 0); }
		public ExpressionListContext expressionList() {
			return getRuleContext(ExpressionListContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(PlpgsqlParser.RPAREN, 0); }
		public SelectQueryContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_selectQuery; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).enterSelectQuery(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).exitSelectQuery(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PlpgsqlParserVisitor ) return ((PlpgsqlParserVisitor<? extends T>)visitor).visitSelectQuery(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SelectQueryContext selectQuery() throws RecognitionException {
		SelectQueryContext _localctx = new SelectQueryContext(_ctx, getState());
		enterRule(_localctx, 130, RULE_selectQuery);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1161);
			match(SELECT);
			setState(1171);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case DISTINCT:
				{
				setState(1162);
				match(DISTINCT);
				setState(1168);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ON) {
					{
					setState(1163);
					match(ON);
					setState(1164);
					match(LPAREN);
					setState(1165);
					expressionList();
					setState(1166);
					match(RPAREN);
					}
				}

				}
				break;
			case ALL:
				{
				setState(1170);
				match(ALL);
				}
				break;
			case CASE:
			case EXISTS:
			case CAST:
			case COALESCE:
			case NULLIF:
			case GREATEST:
			case LEAST:
			case POSITION:
			case INTERVAL:
			case ARRAY:
			case LEFT:
			case RIGHT:
			case SUBSTRING:
			case SQLSTATE:
			case NULL:
			case NOT:
			case FOUND:
			case TRUE:
			case FALSE:
			case STAR:
			case LPAREN:
			case IntegerLiteral:
			case NumericLiteral:
			case StringLiteral:
			case DollarQuotedString:
			case Identifier:
			case QuotedIdentifier:
				break;
			default:
				break;
			}
			setState(1173);
			selectList();
			setState(1175);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==FROM) {
				{
				setState(1174);
				fromClause();
				}
			}

			setState(1178);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==WHERE) {
				{
				setState(1177);
				whereClause();
				}
			}

			setState(1181);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==GROUP) {
				{
				setState(1180);
				groupByClause();
				}
			}

			setState(1184);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==HAVING) {
				{
				setState(1183);
				havingClause();
				}
			}

			setState(1187);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ORDER) {
				{
				setState(1186);
				orderByClause();
				}
			}

			setState(1190);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LIMIT) {
				{
				setState(1189);
				limitClause();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class GroupByClauseContext extends ParserRuleContext {
		public TerminalNode GROUP() { return getToken(PlpgsqlParser.GROUP, 0); }
		public TerminalNode BY() { return getToken(PlpgsqlParser.BY, 0); }
		public ExpressionListContext expressionList() {
			return getRuleContext(ExpressionListContext.class,0);
		}
		public GroupByClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_groupByClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).enterGroupByClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).exitGroupByClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PlpgsqlParserVisitor ) return ((PlpgsqlParserVisitor<? extends T>)visitor).visitGroupByClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GroupByClauseContext groupByClause() throws RecognitionException {
		GroupByClauseContext _localctx = new GroupByClauseContext(_ctx, getState());
		enterRule(_localctx, 132, RULE_groupByClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1192);
			match(GROUP);
			setState(1193);
			match(BY);
			setState(1194);
			expressionList();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class HavingClauseContext extends ParserRuleContext {
		public TerminalNode HAVING() { return getToken(PlpgsqlParser.HAVING, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public HavingClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_havingClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).enterHavingClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).exitHavingClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PlpgsqlParserVisitor ) return ((PlpgsqlParserVisitor<? extends T>)visitor).visitHavingClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final HavingClauseContext havingClause() throws RecognitionException {
		HavingClauseContext _localctx = new HavingClauseContext(_ctx, getState());
		enterRule(_localctx, 134, RULE_havingClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1196);
			match(HAVING);
			setState(1197);
			expression(0);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class OrderByClauseContext extends ParserRuleContext {
		public TerminalNode ORDER() { return getToken(PlpgsqlParser.ORDER, 0); }
		public TerminalNode BY() { return getToken(PlpgsqlParser.BY, 0); }
		public List<OrderByItemContext> orderByItem() {
			return getRuleContexts(OrderByItemContext.class);
		}
		public OrderByItemContext orderByItem(int i) {
			return getRuleContext(OrderByItemContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(PlpgsqlParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(PlpgsqlParser.COMMA, i);
		}
		public OrderByClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_orderByClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).enterOrderByClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).exitOrderByClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PlpgsqlParserVisitor ) return ((PlpgsqlParserVisitor<? extends T>)visitor).visitOrderByClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OrderByClauseContext orderByClause() throws RecognitionException {
		OrderByClauseContext _localctx = new OrderByClauseContext(_ctx, getState());
		enterRule(_localctx, 136, RULE_orderByClause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1199);
			match(ORDER);
			setState(1200);
			match(BY);
			setState(1201);
			orderByItem();
			setState(1206);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(1202);
				match(COMMA);
				setState(1203);
				orderByItem();
				}
				}
				setState(1208);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class OrderByItemContext extends ParserRuleContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode NULLS() { return getToken(PlpgsqlParser.NULLS, 0); }
		public TerminalNode ASC() { return getToken(PlpgsqlParser.ASC, 0); }
		public TerminalNode DESC() { return getToken(PlpgsqlParser.DESC, 0); }
		public TerminalNode FIRST() { return getToken(PlpgsqlParser.FIRST, 0); }
		public TerminalNode LAST() { return getToken(PlpgsqlParser.LAST, 0); }
		public OrderByItemContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_orderByItem; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).enterOrderByItem(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).exitOrderByItem(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PlpgsqlParserVisitor ) return ((PlpgsqlParserVisitor<? extends T>)visitor).visitOrderByItem(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OrderByItemContext orderByItem() throws RecognitionException {
		OrderByItemContext _localctx = new OrderByItemContext(_ctx, getState());
		enterRule(_localctx, 138, RULE_orderByItem);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1209);
			expression(0);
			setState(1211);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ASC || _la==DESC) {
				{
				setState(1210);
				_la = _input.LA(1);
				if ( !(_la==ASC || _la==DESC) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
			}

			setState(1215);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==NULLS) {
				{
				setState(1213);
				match(NULLS);
				setState(1214);
				_la = _input.LA(1);
				if ( !(_la==FIRST || _la==LAST) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class LimitClauseContext extends ParserRuleContext {
		public TerminalNode LIMIT() { return getToken(PlpgsqlParser.LIMIT, 0); }
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode ALL() { return getToken(PlpgsqlParser.ALL, 0); }
		public TerminalNode OFFSET() { return getToken(PlpgsqlParser.OFFSET, 0); }
		public LimitClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_limitClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).enterLimitClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).exitLimitClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PlpgsqlParserVisitor ) return ((PlpgsqlParserVisitor<? extends T>)visitor).visitLimitClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LimitClauseContext limitClause() throws RecognitionException {
		LimitClauseContext _localctx = new LimitClauseContext(_ctx, getState());
		enterRule(_localctx, 140, RULE_limitClause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1217);
			match(LIMIT);
			setState(1220);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case CASE:
			case EXISTS:
			case CAST:
			case COALESCE:
			case NULLIF:
			case GREATEST:
			case LEAST:
			case POSITION:
			case INTERVAL:
			case ARRAY:
			case LEFT:
			case RIGHT:
			case SUBSTRING:
			case SQLSTATE:
			case NULL:
			case NOT:
			case FOUND:
			case TRUE:
			case FALSE:
			case LPAREN:
			case IntegerLiteral:
			case NumericLiteral:
			case StringLiteral:
			case DollarQuotedString:
			case Identifier:
			case QuotedIdentifier:
				{
				setState(1218);
				expression(0);
				}
				break;
			case ALL:
				{
				setState(1219);
				match(ALL);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(1224);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==OFFSET) {
				{
				setState(1222);
				match(OFFSET);
				setState(1223);
				expression(0);
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ParameterListContext extends ParserRuleContext {
		public List<ParameterContext> parameter() {
			return getRuleContexts(ParameterContext.class);
		}
		public ParameterContext parameter(int i) {
			return getRuleContext(ParameterContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(PlpgsqlParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(PlpgsqlParser.COMMA, i);
		}
		public ParameterListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_parameterList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).enterParameterList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).exitParameterList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PlpgsqlParserVisitor ) return ((PlpgsqlParserVisitor<? extends T>)visitor).visitParameterList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ParameterListContext parameterList() throws RecognitionException {
		ParameterListContext _localctx = new ParameterListContext(_ctx, getState());
		enterRule(_localctx, 142, RULE_parameterList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1226);
			parameter();
			setState(1231);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(1227);
				match(COMMA);
				setState(1228);
				parameter();
				}
				}
				setState(1233);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ParameterContext extends ParserRuleContext {
		public TerminalNode Identifier() { return getToken(PlpgsqlParser.Identifier, 0); }
		public DataTypeContext dataType() {
			return getRuleContext(DataTypeContext.class,0);
		}
		public ParameterContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_parameter; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).enterParameter(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).exitParameter(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PlpgsqlParserVisitor ) return ((PlpgsqlParserVisitor<? extends T>)visitor).visitParameter(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ParameterContext parameter() throws RecognitionException {
		ParameterContext _localctx = new ParameterContext(_ctx, getState());
		enterRule(_localctx, 144, RULE_parameter);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1234);
			match(Identifier);
			setState(1235);
			dataType(0);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ExpressionContext extends ParserRuleContext {
		public LiteralContext literal() {
			return getRuleContext(LiteralContext.class,0);
		}
		public VariableRefContext variableRef() {
			return getRuleContext(VariableRefContext.class,0);
		}
		public FunctionCallContext functionCall() {
			return getRuleContext(FunctionCallContext.class,0);
		}
		public WindowFunctionContext windowFunction() {
			return getRuleContext(WindowFunctionContext.class,0);
		}
		public SpecialVariableContext specialVariable() {
			return getRuleContext(SpecialVariableContext.class,0);
		}
		public TerminalNode INTERVAL() { return getToken(PlpgsqlParser.INTERVAL, 0); }
		public TerminalNode StringLiteral() { return getToken(PlpgsqlParser.StringLiteral, 0); }
		public TerminalNode CAST() { return getToken(PlpgsqlParser.CAST, 0); }
		public TerminalNode LPAREN() { return getToken(PlpgsqlParser.LPAREN, 0); }
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode AS() { return getToken(PlpgsqlParser.AS, 0); }
		public DataTypeContext dataType() {
			return getRuleContext(DataTypeContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(PlpgsqlParser.RPAREN, 0); }
		public TerminalNode COALESCE() { return getToken(PlpgsqlParser.COALESCE, 0); }
		public ExpressionListContext expressionList() {
			return getRuleContext(ExpressionListContext.class,0);
		}
		public TerminalNode NULLIF() { return getToken(PlpgsqlParser.NULLIF, 0); }
		public TerminalNode COMMA() { return getToken(PlpgsqlParser.COMMA, 0); }
		public TerminalNode GREATEST() { return getToken(PlpgsqlParser.GREATEST, 0); }
		public TerminalNode LEAST() { return getToken(PlpgsqlParser.LEAST, 0); }
		public TerminalNode EXISTS() { return getToken(PlpgsqlParser.EXISTS, 0); }
		public SelectStmtContext selectStmt() {
			return getRuleContext(SelectStmtContext.class,0);
		}
		public TerminalNode NOT() { return getToken(PlpgsqlParser.NOT, 0); }
		public TerminalNode CASE() { return getToken(PlpgsqlParser.CASE, 0); }
		public WhenExprClauseListContext whenExprClauseList() {
			return getRuleContext(WhenExprClauseListContext.class,0);
		}
		public TerminalNode END() { return getToken(PlpgsqlParser.END, 0); }
		public TerminalNode ELSE() { return getToken(PlpgsqlParser.ELSE, 0); }
		public ArrayExpressionContext arrayExpression() {
			return getRuleContext(ArrayExpressionContext.class,0);
		}
		public TerminalNode STAR() { return getToken(PlpgsqlParser.STAR, 0); }
		public TerminalNode SLASH() { return getToken(PlpgsqlParser.SLASH, 0); }
		public TerminalNode PERCENT() { return getToken(PlpgsqlParser.PERCENT, 0); }
		public TerminalNode PLUS() { return getToken(PlpgsqlParser.PLUS, 0); }
		public TerminalNode MINUS() { return getToken(PlpgsqlParser.MINUS, 0); }
		public TerminalNode CONCAT() { return getToken(PlpgsqlParser.CONCAT, 0); }
		public TerminalNode BETWEEN() { return getToken(PlpgsqlParser.BETWEEN, 0); }
		public TerminalNode AND() { return getToken(PlpgsqlParser.AND, 0); }
		public TerminalNode LIKE() { return getToken(PlpgsqlParser.LIKE, 0); }
		public TerminalNode ILIKE() { return getToken(PlpgsqlParser.ILIKE, 0); }
		public TerminalNode SIMILAR() { return getToken(PlpgsqlParser.SIMILAR, 0); }
		public TerminalNode TO() { return getToken(PlpgsqlParser.TO, 0); }
		public TerminalNode OR() { return getToken(PlpgsqlParser.OR, 0); }
		public TerminalNode TYPECAST() { return getToken(PlpgsqlParser.TYPECAST, 0); }
		public TerminalNode EQ() { return getToken(PlpgsqlParser.EQ, 0); }
		public TerminalNode NEQ() { return getToken(PlpgsqlParser.NEQ, 0); }
		public TerminalNode LT() { return getToken(PlpgsqlParser.LT, 0); }
		public TerminalNode LTE() { return getToken(PlpgsqlParser.LTE, 0); }
		public TerminalNode GT() { return getToken(PlpgsqlParser.GT, 0); }
		public TerminalNode GTE() { return getToken(PlpgsqlParser.GTE, 0); }
		public TerminalNode ANY() { return getToken(PlpgsqlParser.ANY, 0); }
		public TerminalNode SOME() { return getToken(PlpgsqlParser.SOME, 0); }
		public TerminalNode ALL() { return getToken(PlpgsqlParser.ALL, 0); }
		public TerminalNode IS() { return getToken(PlpgsqlParser.IS, 0); }
		public TerminalNode NULL() { return getToken(PlpgsqlParser.NULL, 0); }
		public TerminalNode IN() { return getToken(PlpgsqlParser.IN, 0); }
		public ExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).enterExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).exitExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PlpgsqlParserVisitor ) return ((PlpgsqlParserVisitor<? extends T>)visitor).visitExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExpressionContext expression() throws RecognitionException {
		return expression(0);
	}

	private ExpressionContext expression(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		ExpressionContext _localctx = new ExpressionContext(_ctx, _parentState);
		ExpressionContext _prevctx = _localctx;
		int _startState = 146;
		enterRecursionRule(_localctx, 146, RULE_expression, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1301);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,175,_ctx) ) {
			case 1:
				{
				setState(1238);
				literal();
				}
				break;
			case 2:
				{
				setState(1239);
				variableRef();
				}
				break;
			case 3:
				{
				setState(1240);
				functionCall();
				}
				break;
			case 4:
				{
				setState(1241);
				windowFunction();
				}
				break;
			case 5:
				{
				setState(1242);
				specialVariable();
				}
				break;
			case 6:
				{
				setState(1243);
				match(INTERVAL);
				setState(1244);
				match(StringLiteral);
				}
				break;
			case 7:
				{
				setState(1245);
				match(CAST);
				setState(1246);
				match(LPAREN);
				setState(1247);
				expression(0);
				setState(1248);
				match(AS);
				setState(1249);
				dataType(0);
				setState(1250);
				match(RPAREN);
				}
				break;
			case 8:
				{
				setState(1252);
				match(COALESCE);
				setState(1253);
				match(LPAREN);
				setState(1254);
				expressionList();
				setState(1255);
				match(RPAREN);
				}
				break;
			case 9:
				{
				setState(1257);
				match(NULLIF);
				setState(1258);
				match(LPAREN);
				setState(1259);
				expression(0);
				setState(1260);
				match(COMMA);
				setState(1261);
				expression(0);
				setState(1262);
				match(RPAREN);
				}
				break;
			case 10:
				{
				setState(1264);
				match(GREATEST);
				setState(1265);
				match(LPAREN);
				setState(1266);
				expressionList();
				setState(1267);
				match(RPAREN);
				}
				break;
			case 11:
				{
				setState(1269);
				match(LEAST);
				setState(1270);
				match(LPAREN);
				setState(1271);
				expressionList();
				setState(1272);
				match(RPAREN);
				}
				break;
			case 12:
				{
				setState(1274);
				match(EXISTS);
				setState(1275);
				match(LPAREN);
				setState(1276);
				selectStmt();
				setState(1277);
				match(RPAREN);
				}
				break;
			case 13:
				{
				setState(1279);
				match(LPAREN);
				setState(1280);
				selectStmt();
				setState(1281);
				match(RPAREN);
				}
				break;
			case 14:
				{
				setState(1283);
				match(LPAREN);
				setState(1284);
				expression(0);
				setState(1285);
				match(RPAREN);
				}
				break;
			case 15:
				{
				setState(1287);
				match(NOT);
				setState(1288);
				expression(5);
				}
				break;
			case 16:
				{
				setState(1289);
				match(CASE);
				setState(1291);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (((((_la - 9)) & ~0x3f) == 0 && ((1L << (_la - 9)) & -720575940379279359L) != 0) || ((((_la - 73)) & ~0x3f) == 0 && ((1L << (_la - 73)) & 6918655906817245185L) != 0) || ((((_la - 156)) & ~0x3f) == 0 && ((1L << (_la - 156)) & 34643206209633L) != 0)) {
					{
					setState(1290);
					expression(0);
					}
				}

				setState(1293);
				whenExprClauseList();
				setState(1296);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ELSE) {
					{
					setState(1294);
					match(ELSE);
					setState(1295);
					expression(0);
					}
				}

				setState(1298);
				match(END);
				}
				break;
			case 17:
				{
				setState(1300);
				arrayExpression();
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(1375);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,184,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(1373);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,183,_ctx) ) {
					case 1:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1303);
						if (!(precpred(_ctx, 14))) throw new FailedPredicateException(this, "precpred(_ctx, 14)");
						setState(1304);
						_la = _input.LA(1);
						if ( !(((((_la - 163)) & ~0x3f) == 0 && ((1L << (_la - 163)) & 49153L) != 0)) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(1305);
						expression(15);
						}
						break;
					case 2:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1306);
						if (!(precpred(_ctx, 13))) throw new FailedPredicateException(this, "precpred(_ctx, 13)");
						setState(1307);
						_la = _input.LA(1);
						if ( !(_la==PLUS || _la==MINUS) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(1308);
						expression(14);
						}
						break;
					case 3:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1309);
						if (!(precpred(_ctx, 12))) throw new FailedPredicateException(this, "precpred(_ctx, 12)");
						setState(1310);
						match(CONCAT);
						setState(1311);
						expression(13);
						}
						break;
					case 4:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1312);
						if (!(precpred(_ctx, 10))) throw new FailedPredicateException(this, "precpred(_ctx, 10)");
						setState(1314);
						_errHandler.sync(this);
						_la = _input.LA(1);
						if (_la==NOT) {
							{
							setState(1313);
							match(NOT);
							}
						}

						setState(1316);
						match(BETWEEN);
						setState(1317);
						expression(0);
						setState(1318);
						match(AND);
						setState(1319);
						expression(11);
						}
						break;
					case 5:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1321);
						if (!(precpred(_ctx, 9))) throw new FailedPredicateException(this, "precpred(_ctx, 9)");
						setState(1323);
						_errHandler.sync(this);
						_la = _input.LA(1);
						if (_la==NOT) {
							{
							setState(1322);
							match(NOT);
							}
						}

						setState(1325);
						_la = _input.LA(1);
						if ( !(_la==LIKE || _la==ILIKE) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(1326);
						expression(10);
						}
						break;
					case 6:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1327);
						if (!(precpred(_ctx, 8))) throw new FailedPredicateException(this, "precpred(_ctx, 8)");
						setState(1328);
						match(SIMILAR);
						setState(1329);
						match(TO);
						setState(1330);
						expression(9);
						}
						break;
					case 7:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1331);
						if (!(precpred(_ctx, 6))) throw new FailedPredicateException(this, "precpred(_ctx, 6)");
						setState(1332);
						_la = _input.LA(1);
						if ( !(_la==AND || _la==OR) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(1333);
						expression(7);
						}
						break;
					case 8:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1334);
						if (!(precpred(_ctx, 22))) throw new FailedPredicateException(this, "precpred(_ctx, 22)");
						setState(1335);
						match(TYPECAST);
						setState(1336);
						dataType(0);
						}
						break;
					case 9:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1337);
						if (!(precpred(_ctx, 11))) throw new FailedPredicateException(this, "precpred(_ctx, 11)");
						setState(1338);
						_la = _input.LA(1);
						if ( !(((((_la - 169)) & ~0x3f) == 0 && ((1L << (_la - 169)) & 63L) != 0)) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(1339);
						expression(0);
						setState(1343);
						_errHandler.sync(this);
						switch ( getInterpreter().adaptivePredict(_input,178,_ctx) ) {
						case 1:
							{
							setState(1340);
							match(LPAREN);
							setState(1341);
							match(PLUS);
							setState(1342);
							match(RPAREN);
							}
							break;
						}
						}
						break;
					case 10:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1345);
						if (!(precpred(_ctx, 7))) throw new FailedPredicateException(this, "precpred(_ctx, 7)");
						setState(1346);
						_la = _input.LA(1);
						if ( !(((((_la - 169)) & ~0x3f) == 0 && ((1L << (_la - 169)) & 63L) != 0)) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(1347);
						_la = _input.LA(1);
						if ( !(_la==ALL || _la==ANY || _la==SOME) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(1348);
						match(LPAREN);
						setState(1351);
						_errHandler.sync(this);
						switch (_input.LA(1)) {
						case SELECT:
						case WITH:
							{
							setState(1349);
							selectStmt();
							}
							break;
						case CASE:
						case EXISTS:
						case CAST:
						case COALESCE:
						case NULLIF:
						case GREATEST:
						case LEAST:
						case POSITION:
						case INTERVAL:
						case ARRAY:
						case LEFT:
						case RIGHT:
						case SUBSTRING:
						case SQLSTATE:
						case NULL:
						case NOT:
						case FOUND:
						case TRUE:
						case FALSE:
						case LPAREN:
						case IntegerLiteral:
						case NumericLiteral:
						case StringLiteral:
						case DollarQuotedString:
						case Identifier:
						case QuotedIdentifier:
							{
							setState(1350);
							expressionList();
							}
							break;
						default:
							throw new NoViableAltException(this);
						}
						setState(1353);
						match(RPAREN);
						}
						break;
					case 11:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1355);
						if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
						setState(1356);
						match(IS);
						setState(1358);
						_errHandler.sync(this);
						_la = _input.LA(1);
						if (_la==NOT) {
							{
							setState(1357);
							match(NOT);
							}
						}

						setState(1360);
						match(NULL);
						}
						break;
					case 12:
						{
						_localctx = new ExpressionContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(1361);
						if (!(precpred(_ctx, 3))) throw new FailedPredicateException(this, "precpred(_ctx, 3)");
						setState(1363);
						_errHandler.sync(this);
						_la = _input.LA(1);
						if (_la==NOT) {
							{
							setState(1362);
							match(NOT);
							}
						}

						setState(1365);
						match(IN);
						setState(1366);
						match(LPAREN);
						setState(1369);
						_errHandler.sync(this);
						switch (_input.LA(1)) {
						case SELECT:
						case WITH:
							{
							setState(1367);
							selectStmt();
							}
							break;
						case CASE:
						case EXISTS:
						case CAST:
						case COALESCE:
						case NULLIF:
						case GREATEST:
						case LEAST:
						case POSITION:
						case INTERVAL:
						case ARRAY:
						case LEFT:
						case RIGHT:
						case SUBSTRING:
						case SQLSTATE:
						case NULL:
						case NOT:
						case FOUND:
						case TRUE:
						case FALSE:
						case LPAREN:
						case IntegerLiteral:
						case NumericLiteral:
						case StringLiteral:
						case DollarQuotedString:
						case Identifier:
						case QuotedIdentifier:
							{
							setState(1368);
							expressionList();
							}
							break;
						default:
							throw new NoViableAltException(this);
						}
						setState(1371);
						match(RPAREN);
						}
						break;
					}
					} 
				}
				setState(1377);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,184,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class WhenExprClauseListContext extends ParserRuleContext {
		public List<WhenExprClauseContext> whenExprClause() {
			return getRuleContexts(WhenExprClauseContext.class);
		}
		public WhenExprClauseContext whenExprClause(int i) {
			return getRuleContext(WhenExprClauseContext.class,i);
		}
		public WhenExprClauseListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_whenExprClauseList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).enterWhenExprClauseList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).exitWhenExprClauseList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PlpgsqlParserVisitor ) return ((PlpgsqlParserVisitor<? extends T>)visitor).visitWhenExprClauseList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final WhenExprClauseListContext whenExprClauseList() throws RecognitionException {
		WhenExprClauseListContext _localctx = new WhenExprClauseListContext(_ctx, getState());
		enterRule(_localctx, 148, RULE_whenExprClauseList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1379); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(1378);
				whenExprClause();
				}
				}
				setState(1381); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==WHEN );
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class WhenExprClauseContext extends ParserRuleContext {
		public TerminalNode WHEN() { return getToken(PlpgsqlParser.WHEN, 0); }
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode THEN() { return getToken(PlpgsqlParser.THEN, 0); }
		public WhenExprClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_whenExprClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).enterWhenExprClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).exitWhenExprClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PlpgsqlParserVisitor ) return ((PlpgsqlParserVisitor<? extends T>)visitor).visitWhenExprClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final WhenExprClauseContext whenExprClause() throws RecognitionException {
		WhenExprClauseContext _localctx = new WhenExprClauseContext(_ctx, getState());
		enterRule(_localctx, 150, RULE_whenExprClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1383);
			match(WHEN);
			setState(1384);
			expression(0);
			setState(1385);
			match(THEN);
			setState(1386);
			expression(0);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ArrayExpressionContext extends ParserRuleContext {
		public TerminalNode ARRAY() { return getToken(PlpgsqlParser.ARRAY, 0); }
		public TerminalNode LBRACK() { return getToken(PlpgsqlParser.LBRACK, 0); }
		public ExpressionListContext expressionList() {
			return getRuleContext(ExpressionListContext.class,0);
		}
		public TerminalNode RBRACK() { return getToken(PlpgsqlParser.RBRACK, 0); }
		public VariableRefContext variableRef() {
			return getRuleContext(VariableRefContext.class,0);
		}
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public ArrayExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_arrayExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).enterArrayExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).exitArrayExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PlpgsqlParserVisitor ) return ((PlpgsqlParserVisitor<? extends T>)visitor).visitArrayExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ArrayExpressionContext arrayExpression() throws RecognitionException {
		ArrayExpressionContext _localctx = new ArrayExpressionContext(_ctx, getState());
		enterRule(_localctx, 152, RULE_arrayExpression);
		try {
			setState(1398);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ARRAY:
				enterOuterAlt(_localctx, 1);
				{
				setState(1388);
				match(ARRAY);
				setState(1389);
				match(LBRACK);
				setState(1390);
				expressionList();
				setState(1391);
				match(RBRACK);
				}
				break;
			case Identifier:
			case QuotedIdentifier:
				enterOuterAlt(_localctx, 2);
				{
				setState(1393);
				variableRef();
				setState(1394);
				match(LBRACK);
				setState(1395);
				expression(0);
				setState(1396);
				match(RBRACK);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class SpecialVariableContext extends ParserRuleContext {
		public TerminalNode SQLSTATE() { return getToken(PlpgsqlParser.SQLSTATE, 0); }
		public TerminalNode FOUND() { return getToken(PlpgsqlParser.FOUND, 0); }
		public SpecialVariableContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_specialVariable; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).enterSpecialVariable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).exitSpecialVariable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PlpgsqlParserVisitor ) return ((PlpgsqlParserVisitor<? extends T>)visitor).visitSpecialVariable(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SpecialVariableContext specialVariable() throws RecognitionException {
		SpecialVariableContext _localctx = new SpecialVariableContext(_ctx, getState());
		enterRule(_localctx, 154, RULE_specialVariable);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1400);
			_la = _input.LA(1);
			if ( !(_la==SQLSTATE || _la==FOUND) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FunctionCallContext extends ParserRuleContext {
		public TerminalNode LPAREN() { return getToken(PlpgsqlParser.LPAREN, 0); }
		public FunctionCallArgsContext functionCallArgs() {
			return getRuleContext(FunctionCallArgsContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(PlpgsqlParser.RPAREN, 0); }
		public TerminalNode Identifier() { return getToken(PlpgsqlParser.Identifier, 0); }
		public TerminalNode LEFT() { return getToken(PlpgsqlParser.LEFT, 0); }
		public TerminalNode RIGHT() { return getToken(PlpgsqlParser.RIGHT, 0); }
		public TerminalNode SUBSTRING() { return getToken(PlpgsqlParser.SUBSTRING, 0); }
		public TerminalNode POSITION() { return getToken(PlpgsqlParser.POSITION, 0); }
		public FunctionCallContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_functionCall; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).enterFunctionCall(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).exitFunctionCall(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PlpgsqlParserVisitor ) return ((PlpgsqlParserVisitor<? extends T>)visitor).visitFunctionCall(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FunctionCallContext functionCall() throws RecognitionException {
		FunctionCallContext _localctx = new FunctionCallContext(_ctx, getState());
		enterRule(_localctx, 156, RULE_functionCall);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1402);
			_la = _input.LA(1);
			if ( !(((((_la - 73)) & ~0x3f) == 0 && ((1L << (_la - 73)) & 962072674305L) != 0) || _la==Identifier) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(1403);
			match(LPAREN);
			setState(1404);
			functionCallArgs();
			setState(1405);
			match(RPAREN);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FunctionCallArgsContext extends ParserRuleContext {
		public TerminalNode DISTINCT() { return getToken(PlpgsqlParser.DISTINCT, 0); }
		public ExpressionListContext expressionList() {
			return getRuleContext(ExpressionListContext.class,0);
		}
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode IN() { return getToken(PlpgsqlParser.IN, 0); }
		public TerminalNode FROM() { return getToken(PlpgsqlParser.FROM, 0); }
		public TerminalNode FOR() { return getToken(PlpgsqlParser.FOR, 0); }
		public TerminalNode STAR() { return getToken(PlpgsqlParser.STAR, 0); }
		public FunctionCallArgsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_functionCallArgs; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).enterFunctionCallArgs(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).exitFunctionCallArgs(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PlpgsqlParserVisitor ) return ((PlpgsqlParserVisitor<? extends T>)visitor).visitFunctionCallArgs(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FunctionCallArgsContext functionCallArgs() throws RecognitionException {
		FunctionCallArgsContext _localctx = new FunctionCallArgsContext(_ctx, getState());
		enterRule(_localctx, 158, RULE_functionCallArgs);
		int _la;
		try {
			setState(1423);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,188,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1407);
				match(DISTINCT);
				setState(1408);
				expressionList();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1409);
				expressionList();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1410);
				expression(0);
				setState(1411);
				match(IN);
				setState(1412);
				expression(0);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(1414);
				expression(0);
				setState(1415);
				match(FROM);
				setState(1416);
				expression(0);
				setState(1419);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==FOR) {
					{
					setState(1417);
					match(FOR);
					setState(1418);
					expression(0);
					}
				}

				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(1421);
				match(STAR);
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class WindowFunctionContext extends ParserRuleContext {
		public FunctionCallContext functionCall() {
			return getRuleContext(FunctionCallContext.class,0);
		}
		public TerminalNode OVER() { return getToken(PlpgsqlParser.OVER, 0); }
		public TerminalNode LPAREN() { return getToken(PlpgsqlParser.LPAREN, 0); }
		public WindowSpecContext windowSpec() {
			return getRuleContext(WindowSpecContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(PlpgsqlParser.RPAREN, 0); }
		public WindowFunctionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_windowFunction; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).enterWindowFunction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).exitWindowFunction(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PlpgsqlParserVisitor ) return ((PlpgsqlParserVisitor<? extends T>)visitor).visitWindowFunction(this);
			else return visitor.visitChildren(this);
		}
	}

	public final WindowFunctionContext windowFunction() throws RecognitionException {
		WindowFunctionContext _localctx = new WindowFunctionContext(_ctx, getState());
		enterRule(_localctx, 160, RULE_windowFunction);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1425);
			functionCall();
			setState(1426);
			match(OVER);
			setState(1427);
			match(LPAREN);
			setState(1428);
			windowSpec();
			setState(1429);
			match(RPAREN);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class WindowSpecContext extends ParserRuleContext {
		public TerminalNode PARTITION() { return getToken(PlpgsqlParser.PARTITION, 0); }
		public TerminalNode BY() { return getToken(PlpgsqlParser.BY, 0); }
		public ExpressionListContext expressionList() {
			return getRuleContext(ExpressionListContext.class,0);
		}
		public OrderByClauseContext orderByClause() {
			return getRuleContext(OrderByClauseContext.class,0);
		}
		public WindowSpecContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_windowSpec; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).enterWindowSpec(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).exitWindowSpec(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PlpgsqlParserVisitor ) return ((PlpgsqlParserVisitor<? extends T>)visitor).visitWindowSpec(this);
			else return visitor.visitChildren(this);
		}
	}

	public final WindowSpecContext windowSpec() throws RecognitionException {
		WindowSpecContext _localctx = new WindowSpecContext(_ctx, getState());
		enterRule(_localctx, 162, RULE_windowSpec);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1434);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==PARTITION) {
				{
				setState(1431);
				match(PARTITION);
				setState(1432);
				match(BY);
				setState(1433);
				expressionList();
				}
			}

			setState(1437);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ORDER) {
				{
				setState(1436);
				orderByClause();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class CteStmtContext extends ParserRuleContext {
		public TerminalNode WITH() { return getToken(PlpgsqlParser.WITH, 0); }
		public CteListContext cteList() {
			return getRuleContext(CteListContext.class,0);
		}
		public InsertStmtContext insertStmt() {
			return getRuleContext(InsertStmtContext.class,0);
		}
		public UpdateStmtContext updateStmt() {
			return getRuleContext(UpdateStmtContext.class,0);
		}
		public DeleteStmtContext deleteStmt() {
			return getRuleContext(DeleteStmtContext.class,0);
		}
		public SelectIntoStmtContext selectIntoStmt() {
			return getRuleContext(SelectIntoStmtContext.class,0);
		}
		public List<TerminalNode> SEMI() { return getTokens(PlpgsqlParser.SEMI); }
		public TerminalNode SEMI(int i) {
			return getToken(PlpgsqlParser.SEMI, i);
		}
		public CteStmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_cteStmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).enterCteStmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).exitCteStmt(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PlpgsqlParserVisitor ) return ((PlpgsqlParserVisitor<? extends T>)visitor).visitCteStmt(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CteStmtContext cteStmt() throws RecognitionException {
		CteStmtContext _localctx = new CteStmtContext(_ctx, getState());
		enterRule(_localctx, 164, RULE_cteStmt);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1439);
			match(WITH);
			setState(1440);
			cteList();
			setState(1451);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,192,_ctx) ) {
			case 1:
				{
				setState(1441);
				insertStmt();
				}
				break;
			case 2:
				{
				setState(1442);
				updateStmt();
				}
				break;
			case 3:
				{
				setState(1443);
				deleteStmt();
				}
				break;
			case 4:
				{
				setState(1444);
				selectIntoStmt();
				}
				break;
			case 5:
				{
				setState(1446); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(1445);
					_la = _input.LA(1);
					if ( _la <= 0 || (_la==SEMI) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					}
					}
					setState(1448); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & -2L) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & -1L) != 0) || ((((_la - 128)) & ~0x3f) == 0 && ((1L << (_la - 128)) & -288230376151711745L) != 0) || ((((_la - 192)) & ~0x3f) == 0 && ((1L << (_la - 192)) & 4095L) != 0) );
				setState(1450);
				match(SEMI);
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class SqlGenericStmtContext extends ParserRuleContext {
		public List<TerminalNode> SEMI() { return getTokens(PlpgsqlParser.SEMI); }
		public TerminalNode SEMI(int i) {
			return getToken(PlpgsqlParser.SEMI, i);
		}
		public TerminalNode CREATE() { return getToken(PlpgsqlParser.CREATE, 0); }
		public TerminalNode DROP() { return getToken(PlpgsqlParser.DROP, 0); }
		public TerminalNode ALTER() { return getToken(PlpgsqlParser.ALTER, 0); }
		public TerminalNode TRUNCATE() { return getToken(PlpgsqlParser.TRUNCATE, 0); }
		public TerminalNode ANALYZE() { return getToken(PlpgsqlParser.ANALYZE, 0); }
		public TerminalNode VACUUM() { return getToken(PlpgsqlParser.VACUUM, 0); }
		public TerminalNode EXPLAIN() { return getToken(PlpgsqlParser.EXPLAIN, 0); }
		public TerminalNode LOCK() { return getToken(PlpgsqlParser.LOCK, 0); }
		public TerminalNode REINDEX() { return getToken(PlpgsqlParser.REINDEX, 0); }
		public TerminalNode CLUSTER() { return getToken(PlpgsqlParser.CLUSTER, 0); }
		public TerminalNode COMMENT() { return getToken(PlpgsqlParser.COMMENT, 0); }
		public TerminalNode CALL() { return getToken(PlpgsqlParser.CALL, 0); }
		public SqlGenericStmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sqlGenericStmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).enterSqlGenericStmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).exitSqlGenericStmt(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PlpgsqlParserVisitor ) return ((PlpgsqlParserVisitor<? extends T>)visitor).visitSqlGenericStmt(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SqlGenericStmtContext sqlGenericStmt() throws RecognitionException {
		SqlGenericStmtContext _localctx = new SqlGenericStmtContext(_ctx, getState());
		enterRule(_localctx, 166, RULE_sqlGenericStmt);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1453);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 2203316125696L) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(1457);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & -2L) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & -1L) != 0) || ((((_la - 128)) & ~0x3f) == 0 && ((1L << (_la - 128)) & -288230376151711745L) != 0) || ((((_la - 192)) & ~0x3f) == 0 && ((1L << (_la - 192)) & 4095L) != 0)) {
				{
				{
				setState(1454);
				_la = _input.LA(1);
				if ( _la <= 0 || (_la==SEMI) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				}
				setState(1459);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(1460);
			match(SEMI);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class LiteralContext extends ParserRuleContext {
		public TerminalNode IntegerLiteral() { return getToken(PlpgsqlParser.IntegerLiteral, 0); }
		public TerminalNode NumericLiteral() { return getToken(PlpgsqlParser.NumericLiteral, 0); }
		public TerminalNode StringLiteral() { return getToken(PlpgsqlParser.StringLiteral, 0); }
		public TerminalNode DollarQuotedString() { return getToken(PlpgsqlParser.DollarQuotedString, 0); }
		public TerminalNode NULL() { return getToken(PlpgsqlParser.NULL, 0); }
		public TerminalNode TRUE() { return getToken(PlpgsqlParser.TRUE, 0); }
		public TerminalNode FALSE() { return getToken(PlpgsqlParser.FALSE, 0); }
		public LiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_literal; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).enterLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).exitLiteral(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PlpgsqlParserVisitor ) return ((PlpgsqlParserVisitor<? extends T>)visitor).visitLiteral(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LiteralContext literal() throws RecognitionException {
		LiteralContext _localctx = new LiteralContext(_ctx, getState());
		enterRule(_localctx, 168, RULE_literal);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1462);
			_la = _input.LA(1);
			if ( !(((((_la - 134)) & ~0x3f) == 0 && ((1L << (_la - 134)) & -2305843008811040767L) != 0) || _la==DollarQuotedString) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class CreateTempTableStmtContext extends ParserRuleContext {
		public TerminalNode CREATE() { return getToken(PlpgsqlParser.CREATE, 0); }
		public TerminalNode TABLE() { return getToken(PlpgsqlParser.TABLE, 0); }
		public TerminalNode Identifier() { return getToken(PlpgsqlParser.Identifier, 0); }
		public TerminalNode LPAREN() { return getToken(PlpgsqlParser.LPAREN, 0); }
		public CreateTableColumnDefListContext createTableColumnDefList() {
			return getRuleContext(CreateTableColumnDefListContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(PlpgsqlParser.RPAREN, 0); }
		public TerminalNode SEMI() { return getToken(PlpgsqlParser.SEMI, 0); }
		public TerminalNode IF() { return getToken(PlpgsqlParser.IF, 0); }
		public TerminalNode NOT() { return getToken(PlpgsqlParser.NOT, 0); }
		public TerminalNode EXISTS() { return getToken(PlpgsqlParser.EXISTS, 0); }
		public OnCommitClauseContext onCommitClause() {
			return getRuleContext(OnCommitClauseContext.class,0);
		}
		public TerminalNode TEMP() { return getToken(PlpgsqlParser.TEMP, 0); }
		public TerminalNode TEMPORARY() { return getToken(PlpgsqlParser.TEMPORARY, 0); }
		public CreateTempTableStmtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_createTempTableStmt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).enterCreateTempTableStmt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).exitCreateTempTableStmt(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PlpgsqlParserVisitor ) return ((PlpgsqlParserVisitor<? extends T>)visitor).visitCreateTempTableStmt(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CreateTempTableStmtContext createTempTableStmt() throws RecognitionException {
		CreateTempTableStmtContext _localctx = new CreateTempTableStmtContext(_ctx, getState());
		enterRule(_localctx, 170, RULE_createTempTableStmt);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1464);
			match(CREATE);
			setState(1466);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==TEMP || _la==TEMPORARY) {
				{
				setState(1465);
				_la = _input.LA(1);
				if ( !(_la==TEMP || _la==TEMPORARY) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
			}

			setState(1468);
			match(TABLE);
			setState(1472);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==IF) {
				{
				setState(1469);
				match(IF);
				setState(1470);
				match(NOT);
				setState(1471);
				match(EXISTS);
				}
			}

			setState(1474);
			match(Identifier);
			setState(1475);
			match(LPAREN);
			setState(1476);
			createTableColumnDefList();
			setState(1477);
			match(RPAREN);
			setState(1479);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ON) {
				{
				setState(1478);
				onCommitClause();
				}
			}

			setState(1481);
			match(SEMI);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class CreateTableColumnDefListContext extends ParserRuleContext {
		public List<CreateTableColumnDefContext> createTableColumnDef() {
			return getRuleContexts(CreateTableColumnDefContext.class);
		}
		public CreateTableColumnDefContext createTableColumnDef(int i) {
			return getRuleContext(CreateTableColumnDefContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(PlpgsqlParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(PlpgsqlParser.COMMA, i);
		}
		public CreateTableColumnDefListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_createTableColumnDefList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).enterCreateTableColumnDefList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).exitCreateTableColumnDefList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PlpgsqlParserVisitor ) return ((PlpgsqlParserVisitor<? extends T>)visitor).visitCreateTableColumnDefList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CreateTableColumnDefListContext createTableColumnDefList() throws RecognitionException {
		CreateTableColumnDefListContext _localctx = new CreateTableColumnDefListContext(_ctx, getState());
		enterRule(_localctx, 172, RULE_createTableColumnDefList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1483);
			createTableColumnDef();
			setState(1488);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(1484);
				match(COMMA);
				setState(1485);
				createTableColumnDef();
				}
				}
				setState(1490);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class CreateTableColumnDefContext extends ParserRuleContext {
		public TerminalNode Identifier() { return getToken(PlpgsqlParser.Identifier, 0); }
		public DataTypeContext dataType() {
			return getRuleContext(DataTypeContext.class,0);
		}
		public TerminalNode NULL() { return getToken(PlpgsqlParser.NULL, 0); }
		public TerminalNode DEFAULT() { return getToken(PlpgsqlParser.DEFAULT, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode NOT() { return getToken(PlpgsqlParser.NOT, 0); }
		public CreateTableColumnDefContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_createTableColumnDef; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).enterCreateTableColumnDef(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).exitCreateTableColumnDef(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PlpgsqlParserVisitor ) return ((PlpgsqlParserVisitor<? extends T>)visitor).visitCreateTableColumnDef(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CreateTableColumnDefContext createTableColumnDef() throws RecognitionException {
		CreateTableColumnDefContext _localctx = new CreateTableColumnDefContext(_ctx, getState());
		enterRule(_localctx, 174, RULE_createTableColumnDef);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1491);
			match(Identifier);
			setState(1492);
			dataType(0);
			setState(1497);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==NULL || _la==NOT) {
				{
				setState(1494);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NOT) {
					{
					setState(1493);
					match(NOT);
					}
				}

				setState(1496);
				match(NULL);
				}
			}

			setState(1501);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==DEFAULT) {
				{
				setState(1499);
				match(DEFAULT);
				setState(1500);
				expression(0);
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class OnCommitClauseContext extends ParserRuleContext {
		public TerminalNode ON() { return getToken(PlpgsqlParser.ON, 0); }
		public TerminalNode COMMIT() { return getToken(PlpgsqlParser.COMMIT, 0); }
		public TerminalNode DROP() { return getToken(PlpgsqlParser.DROP, 0); }
		public TerminalNode DELETE() { return getToken(PlpgsqlParser.DELETE, 0); }
		public TerminalNode ROWS() { return getToken(PlpgsqlParser.ROWS, 0); }
		public TerminalNode PRESERVE() { return getToken(PlpgsqlParser.PRESERVE, 0); }
		public OnCommitClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_onCommitClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).enterOnCommitClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof PlpgsqlParserListener ) ((PlpgsqlParserListener)listener).exitOnCommitClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof PlpgsqlParserVisitor ) return ((PlpgsqlParserVisitor<? extends T>)visitor).visitOnCommitClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OnCommitClauseContext onCommitClause() throws RecognitionException {
		OnCommitClauseContext _localctx = new OnCommitClauseContext(_ctx, getState());
		enterRule(_localctx, 176, RULE_onCommitClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1503);
			match(ON);
			setState(1504);
			match(COMMIT);
			setState(1510);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case DROP:
				{
				setState(1505);
				match(DROP);
				}
				break;
			case DELETE:
				{
				setState(1506);
				match(DELETE);
				setState(1507);
				match(ROWS);
				}
				break;
			case PRESERVE:
				{
				setState(1508);
				match(PRESERVE);
				setState(1509);
				match(ROWS);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 5:
			return dataType_sempred((DataTypeContext)_localctx, predIndex);
		case 73:
			return expression_sempred((ExpressionContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean dataType_sempred(DataTypeContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return precpred(_ctx, 1);
		}
		return true;
	}
	private boolean expression_sempred(ExpressionContext _localctx, int predIndex) {
		switch (predIndex) {
		case 1:
			return precpred(_ctx, 14);
		case 2:
			return precpred(_ctx, 13);
		case 3:
			return precpred(_ctx, 12);
		case 4:
			return precpred(_ctx, 10);
		case 5:
			return precpred(_ctx, 9);
		case 6:
			return precpred(_ctx, 8);
		case 7:
			return precpred(_ctx, 6);
		case 8:
			return precpred(_ctx, 22);
		case 9:
			return precpred(_ctx, 11);
		case 10:
			return precpred(_ctx, 7);
		case 11:
			return precpred(_ctx, 4);
		case 12:
			return precpred(_ctx, 3);
		}
		return true;
	}

	public static final String _serializedATN =
		"\u0004\u0001\u00cb\u05e9\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001"+
		"\u0002\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004"+
		"\u0002\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007"+
		"\u0002\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b"+
		"\u0002\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002\u000f\u0007"+
		"\u000f\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011\u0002\u0012\u0007"+
		"\u0012\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014\u0002\u0015\u0007"+
		"\u0015\u0002\u0016\u0007\u0016\u0002\u0017\u0007\u0017\u0002\u0018\u0007"+
		"\u0018\u0002\u0019\u0007\u0019\u0002\u001a\u0007\u001a\u0002\u001b\u0007"+
		"\u001b\u0002\u001c\u0007\u001c\u0002\u001d\u0007\u001d\u0002\u001e\u0007"+
		"\u001e\u0002\u001f\u0007\u001f\u0002 \u0007 \u0002!\u0007!\u0002\"\u0007"+
		"\"\u0002#\u0007#\u0002$\u0007$\u0002%\u0007%\u0002&\u0007&\u0002\'\u0007"+
		"\'\u0002(\u0007(\u0002)\u0007)\u0002*\u0007*\u0002+\u0007+\u0002,\u0007"+
		",\u0002-\u0007-\u0002.\u0007.\u0002/\u0007/\u00020\u00070\u00021\u0007"+
		"1\u00022\u00072\u00023\u00073\u00024\u00074\u00025\u00075\u00026\u0007"+
		"6\u00027\u00077\u00028\u00078\u00029\u00079\u0002:\u0007:\u0002;\u0007"+
		";\u0002<\u0007<\u0002=\u0007=\u0002>\u0007>\u0002?\u0007?\u0002@\u0007"+
		"@\u0002A\u0007A\u0002B\u0007B\u0002C\u0007C\u0002D\u0007D\u0002E\u0007"+
		"E\u0002F\u0007F\u0002G\u0007G\u0002H\u0007H\u0002I\u0007I\u0002J\u0007"+
		"J\u0002K\u0007K\u0002L\u0007L\u0002M\u0007M\u0002N\u0007N\u0002O\u0007"+
		"O\u0002P\u0007P\u0002Q\u0007Q\u0002R\u0007R\u0002S\u0007S\u0002T\u0007"+
		"T\u0002U\u0007U\u0002V\u0007V\u0002W\u0007W\u0002X\u0007X\u0001\u0000"+
		"\u0003\u0000\u00b4\b\u0000\u0001\u0000\u0003\u0000\u00b7\b\u0000\u0001"+
		"\u0000\u0001\u0000\u0001\u0000\u0003\u0000\u00bc\b\u0000\u0001\u0000\u0001"+
		"\u0000\u0003\u0000\u00c0\b\u0000\u0001\u0000\u0003\u0000\u00c3\b\u0000"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0002\u0001\u0002"+
		"\u0001\u0002\u0001\u0003\u0004\u0003\u00cd\b\u0003\u000b\u0003\f\u0003"+
		"\u00ce\u0001\u0004\u0001\u0004\u0003\u0004\u00d3\b\u0004\u0001\u0004\u0001"+
		"\u0004\u0001\u0004\u0003\u0004\u00d8\b\u0004\u0001\u0004\u0001\u0004\u0001"+
		"\u0004\u0001\u0004\u0003\u0004\u00de\b\u0004\u0001\u0004\u0001\u0004\u0001"+
		"\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0003"+
		"\u0004\u00e8\b\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001"+
		"\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0003"+
		"\u0004\u00f4\b\u0004\u0001\u0004\u0003\u0004\u00f7\b\u0004\u0001\u0005"+
		"\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005"+
		"\u0001\u0005\u0001\u0005\u0003\u0005\u0102\b\u0005\u0001\u0005\u0003\u0005"+
		"\u0105\b\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005"+
		"\u0003\u0005\u010c\b\u0005\u0001\u0005\u0003\u0005\u010f\b\u0005\u0001"+
		"\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0003"+
		"\u0005\u0117\b\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0003"+
		"\u0005\u011d\b\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001"+
		"\u0005\u0001\u0005\u0001\u0005\u0003\u0005\u0126\b\u0005\u0001\u0005\u0001"+
		"\u0005\u0001\u0005\u0001\u0005\u0003\u0005\u012c\b\u0005\u0001\u0005\u0001"+
		"\u0005\u0001\u0005\u0003\u0005\u0131\b\u0005\u0001\u0005\u0001\u0005\u0001"+
		"\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001"+
		"\u0005\u0001\u0005\u0001\u0005\u0003\u0005\u013e\b\u0005\u0003\u0005\u0140"+
		"\b\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0003\u0005\u0146"+
		"\b\u0005\u0005\u0005\u0148\b\u0005\n\u0005\f\u0005\u014b\t\u0005\u0001"+
		"\u0006\u0005\u0006\u014e\b\u0006\n\u0006\f\u0006\u0151\t\u0006\u0001\u0007"+
		"\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007"+
		"\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007"+
		"\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007"+
		"\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007"+
		"\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0003\u0007"+
		"\u0171\b\u0007\u0001\b\u0001\b\u0001\b\u0001\b\u0001\b\u0001\t\u0001\t"+
		"\u0001\t\u0005\t\u017b\b\t\n\t\f\t\u017e\t\t\u0001\t\u0001\t\u0001\t\u0005"+
		"\t\u0183\b\t\n\t\f\t\u0186\t\t\u0003\t\u0188\b\t\u0001\n\u0003\n\u018b"+
		"\b\n\u0001\n\u0001\n\u0001\n\u0001\n\u0003\n\u0191\b\n\u0001\n\u0001\n"+
		"\u0003\n\u0195\b\n\u0001\n\u0003\n\u0198\b\n\u0001\n\u0003\n\u019b\b\n"+
		"\u0001\n\u0003\n\u019e\b\n\u0001\n\u0003\n\u01a1\b\n\u0001\n\u0003\n\u01a4"+
		"\b\n\u0001\n\u0001\n\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0005"+
		"\u000b\u01ac\b\u000b\n\u000b\f\u000b\u01af\t\u000b\u0003\u000b\u01b1\b"+
		"\u000b\u0001\f\u0001\f\u0003\f\u01b5\b\f\u0001\f\u0003\f\u01b8\b\f\u0001"+
		"\r\u0001\r\u0001\r\u0005\r\u01bd\b\r\n\r\f\r\u01c0\t\r\u0001\u000e\u0001"+
		"\u000e\u0001\u000e\u0005\u000e\u01c5\b\u000e\n\u000e\f\u000e\u01c8\t\u000e"+
		"\u0001\u000f\u0001\u000f\u0001\u000f\u0005\u000f\u01cd\b\u000f\n\u000f"+
		"\f\u000f\u01d0\t\u000f\u0001\u000f\u0003\u000f\u01d3\b\u000f\u0001\u000f"+
		"\u0003\u000f\u01d6\b\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0005\u000f"+
		"\u01db\b\u000f\n\u000f\f\u000f\u01de\t\u000f\u0001\u000f\u0003\u000f\u01e1"+
		"\b\u000f\u0001\u000f\u0003\u000f\u01e4\b\u000f\u0001\u000f\u0001\u000f"+
		"\u0001\u000f\u0003\u000f\u01e9\b\u000f\u0001\u000f\u0003\u000f\u01ec\b"+
		"\u000f\u0001\u000f\u0003\u000f\u01ef\b\u000f\u0001\u000f\u0001\u000f\u0001"+
		"\u000f\u0001\u000f\u0003\u000f\u01f5\b\u000f\u0001\u000f\u0003\u000f\u01f8"+
		"\b\u000f\u0003\u000f\u01fa\b\u000f\u0001\u0010\u0001\u0010\u0001\u0010"+
		"\u0003\u0010\u01ff\b\u0010\u0001\u0010\u0001\u0010\u0001\u0010\u0001\u0010"+
		"\u0001\u0010\u0001\u0010\u0001\u0010\u0001\u0010\u0001\u0010\u0003\u0010"+
		"\u020a\b\u0010\u0003\u0010\u020c\b\u0010\u0001\u0011\u0001\u0011\u0001"+
		"\u0011\u0003\u0011\u0211\b\u0011\u0001\u0011\u0001\u0011\u0003\u0011\u0215"+
		"\b\u0011\u0001\u0011\u0001\u0011\u0003\u0011\u0219\b\u0011\u0001\u0011"+
		"\u0003\u0011\u021c\b\u0011\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0013"+
		"\u0001\u0013\u0001\u0013\u0003\u0013\u0224\b\u0013\u0001\u0013\u0003\u0013"+
		"\u0227\b\u0013\u0001\u0013\u0001\u0013\u0001\u0014\u0001\u0014\u0001\u0014"+
		"\u0001\u0014\u0003\u0014\u022f\b\u0014\u0001\u0014\u0001\u0014\u0003\u0014"+
		"\u0233\b\u0014\u0001\u0014\u0001\u0014\u0001\u0015\u0001\u0015\u0001\u0015"+
		"\u0001\u0015\u0001\u0015\u0005\u0015\u023c\b\u0015\n\u0015\f\u0015\u023f"+
		"\t\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0003\u0015\u0245"+
		"\b\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001"+
		"\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0005\u0015\u0250\b\u0015\n"+
		"\u0015\f\u0015\u0253\t\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001"+
		"\u0015\u0003\u0015\u0259\b\u0015\u0003\u0015\u025b\b\u0015\u0001\u0015"+
		"\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015"+
		"\u0005\u0015\u0264\b\u0015\n\u0015\f\u0015\u0267\t\u0015\u0001\u0015\u0001"+
		"\u0015\u0001\u0015\u0001\u0015\u0003\u0015\u026d\b\u0015\u0001\u0015\u0004"+
		"\u0015\u0270\b\u0015\u000b\u0015\f\u0015\u0271\u0001\u0015\u0003\u0015"+
		"\u0275\b\u0015\u0001\u0016\u0001\u0016\u0001\u0016\u0005\u0016\u027a\b"+
		"\u0016\n\u0016\f\u0016\u027d\t\u0016\u0001\u0017\u0001\u0017\u0001\u0017"+
		"\u0005\u0017\u0282\b\u0017\n\u0017\f\u0017\u0285\t\u0017\u0001\u0018\u0003"+
		"\u0018\u0288\b\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0005"+
		"\u0018\u028e\b\u0018\n\u0018\f\u0018\u0291\t\u0018\u0001\u0018\u0003\u0018"+
		"\u0294\b\u0018\u0001\u0018\u0003\u0018\u0297\b\u0018\u0001\u0018\u0001"+
		"\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0005\u0018\u029e\b\u0018\n"+
		"\u0018\f\u0018\u02a1\t\u0018\u0003\u0018\u02a3\b\u0018\u0001\u0018\u0003"+
		"\u0018\u02a6\b\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0003"+
		"\u0018\u02ac\b\u0018\u0003\u0018\u02ae\b\u0018\u0001\u0018\u0001\u0018"+
		"\u0001\u0019\u0001\u0019\u0001\u0019\u0005\u0019\u02b5\b\u0019\n\u0019"+
		"\f\u0019\u02b8\t\u0019\u0001\u001a\u0001\u001a\u0001\u001a\u0005\u001a"+
		"\u02bd\b\u001a\n\u001a\f\u001a\u02c0\t\u001a\u0001\u001a\u0001\u001a\u0001"+
		"\u001a\u0001\u001b\u0003\u001b\u02c6\b\u001b\u0001\u001b\u0001\u001b\u0001"+
		"\u001b\u0001\u001b\u0001\u001b\u0005\u001b\u02cd\b\u001b\n\u001b\f\u001b"+
		"\u02d0\t\u001b\u0001\u001b\u0003\u001b\u02d3\b\u001b\u0001\u001b\u0003"+
		"\u001b\u02d6\b\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0005"+
		"\u001b\u02dc\b\u001b\n\u001b\f\u001b\u02df\t\u001b\u0003\u001b\u02e1\b"+
		"\u001b\u0001\u001b\u0003\u001b\u02e4\b\u001b\u0001\u001b\u0001\u001b\u0001"+
		"\u001b\u0001\u001b\u0003\u001b\u02ea\b\u001b\u0003\u001b\u02ec\b\u001b"+
		"\u0001\u001b\u0001\u001b\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c"+
		"\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c"+
		"\u0001\u001c\u0003\u001c\u02fb\b\u001c\u0001\u001d\u0001\u001d\u0001\u001d"+
		"\u0001\u001d\u0001\u001d\u0001\u001d\u0001\u001d\u0001\u001d\u0001\u001d"+
		"\u0005\u001d\u0306\b\u001d\n\u001d\f\u001d\u0309\t\u001d\u0001\u001d\u0001"+
		"\u001d\u0003\u001d\u030d\b\u001d\u0001\u001d\u0001\u001d\u0001\u001d\u0001"+
		"\u001d\u0001\u001e\u0001\u001e\u0003\u001e\u0315\b\u001e\u0001\u001e\u0001"+
		"\u001e\u0001\u001e\u0003\u001e\u031a\b\u001e\u0001\u001e\u0001\u001e\u0001"+
		"\u001e\u0001\u001e\u0001\u001f\u0004\u001f\u0321\b\u001f\u000b\u001f\f"+
		"\u001f\u0322\u0001 \u0001 \u0001 \u0001 \u0001 \u0001!\u0003!\u032b\b"+
		"!\u0001!\u0001!\u0001!\u0001!\u0001!\u0003!\u0332\b!\u0001!\u0001!\u0001"+
		"\"\u0003\"\u0337\b\"\u0001\"\u0001\"\u0001\"\u0001\"\u0001\"\u0001\"\u0001"+
		"\"\u0003\"\u0340\b\"\u0001\"\u0001\"\u0001#\u0003#\u0345\b#\u0001#\u0001"+
		"#\u0001#\u0001#\u0003#\u034b\b#\u0001#\u0001#\u0001#\u0001#\u0001#\u0001"+
		"#\u0003#\u0353\b#\u0001#\u0001#\u0001#\u0001#\u0001#\u0003#\u035a\b#\u0001"+
		"#\u0001#\u0001#\u0003#\u035f\b#\u0001#\u0001#\u0001#\u0001#\u0001#\u0001"+
		"#\u0001#\u0001#\u0001#\u0003#\u036a\b#\u0001#\u0001#\u0003#\u036e\b#\u0001"+
		"$\u0001$\u0001$\u0001$\u0001$\u0003$\u0375\b$\u0001$\u0003$\u0378\b$\u0001"+
		"$\u0003$\u037b\b$\u0001$\u0003$\u037e\b$\u0001$\u0003$\u0381\b$\u0001"+
		"$\u0003$\u0384\b$\u0001$\u0003$\u0387\b$\u0001%\u0003%\u038a\b%\u0001"+
		"%\u0001%\u0001%\u0001%\u0003%\u0390\b%\u0001%\u0001%\u0001%\u0001%\u0001"+
		"%\u0001%\u0001%\u0001%\u0003%\u039a\b%\u0001%\u0001%\u0001&\u0001&\u0003"+
		"&\u03a0\b&\u0001&\u0001&\u0003&\u03a4\b&\u0001&\u0001&\u0001\'\u0001\'"+
		"\u0003\'\u03aa\b\'\u0001\'\u0001\'\u0003\'\u03ae\b\'\u0001\'\u0001\'\u0001"+
		"(\u0001(\u0003(\u03b4\b(\u0001(\u0001(\u0001(\u0001(\u0001(\u0001(\u0001"+
		"(\u0001(\u0001(\u0001(\u0001(\u0001(\u0001(\u0001(\u0001(\u0001(\u0001"+
		"(\u0003(\u03c7\b(\u0001(\u0001(\u0003(\u03cb\b(\u0001)\u0001)\u0003)\u03cf"+
		"\b)\u0001)\u0001)\u0001)\u0003)\u03d4\b)\u0001)\u0003)\u03d7\b)\u0001"+
		")\u0001)\u0003)\u03db\b)\u0001)\u0001)\u0001*\u0001*\u0001*\u0005*\u03e2"+
		"\b*\n*\f*\u03e5\t*\u0001+\u0001+\u0001+\u0001+\u0001,\u0001,\u0001,\u0001"+
		",\u0003,\u03ef\b,\u0001,\u0001,\u0001-\u0001-\u0001-\u0001.\u0001.\u0003"+
		".\u03f8\b.\u0001.\u0001.\u0001/\u0001/\u0003/\u03fe\b/\u0001/\u0001/\u0003"+
		"/\u0402\b/\u0001/\u0003/\u0405\b/\u0001/\u0001/\u00010\u00030\u040a\b"+
		"0\u00010\u00030\u040d\b0\u00010\u00030\u0410\b0\u00010\u00010\u00010\u0003"+
		"0\u0415\b0\u00010\u00010\u00030\u0419\b0\u00010\u00010\u00011\u00011\u0001"+
		"1\u00012\u00042\u0421\b2\u000b2\f2\u0422\u00013\u00013\u00013\u00013\u0001"+
		"3\u00014\u00014\u00014\u00054\u042d\b4\n4\f4\u0430\t4\u00015\u00015\u0001"+
		"5\u00035\u0435\b5\u00016\u00016\u00036\u0439\b6\u00016\u00016\u00016\u0001"+
		"6\u00017\u00017\u00017\u00057\u0442\b7\n7\f7\u0445\t7\u00018\u00018\u0001"+
		"8\u00018\u00019\u00019\u0001:\u0001:\u0001:\u0001:\u0001:\u0001:\u0003"+
		":\u0453\b:\u0001:\u0001:\u0003:\u0457\b:\u0001:\u0001:\u0001;\u0001;\u0001"+
		";\u0001;\u0001<\u0001<\u0001<\u0001<\u0001<\u0001<\u0001=\u0003=\u0466"+
		"\b=\u0001=\u0001=\u0001=\u0003=\u046b\b=\u0001=\u0005=\u046e\b=\n=\f="+
		"\u0471\t=\u0001>\u0001>\u0001>\u0001?\u0001?\u0001?\u0005?\u0479\b?\n"+
		"?\f?\u047c\t?\u0001@\u0001@\u0001@\u0001@\u0001@\u0003@\u0483\b@\u0001"+
		"@\u0001@\u0001@\u0001@\u0001@\u0001A\u0001A\u0001A\u0001A\u0001A\u0001"+
		"A\u0001A\u0003A\u0491\bA\u0001A\u0003A\u0494\bA\u0001A\u0001A\u0003A\u0498"+
		"\bA\u0001A\u0003A\u049b\bA\u0001A\u0003A\u049e\bA\u0001A\u0003A\u04a1"+
		"\bA\u0001A\u0003A\u04a4\bA\u0001A\u0003A\u04a7\bA\u0001B\u0001B\u0001"+
		"B\u0001B\u0001C\u0001C\u0001C\u0001D\u0001D\u0001D\u0001D\u0001D\u0005"+
		"D\u04b5\bD\nD\fD\u04b8\tD\u0001E\u0001E\u0003E\u04bc\bE\u0001E\u0001E"+
		"\u0003E\u04c0\bE\u0001F\u0001F\u0001F\u0003F\u04c5\bF\u0001F\u0001F\u0003"+
		"F\u04c9\bF\u0001G\u0001G\u0001G\u0005G\u04ce\bG\nG\fG\u04d1\tG\u0001H"+
		"\u0001H\u0001H\u0001I\u0001I\u0001I\u0001I\u0001I\u0001I\u0001I\u0001"+
		"I\u0001I\u0001I\u0001I\u0001I\u0001I\u0001I\u0001I\u0001I\u0001I\u0001"+
		"I\u0001I\u0001I\u0001I\u0001I\u0001I\u0001I\u0001I\u0001I\u0001I\u0001"+
		"I\u0001I\u0001I\u0001I\u0001I\u0001I\u0001I\u0001I\u0001I\u0001I\u0001"+
		"I\u0001I\u0001I\u0001I\u0001I\u0001I\u0001I\u0001I\u0001I\u0001I\u0001"+
		"I\u0001I\u0001I\u0001I\u0001I\u0001I\u0001I\u0003I\u050c\bI\u0001I\u0001"+
		"I\u0001I\u0003I\u0511\bI\u0001I\u0001I\u0001I\u0003I\u0516\bI\u0001I\u0001"+
		"I\u0001I\u0001I\u0001I\u0001I\u0001I\u0001I\u0001I\u0001I\u0001I\u0003"+
		"I\u0523\bI\u0001I\u0001I\u0001I\u0001I\u0001I\u0001I\u0001I\u0003I\u052c"+
		"\bI\u0001I\u0001I\u0001I\u0001I\u0001I\u0001I\u0001I\u0001I\u0001I\u0001"+
		"I\u0001I\u0001I\u0001I\u0001I\u0001I\u0001I\u0001I\u0001I\u0003I\u0540"+
		"\bI\u0001I\u0001I\u0001I\u0001I\u0001I\u0001I\u0003I\u0548\bI\u0001I\u0001"+
		"I\u0001I\u0001I\u0001I\u0003I\u054f\bI\u0001I\u0001I\u0001I\u0003I\u0554"+
		"\bI\u0001I\u0001I\u0001I\u0001I\u0003I\u055a\bI\u0001I\u0001I\u0005I\u055e"+
		"\bI\nI\fI\u0561\tI\u0001J\u0004J\u0564\bJ\u000bJ\fJ\u0565\u0001K\u0001"+
		"K\u0001K\u0001K\u0001K\u0001L\u0001L\u0001L\u0001L\u0001L\u0001L\u0001"+
		"L\u0001L\u0001L\u0001L\u0003L\u0577\bL\u0001M\u0001M\u0001N\u0001N\u0001"+
		"N\u0001N\u0001N\u0001O\u0001O\u0001O\u0001O\u0001O\u0001O\u0001O\u0001"+
		"O\u0001O\u0001O\u0001O\u0001O\u0003O\u058c\bO\u0001O\u0001O\u0003O\u0590"+
		"\bO\u0001P\u0001P\u0001P\u0001P\u0001P\u0001P\u0001Q\u0001Q\u0001Q\u0003"+
		"Q\u059b\bQ\u0001Q\u0003Q\u059e\bQ\u0001R\u0001R\u0001R\u0001R\u0001R\u0001"+
		"R\u0001R\u0004R\u05a7\bR\u000bR\fR\u05a8\u0001R\u0003R\u05ac\bR\u0001"+
		"S\u0001S\u0005S\u05b0\bS\nS\fS\u05b3\tS\u0001S\u0001S\u0001T\u0001T\u0001"+
		"U\u0001U\u0003U\u05bb\bU\u0001U\u0001U\u0001U\u0001U\u0003U\u05c1\bU\u0001"+
		"U\u0001U\u0001U\u0001U\u0001U\u0003U\u05c8\bU\u0001U\u0001U\u0001V\u0001"+
		"V\u0001V\u0005V\u05cf\bV\nV\fV\u05d2\tV\u0001W\u0001W\u0001W\u0003W\u05d7"+
		"\bW\u0001W\u0003W\u05da\bW\u0001W\u0001W\u0003W\u05de\bW\u0001X\u0001"+
		"X\u0001X\u0001X\u0001X\u0001X\u0001X\u0003X\u05e7\bX\u0001X\u0000\u0002"+
		"\n\u0092Y\u0000\u0002\u0004\u0006\b\n\f\u000e\u0010\u0012\u0014\u0016"+
		"\u0018\u001a\u001c\u001e \"$&(*,.02468:<>@BDFHJLNPRTVXZ\\^`bdfhjlnprt"+
		"vxz|~\u0080\u0082\u0084\u0086\u0088\u008a\u008c\u008e\u0090\u0092\u0094"+
		"\u0096\u0098\u009a\u009c\u009e\u00a0\u00a2\u00a4\u00a6\u00a8\u00aa\u00ac"+
		"\u00ae\u00b0\u0000\u0016\u0002\u0000\r\r\u0085\u0085\u0001\u0000\u00a4"+
		"\u00a5\u0001\u0000\u00ba\u00ba\u0002\u000000\u00a9\u00a9\u0002\u0000\u0004"+
		"\u0004vz\u0001\u0000{\u007f\u0001\u0000PQ\u0001\u0000\u0082\u0083\u0001"+
		"\u0000?A\u0001\u000089\u0001\u0000;<\u0002\u0000\u00a3\u00a3\u00b1\u00b2"+
		"\u0001\u0000\u00af\u00b0\u0001\u0000\u00b5\u00b6\u0001\u0000\u0088\u0089"+
		"\u0001\u0000\u00a9\u00ae\u0002\u000022\u00b8\u00b9\u0002\u0000{{\u009c"+
		"\u009c\u0003\u0000IInp\u00c7\u00c7\u0002\u0000\u0015\u001f))\u0003\u0000"+
		"\u0086\u0086\u00a1\u00a2\u00c3\u00c6\u0001\u0000!\"\u06b8\u0000\u00b3"+
		"\u0001\u0000\u0000\u0000\u0002\u00c4\u0001\u0000\u0000\u0000\u0004\u00c8"+
		"\u0001\u0000\u0000\u0000\u0006\u00cc\u0001\u0000\u0000\u0000\b\u00f6\u0001"+
		"\u0000\u0000\u0000\n\u013f\u0001\u0000\u0000\u0000\f\u014f\u0001\u0000"+
		"\u0000\u0000\u000e\u0170\u0001\u0000\u0000\u0000\u0010\u0172\u0001\u0000"+
		"\u0000\u0000\u0012\u0187\u0001\u0000\u0000\u0000\u0014\u018a\u0001\u0000"+
		"\u0000\u0000\u0016\u01b0\u0001\u0000\u0000\u0000\u0018\u01b2\u0001\u0000"+
		"\u0000\u0000\u001a\u01b9\u0001\u0000\u0000\u0000\u001c\u01c1\u0001\u0000"+
		"\u0000\u0000\u001e\u01f9\u0001\u0000\u0000\u0000 \u020b\u0001\u0000\u0000"+
		"\u0000\"\u021b\u0001\u0000\u0000\u0000$\u021d\u0001\u0000\u0000\u0000"+
		"&\u0220\u0001\u0000\u0000\u0000(\u022a\u0001\u0000\u0000\u0000*\u0274"+
		"\u0001\u0000\u0000\u0000,\u0276\u0001\u0000\u0000\u0000.\u027e\u0001\u0000"+
		"\u0000\u00000\u0287\u0001\u0000\u0000\u00002\u02b1\u0001\u0000\u0000\u0000"+
		"4\u02b9\u0001\u0000\u0000\u00006\u02c5\u0001\u0000\u0000\u00008\u02fa"+
		"\u0001\u0000\u0000\u0000:\u02fc\u0001\u0000\u0000\u0000<\u0312\u0001\u0000"+
		"\u0000\u0000>\u0320\u0001\u0000\u0000\u0000@\u0324\u0001\u0000\u0000\u0000"+
		"B\u032a\u0001\u0000\u0000\u0000D\u0336\u0001\u0000\u0000\u0000F\u036d"+
		"\u0001\u0000\u0000\u0000H\u0386\u0001\u0000\u0000\u0000J\u0389\u0001\u0000"+
		"\u0000\u0000L\u039d\u0001\u0000\u0000\u0000N\u03a7\u0001\u0000\u0000\u0000"+
		"P\u03ca\u0001\u0000\u0000\u0000R\u03cc\u0001\u0000\u0000\u0000T\u03de"+
		"\u0001\u0000\u0000\u0000V\u03e6\u0001\u0000\u0000\u0000X\u03ea\u0001\u0000"+
		"\u0000\u0000Z\u03f2\u0001\u0000\u0000\u0000\\\u03f5\u0001\u0000\u0000"+
		"\u0000^\u03fb\u0001\u0000\u0000\u0000`\u0409\u0001\u0000\u0000\u0000b"+
		"\u041c\u0001\u0000\u0000\u0000d\u0420\u0001\u0000\u0000\u0000f\u0424\u0001"+
		"\u0000\u0000\u0000h\u0429\u0001\u0000\u0000\u0000j\u0434\u0001\u0000\u0000"+
		"\u0000l\u0436\u0001\u0000\u0000\u0000n\u043e\u0001\u0000\u0000\u0000p"+
		"\u0446\u0001\u0000\u0000\u0000r\u044a\u0001\u0000\u0000\u0000t\u044c\u0001"+
		"\u0000\u0000\u0000v\u045a\u0001\u0000\u0000\u0000x\u045e\u0001\u0000\u0000"+
		"\u0000z\u0465\u0001\u0000\u0000\u0000|\u0472\u0001\u0000\u0000\u0000~"+
		"\u0475\u0001\u0000\u0000\u0000\u0080\u047d\u0001\u0000\u0000\u0000\u0082"+
		"\u0489\u0001\u0000\u0000\u0000\u0084\u04a8\u0001\u0000\u0000\u0000\u0086"+
		"\u04ac\u0001\u0000\u0000\u0000\u0088\u04af\u0001\u0000\u0000\u0000\u008a"+
		"\u04b9\u0001\u0000\u0000\u0000\u008c\u04c1\u0001\u0000\u0000\u0000\u008e"+
		"\u04ca\u0001\u0000\u0000\u0000\u0090\u04d2\u0001\u0000\u0000\u0000\u0092"+
		"\u0515\u0001\u0000\u0000\u0000\u0094\u0563\u0001\u0000\u0000\u0000\u0096"+
		"\u0567\u0001\u0000\u0000\u0000\u0098\u0576\u0001\u0000\u0000\u0000\u009a"+
		"\u0578\u0001\u0000\u0000\u0000\u009c\u057a\u0001\u0000\u0000\u0000\u009e"+
		"\u058f\u0001\u0000\u0000\u0000\u00a0\u0591\u0001\u0000\u0000\u0000\u00a2"+
		"\u059a\u0001\u0000\u0000\u0000\u00a4\u059f\u0001\u0000\u0000\u0000\u00a6"+
		"\u05ad\u0001\u0000\u0000\u0000\u00a8\u05b6\u0001\u0000\u0000\u0000\u00aa"+
		"\u05b8\u0001\u0000\u0000\u0000\u00ac\u05cb\u0001\u0000\u0000\u0000\u00ae"+
		"\u05d3\u0001\u0000\u0000\u0000\u00b0\u05df\u0001\u0000\u0000\u0000\u00b2"+
		"\u00b4\u0003\u0002\u0001\u0000\u00b3\u00b2\u0001\u0000\u0000\u0000\u00b3"+
		"\u00b4\u0001\u0000\u0000\u0000\u00b4\u00b6\u0001\u0000\u0000\u0000\u00b5"+
		"\u00b7\u0003\u0004\u0002\u0000\u00b6\u00b5\u0001\u0000\u0000\u0000\u00b6"+
		"\u00b7\u0001\u0000\u0000\u0000\u00b7\u00b8\u0001\u0000\u0000\u0000\u00b8"+
		"\u00b9\u0005\u0002\u0000\u0000\u00b9\u00bb\u0003\f\u0006\u0000\u00ba\u00bc"+
		"\u0003b1\u0000\u00bb\u00ba\u0001\u0000\u0000\u0000\u00bb\u00bc\u0001\u0000"+
		"\u0000\u0000\u00bc\u00bd\u0001\u0000\u0000\u0000\u00bd\u00bf\u0005\u0003"+
		"\u0000\u0000\u00be\u00c0\u0003\u0002\u0001\u0000\u00bf\u00be\u0001\u0000"+
		"\u0000\u0000\u00bf\u00c0\u0001\u0000\u0000\u0000\u00c0\u00c2\u0001\u0000"+
		"\u0000\u0000\u00c1\u00c3\u0005\u00ba\u0000\u0000\u00c2\u00c1\u0001\u0000"+
		"\u0000\u0000\u00c2\u00c3\u0001\u0000\u0000\u0000\u00c3\u0001\u0001\u0000"+
		"\u0000\u0000\u00c4\u00c5\u0005\u00c1\u0000\u0000\u00c5\u00c6\u0005\u00c7"+
		"\u0000\u0000\u00c6\u00c7\u0005\u00c2\u0000\u0000\u00c7\u0003\u0001\u0000"+
		"\u0000\u0000\u00c8\u00c9\u0005\u0001\u0000\u0000\u00c9\u00ca\u0003\u0006"+
		"\u0003\u0000\u00ca\u0005\u0001\u0000\u0000\u0000\u00cb\u00cd\u0003\b\u0004"+
		"\u0000\u00cc\u00cb\u0001\u0000\u0000\u0000\u00cd\u00ce\u0001\u0000\u0000"+
		"\u0000\u00ce\u00cc\u0001\u0000\u0000\u0000\u00ce\u00cf\u0001\u0000\u0000"+
		"\u0000\u00cf\u0007\u0001\u0000\u0000\u0000\u00d0\u00d2\u0005\u00c7\u0000"+
		"\u0000\u00d1\u00d3\u0005\u008a\u0000\u0000\u00d2\u00d1\u0001\u0000\u0000"+
		"\u0000\u00d2\u00d3\u0001\u0000\u0000\u0000\u00d3\u00d4\u0001\u0000\u0000"+
		"\u0000\u00d4\u00d7\u0003\n\u0005\u0000\u00d5\u00d6\u0005\u0087\u0000\u0000"+
		"\u00d6\u00d8\u0005\u0086\u0000\u0000\u00d7\u00d5\u0001\u0000\u0000\u0000"+
		"\u00d7\u00d8\u0001\u0000\u0000\u0000\u00d8\u00dd\u0001\u0000\u0000\u0000"+
		"\u00d9\u00da\u0005\u00a7\u0000\u0000\u00da\u00de\u0003\u0092I\u0000\u00db"+
		"\u00dc\u0005\u008b\u0000\u0000\u00dc\u00de\u0003\u0092I\u0000\u00dd\u00d9"+
		"\u0001\u0000\u0000\u0000\u00dd\u00db\u0001\u0000\u0000\u0000\u00dd\u00de"+
		"\u0001\u0000\u0000\u0000\u00de\u00df\u0001\u0000\u0000\u0000\u00df\u00e0"+
		"\u0005\u00ba\u0000\u0000\u00e0\u00f7\u0001\u0000\u0000\u0000\u00e1\u00e2"+
		"\u0005\u00c7\u0000\u0000\u00e2\u00e7\u0005\u009d\u0000\u0000\u00e3\u00e4"+
		"\u0005\u00bd\u0000\u0000\u00e4\u00e5\u0003\u008eG\u0000\u00e5\u00e6\u0005"+
		"\u00be\u0000\u0000\u00e6\u00e8\u0001\u0000\u0000\u0000\u00e7\u00e3\u0001"+
		"\u0000\u0000\u0000\u00e7\u00e8\u0001\u0000\u0000\u0000\u00e8\u00e9\u0001"+
		"\u0000\u0000\u0000\u00e9\u00ea\u0007\u0000\u0000\u0000\u00ea\u00eb\u0003"+
		"z=\u0000\u00eb\u00ec\u0005\u00ba\u0000\u0000\u00ec\u00f7\u0001\u0000\u0000"+
		"\u0000\u00ed\u00ee\u0005\u00c7\u0000\u0000\u00ee\u00ef\u0005\u009e\u0000"+
		"\u0000\u00ef\u00f3\u0005\r\u0000\u0000\u00f0\u00f4\u0005\u00c7\u0000\u0000"+
		"\u00f1\u00f2\u0005\u00a6\u0000\u0000\u00f2\u00f4\u0005\u00c3\u0000\u0000"+
		"\u00f3\u00f0\u0001\u0000\u0000\u0000\u00f3\u00f1\u0001\u0000\u0000\u0000"+
		"\u00f4\u00f5\u0001\u0000\u0000\u0000\u00f5\u00f7\u0005\u00ba\u0000\u0000"+
		"\u00f6\u00d0\u0001\u0000\u0000\u0000\u00f6\u00e1\u0001\u0000\u0000\u0000"+
		"\u00f6\u00ed\u0001\u0000\u0000\u0000\u00f7\t\u0001\u0000\u0000\u0000\u00f8"+
		"\u00f9\u0006\u0005\uffff\uffff\u0000\u00f9\u0140\u0005R\u0000\u0000\u00fa"+
		"\u0140\u0005S\u0000\u0000\u00fb\u0140\u0005T\u0000\u0000\u00fc\u0104\u0005"+
		"U\u0000\u0000\u00fd\u00fe\u0005\u00bd\u0000\u0000\u00fe\u0101\u0005\u00c3"+
		"\u0000\u0000\u00ff\u0100\u0005\u00bb\u0000\u0000\u0100\u0102\u0005\u00c3"+
		"\u0000\u0000\u0101\u00ff\u0001\u0000\u0000\u0000\u0101\u0102\u0001\u0000"+
		"\u0000\u0000\u0102\u0103\u0001\u0000\u0000\u0000\u0103\u0105\u0005\u00be"+
		"\u0000\u0000\u0104\u00fd\u0001\u0000\u0000\u0000\u0104\u0105\u0001\u0000"+
		"\u0000\u0000\u0105\u0140\u0001\u0000\u0000\u0000\u0106\u010e\u0005V\u0000"+
		"\u0000\u0107\u0108\u0005\u00bd\u0000\u0000\u0108\u010b\u0005\u00c3\u0000"+
		"\u0000\u0109\u010a\u0005\u00bb\u0000\u0000\u010a\u010c\u0005\u00c3\u0000"+
		"\u0000\u010b\u0109\u0001\u0000\u0000\u0000\u010b\u010c\u0001\u0000\u0000"+
		"\u0000\u010c\u010d\u0001\u0000\u0000\u0000\u010d\u010f\u0005\u00be\u0000"+
		"\u0000\u010e\u0107\u0001\u0000\u0000\u0000\u010e\u010f\u0001\u0000\u0000"+
		"\u0000\u010f\u0140\u0001\u0000\u0000\u0000\u0110\u0140\u0005W\u0000\u0000"+
		"\u0111\u0140\u0005X\u0000\u0000\u0112\u0116\u0005Y\u0000\u0000\u0113\u0114"+
		"\u0005\u00bd\u0000\u0000\u0114\u0115\u0005\u00c3\u0000\u0000\u0115\u0117"+
		"\u0005\u00be\u0000\u0000\u0116\u0113\u0001\u0000\u0000\u0000\u0116\u0117"+
		"\u0001\u0000\u0000\u0000\u0117\u0140\u0001\u0000\u0000\u0000\u0118\u011c"+
		"\u0005Z\u0000\u0000\u0119\u011a\u0005\u00bd\u0000\u0000\u011a\u011b\u0005"+
		"\u00c3\u0000\u0000\u011b\u011d\u0005\u00be\u0000\u0000\u011c\u0119\u0001"+
		"\u0000\u0000\u0000\u011c\u011d\u0001\u0000\u0000\u0000\u011d\u0140\u0001"+
		"\u0000\u0000\u0000\u011e\u0140\u0005[\u0000\u0000\u011f\u0140\u0005\\"+
		"\u0000\u0000\u0120\u0140\u0005]\u0000\u0000\u0121\u0125\u0005^\u0000\u0000"+
		"\u0122\u0123\u0005\u00bd\u0000\u0000\u0123\u0124\u0005\u00c3\u0000\u0000"+
		"\u0124\u0126\u0005\u00be\u0000\u0000\u0125\u0122\u0001\u0000\u0000\u0000"+
		"\u0125\u0126\u0001\u0000\u0000\u0000\u0126\u0140\u0001\u0000\u0000\u0000"+
		"\u0127\u012b\u0005_\u0000\u0000\u0128\u0129\u0005\u00bd\u0000\u0000\u0129"+
		"\u012a\u0005\u00c3\u0000\u0000\u012a\u012c\u0005\u00be\u0000\u0000\u012b"+
		"\u0128\u0001\u0000\u0000\u0000\u012b\u012c\u0001\u0000\u0000\u0000\u012c"+
		"\u0130\u0001\u0000\u0000\u0000\u012d\u012e\u0005=\u0000\u0000\u012e\u012f"+
		"\u0005\u00c7\u0000\u0000\u012f\u0131\u0005\u008e\u0000\u0000\u0130\u012d"+
		"\u0001\u0000\u0000\u0000\u0130\u0131\u0001\u0000\u0000\u0000\u0131\u0140"+
		"\u0001\u0000\u0000\u0000\u0132\u0140\u0005`\u0000\u0000\u0133\u0140\u0005"+
		"a\u0000\u0000\u0134\u0140\u0005b\u0000\u0000\u0135\u0140\u0005c\u0000"+
		"\u0000\u0136\u0140\u0005d\u0000\u0000\u0137\u0140\u0005e\u0000\u0000\u0138"+
		"\u0140\u0005f\u0000\u0000\u0139\u0140\u0005g\u0000\u0000\u013a\u013d\u0005"+
		"\u00c7\u0000\u0000\u013b\u013c\u0005\u00a3\u0000\u0000\u013c\u013e\u0007"+
		"\u0001\u0000\u0000\u013d\u013b\u0001\u0000\u0000\u0000\u013d\u013e\u0001"+
		"\u0000\u0000\u0000\u013e\u0140\u0001\u0000\u0000\u0000\u013f\u00f8\u0001"+
		"\u0000\u0000\u0000\u013f\u00fa\u0001\u0000\u0000\u0000\u013f\u00fb\u0001"+
		"\u0000\u0000\u0000\u013f\u00fc\u0001\u0000\u0000\u0000\u013f\u0106\u0001"+
		"\u0000\u0000\u0000\u013f\u0110\u0001\u0000\u0000\u0000\u013f\u0111\u0001"+
		"\u0000\u0000\u0000\u013f\u0112\u0001\u0000\u0000\u0000\u013f\u0118\u0001"+
		"\u0000\u0000\u0000\u013f\u011e\u0001\u0000\u0000\u0000\u013f\u011f\u0001"+
		"\u0000\u0000\u0000\u013f\u0120\u0001\u0000\u0000\u0000\u013f\u0121\u0001"+
		"\u0000\u0000\u0000\u013f\u0127\u0001\u0000\u0000\u0000\u013f\u0132\u0001"+
		"\u0000\u0000\u0000\u013f\u0133\u0001\u0000\u0000\u0000\u013f\u0134\u0001"+
		"\u0000\u0000\u0000\u013f\u0135\u0001\u0000\u0000\u0000\u013f\u0136\u0001"+
		"\u0000\u0000\u0000\u013f\u0137\u0001\u0000\u0000\u0000\u013f\u0138\u0001"+
		"\u0000\u0000\u0000\u013f\u0139\u0001\u0000\u0000\u0000\u013f\u013a\u0001"+
		"\u0000\u0000\u0000\u0140\u0149\u0001\u0000\u0000\u0000\u0141\u0142\n\u0001"+
		"\u0000\u0000\u0142\u0145\u0005k\u0000\u0000\u0143\u0144\u0005\u00bf\u0000"+
		"\u0000\u0144\u0146\u0005\u00c0\u0000\u0000\u0145\u0143\u0001\u0000\u0000"+
		"\u0000\u0145\u0146\u0001\u0000\u0000\u0000\u0146\u0148\u0001\u0000\u0000"+
		"\u0000\u0147\u0141\u0001\u0000\u0000\u0000\u0148\u014b\u0001\u0000\u0000"+
		"\u0000\u0149\u0147\u0001\u0000\u0000\u0000\u0149\u014a\u0001\u0000\u0000"+
		"\u0000\u014a\u000b\u0001\u0000\u0000\u0000\u014b\u0149\u0001\u0000\u0000"+
		"\u0000\u014c\u014e\u0003\u000e\u0007\u0000\u014d\u014c\u0001\u0000\u0000"+
		"\u0000\u014e\u0151\u0001\u0000\u0000\u0000\u014f\u014d\u0001\u0000\u0000"+
		"\u0000\u014f\u0150\u0001\u0000\u0000\u0000\u0150\r\u0001\u0000\u0000\u0000"+
		"\u0151\u014f\u0001\u0000\u0000\u0000\u0152\u0171\u0003\u0010\b\u0000\u0153"+
		"\u0171\u0003\u0014\n\u0000\u0154\u0171\u0003&\u0013\u0000\u0155\u0171"+
		"\u0003(\u0014\u0000\u0156\u0171\u0003*\u0015\u0000\u0157\u0171\u00030"+
		"\u0018\u0000\u0158\u0171\u00036\u001b\u0000\u0159\u0171\u00038\u001c\u0000"+
		"\u015a\u0171\u0003:\u001d\u0000\u015b\u0171\u0003<\u001e\u0000\u015c\u0171"+
		"\u0003B!\u0000\u015d\u0171\u0003D\"\u0000\u015e\u0171\u0003F#\u0000\u015f"+
		"\u0171\u0003J%\u0000\u0160\u0171\u0003L&\u0000\u0161\u0171\u0003N\'\u0000"+
		"\u0162\u0171\u0003P(\u0000\u0163\u0171\u0003R)\u0000\u0164\u0171\u0003"+
		"X,\u0000\u0165\u0171\u0003Z-\u0000\u0166\u0171\u0003`0\u0000\u0167\u0171"+
		"\u0003l6\u0000\u0168\u0171\u0003t:\u0000\u0169\u0171\u0003v;\u0000\u016a"+
		"\u0171\u0003x<\u0000\u016b\u0171\u0003\\.\u0000\u016c\u0171\u0003^/\u0000"+
		"\u016d\u0171\u0003\u00aaU\u0000\u016e\u0171\u0003\u00a4R\u0000\u016f\u0171"+
		"\u0003\u00a6S\u0000\u0170\u0152\u0001\u0000\u0000\u0000\u0170\u0153\u0001"+
		"\u0000\u0000\u0000\u0170\u0154\u0001\u0000\u0000\u0000\u0170\u0155\u0001"+
		"\u0000\u0000\u0000\u0170\u0156\u0001\u0000\u0000\u0000\u0170\u0157\u0001"+
		"\u0000\u0000\u0000\u0170\u0158\u0001\u0000\u0000\u0000\u0170\u0159\u0001"+
		"\u0000\u0000\u0000\u0170\u015a\u0001\u0000\u0000\u0000\u0170\u015b\u0001"+
		"\u0000\u0000\u0000\u0170\u015c\u0001\u0000\u0000\u0000\u0170\u015d\u0001"+
		"\u0000\u0000\u0000\u0170\u015e\u0001\u0000\u0000\u0000\u0170\u015f\u0001"+
		"\u0000\u0000\u0000\u0170\u0160\u0001\u0000\u0000\u0000\u0170\u0161\u0001"+
		"\u0000\u0000\u0000\u0170\u0162\u0001\u0000\u0000\u0000\u0170\u0163\u0001"+
		"\u0000\u0000\u0000\u0170\u0164\u0001\u0000\u0000\u0000\u0170\u0165\u0001"+
		"\u0000\u0000\u0000\u0170\u0166\u0001\u0000\u0000\u0000\u0170\u0167\u0001"+
		"\u0000\u0000\u0000\u0170\u0168\u0001\u0000\u0000\u0000\u0170\u0169\u0001"+
		"\u0000\u0000\u0000\u0170\u016a\u0001\u0000\u0000\u0000\u0170\u016b\u0001"+
		"\u0000\u0000\u0000\u0170\u016c\u0001\u0000\u0000\u0000\u0170\u016d\u0001"+
		"\u0000\u0000\u0000\u0170\u016e\u0001\u0000\u0000\u0000\u0170\u016f\u0001"+
		"\u0000\u0000\u0000\u0171\u000f\u0001\u0000\u0000\u0000\u0172\u0173\u0003"+
		"\u0012\t\u0000\u0173\u0174\u0005\u00a7\u0000\u0000\u0174\u0175\u0003\u0092"+
		"I\u0000\u0175\u0176\u0005\u00ba\u0000\u0000\u0176\u0011\u0001\u0000\u0000"+
		"\u0000\u0177\u017c\u0005\u00c7\u0000\u0000\u0178\u0179\u0005\u00bc\u0000"+
		"\u0000\u0179\u017b\u0005\u00c7\u0000\u0000\u017a\u0178\u0001\u0000\u0000"+
		"\u0000\u017b\u017e\u0001\u0000\u0000\u0000\u017c\u017a\u0001\u0000\u0000"+
		"\u0000\u017c\u017d\u0001\u0000\u0000\u0000\u017d\u0188\u0001\u0000\u0000"+
		"\u0000\u017e\u017c\u0001\u0000\u0000\u0000\u017f\u0184\u0005\u00c8\u0000"+
		"\u0000\u0180\u0181\u0005\u00bc\u0000\u0000\u0181\u0183\u0005\u00c8\u0000"+
		"\u0000\u0182\u0180\u0001\u0000\u0000\u0000\u0183\u0186\u0001\u0000\u0000"+
		"\u0000\u0184\u0182\u0001\u0000\u0000\u0000\u0184\u0185\u0001\u0000\u0000"+
		"\u0000\u0185\u0188\u0001\u0000\u0000\u0000\u0186\u0184\u0001\u0000\u0000"+
		"\u0000\u0187\u0177\u0001\u0000\u0000\u0000\u0187\u017f\u0001\u0000\u0000"+
		"\u0000\u0188\u0013\u0001\u0000\u0000\u0000\u0189\u018b\u0003|>\u0000\u018a"+
		"\u0189\u0001\u0000\u0000\u0000\u018a\u018b\u0001\u0000\u0000\u0000\u018b"+
		"\u018c\u0001\u0000\u0000\u0000\u018c\u018d\u0005#\u0000\u0000\u018d\u018e"+
		"\u0003\u0016\u000b\u0000\u018e\u0190\u0005*\u0000\u0000\u018f\u0191\u0005"+
		"\u008d\u0000\u0000\u0190\u018f\u0001\u0000\u0000\u0000\u0190\u0191\u0001"+
		"\u0000\u0000\u0000\u0191\u0192\u0001\u0000\u0000\u0000\u0192\u0194\u0003"+
		"\u001a\r\u0000\u0193\u0195\u0003\u001c\u000e\u0000\u0194\u0193\u0001\u0000"+
		"\u0000\u0000\u0194\u0195\u0001\u0000\u0000\u0000\u0195\u0197\u0001\u0000"+
		"\u0000\u0000\u0196\u0198\u0003$\u0012\u0000\u0197\u0196\u0001\u0000\u0000"+
		"\u0000\u0197\u0198\u0001\u0000\u0000\u0000\u0198\u019a\u0001\u0000\u0000"+
		"\u0000\u0199\u019b\u0003\u0084B\u0000\u019a\u0199\u0001\u0000\u0000\u0000"+
		"\u019a\u019b\u0001\u0000\u0000\u0000\u019b\u019d\u0001\u0000\u0000\u0000"+
		"\u019c\u019e\u0003\u0086C\u0000\u019d\u019c\u0001\u0000\u0000\u0000\u019d"+
		"\u019e\u0001\u0000\u0000\u0000\u019e\u01a0\u0001\u0000\u0000\u0000\u019f"+
		"\u01a1\u0003\u0088D\u0000\u01a0\u019f\u0001\u0000\u0000\u0000\u01a0\u01a1"+
		"\u0001\u0000\u0000\u0000\u01a1\u01a3\u0001\u0000\u0000\u0000\u01a2\u01a4"+
		"\u0003\u008cF\u0000\u01a3\u01a2\u0001\u0000\u0000\u0000\u01a3\u01a4\u0001"+
		"\u0000\u0000\u0000\u01a4\u01a5\u0001\u0000\u0000\u0000\u01a5\u01a6\u0005"+
		"\u00ba\u0000\u0000\u01a6\u0015\u0001\u0000\u0000\u0000\u01a7\u01b1\u0005"+
		"\u00b1\u0000\u0000\u01a8\u01ad\u0003\u0018\f\u0000\u01a9\u01aa\u0005\u00bb"+
		"\u0000\u0000\u01aa\u01ac\u0003\u0018\f\u0000\u01ab\u01a9\u0001\u0000\u0000"+
		"\u0000\u01ac\u01af\u0001\u0000\u0000\u0000\u01ad\u01ab\u0001\u0000\u0000"+
		"\u0000\u01ad\u01ae\u0001\u0000\u0000\u0000\u01ae\u01b1\u0001\u0000\u0000"+
		"\u0000\u01af\u01ad\u0001\u0000\u0000\u0000\u01b0\u01a7\u0001\u0000\u0000"+
		"\u0000\u01b0\u01a8\u0001\u0000\u0000\u0000\u01b1\u0017\u0001\u0000\u0000"+
		"\u0000\u01b2\u01b7\u0003\u0092I\u0000\u01b3\u01b5\u0005\u0084\u0000\u0000"+
		"\u01b4\u01b3\u0001\u0000\u0000\u0000\u01b4\u01b5\u0001\u0000\u0000\u0000"+
		"\u01b5\u01b6\u0001\u0000\u0000\u0000\u01b6\u01b8\u0005\u00c7\u0000\u0000"+
		"\u01b7\u01b4\u0001\u0000\u0000\u0000\u01b7\u01b8\u0001\u0000\u0000\u0000"+
		"\u01b8\u0019\u0001\u0000\u0000\u0000\u01b9\u01be\u0003\u0012\t\u0000\u01ba"+
		"\u01bb\u0005\u00bb\u0000\u0000\u01bb\u01bd\u0003\u0012\t\u0000\u01bc\u01ba"+
		"\u0001\u0000\u0000\u0000\u01bd\u01c0\u0001\u0000\u0000\u0000\u01be\u01bc"+
		"\u0001\u0000\u0000\u0000\u01be\u01bf\u0001\u0000\u0000\u0000\u01bf\u001b"+
		"\u0001\u0000\u0000\u0000\u01c0\u01be\u0001\u0000\u0000\u0000\u01c1\u01c2"+
		"\u0005+\u0000\u0000\u01c2\u01c6\u0003\u001e\u000f\u0000\u01c3\u01c5\u0003"+
		" \u0010\u0000\u01c4\u01c3\u0001\u0000\u0000\u0000\u01c5\u01c8\u0001\u0000"+
		"\u0000\u0000\u01c6\u01c4\u0001\u0000\u0000\u0000\u01c6\u01c7\u0001\u0000"+
		"\u0000\u0000\u01c7\u001d\u0001\u0000\u0000\u0000\u01c8\u01c6\u0001\u0000"+
		"\u0000\u0000\u01c9\u01ce\u0005\u00c7\u0000\u0000\u01ca\u01cb\u0005\u00bc"+
		"\u0000\u0000\u01cb\u01cd\u0005\u00c7\u0000\u0000\u01cc\u01ca\u0001\u0000"+
		"\u0000\u0000\u01cd\u01d0\u0001\u0000\u0000\u0000\u01ce\u01cc\u0001\u0000"+
		"\u0000\u0000\u01ce\u01cf\u0001\u0000\u0000\u0000\u01cf\u01d5\u0001\u0000"+
		"\u0000\u0000\u01d0\u01ce\u0001\u0000\u0000\u0000\u01d1\u01d3\u0005\u0084"+
		"\u0000\u0000\u01d2\u01d1\u0001\u0000\u0000\u0000\u01d2\u01d3\u0001\u0000"+
		"\u0000\u0000\u01d3\u01d4\u0001\u0000\u0000\u0000\u01d4\u01d6\u0005\u00c7"+
		"\u0000\u0000\u01d5\u01d2\u0001\u0000\u0000\u0000\u01d5\u01d6\u0001\u0000"+
		"\u0000\u0000\u01d6\u01fa\u0001\u0000\u0000\u0000\u01d7\u01dc\u0005\u00c8"+
		"\u0000\u0000\u01d8\u01d9\u0005\u00bc\u0000\u0000\u01d9\u01db\u0005\u00c8"+
		"\u0000\u0000\u01da\u01d8\u0001\u0000\u0000\u0000\u01db\u01de\u0001\u0000"+
		"\u0000\u0000\u01dc\u01da\u0001\u0000\u0000\u0000\u01dc\u01dd\u0001\u0000"+
		"\u0000\u0000\u01dd\u01e3\u0001\u0000\u0000\u0000\u01de\u01dc\u0001\u0000"+
		"\u0000\u0000\u01df\u01e1\u0005\u0084\u0000\u0000\u01e0\u01df\u0001\u0000"+
		"\u0000\u0000\u01e0\u01e1\u0001\u0000\u0000\u0000\u01e1\u01e2\u0001\u0000"+
		"\u0000\u0000\u01e2\u01e4\u0005\u00c7\u0000\u0000\u01e3\u01e0\u0001\u0000"+
		"\u0000\u0000\u01e3\u01e4\u0001\u0000\u0000\u0000\u01e4\u01fa\u0001\u0000"+
		"\u0000\u0000\u01e5\u01e8\u0003\u009cN\u0000\u01e6\u01e7\u0005=\u0000\u0000"+
		"\u01e7\u01e9\u0005\u00c7\u0000\u0000\u01e8\u01e6\u0001\u0000\u0000\u0000"+
		"\u01e8\u01e9\u0001\u0000\u0000\u0000\u01e9\u01ee\u0001\u0000\u0000\u0000"+
		"\u01ea\u01ec\u0005\u0084\u0000\u0000\u01eb\u01ea\u0001\u0000\u0000\u0000"+
		"\u01eb\u01ec\u0001\u0000\u0000\u0000\u01ec\u01ed\u0001\u0000\u0000\u0000"+
		"\u01ed\u01ef\u0005\u00c7\u0000\u0000\u01ee\u01eb\u0001\u0000\u0000\u0000"+
		"\u01ee\u01ef\u0001\u0000\u0000\u0000\u01ef\u01fa\u0001\u0000\u0000\u0000"+
		"\u01f0\u01f1\u0005\u00bd\u0000\u0000\u01f1\u01f2\u0003z=\u0000\u01f2\u01f7"+
		"\u0005\u00be\u0000\u0000\u01f3\u01f5\u0005\u0084\u0000\u0000\u01f4\u01f3"+
		"\u0001\u0000\u0000\u0000\u01f4\u01f5\u0001\u0000\u0000\u0000\u01f5\u01f6"+
		"\u0001\u0000\u0000\u0000\u01f6\u01f8\u0005\u00c7\u0000\u0000\u01f7\u01f4"+
		"\u0001\u0000\u0000\u0000\u01f7\u01f8\u0001\u0000\u0000\u0000\u01f8\u01fa"+
		"\u0001\u0000\u0000\u0000\u01f9\u01c9\u0001\u0000\u0000\u0000\u01f9\u01d7"+
		"\u0001\u0000\u0000\u0000\u01f9\u01e5\u0001\u0000\u0000\u0000\u01f9\u01f0"+
		"\u0001\u0000\u0000\u0000\u01fa\u001f\u0001\u0000\u0000\u0000\u01fb\u01fc"+
		"\u0005\u00bb\u0000\u0000\u01fc\u020c\u0003\u001e\u000f\u0000\u01fd\u01ff"+
		"\u0003\"\u0011\u0000\u01fe\u01fd\u0001\u0000\u0000\u0000\u01fe\u01ff\u0001"+
		"\u0000\u0000\u0000\u01ff\u0200\u0001\u0000\u0000\u0000\u0200\u0201\u0005"+
		"m\u0000\u0000\u0201\u0209\u0003\u001e\u000f\u0000\u0202\u0203\u0005u\u0000"+
		"\u0000\u0203\u020a\u0003\u0092I\u0000\u0204\u0205\u0005\u008c\u0000\u0000"+
		"\u0205\u0206\u0005\u00bd\u0000\u0000\u0206\u0207\u0003,\u0016\u0000\u0207"+
		"\u0208\u0005\u00be\u0000\u0000\u0208\u020a\u0001\u0000\u0000\u0000\u0209"+
		"\u0202\u0001\u0000\u0000\u0000\u0209\u0204\u0001\u0000\u0000\u0000\u0209"+
		"\u020a\u0001\u0000\u0000\u0000\u020a\u020c\u0001\u0000\u0000\u0000\u020b"+
		"\u01fb\u0001\u0000\u0000\u0000\u020b\u01fe\u0001\u0000\u0000\u0000\u020c"+
		"!\u0001\u0000\u0000\u0000\u020d\u021c\u0005q\u0000\u0000\u020e\u0210\u0005"+
		"n\u0000\u0000\u020f\u0211\u0005r\u0000\u0000\u0210\u020f\u0001\u0000\u0000"+
		"\u0000\u0210\u0211\u0001\u0000\u0000\u0000\u0211\u021c\u0001\u0000\u0000"+
		"\u0000\u0212\u0214\u0005o\u0000\u0000\u0213\u0215\u0005r\u0000\u0000\u0214"+
		"\u0213\u0001\u0000\u0000\u0000\u0214\u0215\u0001\u0000\u0000\u0000\u0215"+
		"\u021c\u0001\u0000\u0000\u0000\u0216\u0218\u0005s\u0000\u0000\u0217\u0219"+
		"\u0005r\u0000\u0000\u0218\u0217\u0001\u0000\u0000\u0000\u0218\u0219\u0001"+
		"\u0000\u0000\u0000\u0219\u021c\u0001\u0000\u0000\u0000\u021a\u021c\u0005"+
		"t\u0000\u0000\u021b\u020d\u0001\u0000\u0000\u0000\u021b\u020e\u0001\u0000"+
		"\u0000\u0000\u021b\u0212\u0001\u0000\u0000\u0000\u021b\u0216\u0001\u0000"+
		"\u0000\u0000\u021b\u021a\u0001\u0000\u0000\u0000\u021c#\u0001\u0000\u0000"+
		"\u0000\u021d\u021e\u0005,\u0000\u0000\u021e\u021f\u0003\u0092I\u0000\u021f"+
		"%\u0001\u0000\u0000\u0000\u0220\u0221\u0005\'\u0000\u0000\u0221\u0223"+
		"\u0003\u0016\u000b\u0000\u0222\u0224\u0003\u001c\u000e\u0000\u0223\u0222"+
		"\u0001\u0000\u0000\u0000\u0223\u0224\u0001\u0000\u0000\u0000\u0224\u0226"+
		"\u0001\u0000\u0000\u0000\u0225\u0227\u0003$\u0012\u0000\u0226\u0225\u0001"+
		"\u0000\u0000\u0000\u0226\u0227\u0001\u0000\u0000\u0000\u0227\u0228\u0001"+
		"\u0000\u0000\u0000\u0228\u0229\u0005\u00ba\u0000\u0000\u0229\'\u0001\u0000"+
		"\u0000\u0000\u022a\u022b\u0005(\u0000\u0000\u022b\u022e\u0003\u0092I\u0000"+
		"\u022c\u022d\u0005*\u0000\u0000\u022d\u022f\u0003\u001a\r\u0000\u022e"+
		"\u022c\u0001\u0000\u0000\u0000\u022e\u022f\u0001\u0000\u0000\u0000\u022f"+
		"\u0232\u0001\u0000\u0000\u0000\u0230\u0231\u0005\u008c\u0000\u0000\u0231"+
		"\u0233\u0003.\u0017\u0000\u0232\u0230\u0001\u0000\u0000\u0000\u0232\u0233"+
		"\u0001\u0000\u0000\u0000\u0233\u0234\u0001\u0000\u0000\u0000\u0234\u0235"+
		"\u0005\u00ba\u0000\u0000\u0235)\u0001\u0000\u0000\u0000\u0236\u0237\u0005"+
		"$\u0000\u0000\u0237\u0238\u0005*\u0000\u0000\u0238\u023d\u0005\u00c7\u0000"+
		"\u0000\u0239\u023a\u0005\u00bc\u0000\u0000\u023a\u023c\u0005\u00c7\u0000"+
		"\u0000\u023b\u0239\u0001\u0000\u0000\u0000\u023c\u023f\u0001\u0000\u0000"+
		"\u0000\u023d\u023b\u0001\u0000\u0000\u0000\u023d\u023e\u0001\u0000\u0000"+
		"\u0000\u023e\u0244\u0001\u0000\u0000\u0000\u023f\u023d\u0001\u0000\u0000"+
		"\u0000\u0240\u0241\u0005\u00bd\u0000\u0000\u0241\u0242\u0003,\u0016\u0000"+
		"\u0242\u0243\u0005\u00be\u0000\u0000\u0243\u0245\u0001\u0000\u0000\u0000"+
		"\u0244\u0240\u0001\u0000\u0000\u0000\u0244\u0245\u0001\u0000\u0000\u0000"+
		"\u0245\u0246\u0001\u0000\u0000\u0000\u0246\u0247\u0005.\u0000\u0000\u0247"+
		"\u0248\u0005\u00bd\u0000\u0000\u0248\u0249\u0003.\u0017\u0000\u0249\u0251"+
		"\u0005\u00be\u0000\u0000\u024a\u024b\u0005\u00bb\u0000\u0000\u024b\u024c"+
		"\u0005\u00bd\u0000\u0000\u024c\u024d\u0003.\u0017\u0000\u024d\u024e\u0005"+
		"\u00be\u0000\u0000\u024e\u0250\u0001\u0000\u0000\u0000\u024f\u024a\u0001"+
		"\u0000\u0000\u0000\u0250\u0253\u0001\u0000\u0000\u0000\u0251\u024f\u0001"+
		"\u0000\u0000\u0000\u0251\u0252\u0001\u0000\u0000\u0000\u0252\u025a\u0001"+
		"\u0000\u0000\u0000\u0253\u0251\u0001\u0000\u0000\u0000\u0254\u0255\u0005"+
		"/\u0000\u0000\u0255\u0258\u0003.\u0017\u0000\u0256\u0257\u0005*\u0000"+
		"\u0000\u0257\u0259\u0003\u001a\r\u0000\u0258\u0256\u0001\u0000\u0000\u0000"+
		"\u0258\u0259\u0001\u0000\u0000\u0000\u0259\u025b\u0001\u0000\u0000\u0000"+
		"\u025a\u0254\u0001\u0000\u0000\u0000\u025a\u025b\u0001\u0000\u0000\u0000"+
		"\u025b\u025c\u0001\u0000\u0000\u0000\u025c\u025d\u0005\u00ba\u0000\u0000"+
		"\u025d\u0275\u0001\u0000\u0000\u0000\u025e\u025f\u0005$\u0000\u0000\u025f"+
		"\u0260\u0005*\u0000\u0000\u0260\u0265\u0005\u00c7\u0000\u0000\u0261\u0262"+
		"\u0005\u00bc\u0000\u0000\u0262\u0264\u0005\u00c7\u0000\u0000\u0263\u0261"+
		"\u0001\u0000\u0000\u0000\u0264\u0267\u0001\u0000\u0000\u0000\u0265\u0263"+
		"\u0001\u0000\u0000\u0000\u0265\u0266\u0001\u0000\u0000\u0000\u0266\u026c"+
		"\u0001\u0000\u0000\u0000\u0267\u0265\u0001\u0000\u0000\u0000\u0268\u0269"+
		"\u0005\u00bd\u0000\u0000\u0269\u026a\u0003,\u0016\u0000\u026a\u026b\u0005"+
		"\u00be\u0000\u0000\u026b\u026d\u0001\u0000\u0000\u0000\u026c\u0268\u0001"+
		"\u0000\u0000\u0000\u026c\u026d\u0001\u0000\u0000\u0000\u026d\u026f\u0001"+
		"\u0000\u0000\u0000\u026e\u0270\b\u0002\u0000\u0000\u026f\u026e\u0001\u0000"+
		"\u0000\u0000\u0270\u0271\u0001\u0000\u0000\u0000\u0271\u026f\u0001\u0000"+
		"\u0000\u0000\u0271\u0272\u0001\u0000\u0000\u0000\u0272\u0273\u0001\u0000"+
		"\u0000\u0000\u0273\u0275\u0005\u00ba\u0000\u0000\u0274\u0236\u0001\u0000"+
		"\u0000\u0000\u0274\u025e\u0001\u0000\u0000\u0000\u0275+\u0001\u0000\u0000"+
		"\u0000\u0276\u027b\u0005\u00c7\u0000\u0000\u0277\u0278\u0005\u00bb\u0000"+
		"\u0000\u0278\u027a\u0005\u00c7\u0000\u0000\u0279\u0277\u0001\u0000\u0000"+
		"\u0000\u027a\u027d\u0001\u0000\u0000\u0000\u027b\u0279\u0001\u0000\u0000"+
		"\u0000\u027b\u027c\u0001\u0000\u0000\u0000\u027c-\u0001\u0000\u0000\u0000"+
		"\u027d\u027b\u0001\u0000\u0000\u0000\u027e\u0283\u0003\u0092I\u0000\u027f"+
		"\u0280\u0005\u00bb\u0000\u0000\u0280\u0282\u0003\u0092I\u0000\u0281\u027f"+
		"\u0001\u0000\u0000\u0000\u0282\u0285\u0001\u0000\u0000\u0000\u0283\u0281"+
		"\u0001\u0000\u0000\u0000\u0283\u0284\u0001\u0000\u0000\u0000\u0284/\u0001"+
		"\u0000\u0000\u0000\u0285\u0283\u0001\u0000\u0000\u0000\u0286\u0288\u0003"+
		"|>\u0000\u0287\u0286\u0001\u0000\u0000\u0000\u0287\u0288\u0001\u0000\u0000"+
		"\u0000\u0288\u0289\u0001\u0000\u0000\u0000\u0289\u028a\u0005%\u0000\u0000"+
		"\u028a\u028f\u0005\u00c7\u0000\u0000\u028b\u028c\u0005\u00bc\u0000\u0000"+
		"\u028c\u028e\u0005\u00c7\u0000\u0000\u028d\u028b\u0001\u0000\u0000\u0000"+
		"\u028e\u0291\u0001\u0000\u0000\u0000\u028f\u028d\u0001\u0000\u0000\u0000"+
		"\u028f\u0290\u0001\u0000\u0000\u0000\u0290\u0296\u0001\u0000\u0000\u0000"+
		"\u0291\u028f\u0001\u0000\u0000\u0000\u0292\u0294\u0005\u0084\u0000\u0000"+
		"\u0293\u0292\u0001\u0000\u0000\u0000\u0293\u0294\u0001\u0000\u0000\u0000"+
		"\u0294\u0295\u0001\u0000\u0000\u0000\u0295\u0297\u0005\u00c7\u0000\u0000"+
		"\u0296\u0293\u0001\u0000\u0000\u0000\u0296\u0297\u0001\u0000\u0000\u0000"+
		"\u0297\u0298\u0001\u0000\u0000\u0000\u0298\u0299\u0005-\u0000\u0000\u0299"+
		"\u02a2\u00032\u0019\u0000\u029a\u029b\u0005+\u0000\u0000\u029b\u029f\u0003"+
		"\u001e\u000f\u0000\u029c\u029e\u0003 \u0010\u0000\u029d\u029c\u0001\u0000"+
		"\u0000\u0000\u029e\u02a1\u0001\u0000\u0000\u0000\u029f\u029d\u0001\u0000"+
		"\u0000\u0000\u029f\u02a0\u0001\u0000\u0000\u0000\u02a0\u02a3\u0001\u0000"+
		"\u0000\u0000\u02a1\u029f\u0001\u0000\u0000\u0000\u02a2\u029a\u0001\u0000"+
		"\u0000\u0000\u02a2\u02a3\u0001\u0000\u0000\u0000\u02a3\u02a5\u0001\u0000"+
		"\u0000\u0000\u02a4\u02a6\u0003$\u0012\u0000\u02a5\u02a4\u0001\u0000\u0000"+
		"\u0000\u02a5\u02a6\u0001\u0000\u0000\u0000\u02a6\u02ad\u0001\u0000\u0000"+
		"\u0000\u02a7\u02a8\u0005/\u0000\u0000\u02a8\u02ab\u0003.\u0017\u0000\u02a9"+
		"\u02aa\u0005*\u0000\u0000\u02aa\u02ac\u0003\u001a\r\u0000\u02ab\u02a9"+
		"\u0001\u0000\u0000\u0000\u02ab\u02ac\u0001\u0000\u0000\u0000\u02ac\u02ae"+
		"\u0001\u0000\u0000\u0000\u02ad\u02a7\u0001\u0000\u0000\u0000\u02ad\u02ae"+
		"\u0001\u0000\u0000\u0000\u02ae\u02af\u0001\u0000\u0000\u0000\u02af\u02b0"+
		"\u0005\u00ba\u0000\u0000\u02b01\u0001\u0000\u0000\u0000\u02b1\u02b6\u0003"+
		"4\u001a\u0000\u02b2\u02b3\u0005\u00bb\u0000\u0000\u02b3\u02b5\u00034\u001a"+
		"\u0000\u02b4\u02b2\u0001\u0000\u0000\u0000\u02b5\u02b8\u0001\u0000\u0000"+
		"\u0000\u02b6\u02b4\u0001\u0000\u0000\u0000\u02b6\u02b7\u0001\u0000\u0000"+
		"\u0000\u02b73\u0001\u0000\u0000\u0000\u02b8\u02b6\u0001\u0000\u0000\u0000"+
		"\u02b9\u02be\u0005\u00c7\u0000\u0000\u02ba\u02bb\u0005\u00bc\u0000\u0000"+
		"\u02bb\u02bd\u0005\u00c7\u0000\u0000\u02bc\u02ba\u0001\u0000\u0000\u0000"+
		"\u02bd\u02c0\u0001\u0000\u0000\u0000\u02be\u02bc\u0001\u0000\u0000\u0000"+
		"\u02be\u02bf\u0001\u0000\u0000\u0000\u02bf\u02c1\u0001\u0000\u0000\u0000"+
		"\u02c0\u02be\u0001\u0000\u0000\u0000\u02c1\u02c2\u0005\u00a9\u0000\u0000"+
		"\u02c2\u02c3\u0003\u0092I\u0000\u02c35\u0001\u0000\u0000\u0000\u02c4\u02c6"+
		"\u0003|>\u0000\u02c5\u02c4\u0001\u0000\u0000\u0000\u02c5\u02c6\u0001\u0000"+
		"\u0000\u0000\u02c6\u02c7\u0001\u0000\u0000\u0000\u02c7\u02c8\u0005&\u0000"+
		"\u0000\u02c8\u02c9\u0005+\u0000\u0000\u02c9\u02ce\u0005\u00c7\u0000\u0000"+
		"\u02ca\u02cb\u0005\u00bc\u0000\u0000\u02cb\u02cd\u0005\u00c7\u0000\u0000"+
		"\u02cc\u02ca\u0001\u0000\u0000\u0000\u02cd\u02d0\u0001\u0000\u0000\u0000"+
		"\u02ce\u02cc\u0001\u0000\u0000\u0000\u02ce\u02cf\u0001\u0000\u0000\u0000"+
		"\u02cf\u02d5\u0001\u0000\u0000\u0000\u02d0\u02ce\u0001\u0000\u0000\u0000"+
		"\u02d1\u02d3\u0005\u0084\u0000\u0000\u02d2\u02d1\u0001\u0000\u0000\u0000"+
		"\u02d2\u02d3\u0001\u0000\u0000\u0000\u02d3\u02d4\u0001\u0000\u0000\u0000"+
		"\u02d4\u02d6\u0005\u00c7\u0000\u0000\u02d5\u02d2\u0001\u0000\u0000\u0000"+
		"\u02d5\u02d6\u0001\u0000\u0000\u0000\u02d6\u02e0\u0001\u0000\u0000\u0000"+
		"\u02d7\u02d8\u0005\u008c\u0000\u0000\u02d8\u02dd\u0003\u001e\u000f\u0000"+
		"\u02d9\u02da\u0005\u00bb\u0000\u0000\u02da\u02dc\u0003\u001e\u000f\u0000"+
		"\u02db\u02d9\u0001\u0000\u0000\u0000\u02dc\u02df\u0001\u0000\u0000\u0000"+
		"\u02dd\u02db\u0001\u0000\u0000\u0000\u02dd\u02de\u0001\u0000\u0000\u0000"+
		"\u02de\u02e1\u0001\u0000\u0000\u0000\u02df\u02dd\u0001\u0000\u0000\u0000"+
		"\u02e0\u02d7\u0001\u0000\u0000\u0000\u02e0\u02e1\u0001\u0000\u0000\u0000"+
		"\u02e1\u02e3\u0001\u0000\u0000\u0000\u02e2\u02e4\u0003$\u0012\u0000\u02e3"+
		"\u02e2\u0001\u0000\u0000\u0000\u02e3\u02e4\u0001\u0000\u0000\u0000\u02e4"+
		"\u02eb\u0001\u0000\u0000\u0000\u02e5\u02e6\u0005/\u0000\u0000\u02e6\u02e9"+
		"\u0003.\u0017\u0000\u02e7\u02e8\u0005*\u0000\u0000\u02e8\u02ea\u0003\u001a"+
		"\r\u0000\u02e9\u02e7\u0001\u0000\u0000\u0000\u02e9\u02ea\u0001\u0000\u0000"+
		"\u0000\u02ea\u02ec\u0001\u0000\u0000\u0000\u02eb\u02e5\u0001\u0000\u0000"+
		"\u0000\u02eb\u02ec\u0001\u0000\u0000\u0000\u02ec\u02ed\u0001\u0000\u0000"+
		"\u0000\u02ed\u02ee\u0005\u00ba\u0000\u0000\u02ee7\u0001\u0000\u0000\u0000"+
		"\u02ef\u02f0\u0005-\u0000\u0000\u02f0\u02f1\u0005\u00c7\u0000\u0000\u02f1"+
		"\u02f2\u0007\u0003\u0000\u0000\u02f2\u02f3\u0003\u0092I\u0000\u02f3\u02f4"+
		"\u0005\u00ba\u0000\u0000\u02f4\u02fb\u0001\u0000\u0000\u0000\u02f5\u02f6"+
		"\u0005-\u0000\u0000\u02f6\u02f7\u0005\u00c7\u0000\u0000\u02f7\u02f8\u0005"+
		"+\u0000\u0000\u02f8\u02f9\u0005\u0083\u0000\u0000\u02f9\u02fb\u0005\u00ba"+
		"\u0000\u0000\u02fa\u02ef\u0001\u0000\u0000\u0000\u02fa\u02f5\u0001\u0000"+
		"\u0000\u0000\u02fb9\u0001\u0000\u0000\u0000\u02fc\u02fd\u0005\u0005\u0000"+
		"\u0000\u02fd\u02fe\u0003\u0092I\u0000\u02fe\u02ff\u0005\u0006\u0000\u0000"+
		"\u02ff\u0307\u0003\f\u0006\u0000\u0300\u0301\u0005\u0007\u0000\u0000\u0301"+
		"\u0302\u0003\u0092I\u0000\u0302\u0303\u0005\u0006\u0000\u0000\u0303\u0304"+
		"\u0003\f\u0006\u0000\u0304\u0306\u0001\u0000\u0000\u0000\u0305\u0300\u0001"+
		"\u0000\u0000\u0000\u0306\u0309\u0001\u0000\u0000\u0000\u0307\u0305\u0001"+
		"\u0000\u0000\u0000\u0307\u0308\u0001\u0000\u0000\u0000\u0308\u030c\u0001"+
		"\u0000\u0000\u0000\u0309\u0307\u0001\u0000\u0000\u0000\u030a\u030b\u0005"+
		"\b\u0000\u0000\u030b\u030d\u0003\f\u0006\u0000\u030c\u030a\u0001\u0000"+
		"\u0000\u0000\u030c\u030d\u0001\u0000\u0000\u0000\u030d\u030e\u0001\u0000"+
		"\u0000\u0000\u030e\u030f\u0005\u0003\u0000\u0000\u030f\u0310\u0005\u0005"+
		"\u0000\u0000\u0310\u0311\u0005\u00ba\u0000\u0000\u0311;\u0001\u0000\u0000"+
		"\u0000\u0312\u0314\u0005\t\u0000\u0000\u0313\u0315\u0003\u0092I\u0000"+
		"\u0314\u0313\u0001\u0000\u0000\u0000\u0314\u0315\u0001\u0000\u0000\u0000"+
		"\u0315\u0316\u0001\u0000\u0000\u0000\u0316\u0319\u0003>\u001f\u0000\u0317"+
		"\u0318\u0005\b\u0000\u0000\u0318\u031a\u0003\f\u0006\u0000\u0319\u0317"+
		"\u0001\u0000\u0000\u0000\u0319\u031a\u0001\u0000\u0000\u0000\u031a\u031b"+
		"\u0001\u0000\u0000\u0000\u031b\u031c\u0005\u0003\u0000\u0000\u031c\u031d"+
		"\u0005\t\u0000\u0000\u031d\u031e\u0005\u00ba\u0000\u0000\u031e=\u0001"+
		"\u0000\u0000\u0000\u031f\u0321\u0003@ \u0000\u0320\u031f\u0001\u0000\u0000"+
		"\u0000\u0321\u0322\u0001\u0000\u0000\u0000\u0322\u0320\u0001\u0000\u0000"+
		"\u0000\u0322\u0323\u0001\u0000\u0000\u0000\u0323?\u0001\u0000\u0000\u0000"+
		"\u0324\u0325\u0005\n\u0000\u0000\u0325\u0326\u0003\u0092I\u0000\u0326"+
		"\u0327\u0005\u0006\u0000\u0000\u0327\u0328\u0003\f\u0006\u0000\u0328A"+
		"\u0001\u0000\u0000\u0000\u0329\u032b\u0003\u0002\u0001\u0000\u032a\u0329"+
		"\u0001\u0000\u0000\u0000\u032a\u032b\u0001\u0000\u0000\u0000\u032b\u032c"+
		"\u0001\u0000\u0000\u0000\u032c\u032d\u0005\u000b\u0000\u0000\u032d\u032e"+
		"\u0003\f\u0006\u0000\u032e\u032f\u0005\u0003\u0000\u0000\u032f\u0331\u0005"+
		"\u000b\u0000\u0000\u0330\u0332\u0003\u0002\u0001\u0000\u0331\u0330\u0001"+
		"\u0000\u0000\u0000\u0331\u0332\u0001\u0000\u0000\u0000\u0332\u0333\u0001"+
		"\u0000\u0000\u0000\u0333\u0334\u0005\u00ba\u0000\u0000\u0334C\u0001\u0000"+
		"\u0000\u0000\u0335\u0337\u0003\u0002\u0001\u0000\u0336\u0335\u0001\u0000"+
		"\u0000\u0000\u0336\u0337\u0001\u0000\u0000\u0000\u0337\u0338\u0001\u0000"+
		"\u0000\u0000\u0338\u0339\u0005\f\u0000\u0000\u0339\u033a\u0003\u0092I"+
		"\u0000\u033a\u033b\u0005\u000b\u0000\u0000\u033b\u033c\u0003\f\u0006\u0000"+
		"\u033c\u033d\u0005\u0003\u0000\u0000\u033d\u033f\u0005\u000b\u0000\u0000"+
		"\u033e\u0340\u0003\u0002\u0001\u0000\u033f\u033e\u0001\u0000\u0000\u0000"+
		"\u033f\u0340\u0001\u0000\u0000\u0000\u0340\u0341\u0001\u0000\u0000\u0000"+
		"\u0341\u0342\u0005\u00ba\u0000\u0000\u0342E\u0001\u0000\u0000\u0000\u0343"+
		"\u0345\u0003\u0002\u0001\u0000\u0344\u0343\u0001\u0000\u0000\u0000\u0344"+
		"\u0345\u0001\u0000\u0000\u0000\u0345\u0346\u0001\u0000\u0000\u0000\u0346"+
		"\u0347\u0005\r\u0000\u0000\u0347\u0348\u0005\u00c7\u0000\u0000\u0348\u034a"+
		"\u0005h\u0000\u0000\u0349\u034b\u0005i\u0000\u0000\u034a\u0349\u0001\u0000"+
		"\u0000\u0000\u034a\u034b\u0001\u0000\u0000\u0000\u034b\u034c\u0001\u0000"+
		"\u0000\u0000\u034c\u034d\u0003\u0092I\u0000\u034d\u034e\u0005\u00bc\u0000"+
		"\u0000\u034e\u034f\u0005\u00bc\u0000\u0000\u034f\u0352\u0003\u0092I\u0000"+
		"\u0350\u0351\u0005j\u0000\u0000\u0351\u0353\u0003\u0092I\u0000\u0352\u0350"+
		"\u0001\u0000\u0000\u0000\u0352\u0353\u0001\u0000\u0000\u0000\u0353\u0354"+
		"\u0001\u0000\u0000\u0000\u0354\u0355\u0005\u000b\u0000\u0000\u0355\u0356"+
		"\u0003\f\u0006\u0000\u0356\u0357\u0005\u0003\u0000\u0000\u0357\u0359\u0005"+
		"\u000b\u0000\u0000\u0358\u035a\u0003\u0002\u0001\u0000\u0359\u0358\u0001"+
		"\u0000\u0000\u0000\u0359\u035a\u0001\u0000\u0000\u0000\u035a\u035b\u0001"+
		"\u0000\u0000\u0000\u035b\u035c\u0005\u00ba\u0000\u0000\u035c\u036e\u0001"+
		"\u0000\u0000\u0000\u035d\u035f\u0003\u0002\u0001\u0000\u035e\u035d\u0001"+
		"\u0000\u0000\u0000\u035e\u035f\u0001\u0000\u0000\u0000\u035f\u0360\u0001"+
		"\u0000\u0000\u0000\u0360\u0361\u0005\r\u0000\u0000\u0361\u0362\u0005\u00c7"+
		"\u0000\u0000\u0362\u0363\u0005h\u0000\u0000\u0363\u0364\u0003H$\u0000"+
		"\u0364\u0365\u0005\u000b\u0000\u0000\u0365\u0366\u0003\f\u0006\u0000\u0366"+
		"\u0367\u0005\u0003\u0000\u0000\u0367\u0369\u0005\u000b\u0000\u0000\u0368"+
		"\u036a\u0003\u0002\u0001\u0000\u0369\u0368\u0001\u0000\u0000\u0000\u0369"+
		"\u036a\u0001\u0000\u0000\u0000\u036a\u036b\u0001\u0000\u0000\u0000\u036b"+
		"\u036c\u0005\u00ba\u0000\u0000\u036c\u036e\u0001\u0000\u0000\u0000\u036d"+
		"\u0344\u0001\u0000\u0000\u0000\u036d\u035e\u0001\u0000\u0000\u0000\u036e"+
		"G\u0001\u0000\u0000\u0000\u036f\u0370\u0005(\u0000\u0000\u0370\u0387\u0003"+
		"\u0092I\u0000\u0371\u0372\u0005#\u0000\u0000\u0372\u0374\u0003\u0016\u000b"+
		"\u0000\u0373\u0375\u0003\u001c\u000e\u0000\u0374\u0373\u0001\u0000\u0000"+
		"\u0000\u0374\u0375\u0001\u0000\u0000\u0000\u0375\u0377\u0001\u0000\u0000"+
		"\u0000\u0376\u0378\u0003$\u0012\u0000\u0377\u0376\u0001\u0000\u0000\u0000"+
		"\u0377\u0378\u0001\u0000\u0000\u0000\u0378\u037a\u0001\u0000\u0000\u0000"+
		"\u0379\u037b\u0003\u0084B\u0000\u037a\u0379\u0001\u0000\u0000\u0000\u037a"+
		"\u037b\u0001\u0000\u0000\u0000\u037b\u037d\u0001\u0000\u0000\u0000\u037c"+
		"\u037e\u0003\u0086C\u0000\u037d\u037c\u0001\u0000\u0000\u0000\u037d\u037e"+
		"\u0001\u0000\u0000\u0000\u037e\u0380\u0001\u0000\u0000\u0000\u037f\u0381"+
		"\u0003\u0088D\u0000\u0380\u037f\u0001\u0000\u0000\u0000\u0380\u0381\u0001"+
		"\u0000\u0000\u0000\u0381\u0383\u0001\u0000\u0000\u0000\u0382\u0384\u0003"+
		"\u008cF\u0000\u0383\u0382\u0001\u0000\u0000\u0000\u0383\u0384\u0001\u0000"+
		"\u0000\u0000\u0384\u0387\u0001\u0000\u0000\u0000\u0385\u0387\u0003\u0092"+
		"I\u0000\u0386\u036f\u0001\u0000\u0000\u0000\u0386\u0371\u0001\u0000\u0000"+
		"\u0000\u0386\u0385\u0001\u0000\u0000\u0000\u0387I\u0001\u0000\u0000\u0000"+
		"\u0388\u038a\u0003\u0002\u0001\u0000\u0389\u0388\u0001\u0000\u0000\u0000"+
		"\u0389\u038a\u0001\u0000\u0000\u0000\u038a\u038b\u0001\u0000\u0000\u0000"+
		"\u038b\u038c\u0005\u000e\u0000\u0000\u038c\u038f\u0005\u00c7\u0000\u0000"+
		"\u038d\u038e\u0005l\u0000\u0000\u038e\u0390\u0005\u00c3\u0000\u0000\u038f"+
		"\u038d\u0001\u0000\u0000\u0000\u038f\u0390\u0001\u0000\u0000\u0000\u0390"+
		"\u0391\u0001\u0000\u0000\u0000\u0391\u0392\u0005h\u0000\u0000\u0392\u0393"+
		"\u0005k\u0000\u0000\u0393\u0394\u0003\u0092I\u0000\u0394\u0395\u0005\u000b"+
		"\u0000\u0000\u0395\u0396\u0003\f\u0006\u0000\u0396\u0397\u0005\u0003\u0000"+
		"\u0000\u0397\u0399\u0005\u000b\u0000\u0000\u0398\u039a\u0003\u0002\u0001"+
		"\u0000\u0399\u0398\u0001\u0000\u0000\u0000\u0399\u039a\u0001\u0000\u0000"+
		"\u0000\u039a\u039b\u0001\u0000\u0000\u0000\u039b\u039c\u0005\u00ba\u0000"+
		"\u0000\u039cK\u0001\u0000\u0000\u0000\u039d\u039f\u0005\u000f\u0000\u0000"+
		"\u039e\u03a0\u0005\u00c7\u0000\u0000\u039f\u039e\u0001\u0000\u0000\u0000"+
		"\u039f\u03a0\u0001\u0000\u0000\u0000\u03a0\u03a3\u0001\u0000\u0000\u0000"+
		"\u03a1\u03a2\u0005\n\u0000\u0000\u03a2\u03a4\u0003\u0092I\u0000\u03a3"+
		"\u03a1\u0001\u0000\u0000\u0000\u03a3\u03a4\u0001\u0000\u0000\u0000\u03a4"+
		"\u03a5\u0001\u0000\u0000\u0000\u03a5\u03a6\u0005\u00ba\u0000\u0000\u03a6"+
		"M\u0001\u0000\u0000\u0000\u03a7\u03a9\u0005\u0010\u0000\u0000\u03a8\u03aa"+
		"\u0005\u00c7\u0000\u0000\u03a9\u03a8\u0001\u0000\u0000\u0000\u03a9\u03aa"+
		"\u0001\u0000\u0000\u0000\u03aa\u03ad\u0001\u0000\u0000\u0000\u03ab\u03ac"+
		"\u0005\n\u0000\u0000\u03ac\u03ae\u0003\u0092I\u0000\u03ad\u03ab\u0001"+
		"\u0000\u0000\u0000\u03ad\u03ae\u0001\u0000\u0000\u0000\u03ae\u03af\u0001"+
		"\u0000\u0000\u0000\u03af\u03b0\u0005\u00ba\u0000\u0000\u03b0O\u0001\u0000"+
		"\u0000\u0000\u03b1\u03b3\u0005\u0011\u0000\u0000\u03b2\u03b4\u0003\u0092"+
		"I\u0000\u03b3\u03b2\u0001\u0000\u0000\u0000\u03b3\u03b4\u0001\u0000\u0000"+
		"\u0000\u03b4\u03b5\u0001\u0000\u0000\u0000\u03b5\u03cb\u0005\u00ba\u0000"+
		"\u0000\u03b6\u03b7\u0005\u0011\u0000\u0000\u03b7\u03b8\u0005\u0013\u0000"+
		"\u0000\u03b8\u03b9\u0003\u0092I\u0000\u03b9\u03ba\u0005\u00ba\u0000\u0000"+
		"\u03ba\u03cb\u0001\u0000\u0000\u0000\u03bb\u03bc\u0005\u0011\u0000\u0000"+
		"\u03bc\u03bd\u0005\u0014\u0000\u0000\u03bd\u03be\u0003z=\u0000\u03be\u03bf"+
		"\u0005\u00ba\u0000\u0000\u03bf\u03cb\u0001\u0000\u0000\u0000\u03c0\u03c1"+
		"\u0005\u0011\u0000\u0000\u03c1\u03c2\u0005\u0014\u0000\u0000\u03c2\u03c3"+
		"\u0005(\u0000\u0000\u03c3\u03c6\u0003\u0092I\u0000\u03c4\u03c5\u0005\u008c"+
		"\u0000\u0000\u03c5\u03c7\u0003.\u0017\u0000\u03c6\u03c4\u0001\u0000\u0000"+
		"\u0000\u03c6\u03c7\u0001\u0000\u0000\u0000\u03c7\u03c8\u0001\u0000\u0000"+
		"\u0000\u03c8\u03c9\u0005\u00ba\u0000\u0000\u03c9\u03cb\u0001\u0000\u0000"+
		"\u0000\u03ca\u03b1\u0001\u0000\u0000\u0000\u03ca\u03b6\u0001\u0000\u0000"+
		"\u0000\u03ca\u03bb\u0001\u0000\u0000\u0000\u03ca\u03c0\u0001\u0000\u0000"+
		"\u0000\u03cbQ\u0001\u0000\u0000\u0000\u03cc\u03ce\u0005\u0012\u0000\u0000"+
		"\u03cd\u03cf\u0007\u0004\u0000\u0000\u03ce\u03cd\u0001\u0000\u0000\u0000"+
		"\u03ce\u03cf\u0001\u0000\u0000\u0000\u03cf\u03d6\u0001\u0000\u0000\u0000"+
		"\u03d0\u03d3\u0005\u00c5\u0000\u0000\u03d1\u03d2\u0005\u00bb\u0000\u0000"+
		"\u03d2\u03d4\u0003.\u0017\u0000\u03d3\u03d1\u0001\u0000\u0000\u0000\u03d3"+
		"\u03d4\u0001\u0000\u0000\u0000\u03d4\u03d7\u0001\u0000\u0000\u0000\u03d5"+
		"\u03d7\u0005\u00c7\u0000\u0000\u03d6\u03d0\u0001\u0000\u0000\u0000\u03d6"+
		"\u03d5\u0001\u0000\u0000\u0000\u03d6\u03d7\u0001\u0000\u0000\u0000\u03d7"+
		"\u03da\u0001\u0000\u0000\u0000\u03d8\u03d9\u0005\u008c\u0000\u0000\u03d9"+
		"\u03db\u0003T*\u0000\u03da\u03d8\u0001\u0000\u0000\u0000\u03da\u03db\u0001"+
		"\u0000\u0000\u0000\u03db\u03dc\u0001\u0000\u0000\u0000\u03dc\u03dd\u0005"+
		"\u00ba\u0000\u0000\u03ddS\u0001\u0000\u0000\u0000\u03de\u03e3\u0003V+"+
		"\u0000\u03df\u03e0\u0005\u00bb\u0000\u0000\u03e0\u03e2\u0003V+\u0000\u03e1"+
		"\u03df\u0001\u0000\u0000\u0000\u03e2\u03e5\u0001\u0000\u0000\u0000\u03e3"+
		"\u03e1\u0001\u0000\u0000\u0000\u03e3\u03e4\u0001\u0000\u0000\u0000\u03e4"+
		"U\u0001\u0000\u0000\u0000\u03e5\u03e3\u0001\u0000\u0000\u0000\u03e6\u03e7"+
		"\u0007\u0005\u0000\u0000\u03e7\u03e8\u0005\u00a9\u0000\u0000\u03e8\u03e9"+
		"\u0003\u0092I\u0000\u03e9W\u0001\u0000\u0000\u0000\u03ea\u03eb\u0005\u0097"+
		"\u0000\u0000\u03eb\u03ee\u0003\u0092I\u0000\u03ec\u03ed\u0005\u00bb\u0000"+
		"\u0000\u03ed\u03ef\u0003\u0092I\u0000\u03ee\u03ec\u0001\u0000\u0000\u0000"+
		"\u03ee\u03ef\u0001\u0000\u0000\u0000\u03ef\u03f0\u0001\u0000\u0000\u0000"+
		"\u03f0\u03f1\u0005\u00ba\u0000\u0000\u03f1Y\u0001\u0000\u0000\u0000\u03f2"+
		"\u03f3\u0005\u0086\u0000\u0000\u03f3\u03f4\u0005\u00ba\u0000\u0000\u03f4"+
		"[\u0001\u0000\u0000\u0000\u03f5\u03f7\u0005L\u0000\u0000\u03f6\u03f8\u0007"+
		"\u0006\u0000\u0000\u03f7\u03f6\u0001\u0000\u0000\u0000\u03f7\u03f8\u0001"+
		"\u0000\u0000\u0000\u03f8\u03f9\u0001\u0000\u0000\u0000\u03f9\u03fa\u0005"+
		"\u00ba\u0000\u0000\u03fa]\u0001\u0000\u0000\u0000\u03fb\u03fd\u0005M\u0000"+
		"\u0000\u03fc\u03fe\u0007\u0006\u0000\u0000\u03fd\u03fc\u0001\u0000\u0000"+
		"\u0000\u03fd\u03fe\u0001\u0000\u0000\u0000\u03fe\u0404\u0001\u0000\u0000"+
		"\u0000\u03ff\u0401\u00050\u0000\u0000\u0400\u0402\u0005N\u0000\u0000\u0401"+
		"\u0400\u0001\u0000\u0000\u0000\u0401\u0402\u0001\u0000\u0000\u0000\u0402"+
		"\u0403\u0001\u0000\u0000\u0000\u0403\u0405\u0005\u00c7\u0000\u0000\u0404"+
		"\u03ff\u0001\u0000\u0000\u0000\u0404\u0405\u0001\u0000\u0000\u0000\u0405"+
		"\u0406\u0001\u0000\u0000\u0000\u0406\u0407\u0005\u00ba\u0000\u0000\u0407"+
		"_\u0001\u0000\u0000\u0000\u0408\u040a\u0003\u0002\u0001\u0000\u0409\u0408"+
		"\u0001\u0000\u0000\u0000\u0409\u040a\u0001\u0000\u0000\u0000\u040a\u040c"+
		"\u0001\u0000\u0000\u0000\u040b\u040d\u0005\u0001\u0000\u0000\u040c\u040b"+
		"\u0001\u0000\u0000\u0000\u040c\u040d\u0001\u0000\u0000\u0000\u040d\u040f"+
		"\u0001\u0000\u0000\u0000\u040e\u0410\u0003\u0006\u0003\u0000\u040f\u040e"+
		"\u0001\u0000\u0000\u0000\u040f\u0410\u0001\u0000\u0000\u0000\u0410\u0411"+
		"\u0001\u0000\u0000\u0000\u0411\u0412\u0005\u0002\u0000\u0000\u0412\u0414"+
		"\u0003\f\u0006\u0000\u0413\u0415\u0003b1\u0000\u0414\u0413\u0001\u0000"+
		"\u0000\u0000\u0414\u0415\u0001\u0000\u0000\u0000\u0415\u0416\u0001\u0000"+
		"\u0000\u0000\u0416\u0418\u0005\u0003\u0000\u0000\u0417\u0419\u0003\u0002"+
		"\u0001\u0000\u0418\u0417\u0001\u0000\u0000\u0000\u0418\u0419\u0001\u0000"+
		"\u0000\u0000\u0419\u041a\u0001\u0000\u0000\u0000\u041a\u041b\u0005\u00ba"+
		"\u0000\u0000\u041ba\u0001\u0000\u0000\u0000\u041c\u041d\u0005\u0004\u0000"+
		"\u0000\u041d\u041e\u0003d2\u0000\u041ec\u0001\u0000\u0000\u0000\u041f"+
		"\u0421\u0003f3\u0000\u0420\u041f\u0001\u0000\u0000\u0000\u0421\u0422\u0001"+
		"\u0000\u0000\u0000\u0422\u0420\u0001\u0000\u0000\u0000\u0422\u0423\u0001"+
		"\u0000\u0000\u0000\u0423e\u0001\u0000\u0000\u0000\u0424\u0425\u0005\n"+
		"\u0000\u0000\u0425\u0426\u0003h4\u0000\u0426\u0427\u0005\u0006\u0000\u0000"+
		"\u0427\u0428\u0003\f\u0006\u0000\u0428g\u0001\u0000\u0000\u0000\u0429"+
		"\u042e\u0003j5\u0000\u042a\u042b\u0005\u0089\u0000\u0000\u042b\u042d\u0003"+
		"j5\u0000\u042c\u042a\u0001\u0000\u0000\u0000\u042d\u0430\u0001\u0000\u0000"+
		"\u0000\u042e\u042c\u0001\u0000\u0000\u0000\u042e\u042f\u0001\u0000\u0000"+
		"\u0000\u042fi\u0001\u0000\u0000\u0000\u0430\u042e\u0001\u0000\u0000\u0000"+
		"\u0431\u0435\u0005\u00c7\u0000\u0000\u0432\u0433\u0005{\u0000\u0000\u0433"+
		"\u0435\u0005\u00c5\u0000\u0000\u0434\u0431\u0001\u0000\u0000\u0000\u0434"+
		"\u0432\u0001\u0000\u0000\u0000\u0435k\u0001\u0000\u0000\u0000\u0436\u0438"+
		"\u0005\u0080\u0000\u0000\u0437\u0439\u0007\u0007\u0000\u0000\u0438\u0437"+
		"\u0001\u0000\u0000\u0000\u0438\u0439\u0001\u0000\u0000\u0000\u0439\u043a"+
		"\u0001\u0000\u0000\u0000\u043a\u043b\u0005\u0081\u0000\u0000\u043b\u043c"+
		"\u0003n7\u0000\u043c\u043d\u0005\u00ba\u0000\u0000\u043dm\u0001\u0000"+
		"\u0000\u0000\u043e\u0443\u0003p8\u0000\u043f\u0440\u0005\u00bb\u0000\u0000"+
		"\u0440\u0442\u0003p8\u0000\u0441\u043f\u0001\u0000\u0000\u0000\u0442\u0445"+
		"\u0001\u0000\u0000\u0000\u0443\u0441\u0001\u0000\u0000\u0000\u0443\u0444"+
		"\u0001\u0000\u0000\u0000\u0444o\u0001\u0000\u0000\u0000\u0445\u0443\u0001"+
		"\u0000\u0000\u0000\u0446\u0447\u0003\u0012\t\u0000\u0447\u0448\u0005\u00a9"+
		"\u0000\u0000\u0448\u0449\u0003r9\u0000\u0449q\u0001\u0000\u0000\u0000"+
		"\u044a\u044b\u0005\u00c7\u0000\u0000\u044bs\u0001\u0000\u0000\u0000\u044c"+
		"\u044d\u0005\u0098\u0000\u0000\u044d\u0452\u0003\u0012\t\u0000\u044e\u044f"+
		"\u0005\u00bd\u0000\u0000\u044f\u0450\u0003.\u0017\u0000\u0450\u0451\u0005"+
		"\u00be\u0000\u0000\u0451\u0453\u0001\u0000\u0000\u0000\u0452\u044e\u0001"+
		"\u0000\u0000\u0000\u0452\u0453\u0001\u0000\u0000\u0000\u0453\u0456\u0001"+
		"\u0000\u0000\u0000\u0454\u0455\u0005\r\u0000\u0000\u0455\u0457\u0003z"+
		"=\u0000\u0456\u0454\u0001\u0000\u0000\u0000\u0456\u0457\u0001\u0000\u0000"+
		"\u0000\u0457\u0458\u0001\u0000\u0000\u0000\u0458\u0459\u0005\u00ba\u0000"+
		"\u0000\u0459u\u0001\u0000\u0000\u0000\u045a\u045b\u0005\u0099\u0000\u0000"+
		"\u045b\u045c\u0003\u0012\t\u0000\u045c\u045d\u0005\u00ba\u0000\u0000\u045d"+
		"w\u0001\u0000\u0000\u0000\u045e\u045f\u0005\u009a\u0000\u0000\u045f\u0460"+
		"\u0003\u0012\t\u0000\u0460\u0461\u0005*\u0000\u0000\u0461\u0462\u0003"+
		"\u001a\r\u0000\u0462\u0463\u0005\u00ba\u0000\u0000\u0463y\u0001\u0000"+
		"\u0000\u0000\u0464\u0466\u0003|>\u0000\u0465\u0464\u0001\u0000\u0000\u0000"+
		"\u0465\u0466\u0001\u0000\u0000\u0000\u0466\u0467\u0001\u0000\u0000\u0000"+
		"\u0467\u046f\u0003\u0082A\u0000\u0468\u046a\u0007\b\u0000\u0000\u0469"+
		"\u046b\u00052\u0000\u0000\u046a\u0469\u0001\u0000\u0000\u0000\u046a\u046b"+
		"\u0001\u0000\u0000\u0000\u046b\u046c\u0001\u0000\u0000\u0000\u046c\u046e"+
		"\u0003\u0082A\u0000\u046d\u0468\u0001\u0000\u0000\u0000\u046e\u0471\u0001"+
		"\u0000\u0000\u0000\u046f\u046d\u0001\u0000\u0000\u0000\u046f\u0470\u0001"+
		"\u0000\u0000\u0000\u0470{\u0001\u0000\u0000\u0000\u0471\u046f\u0001\u0000"+
		"\u0000\u0000\u0472\u0473\u0005=\u0000\u0000\u0473\u0474\u0003~?\u0000"+
		"\u0474}\u0001\u0000\u0000\u0000\u0475\u047a\u0003\u0080@\u0000\u0476\u0477"+
		"\u0005\u00bb\u0000\u0000\u0477\u0479\u0003\u0080@\u0000\u0478\u0476\u0001"+
		"\u0000\u0000\u0000\u0479\u047c\u0001\u0000\u0000\u0000\u047a\u0478\u0001"+
		"\u0000\u0000\u0000\u047a\u047b\u0001\u0000\u0000\u0000\u047b\u007f\u0001"+
		"\u0000\u0000\u0000\u047c\u047a\u0001\u0000\u0000\u0000\u047d\u0482\u0005"+
		"\u00c7\u0000\u0000\u047e\u047f\u0005\u00bd\u0000\u0000\u047f\u0480\u0003"+
		",\u0016\u0000\u0480\u0481\u0005\u00be\u0000\u0000\u0481\u0483\u0001\u0000"+
		"\u0000\u0000\u0482\u047e\u0001\u0000\u0000\u0000\u0482\u0483\u0001\u0000"+
		"\u0000\u0000\u0483\u0484\u0001\u0000\u0000\u0000\u0484\u0485\u0005\u0084"+
		"\u0000\u0000\u0485\u0486\u0005\u00bd\u0000\u0000\u0486\u0487\u0003z=\u0000"+
		"\u0487\u0488\u0005\u00be\u0000\u0000\u0488\u0081\u0001\u0000\u0000\u0000"+
		"\u0489\u0493\u0005#\u0000\u0000\u048a\u0490\u00051\u0000\u0000\u048b\u048c"+
		"\u0005u\u0000\u0000\u048c\u048d\u0005\u00bd\u0000\u0000\u048d\u048e\u0003"+
		".\u0017\u0000\u048e\u048f\u0005\u00be\u0000\u0000\u048f\u0491\u0001\u0000"+
		"\u0000\u0000\u0490\u048b\u0001\u0000\u0000\u0000\u0490\u0491\u0001\u0000"+
		"\u0000\u0000\u0491\u0494\u0001\u0000\u0000\u0000\u0492\u0494\u00052\u0000"+
		"\u0000\u0493\u048a\u0001\u0000\u0000\u0000\u0493\u0492\u0001\u0000\u0000"+
		"\u0000\u0493\u0494\u0001\u0000\u0000\u0000\u0494\u0495\u0001\u0000\u0000"+
		"\u0000\u0495\u0497\u0003\u0016\u000b\u0000\u0496\u0498\u0003\u001c\u000e"+
		"\u0000\u0497\u0496\u0001\u0000\u0000\u0000\u0497\u0498\u0001\u0000\u0000"+
		"\u0000\u0498\u049a\u0001\u0000\u0000\u0000\u0499\u049b\u0003$\u0012\u0000"+
		"\u049a\u0499\u0001\u0000\u0000\u0000\u049a\u049b\u0001\u0000\u0000\u0000"+
		"\u049b\u049d\u0001\u0000\u0000\u0000\u049c\u049e\u0003\u0084B\u0000\u049d"+
		"\u049c\u0001\u0000\u0000\u0000\u049d\u049e\u0001\u0000\u0000\u0000\u049e"+
		"\u04a0\u0001\u0000\u0000\u0000\u049f\u04a1\u0003\u0086C\u0000\u04a0\u049f"+
		"\u0001\u0000\u0000\u0000\u04a0\u04a1\u0001\u0000\u0000\u0000\u04a1\u04a3"+
		"\u0001\u0000\u0000\u0000\u04a2\u04a4\u0003\u0088D\u0000\u04a3\u04a2\u0001"+
		"\u0000\u0000\u0000\u04a3\u04a4\u0001\u0000\u0000\u0000\u04a4\u04a6\u0001"+
		"\u0000\u0000\u0000\u04a5\u04a7\u0003\u008cF\u0000\u04a6\u04a5\u0001\u0000"+
		"\u0000\u0000\u04a6\u04a7\u0001\u0000\u0000\u0000\u04a7\u0083\u0001\u0000"+
		"\u0000\u0000\u04a8\u04a9\u00053\u0000\u0000\u04a9\u04aa\u0005j\u0000\u0000"+
		"\u04aa\u04ab\u0003.\u0017\u0000\u04ab\u0085\u0001\u0000\u0000\u0000\u04ac"+
		"\u04ad\u00054\u0000\u0000\u04ad\u04ae\u0003\u0092I\u0000\u04ae\u0087\u0001"+
		"\u0000\u0000\u0000\u04af\u04b0\u00055\u0000\u0000\u04b0\u04b1\u0005j\u0000"+
		"\u0000\u04b1\u04b6\u0003\u008aE\u0000\u04b2\u04b3\u0005\u00bb\u0000\u0000"+
		"\u04b3\u04b5\u0003\u008aE\u0000\u04b4\u04b2\u0001\u0000\u0000\u0000\u04b5"+
		"\u04b8\u0001\u0000\u0000\u0000\u04b6\u04b4\u0001\u0000\u0000\u0000\u04b6"+
		"\u04b7\u0001\u0000\u0000\u0000\u04b7\u0089\u0001\u0000\u0000\u0000\u04b8"+
		"\u04b6\u0001\u0000\u0000\u0000\u04b9\u04bb\u0003\u0092I\u0000\u04ba\u04bc"+
		"\u0007\t\u0000\u0000\u04bb\u04ba\u0001\u0000\u0000\u0000\u04bb\u04bc\u0001"+
		"\u0000\u0000\u0000\u04bc\u04bf\u0001\u0000\u0000\u0000\u04bd\u04be\u0005"+
		":\u0000\u0000\u04be\u04c0\u0007\n\u0000\u0000\u04bf\u04bd\u0001\u0000"+
		"\u0000\u0000\u04bf\u04c0\u0001\u0000\u0000\u0000\u04c0\u008b\u0001\u0000"+
		"\u0000\u0000\u04c1\u04c4\u00056\u0000\u0000\u04c2\u04c5\u0003\u0092I\u0000"+
		"\u04c3\u04c5\u00052\u0000\u0000\u04c4\u04c2\u0001\u0000\u0000\u0000\u04c4"+
		"\u04c3\u0001\u0000\u0000\u0000\u04c5\u04c8\u0001\u0000\u0000\u0000\u04c6"+
		"\u04c7\u00057\u0000\u0000\u04c7\u04c9\u0003\u0092I\u0000\u04c8\u04c6\u0001"+
		"\u0000\u0000\u0000\u04c8\u04c9\u0001\u0000\u0000\u0000\u04c9\u008d\u0001"+
		"\u0000\u0000\u0000\u04ca\u04cf\u0003\u0090H\u0000\u04cb\u04cc\u0005\u00bb"+
		"\u0000\u0000\u04cc\u04ce\u0003\u0090H\u0000\u04cd\u04cb\u0001\u0000\u0000"+
		"\u0000\u04ce\u04d1\u0001\u0000\u0000\u0000\u04cf\u04cd\u0001\u0000\u0000"+
		"\u0000\u04cf\u04d0\u0001\u0000\u0000\u0000\u04d0\u008f\u0001\u0000\u0000"+
		"\u0000\u04d1\u04cf\u0001\u0000\u0000\u0000\u04d2\u04d3\u0005\u00c7\u0000"+
		"\u0000\u04d3\u04d4\u0003\n\u0005\u0000\u04d4\u0091\u0001\u0000\u0000\u0000"+
		"\u04d5\u04d6\u0006I\uffff\uffff\u0000\u04d6\u0516\u0003\u00a8T\u0000\u04d7"+
		"\u0516\u0003\u0012\t\u0000\u04d8\u0516\u0003\u009cN\u0000\u04d9\u0516"+
		"\u0003\u00a0P\u0000\u04da\u0516\u0003\u009aM\u0000\u04db\u04dc\u0005a"+
		"\u0000\u0000\u04dc\u0516\u0005\u00c5\u0000\u0000\u04dd\u04de\u0005C\u0000"+
		"\u0000\u04de\u04df\u0005\u00bd\u0000\u0000\u04df\u04e0\u0003\u0092I\u0000"+
		"\u04e0\u04e1\u0005\u0084\u0000\u0000\u04e1\u04e2\u0003\n\u0005\u0000\u04e2"+
		"\u04e3\u0005\u00be\u0000\u0000\u04e3\u0516\u0001\u0000\u0000\u0000\u04e4"+
		"\u04e5\u0005E\u0000\u0000\u04e5\u04e6\u0005\u00bd\u0000\u0000\u04e6\u04e7"+
		"\u0003.\u0017\u0000\u04e7\u04e8\u0005\u00be\u0000\u0000\u04e8\u0516\u0001"+
		"\u0000\u0000\u0000\u04e9\u04ea\u0005F\u0000\u0000\u04ea\u04eb\u0005\u00bd"+
		"\u0000\u0000\u04eb\u04ec\u0003\u0092I\u0000\u04ec\u04ed\u0005\u00bb\u0000"+
		"\u0000\u04ed\u04ee\u0003\u0092I\u0000\u04ee\u04ef\u0005\u00be\u0000\u0000"+
		"\u04ef\u0516\u0001\u0000\u0000\u0000\u04f0\u04f1\u0005G\u0000\u0000\u04f1"+
		"\u04f2\u0005\u00bd\u0000\u0000\u04f2\u04f3\u0003.\u0017\u0000\u04f3\u04f4"+
		"\u0005\u00be\u0000\u0000\u04f4\u0516\u0001\u0000\u0000\u0000\u04f5\u04f6"+
		"\u0005H\u0000\u0000\u04f6\u04f7\u0005\u00bd\u0000\u0000\u04f7\u04f8\u0003"+
		".\u0017\u0000\u04f8\u04f9\u0005\u00be\u0000\u0000\u04f9\u0516\u0001\u0000"+
		"\u0000\u0000\u04fa\u04fb\u0005B\u0000\u0000\u04fb\u04fc\u0005\u00bd\u0000"+
		"\u0000\u04fc\u04fd\u0003z=\u0000\u04fd\u04fe\u0005\u00be\u0000\u0000\u04fe"+
		"\u0516\u0001\u0000\u0000\u0000\u04ff\u0500\u0005\u00bd\u0000\u0000\u0500"+
		"\u0501\u0003z=\u0000\u0501\u0502\u0005\u00be\u0000\u0000\u0502\u0516\u0001"+
		"\u0000\u0000\u0000\u0503\u0504\u0005\u00bd\u0000\u0000\u0504\u0505\u0003"+
		"\u0092I\u0000\u0505\u0506\u0005\u00be\u0000\u0000\u0506\u0516\u0001\u0000"+
		"\u0000\u0000\u0507\u0508\u0005\u0087\u0000\u0000\u0508\u0516\u0003\u0092"+
		"I\u0005\u0509\u050b\u0005\t\u0000\u0000\u050a\u050c\u0003\u0092I\u0000"+
		"\u050b\u050a\u0001\u0000\u0000\u0000\u050b\u050c\u0001\u0000\u0000\u0000"+
		"\u050c\u050d\u0001\u0000\u0000\u0000\u050d\u0510\u0003\u0094J\u0000\u050e"+
		"\u050f\u0005\b\u0000\u0000\u050f\u0511\u0003\u0092I\u0000\u0510\u050e"+
		"\u0001\u0000\u0000\u0000\u0510\u0511\u0001\u0000\u0000\u0000\u0511\u0512"+
		"\u0001\u0000\u0000\u0000\u0512\u0513\u0005\u0003\u0000\u0000\u0513\u0516"+
		"\u0001\u0000\u0000\u0000\u0514\u0516\u0003\u0098L\u0000\u0515\u04d5\u0001"+
		"\u0000\u0000\u0000\u0515\u04d7\u0001\u0000\u0000\u0000\u0515\u04d8\u0001"+
		"\u0000\u0000\u0000\u0515\u04d9\u0001\u0000\u0000\u0000\u0515\u04da\u0001"+
		"\u0000\u0000\u0000\u0515\u04db\u0001\u0000\u0000\u0000\u0515\u04dd\u0001"+
		"\u0000\u0000\u0000\u0515\u04e4\u0001\u0000\u0000\u0000\u0515\u04e9\u0001"+
		"\u0000\u0000\u0000\u0515\u04f0\u0001\u0000\u0000\u0000\u0515\u04f5\u0001"+
		"\u0000\u0000\u0000\u0515\u04fa\u0001\u0000\u0000\u0000\u0515\u04ff\u0001"+
		"\u0000\u0000\u0000\u0515\u0503\u0001\u0000\u0000\u0000\u0515\u0507\u0001"+
		"\u0000\u0000\u0000\u0515\u0509\u0001\u0000\u0000\u0000\u0515\u0514\u0001"+
		"\u0000\u0000\u0000\u0516\u055f\u0001\u0000\u0000\u0000\u0517\u0518\n\u000e"+
		"\u0000\u0000\u0518\u0519\u0007\u000b\u0000\u0000\u0519\u055e\u0003\u0092"+
		"I\u000f\u051a\u051b\n\r\u0000\u0000\u051b\u051c\u0007\f\u0000\u0000\u051c"+
		"\u055e\u0003\u0092I\u000e\u051d\u051e\n\f\u0000\u0000\u051e\u051f\u0005"+
		"\u00a8\u0000\u0000\u051f\u055e\u0003\u0092I\r\u0520\u0522\n\n\u0000\u0000"+
		"\u0521\u0523\u0005\u0087\u0000\u0000\u0522\u0521\u0001\u0000\u0000\u0000"+
		"\u0522\u0523\u0001\u0000\u0000\u0000\u0523\u0524\u0001\u0000\u0000\u0000"+
		"\u0524\u0525\u0005>\u0000\u0000\u0525\u0526\u0003\u0092I\u0000\u0526\u0527"+
		"\u0005\u0088\u0000\u0000\u0527\u0528\u0003\u0092I\u000b\u0528\u055e\u0001"+
		"\u0000\u0000\u0000\u0529\u052b\n\t\u0000\u0000\u052a\u052c\u0005\u0087"+
		"\u0000\u0000\u052b\u052a\u0001\u0000\u0000\u0000\u052b\u052c\u0001\u0000"+
		"\u0000\u0000\u052c\u052d\u0001\u0000\u0000\u0000\u052d\u052e\u0007\r\u0000"+
		"\u0000\u052e\u055e\u0003\u0092I\n\u052f\u0530\n\b\u0000\u0000\u0530\u0531"+
		"\u0005\u00b7\u0000\u0000\u0531\u0532\u00050\u0000\u0000\u0532\u055e\u0003"+
		"\u0092I\t\u0533\u0534\n\u0006\u0000\u0000\u0534\u0535\u0007\u000e\u0000"+
		"\u0000\u0535\u055e\u0003\u0092I\u0007\u0536\u0537\n\u0016\u0000\u0000"+
		"\u0537\u0538\u0005\u00b4\u0000\u0000\u0538\u055e\u0003\n\u0005\u0000\u0539"+
		"\u053a\n\u000b\u0000\u0000\u053a\u053b\u0007\u000f\u0000\u0000\u053b\u053f"+
		"\u0003\u0092I\u0000\u053c\u053d\u0005\u00bd\u0000\u0000\u053d\u053e\u0005"+
		"\u00af\u0000\u0000\u053e\u0540\u0005\u00be\u0000\u0000\u053f\u053c\u0001"+
		"\u0000\u0000\u0000\u053f\u0540\u0001\u0000\u0000\u0000\u0540\u055e\u0001"+
		"\u0000\u0000\u0000\u0541\u0542\n\u0007\u0000\u0000\u0542\u0543\u0007\u000f"+
		"\u0000\u0000\u0543\u0544\u0007\u0010\u0000\u0000\u0544\u0547\u0005\u00bd"+
		"\u0000\u0000\u0545\u0548\u0003z=\u0000\u0546\u0548\u0003.\u0017\u0000"+
		"\u0547\u0545\u0001\u0000\u0000\u0000\u0547\u0546\u0001\u0000\u0000\u0000"+
		"\u0548\u0549\u0001\u0000\u0000\u0000\u0549\u054a\u0005\u00be\u0000\u0000"+
		"\u054a\u055e\u0001\u0000\u0000\u0000\u054b\u054c\n\u0004\u0000\u0000\u054c"+
		"\u054e\u0005\u0085\u0000\u0000\u054d\u054f\u0005\u0087\u0000\u0000\u054e"+
		"\u054d\u0001\u0000\u0000\u0000\u054e\u054f\u0001\u0000\u0000\u0000\u054f"+
		"\u0550\u0001\u0000\u0000\u0000\u0550\u055e\u0005\u0086\u0000\u0000\u0551"+
		"\u0553\n\u0003\u0000\u0000\u0552\u0554\u0005\u0087\u0000\u0000\u0553\u0552"+
		"\u0001\u0000\u0000\u0000\u0553\u0554\u0001\u0000\u0000\u0000\u0554\u0555"+
		"\u0001\u0000\u0000\u0000\u0555\u0556\u0005h\u0000\u0000\u0556\u0559\u0005"+
		"\u00bd\u0000\u0000\u0557\u055a\u0003z=\u0000\u0558\u055a\u0003.\u0017"+
		"\u0000\u0559\u0557\u0001\u0000\u0000\u0000\u0559\u0558\u0001\u0000\u0000"+
		"\u0000\u055a\u055b\u0001\u0000\u0000\u0000\u055b\u055c\u0005\u00be\u0000"+
		"\u0000\u055c\u055e\u0001\u0000\u0000\u0000\u055d\u0517\u0001\u0000\u0000"+
		"\u0000\u055d\u051a\u0001\u0000\u0000\u0000\u055d\u051d\u0001\u0000\u0000"+
		"\u0000\u055d\u0520\u0001\u0000\u0000\u0000\u055d\u0529\u0001\u0000\u0000"+
		"\u0000\u055d\u052f\u0001\u0000\u0000\u0000\u055d\u0533\u0001\u0000\u0000"+
		"\u0000\u055d\u0536\u0001\u0000\u0000\u0000\u055d\u0539\u0001\u0000\u0000"+
		"\u0000\u055d\u0541\u0001\u0000\u0000\u0000\u055d\u054b\u0001\u0000\u0000"+
		"\u0000\u055d\u0551\u0001\u0000\u0000\u0000\u055e\u0561\u0001\u0000\u0000"+
		"\u0000\u055f\u055d\u0001\u0000\u0000\u0000\u055f\u0560\u0001\u0000\u0000"+
		"\u0000\u0560\u0093\u0001\u0000\u0000\u0000\u0561\u055f\u0001\u0000\u0000"+
		"\u0000\u0562\u0564\u0003\u0096K\u0000\u0563\u0562\u0001\u0000\u0000\u0000"+
		"\u0564\u0565\u0001\u0000\u0000\u0000\u0565\u0563\u0001\u0000\u0000\u0000"+
		"\u0565\u0566\u0001\u0000\u0000\u0000\u0566\u0095\u0001\u0000\u0000\u0000"+
		"\u0567\u0568\u0005\n\u0000\u0000\u0568\u0569\u0003\u0092I\u0000\u0569"+
		"\u056a\u0005\u0006\u0000\u0000\u056a\u056b\u0003\u0092I\u0000\u056b\u0097"+
		"\u0001\u0000\u0000\u0000\u056c\u056d\u0005k\u0000\u0000\u056d\u056e\u0005"+
		"\u00bf\u0000\u0000\u056e\u056f\u0003.\u0017\u0000\u056f\u0570\u0005\u00c0"+
		"\u0000\u0000\u0570\u0577\u0001\u0000\u0000\u0000\u0571\u0572\u0003\u0012"+
		"\t\u0000\u0572\u0573\u0005\u00bf\u0000\u0000\u0573\u0574\u0003\u0092I"+
		"\u0000\u0574\u0575\u0005\u00c0\u0000\u0000\u0575\u0577\u0001\u0000\u0000"+
		"\u0000\u0576\u056c\u0001\u0000\u0000\u0000\u0576\u0571\u0001\u0000\u0000"+
		"\u0000\u0577\u0099\u0001\u0000\u0000\u0000\u0578\u0579\u0007\u0011\u0000"+
		"\u0000\u0579\u009b\u0001\u0000\u0000\u0000\u057a\u057b\u0007\u0012\u0000"+
		"\u0000\u057b\u057c\u0005\u00bd\u0000\u0000\u057c\u057d\u0003\u009eO\u0000"+
		"\u057d\u057e\u0005\u00be\u0000\u0000\u057e\u009d\u0001\u0000\u0000\u0000"+
		"\u057f\u0580\u00051\u0000\u0000\u0580\u0590\u0003.\u0017\u0000\u0581\u0590"+
		"\u0003.\u0017\u0000\u0582\u0583\u0003\u0092I\u0000\u0583\u0584\u0005h"+
		"\u0000\u0000\u0584\u0585\u0003\u0092I\u0000\u0585\u0590\u0001\u0000\u0000"+
		"\u0000\u0586\u0587\u0003\u0092I\u0000\u0587\u0588\u0005+\u0000\u0000\u0588"+
		"\u058b\u0003\u0092I\u0000\u0589\u058a\u0005\r\u0000\u0000\u058a\u058c"+
		"\u0003\u0092I\u0000\u058b\u0589\u0001\u0000\u0000\u0000\u058b\u058c\u0001"+
		"\u0000\u0000\u0000\u058c\u0590\u0001\u0000\u0000\u0000\u058d\u0590\u0005"+
		"\u00b1\u0000\u0000\u058e\u0590\u0001\u0000\u0000\u0000\u058f\u057f\u0001"+
		"\u0000\u0000\u0000\u058f\u0581\u0001\u0000\u0000\u0000\u058f\u0582\u0001"+
		"\u0000\u0000\u0000\u058f\u0586\u0001\u0000\u0000\u0000\u058f\u058d\u0001"+
		"\u0000\u0000\u0000\u058f\u058e\u0001\u0000\u0000\u0000\u0590\u009f\u0001"+
		"\u0000\u0000\u0000\u0591\u0592\u0003\u009cN\u0000\u0592\u0593\u0005J\u0000"+
		"\u0000\u0593\u0594\u0005\u00bd\u0000\u0000\u0594\u0595\u0003\u00a2Q\u0000"+
		"\u0595\u0596\u0005\u00be\u0000\u0000\u0596\u00a1\u0001\u0000\u0000\u0000"+
		"\u0597\u0598\u0005K\u0000\u0000\u0598\u0599\u0005j\u0000\u0000\u0599\u059b"+
		"\u0003.\u0017\u0000\u059a\u0597\u0001\u0000\u0000\u0000\u059a\u059b\u0001"+
		"\u0000\u0000\u0000\u059b\u059d\u0001\u0000\u0000\u0000\u059c\u059e\u0003"+
		"\u0088D\u0000\u059d\u059c\u0001\u0000\u0000\u0000\u059d\u059e\u0001\u0000"+
		"\u0000\u0000\u059e\u00a3\u0001\u0000\u0000\u0000\u059f\u05a0\u0005=\u0000"+
		"\u0000\u05a0\u05ab\u0003~?\u0000\u05a1\u05ac\u0003*\u0015\u0000\u05a2"+
		"\u05ac\u00030\u0018\u0000\u05a3\u05ac\u00036\u001b\u0000\u05a4\u05ac\u0003"+
		"\u0014\n\u0000\u05a5\u05a7\b\u0002\u0000\u0000\u05a6\u05a5\u0001\u0000"+
		"\u0000\u0000\u05a7\u05a8\u0001\u0000\u0000\u0000\u05a8\u05a6\u0001\u0000"+
		"\u0000\u0000\u05a8\u05a9\u0001\u0000\u0000\u0000\u05a9\u05aa\u0001\u0000"+
		"\u0000\u0000\u05aa\u05ac\u0005\u00ba\u0000\u0000\u05ab\u05a1\u0001\u0000"+
		"\u0000\u0000\u05ab\u05a2\u0001\u0000\u0000\u0000\u05ab\u05a3\u0001\u0000"+
		"\u0000\u0000\u05ab\u05a4\u0001\u0000\u0000\u0000\u05ab\u05a6\u0001\u0000"+
		"\u0000\u0000\u05ac\u00a5\u0001\u0000\u0000\u0000\u05ad\u05b1\u0007\u0013"+
		"\u0000\u0000\u05ae\u05b0\b\u0002\u0000\u0000\u05af\u05ae\u0001\u0000\u0000"+
		"\u0000\u05b0\u05b3\u0001\u0000\u0000\u0000\u05b1\u05af\u0001\u0000\u0000"+
		"\u0000\u05b1\u05b2\u0001\u0000\u0000\u0000\u05b2\u05b4\u0001\u0000\u0000"+
		"\u0000\u05b3\u05b1\u0001\u0000\u0000\u0000\u05b4\u05b5\u0005\u00ba\u0000"+
		"\u0000\u05b5\u00a7\u0001\u0000\u0000\u0000\u05b6\u05b7\u0007\u0014\u0000"+
		"\u0000\u05b7\u00a9\u0001\u0000\u0000\u0000\u05b8\u05ba\u0005\u0015\u0000"+
		"\u0000\u05b9\u05bb\u0007\u0015\u0000\u0000\u05ba\u05b9\u0001\u0000\u0000"+
		"\u0000\u05ba\u05bb\u0001\u0000\u0000\u0000\u05bb\u05bc\u0001\u0000\u0000"+
		"\u0000\u05bc\u05c0\u0005 \u0000\u0000\u05bd\u05be\u0005\u0005\u0000\u0000"+
		"\u05be\u05bf\u0005\u0087\u0000\u0000\u05bf\u05c1\u0005B\u0000\u0000\u05c0"+
		"\u05bd\u0001\u0000\u0000\u0000\u05c0\u05c1\u0001\u0000\u0000\u0000\u05c1"+
		"\u05c2\u0001\u0000\u0000\u0000\u05c2\u05c3\u0005\u00c7\u0000\u0000\u05c3"+
		"\u05c4\u0005\u00bd\u0000\u0000\u05c4\u05c5\u0003\u00acV\u0000\u05c5\u05c7"+
		"\u0005\u00be\u0000\u0000\u05c6\u05c8\u0003\u00b0X\u0000\u05c7\u05c6\u0001"+
		"\u0000\u0000\u0000\u05c7\u05c8\u0001\u0000\u0000\u0000\u05c8\u05c9\u0001"+
		"\u0000\u0000\u0000\u05c9\u05ca\u0005\u00ba\u0000\u0000\u05ca\u00ab\u0001"+
		"\u0000\u0000\u0000\u05cb\u05d0\u0003\u00aeW\u0000\u05cc\u05cd\u0005\u00bb"+
		"\u0000\u0000\u05cd\u05cf\u0003\u00aeW\u0000\u05ce\u05cc\u0001\u0000\u0000"+
		"\u0000\u05cf\u05d2\u0001\u0000\u0000\u0000\u05d0\u05ce\u0001\u0000\u0000"+
		"\u0000\u05d0\u05d1\u0001\u0000\u0000\u0000\u05d1\u00ad\u0001\u0000\u0000"+
		"\u0000\u05d2\u05d0\u0001\u0000\u0000\u0000\u05d3\u05d4\u0005\u00c7\u0000"+
		"\u0000\u05d4\u05d9\u0003\n\u0005\u0000\u05d5\u05d7\u0005\u0087\u0000\u0000"+
		"\u05d6\u05d5\u0001\u0000\u0000\u0000\u05d6\u05d7\u0001\u0000\u0000\u0000"+
		"\u05d7\u05d8\u0001\u0000\u0000\u0000\u05d8\u05da\u0005\u0086\u0000\u0000"+
		"\u05d9\u05d6\u0001\u0000\u0000\u0000\u05d9\u05da\u0001\u0000\u0000\u0000"+
		"\u05da\u05dd\u0001\u0000\u0000\u0000\u05db\u05dc\u0005\u008b\u0000\u0000"+
		"\u05dc\u05de\u0003\u0092I\u0000\u05dd\u05db\u0001\u0000\u0000\u0000\u05dd"+
		"\u05de\u0001\u0000\u0000\u0000\u05de\u00af\u0001\u0000\u0000\u0000\u05df"+
		"\u05e0\u0005u\u0000\u0000\u05e0\u05e6\u0005L\u0000\u0000\u05e1\u05e7\u0005"+
		"\u0016\u0000\u0000\u05e2\u05e3\u0005&\u0000\u0000\u05e3\u05e7\u0005\u009f"+
		"\u0000\u0000\u05e4\u05e5\u0005\u00a0\u0000\u0000\u05e5\u05e7\u0005\u009f"+
		"\u0000\u0000\u05e6\u05e1\u0001\u0000\u0000\u0000\u05e6\u05e2\u0001\u0000"+
		"\u0000\u0000\u05e6\u05e4\u0001\u0000\u0000\u0000\u05e7\u00b1\u0001\u0000"+
		"\u0000\u0000\u00ca\u00b3\u00b6\u00bb\u00bf\u00c2\u00ce\u00d2\u00d7\u00dd"+
		"\u00e7\u00f3\u00f6\u0101\u0104\u010b\u010e\u0116\u011c\u0125\u012b\u0130"+
		"\u013d\u013f\u0145\u0149\u014f\u0170\u017c\u0184\u0187\u018a\u0190\u0194"+
		"\u0197\u019a\u019d\u01a0\u01a3\u01ad\u01b0\u01b4\u01b7\u01be\u01c6\u01ce"+
		"\u01d2\u01d5\u01dc\u01e0\u01e3\u01e8\u01eb\u01ee\u01f4\u01f7\u01f9\u01fe"+
		"\u0209\u020b\u0210\u0214\u0218\u021b\u0223\u0226\u022e\u0232\u023d\u0244"+
		"\u0251\u0258\u025a\u0265\u026c\u0271\u0274\u027b\u0283\u0287\u028f\u0293"+
		"\u0296\u029f\u02a2\u02a5\u02ab\u02ad\u02b6\u02be\u02c5\u02ce\u02d2\u02d5"+
		"\u02dd\u02e0\u02e3\u02e9\u02eb\u02fa\u0307\u030c\u0314\u0319\u0322\u032a"+
		"\u0331\u0336\u033f\u0344\u034a\u0352\u0359\u035e\u0369\u036d\u0374\u0377"+
		"\u037a\u037d\u0380\u0383\u0386\u0389\u038f\u0399\u039f\u03a3\u03a9\u03ad"+
		"\u03b3\u03c6\u03ca\u03ce\u03d3\u03d6\u03da\u03e3\u03ee\u03f7\u03fd\u0401"+
		"\u0404\u0409\u040c\u040f\u0414\u0418\u0422\u042e\u0434\u0438\u0443\u0452"+
		"\u0456\u0465\u046a\u046f\u047a\u0482\u0490\u0493\u0497\u049a\u049d\u04a0"+
		"\u04a3\u04a6\u04b6\u04bb\u04bf\u04c4\u04c8\u04cf\u050b\u0510\u0515\u0522"+
		"\u052b\u053f\u0547\u054e\u0553\u0559\u055d\u055f\u0565\u0576\u058b\u058f"+
		"\u059a\u059d\u05a8\u05ab\u05b1\u05ba\u05c0\u05c7\u05d0\u05d6\u05d9\u05dd"+
		"\u05e6";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}