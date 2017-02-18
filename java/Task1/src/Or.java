import exceptions.EvaluationException;

class Or extends AbstractBinaryOperation implements Expression {

    @Override
    protected boolean implOperation(boolean first, boolean second) throws EvaluationException {
        return first || second;
    }

    @Override
    public String toString() {
        return "(" + firstArgument.toString() + ") ||  (" + secondArgument.toString() + ")";
    }

    Or(Expression firstArgument, Expression secondArgument) {
        super(firstArgument, secondArgument);
    }
}
