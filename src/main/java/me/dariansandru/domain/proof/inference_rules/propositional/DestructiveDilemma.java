package me.dariansandru.domain.proof.inference_rules.propositional;

import me.dariansandru.domain.data_structures.ast.AST;
import me.dariansandru.domain.data_structures.ast.PropositionalAST;
import me.dariansandru.domain.language.LogicalOperator;
import me.dariansandru.domain.proof.SubGoal;
import me.dariansandru.domain.proof.inference_rules.InferenceRule;
import me.dariansandru.utils.flyweight.LogicalOperatorFlyweight;
import me.dariansandru.utils.helper.KnowledgeBaseRegistry;
import me.dariansandru.utils.helper.PropositionalLogicHelper;

import java.util.ArrayList;
import java.util.List;

public class DestructiveDilemma implements InferenceRule {

    private final List<AST> derived = new ArrayList<>();

    @Override
    public String name() {
        return "Destructive Dilemma";
    }

    @Override
    public boolean canInference(List<AST> kb, AST goal) {

        boolean shouldInference = false;

        for (AST ast1 : kb) {
            if (PropositionalLogicHelper.getOutermostOperation(ast1) != LogicalOperator.IMPLICATION) continue;

            PropositionalAST ast1Left = (PropositionalAST) ast1.getSubtree(0);
            PropositionalAST ast1Right = (PropositionalAST) ast1.getSubtree(1);

            for (AST ast2 : kb) {
                if (ast1 == ast2 || PropositionalLogicHelper.getOutermostOperation(ast2) != LogicalOperator.IMPLICATION) continue;
                PropositionalAST ast2Left = (PropositionalAST) ast2.getSubtree(0);
                PropositionalAST ast2Right = (PropositionalAST) ast2.getSubtree(1);

                PropositionalAST ast1RightNegated = new PropositionalAST(ast1Right.getFormulaString(), true);
                ast1RightNegated.negate();
                PropositionalAST ast2RightNegated = new PropositionalAST(ast2Right.getFormulaString(), true);
                ast2RightNegated.negate();

                PropositionalAST expectedDisjunction = PropositionalLogicHelper.buildFormula(ast1RightNegated, ast2RightNegated, LogicalOperatorFlyweight.getDisjunctionString());

                for (AST ast3 : kb) {
                    if (PropositionalLogicHelper.getOutermostOperation(ast3) != LogicalOperator.DISJUNCTION) continue;
                    if (expectedDisjunction.isEquivalentTo(ast3)) {
                        PropositionalAST ast1LeftNegated = new PropositionalAST(ast1Left.getFormulaString(), true);
                        ast1LeftNegated.negate();
                        PropositionalAST ast2LeftNegated = new PropositionalAST(ast2Left.getFormulaString(), true);
                        ast2LeftNegated.negate();

                        PropositionalAST newAST = PropositionalLogicHelper.buildFormula(ast1LeftNegated, ast2LeftNegated, LogicalOperatorFlyweight.getDisjunctionString());

                        if (inDerived(newAST)) continue;

                        KnowledgeBaseRegistry.addEntry(newAST.toString(), "From " + ast1 + ", " + ast2 + " and " + ast3 + ", by " + name() + ", we derive " + newAST, List.of(ast1.toString(), ast2.toString(), ast3.toString()));

                        derived.add(newAST);
                        shouldInference = true;
                    }
                }
            }
        }

        return shouldInference;
    }

    @Override
    public List<AST> inference(List<AST> asts, AST goal) {
        derived.clear();
        if (canInference(asts, goal)) return derived;
        return new ArrayList<>();
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