package me.dariansandru.domain.proof.proofs;

import me.dariansandru.utils.data_structures.ast.AST;

import java.util.List;

public interface Proof {
    List<AST> getKnowledgeBase();
    List<AST> getGoals();

    List<String> getAssumptions();
    List<String> getConclusions();
    void printProof();

    boolean isProven();
    boolean moveLeft();
    boolean moveRight();
    boolean moveUp();
}
