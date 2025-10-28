package me.dariansandru.domain.data_structures.ast;

import me.dariansandru.domain.language.interpretation.Interpretation;

public interface AST {
    String toString();

    AST copy();
    AST simplify();

    boolean validate(int line);
    void negate();
    Object evaluate(Interpretation interpretation);

    boolean isEquivalentTo(AST other);
    boolean hasSameStructure(AST other);
    boolean isEmpty();

    boolean isContradiction();
    void checkContradiction();
    boolean isTautology();
    void checkTautology();

    void moveLeft();
    void moveRight();
    void moveUp();

    Object getRoot();
    AST getSubtree(int childIndex);
}
