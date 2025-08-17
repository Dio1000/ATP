package me.dariansandru.utils.data_structures.ast;

public interface AST {
    String toString();

    AST copy();
    AST simplify();

    boolean validate(int line);
    Object evaluate();
    boolean isEquivalentTo(AST other);

    void moveLeft();
    void moveRight();
    void moveUp();

    Object getRoot();
    AST getSubtree(int childIndex);
}
