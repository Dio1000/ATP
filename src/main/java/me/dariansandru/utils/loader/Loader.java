package me.dariansandru.utils.loader;

import me.dariansandru.domain.proof.inference_rules.CustomPropositionalInferenceRule;

import java.util.List;

public interface Loader {
    List<String> getLines(String path);
    CustomPropositionalInferenceRule loadCustomRule(List<String> lines, String ruleName);
}
