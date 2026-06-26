package me.dariansandru.domain.proof.automated_proof;

import me.dariansandru.domain.data_structures.ast.AST;

import java.util.List;

/*
Implementing this interface allows for the creation of a new type of Proof.
This is useful when creating a new Universe of Discourse, each having their own
Signature, thus requiring handling of different formulas, functions and predicates.
A new proof has methods for getting the Knowledge Base and the Goals, a method
for printing the proof and a flag that stores the proved state of the Proof.
 */
public interface Proof {
    List<AST> getKnowledgeBase();
    List<AST> getGoals();
    void printProof();
    boolean isProven();
}
