package me.dariansandru.utils.helper;

import me.dariansandru.domain.proof.ProofStep;
import me.dariansandru.domain.proof.SubGoal;
import me.dariansandru.io.OutputDevice;
import me.dariansandru.domain.data_structures.ast.AST;
import me.dariansandru.domain.data_structures.ast.PropositionalAST;
import me.dariansandru.utils.global.GlobalFlags;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public abstract class ProofTextHelper {

    private static final List<ProofStep> fullProof = new ArrayList<>();
    private static final List<ProofStep> assumptionSteps = new ArrayList<>();
    private static final List<ProofStep> conclusionSteps = new ArrayList<>();
    private static final List<List<ProofStep>> proofSteps = new ArrayList<>();
    private static int rightMostIndent = 0;

    private static final List<String> formalProofSteps = new ArrayList<>();
    private static final Map<String, Integer> formalStepMap = new HashMap<>();

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

    public static void getProofText(String formula) {
        List<ProofStep> proofText = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        addDerivationSteps(formula, rightMostIndent, proofText, visited);
        addPRoofStep(proofText);
        buildFormalProof(formula);
    }

    public static void getProofText(SubGoal subGoal) {
        List<ProofStep> proofText = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        addDerivationSteps(subGoal.getGoal().toString(), rightMostIndent, proofText, visited);
        addPRoofStep(proofText);
        buildFormalProof(subGoal.getGoal().toString());
    }

    public static void addPRoofStep(List<ProofStep> proofTexts) {
        List<ProofStep> validProofSteps = new ArrayList<>();
        for (ProofStep proofText : proofTexts) {
            if (!proofText.text().startsWith("Strategy:")) validProofSteps.add(proofText);
        }
        proofSteps.add(validProofSteps);
    }

    public static void getProofTextContradiction(AST ast) {
        KnowledgeBaseRegistry.addEntry("Contradiction", "From " + ast + " we derive a contradiction", List.of(ast.toString()));
        KnowledgeBaseRegistry.addObtainedFrom("Contradiction", List.of(ast.toString()), "Contradiction");

        List<ProofStep> proofText = new ArrayList<>();
        Set<String> visited = new HashSet<>();

        addDerivationSteps("Contradiction", rightMostIndent, proofText, visited);
        addPRoofStep(proofText);

        buildFormalProof("Contradiction");
    }

    private static List<ProofStep> getAllByIndentation(List<ProofStep> steps, int indent) {
        List<ProofStep> result = new ArrayList<>();
        for (ProofStep step : steps) {
            if (step.indent() == indent) result.add(step);
        }
        return result;
    }

    private static void removeSteps(List<ProofStep> steps, List<ProofStep> toRemove) {
        steps.removeAll(toRemove);
    }

    public static void print() {
        int minIndent = Integer.MAX_VALUE;
        int maxIndent = Integer.MIN_VALUE;

        List<ProofStep> allProofSteps = new ArrayList<>();
        for (List<ProofStep> proof : proofSteps) {
            allProofSteps.addAll(proof);
        }

        for (ProofStep step : allProofSteps) {
            minIndent = Math.min(minIndent, step.indent());
            maxIndent = Math.max(maxIndent, step.indent());
        }
        fullProof.clear();

        for (ProofStep step : assumptionSteps) {
            if (GlobalFlags.outputToConsole) OutputDevice.writeIndentedToConsole(step.text(), step.indent());
            fullProof.add(step);
        }

        for (ProofStep step : allProofSteps) {
            int transformedIndent = (maxIndent - step.indent()) + minIndent;
            if (GlobalFlags.outputToConsole) OutputDevice.writeIndentedToConsole(step.text(), transformedIndent);
            fullProof.add(new ProofStep(step.text(), transformedIndent));
        }
        for (ProofStep step : conclusionSteps) {
            if (GlobalFlags.outputToConsole) OutputDevice.writeIndentedToConsole(step.text(), step.indent());
            fullProof.add(step);
        }
    }

    private static void addDerivationSteps(String formula, int indent, List<ProofStep> proofText, Set<String> visited) {
        if (visited.contains(formula)) return;
        visited.add(formula);

        String ruleText = KnowledgeBaseRegistry.getString(formula);
        List<String> parents = KnowledgeBaseRegistry.from(formula);

        if (parents != null && !parents.isEmpty()) {
            for (String parent : parents) {
                addDerivationSteps(parent, indent + 1, proofText, visited);
            }
        }

        if (ruleText != null && !ruleText.isEmpty() && !ruleText.equals("Hypothesis")) {
            proofText.add(new ProofStep(ruleText, indent));
        }
        else if (parents == null || parents.isEmpty()) {
            if (!formula.equals("Contradiction") && !formula.contains("->") && !formula.contains("<->") && !formula.contains("AND") && !formula.contains("OR")) {
                proofText.add(new ProofStep("We derive " + formula + " from the Knowledge Base", indent));
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
        for (int i = 1; i < strings.length; i++) {
            if (i == strings.length - 1) builder.append(" and ").append(strings[i].strip());
            else builder.append(", ").append(strings[i].strip());
        }
        return builder.toString();
    }

    public static String getDisjunctionAssumption(String disjunction, String... strings) {
        StringBuilder builder = new StringBuilder();
        builder.append("To prove ").append(disjunction).append(", prove ").append(strings[0].strip());
        for (int i = 1; i < strings.length; i++) {
            if (i == strings.length - 1) builder.append(" or ").append(strings[i].strip());
            builder.append(", ").append(strings[i].strip());
        }
        return builder.toString();
    }

    public static String getProofByCasesAssumption(String goal, String disjunction) {
        return "To prove " + goal + ", having the disjunction " + disjunction + ", we will use Proof By Cases";
    }

    public static String getNegationAssumption(AST assumption) {
        AST negatedAssumption = new PropositionalAST(assumption.toString());
        negatedAssumption.validate(0);
        negatedAssumption.negate();
        return "To prove " + assumption + ", assume " + negatedAssumption + " and prove a contradiction";
    }

    public static void printWithSymbol(String string, String symbol) {
        String[] parts = string.split("\n");
        int length = parts[0].length();
        for (String part : parts) {
            if (part.length() > length) length = part.length();
        }

        for (int i = 0; i < length; i++) System.out.print(symbol);
        System.out.println();
        System.out.println(string);
        for (int i = 0; i < length; i++) System.out.print(symbol);
    }

    public static String getProofString() {
        StringBuilder builder = new StringBuilder();
        for (ProofStep proofStep : fullProof) {
            builder.append(buildProofStep(proofStep)).append("\n");
        }
        return builder.toString();
    }

    private static String buildProofStep(ProofStep step) {
        int indentation = step.indent();
        StringBuilder builder = new StringBuilder();
        String indent = "     ";

        while (indentation != 0) {
            builder.append(indent);
            indentation--;
        }
        builder.append(step.text());
        return builder.toString();
    }

    public static void clear() {
        fullProof.clear();
        assumptionSteps.clear();
        conclusionSteps.clear();
        proofSteps.clear();
        rightMostIndent = 0;
        formalProofSteps.clear();
        formalStepMap.clear();
    }

    private static void getFormalStepsChronological(String formula, List<String> chronological, Set<String> visited) {
        if (visited.contains(formula)) return;
        visited.add(formula);

        List<String> parents = KnowledgeBaseRegistry.from(formula);
        if (parents != null && !parents.isEmpty()) {
            for (String parent : parents) {
                getFormalStepsChronological(parent, chronological, visited);
            }
        }
        chronological.add(formula);
    }

    public static void buildFormalProof(String targetFormula) {
        List<String> chronological = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        getFormalStepsChronological(targetFormula, chronological, visited);

        List<String> premises = new ArrayList<>();
        List<String> derived = new ArrayList<>();

        for (String formula : chronological) {
            List<String> parents = KnowledgeBaseRegistry.from(formula);
            if (parents == null || parents.isEmpty()) {
                premises.add(formula);
            } else {
                derived.add(formula);
            }
        }

        List<String> orderedSteps = new ArrayList<>(premises);
        orderedSteps.addAll(derived);

        formalProofSteps.clear();
        formalStepMap.clear();

        for (int i = 0; i < orderedSteps.size(); i++) {
            String formula = orderedSteps.get(i);
            int stepNum = i + 1;
            formalStepMap.put(formula, stepNum);

            List<String> parents = KnowledgeBaseRegistry.from(formula);
            String ruleName = KnowledgeBaseRegistry.getRule(formula);
            if (ruleName != null && ruleName.startsWith("Strategy:")) ruleName = ruleName.split(":")[1].strip();

            getFormalSteps(formula, stepNum, parents, ruleName);
        }
    }

    private static void getFormalSteps(String formula, int stepNum, List<String> parents, String ruleName) {
        if (parents == null || parents.isEmpty()) {
            if (ruleName == null || ruleName.isEmpty() || ruleName.equals("Hypothesis")) ruleName = "Premise";
        }
        else {
            if (ruleName == null || ruleName.isEmpty() || ruleName.equals("Hypothesis") || ruleName.equals("Premise")) ruleName = "Derived";
        }

        StringBuilder line = new StringBuilder();
        line.append(stepNum).append(". ").append(formula).append(" (").append(ruleName);

        if (parents != null && !parents.isEmpty()) {
            line.append(": ");
            for (int j = 0; j < parents.size(); j++) {
                Integer parentIndex = formalStepMap.get(parents.get(j));
                line.append(parentIndex != null ? parentIndex : "?");
                if (j < parents.size() - 1) line.append(", ");
            }
        }
        line.append(")");
        formalProofSteps.add(line.toString());
    }

    public static void printFormalProof() {
        for (String step : formalProofSteps) {
            if (GlobalFlags.outputToConsole) OutputDevice.writeToConsole(step);
        }
    }

    public static String getFormalProofString() {
        return String.join("\n", formalProofSteps);
    }
}