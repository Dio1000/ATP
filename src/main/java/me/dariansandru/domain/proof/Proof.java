package me.dariansandru.domain.proof;

import me.dariansandru.domain.proof.inference_rules.InferenceRule;
import me.dariansandru.domain.signature.Signature;
import me.dariansandru.io.OutputDevice;
import me.dariansandru.reflexivity.InferenceRulesFactory;
import me.dariansandru.utils.data_structures.ast.AST;

import java.util.ArrayList;
import java.util.List;

public class Proof {

    private final Signature signature;
    private final List<InferenceRule> inferenceRules;

    private final List<AST> knowledgeBase;
    private final List<AST> goals;

    private List<String> proofText = new ArrayList<>();
    private final String indentation = "     ";

    public Proof(Signature signature, List<AST> knowledgeBase, List<AST> goals) {
        this.signature = signature;
        this.inferenceRules = InferenceRulesFactory.createRules(signature);
        this.knowledgeBase = knowledgeBase;
        this.goals = goals;
    }

    public void prove() {

        printProof();
    }

    public void simplifyGoals() {
        for (AST goal : goals) {

        }
    }

    public void printProof() {
        for (int index = 0 ; index < proofText.size() ; index+=2) {
            OutputDevice.writeToConsole(indentation, proofText.get(index), index);
        }

        for (int index = 1 ; index < proofText.size() ; index+=2) {
            OutputDevice.writeToConsole(indentation, proofText.get(index), proofText.size() / 2 - index);
        }
    }

}
