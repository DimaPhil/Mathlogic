package Hometask7.hierarchy;

public class Sum extends BinaryOperator implements Expression {

    public Sum(Expression firstArgument, Expression secondArgument) {
        super(firstArgument, secondArgument);
    }

    @Override
    public String toString() {
        return "(" + firstArgument.toString() + ")+(" + secondArgument.toString() + ")";
    }
}
