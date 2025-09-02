package me.dariansandru.domain.proof.proofs;

import me.dariansandru.domain.logical_operator.Conjunction;
import me.dariansandru.domain.logical_operator.Disjunction;
import me.dariansandru.domain.logical_operator.Implication;
import me.dariansandru.domain.proof.Strategy;
import me.dariansandru.domain.proof.inference_rules.InferenceRule;
import me.dariansandru.domain.proof.proof_states.ProofState;
import me.dariansandru.domain.proof.proof_states.PropositionalProofState;
import me.dariansandru.domain.signature.Signature;
import me.dariansandru.io.OutputDevice;
import me.dariansandru.reflexivity.InferenceRulesFactory;
import me.dariansandru.domain.data_structures.ast.AST;
import me.dariansandru.domain.data_structures.ast.PropositionalAST;
import me.dariansandru.domain.data_structures.ast.PropositionalASTNode;
import me.dariansandru.utils.helper.ProofTextHelper;

import java.util.ArrayList;
import java.util.List;

public class PropositionalProof implements Proof{

    private final List<InferenceRule> inferenceRules;
    private final List<AST> knowledgeBase;
    private final List<AST> goals;
    private boolean isProven = false;

    private final List<String> assumptions = new ArrayList<>();
    private final List<String> conclusions = new ArrayList<>();

    private final PropositionalProofState root;

    public PropositionalProof(Signature signature, List<AST> knowledgeBase, List<AST> goals) {
        this.inferenceRules = InferenceRulesFactory.createRules(signature);
        this.knowledgeBase = knowledgeBase;
        this.goals = goals;

        this.root = new PropositionalProofState(knowledgeBase, goals, inferenceRules);
    }

    public void buildTree(ProofState state, int indent) {
        Strategy strategy = ((PropositionalProofState) state).notifyProof();

        if (strategy == Strategy.IMPLICATION_STRATEGY) {
            assumptions.add(ProofTextHelper.getAssumption(
                    state.getGoal().toString(),
                    state.getGoal().getSubtree(0).toString(),
                    state.getGoal().getSubtree(1).toString()));
            conclusions.add(ProofTextHelper.getConclusion(
                    state.getGoal().toString()));
            ProofTextHelper.addAssumptionStep(assumptions.getLast(), indent);
            ProofTextHelper.addConclusionStep(conclusions.getLast(), indent);

            ImplicationStrategy(state);
            buildTree(state.getChildren().getFirst(), indent + 1);
        }
        else if (strategy == Strategy.EQUIVALENCE_STRATEGY) {
            String leftImplication = state.getGoal().getSubtree(0).toString() + " " + new Implication().getRepresentation() + " " + state.getGoal().getSubtree(1).toString();
            String rightImplication = state.getGoal().getSubtree(1).toString() + " " + new Implication().getRepresentation() + " " + state.getGoal().getSubtree(0).toString();
            assumptions.add(ProofTextHelper.getEquivalenceAssumption(
                    state.getGoal().toString(),
                    leftImplication,
                    rightImplication));
            conclusions.add(ProofTextHelper.getConclusion(
                    state.getGoal().toString()));
            ProofTextHelper.addAssumptionStep(assumptions.getLast(), indent);
            ProofTextHelper.addConclusionStep(conclusions.getLast(), indent);

            EquivalenceStrategy(state);
            buildTree(state.getChildren().getFirst(), indent + 1);
            buildTree(state.getChildren().get(1), indent + 1);
        }
        else if (strategy == Strategy.CONJUNCTION_STRATEGY) {
            String assumption;
            assumption = state.getGoal().toString();
            String[] parts = assumption.split(new Conjunction().getRepresentation());
            assumptions.add(ProofTextHelper.getConjunctionAssumption(assumption, parts));
            conclusions.add(ProofTextHelper.getConclusion(
                    state.getGoal().toString()));
            ProofTextHelper.addAssumptionStep(assumptions.getLast(), indent);
            ProofTextHelper.addConclusionStep(conclusions.getLast(), indent);

            int childrenNumber = state.getChildren().size();
            ConjunctionStrategy(state);
            for (int i = 0 ; i < childrenNumber ; i++)
                buildTree(state.getChildren().get(i), indent + 1);
        }
        else if (strategy == Strategy.DISJUNCTION_STRATEGY) {
            String assumption = state.getGoal().toString();
            String[] parts = assumption.split(new Disjunction().getRepresentation());
            assumptions.add(ProofTextHelper.getDisjunctionAssumption(assumption, parts));
            conclusions.add(ProofTextHelper.getConclusion(
                    state.getGoal().toString()));
            ProofTextHelper.addAssumptionStep(assumptions.getLast(), indent);
            ProofTextHelper.addConclusionStep(conclusions.getLast(), indent);

            int childrenNumber = state.getChildren().size();
            assumptions.add(ProofTextHelper.getDisjunctionAssumption(assumption, parts));
            conclusions.add(ProofTextHelper.getConclusion(
                    state.getGoal().toString()));
            ProofTextHelper.addAssumptionStep(assumptions.getLast(), indent);
            ProofTextHelper.addConclusionStep(conclusions.getLast(), indent);

            DisjunctionStrategy(state);
            for (int i = 0 ; i < childrenNumber ; i++)
                buildTree(state.getChildren().get(i), indent + 1);
        }
        else if (strategy == Strategy.NEGATION_STRATEGY) {
            assumptions.add(ProofTextHelper.getNegationAssumption(state.getGoal()));
            conclusions.add(ProofTextHelper.getConclusion(state.getGoal().toString()));
            ProofTextHelper.addAssumptionStep(assumptions.getLast(), indent);
            ProofTextHelper.addConclusionStep(conclusions.getLast(), indent);

            NegationStrategy(state);
        }
    }

    public void prove() {
        long startTime = System.nanoTime();

        buildTree(root, 0);
        root.prove();
        isProven = root.isProven();

        System.out.println();
        ProofTextHelper.print();

        long endTime = System.nanoTime();
        long duration = endTime - startTime;
        double durationMs = duration / 1_000_000.0;

        ProofTextHelper.printWithSymbol("Proof completed in " + durationMs + " ms", "-");
    }

    public void EquivalenceStrategy(ProofState state) {
        AST goal = state.getGoal();

        String implication = new Implication().getRepresentation();

        AST newGoal1 = new PropositionalAST(goal.getSubtree(0) + " " + implication + " " + goal.getSubtree(1));
        AST newGoal2 = new PropositionalAST(goal.getSubtree(1) + " " + implication + " " + goal.getSubtree(0));
        newGoal1.validate(0);
        newGoal2.validate(0);

        List<AST> newKB1 = new ArrayList<>(state.getKnowledgeBase());
        List<AST> newKB2 = new ArrayList<>(state.getKnowledgeBase());

        PropositionalProofState newState1 = new PropositionalProofState(newKB1, List.of(newGoal1), inferenceRules);
        PropositionalProofState newState2 = new PropositionalProofState(newKB2, List.of(newGoal2), inferenceRules);

        state.addChild(newState1);
        state.addChild(newState2);
    }


    public void ImplicationStrategy(ProofState state) {
        List<AST> newKnowledgeBase = new ArrayList<>(state.getKnowledgeBase());
        List<AST> newGoals = new ArrayList<>();

        AST goal = state.getGoal();

        AST newKBEntry = goal.getSubtree(0);
        newKBEntry.validate(0);
        newKnowledgeBase.add(newKBEntry);

        AST newGoal = goal.getSubtree(1);
        newGoal.validate(0);
        newGoals.add(newGoal);

        PropositionalProofState newState = new PropositionalProofState(newKnowledgeBase, newGoals, inferenceRules);

        state.addChild(newState);
    }

    private void NegationStrategy(ProofState state) {
        List<AST> newKnowledgeBase = new ArrayList<>(state.getKnowledgeBase());
        List<AST> newGoals = new ArrayList<>();

        PropositionalAST kbEntry = (PropositionalAST) state.getGoals().getFirst();
        kbEntry.negate();

        newKnowledgeBase.add(kbEntry);
        newGoals.add(new PropositionalAST(true));

        PropositionalProofState newState =
                new PropositionalProofState(newKnowledgeBase, newGoals, inferenceRules);

        state.addChild(newState);
    }

    public void ConjunctionStrategy(ProofState state) {
        if (state.getGoals().size() != 1) {
            int children = state.getGoals().size();

            for (int i = 0; i < children; i++) {
                List<AST> newKB = new ArrayList<>(state.getKnowledgeBase());
                List<AST> newGoals = List.of(state.getGoals().get(i));

                PropositionalProofState newState =
                        new PropositionalProofState(newKB, newGoals, inferenceRules);

                state.addChild(newState);
            }
            return;
        }

        extractGoals(state);
    }


    public void DisjunctionStrategy(ProofState state) {
        extractGoals(state);
    }

    private void extractGoals(ProofState state) {
        AST goal = state.getGoal();

        int children = ((PropositionalASTNode) goal.getRoot()).getChildren().size();

        for (int i = 0; i < children; i++) {
            AST newGoal = goal.getSubtree(i);
            newGoal.validate(0);
            List<AST> newKB = new ArrayList<>(state.getKnowledgeBase());

            PropositionalProofState newState =
                    new PropositionalProofState(newKB, List.of(newGoal), inferenceRules);

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
}