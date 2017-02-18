package Hometask7.hierarchy;

public class Equals extends BinaryOperator implements Expression {

    public Equals(Expression firstArgument, Expression secondArgument) {
        super(firstArgument, secondArgument);
    }

    @Override
    public String toString() {
        return "(" + firstArgument.toString() + ")=(" + secondArgument.toString() + ")";
    }
}
