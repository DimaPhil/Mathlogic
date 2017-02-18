package Hometask7.hierarchy;

import Hometask7.exceptions.EvaluationException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Negate implements Expression {
    public Expression negative;

    public Negate(Expression negative) {
        this.negative = negative;
    }

    @Override
    public boolean isBaseFor(Expression derived, HashMap<Expression, Expression> checker) throws EvaluationException {
        if (!derived.getClass().equals(this.getClass())) return false;
        Negate other = (Negate) derived;
        return negative.isBaseFor(other.negative, checker);
    }

    @Override
    public Expression substitute(HashMap<Variable, Expression> changes, HashMap<Expression, Integer> counter) {
        return new Negate(negative.substitute(changes, counter));
    }

    @Override
    public Expression substitute(HashMap<Variable, Expression> changes) {
        return new Negate(negative.substitute(changes));
    }

    @Override
    public void getFreeVariables(Map<Expression, Integer> counter, Set<Expression> accumulator) {
        negative.getFreeVariables(counter, accumulator);
    }

    @Override
    public boolean isTermFreeToSubstituteInsteadOf(Set<Expression> freeVars, Expression insteadVariable, Map<Expression, Integer> counter) {
        return negative.isTermFreeToSubstituteInsteadOf(freeVars, insteadVariable, counter);
    }

    @Override
    public String toString() {
        return "!(" + negative.toString() + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!o.getClass().equals(this.getClass())) return false;
        Negate negate = (Negate) o;
        return negative.equals(negate.negative);
    }

    @Override
    public int hashCode() {
        return negative.hashCode();
    }
}
