import exceptions.EvaluationException;

import java.util.HashMap;

class Negate implements Expression {
    private Expression negative;

    Negate(Expression negative) {
        this.negative = negative;
    }

    @Override
    public boolean isBaseFor(Expression derived, HashMap<Expression, Expression> checker) throws EvaluationException {
        if (!derived.getClass().equals(this.getClass())) return false;
        Negate other = (Negate) derived;
        return negative.isBaseFor(other.negative, checker);
    }

    @Override
    public String toString() {
        return "Not(" + negative.toString() + ")";
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
