package me.dariansandru.domain.proof.inference_rules.propositional;

import me.dariansandru.domain.data_structures.ast.AST;
import me.dariansandru.domain.data_structures.ast.PropositionalAST;
import me.dariansandru.domain.language.LogicalOperator;
import me.dariansandru.domain.proof.SubGoal;
import me.dariansandru.domain.proof.inference_rules.InferenceRule;
import me.dariansandru.utils.helper.KnowledgeBaseRegistry;
import me.dariansandru.utils.helper.PropositionalLogicHelper;

import java.util.ArrayList;
import java.util.List;

public class Absorption implements InferenceRule {

    private final List<AST> derived = new ArrayList<>();

    @Override
    public String name() {
        return "Absorption";
    }

    @Override
    public boolean canInference(List<AST> kb, AST goal) {
        boolean shouldInference = false;
        if (goal == null) return false;

        if (PropositionalLogicHelper.getOutermostOperation(goal) != LogicalOperator.IMPLICATION) return false;
        PropositionalAST goalLeft = (PropositionalAST) goal.getSubtree(0);
        PropositionalAST goalRight = (PropositionalAST) goal.getSubtree(1);
        if (PropositionalLogicHelper.getOutermostOperation(goalRight) != LogicalOperator.CONJUNCTION) return false;

        PropositionalAST goalConjunctionLeft = (PropositionalAST) goalRight.getSubtree(0);
        PropositionalAST goalConjunctionRight = (PropositionalAST) goalRight.getSubtree(1);

        for (AST ast : kb) {
            PropositionalAST astLeft = (PropositionalAST) ast.getSubtree(0);
            PropositionalAST astRight = (PropositionalAST) ast.getSubtree(1);

            if (astLeft.isEquivalentTo(goalLeft)) {
                if (astRight.isEquivalentTo(goalConjunctionLeft) || astRight.isEquivalentTo(goalConjunctionRight)) {
                    if (inDerived(goal)) continue;

                    // FIX: Added proper KnowledgeBaseRegistry tracking
                    KnowledgeBaseRegistry.addEntry(goal.toString(), "From " + ast + ", by " + name() + ", we derive " + goal, List.of(ast.toString()));

                    derived.add(goal);
                    shouldInference = true;
                }
            }
        }
        return shouldInference;
    }

    @Override
    public List<AST> inference(List<AST> asts, AST goal) {
        derived.clear();
        if (canInference(asts, goal)) return derived;
        return new ArrayList<>();
    }

    @Override
    public List<SubGoal> getSubGoals(List<AST> knowledgeBase, AST... asts) {
        return List.of();
    }

    @Override
    public String getText(SubGoal subGoal) {
        return "";
    }

    public boolean inDerived(AST ast) {
        for (AST derivedAST : derived) {
            if (ast.isEquivalentTo(derivedAST)) return true;
        }
        return false;
    }
}