package me.dariansandru.domain.proof.inference_rules.propositional;

import me.dariansandru.domain.proof.SubGoal;
import me.dariansandru.domain.proof.inference_rules.InferenceRule;
import me.dariansandru.domain.data_structures.ast.AST;
import me.dariansandru.domain.data_structures.ast.PropositionalAST;
import me.dariansandru.utils.flyweight.LogicalOperatorFlyweight;
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
        return false;
    }

    @Override
    public List<AST> inference(List<AST> asts, AST goal) {
        return derived;
    }

    @Override
    public List<SubGoal> getSubGoals(List<AST> knowledgeBase, AST... asts) {
        if (asts.length != 1) return new ArrayList<>();
        List<SubGoal> subGoals = new ArrayList<>();

        subGoals.addAll(directContradiction(knowledgeBase));
        subGoals.addAll(implicationContradiction(knowledgeBase));
        subGoals.addAll(conjunctionContradiction(knowledgeBase));

        return subGoals;
    }

    @Override
    public String getText(SubGoal subGoal) {
        if (subGoal.getGoal().isEquivalentTo(subGoal.getFormula())) {
            return "From " + subGoal.getGoal() + " we derive a contradiction";
        }
        return "From " + subGoal.getGoal() + " and " + subGoal.getFormula() + ", we derive a contradiction";
    }

    public List<SubGoal> directContradiction(List<AST> knowledgeBase) {
        List<SubGoal> subGoals = new ArrayList<>();
        for (AST ast : knowledgeBase) {
            if (!((PropositionalAST) ast).isAtomic()) continue;
            AST goal = new PropositionalAST(String.valueOf(ast), true);
            goal.negate();

            SubGoal subGoal = new SubGoal(goal, PropositionalInferenceRule.CONTRADICTION, ast);
            subGoals.add(subGoal);
        }
        return subGoals;
    }

    public List<SubGoal> implicationContradiction(List<AST> knowledgeBase) {
        List<SubGoal> subGoals = new ArrayList<>();

        return subGoals;
    }
    
    public List<SubGoal> conjunctionContradiction(List<AST> knowledgeBase) {
        Set<AST> atoms = new HashSet<>();
        List<SubGoal> subGoals = new ArrayList<>();
        for (AST ast : knowledgeBase) {
            atoms.addAll(PropositionalLogicHelper.getAtoms(ast));
            if (((PropositionalAST) ast).isAtomic()) continue;

            PropositionalAST left = (PropositionalAST) ast.getSubtree(0);
            PropositionalAST right = (PropositionalAST) ast.getSubtree(1);

            SubGoal subGoal = new SubGoal(left, PropositionalInferenceRule.CONTRADICTION, ast, List.of(right));
            subGoals.add(subGoal);
        }

        for (AST atom : atoms) {
            AST negatedAtom = new PropositionalAST(atom.toString(), true);
            negatedAtom.negate();

            PropositionalAST newSubGoal = new PropositionalAST(atom + " " + LogicalOperatorFlyweight.getConjunctionString() + " " + negatedAtom, true);
            SubGoal subGoal = new SubGoal(newSubGoal, PropositionalInferenceRule.CONTRADICTION, newSubGoal);
            subGoal.addChild(subGoal);
        }
        return subGoals;
    }

    private static PropositionalAST getConjunctionAST(PropositionalAST left, PropositionalAST right) {
        PropositionalAST implicationLeft = new PropositionalAST(left + " " + LogicalOperatorFlyweight.getImplicationString() + " " + right, true);
        PropositionalAST implicationRight = new PropositionalAST(right + " " + LogicalOperatorFlyweight.getImplicationString() + " " + left, true);

        String formula = "(" + implicationLeft + ") " + LogicalOperatorFlyweight.getConjunctionString() + " (" + implicationRight + ")";
        return new PropositionalAST(formula, true);
    }
}
