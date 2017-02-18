import java.util.HashMap;

class Symbol implements Expression {
    private String symbolName;

    Symbol(String symbolName) {
        this.symbolName = symbolName;
    }

    @Override
    public String toString() {
        return "\"" + symbolName + "\"";
    }

    @Override
    public boolean isBaseFor(Expression derived, HashMap<Expression, Expression> checker) {
        if (checker.containsKey(this)) {
            return checker.get(this).equals(derived);
        } else {
            checker.put(this, derived);
            return true;
        }
    }

    @Override
    public int hashCode() {
        return symbolName.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!obj.getClass().equals(this.getClass())) return false;
        Symbol other = (Symbol) obj;
        return symbolName.equals(other.symbolName);
    }
}
