import exceptions.ParserException;
import parser.*;
import parser.Number;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.lang.*;
import java.math.BigInteger;
import java.util.Scanner;

public class Main {

    //operator "."
    private static Ordinal dot(Ordinal left, Ordinal right) {
        return new CList(left, right);
    }

    //operator pair: x -> y -> (x, y)
    private static Ordinal makePair(Ordinal left, Ordinal right) {
        return dot(left, right);
    }

    private static Ordinal first(Ordinal a) {
        if (isAtom(a)) {
            System.out.println("first on atom");
            return CList.NIL;
        }
        return ((CList) a).first;
    }

    private static Ordinal rest(Ordinal a) {
        if (isAtom(a)) {
            System.out.println("rest on atom");
            return CList.NIL;
        }
        return ((CList) a).second;
    }

    private static Ordinal firstN(Ordinal a, int n) {
        if (n == 0 || isAtom(a)) {
            if (isAtom(a)) {
                System.out.println("firstN on atom");
            }
            return CList.NIL;
        }
        return dot(first(a), firstN(rest(a), n - 1));
    }

    private static Ordinal restN(Ordinal a, int n) {
        if (n == 0 || isAtom(a)) {
            return a;
        }
        return restN(rest(a), n - 1);
    }

    //true iff a is not a list
    private static boolean isAtom(Ordinal a) {
        return !a.getClass().equals(CList.class) || a.getClass().equals(CList.class) && a.equals(CList.NIL);
    }

    private static int length(Ordinal a) {
        if (isAtom(a)) {
            return 0;
        }
        return 1 + length(rest(a));
    }

    private static int size(Ordinal a) {
        if (isAtom(a)) {
            return 1;
        }
        return size(fe(a)) + size(rest(a));
    }

    private static Ordinal append(Ordinal a, Ordinal b) {
        if (isAtom(a)) {
            return b;
        }
        return dot(first(a), append(rest(a), b));
    }

    //the first exponent of a
    private static Ordinal fe(Ordinal a) {
        if (isAtom(a)) {
            return Atom.ZERO;
        }
        return first(first(a));
    }

    //the first coefficient of a
    private static Ordinal fc(Ordinal a) {
        if (isAtom(a)) {
            return a;
        }
        return rest(first(a));
    }
    private enum CompareResult {
        LESS,
        GREATER,
        EQUALS

    }

    //ordering on naturals
    private static CompareResult cmpW(Atom p, Atom q) {
        BigInteger first = p.number;
        BigInteger second = q.number;
        if (first.compareTo(second) < 0) {
            return CompareResult.LESS;
        } else if (first.compareTo(second) == 0) {
            return CompareResult.EQUALS;
        } else {
            return CompareResult.GREATER;
        }
    }

    //ordering on ordinals
    private static CompareResult cmpO(Ordinal a, Ordinal b) {
        if (isAtom(a) && isAtom(b)) {
            return cmpW((Atom) a, (Atom) b);
        }
        if (isAtom(a)) {
            return CompareResult.LESS;
        }
        if (isAtom(b)) {
            return CompareResult.GREATER;
        }
        if (cmpO(fe(a), fe(b)) != CompareResult.EQUALS) {
            return cmpO(fe(a), fe(b));
        }
        if (cmpW((Atom) fc(a), (Atom) fc(b)) != CompareResult.EQUALS) {
            return cmpW((Atom) fc(a), (Atom) fc(b));
        }
        return cmpO(rest(a), rest(b));
    }

    //< relation on ordinals
    private static boolean lessO(Ordinal a, Ordinal b) {
        return cmpO(a, b) == CompareResult.LESS;
    }

    //naturals addition
    private static Atom addW(Atom a, Atom b) {
        BigInteger first = a.number;
        BigInteger second = b.number;
        return new Atom(first.add(second));
    }

    //ordinal addition
    private static Ordinal addO(Ordinal a, Ordinal b) {
        if (isAtom(a) && isAtom(b)) {
            return addW((Atom) a, (Atom) b);
        }
        if (cmpO(fe(a), fe(b)) == CompareResult.LESS) {
            return b;
        }
        if (cmpO(fe(a), fe(b)) == CompareResult.EQUALS) {
            return dot(makePair(fe(a), addW((Atom) fc(a), (Atom) fc(b))), rest(b));
        }
        return dot(makePair(fe(a), fc(a)), addO(rest(a), b));
    }

    //naturals addition
    private static Atom minusW(Atom a, Atom b) {
        BigInteger first = a.number;
        BigInteger second = b.number;
        return new Atom(first.subtract(second));
    }

    // < on naturals
    private static boolean lessW(Atom a, Atom b) {
        return cmpW(a, b) == CompareResult.LESS;
    }

    //ordinal subtraction
    private static Ordinal minusO(Ordinal a, Ordinal b) {
        if (isAtom(a) && isAtom(b) && lessW((Atom) a, (Atom) b)) {
            return Atom.ZERO;
        }
        if (isAtom(a) && isAtom(b)) {
            return minusW((Atom) a, (Atom) b);
        }
        if (cmpO(fe(a), fe(b)) == CompareResult.LESS) {
            return Atom.ZERO;
        }
        if (cmpO(fe(a), fe(b)) == CompareResult.GREATER) {
            return a;
        }
        if (lessW(fc(a), fc(b))) {
            return Atom.ZERO;
        }
        if (lessW(fc(b), fc(a))) {
            return dot(makePair(fe(a), minusW((Atom) fc(a), (Atom) fc(b))), rest(a));
        }
        return minusO(rest(a), rest(b));
    }

    //finds the index of the first exponent of a that is <= fe(b)
    private static int c(Ordinal a, Ordinal b) {
        if (lessO(fe(b), fe(a))) {
            return 1 + c(rest(a), b);
        }
        return 0;
    }

    //skips over the first n elements of a and then calls c
    private static int count(Ordinal a, Ordinal b, int n) {
        return n + c(restN(a, n), b);
    }

    //naturals multiplication
    private static Atom multW(Atom a, Atom b) {
        BigInteger aN = a.number;
        BigInteger bN = b.number;
        return new Atom(aN.multiply(bN));
    }

    //checks if ordinal equals to zero natural number
    private static boolean isZero(Ordinal a) {
        //TODO: write a good form of isZero
        if (isAtom(a)) {
            return a.equals(Atom.ZERO);
        }
        return false;
    }

    //psuedo multiplication for ordinals
    private static Ordinal pMult(Ordinal a, Ordinal b, int n) {
        if (isZero(a) || isZero(b)) {
            return Atom.ZERO;
        }
        if (isAtom(a) && isAtom(b)) {
            return multW((Atom) a, (Atom) b);
        }
        if (isAtom(b)) {
            return dot(makePair(fe(a), multW((Atom) fc(a), (Atom) b)), rest(a));
        }
        int m = count(fe(a), fe(b), n);
        return dot(makePair(pAdd(fe(a), fe(b), m), fc(b)), pMult(a, rest(b), m));
    }

    //smarter ordinal multiplication
    private static Ordinal multO(Ordinal a, Ordinal b) {
        return pMult(a, b, 0);
    }

    // < relation on naturals
    private static boolean lessW(Ordinal a, Ordinal b) {
        BigInteger first = ((Atom) a).number;
        BigInteger second = ((Atom) b).number;
        return first.compareTo(second) < 0;
    }

    // raise to power on naturals
    private static Ordinal expW(Ordinal p, Ordinal b) {
        BigInteger number = ((Atom) p).number;
        BigInteger power = ((Atom) b).number;
        return new Atom(number.pow(power.intValue()));
    }

    //raising a natural number to an infinite ordinal power
    private static Ordinal exp1(Ordinal p, Ordinal b) {
        if (cmpO(fe(b), Atom.ONE) == CompareResult.EQUALS) {
            return makePair(makePair(fc(b), expW(p, rest(b))), Atom.ZERO);
        }
        if (isAtom(rest(b))) {
            //(<(<fe(b) − 1,fc(b)> ,0), exp(p,rest(b))> ,0)
            Ordinal innerPair = makePair(minusO(fe(b), Atom.ONE), fc(b));
            Ordinal outerPair = makePair(makePair(innerPair, Atom.ZERO), expW(p, rest(b)));
            return makePair(outerPair, Atom.ZERO);
        }
        //(<<fe(b) − 1, 1> . fe(c), fc(c)> ,0)>
        Ordinal c = exp1(p, rest(b));
        Ordinal innerPair = makePair(minusO(fe(b), Atom.ONE), Atom.ONE);
        Ordinal outerPair = makePair(dot(innerPair, fe(c)), fc(c));
        return makePair(outerPair, Atom.ZERO);
    }

    //raising a limit ordinal to a positive integer power
    private static Ordinal exp2(Ordinal a, Atom q) {
        if (q.equals(Atom.ONE)) {
            return a;
        }
        Ordinal mul = multO(fe(a), minusW(q, Atom.ONE));
        return multO(makePair(makePair(mul, Atom.ONE), Atom.ZERO), a);
    }

    //returns true if a = 0 or a represents a limit ordinal
    private static boolean limitP(Ordinal a) {
        if (isAtom(a)) {
            return a.equals(Atom.ZERO);
        }
        return limitP(rest(a));
    }

    //returns the greatest ordinal, b, such that limitp(b) and b < a
    private static Ordinal limitPart(Ordinal a) {
        if (isAtom(a)) {
            return Atom.ZERO;
        }
        return dot(first(a), limitPart(rest(a)));
    }

    //returns the natural part of an ordinal
    private static Atom natPart(Ordinal a) {
        if (isAtom(a)) {
            return (Atom) a;
        }
        return natPart(rest(a));
    }

    //skips over the first n elements of a and then adds the rest to b
    private static Ordinal pAdd(Ordinal a, Ordinal b, int n) {
        return append(firstN(a, n), addO(restN(a, n), b));
    }

    //raising an infinite ordinal to a natural power
    private static Ordinal exp3(Ordinal a, Atom q) {
        if (q.equals(Atom.ZERO)) {
            return Atom.ONE;
        }
        if (q.equals(Atom.ONE)) {
            return a;
        }
        if (limitP(a)) {
            return exp2(a, q);
        }
        return multO(exp3(a, minusW(q, Atom.ONE)), a);
    }

    //raising an infinite ordinal to a possibly infinite power
    private static Ordinal exp4(Ordinal a, Ordinal b) {
        Ordinal first = makePair(makePair(multO(fe(a), limitPart(b)), Atom.ONE), Atom.ZERO);
        Ordinal exp = exp3(a, natPart(b));
        return multO(first, exp);
    }

    private static Ordinal expO(Ordinal a, Ordinal b) {
        if (b.equals(Atom.ZERO) || a.equals(Atom.ONE)) {
            return Atom.ONE;
        }
        if (a.equals(Atom.ZERO)) {
            return Atom.ZERO;
        }
        if (isAtom(a) && isAtom(b)) {
            return expW(a, b);
        }
        if (isAtom(a)) {
            return exp1(a, b);
        }
        if (isAtom(b)) {
            return exp3(a, (Atom) b);
        }
        return exp4(a, b);
    }

    private static Ordinal getOrdinal(Expression curNode) {
        if (curNode.getClass().equals(Number.class)) {
            BigInteger value = new BigInteger(((Number) curNode).getNumber());
            return new Atom(value);
        }
        if (curNode.getClass().equals(OmegaVariable.class)) {
            return new CList(new CList(Atom.ONE, Atom.ONE), Atom.ZERO);
        }
        if (curNode.getClass().equals(Sum.class)) {
            Ordinal left = getOrdinal(((Sum) curNode).firstArgument);
            Ordinal right = getOrdinal(((Sum) curNode).secondArgument);
            return addO(left, right);
        }
        if (curNode.getClass().equals(Multiply.class)) {
            Ordinal left = getOrdinal(((Multiply) curNode).firstArgument);
            Ordinal right = getOrdinal(((Multiply) curNode).secondArgument);
            return multO(left, right);
        }
        if (curNode.getClass().equals(Power.class)) {
            Ordinal left = getOrdinal(((Power) curNode).firstArgument);
            Ordinal right = getOrdinal(((Power) curNode).secondArgument);
            return expO(left, right);
        }
        throw new RuntimeException("Cannot detect class type of node: " + curNode.getClass().getCanonicalName());
    }

    public static void main(String[] args) {
        try (Scanner input = new Scanner(new File("input.txt"));
             PrintWriter out = new PrintWriter("output.txt")
        ) {
            while (input.hasNextLine()) {
                String line = input.nextLine();
                ExpressionParser parser = new ExpressionParser();
                Expression result = parser.parse(line);

                Expression leftExpression = ((Equals) result).firstArgument;
                Expression rightExpression = ((Equals) result).secondArgument;
                System.out.println("Left expression: " + leftExpression);
                System.out.println("Right expression: " + rightExpression);

                Ordinal leftOrdinal = getOrdinal(leftExpression);
                Ordinal rightOrdinal = getOrdinal(rightExpression);

                System.out.println("Left ordinal: " + leftOrdinal);
                System.out.println("Right ordinal: " + rightOrdinal);

                if (cmpO(leftOrdinal, rightOrdinal) == CompareResult.EQUALS) {
                    out.println("Равны");
                } else {
                    out.println("Не равны");
                }
            }
        } catch (ParserException | FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
