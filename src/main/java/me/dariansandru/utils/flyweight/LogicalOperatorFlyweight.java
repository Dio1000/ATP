package me.dariansandru.utils.flyweight;

import me.dariansandru.domain.language.logical_operator.*;

public abstract class LogicalOperatorFlyweight {

    private static final Negation negation = new Negation();
    private static final String negationString = negation.getRepresentation();

    private static final Conjunction conjunction = new Conjunction();
    private static final String conjunctionString = conjunction.getRepresentation();

    private static final Disjunction disjunction = new Disjunction();
    private static final String disjunctionString = disjunction.getRepresentation();

    private static final Implication implication = new Implication();
    private static final String implicationString = implication.getRepresentation();

    private static final Equivalence equivalence = new Equivalence();
    private static final String equivalenceString = equivalence.getRepresentation();

    public static Negation getNegation() {
        return negation;
    }

    public static String getNegationString() {
        return negationString;
    }

    public static Conjunction getConjunction() {
        return conjunction;
    }

    public static String getConjunctionString() {
        return conjunctionString;
    }

    public static Disjunction getDisjunction() {
        return disjunction;
    }

    public static String getDisjunctionString() {
        return disjunctionString;
    }

    public static Implication getImplication() {
        return implication;
    }

    public static String getImplicationString() {
        return implicationString;
    }

    public static Equivalence getEquivalence() {
        return equivalence;
    }

    public static String getEquivalenceString() {
        return equivalenceString;
    }

}
