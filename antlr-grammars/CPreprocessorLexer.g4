lexer grammar CPreprocessorLexer;

@header {
package legacymodernizer.parser.antlr.c.preprocessor;
}

@members {
public boolean hasUnterminatedBlockComment() {
    return _mode == BLOCK_COMMENT_MODE;
}
}

HASHHASH
    : '##'
    | '%:%:'
    ;

HASH
    : '#'
    | '%:'
    ;

DEFINE
    : 'define'
    ;

ELLIPSIS
    : '...'
    ;

LPAREN
    : '('
    ;

RPAREN
    : ')'
    ;

COMMA
    : ','
    ;

STRING_LITERAL
    : ENCODING_PREFIX? '"' S_CHAR* '"'
    ;

CHARACTER_CONSTANT
    : ENCODING_PREFIX? '\'' C_CHAR+ '\''
    ;

PP_NUMBER
    : (DIGIT | '.' DIGIT)
      (DIGIT | IDENTIFIER_NONDIGIT | '.' | [eEpP] [+-]?)*
    ;

IDENTIFIER
    : IDENTIFIER_NONDIGIT (IDENTIFIER_NONDIGIT | IDENTIFIER_CONTINUE)*
    ;

BLOCK_COMMENT_START
    : '/*' -> pushMode(BLOCK_COMMENT_MODE)
    ;

LINE_COMMENT
    : '//' ~[\r\n]*
    ;

WS
    : [ \t\f\u000B]+
    ;

NEWLINE
    : '\r\n'
    | '\r'
    | '\n'
    ;

PUNCTUATOR
    : '[' | ']' | '{' | '}' | '.' | '->' | '++' | '--'
    | '&' | '*' | '+' | '-' | '~' | '!' | '/' | '%' | '<<' | '>>'
    | '<' | '>' | '<=' | '>=' | '==' | '!=' | '^' | '|' | '&&' | '||'
    | '?' | ':' | ';' | '=' | '*=' | '/=' | '%=' | '+=' | '-='
    | '<<=' | '>>=' | '&=' | '^=' | '|=' | '<:' | ':>' | '<%' | '%>'
    ;

OTHER
    : .
    ;

mode BLOCK_COMMENT_MODE;

BLOCK_COMMENT_END
    : '*/' -> popMode
    ;

BLOCK_COMMENT_NEWLINE
    : ('\r\n' | '\r' | '\n') -> type(NEWLINE)
    ;

BLOCK_COMMENT_CONTENT
    : ~[*\r\n]+ -> type(WS)
    ;

BLOCK_COMMENT_STAR
    : '*' -> type(WS)
    ;

fragment ENCODING_PREFIX
    : 'u8'
    | 'u'
    | 'U'
    | 'L'
    ;

fragment S_CHAR
    : ~["\\\r\n]
    | ESCAPE_SEQUENCE
    ;

fragment C_CHAR
    : ~['\\\r\n]
    | ESCAPE_SEQUENCE
    ;

fragment ESCAPE_SEQUENCE
    : '\\' .
    ;

fragment IDENTIFIER_NONDIGIT
    : '_'
    | [\p{XID_Start}]
    | UNIVERSAL_CHARACTER_NAME
    ;

fragment IDENTIFIER_CONTINUE
    : [\p{XID_Continue}]
    | UNIVERSAL_CHARACTER_NAME
    ;

fragment UNIVERSAL_CHARACTER_NAME
    : '\\u' HEX_QUAD
    | '\\U' HEX_QUAD HEX_QUAD
    ;

fragment HEX_QUAD
    : HEX_DIGIT HEX_DIGIT HEX_DIGIT HEX_DIGIT
    ;

fragment HEX_DIGIT
    : [0-9a-fA-F]
    ;

fragment DIGIT
    : [0-9]
    ;
