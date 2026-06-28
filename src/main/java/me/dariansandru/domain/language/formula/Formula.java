package me.dariansandru.domain.language.formula;

import me.dariansandru.domain.language.UniverseOfDiscourse;
import me.dariansandru.domain.data_structures.ast.AST;

/**
Implementing this interface allows the user to extend the system with a new type of formula.
This allows for extending the new Universes of Discourse with their own types of formulas,
which are used for manipulating data in proofs.
 **/
public interface Formula {
    String toString();
    UniverseOfDiscourse getUniverseOfDiscourse();

    Formula copy();
    Formula simplify();

    boolean equivalentTo(Formula otherFormula);
    AST getAST();
}
