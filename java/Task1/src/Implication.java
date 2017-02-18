import java.util.HashMap;

/**
 * Created by Сергей on 15.04.14.
 */
public class Implication implements Expression {
    private Expression statement;
    private Expression consequence;

    @Override
    public boolean isBaseFor(Expression derived, HashMap<Expression, Expression> checker) {
        if (!derived.getClass().equals(this.getClass())) return false;
        Implication other = (Implication) derived;
        boolean checkStatement = statement.isBaseFor(other.statement, checker);
        boolean checkConsequence = consequence.isBaseFor(other.consequence, checker);
        return checkStatement && checkConsequence;
    }

    @Override
    public String toString() {
        return "(" + statement.toString() + ") ---> (" + consequence.toString() + ")";
    }

    Implication(Expression statement, Expression consequence) {
        this.statement = statement;
        this.consequence = consequence;
    }

    @Override
    public Expression modusPonens(Expression carrier) {
        if (!statement.equals(carrier)) return null;
        return consequence;
    }

    @Override
    public int hashCode() {
        return statement.hashCode() + consequence.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!obj.getClass().equals(this.getClass())) return false;
        Implication other = ((Implication) obj);
        return statement.equals(other.statement) && consequence.equals(other.consequence);
    }
}
