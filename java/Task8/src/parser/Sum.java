package parser;

public class Sum extends BinaryOperator implements Expression {

    Sum(Expression firstArgument, Expression secondArgument) {
        super(firstArgument, secondArgument);
    }

    @Override
    public String toString() {
        return "(" + firstArgument.toString() + " + " + secondArgument.toString() + ")";
    }
}
