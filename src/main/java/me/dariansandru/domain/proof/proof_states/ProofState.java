package me.dariansandru.domain.proof.proof_states;
import me.dariansandru.domain.proof.inference_rules.InferenceRule;
import me.dariansandru.utils.data_structures.ast.AST;

import java.util.List;

public interface ProofState {
    List<AST> getKnowledgeBase();
    List<AST> getGoals();
    AST getGoal();
    List<InferenceRule> getInferenceRules();

    boolean isVisited();
    boolean areChildrenInConjunction();
    boolean isProven();

    void prove();
    boolean simplify();

    List<ProofState> getChildren();
    void addChild(ProofState proofState);
    ProofState getParent();
    void addParent(ProofState proofState);
}
