// Generated from antlr-grammars/CParser.g4 by ANTLR 4.13.2
package legacymodernizer.parser.antlr.c;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue", "this-escape"})
public class CParser extends CParserBase {
	static { RuntimeMetaData.checkVersion("4.13.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		Attribute=1, KW__builtin_offsetof=2, KW__builtin_va_arg=3, KW__builtin_choose_expr=4, 
		KW__builtin_types_compatible_p=5, KW__builtin_tgmath=6, KW__builtin_complex=7, 
		KW__cdecl=8, KW__clrcall=9, KW__declspec=10, KW__extension__=11, KW__fastcall=12, 
		KW__m128=13, KW__m128d=14, KW__m128i=15, KW__stdcall=16, KW__thiscall=17, 
		KW__vectorcall=18, KW__real__=19, KW__imag__=20, KW__func__=21, KW__FUNCTION__=22, 
		KW__PRETTY_FUNCTION__=23, Alignas=24, Alignof=25, Maxof=26, Minof=27, 
		Countof=28, Asm=29, Auto=30, Bool=31, Break=32, Case=33, Char=34, Const=35, 
		Constexpr=36, Continue=37, Default=38, Deprecated=39, Do=40, Double=41, 
		Else=42, Enum=43, Extern=44, False_=45, Float=46, For=47, Goto=48, If=49, 
		Inline=50, Int=51, Label=52, Long=53, Nulptr=54, Register=55, Restrict=56, 
		Return=57, Short=58, Signed=59, Sizeof=60, Static=61, Static_assert=62, 
		Struct=63, Switch=64, True_=65, Typedef=66, Typeof=67, Typeof_unqual=68, 
		Union=69, Unsigned=70, Void=71, Volatile=72, While=73, Atomic=74, BitInt=75, 
		Complex=76, Decimal128=77, Decimal32=78, Decimal64=79, Generic=80, Imaginary=81, 
		Noreturn=82, StaticAssert=83, ThreadLocal=84, LeftParen=85, RightParen=86, 
		LeftBracket=87, RightBracket=88, LeftBrace=89, RightBrace=90, Less=91, 
		LessEqual=92, Greater=93, GreaterEqual=94, LeftShift=95, RightShift=96, 
		Plus=97, PlusPlus=98, Minus=99, MinusMinus=100, Star=101, Div=102, Mod=103, 
		And=104, Or=105, AndAnd=106, OrOr=107, Caret=108, Not=109, Tilde=110, 
		Question=111, Colon=112, Semi=113, Comma=114, Assign=115, StarAssign=116, 
		DivAssign=117, ModAssign=118, PlusAssign=119, MinusAssign=120, LeftShiftAssign=121, 
		RightShiftAssign=122, AndAssign=123, XorAssign=124, OrAssign=125, Equal=126, 
		NotEqual=127, Arrow=128, Dot=129, Ellipsis=130, Identifier=131, IntegerConstant=132, 
		FloatingConstant=133, DigitSequence=134, CharacterConstant=135, StringLiteral=136, 
		MultiLineMacro=137, LineDirective=138, Directive=139, Whitespace=140, 
		Newline=141, BlockComment=142, LineComment=143;
	public static final int
		RULE_compilationUnit = 0, RULE_constant = 1, RULE_enumerationConstant = 2, 
		RULE_predefinedConstant = 3, RULE_primaryExpression = 4, RULE_exprList = 5, 
		RULE_genericSelection = 6, RULE_genericAssocList = 7, RULE_genericAssociation = 8, 
		RULE_postfixExpression = 9, RULE_argumentExpressionList = 10, RULE_unaryExpression = 11, 
		RULE_castExpression = 12, RULE_multiplicativeExpression = 13, RULE_additiveExpression = 14, 
		RULE_shiftExpression = 15, RULE_relationalExpression = 16, RULE_equalityExpression = 17, 
		RULE_andExpression = 18, RULE_exclusiveOrExpression = 19, RULE_inclusiveOrExpression = 20, 
		RULE_logicalAndExpression = 21, RULE_logicalOrExpression = 22, RULE_conditionalExpression = 23, 
		RULE_assignmentExpression = 24, RULE_expression = 25, RULE_constantExpression = 26, 
		RULE_declaration = 27, RULE_declarationSpecifiers = 28, RULE_declarationSpecifier = 29, 
		RULE_initDeclaratorList = 30, RULE_initDeclarator = 31, RULE_attributeDeclaration = 32, 
		RULE_storageClassSpecifier = 33, RULE_typeSpecifier = 34, RULE_structOrUnionSpecifier = 35, 
		RULE_structOrUnion = 36, RULE_memberDeclarationList = 37, RULE_memberDeclaration = 38, 
		RULE_specifierQualifierList = 39, RULE_typeSpecifierQualifier = 40, RULE_memberDeclaratorList = 41, 
		RULE_memberDeclarator = 42, RULE_enumSpecifier = 43, RULE_enumeratorList = 44, 
		RULE_enumerator = 45, RULE_enumTypeSpecifier = 46, RULE_atomicTypeSpecifier = 47, 
		RULE_typeofSpecifier = 48, RULE_typeofSpecifierArgument = 49, RULE_typeQualifier = 50, 
		RULE_functionSpecifier = 51, RULE_alignmentSpecifier = 52, RULE_declarator = 53, 
		RULE_directDeclarator = 54, RULE_pointer = 55, RULE_typeQualifierList = 56, 
		RULE_parameterTypeList = 57, RULE_parameterList = 58, RULE_parameterDeclaration = 59, 
		RULE_typeName = 60, RULE_abstractDeclarator = 61, RULE_directAbstractDeclarator = 62, 
		RULE_typedefName = 63, RULE_initializer = 64, RULE_initializerList = 65, 
		RULE_designation = 66, RULE_designatorList = 67, RULE_designator = 68, 
		RULE_staticAssertDeclaration = 69, RULE_attributeSpecifierSequence = 70, 
		RULE_attributeSpecifier = 71, RULE_attributeList = 72, RULE_attribute = 73, 
		RULE_attributeToken = 74, RULE_attributeArgumentClause = 75, RULE_balancedTokenSequence = 76, 
		RULE_balancedToken = 77, RULE_statement = 78, RULE_labeledStatement = 79, 
		RULE_compoundStatement = 80, RULE_blockItemList = 81, RULE_blockItem = 82, 
		RULE_expressionStatement = 83, RULE_selectionStatement = 84, RULE_iterationStatement = 85, 
		RULE_forCondition = 86, RULE_forDeclaration = 87, RULE_forExpression = 88, 
		RULE_jumpStatement = 89, RULE_translationUnit = 90, RULE_externalDeclaration = 91, 
		RULE_functionDefinition = 92, RULE_declarationList = 93, RULE_functionBody = 94, 
		RULE_identifierList = 95, RULE_gnuArrayDesignator = 96, RULE_gnuIdentifier = 97, 
		RULE_asmArgument = 98, RULE_asmClobbers = 99, RULE_asmDefinition = 100, 
		RULE_toplevelAsmArgument = 101, RULE_asmOperand = 102, RULE_asmOperands = 103, 
		RULE_asmQualifier = 104, RULE_asmQualifierList = 105, RULE_asmStatement = 106, 
		RULE_asmStringLiteral = 107, RULE_gccDeclaratorExtension = 108, RULE_gnuAttribute = 109, 
		RULE_gnuAttributeList = 110, RULE_gnuAttributes = 111, RULE_gnuSingleAttribute = 112, 
		RULE_simpleAsmExpr = 113, RULE_vcSpecificModifer = 114;
	private static String[] makeRuleNames() {
		return new String[] {
			"compilationUnit", "constant", "enumerationConstant", "predefinedConstant", 
			"primaryExpression", "exprList", "genericSelection", "genericAssocList", 
			"genericAssociation", "postfixExpression", "argumentExpressionList", 
			"unaryExpression", "castExpression", "multiplicativeExpression", "additiveExpression", 
			"shiftExpression", "relationalExpression", "equalityExpression", "andExpression", 
			"exclusiveOrExpression", "inclusiveOrExpression", "logicalAndExpression", 
			"logicalOrExpression", "conditionalExpression", "assignmentExpression", 
			"expression", "constantExpression", "declaration", "declarationSpecifiers", 
			"declarationSpecifier", "initDeclaratorList", "initDeclarator", "attributeDeclaration", 
			"storageClassSpecifier", "typeSpecifier", "structOrUnionSpecifier", "structOrUnion", 
			"memberDeclarationList", "memberDeclaration", "specifierQualifierList", 
			"typeSpecifierQualifier", "memberDeclaratorList", "memberDeclarator", 
			"enumSpecifier", "enumeratorList", "enumerator", "enumTypeSpecifier", 
			"atomicTypeSpecifier", "typeofSpecifier", "typeofSpecifierArgument", 
			"typeQualifier", "functionSpecifier", "alignmentSpecifier", "declarator", 
			"directDeclarator", "pointer", "typeQualifierList", "parameterTypeList", 
			"parameterList", "parameterDeclaration", "typeName", "abstractDeclarator", 
			"directAbstractDeclarator", "typedefName", "initializer", "initializerList", 
			"designation", "designatorList", "designator", "staticAssertDeclaration", 
			"attributeSpecifierSequence", "attributeSpecifier", "attributeList", 
			"attribute", "attributeToken", "attributeArgumentClause", "balancedTokenSequence", 
			"balancedToken", "statement", "labeledStatement", "compoundStatement", 
			"blockItemList", "blockItem", "expressionStatement", "selectionStatement", 
			"iterationStatement", "forCondition", "forDeclaration", "forExpression", 
			"jumpStatement", "translationUnit", "externalDeclaration", "functionDefinition", 
			"declarationList", "functionBody", "identifierList", "gnuArrayDesignator", 
			"gnuIdentifier", "asmArgument", "asmClobbers", "asmDefinition", "toplevelAsmArgument", 
			"asmOperand", "asmOperands", "asmQualifier", "asmQualifierList", "asmStatement", 
			"asmStringLiteral", "gccDeclaratorExtension", "gnuAttribute", "gnuAttributeList", 
			"gnuAttributes", "gnuSingleAttribute", "simpleAsmExpr", "vcSpecificModifer"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, null, "'__builtin_offsetof'", "'__builtin_va_arg'", "'__builtin_choose_expr'", 
			"'__builtin_types_compatible_p'", "'__builtin_tgmath'", "'__builtin_complex'", 
			"'__cdecl'", "'__clrcall'", "'__declspec'", "'__extension__'", "'__fastcall'", 
			"'__m128'", "'__m128d'", "'__m128i'", "'__stdcall'", "'__thiscall'", 
			"'__vectorcall'", "'__real__'", "'__imag__'", "'__func__'", "'__FUNCTION__'", 
			"'__PRETTY_FUNCTION__'", null, null, "'_Maxof'", "'_Minof'", "'_Countof'", 
			null, "'auto'", null, "'break'", "'case'", "'char'", "'const'", "'constexpr'", 
			"'continue'", "'default'", "'deprecated'", "'do'", "'double'", "'else'", 
			"'enum'", "'extern'", "'false'", "'float'", "'for'", "'goto'", "'if'", 
			null, "'int'", "'__label__'", "'long'", "'nullptr'", "'register'", null, 
			"'return'", "'short'", "'signed'", "'sizeof'", "'static'", "'static_assert'", 
			"'struct'", "'switch'", "'true'", "'typedef'", null, null, "'union'", 
			"'unsigned'", "'void'", null, "'while'", "'_Atomic'", "'_BitInt'", "'_Complex'", 
			"'_Decimal128'", "'_Decimal32'", "'_Decimal64'", "'_Generic'", "'_Imaginary'", 
			"'_Noreturn'", "'_Static_assert'", null, "'('", "')'", "'['", "']'", 
			"'{'", "'}'", "'<'", "'<='", "'>'", "'>='", "'<<'", "'>>'", "'+'", "'++'", 
			"'-'", "'--'", "'*'", "'/'", "'%'", "'&'", "'|'", "'&&'", "'||'", "'^'", 
			"'!'", "'~'", "'?'", "':'", "';'", "','", "'='", "'*='", "'/='", "'%='", 
			"'+='", "'-='", "'<<='", "'>>='", "'&='", "'^='", "'|='", "'=='", "'!='", 
			"'->'", "'.'", "'...'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "Attribute", "KW__builtin_offsetof", "KW__builtin_va_arg", "KW__builtin_choose_expr", 
			"KW__builtin_types_compatible_p", "KW__builtin_tgmath", "KW__builtin_complex", 
			"KW__cdecl", "KW__clrcall", "KW__declspec", "KW__extension__", "KW__fastcall", 
			"KW__m128", "KW__m128d", "KW__m128i", "KW__stdcall", "KW__thiscall", 
			"KW__vectorcall", "KW__real__", "KW__imag__", "KW__func__", "KW__FUNCTION__", 
			"KW__PRETTY_FUNCTION__", "Alignas", "Alignof", "Maxof", "Minof", "Countof", 
			"Asm", "Auto", "Bool", "Break", "Case", "Char", "Const", "Constexpr", 
			"Continue", "Default", "Deprecated", "Do", "Double", "Else", "Enum", 
			"Extern", "False_", "Float", "For", "Goto", "If", "Inline", "Int", "Label", 
			"Long", "Nulptr", "Register", "Restrict", "Return", "Short", "Signed", 
			"Sizeof", "Static", "Static_assert", "Struct", "Switch", "True_", "Typedef", 
			"Typeof", "Typeof_unqual", "Union", "Unsigned", "Void", "Volatile", "While", 
			"Atomic", "BitInt", "Complex", "Decimal128", "Decimal32", "Decimal64", 
			"Generic", "Imaginary", "Noreturn", "StaticAssert", "ThreadLocal", "LeftParen", 
			"RightParen", "LeftBracket", "RightBracket", "LeftBrace", "RightBrace", 
			"Less", "LessEqual", "Greater", "GreaterEqual", "LeftShift", "RightShift", 
			"Plus", "PlusPlus", "Minus", "MinusMinus", "Star", "Div", "Mod", "And", 
			"Or", "AndAnd", "OrOr", "Caret", "Not", "Tilde", "Question", "Colon", 
			"Semi", "Comma", "Assign", "StarAssign", "DivAssign", "ModAssign", "PlusAssign", 
			"MinusAssign", "LeftShiftAssign", "RightShiftAssign", "AndAssign", "XorAssign", 
			"OrAssign", "Equal", "NotEqual", "Arrow", "Dot", "Ellipsis", "Identifier", 
			"IntegerConstant", "FloatingConstant", "DigitSequence", "CharacterConstant", 
			"StringLiteral", "MultiLineMacro", "LineDirective", "Directive", "Whitespace", 
			"Newline", "BlockComment", "LineComment"
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
	public String getGrammarFileName() { return "CParser.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public CParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@SuppressWarnings("CheckReturnValue")
	public static class CompilationUnitContext extends ParserRuleContext {
		public TerminalNode EOF() { return getToken(CParser.EOF, 0); }
		public TranslationUnitContext translationUnit() {
			return getRuleContext(TranslationUnitContext.class,0);
		}
		public CompilationUnitContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_compilationUnit; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterCompilationUnit(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitCompilationUnit(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitCompilationUnit(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CompilationUnitContext compilationUnit() throws RecognitionException {
		CompilationUnitContext _localctx = new CompilationUnitContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_compilationUnit);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(231);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,0,_ctx) ) {
			case 1:
				{
				setState(230);
				translationUnit();
				}
				break;
			}
			this.OutputSymbolTable();
			setState(234);
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
	public static class ConstantContext extends ParserRuleContext {
		public TerminalNode IntegerConstant() { return getToken(CParser.IntegerConstant, 0); }
		public TerminalNode FloatingConstant() { return getToken(CParser.FloatingConstant, 0); }
		public TerminalNode CharacterConstant() { return getToken(CParser.CharacterConstant, 0); }
		public PredefinedConstantContext predefinedConstant() {
			return getRuleContext(PredefinedConstantContext.class,0);
		}
		public ConstantContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_constant; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterConstant(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitConstant(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitConstant(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ConstantContext constant() throws RecognitionException {
		ConstantContext _localctx = new ConstantContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_constant);
		try {
			setState(240);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case IntegerConstant:
				enterOuterAlt(_localctx, 1);
				{
				setState(236);
				match(IntegerConstant);
				}
				break;
			case FloatingConstant:
				enterOuterAlt(_localctx, 2);
				{
				setState(237);
				match(FloatingConstant);
				}
				break;
			case CharacterConstant:
				enterOuterAlt(_localctx, 3);
				{
				setState(238);
				match(CharacterConstant);
				}
				break;
			case False_:
			case Nulptr:
			case True_:
				enterOuterAlt(_localctx, 4);
				{
				setState(239);
				predefinedConstant();
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
	public static class EnumerationConstantContext extends ParserRuleContext {
		public TerminalNode Identifier() { return getToken(CParser.Identifier, 0); }
		public EnumerationConstantContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_enumerationConstant; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterEnumerationConstant(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitEnumerationConstant(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitEnumerationConstant(this);
			else return visitor.visitChildren(this);
		}
	}

	public final EnumerationConstantContext enumerationConstant() throws RecognitionException {
		EnumerationConstantContext _localctx = new EnumerationConstantContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_enumerationConstant);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(242);
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
	public static class PredefinedConstantContext extends ParserRuleContext {
		public TerminalNode False_() { return getToken(CParser.False_, 0); }
		public TerminalNode True_() { return getToken(CParser.True_, 0); }
		public TerminalNode Nulptr() { return getToken(CParser.Nulptr, 0); }
		public PredefinedConstantContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_predefinedConstant; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterPredefinedConstant(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitPredefinedConstant(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitPredefinedConstant(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PredefinedConstantContext predefinedConstant() throws RecognitionException {
		PredefinedConstantContext _localctx = new PredefinedConstantContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_predefinedConstant);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(244);
			_la = _input.LA(1);
			if ( !(((((_la - 45)) & ~0x3f) == 0 && ((1L << (_la - 45)) & 1049089L) != 0)) ) {
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
	public static class PrimaryExpressionContext extends ParserRuleContext {
		public TerminalNode Identifier() { return getToken(CParser.Identifier, 0); }
		public ConstantContext constant() {
			return getRuleContext(ConstantContext.class,0);
		}
		public List<TerminalNode> StringLiteral() { return getTokens(CParser.StringLiteral); }
		public TerminalNode StringLiteral(int i) {
			return getToken(CParser.StringLiteral, i);
		}
		public TerminalNode LeftParen() { return getToken(CParser.LeftParen, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode RightParen() { return getToken(CParser.RightParen, 0); }
		public GenericSelectionContext genericSelection() {
			return getRuleContext(GenericSelectionContext.class,0);
		}
		public TerminalNode KW__func__() { return getToken(CParser.KW__func__, 0); }
		public TerminalNode KW__FUNCTION__() { return getToken(CParser.KW__FUNCTION__, 0); }
		public TerminalNode KW__PRETTY_FUNCTION__() { return getToken(CParser.KW__PRETTY_FUNCTION__, 0); }
		public CompoundStatementContext compoundStatement() {
			return getRuleContext(CompoundStatementContext.class,0);
		}
		public TerminalNode KW__extension__() { return getToken(CParser.KW__extension__, 0); }
		public TerminalNode KW__builtin_va_arg() { return getToken(CParser.KW__builtin_va_arg, 0); }
		public List<UnaryExpressionContext> unaryExpression() {
			return getRuleContexts(UnaryExpressionContext.class);
		}
		public UnaryExpressionContext unaryExpression(int i) {
			return getRuleContext(UnaryExpressionContext.class,i);
		}
		public List<TerminalNode> Comma() { return getTokens(CParser.Comma); }
		public TerminalNode Comma(int i) {
			return getToken(CParser.Comma, i);
		}
		public List<TypeNameContext> typeName() {
			return getRuleContexts(TypeNameContext.class);
		}
		public TypeNameContext typeName(int i) {
			return getRuleContext(TypeNameContext.class,i);
		}
		public TerminalNode KW__builtin_offsetof() { return getToken(CParser.KW__builtin_offsetof, 0); }
		public TerminalNode KW__builtin_choose_expr() { return getToken(CParser.KW__builtin_choose_expr, 0); }
		public TerminalNode KW__builtin_types_compatible_p() { return getToken(CParser.KW__builtin_types_compatible_p, 0); }
		public TerminalNode KW__builtin_tgmath() { return getToken(CParser.KW__builtin_tgmath, 0); }
		public ExprListContext exprList() {
			return getRuleContext(ExprListContext.class,0);
		}
		public TerminalNode KW__builtin_complex() { return getToken(CParser.KW__builtin_complex, 0); }
		public List<AssignmentExpressionContext> assignmentExpression() {
			return getRuleContexts(AssignmentExpressionContext.class);
		}
		public AssignmentExpressionContext assignmentExpression(int i) {
			return getRuleContext(AssignmentExpressionContext.class,i);
		}
		public PrimaryExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_primaryExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterPrimaryExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitPrimaryExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitPrimaryExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PrimaryExpressionContext primaryExpression() throws RecognitionException {
		PrimaryExpressionContext _localctx = new PrimaryExpressionContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_primaryExpression);
		int _la;
		try {
			setState(311);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,4,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(246);
				match(Identifier);
				this.LookupSymbol();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(248);
				constant();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(250); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(249);
					match(StringLiteral);
					}
					}
					setState(252); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==StringLiteral );
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(254);
				match(LeftParen);
				setState(255);
				expression();
				setState(256);
				match(RightParen);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(258);
				genericSelection();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(259);
				match(KW__func__);
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(260);
				match(KW__FUNCTION__);
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(261);
				match(KW__PRETTY_FUNCTION__);
				}
				break;
			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(263);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==KW__extension__) {
					{
					setState(262);
					match(KW__extension__);
					}
				}

				setState(265);
				match(LeftParen);
				setState(266);
				compoundStatement();
				setState(267);
				match(RightParen);
				}
				break;
			case 10:
				enterOuterAlt(_localctx, 10);
				{
				setState(269);
				match(KW__builtin_va_arg);
				setState(270);
				match(LeftParen);
				setState(271);
				unaryExpression();
				setState(272);
				match(Comma);
				setState(273);
				typeName();
				setState(274);
				match(RightParen);
				}
				break;
			case 11:
				enterOuterAlt(_localctx, 11);
				{
				setState(276);
				match(KW__builtin_offsetof);
				setState(277);
				match(LeftParen);
				setState(278);
				typeName();
				setState(279);
				match(Comma);
				setState(280);
				unaryExpression();
				setState(281);
				match(RightParen);
				}
				break;
			case 12:
				enterOuterAlt(_localctx, 12);
				{
				setState(283);
				match(KW__builtin_choose_expr);
				setState(284);
				match(LeftParen);
				setState(285);
				unaryExpression();
				setState(286);
				match(Comma);
				setState(287);
				unaryExpression();
				setState(288);
				match(Comma);
				setState(289);
				unaryExpression();
				setState(290);
				match(RightParen);
				}
				break;
			case 13:
				enterOuterAlt(_localctx, 13);
				{
				setState(292);
				match(KW__builtin_types_compatible_p);
				setState(293);
				match(LeftParen);
				setState(294);
				typeName();
				setState(295);
				match(Comma);
				setState(296);
				typeName();
				setState(297);
				match(RightParen);
				}
				break;
			case 14:
				enterOuterAlt(_localctx, 14);
				{
				setState(299);
				match(KW__builtin_tgmath);
				setState(300);
				match(LeftParen);
				setState(301);
				exprList();
				setState(302);
				match(RightParen);
				}
				break;
			case 15:
				enterOuterAlt(_localctx, 15);
				{
				setState(304);
				match(KW__builtin_complex);
				setState(305);
				match(LeftParen);
				setState(306);
				assignmentExpression();
				setState(307);
				match(Comma);
				setState(308);
				assignmentExpression();
				setState(309);
				match(RightParen);
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
	public static class ExprListContext extends ParserRuleContext {
		public List<AssignmentExpressionContext> assignmentExpression() {
			return getRuleContexts(AssignmentExpressionContext.class);
		}
		public AssignmentExpressionContext assignmentExpression(int i) {
			return getRuleContext(AssignmentExpressionContext.class,i);
		}
		public List<TerminalNode> Comma() { return getTokens(CParser.Comma); }
		public TerminalNode Comma(int i) {
			return getToken(CParser.Comma, i);
		}
		public ExprListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_exprList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterExprList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitExprList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitExprList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExprListContext exprList() throws RecognitionException {
		ExprListContext _localctx = new ExprListContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_exprList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(313);
			assignmentExpression();
			setState(318);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==Comma) {
				{
				{
				setState(314);
				match(Comma);
				setState(315);
				assignmentExpression();
				}
				}
				setState(320);
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
	public static class GenericSelectionContext extends ParserRuleContext {
		public TerminalNode Generic() { return getToken(CParser.Generic, 0); }
		public TerminalNode LeftParen() { return getToken(CParser.LeftParen, 0); }
		public AssignmentExpressionContext assignmentExpression() {
			return getRuleContext(AssignmentExpressionContext.class,0);
		}
		public TerminalNode Comma() { return getToken(CParser.Comma, 0); }
		public GenericAssocListContext genericAssocList() {
			return getRuleContext(GenericAssocListContext.class,0);
		}
		public TerminalNode RightParen() { return getToken(CParser.RightParen, 0); }
		public GenericSelectionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_genericSelection; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterGenericSelection(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitGenericSelection(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitGenericSelection(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GenericSelectionContext genericSelection() throws RecognitionException {
		GenericSelectionContext _localctx = new GenericSelectionContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_genericSelection);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(321);
			match(Generic);
			setState(322);
			match(LeftParen);
			setState(323);
			assignmentExpression();
			setState(324);
			match(Comma);
			setState(325);
			genericAssocList();
			setState(326);
			match(RightParen);
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
	public static class GenericAssocListContext extends ParserRuleContext {
		public List<GenericAssociationContext> genericAssociation() {
			return getRuleContexts(GenericAssociationContext.class);
		}
		public GenericAssociationContext genericAssociation(int i) {
			return getRuleContext(GenericAssociationContext.class,i);
		}
		public List<TerminalNode> Comma() { return getTokens(CParser.Comma); }
		public TerminalNode Comma(int i) {
			return getToken(CParser.Comma, i);
		}
		public GenericAssocListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_genericAssocList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterGenericAssocList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitGenericAssocList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitGenericAssocList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GenericAssocListContext genericAssocList() throws RecognitionException {
		GenericAssocListContext _localctx = new GenericAssocListContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_genericAssocList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(328);
			genericAssociation();
			setState(333);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==Comma) {
				{
				{
				setState(329);
				match(Comma);
				setState(330);
				genericAssociation();
				}
				}
				setState(335);
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
	public static class GenericAssociationContext extends ParserRuleContext {
		public TerminalNode Colon() { return getToken(CParser.Colon, 0); }
		public AssignmentExpressionContext assignmentExpression() {
			return getRuleContext(AssignmentExpressionContext.class,0);
		}
		public TypeNameContext typeName() {
			return getRuleContext(TypeNameContext.class,0);
		}
		public TerminalNode Default() { return getToken(CParser.Default, 0); }
		public GenericAssociationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_genericAssociation; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterGenericAssociation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitGenericAssociation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitGenericAssociation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GenericAssociationContext genericAssociation() throws RecognitionException {
		GenericAssociationContext _localctx = new GenericAssociationContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_genericAssociation);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(338);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,7,_ctx) ) {
			case 1:
				{
				setState(336);
				typeName();
				}
				break;
			case 2:
				{
				setState(337);
				match(Default);
				}
				break;
			}
			setState(340);
			match(Colon);
			setState(341);
			assignmentExpression();
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
	public static class PostfixExpressionContext extends ParserRuleContext {
		public PrimaryExpressionContext primaryExpression() {
			return getRuleContext(PrimaryExpressionContext.class,0);
		}
		public List<TerminalNode> LeftParen() { return getTokens(CParser.LeftParen); }
		public TerminalNode LeftParen(int i) {
			return getToken(CParser.LeftParen, i);
		}
		public TypeNameContext typeName() {
			return getRuleContext(TypeNameContext.class,0);
		}
		public List<TerminalNode> RightParen() { return getTokens(CParser.RightParen); }
		public TerminalNode RightParen(int i) {
			return getToken(CParser.RightParen, i);
		}
		public TerminalNode LeftBrace() { return getToken(CParser.LeftBrace, 0); }
		public TerminalNode RightBrace() { return getToken(CParser.RightBrace, 0); }
		public List<TerminalNode> LeftBracket() { return getTokens(CParser.LeftBracket); }
		public TerminalNode LeftBracket(int i) {
			return getToken(CParser.LeftBracket, i);
		}
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public List<TerminalNode> RightBracket() { return getTokens(CParser.RightBracket); }
		public TerminalNode RightBracket(int i) {
			return getToken(CParser.RightBracket, i);
		}
		public List<TerminalNode> Identifier() { return getTokens(CParser.Identifier); }
		public TerminalNode Identifier(int i) {
			return getToken(CParser.Identifier, i);
		}
		public List<TerminalNode> PlusPlus() { return getTokens(CParser.PlusPlus); }
		public TerminalNode PlusPlus(int i) {
			return getToken(CParser.PlusPlus, i);
		}
		public List<TerminalNode> MinusMinus() { return getTokens(CParser.MinusMinus); }
		public TerminalNode MinusMinus(int i) {
			return getToken(CParser.MinusMinus, i);
		}
		public List<TerminalNode> Dot() { return getTokens(CParser.Dot); }
		public TerminalNode Dot(int i) {
			return getToken(CParser.Dot, i);
		}
		public List<TerminalNode> Arrow() { return getTokens(CParser.Arrow); }
		public TerminalNode Arrow(int i) {
			return getToken(CParser.Arrow, i);
		}
		public TerminalNode KW__extension__() { return getToken(CParser.KW__extension__, 0); }
		public InitializerListContext initializerList() {
			return getRuleContext(InitializerListContext.class,0);
		}
		public TerminalNode Comma() { return getToken(CParser.Comma, 0); }
		public List<ArgumentExpressionListContext> argumentExpressionList() {
			return getRuleContexts(ArgumentExpressionListContext.class);
		}
		public ArgumentExpressionListContext argumentExpressionList(int i) {
			return getRuleContext(ArgumentExpressionListContext.class,i);
		}
		public PostfixExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_postfixExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterPostfixExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitPostfixExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitPostfixExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PostfixExpressionContext postfixExpression() throws RecognitionException {
		PostfixExpressionContext _localctx = new PostfixExpressionContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_postfixExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(359);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,11,_ctx) ) {
			case 1:
				{
				setState(343);
				primaryExpression();
				}
				break;
			case 2:
				{
				setState(345);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==KW__extension__) {
					{
					setState(344);
					match(KW__extension__);
					}
				}

				setState(347);
				match(LeftParen);
				setState(348);
				typeName();
				setState(349);
				match(RightParen);
				setState(350);
				match(LeftBrace);
				setState(352);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,9,_ctx) ) {
				case 1:
					{
					setState(351);
					initializerList();
					}
					break;
				}
				setState(355);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==Comma) {
					{
					setState(354);
					match(Comma);
					}
				}

				setState(357);
				match(RightBrace);
				}
				break;
			}
			setState(376);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (((((_la - 85)) & ~0x3f) == 0 && ((1L << (_la - 85)) & 26388279107589L) != 0)) {
				{
				setState(374);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case LeftBracket:
					{
					setState(361);
					match(LeftBracket);
					setState(362);
					expression();
					setState(363);
					match(RightBracket);
					}
					break;
				case LeftParen:
					{
					setState(365);
					match(LeftParen);
					setState(367);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,12,_ctx) ) {
					case 1:
						{
						setState(366);
						argumentExpressionList();
						}
						break;
					}
					setState(369);
					match(RightParen);
					}
					break;
				case Arrow:
				case Dot:
					{
					setState(370);
					_la = _input.LA(1);
					if ( !(_la==Arrow || _la==Dot) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(371);
					match(Identifier);
					}
					break;
				case PlusPlus:
					{
					setState(372);
					match(PlusPlus);
					}
					break;
				case MinusMinus:
					{
					setState(373);
					match(MinusMinus);
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				setState(378);
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
	public static class ArgumentExpressionListContext extends ParserRuleContext {
		public List<AssignmentExpressionContext> assignmentExpression() {
			return getRuleContexts(AssignmentExpressionContext.class);
		}
		public AssignmentExpressionContext assignmentExpression(int i) {
			return getRuleContext(AssignmentExpressionContext.class,i);
		}
		public List<TerminalNode> Comma() { return getTokens(CParser.Comma); }
		public TerminalNode Comma(int i) {
			return getToken(CParser.Comma, i);
		}
		public ArgumentExpressionListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_argumentExpressionList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterArgumentExpressionList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitArgumentExpressionList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitArgumentExpressionList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ArgumentExpressionListContext argumentExpressionList() throws RecognitionException {
		ArgumentExpressionListContext _localctx = new ArgumentExpressionListContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_argumentExpressionList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(379);
			assignmentExpression();
			setState(384);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==Comma) {
				{
				{
				setState(380);
				match(Comma);
				setState(381);
				assignmentExpression();
				}
				}
				setState(386);
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
	public static class UnaryExpressionContext extends ParserRuleContext {
		public Token unaryOperator;
		public PostfixExpressionContext postfixExpression() {
			return getRuleContext(PostfixExpressionContext.class,0);
		}
		public TerminalNode PlusPlus() { return getToken(CParser.PlusPlus, 0); }
		public UnaryExpressionContext unaryExpression() {
			return getRuleContext(UnaryExpressionContext.class,0);
		}
		public TerminalNode MinusMinus() { return getToken(CParser.MinusMinus, 0); }
		public CastExpressionContext castExpression() {
			return getRuleContext(CastExpressionContext.class,0);
		}
		public TerminalNode And() { return getToken(CParser.And, 0); }
		public TerminalNode Star() { return getToken(CParser.Star, 0); }
		public TerminalNode Plus() { return getToken(CParser.Plus, 0); }
		public TerminalNode Minus() { return getToken(CParser.Minus, 0); }
		public TerminalNode Tilde() { return getToken(CParser.Tilde, 0); }
		public TerminalNode Not() { return getToken(CParser.Not, 0); }
		public TerminalNode KW__extension__() { return getToken(CParser.KW__extension__, 0); }
		public TerminalNode KW__real__() { return getToken(CParser.KW__real__, 0); }
		public TerminalNode KW__imag__() { return getToken(CParser.KW__imag__, 0); }
		public TerminalNode Sizeof() { return getToken(CParser.Sizeof, 0); }
		public TerminalNode LeftParen() { return getToken(CParser.LeftParen, 0); }
		public TypeNameContext typeName() {
			return getRuleContext(TypeNameContext.class,0);
		}
		public TerminalNode RightParen() { return getToken(CParser.RightParen, 0); }
		public TerminalNode Alignof() { return getToken(CParser.Alignof, 0); }
		public TerminalNode Countof() { return getToken(CParser.Countof, 0); }
		public TerminalNode Maxof() { return getToken(CParser.Maxof, 0); }
		public TerminalNode Minof() { return getToken(CParser.Minof, 0); }
		public TerminalNode AndAnd() { return getToken(CParser.AndAnd, 0); }
		public TerminalNode Identifier() { return getToken(CParser.Identifier, 0); }
		public UnaryExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_unaryExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterUnaryExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitUnaryExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitUnaryExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final UnaryExpressionContext unaryExpression() throws RecognitionException {
		UnaryExpressionContext _localctx = new UnaryExpressionContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_unaryExpression);
		int _la;
		try {
			setState(435);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,16,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(387);
				postfixExpression();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(388);
				match(PlusPlus);
				setState(389);
				unaryExpression();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(390);
				match(MinusMinus);
				setState(391);
				unaryExpression();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(392);
				((UnaryExpressionContext)_localctx).unaryOperator = _input.LT(1);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 1574912L) != 0) || ((((_la - 97)) & ~0x3f) == 0 && ((1L << (_la - 97)) & 12437L) != 0)) ) {
					((UnaryExpressionContext)_localctx).unaryOperator = (Token)_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(393);
				castExpression();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(394);
				if (!(!this.IsSomethingOfTypeName())) throw new FailedPredicateException(this, "!this.IsSomethingOfTypeName()");
				setState(395);
				match(Sizeof);
				setState(396);
				unaryExpression();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(397);
				if (!(this.IsSomethingOfTypeName())) throw new FailedPredicateException(this, "this.IsSomethingOfTypeName()");
				setState(398);
				match(Sizeof);
				setState(399);
				match(LeftParen);
				setState(400);
				typeName();
				setState(401);
				match(RightParen);
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(403);
				if (!(this.IsSomethingOfTypeName())) throw new FailedPredicateException(this, "this.IsSomethingOfTypeName()");
				setState(404);
				match(Alignof);
				setState(405);
				match(LeftParen);
				setState(406);
				typeName();
				setState(407);
				match(RightParen);
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(409);
				if (!(!this.IsSomethingOfTypeName())) throw new FailedPredicateException(this, "!this.IsSomethingOfTypeName()");
				setState(410);
				match(Countof);
				setState(411);
				unaryExpression();
				}
				break;
			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(412);
				if (!(this.IsSomethingOfTypeName())) throw new FailedPredicateException(this, "this.IsSomethingOfTypeName()");
				setState(413);
				match(Countof);
				setState(414);
				match(LeftParen);
				setState(415);
				typeName();
				setState(416);
				match(RightParen);
				}
				break;
			case 10:
				enterOuterAlt(_localctx, 10);
				{
				setState(418);
				if (!(!this.IsSomethingOfTypeName())) throw new FailedPredicateException(this, "!this.IsSomethingOfTypeName()");
				setState(419);
				match(Alignof);
				setState(420);
				unaryExpression();
				}
				break;
			case 11:
				enterOuterAlt(_localctx, 11);
				{
				setState(421);
				if (!(this.IsSomethingOfTypeName())) throw new FailedPredicateException(this, "this.IsSomethingOfTypeName()");
				setState(422);
				match(Maxof);
				setState(423);
				match(LeftParen);
				setState(424);
				typeName();
				setState(425);
				match(RightParen);
				}
				break;
			case 12:
				enterOuterAlt(_localctx, 12);
				{
				setState(427);
				if (!(this.IsSomethingOfTypeName())) throw new FailedPredicateException(this, "this.IsSomethingOfTypeName()");
				setState(428);
				match(Minof);
				setState(429);
				match(LeftParen);
				setState(430);
				typeName();
				setState(431);
				match(RightParen);
				}
				break;
			case 13:
				enterOuterAlt(_localctx, 13);
				{
				setState(433);
				match(AndAnd);
				setState(434);
				match(Identifier);
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
	public static class CastExpressionContext extends ParserRuleContext {
		public TerminalNode LeftParen() { return getToken(CParser.LeftParen, 0); }
		public TypeNameContext typeName() {
			return getRuleContext(TypeNameContext.class,0);
		}
		public TerminalNode RightParen() { return getToken(CParser.RightParen, 0); }
		public CastExpressionContext castExpression() {
			return getRuleContext(CastExpressionContext.class,0);
		}
		public UnaryExpressionContext unaryExpression() {
			return getRuleContext(UnaryExpressionContext.class,0);
		}
		public TerminalNode DigitSequence() { return getToken(CParser.DigitSequence, 0); }
		public CastExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_castExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterCastExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitCastExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitCastExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CastExpressionContext castExpression() throws RecognitionException {
		CastExpressionContext _localctx = new CastExpressionContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_castExpression);
		try {
			setState(445);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,17,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(437);
				if (!(this.IsCast())) throw new FailedPredicateException(this, "this.IsCast()");
				setState(438);
				match(LeftParen);
				setState(439);
				typeName();
				setState(440);
				match(RightParen);
				setState(441);
				castExpression();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(443);
				unaryExpression();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(444);
				match(DigitSequence);
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
	public static class MultiplicativeExpressionContext extends ParserRuleContext {
		public List<CastExpressionContext> castExpression() {
			return getRuleContexts(CastExpressionContext.class);
		}
		public CastExpressionContext castExpression(int i) {
			return getRuleContext(CastExpressionContext.class,i);
		}
		public List<TerminalNode> Star() { return getTokens(CParser.Star); }
		public TerminalNode Star(int i) {
			return getToken(CParser.Star, i);
		}
		public List<TerminalNode> Div() { return getTokens(CParser.Div); }
		public TerminalNode Div(int i) {
			return getToken(CParser.Div, i);
		}
		public List<TerminalNode> Mod() { return getTokens(CParser.Mod); }
		public TerminalNode Mod(int i) {
			return getToken(CParser.Mod, i);
		}
		public MultiplicativeExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_multiplicativeExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterMultiplicativeExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitMultiplicativeExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitMultiplicativeExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MultiplicativeExpressionContext multiplicativeExpression() throws RecognitionException {
		MultiplicativeExpressionContext _localctx = new MultiplicativeExpressionContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_multiplicativeExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(447);
			castExpression();
			setState(452);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (((((_la - 101)) & ~0x3f) == 0 && ((1L << (_la - 101)) & 7L) != 0)) {
				{
				{
				setState(448);
				_la = _input.LA(1);
				if ( !(((((_la - 101)) & ~0x3f) == 0 && ((1L << (_la - 101)) & 7L) != 0)) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(449);
				castExpression();
				}
				}
				setState(454);
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
	public static class AdditiveExpressionContext extends ParserRuleContext {
		public List<MultiplicativeExpressionContext> multiplicativeExpression() {
			return getRuleContexts(MultiplicativeExpressionContext.class);
		}
		public MultiplicativeExpressionContext multiplicativeExpression(int i) {
			return getRuleContext(MultiplicativeExpressionContext.class,i);
		}
		public List<TerminalNode> Plus() { return getTokens(CParser.Plus); }
		public TerminalNode Plus(int i) {
			return getToken(CParser.Plus, i);
		}
		public List<TerminalNode> Minus() { return getTokens(CParser.Minus); }
		public TerminalNode Minus(int i) {
			return getToken(CParser.Minus, i);
		}
		public AdditiveExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_additiveExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterAdditiveExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitAdditiveExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitAdditiveExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AdditiveExpressionContext additiveExpression() throws RecognitionException {
		AdditiveExpressionContext _localctx = new AdditiveExpressionContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_additiveExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(455);
			multiplicativeExpression();
			setState(460);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==Plus || _la==Minus) {
				{
				{
				setState(456);
				_la = _input.LA(1);
				if ( !(_la==Plus || _la==Minus) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(457);
				multiplicativeExpression();
				}
				}
				setState(462);
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
	public static class ShiftExpressionContext extends ParserRuleContext {
		public List<AdditiveExpressionContext> additiveExpression() {
			return getRuleContexts(AdditiveExpressionContext.class);
		}
		public AdditiveExpressionContext additiveExpression(int i) {
			return getRuleContext(AdditiveExpressionContext.class,i);
		}
		public List<TerminalNode> LeftShift() { return getTokens(CParser.LeftShift); }
		public TerminalNode LeftShift(int i) {
			return getToken(CParser.LeftShift, i);
		}
		public List<TerminalNode> RightShift() { return getTokens(CParser.RightShift); }
		public TerminalNode RightShift(int i) {
			return getToken(CParser.RightShift, i);
		}
		public ShiftExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_shiftExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterShiftExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitShiftExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitShiftExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ShiftExpressionContext shiftExpression() throws RecognitionException {
		ShiftExpressionContext _localctx = new ShiftExpressionContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_shiftExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(463);
			additiveExpression();
			setState(468);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==LeftShift || _la==RightShift) {
				{
				{
				setState(464);
				_la = _input.LA(1);
				if ( !(_la==LeftShift || _la==RightShift) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(465);
				additiveExpression();
				}
				}
				setState(470);
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
	public static class RelationalExpressionContext extends ParserRuleContext {
		public List<ShiftExpressionContext> shiftExpression() {
			return getRuleContexts(ShiftExpressionContext.class);
		}
		public ShiftExpressionContext shiftExpression(int i) {
			return getRuleContext(ShiftExpressionContext.class,i);
		}
		public List<TerminalNode> Less() { return getTokens(CParser.Less); }
		public TerminalNode Less(int i) {
			return getToken(CParser.Less, i);
		}
		public List<TerminalNode> Greater() { return getTokens(CParser.Greater); }
		public TerminalNode Greater(int i) {
			return getToken(CParser.Greater, i);
		}
		public List<TerminalNode> LessEqual() { return getTokens(CParser.LessEqual); }
		public TerminalNode LessEqual(int i) {
			return getToken(CParser.LessEqual, i);
		}
		public List<TerminalNode> GreaterEqual() { return getTokens(CParser.GreaterEqual); }
		public TerminalNode GreaterEqual(int i) {
			return getToken(CParser.GreaterEqual, i);
		}
		public RelationalExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_relationalExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterRelationalExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitRelationalExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitRelationalExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RelationalExpressionContext relationalExpression() throws RecognitionException {
		RelationalExpressionContext _localctx = new RelationalExpressionContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_relationalExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(471);
			shiftExpression();
			setState(476);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (((((_la - 91)) & ~0x3f) == 0 && ((1L << (_la - 91)) & 15L) != 0)) {
				{
				{
				setState(472);
				_la = _input.LA(1);
				if ( !(((((_la - 91)) & ~0x3f) == 0 && ((1L << (_la - 91)) & 15L) != 0)) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(473);
				shiftExpression();
				}
				}
				setState(478);
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
	public static class EqualityExpressionContext extends ParserRuleContext {
		public List<RelationalExpressionContext> relationalExpression() {
			return getRuleContexts(RelationalExpressionContext.class);
		}
		public RelationalExpressionContext relationalExpression(int i) {
			return getRuleContext(RelationalExpressionContext.class,i);
		}
		public List<TerminalNode> Equal() { return getTokens(CParser.Equal); }
		public TerminalNode Equal(int i) {
			return getToken(CParser.Equal, i);
		}
		public List<TerminalNode> NotEqual() { return getTokens(CParser.NotEqual); }
		public TerminalNode NotEqual(int i) {
			return getToken(CParser.NotEqual, i);
		}
		public EqualityExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_equalityExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterEqualityExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitEqualityExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitEqualityExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final EqualityExpressionContext equalityExpression() throws RecognitionException {
		EqualityExpressionContext _localctx = new EqualityExpressionContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_equalityExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(479);
			relationalExpression();
			setState(484);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==Equal || _la==NotEqual) {
				{
				{
				setState(480);
				_la = _input.LA(1);
				if ( !(_la==Equal || _la==NotEqual) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(481);
				relationalExpression();
				}
				}
				setState(486);
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
	public static class AndExpressionContext extends ParserRuleContext {
		public List<EqualityExpressionContext> equalityExpression() {
			return getRuleContexts(EqualityExpressionContext.class);
		}
		public EqualityExpressionContext equalityExpression(int i) {
			return getRuleContext(EqualityExpressionContext.class,i);
		}
		public List<TerminalNode> And() { return getTokens(CParser.And); }
		public TerminalNode And(int i) {
			return getToken(CParser.And, i);
		}
		public AndExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_andExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterAndExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitAndExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitAndExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AndExpressionContext andExpression() throws RecognitionException {
		AndExpressionContext _localctx = new AndExpressionContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_andExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(487);
			equalityExpression();
			setState(492);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==And) {
				{
				{
				setState(488);
				match(And);
				setState(489);
				equalityExpression();
				}
				}
				setState(494);
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
	public static class ExclusiveOrExpressionContext extends ParserRuleContext {
		public List<AndExpressionContext> andExpression() {
			return getRuleContexts(AndExpressionContext.class);
		}
		public AndExpressionContext andExpression(int i) {
			return getRuleContext(AndExpressionContext.class,i);
		}
		public List<TerminalNode> Caret() { return getTokens(CParser.Caret); }
		public TerminalNode Caret(int i) {
			return getToken(CParser.Caret, i);
		}
		public ExclusiveOrExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_exclusiveOrExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterExclusiveOrExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitExclusiveOrExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitExclusiveOrExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExclusiveOrExpressionContext exclusiveOrExpression() throws RecognitionException {
		ExclusiveOrExpressionContext _localctx = new ExclusiveOrExpressionContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_exclusiveOrExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(495);
			andExpression();
			setState(500);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==Caret) {
				{
				{
				setState(496);
				match(Caret);
				setState(497);
				andExpression();
				}
				}
				setState(502);
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
	public static class InclusiveOrExpressionContext extends ParserRuleContext {
		public List<ExclusiveOrExpressionContext> exclusiveOrExpression() {
			return getRuleContexts(ExclusiveOrExpressionContext.class);
		}
		public ExclusiveOrExpressionContext exclusiveOrExpression(int i) {
			return getRuleContext(ExclusiveOrExpressionContext.class,i);
		}
		public List<TerminalNode> Or() { return getTokens(CParser.Or); }
		public TerminalNode Or(int i) {
			return getToken(CParser.Or, i);
		}
		public InclusiveOrExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_inclusiveOrExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterInclusiveOrExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitInclusiveOrExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitInclusiveOrExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final InclusiveOrExpressionContext inclusiveOrExpression() throws RecognitionException {
		InclusiveOrExpressionContext _localctx = new InclusiveOrExpressionContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_inclusiveOrExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(503);
			exclusiveOrExpression();
			setState(508);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==Or) {
				{
				{
				setState(504);
				match(Or);
				setState(505);
				exclusiveOrExpression();
				}
				}
				setState(510);
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
	public static class LogicalAndExpressionContext extends ParserRuleContext {
		public List<InclusiveOrExpressionContext> inclusiveOrExpression() {
			return getRuleContexts(InclusiveOrExpressionContext.class);
		}
		public InclusiveOrExpressionContext inclusiveOrExpression(int i) {
			return getRuleContext(InclusiveOrExpressionContext.class,i);
		}
		public List<TerminalNode> AndAnd() { return getTokens(CParser.AndAnd); }
		public TerminalNode AndAnd(int i) {
			return getToken(CParser.AndAnd, i);
		}
		public LogicalAndExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_logicalAndExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterLogicalAndExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitLogicalAndExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitLogicalAndExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LogicalAndExpressionContext logicalAndExpression() throws RecognitionException {
		LogicalAndExpressionContext _localctx = new LogicalAndExpressionContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_logicalAndExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(511);
			inclusiveOrExpression();
			setState(516);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==AndAnd) {
				{
				{
				setState(512);
				match(AndAnd);
				setState(513);
				inclusiveOrExpression();
				}
				}
				setState(518);
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
	public static class LogicalOrExpressionContext extends ParserRuleContext {
		public List<LogicalAndExpressionContext> logicalAndExpression() {
			return getRuleContexts(LogicalAndExpressionContext.class);
		}
		public LogicalAndExpressionContext logicalAndExpression(int i) {
			return getRuleContext(LogicalAndExpressionContext.class,i);
		}
		public List<TerminalNode> OrOr() { return getTokens(CParser.OrOr); }
		public TerminalNode OrOr(int i) {
			return getToken(CParser.OrOr, i);
		}
		public LogicalOrExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_logicalOrExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterLogicalOrExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitLogicalOrExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitLogicalOrExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LogicalOrExpressionContext logicalOrExpression() throws RecognitionException {
		LogicalOrExpressionContext _localctx = new LogicalOrExpressionContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_logicalOrExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(519);
			logicalAndExpression();
			setState(524);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==OrOr) {
				{
				{
				setState(520);
				match(OrOr);
				setState(521);
				logicalAndExpression();
				}
				}
				setState(526);
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
	public static class ConditionalExpressionContext extends ParserRuleContext {
		public LogicalOrExpressionContext logicalOrExpression() {
			return getRuleContext(LogicalOrExpressionContext.class,0);
		}
		public TerminalNode Question() { return getToken(CParser.Question, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode Colon() { return getToken(CParser.Colon, 0); }
		public ConditionalExpressionContext conditionalExpression() {
			return getRuleContext(ConditionalExpressionContext.class,0);
		}
		public ConditionalExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_conditionalExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterConditionalExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitConditionalExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitConditionalExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ConditionalExpressionContext conditionalExpression() throws RecognitionException {
		ConditionalExpressionContext _localctx = new ConditionalExpressionContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_conditionalExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(527);
			logicalOrExpression();
			setState(533);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==Question) {
				{
				setState(528);
				match(Question);
				setState(529);
				expression();
				setState(530);
				match(Colon);
				setState(531);
				conditionalExpression();
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
	public static class AssignmentExpressionContext extends ParserRuleContext {
		public Token assignementOperator;
		public ConditionalExpressionContext conditionalExpression() {
			return getRuleContext(ConditionalExpressionContext.class,0);
		}
		public UnaryExpressionContext unaryExpression() {
			return getRuleContext(UnaryExpressionContext.class,0);
		}
		public AssignmentExpressionContext assignmentExpression() {
			return getRuleContext(AssignmentExpressionContext.class,0);
		}
		public TerminalNode Assign() { return getToken(CParser.Assign, 0); }
		public TerminalNode StarAssign() { return getToken(CParser.StarAssign, 0); }
		public TerminalNode DivAssign() { return getToken(CParser.DivAssign, 0); }
		public TerminalNode ModAssign() { return getToken(CParser.ModAssign, 0); }
		public TerminalNode PlusAssign() { return getToken(CParser.PlusAssign, 0); }
		public TerminalNode MinusAssign() { return getToken(CParser.MinusAssign, 0); }
		public TerminalNode LeftShiftAssign() { return getToken(CParser.LeftShiftAssign, 0); }
		public TerminalNode RightShiftAssign() { return getToken(CParser.RightShiftAssign, 0); }
		public TerminalNode AndAssign() { return getToken(CParser.AndAssign, 0); }
		public TerminalNode XorAssign() { return getToken(CParser.XorAssign, 0); }
		public TerminalNode OrAssign() { return getToken(CParser.OrAssign, 0); }
		public TerminalNode DigitSequence() { return getToken(CParser.DigitSequence, 0); }
		public AssignmentExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_assignmentExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterAssignmentExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitAssignmentExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitAssignmentExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AssignmentExpressionContext assignmentExpression() throws RecognitionException {
		AssignmentExpressionContext _localctx = new AssignmentExpressionContext(_ctx, getState());
		enterRule(_localctx, 48, RULE_assignmentExpression);
		int _la;
		try {
			setState(541);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,29,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(535);
				conditionalExpression();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(536);
				unaryExpression();
				setState(537);
				((AssignmentExpressionContext)_localctx).assignementOperator = _input.LT(1);
				_la = _input.LA(1);
				if ( !(((((_la - 115)) & ~0x3f) == 0 && ((1L << (_la - 115)) & 2047L) != 0)) ) {
					((AssignmentExpressionContext)_localctx).assignementOperator = (Token)_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(538);
				assignmentExpression();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(540);
				match(DigitSequence);
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
	public static class ExpressionContext extends ParserRuleContext {
		public List<AssignmentExpressionContext> assignmentExpression() {
			return getRuleContexts(AssignmentExpressionContext.class);
		}
		public AssignmentExpressionContext assignmentExpression(int i) {
			return getRuleContext(AssignmentExpressionContext.class,i);
		}
		public List<TerminalNode> Comma() { return getTokens(CParser.Comma); }
		public TerminalNode Comma(int i) {
			return getToken(CParser.Comma, i);
		}
		public ExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExpressionContext expression() throws RecognitionException {
		ExpressionContext _localctx = new ExpressionContext(_ctx, getState());
		enterRule(_localctx, 50, RULE_expression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(543);
			assignmentExpression();
			setState(548);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==Comma) {
				{
				{
				setState(544);
				match(Comma);
				setState(545);
				assignmentExpression();
				}
				}
				setState(550);
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
	public static class ConstantExpressionContext extends ParserRuleContext {
		public ConditionalExpressionContext conditionalExpression() {
			return getRuleContext(ConditionalExpressionContext.class,0);
		}
		public ConstantExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_constantExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterConstantExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitConstantExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitConstantExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ConstantExpressionContext constantExpression() throws RecognitionException {
		ConstantExpressionContext _localctx = new ConstantExpressionContext(_ctx, getState());
		enterRule(_localctx, 52, RULE_constantExpression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(551);
			conditionalExpression();
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
		public DeclarationSpecifiersContext declarationSpecifiers() {
			return getRuleContext(DeclarationSpecifiersContext.class,0);
		}
		public TerminalNode Semi() { return getToken(CParser.Semi, 0); }
		public StaticAssertDeclarationContext staticAssertDeclaration() {
			return getRuleContext(StaticAssertDeclarationContext.class,0);
		}
		public AttributeDeclarationContext attributeDeclaration() {
			return getRuleContext(AttributeDeclarationContext.class,0);
		}
		public InitDeclaratorListContext initDeclaratorList() {
			return getRuleContext(InitDeclaratorListContext.class,0);
		}
		public DeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_declaration; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterDeclaration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitDeclaration(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DeclarationContext declaration() throws RecognitionException {
		DeclarationContext _localctx = new DeclarationContext(_ctx, getState());
		enterRule(_localctx, 54, RULE_declaration);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(563);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,32,_ctx) ) {
			case 1:
				{
				setState(553);
				declarationSpecifiers();
				setState(557);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,31,_ctx) ) {
				case 1:
					{
					setState(554);
					if (!(this.IsInitDeclaratorList())) throw new FailedPredicateException(this, "this.IsInitDeclaratorList()");
					setState(555);
					initDeclaratorList();
					}
					break;
				case 2:
					{
					}
					break;
				}
				setState(559);
				match(Semi);
				}
				break;
			case 2:
				{
				setState(561);
				staticAssertDeclaration();
				}
				break;
			case 3:
				{
				setState(562);
				attributeDeclaration();
				}
				break;
			}
			this.EnterDeclaration();
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
	public static class DeclarationSpecifiersContext extends ParserRuleContext {
		public List<DeclarationSpecifierContext> declarationSpecifier() {
			return getRuleContexts(DeclarationSpecifierContext.class);
		}
		public DeclarationSpecifierContext declarationSpecifier(int i) {
			return getRuleContext(DeclarationSpecifierContext.class,i);
		}
		public DeclarationSpecifiersContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_declarationSpecifiers; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterDeclarationSpecifiers(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitDeclarationSpecifiers(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitDeclarationSpecifiers(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DeclarationSpecifiersContext declarationSpecifiers() throws RecognitionException {
		DeclarationSpecifiersContext _localctx = new DeclarationSpecifiersContext(_ctx, getState());
		enterRule(_localctx, 56, RULE_declarationSpecifiers);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(569); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(567);
					if (!( this.IsDeclarationSpecifier())) throw new FailedPredicateException(this, " this.IsDeclarationSpecifier()");
					setState(568);
					declarationSpecifier();
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(571); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,33,_ctx);
			} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
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
	public static class DeclarationSpecifierContext extends ParserRuleContext {
		public StorageClassSpecifierContext storageClassSpecifier() {
			return getRuleContext(StorageClassSpecifierContext.class,0);
		}
		public TypeSpecifierContext typeSpecifier() {
			return getRuleContext(TypeSpecifierContext.class,0);
		}
		public TypeQualifierContext typeQualifier() {
			return getRuleContext(TypeQualifierContext.class,0);
		}
		public FunctionSpecifierContext functionSpecifier() {
			return getRuleContext(FunctionSpecifierContext.class,0);
		}
		public AlignmentSpecifierContext alignmentSpecifier() {
			return getRuleContext(AlignmentSpecifierContext.class,0);
		}
		public DeclarationSpecifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_declarationSpecifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterDeclarationSpecifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitDeclarationSpecifier(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitDeclarationSpecifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DeclarationSpecifierContext declarationSpecifier() throws RecognitionException {
		DeclarationSpecifierContext _localctx = new DeclarationSpecifierContext(_ctx, getState());
		enterRule(_localctx, 58, RULE_declarationSpecifier);
		try {
			setState(578);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,34,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(573);
				storageClassSpecifier();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(574);
				typeSpecifier();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(575);
				typeQualifier();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(576);
				functionSpecifier();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(577);
				alignmentSpecifier();
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
	public static class InitDeclaratorListContext extends ParserRuleContext {
		public List<InitDeclaratorContext> initDeclarator() {
			return getRuleContexts(InitDeclaratorContext.class);
		}
		public InitDeclaratorContext initDeclarator(int i) {
			return getRuleContext(InitDeclaratorContext.class,i);
		}
		public List<TerminalNode> Comma() { return getTokens(CParser.Comma); }
		public TerminalNode Comma(int i) {
			return getToken(CParser.Comma, i);
		}
		public InitDeclaratorListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_initDeclaratorList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterInitDeclaratorList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitInitDeclaratorList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitInitDeclaratorList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final InitDeclaratorListContext initDeclaratorList() throws RecognitionException {
		InitDeclaratorListContext _localctx = new InitDeclaratorListContext(_ctx, getState());
		enterRule(_localctx, 60, RULE_initDeclaratorList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(580);
			initDeclarator();
			setState(585);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==Comma) {
				{
				{
				setState(581);
				match(Comma);
				setState(582);
				initDeclarator();
				}
				}
				setState(587);
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
	public static class InitDeclaratorContext extends ParserRuleContext {
		public DeclaratorContext declarator() {
			return getRuleContext(DeclaratorContext.class,0);
		}
		public TerminalNode Assign() { return getToken(CParser.Assign, 0); }
		public InitializerContext initializer() {
			return getRuleContext(InitializerContext.class,0);
		}
		public InitDeclaratorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_initDeclarator; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterInitDeclarator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitInitDeclarator(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitInitDeclarator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final InitDeclaratorContext initDeclarator() throws RecognitionException {
		InitDeclaratorContext _localctx = new InitDeclaratorContext(_ctx, getState());
		enterRule(_localctx, 62, RULE_initDeclarator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(588);
			declarator();
			setState(591);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==Assign) {
				{
				setState(589);
				match(Assign);
				setState(590);
				initializer();
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
	public static class AttributeDeclarationContext extends ParserRuleContext {
		public AttributeSpecifierSequenceContext attributeSpecifierSequence() {
			return getRuleContext(AttributeSpecifierSequenceContext.class,0);
		}
		public TerminalNode Semi() { return getToken(CParser.Semi, 0); }
		public AttributeDeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_attributeDeclaration; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterAttributeDeclaration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitAttributeDeclaration(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitAttributeDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AttributeDeclarationContext attributeDeclaration() throws RecognitionException {
		AttributeDeclarationContext _localctx = new AttributeDeclarationContext(_ctx, getState());
		enterRule(_localctx, 64, RULE_attributeDeclaration);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(593);
			attributeSpecifierSequence();
			setState(594);
			match(Semi);
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
	public static class StorageClassSpecifierContext extends ParserRuleContext {
		public TerminalNode Auto() { return getToken(CParser.Auto, 0); }
		public TerminalNode Constexpr() { return getToken(CParser.Constexpr, 0); }
		public TerminalNode Extern() { return getToken(CParser.Extern, 0); }
		public TerminalNode Register() { return getToken(CParser.Register, 0); }
		public TerminalNode Static() { return getToken(CParser.Static, 0); }
		public TerminalNode ThreadLocal() { return getToken(CParser.ThreadLocal, 0); }
		public TerminalNode Typedef() { return getToken(CParser.Typedef, 0); }
		public StorageClassSpecifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_storageClassSpecifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterStorageClassSpecifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitStorageClassSpecifier(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitStorageClassSpecifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StorageClassSpecifierContext storageClassSpecifier() throws RecognitionException {
		StorageClassSpecifierContext _localctx = new StorageClassSpecifierContext(_ctx, getState());
		enterRule(_localctx, 66, RULE_storageClassSpecifier);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(596);
			_la = _input.LA(1);
			if ( !(((((_la - 30)) & ~0x3f) == 0 && ((1L << (_la - 30)) & 18014469410013249L) != 0)) ) {
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
	public static class TypeSpecifierContext extends ParserRuleContext {
		public TerminalNode Void() { return getToken(CParser.Void, 0); }
		public TerminalNode Char() { return getToken(CParser.Char, 0); }
		public TerminalNode Short() { return getToken(CParser.Short, 0); }
		public TerminalNode Int() { return getToken(CParser.Int, 0); }
		public TerminalNode Long() { return getToken(CParser.Long, 0); }
		public TerminalNode Float() { return getToken(CParser.Float, 0); }
		public TerminalNode Double() { return getToken(CParser.Double, 0); }
		public TerminalNode Signed() { return getToken(CParser.Signed, 0); }
		public TerminalNode Unsigned() { return getToken(CParser.Unsigned, 0); }
		public TerminalNode Bool() { return getToken(CParser.Bool, 0); }
		public TerminalNode Complex() { return getToken(CParser.Complex, 0); }
		public TerminalNode KW__m128() { return getToken(CParser.KW__m128, 0); }
		public TerminalNode KW__m128d() { return getToken(CParser.KW__m128d, 0); }
		public TerminalNode KW__m128i() { return getToken(CParser.KW__m128i, 0); }
		public TerminalNode KW__extension__() { return getToken(CParser.KW__extension__, 0); }
		public TerminalNode LeftParen() { return getToken(CParser.LeftParen, 0); }
		public TerminalNode RightParen() { return getToken(CParser.RightParen, 0); }
		public AtomicTypeSpecifierContext atomicTypeSpecifier() {
			return getRuleContext(AtomicTypeSpecifierContext.class,0);
		}
		public StructOrUnionSpecifierContext structOrUnionSpecifier() {
			return getRuleContext(StructOrUnionSpecifierContext.class,0);
		}
		public EnumSpecifierContext enumSpecifier() {
			return getRuleContext(EnumSpecifierContext.class,0);
		}
		public TypedefNameContext typedefName() {
			return getRuleContext(TypedefNameContext.class,0);
		}
		public TypeofSpecifierContext typeofSpecifier() {
			return getRuleContext(TypeofSpecifierContext.class,0);
		}
		public TypeSpecifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeSpecifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterTypeSpecifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitTypeSpecifier(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitTypeSpecifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypeSpecifierContext typeSpecifier() throws RecognitionException {
		TypeSpecifierContext _localctx = new TypeSpecifierContext(_ctx, getState());
		enterRule(_localctx, 68, RULE_typeSpecifier);
		int _la;
		try {
			setState(624);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,38,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(598);
				match(Void);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(599);
				match(Char);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(600);
				match(Short);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(601);
				match(Int);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(602);
				match(Long);
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(603);
				match(Float);
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(604);
				match(Double);
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(605);
				match(Signed);
				}
				break;
			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(606);
				match(Unsigned);
				}
				break;
			case 10:
				enterOuterAlt(_localctx, 10);
				{
				setState(607);
				match(Bool);
				}
				break;
			case 11:
				enterOuterAlt(_localctx, 11);
				{
				setState(608);
				match(Complex);
				}
				break;
			case 12:
				enterOuterAlt(_localctx, 12);
				{
				setState(609);
				match(KW__m128);
				}
				break;
			case 13:
				enterOuterAlt(_localctx, 13);
				{
				setState(610);
				match(KW__m128d);
				}
				break;
			case 14:
				enterOuterAlt(_localctx, 14);
				{
				setState(611);
				match(KW__m128i);
				}
				break;
			case 15:
				enterOuterAlt(_localctx, 15);
				{
				setState(612);
				match(KW__extension__);
				setState(613);
				match(LeftParen);
				setState(614);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 57344L) != 0)) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(615);
				match(RightParen);
				}
				break;
			case 16:
				enterOuterAlt(_localctx, 16);
				{
				setState(616);
				atomicTypeSpecifier();
				}
				break;
			case 17:
				enterOuterAlt(_localctx, 17);
				{
				setState(617);
				structOrUnionSpecifier();
				}
				break;
			case 18:
				enterOuterAlt(_localctx, 18);
				{
				setState(618);
				enumSpecifier();
				}
				break;
			case 19:
				enterOuterAlt(_localctx, 19);
				{
				setState(620);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,37,_ctx) ) {
				case 1:
					{
					setState(619);
					match(KW__extension__);
					}
					break;
				}
				setState(622);
				typedefName();
				}
				break;
			case 20:
				enterOuterAlt(_localctx, 20);
				{
				setState(623);
				typeofSpecifier();
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
	public static class StructOrUnionSpecifierContext extends ParserRuleContext {
		public StructOrUnionContext structOrUnion() {
			return getRuleContext(StructOrUnionContext.class,0);
		}
		public TerminalNode LeftBrace() { return getToken(CParser.LeftBrace, 0); }
		public TerminalNode RightBrace() { return getToken(CParser.RightBrace, 0); }
		public TerminalNode Identifier() { return getToken(CParser.Identifier, 0); }
		public AttributeSpecifierSequenceContext attributeSpecifierSequence() {
			return getRuleContext(AttributeSpecifierSequenceContext.class,0);
		}
		public GnuAttributesContext gnuAttributes() {
			return getRuleContext(GnuAttributesContext.class,0);
		}
		public MemberDeclarationListContext memberDeclarationList() {
			return getRuleContext(MemberDeclarationListContext.class,0);
		}
		public StructOrUnionSpecifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_structOrUnionSpecifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterStructOrUnionSpecifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitStructOrUnionSpecifier(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitStructOrUnionSpecifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StructOrUnionSpecifierContext structOrUnionSpecifier() throws RecognitionException {
		StructOrUnionSpecifierContext _localctx = new StructOrUnionSpecifierContext(_ctx, getState());
		enterRule(_localctx, 70, RULE_structOrUnionSpecifier);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(626);
			structOrUnion();
			setState(628);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LeftBracket) {
				{
				setState(627);
				attributeSpecifierSequence();
				}
			}

			setState(631);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==Attribute) {
				{
				setState(630);
				gnuAttributes();
				}
			}

			setState(643);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,43,_ctx) ) {
			case 1:
				{
				setState(634);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==Identifier) {
					{
					setState(633);
					match(Identifier);
					}
				}

				setState(636);
				match(LeftBrace);
				setState(639);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,42,_ctx) ) {
				case 1:
					{
					setState(637);
					if (!(this.IsNullStructDeclarationListExtension())) throw new FailedPredicateException(this, "this.IsNullStructDeclarationListExtension()");
					}
					break;
				case 2:
					{
					setState(638);
					memberDeclarationList();
					}
					break;
				}
				setState(641);
				match(RightBrace);
				}
				break;
			case 2:
				{
				setState(642);
				match(Identifier);
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
	public static class StructOrUnionContext extends ParserRuleContext {
		public TerminalNode Struct() { return getToken(CParser.Struct, 0); }
		public TerminalNode Union() { return getToken(CParser.Union, 0); }
		public StructOrUnionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_structOrUnion; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterStructOrUnion(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitStructOrUnion(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitStructOrUnion(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StructOrUnionContext structOrUnion() throws RecognitionException {
		StructOrUnionContext _localctx = new StructOrUnionContext(_ctx, getState());
		enterRule(_localctx, 72, RULE_structOrUnion);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(645);
			_la = _input.LA(1);
			if ( !(_la==Struct || _la==Union) ) {
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
	public static class MemberDeclarationListContext extends ParserRuleContext {
		public List<MemberDeclarationContext> memberDeclaration() {
			return getRuleContexts(MemberDeclarationContext.class);
		}
		public MemberDeclarationContext memberDeclaration(int i) {
			return getRuleContext(MemberDeclarationContext.class,i);
		}
		public MemberDeclarationListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_memberDeclarationList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterMemberDeclarationList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitMemberDeclarationList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitMemberDeclarationList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MemberDeclarationListContext memberDeclarationList() throws RecognitionException {
		MemberDeclarationListContext _localctx = new MemberDeclarationListContext(_ctx, getState());
		enterRule(_localctx, 74, RULE_memberDeclarationList);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(648); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(647);
					memberDeclaration();
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(650); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,44,_ctx);
			} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
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
	public static class MemberDeclarationContext extends ParserRuleContext {
		public SpecifierQualifierListContext specifierQualifierList() {
			return getRuleContext(SpecifierQualifierListContext.class,0);
		}
		public TerminalNode Semi() { return getToken(CParser.Semi, 0); }
		public AttributeSpecifierSequenceContext attributeSpecifierSequence() {
			return getRuleContext(AttributeSpecifierSequenceContext.class,0);
		}
		public MemberDeclaratorListContext memberDeclaratorList() {
			return getRuleContext(MemberDeclaratorListContext.class,0);
		}
		public StaticAssertDeclarationContext staticAssertDeclaration() {
			return getRuleContext(StaticAssertDeclarationContext.class,0);
		}
		public TerminalNode KW__extension__() { return getToken(CParser.KW__extension__, 0); }
		public MemberDeclarationContext memberDeclaration() {
			return getRuleContext(MemberDeclarationContext.class,0);
		}
		public MemberDeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_memberDeclaration; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterMemberDeclaration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitMemberDeclaration(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitMemberDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MemberDeclarationContext memberDeclaration() throws RecognitionException {
		MemberDeclarationContext _localctx = new MemberDeclarationContext(_ctx, getState());
		enterRule(_localctx, 76, RULE_memberDeclaration);
		int _la;
		try {
			setState(664);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,47,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(653);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,45,_ctx) ) {
				case 1:
					{
					setState(652);
					attributeSpecifierSequence();
					}
					break;
				}
				setState(655);
				specifierQualifierList();
				setState(657);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 463618L) != 0) || ((((_la - 85)) & ~0x3f) == 0 && ((1L << (_la - 85)) & 70368886849537L) != 0)) {
					{
					setState(656);
					memberDeclaratorList();
					}
				}

				setState(659);
				match(Semi);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(661);
				staticAssertDeclaration();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(662);
				match(KW__extension__);
				setState(663);
				memberDeclaration();
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
	public static class SpecifierQualifierListContext extends ParserRuleContext {
		public GnuAttributesContext gnuAttributes() {
			return getRuleContext(GnuAttributesContext.class,0);
		}
		public List<TypeSpecifierQualifierContext> typeSpecifierQualifier() {
			return getRuleContexts(TypeSpecifierQualifierContext.class);
		}
		public TypeSpecifierQualifierContext typeSpecifierQualifier(int i) {
			return getRuleContext(TypeSpecifierQualifierContext.class,i);
		}
		public AttributeSpecifierSequenceContext attributeSpecifierSequence() {
			return getRuleContext(AttributeSpecifierSequenceContext.class,0);
		}
		public SpecifierQualifierListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_specifierQualifierList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterSpecifierQualifierList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitSpecifierQualifierList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitSpecifierQualifierList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SpecifierQualifierListContext specifierQualifierList() throws RecognitionException {
		SpecifierQualifierListContext _localctx = new SpecifierQualifierListContext(_ctx, getState());
		enterRule(_localctx, 78, RULE_specifierQualifierList);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(667);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,48,_ctx) ) {
			case 1:
				{
				setState(666);
				gnuAttributes();
				}
				break;
			}
			setState(671); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(669);
					if (!(this.IsTypeSpecifierQualifier())) throw new FailedPredicateException(this, "this.IsTypeSpecifierQualifier()");
					setState(670);
					typeSpecifierQualifier();
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(673); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,49,_ctx);
			} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
			setState(676);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,50,_ctx) ) {
			case 1:
				{
				setState(675);
				attributeSpecifierSequence();
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
	public static class TypeSpecifierQualifierContext extends ParserRuleContext {
		public TypeSpecifierContext typeSpecifier() {
			return getRuleContext(TypeSpecifierContext.class,0);
		}
		public TypeQualifierContext typeQualifier() {
			return getRuleContext(TypeQualifierContext.class,0);
		}
		public AlignmentSpecifierContext alignmentSpecifier() {
			return getRuleContext(AlignmentSpecifierContext.class,0);
		}
		public TypeSpecifierQualifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeSpecifierQualifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterTypeSpecifierQualifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitTypeSpecifierQualifier(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitTypeSpecifierQualifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypeSpecifierQualifierContext typeSpecifierQualifier() throws RecognitionException {
		TypeSpecifierQualifierContext _localctx = new TypeSpecifierQualifierContext(_ctx, getState());
		enterRule(_localctx, 80, RULE_typeSpecifierQualifier);
		try {
			setState(681);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,51,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(678);
				typeSpecifier();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(679);
				typeQualifier();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(680);
				alignmentSpecifier();
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
	public static class MemberDeclaratorListContext extends ParserRuleContext {
		public List<MemberDeclaratorContext> memberDeclarator() {
			return getRuleContexts(MemberDeclaratorContext.class);
		}
		public MemberDeclaratorContext memberDeclarator(int i) {
			return getRuleContext(MemberDeclaratorContext.class,i);
		}
		public List<TerminalNode> Comma() { return getTokens(CParser.Comma); }
		public TerminalNode Comma(int i) {
			return getToken(CParser.Comma, i);
		}
		public List<GnuAttributesContext> gnuAttributes() {
			return getRuleContexts(GnuAttributesContext.class);
		}
		public GnuAttributesContext gnuAttributes(int i) {
			return getRuleContext(GnuAttributesContext.class,i);
		}
		public MemberDeclaratorListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_memberDeclaratorList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterMemberDeclaratorList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitMemberDeclaratorList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitMemberDeclaratorList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MemberDeclaratorListContext memberDeclaratorList() throws RecognitionException {
		MemberDeclaratorListContext _localctx = new MemberDeclaratorListContext(_ctx, getState());
		enterRule(_localctx, 82, RULE_memberDeclaratorList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(683);
			memberDeclarator();
			setState(691);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==Comma) {
				{
				{
				setState(684);
				match(Comma);
				setState(686);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,52,_ctx) ) {
				case 1:
					{
					setState(685);
					gnuAttributes();
					}
					break;
				}
				setState(688);
				memberDeclarator();
				}
				}
				setState(693);
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
	public static class MemberDeclaratorContext extends ParserRuleContext {
		public DeclaratorContext declarator() {
			return getRuleContext(DeclaratorContext.class,0);
		}
		public GnuAttributesContext gnuAttributes() {
			return getRuleContext(GnuAttributesContext.class,0);
		}
		public TerminalNode Colon() { return getToken(CParser.Colon, 0); }
		public ConstantExpressionContext constantExpression() {
			return getRuleContext(ConstantExpressionContext.class,0);
		}
		public MemberDeclaratorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_memberDeclarator; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterMemberDeclarator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitMemberDeclarator(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitMemberDeclarator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MemberDeclaratorContext memberDeclarator() throws RecognitionException {
		MemberDeclaratorContext _localctx = new MemberDeclaratorContext(_ctx, getState());
		enterRule(_localctx, 84, RULE_memberDeclarator);
		int _la;
		try {
			setState(706);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,57,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(694);
				declarator();
				setState(696);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==Attribute) {
					{
					setState(695);
					gnuAttributes();
					}
				}

				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(699);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 463618L) != 0) || ((((_la - 85)) & ~0x3f) == 0 && ((1L << (_la - 85)) & 70368752631809L) != 0)) {
					{
					setState(698);
					declarator();
					}
				}

				setState(701);
				match(Colon);
				setState(702);
				constantExpression();
				setState(704);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==Attribute) {
					{
					setState(703);
					gnuAttributes();
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
	public static class EnumSpecifierContext extends ParserRuleContext {
		public TerminalNode Enum() { return getToken(CParser.Enum, 0); }
		public TerminalNode LeftBrace() { return getToken(CParser.LeftBrace, 0); }
		public EnumeratorListContext enumeratorList() {
			return getRuleContext(EnumeratorListContext.class,0);
		}
		public TerminalNode RightBrace() { return getToken(CParser.RightBrace, 0); }
		public AttributeSpecifierSequenceContext attributeSpecifierSequence() {
			return getRuleContext(AttributeSpecifierSequenceContext.class,0);
		}
		public GnuAttributesContext gnuAttributes() {
			return getRuleContext(GnuAttributesContext.class,0);
		}
		public TerminalNode Identifier() { return getToken(CParser.Identifier, 0); }
		public EnumTypeSpecifierContext enumTypeSpecifier() {
			return getRuleContext(EnumTypeSpecifierContext.class,0);
		}
		public TerminalNode Comma() { return getToken(CParser.Comma, 0); }
		public EnumSpecifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_enumSpecifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterEnumSpecifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitEnumSpecifier(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitEnumSpecifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final EnumSpecifierContext enumSpecifier() throws RecognitionException {
		EnumSpecifierContext _localctx = new EnumSpecifierContext(_ctx, getState());
		enterRule(_localctx, 86, RULE_enumSpecifier);
		int _la;
		try {
			setState(733);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,64,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(708);
				match(Enum);
				setState(710);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,58,_ctx) ) {
				case 1:
					{
					setState(709);
					attributeSpecifierSequence();
					}
					break;
				}
				setState(713);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,59,_ctx) ) {
				case 1:
					{
					setState(712);
					gnuAttributes();
					}
					break;
				}
				setState(716);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,60,_ctx) ) {
				case 1:
					{
					setState(715);
					match(Identifier);
					}
					break;
				}
				setState(719);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,61,_ctx) ) {
				case 1:
					{
					setState(718);
					enumTypeSpecifier();
					}
					break;
				}
				setState(721);
				match(LeftBrace);
				setState(722);
				enumeratorList();
				setState(724);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==Comma) {
					{
					setState(723);
					match(Comma);
					}
				}

				setState(726);
				match(RightBrace);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(728);
				match(Enum);
				setState(729);
				match(Identifier);
				setState(731);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,63,_ctx) ) {
				case 1:
					{
					setState(730);
					enumTypeSpecifier();
					}
					break;
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
	public static class EnumeratorListContext extends ParserRuleContext {
		public List<EnumeratorContext> enumerator() {
			return getRuleContexts(EnumeratorContext.class);
		}
		public EnumeratorContext enumerator(int i) {
			return getRuleContext(EnumeratorContext.class,i);
		}
		public List<TerminalNode> Comma() { return getTokens(CParser.Comma); }
		public TerminalNode Comma(int i) {
			return getToken(CParser.Comma, i);
		}
		public EnumeratorListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_enumeratorList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterEnumeratorList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitEnumeratorList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitEnumeratorList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final EnumeratorListContext enumeratorList() throws RecognitionException {
		EnumeratorListContext _localctx = new EnumeratorListContext(_ctx, getState());
		enterRule(_localctx, 88, RULE_enumeratorList);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(735);
			enumerator();
			setState(740);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,65,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(736);
					match(Comma);
					setState(737);
					enumerator();
					}
					} 
				}
				setState(742);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,65,_ctx);
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
	public static class EnumeratorContext extends ParserRuleContext {
		public EnumerationConstantContext enumerationConstant() {
			return getRuleContext(EnumerationConstantContext.class,0);
		}
		public AttributeSpecifierSequenceContext attributeSpecifierSequence() {
			return getRuleContext(AttributeSpecifierSequenceContext.class,0);
		}
		public GnuAttributesContext gnuAttributes() {
			return getRuleContext(GnuAttributesContext.class,0);
		}
		public TerminalNode Assign() { return getToken(CParser.Assign, 0); }
		public ConstantExpressionContext constantExpression() {
			return getRuleContext(ConstantExpressionContext.class,0);
		}
		public EnumeratorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_enumerator; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterEnumerator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitEnumerator(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitEnumerator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final EnumeratorContext enumerator() throws RecognitionException {
		EnumeratorContext _localctx = new EnumeratorContext(_ctx, getState());
		enterRule(_localctx, 90, RULE_enumerator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(743);
			enumerationConstant();
			setState(745);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LeftBracket) {
				{
				setState(744);
				attributeSpecifierSequence();
				}
			}

			setState(748);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==Attribute) {
				{
				setState(747);
				gnuAttributes();
				}
			}

			setState(752);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==Assign) {
				{
				setState(750);
				match(Assign);
				setState(751);
				constantExpression();
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
	public static class EnumTypeSpecifierContext extends ParserRuleContext {
		public SpecifierQualifierListContext specifierQualifierList() {
			return getRuleContext(SpecifierQualifierListContext.class,0);
		}
		public EnumTypeSpecifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_enumTypeSpecifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterEnumTypeSpecifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitEnumTypeSpecifier(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitEnumTypeSpecifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final EnumTypeSpecifierContext enumTypeSpecifier() throws RecognitionException {
		EnumTypeSpecifierContext _localctx = new EnumTypeSpecifierContext(_ctx, getState());
		enterRule(_localctx, 92, RULE_enumTypeSpecifier);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(754);
			specifierQualifierList();
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
	public static class AtomicTypeSpecifierContext extends ParserRuleContext {
		public TerminalNode Atomic() { return getToken(CParser.Atomic, 0); }
		public TerminalNode LeftParen() { return getToken(CParser.LeftParen, 0); }
		public TypeNameContext typeName() {
			return getRuleContext(TypeNameContext.class,0);
		}
		public TerminalNode RightParen() { return getToken(CParser.RightParen, 0); }
		public AtomicTypeSpecifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_atomicTypeSpecifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterAtomicTypeSpecifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitAtomicTypeSpecifier(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitAtomicTypeSpecifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AtomicTypeSpecifierContext atomicTypeSpecifier() throws RecognitionException {
		AtomicTypeSpecifierContext _localctx = new AtomicTypeSpecifierContext(_ctx, getState());
		enterRule(_localctx, 94, RULE_atomicTypeSpecifier);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(756);
			match(Atomic);
			setState(757);
			match(LeftParen);
			setState(758);
			typeName();
			setState(759);
			match(RightParen);
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
	public static class TypeofSpecifierContext extends ParserRuleContext {
		public TerminalNode LeftParen() { return getToken(CParser.LeftParen, 0); }
		public TypeofSpecifierArgumentContext typeofSpecifierArgument() {
			return getRuleContext(TypeofSpecifierArgumentContext.class,0);
		}
		public TerminalNode RightParen() { return getToken(CParser.RightParen, 0); }
		public TerminalNode Typeof() { return getToken(CParser.Typeof, 0); }
		public TerminalNode Typeof_unqual() { return getToken(CParser.Typeof_unqual, 0); }
		public TypeofSpecifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeofSpecifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterTypeofSpecifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitTypeofSpecifier(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitTypeofSpecifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypeofSpecifierContext typeofSpecifier() throws RecognitionException {
		TypeofSpecifierContext _localctx = new TypeofSpecifierContext(_ctx, getState());
		enterRule(_localctx, 96, RULE_typeofSpecifier);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(761);
			_la = _input.LA(1);
			if ( !(_la==Typeof || _la==Typeof_unqual) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(762);
			match(LeftParen);
			setState(763);
			typeofSpecifierArgument();
			setState(764);
			match(RightParen);
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
	public static class TypeofSpecifierArgumentContext extends ParserRuleContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TypeNameContext typeName() {
			return getRuleContext(TypeNameContext.class,0);
		}
		public TypeofSpecifierArgumentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeofSpecifierArgument; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterTypeofSpecifierArgument(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitTypeofSpecifierArgument(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitTypeofSpecifierArgument(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypeofSpecifierArgumentContext typeofSpecifierArgument() throws RecognitionException {
		TypeofSpecifierArgumentContext _localctx = new TypeofSpecifierArgumentContext(_ctx, getState());
		enterRule(_localctx, 98, RULE_typeofSpecifierArgument);
		try {
			setState(768);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,69,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(766);
				expression();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(767);
				typeName();
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
	public static class TypeQualifierContext extends ParserRuleContext {
		public TerminalNode Const() { return getToken(CParser.Const, 0); }
		public TerminalNode Restrict() { return getToken(CParser.Restrict, 0); }
		public TerminalNode Volatile() { return getToken(CParser.Volatile, 0); }
		public TerminalNode Atomic() { return getToken(CParser.Atomic, 0); }
		public TypeQualifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeQualifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterTypeQualifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitTypeQualifier(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitTypeQualifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypeQualifierContext typeQualifier() throws RecognitionException {
		TypeQualifierContext _localctx = new TypeQualifierContext(_ctx, getState());
		enterRule(_localctx, 100, RULE_typeQualifier);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(770);
			_la = _input.LA(1);
			if ( !(((((_la - 35)) & ~0x3f) == 0 && ((1L << (_la - 35)) & 687196864513L) != 0)) ) {
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
	public static class FunctionSpecifierContext extends ParserRuleContext {
		public TerminalNode Inline() { return getToken(CParser.Inline, 0); }
		public TerminalNode Noreturn() { return getToken(CParser.Noreturn, 0); }
		public TerminalNode KW__stdcall() { return getToken(CParser.KW__stdcall, 0); }
		public GnuAttributeContext gnuAttribute() {
			return getRuleContext(GnuAttributeContext.class,0);
		}
		public TerminalNode KW__declspec() { return getToken(CParser.KW__declspec, 0); }
		public List<TerminalNode> LeftParen() { return getTokens(CParser.LeftParen); }
		public TerminalNode LeftParen(int i) {
			return getToken(CParser.LeftParen, i);
		}
		public List<TerminalNode> RightParen() { return getTokens(CParser.RightParen); }
		public TerminalNode RightParen(int i) {
			return getToken(CParser.RightParen, i);
		}
		public TerminalNode Identifier() { return getToken(CParser.Identifier, 0); }
		public TerminalNode Restrict() { return getToken(CParser.Restrict, 0); }
		public TerminalNode Deprecated() { return getToken(CParser.Deprecated, 0); }
		public TerminalNode StringLiteral() { return getToken(CParser.StringLiteral, 0); }
		public FunctionSpecifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_functionSpecifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterFunctionSpecifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitFunctionSpecifier(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitFunctionSpecifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FunctionSpecifierContext functionSpecifier() throws RecognitionException {
		FunctionSpecifierContext _localctx = new FunctionSpecifierContext(_ctx, getState());
		enterRule(_localctx, 102, RULE_functionSpecifier);
		int _la;
		try {
			setState(789);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case Inline:
				enterOuterAlt(_localctx, 1);
				{
				setState(772);
				match(Inline);
				}
				break;
			case Noreturn:
				enterOuterAlt(_localctx, 2);
				{
				setState(773);
				match(Noreturn);
				}
				break;
			case KW__stdcall:
				enterOuterAlt(_localctx, 3);
				{
				setState(774);
				match(KW__stdcall);
				}
				break;
			case Attribute:
				enterOuterAlt(_localctx, 4);
				{
				setState(775);
				gnuAttribute();
				}
				break;
			case KW__declspec:
				enterOuterAlt(_localctx, 5);
				{
				setState(776);
				match(KW__declspec);
				setState(777);
				match(LeftParen);
				setState(786);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case Identifier:
					{
					setState(778);
					match(Identifier);
					}
					break;
				case Restrict:
					{
					setState(779);
					match(Restrict);
					}
					break;
				case Deprecated:
					{
					setState(780);
					match(Deprecated);
					setState(781);
					match(LeftParen);
					setState(783);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==StringLiteral) {
						{
						setState(782);
						match(StringLiteral);
						}
					}

					setState(785);
					match(RightParen);
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(788);
				match(RightParen);
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
	public static class AlignmentSpecifierContext extends ParserRuleContext {
		public TerminalNode Alignas() { return getToken(CParser.Alignas, 0); }
		public TerminalNode LeftParen() { return getToken(CParser.LeftParen, 0); }
		public TerminalNode RightParen() { return getToken(CParser.RightParen, 0); }
		public TypeNameContext typeName() {
			return getRuleContext(TypeNameContext.class,0);
		}
		public ConstantExpressionContext constantExpression() {
			return getRuleContext(ConstantExpressionContext.class,0);
		}
		public AlignmentSpecifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_alignmentSpecifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterAlignmentSpecifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitAlignmentSpecifier(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitAlignmentSpecifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AlignmentSpecifierContext alignmentSpecifier() throws RecognitionException {
		AlignmentSpecifierContext _localctx = new AlignmentSpecifierContext(_ctx, getState());
		enterRule(_localctx, 104, RULE_alignmentSpecifier);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(791);
			match(Alignas);
			setState(792);
			match(LeftParen);
			setState(795);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,73,_ctx) ) {
			case 1:
				{
				setState(793);
				typeName();
				}
				break;
			case 2:
				{
				setState(794);
				constantExpression();
				}
				break;
			}
			setState(797);
			match(RightParen);
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
	public static class DeclaratorContext extends ParserRuleContext {
		public DirectDeclaratorContext directDeclarator() {
			return getRuleContext(DirectDeclaratorContext.class,0);
		}
		public List<PointerContext> pointer() {
			return getRuleContexts(PointerContext.class);
		}
		public PointerContext pointer(int i) {
			return getRuleContext(PointerContext.class,i);
		}
		public List<GnuAttributeContext> gnuAttribute() {
			return getRuleContexts(GnuAttributeContext.class);
		}
		public GnuAttributeContext gnuAttribute(int i) {
			return getRuleContext(GnuAttributeContext.class,i);
		}
		public List<GccDeclaratorExtensionContext> gccDeclaratorExtension() {
			return getRuleContexts(GccDeclaratorExtensionContext.class);
		}
		public GccDeclaratorExtensionContext gccDeclaratorExtension(int i) {
			return getRuleContext(GccDeclaratorExtensionContext.class,i);
		}
		public DeclaratorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_declarator; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterDeclarator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitDeclarator(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitDeclarator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DeclaratorContext declarator() throws RecognitionException {
		DeclaratorContext _localctx = new DeclaratorContext(_ctx, getState());
		enterRule(_localctx, 106, RULE_declarator);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(805);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,75,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(800);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==Attribute) {
						{
						setState(799);
						gnuAttribute();
						}
					}

					setState(802);
					pointer();
					}
					} 
				}
				setState(807);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,75,_ctx);
			}
			{
			setState(811);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,76,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(808);
					gnuAttribute();
					}
					} 
				}
				setState(813);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,76,_ctx);
			}
			setState(814);
			directDeclarator();
			setState(818);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,77,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(815);
					gccDeclaratorExtension();
					}
					} 
				}
				setState(820);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,77,_ctx);
			}
			}
			this.EnterDeclaration();
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
	public static class DirectDeclaratorContext extends ParserRuleContext {
		public TerminalNode Identifier() { return getToken(CParser.Identifier, 0); }
		public List<TerminalNode> LeftParen() { return getTokens(CParser.LeftParen); }
		public TerminalNode LeftParen(int i) {
			return getToken(CParser.LeftParen, i);
		}
		public DeclaratorContext declarator() {
			return getRuleContext(DeclaratorContext.class,0);
		}
		public List<TerminalNode> RightParen() { return getTokens(CParser.RightParen); }
		public TerminalNode RightParen(int i) {
			return getToken(CParser.RightParen, i);
		}
		public TerminalNode Colon() { return getToken(CParser.Colon, 0); }
		public TerminalNode DigitSequence() { return getToken(CParser.DigitSequence, 0); }
		public VcSpecificModiferContext vcSpecificModifer() {
			return getRuleContext(VcSpecificModiferContext.class,0);
		}
		public GnuAttributeContext gnuAttribute() {
			return getRuleContext(GnuAttributeContext.class,0);
		}
		public List<TerminalNode> LeftBracket() { return getTokens(CParser.LeftBracket); }
		public TerminalNode LeftBracket(int i) {
			return getToken(CParser.LeftBracket, i);
		}
		public List<TerminalNode> RightBracket() { return getTokens(CParser.RightBracket); }
		public TerminalNode RightBracket(int i) {
			return getToken(CParser.RightBracket, i);
		}
		public List<TerminalNode> Static() { return getTokens(CParser.Static); }
		public TerminalNode Static(int i) {
			return getToken(CParser.Static, i);
		}
		public List<AssignmentExpressionContext> assignmentExpression() {
			return getRuleContexts(AssignmentExpressionContext.class);
		}
		public AssignmentExpressionContext assignmentExpression(int i) {
			return getRuleContext(AssignmentExpressionContext.class,i);
		}
		public List<TypeQualifierListContext> typeQualifierList() {
			return getRuleContexts(TypeQualifierListContext.class);
		}
		public TypeQualifierListContext typeQualifierList(int i) {
			return getRuleContext(TypeQualifierListContext.class,i);
		}
		public List<TerminalNode> Star() { return getTokens(CParser.Star); }
		public TerminalNode Star(int i) {
			return getToken(CParser.Star, i);
		}
		public List<ParameterTypeListContext> parameterTypeList() {
			return getRuleContexts(ParameterTypeListContext.class);
		}
		public ParameterTypeListContext parameterTypeList(int i) {
			return getRuleContext(ParameterTypeListContext.class,i);
		}
		public List<AttributeSpecifierSequenceContext> attributeSpecifierSequence() {
			return getRuleContexts(AttributeSpecifierSequenceContext.class);
		}
		public AttributeSpecifierSequenceContext attributeSpecifierSequence(int i) {
			return getRuleContext(AttributeSpecifierSequenceContext.class,i);
		}
		public DirectDeclaratorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_directDeclarator; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterDirectDeclarator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitDirectDeclarator(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitDirectDeclarator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DirectDeclaratorContext directDeclarator() throws RecognitionException {
		DirectDeclaratorContext _localctx = new DirectDeclaratorContext(_ctx, getState());
		enterRule(_localctx, 108, RULE_directDeclarator);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(843);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,79,_ctx) ) {
			case 1:
				{
				setState(823);
				match(Identifier);
				setState(825);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,78,_ctx) ) {
				case 1:
					{
					setState(824);
					attributeSpecifierSequence();
					}
					break;
				}
				}
				break;
			case 2:
				{
				setState(827);
				match(LeftParen);
				setState(828);
				declarator();
				setState(829);
				match(RightParen);
				}
				break;
			case 3:
				{
				setState(831);
				match(Identifier);
				setState(832);
				match(Colon);
				setState(833);
				match(DigitSequence);
				}
				break;
			case 4:
				{
				setState(834);
				vcSpecificModifer();
				setState(835);
				match(Identifier);
				}
				break;
			case 5:
				{
				setState(837);
				match(LeftParen);
				setState(838);
				vcSpecificModifer();
				setState(839);
				declarator();
				setState(840);
				match(RightParen);
				}
				break;
			case 6:
				{
				setState(842);
				gnuAttribute();
				}
				break;
			}
			setState(891);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,90,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					setState(889);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,89,_ctx) ) {
					case 1:
						{
						setState(845);
						match(LeftBracket);
						setState(847);
						_errHandler.sync(this);
						switch ( getInterpreter().adaptivePredict(_input,80,_ctx) ) {
						case 1:
							{
							setState(846);
							typeQualifierList();
							}
							break;
						}
						setState(850);
						_errHandler.sync(this);
						switch ( getInterpreter().adaptivePredict(_input,81,_ctx) ) {
						case 1:
							{
							setState(849);
							assignmentExpression();
							}
							break;
						}
						setState(852);
						match(RightBracket);
						setState(854);
						_errHandler.sync(this);
						switch ( getInterpreter().adaptivePredict(_input,82,_ctx) ) {
						case 1:
							{
							setState(853);
							attributeSpecifierSequence();
							}
							break;
						}
						}
						break;
					case 2:
						{
						setState(856);
						match(LeftBracket);
						setState(857);
						match(Static);
						setState(859);
						_errHandler.sync(this);
						switch ( getInterpreter().adaptivePredict(_input,83,_ctx) ) {
						case 1:
							{
							setState(858);
							typeQualifierList();
							}
							break;
						}
						setState(861);
						assignmentExpression();
						setState(862);
						match(RightBracket);
						setState(864);
						_errHandler.sync(this);
						switch ( getInterpreter().adaptivePredict(_input,84,_ctx) ) {
						case 1:
							{
							setState(863);
							attributeSpecifierSequence();
							}
							break;
						}
						}
						break;
					case 3:
						{
						setState(866);
						match(LeftBracket);
						setState(867);
						typeQualifierList();
						setState(868);
						match(Static);
						setState(869);
						assignmentExpression();
						setState(870);
						match(RightBracket);
						setState(872);
						_errHandler.sync(this);
						switch ( getInterpreter().adaptivePredict(_input,85,_ctx) ) {
						case 1:
							{
							setState(871);
							attributeSpecifierSequence();
							}
							break;
						}
						}
						break;
					case 4:
						{
						setState(874);
						match(LeftBracket);
						setState(876);
						_errHandler.sync(this);
						_la = _input.LA(1);
						if (((((_la - 35)) & ~0x3f) == 0 && ((1L << (_la - 35)) & 687196864513L) != 0)) {
							{
							setState(875);
							typeQualifierList();
							}
						}

						setState(878);
						match(Star);
						setState(879);
						match(RightBracket);
						setState(881);
						_errHandler.sync(this);
						switch ( getInterpreter().adaptivePredict(_input,87,_ctx) ) {
						case 1:
							{
							setState(880);
							attributeSpecifierSequence();
							}
							break;
						}
						}
						break;
					case 5:
						{
						setState(883);
						match(LeftParen);
						setState(884);
						parameterTypeList();
						setState(885);
						match(RightParen);
						setState(887);
						_errHandler.sync(this);
						switch ( getInterpreter().adaptivePredict(_input,88,_ctx) ) {
						case 1:
							{
							setState(886);
							attributeSpecifierSequence();
							}
							break;
						}
						}
						break;
					}
					} 
				}
				setState(893);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,90,_ctx);
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
	public static class PointerContext extends ParserRuleContext {
		public List<TerminalNode> Star() { return getTokens(CParser.Star); }
		public TerminalNode Star(int i) {
			return getToken(CParser.Star, i);
		}
		public List<TerminalNode> Caret() { return getTokens(CParser.Caret); }
		public TerminalNode Caret(int i) {
			return getToken(CParser.Caret, i);
		}
		public List<TypeQualifierListContext> typeQualifierList() {
			return getRuleContexts(TypeQualifierListContext.class);
		}
		public TypeQualifierListContext typeQualifierList(int i) {
			return getRuleContext(TypeQualifierListContext.class,i);
		}
		public PointerContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_pointer; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterPointer(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitPointer(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitPointer(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PointerContext pointer() throws RecognitionException {
		PointerContext _localctx = new PointerContext(_ctx, getState());
		enterRule(_localctx, 110, RULE_pointer);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(898); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(894);
					_la = _input.LA(1);
					if ( !(_la==Star || _la==Caret) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(896);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (((((_la - 35)) & ~0x3f) == 0 && ((1L << (_la - 35)) & 687196864513L) != 0)) {
						{
						setState(895);
						typeQualifierList();
						}
					}

					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(900); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,92,_ctx);
			} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
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
	public static class TypeQualifierListContext extends ParserRuleContext {
		public List<TypeQualifierContext> typeQualifier() {
			return getRuleContexts(TypeQualifierContext.class);
		}
		public TypeQualifierContext typeQualifier(int i) {
			return getRuleContext(TypeQualifierContext.class,i);
		}
		public TypeQualifierListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeQualifierList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterTypeQualifierList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitTypeQualifierList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitTypeQualifierList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypeQualifierListContext typeQualifierList() throws RecognitionException {
		TypeQualifierListContext _localctx = new TypeQualifierListContext(_ctx, getState());
		enterRule(_localctx, 112, RULE_typeQualifierList);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(903); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(902);
					typeQualifier();
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(905); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,93,_ctx);
			} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
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
	public static class ParameterTypeListContext extends ParserRuleContext {
		public ParameterListContext parameterList() {
			return getRuleContext(ParameterListContext.class,0);
		}
		public TerminalNode Comma() { return getToken(CParser.Comma, 0); }
		public TerminalNode Ellipsis() { return getToken(CParser.Ellipsis, 0); }
		public ParameterTypeListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_parameterTypeList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterParameterTypeList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitParameterTypeList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitParameterTypeList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ParameterTypeListContext parameterTypeList() throws RecognitionException {
		ParameterTypeListContext _localctx = new ParameterTypeListContext(_ctx, getState());
		enterRule(_localctx, 114, RULE_parameterTypeList);
		int _la;
		try {
			setState(913);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,95,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(907);
				parameterList();
				setState(910);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==Comma) {
					{
					setState(908);
					match(Comma);
					setState(909);
					match(Ellipsis);
					}
				}

				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(912);
				match(Ellipsis);
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
	public static class ParameterListContext extends ParserRuleContext {
		public List<ParameterDeclarationContext> parameterDeclaration() {
			return getRuleContexts(ParameterDeclarationContext.class);
		}
		public ParameterDeclarationContext parameterDeclaration(int i) {
			return getRuleContext(ParameterDeclarationContext.class,i);
		}
		public List<TerminalNode> Comma() { return getTokens(CParser.Comma); }
		public TerminalNode Comma(int i) {
			return getToken(CParser.Comma, i);
		}
		public ParameterListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_parameterList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterParameterList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitParameterList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitParameterList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ParameterListContext parameterList() throws RecognitionException {
		ParameterListContext _localctx = new ParameterListContext(_ctx, getState());
		enterRule(_localctx, 116, RULE_parameterList);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(915);
			parameterDeclaration();
			setState(920);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,96,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(916);
					match(Comma);
					setState(917);
					parameterDeclaration();
					}
					} 
				}
				setState(922);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,96,_ctx);
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
	public static class ParameterDeclarationContext extends ParserRuleContext {
		public DeclaratorContext declarator() {
			return getRuleContext(DeclaratorContext.class,0);
		}
		public AbstractDeclaratorContext abstractDeclarator() {
			return getRuleContext(AbstractDeclaratorContext.class,0);
		}
		public DeclarationSpecifiersContext declarationSpecifiers() {
			return getRuleContext(DeclarationSpecifiersContext.class,0);
		}
		public AttributeSpecifierSequenceContext attributeSpecifierSequence() {
			return getRuleContext(AttributeSpecifierSequenceContext.class,0);
		}
		public ParameterDeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_parameterDeclaration; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterParameterDeclaration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitParameterDeclaration(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitParameterDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ParameterDeclarationContext parameterDeclaration() throws RecognitionException {
		ParameterDeclarationContext _localctx = new ParameterDeclarationContext(_ctx, getState());
		enterRule(_localctx, 118, RULE_parameterDeclaration);
		try {
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(924);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,97,_ctx) ) {
			case 1:
				{
				setState(923);
				attributeSpecifierSequence();
				}
				break;
			}
			setState(929);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,98,_ctx) ) {
			case 1:
				{
				setState(926);
				if (!(this.IsDeclarationSpecifier())) throw new FailedPredicateException(this, "this.IsDeclarationSpecifier()");
				setState(927);
				declarationSpecifiers();
				}
				break;
			case 2:
				{
				}
				break;
			}
			}
			setState(934);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,99,_ctx) ) {
			case 1:
				{
				setState(931);
				declarator();
				}
				break;
			case 2:
				{
				setState(932);
				abstractDeclarator();
				}
				break;
			case 3:
				{
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
	public static class TypeNameContext extends ParserRuleContext {
		public SpecifierQualifierListContext specifierQualifierList() {
			return getRuleContext(SpecifierQualifierListContext.class,0);
		}
		public AbstractDeclaratorContext abstractDeclarator() {
			return getRuleContext(AbstractDeclaratorContext.class,0);
		}
		public TypeNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterTypeName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitTypeName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitTypeName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypeNameContext typeName() throws RecognitionException {
		TypeNameContext _localctx = new TypeNameContext(_ctx, getState());
		enterRule(_localctx, 120, RULE_typeName);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(936);
			specifierQualifierList();
			setState(938);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 463616L) != 0) || ((((_la - 85)) & ~0x3f) == 0 && ((1L << (_la - 85)) & 8454149L) != 0)) {
				{
				setState(937);
				abstractDeclarator();
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
	public static class AbstractDeclaratorContext extends ParserRuleContext {
		public PointerContext pointer() {
			return getRuleContext(PointerContext.class,0);
		}
		public VcSpecificModiferContext vcSpecificModifer() {
			return getRuleContext(VcSpecificModiferContext.class,0);
		}
		public DirectAbstractDeclaratorContext directAbstractDeclarator() {
			return getRuleContext(DirectAbstractDeclaratorContext.class,0);
		}
		public List<GccDeclaratorExtensionContext> gccDeclaratorExtension() {
			return getRuleContexts(GccDeclaratorExtensionContext.class);
		}
		public GccDeclaratorExtensionContext gccDeclaratorExtension(int i) {
			return getRuleContext(GccDeclaratorExtensionContext.class,i);
		}
		public AbstractDeclaratorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_abstractDeclarator; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterAbstractDeclarator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitAbstractDeclarator(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitAbstractDeclarator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AbstractDeclaratorContext abstractDeclarator() throws RecognitionException {
		AbstractDeclaratorContext _localctx = new AbstractDeclaratorContext(_ctx, getState());
		enterRule(_localctx, 122, RULE_abstractDeclarator);
		int _la;
		try {
			setState(957);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,105,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(941);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 463616L) != 0)) {
					{
					setState(940);
					vcSpecificModifer();
					}
				}

				setState(943);
				pointer();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(945);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 463616L) != 0)) {
					{
					setState(944);
					vcSpecificModifer();
					}
				}

				setState(948);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==Star || _la==Caret) {
					{
					setState(947);
					pointer();
					}
				}

				setState(950);
				directAbstractDeclarator(0);
				setState(954);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==Attribute || _la==Asm) {
					{
					{
					setState(951);
					gccDeclaratorExtension();
					}
					}
					setState(956);
					_errHandler.sync(this);
					_la = _input.LA(1);
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
	public static class DirectAbstractDeclaratorContext extends ParserRuleContext {
		public TerminalNode LeftParen() { return getToken(CParser.LeftParen, 0); }
		public AbstractDeclaratorContext abstractDeclarator() {
			return getRuleContext(AbstractDeclaratorContext.class,0);
		}
		public TerminalNode RightParen() { return getToken(CParser.RightParen, 0); }
		public List<GccDeclaratorExtensionContext> gccDeclaratorExtension() {
			return getRuleContexts(GccDeclaratorExtensionContext.class);
		}
		public GccDeclaratorExtensionContext gccDeclaratorExtension(int i) {
			return getRuleContext(GccDeclaratorExtensionContext.class,i);
		}
		public TerminalNode LeftBracket() { return getToken(CParser.LeftBracket, 0); }
		public TerminalNode RightBracket() { return getToken(CParser.RightBracket, 0); }
		public TypeQualifierListContext typeQualifierList() {
			return getRuleContext(TypeQualifierListContext.class,0);
		}
		public AssignmentExpressionContext assignmentExpression() {
			return getRuleContext(AssignmentExpressionContext.class,0);
		}
		public TerminalNode Static() { return getToken(CParser.Static, 0); }
		public TerminalNode Star() { return getToken(CParser.Star, 0); }
		public ParameterTypeListContext parameterTypeList() {
			return getRuleContext(ParameterTypeListContext.class,0);
		}
		public DirectAbstractDeclaratorContext directAbstractDeclarator() {
			return getRuleContext(DirectAbstractDeclaratorContext.class,0);
		}
		public DirectAbstractDeclaratorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_directAbstractDeclarator; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterDirectAbstractDeclarator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitDirectAbstractDeclarator(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitDirectAbstractDeclarator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DirectAbstractDeclaratorContext directAbstractDeclarator() throws RecognitionException {
		return directAbstractDeclarator(0);
	}

	private DirectAbstractDeclaratorContext directAbstractDeclarator(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		DirectAbstractDeclaratorContext _localctx = new DirectAbstractDeclaratorContext(_ctx, _parentState);
		DirectAbstractDeclaratorContext _prevctx = _localctx;
		int _startState = 124;
		enterRecursionRule(_localctx, 124, RULE_directAbstractDeclarator, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1003);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,111,_ctx) ) {
			case 1:
				{
				setState(960);
				match(LeftParen);
				setState(961);
				abstractDeclarator();
				setState(962);
				match(RightParen);
				setState(966);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,106,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(963);
						gccDeclaratorExtension();
						}
						} 
					}
					setState(968);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,106,_ctx);
				}
				}
				break;
			case 2:
				{
				setState(969);
				match(LeftBracket);
				setState(971);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,107,_ctx) ) {
				case 1:
					{
					setState(970);
					typeQualifierList();
					}
					break;
				}
				setState(974);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,108,_ctx) ) {
				case 1:
					{
					setState(973);
					assignmentExpression();
					}
					break;
				}
				setState(976);
				match(RightBracket);
				}
				break;
			case 3:
				{
				setState(977);
				match(LeftBracket);
				setState(978);
				match(Static);
				setState(980);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,109,_ctx) ) {
				case 1:
					{
					setState(979);
					typeQualifierList();
					}
					break;
				}
				setState(982);
				assignmentExpression();
				setState(983);
				match(RightBracket);
				}
				break;
			case 4:
				{
				setState(985);
				match(LeftBracket);
				setState(986);
				typeQualifierList();
				setState(987);
				match(Static);
				setState(988);
				assignmentExpression();
				setState(989);
				match(RightBracket);
				}
				break;
			case 5:
				{
				setState(991);
				match(LeftBracket);
				setState(992);
				match(Star);
				setState(993);
				match(RightBracket);
				}
				break;
			case 6:
				{
				setState(994);
				match(LeftParen);
				setState(995);
				parameterTypeList();
				setState(996);
				match(RightParen);
				setState(1000);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,110,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(997);
						gccDeclaratorExtension();
						}
						} 
					}
					setState(1002);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,110,_ctx);
				}
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(1046);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,117,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(1044);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,116,_ctx) ) {
					case 1:
						{
						_localctx = new DirectAbstractDeclaratorContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_directAbstractDeclarator);
						setState(1005);
						if (!(precpred(_ctx, 5))) throw new FailedPredicateException(this, "precpred(_ctx, 5)");
						setState(1006);
						match(LeftBracket);
						setState(1008);
						_errHandler.sync(this);
						switch ( getInterpreter().adaptivePredict(_input,112,_ctx) ) {
						case 1:
							{
							setState(1007);
							typeQualifierList();
							}
							break;
						}
						setState(1011);
						_errHandler.sync(this);
						switch ( getInterpreter().adaptivePredict(_input,113,_ctx) ) {
						case 1:
							{
							setState(1010);
							assignmentExpression();
							}
							break;
						}
						setState(1013);
						match(RightBracket);
						}
						break;
					case 2:
						{
						_localctx = new DirectAbstractDeclaratorContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_directAbstractDeclarator);
						setState(1014);
						if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
						setState(1015);
						match(LeftBracket);
						setState(1016);
						match(Static);
						setState(1018);
						_errHandler.sync(this);
						switch ( getInterpreter().adaptivePredict(_input,114,_ctx) ) {
						case 1:
							{
							setState(1017);
							typeQualifierList();
							}
							break;
						}
						setState(1020);
						assignmentExpression();
						setState(1021);
						match(RightBracket);
						}
						break;
					case 3:
						{
						_localctx = new DirectAbstractDeclaratorContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_directAbstractDeclarator);
						setState(1023);
						if (!(precpred(_ctx, 3))) throw new FailedPredicateException(this, "precpred(_ctx, 3)");
						setState(1024);
						match(LeftBracket);
						setState(1025);
						typeQualifierList();
						setState(1026);
						match(Static);
						setState(1027);
						assignmentExpression();
						setState(1028);
						match(RightBracket);
						}
						break;
					case 4:
						{
						_localctx = new DirectAbstractDeclaratorContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_directAbstractDeclarator);
						setState(1030);
						if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
						setState(1031);
						match(LeftBracket);
						setState(1032);
						match(Star);
						setState(1033);
						match(RightBracket);
						}
						break;
					case 5:
						{
						_localctx = new DirectAbstractDeclaratorContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_directAbstractDeclarator);
						setState(1034);
						if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
						setState(1035);
						match(LeftParen);
						setState(1036);
						parameterTypeList();
						setState(1037);
						match(RightParen);
						setState(1041);
						_errHandler.sync(this);
						_alt = getInterpreter().adaptivePredict(_input,115,_ctx);
						while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
							if ( _alt==1 ) {
								{
								{
								setState(1038);
								gccDeclaratorExtension();
								}
								} 
							}
							setState(1043);
							_errHandler.sync(this);
							_alt = getInterpreter().adaptivePredict(_input,115,_ctx);
						}
						}
						break;
					}
					} 
				}
				setState(1048);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,117,_ctx);
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
	public static class TypedefNameContext extends ParserRuleContext {
		public TerminalNode Identifier() { return getToken(CParser.Identifier, 0); }
		public TypedefNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typedefName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterTypedefName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitTypedefName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitTypedefName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypedefNameContext typedefName() throws RecognitionException {
		TypedefNameContext _localctx = new TypedefNameContext(_ctx, getState());
		enterRule(_localctx, 126, RULE_typedefName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1049);
			if (!(this.IsTypedefName())) throw new FailedPredicateException(this, "this.IsTypedefName()");
			setState(1050);
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
	public static class InitializerContext extends ParserRuleContext {
		public AssignmentExpressionContext assignmentExpression() {
			return getRuleContext(AssignmentExpressionContext.class,0);
		}
		public TerminalNode LeftBrace() { return getToken(CParser.LeftBrace, 0); }
		public InitializerListContext initializerList() {
			return getRuleContext(InitializerListContext.class,0);
		}
		public TerminalNode RightBrace() { return getToken(CParser.RightBrace, 0); }
		public TerminalNode Comma() { return getToken(CParser.Comma, 0); }
		public InitializerContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_initializer; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterInitializer(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitInitializer(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitInitializer(this);
			else return visitor.visitChildren(this);
		}
	}

	public final InitializerContext initializer() throws RecognitionException {
		InitializerContext _localctx = new InitializerContext(_ctx, getState());
		enterRule(_localctx, 128, RULE_initializer);
		int _la;
		try {
			setState(1062);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,119,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1052);
				assignmentExpression();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1053);
				match(LeftBrace);
				setState(1054);
				initializerList();
				setState(1056);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==Comma) {
					{
					setState(1055);
					match(Comma);
					}
				}

				setState(1058);
				match(RightBrace);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1060);
				match(LeftBrace);
				setState(1061);
				match(RightBrace);
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
	public static class InitializerListContext extends ParserRuleContext {
		public List<InitializerContext> initializer() {
			return getRuleContexts(InitializerContext.class);
		}
		public InitializerContext initializer(int i) {
			return getRuleContext(InitializerContext.class,i);
		}
		public List<DesignationContext> designation() {
			return getRuleContexts(DesignationContext.class);
		}
		public DesignationContext designation(int i) {
			return getRuleContext(DesignationContext.class,i);
		}
		public List<TerminalNode> Comma() { return getTokens(CParser.Comma); }
		public TerminalNode Comma(int i) {
			return getToken(CParser.Comma, i);
		}
		public InitializerListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_initializerList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterInitializerList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitInitializerList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitInitializerList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final InitializerListContext initializerList() throws RecognitionException {
		InitializerListContext _localctx = new InitializerListContext(_ctx, getState());
		enterRule(_localctx, 130, RULE_initializerList);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1065);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,120,_ctx) ) {
			case 1:
				{
				setState(1064);
				designation();
				}
				break;
			}
			setState(1067);
			initializer();
			setState(1075);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,122,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(1068);
					match(Comma);
					setState(1070);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,121,_ctx) ) {
					case 1:
						{
						setState(1069);
						designation();
						}
						break;
					}
					setState(1072);
					initializer();
					}
					} 
				}
				setState(1077);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,122,_ctx);
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
	public static class DesignationContext extends ParserRuleContext {
		public DesignatorListContext designatorList() {
			return getRuleContext(DesignatorListContext.class,0);
		}
		public TerminalNode Assign() { return getToken(CParser.Assign, 0); }
		public GnuArrayDesignatorContext gnuArrayDesignator() {
			return getRuleContext(GnuArrayDesignatorContext.class,0);
		}
		public GnuIdentifierContext gnuIdentifier() {
			return getRuleContext(GnuIdentifierContext.class,0);
		}
		public TerminalNode Colon() { return getToken(CParser.Colon, 0); }
		public DesignationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_designation; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterDesignation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitDesignation(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitDesignation(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DesignationContext designation() throws RecognitionException {
		DesignationContext _localctx = new DesignationContext(_ctx, getState());
		enterRule(_localctx, 132, RULE_designation);
		try {
			setState(1085);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,123,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1078);
				designatorList();
				setState(1079);
				match(Assign);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1081);
				gnuArrayDesignator();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1082);
				gnuIdentifier();
				setState(1083);
				match(Colon);
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
	public static class DesignatorListContext extends ParserRuleContext {
		public List<DesignatorContext> designator() {
			return getRuleContexts(DesignatorContext.class);
		}
		public DesignatorContext designator(int i) {
			return getRuleContext(DesignatorContext.class,i);
		}
		public DesignatorListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_designatorList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterDesignatorList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitDesignatorList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitDesignatorList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DesignatorListContext designatorList() throws RecognitionException {
		DesignatorListContext _localctx = new DesignatorListContext(_ctx, getState());
		enterRule(_localctx, 134, RULE_designatorList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1088); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(1087);
				designator();
				}
				}
				setState(1090); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==LeftBracket || _la==Dot );
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
	public static class DesignatorContext extends ParserRuleContext {
		public GnuArrayDesignatorContext gnuArrayDesignator() {
			return getRuleContext(GnuArrayDesignatorContext.class,0);
		}
		public TerminalNode Dot() { return getToken(CParser.Dot, 0); }
		public TerminalNode Identifier() { return getToken(CParser.Identifier, 0); }
		public DesignatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_designator; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterDesignator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitDesignator(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitDesignator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DesignatorContext designator() throws RecognitionException {
		DesignatorContext _localctx = new DesignatorContext(_ctx, getState());
		enterRule(_localctx, 136, RULE_designator);
		try {
			setState(1095);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LeftBracket:
				enterOuterAlt(_localctx, 1);
				{
				setState(1092);
				gnuArrayDesignator();
				}
				break;
			case Dot:
				enterOuterAlt(_localctx, 2);
				{
				setState(1093);
				match(Dot);
				setState(1094);
				match(Identifier);
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
	public static class StaticAssertDeclarationContext extends ParserRuleContext {
		public TerminalNode StaticAssert() { return getToken(CParser.StaticAssert, 0); }
		public TerminalNode LeftParen() { return getToken(CParser.LeftParen, 0); }
		public ConstantExpressionContext constantExpression() {
			return getRuleContext(ConstantExpressionContext.class,0);
		}
		public TerminalNode RightParen() { return getToken(CParser.RightParen, 0); }
		public TerminalNode Semi() { return getToken(CParser.Semi, 0); }
		public TerminalNode Comma() { return getToken(CParser.Comma, 0); }
		public TerminalNode StringLiteral() { return getToken(CParser.StringLiteral, 0); }
		public StaticAssertDeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_staticAssertDeclaration; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterStaticAssertDeclaration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitStaticAssertDeclaration(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitStaticAssertDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StaticAssertDeclarationContext staticAssertDeclaration() throws RecognitionException {
		StaticAssertDeclarationContext _localctx = new StaticAssertDeclarationContext(_ctx, getState());
		enterRule(_localctx, 138, RULE_staticAssertDeclaration);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1097);
			match(StaticAssert);
			setState(1098);
			match(LeftParen);
			setState(1099);
			constantExpression();
			setState(1102);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==Comma) {
				{
				setState(1100);
				match(Comma);
				setState(1101);
				match(StringLiteral);
				}
			}

			setState(1104);
			match(RightParen);
			setState(1105);
			match(Semi);
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
	public static class AttributeSpecifierSequenceContext extends ParserRuleContext {
		public List<AttributeSpecifierContext> attributeSpecifier() {
			return getRuleContexts(AttributeSpecifierContext.class);
		}
		public AttributeSpecifierContext attributeSpecifier(int i) {
			return getRuleContext(AttributeSpecifierContext.class,i);
		}
		public AttributeSpecifierSequenceContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_attributeSpecifierSequence; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterAttributeSpecifierSequence(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitAttributeSpecifierSequence(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitAttributeSpecifierSequence(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AttributeSpecifierSequenceContext attributeSpecifierSequence() throws RecognitionException {
		AttributeSpecifierSequenceContext _localctx = new AttributeSpecifierSequenceContext(_ctx, getState());
		enterRule(_localctx, 140, RULE_attributeSpecifierSequence);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1108); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(1107);
					attributeSpecifier();
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(1110); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,127,_ctx);
			} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
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
	public static class AttributeSpecifierContext extends ParserRuleContext {
		public List<TerminalNode> LeftBracket() { return getTokens(CParser.LeftBracket); }
		public TerminalNode LeftBracket(int i) {
			return getToken(CParser.LeftBracket, i);
		}
		public AttributeListContext attributeList() {
			return getRuleContext(AttributeListContext.class,0);
		}
		public List<TerminalNode> RightBracket() { return getTokens(CParser.RightBracket); }
		public TerminalNode RightBracket(int i) {
			return getToken(CParser.RightBracket, i);
		}
		public AttributeSpecifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_attributeSpecifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterAttributeSpecifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitAttributeSpecifier(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitAttributeSpecifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AttributeSpecifierContext attributeSpecifier() throws RecognitionException {
		AttributeSpecifierContext _localctx = new AttributeSpecifierContext(_ctx, getState());
		enterRule(_localctx, 142, RULE_attributeSpecifier);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1112);
			match(LeftBracket);
			setState(1113);
			match(LeftBracket);
			setState(1114);
			attributeList();
			setState(1115);
			match(RightBracket);
			setState(1116);
			match(RightBracket);
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
	public static class AttributeListContext extends ParserRuleContext {
		public List<AttributeContext> attribute() {
			return getRuleContexts(AttributeContext.class);
		}
		public AttributeContext attribute(int i) {
			return getRuleContext(AttributeContext.class,i);
		}
		public List<TerminalNode> Comma() { return getTokens(CParser.Comma); }
		public TerminalNode Comma(int i) {
			return getToken(CParser.Comma, i);
		}
		public AttributeListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_attributeList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterAttributeList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitAttributeList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitAttributeList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AttributeListContext attributeList() throws RecognitionException {
		AttributeListContext _localctx = new AttributeListContext(_ctx, getState());
		enterRule(_localctx, 144, RULE_attributeList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1118);
			attribute();
			setState(1123);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==Comma) {
				{
				{
				setState(1119);
				match(Comma);
				setState(1120);
				attribute();
				}
				}
				setState(1125);
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
	public static class AttributeContext extends ParserRuleContext {
		public AttributeTokenContext attributeToken() {
			return getRuleContext(AttributeTokenContext.class,0);
		}
		public AttributeArgumentClauseContext attributeArgumentClause() {
			return getRuleContext(AttributeArgumentClauseContext.class,0);
		}
		public AttributeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_attribute; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterAttribute(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitAttribute(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitAttribute(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AttributeContext attribute() throws RecognitionException {
		AttributeContext _localctx = new AttributeContext(_ctx, getState());
		enterRule(_localctx, 146, RULE_attribute);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1126);
			attributeToken();
			setState(1128);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LeftParen) {
				{
				setState(1127);
				attributeArgumentClause();
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
	public static class AttributeTokenContext extends ParserRuleContext {
		public List<TerminalNode> Identifier() { return getTokens(CParser.Identifier); }
		public TerminalNode Identifier(int i) {
			return getToken(CParser.Identifier, i);
		}
		public List<TerminalNode> Colon() { return getTokens(CParser.Colon); }
		public TerminalNode Colon(int i) {
			return getToken(CParser.Colon, i);
		}
		public AttributeTokenContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_attributeToken; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterAttributeToken(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitAttributeToken(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitAttributeToken(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AttributeTokenContext attributeToken() throws RecognitionException {
		AttributeTokenContext _localctx = new AttributeTokenContext(_ctx, getState());
		enterRule(_localctx, 148, RULE_attributeToken);
		try {
			setState(1135);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,130,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1130);
				match(Identifier);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1131);
				match(Identifier);
				setState(1132);
				match(Colon);
				setState(1133);
				match(Colon);
				setState(1134);
				match(Identifier);
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
	public static class AttributeArgumentClauseContext extends ParserRuleContext {
		public TerminalNode LeftParen() { return getToken(CParser.LeftParen, 0); }
		public TerminalNode RightParen() { return getToken(CParser.RightParen, 0); }
		public BalancedTokenSequenceContext balancedTokenSequence() {
			return getRuleContext(BalancedTokenSequenceContext.class,0);
		}
		public AttributeArgumentClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_attributeArgumentClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterAttributeArgumentClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitAttributeArgumentClause(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitAttributeArgumentClause(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AttributeArgumentClauseContext attributeArgumentClause() throws RecognitionException {
		AttributeArgumentClauseContext _localctx = new AttributeArgumentClauseContext(_ctx, getState());
		enterRule(_localctx, 150, RULE_attributeArgumentClause);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1137);
			match(LeftParen);
			setState(1139);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (((((_la - 85)) & ~0x3f) == 0 && ((1L << (_la - 85)) & 21L) != 0)) {
				{
				setState(1138);
				balancedTokenSequence();
				}
			}

			setState(1141);
			match(RightParen);
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
	public static class BalancedTokenSequenceContext extends ParserRuleContext {
		public List<BalancedTokenContext> balancedToken() {
			return getRuleContexts(BalancedTokenContext.class);
		}
		public BalancedTokenContext balancedToken(int i) {
			return getRuleContext(BalancedTokenContext.class,i);
		}
		public BalancedTokenSequenceContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_balancedTokenSequence; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterBalancedTokenSequence(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitBalancedTokenSequence(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitBalancedTokenSequence(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BalancedTokenSequenceContext balancedTokenSequence() throws RecognitionException {
		BalancedTokenSequenceContext _localctx = new BalancedTokenSequenceContext(_ctx, getState());
		enterRule(_localctx, 152, RULE_balancedTokenSequence);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1144); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(1143);
				balancedToken();
				}
				}
				setState(1146); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( ((((_la - 85)) & ~0x3f) == 0 && ((1L << (_la - 85)) & 21L) != 0) );
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
	public static class BalancedTokenContext extends ParserRuleContext {
		public TerminalNode LeftParen() { return getToken(CParser.LeftParen, 0); }
		public TerminalNode RightParen() { return getToken(CParser.RightParen, 0); }
		public BalancedTokenSequenceContext balancedTokenSequence() {
			return getRuleContext(BalancedTokenSequenceContext.class,0);
		}
		public TerminalNode LeftBracket() { return getToken(CParser.LeftBracket, 0); }
		public TerminalNode RightBracket() { return getToken(CParser.RightBracket, 0); }
		public TerminalNode LeftBrace() { return getToken(CParser.LeftBrace, 0); }
		public TerminalNode RightBrace() { return getToken(CParser.RightBrace, 0); }
		public BalancedTokenContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_balancedToken; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterBalancedToken(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitBalancedToken(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitBalancedToken(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BalancedTokenContext balancedToken() throws RecognitionException {
		BalancedTokenContext _localctx = new BalancedTokenContext(_ctx, getState());
		enterRule(_localctx, 154, RULE_balancedToken);
		int _la;
		try {
			setState(1163);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LeftParen:
				enterOuterAlt(_localctx, 1);
				{
				setState(1148);
				match(LeftParen);
				setState(1150);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (((((_la - 85)) & ~0x3f) == 0 && ((1L << (_la - 85)) & 21L) != 0)) {
					{
					setState(1149);
					balancedTokenSequence();
					}
				}

				setState(1152);
				match(RightParen);
				}
				break;
			case LeftBracket:
				enterOuterAlt(_localctx, 2);
				{
				setState(1153);
				match(LeftBracket);
				setState(1155);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (((((_la - 85)) & ~0x3f) == 0 && ((1L << (_la - 85)) & 21L) != 0)) {
					{
					setState(1154);
					balancedTokenSequence();
					}
				}

				setState(1157);
				match(RightBracket);
				}
				break;
			case LeftBrace:
				enterOuterAlt(_localctx, 3);
				{
				setState(1158);
				match(LeftBrace);
				setState(1160);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (((((_la - 85)) & ~0x3f) == 0 && ((1L << (_la - 85)) & 21L) != 0)) {
					{
					setState(1159);
					balancedTokenSequence();
					}
				}

				setState(1162);
				match(RightBrace);
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
	public static class StatementContext extends ParserRuleContext {
		public LabeledStatementContext labeledStatement() {
			return getRuleContext(LabeledStatementContext.class,0);
		}
		public CompoundStatementContext compoundStatement() {
			return getRuleContext(CompoundStatementContext.class,0);
		}
		public ExpressionStatementContext expressionStatement() {
			return getRuleContext(ExpressionStatementContext.class,0);
		}
		public SelectionStatementContext selectionStatement() {
			return getRuleContext(SelectionStatementContext.class,0);
		}
		public IterationStatementContext iterationStatement() {
			return getRuleContext(IterationStatementContext.class,0);
		}
		public JumpStatementContext jumpStatement() {
			return getRuleContext(JumpStatementContext.class,0);
		}
		public AsmStatementContext asmStatement() {
			return getRuleContext(AsmStatementContext.class,0);
		}
		public StatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_statement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StatementContext statement() throws RecognitionException {
		StatementContext _localctx = new StatementContext(_ctx, getState());
		enterRule(_localctx, 156, RULE_statement);
		try {
			setState(1172);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,137,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1165);
				labeledStatement();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1166);
				compoundStatement();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1167);
				expressionStatement();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(1168);
				selectionStatement();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(1169);
				iterationStatement();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(1170);
				jumpStatement();
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(1171);
				asmStatement();
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
	public static class LabeledStatementContext extends ParserRuleContext {
		public TerminalNode Identifier() { return getToken(CParser.Identifier, 0); }
		public TerminalNode Colon() { return getToken(CParser.Colon, 0); }
		public StatementContext statement() {
			return getRuleContext(StatementContext.class,0);
		}
		public TerminalNode Label() { return getToken(CParser.Label, 0); }
		public TerminalNode Semi() { return getToken(CParser.Semi, 0); }
		public TerminalNode Case() { return getToken(CParser.Case, 0); }
		public ConstantExpressionContext constantExpression() {
			return getRuleContext(ConstantExpressionContext.class,0);
		}
		public TerminalNode Default() { return getToken(CParser.Default, 0); }
		public LabeledStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_labeledStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterLabeledStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitLabeledStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitLabeledStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LabeledStatementContext labeledStatement() throws RecognitionException {
		LabeledStatementContext _localctx = new LabeledStatementContext(_ctx, getState());
		enterRule(_localctx, 158, RULE_labeledStatement);
		try {
			setState(1190);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case Identifier:
				enterOuterAlt(_localctx, 1);
				{
				setState(1174);
				match(Identifier);
				setState(1175);
				match(Colon);
				setState(1177);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,138,_ctx) ) {
				case 1:
					{
					setState(1176);
					statement();
					}
					break;
				}
				}
				break;
			case Label:
				enterOuterAlt(_localctx, 2);
				{
				setState(1179);
				match(Label);
				setState(1180);
				match(Identifier);
				setState(1181);
				match(Semi);
				}
				break;
			case Case:
				enterOuterAlt(_localctx, 3);
				{
				setState(1182);
				match(Case);
				setState(1183);
				constantExpression();
				setState(1184);
				match(Colon);
				setState(1185);
				statement();
				}
				break;
			case Default:
				enterOuterAlt(_localctx, 4);
				{
				setState(1187);
				match(Default);
				setState(1188);
				match(Colon);
				setState(1189);
				statement();
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
	public static class CompoundStatementContext extends ParserRuleContext {
		public TerminalNode LeftBrace() { return getToken(CParser.LeftBrace, 0); }
		public TerminalNode RightBrace() { return getToken(CParser.RightBrace, 0); }
		public BlockItemListContext blockItemList() {
			return getRuleContext(BlockItemListContext.class,0);
		}
		public CompoundStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_compoundStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterCompoundStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitCompoundStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitCompoundStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CompoundStatementContext compoundStatement() throws RecognitionException {
		CompoundStatementContext _localctx = new CompoundStatementContext(_ctx, getState());
		enterRule(_localctx, 160, RULE_compoundStatement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1192);
			match(LeftBrace);
			this.EnterScope();
			setState(1195);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,140,_ctx) ) {
			case 1:
				{
				setState(1194);
				blockItemList();
				}
				break;
			}
			setState(1197);
			match(RightBrace);
			this.ExitScope();
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
	public static class BlockItemListContext extends ParserRuleContext {
		public List<BlockItemContext> blockItem() {
			return getRuleContexts(BlockItemContext.class);
		}
		public BlockItemContext blockItem(int i) {
			return getRuleContext(BlockItemContext.class,i);
		}
		public BlockItemListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_blockItemList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterBlockItemList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitBlockItemList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitBlockItemList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BlockItemListContext blockItemList() throws RecognitionException {
		BlockItemListContext _localctx = new BlockItemListContext(_ctx, getState());
		enterRule(_localctx, 162, RULE_blockItemList);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1201); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(1200);
					blockItem();
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(1203); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,141,_ctx);
			} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
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
	public static class BlockItemContext extends ParserRuleContext {
		public StatementContext statement() {
			return getRuleContext(StatementContext.class,0);
		}
		public DeclarationContext declaration() {
			return getRuleContext(DeclarationContext.class,0);
		}
		public BlockItemContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_blockItem; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterBlockItem(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitBlockItem(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitBlockItem(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BlockItemContext blockItem() throws RecognitionException {
		BlockItemContext _localctx = new BlockItemContext(_ctx, getState());
		enterRule(_localctx, 164, RULE_blockItem);
		try {
			setState(1209);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,142,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1205);
				if (!(this.IsStatement())) throw new FailedPredicateException(this, "this.IsStatement()");
				setState(1206);
				statement();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1207);
				if (!(this.IsDeclaration())) throw new FailedPredicateException(this, "this.IsDeclaration()");
				setState(1208);
				declaration();
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
	public static class ExpressionStatementContext extends ParserRuleContext {
		public TerminalNode Semi() { return getToken(CParser.Semi, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public ExpressionStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expressionStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterExpressionStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitExpressionStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitExpressionStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExpressionStatementContext expressionStatement() throws RecognitionException {
		ExpressionStatementContext _localctx = new ExpressionStatementContext(_ctx, getState());
		enterRule(_localctx, 166, RULE_expressionStatement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1212);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,143,_ctx) ) {
			case 1:
				{
				setState(1211);
				expression();
				}
				break;
			}
			setState(1214);
			match(Semi);
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
	public static class SelectionStatementContext extends ParserRuleContext {
		public TerminalNode If() { return getToken(CParser.If, 0); }
		public TerminalNode LeftParen() { return getToken(CParser.LeftParen, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode RightParen() { return getToken(CParser.RightParen, 0); }
		public List<StatementContext> statement() {
			return getRuleContexts(StatementContext.class);
		}
		public StatementContext statement(int i) {
			return getRuleContext(StatementContext.class,i);
		}
		public TerminalNode Else() { return getToken(CParser.Else, 0); }
		public TerminalNode Switch() { return getToken(CParser.Switch, 0); }
		public SelectionStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_selectionStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterSelectionStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitSelectionStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitSelectionStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SelectionStatementContext selectionStatement() throws RecognitionException {
		SelectionStatementContext _localctx = new SelectionStatementContext(_ctx, getState());
		enterRule(_localctx, 168, RULE_selectionStatement);
		try {
			setState(1231);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case If:
				enterOuterAlt(_localctx, 1);
				{
				setState(1216);
				match(If);
				setState(1217);
				match(LeftParen);
				setState(1218);
				expression();
				setState(1219);
				match(RightParen);
				setState(1220);
				statement();
				setState(1223);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,144,_ctx) ) {
				case 1:
					{
					setState(1221);
					match(Else);
					setState(1222);
					statement();
					}
					break;
				}
				}
				break;
			case Switch:
				enterOuterAlt(_localctx, 2);
				{
				setState(1225);
				match(Switch);
				setState(1226);
				match(LeftParen);
				setState(1227);
				expression();
				setState(1228);
				match(RightParen);
				setState(1229);
				statement();
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
	public static class IterationStatementContext extends ParserRuleContext {
		public TerminalNode While() { return getToken(CParser.While, 0); }
		public TerminalNode LeftParen() { return getToken(CParser.LeftParen, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode RightParen() { return getToken(CParser.RightParen, 0); }
		public StatementContext statement() {
			return getRuleContext(StatementContext.class,0);
		}
		public TerminalNode Do() { return getToken(CParser.Do, 0); }
		public TerminalNode Semi() { return getToken(CParser.Semi, 0); }
		public TerminalNode For() { return getToken(CParser.For, 0); }
		public ForConditionContext forCondition() {
			return getRuleContext(ForConditionContext.class,0);
		}
		public IterationStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_iterationStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterIterationStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitIterationStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitIterationStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IterationStatementContext iterationStatement() throws RecognitionException {
		IterationStatementContext _localctx = new IterationStatementContext(_ctx, getState());
		enterRule(_localctx, 170, RULE_iterationStatement);
		try {
			setState(1253);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case While:
				enterOuterAlt(_localctx, 1);
				{
				setState(1233);
				match(While);
				setState(1234);
				match(LeftParen);
				setState(1235);
				expression();
				setState(1236);
				match(RightParen);
				setState(1237);
				statement();
				}
				break;
			case Do:
				enterOuterAlt(_localctx, 2);
				{
				setState(1239);
				match(Do);
				setState(1240);
				statement();
				setState(1241);
				match(While);
				setState(1242);
				match(LeftParen);
				setState(1243);
				expression();
				setState(1244);
				match(RightParen);
				setState(1245);
				match(Semi);
				}
				break;
			case For:
				enterOuterAlt(_localctx, 3);
				{
				setState(1247);
				match(For);
				setState(1248);
				match(LeftParen);
				setState(1249);
				forCondition();
				setState(1250);
				match(RightParen);
				setState(1251);
				statement();
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
	public static class ForConditionContext extends ParserRuleContext {
		public List<TerminalNode> Semi() { return getTokens(CParser.Semi); }
		public TerminalNode Semi(int i) {
			return getToken(CParser.Semi, i);
		}
		public ForDeclarationContext forDeclaration() {
			return getRuleContext(ForDeclarationContext.class,0);
		}
		public List<ForExpressionContext> forExpression() {
			return getRuleContexts(ForExpressionContext.class);
		}
		public ForExpressionContext forExpression(int i) {
			return getRuleContext(ForExpressionContext.class,i);
		}
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public ForConditionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_forCondition; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterForCondition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitForCondition(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitForCondition(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ForConditionContext forCondition() throws RecognitionException {
		ForConditionContext _localctx = new ForConditionContext(_ctx, getState());
		enterRule(_localctx, 172, RULE_forCondition);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1259);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,148,_ctx) ) {
			case 1:
				{
				setState(1255);
				forDeclaration();
				}
				break;
			case 2:
				{
				setState(1257);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,147,_ctx) ) {
				case 1:
					{
					setState(1256);
					expression();
					}
					break;
				}
				}
				break;
			}
			setState(1261);
			match(Semi);
			setState(1263);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,149,_ctx) ) {
			case 1:
				{
				setState(1262);
				forExpression();
				}
				break;
			}
			setState(1265);
			match(Semi);
			setState(1267);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,150,_ctx) ) {
			case 1:
				{
				setState(1266);
				forExpression();
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
	public static class ForDeclarationContext extends ParserRuleContext {
		public DeclarationSpecifiersContext declarationSpecifiers() {
			return getRuleContext(DeclarationSpecifiersContext.class,0);
		}
		public InitDeclaratorListContext initDeclaratorList() {
			return getRuleContext(InitDeclaratorListContext.class,0);
		}
		public ForDeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_forDeclaration; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterForDeclaration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitForDeclaration(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitForDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ForDeclarationContext forDeclaration() throws RecognitionException {
		ForDeclarationContext _localctx = new ForDeclarationContext(_ctx, getState());
		enterRule(_localctx, 174, RULE_forDeclaration);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1269);
			declarationSpecifiers();
			setState(1271);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 463618L) != 0) || ((((_la - 85)) & ~0x3f) == 0 && ((1L << (_la - 85)) & 70368752631809L) != 0)) {
				{
				setState(1270);
				initDeclaratorList();
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
	public static class ForExpressionContext extends ParserRuleContext {
		public List<AssignmentExpressionContext> assignmentExpression() {
			return getRuleContexts(AssignmentExpressionContext.class);
		}
		public AssignmentExpressionContext assignmentExpression(int i) {
			return getRuleContext(AssignmentExpressionContext.class,i);
		}
		public List<TerminalNode> Comma() { return getTokens(CParser.Comma); }
		public TerminalNode Comma(int i) {
			return getToken(CParser.Comma, i);
		}
		public ForExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_forExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterForExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitForExpression(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitForExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ForExpressionContext forExpression() throws RecognitionException {
		ForExpressionContext _localctx = new ForExpressionContext(_ctx, getState());
		enterRule(_localctx, 176, RULE_forExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1273);
			assignmentExpression();
			setState(1278);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==Comma) {
				{
				{
				setState(1274);
				match(Comma);
				setState(1275);
				assignmentExpression();
				}
				}
				setState(1280);
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
	public static class JumpStatementContext extends ParserRuleContext {
		public TerminalNode Semi() { return getToken(CParser.Semi, 0); }
		public TerminalNode Goto() { return getToken(CParser.Goto, 0); }
		public TerminalNode Identifier() { return getToken(CParser.Identifier, 0); }
		public TerminalNode Continue() { return getToken(CParser.Continue, 0); }
		public TerminalNode Break() { return getToken(CParser.Break, 0); }
		public TerminalNode Return() { return getToken(CParser.Return, 0); }
		public UnaryExpressionContext unaryExpression() {
			return getRuleContext(UnaryExpressionContext.class,0);
		}
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public JumpStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_jumpStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterJumpStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitJumpStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitJumpStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final JumpStatementContext jumpStatement() throws RecognitionException {
		JumpStatementContext _localctx = new JumpStatementContext(_ctx, getState());
		enterRule(_localctx, 178, RULE_jumpStatement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1291);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,154,_ctx) ) {
			case 1:
				{
				setState(1281);
				match(Goto);
				setState(1282);
				match(Identifier);
				}
				break;
			case 2:
				{
				setState(1283);
				match(Continue);
				}
				break;
			case 3:
				{
				setState(1284);
				match(Break);
				}
				break;
			case 4:
				{
				setState(1285);
				match(Return);
				setState(1287);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,153,_ctx) ) {
				case 1:
					{
					setState(1286);
					expression();
					}
					break;
				}
				}
				break;
			case 5:
				{
				setState(1289);
				match(Goto);
				setState(1290);
				unaryExpression();
				}
				break;
			}
			setState(1293);
			match(Semi);
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
	public static class TranslationUnitContext extends ParserRuleContext {
		public List<ExternalDeclarationContext> externalDeclaration() {
			return getRuleContexts(ExternalDeclarationContext.class);
		}
		public ExternalDeclarationContext externalDeclaration(int i) {
			return getRuleContext(ExternalDeclarationContext.class,i);
		}
		public TranslationUnitContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_translationUnit; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterTranslationUnit(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitTranslationUnit(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitTranslationUnit(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TranslationUnitContext translationUnit() throws RecognitionException {
		TranslationUnitContext _localctx = new TranslationUnitContext(_ctx, getState());
		enterRule(_localctx, 180, RULE_translationUnit);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1296); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(1295);
					externalDeclaration();
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(1298); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,155,_ctx);
			} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
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
	public static class ExternalDeclarationContext extends ParserRuleContext {
		public FunctionDefinitionContext functionDefinition() {
			return getRuleContext(FunctionDefinitionContext.class,0);
		}
		public DeclarationContext declaration() {
			return getRuleContext(DeclarationContext.class,0);
		}
		public TerminalNode Semi() { return getToken(CParser.Semi, 0); }
		public AsmDefinitionContext asmDefinition() {
			return getRuleContext(AsmDefinitionContext.class,0);
		}
		public TerminalNode KW__extension__() { return getToken(CParser.KW__extension__, 0); }
		public ExternalDeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_externalDeclaration; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterExternalDeclaration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitExternalDeclaration(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitExternalDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExternalDeclarationContext externalDeclaration() throws RecognitionException {
		ExternalDeclarationContext _localctx = new ExternalDeclarationContext(_ctx, getState());
		enterRule(_localctx, 182, RULE_externalDeclaration);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1301);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,156,_ctx) ) {
			case 1:
				{
				setState(1300);
				match(KW__extension__);
				}
				break;
			}
			setState(1307);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,157,_ctx) ) {
			case 1:
				{
				setState(1303);
				functionDefinition();
				}
				break;
			case 2:
				{
				setState(1304);
				declaration();
				}
				break;
			case 3:
				{
				setState(1305);
				match(Semi);
				}
				break;
			case 4:
				{
				setState(1306);
				asmDefinition();
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
	public static class FunctionDefinitionContext extends ParserRuleContext {
		public DeclaratorContext declarator() {
			return getRuleContext(DeclaratorContext.class,0);
		}
		public FunctionBodyContext functionBody() {
			return getRuleContext(FunctionBodyContext.class,0);
		}
		public AttributeSpecifierSequenceContext attributeSpecifierSequence() {
			return getRuleContext(AttributeSpecifierSequenceContext.class,0);
		}
		public DeclarationSpecifiersContext declarationSpecifiers() {
			return getRuleContext(DeclarationSpecifiersContext.class,0);
		}
		public DeclarationListContext declarationList() {
			return getRuleContext(DeclarationListContext.class,0);
		}
		public FunctionDefinitionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_functionDefinition; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterFunctionDefinition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitFunctionDefinition(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitFunctionDefinition(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FunctionDefinitionContext functionDefinition() throws RecognitionException {
		FunctionDefinitionContext _localctx = new FunctionDefinitionContext(_ctx, getState());
		enterRule(_localctx, 184, RULE_functionDefinition);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1310);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,158,_ctx) ) {
			case 1:
				{
				setState(1309);
				attributeSpecifierSequence();
				}
				break;
			}
			setState(1313);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,159,_ctx) ) {
			case 1:
				{
				setState(1312);
				declarationSpecifiers();
				}
				break;
			}
			setState(1315);
			declarator();
			setState(1317);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,160,_ctx) ) {
			case 1:
				{
				setState(1316);
				declarationList();
				}
				break;
			}
			setState(1319);
			functionBody();
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
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterDeclarationList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitDeclarationList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitDeclarationList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DeclarationListContext declarationList() throws RecognitionException {
		DeclarationListContext _localctx = new DeclarationListContext(_ctx, getState());
		enterRule(_localctx, 186, RULE_declarationList);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1322); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(1321);
					declaration();
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(1324); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,161,_ctx);
			} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
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
	public static class FunctionBodyContext extends ParserRuleContext {
		public CompoundStatementContext compoundStatement() {
			return getRuleContext(CompoundStatementContext.class,0);
		}
		public FunctionBodyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_functionBody; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterFunctionBody(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitFunctionBody(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitFunctionBody(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FunctionBodyContext functionBody() throws RecognitionException {
		FunctionBodyContext _localctx = new FunctionBodyContext(_ctx, getState());
		enterRule(_localctx, 188, RULE_functionBody);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1326);
			compoundStatement();
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
	public static class IdentifierListContext extends ParserRuleContext {
		public List<TerminalNode> Identifier() { return getTokens(CParser.Identifier); }
		public TerminalNode Identifier(int i) {
			return getToken(CParser.Identifier, i);
		}
		public List<TerminalNode> Comma() { return getTokens(CParser.Comma); }
		public TerminalNode Comma(int i) {
			return getToken(CParser.Comma, i);
		}
		public IdentifierListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_identifierList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterIdentifierList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitIdentifierList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitIdentifierList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IdentifierListContext identifierList() throws RecognitionException {
		IdentifierListContext _localctx = new IdentifierListContext(_ctx, getState());
		enterRule(_localctx, 190, RULE_identifierList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1328);
			match(Identifier);
			setState(1333);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==Comma) {
				{
				{
				setState(1329);
				match(Comma);
				setState(1330);
				match(Identifier);
				}
				}
				setState(1335);
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
	public static class GnuArrayDesignatorContext extends ParserRuleContext {
		public TerminalNode LeftBracket() { return getToken(CParser.LeftBracket, 0); }
		public List<ConstantExpressionContext> constantExpression() {
			return getRuleContexts(ConstantExpressionContext.class);
		}
		public ConstantExpressionContext constantExpression(int i) {
			return getRuleContext(ConstantExpressionContext.class,i);
		}
		public TerminalNode RightBracket() { return getToken(CParser.RightBracket, 0); }
		public TerminalNode Ellipsis() { return getToken(CParser.Ellipsis, 0); }
		public GnuArrayDesignatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_gnuArrayDesignator; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterGnuArrayDesignator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitGnuArrayDesignator(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitGnuArrayDesignator(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GnuArrayDesignatorContext gnuArrayDesignator() throws RecognitionException {
		GnuArrayDesignatorContext _localctx = new GnuArrayDesignatorContext(_ctx, getState());
		enterRule(_localctx, 192, RULE_gnuArrayDesignator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1336);
			match(LeftBracket);
			setState(1337);
			constantExpression();
			setState(1340);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==Ellipsis) {
				{
				setState(1338);
				match(Ellipsis);
				setState(1339);
				constantExpression();
				}
			}

			setState(1342);
			match(RightBracket);
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
	public static class GnuIdentifierContext extends ParserRuleContext {
		public TerminalNode Identifier() { return getToken(CParser.Identifier, 0); }
		public GnuIdentifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_gnuIdentifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterGnuIdentifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitGnuIdentifier(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitGnuIdentifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GnuIdentifierContext gnuIdentifier() throws RecognitionException {
		GnuIdentifierContext _localctx = new GnuIdentifierContext(_ctx, getState());
		enterRule(_localctx, 194, RULE_gnuIdentifier);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1344);
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
	public static class AsmArgumentContext extends ParserRuleContext {
		public AsmStringLiteralContext asmStringLiteral() {
			return getRuleContext(AsmStringLiteralContext.class,0);
		}
		public List<TerminalNode> Colon() { return getTokens(CParser.Colon); }
		public TerminalNode Colon(int i) {
			return getToken(CParser.Colon, i);
		}
		public List<AsmOperandsContext> asmOperands() {
			return getRuleContexts(AsmOperandsContext.class);
		}
		public AsmOperandsContext asmOperands(int i) {
			return getRuleContext(AsmOperandsContext.class,i);
		}
		public List<AsmClobbersContext> asmClobbers() {
			return getRuleContexts(AsmClobbersContext.class);
		}
		public AsmClobbersContext asmClobbers(int i) {
			return getRuleContext(AsmClobbersContext.class,i);
		}
		public AsmArgumentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_asmArgument; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterAsmArgument(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitAsmArgument(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitAsmArgument(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AsmArgumentContext asmArgument() throws RecognitionException {
		AsmArgumentContext _localctx = new AsmArgumentContext(_ctx, getState());
		enterRule(_localctx, 196, RULE_asmArgument);
		int _la;
		try {
			setState(1367);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,169,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1346);
				asmStringLiteral();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1347);
				asmStringLiteral();
				setState(1348);
				match(Colon);
				setState(1350);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LeftBracket || _la==StringLiteral) {
					{
					setState(1349);
					asmOperands();
					}
				}

				setState(1365);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==Colon) {
					{
					setState(1352);
					match(Colon);
					setState(1354);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==LeftBracket || _la==StringLiteral) {
						{
						setState(1353);
						asmOperands();
						}
					}

					setState(1362);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==Colon) {
						{
						{
						setState(1356);
						match(Colon);
						setState(1358);
						_errHandler.sync(this);
						_la = _input.LA(1);
						if (_la==Identifier || _la==StringLiteral) {
							{
							setState(1357);
							asmClobbers();
							}
						}

						}
						}
						setState(1364);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
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
	public static class AsmClobbersContext extends ParserRuleContext {
		public List<AsmStringLiteralContext> asmStringLiteral() {
			return getRuleContexts(AsmStringLiteralContext.class);
		}
		public AsmStringLiteralContext asmStringLiteral(int i) {
			return getRuleContext(AsmStringLiteralContext.class,i);
		}
		public List<TerminalNode> Identifier() { return getTokens(CParser.Identifier); }
		public TerminalNode Identifier(int i) {
			return getToken(CParser.Identifier, i);
		}
		public List<TerminalNode> Comma() { return getTokens(CParser.Comma); }
		public TerminalNode Comma(int i) {
			return getToken(CParser.Comma, i);
		}
		public AsmClobbersContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_asmClobbers; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterAsmClobbers(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitAsmClobbers(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitAsmClobbers(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AsmClobbersContext asmClobbers() throws RecognitionException {
		AsmClobbersContext _localctx = new AsmClobbersContext(_ctx, getState());
		enterRule(_localctx, 198, RULE_asmClobbers);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1371);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case StringLiteral:
				{
				setState(1369);
				asmStringLiteral();
				}
				break;
			case Identifier:
				{
				setState(1370);
				match(Identifier);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(1380);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==Comma) {
				{
				{
				setState(1373);
				match(Comma);
				setState(1376);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case StringLiteral:
					{
					setState(1374);
					asmStringLiteral();
					}
					break;
				case Identifier:
					{
					setState(1375);
					match(Identifier);
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				}
				setState(1382);
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
	public static class AsmDefinitionContext extends ParserRuleContext {
		public SimpleAsmExprContext simpleAsmExpr() {
			return getRuleContext(SimpleAsmExprContext.class,0);
		}
		public TerminalNode Asm() { return getToken(CParser.Asm, 0); }
		public TerminalNode LeftParen() { return getToken(CParser.LeftParen, 0); }
		public ToplevelAsmArgumentContext toplevelAsmArgument() {
			return getRuleContext(ToplevelAsmArgumentContext.class,0);
		}
		public TerminalNode RightParen() { return getToken(CParser.RightParen, 0); }
		public AsmDefinitionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_asmDefinition; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterAsmDefinition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitAsmDefinition(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitAsmDefinition(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AsmDefinitionContext asmDefinition() throws RecognitionException {
		AsmDefinitionContext _localctx = new AsmDefinitionContext(_ctx, getState());
		enterRule(_localctx, 200, RULE_asmDefinition);
		try {
			setState(1389);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,173,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1383);
				simpleAsmExpr();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1384);
				match(Asm);
				setState(1385);
				match(LeftParen);
				setState(1386);
				toplevelAsmArgument();
				setState(1387);
				match(RightParen);
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
	public static class ToplevelAsmArgumentContext extends ParserRuleContext {
		public AsmStringLiteralContext asmStringLiteral() {
			return getRuleContext(AsmStringLiteralContext.class,0);
		}
		public List<TerminalNode> Colon() { return getTokens(CParser.Colon); }
		public TerminalNode Colon(int i) {
			return getToken(CParser.Colon, i);
		}
		public List<AsmOperandsContext> asmOperands() {
			return getRuleContexts(AsmOperandsContext.class);
		}
		public AsmOperandsContext asmOperands(int i) {
			return getRuleContext(AsmOperandsContext.class,i);
		}
		public ToplevelAsmArgumentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_toplevelAsmArgument; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterToplevelAsmArgument(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitToplevelAsmArgument(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitToplevelAsmArgument(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ToplevelAsmArgumentContext toplevelAsmArgument() throws RecognitionException {
		ToplevelAsmArgumentContext _localctx = new ToplevelAsmArgumentContext(_ctx, getState());
		enterRule(_localctx, 202, RULE_toplevelAsmArgument);
		int _la;
		try {
			setState(1406);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,177,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(1391);
				asmStringLiteral();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(1392);
				asmStringLiteral();
				setState(1393);
				match(Colon);
				setState(1395);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LeftBracket || _la==StringLiteral) {
					{
					setState(1394);
					asmOperands();
					}
				}

				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(1397);
				asmStringLiteral();
				setState(1398);
				match(Colon);
				setState(1400);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LeftBracket || _la==StringLiteral) {
					{
					setState(1399);
					asmOperands();
					}
				}

				setState(1402);
				match(Colon);
				setState(1404);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LeftBracket || _la==StringLiteral) {
					{
					setState(1403);
					asmOperands();
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
	public static class AsmOperandContext extends ParserRuleContext {
		public AsmStringLiteralContext asmStringLiteral() {
			return getRuleContext(AsmStringLiteralContext.class,0);
		}
		public TerminalNode LeftParen() { return getToken(CParser.LeftParen, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode RightParen() { return getToken(CParser.RightParen, 0); }
		public TerminalNode LeftBracket() { return getToken(CParser.LeftBracket, 0); }
		public TerminalNode Identifier() { return getToken(CParser.Identifier, 0); }
		public TerminalNode RightBracket() { return getToken(CParser.RightBracket, 0); }
		public AsmOperandContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_asmOperand; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterAsmOperand(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitAsmOperand(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitAsmOperand(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AsmOperandContext asmOperand() throws RecognitionException {
		AsmOperandContext _localctx = new AsmOperandContext(_ctx, getState());
		enterRule(_localctx, 204, RULE_asmOperand);
		try {
			setState(1421);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case StringLiteral:
				enterOuterAlt(_localctx, 1);
				{
				setState(1408);
				asmStringLiteral();
				setState(1409);
				match(LeftParen);
				setState(1410);
				expression();
				setState(1411);
				match(RightParen);
				}
				break;
			case LeftBracket:
				enterOuterAlt(_localctx, 2);
				{
				setState(1413);
				match(LeftBracket);
				setState(1414);
				match(Identifier);
				setState(1415);
				match(RightBracket);
				setState(1416);
				asmStringLiteral();
				setState(1417);
				match(LeftParen);
				setState(1418);
				expression();
				setState(1419);
				match(RightParen);
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
	public static class AsmOperandsContext extends ParserRuleContext {
		public List<AsmOperandContext> asmOperand() {
			return getRuleContexts(AsmOperandContext.class);
		}
		public AsmOperandContext asmOperand(int i) {
			return getRuleContext(AsmOperandContext.class,i);
		}
		public List<TerminalNode> Comma() { return getTokens(CParser.Comma); }
		public TerminalNode Comma(int i) {
			return getToken(CParser.Comma, i);
		}
		public AsmOperandsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_asmOperands; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterAsmOperands(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitAsmOperands(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitAsmOperands(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AsmOperandsContext asmOperands() throws RecognitionException {
		AsmOperandsContext _localctx = new AsmOperandsContext(_ctx, getState());
		enterRule(_localctx, 206, RULE_asmOperands);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1423);
			asmOperand();
			setState(1428);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==Comma) {
				{
				{
				setState(1424);
				match(Comma);
				setState(1425);
				asmOperand();
				}
				}
				setState(1430);
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
	public static class AsmQualifierContext extends ParserRuleContext {
		public TerminalNode Volatile() { return getToken(CParser.Volatile, 0); }
		public TerminalNode Inline() { return getToken(CParser.Inline, 0); }
		public TerminalNode Goto() { return getToken(CParser.Goto, 0); }
		public AsmQualifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_asmQualifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterAsmQualifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitAsmQualifier(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitAsmQualifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AsmQualifierContext asmQualifier() throws RecognitionException {
		AsmQualifierContext _localctx = new AsmQualifierContext(_ctx, getState());
		enterRule(_localctx, 208, RULE_asmQualifier);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1431);
			_la = _input.LA(1);
			if ( !(((((_la - 48)) & ~0x3f) == 0 && ((1L << (_la - 48)) & 16777221L) != 0)) ) {
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
	public static class AsmQualifierListContext extends ParserRuleContext {
		public List<AsmQualifierContext> asmQualifier() {
			return getRuleContexts(AsmQualifierContext.class);
		}
		public AsmQualifierContext asmQualifier(int i) {
			return getRuleContext(AsmQualifierContext.class,i);
		}
		public AsmQualifierListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_asmQualifierList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterAsmQualifierList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitAsmQualifierList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitAsmQualifierList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AsmQualifierListContext asmQualifierList() throws RecognitionException {
		AsmQualifierListContext _localctx = new AsmQualifierListContext(_ctx, getState());
		enterRule(_localctx, 210, RULE_asmQualifierList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1434); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(1433);
				asmQualifier();
				}
				}
				setState(1436); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( ((((_la - 48)) & ~0x3f) == 0 && ((1L << (_la - 48)) & 16777221L) != 0) );
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
	public static class AsmStatementContext extends ParserRuleContext {
		public TerminalNode Asm() { return getToken(CParser.Asm, 0); }
		public TerminalNode LeftParen() { return getToken(CParser.LeftParen, 0); }
		public AsmArgumentContext asmArgument() {
			return getRuleContext(AsmArgumentContext.class,0);
		}
		public TerminalNode RightParen() { return getToken(CParser.RightParen, 0); }
		public TerminalNode Semi() { return getToken(CParser.Semi, 0); }
		public AsmQualifierListContext asmQualifierList() {
			return getRuleContext(AsmQualifierListContext.class,0);
		}
		public AsmStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_asmStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterAsmStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitAsmStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitAsmStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AsmStatementContext asmStatement() throws RecognitionException {
		AsmStatementContext _localctx = new AsmStatementContext(_ctx, getState());
		enterRule(_localctx, 212, RULE_asmStatement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1438);
			match(Asm);
			setState(1440);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (((((_la - 48)) & ~0x3f) == 0 && ((1L << (_la - 48)) & 16777221L) != 0)) {
				{
				setState(1439);
				asmQualifierList();
				}
			}

			setState(1442);
			match(LeftParen);
			setState(1443);
			asmArgument();
			setState(1444);
			match(RightParen);
			setState(1445);
			match(Semi);
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
	public static class AsmStringLiteralContext extends ParserRuleContext {
		public TerminalNode StringLiteral() { return getToken(CParser.StringLiteral, 0); }
		public AsmStringLiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_asmStringLiteral; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterAsmStringLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitAsmStringLiteral(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitAsmStringLiteral(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AsmStringLiteralContext asmStringLiteral() throws RecognitionException {
		AsmStringLiteralContext _localctx = new AsmStringLiteralContext(_ctx, getState());
		enterRule(_localctx, 214, RULE_asmStringLiteral);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1447);
			match(StringLiteral);
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
	public static class GccDeclaratorExtensionContext extends ParserRuleContext {
		public AsmDefinitionContext asmDefinition() {
			return getRuleContext(AsmDefinitionContext.class,0);
		}
		public GnuAttributeContext gnuAttribute() {
			return getRuleContext(GnuAttributeContext.class,0);
		}
		public GccDeclaratorExtensionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_gccDeclaratorExtension; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterGccDeclaratorExtension(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitGccDeclaratorExtension(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitGccDeclaratorExtension(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GccDeclaratorExtensionContext gccDeclaratorExtension() throws RecognitionException {
		GccDeclaratorExtensionContext _localctx = new GccDeclaratorExtensionContext(_ctx, getState());
		enterRule(_localctx, 216, RULE_gccDeclaratorExtension);
		try {
			setState(1451);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case Asm:
				enterOuterAlt(_localctx, 1);
				{
				setState(1449);
				asmDefinition();
				}
				break;
			case Attribute:
				enterOuterAlt(_localctx, 2);
				{
				setState(1450);
				gnuAttribute();
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
	public static class GnuAttributeContext extends ParserRuleContext {
		public TerminalNode Attribute() { return getToken(CParser.Attribute, 0); }
		public List<TerminalNode> LeftParen() { return getTokens(CParser.LeftParen); }
		public TerminalNode LeftParen(int i) {
			return getToken(CParser.LeftParen, i);
		}
		public GnuAttributeListContext gnuAttributeList() {
			return getRuleContext(GnuAttributeListContext.class,0);
		}
		public List<TerminalNode> RightParen() { return getTokens(CParser.RightParen); }
		public TerminalNode RightParen(int i) {
			return getToken(CParser.RightParen, i);
		}
		public GnuAttributeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_gnuAttribute; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterGnuAttribute(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitGnuAttribute(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitGnuAttribute(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GnuAttributeContext gnuAttribute() throws RecognitionException {
		GnuAttributeContext _localctx = new GnuAttributeContext(_ctx, getState());
		enterRule(_localctx, 218, RULE_gnuAttribute);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1453);
			match(Attribute);
			setState(1454);
			match(LeftParen);
			setState(1455);
			match(LeftParen);
			setState(1456);
			gnuAttributeList();
			setState(1457);
			match(RightParen);
			setState(1458);
			match(RightParen);
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
	public static class GnuAttributeListContext extends ParserRuleContext {
		public List<GnuSingleAttributeContext> gnuSingleAttribute() {
			return getRuleContexts(GnuSingleAttributeContext.class);
		}
		public GnuSingleAttributeContext gnuSingleAttribute(int i) {
			return getRuleContext(GnuSingleAttributeContext.class,i);
		}
		public GnuAttributeListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_gnuAttributeList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterGnuAttributeList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitGnuAttributeList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitGnuAttributeList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GnuAttributeListContext gnuAttributeList() throws RecognitionException {
		GnuAttributeListContext _localctx = new GnuAttributeListContext(_ctx, getState());
		enterRule(_localctx, 220, RULE_gnuAttributeList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1463);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & -2L) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & -4194305L) != 0) || ((((_la - 128)) & ~0x3f) == 0 && ((1L << (_la - 128)) & 65535L) != 0)) {
				{
				{
				setState(1460);
				gnuSingleAttribute();
				}
				}
				setState(1465);
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
	public static class GnuAttributesContext extends ParserRuleContext {
		public List<GnuAttributeContext> gnuAttribute() {
			return getRuleContexts(GnuAttributeContext.class);
		}
		public GnuAttributeContext gnuAttribute(int i) {
			return getRuleContext(GnuAttributeContext.class,i);
		}
		public GnuAttributesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_gnuAttributes; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterGnuAttributes(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitGnuAttributes(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitGnuAttributes(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GnuAttributesContext gnuAttributes() throws RecognitionException {
		GnuAttributesContext _localctx = new GnuAttributesContext(_ctx, getState());
		enterRule(_localctx, 222, RULE_gnuAttributes);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(1467); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(1466);
					gnuAttribute();
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(1469); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,184,_ctx);
			} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
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
	public static class GnuSingleAttributeContext extends ParserRuleContext {
		public TerminalNode LeftParen() { return getToken(CParser.LeftParen, 0); }
		public TerminalNode RightParen() { return getToken(CParser.RightParen, 0); }
		public GnuAttributeListContext gnuAttributeList() {
			return getRuleContext(GnuAttributeListContext.class,0);
		}
		public GnuSingleAttributeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_gnuSingleAttribute; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterGnuSingleAttribute(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitGnuSingleAttribute(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitGnuSingleAttribute(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GnuSingleAttributeContext gnuSingleAttribute() throws RecognitionException {
		GnuSingleAttributeContext _localctx = new GnuSingleAttributeContext(_ctx, getState());
		enterRule(_localctx, 224, RULE_gnuSingleAttribute);
		int _la;
		try {
			setState(1476);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case Attribute:
			case KW__builtin_offsetof:
			case KW__builtin_va_arg:
			case KW__builtin_choose_expr:
			case KW__builtin_types_compatible_p:
			case KW__builtin_tgmath:
			case KW__builtin_complex:
			case KW__cdecl:
			case KW__clrcall:
			case KW__declspec:
			case KW__extension__:
			case KW__fastcall:
			case KW__m128:
			case KW__m128d:
			case KW__m128i:
			case KW__stdcall:
			case KW__thiscall:
			case KW__vectorcall:
			case KW__real__:
			case KW__imag__:
			case KW__func__:
			case KW__FUNCTION__:
			case KW__PRETTY_FUNCTION__:
			case Alignas:
			case Alignof:
			case Maxof:
			case Minof:
			case Countof:
			case Asm:
			case Auto:
			case Bool:
			case Break:
			case Case:
			case Char:
			case Const:
			case Constexpr:
			case Continue:
			case Default:
			case Deprecated:
			case Do:
			case Double:
			case Else:
			case Enum:
			case Extern:
			case False_:
			case Float:
			case For:
			case Goto:
			case If:
			case Inline:
			case Int:
			case Label:
			case Long:
			case Nulptr:
			case Register:
			case Restrict:
			case Return:
			case Short:
			case Signed:
			case Sizeof:
			case Static:
			case Static_assert:
			case Struct:
			case Switch:
			case True_:
			case Typedef:
			case Typeof:
			case Typeof_unqual:
			case Union:
			case Unsigned:
			case Void:
			case Volatile:
			case While:
			case Atomic:
			case BitInt:
			case Complex:
			case Decimal128:
			case Decimal32:
			case Decimal64:
			case Generic:
			case Imaginary:
			case Noreturn:
			case StaticAssert:
			case ThreadLocal:
			case LeftBracket:
			case RightBracket:
			case LeftBrace:
			case RightBrace:
			case Less:
			case LessEqual:
			case Greater:
			case GreaterEqual:
			case LeftShift:
			case RightShift:
			case Plus:
			case PlusPlus:
			case Minus:
			case MinusMinus:
			case Star:
			case Div:
			case Mod:
			case And:
			case Or:
			case AndAnd:
			case OrOr:
			case Caret:
			case Not:
			case Tilde:
			case Question:
			case Colon:
			case Semi:
			case Comma:
			case Assign:
			case StarAssign:
			case DivAssign:
			case ModAssign:
			case PlusAssign:
			case MinusAssign:
			case LeftShiftAssign:
			case RightShiftAssign:
			case AndAssign:
			case XorAssign:
			case OrAssign:
			case Equal:
			case NotEqual:
			case Arrow:
			case Dot:
			case Ellipsis:
			case Identifier:
			case IntegerConstant:
			case FloatingConstant:
			case DigitSequence:
			case CharacterConstant:
			case StringLiteral:
			case MultiLineMacro:
			case LineDirective:
			case Directive:
			case Whitespace:
			case Newline:
			case BlockComment:
			case LineComment:
				enterOuterAlt(_localctx, 1);
				{
				setState(1471);
				_la = _input.LA(1);
				if ( _la <= 0 || (_la==LeftParen || _la==RightParen) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case LeftParen:
				enterOuterAlt(_localctx, 2);
				{
				setState(1472);
				match(LeftParen);
				setState(1473);
				gnuAttributeList();
				setState(1474);
				match(RightParen);
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
	public static class SimpleAsmExprContext extends ParserRuleContext {
		public TerminalNode Asm() { return getToken(CParser.Asm, 0); }
		public TerminalNode LeftParen() { return getToken(CParser.LeftParen, 0); }
		public AsmStringLiteralContext asmStringLiteral() {
			return getRuleContext(AsmStringLiteralContext.class,0);
		}
		public TerminalNode RightParen() { return getToken(CParser.RightParen, 0); }
		public SimpleAsmExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_simpleAsmExpr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterSimpleAsmExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitSimpleAsmExpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitSimpleAsmExpr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SimpleAsmExprContext simpleAsmExpr() throws RecognitionException {
		SimpleAsmExprContext _localctx = new SimpleAsmExprContext(_ctx, getState());
		enterRule(_localctx, 226, RULE_simpleAsmExpr);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1478);
			match(Asm);
			setState(1479);
			match(LeftParen);
			setState(1480);
			asmStringLiteral();
			setState(1481);
			match(RightParen);
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
	public static class VcSpecificModiferContext extends ParserRuleContext {
		public TerminalNode KW__cdecl() { return getToken(CParser.KW__cdecl, 0); }
		public TerminalNode KW__clrcall() { return getToken(CParser.KW__clrcall, 0); }
		public TerminalNode KW__stdcall() { return getToken(CParser.KW__stdcall, 0); }
		public TerminalNode KW__fastcall() { return getToken(CParser.KW__fastcall, 0); }
		public TerminalNode KW__thiscall() { return getToken(CParser.KW__thiscall, 0); }
		public TerminalNode KW__vectorcall() { return getToken(CParser.KW__vectorcall, 0); }
		public VcSpecificModiferContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_vcSpecificModifer; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).enterVcSpecificModifer(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CParserListener ) ((CParserListener)listener).exitVcSpecificModifer(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CParserVisitor ) return ((CParserVisitor<? extends T>)visitor).visitVcSpecificModifer(this);
			else return visitor.visitChildren(this);
		}
	}

	public final VcSpecificModiferContext vcSpecificModifer() throws RecognitionException {
		VcSpecificModiferContext _localctx = new VcSpecificModiferContext(_ctx, getState());
		enterRule(_localctx, 228, RULE_vcSpecificModifer);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(1483);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 463616L) != 0)) ) {
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

	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 11:
			return unaryExpression_sempred((UnaryExpressionContext)_localctx, predIndex);
		case 12:
			return castExpression_sempred((CastExpressionContext)_localctx, predIndex);
		case 27:
			return declaration_sempred((DeclarationContext)_localctx, predIndex);
		case 28:
			return declarationSpecifiers_sempred((DeclarationSpecifiersContext)_localctx, predIndex);
		case 35:
			return structOrUnionSpecifier_sempred((StructOrUnionSpecifierContext)_localctx, predIndex);
		case 39:
			return specifierQualifierList_sempred((SpecifierQualifierListContext)_localctx, predIndex);
		case 59:
			return parameterDeclaration_sempred((ParameterDeclarationContext)_localctx, predIndex);
		case 62:
			return directAbstractDeclarator_sempred((DirectAbstractDeclaratorContext)_localctx, predIndex);
		case 63:
			return typedefName_sempred((TypedefNameContext)_localctx, predIndex);
		case 82:
			return blockItem_sempred((BlockItemContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean unaryExpression_sempred(UnaryExpressionContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return !this.IsSomethingOfTypeName();
		case 1:
			return this.IsSomethingOfTypeName();
		case 2:
			return this.IsSomethingOfTypeName();
		case 3:
			return !this.IsSomethingOfTypeName();
		case 4:
			return this.IsSomethingOfTypeName();
		case 5:
			return !this.IsSomethingOfTypeName();
		case 6:
			return this.IsSomethingOfTypeName();
		case 7:
			return this.IsSomethingOfTypeName();
		}
		return true;
	}
	private boolean castExpression_sempred(CastExpressionContext _localctx, int predIndex) {
		switch (predIndex) {
		case 8:
			return this.IsCast();
		}
		return true;
	}
	private boolean declaration_sempred(DeclarationContext _localctx, int predIndex) {
		switch (predIndex) {
		case 9:
			return this.IsInitDeclaratorList();
		}
		return true;
	}
	private boolean declarationSpecifiers_sempred(DeclarationSpecifiersContext _localctx, int predIndex) {
		switch (predIndex) {
		case 10:
			return  this.IsDeclarationSpecifier();
		}
		return true;
	}
	private boolean structOrUnionSpecifier_sempred(StructOrUnionSpecifierContext _localctx, int predIndex) {
		switch (predIndex) {
		case 11:
			return this.IsNullStructDeclarationListExtension();
		}
		return true;
	}
	private boolean specifierQualifierList_sempred(SpecifierQualifierListContext _localctx, int predIndex) {
		switch (predIndex) {
		case 12:
			return this.IsTypeSpecifierQualifier();
		}
		return true;
	}
	private boolean parameterDeclaration_sempred(ParameterDeclarationContext _localctx, int predIndex) {
		switch (predIndex) {
		case 13:
			return this.IsDeclarationSpecifier();
		}
		return true;
	}
	private boolean directAbstractDeclarator_sempred(DirectAbstractDeclaratorContext _localctx, int predIndex) {
		switch (predIndex) {
		case 14:
			return precpred(_ctx, 5);
		case 15:
			return precpred(_ctx, 4);
		case 16:
			return precpred(_ctx, 3);
		case 17:
			return precpred(_ctx, 2);
		case 18:
			return precpred(_ctx, 1);
		}
		return true;
	}
	private boolean typedefName_sempred(TypedefNameContext _localctx, int predIndex) {
		switch (predIndex) {
		case 19:
			return this.IsTypedefName();
		}
		return true;
	}
	private boolean blockItem_sempred(BlockItemContext _localctx, int predIndex) {
		switch (predIndex) {
		case 20:
			return this.IsStatement();
		case 21:
			return this.IsDeclaration();
		}
		return true;
	}

	public static final String _serializedATN =
		"\u0004\u0001\u008f\u05ce\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001"+
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
		"T\u0002U\u0007U\u0002V\u0007V\u0002W\u0007W\u0002X\u0007X\u0002Y\u0007"+
		"Y\u0002Z\u0007Z\u0002[\u0007[\u0002\\\u0007\\\u0002]\u0007]\u0002^\u0007"+
		"^\u0002_\u0007_\u0002`\u0007`\u0002a\u0007a\u0002b\u0007b\u0002c\u0007"+
		"c\u0002d\u0007d\u0002e\u0007e\u0002f\u0007f\u0002g\u0007g\u0002h\u0007"+
		"h\u0002i\u0007i\u0002j\u0007j\u0002k\u0007k\u0002l\u0007l\u0002m\u0007"+
		"m\u0002n\u0007n\u0002o\u0007o\u0002p\u0007p\u0002q\u0007q\u0002r\u0007"+
		"r\u0001\u0000\u0003\u0000\u00e8\b\u0000\u0001\u0000\u0001\u0000\u0001"+
		"\u0000\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0003\u0001\u00f1"+
		"\b\u0001\u0001\u0002\u0001\u0002\u0001\u0003\u0001\u0003\u0001\u0004\u0001"+
		"\u0004\u0001\u0004\u0001\u0004\u0004\u0004\u00fb\b\u0004\u000b\u0004\f"+
		"\u0004\u00fc\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004"+
		"\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0003\u0004\u0108\b\u0004"+
		"\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004"+
		"\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004"+
		"\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004"+
		"\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004"+
		"\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004"+
		"\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004"+
		"\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004"+
		"\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0003\u0004\u0138\b\u0004"+
		"\u0001\u0005\u0001\u0005\u0001\u0005\u0005\u0005\u013d\b\u0005\n\u0005"+
		"\f\u0005\u0140\t\u0005\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006"+
		"\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0007\u0001\u0007\u0001\u0007"+
		"\u0005\u0007\u014c\b\u0007\n\u0007\f\u0007\u014f\t\u0007\u0001\b\u0001"+
		"\b\u0003\b\u0153\b\b\u0001\b\u0001\b\u0001\b\u0001\t\u0001\t\u0003\t\u015a"+
		"\b\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0003\t\u0161\b\t\u0001\t"+
		"\u0003\t\u0164\b\t\u0001\t\u0001\t\u0003\t\u0168\b\t\u0001\t\u0001\t\u0001"+
		"\t\u0001\t\u0001\t\u0001\t\u0003\t\u0170\b\t\u0001\t\u0001\t\u0001\t\u0001"+
		"\t\u0001\t\u0005\t\u0177\b\t\n\t\f\t\u017a\t\t\u0001\n\u0001\n\u0001\n"+
		"\u0005\n\u017f\b\n\n\n\f\n\u0182\t\n\u0001\u000b\u0001\u000b\u0001\u000b"+
		"\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b"+
		"\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b"+
		"\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b"+
		"\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b"+
		"\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b"+
		"\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b"+
		"\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b"+
		"\u0001\u000b\u0001\u000b\u0001\u000b\u0003\u000b\u01b4\b\u000b\u0001\f"+
		"\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0003\f\u01be"+
		"\b\f\u0001\r\u0001\r\u0001\r\u0005\r\u01c3\b\r\n\r\f\r\u01c6\t\r\u0001"+
		"\u000e\u0001\u000e\u0001\u000e\u0005\u000e\u01cb\b\u000e\n\u000e\f\u000e"+
		"\u01ce\t\u000e\u0001\u000f\u0001\u000f\u0001\u000f\u0005\u000f\u01d3\b"+
		"\u000f\n\u000f\f\u000f\u01d6\t\u000f\u0001\u0010\u0001\u0010\u0001\u0010"+
		"\u0005\u0010\u01db\b\u0010\n\u0010\f\u0010\u01de\t\u0010\u0001\u0011\u0001"+
		"\u0011\u0001\u0011\u0005\u0011\u01e3\b\u0011\n\u0011\f\u0011\u01e6\t\u0011"+
		"\u0001\u0012\u0001\u0012\u0001\u0012\u0005\u0012\u01eb\b\u0012\n\u0012"+
		"\f\u0012\u01ee\t\u0012\u0001\u0013\u0001\u0013\u0001\u0013\u0005\u0013"+
		"\u01f3\b\u0013\n\u0013\f\u0013\u01f6\t\u0013\u0001\u0014\u0001\u0014\u0001"+
		"\u0014\u0005\u0014\u01fb\b\u0014\n\u0014\f\u0014\u01fe\t\u0014\u0001\u0015"+
		"\u0001\u0015\u0001\u0015\u0005\u0015\u0203\b\u0015\n\u0015\f\u0015\u0206"+
		"\t\u0015\u0001\u0016\u0001\u0016\u0001\u0016\u0005\u0016\u020b\b\u0016"+
		"\n\u0016\f\u0016\u020e\t\u0016\u0001\u0017\u0001\u0017\u0001\u0017\u0001"+
		"\u0017\u0001\u0017\u0001\u0017\u0003\u0017\u0216\b\u0017\u0001\u0018\u0001"+
		"\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0003\u0018\u021e"+
		"\b\u0018\u0001\u0019\u0001\u0019\u0001\u0019\u0005\u0019\u0223\b\u0019"+
		"\n\u0019\f\u0019\u0226\t\u0019\u0001\u001a\u0001\u001a\u0001\u001b\u0001"+
		"\u001b\u0001\u001b\u0001\u001b\u0003\u001b\u022e\b\u001b\u0001\u001b\u0001"+
		"\u001b\u0001\u001b\u0001\u001b\u0003\u001b\u0234\b\u001b\u0001\u001b\u0001"+
		"\u001b\u0001\u001c\u0001\u001c\u0004\u001c\u023a\b\u001c\u000b\u001c\f"+
		"\u001c\u023b\u0001\u001d\u0001\u001d\u0001\u001d\u0001\u001d\u0001\u001d"+
		"\u0003\u001d\u0243\b\u001d\u0001\u001e\u0001\u001e\u0001\u001e\u0005\u001e"+
		"\u0248\b\u001e\n\u001e\f\u001e\u024b\t\u001e\u0001\u001f\u0001\u001f\u0001"+
		"\u001f\u0003\u001f\u0250\b\u001f\u0001 \u0001 \u0001 \u0001!\u0001!\u0001"+
		"\"\u0001\"\u0001\"\u0001\"\u0001\"\u0001\"\u0001\"\u0001\"\u0001\"\u0001"+
		"\"\u0001\"\u0001\"\u0001\"\u0001\"\u0001\"\u0001\"\u0001\"\u0001\"\u0001"+
		"\"\u0001\"\u0001\"\u0001\"\u0003\"\u026d\b\"\u0001\"\u0001\"\u0003\"\u0271"+
		"\b\"\u0001#\u0001#\u0003#\u0275\b#\u0001#\u0003#\u0278\b#\u0001#\u0003"+
		"#\u027b\b#\u0001#\u0001#\u0001#\u0003#\u0280\b#\u0001#\u0001#\u0003#\u0284"+
		"\b#\u0001$\u0001$\u0001%\u0004%\u0289\b%\u000b%\f%\u028a\u0001&\u0003"+
		"&\u028e\b&\u0001&\u0001&\u0003&\u0292\b&\u0001&\u0001&\u0001&\u0001&\u0001"+
		"&\u0003&\u0299\b&\u0001\'\u0003\'\u029c\b\'\u0001\'\u0001\'\u0004\'\u02a0"+
		"\b\'\u000b\'\f\'\u02a1\u0001\'\u0003\'\u02a5\b\'\u0001(\u0001(\u0001("+
		"\u0003(\u02aa\b(\u0001)\u0001)\u0001)\u0003)\u02af\b)\u0001)\u0005)\u02b2"+
		"\b)\n)\f)\u02b5\t)\u0001*\u0001*\u0003*\u02b9\b*\u0001*\u0003*\u02bc\b"+
		"*\u0001*\u0001*\u0001*\u0003*\u02c1\b*\u0003*\u02c3\b*\u0001+\u0001+\u0003"+
		"+\u02c7\b+\u0001+\u0003+\u02ca\b+\u0001+\u0003+\u02cd\b+\u0001+\u0003"+
		"+\u02d0\b+\u0001+\u0001+\u0001+\u0003+\u02d5\b+\u0001+\u0001+\u0001+\u0001"+
		"+\u0001+\u0003+\u02dc\b+\u0003+\u02de\b+\u0001,\u0001,\u0001,\u0005,\u02e3"+
		"\b,\n,\f,\u02e6\t,\u0001-\u0001-\u0003-\u02ea\b-\u0001-\u0003-\u02ed\b"+
		"-\u0001-\u0001-\u0003-\u02f1\b-\u0001.\u0001.\u0001/\u0001/\u0001/\u0001"+
		"/\u0001/\u00010\u00010\u00010\u00010\u00010\u00011\u00011\u00031\u0301"+
		"\b1\u00012\u00012\u00013\u00013\u00013\u00013\u00013\u00013\u00013\u0001"+
		"3\u00013\u00013\u00013\u00033\u0310\b3\u00013\u00033\u0313\b3\u00013\u0003"+
		"3\u0316\b3\u00014\u00014\u00014\u00014\u00034\u031c\b4\u00014\u00014\u0001"+
		"5\u00035\u0321\b5\u00015\u00055\u0324\b5\n5\f5\u0327\t5\u00015\u00055"+
		"\u032a\b5\n5\f5\u032d\t5\u00015\u00015\u00055\u0331\b5\n5\f5\u0334\t5"+
		"\u00015\u00015\u00016\u00016\u00036\u033a\b6\u00016\u00016\u00016\u0001"+
		"6\u00016\u00016\u00016\u00016\u00016\u00016\u00016\u00016\u00016\u0001"+
		"6\u00016\u00016\u00036\u034c\b6\u00016\u00016\u00036\u0350\b6\u00016\u0003"+
		"6\u0353\b6\u00016\u00016\u00036\u0357\b6\u00016\u00016\u00016\u00036\u035c"+
		"\b6\u00016\u00016\u00016\u00036\u0361\b6\u00016\u00016\u00016\u00016\u0001"+
		"6\u00016\u00036\u0369\b6\u00016\u00016\u00036\u036d\b6\u00016\u00016\u0001"+
		"6\u00036\u0372\b6\u00016\u00016\u00016\u00016\u00036\u0378\b6\u00056\u037a"+
		"\b6\n6\f6\u037d\t6\u00017\u00017\u00037\u0381\b7\u00047\u0383\b7\u000b"+
		"7\f7\u0384\u00018\u00048\u0388\b8\u000b8\f8\u0389\u00019\u00019\u0001"+
		"9\u00039\u038f\b9\u00019\u00039\u0392\b9\u0001:\u0001:\u0001:\u0005:\u0397"+
		"\b:\n:\f:\u039a\t:\u0001;\u0003;\u039d\b;\u0001;\u0001;\u0001;\u0003;"+
		"\u03a2\b;\u0001;\u0001;\u0001;\u0003;\u03a7\b;\u0001<\u0001<\u0003<\u03ab"+
		"\b<\u0001=\u0003=\u03ae\b=\u0001=\u0001=\u0003=\u03b2\b=\u0001=\u0003"+
		"=\u03b5\b=\u0001=\u0001=\u0005=\u03b9\b=\n=\f=\u03bc\t=\u0003=\u03be\b"+
		"=\u0001>\u0001>\u0001>\u0001>\u0001>\u0005>\u03c5\b>\n>\f>\u03c8\t>\u0001"+
		">\u0001>\u0003>\u03cc\b>\u0001>\u0003>\u03cf\b>\u0001>\u0001>\u0001>\u0001"+
		">\u0003>\u03d5\b>\u0001>\u0001>\u0001>\u0001>\u0001>\u0001>\u0001>\u0001"+
		">\u0001>\u0001>\u0001>\u0001>\u0001>\u0001>\u0001>\u0001>\u0005>\u03e7"+
		"\b>\n>\f>\u03ea\t>\u0003>\u03ec\b>\u0001>\u0001>\u0001>\u0003>\u03f1\b"+
		">\u0001>\u0003>\u03f4\b>\u0001>\u0001>\u0001>\u0001>\u0001>\u0003>\u03fb"+
		"\b>\u0001>\u0001>\u0001>\u0001>\u0001>\u0001>\u0001>\u0001>\u0001>\u0001"+
		">\u0001>\u0001>\u0001>\u0001>\u0001>\u0001>\u0001>\u0001>\u0001>\u0005"+
		">\u0410\b>\n>\f>\u0413\t>\u0005>\u0415\b>\n>\f>\u0418\t>\u0001?\u0001"+
		"?\u0001?\u0001@\u0001@\u0001@\u0001@\u0003@\u0421\b@\u0001@\u0001@\u0001"+
		"@\u0001@\u0003@\u0427\b@\u0001A\u0003A\u042a\bA\u0001A\u0001A\u0001A\u0003"+
		"A\u042f\bA\u0001A\u0005A\u0432\bA\nA\fA\u0435\tA\u0001B\u0001B\u0001B"+
		"\u0001B\u0001B\u0001B\u0001B\u0003B\u043e\bB\u0001C\u0004C\u0441\bC\u000b"+
		"C\fC\u0442\u0001D\u0001D\u0001D\u0003D\u0448\bD\u0001E\u0001E\u0001E\u0001"+
		"E\u0001E\u0003E\u044f\bE\u0001E\u0001E\u0001E\u0001F\u0004F\u0455\bF\u000b"+
		"F\fF\u0456\u0001G\u0001G\u0001G\u0001G\u0001G\u0001G\u0001H\u0001H\u0001"+
		"H\u0005H\u0462\bH\nH\fH\u0465\tH\u0001I\u0001I\u0003I\u0469\bI\u0001J"+
		"\u0001J\u0001J\u0001J\u0001J\u0003J\u0470\bJ\u0001K\u0001K\u0003K\u0474"+
		"\bK\u0001K\u0001K\u0001L\u0004L\u0479\bL\u000bL\fL\u047a\u0001M\u0001"+
		"M\u0003M\u047f\bM\u0001M\u0001M\u0001M\u0003M\u0484\bM\u0001M\u0001M\u0001"+
		"M\u0003M\u0489\bM\u0001M\u0003M\u048c\bM\u0001N\u0001N\u0001N\u0001N\u0001"+
		"N\u0001N\u0001N\u0003N\u0495\bN\u0001O\u0001O\u0001O\u0003O\u049a\bO\u0001"+
		"O\u0001O\u0001O\u0001O\u0001O\u0001O\u0001O\u0001O\u0001O\u0001O\u0001"+
		"O\u0003O\u04a7\bO\u0001P\u0001P\u0001P\u0003P\u04ac\bP\u0001P\u0001P\u0001"+
		"P\u0001Q\u0004Q\u04b2\bQ\u000bQ\fQ\u04b3\u0001R\u0001R\u0001R\u0001R\u0003"+
		"R\u04ba\bR\u0001S\u0003S\u04bd\bS\u0001S\u0001S\u0001T\u0001T\u0001T\u0001"+
		"T\u0001T\u0001T\u0001T\u0003T\u04c8\bT\u0001T\u0001T\u0001T\u0001T\u0001"+
		"T\u0001T\u0003T\u04d0\bT\u0001U\u0001U\u0001U\u0001U\u0001U\u0001U\u0001"+
		"U\u0001U\u0001U\u0001U\u0001U\u0001U\u0001U\u0001U\u0001U\u0001U\u0001"+
		"U\u0001U\u0001U\u0001U\u0003U\u04e6\bU\u0001V\u0001V\u0003V\u04ea\bV\u0003"+
		"V\u04ec\bV\u0001V\u0001V\u0003V\u04f0\bV\u0001V\u0001V\u0003V\u04f4\b"+
		"V\u0001W\u0001W\u0003W\u04f8\bW\u0001X\u0001X\u0001X\u0005X\u04fd\bX\n"+
		"X\fX\u0500\tX\u0001Y\u0001Y\u0001Y\u0001Y\u0001Y\u0001Y\u0003Y\u0508\b"+
		"Y\u0001Y\u0001Y\u0003Y\u050c\bY\u0001Y\u0001Y\u0001Z\u0004Z\u0511\bZ\u000b"+
		"Z\fZ\u0512\u0001[\u0003[\u0516\b[\u0001[\u0001[\u0001[\u0001[\u0003[\u051c"+
		"\b[\u0001\\\u0003\\\u051f\b\\\u0001\\\u0003\\\u0522\b\\\u0001\\\u0001"+
		"\\\u0003\\\u0526\b\\\u0001\\\u0001\\\u0001]\u0004]\u052b\b]\u000b]\f]"+
		"\u052c\u0001^\u0001^\u0001_\u0001_\u0001_\u0005_\u0534\b_\n_\f_\u0537"+
		"\t_\u0001`\u0001`\u0001`\u0001`\u0003`\u053d\b`\u0001`\u0001`\u0001a\u0001"+
		"a\u0001b\u0001b\u0001b\u0001b\u0003b\u0547\bb\u0001b\u0001b\u0003b\u054b"+
		"\bb\u0001b\u0001b\u0003b\u054f\bb\u0005b\u0551\bb\nb\fb\u0554\tb\u0003"+
		"b\u0556\bb\u0003b\u0558\bb\u0001c\u0001c\u0003c\u055c\bc\u0001c\u0001"+
		"c\u0001c\u0003c\u0561\bc\u0005c\u0563\bc\nc\fc\u0566\tc\u0001d\u0001d"+
		"\u0001d\u0001d\u0001d\u0001d\u0003d\u056e\bd\u0001e\u0001e\u0001e\u0001"+
		"e\u0003e\u0574\be\u0001e\u0001e\u0001e\u0003e\u0579\be\u0001e\u0001e\u0003"+
		"e\u057d\be\u0003e\u057f\be\u0001f\u0001f\u0001f\u0001f\u0001f\u0001f\u0001"+
		"f\u0001f\u0001f\u0001f\u0001f\u0001f\u0001f\u0003f\u058e\bf\u0001g\u0001"+
		"g\u0001g\u0005g\u0593\bg\ng\fg\u0596\tg\u0001h\u0001h\u0001i\u0004i\u059b"+
		"\bi\u000bi\fi\u059c\u0001j\u0001j\u0003j\u05a1\bj\u0001j\u0001j\u0001"+
		"j\u0001j\u0001j\u0001k\u0001k\u0001l\u0001l\u0003l\u05ac\bl\u0001m\u0001"+
		"m\u0001m\u0001m\u0001m\u0001m\u0001m\u0001n\u0005n\u05b6\bn\nn\fn\u05b9"+
		"\tn\u0001o\u0004o\u05bc\bo\u000bo\fo\u05bd\u0001p\u0001p\u0001p\u0001"+
		"p\u0001p\u0003p\u05c5\bp\u0001q\u0001q\u0001q\u0001q\u0001q\u0001r\u0001"+
		"r\u0001r\u0000\u0001|s\u0000\u0002\u0004\u0006\b\n\f\u000e\u0010\u0012"+
		"\u0014\u0016\u0018\u001a\u001c\u001e \"$&(*,.02468:<>@BDFHJLNPRTVXZ\\"+
		"^`bdfhjlnprtvxz|~\u0080\u0082\u0084\u0086\u0088\u008a\u008c\u008e\u0090"+
		"\u0092\u0094\u0096\u0098\u009a\u009c\u009e\u00a0\u00a2\u00a4\u00a6\u00a8"+
		"\u00aa\u00ac\u00ae\u00b0\u00b2\u00b4\u00b6\u00b8\u00ba\u00bc\u00be\u00c0"+
		"\u00c2\u00c4\u00c6\u00c8\u00ca\u00cc\u00ce\u00d0\u00d2\u00d4\u00d6\u00d8"+
		"\u00da\u00dc\u00de\u00e0\u00e2\u00e4\u0000\u0012\u0003\u0000--66AA\u0001"+
		"\u0000\u0080\u0081\u0007\u0000\u000b\u000b\u0013\u0014aacceehhmn\u0001"+
		"\u0000eg\u0002\u0000aacc\u0001\u0000_`\u0001\u0000[^\u0001\u0000~\u007f"+
		"\u0001\u0000s}\u0007\u0000\u001e\u001e$$,,77==BBTT\u0001\u0000\r\u000f"+
		"\u0002\u0000??EE\u0001\u0000CD\u0004\u0000##88HHJJ\u0002\u0000eell\u0003"+
		"\u00000022HH\u0001\u0000UV\u0003\u0000\b\t\f\f\u0010\u0012\u066f\u0000"+
		"\u00e7\u0001\u0000\u0000\u0000\u0002\u00f0\u0001\u0000\u0000\u0000\u0004"+
		"\u00f2\u0001\u0000\u0000\u0000\u0006\u00f4\u0001\u0000\u0000\u0000\b\u0137"+
		"\u0001\u0000\u0000\u0000\n\u0139\u0001\u0000\u0000\u0000\f\u0141\u0001"+
		"\u0000\u0000\u0000\u000e\u0148\u0001\u0000\u0000\u0000\u0010\u0152\u0001"+
		"\u0000\u0000\u0000\u0012\u0167\u0001\u0000\u0000\u0000\u0014\u017b\u0001"+
		"\u0000\u0000\u0000\u0016\u01b3\u0001\u0000\u0000\u0000\u0018\u01bd\u0001"+
		"\u0000\u0000\u0000\u001a\u01bf\u0001\u0000\u0000\u0000\u001c\u01c7\u0001"+
		"\u0000\u0000\u0000\u001e\u01cf\u0001\u0000\u0000\u0000 \u01d7\u0001\u0000"+
		"\u0000\u0000\"\u01df\u0001\u0000\u0000\u0000$\u01e7\u0001\u0000\u0000"+
		"\u0000&\u01ef\u0001\u0000\u0000\u0000(\u01f7\u0001\u0000\u0000\u0000*"+
		"\u01ff\u0001\u0000\u0000\u0000,\u0207\u0001\u0000\u0000\u0000.\u020f\u0001"+
		"\u0000\u0000\u00000\u021d\u0001\u0000\u0000\u00002\u021f\u0001\u0000\u0000"+
		"\u00004\u0227\u0001\u0000\u0000\u00006\u0233\u0001\u0000\u0000\u00008"+
		"\u0239\u0001\u0000\u0000\u0000:\u0242\u0001\u0000\u0000\u0000<\u0244\u0001"+
		"\u0000\u0000\u0000>\u024c\u0001\u0000\u0000\u0000@\u0251\u0001\u0000\u0000"+
		"\u0000B\u0254\u0001\u0000\u0000\u0000D\u0270\u0001\u0000\u0000\u0000F"+
		"\u0272\u0001\u0000\u0000\u0000H\u0285\u0001\u0000\u0000\u0000J\u0288\u0001"+
		"\u0000\u0000\u0000L\u0298\u0001\u0000\u0000\u0000N\u029b\u0001\u0000\u0000"+
		"\u0000P\u02a9\u0001\u0000\u0000\u0000R\u02ab\u0001\u0000\u0000\u0000T"+
		"\u02c2\u0001\u0000\u0000\u0000V\u02dd\u0001\u0000\u0000\u0000X\u02df\u0001"+
		"\u0000\u0000\u0000Z\u02e7\u0001\u0000\u0000\u0000\\\u02f2\u0001\u0000"+
		"\u0000\u0000^\u02f4\u0001\u0000\u0000\u0000`\u02f9\u0001\u0000\u0000\u0000"+
		"b\u0300\u0001\u0000\u0000\u0000d\u0302\u0001\u0000\u0000\u0000f\u0315"+
		"\u0001\u0000\u0000\u0000h\u0317\u0001\u0000\u0000\u0000j\u0325\u0001\u0000"+
		"\u0000\u0000l\u034b\u0001\u0000\u0000\u0000n\u0382\u0001\u0000\u0000\u0000"+
		"p\u0387\u0001\u0000\u0000\u0000r\u0391\u0001\u0000\u0000\u0000t\u0393"+
		"\u0001\u0000\u0000\u0000v\u039c\u0001\u0000\u0000\u0000x\u03a8\u0001\u0000"+
		"\u0000\u0000z\u03bd\u0001\u0000\u0000\u0000|\u03eb\u0001\u0000\u0000\u0000"+
		"~\u0419\u0001\u0000\u0000\u0000\u0080\u0426\u0001\u0000\u0000\u0000\u0082"+
		"\u0429\u0001\u0000\u0000\u0000\u0084\u043d\u0001\u0000\u0000\u0000\u0086"+
		"\u0440\u0001\u0000\u0000\u0000\u0088\u0447\u0001\u0000\u0000\u0000\u008a"+
		"\u0449\u0001\u0000\u0000\u0000\u008c\u0454\u0001\u0000\u0000\u0000\u008e"+
		"\u0458\u0001\u0000\u0000\u0000\u0090\u045e\u0001\u0000\u0000\u0000\u0092"+
		"\u0466\u0001\u0000\u0000\u0000\u0094\u046f\u0001\u0000\u0000\u0000\u0096"+
		"\u0471\u0001\u0000\u0000\u0000\u0098\u0478\u0001\u0000\u0000\u0000\u009a"+
		"\u048b\u0001\u0000\u0000\u0000\u009c\u0494\u0001\u0000\u0000\u0000\u009e"+
		"\u04a6\u0001\u0000\u0000\u0000\u00a0\u04a8\u0001\u0000\u0000\u0000\u00a2"+
		"\u04b1\u0001\u0000\u0000\u0000\u00a4\u04b9\u0001\u0000\u0000\u0000\u00a6"+
		"\u04bc\u0001\u0000\u0000\u0000\u00a8\u04cf\u0001\u0000\u0000\u0000\u00aa"+
		"\u04e5\u0001\u0000\u0000\u0000\u00ac\u04eb\u0001\u0000\u0000\u0000\u00ae"+
		"\u04f5\u0001\u0000\u0000\u0000\u00b0\u04f9\u0001\u0000\u0000\u0000\u00b2"+
		"\u050b\u0001\u0000\u0000\u0000\u00b4\u0510\u0001\u0000\u0000\u0000\u00b6"+
		"\u0515\u0001\u0000\u0000\u0000\u00b8\u051e\u0001\u0000\u0000\u0000\u00ba"+
		"\u052a\u0001\u0000\u0000\u0000\u00bc\u052e\u0001\u0000\u0000\u0000\u00be"+
		"\u0530\u0001\u0000\u0000\u0000\u00c0\u0538\u0001\u0000\u0000\u0000\u00c2"+
		"\u0540\u0001\u0000\u0000\u0000\u00c4\u0557\u0001\u0000\u0000\u0000\u00c6"+
		"\u055b\u0001\u0000\u0000\u0000\u00c8\u056d\u0001\u0000\u0000\u0000\u00ca"+
		"\u057e\u0001\u0000\u0000\u0000\u00cc\u058d\u0001\u0000\u0000\u0000\u00ce"+
		"\u058f\u0001\u0000\u0000\u0000\u00d0\u0597\u0001\u0000\u0000\u0000\u00d2"+
		"\u059a\u0001\u0000\u0000\u0000\u00d4\u059e\u0001\u0000\u0000\u0000\u00d6"+
		"\u05a7\u0001\u0000\u0000\u0000\u00d8\u05ab\u0001\u0000\u0000\u0000\u00da"+
		"\u05ad\u0001\u0000\u0000\u0000\u00dc\u05b7\u0001\u0000\u0000\u0000\u00de"+
		"\u05bb\u0001\u0000\u0000\u0000\u00e0\u05c4\u0001\u0000\u0000\u0000\u00e2"+
		"\u05c6\u0001\u0000\u0000\u0000\u00e4\u05cb\u0001\u0000\u0000\u0000\u00e6"+
		"\u00e8\u0003\u00b4Z\u0000\u00e7\u00e6\u0001\u0000\u0000\u0000\u00e7\u00e8"+
		"\u0001\u0000\u0000\u0000\u00e8\u00e9\u0001\u0000\u0000\u0000\u00e9\u00ea"+
		"\u0006\u0000\uffff\uffff\u0000\u00ea\u00eb\u0005\u0000\u0000\u0001\u00eb"+
		"\u0001\u0001\u0000\u0000\u0000\u00ec\u00f1\u0005\u0084\u0000\u0000\u00ed"+
		"\u00f1\u0005\u0085\u0000\u0000\u00ee\u00f1\u0005\u0087\u0000\u0000\u00ef"+
		"\u00f1\u0003\u0006\u0003\u0000\u00f0\u00ec\u0001\u0000\u0000\u0000\u00f0"+
		"\u00ed\u0001\u0000\u0000\u0000\u00f0\u00ee\u0001\u0000\u0000\u0000\u00f0"+
		"\u00ef\u0001\u0000\u0000\u0000\u00f1\u0003\u0001\u0000\u0000\u0000\u00f2"+
		"\u00f3\u0005\u0083\u0000\u0000\u00f3\u0005\u0001\u0000\u0000\u0000\u00f4"+
		"\u00f5\u0007\u0000\u0000\u0000\u00f5\u0007\u0001\u0000\u0000\u0000\u00f6"+
		"\u00f7\u0005\u0083\u0000\u0000\u00f7\u0138\u0006\u0004\uffff\uffff\u0000"+
		"\u00f8\u0138\u0003\u0002\u0001\u0000\u00f9\u00fb\u0005\u0088\u0000\u0000"+
		"\u00fa\u00f9\u0001\u0000\u0000\u0000\u00fb\u00fc\u0001\u0000\u0000\u0000"+
		"\u00fc\u00fa\u0001\u0000\u0000\u0000\u00fc\u00fd\u0001\u0000\u0000\u0000"+
		"\u00fd\u0138\u0001\u0000\u0000\u0000\u00fe\u00ff\u0005U\u0000\u0000\u00ff"+
		"\u0100\u00032\u0019\u0000\u0100\u0101\u0005V\u0000\u0000\u0101\u0138\u0001"+
		"\u0000\u0000\u0000\u0102\u0138\u0003\f\u0006\u0000\u0103\u0138\u0005\u0015"+
		"\u0000\u0000\u0104\u0138\u0005\u0016\u0000\u0000\u0105\u0138\u0005\u0017"+
		"\u0000\u0000\u0106\u0108\u0005\u000b\u0000\u0000\u0107\u0106\u0001\u0000"+
		"\u0000\u0000\u0107\u0108\u0001\u0000\u0000\u0000\u0108\u0109\u0001\u0000"+
		"\u0000\u0000\u0109\u010a\u0005U\u0000\u0000\u010a\u010b\u0003\u00a0P\u0000"+
		"\u010b\u010c\u0005V\u0000\u0000\u010c\u0138\u0001\u0000\u0000\u0000\u010d"+
		"\u010e\u0005\u0003\u0000\u0000\u010e\u010f\u0005U\u0000\u0000\u010f\u0110"+
		"\u0003\u0016\u000b\u0000\u0110\u0111\u0005r\u0000\u0000\u0111\u0112\u0003"+
		"x<\u0000\u0112\u0113\u0005V\u0000\u0000\u0113\u0138\u0001\u0000\u0000"+
		"\u0000\u0114\u0115\u0005\u0002\u0000\u0000\u0115\u0116\u0005U\u0000\u0000"+
		"\u0116\u0117\u0003x<\u0000\u0117\u0118\u0005r\u0000\u0000\u0118\u0119"+
		"\u0003\u0016\u000b\u0000\u0119\u011a\u0005V\u0000\u0000\u011a\u0138\u0001"+
		"\u0000\u0000\u0000\u011b\u011c\u0005\u0004\u0000\u0000\u011c\u011d\u0005"+
		"U\u0000\u0000\u011d\u011e\u0003\u0016\u000b\u0000\u011e\u011f\u0005r\u0000"+
		"\u0000\u011f\u0120\u0003\u0016\u000b\u0000\u0120\u0121\u0005r\u0000\u0000"+
		"\u0121\u0122\u0003\u0016\u000b\u0000\u0122\u0123\u0005V\u0000\u0000\u0123"+
		"\u0138\u0001\u0000\u0000\u0000\u0124\u0125\u0005\u0005\u0000\u0000\u0125"+
		"\u0126\u0005U\u0000\u0000\u0126\u0127\u0003x<\u0000\u0127\u0128\u0005"+
		"r\u0000\u0000\u0128\u0129\u0003x<\u0000\u0129\u012a\u0005V\u0000\u0000"+
		"\u012a\u0138\u0001\u0000\u0000\u0000\u012b\u012c\u0005\u0006\u0000\u0000"+
		"\u012c\u012d\u0005U\u0000\u0000\u012d\u012e\u0003\n\u0005\u0000\u012e"+
		"\u012f\u0005V\u0000\u0000\u012f\u0138\u0001\u0000\u0000\u0000\u0130\u0131"+
		"\u0005\u0007\u0000\u0000\u0131\u0132\u0005U\u0000\u0000\u0132\u0133\u0003"+
		"0\u0018\u0000\u0133\u0134\u0005r\u0000\u0000\u0134\u0135\u00030\u0018"+
		"\u0000\u0135\u0136\u0005V\u0000\u0000\u0136\u0138\u0001\u0000\u0000\u0000"+
		"\u0137\u00f6\u0001\u0000\u0000\u0000\u0137\u00f8\u0001\u0000\u0000\u0000"+
		"\u0137\u00fa\u0001\u0000\u0000\u0000\u0137\u00fe\u0001\u0000\u0000\u0000"+
		"\u0137\u0102\u0001\u0000\u0000\u0000\u0137\u0103\u0001\u0000\u0000\u0000"+
		"\u0137\u0104\u0001\u0000\u0000\u0000\u0137\u0105\u0001\u0000\u0000\u0000"+
		"\u0137\u0107\u0001\u0000\u0000\u0000\u0137\u010d\u0001\u0000\u0000\u0000"+
		"\u0137\u0114\u0001\u0000\u0000\u0000\u0137\u011b\u0001\u0000\u0000\u0000"+
		"\u0137\u0124\u0001\u0000\u0000\u0000\u0137\u012b\u0001\u0000\u0000\u0000"+
		"\u0137\u0130\u0001\u0000\u0000\u0000\u0138\t\u0001\u0000\u0000\u0000\u0139"+
		"\u013e\u00030\u0018\u0000\u013a\u013b\u0005r\u0000\u0000\u013b\u013d\u0003"+
		"0\u0018\u0000\u013c\u013a\u0001\u0000\u0000\u0000\u013d\u0140\u0001\u0000"+
		"\u0000\u0000\u013e\u013c\u0001\u0000\u0000\u0000\u013e\u013f\u0001\u0000"+
		"\u0000\u0000\u013f\u000b\u0001\u0000\u0000\u0000\u0140\u013e\u0001\u0000"+
		"\u0000\u0000\u0141\u0142\u0005P\u0000\u0000\u0142\u0143\u0005U\u0000\u0000"+
		"\u0143\u0144\u00030\u0018\u0000\u0144\u0145\u0005r\u0000\u0000\u0145\u0146"+
		"\u0003\u000e\u0007\u0000\u0146\u0147\u0005V\u0000\u0000\u0147\r\u0001"+
		"\u0000\u0000\u0000\u0148\u014d\u0003\u0010\b\u0000\u0149\u014a\u0005r"+
		"\u0000\u0000\u014a\u014c\u0003\u0010\b\u0000\u014b\u0149\u0001\u0000\u0000"+
		"\u0000\u014c\u014f\u0001\u0000\u0000\u0000\u014d\u014b\u0001\u0000\u0000"+
		"\u0000\u014d\u014e\u0001\u0000\u0000\u0000\u014e\u000f\u0001\u0000\u0000"+
		"\u0000\u014f\u014d\u0001\u0000\u0000\u0000\u0150\u0153\u0003x<\u0000\u0151"+
		"\u0153\u0005&\u0000\u0000\u0152\u0150\u0001\u0000\u0000\u0000\u0152\u0151"+
		"\u0001\u0000\u0000\u0000\u0153\u0154\u0001\u0000\u0000\u0000\u0154\u0155"+
		"\u0005p\u0000\u0000\u0155\u0156\u00030\u0018\u0000\u0156\u0011\u0001\u0000"+
		"\u0000\u0000\u0157\u0168\u0003\b\u0004\u0000\u0158\u015a\u0005\u000b\u0000"+
		"\u0000\u0159\u0158\u0001\u0000\u0000\u0000\u0159\u015a\u0001\u0000\u0000"+
		"\u0000\u015a\u015b\u0001\u0000\u0000\u0000\u015b\u015c\u0005U\u0000\u0000"+
		"\u015c\u015d\u0003x<\u0000\u015d\u015e\u0005V\u0000\u0000\u015e\u0160"+
		"\u0005Y\u0000\u0000\u015f\u0161\u0003\u0082A\u0000\u0160\u015f\u0001\u0000"+
		"\u0000\u0000\u0160\u0161\u0001\u0000\u0000\u0000\u0161\u0163\u0001\u0000"+
		"\u0000\u0000\u0162\u0164\u0005r\u0000\u0000\u0163\u0162\u0001\u0000\u0000"+
		"\u0000\u0163\u0164\u0001\u0000\u0000\u0000\u0164\u0165\u0001\u0000\u0000"+
		"\u0000\u0165\u0166\u0005Z\u0000\u0000\u0166\u0168\u0001\u0000\u0000\u0000"+
		"\u0167\u0157\u0001\u0000\u0000\u0000\u0167\u0159\u0001\u0000\u0000\u0000"+
		"\u0168\u0178\u0001\u0000\u0000\u0000\u0169\u016a\u0005W\u0000\u0000\u016a"+
		"\u016b\u00032\u0019\u0000\u016b\u016c\u0005X\u0000\u0000\u016c\u0177\u0001"+
		"\u0000\u0000\u0000\u016d\u016f\u0005U\u0000\u0000\u016e\u0170\u0003\u0014"+
		"\n\u0000\u016f\u016e\u0001\u0000\u0000\u0000\u016f\u0170\u0001\u0000\u0000"+
		"\u0000\u0170\u0171\u0001\u0000\u0000\u0000\u0171\u0177\u0005V\u0000\u0000"+
		"\u0172\u0173\u0007\u0001\u0000\u0000\u0173\u0177\u0005\u0083\u0000\u0000"+
		"\u0174\u0177\u0005b\u0000\u0000\u0175\u0177\u0005d\u0000\u0000\u0176\u0169"+
		"\u0001\u0000\u0000\u0000\u0176\u016d\u0001\u0000\u0000\u0000\u0176\u0172"+
		"\u0001\u0000\u0000\u0000\u0176\u0174\u0001\u0000\u0000\u0000\u0176\u0175"+
		"\u0001\u0000\u0000\u0000\u0177\u017a\u0001\u0000\u0000\u0000\u0178\u0176"+
		"\u0001\u0000\u0000\u0000\u0178\u0179\u0001\u0000\u0000\u0000\u0179\u0013"+
		"\u0001\u0000\u0000\u0000\u017a\u0178\u0001\u0000\u0000\u0000\u017b\u0180"+
		"\u00030\u0018\u0000\u017c\u017d\u0005r\u0000\u0000\u017d\u017f\u00030"+
		"\u0018\u0000\u017e\u017c\u0001\u0000\u0000\u0000\u017f\u0182\u0001\u0000"+
		"\u0000\u0000\u0180\u017e\u0001\u0000\u0000\u0000\u0180\u0181\u0001\u0000"+
		"\u0000\u0000\u0181\u0015\u0001\u0000\u0000\u0000\u0182\u0180\u0001\u0000"+
		"\u0000\u0000\u0183\u01b4\u0003\u0012\t\u0000\u0184\u0185\u0005b\u0000"+
		"\u0000\u0185\u01b4\u0003\u0016\u000b\u0000\u0186\u0187\u0005d\u0000\u0000"+
		"\u0187\u01b4\u0003\u0016\u000b\u0000\u0188\u0189\u0007\u0002\u0000\u0000"+
		"\u0189\u01b4\u0003\u0018\f\u0000\u018a\u018b\u0004\u000b\u0000\u0000\u018b"+
		"\u018c\u0005<\u0000\u0000\u018c\u01b4\u0003\u0016\u000b\u0000\u018d\u018e"+
		"\u0004\u000b\u0001\u0000\u018e\u018f\u0005<\u0000\u0000\u018f\u0190\u0005"+
		"U\u0000\u0000\u0190\u0191\u0003x<\u0000\u0191\u0192\u0005V\u0000\u0000"+
		"\u0192\u01b4\u0001\u0000\u0000\u0000\u0193\u0194\u0004\u000b\u0002\u0000"+
		"\u0194\u0195\u0005\u0019\u0000\u0000\u0195\u0196\u0005U\u0000\u0000\u0196"+
		"\u0197\u0003x<\u0000\u0197\u0198\u0005V\u0000\u0000\u0198\u01b4\u0001"+
		"\u0000\u0000\u0000\u0199\u019a\u0004\u000b\u0003\u0000\u019a\u019b\u0005"+
		"\u001c\u0000\u0000\u019b\u01b4\u0003\u0016\u000b\u0000\u019c\u019d\u0004"+
		"\u000b\u0004\u0000\u019d\u019e\u0005\u001c\u0000\u0000\u019e\u019f\u0005"+
		"U\u0000\u0000\u019f\u01a0\u0003x<\u0000\u01a0\u01a1\u0005V\u0000\u0000"+
		"\u01a1\u01b4\u0001\u0000\u0000\u0000\u01a2\u01a3\u0004\u000b\u0005\u0000"+
		"\u01a3\u01a4\u0005\u0019\u0000\u0000\u01a4\u01b4\u0003\u0016\u000b\u0000"+
		"\u01a5\u01a6\u0004\u000b\u0006\u0000\u01a6\u01a7\u0005\u001a\u0000\u0000"+
		"\u01a7\u01a8\u0005U\u0000\u0000\u01a8\u01a9\u0003x<\u0000\u01a9\u01aa"+
		"\u0005V\u0000\u0000\u01aa\u01b4\u0001\u0000\u0000\u0000\u01ab\u01ac\u0004"+
		"\u000b\u0007\u0000\u01ac\u01ad\u0005\u001b\u0000\u0000\u01ad\u01ae\u0005"+
		"U\u0000\u0000\u01ae\u01af\u0003x<\u0000\u01af\u01b0\u0005V\u0000\u0000"+
		"\u01b0\u01b4\u0001\u0000\u0000\u0000\u01b1\u01b2\u0005j\u0000\u0000\u01b2"+
		"\u01b4\u0005\u0083\u0000\u0000\u01b3\u0183\u0001\u0000\u0000\u0000\u01b3"+
		"\u0184\u0001\u0000\u0000\u0000\u01b3\u0186\u0001\u0000\u0000\u0000\u01b3"+
		"\u0188\u0001\u0000\u0000\u0000\u01b3\u018a\u0001\u0000\u0000\u0000\u01b3"+
		"\u018d\u0001\u0000\u0000\u0000\u01b3\u0193\u0001\u0000\u0000\u0000\u01b3"+
		"\u0199\u0001\u0000\u0000\u0000\u01b3\u019c\u0001\u0000\u0000\u0000\u01b3"+
		"\u01a2\u0001\u0000\u0000\u0000\u01b3\u01a5\u0001\u0000\u0000\u0000\u01b3"+
		"\u01ab\u0001\u0000\u0000\u0000\u01b3\u01b1\u0001\u0000\u0000\u0000\u01b4"+
		"\u0017\u0001\u0000\u0000\u0000\u01b5\u01b6\u0004\f\b\u0000\u01b6\u01b7"+
		"\u0005U\u0000\u0000\u01b7\u01b8\u0003x<\u0000\u01b8\u01b9\u0005V\u0000"+
		"\u0000\u01b9\u01ba\u0003\u0018\f\u0000\u01ba\u01be\u0001\u0000\u0000\u0000"+
		"\u01bb\u01be\u0003\u0016\u000b\u0000\u01bc\u01be\u0005\u0086\u0000\u0000"+
		"\u01bd\u01b5\u0001\u0000\u0000\u0000\u01bd\u01bb\u0001\u0000\u0000\u0000"+
		"\u01bd\u01bc\u0001\u0000\u0000\u0000\u01be\u0019\u0001\u0000\u0000\u0000"+
		"\u01bf\u01c4\u0003\u0018\f\u0000\u01c0\u01c1\u0007\u0003\u0000\u0000\u01c1"+
		"\u01c3\u0003\u0018\f\u0000\u01c2\u01c0\u0001\u0000\u0000\u0000\u01c3\u01c6"+
		"\u0001\u0000\u0000\u0000\u01c4\u01c2\u0001\u0000\u0000\u0000\u01c4\u01c5"+
		"\u0001\u0000\u0000\u0000\u01c5\u001b\u0001\u0000\u0000\u0000\u01c6\u01c4"+
		"\u0001\u0000\u0000\u0000\u01c7\u01cc\u0003\u001a\r\u0000\u01c8\u01c9\u0007"+
		"\u0004\u0000\u0000\u01c9\u01cb\u0003\u001a\r\u0000\u01ca\u01c8\u0001\u0000"+
		"\u0000\u0000\u01cb\u01ce\u0001\u0000\u0000\u0000\u01cc\u01ca\u0001\u0000"+
		"\u0000\u0000\u01cc\u01cd\u0001\u0000\u0000\u0000\u01cd\u001d\u0001\u0000"+
		"\u0000\u0000\u01ce\u01cc\u0001\u0000\u0000\u0000\u01cf\u01d4\u0003\u001c"+
		"\u000e\u0000\u01d0\u01d1\u0007\u0005\u0000\u0000\u01d1\u01d3\u0003\u001c"+
		"\u000e\u0000\u01d2\u01d0\u0001\u0000\u0000\u0000\u01d3\u01d6\u0001\u0000"+
		"\u0000\u0000\u01d4\u01d2\u0001\u0000\u0000\u0000\u01d4\u01d5\u0001\u0000"+
		"\u0000\u0000\u01d5\u001f\u0001\u0000\u0000\u0000\u01d6\u01d4\u0001\u0000"+
		"\u0000\u0000\u01d7\u01dc\u0003\u001e\u000f\u0000\u01d8\u01d9\u0007\u0006"+
		"\u0000\u0000\u01d9\u01db\u0003\u001e\u000f\u0000\u01da\u01d8\u0001\u0000"+
		"\u0000\u0000\u01db\u01de\u0001\u0000\u0000\u0000\u01dc\u01da\u0001\u0000"+
		"\u0000\u0000\u01dc\u01dd\u0001\u0000\u0000\u0000\u01dd!\u0001\u0000\u0000"+
		"\u0000\u01de\u01dc\u0001\u0000\u0000\u0000\u01df\u01e4\u0003 \u0010\u0000"+
		"\u01e0\u01e1\u0007\u0007\u0000\u0000\u01e1\u01e3\u0003 \u0010\u0000\u01e2"+
		"\u01e0\u0001\u0000\u0000\u0000\u01e3\u01e6\u0001\u0000\u0000\u0000\u01e4"+
		"\u01e2\u0001\u0000\u0000\u0000\u01e4\u01e5\u0001\u0000\u0000\u0000\u01e5"+
		"#\u0001\u0000\u0000\u0000\u01e6\u01e4\u0001\u0000\u0000\u0000\u01e7\u01ec"+
		"\u0003\"\u0011\u0000\u01e8\u01e9\u0005h\u0000\u0000\u01e9\u01eb\u0003"+
		"\"\u0011\u0000\u01ea\u01e8\u0001\u0000\u0000\u0000\u01eb\u01ee\u0001\u0000"+
		"\u0000\u0000\u01ec\u01ea\u0001\u0000\u0000\u0000\u01ec\u01ed\u0001\u0000"+
		"\u0000\u0000\u01ed%\u0001\u0000\u0000\u0000\u01ee\u01ec\u0001\u0000\u0000"+
		"\u0000\u01ef\u01f4\u0003$\u0012\u0000\u01f0\u01f1\u0005l\u0000\u0000\u01f1"+
		"\u01f3\u0003$\u0012\u0000\u01f2\u01f0\u0001\u0000\u0000\u0000\u01f3\u01f6"+
		"\u0001\u0000\u0000\u0000\u01f4\u01f2\u0001\u0000\u0000\u0000\u01f4\u01f5"+
		"\u0001\u0000\u0000\u0000\u01f5\'\u0001\u0000\u0000\u0000\u01f6\u01f4\u0001"+
		"\u0000\u0000\u0000\u01f7\u01fc\u0003&\u0013\u0000\u01f8\u01f9\u0005i\u0000"+
		"\u0000\u01f9\u01fb\u0003&\u0013\u0000\u01fa\u01f8\u0001\u0000\u0000\u0000"+
		"\u01fb\u01fe\u0001\u0000\u0000\u0000\u01fc\u01fa\u0001\u0000\u0000\u0000"+
		"\u01fc\u01fd\u0001\u0000\u0000\u0000\u01fd)\u0001\u0000\u0000\u0000\u01fe"+
		"\u01fc\u0001\u0000\u0000\u0000\u01ff\u0204\u0003(\u0014\u0000\u0200\u0201"+
		"\u0005j\u0000\u0000\u0201\u0203\u0003(\u0014\u0000\u0202\u0200\u0001\u0000"+
		"\u0000\u0000\u0203\u0206\u0001\u0000\u0000\u0000\u0204\u0202\u0001\u0000"+
		"\u0000\u0000\u0204\u0205\u0001\u0000\u0000\u0000\u0205+\u0001\u0000\u0000"+
		"\u0000\u0206\u0204\u0001\u0000\u0000\u0000\u0207\u020c\u0003*\u0015\u0000"+
		"\u0208\u0209\u0005k\u0000\u0000\u0209\u020b\u0003*\u0015\u0000\u020a\u0208"+
		"\u0001\u0000\u0000\u0000\u020b\u020e\u0001\u0000\u0000\u0000\u020c\u020a"+
		"\u0001\u0000\u0000\u0000\u020c\u020d\u0001\u0000\u0000\u0000\u020d-\u0001"+
		"\u0000\u0000\u0000\u020e\u020c\u0001\u0000\u0000\u0000\u020f\u0215\u0003"+
		",\u0016\u0000\u0210\u0211\u0005o\u0000\u0000\u0211\u0212\u00032\u0019"+
		"\u0000\u0212\u0213\u0005p\u0000\u0000\u0213\u0214\u0003.\u0017\u0000\u0214"+
		"\u0216\u0001\u0000\u0000\u0000\u0215\u0210\u0001\u0000\u0000\u0000\u0215"+
		"\u0216\u0001\u0000\u0000\u0000\u0216/\u0001\u0000\u0000\u0000\u0217\u021e"+
		"\u0003.\u0017\u0000\u0218\u0219\u0003\u0016\u000b\u0000\u0219\u021a\u0007"+
		"\b\u0000\u0000\u021a\u021b\u00030\u0018\u0000\u021b\u021e\u0001\u0000"+
		"\u0000\u0000\u021c\u021e\u0005\u0086\u0000\u0000\u021d\u0217\u0001\u0000"+
		"\u0000\u0000\u021d\u0218\u0001\u0000\u0000\u0000\u021d\u021c\u0001\u0000"+
		"\u0000\u0000\u021e1\u0001\u0000\u0000\u0000\u021f\u0224\u00030\u0018\u0000"+
		"\u0220\u0221\u0005r\u0000\u0000\u0221\u0223\u00030\u0018\u0000\u0222\u0220"+
		"\u0001\u0000\u0000\u0000\u0223\u0226\u0001\u0000\u0000\u0000\u0224\u0222"+
		"\u0001\u0000\u0000\u0000\u0224\u0225\u0001\u0000\u0000\u0000\u02253\u0001"+
		"\u0000\u0000\u0000\u0226\u0224\u0001\u0000\u0000\u0000\u0227\u0228\u0003"+
		".\u0017\u0000\u02285\u0001\u0000\u0000\u0000\u0229\u022d\u00038\u001c"+
		"\u0000\u022a\u022b\u0004\u001b\t\u0000\u022b\u022e\u0003<\u001e\u0000"+
		"\u022c\u022e\u0001\u0000\u0000\u0000\u022d\u022a\u0001\u0000\u0000\u0000"+
		"\u022d\u022c\u0001\u0000\u0000\u0000\u022e\u022f\u0001\u0000\u0000\u0000"+
		"\u022f\u0230\u0005q\u0000\u0000\u0230\u0234\u0001\u0000\u0000\u0000\u0231"+
		"\u0234\u0003\u008aE\u0000\u0232\u0234\u0003@ \u0000\u0233\u0229\u0001"+
		"\u0000\u0000\u0000\u0233\u0231\u0001\u0000\u0000\u0000\u0233\u0232\u0001"+
		"\u0000\u0000\u0000\u0234\u0235\u0001\u0000\u0000\u0000\u0235\u0236\u0006"+
		"\u001b\uffff\uffff\u0000\u02367\u0001\u0000\u0000\u0000\u0237\u0238\u0004"+
		"\u001c\n\u0000\u0238\u023a\u0003:\u001d\u0000\u0239\u0237\u0001\u0000"+
		"\u0000\u0000\u023a\u023b\u0001\u0000\u0000\u0000\u023b\u0239\u0001\u0000"+
		"\u0000\u0000\u023b\u023c\u0001\u0000\u0000\u0000\u023c9\u0001\u0000\u0000"+
		"\u0000\u023d\u0243\u0003B!\u0000\u023e\u0243\u0003D\"\u0000\u023f\u0243"+
		"\u0003d2\u0000\u0240\u0243\u0003f3\u0000\u0241\u0243\u0003h4\u0000\u0242"+
		"\u023d\u0001\u0000\u0000\u0000\u0242\u023e\u0001\u0000\u0000\u0000\u0242"+
		"\u023f\u0001\u0000\u0000\u0000\u0242\u0240\u0001\u0000\u0000\u0000\u0242"+
		"\u0241\u0001\u0000\u0000\u0000\u0243;\u0001\u0000\u0000\u0000\u0244\u0249"+
		"\u0003>\u001f\u0000\u0245\u0246\u0005r\u0000\u0000\u0246\u0248\u0003>"+
		"\u001f\u0000\u0247\u0245\u0001\u0000\u0000\u0000\u0248\u024b\u0001\u0000"+
		"\u0000\u0000\u0249\u0247\u0001\u0000\u0000\u0000\u0249\u024a\u0001\u0000"+
		"\u0000\u0000\u024a=\u0001\u0000\u0000\u0000\u024b\u0249\u0001\u0000\u0000"+
		"\u0000\u024c\u024f\u0003j5\u0000\u024d\u024e\u0005s\u0000\u0000\u024e"+
		"\u0250\u0003\u0080@\u0000\u024f\u024d\u0001\u0000\u0000\u0000\u024f\u0250"+
		"\u0001\u0000\u0000\u0000\u0250?\u0001\u0000\u0000\u0000\u0251\u0252\u0003"+
		"\u008cF\u0000\u0252\u0253\u0005q\u0000\u0000\u0253A\u0001\u0000\u0000"+
		"\u0000\u0254\u0255\u0007\t\u0000\u0000\u0255C\u0001\u0000\u0000\u0000"+
		"\u0256\u0271\u0005G\u0000\u0000\u0257\u0271\u0005\"\u0000\u0000\u0258"+
		"\u0271\u0005:\u0000\u0000\u0259\u0271\u00053\u0000\u0000\u025a\u0271\u0005"+
		"5\u0000\u0000\u025b\u0271\u0005.\u0000\u0000\u025c\u0271\u0005)\u0000"+
		"\u0000\u025d\u0271\u0005;\u0000\u0000\u025e\u0271\u0005F\u0000\u0000\u025f"+
		"\u0271\u0005\u001f\u0000\u0000\u0260\u0271\u0005L\u0000\u0000\u0261\u0271"+
		"\u0005\r\u0000\u0000\u0262\u0271\u0005\u000e\u0000\u0000\u0263\u0271\u0005"+
		"\u000f\u0000\u0000\u0264\u0265\u0005\u000b\u0000\u0000\u0265\u0266\u0005"+
		"U\u0000\u0000\u0266\u0267\u0007\n\u0000\u0000\u0267\u0271\u0005V\u0000"+
		"\u0000\u0268\u0271\u0003^/\u0000\u0269\u0271\u0003F#\u0000\u026a\u0271"+
		"\u0003V+\u0000\u026b\u026d\u0005\u000b\u0000\u0000\u026c\u026b\u0001\u0000"+
		"\u0000\u0000\u026c\u026d\u0001\u0000\u0000\u0000\u026d\u026e\u0001\u0000"+
		"\u0000\u0000\u026e\u0271\u0003~?\u0000\u026f\u0271\u0003`0\u0000\u0270"+
		"\u0256\u0001\u0000\u0000\u0000\u0270\u0257\u0001\u0000\u0000\u0000\u0270"+
		"\u0258\u0001\u0000\u0000\u0000\u0270\u0259\u0001\u0000\u0000\u0000\u0270"+
		"\u025a\u0001\u0000\u0000\u0000\u0270\u025b\u0001\u0000\u0000\u0000\u0270"+
		"\u025c\u0001\u0000\u0000\u0000\u0270\u025d\u0001\u0000\u0000\u0000\u0270"+
		"\u025e\u0001\u0000\u0000\u0000\u0270\u025f\u0001\u0000\u0000\u0000\u0270"+
		"\u0260\u0001\u0000\u0000\u0000\u0270\u0261\u0001\u0000\u0000\u0000\u0270"+
		"\u0262\u0001\u0000\u0000\u0000\u0270\u0263\u0001\u0000\u0000\u0000\u0270"+
		"\u0264\u0001\u0000\u0000\u0000\u0270\u0268\u0001\u0000\u0000\u0000\u0270"+
		"\u0269\u0001\u0000\u0000\u0000\u0270\u026a\u0001\u0000\u0000\u0000\u0270"+
		"\u026c\u0001\u0000\u0000\u0000\u0270\u026f\u0001\u0000\u0000\u0000\u0271"+
		"E\u0001\u0000\u0000\u0000\u0272\u0274\u0003H$\u0000\u0273\u0275\u0003"+
		"\u008cF\u0000\u0274\u0273\u0001\u0000\u0000\u0000\u0274\u0275\u0001\u0000"+
		"\u0000\u0000\u0275\u0277\u0001\u0000\u0000\u0000\u0276\u0278\u0003\u00de"+
		"o\u0000\u0277\u0276\u0001\u0000\u0000\u0000\u0277\u0278\u0001\u0000\u0000"+
		"\u0000\u0278\u0283\u0001\u0000\u0000\u0000\u0279\u027b\u0005\u0083\u0000"+
		"\u0000\u027a\u0279\u0001\u0000\u0000\u0000\u027a\u027b\u0001\u0000\u0000"+
		"\u0000\u027b\u027c\u0001\u0000\u0000\u0000\u027c\u027f\u0005Y\u0000\u0000"+
		"\u027d\u0280\u0004#\u000b\u0000\u027e\u0280\u0003J%\u0000\u027f\u027d"+
		"\u0001\u0000\u0000\u0000\u027f\u027e\u0001\u0000\u0000\u0000\u0280\u0281"+
		"\u0001\u0000\u0000\u0000\u0281\u0284\u0005Z\u0000\u0000\u0282\u0284\u0005"+
		"\u0083\u0000\u0000\u0283\u027a\u0001\u0000\u0000\u0000\u0283\u0282\u0001"+
		"\u0000\u0000\u0000\u0284G\u0001\u0000\u0000\u0000\u0285\u0286\u0007\u000b"+
		"\u0000\u0000\u0286I\u0001\u0000\u0000\u0000\u0287\u0289\u0003L&\u0000"+
		"\u0288\u0287\u0001\u0000\u0000\u0000\u0289\u028a\u0001\u0000\u0000\u0000"+
		"\u028a\u0288\u0001\u0000\u0000\u0000\u028a\u028b\u0001\u0000\u0000\u0000"+
		"\u028bK\u0001\u0000\u0000\u0000\u028c\u028e\u0003\u008cF\u0000\u028d\u028c"+
		"\u0001\u0000\u0000\u0000\u028d\u028e\u0001\u0000\u0000\u0000\u028e\u028f"+
		"\u0001\u0000\u0000\u0000\u028f\u0291\u0003N\'\u0000\u0290\u0292\u0003"+
		"R)\u0000\u0291\u0290\u0001\u0000\u0000\u0000\u0291\u0292\u0001\u0000\u0000"+
		"\u0000\u0292\u0293\u0001\u0000\u0000\u0000\u0293\u0294\u0005q\u0000\u0000"+
		"\u0294\u0299\u0001\u0000\u0000\u0000\u0295\u0299\u0003\u008aE\u0000\u0296"+
		"\u0297\u0005\u000b\u0000\u0000\u0297\u0299\u0003L&\u0000\u0298\u028d\u0001"+
		"\u0000\u0000\u0000\u0298\u0295\u0001\u0000\u0000\u0000\u0298\u0296\u0001"+
		"\u0000\u0000\u0000\u0299M\u0001\u0000\u0000\u0000\u029a\u029c\u0003\u00de"+
		"o\u0000\u029b\u029a\u0001\u0000\u0000\u0000\u029b\u029c\u0001\u0000\u0000"+
		"\u0000\u029c\u029f\u0001\u0000\u0000\u0000\u029d\u029e\u0004\'\f\u0000"+
		"\u029e\u02a0\u0003P(\u0000\u029f\u029d\u0001\u0000\u0000\u0000\u02a0\u02a1"+
		"\u0001\u0000\u0000\u0000\u02a1\u029f\u0001\u0000\u0000\u0000\u02a1\u02a2"+
		"\u0001\u0000\u0000\u0000\u02a2\u02a4\u0001\u0000\u0000\u0000\u02a3\u02a5"+
		"\u0003\u008cF\u0000\u02a4\u02a3\u0001\u0000\u0000\u0000\u02a4\u02a5\u0001"+
		"\u0000\u0000\u0000\u02a5O\u0001\u0000\u0000\u0000\u02a6\u02aa\u0003D\""+
		"\u0000\u02a7\u02aa\u0003d2\u0000\u02a8\u02aa\u0003h4\u0000\u02a9\u02a6"+
		"\u0001\u0000\u0000\u0000\u02a9\u02a7\u0001\u0000\u0000\u0000\u02a9\u02a8"+
		"\u0001\u0000\u0000\u0000\u02aaQ\u0001\u0000\u0000\u0000\u02ab\u02b3\u0003"+
		"T*\u0000\u02ac\u02ae\u0005r\u0000\u0000\u02ad\u02af\u0003\u00deo\u0000"+
		"\u02ae\u02ad\u0001\u0000\u0000\u0000\u02ae\u02af\u0001\u0000\u0000\u0000"+
		"\u02af\u02b0\u0001\u0000\u0000\u0000\u02b0\u02b2\u0003T*\u0000\u02b1\u02ac"+
		"\u0001\u0000\u0000\u0000\u02b2\u02b5\u0001\u0000\u0000\u0000\u02b3\u02b1"+
		"\u0001\u0000\u0000\u0000\u02b3\u02b4\u0001\u0000\u0000\u0000\u02b4S\u0001"+
		"\u0000\u0000\u0000\u02b5\u02b3\u0001\u0000\u0000\u0000\u02b6\u02b8\u0003"+
		"j5\u0000\u02b7\u02b9\u0003\u00deo\u0000\u02b8\u02b7\u0001\u0000\u0000"+
		"\u0000\u02b8\u02b9\u0001\u0000\u0000\u0000\u02b9\u02c3\u0001\u0000\u0000"+
		"\u0000\u02ba\u02bc\u0003j5\u0000\u02bb\u02ba\u0001\u0000\u0000\u0000\u02bb"+
		"\u02bc\u0001\u0000\u0000\u0000\u02bc\u02bd\u0001\u0000\u0000\u0000\u02bd"+
		"\u02be\u0005p\u0000\u0000\u02be\u02c0\u00034\u001a\u0000\u02bf\u02c1\u0003"+
		"\u00deo\u0000\u02c0\u02bf\u0001\u0000\u0000\u0000\u02c0\u02c1\u0001\u0000"+
		"\u0000\u0000\u02c1\u02c3\u0001\u0000\u0000\u0000\u02c2\u02b6\u0001\u0000"+
		"\u0000\u0000\u02c2\u02bb\u0001\u0000\u0000\u0000\u02c3U\u0001\u0000\u0000"+
		"\u0000\u02c4\u02c6\u0005+\u0000\u0000\u02c5\u02c7\u0003\u008cF\u0000\u02c6"+
		"\u02c5\u0001\u0000\u0000\u0000\u02c6\u02c7\u0001\u0000\u0000\u0000\u02c7"+
		"\u02c9\u0001\u0000\u0000\u0000\u02c8\u02ca\u0003\u00deo\u0000\u02c9\u02c8"+
		"\u0001\u0000\u0000\u0000\u02c9\u02ca\u0001\u0000\u0000\u0000\u02ca\u02cc"+
		"\u0001\u0000\u0000\u0000\u02cb\u02cd\u0005\u0083\u0000\u0000\u02cc\u02cb"+
		"\u0001\u0000\u0000\u0000\u02cc\u02cd\u0001\u0000\u0000\u0000\u02cd\u02cf"+
		"\u0001\u0000\u0000\u0000\u02ce\u02d0\u0003\\.\u0000\u02cf\u02ce\u0001"+
		"\u0000\u0000\u0000\u02cf\u02d0\u0001\u0000\u0000\u0000\u02d0\u02d1\u0001"+
		"\u0000\u0000\u0000\u02d1\u02d2\u0005Y\u0000\u0000\u02d2\u02d4\u0003X,"+
		"\u0000\u02d3\u02d5\u0005r\u0000\u0000\u02d4\u02d3\u0001\u0000\u0000\u0000"+
		"\u02d4\u02d5\u0001\u0000\u0000\u0000\u02d5\u02d6\u0001\u0000\u0000\u0000"+
		"\u02d6\u02d7\u0005Z\u0000\u0000\u02d7\u02de\u0001\u0000\u0000\u0000\u02d8"+
		"\u02d9\u0005+\u0000\u0000\u02d9\u02db\u0005\u0083\u0000\u0000\u02da\u02dc"+
		"\u0003\\.\u0000\u02db\u02da\u0001\u0000\u0000\u0000\u02db\u02dc\u0001"+
		"\u0000\u0000\u0000\u02dc\u02de\u0001\u0000\u0000\u0000\u02dd\u02c4\u0001"+
		"\u0000\u0000\u0000\u02dd\u02d8\u0001\u0000\u0000\u0000\u02deW\u0001\u0000"+
		"\u0000\u0000\u02df\u02e4\u0003Z-\u0000\u02e0\u02e1\u0005r\u0000\u0000"+
		"\u02e1\u02e3\u0003Z-\u0000\u02e2\u02e0\u0001\u0000\u0000\u0000\u02e3\u02e6"+
		"\u0001\u0000\u0000\u0000\u02e4\u02e2\u0001\u0000\u0000\u0000\u02e4\u02e5"+
		"\u0001\u0000\u0000\u0000\u02e5Y\u0001\u0000\u0000\u0000\u02e6\u02e4\u0001"+
		"\u0000\u0000\u0000\u02e7\u02e9\u0003\u0004\u0002\u0000\u02e8\u02ea\u0003"+
		"\u008cF\u0000\u02e9\u02e8\u0001\u0000\u0000\u0000\u02e9\u02ea\u0001\u0000"+
		"\u0000\u0000\u02ea\u02ec\u0001\u0000\u0000\u0000\u02eb\u02ed\u0003\u00de"+
		"o\u0000\u02ec\u02eb\u0001\u0000\u0000\u0000\u02ec\u02ed\u0001\u0000\u0000"+
		"\u0000\u02ed\u02f0\u0001\u0000\u0000\u0000\u02ee\u02ef\u0005s\u0000\u0000"+
		"\u02ef\u02f1\u00034\u001a\u0000\u02f0\u02ee\u0001\u0000\u0000\u0000\u02f0"+
		"\u02f1\u0001\u0000\u0000\u0000\u02f1[\u0001\u0000\u0000\u0000\u02f2\u02f3"+
		"\u0003N\'\u0000\u02f3]\u0001\u0000\u0000\u0000\u02f4\u02f5\u0005J\u0000"+
		"\u0000\u02f5\u02f6\u0005U\u0000\u0000\u02f6\u02f7\u0003x<\u0000\u02f7"+
		"\u02f8\u0005V\u0000\u0000\u02f8_\u0001\u0000\u0000\u0000\u02f9\u02fa\u0007"+
		"\f\u0000\u0000\u02fa\u02fb\u0005U\u0000\u0000\u02fb\u02fc\u0003b1\u0000"+
		"\u02fc\u02fd\u0005V\u0000\u0000\u02fda\u0001\u0000\u0000\u0000\u02fe\u0301"+
		"\u00032\u0019\u0000\u02ff\u0301\u0003x<\u0000\u0300\u02fe\u0001\u0000"+
		"\u0000\u0000\u0300\u02ff\u0001\u0000\u0000\u0000\u0301c\u0001\u0000\u0000"+
		"\u0000\u0302\u0303\u0007\r\u0000\u0000\u0303e\u0001\u0000\u0000\u0000"+
		"\u0304\u0316\u00052\u0000\u0000\u0305\u0316\u0005R\u0000\u0000\u0306\u0316"+
		"\u0005\u0010\u0000\u0000\u0307\u0316\u0003\u00dam\u0000\u0308\u0309\u0005"+
		"\n\u0000\u0000\u0309\u0312\u0005U\u0000\u0000\u030a\u0313\u0005\u0083"+
		"\u0000\u0000\u030b\u0313\u00058\u0000\u0000\u030c\u030d\u0005\'\u0000"+
		"\u0000\u030d\u030f\u0005U\u0000\u0000\u030e\u0310\u0005\u0088\u0000\u0000"+
		"\u030f\u030e\u0001\u0000\u0000\u0000\u030f\u0310\u0001\u0000\u0000\u0000"+
		"\u0310\u0311\u0001\u0000\u0000\u0000\u0311\u0313\u0005V\u0000\u0000\u0312"+
		"\u030a\u0001\u0000\u0000\u0000\u0312\u030b\u0001\u0000\u0000\u0000\u0312"+
		"\u030c\u0001\u0000\u0000\u0000\u0313\u0314\u0001\u0000\u0000\u0000\u0314"+
		"\u0316\u0005V\u0000\u0000\u0315\u0304\u0001\u0000\u0000\u0000\u0315\u0305"+
		"\u0001\u0000\u0000\u0000\u0315\u0306\u0001\u0000\u0000\u0000\u0315\u0307"+
		"\u0001\u0000\u0000\u0000\u0315\u0308\u0001\u0000\u0000\u0000\u0316g\u0001"+
		"\u0000\u0000\u0000\u0317\u0318\u0005\u0018\u0000\u0000\u0318\u031b\u0005"+
		"U\u0000\u0000\u0319\u031c\u0003x<\u0000\u031a\u031c\u00034\u001a\u0000"+
		"\u031b\u0319\u0001\u0000\u0000\u0000\u031b\u031a\u0001\u0000\u0000\u0000"+
		"\u031c\u031d\u0001\u0000\u0000\u0000\u031d\u031e\u0005V\u0000\u0000\u031e"+
		"i\u0001\u0000\u0000\u0000\u031f\u0321\u0003\u00dam\u0000\u0320\u031f\u0001"+
		"\u0000\u0000\u0000\u0320\u0321\u0001\u0000\u0000\u0000\u0321\u0322\u0001"+
		"\u0000\u0000\u0000\u0322\u0324\u0003n7\u0000\u0323\u0320\u0001\u0000\u0000"+
		"\u0000\u0324\u0327\u0001\u0000\u0000\u0000\u0325\u0323\u0001\u0000\u0000"+
		"\u0000\u0325\u0326\u0001\u0000\u0000\u0000\u0326\u032b\u0001\u0000\u0000"+
		"\u0000\u0327\u0325\u0001\u0000\u0000\u0000\u0328\u032a\u0003\u00dam\u0000"+
		"\u0329\u0328\u0001\u0000\u0000\u0000\u032a\u032d\u0001\u0000\u0000\u0000"+
		"\u032b\u0329\u0001\u0000\u0000\u0000\u032b\u032c\u0001\u0000\u0000\u0000"+
		"\u032c\u032e\u0001\u0000\u0000\u0000\u032d\u032b\u0001\u0000\u0000\u0000"+
		"\u032e\u0332\u0003l6\u0000\u032f\u0331\u0003\u00d8l\u0000\u0330\u032f"+
		"\u0001\u0000\u0000\u0000\u0331\u0334\u0001\u0000\u0000\u0000\u0332\u0330"+
		"\u0001\u0000\u0000\u0000\u0332\u0333\u0001\u0000\u0000\u0000\u0333\u0335"+
		"\u0001\u0000\u0000\u0000\u0334\u0332\u0001\u0000\u0000\u0000\u0335\u0336"+
		"\u00065\uffff\uffff\u0000\u0336k\u0001\u0000\u0000\u0000\u0337\u0339\u0005"+
		"\u0083\u0000\u0000\u0338\u033a\u0003\u008cF\u0000\u0339\u0338\u0001\u0000"+
		"\u0000\u0000\u0339\u033a\u0001\u0000\u0000\u0000\u033a\u034c\u0001\u0000"+
		"\u0000\u0000\u033b\u033c\u0005U\u0000\u0000\u033c\u033d\u0003j5\u0000"+
		"\u033d\u033e\u0005V\u0000\u0000\u033e\u034c\u0001\u0000\u0000\u0000\u033f"+
		"\u0340\u0005\u0083\u0000\u0000\u0340\u0341\u0005p\u0000\u0000\u0341\u034c"+
		"\u0005\u0086\u0000\u0000\u0342\u0343\u0003\u00e4r\u0000\u0343\u0344\u0005"+
		"\u0083\u0000\u0000\u0344\u034c\u0001\u0000\u0000\u0000\u0345\u0346\u0005"+
		"U\u0000\u0000\u0346\u0347\u0003\u00e4r\u0000\u0347\u0348\u0003j5\u0000"+
		"\u0348\u0349\u0005V\u0000\u0000\u0349\u034c\u0001\u0000\u0000\u0000\u034a"+
		"\u034c\u0003\u00dam\u0000\u034b\u0337\u0001\u0000\u0000\u0000\u034b\u033b"+
		"\u0001\u0000\u0000\u0000\u034b\u033f\u0001\u0000\u0000\u0000\u034b\u0342"+
		"\u0001\u0000\u0000\u0000\u034b\u0345\u0001\u0000\u0000\u0000\u034b\u034a"+
		"\u0001\u0000\u0000\u0000\u034c\u037b\u0001\u0000\u0000\u0000\u034d\u034f"+
		"\u0005W\u0000\u0000\u034e\u0350\u0003p8\u0000\u034f\u034e\u0001\u0000"+
		"\u0000\u0000\u034f\u0350\u0001\u0000\u0000\u0000\u0350\u0352\u0001\u0000"+
		"\u0000\u0000\u0351\u0353\u00030\u0018\u0000\u0352\u0351\u0001\u0000\u0000"+
		"\u0000\u0352\u0353\u0001\u0000\u0000\u0000\u0353\u0354\u0001\u0000\u0000"+
		"\u0000\u0354\u0356\u0005X\u0000\u0000\u0355\u0357\u0003\u008cF\u0000\u0356"+
		"\u0355\u0001\u0000\u0000\u0000\u0356\u0357\u0001\u0000\u0000\u0000\u0357"+
		"\u037a\u0001\u0000\u0000\u0000\u0358\u0359\u0005W\u0000\u0000\u0359\u035b"+
		"\u0005=\u0000\u0000\u035a\u035c\u0003p8\u0000\u035b\u035a\u0001\u0000"+
		"\u0000\u0000\u035b\u035c\u0001\u0000\u0000\u0000\u035c\u035d\u0001\u0000"+
		"\u0000\u0000\u035d\u035e\u00030\u0018\u0000\u035e\u0360\u0005X\u0000\u0000"+
		"\u035f\u0361\u0003\u008cF\u0000\u0360\u035f\u0001\u0000\u0000\u0000\u0360"+
		"\u0361\u0001\u0000\u0000\u0000\u0361\u037a\u0001\u0000\u0000\u0000\u0362"+
		"\u0363\u0005W\u0000\u0000\u0363\u0364\u0003p8\u0000\u0364\u0365\u0005"+
		"=\u0000\u0000\u0365\u0366\u00030\u0018\u0000\u0366\u0368\u0005X\u0000"+
		"\u0000\u0367\u0369\u0003\u008cF\u0000\u0368\u0367\u0001\u0000\u0000\u0000"+
		"\u0368\u0369\u0001\u0000\u0000\u0000\u0369\u037a\u0001\u0000\u0000\u0000"+
		"\u036a\u036c\u0005W\u0000\u0000\u036b\u036d\u0003p8\u0000\u036c\u036b"+
		"\u0001\u0000\u0000\u0000\u036c\u036d\u0001\u0000\u0000\u0000\u036d\u036e"+
		"\u0001\u0000\u0000\u0000\u036e\u036f\u0005e\u0000\u0000\u036f\u0371\u0005"+
		"X\u0000\u0000\u0370\u0372\u0003\u008cF\u0000\u0371\u0370\u0001\u0000\u0000"+
		"\u0000\u0371\u0372\u0001\u0000\u0000\u0000\u0372\u037a\u0001\u0000\u0000"+
		"\u0000\u0373\u0374\u0005U\u0000\u0000\u0374\u0375\u0003r9\u0000\u0375"+
		"\u0377\u0005V\u0000\u0000\u0376\u0378\u0003\u008cF\u0000\u0377\u0376\u0001"+
		"\u0000\u0000\u0000\u0377\u0378\u0001\u0000\u0000\u0000\u0378\u037a\u0001"+
		"\u0000\u0000\u0000\u0379\u034d\u0001\u0000\u0000\u0000\u0379\u0358\u0001"+
		"\u0000\u0000\u0000\u0379\u0362\u0001\u0000\u0000\u0000\u0379\u036a\u0001"+
		"\u0000\u0000\u0000\u0379\u0373\u0001\u0000\u0000\u0000\u037a\u037d\u0001"+
		"\u0000\u0000\u0000\u037b\u0379\u0001\u0000\u0000\u0000\u037b\u037c\u0001"+
		"\u0000\u0000\u0000\u037cm\u0001\u0000\u0000\u0000\u037d\u037b\u0001\u0000"+
		"\u0000\u0000\u037e\u0380\u0007\u000e\u0000\u0000\u037f\u0381\u0003p8\u0000"+
		"\u0380\u037f\u0001\u0000\u0000\u0000\u0380\u0381\u0001\u0000\u0000\u0000"+
		"\u0381\u0383\u0001\u0000\u0000\u0000\u0382\u037e\u0001\u0000\u0000\u0000"+
		"\u0383\u0384\u0001\u0000\u0000\u0000\u0384\u0382\u0001\u0000\u0000\u0000"+
		"\u0384\u0385\u0001\u0000\u0000\u0000\u0385o\u0001\u0000\u0000\u0000\u0386"+
		"\u0388\u0003d2\u0000\u0387\u0386\u0001\u0000\u0000\u0000\u0388\u0389\u0001"+
		"\u0000\u0000\u0000\u0389\u0387\u0001\u0000\u0000\u0000\u0389\u038a\u0001"+
		"\u0000\u0000\u0000\u038aq\u0001\u0000\u0000\u0000\u038b\u038e\u0003t:"+
		"\u0000\u038c\u038d\u0005r\u0000\u0000\u038d\u038f\u0005\u0082\u0000\u0000"+
		"\u038e\u038c\u0001\u0000\u0000\u0000\u038e\u038f\u0001\u0000\u0000\u0000"+
		"\u038f\u0392\u0001\u0000\u0000\u0000\u0390\u0392\u0005\u0082\u0000\u0000"+
		"\u0391\u038b\u0001\u0000\u0000\u0000\u0391\u0390\u0001\u0000\u0000\u0000"+
		"\u0392s\u0001\u0000\u0000\u0000\u0393\u0398\u0003v;\u0000\u0394\u0395"+
		"\u0005r\u0000\u0000\u0395\u0397\u0003v;\u0000\u0396\u0394\u0001\u0000"+
		"\u0000\u0000\u0397\u039a\u0001\u0000\u0000\u0000\u0398\u0396\u0001\u0000"+
		"\u0000\u0000\u0398\u0399\u0001\u0000\u0000\u0000\u0399u\u0001\u0000\u0000"+
		"\u0000\u039a\u0398\u0001\u0000\u0000\u0000\u039b\u039d\u0003\u008cF\u0000"+
		"\u039c\u039b\u0001\u0000\u0000\u0000\u039c\u039d\u0001\u0000\u0000\u0000"+
		"\u039d\u03a1\u0001\u0000\u0000\u0000\u039e\u039f\u0004;\r\u0000\u039f"+
		"\u03a2\u00038\u001c\u0000\u03a0\u03a2\u0001\u0000\u0000\u0000\u03a1\u039e"+
		"\u0001\u0000\u0000\u0000\u03a1\u03a0\u0001\u0000\u0000\u0000\u03a2\u03a6"+
		"\u0001\u0000\u0000\u0000\u03a3\u03a7\u0003j5\u0000\u03a4\u03a7\u0003z"+
		"=\u0000\u03a5\u03a7\u0001\u0000\u0000\u0000\u03a6\u03a3\u0001\u0000\u0000"+
		"\u0000\u03a6\u03a4\u0001\u0000\u0000\u0000\u03a6\u03a5\u0001\u0000\u0000"+
		"\u0000\u03a7w\u0001\u0000\u0000\u0000\u03a8\u03aa\u0003N\'\u0000\u03a9"+
		"\u03ab\u0003z=\u0000\u03aa\u03a9\u0001\u0000\u0000\u0000\u03aa\u03ab\u0001"+
		"\u0000\u0000\u0000\u03aby\u0001\u0000\u0000\u0000\u03ac\u03ae\u0003\u00e4"+
		"r\u0000\u03ad\u03ac\u0001\u0000\u0000\u0000\u03ad\u03ae\u0001\u0000\u0000"+
		"\u0000\u03ae\u03af\u0001\u0000\u0000\u0000\u03af\u03be\u0003n7\u0000\u03b0"+
		"\u03b2\u0003\u00e4r\u0000\u03b1\u03b0\u0001\u0000\u0000\u0000\u03b1\u03b2"+
		"\u0001\u0000\u0000\u0000\u03b2\u03b4\u0001\u0000\u0000\u0000\u03b3\u03b5"+
		"\u0003n7\u0000\u03b4\u03b3\u0001\u0000\u0000\u0000\u03b4\u03b5\u0001\u0000"+
		"\u0000\u0000\u03b5\u03b6\u0001\u0000\u0000\u0000\u03b6\u03ba\u0003|>\u0000"+
		"\u03b7\u03b9\u0003\u00d8l\u0000\u03b8\u03b7\u0001\u0000\u0000\u0000\u03b9"+
		"\u03bc\u0001\u0000\u0000\u0000\u03ba\u03b8\u0001\u0000\u0000\u0000\u03ba"+
		"\u03bb\u0001\u0000\u0000\u0000\u03bb\u03be\u0001\u0000\u0000\u0000\u03bc"+
		"\u03ba\u0001\u0000\u0000\u0000\u03bd\u03ad\u0001\u0000\u0000\u0000\u03bd"+
		"\u03b1\u0001\u0000\u0000\u0000\u03be{\u0001\u0000\u0000\u0000\u03bf\u03c0"+
		"\u0006>\uffff\uffff\u0000\u03c0\u03c1\u0005U\u0000\u0000\u03c1\u03c2\u0003"+
		"z=\u0000\u03c2\u03c6\u0005V\u0000\u0000\u03c3\u03c5\u0003\u00d8l\u0000"+
		"\u03c4\u03c3\u0001\u0000\u0000\u0000\u03c5\u03c8\u0001\u0000\u0000\u0000"+
		"\u03c6\u03c4\u0001\u0000\u0000\u0000\u03c6\u03c7\u0001\u0000\u0000\u0000"+
		"\u03c7\u03ec\u0001\u0000\u0000\u0000\u03c8\u03c6\u0001\u0000\u0000\u0000"+
		"\u03c9\u03cb\u0005W\u0000\u0000\u03ca\u03cc\u0003p8\u0000\u03cb\u03ca"+
		"\u0001\u0000\u0000\u0000\u03cb\u03cc\u0001\u0000\u0000\u0000\u03cc\u03ce"+
		"\u0001\u0000\u0000\u0000\u03cd\u03cf\u00030\u0018\u0000\u03ce\u03cd\u0001"+
		"\u0000\u0000\u0000\u03ce\u03cf\u0001\u0000\u0000\u0000\u03cf\u03d0\u0001"+
		"\u0000\u0000\u0000\u03d0\u03ec\u0005X\u0000\u0000\u03d1\u03d2\u0005W\u0000"+
		"\u0000\u03d2\u03d4\u0005=\u0000\u0000\u03d3\u03d5\u0003p8\u0000\u03d4"+
		"\u03d3\u0001\u0000\u0000\u0000\u03d4\u03d5\u0001\u0000\u0000\u0000\u03d5"+
		"\u03d6\u0001\u0000\u0000\u0000\u03d6\u03d7\u00030\u0018\u0000\u03d7\u03d8"+
		"\u0005X\u0000\u0000\u03d8\u03ec\u0001\u0000\u0000\u0000\u03d9\u03da\u0005"+
		"W\u0000\u0000\u03da\u03db\u0003p8\u0000\u03db\u03dc\u0005=\u0000\u0000"+
		"\u03dc\u03dd\u00030\u0018\u0000\u03dd\u03de\u0005X\u0000\u0000\u03de\u03ec"+
		"\u0001\u0000\u0000\u0000\u03df\u03e0\u0005W\u0000\u0000\u03e0\u03e1\u0005"+
		"e\u0000\u0000\u03e1\u03ec\u0005X\u0000\u0000\u03e2\u03e3\u0005U\u0000"+
		"\u0000\u03e3\u03e4\u0003r9\u0000\u03e4\u03e8\u0005V\u0000\u0000\u03e5"+
		"\u03e7\u0003\u00d8l\u0000\u03e6\u03e5\u0001\u0000\u0000\u0000\u03e7\u03ea"+
		"\u0001\u0000\u0000\u0000\u03e8\u03e6\u0001\u0000\u0000\u0000\u03e8\u03e9"+
		"\u0001\u0000\u0000\u0000\u03e9\u03ec\u0001\u0000\u0000\u0000\u03ea\u03e8"+
		"\u0001\u0000\u0000\u0000\u03eb\u03bf\u0001\u0000\u0000\u0000\u03eb\u03c9"+
		"\u0001\u0000\u0000\u0000\u03eb\u03d1\u0001\u0000\u0000\u0000\u03eb\u03d9"+
		"\u0001\u0000\u0000\u0000\u03eb\u03df\u0001\u0000\u0000\u0000\u03eb\u03e2"+
		"\u0001\u0000\u0000\u0000\u03ec\u0416\u0001\u0000\u0000\u0000\u03ed\u03ee"+
		"\n\u0005\u0000\u0000\u03ee\u03f0\u0005W\u0000\u0000\u03ef\u03f1\u0003"+
		"p8\u0000\u03f0\u03ef\u0001\u0000\u0000\u0000\u03f0\u03f1\u0001\u0000\u0000"+
		"\u0000\u03f1\u03f3\u0001\u0000\u0000\u0000\u03f2\u03f4\u00030\u0018\u0000"+
		"\u03f3\u03f2\u0001\u0000\u0000\u0000\u03f3\u03f4\u0001\u0000\u0000\u0000"+
		"\u03f4\u03f5\u0001\u0000\u0000\u0000\u03f5\u0415\u0005X\u0000\u0000\u03f6"+
		"\u03f7\n\u0004\u0000\u0000\u03f7\u03f8\u0005W\u0000\u0000\u03f8\u03fa"+
		"\u0005=\u0000\u0000\u03f9\u03fb\u0003p8\u0000\u03fa\u03f9\u0001\u0000"+
		"\u0000\u0000\u03fa\u03fb\u0001\u0000\u0000\u0000\u03fb\u03fc\u0001\u0000"+
		"\u0000\u0000\u03fc\u03fd\u00030\u0018\u0000\u03fd\u03fe\u0005X\u0000\u0000"+
		"\u03fe\u0415\u0001\u0000\u0000\u0000\u03ff\u0400\n\u0003\u0000\u0000\u0400"+
		"\u0401\u0005W\u0000\u0000\u0401\u0402\u0003p8\u0000\u0402\u0403\u0005"+
		"=\u0000\u0000\u0403\u0404\u00030\u0018\u0000\u0404\u0405\u0005X\u0000"+
		"\u0000\u0405\u0415\u0001\u0000\u0000\u0000\u0406\u0407\n\u0002\u0000\u0000"+
		"\u0407\u0408\u0005W\u0000\u0000\u0408\u0409\u0005e\u0000\u0000\u0409\u0415"+
		"\u0005X\u0000\u0000\u040a\u040b\n\u0001\u0000\u0000\u040b\u040c\u0005"+
		"U\u0000\u0000\u040c\u040d\u0003r9\u0000\u040d\u0411\u0005V\u0000\u0000"+
		"\u040e\u0410\u0003\u00d8l\u0000\u040f\u040e\u0001\u0000\u0000\u0000\u0410"+
		"\u0413\u0001\u0000\u0000\u0000\u0411\u040f\u0001\u0000\u0000\u0000\u0411"+
		"\u0412\u0001\u0000\u0000\u0000\u0412\u0415\u0001\u0000\u0000\u0000\u0413"+
		"\u0411\u0001\u0000\u0000\u0000\u0414\u03ed\u0001\u0000\u0000\u0000\u0414"+
		"\u03f6\u0001\u0000\u0000\u0000\u0414\u03ff\u0001\u0000\u0000\u0000\u0414"+
		"\u0406\u0001\u0000\u0000\u0000\u0414\u040a\u0001\u0000\u0000\u0000\u0415"+
		"\u0418\u0001\u0000\u0000\u0000\u0416\u0414\u0001\u0000\u0000\u0000\u0416"+
		"\u0417\u0001\u0000\u0000\u0000\u0417}\u0001\u0000\u0000\u0000\u0418\u0416"+
		"\u0001\u0000\u0000\u0000\u0419\u041a\u0004?\u0013\u0000\u041a\u041b\u0005"+
		"\u0083\u0000\u0000\u041b\u007f\u0001\u0000\u0000\u0000\u041c\u0427\u0003"+
		"0\u0018\u0000\u041d\u041e\u0005Y\u0000\u0000\u041e\u0420\u0003\u0082A"+
		"\u0000\u041f\u0421\u0005r\u0000\u0000\u0420\u041f\u0001\u0000\u0000\u0000"+
		"\u0420\u0421\u0001\u0000\u0000\u0000\u0421\u0422\u0001\u0000\u0000\u0000"+
		"\u0422\u0423\u0005Z\u0000\u0000\u0423\u0427\u0001\u0000\u0000\u0000\u0424"+
		"\u0425\u0005Y\u0000\u0000\u0425\u0427\u0005Z\u0000\u0000\u0426\u041c\u0001"+
		"\u0000\u0000\u0000\u0426\u041d\u0001\u0000\u0000\u0000\u0426\u0424\u0001"+
		"\u0000\u0000\u0000\u0427\u0081\u0001\u0000\u0000\u0000\u0428\u042a\u0003"+
		"\u0084B\u0000\u0429\u0428\u0001\u0000\u0000\u0000\u0429\u042a\u0001\u0000"+
		"\u0000\u0000\u042a\u042b\u0001\u0000\u0000\u0000\u042b\u0433\u0003\u0080"+
		"@\u0000\u042c\u042e\u0005r\u0000\u0000\u042d\u042f\u0003\u0084B\u0000"+
		"\u042e\u042d\u0001\u0000\u0000\u0000\u042e\u042f\u0001\u0000\u0000\u0000"+
		"\u042f\u0430\u0001\u0000\u0000\u0000\u0430\u0432\u0003\u0080@\u0000\u0431"+
		"\u042c\u0001\u0000\u0000\u0000\u0432\u0435\u0001\u0000\u0000\u0000\u0433"+
		"\u0431\u0001\u0000\u0000\u0000\u0433\u0434\u0001\u0000\u0000\u0000\u0434"+
		"\u0083\u0001\u0000\u0000\u0000\u0435\u0433\u0001\u0000\u0000\u0000\u0436"+
		"\u0437\u0003\u0086C\u0000\u0437\u0438\u0005s\u0000\u0000\u0438\u043e\u0001"+
		"\u0000\u0000\u0000\u0439\u043e\u0003\u00c0`\u0000\u043a\u043b\u0003\u00c2"+
		"a\u0000\u043b\u043c\u0005p\u0000\u0000\u043c\u043e\u0001\u0000\u0000\u0000"+
		"\u043d\u0436\u0001\u0000\u0000\u0000\u043d\u0439\u0001\u0000\u0000\u0000"+
		"\u043d\u043a\u0001\u0000\u0000\u0000\u043e\u0085\u0001\u0000\u0000\u0000"+
		"\u043f\u0441\u0003\u0088D\u0000\u0440\u043f\u0001\u0000\u0000\u0000\u0441"+
		"\u0442\u0001\u0000\u0000\u0000\u0442\u0440\u0001\u0000\u0000\u0000\u0442"+
		"\u0443\u0001\u0000\u0000\u0000\u0443\u0087\u0001\u0000\u0000\u0000\u0444"+
		"\u0448\u0003\u00c0`\u0000\u0445\u0446\u0005\u0081\u0000\u0000\u0446\u0448"+
		"\u0005\u0083\u0000\u0000\u0447\u0444\u0001\u0000\u0000\u0000\u0447\u0445"+
		"\u0001\u0000\u0000\u0000\u0448\u0089\u0001\u0000\u0000\u0000\u0449\u044a"+
		"\u0005S\u0000\u0000\u044a\u044b\u0005U\u0000\u0000\u044b\u044e\u00034"+
		"\u001a\u0000\u044c\u044d\u0005r\u0000\u0000\u044d\u044f\u0005\u0088\u0000"+
		"\u0000\u044e\u044c\u0001\u0000\u0000\u0000\u044e\u044f\u0001\u0000\u0000"+
		"\u0000\u044f\u0450\u0001\u0000\u0000\u0000\u0450\u0451\u0005V\u0000\u0000"+
		"\u0451\u0452\u0005q\u0000\u0000\u0452\u008b\u0001\u0000\u0000\u0000\u0453"+
		"\u0455\u0003\u008eG\u0000\u0454\u0453\u0001\u0000\u0000\u0000\u0455\u0456"+
		"\u0001\u0000\u0000\u0000\u0456\u0454\u0001\u0000\u0000\u0000\u0456\u0457"+
		"\u0001\u0000\u0000\u0000\u0457\u008d\u0001\u0000\u0000\u0000\u0458\u0459"+
		"\u0005W\u0000\u0000\u0459\u045a\u0005W\u0000\u0000\u045a\u045b\u0003\u0090"+
		"H\u0000\u045b\u045c\u0005X\u0000\u0000\u045c\u045d\u0005X\u0000\u0000"+
		"\u045d\u008f\u0001\u0000\u0000\u0000\u045e\u0463\u0003\u0092I\u0000\u045f"+
		"\u0460\u0005r\u0000\u0000\u0460\u0462\u0003\u0092I\u0000\u0461\u045f\u0001"+
		"\u0000\u0000\u0000\u0462\u0465\u0001\u0000\u0000\u0000\u0463\u0461\u0001"+
		"\u0000\u0000\u0000\u0463\u0464\u0001\u0000\u0000\u0000\u0464\u0091\u0001"+
		"\u0000\u0000\u0000\u0465\u0463\u0001\u0000\u0000\u0000\u0466\u0468\u0003"+
		"\u0094J\u0000\u0467\u0469\u0003\u0096K\u0000\u0468\u0467\u0001\u0000\u0000"+
		"\u0000\u0468\u0469\u0001\u0000\u0000\u0000\u0469\u0093\u0001\u0000\u0000"+
		"\u0000\u046a\u0470\u0005\u0083\u0000\u0000\u046b\u046c\u0005\u0083\u0000"+
		"\u0000\u046c\u046d\u0005p\u0000\u0000\u046d\u046e\u0005p\u0000\u0000\u046e"+
		"\u0470\u0005\u0083\u0000\u0000\u046f\u046a\u0001\u0000\u0000\u0000\u046f"+
		"\u046b\u0001\u0000\u0000\u0000\u0470\u0095\u0001\u0000\u0000\u0000\u0471"+
		"\u0473\u0005U\u0000\u0000\u0472\u0474\u0003\u0098L\u0000\u0473\u0472\u0001"+
		"\u0000\u0000\u0000\u0473\u0474\u0001\u0000\u0000\u0000\u0474\u0475\u0001"+
		"\u0000\u0000\u0000\u0475\u0476\u0005V\u0000\u0000\u0476\u0097\u0001\u0000"+
		"\u0000\u0000\u0477\u0479\u0003\u009aM\u0000\u0478\u0477\u0001\u0000\u0000"+
		"\u0000\u0479\u047a\u0001\u0000\u0000\u0000\u047a\u0478\u0001\u0000\u0000"+
		"\u0000\u047a\u047b\u0001\u0000\u0000\u0000\u047b\u0099\u0001\u0000\u0000"+
		"\u0000\u047c\u047e\u0005U\u0000\u0000\u047d\u047f\u0003\u0098L\u0000\u047e"+
		"\u047d\u0001\u0000\u0000\u0000\u047e\u047f\u0001\u0000\u0000\u0000\u047f"+
		"\u0480\u0001\u0000\u0000\u0000\u0480\u048c\u0005V\u0000\u0000\u0481\u0483"+
		"\u0005W\u0000\u0000\u0482\u0484\u0003\u0098L\u0000\u0483\u0482\u0001\u0000"+
		"\u0000\u0000\u0483\u0484\u0001\u0000\u0000\u0000\u0484\u0485\u0001\u0000"+
		"\u0000\u0000\u0485\u048c\u0005X\u0000\u0000\u0486\u0488\u0005Y\u0000\u0000"+
		"\u0487\u0489\u0003\u0098L\u0000\u0488\u0487\u0001\u0000\u0000\u0000\u0488"+
		"\u0489\u0001\u0000\u0000\u0000\u0489\u048a\u0001\u0000\u0000\u0000\u048a"+
		"\u048c\u0005Z\u0000\u0000\u048b\u047c\u0001\u0000\u0000\u0000\u048b\u0481"+
		"\u0001\u0000\u0000\u0000\u048b\u0486\u0001\u0000\u0000\u0000\u048c\u009b"+
		"\u0001\u0000\u0000\u0000\u048d\u0495\u0003\u009eO\u0000\u048e\u0495\u0003"+
		"\u00a0P\u0000\u048f\u0495\u0003\u00a6S\u0000\u0490\u0495\u0003\u00a8T"+
		"\u0000\u0491\u0495\u0003\u00aaU\u0000\u0492\u0495\u0003\u00b2Y\u0000\u0493"+
		"\u0495\u0003\u00d4j\u0000\u0494\u048d\u0001\u0000\u0000\u0000\u0494\u048e"+
		"\u0001\u0000\u0000\u0000\u0494\u048f\u0001\u0000\u0000\u0000\u0494\u0490"+
		"\u0001\u0000\u0000\u0000\u0494\u0491\u0001\u0000\u0000\u0000\u0494\u0492"+
		"\u0001\u0000\u0000\u0000\u0494\u0493\u0001\u0000\u0000\u0000\u0495\u009d"+
		"\u0001\u0000\u0000\u0000\u0496\u0497\u0005\u0083\u0000\u0000\u0497\u0499"+
		"\u0005p\u0000\u0000\u0498\u049a\u0003\u009cN\u0000\u0499\u0498\u0001\u0000"+
		"\u0000\u0000\u0499\u049a\u0001\u0000\u0000\u0000\u049a\u04a7\u0001\u0000"+
		"\u0000\u0000\u049b\u049c\u00054\u0000\u0000\u049c\u049d\u0005\u0083\u0000"+
		"\u0000\u049d\u04a7\u0005q\u0000\u0000\u049e\u049f\u0005!\u0000\u0000\u049f"+
		"\u04a0\u00034\u001a\u0000\u04a0\u04a1\u0005p\u0000\u0000\u04a1\u04a2\u0003"+
		"\u009cN\u0000\u04a2\u04a7\u0001\u0000\u0000\u0000\u04a3\u04a4\u0005&\u0000"+
		"\u0000\u04a4\u04a5\u0005p\u0000\u0000\u04a5\u04a7\u0003\u009cN\u0000\u04a6"+
		"\u0496\u0001\u0000\u0000\u0000\u04a6\u049b\u0001\u0000\u0000\u0000\u04a6"+
		"\u049e\u0001\u0000\u0000\u0000\u04a6\u04a3\u0001\u0000\u0000\u0000\u04a7"+
		"\u009f\u0001\u0000\u0000\u0000\u04a8\u04a9\u0005Y\u0000\u0000\u04a9\u04ab"+
		"\u0006P\uffff\uffff\u0000\u04aa\u04ac\u0003\u00a2Q\u0000\u04ab\u04aa\u0001"+
		"\u0000\u0000\u0000\u04ab\u04ac\u0001\u0000\u0000\u0000\u04ac\u04ad\u0001"+
		"\u0000\u0000\u0000\u04ad\u04ae\u0005Z\u0000\u0000\u04ae\u04af\u0006P\uffff"+
		"\uffff\u0000\u04af\u00a1\u0001\u0000\u0000\u0000\u04b0\u04b2\u0003\u00a4"+
		"R\u0000\u04b1\u04b0\u0001\u0000\u0000\u0000\u04b2\u04b3\u0001\u0000\u0000"+
		"\u0000\u04b3\u04b1\u0001\u0000\u0000\u0000\u04b3\u04b4\u0001\u0000\u0000"+
		"\u0000\u04b4\u00a3\u0001\u0000\u0000\u0000\u04b5\u04b6\u0004R\u0014\u0000"+
		"\u04b6\u04ba\u0003\u009cN\u0000\u04b7\u04b8\u0004R\u0015\u0000\u04b8\u04ba"+
		"\u00036\u001b\u0000\u04b9\u04b5\u0001\u0000\u0000\u0000\u04b9\u04b7\u0001"+
		"\u0000\u0000\u0000\u04ba\u00a5\u0001\u0000\u0000\u0000\u04bb\u04bd\u0003"+
		"2\u0019\u0000\u04bc\u04bb\u0001\u0000\u0000\u0000\u04bc\u04bd\u0001\u0000"+
		"\u0000\u0000\u04bd\u04be\u0001\u0000\u0000\u0000\u04be\u04bf\u0005q\u0000"+
		"\u0000\u04bf\u00a7\u0001\u0000\u0000\u0000\u04c0\u04c1\u00051\u0000\u0000"+
		"\u04c1\u04c2\u0005U\u0000\u0000\u04c2\u04c3\u00032\u0019\u0000\u04c3\u04c4"+
		"\u0005V\u0000\u0000\u04c4\u04c7\u0003\u009cN\u0000\u04c5\u04c6\u0005*"+
		"\u0000\u0000\u04c6\u04c8\u0003\u009cN\u0000\u04c7\u04c5\u0001\u0000\u0000"+
		"\u0000\u04c7\u04c8\u0001\u0000\u0000\u0000\u04c8\u04d0\u0001\u0000\u0000"+
		"\u0000\u04c9\u04ca\u0005@\u0000\u0000\u04ca\u04cb\u0005U\u0000\u0000\u04cb"+
		"\u04cc\u00032\u0019\u0000\u04cc\u04cd\u0005V\u0000\u0000\u04cd\u04ce\u0003"+
		"\u009cN\u0000\u04ce\u04d0\u0001\u0000\u0000\u0000\u04cf\u04c0\u0001\u0000"+
		"\u0000\u0000\u04cf\u04c9\u0001\u0000\u0000\u0000\u04d0\u00a9\u0001\u0000"+
		"\u0000\u0000\u04d1\u04d2\u0005I\u0000\u0000\u04d2\u04d3\u0005U\u0000\u0000"+
		"\u04d3\u04d4\u00032\u0019\u0000\u04d4\u04d5\u0005V\u0000\u0000\u04d5\u04d6"+
		"\u0003\u009cN\u0000\u04d6\u04e6\u0001\u0000\u0000\u0000\u04d7\u04d8\u0005"+
		"(\u0000\u0000\u04d8\u04d9\u0003\u009cN\u0000\u04d9\u04da\u0005I\u0000"+
		"\u0000\u04da\u04db\u0005U\u0000\u0000\u04db\u04dc\u00032\u0019\u0000\u04dc"+
		"\u04dd\u0005V\u0000\u0000\u04dd\u04de\u0005q\u0000\u0000\u04de\u04e6\u0001"+
		"\u0000\u0000\u0000\u04df\u04e0\u0005/\u0000\u0000\u04e0\u04e1\u0005U\u0000"+
		"\u0000\u04e1\u04e2\u0003\u00acV\u0000\u04e2\u04e3\u0005V\u0000\u0000\u04e3"+
		"\u04e4\u0003\u009cN\u0000\u04e4\u04e6\u0001\u0000\u0000\u0000\u04e5\u04d1"+
		"\u0001\u0000\u0000\u0000\u04e5\u04d7\u0001\u0000\u0000\u0000\u04e5\u04df"+
		"\u0001\u0000\u0000\u0000\u04e6\u00ab\u0001\u0000\u0000\u0000\u04e7\u04ec"+
		"\u0003\u00aeW\u0000\u04e8\u04ea\u00032\u0019\u0000\u04e9\u04e8\u0001\u0000"+
		"\u0000\u0000\u04e9\u04ea\u0001\u0000\u0000\u0000\u04ea\u04ec\u0001\u0000"+
		"\u0000\u0000\u04eb\u04e7\u0001\u0000\u0000\u0000\u04eb\u04e9\u0001\u0000"+
		"\u0000\u0000\u04ec\u04ed\u0001\u0000\u0000\u0000\u04ed\u04ef\u0005q\u0000"+
		"\u0000\u04ee\u04f0\u0003\u00b0X\u0000\u04ef\u04ee\u0001\u0000\u0000\u0000"+
		"\u04ef\u04f0\u0001\u0000\u0000\u0000\u04f0\u04f1\u0001\u0000\u0000\u0000"+
		"\u04f1\u04f3\u0005q\u0000\u0000\u04f2\u04f4\u0003\u00b0X\u0000\u04f3\u04f2"+
		"\u0001\u0000\u0000\u0000\u04f3\u04f4\u0001\u0000\u0000\u0000\u04f4\u00ad"+
		"\u0001\u0000\u0000\u0000\u04f5\u04f7\u00038\u001c\u0000\u04f6\u04f8\u0003"+
		"<\u001e\u0000\u04f7\u04f6\u0001\u0000\u0000\u0000\u04f7\u04f8\u0001\u0000"+
		"\u0000\u0000\u04f8\u00af\u0001\u0000\u0000\u0000\u04f9\u04fe\u00030\u0018"+
		"\u0000\u04fa\u04fb\u0005r\u0000\u0000\u04fb\u04fd\u00030\u0018\u0000\u04fc"+
		"\u04fa\u0001\u0000\u0000\u0000\u04fd\u0500\u0001\u0000\u0000\u0000\u04fe"+
		"\u04fc\u0001\u0000\u0000\u0000\u04fe\u04ff\u0001\u0000\u0000\u0000\u04ff"+
		"\u00b1\u0001\u0000\u0000\u0000\u0500\u04fe\u0001\u0000\u0000\u0000\u0501"+
		"\u0502\u00050\u0000\u0000\u0502\u050c\u0005\u0083\u0000\u0000\u0503\u050c"+
		"\u0005%\u0000\u0000\u0504\u050c\u0005 \u0000\u0000\u0505\u0507\u00059"+
		"\u0000\u0000\u0506\u0508\u00032\u0019\u0000\u0507\u0506\u0001\u0000\u0000"+
		"\u0000\u0507\u0508\u0001\u0000\u0000\u0000\u0508\u050c\u0001\u0000\u0000"+
		"\u0000\u0509\u050a\u00050\u0000\u0000\u050a\u050c\u0003\u0016\u000b\u0000"+
		"\u050b\u0501\u0001\u0000\u0000\u0000\u050b\u0503\u0001\u0000\u0000\u0000"+
		"\u050b\u0504\u0001\u0000\u0000\u0000\u050b\u0505\u0001\u0000\u0000\u0000"+
		"\u050b\u0509\u0001\u0000\u0000\u0000\u050c\u050d\u0001\u0000\u0000\u0000"+
		"\u050d\u050e\u0005q\u0000\u0000\u050e\u00b3\u0001\u0000\u0000\u0000\u050f"+
		"\u0511\u0003\u00b6[\u0000\u0510\u050f\u0001\u0000\u0000\u0000\u0511\u0512"+
		"\u0001\u0000\u0000\u0000\u0512\u0510\u0001\u0000\u0000\u0000\u0512\u0513"+
		"\u0001\u0000\u0000\u0000\u0513\u00b5\u0001\u0000\u0000\u0000\u0514\u0516"+
		"\u0005\u000b\u0000\u0000\u0515\u0514\u0001\u0000\u0000\u0000\u0515\u0516"+
		"\u0001\u0000\u0000\u0000\u0516\u051b\u0001\u0000\u0000\u0000\u0517\u051c"+
		"\u0003\u00b8\\\u0000\u0518\u051c\u00036\u001b\u0000\u0519\u051c\u0005"+
		"q\u0000\u0000\u051a\u051c\u0003\u00c8d\u0000\u051b\u0517\u0001\u0000\u0000"+
		"\u0000\u051b\u0518\u0001\u0000\u0000\u0000\u051b\u0519\u0001\u0000\u0000"+
		"\u0000\u051b\u051a\u0001\u0000\u0000\u0000\u051c\u00b7\u0001\u0000\u0000"+
		"\u0000\u051d\u051f\u0003\u008cF\u0000\u051e\u051d\u0001\u0000\u0000\u0000"+
		"\u051e\u051f\u0001\u0000\u0000\u0000\u051f\u0521\u0001\u0000\u0000\u0000"+
		"\u0520\u0522\u00038\u001c\u0000\u0521\u0520\u0001\u0000\u0000\u0000\u0521"+
		"\u0522\u0001\u0000\u0000\u0000\u0522\u0523\u0001\u0000\u0000\u0000\u0523"+
		"\u0525\u0003j5\u0000\u0524\u0526\u0003\u00ba]\u0000\u0525\u0524\u0001"+
		"\u0000\u0000\u0000\u0525\u0526\u0001\u0000\u0000\u0000\u0526\u0527\u0001"+
		"\u0000\u0000\u0000\u0527\u0528\u0003\u00bc^\u0000\u0528\u00b9\u0001\u0000"+
		"\u0000\u0000\u0529\u052b\u00036\u001b\u0000\u052a\u0529\u0001\u0000\u0000"+
		"\u0000\u052b\u052c\u0001\u0000\u0000\u0000\u052c\u052a\u0001\u0000\u0000"+
		"\u0000\u052c\u052d\u0001\u0000\u0000\u0000\u052d\u00bb\u0001\u0000\u0000"+
		"\u0000\u052e\u052f\u0003\u00a0P\u0000\u052f\u00bd\u0001\u0000\u0000\u0000"+
		"\u0530\u0535\u0005\u0083\u0000\u0000\u0531\u0532\u0005r\u0000\u0000\u0532"+
		"\u0534\u0005\u0083\u0000\u0000\u0533\u0531\u0001\u0000\u0000\u0000\u0534"+
		"\u0537\u0001\u0000\u0000\u0000\u0535\u0533\u0001\u0000\u0000\u0000\u0535"+
		"\u0536\u0001\u0000\u0000\u0000\u0536\u00bf\u0001\u0000\u0000\u0000\u0537"+
		"\u0535\u0001\u0000\u0000\u0000\u0538\u0539\u0005W\u0000\u0000\u0539\u053c"+
		"\u00034\u001a\u0000\u053a\u053b\u0005\u0082\u0000\u0000\u053b\u053d\u0003"+
		"4\u001a\u0000\u053c\u053a\u0001\u0000\u0000\u0000\u053c\u053d\u0001\u0000"+
		"\u0000\u0000\u053d\u053e\u0001\u0000\u0000\u0000\u053e\u053f\u0005X\u0000"+
		"\u0000\u053f\u00c1\u0001\u0000\u0000\u0000\u0540\u0541\u0005\u0083\u0000"+
		"\u0000\u0541\u00c3\u0001\u0000\u0000\u0000\u0542\u0558\u0003\u00d6k\u0000"+
		"\u0543\u0544\u0003\u00d6k\u0000\u0544\u0546\u0005p\u0000\u0000\u0545\u0547"+
		"\u0003\u00ceg\u0000\u0546\u0545\u0001\u0000\u0000\u0000\u0546\u0547\u0001"+
		"\u0000\u0000\u0000\u0547\u0555\u0001\u0000\u0000\u0000\u0548\u054a\u0005"+
		"p\u0000\u0000\u0549\u054b\u0003\u00ceg\u0000\u054a\u0549\u0001\u0000\u0000"+
		"\u0000\u054a\u054b\u0001\u0000\u0000\u0000\u054b\u0552\u0001\u0000\u0000"+
		"\u0000\u054c\u054e\u0005p\u0000\u0000\u054d\u054f\u0003\u00c6c\u0000\u054e"+
		"\u054d\u0001\u0000\u0000\u0000\u054e\u054f\u0001\u0000\u0000\u0000\u054f"+
		"\u0551\u0001\u0000\u0000\u0000\u0550\u054c\u0001\u0000\u0000\u0000\u0551"+
		"\u0554\u0001\u0000\u0000\u0000\u0552\u0550\u0001\u0000\u0000\u0000\u0552"+
		"\u0553\u0001\u0000\u0000\u0000\u0553\u0556\u0001\u0000\u0000\u0000\u0554"+
		"\u0552\u0001\u0000\u0000\u0000\u0555\u0548\u0001\u0000\u0000\u0000\u0555"+
		"\u0556\u0001\u0000\u0000\u0000\u0556\u0558\u0001\u0000\u0000\u0000\u0557"+
		"\u0542\u0001\u0000\u0000\u0000\u0557\u0543\u0001\u0000\u0000\u0000\u0558"+
		"\u00c5\u0001\u0000\u0000\u0000\u0559\u055c\u0003\u00d6k\u0000\u055a\u055c"+
		"\u0005\u0083\u0000\u0000\u055b\u0559\u0001\u0000\u0000\u0000\u055b\u055a"+
		"\u0001\u0000\u0000\u0000\u055c\u0564\u0001\u0000\u0000\u0000\u055d\u0560"+
		"\u0005r\u0000\u0000\u055e\u0561\u0003\u00d6k\u0000\u055f\u0561\u0005\u0083"+
		"\u0000\u0000\u0560\u055e\u0001\u0000\u0000\u0000\u0560\u055f\u0001\u0000"+
		"\u0000\u0000\u0561\u0563\u0001\u0000\u0000\u0000\u0562\u055d\u0001\u0000"+
		"\u0000\u0000\u0563\u0566\u0001\u0000\u0000\u0000\u0564\u0562\u0001\u0000"+
		"\u0000\u0000\u0564\u0565\u0001\u0000\u0000\u0000\u0565\u00c7\u0001\u0000"+
		"\u0000\u0000\u0566\u0564\u0001\u0000\u0000\u0000\u0567\u056e\u0003\u00e2"+
		"q\u0000\u0568\u0569\u0005\u001d\u0000\u0000\u0569\u056a\u0005U\u0000\u0000"+
		"\u056a\u056b\u0003\u00cae\u0000\u056b\u056c\u0005V\u0000\u0000\u056c\u056e"+
		"\u0001\u0000\u0000\u0000\u056d\u0567\u0001\u0000\u0000\u0000\u056d\u0568"+
		"\u0001\u0000\u0000\u0000\u056e\u00c9\u0001\u0000\u0000\u0000\u056f\u057f"+
		"\u0003\u00d6k\u0000\u0570\u0571\u0003\u00d6k\u0000\u0571\u0573\u0005p"+
		"\u0000\u0000\u0572\u0574\u0003\u00ceg\u0000\u0573\u0572\u0001\u0000\u0000"+
		"\u0000\u0573\u0574\u0001\u0000\u0000\u0000\u0574\u057f\u0001\u0000\u0000"+
		"\u0000\u0575\u0576\u0003\u00d6k\u0000\u0576\u0578\u0005p\u0000\u0000\u0577"+
		"\u0579\u0003\u00ceg\u0000\u0578\u0577\u0001\u0000\u0000\u0000\u0578\u0579"+
		"\u0001\u0000\u0000\u0000\u0579\u057a\u0001\u0000\u0000\u0000\u057a\u057c"+
		"\u0005p\u0000\u0000\u057b\u057d\u0003\u00ceg\u0000\u057c\u057b\u0001\u0000"+
		"\u0000\u0000\u057c\u057d\u0001\u0000\u0000\u0000\u057d\u057f\u0001\u0000"+
		"\u0000\u0000\u057e\u056f\u0001\u0000\u0000\u0000\u057e\u0570\u0001\u0000"+
		"\u0000\u0000\u057e\u0575\u0001\u0000\u0000\u0000\u057f\u00cb\u0001\u0000"+
		"\u0000\u0000\u0580\u0581\u0003\u00d6k\u0000\u0581\u0582\u0005U\u0000\u0000"+
		"\u0582\u0583\u00032\u0019\u0000\u0583\u0584\u0005V\u0000\u0000\u0584\u058e"+
		"\u0001\u0000\u0000\u0000\u0585\u0586\u0005W\u0000\u0000\u0586\u0587\u0005"+
		"\u0083\u0000\u0000\u0587\u0588\u0005X\u0000\u0000\u0588\u0589\u0003\u00d6"+
		"k\u0000\u0589\u058a\u0005U\u0000\u0000\u058a\u058b\u00032\u0019\u0000"+
		"\u058b\u058c\u0005V\u0000\u0000\u058c\u058e\u0001\u0000\u0000\u0000\u058d"+
		"\u0580\u0001\u0000\u0000\u0000\u058d\u0585\u0001\u0000\u0000\u0000\u058e"+
		"\u00cd\u0001\u0000\u0000\u0000\u058f\u0594\u0003\u00ccf\u0000\u0590\u0591"+
		"\u0005r\u0000\u0000\u0591\u0593\u0003\u00ccf\u0000\u0592\u0590\u0001\u0000"+
		"\u0000\u0000\u0593\u0596\u0001\u0000\u0000\u0000\u0594\u0592\u0001\u0000"+
		"\u0000\u0000\u0594\u0595\u0001\u0000\u0000\u0000\u0595\u00cf\u0001\u0000"+
		"\u0000\u0000\u0596\u0594\u0001\u0000\u0000\u0000\u0597\u0598\u0007\u000f"+
		"\u0000\u0000\u0598\u00d1\u0001\u0000\u0000\u0000\u0599\u059b\u0003\u00d0"+
		"h\u0000\u059a\u0599\u0001\u0000\u0000\u0000\u059b\u059c\u0001\u0000\u0000"+
		"\u0000\u059c\u059a\u0001\u0000\u0000\u0000\u059c\u059d\u0001\u0000\u0000"+
		"\u0000\u059d\u00d3\u0001\u0000\u0000\u0000\u059e\u05a0\u0005\u001d\u0000"+
		"\u0000\u059f\u05a1\u0003\u00d2i\u0000\u05a0\u059f\u0001\u0000\u0000\u0000"+
		"\u05a0\u05a1\u0001\u0000\u0000\u0000\u05a1\u05a2\u0001\u0000\u0000\u0000"+
		"\u05a2\u05a3\u0005U\u0000\u0000\u05a3\u05a4\u0003\u00c4b\u0000\u05a4\u05a5"+
		"\u0005V\u0000\u0000\u05a5\u05a6\u0005q\u0000\u0000\u05a6\u00d5\u0001\u0000"+
		"\u0000\u0000\u05a7\u05a8\u0005\u0088\u0000\u0000\u05a8\u00d7\u0001\u0000"+
		"\u0000\u0000\u05a9\u05ac\u0003\u00c8d\u0000\u05aa\u05ac\u0003\u00dam\u0000"+
		"\u05ab\u05a9\u0001\u0000\u0000\u0000\u05ab\u05aa\u0001\u0000\u0000\u0000"+
		"\u05ac\u00d9\u0001\u0000\u0000\u0000\u05ad\u05ae\u0005\u0001\u0000\u0000"+
		"\u05ae\u05af\u0005U\u0000\u0000\u05af\u05b0\u0005U\u0000\u0000\u05b0\u05b1"+
		"\u0003\u00dcn\u0000\u05b1\u05b2\u0005V\u0000\u0000\u05b2\u05b3\u0005V"+
		"\u0000\u0000\u05b3\u00db\u0001\u0000\u0000\u0000\u05b4\u05b6\u0003\u00e0"+
		"p\u0000\u05b5\u05b4\u0001\u0000\u0000\u0000\u05b6\u05b9\u0001\u0000\u0000"+
		"\u0000\u05b7\u05b5\u0001\u0000\u0000\u0000\u05b7\u05b8\u0001\u0000\u0000"+
		"\u0000\u05b8\u00dd\u0001\u0000\u0000\u0000\u05b9\u05b7\u0001\u0000\u0000"+
		"\u0000\u05ba\u05bc\u0003\u00dam\u0000\u05bb\u05ba\u0001\u0000\u0000\u0000"+
		"\u05bc\u05bd\u0001\u0000\u0000\u0000\u05bd\u05bb\u0001\u0000\u0000\u0000"+
		"\u05bd\u05be\u0001\u0000\u0000\u0000\u05be\u00df\u0001\u0000\u0000\u0000"+
		"\u05bf\u05c5\b\u0010\u0000\u0000\u05c0\u05c1\u0005U\u0000\u0000\u05c1"+
		"\u05c2\u0003\u00dcn\u0000\u05c2\u05c3\u0005V\u0000\u0000\u05c3\u05c5\u0001"+
		"\u0000\u0000\u0000\u05c4\u05bf\u0001\u0000\u0000\u0000\u05c4\u05c0\u0001"+
		"\u0000\u0000\u0000\u05c5\u00e1\u0001\u0000\u0000\u0000\u05c6\u05c7\u0005"+
		"\u001d\u0000\u0000\u05c7\u05c8\u0005U\u0000\u0000\u05c8\u05c9\u0003\u00d6"+
		"k\u0000\u05c9\u05ca\u0005V\u0000\u0000\u05ca\u00e3\u0001\u0000\u0000\u0000"+
		"\u05cb\u05cc\u0007\u0011\u0000\u0000\u05cc\u00e5\u0001\u0000\u0000\u0000"+
		"\u00ba\u00e7\u00f0\u00fc\u0107\u0137\u013e\u014d\u0152\u0159\u0160\u0163"+
		"\u0167\u016f\u0176\u0178\u0180\u01b3\u01bd\u01c4\u01cc\u01d4\u01dc\u01e4"+
		"\u01ec\u01f4\u01fc\u0204\u020c\u0215\u021d\u0224\u022d\u0233\u023b\u0242"+
		"\u0249\u024f\u026c\u0270\u0274\u0277\u027a\u027f\u0283\u028a\u028d\u0291"+
		"\u0298\u029b\u02a1\u02a4\u02a9\u02ae\u02b3\u02b8\u02bb\u02c0\u02c2\u02c6"+
		"\u02c9\u02cc\u02cf\u02d4\u02db\u02dd\u02e4\u02e9\u02ec\u02f0\u0300\u030f"+
		"\u0312\u0315\u031b\u0320\u0325\u032b\u0332\u0339\u034b\u034f\u0352\u0356"+
		"\u035b\u0360\u0368\u036c\u0371\u0377\u0379\u037b\u0380\u0384\u0389\u038e"+
		"\u0391\u0398\u039c\u03a1\u03a6\u03aa\u03ad\u03b1\u03b4\u03ba\u03bd\u03c6"+
		"\u03cb\u03ce\u03d4\u03e8\u03eb\u03f0\u03f3\u03fa\u0411\u0414\u0416\u0420"+
		"\u0426\u0429\u042e\u0433\u043d\u0442\u0447\u044e\u0456\u0463\u0468\u046f"+
		"\u0473\u047a\u047e\u0483\u0488\u048b\u0494\u0499\u04a6\u04ab\u04b3\u04b9"+
		"\u04bc\u04c7\u04cf\u04e5\u04e9\u04eb\u04ef\u04f3\u04f7\u04fe\u0507\u050b"+
		"\u0512\u0515\u051b\u051e\u0521\u0525\u052c\u0535\u053c\u0546\u054a\u054e"+
		"\u0552\u0555\u0557\u055b\u0560\u0564\u056d\u0573\u0578\u057c\u057e\u058d"+
		"\u0594\u059c\u05a0\u05ab\u05b7\u05bd\u05c4";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}