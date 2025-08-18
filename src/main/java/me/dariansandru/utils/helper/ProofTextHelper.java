package me.dariansandru.utils.helper;

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

    public static String getAssumption(String assumption, String conclusion) {
        return "Assume " + assumption + " and prove " + conclusion;
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

}
