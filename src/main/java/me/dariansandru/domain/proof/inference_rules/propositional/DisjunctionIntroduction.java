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

        if (PropositionalLogicHelper.getOutermostOperation(goal) == LogicalOperator.DISJUNCTION) {
            AST left = goal.getSubtree(0);
            AST right = goal.getSubtree(1);

            for (AST ast : asts) {
                if (ast.isEquivalentTo(left)) {
                    AST newAST = new PropositionalAST(ast + " " + LogicalOperatorFlyweight.getDisjunctionString() + " " + right, true);
                    KnowledgeBaseRegistry.addEntry(newAST.toString(), "From " + ast + ", by " + name() + ", we derive " + newAST, List.of(ast.toString()));
                    derived.add(newAST);
                    shouldInference = true;
                    break;
                }
                else if (ast.isEquivalentTo(right)) {
                    AST newAST = new PropositionalAST(left + " " + LogicalOperatorFlyweight.getDisjunctionString() + " " + ast, true);
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
}
