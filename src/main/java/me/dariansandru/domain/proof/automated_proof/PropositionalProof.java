package me.dariansandru.domain.proof.automated_proof;

import me.dariansandru.domain.language.LogicalOperator;
import me.dariansandru.domain.proof.Strategy;
import me.dariansandru.domain.proof.inference_rules.InferenceRule;
import me.dariansandru.domain.proof.proof_states.ProofState;
import me.dariansandru.domain.proof.proof_states.PropositionalProofState;
import me.dariansandru.domain.language.signature.Signature;
import me.dariansandru.io.OutputDevice;
import me.dariansandru.utils.factory.InferenceRulesFactory;
import me.dariansandru.domain.data_structures.ast.AST;
import me.dariansandru.domain.data_structures.ast.PropositionalAST;
import me.dariansandru.domain.data_structures.ast.PropositionalASTNode;
import me.dariansandru.utils.flyweight.LogicalOperatorFlyweight;
import me.dariansandru.utils.helper.ProofTextHelper;
import me.dariansandru.utils.helper.PropositionalLogicHelper;

import java.util.ArrayList;
import java.util.List;

public class PropositionalProof implements Proof{

    private final List<InferenceRule> inferenceRules;
    private final List<AST> knowledgeBase;
    private final List<AST> goals;
    private boolean isProven = false;

    private final List<String> assumptions = new ArrayList<>();
    private final List<String> conclusions = new ArrayList<>();

    private PropositionalProofState root;

    public PropositionalProof(Signature signature, List<AST> knowledgeBase, List<AST> goals) {
        this.inferenceRules = InferenceRulesFactory.createRules(signature);
        this.knowledgeBase = knowledgeBase;
        this.goals = goals;

        this.root = new PropositionalProofState(knowledgeBase, goals, inferenceRules);
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
        isProven = false;
        root.setUnproven();
        this.root = new PropositionalProofState(knowledgeBase, goals, inferenceRules);
    }

    public void proveWithoutPrinting() {
        buildTree(root, 0);
        root.prove();
        isProven = root.isProven();
    }

    public void buildTree(ProofState state, int indent) {
        Strategy strategy = ((PropositionalProofState) state).notifyProof();

        if (strategy == Strategy.IMPLICATION_STRATEGY) {
            solveImplication(state, indent);
        }
        else if (strategy == Strategy.EQUIVALENCE_STRATEGY) {
            solveEquivalence(state, indent);
        }
        else if (strategy == Strategy.CONJUNCTION_STRATEGY) {
            solveConjunction(state, indent);
        }
        else if (strategy == Strategy.DISJUNCTION_STRATEGY) {
            // solveDisjunction(state, indent);
        }
        else if (strategy == Strategy.NEGATION_STRATEGY) {
            solveNegation(state, indent);
        }
        else if (strategy == Strategy.PROOF_BY_CASES) {
            checkProofByCases(state, indent);
        }
    }

    private void checkProofByCases(ProofState state, int indent) {
        List<AST> newKB = new ArrayList<>();
        for (AST ast : knowledgeBase) {
            newKB.add(new PropositionalAST(ast.toString(), true));
        }

        for (AST ast : newKB) {
            if (PropositionalLogicHelper.getOutermostOperation(ast) == LogicalOperator.DISJUNCTION) {
                PropositionalAST disjunctionAST = (PropositionalAST) ast;
                assumptions.add(ProofTextHelper.getProofByCasesAssumption(
                        state.getGoal().toString(),
                        disjunctionAST.toString()));
                ProofTextHelper.addAssumptionStep(assumptions.getLast(), indent);
                conclusions.add(ProofTextHelper.getConclusion(
                        state.getGoal().toString()));
                ProofTextHelper.addConclusionStep(conclusions.getLast(), indent);

                int childNumber = ((PropositionalASTNode) disjunctionAST.getRoot()).getChildren().size();
                for (int i = 0 ; i < childNumber ; i++) {
                    List<AST> newKB1 = new ArrayList<>();
                    newKB1.add(ast.getSubtree(i));

                    PropositionalProofState newState = new PropositionalProofState(newKB1, List.of(state.getGoal()), inferenceRules);
                    state.addChild(newState);
                }
                newKB.remove(disjunctionAST);
                break;
            }
        }
    }

    private void addAssumptionAndConclusionText(ProofState state, int indent) {
        assumptions.add(ProofTextHelper.getAssumption(
                state.getGoal().toString(),
                state.getGoal().getSubtree(0).toString(),
                state.getGoal().getSubtree(1).toString()));
        conclusions.add(ProofTextHelper.getConclusion(
                state.getGoal().toString()));
        ProofTextHelper.addAssumptionStep(assumptions.getLast(), indent);
        ProofTextHelper.addConclusionStep(conclusions.getLast(), indent);
    }

    private void solveImplication(ProofState state, int indent) {
        addAssumptionAndConclusionText(state, indent);

        ImplicationStrategy(state);
        buildTree(state.getChildren().getFirst(), indent + 1);
    }

    private void solveEquivalence(ProofState state, int indent) {
        String leftImplication = state.getGoal().getSubtree(0).toString() + " " + LogicalOperatorFlyweight.getImplicationString() + " " + state.getGoal().getSubtree(1).toString();
        String rightImplication = state.getGoal().getSubtree(1).toString() + " " + LogicalOperatorFlyweight.getImplicationString() + " " + state.getGoal().getSubtree(0).toString();
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

    private void solveConjunction(ProofState state, int indent) {
        String assumption;
        assumption = state.getGoal().toString();
        String[] parts = assumption.split(LogicalOperatorFlyweight.getConjunctionString());
        assumptions.add(ProofTextHelper.getConjunctionAssumption(assumption, parts));
        conclusions.add(ProofTextHelper.getConclusion(
                state.getGoal().toString()));
        ProofTextHelper.addAssumptionStep(assumptions.getLast(), indent);
        ProofTextHelper.addConclusionStep(conclusions.getLast(), indent);

        ConjunctionStrategy(state);
        int childrenNumber = state.getChildren().size();
        for (int i = 0 ; i < childrenNumber ; i++)
            buildTree(state.getChildren().get(i), indent + 1);
    }

    private void solveDisjunction(ProofState state, int indent) {
        String assumption = state.getGoal().toString();
        String[] parts = assumption.split(LogicalOperatorFlyweight.getDisjunctionString());
        assumptions.add(ProofTextHelper.getDisjunctionAssumption(assumption, parts));
        conclusions.add(ProofTextHelper.getConclusion(
                state.getGoal().toString()));
        ProofTextHelper.addAssumptionStep(assumptions.getLast(), indent);
        ProofTextHelper.addConclusionStep(conclusions.getLast(), indent);

        assumptions.add(ProofTextHelper.getDisjunctionAssumption(assumption, parts));
        conclusions.add(ProofTextHelper.getConclusion(
                state.getGoal().toString()));
        ProofTextHelper.addAssumptionStep(assumptions.getLast(), indent);
        ProofTextHelper.addConclusionStep(conclusions.getLast(), indent);

        DisjunctionStrategy(state);
        int childrenNumber = state.getChildren().size();
        for (int i = 0 ; i < childrenNumber ; i++)
            buildTree(state.getChildren().get(i), indent + 1);
    }

    private void solveNegation(ProofState state, int indent) {
        assumptions.add(ProofTextHelper.getNegationAssumption(state.getGoal()));
        conclusions.add(ProofTextHelper.getConclusion(state.getGoal().toString()));
        ProofTextHelper.addAssumptionStep(assumptions.getLast(), indent);
        ProofTextHelper.addConclusionStep(conclusions.getLast(), indent);

        NegationStrategy(state);
    }

    private void EquivalenceStrategy(ProofState state) {
        AST goal = state.getGoal();

        String implication = LogicalOperatorFlyweight.getImplicationString();

        AST newGoal1 = new PropositionalAST(goal.getSubtree(0) + " " + implication + " " + goal.getSubtree(1), true);
        AST newGoal2 = new PropositionalAST(goal.getSubtree(1) + " " + implication + " " + goal.getSubtree(0), true);

        List<AST> newKB1 = new ArrayList<>(state.getKnowledgeBase());
        List<AST> newKB2 = new ArrayList<>(state.getKnowledgeBase());

        PropositionalProofState newState1 = new PropositionalProofState(newKB1, List.of(newGoal1), inferenceRules);
        PropositionalProofState newState2 = new PropositionalProofState(newKB2, List.of(newGoal2), inferenceRules);

        state.addChild(newState1);
        state.addChild(newState2);
    }

    private void ImplicationStrategy(ProofState state) {
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

    private void ConjunctionStrategy(ProofState state) {
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


    private void DisjunctionStrategy(ProofState state) {
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