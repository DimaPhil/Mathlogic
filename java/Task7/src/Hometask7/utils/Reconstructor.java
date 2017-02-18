package Hometask7.utils;

import Hometask7.exceptions.ParserException;
import Hometask7.hierarchy.*;
import Hometask7.parser.ExpressionParser;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Reconstructor {

    private static ExpressionParser parser = new ExpressionParser();

    private static Expression[] axioms10;
    private static Expression[] atoAProof;
    private static Expression[] implToConjProof;
    private static Expression[] conjToImplProof;
    private static Expression[] implReverseProof;
    private static Expression[] formalArithmeticAxioms;
    public static Expression[] swapHypoProof;
    public static Expression[] contraProof;

    static {
        try (
                Scanner axiomsScanner = new Scanner(new File("ProofTexts/axioms10.txt"));
                Scanner implToConjScanner = new Scanner(new File("ProofTexts/implToConjProof.txt"));
                Scanner conjToImplScanner = new Scanner(new File("ProofTexts/conjToImplProof.txt"));
                Scanner atoAScanner = new Scanner(new File("ProofTexts/atoAProof.txt"));
                Scanner formalArithmeticScanner = new Scanner(new File("ProofTexts/formalArithmeticAxioms.txt"));
                Scanner implReverseScanner = new Scanner(new File("ProofTexts/implReverseProof.txt"));
                Scanner swapHypoScanner = new Scanner(new File("ProofTexts/swapHypoProof.txt"));
                Scanner contraScanner = new Scanner(new File("ProofTexts/contrapositionProof.txt"));
        ) {
            axioms10 = readWhilePossibleIntoArray(axiomsScanner, true);
            atoAProof = readWhilePossibleIntoArray(atoAScanner, true);
            implToConjProof = readWhilePossibleIntoArray(implToConjScanner, true);
            conjToImplProof = readWhilePossibleIntoArray(conjToImplScanner, true);
            implReverseProof = readWhilePossibleIntoArray(implReverseScanner, true);
            swapHypoProof = readWhilePossibleIntoArray(swapHypoScanner, true);
            contraProof = readWhilePossibleIntoArray(contraScanner, true);
            formalArithmeticAxioms = readWhilePossibleIntoArray(formalArithmeticScanner, false);
        } catch (IOException | ParserException e) {
            e.printStackTrace();
        }
    }

    public static List<Expression> reconstructProof(Expression schemeProof[], Expression a, Expression b, Expression c) {
        List<Expression> newProof = new ArrayList<>();
        HashMap<Variable, Expression> changes = new HashMap<>();
        if (a != null) {
            changes.put(new Variable("a"), a);
        }
        if (b != null) {
            changes.put(new Variable("b"), b);
        }
        if (c != null) {
            changes.put(new Variable("c"), c);
        }
        for (Expression schemeProofLine : schemeProof) {
            newProof.add(schemeProofLine.substitute(changes));
        }
        return newProof;
    }

    private static Expression[] readWhilePossibleIntoArray(Scanner in, boolean asPlaceHolder) throws ParserException {
        List<Expression> allExpressions = new ArrayList<>();
        while (in.hasNextLine()) {
            String curLine = in.nextLine();
            if (curLine.isEmpty()) {
                break;
            }
            if (asPlaceHolder) {
                allExpressions.add(parser.parseAsPlaceHolder(curLine));
            } else {
                allExpressions.add(parser.parse(curLine));
            }
        }
        return allExpressions.toArray(new Expression[allExpressions.size()]);
    }
    private static class PairOfIndexes {
        int what;
        int where;

        PairOfIndexes(int what, int where) {
            this.what = what;
            this.where = where;
        }

    }
    private static class ProofException extends RuntimeException {
        ProofException(int badLineNumber, String message) {
            super("Вывод некорректен начиная с формулы номер " + badLineNumber + ": " + message);
        }

    }

    public static List<Expression> deduction(List<Expression> oldProof, List<Expression> hypothesis, Expression reducible) throws ProofException {
        List<Expression> newProof = new ArrayList<>();
        boolean makeDeduction = true;
        if (reducible == null) {
            //Simply check existing proof, without deduction
            makeDeduction = false;
            //To avoid NullPointerException
            reducible = new Zero();
        }
        Set<Expression> freeVariablesOfReducible = reducible.getFreeVariables();

        Map<Expression, PairOfIndexes> allModusPonenses = new HashMap<>();
        Map<Expression, Integer> allExpressions = new HashMap<>();
        Map<Expression, HashMap<Expression, Integer>> allImplicationsStartedWith = new HashMap<>();

        for (int lineNumber = 0; lineNumber < oldProof.size(); lineNumber++) {
            Expression expression = oldProof.get(lineNumber);
            //0. Firstly add all information about new modes-ponenses:
            if (!allExpressions.containsKey(expression)) {
                if (allImplicationsStartedWith.containsKey(expression)) {
                    HashMap<Expression, Integer> allConsequences = allImplicationsStartedWith.get(expression);
                    for (Expression curConsequence : allConsequences.keySet()) {
                        int indexOfStatement = allConsequences.get(curConsequence);
                        allModusPonenses.put(curConsequence, new PairOfIndexes(lineNumber, indexOfStatement));
                    }
                }
                allExpressions.put(expression, lineNumber);
            }
            if (expression.getClass().equals(Implication.class)) {
                Expression statement = ((Implication) expression).statement;
                Expression consequence = ((Implication) expression).consequence;
                if (allExpressions.containsKey(statement)) {
                    int indexOfStatement = allExpressions.get(statement);
                    allModusPonenses.put(consequence, new PairOfIndexes(indexOfStatement, lineNumber));
                }
                if (!allImplicationsStartedWith.containsKey(statement)) {
                    allImplicationsStartedWith.put(statement, new HashMap<>());
                }
                allImplicationsStartedWith.get(statement).put(consequence, lineNumber);
            }

            //1. check if reducible == curExpression
            {
                if (expression.equals(reducible)) {
                    if (makeDeduction) {
                        HashMap<Variable, Expression> substitutions = new HashMap<>();
                        substitutions.put(new Variable("a"), expression);
                        for (Expression proofLine : atoAProof) {
                            newProof.add(proofLine.substitute(substitutions));
                        }
                    }
                    continue;
                }
            }
            //2. check if expression is an axiom or an hypothesis
            {
                boolean isAxiomOrHypothesis = false;
                Expression origin = null;
                //Check if one of propositional logic axioms
                for (Expression axiom : axioms10) {
                    if (axiom.isBaseFor(expression)) {
                        origin = expression;
                        isAxiomOrHypothesis = true;
                        break;
                    }
                }
                //Check if hypothesis
                for (Expression ahypothesis : hypothesis) {
                    if (ahypothesis.equals(expression)) {
                        origin = ahypothesis;
                        isAxiomOrHypothesis = true;
                        break;
                    }
                }
                //Check if formal arithmetic axiom
                for (Expression formalAxiom : formalArithmeticAxioms) {
                    if (formalAxiom.equals(expression)) {
                        origin = formalAxiom;
                        isAxiomOrHypothesis = true;
                        break;
                    }
                }
                //Check if inductConclusion arithmetic axiom
                if (expression.getClass().equals(Implication.class)) {
                    //Check if phi[x:=0] & @x(phi->phi[x:=x'])->phi ???
                    Expression leftPart = ((Implication) expression).statement;
                    Expression phi = ((Implication) expression).consequence;
                    if (leftPart.getClass().equals(Conjunction.class)) {
                        Expression probPhi0 = ((Conjunction) leftPart).firstArgument;
                        Expression rightConj = ((Conjunction) leftPart).secondArgument;
                        if (rightConj.getClass().equals(ForAll.class)) {
                            Expression variable = ((ForAll) rightConj).variable;
                            Expression statement = ((ForAll) rightConj).statement;
                            if (statement.getClass().equals(Implication.class)) {
                                Expression probablePhi = ((Implication) statement).statement;
                                Expression nextPhi = ((Implication) statement).consequence;
                                //probPhi0&@x(probPhi->nextPhi) -> phi
                                if (probablePhi.equals(phi)) {
                                    //probPhi0&@x(phi->nextPhi)->phi
                                    HashMap<Variable, Expression> changesForPhiZero = new HashMap<>();
                                    changesForPhiZero.put((Variable) variable, new Zero());
                                    Expression checkerPhiZero = phi.substitute(changesForPhiZero);
                                    if (checkerPhiZero.equals(probPhi0)) {
                                        //phi0&@x(phi->nextPhi)->phi
                                        HashMap<Variable, Expression> changesForPhiNext = new HashMap<>();
                                        changesForPhiNext.put((Variable) variable, new Quote(variable));
                                        Expression checkerPhiNext = phi.substitute(changesForPhiNext);
                                        if (checkerPhiNext.equals(nextPhi)) {
                                            //phi0&@x(phi->phi[x:=x'])->phi
                                            //It's really the inductConclusion axiom!!!
                                            isAxiomOrHypothesis = true;
                                            origin = expression;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (isAxiomOrHypothesis) {
                    //bi
                    //bi->a->bi
                    //MP: a->bi
                    //origin is axiom or one of hypothesis
                    if (makeDeduction) {
                        newProof.add(origin);
                        newProof.add(new Implication(origin, new Implication(reducible, origin)));
                        newProof.add(new Implication(reducible, origin));
                    }
                    continue;
                }
            }
            //3) check if statement Bi derived from modus ponens of others: Bk = Bj -> Bi
            {
                if (allModusPonenses.containsKey(expression)) {
                    PairOfIndexes pair = allModusPonenses.get(expression);
                    Expression baseExpression = oldProof.get(pair.where);
                    Expression stateExpression = oldProof.get(pair.what);
                    if (makeDeduction) {
                        HashMap<Variable, Expression> substitutions = new HashMap<>();
                        substitutions.put(new Variable("a"), reducible);
                        substitutions.put(new Variable("bk"), baseExpression);
                        substitutions.put(new Variable("bj"), stateExpression);
                        substitutions.put(new Variable("bi"), expression);
                        try {
                            newProof.add(parser.parseAsPlaceHolder("(a->bj)->(a->bk)->(a->bi)").substitute(substitutions));
                            newProof.add(parser.parseAsPlaceHolder("(a->bk)->(a->bi)").substitute(substitutions));
                            newProof.add(parser.parseAsPlaceHolder("(a->bi)").substitute(substitutions));
                        } catch (ParserException ignore) {
                        }
                    }
                    continue;
                }
            }
            //4) check if statement is axiom#11: @xA->A[x:=z]
            {
                if (expression.getClass().equals(Implication.class)) {
                    Expression leftExpression = ((Implication) expression).statement;
                    Expression rightExpression = ((Implication) expression).consequence;
                    if (leftExpression.getClass().equals(ForAll.class)) {
                        Expression variable = ((ForAll) leftExpression).variable;
                        Expression statement = ((ForAll) leftExpression).statement;
                        HashMap<Expression, Expression> checker = new HashMap<>();
                        //Is it possible, without constraints, that expression may be the base of right expression?
                        if (statement.isBaseFor(rightExpression, checker)) {
                            if (!checker.containsKey(variable)) {
                                if (makeDeduction) {
                                    //bi
                                    //bi->a->bi
                                    //MP: a->bi
                                    newProof.add(expression);
                                    newProof.add(new Implication(expression, new Implication(reducible, expression)));
                                    newProof.add(new Implication(reducible, expression));
                                }
                                continue;
                            } else {
                                //get 'z' from right part A[x:=z]
                                Expression tettaTerm = checker.get(variable);
                                //@xA->A[x:=tettaTerm]
                                //check if tettaTerm is free to substitute into A instead of x
                                if (statement.isTermFreeToSubstituteInsteadOf(tettaTerm, variable)) {
                                    if (makeDeduction) {
                                        //bi
                                        //bi->a->bi
                                        //MP: a->bi
                                        newProof.add(expression);
                                        newProof.add(new Implication(expression, new Implication(reducible, expression)));
                                        newProof.add(new Implication(reducible, expression));
                                    }
                                    continue;
                                } else {
                                    throw new ProofException(lineNumber + 1, "терм " + tettaTerm + " не свободен для подстановки в формулу " + statement + " вместо переменной " + variable + ".");
                                }
                            }
                        }
                    }
                }
            }
            //5. check if statement is axiom#12: A[x:=z]->?xA
            {
                //!?a@b?bP(a,b)|Q(a,b) -> ?a1(!?a@b?bP(a,b)|Q(a,b))
                if (expression.getClass().equals(Implication.class)) {
                    Expression leftExpression = ((Implication) expression).statement;
                    Expression rightExpression = ((Implication) expression).consequence;
                    if (rightExpression.getClass().equals(Exists.class)) {
                        Expression variable = ((Exists) rightExpression).variable;
                        Expression statement = ((Exists) rightExpression).statement;
                        HashMap<Expression, Expression> checker = new HashMap<>();
                        //Is it possible, without constraints, that expression may be the base of left expression?
                        if (statement.isBaseFor(leftExpression, checker)) {
                            if (!checker.containsKey(variable)) {
                                if (makeDeduction) {
                                    //bi
                                    //bi->a->bi
                                    //MP: a->bi
                                    newProof.add(expression);
                                    newProof.add(new Implication(expression, new Implication(reducible, expression)));
                                    newProof.add(new Implication(reducible, expression));
                                }
                                continue;
                            } else {
                                //get 'z' from left part A[x:=z]
                                Expression tettaTerm = checker.get(variable);
                                //A[x:=tettaTerm]->?xA
                                //check if tettaTerm is free to substitute into A instead of x
                                if (statement.isTermFreeToSubstituteInsteadOf(tettaTerm, variable)) {
                                    if (makeDeduction) {
                                        //bi
                                        //bi->a->bi
                                        //MP: a->bi
                                        newProof.add(expression);
                                        newProof.add(new Implication(expression, new Implication(reducible, expression)));
                                        newProof.add(new Implication(reducible, expression));
                                    }
                                    continue;
                                } else {
                                    throw new ProofException(lineNumber + 1, "терм " + tettaTerm + " не свободен для подстановки в формулу " + statement + " вместо переменной " + variable + ".");
                                }
                            }
                        }
                    }
                }
            }
            //6. check if statement is the first new rule: (B->A) => (B->@xA)
            {
                if (expression.getClass().equals(Implication.class)) {
                    Expression leftExpression = ((Implication) expression).statement;
                    Expression rightExpression = ((Implication) expression).consequence;
                    if (rightExpression.getClass().equals(ForAll.class)) {
                        Expression variable = ((ForAll) rightExpression).variable;
                        Expression statement = ((ForAll) rightExpression).statement;
                        if (leftExpression.getFreeVariables().contains(variable)) {
                            throw new ProofException(lineNumber + 1, "переменная " + variable + " входит свободно в формулу " + leftExpression + ".");
                        }
                        if (freeVariablesOfReducible.contains(variable)) {
                            throw new ProofException(lineNumber + 1, "используется правило с квантором по переменной " + variable + ", входящей свободно в допущение " + reducible);
                        }
                        boolean isFirstRule = false;
                        for (int i = 0; i < lineNumber; i++) {
                            Expression baseExpression = oldProof.get(i);
                            if (baseExpression.getClass().equals(Implication.class)) {
                                Expression baseLeftExpression = ((Implication) baseExpression).statement;
                                Expression baseRightExpression = ((Implication) baseExpression).consequence;
                                //Check if baseExpression == (B->A), that is base of current expression
                                if (baseLeftExpression.equals(leftExpression) && baseRightExpression.equals(statement)) {
                                    isFirstRule = true;
                                    if (makeDeduction) {
                                        //add (a->b->c)->(a&b->c) proof
                                        //where a := reducible
                                        //where b := leftExpression
                                        //where c := statement
                                        newProof.addAll(reconstructProof(implToConjProof, reducible, leftExpression, statement));
                                        //add (a&b->c) with the same substitutions, as far as (a->b->c) is already added on previous steps
                                        newProof.add(new Implication(new Conjunction(reducible, leftExpression), statement));
                                        //add (a&b->@x(c)) with the same substitutions
                                        newProof.add(new Implication(new Conjunction(reducible, leftExpression), new ForAll(variable, statement)));
                                        //add (a&b->c)->(a->b->c) proof
                                        //where a := reducible
                                        //where b := leftExpression
                                        //where c := rightExpression == @x(statement)
                                        newProof.addAll(reconstructProof(conjToImplProof, reducible, leftExpression, new ForAll(variable, statement)));
                                        //add reducible->(leftExpression->@x(statement)) == expression
                                        newProof.add(new Implication(reducible, expression));
                                    }
                                }
                            }
                        }
                        if (isFirstRule) {
                            continue;
                        }
                    }
                }
            }
            //7. check if statement is the second new rule: (A->B) => (?xA->B)
            {
                if (expression.getClass().equals(Implication.class)) {
                    Expression leftExpression = ((Implication) expression).statement;
                    Expression rightExpression = ((Implication) expression).consequence;
                    if (leftExpression.getClass().equals(Exists.class)) {
                        Expression variable = ((Exists) leftExpression).variable;
                        Expression statement = ((Exists) leftExpression).statement;
                        if (rightExpression.getFreeVariables().contains(variable)) {
                            throw new ProofException(lineNumber + 1, "переменная " + variable + "входит свободно в формулу " + leftExpression + ".");
                        }
                        if (freeVariablesOfReducible.contains(variable)) {
                            throw new ProofException(lineNumber + 1, "используется правило с квантором по переменной " + variable + ", входящей свободно в допущение " + reducible);
                        }
                        boolean isSecondRule = false;
                        for (int i = 0; i < lineNumber; i++) {
                            Expression baseExpression = oldProof.get(i);
                            if (baseExpression.getClass().equals(Implication.class)) {
                                Expression baseLeftExpression = ((Implication) baseExpression).statement;
                                Expression baseRightExpression = ((Implication) baseExpression).consequence;
                                if (baseRightExpression.equals(rightExpression) && baseLeftExpression.equals(statement)) {
                                    isSecondRule = true;
                                    if (makeDeduction) {
                                        //add (a->b->c)->(b->a->c) proof
                                        //where a := reducible
                                        //where b := statement
                                        //where c := rightExpression
                                        newProof.addAll(reconstructProof(implReverseProof, reducible, statement, rightExpression));
                                        //add (b->a->c) with the same substitutions, as far as (a->b->c) is already added on previous steps
                                        newProof.add(new Implication(statement, new Implication(reducible, rightExpression)));
                                        //add (?x(b)->a->c) with the same substitutions
                                        newProof.add(new Implication(new Exists(variable, statement), new Implication(reducible, rightExpression)));
                                        //add (?x(b)->a->c)->(a->?x(b)->c) proof
                                        //where a := reducible
                                        //where b := statement
                                        //where c := rightExpression
                                        //(a->b->c)->(b->a->c)
                                        newProof.addAll(reconstructProof(implReverseProof, new Exists(variable, statement), reducible, rightExpression));
                                        //add reducible->(leftExpression->@x(statement)) == expression
                                        newProof.add(new Implication(reducible, expression));
                                    }
                                }
                            }
                        }
                        if (isSecondRule) {
                            continue;
                        }
                    }
                }
            }
            {
                //We have not encountered the right case, so this is a bad expression
                throw new ProofException(lineNumber + 1, "выражение " + expression + " не является аксиомой и не выводится из предыдущих.");
            }
        }
        return newProof;
    }

}
