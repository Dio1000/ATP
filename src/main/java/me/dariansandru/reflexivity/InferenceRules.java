package me.dariansandru.reflexivity;

import me.dariansandru.domain.proof.inference_rules.InferenceRule;

import java.util.List;

public interface InferenceRules {
    List<InferenceRule> get();
    List<InferenceRule> getCustom(String path);
}
