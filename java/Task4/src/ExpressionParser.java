import exceptions.IllegalCharacterException;
import exceptions.ParserException;

import java.util.ArrayList;

class ExpressionParser {
    private String expression;
    private int position;

    private Token currentToken;
    private String currentString;

    private boolean isPlaceHolder = false;

    private Expression makeParsing(String string) throws ParserException {
        if (string == null || string.isEmpty()) {
            return new EmptyExpression();
        }
        expression = string;
        position = 0;
        currentToken = nextToken();
        return readExpression();
    }

    Expression parse(String string) throws ParserException {
        isPlaceHolder = false;
        return makeParsing(string);
    }

    Expression parseAsPlaceHolder(String string) throws ParserException {
        isPlaceHolder = true;
        return makeParsing(string);
    }

    private enum Token {
        OPEN,
        CLOSE,
        AND,
        OR,
        NOT,
        ARROW,
        LOWER_LETTER,
        UPPER_LETTER,
        PLUS,
        MULTIPLY,
        ANY,
        EXISTS,
        ZERO,
        COMMA,
        EQUALS,
        SINGLE_QUOTE,
        END,
    }

    private boolean isDigit(char c) {
        return '0' <= c && c <= '9';
    }

    private boolean isLowerLetter(char c) {
        return 'a' <= c && c <= 'z';
    }

    private boolean isUpperLetter(char c) {
        return 'A' <= c && c <= 'Z';
    }

    private Token nextToken() throws IllegalCharacterException {
        while (Character.isWhitespace(currentChar())) position++;
        if (position >= expression.length()) return Token.END;
        char oldChar = currentChar();
        position++;
        switch (oldChar) {
            case '(':
                return Token.OPEN;
            case ')':
                return Token.CLOSE;
            case '&':
                return Token.AND;
            case '|':
                return Token.OR;
            case '!':
                return Token.NOT;
            case '@':
                return Token.ANY;
            case '?':
                return Token.EXISTS;
            case '*':
                return Token.MULTIPLY;
            case '+':
                return Token.PLUS;
            case ',':
                return Token.COMMA;
            case '=':
                return Token.EQUALS;
            case '0':
                return Token.ZERO;
            case '\'':
                return Token.SINGLE_QUOTE;
            case '-': {
                if (currentChar() != '>')
                    throw new IllegalCharacterException(position);
                position++;
                return Token.ARROW;
            }
            default: {
                if (isLowerLetter(oldChar)) {
                    currentString = String.valueOf(oldChar);
                    while (isLowerLetter(currentChar()) || isDigit(currentChar())) {
                        currentString += currentChar();
                        position++;
                    }
                    return Token.LOWER_LETTER;
                }
                if (isUpperLetter(oldChar)) {
                    currentString = String.valueOf(oldChar);
                    while (isUpperLetter(currentChar()) || isDigit(currentChar())) {
                        currentString += currentChar();
                        position++;
                    }
                    return Token.UPPER_LETTER;
                }
                throw new IllegalCharacterException(position);
            }
        }
    }

    private char currentChar() {
        //$ - impossible symbol in parsing
        return position >= expression.length() ? '$' : expression.charAt(position);
    }

    private Expression readMultiplier() throws ParserException {
        //('a'..'z') {'0'..'9'}* '('терм {',' терм}* ')' | (Переменная) | '('терм')' | '0' | (умножаемое) "'"  (тут штрих)
        Expression result;
        if (currentToken == Token.LOWER_LETTER) {
            String functionName = currentString;
            currentToken = nextToken();
            ArrayList<Expression> terms = new ArrayList<>();
            if (currentToken != Token.OPEN) {
                //It's variable, because we have not encountered an open bracket
                result = new Variable(functionName);
            } else {
                currentToken = nextToken();
                Expression term = readTerm();
                terms.add(term);
                while (currentToken == Token.COMMA) {
                    currentToken = nextToken();
                    term = readTerm();
                    terms.add(term);
                }
                if (currentToken != Token.CLOSE) {
                    throw new IllegalCharacterException(position);
                }
                currentToken = nextToken();
                result = new Function(functionName, terms);
            }
        } else if (currentToken == Token.ZERO) {
            currentToken = nextToken();
            result = new Zero();
        } else if (currentToken == Token.OPEN) {
            currentToken = nextToken();
            result = readTerm();
            if (currentToken != Token.CLOSE) {
                throw new IllegalCharacterException(position);
            }
            currentToken = nextToken();
        } else {
            //There are no the other variants
            throw new IllegalCharacterException(position);
        }
        while (currentToken == Token.SINGLE_QUOTE) {
            currentToken = nextToken();
            result = new Quote(result);
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

    private Expression readTerm() throws ParserException {
        //(Терм) = (Слагаемое) | (Терм) '+' (Слагамое)
        Expression result = readSummary();
        while (currentToken == Token.PLUS) {
            currentToken = nextToken();
            Expression cur = readSummary();
            result = new Sum(result, cur);
        }
        return result;
    }

    private Expression readVariable() throws ParserException {
        //('a'..'z'){'0'..'9'}*
        if (currentToken != Token.LOWER_LETTER)
            throw new IllegalCharacterException(position);
        String varName = currentString;
        currentToken = nextToken();
        return new Variable(varName);
    }

    private Expression readPredicate() throws ParserException {
        //('A'..'Z') {'0'..'9'}* ['('терм {','терм }* ')'] | (терм) '=' (терм)
        if (currentToken == Token.UPPER_LETTER) {
            String predicateName = currentString;
            currentToken = nextToken();
            ArrayList<Expression> terms = new ArrayList<>();
            if (currentToken == Token.OPEN) {
                currentToken = nextToken();
                Expression term = readTerm();
                terms.add(term);
                while (currentToken == Token.COMMA) {
                    currentToken = nextToken();
                    term = readTerm();
                    terms.add(term);
                }
                if (currentToken != Token.CLOSE)
                    throw new IllegalCharacterException(position);
                currentToken = nextToken();
            }
            return new Predicate(predicateName, terms);
        } else {
            Expression firstTerm = readTerm();
            if (currentToken != Token.EQUALS)
                throw new IllegalCharacterException(position);
            currentToken = nextToken();
            Expression secondTerm = readTerm();
            return new Equals(firstTerm, secondTerm);
        }
    }

    private boolean isPredicate() {
        //(Предикат) = ('A'..'Z') {'0'..'9'}* ['('терм {','терм }* ')'] | (терм) '=' (терм)
        if (currentToken == Token.UPPER_LETTER) {
            return true;
        }
        int nextEqualsIndex = expression.indexOf('=', position);
        if (nextEqualsIndex == -1) {
            return false;
        }
        String subExpression = expression.substring(position, nextEqualsIndex);
        int balance = currentToken == Token.OPEN ? 1 : 0;
        for (int pos = 0; pos < subExpression.length(); pos++) {
            if (subExpression.charAt(pos) == '(') {
                balance++;
            }
            else if (subExpression.charAt(pos) == ')') {
                balance--;
                if (balance < 0) {
                    return false;
                }
            }
        }
        return balance == 0;
    }

    private Expression readUnary() throws ParserException {
        //If placeHolder mode ON, then it has the following grammar rules
        //(Унарное) = ('a'...'z') {'0'..'9'} | '!'(унарное) | '('выражение')'
        if (isPlaceHolder) {
            Expression result;
            if (currentToken == Token.LOWER_LETTER) {
                String variableName = currentString;
                currentToken = nextToken();
                result = new Variable(variableName);
            } else if (currentToken == Token.NOT) {
                currentToken = nextToken();
                Expression unary = readUnary();
                result = new Negate(unary);
            } else if (currentToken == Token.OPEN) {
                currentToken = nextToken();
                result = readExpression();
                if (currentToken != Token.CLOSE)
                    throw new IllegalCharacterException(position);
                currentToken = nextToken();
            } else {
                throw new IllegalCharacterException(position);
            }
            return result;
        }
        //(Унарное) = (предикат) | '!'(унарное) | '('выражение')' | ('@' | '?') (переменная) (унарное)
        Expression result;
        if (currentToken == Token.NOT) {
            currentToken = nextToken();
            Expression unary = readUnary();
            result = new Negate(unary);
        } else if (currentToken == Token.ANY) {
            currentToken = nextToken();
            Expression variable = readVariable();
            Expression unary = readUnary();
            result = new ForAllQuantifier(variable, unary);
        } else if (currentToken == Token.EXISTS) {
            currentToken = nextToken();
            Expression variable = readVariable();
            Expression unary = readUnary();
            result = new ExistsQuantifier(variable, unary);
        } else if (isPredicate()) {
            result = readPredicate();
        } else if (currentToken == Token.OPEN) {
            currentToken = nextToken();
            result = readExpression();
            if (currentToken != Token.CLOSE)
                throw new IllegalCharacterException(position);
            currentToken = nextToken();
        } else {
            result = readPredicate();
        }
        return result;
    }


    private Expression readConjunction() throws ParserException {
        //(конъюнкция) ::= (унарное) | (конъюнкция) ‘&’ (унарное)
        Expression result = readUnary();
        while (currentToken == Token.AND) {
            currentToken = nextToken();
            Expression cur = readUnary();
            result = new Conjunction(result, cur);
        }
        return result;
    }

    private Expression readDisjunction() throws ParserException {
        //(дизъюнкция) ::= (конъюнкция) | (дизъюнкция) ‘|’ (конъюнкция)
        Expression result = readConjunction();
        while (currentToken == Token.OR) {
            currentToken = nextToken();
            Expression cur = readConjunction();
            result = new Disjunction(result, cur);
        }
        return result;
    }

    private Expression readExpression() throws ParserException {
        //(выражение)  ::= (дизъюнкция) | (дизъюнкция) ‘->’ (выражение)
        Expression result = readDisjunction();
        while (currentToken == Token.ARROW) {
            currentToken = nextToken();
            Expression cur = readExpression();
            result = new Implication(result, cur);
        }
        if (currentToken != Token.CLOSE && currentToken != Token.END) {
            throw new ParserException("Parsing error of string \n" + expression + "\n", position);
        }
        return result;
    }
}
