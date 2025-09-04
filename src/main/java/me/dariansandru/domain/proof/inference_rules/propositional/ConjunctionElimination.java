package me.dariansandru.domain.proof.inference_rules.propositional;

import me.dariansandru.domain.LogicalOperator;
import me.dariansandru.domain.logical_operator.Conjunction;
import me.dariansandru.domain.predicate.Predicate;
import me.dariansandru.domain.proof.SubGoal;
import me.dariansandru.domain.proof.inference_rules.InferenceRule;
import me.dariansandru.domain.data_structures.ast.AST;
import me.dariansandru.domain.data_structures.ast.PropositionalASTNode;
import me.dariansandru.utils.helper.KnowledgeBaseRegistry;

import java.util.ArrayList;
import java.util.List;

public class ConjunctionElimination implements InferenceRule {

    private final List<AST> derived = new ArrayList<>();

    @Override
    public String getName() {
        return "Conjunction Elimination";
    }

    @Override
    public boolean canInference(List<AST> asts, AST goal) {
        boolean shouldInference = false;

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
                derived.add(left);
                KnowledgeBaseRegistry.addEntry(left.toString(), "From " + ast + " by " + getName() + ", we derive " + left, List.of(ast.toString()));
                shouldInference = true;
            }
            if (!hasRight)
            {
                derived.add(right);
                KnowledgeBaseRegistry.addEntry(right.toString(), "From " + ast + " by " + getName() + ", we derive " + right, List.of(ast.toString()));
                shouldInference = true;
            }
        }
        return shouldInference;
    }

    @Override
    public List<AST> inference(List<AST> asts, AST goal) {
        if (!canInference(asts, goal)) return new ArrayList<>();
        return derived;
    }

    @Override
    public List<SubGoal> getSubGoals(List<AST> knowledgeBase, AST... asts) {
//        if (asts.length != 1) return new ArrayList<>();
//        AST goal = asts[0];
//        List<SubGoal> subGoals = new ArrayList<>();
//
//        for (AST ast : knowledgeBase) {
//            if (!(ast instanceof PropositionalAST)) continue;
//            if (PropositionalLogicHelper.getOutermostOperation(ast) == LogicalOperator.CONJUNCTION) {
//                PropositionalAST left = (PropositionalAST) ast.getSubtree(0);
//                PropositionalAST right = (PropositionalAST) ast.getSubtree(1);
//
//                if (goal.isEquivalentTo(left)) {
//                    subGoals.add(new SubGoal(left, PropositionalInferenceRule.CONJUNCTION_ELIMINATION, goal));
//                }
//                if (goal.isEquivalentTo(right)) {
//                    subGoals.add(new SubGoal(right, PropositionalInferenceRule.CONJUNCTION_ELIMINATION, goal));
//                }
//            }
//        }
//
//        return subGoals;
        return new ArrayList<>();
    }


    @Override
    public String getText(SubGoal subGoal) {
        return "From " + subGoal.getGoal() + " and " + subGoal.getFormula() + " by " + getName() + ", we can derive " + subGoal.getFormula();
    }

    private boolean contains(List<AST> asts, AST other) {
        for (AST ast : asts) {
            if (ast.isEquivalentTo(other)) return true;
        }
        return false;
    }
}
