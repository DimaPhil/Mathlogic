package Hometask7;

import Hometask7.hierarchy.Expression;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        try (Scanner input = new Scanner(new File("input.txt"));
             PrintWriter output = new PrintWriter(new File("output.txt"))) {
            int n = input.nextInt();
            int m = input.nextInt();
            if (m == 0) {
                throw new IllegalArgumentException("Can't check division by zero");
            }
            if (n % m == 0) {
                eraseDuplicates(new DivisionProof().divides(m, n).proof).stream().forEach(output::println);
            } else {
                eraseDuplicates(new DivisionProof().notDivides(m, n).proof).stream().forEach(output::println);
            }
        } catch (IOException e) {
            System.out.println("Incorrect input, usage: <integer> <integer> on one line");
            System.out.println(e.toString());
        }
    }

    private static List<Expression> eraseDuplicates(List<Expression> lst) {
        Set<Expression> was = new HashSet<>();
        List<Expression> result = new ArrayList<>();
        for (Expression expression : lst) {
            if (!was.contains(expression)) {
                was.add(expression);
                result.add(expression);
            }
        }
        return result;
    }
}
