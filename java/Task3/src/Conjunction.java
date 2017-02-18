import exceptions.EvaluationException;

import java.util.Map;

class Conjunction extends AbstractBinaryOperation implements Expression {

    @Override
    public String toString() {
        return "(" + firstArgument.toString() + ") & (" + secondArgument.toString() + ")";
    }

    Conjunction(Expression firstArgument, Expression secondArgument) {
        super(firstArgument, secondArgument);
    }

    @Override
    protected boolean implOperation(boolean first, boolean second) throws EvaluationException {
        return first && second;
    }

    @Override
    public Expression substitute(Map<Symbol, Expression> changes) {
        return new Conjunction(firstArgument.substitute(changes), secondArgument.substitute(changes));
    }
}
