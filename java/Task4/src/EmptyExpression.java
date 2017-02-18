import java.util.Map;
import java.util.Set;

class EmptyExpression implements Expression {

    EmptyExpression() {
    }

    @Override
    public boolean isBaseFor(Expression derived, Map<Expression, Expression> checker) {
        return false;
    }

    @Override
    public Expression substitute(Map<Variable, Expression> changes, Map<Expression, Integer> counter) {
        return this;
    }

    @Override
    public void getFreeVariables(Map<Expression, Integer> counter, Set<Expression> accumulator) {
    }

    @Override
    public boolean isTermFreeToSubstituteInsteadOf(Set<Expression> freeVars, Expression insteadVariable, Map<Expression, Integer> counter) {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || o.getClass().equals(this.getClass());
    }

    @Override
    public int hashCode() {
        return "EmptyExpression".hashCode();
    }

}
