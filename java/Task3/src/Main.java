import exceptions.ParserException;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class Main {

    private static ExpressionParser parser = new ExpressionParser();
    private static Expression[] axioms;
    private static Expression[] exclusionOfThird;
    private static Expression[][][] implication = new Expression[2][2][];
    private static Expression[][][] conjunction = new Expression[2][2][];
    private static Expression[][][] disjunction = new Expression[2][2][];
    private static Expression[][] negation = new Expression[2][];


    private static Expression[] readWhilePossibleIntoArray(Scanner in) {
        List<Expression> allExpressions = new ArrayList<>();
        while (in.hasNextLine()) {
            String curLine = in.nextLine();
            if (curLine.isEmpty())
                break;
            try {
                allExpressions.add(parser.parse(curLine));
            } catch (ParserException ignore) {
            }
        }
        Expression[] array = new Expression[allExpressions.size()];
        allExpressions.toArray(array);
        return array;
    }

    private static void readAxioms(Scanner in) {
        axioms = readWhilePossibleIntoArray(in);
    }

    private static void readExclusionOfThird(Scanner in) {
        exclusionOfThird = readWhilePossibleIntoArray(in);
    }

    private static void readBundleProofs(Scanner in) {
        Expression[][][][] order = {implication, conjunction, disjunction};
        for (Expression[][][] bundle : order) {
            for (int i = 0; i <= 1; i++) {
                for (int j = 0; j <= 1; j++) {
                    String metaDescription = in.nextLine();
                    bundle[i][j] = readWhilePossibleIntoArray(in);
                }
            }
        }
        for (int i = 0; i <= 1; i++) {
            String metaDescription = in.nextLine();
            negation[i] = readWhilePossibleIntoArray(in);
        }
    }

    private static List<Expression> deductionConversion(List<Expression> oldProof, Expression[] axioms,
                                                        List<Expression> hypothesis, Expression reducible) {

        class PairOfIndexes {
            private int what;
            private int where;
            private PairOfIndexes(int what, int where) {
                this.what = what;
                this.where = where;
            }
        }

        Map<Expression, PairOfIndexes> allModusPonenses = new HashMap<>();
        Map<Expression, Integer> allExpressions = new HashMap<>();
        Map<Expression, Map<Expression, Integer>> allImplicationsStartsWith = new HashMap<>();
        List<Expression> newProof = new ArrayList<>();
        for (int lineNumber = 0; lineNumber < oldProof.size(); lineNumber++) {
            Expression curExpr = oldProof.get(lineNumber);
            boolean detected = false;
            if (curExpr.equals(reducible)) {
                //proof of a->a
                detected = true;
                HashMap<Symbol, Expression> substitutions = new HashMap<>();
                substitutions.put(new Symbol("a"), curExpr);
                try {
                    newProof.add(parser.parse("a->(a->a)").substitute(substitutions));
                    newProof.add(parser.parse("(a->(a->a))->(a->(a->a)->a)->(a->a)").substitute(substitutions));
                    newProof.add(parser.parse("(a->(a->a)->a)->(a->a)").substitute(substitutions));
                    newProof.add(parser.parse("a->(a->a)->a").substitute(substitutions));
                    newProof.add(parser.parse("a->a").substitute(substitutions));
                } catch (ParserException ignore) {
                }
            }
            if (!detected) {
                Expression origin = null;
                for (Expression axiom : axioms)
                    if (axiom.isBaseFor(curExpr)) {
                        origin = curExpr;
                        detected = true;
                        break;
                    }
                for (Expression ahypothesis : hypothesis) {
                    if (ahypothesis.equals(curExpr)) {
                        origin = ahypothesis;
                        detected = true;
                        break;
                    }
                }
                if (detected) {
                    //bi
                    //bi->a->bi
                    //MP: a->bi
                    //origin is axiom or one of hypothesis
                    newProof.add(origin);
                    newProof.add(new Implication(origin, new Implication(reducible, origin)));
                    newProof.add(new Implication(reducible, origin));
                }
            }
            if (!detected) {
                //Modus Ponens bj into bk = (bj -> bi) :
                //1) (a->bj) -> (a->(bj->bi)) -> (a->bi)
                //2) (a->(bj->bi)) -> (a->bi)
                if (allModusPonenses.containsKey(curExpr)) {
                    PairOfIndexes pair = allModusPonenses.get(curExpr);
                    HashMap<Symbol, Expression> substitutions = new HashMap<>();
                    substitutions.put(new Symbol("a"), reducible);
                    substitutions.put(new Symbol("bj"), oldProof.get(pair.what));
                    substitutions.put(new Symbol("bk"), oldProof.get(pair.where));
                    substitutions.put(new Symbol("bi"), curExpr);
                    try {
                        newProof.add(parser.parse("(a->bj)->(a->bk)->(a->bi)").substitute(substitutions));
                        newProof.add(parser.parse("(a->bk)->(a->bi)").substitute(substitutions));
                        newProof.add(parser.parse("(a->bi)").substitute(substitutions));
                    } catch (ParserException ignore) {
                    }
                }
            }
            if (curExpr.getClass().equals(Implication.class)) {
                Expression statement = ((Implication) curExpr).statement;
                Expression consequence = ((Implication) curExpr).consequence;
                if (allExpressions.containsKey(statement)) {
                    int indexOfStatement = allExpressions.get(statement);
                    allModusPonenses.put(consequence, new PairOfIndexes(indexOfStatement, lineNumber));
                }
                if (!allImplicationsStartsWith.containsKey(statement))
                    allImplicationsStartsWith.put(statement, new HashMap<>());
                allImplicationsStartsWith.get(statement).put(consequence, lineNumber);
            }
            if (!allExpressions.containsKey(curExpr)) {
                if (allImplicationsStartsWith.containsKey(curExpr)) {
                    Map<Expression, Integer> allConsequences = allImplicationsStartsWith.get(curExpr);
                    for (Expression curConsequence : allConsequences.keySet()) {
                        int indexOfStatement = allConsequences.get(curConsequence);
                        allModusPonenses.put(curConsequence, new PairOfIndexes(lineNumber, indexOfStatement));
                    }
                }
                allExpressions.put(curExpr, lineNumber);
            }
        }
        return newProof;
    }


    private static Map<Symbol, Boolean> getWrongEstimation(Expression expression) {
        Set<Symbol> setOfNames = expression.getPropositionalVariables();
        Symbol allPropositionalVariables[] = new Symbol[setOfNames.size()];
        setOfNames.toArray(allPropositionalVariables);
        Map<Symbol, Boolean> currentEstimations = new HashMap<>();
        int numberOfVariables = allPropositionalVariables.length;
        for (int mask = 0; mask < (1 << numberOfVariables); mask++) {
            for (int i = 0; i < numberOfVariables; i++) {
                int ithBit = (mask >> i) & 1;
                Boolean value = Boolean.FALSE;
                if (ithBit != 0)
                    value = Boolean.TRUE;
                currentEstimations.put(allPropositionalVariables[i], value);
            }
            if (!expression.evaluate(currentEstimations))
                return currentEstimations;
        }
        return null;
    }

    private static List<Expression> reconstructProof(Expression[] schemeProof, Expression a, Expression b) {
        List<Expression> newProof = new ArrayList<>();
        Map<Symbol, Expression> changes = new HashMap<>();
        changes.put(new Symbol("a"), a);
        if (b != null)
            changes.put(new Symbol("b"), b);
        for (Expression schemeProofLine : schemeProof) {
            newProof.add(schemeProofLine.substitute(changes));
        }
        return newProof;
    }

    private static List<Expression> excludeVariable(Expression expression, List<Expression> hypothesis, Symbol reducibleVariable,
                                                    List<Expression> proof1, List<Expression> proof2) {
        List<Expression> newProof = new ArrayList<>();
        List<Expression> deduction1 = deductionConversion(proof1, axioms, hypothesis, new Negate(reducibleVariable));
        List<Expression> deduction2 = deductionConversion(proof2, axioms, hypothesis, reducibleVariable);
        newProof.addAll(deduction1);
        newProof.addAll(deduction2);
        Map<Symbol, Expression> substitutions = new HashMap<>();
        substitutions.put(new Symbol("p"), reducibleVariable);
        substitutions.put(new Symbol("a"), expression);
        for (Expression curProofLine : exclusionOfThird)
            newProof.add(curProofLine.substitute(substitutions));
        try {
            newProof.add(parser.parse("(p->a)->(!p->a)->(p|!p->a)").substitute(substitutions));
            newProof.add(parser.parse("(!p->a)->(p|!p->a)").substitute(substitutions));
            newProof.add(parser.parse("(p|!p->a)").substitute(substitutions));
            newProof.add(parser.parse("a").substitute(substitutions));
        } catch (ParserException ignore) {
        }
        return newProof;
    }

    private static int buildProofForFixedVariables(Expression u, Map<Symbol, Boolean> values, List<Expression> proof) {
        if (u.getClass().equals(Implication.class)) {
            Expression statement = ((Implication) u).statement;
            Expression consequence = ((Implication) u).consequence;
            int resultStatement = buildProofForFixedVariables(statement, values, proof);
            int resultConsequence = buildProofForFixedVariables(consequence, values, proof);
            List<Expression> reconstructedProof = reconstructProof(implication[resultStatement][resultConsequence], statement, consequence);
            proof.addAll(reconstructedProof);
            return (resultStatement == 1 && resultConsequence == 0) ? 0 : 1;
        } else if (u.getClass().equals(Conjunction.class)) {
            Expression firstExpression = ((Conjunction) u).firstArgument;
            Expression secondExpression = ((Conjunction) u).secondArgument;
            int resultFirst = buildProofForFixedVariables(firstExpression, values, proof);
            int resultSecond = buildProofForFixedVariables(secondExpression, values, proof);
            List<Expression> reconstructedProof = reconstructProof(conjunction[resultFirst][resultSecond], firstExpression, secondExpression);
            proof.addAll(reconstructedProof);
            return resultFirst * resultSecond;
        } else if (u.getClass().equals(Disjunction.class)) {
            Expression firstExpression = ((Disjunction) u).firstArgument;
            Expression secondExpression = ((Disjunction) u).secondArgument;
            int resultFirst = buildProofForFixedVariables(firstExpression, values, proof);
            int resultSecond = buildProofForFixedVariables(secondExpression, values, proof);
            List<Expression> reconstructedProof = reconstructProof(disjunction[resultFirst][resultSecond], firstExpression, secondExpression);
            proof.addAll(reconstructedProof);
            return (resultFirst + resultSecond >= 1) ? 1 : 0;
        } else if (u.getClass().equals(Negate.class)) {
            Expression subExpression = ((Negate) u).negative;
            int result = buildProofForFixedVariables(subExpression, values, proof);
            List<Expression> reconstructedProof = reconstructProof(negation[result], subExpression, null);
            proof.addAll(reconstructedProof);
            return (1 - result);
        } else if (u.getClass().equals(Symbol.class)) {
            if (values.get(u)) {
                proof.add(u);
                return 1;
            } else {
                return 0;
            }
        }
        return 0;
    }

    private static List<Expression> buildProofForFixedVariables(Expression expression, Map<Symbol, Boolean> values) {
        List<Expression> proof = new ArrayList<>();
        if (buildProofForFixedVariables(expression, values, proof) == 0) {
            throw new RuntimeException("Wrong estimations were checked earlier");
        }
        return proof;
    }

    private static List<Expression> getEntireProof(Expression expression) {
        Set<Symbol> setOfNames = expression.getPropositionalVariables();
        List<Symbol> allPropositionalVariables = new ArrayList<>();
        for (Symbol element : setOfNames) {
            allPropositionalVariables.add(element);
        }
        Map<Symbol, Boolean> values = new HashMap<>();
        int numberOfVariables = allPropositionalVariables.size();
        List<List<Expression>> allProofs = new ArrayList<>();
        for (int mask = 0; mask < (1 << numberOfVariables); mask++) {
            for (int i = 0; i < numberOfVariables; i++) {
                int ithBit = (mask >> (numberOfVariables - i - 1)) & 1;
                Boolean value = Boolean.FALSE;
                if (ithBit != 0)
                    value = Boolean.TRUE;
                values.put(allPropositionalVariables.get(i), value);
            }
            allProofs.add(buildProofForFixedVariables(expression, values));
        }

        for (int iteration = 0; iteration < numberOfVariables; iteration++) {
            Symbol currentReducibleVariable = allPropositionalVariables.get(numberOfVariables - iteration - 1);
            List<List<Expression>> allProofs2 = new ArrayList<>();
            for (int setMask = 0; setMask < allProofs.size(); setMask += 2) {
                List<Expression> hypothesis = new ArrayList<>();
                for (int bit = 0; bit < numberOfVariables - iteration; bit++) {
                    int ithBit = (setMask >> (numberOfVariables - iteration - bit - 1)) & 1;
                    if (ithBit == 0)
                        hypothesis.add(new Negate(allPropositionalVariables.get(bit)));
                    else
                        hypothesis.add(allPropositionalVariables.get(bit));
                }
                List<Expression> proof1 = allProofs.get(setMask);
                List<Expression> proof2 = allProofs.get(setMask + 1);
                List<Expression> excludedVariableProof = excludeVariable(expression, hypothesis, currentReducibleVariable, proof1, proof2);
                allProofs2.add(excludedVariableProof);
            }
            allProofs = allProofs2;
        }
        List<Expression> entireProof = new ArrayList<>();
        for (List<Expression> arrayList : allProofs)
            entireProof.addAll(arrayList);
        return entireProof;
    }

    public static void main(String[] args) throws IOException, ParserException {
        try (
                Scanner in = new Scanner(new File("input.txt"));
                Scanner axiomsScanner = new Scanner(new File("axioms.txt"));
                Scanner bundleProofsScanner = new Scanner(new File("bundleProofs.txt"));
                Scanner exclusionOfThirdScanner = new Scanner(new File("exclusionOfThirdProof.txt"));
                PrintWriter out = new PrintWriter("output.txt");
        ) {
            readAxioms(axiomsScanner);
            readBundleProofs(bundleProofsScanner);
            readExclusionOfThird(exclusionOfThirdScanner);

            String initialExprString = in.nextLine();
            Expression initialExpression = parser.parse(initialExprString);

            Map<Symbol, Boolean> wrongEstimation = getWrongEstimation(initialExpression);
            if (wrongEstimation != null) {
                out.println("Высказывание ложно при");
                for (Symbol symbol : wrongEstimation.keySet()) {
                    Boolean value = wrongEstimation.get(symbol);
                    out.print(symbol + "=" + (value ? "И" : "Л") + " ");
                }
            } else {
                //it's provable
                List<Expression> entireProof = getEntireProof(initialExpression);
                for (Expression proofLine : entireProof) {
                    out.println(proofLine);
                }
            }
        } catch (ParserException | RuntimeException | IOException e) {
            System.err.println(e);
            e.printStackTrace();
        }
    }
}
