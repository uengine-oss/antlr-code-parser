parser grammar PlpgsqlParser;

options {
    tokenVocab = PlpgsqlLexer;
}

// Entry point
plpgsqlBlock
    : label? declareSection? BEGIN statementList exceptionSection? END label? SEMI?
    ;

// Label for blocks (e.g., << outerblock >>)
label
    : LABEL_START Identifier LABEL_END
    ;

// Declaration section
declareSection
    : DECLARE declarationList
    ;

declarationList
    : declaration+
    ;

declaration
    : Identifier CONSTANT? dataType (NOT NULL)? (ASSIGN expression | DEFAULT expression)? SEMI
    | Identifier CURSOR (LPAREN parameterList RPAREN)? (IS | FOR) selectStmt SEMI
    | Identifier ALIAS FOR (Identifier | DOLLAR IntegerLiteral) SEMI
    ;

dataType
    : INTEGER
    | BIGINT
    | SMALLINT
    | NUMERIC (LPAREN IntegerLiteral (COMMA IntegerLiteral)? RPAREN)?
    | DECIMAL (LPAREN IntegerLiteral (COMMA IntegerLiteral)? RPAREN)?
    | REAL
    | DOUBLE
    | VARCHAR (LPAREN IntegerLiteral RPAREN)?
    | CHAR (LPAREN IntegerLiteral RPAREN)?
    | TEXT
    | BOOLEAN
    | DATE
    | TIME (LPAREN IntegerLiteral RPAREN)?
    | TIMESTAMP (LPAREN IntegerLiteral RPAREN)? (WITH Identifier ZONE)?
    | TIMESTAMPTZ
    | INTERVAL
    | RECORD
    | JSON
    | JSONB
    | UUID
    | BYTEA
    | MONEY
    | Identifier (PERCENT (TYPE | ROWTYPE))?
    | dataType ARRAY (LBRACK RBRACK)?
    ;

// Statement list
statementList
    : statement*
    ;

statement
    : assignmentStmt
    | selectIntoStmt
    | performStmt
    | executeStmt
    | insertStmt
    | updateStmt
    | deleteStmt
    | setStmt
    | ifStmt
    | caseStmt
    | loopStmt
    | whileStmt
    | forStmt
    | foreachStmt
    | exitStmt
    | continueStmt
    | returnStmt
    | raiseStmt
    | assertStmt
    | nullStmt
    | nestedBlock
    | getDiagnosticsStmt
    | openCursorStmt
    | closeCursorStmt
    | fetchStmt
    | commitStmt
    | rollbackStmt
    | createTempTableStmt
    | cteStmt
    | sqlGenericStmt
    ;

// Assignment statement
assignmentStmt
    : variableRef ASSIGN expression SEMI
    ;

variableRef
    : Identifier (DOT Identifier)*
    | QuotedIdentifier (DOT QuotedIdentifier)*
    ;

// SELECT INTO statement
selectIntoStmt
    : withClause? SELECT selectList INTO STRICT? variableList fromClause? whereClause? groupByClause? havingClause? orderByClause? limitClause? SEMI
    ;

selectList
    : STAR
    | selectItem (COMMA selectItem)*
    ;

selectItem
    : expression (AS? Identifier)?
    ;

variableList
    : variableRef (COMMA variableRef)*
    ;

fromClause
    : FROM tableRef (joinClause)*
    ;

tableRef
    : Identifier (DOT Identifier)* (AS? Identifier)?
    | QuotedIdentifier (DOT QuotedIdentifier)* (AS? Identifier)?
    | functionCall (WITH Identifier)? (AS? Identifier)?
    | LPAREN selectStmt RPAREN (AS? Identifier)?
    ;

joinClause
    : COMMA tableRef
    | joinType? JOIN tableRef (ON expression | USING LPAREN columnList RPAREN)?
    ;

joinType
    : INNER
    | LEFT OUTER?
    | RIGHT OUTER?
    | FULL OUTER?
    | CROSS
    ;

whereClause
    : WHERE expression
    ;

// PERFORM statement (SELECT without INTO)
performStmt
    : PERFORM selectList fromClause? whereClause? SEMI
    ;

// EXECUTE statement
executeStmt
    : EXECUTE expression (INTO variableList)? (USING expressionList)? SEMI
    ;

// INSERT statement  
// INSERT ... SELECT의 경우 복잡한 SELECT 구문을 완벽히 파싱하지 않고 세미콜론까지 소비
insertStmt
    : INSERT INTO Identifier (DOT Identifier)* (LPAREN columnList RPAREN)? VALUES LPAREN expressionList RPAREN (COMMA LPAREN expressionList RPAREN)* (RETURNING expressionList (INTO variableList)?)? SEMI
    | INSERT INTO Identifier (DOT Identifier)* (LPAREN columnList RPAREN)? (~SEMI)+ SEMI  // SELECT 포함한 모든 것을 세미콜론까지 소비
    ;

columnList
    : Identifier (COMMA Identifier)*
    ;

expressionList
    : expression (COMMA expression)*
    ;

// UPDATE statement
updateStmt
    : withClause? UPDATE Identifier (DOT Identifier)* (AS? Identifier)? SET assignmentList (FROM tableRef (joinClause)*)? whereClause? (RETURNING expressionList (INTO variableList)?)? SEMI
    ;

assignmentList
    : columnAssignment (COMMA columnAssignment)*
    ;

columnAssignment
    : Identifier (DOT Identifier)* EQ expression
    ;

// DELETE statement
deleteStmt
    : withClause? DELETE FROM Identifier (DOT Identifier)* (AS? Identifier)? (USING tableRef (COMMA tableRef)*)? whereClause? (RETURNING expressionList (INTO variableList)?)? SEMI
    ;

// SET statement (runtime configuration)
setStmt
    : SET Identifier (TO | EQ) expression SEMI
    | SET Identifier FROM CURRENT SEMI
    ;

// IF statement
ifStmt
    : IF expression THEN statementList (ELSIF expression THEN statementList)* (ELSE statementList)? END IF SEMI
    ;

// CASE statement
caseStmt
    : CASE expression? whenClauseList (ELSE statementList)? END CASE SEMI
    ;

whenClauseList
    : whenClause+
    ;

whenClause
    : WHEN expression THEN statementList
    ;

// LOOP statement
loopStmt
    : label? LOOP statementList END LOOP label? SEMI
    ;

// WHILE statement
whileStmt
    : label? WHILE expression LOOP statementList END LOOP label? SEMI
    ;

// FOR statement
forStmt
    : label? FOR Identifier IN REVERSE? expression DOT DOT expression (BY expression)? LOOP statementList END LOOP label? SEMI
    | label? FOR Identifier IN forQuerySource LOOP statementList END LOOP label? SEMI
    ;

forQuerySource
    : EXECUTE expression
    | SELECT selectList fromClause? whereClause? groupByClause? havingClause? orderByClause? limitClause?
    | expression
    ;

// FOREACH statement
foreachStmt
    : label? FOREACH Identifier (SLICE IntegerLiteral)? IN ARRAY expression LOOP statementList END LOOP label? SEMI
    ;

// EXIT statement
exitStmt
    : EXIT Identifier? (WHEN expression)? SEMI
    ;

// CONTINUE statement
continueStmt
    : CONTINUE Identifier? (WHEN expression)? SEMI
    ;

// RETURN statement
returnStmt
    : RETURN expression? SEMI
    | RETURN NEXT expression SEMI
    | RETURN QUERY selectStmt SEMI
    | RETURN QUERY EXECUTE expression (USING expressionList)? SEMI
    ;

// RAISE statement
raiseStmt
    : RAISE (NOTICE | WARNING | INFO | DEBUG | LOG | EXCEPTION)? (StringLiteral (COMMA expressionList)? | Identifier)? (USING raiseOptionList)? SEMI
    ;

raiseOptionList
    : raiseOption (COMMA raiseOption)*
    ;

raiseOption
    : (MESSAGE | DETAIL | HINT | ERRCODE | SQLSTATE) EQ expression
    ;

// ASSERT statement
assertStmt
    : ASSERT expression (COMMA expression)? SEMI
    ;

// NULL statement
nullStmt
    : NULL SEMI
    ;

// COMMIT statement
commitStmt
    : COMMIT (WORK | TRANSACTION)? SEMI
    ;

// ROLLBACK statement
rollbackStmt
    : ROLLBACK (WORK | TRANSACTION)? (TO SAVEPOINT? Identifier)? SEMI
    ;

// Nested block
nestedBlock
    : label? DECLARE? declarationList? BEGIN statementList exceptionSection? END label? SEMI
    ;

// Exception section
exceptionSection
    : EXCEPTION exceptionHandlerList
    ;

exceptionHandlerList
    : exceptionHandler+
    ;

exceptionHandler
    : WHEN exceptionConditionList THEN statementList
    ;

exceptionConditionList
    : exceptionCondition (OR exceptionCondition)*
    ;

exceptionCondition
    : Identifier
    | SQLSTATE StringLiteral
    ;

// GET DIAGNOSTICS
getDiagnosticsStmt
    : GET (CURRENT | STACKED)? DIAGNOSTICS diagnosticsItemList SEMI
    ;

diagnosticsItemList
    : diagnosticsItem (COMMA diagnosticsItem)*
    ;

diagnosticsItem
    : variableRef EQ diagnosticsOption
    ;

diagnosticsOption
    : Identifier  // ROW_COUNT, RESULT_OID, etc.
    ;

// Cursor operations
openCursorStmt
    : OPEN variableRef (LPAREN expressionList RPAREN)? (FOR selectStmt)? SEMI
    ;

closeCursorStmt
    : CLOSE variableRef SEMI
    ;

fetchStmt
    : FETCH variableRef INTO variableList SEMI
    ;

// SELECT statement (simplified but extended)
selectStmt
    : withClause? selectQuery ((UNION | INTERSECT | EXCEPT) ALL? selectQuery)*
    ;

withClause
    : WITH cteList
    ;

cteList
    : cte (COMMA cte)*
    ;

cte
    : Identifier (LPAREN columnList RPAREN)? AS LPAREN selectStmt RPAREN
    ;

selectQuery
    : SELECT (DISTINCT (ON LPAREN expressionList RPAREN)? | ALL)? selectList fromClause? whereClause? groupByClause? havingClause? orderByClause? limitClause?
    ;

groupByClause
    : GROUP BY expressionList
    ;

havingClause
    : HAVING expression
    ;

orderByClause
    : ORDER BY orderByItem (COMMA orderByItem)*
    ;

orderByItem
    : expression (ASC | DESC)? (NULLS (FIRST | LAST))?
    ;

limitClause
    : LIMIT (expression | ALL) (OFFSET expression)?
    ;

// Parameter list
parameterList
    : parameter (COMMA parameter)*
    ;

parameter
    : Identifier dataType
    ;

// Expression (simplified for PL/pgSQL)
expression
    : literal
    | variableRef
    | functionCall
    | windowFunction
    | specialVariable
    | INTERVAL StringLiteral
    | CAST LPAREN expression AS dataType RPAREN
    | expression TYPECAST dataType
    | COALESCE LPAREN expressionList RPAREN
    | NULLIF LPAREN expression COMMA expression RPAREN
    | GREATEST LPAREN expressionList RPAREN
    | LEAST LPAREN expressionList RPAREN
    | EXISTS LPAREN selectStmt RPAREN
    | LPAREN selectStmt RPAREN
    | LPAREN expression RPAREN
    | expression (STAR | SLASH | PERCENT) expression
    | expression (PLUS | MINUS) expression
    | expression CONCAT expression
    | expression (EQ | NEQ | LT | LTE | GT | GTE) expression (LPAREN PLUS RPAREN)?  // Oracle outer join hint: col = col2 (+)
    | expression (NOT)? BETWEEN expression AND expression
    | expression (NOT)? (LIKE | ILIKE) expression
    | expression SIMILAR TO expression
    | expression (EQ | NEQ | LT | LTE | GT | GTE) (ANY | SOME | ALL) LPAREN (selectStmt | expressionList) RPAREN
    | expression (AND | OR) expression
    | NOT expression
    | expression IS (NOT)? NULL
    | expression (NOT)? IN LPAREN (selectStmt | expressionList) RPAREN
    | CASE expression? whenExprClauseList (ELSE expression)? END
    | arrayExpression
    ;

whenExprClauseList
    : whenExprClause+
    ;

whenExprClause
    : WHEN expression THEN expression
    ;

arrayExpression
    : ARRAY LBRACK expressionList RBRACK
    | variableRef LBRACK expression RBRACK
    ;

// Special PL/pgSQL variables (e.g., SQLSTATE in EXCEPTION blocks, FOUND after DML)
specialVariable
    : SQLSTATE
    | FOUND
    ;

functionCall
    : (Identifier | LEFT | RIGHT | SUBSTRING | POSITION) LPAREN functionCallArgs RPAREN
    ;

functionCallArgs
    : DISTINCT expressionList
    | expressionList
    | expression IN expression  // position('str' in col)
    | expression FROM expression (FOR expression)?  // substring(col from 1 for 10)
    | STAR
    |  // 빈 파라미터 허용
    ;

// Window function (e.g., row_number() OVER (...))
windowFunction
    : functionCall OVER LPAREN windowSpec RPAREN
    ;

windowSpec
    : (PARTITION BY expressionList)? orderByClause?
    ;

// CTE (Common Table Expression) statement: WITH ... INSERT/UPDATE/DELETE/SELECT
cteStmt
    : WITH cteList (insertStmt | updateStmt | deleteStmt | selectIntoStmt | (~SEMI)+ SEMI)
    ;

// Generic SQL consumer to improve tolerance: consumes until ';'
sqlGenericStmt
    : (CREATE | DROP | ALTER | TRUNCATE | ANALYZE | VACUUM | EXPLAIN | LOCK | REINDEX | CLUSTER | COMMENT | CALL)
      (~SEMI)* SEMI
    ;

literal
    : IntegerLiteral
    | NumericLiteral
    | StringLiteral
    | DollarQuotedString
    | NULL
    | TRUE
    | FALSE
    ;

// ===== Additional DDL allowed inside PL/pgSQL blocks =====
createTempTableStmt
    : CREATE (TEMP | TEMPORARY)? TABLE (IF NOT EXISTS)? Identifier LPAREN createTableColumnDefList RPAREN onCommitClause? SEMI
    ;

createTableColumnDefList
    : createTableColumnDef (COMMA createTableColumnDef)*
    ;

createTableColumnDef
    : Identifier dataType (NOT? NULL)? (DEFAULT expression)?
    ;

onCommitClause
    : ON COMMIT (DROP | DELETE ROWS | PRESERVE ROWS)
    ;