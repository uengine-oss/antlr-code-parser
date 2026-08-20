// Generated from antlr-grammars/CPreprocessorParser.g4 by ANTLR 4.13.2

package legacymodernizer.parser.antlr.c.preprocessor;

import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue", "this-escape"})
public class CPreprocessorParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.13.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		HASHHASH=1, HASH=2, DEFINE=3, ELLIPSIS=4, LPAREN=5, RPAREN=6, COMMA=7,
		STRING_LITERAL=8, CHARACTER_CONSTANT=9, PP_NUMBER=10, IDENTIFIER=11, BLOCK_COMMENT_START=12,
		LINE_COMMENT=13, WS=14, NEWLINE=15, PUNCTUATOR=16, OTHER=17, BLOCK_COMMENT_END=18,
		BLOCK_COMMENT_STAR=19;
	public static final int
		RULE_preprocessingFile = 0, RULE_logicalLine = 1, RULE_finalLogicalLine = 2,
		RULE_defineLine = 3, RULE_malformedDefineLine = 4, RULE_otherLine = 5,
		RULE_defineDirective = 6, RULE_macroName = 7, RULE_parameterList = 8,
		RULE_macroParameter = 9, RULE_objectReplacement = 10, RULE_replacementList = 11,
		RULE_replacementTokens = 12, RULE_nonLparenReplacementToken = 13, RULE_replacementToken = 14,
		RULE_requiredHorizontal = 15, RULE_horizontal = 16, RULE_ppToken = 17;
	private static String[] makeRuleNames() {
		return new String[] {
			"preprocessingFile", "logicalLine", "finalLogicalLine", "defineLine",
			"malformedDefineLine", "otherLine", "defineDirective", "macroName", "parameterList",
			"macroParameter", "objectReplacement", "replacementList", "replacementTokens",
			"nonLparenReplacementToken", "replacementToken", "requiredHorizontal",
			"horizontal", "ppToken"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, null, null, "'define'", "'...'", "'('", "')'", "','", null, null,
			null, null, "'/*'", null, null, null, null, null, "'*/'", "'*'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "HASHHASH", "HASH", "DEFINE", "ELLIPSIS", "LPAREN", "RPAREN", "COMMA",
			"STRING_LITERAL", "CHARACTER_CONSTANT", "PP_NUMBER", "IDENTIFIER", "BLOCK_COMMENT_START",
			"LINE_COMMENT", "WS", "NEWLINE", "PUNCTUATOR", "OTHER", "BLOCK_COMMENT_END",
			"BLOCK_COMMENT_STAR"
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
	public String getGrammarFileName() { return "CPreprocessorParser.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public CPreprocessorParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@SuppressWarnings("CheckReturnValue")
	public static class PreprocessingFileContext extends ParserRuleContext {
		public TerminalNode EOF() { return getToken(CPreprocessorParser.EOF, 0); }
		public List<LogicalLineContext> logicalLine() {
			return getRuleContexts(LogicalLineContext.class);
		}
		public LogicalLineContext logicalLine(int i) {
			return getRuleContext(LogicalLineContext.class,i);
		}
		public List<TerminalNode> NEWLINE() { return getTokens(CPreprocessorParser.NEWLINE); }
		public TerminalNode NEWLINE(int i) {
			return getToken(CPreprocessorParser.NEWLINE, i);
		}
		public FinalLogicalLineContext finalLogicalLine() {
			return getRuleContext(FinalLogicalLineContext.class,0);
		}
		public PreprocessingFileContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_preprocessingFile; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CPreprocessorParserListener ) ((CPreprocessorParserListener)listener).enterPreprocessingFile(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CPreprocessorParserListener ) ((CPreprocessorParserListener)listener).exitPreprocessingFile(this);
		}
	}

	public final PreprocessingFileContext preprocessingFile() throws RecognitionException {
		PreprocessingFileContext _localctx = new PreprocessingFileContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_preprocessingFile);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(41);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,0,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(36);
					logicalLine();
					setState(37);
					match(NEWLINE);
					}
					}
				}
				setState(43);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,0,_ctx);
			}
			setState(45);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 491518L) != 0)) {
				{
				setState(44);
				finalLogicalLine();
				}
			}

			setState(47);
			match(EOF);
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
	public static class LogicalLineContext extends ParserRuleContext {
		public DefineLineContext defineLine() {
			return getRuleContext(DefineLineContext.class,0);
		}
		public MalformedDefineLineContext malformedDefineLine() {
			return getRuleContext(MalformedDefineLineContext.class,0);
		}
		public OtherLineContext otherLine() {
			return getRuleContext(OtherLineContext.class,0);
		}
		public LogicalLineContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_logicalLine; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CPreprocessorParserListener ) ((CPreprocessorParserListener)listener).enterLogicalLine(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CPreprocessorParserListener ) ((CPreprocessorParserListener)listener).exitLogicalLine(this);
		}
	}

	public final LogicalLineContext logicalLine() throws RecognitionException {
		LogicalLineContext _localctx = new LogicalLineContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_logicalLine);
		try {
			setState(52);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,2,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(49);
				defineLine();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(50);
				malformedDefineLine();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(51);
				otherLine();
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
	public static class FinalLogicalLineContext extends ParserRuleContext {
		public DefineLineContext defineLine() {
			return getRuleContext(DefineLineContext.class,0);
		}
		public MalformedDefineLineContext malformedDefineLine() {
			return getRuleContext(MalformedDefineLineContext.class,0);
		}
		public List<PpTokenContext> ppToken() {
			return getRuleContexts(PpTokenContext.class);
		}
		public PpTokenContext ppToken(int i) {
			return getRuleContext(PpTokenContext.class,i);
		}
		public FinalLogicalLineContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_finalLogicalLine; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CPreprocessorParserListener ) ((CPreprocessorParserListener)listener).enterFinalLogicalLine(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CPreprocessorParserListener ) ((CPreprocessorParserListener)listener).exitFinalLogicalLine(this);
		}
	}

	public final FinalLogicalLineContext finalLogicalLine() throws RecognitionException {
		FinalLogicalLineContext _localctx = new FinalLogicalLineContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_finalLogicalLine);
		int _la;
		try {
			setState(61);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,4,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(54);
				defineLine();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(55);
				malformedDefineLine();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(57);
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(56);
					ppToken();
					}
					}
					setState(59);
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & 491518L) != 0) );
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
	public static class DefineLineContext extends ParserRuleContext {
		public TerminalNode HASH() { return getToken(CPreprocessorParser.HASH, 0); }
		public DefineDirectiveContext defineDirective() {
			return getRuleContext(DefineDirectiveContext.class,0);
		}
		public List<HorizontalContext> horizontal() {
			return getRuleContexts(HorizontalContext.class);
		}
		public HorizontalContext horizontal(int i) {
			return getRuleContext(HorizontalContext.class,i);
		}
		public DefineLineContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_defineLine; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CPreprocessorParserListener ) ((CPreprocessorParserListener)listener).enterDefineLine(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CPreprocessorParserListener ) ((CPreprocessorParserListener)listener).exitDefineLine(this);
		}
	}

	public final DefineLineContext defineLine() throws RecognitionException {
		DefineLineContext _localctx = new DefineLineContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_defineLine);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(66);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 290816L) != 0)) {
				{
				{
				setState(63);
				horizontal();
				}
				}
				setState(68);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(69);
			match(HASH);
			setState(73);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 290816L) != 0)) {
				{
				{
				setState(70);
				horizontal();
				}
				}
				setState(75);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(76);
			defineDirective();
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
	public static class MalformedDefineLineContext extends ParserRuleContext {
		public TerminalNode HASH() { return getToken(CPreprocessorParser.HASH, 0); }
		public TerminalNode DEFINE() { return getToken(CPreprocessorParser.DEFINE, 0); }
		public List<HorizontalContext> horizontal() {
			return getRuleContexts(HorizontalContext.class);
		}
		public HorizontalContext horizontal(int i) {
			return getRuleContext(HorizontalContext.class,i);
		}
		public List<PpTokenContext> ppToken() {
			return getRuleContexts(PpTokenContext.class);
		}
		public PpTokenContext ppToken(int i) {
			return getRuleContext(PpTokenContext.class,i);
		}
		public MalformedDefineLineContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_malformedDefineLine; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CPreprocessorParserListener ) ((CPreprocessorParserListener)listener).enterMalformedDefineLine(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CPreprocessorParserListener ) ((CPreprocessorParserListener)listener).exitMalformedDefineLine(this);
		}
	}

	public final MalformedDefineLineContext malformedDefineLine() throws RecognitionException {
		MalformedDefineLineContext _localctx = new MalformedDefineLineContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_malformedDefineLine);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(81);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 290816L) != 0)) {
				{
				{
				setState(78);
				horizontal();
				}
				}
				setState(83);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(84);
			match(HASH);
			setState(88);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 290816L) != 0)) {
				{
				{
				setState(85);
				horizontal();
				}
				}
				setState(90);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(91);
			match(DEFINE);
			setState(95);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 491518L) != 0)) {
				{
				{
				setState(92);
				ppToken();
				}
				}
				setState(97);
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
	public static class OtherLineContext extends ParserRuleContext {
		public List<PpTokenContext> ppToken() {
			return getRuleContexts(PpTokenContext.class);
		}
		public PpTokenContext ppToken(int i) {
			return getRuleContext(PpTokenContext.class,i);
		}
		public OtherLineContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_otherLine; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CPreprocessorParserListener ) ((CPreprocessorParserListener)listener).enterOtherLine(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CPreprocessorParserListener ) ((CPreprocessorParserListener)listener).exitOtherLine(this);
		}
	}

	public final OtherLineContext otherLine() throws RecognitionException {
		OtherLineContext _localctx = new OtherLineContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_otherLine);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(101);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 491518L) != 0)) {
				{
				{
				setState(98);
				ppToken();
				}
				}
				setState(103);
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
	public static class DefineDirectiveContext extends ParserRuleContext {
		public DefineDirectiveContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_defineDirective; }

		public DefineDirectiveContext() { }
		public void copyFrom(DefineDirectiveContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class FunctionDefineContext extends DefineDirectiveContext {
		public TerminalNode DEFINE() { return getToken(CPreprocessorParser.DEFINE, 0); }
		public RequiredHorizontalContext requiredHorizontal() {
			return getRuleContext(RequiredHorizontalContext.class,0);
		}
		public MacroNameContext macroName() {
			return getRuleContext(MacroNameContext.class,0);
		}
		public TerminalNode LPAREN() { return getToken(CPreprocessorParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(CPreprocessorParser.RPAREN, 0); }
		public List<HorizontalContext> horizontal() {
			return getRuleContexts(HorizontalContext.class);
		}
		public HorizontalContext horizontal(int i) {
			return getRuleContext(HorizontalContext.class,i);
		}
		public ParameterListContext parameterList() {
			return getRuleContext(ParameterListContext.class,0);
		}
		public ReplacementListContext replacementList() {
			return getRuleContext(ReplacementListContext.class,0);
		}
		public FunctionDefineContext(DefineDirectiveContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CPreprocessorParserListener ) ((CPreprocessorParserListener)listener).enterFunctionDefine(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CPreprocessorParserListener ) ((CPreprocessorParserListener)listener).exitFunctionDefine(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ObjectDefineContext extends DefineDirectiveContext {
		public TerminalNode DEFINE() { return getToken(CPreprocessorParser.DEFINE, 0); }
		public RequiredHorizontalContext requiredHorizontal() {
			return getRuleContext(RequiredHorizontalContext.class,0);
		}
		public MacroNameContext macroName() {
			return getRuleContext(MacroNameContext.class,0);
		}
		public ObjectReplacementContext objectReplacement() {
			return getRuleContext(ObjectReplacementContext.class,0);
		}
		public ObjectDefineContext(DefineDirectiveContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CPreprocessorParserListener ) ((CPreprocessorParserListener)listener).enterObjectDefine(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CPreprocessorParserListener ) ((CPreprocessorParserListener)listener).exitObjectDefine(this);
		}
	}

	public final DefineDirectiveContext defineDirective() throws RecognitionException {
		DefineDirectiveContext _localctx = new DefineDirectiveContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_defineDirective);
		int _la;
		try {
			int _alt;
			setState(133);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,16,_ctx) ) {
			case 1:
				_localctx = new FunctionDefineContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(104);
				match(DEFINE);
				setState(105);
				requiredHorizontal();
				setState(106);
				macroName();
				setState(107);
				match(LPAREN);
				setState(111);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,11,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(108);
						horizontal();
						}
						}
					}
					setState(113);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,11,_ctx);
				}
				setState(115);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ELLIPSIS || _la==IDENTIFIER) {
					{
					setState(114);
					parameterList();
					}
				}

				setState(120);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 290816L) != 0)) {
					{
					{
					setState(117);
					horizontal();
					}
					}
					setState(122);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(123);
				match(RPAREN);
				setState(125);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 491518L) != 0)) {
					{
					setState(124);
					replacementList();
					}
				}

				}
				break;
			case 2:
				_localctx = new ObjectDefineContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(127);
				match(DEFINE);
				setState(128);
				requiredHorizontal();
				setState(129);
				macroName();
				setState(131);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 491486L) != 0)) {
					{
					setState(130);
					objectReplacement();
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
	public static class MacroNameContext extends ParserRuleContext {
		public TerminalNode IDENTIFIER() { return getToken(CPreprocessorParser.IDENTIFIER, 0); }
		public MacroNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_macroName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CPreprocessorParserListener ) ((CPreprocessorParserListener)listener).enterMacroName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CPreprocessorParserListener ) ((CPreprocessorParserListener)listener).exitMacroName(this);
		}
	}

	public final MacroNameContext macroName() throws RecognitionException {
		MacroNameContext _localctx = new MacroNameContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_macroName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(135);
			match(IDENTIFIER);
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
		public TerminalNode ELLIPSIS() { return getToken(CPreprocessorParser.ELLIPSIS, 0); }
		public List<MacroParameterContext> macroParameter() {
			return getRuleContexts(MacroParameterContext.class);
		}
		public MacroParameterContext macroParameter(int i) {
			return getRuleContext(MacroParameterContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(CPreprocessorParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(CPreprocessorParser.COMMA, i);
		}
		public List<HorizontalContext> horizontal() {
			return getRuleContexts(HorizontalContext.class);
		}
		public HorizontalContext horizontal(int i) {
			return getRuleContext(HorizontalContext.class,i);
		}
		public ParameterListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_parameterList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CPreprocessorParserListener ) ((CPreprocessorParserListener)listener).enterParameterList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CPreprocessorParserListener ) ((CPreprocessorParserListener)listener).exitParameterList(this);
		}
	}

	public final ParameterListContext parameterList() throws RecognitionException {
		ParameterListContext _localctx = new ParameterListContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_parameterList);
		int _la;
		try {
			int _alt;
			setState(183);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,24,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(137);
				match(ELLIPSIS);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(138);
				macroParameter();
				setState(155);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,19,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(142);
						_errHandler.sync(this);
						_la = _input.LA(1);
						while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 290816L) != 0)) {
							{
							{
							setState(139);
							horizontal();
							}
							}
							setState(144);
							_errHandler.sync(this);
							_la = _input.LA(1);
						}
						setState(145);
						match(COMMA);
						setState(149);
						_errHandler.sync(this);
						_la = _input.LA(1);
						while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 290816L) != 0)) {
							{
							{
							setState(146);
							horizontal();
							}
							}
							setState(151);
							_errHandler.sync(this);
							_la = _input.LA(1);
						}
						setState(152);
						macroParameter();
						}
						}
					}
					setState(157);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,19,_ctx);
				}
				setState(172);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,22,_ctx) ) {
				case 1:
					{
					setState(161);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 290816L) != 0)) {
						{
						{
						setState(158);
						horizontal();
						}
						}
						setState(163);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					setState(164);
					match(COMMA);
					setState(168);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 290816L) != 0)) {
						{
						{
						setState(165);
						horizontal();
						}
						}
						setState(170);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					setState(171);
					match(ELLIPSIS);
					}
					break;
				}
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(174);
				macroParameter();
				setState(178);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 290816L) != 0)) {
					{
					{
					setState(175);
					horizontal();
					}
					}
					setState(180);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(181);
				match(ELLIPSIS);
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
	public static class MacroParameterContext extends ParserRuleContext {
		public TerminalNode IDENTIFIER() { return getToken(CPreprocessorParser.IDENTIFIER, 0); }
		public MacroParameterContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_macroParameter; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CPreprocessorParserListener ) ((CPreprocessorParserListener)listener).enterMacroParameter(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CPreprocessorParserListener ) ((CPreprocessorParserListener)listener).exitMacroParameter(this);
		}
	}

	public final MacroParameterContext macroParameter() throws RecognitionException {
		MacroParameterContext _localctx = new MacroParameterContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_macroParameter);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(185);
			match(IDENTIFIER);
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
	public static class ObjectReplacementContext extends ParserRuleContext {
		public List<HorizontalContext> horizontal() {
			return getRuleContexts(HorizontalContext.class);
		}
		public HorizontalContext horizontal(int i) {
			return getRuleContext(HorizontalContext.class,i);
		}
		public ReplacementTokensContext replacementTokens() {
			return getRuleContext(ReplacementTokensContext.class,0);
		}
		public NonLparenReplacementTokenContext nonLparenReplacementToken() {
			return getRuleContext(NonLparenReplacementTokenContext.class,0);
		}
		public List<ReplacementTokenContext> replacementToken() {
			return getRuleContexts(ReplacementTokenContext.class);
		}
		public ReplacementTokenContext replacementToken(int i) {
			return getRuleContext(ReplacementTokenContext.class,i);
		}
		public ObjectReplacementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_objectReplacement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CPreprocessorParserListener ) ((CPreprocessorParserListener)listener).enterObjectReplacement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CPreprocessorParserListener ) ((CPreprocessorParserListener)listener).exitObjectReplacement(this);
		}
	}

	public final ObjectReplacementContext objectReplacement() throws RecognitionException {
		ObjectReplacementContext _localctx = new ObjectReplacementContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_objectReplacement);
		int _la;
		try {
			int _alt;
			setState(214);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case BLOCK_COMMENT_START:
			case LINE_COMMENT:
			case WS:
			case BLOCK_COMMENT_END:
				enterOuterAlt(_localctx, 1);
				{
				setState(188);
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(187);
					horizontal();
					}
					}
					setState(190);
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & 290816L) != 0) );
				setState(193);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 200702L) != 0)) {
					{
					setState(192);
					replacementTokens();
					}
				}

				}
				break;
			case HASHHASH:
			case HASH:
			case DEFINE:
			case ELLIPSIS:
			case RPAREN:
			case COMMA:
			case STRING_LITERAL:
			case CHARACTER_CONSTANT:
			case PP_NUMBER:
			case IDENTIFIER:
			case PUNCTUATOR:
			case OTHER:
				enterOuterAlt(_localctx, 2);
				{
				setState(195);
				nonLparenReplacementToken();
				setState(205);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,28,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(199);
						_errHandler.sync(this);
						_la = _input.LA(1);
						while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 290816L) != 0)) {
							{
							{
							setState(196);
							horizontal();
							}
							}
							setState(201);
							_errHandler.sync(this);
							_la = _input.LA(1);
						}
						setState(202);
						replacementToken();
						}
						}
					}
					setState(207);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,28,_ctx);
				}
				setState(211);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 290816L) != 0)) {
					{
					{
					setState(208);
					horizontal();
					}
					}
					setState(213);
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
	public static class ReplacementListContext extends ParserRuleContext {
		public List<ReplacementTokenContext> replacementToken() {
			return getRuleContexts(ReplacementTokenContext.class);
		}
		public ReplacementTokenContext replacementToken(int i) {
			return getRuleContext(ReplacementTokenContext.class,i);
		}
		public List<HorizontalContext> horizontal() {
			return getRuleContexts(HorizontalContext.class);
		}
		public HorizontalContext horizontal(int i) {
			return getRuleContext(HorizontalContext.class,i);
		}
		public ReplacementListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_replacementList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CPreprocessorParserListener ) ((CPreprocessorParserListener)listener).enterReplacementList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CPreprocessorParserListener ) ((CPreprocessorParserListener)listener).exitReplacementList(this);
		}
	}

	public final ReplacementListContext replacementList() throws RecognitionException {
		ReplacementListContext _localctx = new ReplacementListContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_replacementList);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(219);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 290816L) != 0)) {
				{
				{
				setState(216);
				horizontal();
				}
				}
				setState(221);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(222);
			replacementToken();
			setState(232);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,33,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(226);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 290816L) != 0)) {
						{
						{
						setState(223);
						horizontal();
						}
						}
						setState(228);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					setState(229);
					replacementToken();
					}
					}
				}
				setState(234);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,33,_ctx);
			}
			setState(238);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 290816L) != 0)) {
				{
				{
				setState(235);
				horizontal();
				}
				}
				setState(240);
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
	public static class ReplacementTokensContext extends ParserRuleContext {
		public List<ReplacementTokenContext> replacementToken() {
			return getRuleContexts(ReplacementTokenContext.class);
		}
		public ReplacementTokenContext replacementToken(int i) {
			return getRuleContext(ReplacementTokenContext.class,i);
		}
		public List<HorizontalContext> horizontal() {
			return getRuleContexts(HorizontalContext.class);
		}
		public HorizontalContext horizontal(int i) {
			return getRuleContext(HorizontalContext.class,i);
		}
		public ReplacementTokensContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_replacementTokens; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CPreprocessorParserListener ) ((CPreprocessorParserListener)listener).enterReplacementTokens(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CPreprocessorParserListener ) ((CPreprocessorParserListener)listener).exitReplacementTokens(this);
		}
	}

	public final ReplacementTokensContext replacementTokens() throws RecognitionException {
		ReplacementTokensContext _localctx = new ReplacementTokensContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_replacementTokens);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(241);
			replacementToken();
			setState(251);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,36,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(245);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 290816L) != 0)) {
						{
						{
						setState(242);
						horizontal();
						}
						}
						setState(247);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					setState(248);
					replacementToken();
					}
					}
				}
				setState(253);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,36,_ctx);
			}
			setState(257);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 290816L) != 0)) {
				{
				{
				setState(254);
				horizontal();
				}
				}
				setState(259);
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
	public static class NonLparenReplacementTokenContext extends ParserRuleContext {
		public TerminalNode HASHHASH() { return getToken(CPreprocessorParser.HASHHASH, 0); }
		public TerminalNode HASH() { return getToken(CPreprocessorParser.HASH, 0); }
		public TerminalNode DEFINE() { return getToken(CPreprocessorParser.DEFINE, 0); }
		public TerminalNode ELLIPSIS() { return getToken(CPreprocessorParser.ELLIPSIS, 0); }
		public TerminalNode RPAREN() { return getToken(CPreprocessorParser.RPAREN, 0); }
		public TerminalNode COMMA() { return getToken(CPreprocessorParser.COMMA, 0); }
		public TerminalNode STRING_LITERAL() { return getToken(CPreprocessorParser.STRING_LITERAL, 0); }
		public TerminalNode CHARACTER_CONSTANT() { return getToken(CPreprocessorParser.CHARACTER_CONSTANT, 0); }
		public TerminalNode PP_NUMBER() { return getToken(CPreprocessorParser.PP_NUMBER, 0); }
		public TerminalNode IDENTIFIER() { return getToken(CPreprocessorParser.IDENTIFIER, 0); }
		public TerminalNode PUNCTUATOR() { return getToken(CPreprocessorParser.PUNCTUATOR, 0); }
		public TerminalNode OTHER() { return getToken(CPreprocessorParser.OTHER, 0); }
		public NonLparenReplacementTokenContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_nonLparenReplacementToken; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CPreprocessorParserListener ) ((CPreprocessorParserListener)listener).enterNonLparenReplacementToken(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CPreprocessorParserListener ) ((CPreprocessorParserListener)listener).exitNonLparenReplacementToken(this);
		}
	}

	public final NonLparenReplacementTokenContext nonLparenReplacementToken() throws RecognitionException {
		NonLparenReplacementTokenContext _localctx = new NonLparenReplacementTokenContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_nonLparenReplacementToken);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(260);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 200670L) != 0)) ) {
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
	public static class ReplacementTokenContext extends ParserRuleContext {
		public NonLparenReplacementTokenContext nonLparenReplacementToken() {
			return getRuleContext(NonLparenReplacementTokenContext.class,0);
		}
		public TerminalNode LPAREN() { return getToken(CPreprocessorParser.LPAREN, 0); }
		public ReplacementTokenContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_replacementToken; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CPreprocessorParserListener ) ((CPreprocessorParserListener)listener).enterReplacementToken(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CPreprocessorParserListener ) ((CPreprocessorParserListener)listener).exitReplacementToken(this);
		}
	}

	public final ReplacementTokenContext replacementToken() throws RecognitionException {
		ReplacementTokenContext _localctx = new ReplacementTokenContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_replacementToken);
		try {
			setState(264);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case HASHHASH:
			case HASH:
			case DEFINE:
			case ELLIPSIS:
			case RPAREN:
			case COMMA:
			case STRING_LITERAL:
			case CHARACTER_CONSTANT:
			case PP_NUMBER:
			case IDENTIFIER:
			case PUNCTUATOR:
			case OTHER:
				enterOuterAlt(_localctx, 1);
				{
				setState(262);
				nonLparenReplacementToken();
				}
				break;
			case LPAREN:
				enterOuterAlt(_localctx, 2);
				{
				setState(263);
				match(LPAREN);
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
	public static class RequiredHorizontalContext extends ParserRuleContext {
		public List<HorizontalContext> horizontal() {
			return getRuleContexts(HorizontalContext.class);
		}
		public HorizontalContext horizontal(int i) {
			return getRuleContext(HorizontalContext.class,i);
		}
		public RequiredHorizontalContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_requiredHorizontal; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CPreprocessorParserListener ) ((CPreprocessorParserListener)listener).enterRequiredHorizontal(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CPreprocessorParserListener ) ((CPreprocessorParserListener)listener).exitRequiredHorizontal(this);
		}
	}

	public final RequiredHorizontalContext requiredHorizontal() throws RecognitionException {
		RequiredHorizontalContext _localctx = new RequiredHorizontalContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_requiredHorizontal);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(267);
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(266);
				horizontal();
				}
				}
				setState(269);
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & 290816L) != 0) );
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
	public static class HorizontalContext extends ParserRuleContext {
		public TerminalNode WS() { return getToken(CPreprocessorParser.WS, 0); }
		public TerminalNode LINE_COMMENT() { return getToken(CPreprocessorParser.LINE_COMMENT, 0); }
		public TerminalNode BLOCK_COMMENT_START() { return getToken(CPreprocessorParser.BLOCK_COMMENT_START, 0); }
		public TerminalNode BLOCK_COMMENT_END() { return getToken(CPreprocessorParser.BLOCK_COMMENT_END, 0); }
		public HorizontalContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_horizontal; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CPreprocessorParserListener ) ((CPreprocessorParserListener)listener).enterHorizontal(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CPreprocessorParserListener ) ((CPreprocessorParserListener)listener).exitHorizontal(this);
		}
	}

	public final HorizontalContext horizontal() throws RecognitionException {
		HorizontalContext _localctx = new HorizontalContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_horizontal);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(271);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 290816L) != 0)) ) {
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
	public static class PpTokenContext extends ParserRuleContext {
		public HorizontalContext horizontal() {
			return getRuleContext(HorizontalContext.class,0);
		}
		public ReplacementTokenContext replacementToken() {
			return getRuleContext(ReplacementTokenContext.class,0);
		}
		public PpTokenContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_ppToken; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CPreprocessorParserListener ) ((CPreprocessorParserListener)listener).enterPpToken(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CPreprocessorParserListener ) ((CPreprocessorParserListener)listener).exitPpToken(this);
		}
	}

	public final PpTokenContext ppToken() throws RecognitionException {
		PpTokenContext _localctx = new PpTokenContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_ppToken);
		try {
			setState(275);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case BLOCK_COMMENT_START:
			case LINE_COMMENT:
			case WS:
			case BLOCK_COMMENT_END:
				enterOuterAlt(_localctx, 1);
				{
				setState(273);
				horizontal();
				}
				break;
			case HASHHASH:
			case HASH:
			case DEFINE:
			case ELLIPSIS:
			case LPAREN:
			case RPAREN:
			case COMMA:
			case STRING_LITERAL:
			case CHARACTER_CONSTANT:
			case PP_NUMBER:
			case IDENTIFIER:
			case PUNCTUATOR:
			case OTHER:
				enterOuterAlt(_localctx, 2);
				{
				setState(274);
				replacementToken();
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

	public static final String _serializedATN =
		"\u0004\u0001\u0013\u0116\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001"+
		"\u0002\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004"+
		"\u0002\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007"+
		"\u0002\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b"+
		"\u0002\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002\u000f\u0007"+
		"\u000f\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011\u0001\u0000\u0001"+
		"\u0000\u0001\u0000\u0005\u0000(\b\u0000\n\u0000\f\u0000+\t\u0000\u0001"+
		"\u0000\u0003\u0000.\b\u0000\u0001\u0000\u0001\u0000\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0003\u00015\b\u0001\u0001\u0002\u0001\u0002\u0001"+
		"\u0002\u0004\u0002:\b\u0002\u000b\u0002\f\u0002;\u0003\u0002>\b\u0002"+
		"\u0001\u0003\u0005\u0003A\b\u0003\n\u0003\f\u0003D\t\u0003\u0001\u0003"+
		"\u0001\u0003\u0005\u0003H\b\u0003\n\u0003\f\u0003K\t\u0003\u0001\u0003"+
		"\u0001\u0003\u0001\u0004\u0005\u0004P\b\u0004\n\u0004\f\u0004S\t\u0004"+
		"\u0001\u0004\u0001\u0004\u0005\u0004W\b\u0004\n\u0004\f\u0004Z\t\u0004"+
		"\u0001\u0004\u0001\u0004\u0005\u0004^\b\u0004\n\u0004\f\u0004a\t\u0004"+
		"\u0001\u0005\u0005\u0005d\b\u0005\n\u0005\f\u0005g\t\u0005\u0001\u0006"+
		"\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0005\u0006n\b\u0006"+
		"\n\u0006\f\u0006q\t\u0006\u0001\u0006\u0003\u0006t\b\u0006\u0001\u0006"+
		"\u0005\u0006w\b\u0006\n\u0006\f\u0006z\t\u0006\u0001\u0006\u0001\u0006"+
		"\u0003\u0006~\b\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006"+
		"\u0003\u0006\u0084\b\u0006\u0003\u0006\u0086\b\u0006\u0001\u0007\u0001"+
		"\u0007\u0001\b\u0001\b\u0001\b\u0005\b\u008d\b\b\n\b\f\b\u0090\t\b\u0001"+
		"\b\u0001\b\u0005\b\u0094\b\b\n\b\f\b\u0097\t\b\u0001\b\u0005\b\u009a\b"+
		"\b\n\b\f\b\u009d\t\b\u0001\b\u0005\b\u00a0\b\b\n\b\f\b\u00a3\t\b\u0001"+
		"\b\u0001\b\u0005\b\u00a7\b\b\n\b\f\b\u00aa\t\b\u0001\b\u0003\b\u00ad\b"+
		"\b\u0001\b\u0001\b\u0005\b\u00b1\b\b\n\b\f\b\u00b4\t\b\u0001\b\u0001\b"+
		"\u0003\b\u00b8\b\b\u0001\t\u0001\t\u0001\n\u0004\n\u00bd\b\n\u000b\n\f"+
		"\n\u00be\u0001\n\u0003\n\u00c2\b\n\u0001\n\u0001\n\u0005\n\u00c6\b\n\n"+
		"\n\f\n\u00c9\t\n\u0001\n\u0005\n\u00cc\b\n\n\n\f\n\u00cf\t\n\u0001\n\u0005"+
		"\n\u00d2\b\n\n\n\f\n\u00d5\t\n\u0003\n\u00d7\b\n\u0001\u000b\u0005\u000b"+
		"\u00da\b\u000b\n\u000b\f\u000b\u00dd\t\u000b\u0001\u000b\u0001\u000b\u0005"+
		"\u000b\u00e1\b\u000b\n\u000b\f\u000b\u00e4\t\u000b\u0001\u000b\u0005\u000b"+
		"\u00e7\b\u000b\n\u000b\f\u000b\u00ea\t\u000b\u0001\u000b\u0005\u000b\u00ed"+
		"\b\u000b\n\u000b\f\u000b\u00f0\t\u000b\u0001\f\u0001\f\u0005\f\u00f4\b"+
		"\f\n\f\f\f\u00f7\t\f\u0001\f\u0005\f\u00fa\b\f\n\f\f\f\u00fd\t\f\u0001"+
		"\f\u0005\f\u0100\b\f\n\f\f\f\u0103\t\f\u0001\r\u0001\r\u0001\u000e\u0001"+
		"\u000e\u0003\u000e\u0109\b\u000e\u0001\u000f\u0004\u000f\u010c\b\u000f"+
		"\u000b\u000f\f\u000f\u010d\u0001\u0010\u0001\u0010\u0001\u0011\u0001\u0011"+
		"\u0003\u0011\u0114\b\u0011\u0001\u0011\u0000\u0000\u0012\u0000\u0002\u0004"+
		"\u0006\b\n\f\u000e\u0010\u0012\u0014\u0016\u0018\u001a\u001c\u001e \""+
		"\u0000\u0002\u0003\u0000\u0001\u0004\u0006\u000b\u0010\u0011\u0002\u0000"+
		"\f\u000e\u0012\u0012\u012f\u0000)\u0001\u0000\u0000\u0000\u00024\u0001"+
		"\u0000\u0000\u0000\u0004=\u0001\u0000\u0000\u0000\u0006B\u0001\u0000\u0000"+
		"\u0000\bQ\u0001\u0000\u0000\u0000\ne\u0001\u0000\u0000\u0000\f\u0085\u0001"+
		"\u0000\u0000\u0000\u000e\u0087\u0001\u0000\u0000\u0000\u0010\u00b7\u0001"+
		"\u0000\u0000\u0000\u0012\u00b9\u0001\u0000\u0000\u0000\u0014\u00d6\u0001"+
		"\u0000\u0000\u0000\u0016\u00db\u0001\u0000\u0000\u0000\u0018\u00f1\u0001"+
		"\u0000\u0000\u0000\u001a\u0104\u0001\u0000\u0000\u0000\u001c\u0108\u0001"+
		"\u0000\u0000\u0000\u001e\u010b\u0001\u0000\u0000\u0000 \u010f\u0001\u0000"+
		"\u0000\u0000\"\u0113\u0001\u0000\u0000\u0000$%\u0003\u0002\u0001\u0000"+
		"%&\u0005\u000f\u0000\u0000&(\u0001\u0000\u0000\u0000\'$\u0001\u0000\u0000"+
		"\u0000(+\u0001\u0000\u0000\u0000)\'\u0001\u0000\u0000\u0000)*\u0001\u0000"+
		"\u0000\u0000*-\u0001\u0000\u0000\u0000+)\u0001\u0000\u0000\u0000,.\u0003"+
		"\u0004\u0002\u0000-,\u0001\u0000\u0000\u0000-.\u0001\u0000\u0000\u0000"+
		"./\u0001\u0000\u0000\u0000/0\u0005\u0000\u0000\u00010\u0001\u0001\u0000"+
		"\u0000\u000015\u0003\u0006\u0003\u000025\u0003\b\u0004\u000035\u0003\n"+
		"\u0005\u000041\u0001\u0000\u0000\u000042\u0001\u0000\u0000\u000043\u0001"+
		"\u0000\u0000\u00005\u0003\u0001\u0000\u0000\u00006>\u0003\u0006\u0003"+
		"\u00007>\u0003\b\u0004\u00008:\u0003\"\u0011\u000098\u0001\u0000\u0000"+
		"\u0000:;\u0001\u0000\u0000\u0000;9\u0001\u0000\u0000\u0000;<\u0001\u0000"+
		"\u0000\u0000<>\u0001\u0000\u0000\u0000=6\u0001\u0000\u0000\u0000=7\u0001"+
		"\u0000\u0000\u0000=9\u0001\u0000\u0000\u0000>\u0005\u0001\u0000\u0000"+
		"\u0000?A\u0003 \u0010\u0000@?\u0001\u0000\u0000\u0000AD\u0001\u0000\u0000"+
		"\u0000B@\u0001\u0000\u0000\u0000BC\u0001\u0000\u0000\u0000CE\u0001\u0000"+
		"\u0000\u0000DB\u0001\u0000\u0000\u0000EI\u0005\u0002\u0000\u0000FH\u0003"+
		" \u0010\u0000GF\u0001\u0000\u0000\u0000HK\u0001\u0000\u0000\u0000IG\u0001"+
		"\u0000\u0000\u0000IJ\u0001\u0000\u0000\u0000JL\u0001\u0000\u0000\u0000"+
		"KI\u0001\u0000\u0000\u0000LM\u0003\f\u0006\u0000M\u0007\u0001\u0000\u0000"+
		"\u0000NP\u0003 \u0010\u0000ON\u0001\u0000\u0000\u0000PS\u0001\u0000\u0000"+
		"\u0000QO\u0001\u0000\u0000\u0000QR\u0001\u0000\u0000\u0000RT\u0001\u0000"+
		"\u0000\u0000SQ\u0001\u0000\u0000\u0000TX\u0005\u0002\u0000\u0000UW\u0003"+
		" \u0010\u0000VU\u0001\u0000\u0000\u0000WZ\u0001\u0000\u0000\u0000XV\u0001"+
		"\u0000\u0000\u0000XY\u0001\u0000\u0000\u0000Y[\u0001\u0000\u0000\u0000"+
		"ZX\u0001\u0000\u0000\u0000[_\u0005\u0003\u0000\u0000\\^\u0003\"\u0011"+
		"\u0000]\\\u0001\u0000\u0000\u0000^a\u0001\u0000\u0000\u0000_]\u0001\u0000"+
		"\u0000\u0000_`\u0001\u0000\u0000\u0000`\t\u0001\u0000\u0000\u0000a_\u0001"+
		"\u0000\u0000\u0000bd\u0003\"\u0011\u0000cb\u0001\u0000\u0000\u0000dg\u0001"+
		"\u0000\u0000\u0000ec\u0001\u0000\u0000\u0000ef\u0001\u0000\u0000\u0000"+
		"f\u000b\u0001\u0000\u0000\u0000ge\u0001\u0000\u0000\u0000hi\u0005\u0003"+
		"\u0000\u0000ij\u0003\u001e\u000f\u0000jk\u0003\u000e\u0007\u0000ko\u0005"+
		"\u0005\u0000\u0000ln\u0003 \u0010\u0000ml\u0001\u0000\u0000\u0000nq\u0001"+
		"\u0000\u0000\u0000om\u0001\u0000\u0000\u0000op\u0001\u0000\u0000\u0000"+
		"ps\u0001\u0000\u0000\u0000qo\u0001\u0000\u0000\u0000rt\u0003\u0010\b\u0000"+
		"sr\u0001\u0000\u0000\u0000st\u0001\u0000\u0000\u0000tx\u0001\u0000\u0000"+
		"\u0000uw\u0003 \u0010\u0000vu\u0001\u0000\u0000\u0000wz\u0001\u0000\u0000"+
		"\u0000xv\u0001\u0000\u0000\u0000xy\u0001\u0000\u0000\u0000y{\u0001\u0000"+
		"\u0000\u0000zx\u0001\u0000\u0000\u0000{}\u0005\u0006\u0000\u0000|~\u0003"+
		"\u0016\u000b\u0000}|\u0001\u0000\u0000\u0000}~\u0001\u0000\u0000\u0000"+
		"~\u0086\u0001\u0000\u0000\u0000\u007f\u0080\u0005\u0003\u0000\u0000\u0080"+
		"\u0081\u0003\u001e\u000f\u0000\u0081\u0083\u0003\u000e\u0007\u0000\u0082"+
		"\u0084\u0003\u0014\n\u0000\u0083\u0082\u0001\u0000\u0000\u0000\u0083\u0084"+
		"\u0001\u0000\u0000\u0000\u0084\u0086\u0001\u0000\u0000\u0000\u0085h\u0001"+
		"\u0000\u0000\u0000\u0085\u007f\u0001\u0000\u0000\u0000\u0086\r\u0001\u0000"+
		"\u0000\u0000\u0087\u0088\u0005\u000b\u0000\u0000\u0088\u000f\u0001\u0000"+
		"\u0000\u0000\u0089\u00b8\u0005\u0004\u0000\u0000\u008a\u009b\u0003\u0012"+
		"\t\u0000\u008b\u008d\u0003 \u0010\u0000\u008c\u008b\u0001\u0000\u0000"+
		"\u0000\u008d\u0090\u0001\u0000\u0000\u0000\u008e\u008c\u0001\u0000\u0000"+
		"\u0000\u008e\u008f\u0001\u0000\u0000\u0000\u008f\u0091\u0001\u0000\u0000"+
		"\u0000\u0090\u008e\u0001\u0000\u0000\u0000\u0091\u0095\u0005\u0007\u0000"+
		"\u0000\u0092\u0094\u0003 \u0010\u0000\u0093\u0092\u0001\u0000\u0000\u0000"+
		"\u0094\u0097\u0001\u0000\u0000\u0000\u0095\u0093\u0001\u0000\u0000\u0000"+
		"\u0095\u0096\u0001\u0000\u0000\u0000\u0096\u0098\u0001\u0000\u0000\u0000"+
		"\u0097\u0095\u0001\u0000\u0000\u0000\u0098\u009a\u0003\u0012\t\u0000\u0099"+
		"\u008e\u0001\u0000\u0000\u0000\u009a\u009d\u0001\u0000\u0000\u0000\u009b"+
		"\u0099\u0001\u0000\u0000\u0000\u009b\u009c\u0001\u0000\u0000\u0000\u009c"+
		"\u00ac\u0001\u0000\u0000\u0000\u009d\u009b\u0001\u0000\u0000\u0000\u009e"+
		"\u00a0\u0003 \u0010\u0000\u009f\u009e\u0001\u0000\u0000\u0000\u00a0\u00a3"+
		"\u0001\u0000\u0000\u0000\u00a1\u009f\u0001\u0000\u0000\u0000\u00a1\u00a2"+
		"\u0001\u0000\u0000\u0000\u00a2\u00a4\u0001\u0000\u0000\u0000\u00a3\u00a1"+
		"\u0001\u0000\u0000\u0000\u00a4\u00a8\u0005\u0007\u0000\u0000\u00a5\u00a7"+
		"\u0003 \u0010\u0000\u00a6\u00a5\u0001\u0000\u0000\u0000\u00a7\u00aa\u0001"+
		"\u0000\u0000\u0000\u00a8\u00a6\u0001\u0000\u0000\u0000\u00a8\u00a9\u0001"+
		"\u0000\u0000\u0000\u00a9\u00ab\u0001\u0000\u0000\u0000\u00aa\u00a8\u0001"+
		"\u0000\u0000\u0000\u00ab\u00ad\u0005\u0004\u0000\u0000\u00ac\u00a1\u0001"+
		"\u0000\u0000\u0000\u00ac\u00ad\u0001\u0000\u0000\u0000\u00ad\u00b8\u0001"+
		"\u0000\u0000\u0000\u00ae\u00b2\u0003\u0012\t\u0000\u00af\u00b1\u0003 "+
		"\u0010\u0000\u00b0\u00af\u0001\u0000\u0000\u0000\u00b1\u00b4\u0001\u0000"+
		"\u0000\u0000\u00b2\u00b0\u0001\u0000\u0000\u0000\u00b2\u00b3\u0001\u0000"+
		"\u0000\u0000\u00b3\u00b5\u0001\u0000\u0000\u0000\u00b4\u00b2\u0001\u0000"+
		"\u0000\u0000\u00b5\u00b6\u0005\u0004\u0000\u0000\u00b6\u00b8\u0001\u0000"+
		"\u0000\u0000\u00b7\u0089\u0001\u0000\u0000\u0000\u00b7\u008a\u0001\u0000"+
		"\u0000\u0000\u00b7\u00ae\u0001\u0000\u0000\u0000\u00b8\u0011\u0001\u0000"+
		"\u0000\u0000\u00b9\u00ba\u0005\u000b\u0000\u0000\u00ba\u0013\u0001\u0000"+
		"\u0000\u0000\u00bb\u00bd\u0003 \u0010\u0000\u00bc\u00bb\u0001\u0000\u0000"+
		"\u0000\u00bd\u00be\u0001\u0000\u0000\u0000\u00be\u00bc\u0001\u0000\u0000"+
		"\u0000\u00be\u00bf\u0001\u0000\u0000\u0000\u00bf\u00c1\u0001\u0000\u0000"+
		"\u0000\u00c0\u00c2\u0003\u0018\f\u0000\u00c1\u00c0\u0001\u0000\u0000\u0000"+
		"\u00c1\u00c2\u0001\u0000\u0000\u0000\u00c2\u00d7\u0001\u0000\u0000\u0000"+
		"\u00c3\u00cd\u0003\u001a\r\u0000\u00c4\u00c6\u0003 \u0010\u0000\u00c5"+
		"\u00c4\u0001\u0000\u0000\u0000\u00c6\u00c9\u0001\u0000\u0000\u0000\u00c7"+
		"\u00c5\u0001\u0000\u0000\u0000\u00c7\u00c8\u0001\u0000\u0000\u0000\u00c8"+
		"\u00ca\u0001\u0000\u0000\u0000\u00c9\u00c7\u0001\u0000\u0000\u0000\u00ca"+
		"\u00cc\u0003\u001c\u000e\u0000\u00cb\u00c7\u0001\u0000\u0000\u0000\u00cc"+
		"\u00cf\u0001\u0000\u0000\u0000\u00cd\u00cb\u0001\u0000\u0000\u0000\u00cd"+
		"\u00ce\u0001\u0000\u0000\u0000\u00ce\u00d3\u0001\u0000\u0000\u0000\u00cf"+
		"\u00cd\u0001\u0000\u0000\u0000\u00d0\u00d2\u0003 \u0010\u0000\u00d1\u00d0"+
		"\u0001\u0000\u0000\u0000\u00d2\u00d5\u0001\u0000\u0000\u0000\u00d3\u00d1"+
		"\u0001\u0000\u0000\u0000\u00d3\u00d4\u0001\u0000\u0000\u0000\u00d4\u00d7"+
		"\u0001\u0000\u0000\u0000\u00d5\u00d3\u0001\u0000\u0000\u0000\u00d6\u00bc"+
		"\u0001\u0000\u0000\u0000\u00d6\u00c3\u0001\u0000\u0000\u0000\u00d7\u0015"+
		"\u0001\u0000\u0000\u0000\u00d8\u00da\u0003 \u0010\u0000\u00d9\u00d8\u0001"+
		"\u0000\u0000\u0000\u00da\u00dd\u0001\u0000\u0000\u0000\u00db\u00d9\u0001"+
		"\u0000\u0000\u0000\u00db\u00dc\u0001\u0000\u0000\u0000\u00dc\u00de\u0001"+
		"\u0000\u0000\u0000\u00dd\u00db\u0001\u0000\u0000\u0000\u00de\u00e8\u0003"+
		"\u001c\u000e\u0000\u00df\u00e1\u0003 \u0010\u0000\u00e0\u00df\u0001\u0000"+
		"\u0000\u0000\u00e1\u00e4\u0001\u0000\u0000\u0000\u00e2\u00e0\u0001\u0000"+
		"\u0000\u0000\u00e2\u00e3\u0001\u0000\u0000\u0000\u00e3\u00e5\u0001\u0000"+
		"\u0000\u0000\u00e4\u00e2\u0001\u0000\u0000\u0000\u00e5\u00e7\u0003\u001c"+
		"\u000e\u0000\u00e6\u00e2\u0001\u0000\u0000\u0000\u00e7\u00ea\u0001\u0000"+
		"\u0000\u0000\u00e8\u00e6\u0001\u0000\u0000\u0000\u00e8\u00e9\u0001\u0000"+
		"\u0000\u0000\u00e9\u00ee\u0001\u0000\u0000\u0000\u00ea\u00e8\u0001\u0000"+
		"\u0000\u0000\u00eb\u00ed\u0003 \u0010\u0000\u00ec\u00eb\u0001\u0000\u0000"+
		"\u0000\u00ed\u00f0\u0001\u0000\u0000\u0000\u00ee\u00ec\u0001\u0000\u0000"+
		"\u0000\u00ee\u00ef\u0001\u0000\u0000\u0000\u00ef\u0017\u0001\u0000\u0000"+
		"\u0000\u00f0\u00ee\u0001\u0000\u0000\u0000\u00f1\u00fb\u0003\u001c\u000e"+
		"\u0000\u00f2\u00f4\u0003 \u0010\u0000\u00f3\u00f2\u0001\u0000\u0000\u0000"+
		"\u00f4\u00f7\u0001\u0000\u0000\u0000\u00f5\u00f3\u0001\u0000\u0000\u0000"+
		"\u00f5\u00f6\u0001\u0000\u0000\u0000\u00f6\u00f8\u0001\u0000\u0000\u0000"+
		"\u00f7\u00f5\u0001\u0000\u0000\u0000\u00f8\u00fa\u0003\u001c\u000e\u0000"+
		"\u00f9\u00f5\u0001\u0000\u0000\u0000\u00fa\u00fd\u0001\u0000\u0000\u0000"+
		"\u00fb\u00f9\u0001\u0000\u0000\u0000\u00fb\u00fc\u0001\u0000\u0000\u0000"+
		"\u00fc\u0101\u0001\u0000\u0000\u0000\u00fd\u00fb\u0001\u0000\u0000\u0000"+
		"\u00fe\u0100\u0003 \u0010\u0000\u00ff\u00fe\u0001\u0000\u0000\u0000\u0100"+
		"\u0103\u0001\u0000\u0000\u0000\u0101\u00ff\u0001\u0000\u0000\u0000\u0101"+
		"\u0102\u0001\u0000\u0000\u0000\u0102\u0019\u0001\u0000\u0000\u0000\u0103"+
		"\u0101\u0001\u0000\u0000\u0000\u0104\u0105\u0007\u0000\u0000\u0000\u0105"+
		"\u001b\u0001\u0000\u0000\u0000\u0106\u0109\u0003\u001a\r\u0000\u0107\u0109"+
		"\u0005\u0005\u0000\u0000\u0108\u0106\u0001\u0000\u0000\u0000\u0108\u0107"+
		"\u0001\u0000\u0000\u0000\u0109\u001d\u0001\u0000\u0000\u0000\u010a\u010c"+
		"\u0003 \u0010\u0000\u010b\u010a\u0001\u0000\u0000\u0000\u010c\u010d\u0001"+
		"\u0000\u0000\u0000\u010d\u010b\u0001\u0000\u0000\u0000\u010d\u010e\u0001"+
		"\u0000\u0000\u0000\u010e\u001f\u0001\u0000\u0000\u0000\u010f\u0110\u0007"+
		"\u0001\u0000\u0000\u0110!\u0001\u0000\u0000\u0000\u0111\u0114\u0003 \u0010"+
		"\u0000\u0112\u0114\u0003\u001c\u000e\u0000\u0113\u0111\u0001\u0000\u0000"+
		"\u0000\u0113\u0112\u0001\u0000\u0000\u0000\u0114#\u0001\u0000\u0000\u0000"+
		"))-4;=BIQX_eosx}\u0083\u0085\u008e\u0095\u009b\u00a1\u00a8\u00ac\u00b2"+
		"\u00b7\u00be\u00c1\u00c7\u00cd\u00d3\u00d6\u00db\u00e2\u00e8\u00ee\u00f5"+
		"\u00fb\u0101\u0108\u010d\u0113";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}