package me.dariansandru.domain.data_structures.ast.exception;

public class ASTException extends RuntimeException {
    public ASTException(String message, Throwable err) {
        super(message, err);
    }
    public ASTException(String message) {
        super(message);
    }
}
