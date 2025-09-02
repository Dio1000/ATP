package me.dariansandru.domain;

import me.dariansandru.domain.data_structures.BiMap;

public enum Notation {

    INFIX("Infix"),
    PREFIX("Prefix"),
    POSTFIX("Postfix"),
    UNKNOWN("UNKNOWN");

    private final String displayName;
    private static final BiMap stringNotationMap = new BiMap();

    Notation(String displayName) {
        this.displayName = displayName;
    }

    static {
        for (Notation notation : Notation.values()) {
            stringNotationMap.put(notation.displayName, notation);
        }
    }

    public static Notation getFromString(String notation) {
        Notation result = (Notation) stringNotationMap.get(notation);
        return (result != null) ? result : UNKNOWN;
    }

    public static String getFromEnum(Notation notation) {
        String result = (String) stringNotationMap.get(notation);
        return (result != null) ? result : "Unknown";
    }

}
