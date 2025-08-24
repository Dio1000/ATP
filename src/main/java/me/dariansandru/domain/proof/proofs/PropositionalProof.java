package me.dariansandru.domain.proof.proofs;

import me.dariansandru.domain.logical_operator.Implication;
import me.dariansandru.domain.proof.Strategy;
import me.dariansandru.domain.proof.inference_rules.InferenceRule;
import me.dariansandru.domain.proof.proof_states.PropositionalProofState;
import me.dariansandru.domain.signature.Signature;
import me.dariansandru.io.OutputDevice;
import me.dariansandru.reflexivity.InferenceRulesFactory;
import me.dariansandru.utils.data_structures.ast.AST;
import me.dariansandru.utils.data_structures.ast.PropositionalAST;

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
    private PropositionalProofState currentProofState;

    public PropositionalProof(Signature signature, List<AST> knowledgeBase, List<AST> goals) {
        this.inferenceRules = InferenceRulesFactory.createRules(signature);
        this.knowledgeBase = knowledgeBase;
        this.goals = goals;

        this.root = new PropositionalProofState(knowledgeBase, goals, inferenceRules);
        this.currentProofState = root;
    }

    public void prove() {
        while (!root.isProven()) {
            Strategy strategy = currentProofState.notifyProof();

            if (strategy == Strategy.IMPLICATION_STRATEGY) {
                ImplicationStrategy();
            }
            else if (strategy == Strategy.EQUIVALENCE_STRATEGY) {
                EquivalenceStrategy();
            }
            else if (strategy == Strategy.CONJUNCTION_STRATEGY) {
                ConjunctionStrategy();
            }
            else if (strategy == Strategy.DISJUNCTION_STRATEGY) {
                DisjunctionStrategy();
            }
            else if (strategy == Strategy.NO_STRATEGY) {
                currentProofState.prove();
                if (currentProofState.isProven()) {
                    if (currentProofState.getParent() != null) {
                        currentProofState = (PropositionalProofState) currentProofState.getParent();
                    }
                }
                else break;
            }
        }
        isProven = root.isProven();
        if (isProven) System.out.println("Reached");
        printProof();
    }

    public void EquivalenceStrategy() {
        List<AST> newGoals1 = new ArrayList<>();
        List<AST> newGoals2 = new ArrayList<>();

        AST goal = currentProofState.getGoal();

        newGoals1.add(new PropositionalAST(goal.getSubtree(0) + " " + new Implication().getRepresentation() + " " + goal.getSubtree(1)));
        newGoals2.add(new PropositionalAST(goal.getSubtree(1) + " " + new Implication().getRepresentation() + " " + goal.getSubtree(0)));

        PropositionalProofState newState1 = new PropositionalProofState(knowledgeBase, newGoals1, inferenceRules);
        PropositionalProofState newState2 = new PropositionalProofState(knowledgeBase, newGoals2, inferenceRules);

        currentProofState.addChild(newState1);
        currentProofState.addChild(newState2);

        currentProofState = newState1;
    }

    public void ImplicationStrategy() {
        List<AST> newKnowledgeBase = currentProofState.getKnowledgeBase();
        List<AST> newGoals = new ArrayList<>();
        AST goal = currentProofState.getGoal();

        newKnowledgeBase.add(goal.getSubtree(0));
        newGoals.add(goal.getSubtree(1));
        PropositionalProofState newState = new PropositionalProofState(newKnowledgeBase, newGoals, inferenceRules);

        currentProofState.addChild(newState);
        currentProofState = newState;
    }

    public void ConjunctionStrategy() {

    }

    public void DisjunctionStrategy() {

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
