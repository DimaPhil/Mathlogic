package Hometask7.hierarchy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Predicate implements Expression {

    public String predicateName;
    public ArrayList<Expression> terms;

    public Predicate(String predicateName, ArrayList<Expression> terms) {
        this.predicateName = predicateName;
        this.terms = terms;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder(predicateName);
        if (!terms.isEmpty()) {
            result.append("(");
            result.append(terms.get(0).toString());
            for (int i = 1; i < terms.size(); i++) {
                result.append(", ");
                String termRepresentation = terms.get(i).toString();
                result.append(termRepresentation);
            }
            result.append(")");
        }
        return result.toString();
    }

    @Override
    public boolean isBaseFor(Expression derived, HashMap<Expression, Expression> checker) {
        if (!derived.getClass().equals(this.getClass())) return false;
        Predicate other = (Predicate) derived;
        if (!predicateName.equals(other.predicateName)) {
            return false;
        }
        for (int i = 0; i < terms.size(); i++) {
            if (!terms.get(i).isBaseFor(other.terms.get(i), checker)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Expression substitute(HashMap<Variable, Expression> changes, HashMap<Expression, Integer> counter) {
        ArrayList<Expression> newTerms = new ArrayList<>();
        for (Expression term : terms) {
            newTerms.add(term.substitute(changes, counter));
        }
        return new Predicate(predicateName, newTerms);
    }

    @Override
    public void getFreeVariables(Map<Expression, Integer> counter, Set<Expression> accumulator) {
        for (Expression term : terms) {
            term.getFreeVariables(counter, accumulator);
        }
    }

    @Override
    public boolean isTermFreeToSubstituteInsteadOf(Set<Expression> freeVars, Expression insteadVariable, Map<Expression, Integer> counter) {
        for (Expression term : terms) {
            if (!term.isTermFreeToSubstituteInsteadOf(freeVars, insteadVariable, counter)) {
                return false;
            }
        }
        return true;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!o.getClass().equals(this.getClass())) return false;
        Predicate predicate = (Predicate) o;
        return predicateName.equals(predicate.predicateName) && terms.equals(predicate.terms);
    }

    @Override
    public int hashCode() {
        int result = predicateName.hashCode();
        result = 31 * result + terms.hashCode();
        return result;
    }
}
