package me.dariansandru.domain.proof.inference_rules.propositional;

public enum PropositionalInferenceRule {
    HYPOTHESIS,
    CONTRADICTION,
    MODUS_PONENS,
    MODUS_TOLLENS,
    CONJUNCTION_ELIMINATION,
    CONJUNCTION_INTRODUCTION,
    DISJUNCTION_ELIMINATION,
    DISJUNCTION_INTRODUCTION,
    EQUIVALENCE_INTRODUCTION,
    NO_RULE;

    @Override
    public String toString() {
        return switch (this) {
            case HYPOTHESIS -> "Hypothesis";
            case CONTRADICTION -> "Contradiction";
            case MODUS_PONENS -> "Modus Ponens";
            case MODUS_TOLLENS -> "Modus Tollens";
            case CONJUNCTION_ELIMINATION -> "Conjunction Elimination";
            case CONJUNCTION_INTRODUCTION -> "Conjunction Introduction";
            case DISJUNCTION_ELIMINATION -> "Disjunction Elimination";
            case DISJUNCTION_INTRODUCTION -> "Disjunction Introduction";
            case EQUIVALENCE_INTRODUCTION -> "Equivalence Introduction";
            case NO_RULE -> "No Rule";
        };
    }
}
