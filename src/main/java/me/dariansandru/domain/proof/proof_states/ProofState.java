package me.dariansandru.domain.proof.proof_states;

import me.dariansandru.domain.formula.Formula;
import me.dariansandru.domain.proof.inference_rules.InferenceRule;

import java.util.List;

public interface ProofState<F extends Formula, R extends InferenceRule> {
    List<F> getKnowledgeBase();
    List<F> getGoals();
    List<R> getInferenceRules();
}
