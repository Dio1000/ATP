package me.dariansandru.domain.proof.proof_states;

import me.dariansandru.domain.proof.Strategy;
import me.dariansandru.domain.proof.SubGoal;
import me.dariansandru.domain.proof.inference_rules.InferenceRule;
import me.dariansandru.domain.proof.inference_rules.propositional.PropositionalInferenceRule;
import me.dariansandru.utils.data_structures.ast.AST;
import me.dariansandru.utils.data_structures.ast.PropositionalAST;
import me.dariansandru.utils.helper.PropositionalLogicHelper;

import java.util.ArrayList;
import java.util.List;

public class PropositionalProofState implements ProofState {

    private final List<AST> knowledgeBase;
    private final List<AST> goals;
    private List<InferenceRule> inferenceRules;

    boolean isVisited = false;
    boolean childrenInConjunction = false;
    boolean isProven = false;
    boolean expanded = false;

    private ProofState parent;
    private List<ProofState> children = new ArrayList<>();
    private SubGoal root;

    private int currentChildIndex = 0;

    public PropositionalProofState(List<AST> knowledgeBase,
                                   List<AST> goals,
                                   List<InferenceRule> inferenceRules) {
        this.knowledgeBase = knowledgeBase;
        this.goals = goals;
        this.inferenceRules = inferenceRules;
    }

    @Override
    public List<AST> getKnowledgeBase() {
        return knowledgeBase;
    }

    @Override
    public List<AST> getGoals() {
        return goals;
    }

    public AST getGoal() {
        return goals.getFirst();
    }

    @Override
    public boolean isVisited() {
        return isVisited;
    }

    @Override
    public boolean areChildrenInConjunction() {
        return childrenInConjunction;
    }

    @Override
    public boolean isProven() {
        return isProven;
    }

    @Override
    public List<InferenceRule> getInferenceRules() {
        return inferenceRules;
    }

    @Override
    public void prove() {
        if (!children.isEmpty()) {
            proveChildren();
        }
        if (isProven) return;

        while (!isProven) {
            if (containsGoal()) {
                isProven = true;
                break;
            }

            this.root = new SubGoal(goals.getFirst(), PropositionalInferenceRule.HYPOTHESIS, goals.getFirst());
            expandSubGoal(root);
        }
    }

    private void expandSubGoal(SubGoal subGoal) {
        if (containsSubGoal(subGoal)) {
            this.isProven = true;
            return;
        }

        for (InferenceRule rule : inferenceRules) {
            List<SubGoal> newSubGoals = rule.getSubGoals(knowledgeBase, subGoal.getGoal());
            if (!newSubGoals.isEmpty()) {
                subGoal.addChildren(newSubGoals);

                for (SubGoal child : newSubGoals) {
                    expandSubGoal(child);
                    if (isProven) return;
                }
            }
        }
    }

    private void proveChildren() {
        this.isProven = childrenInConjunction;

        while (currentChildIndex < children.size()) {
            ProofState child = children.get(currentChildIndex);
            if (!child.isProven()) child.prove();

            if (childrenInConjunction) {
                if (!child.isProven()) {
                    this.isProven = false;
                    return;
                }
            }
            else {
                if (child.isProven()) {
                    this.isProven = true;
                    return;
                }
            }
            currentChildIndex++;
        }
    }

    @Override
    public boolean simplify() {
        if (!(goals.getFirst() instanceof PropositionalAST goal)) {
            return false;
        }
        return goals.size() != 1 || !goal.isAtomic();
    }

    @Override
    public List<ProofState> getChildren() {
        return children;
    }

    @Override
    public void addChild(ProofState proofState) {
        children.add(proofState);
        proofState.addParent(this);
    }

    @Override
    public ProofState getParent() {
        return parent;
    }

    @Override
    public void addParent(ProofState proofState) {
        this.parent = proofState;
    }

    public Strategy notifyProof() {
        if (isProven) return Strategy.NO_STRATEGY;
        if (expanded) return Strategy.NO_STRATEGY;

        expanded = true;

        if (!simplify()) {
            return Strategy.NO_STRATEGY;
        }
        if (this.goals.size() != 1) {
            this.childrenInConjunction = true;
            return Strategy.CONJUNCTION_STRATEGY;
        }
        switch (PropositionalLogicHelper.getOutermostOperation(this.goals.getFirst())) {
            case IMPLICATION -> {
                this.childrenInConjunction = true;
                return Strategy.IMPLICATION_STRATEGY;
            }
            case EQUIVALENCE -> {
                this.childrenInConjunction = true;
                return Strategy.EQUIVALENCE_STRATEGY;
            }
            case CONJUNCTION -> {
                this.childrenInConjunction = true;
                return Strategy.CONJUNCTION_STRATEGY;
            }
            case DISJUNCTION -> {
                this.childrenInConjunction = false;
                return Strategy.DISJUNCTION_STRATEGY;
            }
            case NEGATION -> {
                this.childrenInConjunction = true;
                return Strategy.NEGATION_STRATEGY;
            }
            default -> {
                this.childrenInConjunction = false;
                return Strategy.NO_STRATEGY;
            }
        }
    }

    public void print() {
        System.out.println("Knowledge Base:");
        for (AST ast : knowledgeBase) System.out.println(ast);

        System.out.println("\nGoals:");
        for (AST goal : goals) System.out.println(goal);
    }

    private boolean containsGoal() {
        for (AST ast : knowledgeBase) {
            if (ast.isEquivalentTo(goals.getFirst())) return true;
        }
        return false;
    }

    private boolean containsSubGoal(SubGoal subGoal) {
        for (AST ast : knowledgeBase) {
            if (ast.isEquivalentTo(subGoal.getGoal())) return true;
        }
        return false;
    }
}