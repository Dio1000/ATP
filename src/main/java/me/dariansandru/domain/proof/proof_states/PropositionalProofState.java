package me.dariansandru.domain.proof.proof_states;

import me.dariansandru.domain.LogicalOperator;
import me.dariansandru.domain.proof.Strategy;
import me.dariansandru.domain.proof.SubGoal;
import me.dariansandru.domain.proof.inference_rules.InferenceRule;
import me.dariansandru.domain.proof.inference_rules.propositional.ContradictionRule;
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
            if (goals.getFirst().toString().equals("Contradiction")
                    && containsContradiction()) {
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

        if (subGoal.getGoal().isContradiction()) {
            ContradictionRule rule = new ContradictionRule();
            if (processSubGoals(subGoal, rule.getSubGoals(knowledgeBase, subGoal.getGoal()))) return;
        }

        for (InferenceRule rule : inferenceRules) {
            if (processSubGoals(subGoal, rule.getSubGoals(knowledgeBase, subGoal.getGoal()))) return;
        }
    }

    private boolean processSubGoals(SubGoal subGoal, List<SubGoal> subGoals) {
        if (!subGoals.isEmpty()) {
            subGoal.addChildren(subGoals);

            for (SubGoal child : subGoals) {
                expandSubGoal(child);
                if (isProven) return true;
            }
        }
        return false;
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

        if (PropositionalLogicHelper.getOutermostOperation(this.goals.getFirst()) == LogicalOperator.NEGATION) {
            this.childrenInConjunction = true;
            return Strategy.NEGATION_STRATEGY;
        }

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

    // May be wrong?
    private boolean containsContradiction() {
        for (AST ast : knowledgeBase) {
            if (PropositionalLogicHelper.getOutermostOperation(ast) == LogicalOperator.CONJUNCTION) {
                PropositionalAST left = (PropositionalAST) ast.getSubtree(0);
                PropositionalAST right = (PropositionalAST) ast.getSubtree(1);

                PropositionalAST negatedLeft = left;
                PropositionalAST negatedRight = right;
                negatedLeft.negate();
                negatedRight.negate();

                if (left.isEquivalentTo(negatedLeft) && right.isEquivalentTo(negatedRight)) return true;
            }
        }

        return false;
    }
}