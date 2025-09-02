package me.dariansandru.domain.formula;

import me.dariansandru.domain.UniverseOfDiscourse;
import me.dariansandru.domain.data_structures.ast.AST;
import me.dariansandru.domain.data_structures.ast.PropositionalAST;

public class PropositionalFormula implements Formula {

    private final String formula;
    private final PropositionalAST ast;

    public PropositionalFormula(String formula) {
        this.formula = formula;
        this.ast = new PropositionalAST(formula);
    }

    @Override
    public String toString() {
        return formula;
    }

    @Override
    public UniverseOfDiscourse getUniverseOfDiscourse() {
        return null;
    }

    @Override
    public Formula copy() {
        return null;
    }

    @Override
    public boolean equivalentTo(Formula otherFormula) {
        return false;
    }

    @Override
    public AST getAST() {
        return ast;
    }

    @Override
    public Formula simplify() {
        return null;
    }
}
