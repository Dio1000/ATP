package me.dariansandru.utils.helper;

import me.dariansandru.domain.language.LogicalOperator;
import me.dariansandru.domain.language.logical_operator.*;
import me.dariansandru.domain.language.predicate.Predicate;
import me.dariansandru.domain.data_structures.ast.AST;
import me.dariansandru.domain.data_structures.ast.ASTNode;
import me.dariansandru.domain.data_structures.ast.PropositionalAST;
import me.dariansandru.domain.data_structures.ast.PropositionalASTNode;
import me.dariansandru.utils.flyweight.LogicalOperatorFlyweight;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class PropositionalLogicHelper {

    private static int NArityConstant = 1_000_000_000;

    public static LogicalOperator getOutermostOperation(AST ast) {
        if (ast.isTautology() || ast.isContradiction()) return LogicalOperator.NOT_A_LOGICAL_OPERATOR;

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

    public static LogicalOperator getLogicalOperator(Predicate predicate) {
        if (predicate instanceof Implication) return LogicalOperator.IMPLICATION;
        else if (predicate instanceof Equivalence) return LogicalOperator.EQUIVALENCE;
        else if (predicate instanceof Conjunction) return LogicalOperator.CONJUNCTION;
        else if (predicate instanceof Disjunction) return LogicalOperator.DISJUNCTION;
        else if (predicate instanceof Negation) return LogicalOperator.NEGATION;
        else return LogicalOperator.NOT_A_LOGICAL_OPERATOR;
    }

    public static Set<String> getAtoms(AST ast) {
        Set<String> atoms = new HashSet<>();
        collectAtoms((PropositionalASTNode) ast.getRoot(), atoms);
        return atoms;
    }

    private static void collectAtoms(PropositionalASTNode node, Set<String> atoms) {
        if (node == null || node.getKey() == null) return;

        Predicate predicate = (Predicate) node.getKey();

        if (predicate.getArity() == 0) {
            atoms.add(predicate.getRepresentation());
        }

        for (ASTNode child : node.getChildren()) {
            collectAtoms((PropositionalASTNode) child, atoms);
        }
    }

    public static int getNArityConstant() {
        return NArityConstant;
    }

    public static List<AST> getRedundantAntecedents(List<AST> antecedents, AST conclusion) {
        List<AST> redundantAntecedents = new ArrayList<>();
        List<List<AST>> antecedentSubsets = getAntecedentSubsets(antecedents);

        for (List<AST> subset : antecedentSubsets) {
            PropositionalAST ast = (PropositionalAST) buildImplication(subset, conclusion);
            ast.buildBDD();

            if (ast.getBuilder().isTautology()) {
                for (AST antecedent : antecedents) {
                    if (!subsetContains(subset, antecedent) && !redundantAntecedents.contains(antecedent)) {
                        redundantAntecedents.add(antecedent);
                    }
                }
                return redundantAntecedents;
            }
        }
        return redundantAntecedents;
    }

    private static boolean subsetContains(List<AST> subset, AST ast) {
        for (AST element : subset) {
            if (element.isEquivalentTo(ast)) return true;
        }
        return false;
    }

    private static List<List<AST>> getAntecedentSubsets(List<AST> antecedents) {
        List<List<AST>> subsets = new ArrayList<>();
        int antecedentSize = antecedents.size();

        for (int mask = 1; mask < (1 << antecedentSize); mask++) {
            List<AST> subset = new ArrayList<>();
            for (int i = 0; i < antecedentSize; i++) {
                if ((mask & (1 << i)) != 0) {
                    subset.add(antecedents.get(i));
                }
            }
            subsets.add(subset);
        }
        return subsets;
    }

    public static AST buildImplication(List<AST> subset, AST conclusion) {
        if (subset.isEmpty()) return conclusion;

        String conclusionString = conclusion.toString();
        if (PropositionalLogicHelper.getOutermostOperation(conclusion) != LogicalOperator.NOT_A_LOGICAL_OPERATOR) {
            conclusionString = "(" + conclusionString + ")";
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < subset.size(); i++) {
            AST current = subset.get(i);
            PropositionalAST currentAst = new PropositionalAST(current.toString(), true);

            if (PropositionalLogicHelper.getOutermostOperation(currentAst) != LogicalOperator.NOT_A_LOGICAL_OPERATOR) {
                builder.append("(").append(current).append(")");
            }
            else builder.append(current);

            if (i < subset.size() - 1) builder.append(" ").append(LogicalOperatorFlyweight.getConjunctionString()).append(" ");
        }

        if (PropositionalLogicHelper.getOutermostOperation(new PropositionalAST(builder.toString(), true))
                == LogicalOperator.NOT_A_LOGICAL_OPERATOR || subset.size() == 1) {
            String formulaString = builder + " " + LogicalOperatorFlyweight.getImplicationString() + " " + conclusionString;
            return new PropositionalAST(formulaString, true);
        }
        else {
            String formulaString = "(" + builder + ") " + LogicalOperatorFlyweight.getImplicationString() + " " + conclusionString;
            return new PropositionalAST(formulaString, true);
        }
    }

}
