class Equals extends AbstractBinaryOperation implements Expression {

    Equals(Expression firstArgument, Expression secondArgument) {
        super(firstArgument, secondArgument);
    }

    @Override
    public String toString() {
        return "(" + firstArgument.toString() + " = " + secondArgument.toString() + ")";
    }
}
