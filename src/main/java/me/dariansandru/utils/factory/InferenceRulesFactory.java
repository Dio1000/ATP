package me.dariansandru.utils.factory;

import me.dariansandru.domain.UniverseOfDiscourse;
import me.dariansandru.domain.proof.inference_rules.InferenceRule;
import me.dariansandru.domain.signature.Signature;
import me.dariansandru.reflexivity.*;

import java.util.List;

public class InferenceRulesFactory {

    public static List<InferenceRule> createRules(Signature signature) {
        UniverseOfDiscourse universeOfDiscourse = signature.getUniverseOfDiscourse();
        List<InferenceRule> inferenceRules;
        switch (universeOfDiscourse) {
            case PROPOSITIONS -> inferenceRules = new PropositionalInferenceRules().get();
            case INTEGER_NUMBERS -> inferenceRules = new IntegerInferenceRules().get();
            case RATIONAL_NUMBERS -> inferenceRules = new RationalInferenceRules().get();
            case REAL_NUMBERS -> inferenceRules = new RealInferenceRules().get();
            case STRINGS -> inferenceRules = new StringInferenceRules().get();
            default -> throw new IllegalStateException("Universe of discourse '" + universeOfDiscourse + "' does not exist!");
        }

        return inferenceRules;
    }
}
