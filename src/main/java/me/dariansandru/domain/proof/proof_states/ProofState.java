package me.dariansandru.domain.proof.proof_states;

import me.dariansandru.domain.formula.Formula;
import me.dariansandru.domain.proof.inference_rules.InferenceRule;

import java.util.List;

public interface ProofState {
    List<Formula> getKnowledgeBase();
    List<Formula> getGoals();
    List<InferenceRule> getInferenceRules();
    void initInferenceRules();
}
