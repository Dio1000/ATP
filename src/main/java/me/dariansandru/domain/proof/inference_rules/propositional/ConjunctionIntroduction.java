package me.dariansandru.domain.proof.inference_rules.propositional;

import me.dariansandru.domain.logical_operator.Conjunction;
import me.dariansandru.domain.proof.SubGoal;
import me.dariansandru.domain.proof.inference_rules.InferenceRule;
import me.dariansandru.utils.data_structures.ast.AST;
import me.dariansandru.utils.data_structures.ast.PropositionalAST;

import java.util.List;

public class ConjunctionIntroduction implements InferenceRule {

    private AST left = null;
    private AST right = null;

    @Override
    public String getName() {
        return "Conjunction Introduction";
    }

    @Override
    public boolean canInference(List<AST> asts) {
        for (AST ast : asts) {
            if (!(ast instanceof PropositionalAST)) continue;
            if (((PropositionalAST) ast).isAtomic() && left == null) left = ast;
            else if (((PropositionalAST) ast).isAtomic() && !ast.isEquivalentTo(left)) right = ast;

            if (left != null && right != null) return true;
        }

        return false;
    }

    @Override
    public AST inference(List<AST> asts) {
        PropositionalAST newAST = new PropositionalAST(left + " " + new Conjunction().getRepresentation() + " " + right);
        newAST.validate(0);
        return newAST;
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
