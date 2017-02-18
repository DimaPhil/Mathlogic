import java.util.*;

public class Variable implements Expression {

    private String variableName;

    public Variable(String variableName) {
        this.variableName = variableName;
    }

    @Override
    public String toString() {
        return variableName;
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
    public Expression substitute(Map<Variable, Expression> changes, Map<Expression, Integer> counter) {
        if (!counter.containsKey(this) || counter.get(this) == 0) {
            if (changes.containsKey(this)) {
                return changes.get(this);
            }
        }
        return this;
    }

    @Override
    public void getFreeVariables(Map<Expression, Integer> counter, Set<Expression> accumulator) {
        if (!counter.containsKey(this) || counter.get(this) == 0) {
            accumulator.add(this);
        }
    }

    @Override
    public boolean isTermFreeToSubstituteInsteadOf(Set<Expression> freeVars, Expression insteadVariable, Map<Expression, Integer> counter) {
        if (this.equals(insteadVariable)) {
            if (!counter.containsKey(this) || counter.get(this) == 0) {
                for (Expression var : freeVars) {
                    if (counter.containsKey(var) && counter.get(var) > 0) {
                        //Free variable of Tetta will be closed by some quantifier of base expression
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!o.getClass().equals(this.getClass())) return false;
        Variable variable = (Variable) o;
        return variableName.equals(variable.variableName);
    }

    @Override
    public int hashCode() {
        return variableName.hashCode();
    }
}
