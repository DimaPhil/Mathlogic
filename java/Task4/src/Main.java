import exceptions.ParserException;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class Main {

    private static ExpressionParser parser = new ExpressionParser();

    private static Expression[] axioms10;
    private static Expression[] atoAProof;
    private static Expression[] implToConjProof;
    private static Expression[] conjToImplProof;
    private static Expression[] implReverseProof;
    private static Expression[] formalArithmeticAxioms;

    private static Expression[] readWhilePossibleIntoArray(Scanner in) throws ParserException {
        List<Expression> allExpressions = new ArrayList<>();
        while (in.hasNextLine()) {
            String curLine = in.nextLine();
            if (curLine.isEmpty()) {
                break;
            }
            allExpressions.add(parser.parseAsPlaceHolder(curLine));
        }
        return allExpressions.toArray(new Expression[allExpressions.size()]);
    }

    private static void readAxioms(Scanner in) throws ParserException {
        axioms10 = readWhilePossibleIntoArray(in);
    }

    private static void readAtoAProof(Scanner in) throws ParserException {
        atoAProof = readWhilePossibleIntoArray(in);
    }

    private static void readImplToConjProof(Scanner in) throws ParserException {
        implToConjProof = readWhilePossibleIntoArray(in);
    }

    private static void readConjToImplProof(Scanner in) throws ParserException {
        conjToImplProof = readWhilePossibleIntoArray(in);
    }

    private static void readImplReverseProof(Scanner in) throws ParserException {
        implReverseProof = readWhilePossibleIntoArray(in);
    }

    private static void readFormalArithmeticAxioms(Scanner in) throws ParserException {
        List<Expression> allAxioms = new ArrayList<>();
        while (in.hasNextLine()) {
            allAxioms.add(parser.parse(in.nextLine()));
        }
        formalArithmeticAxioms = allAxioms.toArray(new Expression[allAxioms.size()]);
    }

    private static class ProofException extends RuntimeException {
        ProofException(int badLineNumber, String message) {
            super("Вывод некорректен начиная с формулы номер " + badLineNumber + ": " + message);
        }
    }

    private static List<Expression> reconstructProof(Expression schemeProof[], Expression a, Expression b, Expression c) {
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

    private static class PairOfIndexes {
        int what;
        int where;

        PairOfIndexes(int what, int where) {
            this.what = what;
            this.where = where;
        }
    }

    //TODO: check substitutions
    private static List<Expression> getNewProof(List<Expression> oldProof, Expression[] axioms10,
                                                List<Expression> hypothesis, Expression reducible) throws ProofException {
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
        Map<Expression, Map<Expression, Integer>> allImplicationsStartedWith = new HashMap<>();

        for (int lineNumber = 0; lineNumber < oldProof.size(); lineNumber++) {
            Expression expression = oldProof.get(lineNumber);
            //0) Firstly add all information about new modes-ponenses:
            if (!allExpressions.containsKey(expression)) {
                if (allImplicationsStartedWith.containsKey(expression)) {
                    Map<Expression, Integer> allConsequences = allImplicationsStartedWith.get(expression);
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

            //1) check if reducible == curExpression
            {
                if (expression.equals(reducible)) {
                    if (makeDeduction) {
                        Map<Variable, Expression> substitutions = new HashMap<>();
                        substitutions.put(new Variable("a"), expression);
                        for (Expression proofLine : atoAProof) {
                            newProof.add(proofLine.substitute(substitutions));
                        }
                    }
                    continue;
                }
            }
            //2) check if expression is an axiom or an hypothesis
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
                //Check if induction arithmetic axiom
                if (expression.getClass().equals(Implication.class)) {
                    //Check if phi[x:=0] & @x(phi->phi[x:=x'])->phi ???
                    Expression leftPart = ((Implication) expression).statement;
                    Expression phi = ((Implication) expression).consequence;
                    if (leftPart.getClass().equals(Conjunction.class)) {
                        Expression probPhi0 = ((Conjunction) leftPart).firstArgument;
                        Expression rightConj = ((Conjunction) leftPart).secondArgument;
                        if (rightConj.getClass().equals(ForAllQuantifier.class)) {
                            Expression variable = ((ForAllQuantifier) rightConj).variable;
                            Expression statement = ((ForAllQuantifier) rightConj).statement;
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
                                            //It's really the induction axiom!!!
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
                    if (leftExpression.getClass().equals(ForAllQuantifier.class)) {
                        Expression variable = ((ForAllQuantifier) leftExpression).variable;
                        Expression statement = ((ForAllQuantifier) leftExpression).statement;
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
            //5) check if statement is axiom#12: A[x:=z]->?xA
            {
                //!?a@b?bP(a,b)|Q(a,b) -> ?a1(!?a@b?bP(a,b)|Q(a,b))
                if (expression.getClass().equals(Implication.class)) {
                    Expression leftExpression = ((Implication) expression).statement;
                    Expression rightExpression = ((Implication) expression).consequence;
                    if (rightExpression.getClass().equals(ExistsQuantifier.class)) {
                        Expression variable = ((ExistsQuantifier) rightExpression).variable;
                        Expression statement = ((ExistsQuantifier) rightExpression).statement;
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
            //6) check if statement is the first new rule: (B->A) => (B->@xA)
            {
                if (expression.getClass().equals(Implication.class)) {
                    Expression leftExpression = ((Implication) expression).statement;
                    Expression rightExpression = ((Implication) expression).consequence;
                    if (rightExpression.getClass().equals(ForAllQuantifier.class)) {
                        Expression variable = ((ForAllQuantifier) rightExpression).variable;
                        Expression statement = ((ForAllQuantifier) rightExpression).statement;
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
                                        newProof.add(new Implication(new Conjunction(reducible, leftExpression), new ForAllQuantifier(variable, statement)));
                                        //add (a&b->c)->(a->b->c) proof
                                        //where a := reducible
                                        //where b := leftExpression
                                        //where c := rightExpression == @x(statement)
                                        newProof.addAll(reconstructProof(conjToImplProof, reducible, leftExpression, new ForAllQuantifier(variable, statement)));
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
            //7) check if statement is the second new rule: (A->B) => (?xA->B)
            {
                if (expression.getClass().equals(Implication.class)) {
                    Expression leftExpression = ((Implication) expression).statement;
                    Expression rightExpression = ((Implication) expression).consequence;
                    if (leftExpression.getClass().equals(ExistsQuantifier.class)) {
                        Expression variable = ((ExistsQuantifier) leftExpression).variable;
                        Expression statement = ((ExistsQuantifier) leftExpression).statement;
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
                                        newProof.add(new Implication(new ExistsQuantifier(variable, statement), new Implication(reducible, rightExpression)));
                                        //add (?x(b)->a->c)->(a->?x(b)->c) proof
                                        //where a := reducible
                                        //where b := statement
                                        //where c := rightExpression
                                        //(a->b->c)->(b->a->c)
                                        newProof.addAll(reconstructProof(implReverseProof, new ExistsQuantifier(variable, statement), reducible, rightExpression));
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


    public static void main(String[] args) {
        try (
                Scanner in = new Scanner(new File("input.txt"));
                Scanner axiomsScanner = new Scanner(new File("axioms10.txt"));
                Scanner implToConjScanner = new Scanner(new File("implToConjProof.txt"));
                Scanner conjToImplScanner = new Scanner(new File("conjToImplProof.txt"));
                Scanner implReverseScanner = new Scanner(new File("implReverseProof.txt"));
                Scanner atoAScanner = new Scanner(new File("atoAProof.txt"));
                Scanner formalArithmeticScanner = new Scanner(new File("formalArithmeticAxioms.txt"));
                PrintWriter out = new PrintWriter("output.txt")
        ) {
            readAxioms(axiomsScanner);
            readAtoAProof(atoAScanner);
            readImplToConjProof(implToConjScanner);
            readConjToImplProof(conjToImplScanner);
            readImplReverseProof(implReverseScanner);
            readFormalArithmeticAxioms(formalArithmeticScanner);

            //Reading <Заголовок>
            String header = in.nextLine();
            String halves[] = header.split("\\|\\-");

            Expression demonstrable = parser.parse(halves[1]);
            List<Expression> hypothesis = new ArrayList<>();

            int balance = 0;
            int lastStart = 0;
            for (int i = 0; i < halves[0].length(); i++) {
                if (halves[0].charAt(i) == '(') {
                    balance++;
                }
                else if (halves[0].charAt(i) == ')') {
                    balance--;
                }
                else if (halves[0].charAt(i) == ',' && balance == 0) {
                    String currentHypothesisStr = halves[0].substring(lastStart, i);
                    lastStart = i + 1;
                    hypothesis.add(parser.parse(currentHypothesisStr));
                }
            }
            if (!halves[0].trim().isEmpty()) {
                hypothesis.add(parser.parse(halves[0].substring(lastStart)));
            }

            //Reading <Доказательство>
            List<Expression> oldProof = new ArrayList<>();
            while (in.hasNextLine()) {
                String line = in.nextLine();
                try {
                    oldProof.add(parser.parse(line));
                } catch (ParserException e) {
                    System.err.println("Error during parsing expression: " + line);
                    System.err.println(e.getMessage());
                    return;
                }
            }

            //Reconstruct old proof with checking errors
            Expression reducible = null;
            if (!hypothesis.isEmpty()) {
                reducible = hypothesis.get(hypothesis.size() - 1);
                hypothesis.remove(hypothesis.size() - 1);
            }
            //System.out.println("Reducible: " + reducible);
            //System.out.println("Demonstrable: " + demonstrable);

            //@a?a@b?bP(a,b)->Q(a,b)
            //(@a(?a(@b(?b(P(a, b))))))->(Q(a, b))

            //?a1@a1?b1@b1(P(a1,b1)->Q(a,b))
            //?a1(@a1(?b1(@b1((P(a1, b1))->(Q(a, b))))))

            try {
                List<Expression> newProof = getNewProof(oldProof, axioms10, hypothesis, reducible);
                //Proof is correct, so just print it out
                if (reducible == null) {
                    newProof = oldProof;
                }
                for (Expression proofLine : newProof) {
                    out.println(proofLine);
                }
            } catch (ProofException e) {
                out.println(e.getMessage());
            }

        } catch (ParserException | RuntimeException | IOException e) {
            System.err.println("Some error has happened. Probable cause: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
