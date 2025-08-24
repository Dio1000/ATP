package me.dariansandru.domain.proof.inference_rules.propositional;

import me.dariansandru.domain.logical_operator.Implication;
import me.dariansandru.domain.predicate.Predicate;
import me.dariansandru.domain.proof.inference_rules.InferenceRule;
import me.dariansandru.utils.data_structures.ast.AST;
import me.dariansandru.utils.data_structures.ast.PropositionalASTNode;

import java.util.List;

public class ModusTollens implements InferenceRule {

    private AST implicationAST = null;

    @Override
    public String getName() {
        return "Modus Tollens";
    }

    @Override
    public boolean canInference(List<AST> asts) {
        implicationAST = null;

        for (AST candidate : asts) {
            if (!(candidate.getRoot() instanceof PropositionalASTNode)) continue;

            Predicate predicate = (Predicate) ((PropositionalASTNode) candidate.getRoot()).getKey();
            if (predicate == null) continue;

            if (predicate.getRepresentation().equals(new Implication().getRepresentation())) {
                AST consequent = candidate.getSubtree(1);
                AST negatedConsequent = consequent.copy();
                negatedConsequent.negate();

                for (AST other : asts) {
                    if (other != candidate && negatedConsequent.isEquivalentTo(other)) {
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

        AST antecedent = implicationAST.getSubtree(0).copy();
        antecedent.negate();
        return antecedent;
    }
}
