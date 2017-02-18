package Hometask7.hierarchy;

public class Multiply extends BinaryOperator implements Expression {

    public Multiply(Expression firstArgument, Expression secondArgument) {
        super(firstArgument, secondArgument);
    }

    @Override
    public String toString() {
        return "(" + firstArgument.toString() + ")*(" + secondArgument.toString() + ")";
    }
}
