import exceptions.ParserException;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class Main {
    private static List<Expression> getNewProof(List<Expression> oldProof, Expression[] axioms,
                                                List<Expression> hypothesis, Expression reducible) {
        class PairOfIndexes {
            private int what;
            private int where;
            private PairOfIndexes(int what, int where) {
                this.what = what;
                this.where = where;
            }
        }
        List<Expression> newProof = new ArrayList<>();
        Map<Expression, PairOfIndexes> allSubstitutions = new HashMap<>();
        for (int lineNumber = 0; lineNumber < oldProof.size(); lineNumber++) {
            Expression curExpr = oldProof.get(lineNumber);
            boolean detected = false;
            if (curExpr.equals(reducible)) {
                //proof of a->a
//                    a->a->a
//                    (a->a->a) -> (a->(a->a)->a) -> (a->a)
//                    (a->(a->a)->a)->(a->a)
//                    a->(a->a)->a
//                    a->a
//                };
                Expression a_a = new Implication(curExpr, curExpr);
                Expression aa_a = new Implication(a_a, curExpr);
                Expression a_aa = new Implication(curExpr, a_a);
                newProof.add(new Implication(curExpr, a_a));
                newProof.add(new Implication(a_aa, new Implication(new Implication(curExpr, aa_a), a_a)));
                newProof.add(new Implication(curExpr, aa_a));
                newProof.add(new Implication(new Implication(curExpr, aa_a), a_a));
                newProof.add(a_a);
                detected = true;
            }
            if (!detected) {
                Expression origin = null;
                for (Expression axiom : axioms)
                    if (axiom.isBaseFor(curExpr)) {
                        origin = curExpr;
                        detected = true;
                        break;
                    }
                for (Expression hypothesi : hypothesis) {
                    if (hypothesi.equals(curExpr)) {
                        origin = hypothesi;
                        detected = true;
                        break;
                    }
                }
                if (detected) {
                    //Bi
                    //Bi->a->Bi
                    //MP: a->Bi
                    newProof.add(origin);
                    newProof.add(new Implication(origin, new Implication(reducible, origin)));
                    newProof.add(new Implication(reducible, origin));
                }
            }
            if (!detected) {
                //MP
                //1) (a->Bj) -> (a->(Bj->Bi)) -> (a->Bi)
                //2) (a->(Bj->Bi)) -> (a->Bi)
                if (allSubstitutions.containsKey(curExpr)) {
                    PairOfIndexes pair = allSubstitutions.get(curExpr);
                    Expression Bj = oldProof.get(pair.what);
                    Expression Bk = oldProof.get(pair.where);
                    newProof.add(new Implication(new Implication(reducible, Bj), new Implication(new Implication(reducible, Bk), new Implication(reducible, curExpr))));
                    newProof.add(new Implication(new Implication(reducible, Bk), new Implication(reducible, curExpr)));
                    newProof.add(new Implication(reducible, curExpr));
                }
            }
            for (int i = 0; i < lineNumber; i++) {
                Expression tryMP = oldProof.get(i).modusPonens(curExpr);
                if (tryMP != null) {
                    allSubstitutions.put(tryMP, new PairOfIndexes(lineNumber, i));
                }
                tryMP = curExpr.modusPonens(oldProof.get(i));
                if (tryMP != null) {
                    allSubstitutions.put(tryMP, new PairOfIndexes(i, lineNumber));
                }
            }
        }
        return newProof;
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

            String header = in.nextLine();
            String[] halves = header.split("\\|\\-");
            String[] hypothesisStrings = halves[0].split(",");
            String demonstrableString = halves[1];

            Expression demonstrable = parser.parse(demonstrableString);
            List<Expression> hypothesis = new ArrayList<>(hypothesisStrings.length);
            for (String hypothesisString : hypothesisStrings) {
                hypothesis.add(parser.parse(hypothesisString));
            }

            List<Expression> oldProof = new ArrayList<>();
            while (in.hasNextLine()) {
                String strExpr = in.nextLine();
                Expression curExpr = parser.parse(strExpr);
                oldProof.add(curExpr);
            }

            List<Expression> newProof;
            List<String> hList = Arrays.asList(hypothesisStrings);
            out.println(String.join(",", hList.subList(0, hList.size() - 1)) + "|-" +
                                    "(" + hList.get(hList.size() - 1) + ")->(" + demonstrableString + ")");
            while (!hypothesis.isEmpty()) {
                Expression reducible = hypothesis.get(hypothesis.size() - 1);
                hypothesis.remove(hypothesis.size() - 1);
                newProof = getNewProof(oldProof, axioms, hypothesis, reducible);
                //out.println("------------------------");
                for (Expression aNewProof : newProof) {
                    out.println(aNewProof);
                }
                oldProof = newProof;
            }
        } catch (ParserException | RuntimeException | IOException e) {
            System.err.println(e.getMessage());
        }
    }
}
