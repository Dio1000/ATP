package me.dariansandru.utils.factory;

import me.dariansandru.domain.proof.inference_rules.InferenceRule;
import me.dariansandru.domain.proof.inference_rules.propositional.ContradictionRule;
import me.dariansandru.domain.proof.inference_rules.propositional.*;

public class PropositionalInferenceRuleFactory {

    public static InferenceRule create(PropositionalInferenceRule rule) {
        return switch (rule) {
            case HYPOTHESIS, NO_RULE -> null;
            case CONTRADICTION -> new ContradictionRule();
            case MODUS_PONENS -> new ModusPonens();
            case MODUS_TOLLENS -> new ModusTollens();
            case CONJUNCTION_ELIMINATION -> new ConjunctionElimination();
            case CONJUNCTION_INTRODUCTION -> new ConjunctionIntroduction();
            case DISJUNCTION_ELIMINATION -> new DisjunctionElimination();
            case DISJUNCTION_INTRODUCTION -> new DisjunctionIntroduction();
            case EQUIVALENCE_INTRODUCTION -> new EquivalenceIntroduction();
            case EQUIVALENCE_ELIMINATION -> new EquivalenceElimination();
            case IMPLICATION_INTRODUCTION -> new ImplicationIntroduction();
            case IMPLICATION_ELIMINATION -> new ImplicationElimination();
        };
    }
}
