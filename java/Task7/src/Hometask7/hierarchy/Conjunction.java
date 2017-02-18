package Hometask7.hierarchy;

public class Conjunction extends BinaryOperator implements Expression {

    @Override
    public String toString() {
        return "(" + firstArgument.toString() + ")&(" + secondArgument.toString() + ")";
    }

    public Conjunction(Expression firstArgument, Expression secondArgument) {
        super(firstArgument, secondArgument);
    }

}
