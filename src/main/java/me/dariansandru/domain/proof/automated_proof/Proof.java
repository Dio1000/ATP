package me.dariansandru.domain.proof.automated_proof;

import me.dariansandru.domain.data_structures.ast.AST;

import java.util.List;

public interface Proof {
    List<AST> getKnowledgeBase();
    List<AST> getGoals();
    void printProof();
    boolean isProven();
}
