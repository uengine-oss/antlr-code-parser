package legacymodernizer.parser.antlr.c;

import java.util.HashSet;
import java.util.Stack;

public class SymbolTable {
    private Stack<Symbol> scopeStack = new Stack<>();
    private int blockCounter = 0;

    public SymbolTable() {
        Symbol globalScope = createSymbol("global", TypeClassification.Global_);
        scopeStack.push(globalScope);
    }

    private Symbol createSymbol(String name, TypeClassification... classifications) {
        Symbol symbol = new Symbol();
        symbol.setName(name);
        HashSet<TypeClassification> classSet = new HashSet<>();
        for (TypeClassification c : classifications) {
            classSet.add(c);
        }
        symbol.setClassification(classSet);
        symbol.setPredefined(true);
        return symbol;
    }

    public void enterScope(Symbol newScope) {
        Symbol current = scopeStack.peek();
        if (newScope == current) return;
        scopeStack.push(newScope);
    }

    public void exitScope() {
        scopeStack.pop();
        if (scopeStack.isEmpty()) {
            throw new RuntimeException("Cannot exit global scope");
        }
    }

    public Symbol currentScope() {
        if (scopeStack.isEmpty()) return null;
        return scopeStack.peek();
    }

    public boolean define(Symbol symbol) {
        Symbol currentScope = currentScope();
        return defineInScope(currentScope, symbol);
    }

    public boolean defineInScope(Symbol currentScope, Symbol symbol) {
        if (currentScope.getMembers().containsKey(symbol.getName())) {
            return false;
        }
        symbol.setParent(currentScope);
        currentScope.getMembers().put(symbol.getName(), symbol);
        return true;
    }

    public Symbol resolve(String name) {
        return resolve(name, null);
    }

    public Symbol resolve(String name, Symbol startScope) {
        if (startScope == null) {
            for (int i = scopeStack.size() - 1; i >= 0; i--) {
                Symbol scope = scopeStack.get(i);
                Symbol symbol = scope.getMembers().get(name);
                if (symbol != null) {
                    return symbol;
                }
            }
            return null;
        } else {
            return startScope.getMembers().get(name);
        }
    }

    public Symbol pushBlockScope() {
        return pushScope("block");
    }

    public Symbol pushPrototypeScope() {
        return pushScope("prototype");
    }

    private Symbol pushScope(String prefix) {
        Symbol blockScope = new Symbol();
        blockScope.setName(prefix + (++blockCounter));
        HashSet<TypeClassification> classSet = new HashSet<>();
        classSet.add(TypeClassification.Block_);
        blockScope.setClassification(classSet);
        blockScope.setPredefined(true);
        enterScope(blockScope);
        return blockScope;
    }

    public void popBlockScope() {
        exitScope();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        toStringHelper(sb, scopeStack.get(0), 0);
        return sb.toString();
    }

    private void toStringHelper(StringBuilder sb, Symbol scope, int depth) {
        String indent = "  ".repeat(depth);
        for (var entry : scope.getMembers().entrySet()) {
            Symbol sym = entry.getValue();
            if (!sym.isPredefined()) {
                sb.append(indent).append(sym.toString()).append("\n");
            }
            if (sym.getClassification().contains(TypeClassification.Block_) ||
                sym.getClassification().contains(TypeClassification.Function_)) {
                toStringHelper(sb, sym, depth + 1);
            }
        }
    }
}
