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

public class HypotheticalSyllogism implements InferenceRule {

    private final List<AST> derived = new ArrayList<>();

    @Override
    public String name() {
        return "Hypothetical Syllogism";
    }

    @Override
    public boolean canInference(List<AST> kb, AST goal) {
        boolean shouldInference = false;

        for (AST ast1 : kb) {
            if (PropositionalLogicHelper.getOutermostOperation(ast1) == LogicalOperator.IMPLICATION) {
                PropositionalAST left1 = (PropositionalAST) ast1.getSubtree(0);
                PropositionalAST right1 = (PropositionalAST) ast1.getSubtree(1);

                for (AST ast2 : kb) {
                    if (PropositionalLogicHelper.getOutermostOperation(ast2) == LogicalOperator.IMPLICATION) {
                        PropositionalAST left2 = (PropositionalAST) ast2.getSubtree(0);
                        PropositionalAST right2 = (PropositionalAST) ast2.getSubtree(1);

                        if (right1.isEquivalentTo(left2)) {
                            PropositionalAST newAST = PropositionalLogicHelper.buildFormula(left1, right2, LogicalOperatorFlyweight.getImplicationString());
                            if (inDerived(newAST)) continue;

                            KnowledgeBaseRegistry.addEntry(newAST.toString(), "From " + ast1 + " and " + ast2 + ", by " + name() + ", we derive " + newAST, List.of(ast1.toString(), ast2.toString()));

                            derived.add(newAST);
                            shouldInference = true;
                        }
                    }
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