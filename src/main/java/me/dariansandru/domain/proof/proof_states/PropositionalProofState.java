package me.dariansandru.domain.proof.proof_states;

import me.dariansandru.domain.formula.Formula;
import me.dariansandru.domain.proof.inference_rules.InferenceRule;

import java.util.List;

public class PropositionalProofState {

    private final List<Formula> knowledgeBase;
    private final List<Formula> goals;
    private List<InferenceRule> inferenceRules;

    public PropositionalProofState(List<Formula> knowledgeBase,
                                   List<Formula> goals,
                                   List<InferenceRule> inferenceRules) {
        this.knowledgeBase = knowledgeBase;
        this.goals = goals;
        this.inferenceRules = inferenceRules;
        initInferenceRules();
    }

    public List<Formula> getKnowledgeBase() {
        return knowledgeBase;
    }

    public List<Formula> getGoals() {
        return goals;
    }

    public List<InferenceRule> getInferenceRules() {
        return inferenceRules;
    }

    private void initInferenceRules() {

    }

}
