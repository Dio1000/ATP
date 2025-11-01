package me.dariansandru.domain.proof.helper;

import me.dariansandru.domain.language.LogicalOperator;

public record Direction(LogicalOperator operator, int child) {

    @Override
    public String toString() {
        return "Operator: " + operator + ", Child index: " + child;
    }
}
