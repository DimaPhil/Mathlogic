import java.util.*;

class Function implements Expression {

    private String functionName;
    private ArrayList<Expression> terms;

    Function(String functionName, ArrayList<Expression> terms) {
        this.functionName = functionName;
        this.terms = terms;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder(functionName);
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
    public boolean isBaseFor(Expression derived, Map<Expression, Expression> checker) {
        if (!derived.getClass().equals(this.getClass())) return false;
        Function other = (Function) derived;
        if (!functionName.equals(other.functionName)) {
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
    public Expression substitute(Map<Variable, Expression> changes, Map<Expression, Integer> counter) {
        ArrayList<Expression> newTerms = new ArrayList<>();
        for (Expression term : terms) {
            newTerms.add(term.substitute(changes, counter));
        }
        return new Function(functionName, newTerms);
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
        Function function = (Function) o;
        return functionName.equals(function.functionName) && terms.equals(function.terms);
    }

    @Override
    public int hashCode() {
        int result = functionName.hashCode();
        result = 31 * result + terms.hashCode();
        return result;
    }
}
