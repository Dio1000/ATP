package me.dariansandru.domain;

import me.dariansandru.utils.data_structures.BiMap;

public enum UniverseOfDiscourse {

    PROPOSITIONS("Propositions"),
    INTEGER_NUMBERS("Integers"),
    RATIONAL_NUMBERS("Rationals"),
    REAL_NUMBERS("Reals"),
    STRINGS("Strings"),
    UNKNOWN("Unknown");

    private final String displayName;
    private static final BiMap stringUniverseMap = new BiMap();

    UniverseOfDiscourse(String displayName) {
        this.displayName = displayName;
    }

    static {
        for (UniverseOfDiscourse universe : UniverseOfDiscourse.values()) {
            stringUniverseMap.put(universe.displayName, universe);
        }
    }

    public static UniverseOfDiscourse getFromString(String universe) {
        UniverseOfDiscourse result = (UniverseOfDiscourse) stringUniverseMap.get(universe);
        return (result != null) ? result : UNKNOWN;
    }

    public static String getFromEnum(UniverseOfDiscourse universe) {
        String result = (String) stringUniverseMap.get(universe);
        return (result != null) ? result : "Unknown";
    }

}
