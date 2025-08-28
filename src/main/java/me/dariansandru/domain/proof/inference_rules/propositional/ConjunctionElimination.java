package me.dariansandru.domain.proof.inference_rules.propositional;

import me.dariansandru.domain.logical_operator.Conjunction;
import me.dariansandru.domain.predicate.Predicate;
import me.dariansandru.domain.proof.SubGoal;
import me.dariansandru.domain.proof.inference_rules.InferenceRule;
import me.dariansandru.utils.data_structures.ast.AST;
import me.dariansandru.utils.data_structures.ast.PropositionalASTNode;

import java.util.List;

public class ConjunctionElimination implements InferenceRule {

    private AST derived = null;

    @Override
    public String getName() {
        return "Conjunction Elimination";
    }

    @Override
    public boolean canInference(List<AST> asts) {
        derived = null;

        for (AST ast : asts) {
            PropositionalASTNode node = (PropositionalASTNode) ast.getRoot();
            if (node == null || node.getKey() == null) continue;

            Predicate predicate = (Predicate) node.getKey();
            if (!predicate.getRepresentation().equals(new Conjunction().getRepresentation())) continue;

            AST left = ast.getSubtree(0);
            AST right = ast.getSubtree(1);

            boolean hasLeft = contains(asts, left);
            boolean hasRight = contains(asts, right);

            if (!hasLeft)
            {
                derived = left;
                return true;
            }
            if (!hasRight)
            {
                derived = right;
                return true;
            }
        }
        return false;
    }

    @Override
    public AST inference(List<AST> asts) {
        return derived;
    }

    @Override
    public List<SubGoal> getSubGoals(List<AST> knowledgeBase, AST... asts) {
        return List.of();
    }

    private boolean contains(List<AST> asts, AST other) {
        for (AST ast : asts) {
            if (ast.isEquivalentTo(other)) return true;
        }
        return false;
    }
}
