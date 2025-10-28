package me.dariansandru.domain.proof.manual_proof.helper;

import me.dariansandru.domain.language.LogicalOperator;
import me.dariansandru.domain.data_structures.ast.AST;
import me.dariansandru.domain.data_structures.ast.PropositionalAST;
import me.dariansandru.domain.proof.manual_proof.ManualPropositionalProof;
import me.dariansandru.domain.proof.manual_proof.ManualPropositionalProofStates;
import me.dariansandru.utils.flyweight.LogicalOperatorFlyweight;
import me.dariansandru.utils.helper.ErrorHelper;
import me.dariansandru.utils.helper.KnowledgeBaseRegistry;
import me.dariansandru.utils.helper.PropositionalLogicHelper;

import java.util.ArrayList;
import java.util.List;

public class ManualPropositionalStrategyHelper {

    private final List<AST> knowledgeBase;
    private final List<AST> goals;
    private final List<ManualPropositionalProof> stateGoals;
    private final List<ManualPropositionalProof> childStates;

    public ManualPropositionalStrategyHelper(
            List<ManualPropositionalProof> stateGoals,
            List<ManualPropositionalProof> childStates,
            List<AST> knowledgeBase,
            List<AST> goals) {
        this.knowledgeBase = knowledgeBase;
        this.goals = goals;
        this.stateGoals = stateGoals;
        this.childStates = childStates;
    }

    public boolean handleImplicationStrategy(int index) {
        AST ast = goals.get(index);
        if (PropositionalLogicHelper.getOutermostOperation(ast) != LogicalOperator.IMPLICATION) {
            ErrorHelper.add("Cannot apply command on " + ast +
                    ". Outermost logical operator is not '" + LogicalOperatorFlyweight.getImplicationString() + "'!");
            return false;
        }

        AST newAST1 = ast.getSubtree(0);
        AST newAST2 = ast.getSubtree(1);
        if (!containsEntry(newAST1, knowledgeBase)) knowledgeBase.add(newAST1);

        goals.remove(ast);
        goals.add(newAST2);

        KnowledgeBaseRegistry.addObtainedFrom(newAST1.toString(), List.of(ast.toString()), "Implication Strategy");
        KnowledgeBaseRegistry.addObtainedFrom(newAST2.toString(), List.of(ast.toString()), "Implication Strategy");
        return true;
    }

    public boolean handleEquivalenceStrategy(int index, ManualPropositionalProof proof) {
        AST ast = goals.get(index);
        if (PropositionalLogicHelper.getOutermostOperation(ast) != LogicalOperator.EQUIVALENCE) {
            ErrorHelper.add("Cannot apply command on " + ast +
                    ". Outermost logical operator is not '" + LogicalOperatorFlyweight.getEquivalenceString() + "'!");
            return false;
        }

        goals.remove(ast);
        AST left = ast.getSubtree(0);
        AST right = ast.getSubtree(1);
        AST newAST1 = new PropositionalAST(left + " " + LogicalOperatorFlyweight.getImplicationString() + " " + right, true);
        AST newAST2 = new PropositionalAST(right + " " + LogicalOperatorFlyweight.getImplicationString() + " " + left, true);

        createNewStates(newAST1, newAST2, proof);

        KnowledgeBaseRegistry.addObtainedFrom(newAST1.toString(), List.of(ast.toString()), "Equivalence Strategy");
        KnowledgeBaseRegistry.addObtainedFrom(newAST2.toString(), List.of(ast.toString()), "Equivalence Strategy");
        return true;
    }

    public boolean handleConjunctionStrategy(int index, ManualPropositionalProof proof) {
        AST ast = goals.get(index);
        if (PropositionalLogicHelper.getOutermostOperation(ast) != LogicalOperator.CONJUNCTION) {
            ErrorHelper.add("Cannot apply command on " + ast +
                    ". Outermost logical operator is not '" + LogicalOperatorFlyweight.getConjunctionString() + "'!");
            return false;
        }

        AST newAST1 = ast.getSubtree(0);
        AST newAST2 = ast.getSubtree(1);
        goals.remove(ast);

        createNewStates(newAST1, newAST2, proof);

        KnowledgeBaseRegistry.addObtainedFrom(newAST1.toString(), List.of(ast.toString()), "Conjunction Strategy");
        KnowledgeBaseRegistry.addObtainedFrom(newAST2.toString(), List.of(ast.toString()), "Conjunction Strategy");
        return true;
    }

    public boolean handleDisjunctionStrategy(int index) {
        return false;
    }

    public boolean handleNegationStrategy(int index) {
        AST ast = goals.get(index);
        ast.negate();
        AST contradictionAST = new PropositionalAST(true);
        if (!containsEntry(ast, knowledgeBase)) knowledgeBase.add(ast);
        goals.remove(ast);
        goals.add(contradictionAST);

        if (PropositionalLogicHelper.getOutermostOperation(ast) != LogicalOperator.NEGATION) {
            KnowledgeBaseRegistry.addObtainedFrom(ast.toString(), List.of(), "Proof of Negation");
            KnowledgeBaseRegistry.addObtainedFrom(contradictionAST.toString(), List.of(), "Proof of Negation");
        }
        else {
            KnowledgeBaseRegistry.addObtainedFrom(ast.toString(), List.of(), "Proof by Contradiction");
            KnowledgeBaseRegistry.addObtainedFrom(contradictionAST.toString(), List.of(), "Proof by Contradiction");
        }
        return true;
    }

    public boolean handleContrapositiveStrategy(int index) {
        AST ast = goals.get(index);
        if (PropositionalLogicHelper.getOutermostOperation(ast) != LogicalOperator.IMPLICATION) {
            ErrorHelper.add("Cannot apply command on " + ast +
                    ". Outermost logical operator is not '" + LogicalOperatorFlyweight.getImplicationString() + "'!");
            return false;
        }

        AST antecedent = ast.getSubtree(0);
        AST conclusion = ast.getSubtree(1);
        antecedent.negate();
        conclusion.negate();

        if (!containsEntry(conclusion, knowledgeBase)) knowledgeBase.add(conclusion);
        goals.remove(ast);
        goals.add(antecedent);

        KnowledgeBaseRegistry.addObtainedFrom(antecedent.toString(), List.of(ast.toString()), "Contrapositive Strategy");
        KnowledgeBaseRegistry.addObtainedFrom(conclusion.toString(), List.of(ast.toString()), "Contrapositive Strategy");
        return true;
    }

    public boolean handleProofByCases(int index, ManualPropositionalProof proof) {
        AST ast = knowledgeBase.get(index);
        knowledgeBase.remove(ast);

        if (PropositionalLogicHelper.getOutermostOperation(ast) == LogicalOperator.DISJUNCTION) {
            AST left = ast.getSubtree(0);
            AST right = ast.getSubtree(1);
            createNewKBStates(left, right, proof);

            KnowledgeBaseRegistry.addObtainedFrom(left.toString(), List.of(ast.toString()), "Proof by Cases");
            KnowledgeBaseRegistry.addObtainedFrom(right.toString(), List.of(ast.toString()), "Proof by Cases");

            goals.removeFirst();
            return true;
        }

        ErrorHelper.add("Cannot apply Proof by Cases on " + ast + "!");
        return false;
    }

    public void createNewState(AST newGoal, ManualPropositionalProof proof) {
        int index = ManualPropositionalProofStates.increaseStateIndex();
        List<AST> newGoals = new ArrayList<>();
        List<AST> newKB = new ArrayList<>();
        copyKB(newKB, knowledgeBase);
        newGoals.add(newGoal);

        ManualPropositionalProof newState = new ManualPropositionalProof(newKB, newGoals, proof, index);
        childStates.add(newState);
        ManualPropositionalProofStates.addState(newState, index);
    }

    private void createNewStates(AST newAST1, AST newAST2, ManualPropositionalProof proof) {
        int index1 = ManualPropositionalProofStates.increaseStateIndex();
        int index2 = ManualPropositionalProofStates.increaseStateIndex();

        List<AST> newKB1 = new ArrayList<>();
        copyKB(newKB1, knowledgeBase);
        List<AST> newKB2 = new ArrayList<>();
        copyKB(newKB2, knowledgeBase);

        List<AST> newGoals1 = new ArrayList<>();
        copyGoals(newGoals1, goals);
        newGoals1.add(newAST1);
        ManualPropositionalProof newState1 = new ManualPropositionalProof(newKB1, newGoals1, proof, index1);

        List<AST> newGoals2 = new ArrayList<>();
        copyGoals(newGoals2, goals);
        newGoals2.add(newAST2);
        ManualPropositionalProof newState2 = new ManualPropositionalProof(newKB2, newGoals2, proof, index2);

        childStates.add(newState1);
        childStates.add(newState2);

        stateGoals.add(newState1);
        stateGoals.add(newState2);

        ManualPropositionalProofStates.addState(newState1, index1);
        ManualPropositionalProofStates.addState(newState2, index2);
    }

    private void createNewKBStates(AST newAST1, AST newAST2, ManualPropositionalProof proof) {
        int index1 = ManualPropositionalProofStates.increaseStateIndex();
        int index2 = ManualPropositionalProofStates.increaseStateIndex();

        List<AST> newKB1 = new ArrayList<>();
        List<AST> newGoals1 = new ArrayList<>();
        copyGoals(newGoals1, goals);
        copyKB(newKB1, knowledgeBase);
        newKB1.add(newAST1);

        ManualPropositionalProof newState1 = new ManualPropositionalProof(newKB1, newGoals1, proof, index1);

        List<AST> newKB2 = new ArrayList<>();
        List<AST> newGoals2 = new ArrayList<>();
        copyGoals(newGoals2, goals);
        copyKB(newKB2, knowledgeBase);
        newKB2.add(newAST2);

        ManualPropositionalProof newState2 = new ManualPropositionalProof(newKB2, newGoals2, proof, index2);

        childStates.add(newState1);
        childStates.add(newState2);

        stateGoals.add(newState1);
        stateGoals.add(newState2);

        ManualPropositionalProofStates.addState(newState1, index1);
        ManualPropositionalProofStates.addState(newState2, index2);
    }

    private void copyGoals(List<AST> newGoals, List<AST> oldGoals) {
        newGoals.addAll(oldGoals);
    }

    private void copyKB(List<AST> newKB, List<AST> oldKB) {
        for (AST ast : oldKB) {
            newKB.add(new PropositionalAST(ast.toString(), true));
        }
    }

    private boolean containsEntry(AST entry, List<AST> knowledgeBase) {
        for (AST ast : knowledgeBase) {
            if (ast.isEquivalentTo(entry)) return true;
        }
        return false;
    }
}
