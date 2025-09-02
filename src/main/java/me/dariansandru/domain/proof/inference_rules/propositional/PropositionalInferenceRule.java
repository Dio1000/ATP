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
    EQUIVALENCE_ELIMINATION,
    IMPLICATION_INTRODUCTION,
    IMPLICATION_ELIMINATION,
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
            case EQUIVALENCE_ELIMINATION ->  "Equivalence Elimination";
            case IMPLICATION_INTRODUCTION -> "Implication Introduction";
            case IMPLICATION_ELIMINATION -> "Implication Elimination";
            case NO_RULE -> "No Rule";
        };
    }
}
