import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public interface Expression {
    boolean evaluate(Map<Symbol, Boolean> estimations);
    boolean isBaseFor(Expression derived, Map<Expression, Expression> checker);
    default boolean isBaseFor(Expression derived) {
        return isBaseFor(derived, new HashMap<>());
    }
    Set<Symbol> getPropositionalVariables();
    Expression substitute(Map<Symbol, Expression> changes);
}