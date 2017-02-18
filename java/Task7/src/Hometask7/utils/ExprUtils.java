package Hometask7.utils;

import Hometask7.hierarchy.*;

import java.util.ArrayList;
import java.util.HashMap;

public class ExprUtils {
    public static Expression arrow(Expression left, Expression right) {
        return new Implication(left, right);
    }

    public static Expression and(Expression left, Expression right) {
        return new Conjunction(left, right);
    }

    public static Expression or(Expression left, Expression right) {
        return new Disjunction(left, right);
    }

    public static Expression not(Expression body) {
        return new Negate(body);
    }

    public static Expression forAll(Variable variable, Expression body) {
        return new ForAll(variable, body);
    }

    public static Expression exists(Variable variable, Expression body) {
        return new Exists(variable, body);
    }

    public static Expression equal(Expression left, Expression right) {
        return new Equals(left, right);
    }

    public static Expression multi(Expression left, Expression right) {
        return new Multiply(left, right);
    }

    public static Expression zero() {
        return new Zero();
    }

    public static Variable var(String name) {
        return new Variable(name);
    }

    public static Expression predicate(String name) {
        return new Predicate(name, new ArrayList<>());
    }

    public static Expression substitute(Expression a, Variable x, Expression t) {
        HashMap<Variable, Expression> substMap = new HashMap<>();
        substMap.put(x, t);
        return a.substitute(substMap);
    }

    public static Expression plus(Expression left, Expression right) {
        return new Sum(left, right);
    }

    public static Expression intToLit(int n) {
        Expression result = new Zero();
        for (int i = 0; i < n; i++) {
            result = new Quote(result);
        }
        return result;
    }

    public static Expression succ(Expression t) {
        return new Quote(t);
    }
}
