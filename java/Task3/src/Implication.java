import java.util.Map;
import java.util.Set;

class Implication implements Expression {
    Expression statement;
    Expression consequence;

    @Override
    public boolean evaluate(Map<Symbol, Boolean> estimations) {
        //A->B === !A|B
        return !statement.evaluate(estimations) || consequence.evaluate(estimations);
    }

    @Override
    public boolean isBaseFor(Expression derived, Map<Expression, Expression> checker) {
        if (!derived.getClass().equals(this.getClass())) return false;
        Implication other = (Implication) derived;
        boolean checkStatement = statement.isBaseFor(other.statement, checker);
        boolean checkConsequence = consequence.isBaseFor(other.consequence, checker);
        return checkStatement && checkConsequence;
    }

    @Override
    public String toString() {
        return "(" + statement.toString() + ")->(" + consequence.toString() + ")";
    }

    Implication(Expression statement, Expression consequence) {
        this.statement = statement;
        this.consequence = consequence;
    }

    @Override
    public Set<Symbol> getPropositionalVariables() {
        Set<Symbol> fromStatement = statement.getPropositionalVariables();
        Set<Symbol> fromConsequence = consequence.getPropositionalVariables();
        fromStatement.addAll(fromConsequence);
        return fromStatement;
    }

    @Override
    public Expression substitute(Map<Symbol, Expression> changes) {
        return new Implication(statement.substitute(changes), consequence.substitute(changes));
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
