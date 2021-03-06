package Hometask7.hierarchy;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class BinaryOperator implements Expression {
    public Expression firstArgument, secondArgument;

    BinaryOperator(Expression firstArgument, Expression secondArgument) {
        this.firstArgument = firstArgument;
        this.secondArgument = secondArgument;
    }

    @Override
    public Expression substitute(HashMap<Variable, Expression> changes, HashMap<Expression, Integer> counter) {
        try {
            Constructor ctor = this.getClass().getDeclaredConstructor(Expression.class, Expression.class);
            return (Expression) ctor.newInstance(firstArgument.substitute(changes, counter), secondArgument.substitute(changes, counter));
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            System.err.println("Error during creating new instance " + e.getMessage());
        }
        return this;
    }

    @Override
    public boolean isBaseFor(Expression derived, HashMap<Expression, Expression> checker) {
        if (!derived.getClass().equals(this.getClass())) return false;
        BinaryOperator other = (BinaryOperator) derived;
        return firstArgument.isBaseFor(other.firstArgument, checker) && secondArgument.isBaseFor(other.secondArgument, checker);
    }

    @Override
    public void getFreeVariables(Map<Expression, Integer> counter, Set<Expression> accumulator) {
        firstArgument.getFreeVariables(counter, accumulator);
        secondArgument.getFreeVariables(counter, accumulator);
    }

    @Override
    public boolean isTermFreeToSubstituteInsteadOf(Set<Expression> freeVars, Expression insteadVariable, Map<Expression, Integer> counter) {
        return firstArgument.isTermFreeToSubstituteInsteadOf(freeVars, insteadVariable, counter) &&
                secondArgument.isTermFreeToSubstituteInsteadOf(freeVars, insteadVariable, counter);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!o.getClass().equals(this.getClass())) return false;
        BinaryOperator that = (BinaryOperator) o;
        return firstArgument.equals(that.firstArgument) && secondArgument.equals(that.secondArgument);
    }

    @Override
    public int hashCode() {
        int result = firstArgument.hashCode();
        result = 31 * result + secondArgument.hashCode();
        return result;
    }
}