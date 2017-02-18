import exceptions.EvaluationException;

public class And extends AbstractBinaryOperation implements Expression {

    @Override
    public String toString() {
        return "(" + firstArgument.toString() + ") && (" + secondArgument.toString() + ")";
    }

    public And(Expression firstArgument, Expression secondArgument) {
        super(firstArgument, secondArgument);
    }

    @Override
    protected boolean implOperation(boolean first, boolean second) throws EvaluationException {
        return first && second;
    }
}
