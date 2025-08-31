package me.dariansandru.domain.proof.inference_rules.propositional;

import me.dariansandru.domain.proof.SubGoal;
import me.dariansandru.domain.proof.inference_rules.InferenceRule;
import me.dariansandru.utils.data_structures.ast.AST;
import me.dariansandru.utils.data_structures.ast.PropositionalAST;

import java.util.ArrayList;
import java.util.List;

public class ContradictionRule implements InferenceRule {

    @Override
    public String getName() {
        return "Contradiction";
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
        if (asts.length != 1) return new ArrayList<>();
        List<SubGoal> subGoals = new ArrayList<>();

        subGoals.addAll(directContradiction(knowledgeBase));
        subGoals.addAll(implicationContradiction(knowledgeBase));

        return subGoals;
    }

    @Override
    public String getText(SubGoal subGoal) {
        return "";
    }

    public List<SubGoal> directContradiction(List<AST> knowledgeBase) {
        List<SubGoal> subGoals = new ArrayList<>();
        for (AST ast : knowledgeBase) {
            AST goal = new PropositionalAST(String.valueOf(ast));
            goal.validate(0);
            goal.negate();

            SubGoal subGoal = new SubGoal(goal, PropositionalInferenceRule.CONTRADICTION, ast);
            subGoals.add(subGoal);
        }
        return subGoals;
    }

    public List<SubGoal> implicationContradiction(List<AST> knowledgeBase) {
        List<SubGoal> subGoals = new ArrayList<>();

        return subGoals;
    }
}
