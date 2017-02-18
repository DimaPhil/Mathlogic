import java.util.HashMap;

public interface Expression {
    boolean isBaseFor(Expression derived, HashMap<Expression, Expression> checker);
    default boolean isBaseFor(Expression derived) {
        return isBaseFor(derived, new HashMap<>());
    }
    default Expression modusPonens(Expression carrier) {
        return null;
    }
}
