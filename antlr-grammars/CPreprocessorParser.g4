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
    | otherLine
    ;

finalLogicalLine
    : defineLine
    | malformedDefineLine
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

defineDirective
    : DEFINE requiredHorizontal macroName LPAREN horizontal* parameterList?
      horizontal* RPAREN replacementList?                         # FunctionDefine
    | DEFINE requiredHorizontal macroName objectReplacement?      # ObjectDefine
    ;

macroName
    : IDENTIFIER
    ;

parameterList
    : ELLIPSIS
    | macroParameter (horizontal* COMMA horizontal* macroParameter)*
      (horizontal* COMMA horizontal* ELLIPSIS)?
    | macroParameter horizontal* ELLIPSIS
    ;

macroParameter
    : IDENTIFIER
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
    | DEFINE
    | ELLIPSIS
    | RPAREN
    | COMMA
    | STRING_LITERAL
    | CHARACTER_CONSTANT
    | PP_NUMBER
    | IDENTIFIER
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
