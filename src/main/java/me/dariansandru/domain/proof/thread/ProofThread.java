package me.dariansandru.domain.proof.thread;

import me.dariansandru.domain.proof.proof_states.ProofState;

public class ProofThread implements Runnable {

    private boolean isProven = false;
    private ProofState proofState;

    public void setProofState(ProofState proofState) {
        this.proofState = proofState;
    }

    public boolean isProven() {
        return this.isProven;
    }

    @Override
    public void run() {
        this.proofState.prove();
        this.isProven = this.proofState.isProven();
    }
}
