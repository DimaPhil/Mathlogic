package parser;

public abstract class BinaryOperator implements Expression {
    public Expression firstArgument, secondArgument;

    BinaryOperator(Expression firstArgument, Expression secondArgument) {
        this.firstArgument = firstArgument;
        this.secondArgument = secondArgument;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!o.getClass().equals(this.getClass())) return false;
        BinaryOperator that = (BinaryOperator) o;
        return firstArgument.equals(that.firstArgument) && secondArgument.equals(that.secondArgument);
    }

    @Override
    public int hashCode() {
        int result = firstArgument.hashCode();
        result = 31 * result + secondArgument.hashCode();
        return result;
    }
}