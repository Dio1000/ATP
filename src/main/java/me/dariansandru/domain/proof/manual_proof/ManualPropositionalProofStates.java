package me.dariansandru.domain.proof.manual_proof;

import me.dariansandru.domain.data_structures.BiMap;

public abstract class ManualPropositionalProofStates {

    private static ManualPropositionalProof originalState;
    private static final BiMap stateIndexMap = new BiMap();
    private static int currentStateIndex = 1;

    public static void addOriginalState(ManualPropositionalProof state) {
        originalState = state;
    }

    public static ManualPropositionalProof getState(int index) {
        if (index == 1) {
            return originalState;
        }
        return (ManualPropositionalProof) stateIndexMap.get(index);
    }

    public static void addState(ManualPropositionalProof state, int index) {
        stateIndexMap.put(state, index);
    }

    public static int increaseStateIndex() {
        currentStateIndex++;
        return currentStateIndex;
    }

    public static int getCurrentStateIndex() {
        return currentStateIndex;
    }

}
