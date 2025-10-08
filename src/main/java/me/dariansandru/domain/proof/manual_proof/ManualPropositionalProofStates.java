package me.dariansandru.domain.proof.manual_proof;

import me.dariansandru.domain.data_structures.BiMap;

import java.util.ArrayList;
import java.util.List;

public abstract class ManualPropositionalProofStates {

    private static final List<ManualPropositionalProof> states = new ArrayList<>();
    private static final BiMap stateIndexMap = new BiMap();

    public static ManualPropositionalProof getState(int index) {
        return (ManualPropositionalProof) stateIndexMap.get(index);
    }

    public static void addState(ManualPropositionalProof state, int index) {
        stateIndexMap.put(state, index);
    }


}
