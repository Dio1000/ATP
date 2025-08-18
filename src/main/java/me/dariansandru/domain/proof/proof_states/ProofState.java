package me.dariansandru.domain.proof.proof_states;

import me.dariansandru.domain.formula.Formula;
import me.dariansandru.domain.proof.inference_rules.InferenceRule;
import me.dariansandru.utils.data_structures.ast.AST;

import java.util.ArrayList;
import java.util.List;

public class ProofState {

    private final List<AST> knowledgeBase;
    private final AST goal;
    private final List<InferenceRule> inferenceRules;

    public ProofState(List<AST> knowledgeBase, AST goal, List<InferenceRule> inferenceRules) {
        this.knowledgeBase = new ArrayList<>(knowledgeBase);
        this.goal = goal;
        this.inferenceRules = new ArrayList<>(inferenceRules);
    }

    public List<AST> getKnowledgeBase() {
        return knowledgeBase;
    }

    public AST getGoals() {
        return goal;
    }

    public List<InferenceRule> getInferenceRules() {
        return inferenceRules;
    }
}
