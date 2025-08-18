package me.dariansandru.domain.proof.inference_rules.propositional;

import me.dariansandru.domain.logical_operator.Implication;
import me.dariansandru.domain.predicate.Predicate;
import me.dariansandru.domain.proof.inference_rules.InferenceRule;
import me.dariansandru.utils.data_structures.ast.AST;
import me.dariansandru.utils.data_structures.ast.PropositionalASTNode;

public class ModusPonens implements InferenceRule {

    private AST implicationAST = null;

    @Override
    public String getName() {
        return "Modus Ponens";
    }

    @Override
    public boolean canInference(AST... asts) {
        if (asts.length != 2) return false;

        if (((Predicate) ((PropositionalASTNode) asts[0].getRoot()).getKey()).getRepresentation().
                equals(new Implication().getRepresentation())) {
            implicationAST = asts[0];
        }
        else if (((Predicate) ((PropositionalASTNode) asts[1].getRoot()).getKey()).getRepresentation().
                equals(new Implication().getRepresentation())) {
            implicationAST = asts[1];
        }

        assert implicationAST != null;
        return implicationAST.getSubtree(0).isEquivalentTo(asts[1]);
    }

    @Override
    public AST inference(AST... asts) {
        return implicationAST.getSubtree(1);
    }

}
