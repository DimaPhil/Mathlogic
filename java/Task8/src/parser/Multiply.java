package parser;

public class Multiply extends BinaryOperator implements Expression {

    Multiply(Expression firstArgument, Expression secondArgument) {
        super(firstArgument, secondArgument);
    }

    @Override
    public String toString() {
        return "(" + firstArgument.toString() + " * " + secondArgument.toString() + ")";
    }
}
