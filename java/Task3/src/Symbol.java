import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class Symbol implements Expression {
    private String symbolName;

    Symbol(String symbolName) {
        this.symbolName = symbolName;
    }

    @Override
    public String toString() {
        return symbolName;
    }

    @Override
    public boolean evaluate(Map<Symbol, Boolean> estimations) {
        if (estimations.containsKey(this))
            return estimations.get(this);
        return false;
    }

    @Override
    public boolean isBaseFor(Expression derived, Map<Expression, Expression> checker) {
        if (checker.containsKey(this)) {
            return checker.get(this).equals(derived);
        } else {
            checker.put(this, derived);
            return true;
        }
    }

    @Override
    public Set<Symbol> getPropositionalVariables() {
        Set<Symbol> result = new HashSet<>();
        result.add(this);
        return result;
    }

    @Override
    public Expression substitute(Map<Symbol, Expression> changes) {
        return changes.get(this);
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