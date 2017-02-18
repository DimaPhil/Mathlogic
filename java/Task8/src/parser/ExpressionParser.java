package parser;

import exceptions.IllegalCharacterException;
import exceptions.ParserException;

public class ExpressionParser {
    private String expression;
    private int position;

    private Token currentToken;
    private String currentString;

    public Expression parse(String string) throws ParserException {
        if (string == null || string.isEmpty()) {
            throw new ParserException("Empty expression");
        }
        expression = string;
        position = 0;
        currentToken = nextToken();
        return readEquation();
    }

    private enum Token {
        OPEN,
        CLOSE,
        LOWER_LETTER,
        DIGIT,
        PLUS,
        MULTIPLY,
        EQUALS,
        POWER,
        END,
    }

    private boolean isDigit(char c) {
        return '0' <= c && c <= '9';
    }

    private boolean isLowerLetter(char c) {
        return 'a' <= c && c <= 'z';
    }

    private Token nextToken() throws IllegalCharacterException {
        while (Character.isWhitespace(currentChar())) position++;
        if (position >= expression.length()) {
            return Token.END;
        }
        char oldChar = currentChar();
        position++;
        switch (oldChar) {
            case '(':
                return Token.OPEN;
            case ')':
                return Token.CLOSE;
            case '*':
                return Token.MULTIPLY;
            case '+':
                return Token.PLUS;
            case '=':
                return Token.EQUALS;
            case '^':
                return Token.POWER;
            default: {
                if (isLowerLetter(oldChar) || isDigit(oldChar)) {
                    currentString = String.valueOf(oldChar);
                    while (isLowerLetter(currentChar()) || isDigit(currentChar())) {
                        currentString += currentChar();
                        position++;
                    }
                    if (isLowerLetter(oldChar)) {
                        return Token.LOWER_LETTER;
                    } else {
                        return Token.DIGIT;
                    }
                }
                throw new IllegalCharacterException(position);
            }
        }
    }

    private char currentChar() {
        //$ - impossible symbol in parsing
        return position >= expression.length() ? '$' : expression.charAt(position);
    }

    private Expression readTerm() throws ParserException {
        //(Терм) = 'w' | {0..9}+ | '(' выражение ')
        if (currentToken == Token.LOWER_LETTER) {
            if (currentString.equals("w")) {
                currentToken = nextToken();
                return new OmegaVariable();
            } else {
                throw new IllegalCharacterException(position);
            }
        } else if (currentToken == Token.OPEN) {
            currentToken = nextToken();
            Expression result = readExpression();
            if (currentToken != Token.CLOSE) {
                throw new IllegalCharacterException(position);
            }
            currentToken = nextToken();
            return result;
        } else if (currentToken == Token.DIGIT) {
            Expression result = new Number(currentString);
            currentToken = nextToken();
            return result;
        } else {
            throw new IllegalCharacterException(position);
        }
    }

    private Expression readMultiplier() throws ParserException {
        //Умножаемое = терм | терм '^' умножаемое
        Expression result = readTerm();
        while (currentToken == Token.POWER) {
            currentToken = nextToken();
            Expression cur = readMultiplier();
            result = new Power(result, cur);
        }
        return result;
    }

    private Expression readSummary() throws ParserException {
        //(Слагаемое) = (Умножаемое) | (Слагаемое) '*' (Умножаемое)
        Expression result = readMultiplier();
        while (currentToken == Token.MULTIPLY) {
            currentToken = nextToken();
            Expression cur = readMultiplier();
            result = new Multiply(result, cur);
        }
        return result;
    }

    private Expression readExpression() throws ParserException {
        //Выражение = слагаемое | выражение '+' слагаемое
        Expression result = readSummary();
        while (currentToken == Token.PLUS) {
            currentToken = nextToken();
            Expression cur = readSummary();
            result = new Sum(result, cur);
        }
        return result;
    }

    private Expression readEquation() throws ParserException {
        //Уравнение = Выражение '=' Выражение
        Expression left = readExpression();
        if (currentToken != Token.EQUALS) {
            throw new ParserException("Error parsing equation", position);
        }
        currentToken = nextToken();
        Expression right = readExpression();
        if (currentToken != Token.CLOSE && currentToken != Token.END) {
            throw new ParserException("Parsing error of string \n" + expression + "\n", position);
        }
        return new Equals(left, right);
    }

}
