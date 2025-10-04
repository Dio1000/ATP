package me.dariansandru.domain.proof.inference_rules.propositional;

import me.dariansandru.domain.proof.SubGoal;
import me.dariansandru.domain.proof.inference_rules.InferenceRule;
import me.dariansandru.domain.data_structures.ast.AST;
import me.dariansandru.domain.data_structures.ast.PropositionalAST;
import me.dariansandru.utils.flyweight.LogicalOperatorFlyweight;
import me.dariansandru.utils.helper.KnowledgeBaseRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ConjunctionIntroduction implements InferenceRule {

    private final List<AST> derived = new ArrayList<>();

    @Override
    public String getName() {
        return "Conjunction Introduction";
    }

    @Override
    public boolean canInference(List<AST> asts, AST goal) {
        boolean shouldInference = false;

        for (AST ast : asts) {
            if (((PropositionalAST) ast).isAtomic()) {
                for (AST ast1 : asts) {
                    if (((PropositionalAST) ast1).isAtomic() && !ast.isEquivalentTo(ast1)) {
                        PropositionalAST newAST = new PropositionalAST(ast + " " + LogicalOperatorFlyweight.getConjunctionString() + " " + ast1, true);

                        boolean duplicate = false;
                        for (AST astString : asts) {
                            if (Objects.equals(astString.toString(), newAST.toString())) {
                                duplicate = true;
                                break;
                            }
                        }

                        if (duplicate) continue;
                        KnowledgeBaseRegistry.addEntry(newAST.toString(), "From " + ast + " and " + ast1 + " by " + getName() + ", we derive " + newAST, List.of(ast.toString(), ast1.toString()));
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
}
