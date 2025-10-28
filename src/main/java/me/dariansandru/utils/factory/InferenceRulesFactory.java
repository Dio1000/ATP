package me.dariansandru.utils.factory;

import me.dariansandru.domain.language.UniverseOfDiscourse;
import me.dariansandru.domain.proof.inference_rules.InferenceRule;
import me.dariansandru.domain.language.signature.Signature;
import me.dariansandru.reflexivity.*;
import me.dariansandru.utils.loader.LoaderPackageHelper;

import java.util.List;

public class InferenceRulesFactory {

    public static List<InferenceRule> createRules(Signature signature) {
        UniverseOfDiscourse universeOfDiscourse = signature.getUniverseOfDiscourse();
        List<InferenceRule> inferenceRules;
        String packageName = LoaderPackageHelper.getPackageName();

        if (packageName.equals("Classical")) {
            switch (universeOfDiscourse) {
                case PROPOSITIONS -> inferenceRules = new PropositionalInferenceRules().get();
                case INTEGER_NUMBERS -> inferenceRules = new IntegerInferenceRules().get();
                case RATIONAL_NUMBERS -> inferenceRules = new RationalInferenceRules().get();
                case REAL_NUMBERS -> inferenceRules = new RealInferenceRules().get();
                case STRINGS -> inferenceRules = new StringInferenceRules().get();
                default -> throw new IllegalStateException("Universe of discourse '" + universeOfDiscourse + "' does not exist!");
            }
        }
        else {
            switch (universeOfDiscourse) {
                case PROPOSITIONS -> inferenceRules = new PropositionalInferenceRules().getCustom(packageName);
                case INTEGER_NUMBERS -> inferenceRules = new IntegerInferenceRules().getCustom(packageName);
                case RATIONAL_NUMBERS -> inferenceRules = new RationalInferenceRules().getCustom(packageName);
                case REAL_NUMBERS -> inferenceRules = new RealInferenceRules().getCustom(packageName);
                case STRINGS -> inferenceRules = new StringInferenceRules().getCustom(packageName);
                default -> throw new IllegalStateException("Universe of discourse '" + universeOfDiscourse + "' does not exist!");
            }
        }

        return inferenceRules;
    }
}
