package Hometask7.utils;

import Hometask7.hierarchy.Equals;
import Hometask7.hierarchy.Expression;
import Hometask7.Proof;

import static Hometask7.Proof.*;

import static Hometask7.utils.ExprUtils.var;

public class Equality {

    //s, t, u -> |- (s = t -> s = u -> t = u)
    private static Proof faxm2Gen(Expression s, Expression t, Expression u) {
        return generateWithTerm(var("c"), u, generateWithTerm(var("b"), t, generateWithTerm(var("a"), s, faxm2)));
    }

    //u -> |- (s = t) -> |- (s = u -> t = u)
    public static Proof faxm2ConvertOnce(Expression u, Proof p) {
        Equals eq = (Equals) p.lastExpr();
        Expression s = eq.firstArgument;
        Expression t = eq.secondArgument;
        return mpBack(faxm2Gen(s, t, u), p);
    }

    //|- (s = t) -> |- (s = u) -> |- (t = u)
    public static Proof faxm2ConvertTwice(Proof p, Proof q) {
        Expression u = ((Equals) q.lastExpr()).secondArgument;
        return mpBack(faxm2ConvertOnce(u, p), q);
    }

    //s -> !s' = 0
    public static Proof succIsNotZero(Expression s) {
        return generateWithTerm(var("a"), s, faxm4);
    }

    //t -> |- (t = t)
    private static Proof equalityRefl(Expression t) {
        return generateWithTerm(var("a"), t, faxm2ConvertTwice(faxm6, faxm6));
    }

    //s, t -> |- (s = t -> t = s)
    public static Proof equalitySym(Expression s, Expression t) {
        Proof am2 = faxm2Gen(s, t, s); //(s = t) -> (s = s) -> (t = s)
        Proof swapHypo1 = swapHypotesisImpl(am2); //(s = s) -> (s = t) -> (t = s)
        Proof eqRefl = equalityRefl(s); //(s = s)
        return mp(eqRefl, swapHypo1);
    }

    //⊢(s = t) -> |- (t = s)
    public static Proof equalitySymConvert(Proof p) {
        Expression s = ((Equals) p.lastExpr()).firstArgument;
        return faxm2ConvertTwice(p, equalityRefl(s));
    }

    //|- (a = b) -> |- (c = a) -> |- (c = b)
    public static Proof equalityShuffle(Proof p, Proof q) {
        return faxm2ConvertTwice(equalitySymConvert(q), p);
    }

    //|- (a = b) -> |- (a = c) -> |- (b = c)
    public static Proof equalityRightConvert(Proof p, Proof q) {
        return faxm2ConvertTwice(p, q);
    }
}
