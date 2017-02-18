import exceptions.EvaluationException;

import java.util.Map;
import java.util.Set;

abstract class AbstractBinaryOperation implements Expression {
    Expression firstArgument, secondArgument;
    AbstractBinaryOperation(Expression firstArgument, Expression secondArgument) {
        this.firstArgument = firstArgument;
        this.secondArgument = secondArgument;
    }

    @Override
    public boolean isBaseFor(Expression derived, Map<Expression, Expression> checker) {
        if (!derived.getClass().equals(this.getClass())) return false;
        AbstractBinaryOperation other = (AbstractBinaryOperation) derived;
        boolean checkFirstArgument = firstArgument.isBaseFor(other.firstArgument, checker);
        boolean checkSecondArgument = secondArgument.isBaseFor(other.secondArgument, checker);
        return checkFirstArgument && checkSecondArgument;
    }

    abstract protected boolean implOperation(boolean first, boolean second) throws EvaluationException;

    @Override
    public int hashCode() {
        return firstArgument.hashCode() * secondArgument.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!obj.getClass().equals(this.getClass())) return false;
        AbstractBinaryOperation other = (AbstractBinaryOperation) obj;
        return firstArgument.equals(other.firstArgument) && secondArgument.equals(other.secondArgument);
    }

    @Override
    public boolean evaluate(Map<Symbol, Boolean> estimations) {
        return implOperation(firstArgument.evaluate(estimations), secondArgument.evaluate(estimations));
    }

    @Override
    public Set<Symbol> getPropositionalVariables() {
        Set<Symbol> fromFirstArgument = firstArgument.getPropositionalVariables();
        Set<Symbol> fromSecondArgument = secondArgument.getPropositionalVariables();
        fromFirstArgument.addAll(fromSecondArgument);
        return fromFirstArgument;
    }
}
