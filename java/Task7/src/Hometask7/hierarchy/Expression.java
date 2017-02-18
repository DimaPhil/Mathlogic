package Hometask7.hierarchy;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public interface Expression {
    boolean isBaseFor(Expression derived, HashMap<Expression, Expression> checker);

    default boolean isBaseFor(Expression derived) {
        return isBaseFor(derived, new HashMap<>());
    }

    Expression substitute(HashMap<Variable, Expression> changes, HashMap<Expression, Integer> counter);

    default Expression substitute(HashMap<Variable, Expression> changes) {
        return substitute(changes, new HashMap<>());
    }

    void getFreeVariables(Map<Expression, Integer> counter, Set<Expression> accumulator);

    default Set<Expression> getFreeVariables() {
        Set<Expression> accumulator = new HashSet<>();
        getFreeVariables(new HashMap<>(), accumulator);
        return accumulator;
    }

    boolean isTermFreeToSubstituteInsteadOf(Set<Expression> freeVars, Expression insteadVariable, Map<Expression, Integer> counter);

    default boolean isTermFreeToSubstituteInsteadOf(Expression term, Expression insteadVariable) {
        return isTermFreeToSubstituteInsteadOf(term.getFreeVariables(), insteadVariable, new HashMap<>());
    }
}
