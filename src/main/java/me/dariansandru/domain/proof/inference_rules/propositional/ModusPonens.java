package me.dariansandru.domain.proof.inference_rules.propositional;

import me.dariansandru.domain.LogicalOperator;
import me.dariansandru.domain.proof.SubGoal;
import me.dariansandru.domain.proof.inference_rules.InferenceRule;
import me.dariansandru.domain.data_structures.ast.AST;
import me.dariansandru.domain.data_structures.ast.PropositionalAST;
import me.dariansandru.utils.helper.KnowledgeBaseRegistry;
import me.dariansandru.utils.helper.PropositionalLogicHelper;

import java.util.ArrayList;
import java.util.List;

public class ModusPonens implements InferenceRule {

    private final List<AST> derived = new ArrayList<>();

    @Override
    public String getName() {
        return "Modus Ponens";
    }

    @Override
    public boolean canInference(List<AST> asts, AST goal) {
        derived.clear();

        for (AST candidate : asts) {
            if (!(candidate instanceof PropositionalAST pCandidate)) continue;

            if (PropositionalLogicHelper.getOutermostOperation(pCandidate) != LogicalOperator.IMPLICATION) continue;

            AST antecedent = pCandidate.getSubtree(0);
            AST conclusion = pCandidate.getSubtree(1);

            for (AST other : asts) {
                if (other != candidate && other.isEquivalentTo(antecedent)) {
                    KnowledgeBaseRegistry.addEntry(conclusion.toString(), "From " + candidate + " and " + antecedent + ", by " + getName() + ", we derive " + conclusion, List.of(candidate.toString()));
                    derived.add(conclusion);
                    break;
                }
            }
        }

        return !derived.isEmpty();
    }

    @Override
    public List<AST> inference(List<AST> asts, AST goal) {
        if (!canInference(asts, goal)) return new ArrayList<>();
        return derived;
    }

    @Override
    public List<SubGoal> getSubGoals(List<AST> knowledgeBase, AST... asts) {
        if (asts.length != 1) return new ArrayList<>();
        List<SubGoal> subGoals = new ArrayList<>();

        for (AST ast : knowledgeBase) {
            if (!(ast instanceof PropositionalAST)) continue;
            if (PropositionalLogicHelper.getOutermostOperation(ast) == LogicalOperator.IMPLICATION) {
                PropositionalAST right = (PropositionalAST) ast.getSubtree(1);
                if (right.isEquivalentTo(asts[0])) {
                    PropositionalAST newGoal = (PropositionalAST) ast.getSubtree(0);
                    // newGoal.validate(0);
                    SubGoal newSubGoal = new SubGoal(newGoal, PropositionalInferenceRule.MODUS_PONENS, ast);

                    KnowledgeBaseRegistry.addEntry(right.toString(), "From " + newSubGoal.getGoal() + " and " + ast + ", by " + getName() + ", we derive " + right, List.of(ast.toString()));
                    subGoals.add(newSubGoal);
                }
            }
        }

        return subGoals;
    }

    @Override
    public String getText(SubGoal subGoal) {
        return "From " + subGoal.getGoal() + " and " + subGoal.getFormula() + ", by Modus Ponens, " + "we derive " + subGoal.getFormula().getSubtree(1);
    }
}
