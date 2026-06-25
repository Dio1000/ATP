package me.dariansandru.domain.proof.inference_rules.propositional;

import me.dariansandru.domain.language.LogicalOperator;
import me.dariansandru.domain.proof.SubGoal;
import me.dariansandru.domain.proof.inference_rules.InferenceRule;
import me.dariansandru.domain.data_structures.ast.AST;
import me.dariansandru.domain.data_structures.ast.PropositionalAST;
import me.dariansandru.utils.flyweight.LogicalOperatorFlyweight;
import me.dariansandru.utils.helper.KnowledgeBaseRegistry;
import me.dariansandru.utils.helper.PropositionalLogicHelper;

import java.util.ArrayList;
import java.util.List;

public class EquivalenceElimination implements InferenceRule {

    private final List<AST> derived = new ArrayList<>();

    @Override
    public String name() {
        return "Equivalence Elimination";
    }

    @Override
    public boolean canInference(List<AST> asts, AST goal) {
        if (asts.size() != 1) return false;

        boolean shouldInference = false;
        for (AST ast : asts) {
            if (PropositionalLogicHelper.getOutermostOperation(ast) != LogicalOperator.EQUIVALENCE) return false;
            PropositionalAST left = (PropositionalAST) ast.getSubtree(0);
            PropositionalAST right = (PropositionalAST) ast.getSubtree(1);

            PropositionalAST newAST1 = PropositionalLogicHelper.buildFormula(left, right, LogicalOperatorFlyweight.getImplicationString());
            PropositionalAST newAST2 = PropositionalLogicHelper.buildFormula(right, left, LogicalOperatorFlyweight.getImplicationString());

            if (!inDerived(newAST1)) {
                KnowledgeBaseRegistry.addEntry(newAST1.toString(), "From " + ast + ", by " + name() + ", we derive " + newAST1, List.of(ast.toString()));
                derived.add(newAST1);
                shouldInference = true;
            }
            if (!inDerived(newAST2)) {
                KnowledgeBaseRegistry.addEntry(newAST2.toString(), "From " + ast + ", by " + name() + ", we derive " + newAST2, List.of(ast.toString()));
                derived.add(newAST2);
                shouldInference = true;
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

        for (AST ast : knowledgeBase) {
            if (!(ast instanceof PropositionalAST)) continue;
            if (PropositionalLogicHelper.getOutermostOperation(ast) == LogicalOperator.EQUIVALENCE) {
                PropositionalAST left = (PropositionalAST) ast.getSubtree(0);
                PropositionalAST right = (PropositionalAST) ast.getSubtree(1);

                if (left.isEquivalentTo(asts[0])) {
                    KnowledgeBaseRegistry.addEntry(asts[0].toString(), "From " + ast + " and " + right + ", by " + name() + ", we derive " + asts[0], List.of(ast.toString(), right.toString()));
                    SubGoal subGoal = new SubGoal(right, PropositionalInferenceRule.EQUIVALENCE_ELIMINATION, ast);
                    subGoals.add(subGoal);
                }
                else if (right.isEquivalentTo(asts[0])) {
                    KnowledgeBaseRegistry.addEntry(asts[0].toString(), "From " + ast + " and " + left + ", by " + name() + ", we derive " + asts[0], List.of(ast.toString(), left.toString()));
                    SubGoal subGoal = new SubGoal(left, PropositionalInferenceRule.EQUIVALENCE_ELIMINATION, ast);
                    subGoals.add(subGoal);
                }
            }
        }

        return subGoals;
    }

    @Override
    public String getText(SubGoal subGoal) {
        PropositionalAST derivedLeft = (PropositionalAST) subGoal.getFormula().getSubtree(0);
        PropositionalAST derivedRight = (PropositionalAST) subGoal.getFormula().getSubtree(1);

        return "From " + subGoal.getFormula() + " and " + subGoal.getGoal() + " by " + name() + ", we conclude " + ((derivedLeft.isEquivalentTo(subGoal.getGoal())) ? derivedRight : derivedLeft);
    }

    public boolean inDerived(AST ast) {
        for (AST derivedAST : derived) {
            if (ast.isEquivalentTo(derivedAST)) return true;
        }
        return false;
    }
}