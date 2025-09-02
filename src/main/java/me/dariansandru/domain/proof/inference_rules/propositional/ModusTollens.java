package me.dariansandru.domain.proof.inference_rules.propositional;

import me.dariansandru.domain.LogicalOperator;
import me.dariansandru.domain.logical_operator.Implication;
import me.dariansandru.domain.predicate.Predicate;
import me.dariansandru.domain.proof.SubGoal;
import me.dariansandru.domain.proof.inference_rules.InferenceRule;
import me.dariansandru.domain.data_structures.ast.AST;
import me.dariansandru.domain.data_structures.ast.PropositionalAST;
import me.dariansandru.domain.data_structures.ast.PropositionalASTNode;
import me.dariansandru.utils.helper.PropositionalLogicHelper;

import java.util.ArrayList;
import java.util.List;

public class ModusTollens implements InferenceRule {

    private AST implicationAST = null;
    private List<AST> derived = new ArrayList<>();

    @Override
    public String getName() {
        return "Modus Tollens";
    }

    @Override
    public boolean canInference(List<AST> asts, AST goal) {
        implicationAST = null;

        for (AST candidate : asts) {
            if (!(candidate.getRoot() instanceof PropositionalASTNode)) continue;

            Predicate predicate = (Predicate) ((PropositionalASTNode) candidate.getRoot()).getKey();
            if (predicate == null) continue;

            if (predicate.getRepresentation().equals(new Implication().getRepresentation())) {
                AST consequent = candidate.getSubtree(1);
                AST negatedConsequent = new PropositionalAST(consequent.toString());
                negatedConsequent.validate(0);
                negatedConsequent.negate();

                for (AST other : asts) {
                    if (other != candidate && negatedConsequent.isEquivalentTo(other)) {
                        implicationAST = candidate;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public List<AST> inference(List<AST> asts, AST goal) {
        return derived;
    }

    @Override
    public List<SubGoal> getSubGoals(List<AST> knowledgeBase, AST... asts) {
        if (asts.length != 1) return new ArrayList<>();
        List<SubGoal> subGoals = new ArrayList<>();

        for (AST ast : knowledgeBase) {
            if (!(ast instanceof PropositionalAST)) continue;
            if (PropositionalLogicHelper.getOutermostOperation(ast) == LogicalOperator.IMPLICATION) {
                PropositionalAST left = (PropositionalAST) ast.getSubtree(0);
                PropositionalAST right = (PropositionalAST) ast.getSubtree(1);
                left.negate();

                if (left.isEquivalentTo(asts[0])) {
                    right.validate(0);
                    right.negate();

                    SubGoal newSubGoal = new SubGoal(right, PropositionalInferenceRule.MODUS_TOLLENS, ast);
                    subGoals.add(newSubGoal);
                }
            }
        }

        return subGoals;
    }

    @Override
    public String getText(SubGoal subGoal) {
        AST negatedFormula = subGoal.getFormula().getSubtree(0);
        negatedFormula.negate();
        return "From " + subGoal.getGoal() + " and " + subGoal.getFormula() + ", by Modus Tollens, " + "we derive " + negatedFormula;

    }

}
