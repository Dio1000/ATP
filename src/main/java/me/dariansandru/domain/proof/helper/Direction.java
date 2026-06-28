package me.dariansandru.domain.proof.helper;

import me.dariansandru.domain.language.LogicalOperator;

/**
 * A Direction is used in building the Path taken from a Node in a Tree
 * to another Node.
 * @param operator Operator that is on the current node
 * @param child Index of the child that the pointer from the Path should go to
 */
public record Direction(LogicalOperator operator, int child) {

    @Override
    public String toString() {
        return "Operator: " + operator + ", Child index: " + child;
    }
}
