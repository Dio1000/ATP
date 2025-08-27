package me.dariansandru.domain.proof.proofs;

import me.dariansandru.domain.logical_operator.Implication;
import me.dariansandru.domain.proof.Strategy;
import me.dariansandru.domain.proof.inference_rules.InferenceRule;
import me.dariansandru.domain.proof.proof_states.ProofState;
import me.dariansandru.domain.proof.proof_states.PropositionalProofState;
import me.dariansandru.domain.signature.Signature;
import me.dariansandru.io.OutputDevice;
import me.dariansandru.reflexivity.InferenceRulesFactory;
import me.dariansandru.utils.data_structures.ast.AST;
import me.dariansandru.utils.data_structures.ast.PropositionalAST;
import me.dariansandru.utils.data_structures.ast.PropositionalASTNode;

import java.util.ArrayList;
import java.util.List;

public class PropositionalProof implements Proof{

    private final List<InferenceRule> inferenceRules;
    private final List<AST> knowledgeBase;
    private final List<AST> goals;
    private boolean isProven = false;

    private List<String> assumptions = new ArrayList<>();
    private List<String> conclusions = new ArrayList<>();

    private PropositionalProofState root;

    public PropositionalProof(Signature signature, List<AST> knowledgeBase, List<AST> goals) {
        this.inferenceRules = InferenceRulesFactory.createRules(signature);
        this.knowledgeBase = knowledgeBase;
        this.goals = goals;

        this.root = new PropositionalProofState(knowledgeBase, goals, inferenceRules);
    }

    public void buildTree(ProofState state) {
        Strategy strategy = ((PropositionalProofState) state).notifyProof();

        if (strategy == Strategy.IMPLICATION_STRATEGY) {
            ImplicationStrategy(state);
            buildTree(state.getChildren().getFirst());
        }
        else if (strategy == Strategy.EQUIVALENCE_STRATEGY) {
            EquivalenceStrategy(state);
            buildTree(state.getChildren().getFirst());
            buildTree(state.getChildren().get(1));
        }
        else if (strategy == Strategy.CONJUNCTION_STRATEGY) {
            ConjunctionStrategy(state);
            int childrenNumber = state.getChildren().size();
            for (int i = 0 ; i < childrenNumber ; i++)
                buildTree(state.getChildren().get(i));
        }
        else if (strategy == Strategy.DISJUNCTION_STRATEGY) {
            DisjunctionStrategy(state);
            int childrenNumber = state.getChildren().size();
            for (int i = 0 ; i < childrenNumber ; i++)
                buildTree(state.getChildren().get(i));
        }
    }

    public void prove() {
        buildTree(root);

        root.prove();
        isProven = root.isProven();
        if (isProven) System.out.println("Reached");

        printProof();
    }

    public void EquivalenceStrategy(ProofState state) {
        List<AST> newGoals1 = new ArrayList<>();
        List<AST> newGoals2 = new ArrayList<>();

        AST goal = ((PropositionalProofState) state).getGoal();

        AST newGoal1 = new PropositionalAST(goal.getSubtree(0) + " " + new Implication().getRepresentation() + " " + goal.getSubtree(1));
        AST newGoal2 = new PropositionalAST(goal.getSubtree(1) + " " + new Implication().getRepresentation() + " " + goal.getSubtree(0));
        newGoal1.validate(0);
        newGoal2.validate(0);

        newGoals1.add(newGoal1);
        newGoals2.add(newGoal2);

        PropositionalProofState newState1 = new PropositionalProofState(knowledgeBase, newGoals1, inferenceRules);
        PropositionalProofState newState2 = new PropositionalProofState(knowledgeBase, newGoals2, inferenceRules);

        state.addChild(newState1);
        state.addChild(newState2);
    }

    public void ImplicationStrategy(ProofState state) {
        List<AST> newKnowledgeBase = state.getKnowledgeBase();
        List<AST> newGoals = new ArrayList<>();

        AST goal = ((PropositionalProofState) state).getGoal();

        AST newKBEntry = goal.getSubtree(0);
        newKBEntry.validate(0);
        newKnowledgeBase.add(newKBEntry);

        AST newGoal = goal.getSubtree(1);
        newGoal.validate(0);
        newGoals.add(newGoal);

        PropositionalProofState newState = new PropositionalProofState(newKnowledgeBase, newGoals, inferenceRules);

        state.addChild(newState);
    }

    public void ConjunctionStrategy(ProofState state) {
        if (state.getGoals().size() != 1) {
            int children = state.getGoals().size();
            PropositionalProofState newCurrentState = null;

            for (int i = 0 ; i < children ; i++) {
                PropositionalProofState newState = new PropositionalProofState(knowledgeBase, List.of(state.getGoals().get(i)), inferenceRules);
                state.addChild(newState);
                if (i == 0) newCurrentState = newState;
            }

            assert newCurrentState != null;
            return;
        }
        extractGoals(state);
    }

    public void DisjunctionStrategy(ProofState state) {
        extractGoals(state);
    }

    private void extractGoals(ProofState state) {
        AST goal = ((PropositionalProofState) state).getGoal();

        int children = ((PropositionalASTNode) goal.getRoot()).getChildren().size();

        for (int i = 0 ; i < children ; i++) {
            AST newGoal = goal.getSubtree(i);
            newGoal.validate(0);

            PropositionalProofState newState = new PropositionalProofState(knowledgeBase, List.of(newGoal), inferenceRules);
            state.addChild(newState);
        }
    }

    @Override
    public List<AST> getKnowledgeBase() {
        return knowledgeBase;
    }

    @Override
    public List<AST> getGoals() {
        return goals;
    }

    @Override
    public List<String> getAssumptions() {
        return assumptions;
    }

    @Override
    public List<String> getConclusions() {
        return conclusions;
    }

    @Override
    public void printProof() {
        for (int i = 0; i < assumptions.size(); i++) {
            OutputDevice.writeIndentedToConsole(assumptions.get(i), i);
        }

        for (int i = conclusions.size() - 1; i >= 0 ; i--) {
            OutputDevice.writeIndentedToConsole(conclusions.get(i), i);
        }
    }

    @Override
    public boolean isProven() {
        return isProven;
    }

    @Override
    public boolean moveLeft() {
        return false;
    }

    @Override
    public boolean moveRight() {
        return false;
    }

    @Override
    public boolean moveUp() {
        return false;
    }
}