package me.dariansandru.domain.proof.inference_rules.propositional;

import me.dariansandru.domain.LogicalOperator;
import me.dariansandru.domain.logical_operator.Implication;
import me.dariansandru.domain.proof.SubGoal;
import me.dariansandru.domain.proof.inference_rules.InferenceRule;
import me.dariansandru.domain.data_structures.ast.AST;
import me.dariansandru.domain.data_structures.ast.PropositionalAST;
import me.dariansandru.utils.helper.KnowledgeBaseRegistry;
import me.dariansandru.utils.helper.PropositionalLogicHelper;

import java.util.ArrayList;
import java.util.List;

public class ImplicationIntroduction implements InferenceRule {

    private final List<AST> derived = new ArrayList<>();

    @Override
    public String getName() {
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
                    PropositionalAST newAST1 = new PropositionalAST(left + " " + new Implication().getRepresentation() + " " + right);
                    PropositionalAST newAST2 = new PropositionalAST(right + " " + new Implication().getRepresentation() + " " + left);
                    newAST1.validate(0);
                    newAST2.validate(0);

                    derived.add(newAST1);
                    derived.add(newAST2);

                    KnowledgeBaseRegistry.addEntry(newAST1.toString(), "From " + ast + " by " + getName() + ", we derive " + newAST1 + " (and " + newAST2 + ")", List.of(ast.toString()));
                    KnowledgeBaseRegistry.addEntry(newAST2.toString(), "From " + ast + " by " + getName() + ", we derive " + newAST2 + " (and " + newAST1 + ")", List.of(ast.toString()));
                    shouldInference = true;
                }
            }
        }

        return shouldInference;
    }

    @Override
    public List<AST> inference(List<AST> kb, AST goal) {
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
}
