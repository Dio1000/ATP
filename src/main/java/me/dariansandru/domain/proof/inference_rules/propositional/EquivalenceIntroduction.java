package me.dariansandru.domain.proof.inference_rules.propositional;

import me.dariansandru.domain.LogicalOperator;
import me.dariansandru.domain.proof.SubGoal;
import me.dariansandru.domain.proof.inference_rules.InferenceRule;
import me.dariansandru.domain.data_structures.ast.AST;
import me.dariansandru.domain.data_structures.ast.PropositionalAST;
import me.dariansandru.utils.helper.PropositionalLogicHelper;

import java.util.ArrayList;
import java.util.List;

public class EquivalenceIntroduction implements InferenceRule {

    private AST leftImplication = null;
    private AST rightImplication = null;
    private AST leftSubtree = null;
    private AST rightSubtree = null;

    private AST leftAtom = null;
    private AST rightAtom = null;

    private List<AST> derived = new ArrayList<>();

    @Override
    public String getName() {
        return "Equivalence Introduction";
    }

    @Override
    public boolean canInference(List<AST> asts, AST goal) {
        for (AST ast : asts) {
            if (!(ast instanceof PropositionalAST)) continue;

            if (((PropositionalAST) ast).isAtomic() && leftAtom == null) leftAtom = ast;
            else if (((PropositionalAST) ast).isAtomic() && !leftAtom.isEquivalentTo(ast)) rightAtom = ast;

            if (PropositionalLogicHelper.getOutermostOperation(ast) == LogicalOperator.IMPLICATION &&
                    leftImplication == null) {
                leftImplication = ast;
                leftSubtree = ast.getSubtree(0);
                rightSubtree = ast.getSubtree(1);
            }
            else if (PropositionalLogicHelper.getOutermostOperation(ast) == LogicalOperator.IMPLICATION &&
                    leftImplication != null) {
                AST currentLeftSubtree = ast.getSubtree(0);
                AST currentRightSubtree = ast.getSubtree(1);
                if (leftSubtree.isEquivalentTo(currentRightSubtree) && rightSubtree.isEquivalentTo(currentLeftSubtree)) {
                    rightImplication = ast;
                }
            }

            if ((leftImplication != null && rightImplication != null) ||
                    (leftAtom != null && rightAtom != null)) return true;
        }

        return false;
    }

    @Override
    public List<AST> inference(List<AST> asts, AST goal) {
//        if (leftAtom != null && rightAtom != null) {
//            PropositionalAST newAST = new PropositionalAST(leftAtom + " " + new Equivalence().getRepresentation() + " " + rightAtom);
//            newAST.validate(0);
//            return List.of(newAST);
//        }
//
//        PropositionalAST newAST = new PropositionalAST(leftSubtree + " " + new Equivalence().getRepresentation() + " " + rightSubtree);
//        newAST.validate(0);
//        return List.of(newAST);

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
}
