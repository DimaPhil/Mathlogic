import java.util.HashMap;

class Conjunction extends AbstractBinaryOperation implements Expression {

    @Override
    public String toString() {
        return "(" + firstArgument.toString() + ")&(" + secondArgument.toString() + ")";
    }

    Conjunction(Expression firstArgument, Expression secondArgument) {
        super(firstArgument, secondArgument);
    }

}
