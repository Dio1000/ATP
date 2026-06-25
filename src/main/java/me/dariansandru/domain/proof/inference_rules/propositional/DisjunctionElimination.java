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
import java.util.List;

public class DisjunctionElimination implements InferenceRule {

    private final List<AST> derived = new ArrayList<>();

    @Override
    public String name() {
        return "Disjunction Elimination";
    }

    @Override
    public boolean canInference(List<AST> asts, AST goal) {
        boolean shouldDerive = false;

        for (AST ast : asts) {
            if (PropositionalLogicHelper.getOutermostOperation(ast) == LogicalOperator.DISJUNCTION) {
                PropositionalAST left = (PropositionalAST) ast.getSubtree(0);
                PropositionalAST right = (PropositionalAST) ast.getSubtree(1);

                if (inDerived(left)) continue;

                // FIX: Only derive and flag IF they are actually equivalent
                if (left.isEquivalentTo(right)) {
                    // FIX: Standardized string spacing so the Text Helper prints it cleanly
                    KnowledgeBaseRegistry.addEntry(left.toString(), "From " + ast + ", by " + name() + ", we derive " + left, List.of(ast.toString()));
                    derived.add(left);
                    shouldDerive = true;
                }
            }
        }
        return shouldDerive;
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

        for (AST ast : knowledgeBase) {
            if (PropositionalLogicHelper.getOutermostOperation(ast) == LogicalOperator.DISJUNCTION) {
                PropositionalAST left = (PropositionalAST) ast.getSubtree(0);
                PropositionalAST right = (PropositionalAST) ast.getSubtree(1);

                PropositionalAST newAST1 = PropositionalLogicHelper.buildFormula(left, (PropositionalAST) asts[0], LogicalOperatorFlyweight.getImplicationString());
                PropositionalAST newAST2 = PropositionalLogicHelper.buildFormula(right, (PropositionalAST) asts[0], LogicalOperatorFlyweight.getImplicationString());

                // Registry correctly uses List.of(ast) mapping the child subgoals to the original disjunction
                KnowledgeBaseRegistry.addEntry(newAST1.toString(), "From " + ast + ", to prove " + asts[0] + ", assume " + left + " and prove " + asts[0], List.of(ast.toString()));
                KnowledgeBaseRegistry.addEntry(newAST2.toString(), "From " + ast + ", to prove " + asts[0] + ", assume " + right + " and prove " + asts[0], List.of(ast.toString()));

                SubGoal subGoal = new SubGoal(newAST1, PropositionalInferenceRule.DISJUNCTION_ELIMINATION, ast, List.of(newAST2));
                subGoals.add(subGoal);
            }
        }

        return subGoals;
    }

    @Override
    public String getText(SubGoal subGoal) {
        return "";
    }

    public boolean inDerived(AST ast) {
        for (AST derivedAST : derived) {
            if (ast.isEquivalentTo(derivedAST)) return true;
        }
        return false;
    }
}