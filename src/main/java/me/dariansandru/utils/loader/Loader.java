package me.dariansandru.utils.loader;

import me.dariansandru.domain.proof.inference_rules.custom.CustomPropositionalInferenceRule;

import java.util.List;

/**
 * Implementing this interface allows the user to extend the system with a new Loader.
 * This is useful when creating new Signatures, enabling extension with new custom inference rules.
 * With new customs rules, a new Loader is needed to fetch them.
 */
public interface Loader {
    List<String> getLines(String path);
    CustomPropositionalInferenceRule loadCustomRule(List<String> lines, String ruleName);
}
