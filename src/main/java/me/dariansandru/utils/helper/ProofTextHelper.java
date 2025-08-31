package me.dariansandru.utils.helper;

import me.dariansandru.domain.proof.ProofStep;
import me.dariansandru.domain.proof.SubGoal;
import me.dariansandru.domain.proof.inference_rules.InferenceRule;
import me.dariansandru.domain.proof.inference_rules.propositional.PropositionalInferenceRule;
import me.dariansandru.io.OutputDevice;
import me.dariansandru.utils.data_structures.ast.AST;
import me.dariansandru.utils.factory.PropositionalInferenceRuleFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class ProofTextHelper {

    private static List<ProofStep> assumptionSteps = new ArrayList<>();
    private static List<ProofStep> conclusionSteps = new ArrayList<>();
    private static List<List<ProofStep>> proofSteps = new ArrayList<>();
    private static int rightMostIndent = 0;

    public static void addAssumptionStep(String step, int indent) {
        ProofStep newStep = new ProofStep(step, indent);
        assumptionSteps.add(newStep);
        if (indent >= rightMostIndent) rightMostIndent = indent + 1;
    }

    public static void addConclusionStep(String step, int indent) {
        ProofStep newStep = new ProofStep(step, indent);
        conclusionSteps.add(newStep);
        if (indent >= rightMostIndent) rightMostIndent = indent + 1;
    }

    public static void getProofText(SubGoal subGoal) {
        List<ProofStep> proofText = new ArrayList<>();
        while (subGoal != null) {
            if (subGoal.getInferenceRule() == PropositionalInferenceRule.HYPOTHESIS) {
                ProofStep proofStep = new ProofStep("We conclude " + subGoal.getGoal() + " from the hypothesis", rightMostIndent);
                proofText.add(proofStep);
                subGoal = subGoal.getParent();
                continue;
            }
            InferenceRule inferenceRule = PropositionalInferenceRuleFactory.create(subGoal.getInferenceRule());
            assert inferenceRule != null;

            ProofStep proofStep = new ProofStep(inferenceRule.getText(subGoal), rightMostIndent);
            proofText.add(proofStep);
            subGoal = subGoal.getParent();
        }

        proofSteps.add(proofText);
    }

    public static void getProofTextHypothesis(SubGoal subGoal) {
        List<ProofStep> proofText = new ArrayList<>();
        while (subGoal != null) {
            ProofStep proofStep = new ProofStep("We conclude " + subGoal.getGoal() + " from the hypothesis", rightMostIndent);
            proofText.add(proofStep);
            subGoal = subGoal.getParent();
        }

        proofSteps.add(proofText);
    }

    public static void print() {
        boolean isAssumption = true;
        boolean isConclusion = false;
        boolean isProof = false;
        int printedProofIndex = 0;

        int currentIndentation = 0;
        do {
            if (isAssumption) {
                ProofStep proofStep = getByIndentation(assumptionSteps, currentIndentation);
                if (proofStep == null) break;
                removeStep(assumptionSteps, proofStep.getText(), proofStep.getIndent());

                OutputDevice.writeIndentedToConsole(proofStep.getText(), proofStep.getIndent());
                currentIndentation++;

                if (currentIndentation == rightMostIndent) {
                    isProof = true;
                    isAssumption = false;
                }
            }
            else if (isConclusion) {
                ProofStep proofStep = getByIndentation(conclusionSteps, currentIndentation);
                if (proofStep == null) break;
                removeStep(conclusionSteps, proofStep.getText(), proofStep.getIndent());

                OutputDevice.writeIndentedToConsole(proofStep.getText(), proofStep.getIndent());
                if (getByIndentation(assumptionSteps, currentIndentation) != null) {
                    isAssumption = true;
                    isConclusion = false;
                }
            }
            else if (isProof) {
                if (printedProofIndex == proofSteps.size()) break;
                List<ProofStep> proof = proofSteps.remove(printedProofIndex);

                for (ProofStep step : proof) {
                    OutputDevice.writeIndentedToConsole(step.getText(), step.getIndent());
                }
                currentIndentation--;

                isConclusion = true;
                isProof = false;
            }

        } while (!(currentIndentation == -1 && isConclusion));
    }

    private static ProofStep getByIndentation(List<ProofStep> steps, int indentation) {
        for (ProofStep proofStep : steps)
            if (proofStep.getIndent() == indentation)
                return proofStep;
        return null;
    }

    private static void removeStep(List<ProofStep> steps, String text, int indent) {
        for (int i = 0; i < steps.size(); i++) {
            ProofStep step = steps.get(i);
            if (step.getIndent() == indent && step.getText().equals(text)) {
                steps.remove(i);
                return;
            }
        }
    }

    public static String getConclusion(String conclusion) {
        Random random = new Random();
        int seed = random.nextInt(0, 3);

        return switch (seed) {
            case 0 -> "Therefore " + conclusion;
            case 1 -> "Thus " + conclusion;
            case 2 -> "We now conclude " + conclusion;
            default -> throw new IllegalStateException("Seed was not computed correctly!");
        };
    }

    public static String getAssumption(String implication, String assumption, String conclusion) {
        return "To prove " + implication + ", assume " + assumption + " and prove " + conclusion;
    }

    public static String getInference(String conclusion, String... strings) {
        if (strings.length == 1) return "From " + strings[0] + " ,we conclude " + conclusion;
        else if (strings.length == 2) return "From " + strings[0] + " and " + strings[1] + " ,we conclude " + conclusion;
        else {
            return getStringChain(conclusion, strings);
        }
    }

    public static String getInference(String conclusion, String rule, String... strings) {
        if (strings.length == 1) return "From " + strings[0] + " ,by " + rule + " we conclude " + conclusion;
        else if (strings.length == 2) return "From " + strings[0] + " and " + strings[1] + " ,by " + rule + " ,we conclude " + conclusion;
        else {
            return getStringChain(conclusion, strings);
        }
    }

    private static String getStringChain(String conclusion, String[] strings) {
        StringBuilder builder = new StringBuilder();
        builder.append("From ");
        for (int i = 0 ; i < strings.length ; i++) {
            if (i != strings.length - 1) builder.append(strings[i]).append(", ");
            else builder.append(strings[i]);
        }
        builder.append(" ,we conclude ").append(conclusion);

        return builder.toString();
    }

    public static String getEquivalenceAssumption(String equivalence, String conclusion1, String conclusion2) {
        return "To prove " + equivalence + ", prove " + conclusion1 + " and " + conclusion2;
    }

    public static String getConjunctionAssumption(String conjunction, String... strings) {
        StringBuilder builder = new StringBuilder();
        builder.append("To prove ").append(conjunction).append(", prove ").append(strings[0].strip());

        for (int i = 1 ; i < strings.length ; i++ ){
            if (i == strings.length - 1 ) builder.append(" and ").append(strings[i].strip());
            else builder.append(", ").append(strings[i].strip());
        }

        return builder.toString();
    }

    public static String getDisjunctionAssumption(String disjunction, String... strings) {
        StringBuilder builder = new StringBuilder();
        builder.append("To prove ").append(disjunction).append(", prove ").append(strings[0].strip());

        for (int i = 1 ; i < strings.length ; i++ ){
            if (i == strings.length - 1 ) builder.append(" or ").append(strings[i].strip());
            builder.append(", ").append(strings[i].strip());
        }

        return builder.toString();
    }

    public static String getNegationAssumption(AST assumption) {
        assumption.negate();
        return "To prove " + assumption + ", assume " + assumption + " and prove a contradiction";
    }

}