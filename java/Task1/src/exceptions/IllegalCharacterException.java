package exceptions;

public class IllegalCharacterException extends ParserException {
    public IllegalCharacterException(int position) {
        super("Illegal character", position);
    }
}
