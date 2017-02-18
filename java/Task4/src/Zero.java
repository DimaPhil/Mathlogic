import java.util.Map;
import java.util.Set;

class Zero implements Expression {

    Zero() {
    }

    @Override
    public String toString() {
        return "0";
    }

    @Override
    public boolean equals(Object o) {
        return this == o || o.getClass().equals(this.getClass());
    }

    @Override
    public boolean isBaseFor(Expression derived, Map<Expression, Expression> checker) {
        return this.equals(derived);
    }

    @Override
    public Expression substitute(Map<Variable, Expression> changes, Map<Expression, Integer> counter) {
        return new Zero();
    }

    @Override
    public Expression substitute(Map<Variable, Expression> changes) {
        return this;
    }

    @Override
    public void getFreeVariables(Map<Expression, Integer> counter, Set<Expression> accumulator) {
    }

    @Override
    public boolean isTermFreeToSubstituteInsteadOf(Set<Expression> freeVars, Expression insteadVariable, Map<Expression, Integer> counter) {
        return true;
    }

    @Override
    public int hashCode() {
        return "0".hashCode();
    }

}
