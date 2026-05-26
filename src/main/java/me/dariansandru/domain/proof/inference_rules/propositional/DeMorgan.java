package me.dariansandru.domain.proof.inference_rules.propositional;

import me.dariansandru.domain.language.LogicalOperator;
import me.dariansandru.domain.data_structures.ast.AST;
import me.dariansandru.domain.data_structures.ast.PropositionalAST;
import me.dariansandru.domain.proof.SubGoal;
import me.dariansandru.domain.proof.inference_rules.InferenceRule;
import me.dariansandru.utils.flyweight.LogicalOperatorFlyweight;
import me.dariansandru.utils.helper.KnowledgeBaseRegistry;
import me.dariansandru.utils.helper.PropositionalLogicHelper;

import java.util.ArrayList;
import java.util.List;

public class DeMorgan implements InferenceRule {

    private final List<AST> derived = new ArrayList<>();

    @Override
    public String name() {
        return "DeMorgan";
    }

    @Override
    public boolean canInference(List<AST> kb, AST goal) {
        boolean shouldInference = false;

        for (AST ast : kb) {
            if (PropositionalLogicHelper.getOutermostOperation(ast) == LogicalOperator.NEGATION) {
                PropositionalAST childAST = (PropositionalAST) ast.getSubtree(0);
                if (PropositionalLogicHelper.getOutermostOperation(childAST) == LogicalOperator.DISJUNCTION) {
                    PropositionalAST left = (PropositionalAST) childAST.getSubtree(0);
                    PropositionalAST right = (PropositionalAST) childAST.getSubtree(1);
                    left.negate();
                    right.negate();

                    PropositionalAST newAST = new PropositionalAST(left + " " + LogicalOperatorFlyweight.getConjunctionString() + " " + right, true);
                    if (inDerived(newAST)) continue;

                    derived.add(newAST);
                    KnowledgeBaseRegistry.addEntry(newAST.toString(), "From " + ast + ", by applying " + name() + ", we derive " + newAST, List.of(ast.toString()));
                    shouldInference = true;
                }
                else if (PropositionalLogicHelper.getOutermostOperation(childAST) == LogicalOperator.CONJUNCTION) {
                    PropositionalAST left = (PropositionalAST) childAST.getSubtree(0);
                    PropositionalAST right = (PropositionalAST) childAST.getSubtree(1);
                    left.negate();
                    right.negate();

                    PropositionalAST newAST = new PropositionalAST(left + " " + LogicalOperatorFlyweight.getDisjunctionString() + " " + right, true);
                    if (inDerived(newAST)) continue;

                    derived.add(newAST);
                    KnowledgeBaseRegistry.addEntry(newAST.toString(), "From " + ast + ", by applying " + name() + ", we derive " + newAST, List.of(ast.toString()));
                    shouldInference = true;
                }
            }
        }

        return shouldInference;
    }

    @Override
    public List<AST> inference(List<AST> kb, AST goal) {
        derived.clear();
        if (!canInference(kb, goal)) return new ArrayList<>();
        return derived;
    }

    @Override
    public List<SubGoal> getSubGoals(List<AST> knowledgeBase, AST... asts) {
        return List.of();
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
