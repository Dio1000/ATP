package me.dariansandru.domain.proof.automated_proof;

import me.dariansandru.domain.language.LogicalOperator;
import me.dariansandru.domain.proof.Strategy;
import me.dariansandru.domain.proof.inference_rules.InferenceRule;
import me.dariansandru.domain.proof.proof_states.ProofState;
import me.dariansandru.domain.proof.proof_states.PropositionalProofState;
import me.dariansandru.domain.language.signature.Signature;
import me.dariansandru.domain.proof.thread.ProofThread;
import me.dariansandru.io.OutputDevice;
import me.dariansandru.utils.factory.InferenceRulesFactory;
import me.dariansandru.domain.data_structures.ast.AST;
import me.dariansandru.domain.data_structures.ast.PropositionalAST;
import me.dariansandru.domain.data_structures.ast.PropositionalASTNode;
import me.dariansandru.utils.flyweight.LogicalOperatorFlyweight;
import me.dariansandru.utils.global.GlobalTimer;
import me.dariansandru.utils.helper.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Propositional Proof class that is used to instance new automated proofs.
 * It handles the logic of building the proof tree (with all its states),
 * checking which strategies can be applied (if multiple, they are ran
 * on different threads) and simplifying the goals.
 * It also holds the root of the Proof Tree, which is an object of type
 * PropositionalProofState. It then delegates responsibility of proving
 * to the root state, which starts the actual logic of inferring new rules
 * and extracting sub-goals.
 */
public class PropositionalProof implements Proof {

    private final List<InferenceRule> inferenceRules;
    private final List<AST> knowledgeBase;
    private final List<AST> goals;
    private boolean isProven = false;

    private final List<String> assumptions = new ArrayList<>();
    private final List<String> conclusions = new ArrayList<>();

    private final List<PropositionalProofState> roots = new ArrayList<>();
    private PropositionalProofState newRoot;
    private final List<Strategy> strategies;
    private int currentlyUsedStrategy = 0;
    private final PropositionalProofState originalState;

    private final List<ProofThread> proofThreads = new ArrayList<>();

    private int currentStateIndex = 0;

    public PropositionalProof(Signature signature, List<AST> knowledgeBase, List<AST> goals) {
        this.inferenceRules = InferenceRulesFactory.createRules(signature);
        this.knowledgeBase = new ArrayList<>(knowledgeBase);
        this.goals = goals;

        this.originalState = new PropositionalProofState(knowledgeBase, goals, inferenceRules);
        setCurrentStateIndex(originalState);
        this.strategies = originalState.notifyProof();
    }

    public PropositionalProof(List<InferenceRule> inferenceRules, List<AST> knowledgeBase, List<AST> goals) {
        this.inferenceRules = inferenceRules;
        this.knowledgeBase = new ArrayList<>(knowledgeBase);
        this.goals = goals;

        this.originalState = new PropositionalProofState(knowledgeBase, goals, inferenceRules);
        setCurrentStateIndex(originalState);
        this.strategies = originalState.notifyProof();
    }

    public boolean prove() {
        for (int i = 0; i < strategies.size(); i++) {
            roots.add(originalState);
        }

        for (int i = 0; i < strategies.size(); i++) {
            PropositionalProofState root = roots.get(i);
            buildTree(root, 0, strategies.get(i));
            ProofThread proofThread = new ProofThread();
            proofThread.setProofState(root);
            proofThreads.add(proofThread);
        }

        for (ProofThread proofThread : proofThreads) {
            proofThread.start();
        }

        boolean anyProven = false;
        for (ProofThread proofThread : proofThreads) {
            try {
                proofThread.join();
                if (proofThread.isProven()) {
                    anyProven = true;
                }
            }
            catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            }
        }

        this.isProven = anyProven;
        GlobalTimer.setEndTime();

        double executionTime = GlobalTimer.getExecutionTime();
        String timeString;
        if (executionTime >= 1000.0) timeString = String.format("%.3f s", executionTime / 1000.0);
        else timeString = String.format("%.2f ms", executionTime);
        double peakMB = MemoryTracker.getPeakMemoryMB();

        if (this.isProven) {
            String message = "Proof completed in " + timeString + "\n" +
                    "Peak memory usage: " + String.format("%.3f", peakMB) + " MB";
            ProofTextHelper.printWithSymbol(message, "-");
        }
        else ProofTextHelper.printWithSymbol("No proof found in " + timeString, "-");

        currentlyUsedStrategy = 0;
        proofThreads.clear();
        return isProven;
    }

    public boolean proveWithoutPrinting() {
        for (int i = 0 ; i < strategies.size() ; i++) roots.add(originalState);
        for (PropositionalProofState root : roots) {
            newRoot = root;
            if (isProven) continue;
            buildTree(root, 0, strategies.get(currentlyUsedStrategy));
            ProofThread proofThread = new ProofThread();
            proofThread.setProofState(newRoot);
            proofThreads.add(proofThread);

            currentlyUsedStrategy++;
        }
        for (ProofThread proofThread : proofThreads) proofThread.run();
        for (ProofThread proofThread : proofThreads) {
            if (proofThread.isProven()) {
                isProven = true;
                break;
            }
        }
        currentlyUsedStrategy = 0;
        proofThreads.clear();

        return isProven;
    }

    private void resentAndPrint(long startTime, PropositionalProofState state) {
        long endTime = System.nanoTime();
        long duration = endTime - startTime;
        double durationMs = duration / 1_000_000.0;

        ProofTextHelper.printWithSymbol("Proof completed in " + durationMs + " ms", "-");
        isProven = false;
        state.setUnproven();
    }

    // Method to build the Proof Tree. It identifies which strategy it can use by
    // receiving it from the PropositionalProofState as a notification. It then
    // routes the logic to handle the correct strategy, creating a new proof state
    // in the process. 
    public void buildTree(ProofState state, int indent, Strategy strategy) {
        if (strategies.isEmpty() || strategy == Strategy.NO_STRATEGY) return;

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
            solveDisjunction(state, indent);
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
                    KnowledgeBaseRegistry.addEntry(ast.getSubtree(i).toString(), "Strategy: Proof By Cases", List.of());

                    PropositionalProofState newState = new PropositionalProofState(newKB1, List.of(state.getGoal()), inferenceRules);
                    setCurrentStateIndex(newState);
                    state.addChild(newState);
                }
                newKB.remove(disjunctionAST);

                for (ProofState childState : state.getChildren()) {
                    PropositionalProofState child = (PropositionalProofState) childState;
                    List<Strategy> childStrategies = child.notifyProof();
                    Strategy next = childStrategies.isEmpty() ? Strategy.NO_STRATEGY : childStrategies.getFirst();
                    buildTree(child, indent + 1, next);
                }
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
        Logger.addStrategy(Strategy.IMPLICATION_STRATEGY);
        addAssumptionAndConclusionText(state, indent);

        ImplicationStrategy(state);

        PropositionalProofState child = (PropositionalProofState) state.getChildren().getFirst();
        List<Strategy> childStrategies = child.notifyProof();
        Strategy next = childStrategies.isEmpty() ? Strategy.NO_STRATEGY : childStrategies.getFirst();

        buildTree(child, indent + 1, next);
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

        for (ProofState childState : state.getChildren()) {
            PropositionalProofState child = (PropositionalProofState) childState;
            List<Strategy> childStrategies = child.notifyProof();
            Strategy next = childStrategies.isEmpty() ? Strategy.NO_STRATEGY : childStrategies.getFirst();
            buildTree(child, indent + 1, next);
        }
    }

    private void solveConjunction(ProofState state, int indent) {
        String assumption = state.getGoal().toString();
        String[] parts = assumption.split(LogicalOperatorFlyweight.getConjunctionString());
        assumptions.add(ProofTextHelper.getConjunctionAssumption(assumption, parts));
        conclusions.add(ProofTextHelper.getConclusion(
                state.getGoal().toString()));
        ProofTextHelper.addAssumptionStep(assumptions.getLast(), indent);
        ProofTextHelper.addConclusionStep(conclusions.getLast(), indent);

        ConjunctionStrategy(state);

        for (ProofState childState : state.getChildren()) {
            PropositionalProofState child = (PropositionalProofState) childState;
            List<Strategy> childStrategies = child.notifyProof();
            Strategy next = childStrategies.isEmpty() ? Strategy.NO_STRATEGY : childStrategies.getFirst();
            buildTree(child, indent + 1, next);
        }
    }

    private void solveDisjunction(ProofState state, int indent) {
        String assumption = state.getGoal().toString();
        String[] parts = assumption.split(LogicalOperatorFlyweight.getDisjunctionString());

        assumptions.add(ProofTextHelper.getDisjunctionAssumption(assumption, parts));
        conclusions.add(ProofTextHelper.getConclusion(state.getGoal().toString()));
        ProofTextHelper.addAssumptionStep(assumptions.getLast(), indent);
        ProofTextHelper.addConclusionStep(conclusions.getLast(), indent);

        DisjunctionStrategy(state);

        for (ProofState childState : state.getChildren()) {
            PropositionalProofState child = (PropositionalProofState) childState;
            List<Strategy> childStrategies = child.notifyProof();
            Strategy next = childStrategies.isEmpty() ? Strategy.NO_STRATEGY : childStrategies.getFirst();
            buildTree(child, indent + 1, next);
        }
    }

    private void solveNegation(ProofState state, int indent) {
        assumptions.add(ProofTextHelper.getNegationAssumption(state.getGoal()));
        conclusions.add(ProofTextHelper.getConclusion(state.getGoal().toString()));
        ProofTextHelper.addAssumptionStep(assumptions.getLast(), indent);
        ProofTextHelper.addConclusionStep(conclusions.getLast(), indent);

        NegationStrategy(state);

        if (!state.getChildren().isEmpty()) {
            PropositionalProofState child = (PropositionalProofState) state.getChildren().getFirst();
            List<Strategy> childStrategies = child.notifyProof();
            Strategy next = childStrategies.isEmpty() ? Strategy.NO_STRATEGY : childStrategies.getFirst();
            buildTree(child, indent + 1, next);
        }
    }

    private void EquivalenceStrategy(ProofState state) {
        Logger.addStrategy(Strategy.EQUIVALENCE_STRATEGY);
        AST goal = state.getGoal();

        String implication = LogicalOperatorFlyweight.getImplicationString();

        AST newGoal1 = new PropositionalAST(goal.getSubtree(0) + " " + implication + " " + goal.getSubtree(1), true);
        AST newGoal2 = new PropositionalAST(goal.getSubtree(1) + " " + implication + " " + goal.getSubtree(0), true);

        List<AST> newKB1 = new ArrayList<>(state.getKnowledgeBase());
        List<AST> newKB2 = new ArrayList<>(state.getKnowledgeBase());

        PropositionalProofState newState1 = new PropositionalProofState(newKB1, List.of(newGoal1), inferenceRules);
        PropositionalProofState newState2 = new PropositionalProofState(newKB2, List.of(newGoal2), inferenceRules);
        setCurrentStateIndex(newState1);
        setCurrentStateIndex(newState2);

        state.addChild(newState1);
        state.addChild(newState2);
    }

    private void ImplicationStrategy(ProofState state) {
        Logger.addStrategy(Strategy.IMPLICATION_STRATEGY);
        List<AST> newKnowledgeBase = new ArrayList<>(state.getKnowledgeBase());
        List<AST> newGoals = new ArrayList<>();

        AST goal = state.getGoal();

        AST newKBEntry = goal.getSubtree(0);
        newKnowledgeBase.add(newKBEntry);
        KnowledgeBaseRegistry.addEntry(newKBEntry.toString(), "Strategy: Implication Strategy", List.of());

        AST newGoal = goal.getSubtree(1);
        newGoals.add(newGoal);

        PropositionalProofState newState = new PropositionalProofState(newKnowledgeBase, newGoals, inferenceRules);
        setCurrentStateIndex(newState);

        state.addChild(newState);
        newRoot = newState;
    }

    private void NegationStrategy(ProofState state) {
        Logger.addStrategy(Strategy.NEGATION_STRATEGY);
        List<AST> newKnowledgeBase = new ArrayList<>(state.getKnowledgeBase());
        List<AST> newGoals = new ArrayList<>();

        PropositionalAST kbEntry = (PropositionalAST) state.getGoals().getFirst();
        PropositionalAST newKBEntry = new PropositionalAST(kbEntry.getFormulaString(), true);
        newKBEntry.negate();

        newKnowledgeBase.add(newKBEntry);
        KnowledgeBaseRegistry.addEntry(newKBEntry.toString(), "Strategy: Negation Strategy", List.of());

        newGoals.add(new PropositionalAST(true));

        PropositionalProofState newState =
                new PropositionalProofState(newKnowledgeBase, newGoals, inferenceRules);
        setCurrentStateIndex(newState);

        state.addChild(newState);
        newRoot = newState;
    }

    private void ConjunctionStrategy(ProofState state) {
        Logger.addStrategy(Strategy.CONJUNCTION_STRATEGY);
        if (state.getGoals().size() != 1) {
            int children = state.getGoals().size();

            for (int i = 0; i < children; i++) {
                List<AST> newKB = new ArrayList<>(state.getKnowledgeBase());
                List<AST> newGoals = List.of(state.getGoals().get(i));

                PropositionalProofState newState = new PropositionalProofState(newKB, newGoals, inferenceRules);
                setCurrentStateIndex(newState);

                state.addChild(newState);
            }
            return;
        }

        extractGoals(state);
    }

    private void DisjunctionStrategy(ProofState state) {
        Logger.addStrategy(Strategy.DISJUNCTION_STRATEGY);
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
            setCurrentStateIndex(newState);

            state.addChild(newState);
        }
    }

    public void setCurrentStateIndex(PropositionalProofState state) {
        state.setStateIndex(currentStateIndex);
        currentStateIndex++;
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

    public String getIndentedString() {
        return this.roots.getFirst().getIndentedProofString();
    }

    public String getFormalString() {
        return this.roots.getFirst().getFormalProofString();
    }
}