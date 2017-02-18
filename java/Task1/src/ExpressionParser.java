import exceptions.IllegalCharacterException;
import exceptions.ParserException;

class ExpressionParser {
    private String expression;
    private int position;

    private Token currentToken;
    private String currentString;

    Expression parse(String string) throws ParserException {
        expression = string;
        position = 0;
        currentToken = nextToken();
        return readExp();
    }

    private enum Token {
        OPEN,
        CLOSE,
        AND,
        OR,
        NOT,
        ARROW,
        LETTER,
        END,
    }

    private boolean isDigit(char c) { return '0' <= c && c <= '9'; }

    private boolean isLetter(char c) {
        return 'a' <= c && c <= 'z' || 'A' <= c && c <= 'Z';
    }

    private Token nextToken() throws IllegalCharacterException {
        while (Character.isWhitespace(currentChar())) position++;
        if (position >= expression.length()) return Token.END;
        char oldChar = currentChar();
        position++;
        switch (oldChar) {
            case '(': return Token.OPEN;
            case ')': return Token.CLOSE;
            case '&': return Token.AND;
            case '|': return Token.OR;
            case '!': return Token.NOT;
            case '-': {
                if (currentChar() != '>')
                    throw new IllegalCharacterException(position);
                position++;
                return Token.ARROW;
            }
            default: {
                if (isLetter(oldChar)) {
                    currentString = String.valueOf(oldChar);
                    while (isLetter(currentChar()) || isDigit(currentChar())) {
                        currentString += currentChar();
                        position++;
                    }
                    return Token.LETTER;
                } else
                    throw new IllegalCharacterException(position);
            }
        }
    }

    private char currentChar() {
        //@ - impossible symbol in parsing
        return position >= expression.length() ? '@' : expression.charAt(position);
    }

    private Expression readNegation() throws ParserException {
        //⟨отрицание⟩ ::= (‘A’...‘Z’) {‘0’...‘9’} | ‘!’ ⟨отрицание⟩ | ‘(’ ⟨выражение⟩ ‘)’
        Expression result;
        if (currentToken == Token.LETTER) {
            String varName = currentString;
            currentToken = nextToken();
            result = new Symbol(varName);
        } else if (currentToken == Token.NOT) {
            currentToken = nextToken();
            Expression cur = readNegation();
            result = new Negate(cur);
        } else if (currentToken == Token.OPEN) {
            currentToken = nextToken();
            result = readExp();
            if (currentToken != Token.CLOSE)
                throw new IllegalCharacterException(position);
            currentToken = nextToken();
        }
        else
            throw new IllegalCharacterException(position);
        return result;
    }

    private Expression readConjunction() throws ParserException {
        //⟨конъюнкция⟩ ::= ⟨отрицание⟩ | ⟨конъюнкция⟩ ‘&’ ⟨отрицание⟩
        Expression result = readNegation();
        while (currentToken == Token.AND) {
            currentToken = nextToken();
            Expression cur = readNegation();
            result = new And(result, cur);
        }
        return result;
    }

    private Expression readDisjunction() throws ParserException {
        //⟨дизъюнкция⟩ ::= ⟨конъюнкция⟩ | ⟨дизъюнкция⟩ ‘|’ ⟨конъюнкция⟩
        Expression result = readConjunction();
        while (currentToken == Token.OR) {
            currentToken = nextToken();
            Expression cur = readConjunction();
            result = new Or(result, cur);
        }
        return result;
    }

    private Expression readExp() throws ParserException {
        //⟨выражение⟩  ::= ⟨дизъюнкция⟩ | ⟨дизъюнкция⟩ ‘->’ ⟨выражение⟩
        Expression result = readDisjunction();
        while (currentToken == Token.ARROW) {
            currentToken = nextToken();
            Expression cur = readExp();
            result = new Implication(result, cur);
        }
        if (currentToken != Token.CLOSE && currentToken != Token.END)
            throw new ParserException("Parsing error ", position);
        return result;
    }
}