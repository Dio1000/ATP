package me.dariansandru.domain.proof.inference_rules.propositional;

import me.dariansandru.domain.data_structures.ast.AST;
import me.dariansandru.domain.data_structures.ast.PropositionalAST;
import me.dariansandru.domain.language.LogicalOperator;
import me.dariansandru.domain.proof.SubGoal;
import me.dariansandru.domain.proof.inference_rules.InferenceRule;
import me.dariansandru.utils.helper.KnowledgeBaseRegistry;
import me.dariansandru.utils.helper.PropositionalLogicHelper;

import java.util.ArrayList;
import java.util.List;

public class DisjunctiveSyllogism implements InferenceRule {

    private final List<AST> derived = new ArrayList<>();

    @Override
    public String name() {
        return "Disjunctive Syllogism";
    }

    @Override
    public boolean canInference(List<AST> kb, AST goal) {

        boolean shouldInference = false;

        for (AST ast1 : kb) {
            if (PropositionalLogicHelper.getOutermostOperation(ast1) != LogicalOperator.DISJUNCTION) continue;

            PropositionalAST left = (PropositionalAST) ast1.getSubtree(0);
            PropositionalAST right = (PropositionalAST) ast1.getSubtree(1);

            PropositionalAST leftNegated = new PropositionalAST(left.getFormulaString(), true);
            leftNegated.negate();
            PropositionalAST rightNegated = new PropositionalAST(right.getFormulaString(), true);
            rightNegated.negate();

            for (AST ast2 : kb) {
                if (leftNegated.isEquivalentTo(ast2)) {
                    if (inDerived(right)) continue;
                    KnowledgeBaseRegistry.addEntry(right.toString(), "From " + ast1 + " and " + ast2 + ", by " + name() + ", we derive " + right, List.of(ast1.toString(), ast2.toString()));
                    derived.add(right);
                    shouldInference = true;
                }
                else if (rightNegated.isEquivalentTo(ast2)) {
                    if (inDerived(left)) continue;
                    KnowledgeBaseRegistry.addEntry(left.toString(), "From " + ast1 + " and " + ast2 + ", by " + name() + ", we derive " + left, List.of(ast1.toString(), ast2.toString()));
                    derived.add(left);
                    shouldInference = true;
                }
            }
        }

        return shouldInference;
    }

    @Override
    public List<AST> inference(List<AST> kb, AST goal) {
        derived.clear();
        if (canInference(kb, goal)) return derived;
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