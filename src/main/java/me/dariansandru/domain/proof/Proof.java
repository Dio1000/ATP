package me.dariansandru.domain.proof;

import me.dariansandru.domain.logical_operator.Implication;
import me.dariansandru.domain.predicate.Predicate;
import me.dariansandru.domain.proof.inference_rules.InferenceRule;
import me.dariansandru.domain.signature.Signature;
import me.dariansandru.io.OutputDevice;
import me.dariansandru.reflexivity.InferenceRulesFactory;
import me.dariansandru.utils.data_structures.ast.AST;
import me.dariansandru.utils.data_structures.ast.PropositionalASTNode;
import me.dariansandru.utils.helper.ProofTextHelper;

import java.util.ArrayList;
import java.util.List;

public class Proof {

    private final Signature signature;
    private final List<InferenceRule> inferenceRules;

    private List<AST> knowledgeBase;
    private List<AST> goals;

    private List<String> assumptions = new ArrayList<>();
    private List<String> conclusions = new ArrayList<>();
    private final String indentation = "     ";

    public Proof(Signature signature, List<AST> knowledgeBase, List<AST> goals) {
        this.signature = signature;
        this.inferenceRules = InferenceRulesFactory.createRules(signature);
        this.knowledgeBase = knowledgeBase;
        this.goals = goals;
    }

    public void prove() {
        simplifyGoals();
        printProof();
    }

    public void simplifyGoals() {
        int index = 0;
        while (index < goals.size()) {
            if (ImplicationStrategy(goals.get(index))) {
                index = 0;
            } else {
                index++;
            }
        }
    }

    public boolean ImplicationStrategy(AST goal) {
        PropositionalASTNode node;
        try {
            node = (PropositionalASTNode) goal.getRoot();
        } catch (Exception e) {
            return false;
        }

        Predicate predicate = (Predicate) node.getKey();

        if (predicate != null &&
                predicate.getRepresentation().equals(new Implication().getRepresentation())) {

            AST assumption = goal.getSubtree(0);
            AST conclusion = goal.getSubtree(1);

            goals.remove(goal);
            knowledgeBase.add(assumption);
            goals.add(conclusion);

            assumptions.add(ProofTextHelper.getAssumption(assumption.toString(), conclusion.toString()));
            conclusions.add(ProofTextHelper.getConclusion(conclusion.toString()));

            return true;
        }

        return false;
    }

    public void printProof() {
        for (int i = 0; i < assumptions.size(); i++) {
            OutputDevice.writeToConsole(indentation, assumptions.get(i), i);
        }

        for (int i = conclusions.size() - 1; i >= 0 ; i--) {
            OutputDevice.writeToConsole(indentation, conclusions.get(i), i);
        }
    }
}
