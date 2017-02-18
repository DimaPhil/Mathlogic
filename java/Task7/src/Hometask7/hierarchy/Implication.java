package Hometask7.hierarchy;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Implication implements Expression {
    public Expression statement;
    public Expression consequence;

    public Implication(Expression statement, Expression consequence) {
        this.statement = statement;
        this.consequence = consequence;
    }

    @Override
    public boolean isBaseFor(Expression derived, HashMap<Expression, Expression> checker) {
        if (!derived.getClass().equals(this.getClass())) return false;
        Implication other = (Implication) derived;
        return statement.isBaseFor(other.statement, checker) && consequence.isBaseFor(other.consequence, checker);
    }

    @Override
    public Expression substitute(HashMap<Variable, Expression> changes, HashMap<Expression, Integer> counter) {
        return new Implication(statement.substitute(changes, counter), consequence.substitute(changes, counter));
    }

    @Override
    public String toString() {
        return "(" + statement.toString() + ")->(" + consequence.toString() + ")";
    }

    @Override
    public Expression substitute(HashMap<Variable, Expression> changes) {
        return new Implication(statement.substitute(changes), consequence.substitute(changes));
    }

    @Override
    public void getFreeVariables(Map<Expression, Integer> counter, Set<Expression> accumulator) {
        statement.getFreeVariables(counter, accumulator);
        consequence.getFreeVariables(counter, accumulator);
    }

    @Override
    public boolean isTermFreeToSubstituteInsteadOf(Set<Expression> freeVars, Expression insteadVariable, Map<Expression, Integer> counter) {
        return statement.isTermFreeToSubstituteInsteadOf(freeVars, insteadVariable, counter) &&
                consequence.isTermFreeToSubstituteInsteadOf(freeVars, insteadVariable, counter);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!o.getClass().equals(this.getClass())) return false;
        Implication that = (Implication) o;
        return consequence.equals(that.consequence) && statement.equals(that.statement);
    }

    @Override
    public int hashCode() {
        int result = statement.hashCode();
        result = 31 * result + consequence.hashCode();
        return result;
    }
}
