package me.dariansandru.domain.proof.proof_states;
import me.dariansandru.domain.proof.inference_rules.InferenceRule;
import me.dariansandru.domain.data_structures.ast.AST;

import java.util.List;

/*
Implementing this interface allows the user to extend the system with a new type
of Proof State. This is useful when creating a new Universe of Discourse, which requires
different proving methods, with different rules of inference and termination requirements.
 */
public interface ProofState {
    List<AST> getKnowledgeBase();
    List<AST> getGoals();
    AST getGoal();
    List<InferenceRule> getInferenceRules();

    void prove();
    boolean simplify();

    boolean isVisited();
    boolean areChildrenInConjunction();
    boolean isProven();

    List<ProofState> getChildren();
    void addChild(ProofState proofState);

    ProofState getParent();
    void addParent(ProofState proofState);

    void setStateIndex(int index);
    void setProven();
    int getStateIndex();
}
