import exceptions.EvaluationException;

class Or extends AbstractBinaryOperation implements Expression {
    @Override
    public String toString() {
        return "(" + firstArgument.toString() + ")|(" + secondArgument.toString() + ")";
    }

    Or(Expression firstArgument, Expression secondArgument) {
        super(firstArgument, secondArgument);
    }
}
