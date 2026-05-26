package me.dariansandru.domain.proof.inference_rules.propositional;

import me.dariansandru.domain.data_structures.ast.AST;
import me.dariansandru.domain.data_structures.ast.PropositionalAST;
import me.dariansandru.domain.language.LogicalOperator;
import me.dariansandru.domain.proof.SubGoal;
import me.dariansandru.domain.proof.inference_rules.InferenceRule;
import me.dariansandru.utils.flyweight.LogicalOperatorFlyweight;
import me.dariansandru.utils.helper.PropositionalLogicHelper;

import java.util.ArrayList;
import java.util.List;

public class EquivalenceIntroduction implements InferenceRule {

    private final List<AST> derived = new ArrayList<>();

    @Override
    public String name() {
        return "Equivalence Introduction";
    }

    @Override
    public boolean canInference(List<AST> asts, AST goal) {

        boolean shouldInference = false;
        for (int i = 0; i < asts.size(); i++) {

            AST first = asts.get(i);
            if (PropositionalLogicHelper.getOutermostOperation(first) != LogicalOperator.IMPLICATION) continue;

            AST firstLeft = first.getSubtree(0);
            AST firstRight = first.getSubtree(1);
            for (int j = i + 1; j < asts.size(); j++) {

                AST second = asts.get(j);
                if (PropositionalLogicHelper.getOutermostOperation(second) != LogicalOperator.IMPLICATION) continue;

                AST secondLeft = second.getSubtree(0);
                AST secondRight = second.getSubtree(1);

                if (firstLeft.isEquivalentTo(secondRight) && firstRight.isEquivalentTo(secondLeft)) {
                    PropositionalAST equivalence = new PropositionalAST(firstLeft + " " + LogicalOperatorFlyweight.getEquivalenceString() + " " + firstRight, true);
                    if (!inDerived(equivalence)) {
                        derived.add(equivalence);
                        shouldInference = true;
                    }
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

        if (asts.length != 1) return new ArrayList<>();
        List<SubGoal> subGoals = new ArrayList<>();
        AST goal = asts[0];

        if (PropositionalLogicHelper.getOutermostOperation(goal) != LogicalOperator.EQUIVALENCE) return subGoals;
        AST left = goal.getSubtree(0);
        AST right = goal.getSubtree(1);

        PropositionalAST implication1 = new PropositionalAST(left + " " + LogicalOperatorFlyweight.getImplicationString() + " " + right, true);
        PropositionalAST implication2 = new PropositionalAST(right + " " + LogicalOperatorFlyweight.getImplicationString() + " " + left, true);

        subGoals.add(new SubGoal(implication1, PropositionalInferenceRule.EQUIVALENCE_INTRODUCTION, goal));
        subGoals.add(new SubGoal(implication2, PropositionalInferenceRule.EQUIVALENCE_INTRODUCTION, goal));

        return subGoals;
    }

    @Override
    public String getText(SubGoal subGoal) {

        AST goal = subGoal.getGoal();
        AST left = goal.getSubtree(0);
        AST right = goal.getSubtree(1);

        return "From " + left + " " + LogicalOperatorFlyweight.getImplicationString() + " " + right + " and " + right + " " + LogicalOperatorFlyweight.getImplicationString() + " " + left +
                " by " + name() + ", we conclude " + goal;
    }

    public boolean inDerived(AST ast) {
        for (AST derivedAST : derived) {
            if (ast.isEquivalentTo(derivedAST)) {
                return true;
            }
        }
        return false;
    }
}