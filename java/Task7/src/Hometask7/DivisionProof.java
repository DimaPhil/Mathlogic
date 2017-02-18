package Hometask7;

import Hometask7.hierarchy.*;
import Hometask7.utils.Reconstructor;

import java.util.function.Function;

import static Hometask7.utils.Equality.*;
import static Hometask7.utils.ExprUtils.*;

class DivisionProof extends Proof {

    //s, t -> |- (s = t -> s' = t')
    private Proof faxm1Gen(Expression s, Expression t) {
        return generateWithTerm(var("b"), t, generateWithTerm(var("a"), s, faxm1));
    }

    /**
     s, t -> |- (s' = t') -> s = t
     */
    private Proof faxm3Gen(Expression s, Expression t) {
        return generateWithTerm(var("b"), t, generateWithTerm(var("a"), s, faxm3));
    }

    //s, t -> |- (s + t' = (s + t)')
    private Proof faxm5Gen(Expression s, Expression t) {
        return generateWithTerm(var("b"), t, generateWithTerm(var("a"), s, faxm5));
    }

    //t -> |- (t + 0 = t)
    private Proof faxm6Gen(Expression t) {
        return generateWithTerm(var("a"), t, faxm6);
    }

    //t -> |- (t * 0 = 0)
    private Proof faxm7Gen(Expression t) {
        return generateWithTerm(var("a"), t, faxm7);
    }

    //s, t -> |- (s * t' = s * t + s)
    private Proof faxm8Gen(Expression s, Expression t) {
        return generateWithTerm(var("b"), t, generateWithTerm(var("a"), s, faxm8));
    }

    //|- (s = t) -> |- (s' = t')
    private Proof faxm1Convert(Proof p) {
        Equals eq = (Equals) p.lastExpr();
        Expression s = eq.firstArgument;
        Expression t = eq.secondArgument;
        return mpBack(faxm1Gen(s, t), p);
    }

    //|- (a -> b) -> |- (a -> b -> c) -> |- (a -> c)
    private Proof faxm2Convert(Proof p, Proof q) {
        Expression a = ((Implication) p.lastExpr()).statement;
        Expression b = ((Implication) p.lastExpr()).consequence;
        Expression c = ((Implication) ((Implication) q.lastExpr()).consequence).consequence;
        return mpTwice(axm2(a, b, c), p, q);
    }

    //|- (s' = t') -> |- (s = t)
    private Proof faxm3Convert(Proof p) {
        Expression s = ((Quote) ((Equals) p.lastExpr()).firstArgument).incremented;
        Expression t = ((Quote) ((Equals) p.lastExpr()).secondArgument).incremented;
        return mpBack(faxm3Gen(s, t), p);
    }

    //n -> |- (a = b) -> |- (a + [n]) = (b+[n])
    private Proof addNRightEquality(int n, Proof p) {
        Equals eq = (Equals) p.lastExpr();
        Expression a = eq.firstArgument;
        Expression b = eq.secondArgument;
        if (n == 0) {
            Proof equalitySymConvert = equalitySymConvert(faxm6Gen(b));
            Proof equalitySymConvert1 = equalitySymConvert(faxm6Gen(a));
            Proof equalityRightConvert = equalityRightConvert(equalitySymConvert1, p);
            return equalityShuffle(equalitySymConvert, equalityRightConvert);
        }
        n = n - 1;
        Proof sucResp1 = faxm1Convert(addNRightEquality(n, p));
        Proof eqSubL = equalityRightConvert(equalitySymConvert(faxm5Gen(a, intToLit(n))), sucResp1);
        return equalityShuffle(equalitySymConvert(faxm5Gen(b, intToLit(n))), eqSubL);
    }

    //"exists x . a" -> |- a[x := t] -> |- exists x . a
    private Proof introduceExists(Expression exa, Proof p) {
        Exists statement = (Exists) exa;
        Variable x = (Variable) statement.variable;
        Expression a = statement.statement;
        Expression al = p.lastExpr();
        Proof result = new Proof();
        result.proof.addAll(p.proof);
        result.proof.add(arrow(al, exists(x, a)));
        result.proof.add(exists(x, a));
        return result;
    }

    //x -> |- (!a) -> |- (!exists x . a)
    private Proof introduceNotExists(Variable x, Proof p) {
        Expression a = ((Negate) p.lastExpr()).negative;
        Expression e = exists(x, a); //∃x.a
        Proof impossible = impossibility(a, e); //a -> ¬a -> ¬∃x.a
        Proof mpf = mpBack(swapHypotesisImpl(impossible), p); //a -> ¬∃x.a
        Proof rex = ruleExists(x, mpf); //∃x.a -> ¬∃x.a
        return faxm9Convert(atoAImpl(e), rex); //¬∃x.a
    }

    //s, t -> "exists z . s * z = t"
    private Expression divisionRelation(Expression s, Expression t) {
        return exists(var("z"), equal(multi(s, var("z")), t));
    }

    //n, m -> exists z([n] * z = [m])
    Proof divides(int n, int m) {
        return introduceExists(divisionRelation(intToLit(n), intToLit(m)), mulDistr(n, m / n));
    }

    //n, m -> [n] + [m] = [n + m]
    private Proof sumDistr(int n, int m) {
        if (m == 0) {
            return faxm6Gen(intToLit(n));
        }
        m = m - 1;
        Proof sucResp1 = faxm1Convert(sumDistr(n, m));
        return equalityRightConvert(equalitySymConvert(faxm5Gen(intToLit(n), intToLit(m))), sucResp1);
    }

    //n, m -> |- ([n] * [m] = [n * m])
    private Proof mulDistr(int n, int m) {
        if (m == 0) {
            return faxm7Gen(intToLit(n));
        }
        m--;
        Proof previous = mulDistr(n, m);
        Proof plusN = addNRightEquality(n, previous);
        Proof faxm8Gen = faxm8Gen(intToLit(n), intToLit(m));
        Proof equalitySymConvert = equalitySymConvert(faxm8Gen);
        Proof eqSubL = equalityRightConvert(equalitySymConvert, plusN);
        return equalityShuffle(sumDistr(n * m, n), eqSubL);
    }

    //a -> |- (a -> a)
    private Proof atoAImpl(Expression a) {
        Proof where = axm2(a, arrow(a, a), a); //a->(a->a) -> (a->(a->a)->a) -> (a -> a)
        Proof one = axm1(a, a); //a->a->a
        Proof two = axm1(a, arrow(a, a)); //a->(a->a)->A
        return mpTwice(where, one, two);
    }

    //b -> |- (a) -> |- (b -> a)
    private Proof implLeftWeakness(Expression b, Proof p) {
        Proof axm1 = axm1(p.lastExpr(), b); //a -> b -> a
        return mpBack(axm1, p); //b -> a
    }

    //⊢(a -> b) -> |- (b -> c) -> |- (a -> c)
    private Proof implTrans(Proof p, Proof q) {
        Expression a = ((Implication) p.lastExpr()).statement;
        return faxm2Convert(p, implLeftWeakness(a, q));
    }

    //a, b -> |- (a -> b) -> (!b -> !a)
    private Proof contra(Expression a, Expression b) {
        return new Proof(Reconstructor.reconstructProof(Reconstructor.contraProof, a, b, null));
    }

    // |- (a -> b) -> |- (!b -> !a)
    Proof contraConvert(Proof p) {
        Expression a = ((Implication) p.lastExpr()).statement;
        Expression b = ((Implication) p.lastExpr()).consequence;
        return mpBack(contra(a, b), p);
    }

    //a, b -> |- (a -> !a -> !b)
    private Proof impossibility(Expression a, Expression b) {
        Proof axm1 = axm1(a, b); //a -> b -> a
        Proof contra = contra(b, a); //(b -> a) -> (!a -> !b)
        return implTrans(axm1, contra);//a -> !a -> !b
    }

    //|- (a -> b) -> |- (a -> !b) -> |- (!a)
    private Proof faxm9Convert(Proof p, Proof q) {
        Expression a = ((Implication) p.lastExpr()).statement;
        Expression b = ((Implication) p.lastExpr()).consequence;
        return mpTwice(axm9(a, b), p, q);
    }

    //|- (a -> b) -> |- (!b) -> |- (!a)
    private Proof contraTwice(Proof ab, Proof nb) {
        Expression a = ((Implication) ab.lastExpr()).statement;
        Proof weakness = implLeftWeakness(a, nb); //|-(a->!b)
        return faxm9Convert(ab, weakness); //|- !a
    }

    //a -> |- (!b) -> (|- (a) -> |- (b)) -> |- (!a)
    private Proof subNot(Expression a, Proof nb, java.util.function.Function<Proof, Proof> ab) {
        Proof hypoAssume = hypotesisAssume(a, ab); //|- (a -> b)
        return contraTwice(hypoAssume, nb); //|- !a
    }

    //s, t, n -> |- (s + [n] = t + [n] -> |- (s = t))
    private Proof sumEraseN(Expression s, Expression t, int n, Proof p) {
        if (n == 0) {
            return equalityShuffle(faxm6Gen(t), equalityRightConvert(faxm6Gen(s), p));
        }
        n = n - 1;
        Proof eqSubL = equalityRightConvert(faxm5Gen(s, intToLit(n)), p);
        Proof eqSubR = equalityShuffle(faxm5Gen(t, intToLit(n)), eqSubL);
        return sumEraseN(s, t, n, faxm3Convert(eqSubR));
    }

    //a, b -> |- (!a * 0 = b')
    private Proof notMulZero(Expression a, Expression b) {
        Proof faxm7Gen = faxm7Gen(a);
        Proof axm2convertOnce = faxm2ConvertOnce(succ(b), faxm7Gen);
        Proof equalitySym = equalitySym(zero(), succ(b));
        Proof imTrans2 = implTrans(axm2convertOnce, equalitySym);
        Proof succNotZero = succIsNotZero(b);
        return contraTwice(imTrans2, succNotZero);
    }

    /**
     a, n, m (n > m) -> |- (!(a+[n]=[m]))
     */
    private Proof notGreaterLemma(Expression a, int n1, int m1) {
        if (m1 == 0) {
            int n = n1 - 1;
            final int finalN = n;
            Function<Proof, Proof> stepFunction = p -> {
                Proof faxm5Gen = faxm5Gen(a, intToLit(finalN));
                return equalityRightConvert(faxm5Gen, p);
            };
            Expression equal = equal(plus(a, intToLit(n1)), zero());
            Proof succNotZero = succIsNotZero(plus(a, intToLit(n)));
            return subNot(equal, succNotZero, stepFunction);
        }
        int n = n1 - 1;
        int m = m1 - 1;
        final int finalN = n;
        Function<Proof, Proof> stepFunction = p -> {
            Expression toLit = intToLit(finalN);
            Proof faxm5gen = faxm5Gen(a, toLit);
            Proof faxm2ConvertTwice = faxm2ConvertTwice(faxm5gen, p);
            return faxm3Convert(faxm2ConvertTwice);
        };
        Expression equal = equal(plus(a, intToLit(n1)), intToLit(m1));
        Proof notGreaterLemma = notGreaterLemma(a, n, m);
        return subNot(equal, notGreaterLemma, stepFunction);
    }

    //n, m, z, (m % n != 0, 1 <= n <= m) -> |- (![n] * z' = [m])
    private Proof notDividesStepLessEq(int n, int m, Expression z) {
        Expression a = intToLit(n); //[n]
        Expression b = intToLit(m); //[m]
        Expression d = intToLit(m - n); //[m-n]
        java.util.function.Function<Proof, Proof> stepFunction = p -> {
            Proof faxm8Gen = faxm8Gen(a, z);
            Proof am22 = faxm2ConvertTwice(faxm8Gen, p);
            Proof equalitySymConvert = equalitySymConvert(sumDistr(m - n, n));
            Proof eqSubR = equalityShuffle(equalitySymConvert, am22);
            return sumEraseN(multi(a, z), d, n, eqSubR);
        };
        return subNot(equal(multi(a, succ(z)), b), notDivides(n, m - n, z), stepFunction);
    }

    //n, m, z (m % n != 0, n > m) -> |- !([n] * z' = m)
    private Proof notDividesStepGreater(int n, int m, Expression z) {
        Expression a = intToLit(n); //[n]
        Expression b = intToLit(m); //[m]
        java.util.function.Function<Proof, Proof> stepFunction = p -> faxm2ConvertTwice(faxm8Gen(a, z), p);
        Expression equal = equal(multi(a, succ(z)), b); //a * z' = b
        Proof notGreaterLemma = notGreaterLemma(multi(a, z), n, m); //!(a*z+[n]=[m])
        return subNot(equal, notGreaterLemma, stepFunction); //(a*z'=b) -> (!a*z+[n]=[m]) -> ... -> !([n]*z'=m)
    }

    /**
     n, m, z (m % n != 0) -> |- !([n] * z' = [m])
     */
    private Proof notDividesInductionStep(int n, int m, Expression z) {
        if (n <= m) {
            return notDividesStepLessEq(n, m, z);
        } else {
            return notDividesStepGreater(n, m, z);
        }
    }

    //a -> |- (0 * a = 0)
    private Proof mulLeftZero(Expression a) {
        Variable x = var("x");
        Proof equalitySymConvert = equalitySymConvert(faxm8Gen(zero(), x));
        Proof faxm6gen = faxm6Gen(multi(zero(), x));
        Proof axm2convert = faxm2Convert(equalitySymConvert, faxm6gen);
        Proof axm2convertOnce = faxm2ConvertOnce(zero(), equalitySymConvert(axm2convert));
        return inductConclusion(x, a, faxm7Gen(zero()), axm2convertOnce);
    }

    //s, t -> |- (!0 * s = t')
    private Proof notDividesBase(Expression s, Expression t) {
        Proof axm2convertOnce = faxm2ConvertOnce(succ(t), mulLeftZero(s));
        Proof equalitySym = equalitySym(zero(), succ(t));
        Proof imTrans2 = implTrans(axm2convertOnce, equalitySym);
        Proof succNotZero = succIsNotZero(t);
        return contraTwice(imTrans2, succNotZero);
    }

    /**
     n, m, z (m % n != 0) -> |- (![n] * z = [m])
     */
    private Proof notDivides(int n, int m, Expression expression) {
        Expression litMPrev = intToLit(m - 1); //[m - 1]
        if (n == 0) {
            return notDividesBase(expression, litMPrev); //|- 0 * z = [m - 1]
        }
        Variable x = var("x");
        Expression litN = intToLit(n); //[n]
        Proof noDivi0 = notMulZero(litN, litMPrev); //![n] * 0 = [m]
        Expression litM = intToLit(m); //[m]
        Expression not = not(equal(multi(litN, x), litM)); //![n] * x = [m]
        Proof nextStep = notDividesInductionStep(n, m, x); //![n] * x' = [m]
        Proof imWeak = implLeftWeakness(not, nextStep); //![n] * x = [m] -> ![n] * x' = [m]
        return inductConclusion(x, expression, noDivi0, imWeak); //![n] * 0 = [m] & @z(![n] * x = [m] -> ![n] * x' = [m]) -> !([n] * x = [m])
    }

    /**
     n, m -> |- (!exists z . ([n] * z = [m])
     */
    Proof notDivides(int n, int m) {
        return introduceNotExists(var("z"), notDivides(n, m, var("z")));
    }

}
