class Disjunction extends AbstractBinaryOperation implements Expression {

    Disjunction(Expression firstArgument, Expression secondArgument) {
        super(firstArgument, secondArgument);
    }

    @Override
    public String toString() {
        return "(" + firstArgument.toString() + ")|(" + secondArgument.toString() + ")";
    }

}
