package me.dariansandru.domain.proof.inference_rules;

import me.dariansandru.domain.proof.SubGoal;
import me.dariansandru.utils.data_structures.ast.AST;

import java.util.List;

public interface InferenceRule {
    String getName();

    public boolean canInference(List<AST> kb, AST goal);
    public List<AST> inference(List<AST> kb, AST goal);

    List<SubGoal> getSubGoals(List<AST> knowledgeBase, AST... asts);
    String getText(SubGoal subGoal);
}
