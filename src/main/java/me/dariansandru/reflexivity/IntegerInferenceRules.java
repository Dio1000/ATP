package me.dariansandru.reflexivity;

import me.dariansandru.domain.proof.inference_rules.InferenceRule;

import java.util.List;

public class IntegerInferenceRules implements InferenceRules{
    @Override
    public List<InferenceRule> get() {
        return List.of();
    }

    @Override
    public List<InferenceRule> getCustom(String path) {
        return List.of();
    }
}
