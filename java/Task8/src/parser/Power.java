package parser;

public class Power extends BinaryOperator implements Expression {

    Power(Expression firstArgument, Expression secondArgument) {
        super(firstArgument, secondArgument);
    }

    @Override
    public String toString() {
        return "(" + firstArgument.toString() + " ^ " + secondArgument.toString() + ")";
    }
}
