package Hometask7.hierarchy;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ForAll implements Expression {

    public Expression variable;
    public Expression statement;

    public ForAll(Expression variable, Expression statement) {
        this.variable = variable;
        this.statement = statement;
    }

    @Override
    public String toString() {
        /*
        Uncomment this to get nice representation
        return "\u2200" + var.toString() + "(" + statement.toString() + ")";
        */
        return "@" + variable.toString() + "(" + statement.toString() + ")";
    }

    @Override
    public boolean isBaseFor(Expression derived, HashMap<Expression, Expression> checker) {
        if (!derived.getClass().equals(this.getClass())) return false;
        ForAll other = (ForAll) derived;
        return variable.equals(other.variable) && statement.isBaseFor(other.statement, checker);
    }

    @Override
    public Expression substitute(HashMap<Variable, Expression> changes, HashMap<Expression, Integer> counter) {
        int count = counter.containsKey(variable) ? counter.get(variable) : 0;
        counter.put(variable, count + 1);
        Expression newStatement = statement.substitute(changes, counter);
        counter.put(variable, count);
        return new ForAll(variable, newStatement);
    }

    @Override
    public boolean isTermFreeToSubstituteInsteadOf(Set<Expression> freeVars, Expression insteadVariable, Map<Expression, Integer> counter) {
        int count = counter.containsKey(variable) ? counter.get(variable) : 0;
        counter.put(variable, count + 1);
        if (!statement.isTermFreeToSubstituteInsteadOf(freeVars, insteadVariable, counter)) {
            return false;
        }
        counter.put(variable, count);
        return true;
    }

    @Override
    public void getFreeVariables(Map<Expression, Integer> counter, Set<Expression> accumulator) {
        int count = counter.containsKey(variable) ? counter.get(variable) : 0;
        counter.put(variable, count + 1);
        statement.getFreeVariables(counter, accumulator);
        counter.put(variable, count);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!o.getClass().equals(this.getClass())) return false;
        ForAll that = (ForAll) o;
        return statement.equals(that.statement) && variable.equals(that.variable);
    }

    @Override
    public int hashCode() {
        int result = variable.hashCode();
        result = 31 * result + statement.hashCode();
        return result;
    }
}
