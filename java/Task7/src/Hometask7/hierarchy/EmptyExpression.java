package Hometask7.hierarchy;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class EmptyExpression implements Expression {

    public EmptyExpression() {
    }

    @Override
    public boolean isBaseFor(Expression derived, HashMap<Expression, Expression> checker) {
        return false;
    }

    @Override
    public Expression substitute(HashMap<Variable, Expression> changes, HashMap<Expression, Integer> counter) {
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
