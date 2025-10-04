package me.dariansandru.reflexivity;

import me.dariansandru.domain.proof.inference_rules.InferenceRule;
import me.dariansandru.domain.proof.inference_rules.propositional.*;

import java.util.ArrayList;
import java.util.List;

public class PropositionalInferenceRules implements InferenceRules {

    @Override
    public List<InferenceRule> get() {
        List<InferenceRule> rules = new ArrayList<>();
        rules.add(new ModusPonens());
        rules.add(new ModusTollens());
        rules.add(new ConjunctionElimination());
        rules.add(new ConjunctionIntroduction());
        rules.add(new DisjunctionElimination());
        rules.add(new DisjunctionIntroduction());
        rules.add(new EquivalenceIntroduction());
        rules.add(new EquivalenceElimination());
        rules.add(new ImplicationIntroduction());
        rules.add(new ImplicationElimination());
        rules.add(new DeMorgan());

        return rules;
    }
}
