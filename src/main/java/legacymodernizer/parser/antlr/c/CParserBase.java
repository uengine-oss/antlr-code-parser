package legacymodernizer.parser.antlr.c;

import org.antlr.v4.runtime.*;
import java.util.*;

/**
 * CParser Base 클래스 (grammars-v4 기반)
 * C 언어 파싱에 필요한 심볼 테이블 관리 및 semantic predicate 구현
 */
public abstract class CParserBase extends Parser {
    private SymbolTable _st;
    private boolean debug = false;
    private Set<String> noSemantics = new HashSet<>();

    protected CParserBase(TokenStream input) {
        super(input);
        _st = new SymbolTable();
    }

    public boolean IsAlignmentSpecifier() { return IsAlignmentSpecifier(1); }
    public boolean IsAlignmentSpecifier(int k) {
        if (noSemantics.contains("IsAlignmentSpecifier")) return true;
        Token lt1 = ((CommonTokenStream) this.getInputStream()).LT(k);
        Symbol resolved = resolveSymbol(lt1);
        return resolved != null && resolved.getClassification().contains(TypeClassification.AlignmentSpecifier_);
    }

    public boolean IsAtomicTypeSpecifier() { return IsAtomicTypeSpecifier(1); }
    public boolean IsAtomicTypeSpecifier(int k) {
        if (noSemantics.contains("IsAtomicTypeSpecifier")) return true;
        Token lt1 = ((CommonTokenStream) this.getInputStream()).LT(k);
        Symbol resolved = resolveSymbol(lt1);
        return resolved != null && resolved.getClassification().contains(TypeClassification.AtomicTypeSpecifier_);
    }

    public boolean IsAttributeDeclaration() {
        return IsAttributeSpecifierSequence();
    }

    public boolean IsAttributeSpecifier() {
        Token lt1 = ((CommonTokenStream) this.getInputStream()).LT(1);
        return lt1.getType() == CLexer.LeftBracket;
    }

    public boolean IsAttributeSpecifierSequence() {
        return IsAttributeSpecifier();
    }

    public boolean IsDeclaration() {
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

    public boolean IsTypeSpecifierQualifier() { return IsTypeSpecifierQualifier(1); }
    public boolean IsTypeSpecifierQualifier(int k) {
        return IsTypeSpecifier(k) || IsTypeQualifier(k) || IsAlignmentSpecifier(k);
    }

    public boolean IsDeclarationSpecifiers() {
        return IsDeclarationSpecifier();
    }

    public boolean IsEnumSpecifier() { return IsEnumSpecifier(1); }
    public boolean IsEnumSpecifier(int k) {
        Token lt1 = ((CommonTokenStream) this.getInputStream()).LT(k);
        return lt1.getType() == CLexer.Enum;
    }

    public boolean IsFunctionSpecifier() {
        Token lt1 = ((CommonTokenStream) this.getInputStream()).LT(1);
        Symbol resolved = resolveSymbol(lt1);
        return resolved != null && resolved.getClassification().contains(TypeClassification.FunctionSpecifier_);
    }

    public boolean IsGnuAttributeBeforeDeclarator() { return IsGnuAttributeBeforeDeclarator(1); }
    public boolean IsGnuAttributeBeforeDeclarator(int k) {
        CommonTokenStream ts = (CommonTokenStream) this.getInputStream();
        int i = k;
        if (ts.LT(i).getType() != CLexer.Attribute) return false;
        i++;
        int depth = 0;
        while (true) {
            Token t = ts.LT(i++);
            if (t.getType() == Token.EOF) return false;
            if (t.getType() == CLexer.LeftParen) depth++;
            else if (t.getType() == CLexer.RightParen) { depth--; if (depth == 0) break; }
        }
        int next = ts.LT(i).getType();
        return next == CLexer.Identifier || next == CLexer.Star || next == CLexer.LeftParen;
    }

    public boolean IsStatement() {
        Token t1 = ((CommonTokenStream) this.getInputStream()).LT(1);
        Token t2 = ((CommonTokenStream) this.getInputStream()).LT(2);
        if (t1.getType() == CLexer.Identifier && t2.getType() == CLexer.Colon) {
            return true;
        }
        return !IsDeclaration();
    }

    public boolean IsStaticAssertDeclaration() {
        Token token = ((CommonTokenStream) this.getInputStream()).LT(1);
        return token.getType() == CLexer.Static_assert;
    }

    public boolean IsStorageClassSpecifier() {
        Token lt1 = ((CommonTokenStream) this.getInputStream()).LT(1);
        Symbol resolved = resolveSymbol(lt1);
        return resolved != null && resolved.getClassification().contains(TypeClassification.StorageClassSpecifier_);
    }

    public boolean IsStructOrUnionSpecifier() { return IsStructOrUnionSpecifier(1); }
    public boolean IsStructOrUnionSpecifier(int k) {
        Token token = ((CommonTokenStream) this.getInputStream()).LT(k);
        return token.getType() == CLexer.Struct || token.getType() == CLexer.Union;
    }

    public boolean IsTypedefName() { return IsTypedefName(1); }
    public boolean IsTypedefName(int k) {
        Token lt1 = ((CommonTokenStream) this.getInputStream()).LT(k);
        Symbol resolved = resolveSymbol(lt1);
        if (resolved == null) return false;
        if (resolved.getClassification().contains(TypeClassification.Variable_)) return false;
        if (resolved.getClassification().contains(TypeClassification.Function_)) return false;
        return true;
    }

    public boolean IsTypeofSpecifier() { return IsTypeofSpecifier(1); }
    public boolean IsTypeofSpecifier(int k) {
        Token token = ((CommonTokenStream) this.getInputStream()).LT(k);
        return token.getType() == CLexer.Typeof || token.getType() == CLexer.Typeof_unqual;
    }

    public boolean IsTypeQualifier() { return IsTypeQualifier(1); }
    public boolean IsTypeQualifier(int k) {
        Token lt1 = ((CommonTokenStream) this.getInputStream()).LT(k);
        Symbol resolved = resolveSymbol(lt1);
        return resolved != null && resolved.getClassification().contains(TypeClassification.TypeQualifier_);
    }

    public boolean IsTypeSpecifier() { return IsTypeSpecifier(1); }
    public boolean IsTypeSpecifier(int k) {
        Token lt1 = ((CommonTokenStream) this.getInputStream()).LT(k);
        Symbol resolved = resolveSymbol(lt1);
        if (resolved != null && resolved.getClassification().contains(TypeClassification.TypeSpecifier_)) {
            return true;
        }
        return IsAtomicTypeSpecifier(k) || IsStructOrUnionSpecifier(k) || IsEnumSpecifier(k)
                || IsTypedefName(k) || IsTypeofSpecifier(k);
    }

    public void EnterDeclaration() {
        ParserRuleContext context = this.getContext();
        while (context != null) {
            if (context instanceof CParser.DeclarationContext) {
                CParser.DeclarationContext declaration_context = (CParser.DeclarationContext) context;
                CParser.DeclarationSpecifiersContext declaration_specifiers = declaration_context.declarationSpecifiers();
                CParser.DeclarationSpecifierContext[] declaration_specifier = declaration_specifiers != null ?
                        declaration_specifiers.declarationSpecifier().toArray(new CParser.DeclarationSpecifierContext[0]) : null;

                CParser.InitDeclaratorListContext init_declarator_list = declaration_context.initDeclaratorList();
                List<CParser.InitDeclaratorContext> init_declarators = init_declarator_list != null ?
                        init_declarator_list.initDeclarator() : null;

                if (init_declarators != null) {
                    boolean isTypedef = false;
                    if (declaration_specifier != null) {
                        for (CParser.DeclarationSpecifierContext ds : declaration_specifier) {
                            if (ds.storageClassSpecifier() != null && ds.storageClassSpecifier().Typedef() != null) {
                                isTypedef = true;
                                break;
                            }
                        }
                    }
                    for (CParser.InitDeclaratorContext id : init_declarators) {
                        CParser.DeclaratorContext y = id != null ? id.declarator() : null;
                        Token idToken = getDeclarationToken(y);
                        if (idToken != null) {
                            String text = idToken.getText();
                            if (isTypedef) {
                                Symbol symbol = new Symbol();
                                symbol.setName(text);
                                HashSet<TypeClassification> classSet = new HashSet<>();
                                classSet.add(TypeClassification.TypeSpecifier_);
                                symbol.setClassification(classSet);
                                _st.define(symbol);
                            } else {
                                Symbol symbol = new Symbol();
                                symbol.setName(text);
                                HashSet<TypeClassification> classSet = new HashSet<>();
                                classSet.add(TypeClassification.Variable_);
                                symbol.setClassification(classSet);
                                _st.define(symbol);
                            }
                        }
                    }
                }
            }
            if (context instanceof CParser.FunctionDefinitionContext) {
                CParser.FunctionDefinitionContext fd = (CParser.FunctionDefinitionContext) context;
                CParser.DeclaratorContext de = fd.declarator();
                CParser.DirectDeclaratorContext dd = de != null ? de.directDeclarator() : null;
                if (dd != null && dd.Identifier() != null) {
                    Token idToken = dd.Identifier().getSymbol();
                    String text = idToken.getText();
                    Symbol symbol = new Symbol();
                    symbol.setName(text);
                    HashSet<TypeClassification> classSet = new HashSet<>();
                    classSet.add(TypeClassification.Function_);
                    symbol.setClassification(classSet);
                    _st.define(symbol);
                    return;
                }
            }
            context = context.getParent();
        }
    }

    private Token getDeclarationToken(CParser.DeclaratorContext y) {
        if (y == null) return null;
        CParser.DirectDeclaratorContext directDeclarator = y.directDeclarator();
        if (directDeclarator != null) {
            CParser.DeclaratorContext more = directDeclarator.declarator();
            Token token = getDeclarationToken(more);
            if (token != null) return token;
            if (directDeclarator.Identifier() != null) {
                return directDeclarator.Identifier().getSymbol();
            }
        }
        return null;
    }

    public boolean IsNullStructDeclarationListExtension() {
        return true;
    }

    public void EnterScope() {
        _st.pushBlockScope();
    }

    public void ExitScope() {
        _st.popBlockScope();
    }

    public void LookupSymbol() {
        // Applied occurrence lookup - no-op for our use case
    }

    public void OutputSymbolTable() {
        // No-op for our use case
    }

    /**
     * 외부에서 수집한 typedef 이름을 심볼 테이블에 등록
     */
    public void registerTypeName(String name) {
        Symbol symbol = new Symbol();
        symbol.setName(name);
        HashSet<TypeClassification> classSet = new HashSet<>();
        classSet.add(TypeClassification.TypeSpecifier_);
        symbol.setClassification(classSet);
        symbol.setPredefined(true);
        _st.define(symbol);
    }

    /**
     * 여러 typedef 이름을 일괄 등록
     */
    public void registerTypeNames(java.util.Collection<String> names) {
        for (String name : names) {
            registerTypeName(name);
        }
    }

    private Symbol resolveSymbol(Token token) {
        if (token == null) return null;
        return _st.resolve(token.getText());
    }

    public boolean IsInitDeclaratorList() {
        Token lt1 = ((CommonTokenStream) this.getInputStream()).LT(1);
        Symbol resolved = resolveSymbol(lt1);
        if (resolved == null) return true;
        if (resolved.getClassification().contains(TypeClassification.TypeQualifier_) ||
            resolved.getClassification().contains(TypeClassification.TypeSpecifier_)) {
            return false;
        }
        return true;
    }

    public boolean IsSomethingOfTypeName() {
        CommonTokenStream ts = (CommonTokenStream) this.getInputStream();
        int lt1Type = ts.LT(1).getType();
        if (!(lt1Type == CLexer.Sizeof ||
              lt1Type == CLexer.Countof ||
              lt1Type == CLexer.Alignof ||
              lt1Type == CLexer.Maxof ||
              lt1Type == CLexer.Minof)) return false;
        if (ts.LT(2).getType() != CLexer.LeftParen) return false;
        return IsTypeName(3);
    }

    public boolean IsTypeName() { return IsTypeName(1); }
    public boolean IsTypeName(int k) {
        return IsSpecifierQualifierList(k);
    }

    public boolean IsSpecifierQualifierList() { return IsSpecifierQualifierList(1); }
    public boolean IsSpecifierQualifierList(int k) {
        if (IsGnuAttributeBeforeDeclarator(k)) return true;
        return IsTypeSpecifierQualifier(k);
    }

    public boolean IsCast() {
        Token t1 = ((CommonTokenStream) this.getInputStream()).LT(1);
        Token t2 = ((CommonTokenStream) this.getInputStream()).LT(2);
        if (t1.getType() != CLexer.LeftParen) {
            return false;
        } else if (t2.getType() != CLexer.Identifier) {
            return true;
        } else {
            Symbol resolved = resolveSymbol(t2);
            if (resolved == null) return false;
            return resolved.getClassification().contains(TypeClassification.TypeSpecifier_);
        }
    }
}
