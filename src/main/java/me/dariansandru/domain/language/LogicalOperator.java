package me.dariansandru.domain.language;

public enum LogicalOperator {
    CONJUNCTION("Conjunction"),
    DISJUNCTION("Disjunction"),
    IMPLICATION("Implication"),
    EQUIVALENCE("Equivalence"),
    NEGATION("Negation"),
    NOT_A_LOGICAL_OPERATOR("Not a logical operator");

    private final String displayName;

    LogicalOperator(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
