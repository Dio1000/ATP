package me.dariansandru.domain.proof.inference_rules.propositional;

import me.dariansandru.domain.LogicalOperator;
import me.dariansandru.domain.logical_operator.Implication;
import me.dariansandru.domain.predicate.Predicate;
import me.dariansandru.domain.proof.SubGoal;
import me.dariansandru.domain.proof.inference_rules.InferenceRule;
import me.dariansandru.utils.data_structures.ast.AST;
import me.dariansandru.utils.data_structures.ast.PropositionalAST;
import me.dariansandru.utils.data_structures.ast.PropositionalASTNode;
import me.dariansandru.utils.helper.PropositionalLogicHelper;

import java.util.ArrayList;
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

    @Override
    public List<SubGoal> getSubGoals(List<AST> knowledgeBase, AST... asts) {
        if (asts.length != 1) return new ArrayList<>();
        List<SubGoal> subGoals = new ArrayList<>();

        for (AST ast : knowledgeBase) {
            if (!(ast instanceof PropositionalAST)) continue;
            if (PropositionalLogicHelper.getOutermostOperation(ast) == LogicalOperator.IMPLICATION) {
                PropositionalAST right = (PropositionalAST) ast.getSubtree(1);
                if (right.isEquivalentTo(asts[0])) {
                    PropositionalAST newGoal = (PropositionalAST) ast.getSubtree(0);
                    newGoal.validate(0);
                    SubGoal newSubGoal = new SubGoal(newGoal, PropositionalInferenceRule.MODUS_PONENS, ast);
                    subGoals.add(newSubGoal);
                }
            }
        }

        return subGoals;
    }

    @Override
    public String getText(SubGoal subGoal) {
        return "From " + subGoal.getGoal() + " and " + subGoal.getFormula() + ", by Modus Ponens, " + "we derive " + subGoal.getFormula().getSubtree(1);
    }
}
