class And extends AbstractBinaryOperation implements Expression {

    @Override
    public String toString() {
        return "(" + firstArgument.toString() + ")&(" + secondArgument.toString() + ")";
    }

    And(Expression firstArgument, Expression secondArgument) {
        super(firstArgument, secondArgument);
    }
}
