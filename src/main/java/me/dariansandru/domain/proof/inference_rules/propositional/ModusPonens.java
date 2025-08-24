package me.dariansandru.domain.proof.inference_rules.propositional;

import me.dariansandru.domain.logical_operator.Implication;
import me.dariansandru.domain.predicate.Predicate;
import me.dariansandru.domain.proof.inference_rules.InferenceRule;
import me.dariansandru.utils.data_structures.ast.AST;
import me.dariansandru.utils.data_structures.ast.PropositionalASTNode;

import java.util.List;

public class ModusPonens implements InferenceRule {

    private AST implicationAST = null;

    @Override
    public String getName() {
        return "Modus Ponens";
    }

    @Override
    public boolean canInference(List<AST> asts) {
        implicationAST = null;

        for (AST candidate : asts) {
            if (!(candidate.getRoot() instanceof PropositionalASTNode)) continue;

            Predicate predicate = (Predicate) ((PropositionalASTNode) candidate.getRoot()).getKey();
            if (predicate == null) continue;

            if (predicate.getRepresentation().equals(new Implication().getRepresentation())) {
                AST antecedent = candidate.getSubtree(0);

                for (AST other : asts) {
                    if (other != candidate && antecedent.isEquivalentTo(other)) {
                        implicationAST = candidate;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public AST inference(List<AST> asts) {
        assert implicationAST != null;
        return implicationAST.getSubtree(1);
    }
}
