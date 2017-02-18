import exceptions.ParserException;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Main {

    private static class PairOfIndexes {
        int what;
        int where;

        PairOfIndexes(int what, int where) {
            this.what = what;
            this.where = where;
        }
    }

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

            ArrayList<Expression> list = new ArrayList<>();
            HashMap<Expression, PairOfIndexes> allSubstitutions = new HashMap<>();
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
                    if (allSubstitutions.containsKey(curExpr)) {
                        PairOfIndexes pair = allSubstitutions.get(curExpr);
                        out.println("(" + lineNumber + ") " + strExpr + " (" + "M.P. " + pair.what + ", " + pair.where + ")");
                    } else {
                        out.println("(" + lineNumber + ") " + strExpr + " (Не доказано)");
                    }
                }
                for (int i = 0; i < list.size(); i++) {
                    Expression tryMP = list.get(i).modusPonens(curExpr);
                    if (tryMP != null)
                        allSubstitutions.put(tryMP, new PairOfIndexes(lineNumber, i + 1));
                    tryMP = curExpr.modusPonens(list.get(i));
                    if (tryMP != null)
                        allSubstitutions.put(tryMP, new PairOfIndexes(i + 1, lineNumber));
                }
                list.add(curExpr);
            }

        } catch (ParserException | RuntimeException | IOException e) {
            System.err.println(e.getMessage());
        }
    }
}
