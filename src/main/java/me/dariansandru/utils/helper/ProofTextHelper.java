package me.dariansandru.utils.helper;

import me.dariansandru.utils.data_structures.ast.AST;

import java.util.Random;

public abstract class ProofTextHelper {

    public static String getConclusion(String conclusion) {
        Random random = new Random();
        int seed = random.nextInt(0, 3);

        return switch (seed) {
            case 0 -> "Therefore " + conclusion;
            case 1 -> "Thus " + conclusion;
            case 2 -> "We now conclude " + conclusion;
            default -> throw new IllegalStateException("Seed was not computed correctly!");
        };
    }

    public static String getAssumption(String implication, String assumption, String conclusion) {
        return "To prove " + implication + ", assume " + assumption + " and prove " + conclusion;
    }

    public static String getInference(String conclusion, String... strings) {
        if (strings.length == 1) return "From " + strings[0] + " ,we conclude " + conclusion;
        else if (strings.length == 2) return "From " + strings[0] + " and " + strings[1] + " ,we conclude " + conclusion;
        else {
            return getStringChain(conclusion, strings);
        }
    }

    public static String getInference(String conclusion, String rule, String... strings) {
        if (strings.length == 1) return "From " + strings[0] + " ,by " + rule + " we conclude " + conclusion;
        else if (strings.length == 2) return "From " + strings[0] + " and " + strings[1] + " ,by " + rule + " ,we conclude " + conclusion;
        else {
            return getStringChain(conclusion, strings);
        }
    }

    private static String getStringChain(String conclusion, String[] strings) {
        StringBuilder builder = new StringBuilder();
        builder.append("From ");
        for (int i = 0 ; i < strings.length ; i++) {
            if (i != strings.length - 1) builder.append(strings[i]).append(", ");
            else builder.append(strings[i]);
        }
        builder.append(" ,we conclude ").append(conclusion);

        return builder.toString();
    }

    public static String getEquivalenceAssumption(String equivalence, String conclusion1, String conclusion2) {
        return "To prove " + equivalence + ", prove " + conclusion1 + " and " + conclusion2;
    }

    public static String getConjunctionAssumption(String conjunction, String... strings) {
        StringBuilder builder = new StringBuilder();
        builder.append("To prove ").append(conjunction).append(", prove ").append(strings[0].strip());

        for (int i = 1 ; i < strings.length ; i++ ){
            if (i == strings.length - 1 ) builder.append(" and ").append(strings[i].strip());
            else builder.append(", ").append(strings[i].strip());
        }

        return builder.toString();
    }

    public static String getDisjunctionAssumption(String disjunction, String... strings) {
        StringBuilder builder = new StringBuilder();
        builder.append("To prove ").append(disjunction).append(", prove ").append(strings[0].strip());

        for (int i = 1 ; i < strings.length ; i++ ){
            if (i == strings.length - 1 ) builder.append(" or ").append(strings[i].strip());
            builder.append(", ").append(strings[i].strip());
        }

        return builder.toString();
    }

    public static String getNegationAssumption(AST assumption) {
        assumption.negate();
        return "To prove " + assumption + ", assume " + assumption + " and prove a contradiction";
    }

}
