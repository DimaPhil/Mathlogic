package Hometask7.exceptions;

public class UnknownVariableName extends ParserException {
    public UnknownVariableName(String varName) {
        super("Illegal var name '" + varName + "'");
    }
}
