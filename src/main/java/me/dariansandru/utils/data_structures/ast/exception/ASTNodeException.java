package me.dariansandru.utils.data_structures.ast.exception;

public class ASTNodeException extends RuntimeException {
    public ASTNodeException(String message, Throwable err) {
        super(message, err);
    }
    public ASTNodeException(String message) {
        super(message);
    }
}
