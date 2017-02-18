package Hometask7.hierarchy;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Quote implements Expression {

    public Expression incremented;

    public Quote(Expression incremented) {
        this.incremented = incremented;
    }

    @Override
    public String toString() {
        if (incremented.getClass().equals(Zero.class)
                || incremented.getClass().equals(Variable.class)
                || incremented.getClass().equals(Quote.class)) {
            return incremented.toString() + "\'";
        }
        return "(" + incremented.toString() + ")" + '\'';
    }

    @Override
    public boolean isBaseFor(Expression derived, HashMap<Expression, Expression> checker) {
        if (!derived.getClass().equals(this.getClass())) return false;
        Quote other = (Quote) derived;
        return incremented.isBaseFor(other.incremented, checker);
    }

    @Override
    public Expression substitute(HashMap<Variable, Expression> changes, HashMap<Expression, Integer> counter) {
        return new Quote(incremented.substitute(changes, counter));
    }

    @Override
    public void getFreeVariables(Map<Expression, Integer> counter, Set<Expression> accumulator) {
        incremented.getFreeVariables(counter, accumulator);
    }

    @Override
    public boolean isTermFreeToSubstituteInsteadOf(Set<Expression> freeVars, Expression insteadVariable, Map<Expression, Integer> counter) {
        return incremented.isTermFreeToSubstituteInsteadOf(freeVars, insteadVariable, counter);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!o.getClass().equals(this.getClass())) return false;
        Quote quote = (Quote) o;
        return incremented.equals(quote.incremented);
    }

    @Override
    public int hashCode() {
        return incremented.hashCode();
    }
}
