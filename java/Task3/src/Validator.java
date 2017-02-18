import exceptions.ParserException;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class Validator {

    public static void main(String[] args) throws IOException, ParserException {
        try (
                Scanner in = new Scanner(new File("input.txt"));
                PrintWriter out = new PrintWriter("output.txt");
        ) {
            String[] stringAxioms = {
                    "a -> b -> a",
                    "(a->b)->(a->b->c)->(a->c)",
                    "a->b->a&b",
                    "a&b->a",
                    "a&b->b",
                    "a->a|b",
                    "b->a|b",
                    "(a->c)->(b->c)->(a|b->c)",
                    "(a->b)->(a->!b)->!c",
                    "!!a->a",
            };

            ExpressionParser parser = new ExpressionParser();
            Expression[] axioms = new Expression[stringAxioms.length];
            for (int i = 0; i < stringAxioms.length; i++) {
                axioms[i] = parser.parse(stringAxioms[i]);
            }

            class PairOfIndexes {
                private int what;
                private int where;

                private PairOfIndexes(int what, int where) {
                    this.what = what;
                    this.where = where;
                }
            }

            HashMap<Expression, PairOfIndexes> allModusPonenses = new HashMap<>();
            HashMap<Expression, Integer> allExpressions = new HashMap<>();
            HashMap<Expression, HashMap<Expression, Integer>> allImplicationsStartsWith = new HashMap<>();
            int lineNumber = 0;
            while (in.hasNextLine()) {
                lineNumber++;
                String strExpr = in.nextLine();
                Expression curExpr = parser.parse(strExpr);
                boolean isAxiom = false;
                for (int i = 0; i < axioms.length; i++)
                    if (axioms[i].isBaseFor(curExpr)) {
                        out.println("(" + lineNumber + ") " + strExpr + " (" + "Сх. акс. " + (i + 1) + ")");
                        isAxiom = true;
                        break;
                    }
                if (!isAxiom) {
                    if (allModusPonenses.containsKey(curExpr)) {
                        PairOfIndexes pair = allModusPonenses.get(curExpr);
                        out.println("(" + lineNumber + ") " + strExpr + " (" + "M.P. " + pair.what + ", " + pair.where + ")");
                    } else {
                        out.println("(" + lineNumber + ") " + strExpr + " (Не доказано)");
                    }
                }
                if (!allExpressions.containsKey(curExpr)) {
                    if (allImplicationsStartsWith.containsKey(curExpr)) {
                        HashMap<Expression, Integer> allConsequences = allImplicationsStartsWith.get(curExpr);
                        for (Expression curConsequence : allConsequences.keySet()) {
                            int indexOfStatement = allConsequences.get(curConsequence);
                            allModusPonenses.put(curConsequence, new PairOfIndexes(lineNumber, indexOfStatement));
                        }
                    }
                    allExpressions.put(curExpr, lineNumber);
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
            }
        } catch (ParserException | RuntimeException | IOException e) {
            System.err.println(e.getMessage());
        }
    }
}