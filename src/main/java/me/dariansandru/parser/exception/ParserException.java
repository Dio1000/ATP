package me.dariansandru.parser.exception;

public class ParserException extends RuntimeException {
    public ParserException(String message, Throwable err) {
        super(message, err);
    }
}
