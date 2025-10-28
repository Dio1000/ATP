package me.dariansandru.domain.proof.inference_rules.propositional;

import me.dariansandru.domain.language.LogicalOperator;
import me.dariansandru.domain.proof.SubGoal;
import me.dariansandru.domain.proof.inference_rules.InferenceRule;
import me.dariansandru.domain.data_structures.ast.AST;
import me.dariansandru.domain.data_structures.ast.PropositionalAST;
import me.dariansandru.utils.helper.KnowledgeBaseRegistry;
import me.dariansandru.utils.helper.PropositionalLogicHelper;

import java.util.ArrayList;
import java.util.List;

public class ModusTollens implements InferenceRule {

    private final List<AST> derived = new ArrayList<>();

    @Override
    public String name() {
        return "Modus Tollens";
    }

    @Override
    public boolean canInference(List<AST> asts, AST goal) {
        derived.clear();

        for (AST candidate : asts) {
            if (!(candidate instanceof PropositionalAST pCandidate)) continue;

            if (PropositionalLogicHelper.getOutermostOperation(pCandidate) != LogicalOperator.IMPLICATION) continue;

            AST antecedent = pCandidate.getSubtree(0);
            AST consequent = pCandidate.getSubtree(1);

            PropositionalAST negatedConsequent = new PropositionalAST(consequent.toString(), true);
            negatedConsequent.negate();

            for (AST other : asts) {
                if (other != candidate && other.isEquivalentTo(negatedConsequent)) {
                    PropositionalAST negatedAntecedent = new PropositionalAST(antecedent.toString(), true);
                    negatedAntecedent.negate();

                    KnowledgeBaseRegistry.addEntry(
                            negatedAntecedent.toString(),
                            "From " + other + " and " + candidate + ", by " + name() + ", we derive " + negatedAntecedent,
                            List.of(candidate.toString(), other.toString())
                    );

                    derived.add(negatedAntecedent);
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
                PropositionalAST antecedent = (PropositionalAST) ast.getSubtree(0);
                PropositionalAST consequent = (PropositionalAST) ast.getSubtree(1);

                PropositionalAST negatedConsequent = new PropositionalAST(consequent.toString(), true);
                negatedConsequent.negate();

                if (negatedConsequent.isEquivalentTo(asts[0])) {
                    PropositionalAST negatedAntecedent = new PropositionalAST(antecedent.toString(), true);
                    negatedAntecedent.negate();

                    KnowledgeBaseRegistry.addEntry(
                            negatedAntecedent.toString(),
                            "From " + ast + " and " + negatedConsequent + ", by " + name() + ", we derive " + negatedAntecedent,
                            List.of(ast.toString())
                    );

                    SubGoal newSubGoal = new SubGoal(negatedAntecedent, PropositionalInferenceRule.MODUS_TOLLENS, ast);
                    subGoals.add(newSubGoal);
                }
            }
        }

        return subGoals;
    }

    @Override
    public String getText(SubGoal subGoal) {
        AST negatedAntecedent = subGoal.getGoal();
        return "From " + subGoal.getGoal() + " and " + subGoal.getFormula() + ", by Modus Tollens, we derive " + negatedAntecedent;
    }
}
