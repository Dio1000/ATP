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

public class DisjunctionIntroduction implements InferenceRule {

    private final List<AST> derived = new ArrayList<>();

    @Override
    public String name() {
        return "Disjunction Introduction";
    }

    @Override
    public boolean canInference(List<AST> asts, AST goal) {
        boolean shouldInference = false;

        if (goal != null && PropositionalLogicHelper.getOutermostOperation(goal) == LogicalOperator.DISJUNCTION) {
            PropositionalAST left = (PropositionalAST) goal.getSubtree(0);
            PropositionalAST right = (PropositionalAST) goal.getSubtree(1);

            for (AST ast : asts) {
                if (ast.isEquivalentTo(left)) {
                    AST newAST = PropositionalLogicHelper.buildFormula((PropositionalAST) ast, right, LogicalOperatorFlyweight.getDisjunctionString());
                    if (inDerived(newAST)) continue;

                    KnowledgeBaseRegistry.addEntry(newAST.toString(), "From " + ast + ", by " + name() + ", we derive " + newAST, List.of(ast.toString()));
                    derived.add(newAST);
                    shouldInference = true;
                    break;
                }
                else if (ast.isEquivalentTo(right)) {
                    AST newAST = PropositionalLogicHelper.buildFormula(left, (PropositionalAST) ast, LogicalOperatorFlyweight.getDisjunctionString());
                    if (inDerived(newAST)) continue;

                    KnowledgeBaseRegistry.addEntry(newAST.toString(), "From " + ast + ", by " + name() + ", we derive " + newAST, List.of(ast.toString()));
                    derived.add(newAST);
                    shouldInference = true;
                    break;
                }
            }
        }
        return shouldInference;
    }

    @Override
    public List<AST> inference(List<AST> asts, AST goal) {
        derived.clear();
        if (canInference(asts, goal)) return derived;
        else return new ArrayList<>();
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