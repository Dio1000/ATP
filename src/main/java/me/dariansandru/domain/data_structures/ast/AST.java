package me.dariansandru.domain.data_structures.ast;

public interface AST {
    String toString();

    AST copy();
    AST simplify();
    void negate();

    boolean validate(int line);
    Object evaluate();

    boolean isEquivalentTo(AST other);
    boolean isEmpty();
    boolean isContradiction();

    void moveLeft();
    void moveRight();
    void moveUp();

    Object getRoot();
    AST getSubtree(int childIndex);
}
