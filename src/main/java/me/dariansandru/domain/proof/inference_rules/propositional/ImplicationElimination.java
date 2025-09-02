package me.dariansandru.domain.proof.inference_rules.propositional;

import me.dariansandru.domain.proof.SubGoal;
import me.dariansandru.domain.proof.inference_rules.InferenceRule;
import me.dariansandru.utils.data_structures.ast.AST;

import java.util.List;

public class ImplicationElimination implements InferenceRule {

    @Override
    public String getName() {
        return "";
    }

    @Override
    public boolean canInference(List<AST> kb, AST goal) {
        return false;
    }

    @Override
    public List<AST> inference(List<AST> kb, AST goal) {
        return List.of();
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
