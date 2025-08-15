package me.dariansandru.io.exception;

public class InputException extends RuntimeException {
    public InputException(String message, Throwable err) {
        super(message, err);
    }
}
