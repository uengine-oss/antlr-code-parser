package legacymodernizer.parser.antlr.c;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ListTokenSource;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import legacymodernizer.parser.parsing.evidence.CallEvidenceCandidate;
import legacymodernizer.parser.parsing.evidence.SourceRangeCandidate;
import legacymodernizer.parser.parsing.evidence.SymbolDefinitionEvidenceCandidate;
import legacymodernizer.parser.parsing.evidence.SymbolEvidenceExtraction;
import legacymodernizer.parser.parsing.evidence.SymbolLookupEvidenceCandidate;

/**
 * C grammar predicates and the translation-unit-local ordinary identifier table.
 *
 * <p>Language keywords are lexer tokens. Only declarations parsed in this translation unit
 * enter the symbol table; header/platform names require configured preprocessing evidence and
 * are never guessed from project text or identifier spelling.</p>
 */
public abstract class CParserBase extends Parser {
    private final SymbolTable symbolTable = new SymbolTable();
    private final Set<String> noSemantics = new HashSet<>();
    private final Map<Symbol, DefinitionRecord> definitions = new IdentityHashMap<>();
    private final Map<LookupKey, MutableLookup> lookups = new LinkedHashMap<>();
    private final Map<Integer, BlockItemDecision> blockItemDecisions = new LinkedHashMap<>();
    private final Map<Integer, SyntaxAlternativeDecision> castDecisions =
            new LinkedHashMap<>();
    private final Map<Integer, SyntaxAlternativeDecision> externalDeclarationDecisions =
            new LinkedHashMap<>();
    private final Map<LookupKey, String> recoveredTypeNameContexts = new LinkedHashMap<>();
    private final Set<LookupKey> grammarConfirmedExpressionStarts = new HashSet<>();
    private final Deque<ParameterScope> parameterScopes = new ArrayDeque<>();
    private final Map<CParser.FunctionDefinitionContext, List<Symbol>> functionParameters =
            new IdentityHashMap<>();
    private boolean syntaxProbe;
    private Integer syntaxProbeTypeNameStartOffset;

    protected CParserBase(TokenStream input) {
        super(input);
        addParseListener(new CTypeNameScopeListener(this));
    }

    public boolean IsAlignmentSpecifier() {
        return IsAlignmentSpecifier(1);
    }

    public boolean IsAlignmentSpecifier(int k) {
        if (noSemantics.contains("IsAlignmentSpecifier")) return true;
        return tokenType(k) == CLexer.Alignas;
    }

    public boolean IsAtomicTypeSpecifier() {
        return IsAtomicTypeSpecifier(1);
    }

    public boolean IsAtomicTypeSpecifier(int k) {
        if (noSemantics.contains("IsAtomicTypeSpecifier")) return true;
        return tokenType(k) == CLexer.Atomic;
    }

    public boolean IsAttributeDeclaration() {
        return IsAttributeSpecifierSequence();
    }

    public boolean IsAttributeSpecifier() {
        return tokenType(1) == CLexer.LeftBracket;
    }

    public boolean IsAttributeSpecifierSequence() {
        return IsAttributeSpecifier();
    }

    public boolean IsDeclaration() {
        Token first = tokens().LT(1);
        if ((!syntaxProbe || syntaxProbeTypeNameStartOffset != null)
                && first.getType() == CLexer.Identifier
                && symbolTable.resolve(first.getText()) == null) {
            BlockItemDecision decision = blockItemDecisions.computeIfAbsent(
                    first.getTokenIndex(), ignored -> probeBlockItem());
            if (decision.declaration() && !decision.statement()) {
                LookupKey key = key(first);
                recoveredTypeNameContexts.put(key, "declaration_only_parse");
                recordLookup(first, "type_name", null, "unresolved_environment",
                        "declaration_only_parse");
                return true;
            }
            if (decision.declaration() && decision.statement()) {
                recordLookup(first, "ordinary_identifier", null, "unresolved_environment",
                        "ambiguous_block_item");
            }
            return false;
        }

        return IsDeclarationSpecifiers()
                || IsAttributeSpecifierSequence()
                || IsStaticAssertDeclaration()
                || IsAttributeDeclaration();
    }

    public boolean IsDeclarationSpecifier() {
        return IsStorageClassSpecifier()
                || IsTypeSpecifier()
                || IsTypeQualifier()
                || (IsFunctionSpecifier() && !IsGnuAttributeBeforeDeclarator())
                || IsAlignmentSpecifier();
    }

    public boolean IsTypeSpecifierQualifier() {
        return IsTypeSpecifierQualifier(1);
    }

    public boolean IsTypeSpecifierQualifier(int k) {
        return IsTypeSpecifier(k) || IsTypeQualifier(k) || IsAlignmentSpecifier(k);
    }

    public boolean IsDeclarationSpecifiers() {
        return IsDeclarationSpecifier();
    }

    public boolean IsEnumSpecifier() {
        return IsEnumSpecifier(1);
    }

    public boolean IsEnumSpecifier(int k) {
        return tokenType(k) == CLexer.Enum;
    }

    public boolean IsFunctionSpecifier() {
        return switch (tokenType(1)) {
            case CLexer.Inline, CLexer.Noreturn, CLexer.KW__stdcall,
                    CLexer.Attribute, CLexer.KW__declspec -> true;
            default -> false;
        };
    }

    public boolean IsGnuAttributeBeforeDeclarator() {
        return IsGnuAttributeBeforeDeclarator(1);
    }

    public boolean IsGnuAttributeBeforeDeclarator(int k) {
        CommonTokenStream tokens = tokens();
        int i = k;
        if (tokens.LT(i).getType() != CLexer.Attribute) return false;
        i++;
        int depth = 0;
        while (true) {
            Token token = tokens.LT(i++);
            if (token.getType() == Token.EOF) return false;
            if (token.getType() == CLexer.LeftParen) depth++;
            else if (token.getType() == CLexer.RightParen) {
                depth--;
                if (depth == 0) break;
            }
        }
        int next = tokens.LT(i).getType();
        return next == CLexer.Identifier || next == CLexer.Star
                || next == CLexer.LeftParen;
    }

    public boolean IsStatement() {
        Token first = tokens().LT(1);
        Token second = tokens().LT(2);
        if (first.getType() == CLexer.Identifier && second.getType() == CLexer.Colon) {
            return true;
        }
        return !IsDeclaration();
    }

    public boolean IsStaticAssertDeclaration() {
        return tokenType(1) == CLexer.Static_assert;
    }

    public boolean IsStorageClassSpecifier() {
        return switch (tokenType(1)) {
            case CLexer.Auto, CLexer.Constexpr, CLexer.Extern, CLexer.Register,
                    CLexer.Static, CLexer.ThreadLocal, CLexer.Typedef -> true;
            default -> false;
        };
    }

    public boolean IsStructOrUnionSpecifier() {
        return IsStructOrUnionSpecifier(1);
    }

    public boolean IsStructOrUnionSpecifier(int k) {
        int type = tokenType(k);
        return type == CLexer.Struct || type == CLexer.Union;
    }

    public boolean IsTypedefName() {
        return IsTypedefName(1);
    }

    public boolean IsTypedefName(int k) {
        Token token = tokens().LT(k);
        if (token.getType() != CLexer.Identifier) return false;
        Symbol resolved = symbolTable.resolve(token.getText());
        boolean priorTypeSpecifier = hasPriorTypeSpecifier(token);
        LookupKey lookupKey = key(token);
        String recoveredContext = recoveredTypeNameContexts.get(lookupKey);
        if (priorTypeSpecifier) {
            if (!syntaxProbe) {
                recordLookup(token, "ordinary_identifier", null,
                        "grammar_context", "typedef_name");
            }
            return false;
        }
        if (resolved == null && !syntaxProbe && recoveredContext == null
                && isUndecidedExternalDeclaration()) {
            SyntaxAlternativeDecision externalDecision =
                    externalDeclarationDecisions.computeIfAbsent(
                            token.getTokenIndex(), ignored -> probeExternalDeclaration());
            if (externalDecision.requiresTypeName()) {
                recoveredTypeNameContexts.put(
                        lookupKey, "external_declaration_only_parse");
                recordLookup(token, "type_name", null, "unresolved_environment",
                        "external_declaration_only_parse");
                return true;
            }
            if (externalDecision.ambiguous()) {
                recordLookup(token, "ordinary_identifier", null, "unresolved_environment",
                        "ambiguous_external_declaration");
                return false;
            }
            if (externalDecision.ordinaryPreferred()) return false;
        }
        boolean probeTypeName = syntaxProbe
                && syntaxProbeTypeNameStartOffset != null
                && (token.getStartIndex() == syntaxProbeTypeNameStartOffset
                        || isDeclarationOnlyTypePosition()
                        || recoveredContext != null);
        boolean recoveredTypeName = !syntaxProbe
                && (recoveredContext != null || isDeclarationOnlyTypePosition());
        if (resolved == null && (probeTypeName || recoveredTypeName)) {
            if (!syntaxProbe) {
                recordLookup(token, "type_name", null, "unresolved_environment",
                        recoveredContext == null
                                ? "declaration_only_context" : recoveredContext);
            }
            return true;
        }
        boolean decision = resolved != null
                && resolved.getClassification().contains(TypeClassification.TypeSpecifier_)
                && !priorTypeSpecifier;
        if (!syntaxProbe && !(resolved == null && inBlockItemDecision())) {
            recordLookup(token, decision ? "type_name" : "ordinary_identifier", resolved,
                    resolved == null ? "unresolved_environment" : "source_declaration",
                    "typedef_name");
        }
        return decision;
    }

    public boolean IsTypeofSpecifier() {
        return IsTypeofSpecifier(1);
    }

    public boolean IsTypeofSpecifier(int k) {
        int type = tokenType(k);
        return type == CLexer.Typeof || type == CLexer.Typeof_unqual;
    }

    public boolean IsTypeQualifier() {
        return IsTypeQualifier(1);
    }

    public boolean IsTypeQualifier(int k) {
        return switch (tokenType(k)) {
            case CLexer.Const, CLexer.Restrict, CLexer.Volatile, CLexer.Atomic -> true;
            default -> false;
        };
    }

    public boolean IsTypeSpecifier() {
        return IsTypeSpecifier(1);
    }

    public boolean IsTypeSpecifier(int k) {
        switch (tokenType(k)) {
            case CLexer.Void, CLexer.Char, CLexer.Short, CLexer.Int, CLexer.Long,
                    CLexer.Float, CLexer.Double, CLexer.Signed, CLexer.Unsigned,
                    CLexer.Bool, CLexer.Complex, CLexer.KW__m128, CLexer.KW__m128d,
                    CLexer.KW__m128i, CLexer.KW__extension__:
                return true;
            default:
                return IsAtomicTypeSpecifier(k) || IsStructOrUnionSpecifier(k)
                        || IsEnumSpecifier(k) || IsTypedefName(k) || IsTypeofSpecifier(k);
        }
    }

    /** Generated grammar action at the end of every declarator. */
    public void EnterDeclaration() {
        if (syntaxProbe) return;
        if (getContext() instanceof CParser.DeclarationContext declaration) {
            if (declaration.initDeclaratorList() != null) {
                declaration.initDeclaratorList().initDeclarator().stream()
                        .map(CParser.InitDeclaratorContext::declarator)
                        .forEach(this::EnterDeclarator);
            }
            return;
        }
        EnterDeclarator(nearest(CParser.DeclaratorContext.class));
    }

    /** Parse-listener entry with the owning declarator supplied explicitly. */
    public void EnterDeclarator(CParser.DeclaratorContext declarator) {
        if (syntaxProbe) return;
        if (declarator == null) return;

        CParser.ParameterDeclarationContext parameter =
                ancestor(declarator, CParser.ParameterDeclarationContext.class);
        if (parameter != null && parameter.declarator() == declarator) {
            defineParameter(parameter, declarator);
            return;
        }
        if (ancestor(declarator, CParser.MemberDeclarationContext.class) != null) return;

        CParser.DeclarationContext declaration =
                ancestor(declarator, CParser.DeclarationContext.class);
        if (declaration != null && ownsDeclarator(declaration, declarator)) {
            boolean typedef = isTypedef(declaration.declarationSpecifiers());
            define(declarator, typedef ? TypeClassification.TypeSpecifier_
                            : TypeClassification.Variable_,
                    typedef ? "typedef_name" : "ordinary_identifier",
                    lexicalScope(declaration), "declarator");
            return;
        }

        CParser.FunctionDefinitionContext function =
                ancestor(declarator, CParser.FunctionDefinitionContext.class);
        if (function != null && function.declarator() == declarator) {
            define(declarator, TypeClassification.Function_, "ordinary_identifier",
                    lexicalScope(function), "declarator");
        }
    }

    public void EnterParameterTypeList(CParser.ParameterTypeListContext context) {
        if (syntaxProbe) return;
        Symbol scope = symbolTable.pushPrototypeScope();
        parameterScopes.push(new ParameterScope(context, scope, new ArrayList<>()));
    }

    public void ExitParameterTypeList(CParser.ParameterTypeListContext context) {
        if (syntaxProbe) return;
        if (parameterScopes.isEmpty() || parameterScopes.peek().context() != context
                || symbolTable.currentScope() != parameterScopes.peek().scope()) {
            throw new IllegalStateException("C parameter scope stack is inconsistent");
        }
        ParameterScope completed = parameterScopes.pop();
        symbolTable.popBlockScope();
        CParser.FunctionDefinitionContext function =
                ancestor(context, CParser.FunctionDefinitionContext.class);
        CParser.DeclaratorContext owner = ancestor(context, CParser.DeclaratorContext.class);
        if (function != null && function.declarator() == owner) {
            functionParameters.put(function, List.copyOf(completed.symbols()));
        }
    }

    public void EnterEnumerator(CParser.EnumeratorContext context) {
        if (syntaxProbe) return;
        if (context == null || context.enumerationConstant() == null
                || context.enumerationConstant().Identifier() == null) {
            return;
        }
        Token token = context.enumerationConstant().Identifier().getSymbol();
        define(token, token.getStopIndex() + 1, TypeClassification.Variable_,
                "ordinary_identifier", lexicalScope(context), "enumerator");
    }

    private void defineParameter(CParser.ParameterDeclarationContext parameter,
                                 CParser.DeclaratorContext declarator) {
        if (parameterScopes.isEmpty()) {
            throw new IllegalStateException("parameter declaration has no prototype scope");
        }
        CParser.ParameterTypeListContext parameterList =
                ancestor(parameter, CParser.ParameterTypeListContext.class);
        ParameterScope active = parameterScopes.peek();
        if (active.context() != parameterList) {
            throw new IllegalStateException("parameter declaration resolved to wrong scope");
        }
        CParser.FunctionDefinitionContext function =
                ancestor(parameter, CParser.FunctionDefinitionContext.class);
        CParser.DeclaratorContext owner =
                ancestor(parameterList, CParser.DeclaratorContext.class);
        ScopeDescriptor scope = function != null && function.declarator() == owner
                ? new ScopeDescriptor("block", function)
                : new ScopeDescriptor("function_prototype", parameterList);
        Symbol symbol = define(declarator, TypeClassification.Variable_,
                "ordinary_identifier", scope, "parameterDeclaration");
        if (symbol != null) active.symbols().add(symbol);
    }

    private Symbol define(CParser.DeclaratorContext declarator,
                          TypeClassification classification,
                          String symbolKind,
                          ScopeDescriptor scope,
                          String grammarRule) {
        Token token = getDeclarationToken(declarator);
        if (token == null || declarator.getStop() == null) return null;
        return define(token, declarator.getStop().getStopIndex() + 1,
                classification, symbolKind, scope, grammarRule);
    }

    private Symbol define(Token token, int visibilityStartOffset,
                          TypeClassification classification,
                          String symbolKind,
                          ScopeDescriptor scope,
                          String grammarRule) {
        Symbol symbol = new Symbol();
        symbol.setName(token.getText());
        HashSet<TypeClassification> classifications = new HashSet<>();
        classifications.add(classification);
        symbol.setClassification(classifications);
        if (!symbolTable.define(symbol)) return null;
        definitions.put(symbol, new DefinitionRecord(
                token, symbolKind, scope.kind(), scope.context(),
                visibilityStartOffset, grammarRule));
        return symbol;
    }

    private static boolean ownsDeclarator(CParser.DeclarationContext declaration,
                                          CParser.DeclaratorContext declarator) {
        if (declaration.initDeclaratorList() == null) return false;
        return declaration.initDeclaratorList().initDeclarator().stream()
                .anyMatch(item -> item.declarator() == declarator);
    }

    private static boolean isTypedef(CParser.DeclarationSpecifiersContext specifiers) {
        if (specifiers == null) return false;
        return specifiers.declarationSpecifier().stream()
                .anyMatch(item -> item.storageClassSpecifier() != null
                        && item.storageClassSpecifier().Typedef() != null);
    }

    private Token getDeclarationToken(CParser.DeclaratorContext declarator) {
        if (declarator == null) return null;
        CParser.DirectDeclaratorContext direct = declarator.directDeclarator();
        if (direct == null) return null;
        Token nested = getDeclarationToken(direct.declarator());
        if (nested != null) return nested;
        return direct.Identifier() == null ? null : direct.Identifier().getSymbol();
    }

    public boolean IsNullStructDeclarationListExtension() {
        return true;
    }

    public void EnterScope() {
        if (syntaxProbe) return;
        symbolTable.pushBlockScope();
        CParser.FunctionDefinitionContext function =
                nearest(CParser.FunctionDefinitionContext.class);
        if (function != null && nearest(CParser.FunctionBodyContext.class) != null) {
            for (Symbol parameter : functionParameters.getOrDefault(function, List.of())) {
                if (!symbolTable.define(parameter)) {
                    throw new IllegalStateException(
                            "duplicate function parameter in body scope: " + parameter.getName());
                }
            }
        }
    }

    public void ExitScope() {
        if (syntaxProbe) return;
        symbolTable.popBlockScope();
    }

    public void LookupSymbol() {
        // Exact semantic-predicate lookup evidence replaces the old no-op.
    }

    public void OutputSymbolTable() {
        // Evidence is sealed after parsing through symbolEvidenceExtraction().
    }

    public boolean IsInitDeclaratorList() {
        Token token = tokens().LT(1);
        if (token.getType() == CLexer.Identifier) return true;
        return !IsTypeQualifier(1) && !IsTypeSpecifier(1);
    }

    public boolean IsSomethingOfTypeName() {
        CommonTokenStream stream = tokens();
        int type = stream.LT(1).getType();
        if (!(type == CLexer.Sizeof || type == CLexer.Countof
                || type == CLexer.Alignof || type == CLexer.Maxof
                || type == CLexer.Minof)) {
            return false;
        }
        return stream.LT(2).getType() == CLexer.LeftParen && IsTypeName(3);
    }

    public boolean IsTypeName() {
        return IsTypeName(1);
    }

    public boolean IsTypeName(int k) {
        return IsSpecifierQualifierList(k);
    }

    public boolean IsSpecifierQualifierList() {
        return IsSpecifierQualifierList(1);
    }

    public boolean IsSpecifierQualifierList(int k) {
        return IsGnuAttributeBeforeDeclarator(k) || IsTypeSpecifierQualifier(k);
    }

    public boolean IsCast() {
        Token first = tokens().LT(1);
        Token second = tokens().LT(2);
        if (first.getType() != CLexer.LeftParen) return false;
        if (second.getType() != CLexer.Identifier) return true;
        if (syntaxProbe) {
            if (syntaxProbeTypeNameStartOffset == null) return false;
            if (second.getStartIndex() == syntaxProbeTypeNameStartOffset) return true;
        }

        Symbol resolved = symbolTable.resolve(second.getText());
        boolean decision = resolved != null
                && resolved.getClassification().contains(TypeClassification.TypeSpecifier_);
        if (decision) {
            recordLookup(second, "type_name", resolved, "source_declaration",
                    "cast_expression");
            return true;
        }
        if (resolved != null) {
            recordLookup(second, "ordinary_identifier", resolved, "source_declaration",
                    "cast_expression");
            return false;
        }

        SyntaxAlternativeDecision grammarDecision = castDecisions.computeIfAbsent(
                first.getTokenIndex(), ignored -> probeCast());
        if (grammarDecision.requiresTypeName()) {
            recoveredTypeNameContexts.put(key(second), "cast_only_parse");
            recordLookup(second, "type_name", null, "unresolved_environment",
                    "cast_only_parse");
            return true;
        }
        if (grammarDecision.ambiguous()) {
            recordLookup(second, "ordinary_identifier", null, "unresolved_environment",
                    "ambiguous_cast_expression");
            return false;
        }
        if (grammarDecision.ordinaryPreferred()) return false;

        recordLookup(second, decision ? "type_name" : "ordinary_identifier", resolved,
                resolved == null ? "unresolved_environment" : "source_declaration",
                "cast_expression");
        return false;
    }

    public SymbolEvidenceExtraction symbolEvidenceExtraction(
            List<CallEvidenceCandidate> calls) {
        List<SymbolDefinitionEvidenceCandidate> sealedDefinitions = definitions.values().stream()
                .map(this::definitionCandidate)
                .sorted(Comparator.comparingInt(item -> item.range().startOffset()))
                .toList();
        Set<SourceRangeCandidate> definitionRanges = new HashSet<>();
        sealedDefinitions.forEach(item -> definitionRanges.add(item.range()));

        List<SymbolLookupEvidenceCandidate> sealedLookups = lookups.values().stream()
                .filter(item -> !definitionRanges.contains(range(item.token())))
                .filter(item -> !grammarConfirmedExpressionStarts.contains(key(item.token())))
                .filter(item -> !isGrammarConfirmedCallCallee(item, calls))
                .map(this::lookupCandidate)
                .sorted(Comparator.comparingInt(item -> item.range().startOffset()))
                .toList();
        return new SymbolEvidenceExtraction(sealedDefinitions, sealedLookups);
    }

    private boolean isGrammarConfirmedCallCallee(
            MutableLookup lookup, List<CallEvidenceCandidate> calls) {
        if (!"unresolved_environment".equals(lookup.provenance())) return false;
        SourceRangeCandidate lookupRange = range(lookup.token());
        return calls != null && calls.stream().anyMatch(call -> {
            SourceRangeCandidate callee = call.calleeRange();
            return callee.startOffset() <= lookupRange.startOffset()
                    && lookupRange.endOffset() <= callee.endOffset();
        });
    }

    private SymbolDefinitionEvidenceCandidate definitionCandidate(DefinitionRecord record) {
        return new SymbolDefinitionEvidenceCandidate(
                range(record.token()),
                record.symbolKind(),
                record.scopeKind(),
                scopeRange(record.scopeKind(), record.scopeContext()),
                record.visibilityStartOffset(),
                record.grammarRule());
    }

    private SymbolLookupEvidenceCandidate lookupCandidate(MutableLookup lookup) {
        DefinitionRecord definition = lookup.definition() == null
                ? null : definitions.get(lookup.definition());
        boolean unresolved = "unresolved_environment".equals(lookup.provenance());
        return new SymbolLookupEvidenceCandidate(
                range(lookup.token()),
                lookup.parserDecision(),
                unresolved ? "unresolved" : "resolved",
                lookup.provenance(),
                definition == null ? null : range(definition.token()),
                null,
                List.copyOf(lookup.contexts()),
                "typedefNameDecision");
    }

    private void recordLookup(Token token, String decision, Symbol resolved,
                              String provenance, String context) {
        if (syntaxProbe) return;
        LookupKey key = key(token);
        MutableLookup existing = lookups.get(key);
        if (existing == null) {
            MutableLookup created = new MutableLookup(token, decision, resolved, provenance,
                    new LinkedHashSet<>());
            created.contexts().add(context);
            lookups.put(key, created);
            return;
        }
        if (!existing.parserDecision().equals(decision) || existing.definition() != resolved
                || !existing.provenance().equals(provenance)) {
            throw new IllegalStateException(
                    "contradictory C type-name decision at " + key.start() + ".." + key.end());
        }
        existing.contexts().add(context);
    }

    public void RecordExpressionStatement(CParser.ExpressionStatementContext context) {
        if (syntaxProbe || context == null || context.getStart() == null) return;
        if (containsUnambiguousPostfixOrAssignment(context)) {
            grammarConfirmedExpressionStarts.add(key(context.getStart()));
        }
    }

    private static boolean containsUnambiguousPostfixOrAssignment(ParseTree tree) {
        if (tree instanceof CParser.AssignmentExpressionContext assignment
                && assignment.assignementOperator != null) {
            return true;
        }
        if (tree instanceof CParser.PostfixExpressionContext postfix
                && (!postfix.Dot().isEmpty() || !postfix.Arrow().isEmpty()
                    || !postfix.PlusPlus().isEmpty() || !postfix.MinusMinus().isEmpty()
                    || !postfix.LeftBracket().isEmpty()
                    || !postfix.argumentExpressionList().isEmpty())) {
            return true;
        }
        for (int index = 0; index < tree.getChildCount(); index++) {
            if (containsUnambiguousPostfixOrAssignment(tree.getChild(index))) return true;
        }
        return false;
    }

    private BlockItemDecision probeBlockItem() {
        return new BlockItemDecision(probeRule(true), probeRule(false));
    }

    private boolean probeRule(boolean declaration) {
        CommonTokenStream probeTokens = copiedRemainingTokens();
        if (probeTokens == null) return false;
        CParser probe = new CParser(probeTokens);
        ((CParserBase) probe).configureSyntaxProbe(
                declaration ? tokens().LT(1).getStartIndex() : null);
        probe.removeErrorListeners();
        probe.removeParseListeners();
        probe.setErrorHandler(new BailErrorStrategy());
        try {
            if (declaration) probe.declaration();
            else probe.statement();
            return probe.getNumberOfSyntaxErrors() == 0;
        } catch (RuntimeException rejected) {
            return false;
        }
    }

    private SyntaxAlternativeDecision probeCast() {
        return new SyntaxAlternativeDecision(probeExpression(true), probeExpression(false));
    }

    private SyntaxProbeResult probeExpression(boolean unknownTypeNames) {
        CommonTokenStream probeTokens = copiedRemainingTokens();
        if (probeTokens == null) return SyntaxProbeResult.REJECTED;
        CParser probe = new CParser(probeTokens);
        ((CParserBase) probe).configureSyntaxProbe(
                unknownTypeNames ? tokens().LT(2).getStartIndex() : null);
        probe.removeErrorListeners();
        probe.removeParseListeners();
        probe.setErrorHandler(new BailErrorStrategy());
        try {
            probe.expression();
            return new SyntaxProbeResult(
                    probe.getNumberOfSyntaxErrors() == 0, probeTokens.index());
        } catch (RuntimeException rejected) {
            return SyntaxProbeResult.REJECTED;
        }
    }

    private SyntaxAlternativeDecision probeExternalDeclaration() {
        return new SyntaxAlternativeDecision(
                probeExternalDeclaration(true), probeExternalDeclaration(false));
    }

    private SyntaxProbeResult probeExternalDeclaration(boolean unknownTypeNames) {
        CommonTokenStream probeTokens = copiedRemainingTokens();
        if (probeTokens == null) return SyntaxProbeResult.REJECTED;
        CParser probe = new CParser(probeTokens);
        ((CParserBase) probe).configureSyntaxProbe(
                unknownTypeNames ? tokens().LT(1).getStartIndex() : null);
        probe.removeErrorListeners();
        probe.removeParseListeners();
        probe.setErrorHandler(new BailErrorStrategy());
        try {
            probe.externalDeclaration();
            return new SyntaxProbeResult(
                    probe.getNumberOfSyntaxErrors() == 0, probeTokens.index());
        } catch (RuntimeException rejected) {
            return SyntaxProbeResult.REJECTED;
        }
    }

    private CommonTokenStream copiedRemainingTokens() {
        CommonTokenStream sourceTokens = tokens();
        sourceTokens.fill();
        int start = sourceTokens.index();
        List<Token> allTokens = sourceTokens.getTokens();
        if (start < 0 || start >= allTokens.size()) return null;
        List<Token> copiedTokens = new ArrayList<>(allTokens.size() - start);
        for (Token token : allTokens.subList(start, allTokens.size())) {
            copiedTokens.add(new CommonToken(token));
        }
        return new CommonTokenStream(new ListTokenSource(copiedTokens));
    }

    private void configureSyntaxProbe(Integer typeNameStartOffset) {
        syntaxProbe = true;
        syntaxProbeTypeNameStartOffset = typeNameStartOffset;
    }

    private boolean inBlockItemDecision() {
        return ancestor(getContext(), CParser.BlockItemContext.class) != null;
    }

    private boolean isUndecidedExternalDeclaration() {
        List<String> ruleStack = getRuleInvocationStack();
        return ruleStack.contains("externalDeclaration")
                && !ruleStack.contains("functionDefinition")
                && !ruleStack.contains("declaration")
                && !ruleStack.contains("parameterDeclaration");
    }

    private boolean isDeclarationOnlyTypePosition() {
        List<String> ruleStack = getRuleInvocationStack();
        if (ruleStack.contains("parameterDeclaration")
                || ruleStack.contains("memberDeclaration")) {
            return true;
        }
        boolean insideFunctionBody = ruleStack.contains("functionBody")
                || ruleStack.contains("blockItem");
        if (ruleStack.contains("functionDefinition") && !insideFunctionBody) {
            return true;
        }
        if (ruleStack.contains("declaration")
                && ruleStack.contains("externalDeclaration")
                && !insideFunctionBody
                && !ruleStack.contains("forDeclaration")) {
            return true;
        }
        if (ancestor(getContext(), CParser.ParameterDeclarationContext.class) != null) {
            return true;
        }
        CParser.DeclarationSpecifiersContext declarationSpecifiers =
                ancestor(getContext(), CParser.DeclarationSpecifiersContext.class);
        if (declarationSpecifiers != null) {
            ParseTree owner = declarationSpecifiers.getParent();
            if (owner instanceof CParser.ParameterDeclarationContext
                    || owner instanceof CParser.FunctionDefinitionContext) {
                return true;
            }
            if (owner instanceof CParser.DeclarationContext declaration) {
                return ancestor(declaration, CParser.BlockItemContext.class) == null
                        && ancestor(declaration, CParser.ForDeclarationContext.class) == null;
            }
        }
        CParser.SpecifierQualifierListContext qualifiers =
                ancestor(getContext(), CParser.SpecifierQualifierListContext.class);
        return qualifiers != null
                && qualifiers.getParent() instanceof CParser.MemberDeclarationContext;
    }

    private boolean hasPriorTypeSpecifier(Token candidate) {
        ParserRuleContext context = getContext();
        while (context != null) {
            if (context instanceof CParser.DeclarationSpecifiersContext specifiers) {
                return specifiers.declarationSpecifier().stream()
                        .filter(item -> item.typeSpecifier() != null && item.getStop() != null)
                        .anyMatch(item -> item.getStop().getStopIndex()
                                < candidate.getStartIndex());
            }
            if (context instanceof CParser.SpecifierQualifierListContext specifiers) {
                return specifiers.typeSpecifierQualifier().stream()
                        .filter(item -> item.typeSpecifier() != null && item.getStop() != null)
                        .anyMatch(item -> item.getStop().getStopIndex()
                                < candidate.getStartIndex());
            }
            context = context.getParent();
        }
        return false;
    }

    private ScopeDescriptor lexicalScope(ParserRuleContext context) {
        CParser.CompoundStatementContext block =
                ancestor(context, CParser.CompoundStatementContext.class);
        return block == null
                ? new ScopeDescriptor("file", null)
                : new ScopeDescriptor("block", block);
    }

    private SourceRangeCandidate scopeRange(String scopeKind, ParserRuleContext context) {
        if ("file".equals(scopeKind)) {
            return new SourceRangeCandidate(0,
                    getInputStream().getTokenSource().getInputStream().size());
        }
        if (context == null || context.getStart() == null || context.getStop() == null) {
            throw new IllegalStateException("incomplete C symbol scope");
        }
        return new SourceRangeCandidate(
                context.getStart().getStartIndex(),
                context.getStop().getStopIndex() + 1);
    }

    private static SourceRangeCandidate range(Token token) {
        return new SourceRangeCandidate(token.getStartIndex(), token.getStopIndex() + 1);
    }

    private static LookupKey key(Token token) {
        return new LookupKey(token.getStartIndex(), token.getStopIndex() + 1);
    }

    private int tokenType(int k) {
        return tokens().LT(k).getType();
    }

    private CommonTokenStream tokens() {
        return (CommonTokenStream) getInputStream();
    }

    private <T extends ParserRuleContext> T nearest(Class<T> type) {
        return ancestor(getContext(), type);
    }

    private static <T extends ParserRuleContext> T ancestor(
            ParserRuleContext context, Class<T> type) {
        ParserRuleContext current = context;
        while (current != null) {
            if (type.isInstance(current)) return type.cast(current);
            current = current.getParent();
        }
        return null;
    }

    private record ScopeDescriptor(String kind, ParserRuleContext context) {
    }

    private record DefinitionRecord(
            Token token,
            String symbolKind,
            String scopeKind,
            ParserRuleContext scopeContext,
            int visibilityStartOffset,
            String grammarRule) {
    }

    private record ParameterScope(
            CParser.ParameterTypeListContext context,
            Symbol scope,
            List<Symbol> symbols) {
    }

    private record LookupKey(int start, int end) {
    }

    private record BlockItemDecision(boolean declaration, boolean statement) {
    }

    private record SyntaxProbeResult(boolean success, int consumedTokens) {
        private static final SyntaxProbeResult REJECTED = new SyntaxProbeResult(false, -1);
    }

    private record SyntaxAlternativeDecision(
            SyntaxProbeResult typeName, SyntaxProbeResult ordinary) {
        private boolean requiresTypeName() {
            return typeName.success()
                    && (!ordinary.success()
                        || typeName.consumedTokens() > ordinary.consumedTokens());
        }

        private boolean ambiguous() {
            return typeName.success() && ordinary.success()
                    && typeName.consumedTokens() == ordinary.consumedTokens();
        }

        private boolean ordinaryPreferred() {
            return ordinary.success()
                    && (!typeName.success()
                        || ordinary.consumedTokens() > typeName.consumedTokens());
        }
    }

    private record MutableLookup(
            Token token,
            String parserDecision,
            Symbol definition,
            String provenance,
            LinkedHashSet<String> contexts) {
    }
}
