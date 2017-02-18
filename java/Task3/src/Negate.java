import exceptions.EvaluationException;

import java.util.Map;
import java.util.Set;

class Negate implements Expression {
    Expression negative;

    Negate(Expression negative) {
        this.negative = negative;
    }

    @Override
    public boolean evaluate(Map<Symbol, Boolean> estimations) {
        return !negative.evaluate(estimations);
    }

    @Override
    public boolean isBaseFor(Expression derived, Map<Expression, Expression> checker) throws EvaluationException {
        if (!derived.getClass().equals(this.getClass())) return false;
        Negate other = (Negate) derived;
        return negative.isBaseFor(other.negative, checker);
    }

    @Override
    public Set<Symbol> getPropositionalVariables() {
        return negative.getPropositionalVariables();
    }

    @Override
    public Expression substitute(Map<Symbol, Expression> changes) {
        return new Negate(negative.substitute(changes));
    }

    @Override
    public String toString() {
        return "!(" + negative.toString() + ")";
    }

    @Override
    public int hashCode() {
        return ~negative.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!obj.getClass().equals(this.getClass())) return false;
        Negate other = (Negate) obj;
        return negative.equals(other.negative);
    }
}
