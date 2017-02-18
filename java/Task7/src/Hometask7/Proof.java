package Hometask7;

import Hometask7.exceptions.IllegalOperationException;
import Hometask7.exceptions.ParserException;
import Hometask7.hierarchy.Expression;
import Hometask7.hierarchy.ForAll;
import Hometask7.hierarchy.Implication;
import Hometask7.hierarchy.Variable;
import Hometask7.parser.ExpressionParser;
import Hometask7.utils.Reconstructor;

import static Hometask7.utils.ExprUtils.*;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Proof {
    List<Expression> proof;
    static Proof faxm1;
    public static Proof faxm2;
    static Proof faxm3;
    public static Proof faxm4;
    static Proof faxm5;
    public static Proof faxm6;
    static Proof faxm7;
    static Proof faxm8;

    static {
        ExpressionParser parser = new ExpressionParser();
        try {
            faxm1 = axm(parser.parse("a=b->a'=b'"));
            faxm2 = axm(parser.parse("a=b->a=c->b=c"));
            faxm3 = axm(parser.parse("a'=b'->a=b"));
            faxm4 = axm(parser.parse("!a'=0"));
            faxm5 = axm(parser.parse("a+b'=(a+b)'"));
            faxm6 = axm(parser.parse("a+0=a"));
            faxm7 = axm(parser.parse("a*0=0"));
            faxm8 = axm(parser.parse("a*b'=a*b+a"));
        } catch (ParserException e) {
            e.printStackTrace();
        }
    }

    static {
        ExpressionParser parser = new ExpressionParser();
        List<Expression> swapHypoProofTemplate = new ArrayList<>();
        try (Scanner swapHypoScanner = new Scanner(new File("ProofTexts/swapHypoProof.txt"))) {
            while (swapHypoScanner.hasNextLine()) {
                swapHypoProofTemplate.add(parser.parseAsPlaceHolder(swapHypoScanner.nextLine()));
            }
        } catch (ParserException | IOException e) {
            e.printStackTrace();
        }
    }

    public Expression lastExpr() {
        return proof.get(proof.size() - 1);
    }

    Proof() {
        proof = new ArrayList<>();
    }

    Proof(List<Expression> proof) {
        this.proof = new ArrayList<>(proof);
    }

    private Proof(Expression single) {
        proof = new ArrayList<>(Collections.singletonList(single));
    }

    private static Proof axm(Expression single) {
        return new Proof(single);
    }

    //a -> b -> a
    static Proof axm1(Expression a, Expression b) {
        return axm(arrow(a, arrow(b, a)));
    }

    //(a -> b) -> (a -> b -> c) -> (a -> c)
    static Proof axm2(Expression a, Expression b, Expression c) {
        return axm(arrow(arrow(a, b), arrow(arrow(a, arrow(b, c)), arrow(a, c))));
    }

    //a -> b -> a & b
    private static Proof axm3(Expression a, Expression b) {
        return axm(arrow(a, arrow(b, and(a, b))));
    }

    //a & b -> a
    public static Proof axm4(Expression a, Expression b) {
        return axm(arrow(and(a, b), a));
    }

    //a & b -> b
    public static Proof axm5(Expression a, Expression b) {
        return axm(arrow(and(a, b), b));
    }

    //a -> a | b
    public static Proof axm6(Expression a, Expression b) {
        return axm(arrow(a, or(a, b)));
    }

    //b -> a | b
    public static Proof axm7(Expression a, Expression b) {
        return axm(arrow(b, or(a, b)));
    }

    //(a -> c) -> (b -> c) -> (a | b -> c)
    public static Proof axm8(Expression a, Expression b, Expression c) {
        return axm(arrow(arrow(a, c), arrow(arrow(b, c), arrow(or(a, b), c))));
    }

    //(a -> b) -> (a -> !b) -> !a
    static Proof axm9(Expression a, Expression b) {
        return axm(arrow(arrow(a, b), arrow(arrow(a, not(b)), not(a))));
    }

    //!!a -> a
    public static Proof axm10(Expression a) {
        return axm(arrow(not(not(a)), a));
    }

    //forall x . a -> a[x := t]
    private static Proof axm11(Expression a, Variable x, Expression t) {
        return axm(arrow(forAll(x, a), substitute(a, x, t)));
    }

    //a[x := t] -> exists x . a
    public static Proof axm12(Expression a, Variable x, Expression t) {
        return axm(arrow(substitute(a, x, t), exists(x, a)));
    }

    //x -> |- (a -> b) -> |- (a -> forall x . b)
    private static Proof ruleAll(Variable x, Proof p) {
        Implication impl = (Implication) p.lastExpr();
        Expression a = impl.statement;
        Expression b = impl.consequence;
        Proof result = new Proof();
        result.proof.addAll(p.proof);
        result.proof.add(arrow(a, forAll(x, b)));
        return result;
    }

    //x -> |- (a -> b) -> |- (exists x . a -> b)
    static Proof ruleExists(Variable x, Proof p) {
        Implication impl = (Implication) p.lastExpr();
        Expression a = impl.statement;
        Expression b = impl.consequence;
        Proof result = new Proof();
        result.proof.addAll(p.proof);
        result.proof.add(arrow(exists(x, a), b));
        return result;
    }

    //|- (a) -> |- (a -> b) -> |- (b)
    public static Proof mp(Proof p, Proof q) {
        Expression a = p.lastExpr();
        Implication aib = (Implication) q.lastExpr();
        if (!a.equals(aib.statement)) {
            throw new IllegalOperationException("wrong Modus ponens A != A': A = " + a + " A' = " + aib.statement);
        }
        Proof result = new Proof();
        result.proof.addAll(p.proof);
        result.proof.addAll(q.proof);
        result.proof.add(aib.consequence);
        return result;
    }

    //|- (a -> b) -> |- a -> |- b
    public static Proof mpBack(Proof where, Proof what) {
        return mp(what, where);
    }

    //|- (a -> b -> c) -> |- (a) -> |- (b) -> |- (c)
    static Proof mpTwice(Proof where, Proof one, Proof two) {
        return mpBack(mpBack(where, one), two);
    }

    //x -> |- a -> |- (forall x . a)
    private static Proof generalize(Variable x, Proof p) {
        Expression a = p.lastExpr();
        Proof theTruth = axm1(predicate("A"), predicate("A")); //A -> A -> A
        Proof mp = mp(p, axm1(a, theTruth.lastExpr())); // a -> (A->A->A) -> a, MP a := (A->A->A) -> a
        return mp(theTruth, ruleAll(x, mp)); //(A->A->A) -> ∀x.a, MP (A->A->A) := ∀x.a
    }

    //t -> |- (forall x . a) -> |- (a[x := t])
    private static Proof ungeneralize(Expression t, Proof p) {
        ForAll all = (ForAll) p.lastExpr();
        Variable x = (Variable) all.variable;
        Expression a = all.statement;
        return mpBack(axm11(a, x, t), p);  //forall x . a -> a[x := t], M.P. forall x . a := a[x := t]
    }

    //x -> t -> |- a -> |- a[x := t]
    public static Proof generateWithTerm(Variable x, Expression t, Proof p) {
        return ungeneralize(t, generalize(x, p));
    }

    //|- (a -> b -> c) -> |- (b -> a -> c)
    public static Proof swapHypotesisImpl(Proof p) {
        Implication lastOne = (Implication) p.lastExpr();
        Implication rightPart = (Implication) lastOne.consequence;
        Expression a = lastOne.statement;
        Expression b = rightPart.statement;
        Expression c = rightPart.consequence;
        Proof swapped = new Proof(Reconstructor.reconstructProof(Reconstructor.swapHypoProof, a, b, c)); //(a->b->c) -> (b->a->c)
        Proof result = new Proof();
        result.proof.addAll(swapped.proof); //(a -> b -> c) -> (b -> a -> c)
        result.proof.addAll(p.proof); //a -> b -> c
        result.proof.add(arrow(b, arrow(a, c))); //b -> a -> c
        return result;
    }

    //a -> (|- a -> |- b) -> |- (a -> b)
    static Proof hypotesisAssume(Expression a, java.util.function.Function<Proof, Proof> f) {
        return deduction(a, f.apply(axm(a)));
    }

    //Г, g |- e -> Г |- (g -> e)
    private static Proof deduction(Expression g, Proof p) {
        return new Proof(Reconstructor.deduction(p.proof, Collections.emptyList(), g));
    }

    //x, a -> |- (a[x := 0] & ∀x(a -> a[x := x']) -> a )
    private static Proof axiomInduction(Variable x, Expression a) {
        Expression ax0 = substitute(a, x, zero());
        Expression axNext = substitute(a, x, succ(x));
        return axm(arrow(and(ax0, forAll(x, arrow(a, axNext))), a));
    }

    //x, t -> |- (e[x := 0]) -> |- (e(x) -> e(x')) -> |- e(t)
    static Proof inductConclusion(Variable x, Expression t, Proof p, Proof q) {
        Expression e0 = p.lastExpr(); //e[x := 0]
        Expression e = ((Implication) q.lastExpr()).statement; //e(x) -> e(x')
        Proof q1 = generalize(x, q);
        Expression i = q1.lastExpr(); //forall x . (e(x)->e(x'))
        Proof axm3 = axm3(e0, i); //e0 -> forall x . (e(x) -> e(x')) -> e0 & forall x . (e(x) -> e(x'))
        Proof mp2 = mpTwice(axm3, p, q1); // e0 & forall x . (e(x) -> e(x'))
        Proof induction = axiomInduction(x, e); //e0 & forall x . (e(x) -> e(x')) -> e
        Proof mpReverse = mpBack(induction, mp2); //e
        return generateWithTerm(x, t, mpReverse); //e[t]
    }
}