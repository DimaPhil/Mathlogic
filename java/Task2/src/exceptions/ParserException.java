package exceptions;

public class ParserException extends Exception {
    ParserException(String s) {
        super(s);
    }

    public ParserException(String message, int position) {
        super(message + " at position " + position);
    }
}
