package me.dariansandru.utils.helper;

import me.dariansandru.domain.proof.ProofStep;
import me.dariansandru.domain.proof.SubGoal;
import me.dariansandru.domain.proof.inference_rules.InferenceRule;
import me.dariansandru.domain.proof.inference_rules.propositional.PropositionalInferenceRule;
import me.dariansandru.io.OutputDevice;
import me.dariansandru.domain.data_structures.ast.AST;
import me.dariansandru.domain.data_structures.ast.PropositionalAST;
import me.dariansandru.utils.factory.PropositionalInferenceRuleFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class ProofTextHelper {

    private static final List<ProofStep> assumptionSteps = new ArrayList<>();
    private static final List<ProofStep> conclusionSteps = new ArrayList<>();
    private static final List<List<ProofStep>> proofSteps = new ArrayList<>();
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
        String formula = subGoal.getGoal().toString();

        while (subGoal != null) {
            if (subGoal.getInferenceRule() == PropositionalInferenceRule.HYPOTHESIS) {
                if (subGoal.getGoal().toString().equals("Contradiction")) {
                    subGoal = subGoal.getParent();
                    continue;
                }
                ProofStep proofStep = new ProofStep("We conclude " + subGoal.getGoal() + " from the Knowledge Base", rightMostIndent);
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

        addDerivationSteps(formula, rightMostIndent + 1, proofText);
        proofSteps.add(proofText);
    }

    public static void getProofTextHypothesis(SubGoal subGoal) {
        List<ProofStep> proofText = new ArrayList<>();
        String formula = subGoal.getGoal().toString();

        while (subGoal != null) {
            if (subGoal.getGoal().toString().equals("Contradiction")) {
                subGoal = subGoal.getParent();
                continue;
            }
            ProofStep proofStep = new ProofStep("We conclude " + subGoal.getGoal() + " from the hypothesis", rightMostIndent);
            proofText.add(proofStep);
            subGoal = subGoal.getParent();
        }

        addDerivationSteps(formula, rightMostIndent + 1, proofText);
        proofSteps.add(proofText);
    }

    public static void getProofTextContradiction(AST ast) {
        List<ProofStep> proofText = new ArrayList<>();
        String formula = ast.toString();

        ProofStep step = new ProofStep(
                "We derive a contradiction because " + formula +
                        ", from the Knowledge Base, is a direct contradiction",
                rightMostIndent
        );
        ProofStep lastStep = new ProofStep(
                "Thus, we have derived a contradiction",
                rightMostIndent
        );
        proofText.add(step);

        addDerivationSteps(formula, rightMostIndent + 1, proofText);
        proofText.add(lastStep);
        proofSteps.add(proofText);
    }

    private static void addDerivationSteps(String formula, int indent, List<ProofStep> proofText) {
        List<String> parents = KnowledgeBaseRegistry.from(formula);
        if (parents == null || parents.isEmpty()) return;

        ProofStep step = new ProofStep(
                "We obtained " + formula + " from " + String.join(", ", parents),
                indent
        );
        proofText.add(step);

        for (String parent : parents) {
            addDerivationSteps(parent, indent + 1, proofText);
        }
    }

    public static void print() {
        if (assumptionSteps.isEmpty() && conclusionSteps.isEmpty()) {
            for (List<ProofStep> proof : proofSteps) {
                for (ProofStep step : proof) {
                    OutputDevice.writeIndentedToConsole(step.getText(), step.getIndent());
                }
            }
        }

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
                else currentIndentation--;
            }
            else if (isProof) {
                if (printedProofIndex == proofSteps.size()) {
                    isConclusion = true;
                    isProof = false;
                    currentIndentation--;
                    continue;
                }
                List<ProofStep> proof = proofSteps.remove(printedProofIndex);

                ProofStep lastStep = null;
                for (ProofStep step : proof) {
                    OutputDevice.writeIndentedToConsole(step.getText(), step.getIndent());
                    lastStep = step;
                }

                assert lastStep != null;
                if (lastStep.getText().endsWith("hypothesis")) continue;

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
        AST negatedAssumption = new PropositionalAST(assumption.toString());
        negatedAssumption.validate(0);
        negatedAssumption.negate();
        return "To prove " + assumption + ", assume " + negatedAssumption + " and prove a contradiction";
    }

    public static void printWithSymbol(String string, String symbol) {
        System.out.println();
        for (int i = 0 ; i < string.length() ; i++) System.out.print(symbol);
        System.out.println();
        System.out.println(string);
        for (int i = 0 ; i < string.length() ; i++) System.out.print(symbol);
    }

}