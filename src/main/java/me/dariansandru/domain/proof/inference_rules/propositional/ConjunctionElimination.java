package me.dariansandru.domain.proof.inference_rules.propositional;

import me.dariansandru.domain.LogicalOperator;
import me.dariansandru.domain.logical_operator.Conjunction;
import me.dariansandru.domain.predicate.Predicate;
import me.dariansandru.domain.proof.SubGoal;
import me.dariansandru.domain.proof.inference_rules.InferenceRule;
import me.dariansandru.utils.data_structures.ast.AST;
import me.dariansandru.utils.data_structures.ast.PropositionalAST;
import me.dariansandru.utils.data_structures.ast.PropositionalASTNode;
import me.dariansandru.utils.helper.PropositionalLogicHelper;

import java.util.ArrayList;
import java.util.List;

public class ConjunctionElimination implements InferenceRule {

    private AST derived = null;
    private List<AST> derivedList = new ArrayList<>();

    @Override
    public String getName() {
        return "Conjunction Elimination";
    }

    @Override
    public boolean canInference(List<AST> asts, AST goal) {
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
    public List<AST> inference(List<AST> asts, AST goal) {
        return derivedList;
    }

    @Override
    public List<SubGoal> getSubGoals(List<AST> knowledgeBase, AST... asts) {
        if (asts.length != 1) return new ArrayList<>();
        List<SubGoal> subGoals = new ArrayList<>();

        for (AST ast : knowledgeBase) {
            if (!(ast instanceof PropositionalAST)) continue;
            if (PropositionalLogicHelper.getOutermostOperation(ast) == LogicalOperator.CONJUNCTION) {
                SubGoal subGoal = new SubGoal(ast, PropositionalInferenceRule.CONJUNCTION_ELIMINATION, asts[0]);
                subGoals.add(subGoal);
            }
        }

        return subGoals;
    }

    @Override
    public String getText(SubGoal subGoal) {
        return "From " + subGoal.getGoal() + " and " + subGoal.getFormula() + ", we can derive " + subGoal.getFormula();
    }

    private boolean contains(List<AST> asts, AST other) {
        for (AST ast : asts) {
            if (ast.isEquivalentTo(other)) return true;
        }
        return false;
    }
}
