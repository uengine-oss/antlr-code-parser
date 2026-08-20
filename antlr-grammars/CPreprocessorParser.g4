parser grammar CPreprocessorParser;

options {
    tokenVocab = CPreprocessorLexer;
}

@header {
package legacymodernizer.parser.antlr.c.preprocessor;
}

/*
 * Pinned C preprocessing control-line grammar for semantic evidence.
 * The main C grammar keeps directives hidden so its parse tree remains stable;
 * this grammar owns directive syntax over the same decoded source.
 */

preprocessingFile
    : (logicalLine NEWLINE)* finalLogicalLine? EOF
    ;

logicalLine
    : defineLine
    | malformedDefineLine
    | includeLine
    | malformedIncludeLine
    | conditionalLine
    | undefLine
    | otherDirectiveLine
    | otherLine
    ;

finalLogicalLine
    : defineLine
    | malformedDefineLine
    | includeLine
    | malformedIncludeLine
    | conditionalLine
    | undefLine
    | otherDirectiveLine
    | ppToken+
    ;

defineLine
    : horizontal* HASH horizontal* defineDirective
    ;

malformedDefineLine
    : horizontal* HASH horizontal* DEFINE ppToken*
    ;

otherLine
    : ppToken*
    ;

includeLine
    : horizontal* HASH horizontal* INCLUDE requiredHorizontal includeTarget horizontal*
    ;

malformedIncludeLine
    : horizontal* HASH horizontal* INCLUDE ppToken*
    ;

includeTarget
    : STRING_LITERAL                                              # QuotedIncludeTarget
    | LESS includeAngleToken+ GREATER                             # AngleIncludeTarget
    | computedHeaderTokens                                       # ComputedIncludeTarget
    ;

computedHeaderTokens
    : preprocessingIdentifier (horizontal* replacementToken)*
    ;

includeAngleToken
    : preprocessingIdentifier
    | HASHHASH | HASH | ELLIPSIS | LPAREN | RPAREN | COMMA
    | PP_NUMBER | CHARACTER_CONSTANT
    | OROR | ANDAND | LSHIFT | RSHIFT | LE | GE | EQ | NE
    | LESS | PIPE | CARET | AMP | PLUS | MINUS | STAR | SLASH
    | PERCENT | BANG | TILDE | QUESTION | COLON | PUNCTUATOR | OTHER
    ;

conditionalLine
    : horizontal* HASH horizontal* conditionalDirective
    ;

conditionalDirective
    : IF requiredHorizontal conditionalExpression horizontal*    # IfDirective
    | IFDEF requiredHorizontal preprocessingIdentifier horizontal* # IfdefDirective
    | IFNDEF requiredHorizontal preprocessingIdentifier horizontal* # IfndefDirective
    | ELIF requiredHorizontal conditionalExpression horizontal*  # ElifDirective
    | ELSE horizontal*                                           # ElseDirective
    | ENDIF horizontal*                                          # EndifDirective
    ;

undefLine
    : horizontal* HASH horizontal* UNDEF requiredHorizontal
      preprocessingIdentifier horizontal*
    ;

otherDirectiveLine
    : horizontal* HASH ppToken*
    ;

defineDirective
    : DEFINE requiredHorizontal macroName LPAREN horizontal* parameterList?
      horizontal* RPAREN replacementList?                         # FunctionDefine
    | DEFINE requiredHorizontal macroName objectReplacement?      # ObjectDefine
    ;

macroName
    : preprocessingIdentifier
    ;

preprocessingIdentifier
    : IDENTIFIER
    | DEFINE | INCLUDE | IFDEF | IFNDEF | ELIF | ENDIF | IF | ELSE | UNDEF | DEFINED
    ;

parameterList
    : ELLIPSIS
    | macroParameter (horizontal* COMMA horizontal* macroParameter)*
      (horizontal* COMMA horizontal* ELLIPSIS)?
    | macroParameter horizontal* ELLIPSIS
    ;

macroParameter
    : preprocessingIdentifier
    ;

conditionalExpression
    : logicalOrExpression
      (horizontal* QUESTION horizontal* conditionalExpression
       horizontal* COLON horizontal* conditionalExpression)?
    ;

logicalOrExpression
    : logicalAndExpression (horizontal* OROR horizontal* logicalAndExpression)*
    ;

logicalAndExpression
    : inclusiveOrExpression (horizontal* ANDAND horizontal* inclusiveOrExpression)*
    ;

inclusiveOrExpression
    : exclusiveOrExpression (horizontal* PIPE horizontal* exclusiveOrExpression)*
    ;

exclusiveOrExpression
    : andExpression (horizontal* CARET horizontal* andExpression)*
    ;

andExpression
    : equalityExpression (horizontal* AMP horizontal* equalityExpression)*
    ;

equalityExpression
    : relationalExpression
      (horizontal* (EQ | NE) horizontal* relationalExpression)*
    ;

relationalExpression
    : shiftExpression
      (horizontal* (LESS | LE | GREATER | GE) horizontal* shiftExpression)*
    ;

shiftExpression
    : additiveExpression
      (horizontal* (LSHIFT | RSHIFT) horizontal* additiveExpression)*
    ;

additiveExpression
    : multiplicativeExpression
      (horizontal* (PLUS | MINUS) horizontal* multiplicativeExpression)*
    ;

multiplicativeExpression
    : unaryExpression
      (horizontal* (STAR | SLASH | PERCENT) horizontal* unaryExpression)*
    ;

unaryExpression
    : (PLUS | MINUS | BANG | TILDE) horizontal* unaryExpression
    | primaryExpression
    ;

primaryExpression
    : PP_NUMBER
    | CHARACTER_CONSTANT
    | DEFINED horizontal*
      (preprocessingIdentifier
       | LPAREN horizontal* preprocessingIdentifier horizontal* RPAREN)
    | preprocessingIdentifier
    | LPAREN horizontal* conditionalExpression horizontal* RPAREN
    ;

objectReplacement
    : horizontal+ replacementTokens?
    | nonLparenReplacementToken (horizontal* replacementToken)* horizontal*
    ;

replacementList
    : horizontal* replacementToken (horizontal* replacementToken)* horizontal*
    ;

replacementTokens
    : replacementToken (horizontal* replacementToken)* horizontal*
    ;

nonLparenReplacementToken
    : HASHHASH
    | HASH
    | preprocessingIdentifier
    | ELLIPSIS
    | RPAREN
    | COMMA
    | STRING_LITERAL
    | CHARACTER_CONSTANT
    | PP_NUMBER
    | OROR | ANDAND | LSHIFT | RSHIFT | LE | GE | EQ | NE
    | LESS | GREATER | PIPE | CARET | AMP | PLUS | MINUS | STAR | SLASH
    | PERCENT | BANG | TILDE | QUESTION | COLON
    | PUNCTUATOR
    | OTHER
    ;

replacementToken
    : nonLparenReplacementToken
    | LPAREN
    ;

requiredHorizontal
    : horizontal+
    ;

horizontal
    : WS
    | LINE_COMMENT
    | BLOCK_COMMENT_START
    | BLOCK_COMMENT_END
    ;

ppToken
    : horizontal
    | replacementToken
    ;
