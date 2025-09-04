package me.dariansandru.domain.proof.inference_rules.propositional;

import me.dariansandru.domain.logical_operator.Conjunction;
import me.dariansandru.domain.logical_operator.Implication;
import me.dariansandru.domain.proof.SubGoal;
import me.dariansandru.domain.proof.inference_rules.InferenceRule;
import me.dariansandru.domain.data_structures.ast.AST;
import me.dariansandru.domain.data_structures.ast.PropositionalAST;
import me.dariansandru.utils.helper.PropositionalLogicHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ContradictionRule implements InferenceRule {

    private List<AST> derived = new ArrayList<>();

    @Override
    public String getName() {
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
            AST goal = new PropositionalAST(String.valueOf(ast));
            goal.validate(0);
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
            AST negatedAtom = new PropositionalAST(atom.toString());
            negatedAtom.validate(0);
            negatedAtom.negate();

            PropositionalAST newSubGoal = new PropositionalAST(atom + " " + new Conjunction().getRepresentation() + " " + negatedAtom);
            newSubGoal.validate(0);
            SubGoal subGoal = new SubGoal(newSubGoal, PropositionalInferenceRule.CONTRADICTION, newSubGoal);
            subGoal.addChild(subGoal);
        }
        return subGoals;
    }

    private static PropositionalAST getConjunctionAST(PropositionalAST left, PropositionalAST right) {
        PropositionalAST implicationLeft = new PropositionalAST(left + " " + new Implication().getRepresentation() + " " + right);
        PropositionalAST implicationRight = new PropositionalAST(right + " " + new Implication().getRepresentation() + " " + left);
        implicationLeft.validate(0);
        implicationRight.validate(0);

        String formula = "(" + implicationLeft + ") " + new Conjunction().getRepresentation() + " (" + implicationRight + ")";
        PropositionalAST newSubGoal = new PropositionalAST(formula);
        newSubGoal.validate(0);
        return newSubGoal;
    }
}
