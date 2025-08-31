package me.dariansandru.domain.proof;

import me.dariansandru.domain.proof.inference_rules.InferenceRule;
import me.dariansandru.domain.proof.inference_rules.propositional.PropositionalInferenceRule;
import me.dariansandru.utils.data_structures.ast.AST;

import java.util.ArrayList;
import java.util.List;

public class SubGoal {
    private final AST goal;
    private final PropositionalInferenceRule inferenceRule;
    private final AST formula;

    private SubGoal parent = null;
    private List<SubGoal> children;

    @Override
    public String toString() {
        assert goal != null;
        return goal.toString();
    }

    public SubGoal(AST goal, PropositionalInferenceRule inferenceRule, AST formula) {
        this.goal = goal;
        this.inferenceRule = inferenceRule;
        this.formula = formula;
        this.children = new ArrayList<>();
    }

    public SubGoal() {
        this.goal = null;
        this.inferenceRule = null;
        this.formula = null;
    }

    public boolean isEmpty() {
        return this.goal == null;
    }

    public AST getFormula() {
        return formula;
    }

    public PropositionalInferenceRule getInferenceRule() {
        return inferenceRule;
    }

    public AST getGoal() {
        return goal;
    }

    public List<SubGoal> getChildren() {
        return children;
    }

    public void addChild(SubGoal subGoal) {
        this.children.add(subGoal);
        subGoal.setParent(this);
    }

    public void addChildren(List<SubGoal> children) {
        this.children = children;
        for (SubGoal subGoal : children) {
            subGoal.setParent(this);
        }
    }

    public SubGoal getParent() {
        return parent;
    }

    public void setParent(SubGoal parent) {
        this.parent = parent;
    }
}
