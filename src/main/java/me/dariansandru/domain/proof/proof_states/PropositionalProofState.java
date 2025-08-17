package me.dariansandru.domain.proof.proof_states;

import me.dariansandru.domain.formula.Formula;
import me.dariansandru.domain.proof.inference_rules.InferenceRule;
import me.dariansandru.reflexivity.PropositionalInferenceRules;

import java.util.List;

public class PropositionalProofState implements ProofState {

    private final List<Formula> knowledgeBase;
    private final List<Formula> goals;
    private List<InferenceRule> inferenceRules;

    public PropositionalProofState(List<Formula> knowledgeBase,
                                   List<Formula> goals) {
        this.knowledgeBase = knowledgeBase;
        this.goals = goals;
        initInferenceRules();
    }

    @Override
    public List<Formula> getKnowledgeBase() {
        return knowledgeBase;
    }

    @Override
    public List<Formula> getGoals() {
        return goals;
    }

    @Override
    public List<InferenceRule> getInferenceRules() {
        return inferenceRules;
    }

    @Override
    public void initInferenceRules() {
        this.inferenceRules = PropositionalInferenceRules.get();
    }

}
