package me.dariansandru.domain.data_structures.ast;

import me.dariansandru.domain.language.interpretation.Interpretation;

/**
Implementing this interface allows the user to create a new type of AST.
This is to be used for adding new Universes of Discourse (e.g. Integers, Strings), which
require different parsing, validation and other operations.
 **/
public interface AST {
    String toString();

    boolean validate(int line);
    void negate();
    Object evaluate(Interpretation interpretation);

    boolean isEquivalentTo(AST other);
    boolean isSameFormula(AST other);
    boolean hasSameStructure(AST other);
    boolean isEmpty();

    boolean isContradiction();
    void checkContradiction();
    boolean isTautology();
    void checkTautology();

    AST copy();
    AST simplify();

    void moveLeft();
    void moveRight();
    void moveUp();

    Object getRoot();
    AST getSubtree(int childIndex);

    int getLength();
}
