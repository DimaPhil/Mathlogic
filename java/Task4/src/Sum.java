import java.util.HashMap;

class Sum extends AbstractBinaryOperation implements Expression {

    Sum(Expression firstArgument, Expression secondArgument) {
        super(firstArgument, secondArgument);
    }

    @Override
    public String toString() {
        return "(" + firstArgument.toString() + " + " + secondArgument.toString() + ")";
    }
}
