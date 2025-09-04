package me.dariansandru.utils.helper;

import java.util.*;

public abstract class KnowledgeBaseRegistry {

    private static final Map<String, String> entryStringMap = new HashMap<>();
    private static final Map<String, Boolean> entryIsUsedMap = new HashMap<>();
    private static final Map<String, List<String>> entryFromMap = new HashMap<>();
    private static final Map<String, String> entryChildMap = new HashMap<>();

    public static void getAllEntries() {
        for (String string : entryStringMap.keySet()) {
            System.out.println(entryStringMap.get(string));
        }
    }

    public static void addEntry(String formula, String origin, List<String> from) {
        if (entryStringMap.containsKey(formula)) return;

        entryStringMap.put(formula, origin);
        entryFromMap.put(formula, new ArrayList<>(from));

        for (String parent : from) {
            entryChildMap.put(parent, formula);
        }
    }

    public static boolean hasEntry(String formula) {
        return entryStringMap.containsKey(formula);
    }

    public static List<String> from(String formula) {
        return entryFromMap.getOrDefault(formula, Collections.emptyList());
    }

    public static String child(String formula) {
        return entryChildMap.getOrDefault(formula, null);
    }

    public static String getString(String formula) {
        return entryStringMap.getOrDefault(formula, "");
    }

    public static void setUsed(String formula) {
        if (entryIsUsedMap.getOrDefault(formula, false)) return;
        entryIsUsedMap.put(formula, true);
    }

    public static void setUnused(String formula) {
        if (!entryIsUsedMap.getOrDefault(formula, true)) return;
        entryIsUsedMap.put(formula, false);
    }

    public static boolean isUsed(String formula) {
        return entryIsUsedMap.getOrDefault(formula, false);
    }
}
