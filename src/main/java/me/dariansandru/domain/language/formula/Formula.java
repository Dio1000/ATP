package me.dariansandru.domain.language.formula;

import me.dariansandru.domain.language.UniverseOfDiscourse;
import me.dariansandru.domain.data_structures.ast.AST;

public interface Formula {
    String toString();
    UniverseOfDiscourse getUniverseOfDiscourse();

    Formula copy();
    Formula simplify();

    boolean equivalentTo(Formula otherFormula);
    AST getAST();
}
