package me.dariansandru.domain.proof.proof_states;

import me.dariansandru.domain.formula.Formula;
import me.dariansandru.domain.proof.inference_rules.InferenceRule;
import me.dariansandru.utils.data_structures.ast.AST;

import java.util.ArrayList;
import java.util.List;

public class ProofState {

    private final List<AST> knowledgeBase;
    private final List<AST> goals;
    private final List<InferenceRule> inferenceRules;

    public ProofState(List<AST> knowledgeBase, List<AST> goals, List<InferenceRule> inferenceRules) {
        this.knowledgeBase = new ArrayList<>(knowledgeBase);
        this.goals = new ArrayList<>(goals);
        this.inferenceRules = new ArrayList<>(inferenceRules);
    }

    public List<AST> getKnowledgeBase() {
        return knowledgeBase;
    }

    public List<AST> getGoals() {
        return goals;
    }

    public List<InferenceRule> getInferenceRules() {
        return inferenceRules;
    }
}
