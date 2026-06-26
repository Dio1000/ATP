package me.dariansandru.domain.proof.inference_rules.propositional;

import me.dariansandru.domain.language.LogicalOperator;
import me.dariansandru.domain.proof.SubGoal;
import me.dariansandru.domain.proof.inference_rules.InferenceRule;
import me.dariansandru.domain.data_structures.ast.AST;
import me.dariansandru.domain.data_structures.ast.PropositionalAST;
import me.dariansandru.utils.flyweight.LogicalOperatorFlyweight;
import me.dariansandru.utils.helper.KnowledgeBaseRegistry;
import me.dariansandru.utils.helper.PropositionalLogicHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ContradictionRule implements InferenceRule {

    private final List<AST> derived = new ArrayList<>();

    @Override
    public String name() {
        return "Contradiction";
    }

    @Override
    public boolean canInference(List<AST> asts, AST goal) {
        for (int i = 0 ; i < asts.size() ; i++) {
            for (int j = i + 1 ; j < asts.size() ; j++) {
                if (isNegationOf(asts.get(i), asts.get(j))) {
                    derived.add(new PropositionalAST(true));
                    return true;
                }
            }
        }
        return PropositionalLogicHelper.buildConjunction(asts).isContradiction();
    }

    @Override
    public List<AST> inference(List<AST> asts, AST goal) {
        derived.clear();
        if (canInference(asts, goal)) return derived;
        return new ArrayList<>();
    }

    @Override
    public List<SubGoal> getSubGoals(List<AST> knowledgeBase, AST... asts) {
        if (asts.length != 1) return new ArrayList<>();
        List<SubGoal> subGoals = new ArrayList<>();

        subGoals.addAll(directContradiction(knowledgeBase));
        subGoals.addAll(conjunctionContradiction(knowledgeBase));
        return subGoals;
    }

    @Override
    public String getText(SubGoal subGoal) {
        if (subGoal.getGoal().isEquivalentTo(subGoal.getFormula())) return "From " + subGoal.getGoal() + " we derive a contradiction";
        return "From " + subGoal.getGoal() + " and " + subGoal.getFormula() + ", we derive a contradiction";
    }

    public List<SubGoal> directContradiction(List<AST> knowledgeBase) {
        List<SubGoal> subGoals = new ArrayList<>();
        for (AST ast : knowledgeBase) {
            if (!((PropositionalAST) ast).isAtomic()) continue;
            AST goal = new PropositionalAST(String.valueOf(ast), true);
            goal.negate();

            KnowledgeBaseRegistry.addEntry(goal.toString(), "To derive a contradiction, we attempt to prove " + goal + " since we already have " + ast,
                    List.of(ast.toString()));

            SubGoal subGoal = new SubGoal(goal, PropositionalInferenceRule.CONTRADICTION, ast);
            subGoals.add(subGoal);
            subGoal.addChild(subGoal);
        }
        return subGoals;
    }

    public List<SubGoal> conjunctionContradiction(List<AST> knowledgeBase) {
        Set<String> atoms = new HashSet<>();
        List<SubGoal> subGoals = new ArrayList<>();

        for (AST ast : knowledgeBase) {
            atoms.addAll(PropositionalLogicHelper.getAtoms(ast));
            if (((PropositionalAST) ast).isAtomic() ||
                    PropositionalLogicHelper.getOutermostOperation(ast) != LogicalOperator.CONJUNCTION) continue;

            PropositionalAST left = (PropositionalAST) ast.getSubtree(0);
            PropositionalAST right = (PropositionalAST) ast.getSubtree(1);

            KnowledgeBaseRegistry.addEntry(left.toString(), "To derive a contradiction from " + ast + ", we target " + left,
                    List.of(ast.toString()));

            SubGoal subGoal = new SubGoal(left, PropositionalInferenceRule.CONTRADICTION, ast, List.of(right));
            subGoals.add(subGoal);
        }

        for (String atom : atoms) {
            PropositionalAST negatedAtom = new PropositionalAST(atom, true);
            negatedAtom.negate();

            PropositionalAST newSubGoal = PropositionalLogicHelper.buildFormula(new PropositionalAST(atom, true), negatedAtom,  LogicalOperatorFlyweight.getConjunctionString());
            KnowledgeBaseRegistry.addEntry(newSubGoal.toString(), "A direct contradiction requires proving " + newSubGoal, List.of());

            SubGoal subGoal = new SubGoal(newSubGoal, PropositionalInferenceRule.CONTRADICTION, newSubGoal);
            subGoals.add(subGoal);
        }
        return subGoals;
    }

    public boolean inDerived(AST ast) {
        for (AST derivedAST : derived) {
            if (ast.isEquivalentTo(derivedAST)) return true;
        }
        return false;
    }

    private boolean isNegationOf(AST first, AST second) {
        if (first == null || second == null) return false;

        try {
            PropositionalAST negatedFirst = new PropositionalAST(first.toString(), true);
            negatedFirst.negate();

            PropositionalAST negatedSecond = new PropositionalAST(second.toString(), true);
            negatedSecond.negate();

            return first.isEquivalentTo(negatedSecond) || second.isEquivalentTo(negatedFirst);
        }
        catch (Exception e) {
            return false;
        }
    }
}