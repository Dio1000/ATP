package me.dariansandru.io.exception;

public class OutputException extends RuntimeException {
    public OutputException(String message, Throwable err) {
        super(message, err);
    }
}
