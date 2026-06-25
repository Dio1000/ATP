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

public class ImplicationIntroduction implements InferenceRule {

    private final List<AST> derived = new ArrayList<>();

    @Override
    public String name() {
        return "Implication Introduction";
    }

    @Override
    public boolean canInference(List<AST> kb, AST goal) {
        boolean shouldInference = false;

        for (AST ast : kb) {
            if (PropositionalLogicHelper.getOutermostOperation(ast) == LogicalOperator.EQUIVALENCE) {
                PropositionalAST left = (PropositionalAST) ast.getSubtree(0);
                PropositionalAST right = (PropositionalAST) ast.getSubtree(1);

                if (left.isEquivalentTo(goal) || right.isEquivalentTo(goal)) {
                    PropositionalAST newAST1 = PropositionalLogicHelper.buildFormula(left, right, LogicalOperatorFlyweight.getImplicationString());
                    PropositionalAST newAST2 = PropositionalLogicHelper.buildFormula(right, left, LogicalOperatorFlyweight.getImplicationString());

                    if (!inDerived(newAST1)) {
                        derived.add(newAST1);
                        KnowledgeBaseRegistry.addEntry(newAST1.toString(), "From " + ast + ", by " + name() + ", we derive " + newAST1, List.of(ast.toString()));
                        shouldInference = true;
                    }
                    if (!inDerived(newAST2)) {
                        derived.add(newAST2);
                        KnowledgeBaseRegistry.addEntry(newAST2.toString(), "From " + ast + ", by " + name() + ", we derive " + newAST2, List.of(ast.toString()));
                        shouldInference = true;
                    }
                }
            }
        }

        return shouldInference;
    }

    @Override
    public List<AST> inference(List<AST> kb, AST goal) {
        derived.clear();
        if (!canInference(kb, goal)) return new ArrayList<>();
        return derived;
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