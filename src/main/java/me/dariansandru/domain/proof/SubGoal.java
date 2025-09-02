package me.dariansandru.domain.proof;

import me.dariansandru.domain.proof.inference_rules.propositional.PropositionalInferenceRule;
import me.dariansandru.utils.data_structures.ast.AST;

import java.util.ArrayList;
import java.util.List;

public class SubGoal {
    private final AST goal;
    private final List<AST> otherGoals;
    private int otherGoalIndex = 0;
    private boolean isExpanded = false;

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
        this.otherGoals = new ArrayList<>();
        this.inferenceRule = inferenceRule;
        this.formula = formula;
        this.children = new ArrayList<>();
    }

    public SubGoal() {
        this.goal = null;
        this.otherGoals = null;
        this.inferenceRule = null;
        this.formula = null;
    }

    public SubGoal(AST goal, PropositionalInferenceRule inferenceRule, AST formula, List<AST> otherGoals) {
        this.goal = goal;
        this.otherGoals = otherGoals;
        this.inferenceRule = inferenceRule;
        this.formula = formula;
        this.children = new ArrayList<>();
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

    public List<AST> getOtherGoals() {
        return otherGoals;
    }

    public int getOtherGoalIndex() {
        return otherGoalIndex;
    }

    public void incrementOtherGoalIndex() {
        this.otherGoalIndex++;
    }

    public boolean hasMoreOtherGoals() {
        assert otherGoals != null;
        return otherGoalIndex < otherGoals.size();
    }

    public AST getCurrentOtherGoal() {
        assert otherGoals != null;
        return otherGoals.isEmpty() ? null : otherGoals.get(otherGoalIndex);
    }

    public void setExpanded() {
        this.isExpanded = true;
    }

    public boolean isExpanded() {
        return this.isExpanded;
    }
}

