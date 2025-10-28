package me.dariansandru.utils.helper;

import me.dariansandru.domain.language.LogicalOperator;
import me.dariansandru.domain.language.logical_operator.*;
import me.dariansandru.domain.language.predicate.Predicate;
import me.dariansandru.domain.data_structures.ast.AST;
import me.dariansandru.domain.data_structures.ast.ASTNode;
import me.dariansandru.domain.data_structures.ast.PropositionalAST;
import me.dariansandru.domain.data_structures.ast.PropositionalASTNode;

import java.util.HashSet;
import java.util.Set;

public abstract class PropositionalLogicHelper {

    private static int NArityConstant = 1_000_000_000;

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

    public static Set<AST> getAtoms(AST ast) {
        Set<AST> atoms = new HashSet<>();
        collectAtoms((PropositionalASTNode) ast.getRoot(), atoms);
        return atoms;
    }

    private static void collectAtoms(PropositionalASTNode node, Set<AST> atoms) {
        if (node == null || node.getKey() == null) return;

        Predicate predicate = (Predicate) node.getKey();

        if (predicate.getArity() == 0) {
            AST ast = new PropositionalAST(predicate.getRepresentation());
            ast.validate(0);
            atoms.add(ast);
        }

        for (ASTNode child : node.getChildren()) {
            collectAtoms((PropositionalASTNode) child, atoms);
        }
    }

    public static int getNArityConstant() {
        return NArityConstant;
    }
}
