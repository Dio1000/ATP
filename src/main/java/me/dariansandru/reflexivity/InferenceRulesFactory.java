package me.dariansandru.reflexivity;

import me.dariansandru.domain.UniverseOfDiscourse;
import me.dariansandru.domain.proof.inference_rules.InferenceRule;
import me.dariansandru.domain.signature.Signature;
import me.dariansandru.tokenizer.Type;

import java.util.ArrayList;
import java.util.List;

public class InferenceRulesFactory {

    public static List<InferenceRule> createRules(Signature signature) {
        UniverseOfDiscourse universeOfDiscourse = signature.getUniverseOfDiscourse();
        switch (universeOfDiscourse) {
            case PROPOSITIONS -> new PropositionalInferenceRules().get();
            case INTEGER_NUMBERS -> new IntegerInferenceRules().get();
            case RATIONAL_NUMBERS -> new RationalInferenceRules().get();
            case REAL_NUMBERS -> new RealInferenceRules().get();
            case STRINGS -> new StringInferenceRules().get();
            default -> throw new IllegalStateException("Universe of discourse '" + universeOfDiscourse + "' does not exist!");
        }

        return new ArrayList<>();
    }
}
