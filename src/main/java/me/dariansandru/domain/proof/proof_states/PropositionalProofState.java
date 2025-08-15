package me.dariansandru.domain.proof.proof_states;

import me.dariansandru.domain.formula.PropositionalFormula;
import me.dariansandru.domain.proof.inference_rules.PropositionalInferenceRule;

import java.util.List;

public class PropositionalProofState implements ProofState<PropositionalFormula, PropositionalInferenceRule> {

    private final List<PropositionalFormula> knowledgeBase;
    private final List<PropositionalFormula> goals;
    private final List<PropositionalInferenceRule> inferenceRules;

    public PropositionalProofState(List<PropositionalFormula> knowledgeBase,
                                   List<PropositionalFormula> goals,
                                   List<PropositionalInferenceRule> inferenceRules) {
        this.knowledgeBase = knowledgeBase;
        this.goals = goals;
        this.inferenceRules = inferenceRules;
    }

    @Override
    public List<PropositionalFormula> getKnowledgeBase() {
        return knowledgeBase;
    }

    @Override
    public List<PropositionalFormula> getGoals() {
        return goals;
    }

    @Override
    public List<PropositionalInferenceRule> getInferenceRules() {
        return inferenceRules;
    }

}
