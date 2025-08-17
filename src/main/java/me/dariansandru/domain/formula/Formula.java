package me.dariansandru.domain.formula;

import me.dariansandru.domain.UniverseOfDiscourse;
import me.dariansandru.utils.data_structures.ast.AST;

public interface Formula {
    String toString();
    UniverseOfDiscourse getUniverseOfDiscourse();

    Formula copy();
    Formula simplify();

    boolean equivalentTo(Formula otherFormula);
    AST getAST();
}
