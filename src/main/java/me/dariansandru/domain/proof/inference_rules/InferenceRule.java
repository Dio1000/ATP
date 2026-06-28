package me.dariansandru.domain.proof.inference_rules;

import me.dariansandru.domain.proof.SubGoal;
import me.dariansandru.domain.data_structures.ast.AST;

import java.util.List;

/**
Implementing this interface allows the user to define a new Inference Rule. This is useful
when expending a Universe of Discourse with new rules. An Inference Rule must contain
a name, a canInference and inference methods, two methods that allow checking
that a list of ASTs can inference a goal, then derive the formula.
A method getSubGoals is also included, but some Inference Rules may choose to not use it.
Same goes for the getText method, which creates a string for storing a text for the derivation
of a given sub-goal.
 */
public interface InferenceRule {
    String name();

    boolean canInference(List<AST> kb, AST goal);
    List<AST> inference(List<AST> kb, AST goal);

    List<SubGoal> getSubGoals(List<AST> knowledgeBase, AST... asts);
    String getText(SubGoal subGoal);
}
