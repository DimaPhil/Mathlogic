package Hometask7.hierarchy;

public class Disjunction extends BinaryOperator implements Expression {

    public Disjunction(Expression firstArgument, Expression secondArgument) {
        super(firstArgument, secondArgument);
    }

    @Override
    public String toString() {
        return "(" + firstArgument.toString() + ")|(" + secondArgument.toString() + ")";
    }

}
