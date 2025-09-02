package me.dariansandru.domain.proof.inference_rules.propositional;

import me.dariansandru.domain.LogicalOperator;
import me.dariansandru.domain.logical_operator.Disjunction;
import me.dariansandru.domain.proof.SubGoal;
import me.dariansandru.domain.proof.inference_rules.InferenceRule;
import me.dariansandru.domain.data_structures.ast.AST;
import me.dariansandru.domain.data_structures.ast.PropositionalAST;
import me.dariansandru.utils.helper.KnowledgeBaseRegistry;
import me.dariansandru.utils.helper.PropositionalLogicHelper;

import java.util.ArrayList;
import java.util.List;

public class DisjunctionIntroduction implements InferenceRule {

    private List<AST> derived = new ArrayList<>();

    @Override
    public String getName() {
        return "Disjunction Introduction";
    }

    @Override
    public boolean canInference(List<AST> asts, AST goal) {
        boolean shouldInference = false;

        for (AST ast : asts) {
            if (PropositionalLogicHelper.getOutermostOperation(ast) == LogicalOperator.IMPLICATION) {
                PropositionalAST left = (PropositionalAST) ast.getSubtree(0);
                PropositionalAST right = (PropositionalAST) ast.getSubtree(1);

                PropositionalAST negatedLeft = new PropositionalAST(left.toString());
                negatedLeft.validate(0);
                negatedLeft.negate();

                PropositionalAST newAST = new PropositionalAST(negatedLeft + " " + new Disjunction().getRepresentation() + " " + right);
                newAST.validate(0);

                KnowledgeBaseRegistry.addEntry(newAST.toString(), "From " + ast + ", we derive " + newAST, List.of(ast.toString()));
                derived.add(newAST);
                shouldInference = true;
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
