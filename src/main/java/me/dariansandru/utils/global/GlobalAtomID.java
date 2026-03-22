package me.dariansandru.utils.global;


import me.dariansandru.domain.data_structures.BiMap;

import java.util.HashSet;
import java.util.Set;

public abstract class GlobalAtomID {
    private static final BiMap atomIdMap = new BiMap();
    private static final Set<String> atoms = new HashSet<>();
    private static int currentID = 2;

    public static void addAtomId(String atom) {
        if (!atoms.contains(atom)) {
            atomIdMap.put(atom, currentID);
            atoms.add(atom);
            currentID++;
        }
    }

    public static Integer getAtomId(String atom) {
        if (atom.equals("Tautology")) return 0;
        else if (atom.equals("Contradiction")) return 1;

        return (Integer) atomIdMap.get(atom);
    }

    public static void reset() {
        atomIdMap.reset();
    }
}
