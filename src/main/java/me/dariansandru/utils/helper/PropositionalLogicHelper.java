package me.dariansandru.utils.helper;

import me.dariansandru.domain.LogicalOperator;
import me.dariansandru.domain.logical_operator.*;
import me.dariansandru.domain.predicate.Predicate;
import me.dariansandru.utils.data_structures.ast.AST;
import me.dariansandru.utils.data_structures.ast.PropositionalASTNode;

public abstract class PropositionalLogicHelper {

    public static LogicalOperator getOutermostOperation(AST ast) {
        PropositionalASTNode node;
        try {
            node = (PropositionalASTNode) ast.getRoot();
        } catch (Exception e) {
            return LogicalOperator.NOT_A_LOGICAL_OPERATOR;
        }

        Predicate predicate = (Predicate) node.getKey();
        if (predicate == null) return LogicalOperator.NOT_A_LOGICAL_OPERATOR;

        if (predicate.getRepresentation().equals(new Implication().getRepresentation())) return LogicalOperator.IMPLICATION;
        else if (predicate.getRepresentation().equals(new Equivalence().getRepresentation())) return LogicalOperator.EQUIVALENCE;
        else if (predicate.getRepresentation().equals(new Conjunction().getRepresentation())) return LogicalOperator.CONJUNCTION;
        else if (predicate.getRepresentation().equals(new Disjunction().getRepresentation())) return LogicalOperator.DISJUNCTION;
        else if (predicate.getRepresentation().equals(new Negation().getRepresentation())) return LogicalOperator.NEGATION;
        else return LogicalOperator.NOT_A_LOGICAL_OPERATOR;
    }
}
