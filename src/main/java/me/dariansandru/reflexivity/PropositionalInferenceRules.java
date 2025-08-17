package me.dariansandru.reflexivity;

import me.dariansandru.domain.proof.inference_rules.InferenceRule;
import me.dariansandru.domain.proof.inference_rules.propositional.ModusPonens;
import me.dariansandru.domain.proof.inference_rules.propositional.ModusTollens;

import java.util.ArrayList;
import java.util.List;

public class PropositionalInferenceRules implements InferenceRules {

    @Override
    public List<InferenceRule> get() {
        List<InferenceRule> rules = new ArrayList<>();
        rules.add(new ModusPonens());
        rules.add(new ModusTollens());

        return rules;
    }
}
