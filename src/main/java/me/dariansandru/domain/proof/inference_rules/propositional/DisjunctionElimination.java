package me.dariansandru.domain.proof.inference_rules.propositional;

import me.dariansandru.domain.proof.SubGoal;
import me.dariansandru.domain.proof.inference_rules.InferenceRule;
import me.dariansandru.utils.data_structures.ast.AST;

import java.util.List;

public class DisjunctionElimination implements InferenceRule {
    @Override
    public String getName() {
        return "Disjunction Elimination";
    }

    @Override
    public boolean canInference(List<AST> asts) {
        return false;
    }

    @Override
    public AST inference(List<AST> asts) {
        return null;
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
