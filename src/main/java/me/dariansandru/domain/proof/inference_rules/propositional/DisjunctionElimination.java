package me.dariansandru.domain.proof.inference_rules.propositional;

import me.dariansandru.domain.LogicalOperator;
import me.dariansandru.domain.proof.SubGoal;
import me.dariansandru.domain.proof.inference_rules.InferenceRule;
import me.dariansandru.domain.data_structures.ast.AST;
import me.dariansandru.domain.data_structures.ast.PropositionalAST;
import me.dariansandru.utils.helper.KnowledgeBaseRegistry;
import me.dariansandru.utils.helper.PropositionalLogicHelper;

import java.util.ArrayList;
import java.util.List;

public class DisjunctionElimination implements InferenceRule {

    private List<AST> derived = new ArrayList<>();

    @Override
    public String getName() {
        return "Disjunction Elimination";
    }

    // TODO: Change logic of else branch, you cannot conclude A and B from A OR B
    @Override
    public boolean canInference(List<AST> asts, AST goal) {
        boolean shouldDerive = false;
        for (AST ast : asts) {
            if (((PropositionalAST) goal).isAtomic() &&
                    PropositionalLogicHelper.getOutermostOperation(ast) == LogicalOperator.DISJUNCTION) {
                PropositionalAST left = (PropositionalAST) ast.getSubtree(0);
                PropositionalAST right = (PropositionalAST) ast.getSubtree(1);

                if (left.isEquivalentTo(right)) {
                    KnowledgeBaseRegistry.addEntry(left.toString(), "From " + ast + " by " + getName() + ", we derive " + left, List.of(ast.toString()));
                    derived.add(left);
                    shouldDerive = true;
                }
                else {
                    KnowledgeBaseRegistry.addEntry(left.toString(), "From " + ast + " by " + getName() + ", we derive " + left + " and " + right, List.of(ast.toString()));
                    KnowledgeBaseRegistry.addEntry(right.toString(), "From " + ast + " by " + getName() + ", we derive " + right + " and " + left, List.of(ast.toString()));
                    derived.add(left);
                    shouldDerive = true;
                }
             }
        }
        return shouldDerive;
    }

    @Override
    public List<AST> inference(List<AST> asts, AST goal) {
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
}
