import exceptions.EvaluationException;

import java.util.Map;

class Disjunction extends AbstractBinaryOperation implements Expression {

    @Override
    protected boolean implOperation(boolean first, boolean second) throws EvaluationException {
        return first || second;
    }

    @Override
    public String toString() {
        return "(" + firstArgument.toString() + ")|(" + secondArgument.toString() + ")";
    }

    Disjunction(Expression firstArgument, Expression secondArgument) {
        super(firstArgument, secondArgument);
    }

    @Override
    public Expression substitute(Map<Symbol, Expression> changes) {
        return new Disjunction(firstArgument.substitute(changes), secondArgument.substitute(changes));
    }
}