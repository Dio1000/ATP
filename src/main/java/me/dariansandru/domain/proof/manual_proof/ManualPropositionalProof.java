package me.dariansandru.domain.proof.manual_proof;

import me.dariansandru.domain.LogicalOperator;
import me.dariansandru.domain.data_structures.ast.AST;
import me.dariansandru.domain.data_structures.ast.PropositionalAST;
import me.dariansandru.domain.proof.manual_proof.helper.ManualPropositionalInferenceRuleHelper;
import me.dariansandru.domain.proof.manual_proof.helper.ManualPropositionalStrategyHelper;
import me.dariansandru.io.InputDevice;
import me.dariansandru.io.OutputDevice;
import me.dariansandru.parser.command.Command;
import me.dariansandru.parser.command.CommandParser;
import me.dariansandru.utils.helper.ErrorHelper;
import me.dariansandru.utils.helper.KnowledgeBaseRegistry;
import me.dariansandru.utils.helper.PropositionalLogicHelper;
import me.dariansandru.utils.helper.WarningHelper;

import java.util.ArrayList;
import java.util.List;

public class ManualPropositionalProof {

    private final List<AST> knowledgeBase;
    private final List<AST> goals;
    private final List<ManualPropositionalProof> stateGoals = new ArrayList<>();
    private final List<ManualPropositionalProof> childStates = new ArrayList<>();
    private boolean isProven = false;

    private final String kbName = "KB";
    private final String goalName = "G";
    private final String stateName = "S";

    private List<String> arguments = new ArrayList<>();
    private final ManualPropositionalProof parent;
    private final int stateIndex;

    public ManualPropositionalProof(List<AST> knowledgeBase, List<AST> goals, ManualPropositionalProof parent, int index) {
        this.knowledgeBase = knowledgeBase;
        this.goals = goals;
        this.parent = parent;
        this.stateIndex = index;

        ManualPropositionalStrategyHelper.loadData(stateGoals, childStates, knowledgeBase, goals);
        ManualPropositionalInferenceRuleHelper.loadData(knowledgeBase);
    }

    public ManualPropositionalProof getParent() {
        return parent;
    }

    public ManualPropositionalProof getChild(int index) {
        return childStates.get(index);
    }

    public int getStateIndex() {
        return stateIndex;
    }

    public boolean isProven() {
        return isProven;
    }

    public AST getGoal() {
        return (!goals.isEmpty()) ? this.goals.getFirst() : new PropositionalAST("", true);
    }

    private void addHypothesis() {
        for (AST ast : knowledgeBase) {
            KnowledgeBaseRegistry.addObtainedFrom(ast.toString(), "Hypothesis");
        }
        for (AST ast : goals) {
            KnowledgeBaseRegistry.addObtainedFrom(ast.toString(), "Hypothesis");
        }
    }

    public void prove() {
        if (this.stateIndex == 1) {
            addHypothesis();
            if (goals.size() != 1) {
                for (AST goal : goals) {
                    ManualPropositionalStrategyHelper.createNewState(goal, this);
                }
                goals.clear();
            }
        }

        while (!isProven) {
            printState();

            String commandString = InputDevice.read();
            while (!CommandParser.parse(commandString)) {
                ErrorHelper.printAndReset();
                OutputDevice.writeNewLine();
                commandString = InputDevice.read();
            }

            Command command = CommandParser.getCurrentCommand();
            if (!handlePropositionalLogicCommand(commandString, command)) {
                ErrorHelper.printAndReset();
                OutputDevice.writeNewLine();
                arguments.clear();
            }
            WarningHelper.printAndReset();

            if (this.isProven) break;
        }

        if (this.parent == null) {
            OutputDevice.writeToConsole("Proof completed!");
        }
        else {
            OutputDevice.writeToConsole("Proof State completed!");
        }
    }

    int getIntegerIndex(Command command) {
        String argument = arguments.getFirst();
        String type = getArgumentType(argument);
        int index = getArgumentIndex(argument);

        if (!type.isEmpty()) {
            return -1;
        }
        return index + 1;
    }

    int getIndexOfStrategy(Command command) {
        String argument = arguments.getFirst();
        String type = getArgumentType(argument);
        int index = getArgumentIndex(argument);

        if (!type.equals(goalName)) {
            ManualPropositionalInferenceRuleHelper.addGoalError(command);
            return -1;
        }

        if (index >= goals.size()) {
            ManualPropositionalInferenceRuleHelper.addOutOfBoundsError(index);
            return -1;
        }

        return index;
    }

    private List<Integer> getIndexOfArityN(Command command) {
        List<Integer> indices = new ArrayList<>();

        int size = arguments.size();
        for (int i = 0 ; i < size ; i++) {
            String argument = arguments.get(i);
            String type = getArgumentType(argument);
            int index = getArgumentIndex(argument);

            if (!type.equals(kbName)) {
                ManualPropositionalInferenceRuleHelper.addKBError(command);
                return new ArrayList<>();
            }

            if (index > knowledgeBase.size()) {
                ManualPropositionalInferenceRuleHelper.addOutOfBoundsError(index);
                return new ArrayList<>();
            }
            indices.add(index);
        }

        return indices;
    }

    private boolean handlePropositionalLogicCommand(String commandString, Command command) {
        arguments = CommandParser.getArguments(commandString);

        switch (command) {
            case IMPLICATION_STRATEGY -> {
                int index = getIndexOfStrategy(command);
                if (index == -1) return false;

                return ManualPropositionalStrategyHelper.handleImplicationStrategy(index);
            }
            case EQUIVALENCE_STRATEGY -> {
                int index = getIndexOfStrategy(command);
                if (index == -1) return false;

                return ManualPropositionalStrategyHelper.handleEquivalenceStrategy(index, this);
            }
            case CONJUNCTION_STRATEGY -> {
                int index = getIndexOfStrategy(command);
                if (index == -1) return false;

                return ManualPropositionalStrategyHelper.handleConjunctionStrategy(index, this);
            }
            case DISJUNCTION_STRATEGY -> {
                int index = getIndexOfStrategy(command);
                if (index == -1) return false;

                return ManualPropositionalStrategyHelper.handleDisjunctionStrategy(index);
            }
            case NEGATION_STRATEGY -> {
                int index = getIndexOfStrategy(command);
                if (index == -1) return false;

                return ManualPropositionalStrategyHelper.handleNegationStrategy(index);
            }
            case PROOF_BY_CASES -> {
                List<Integer> indices = getIndexOfArityN(command);
                if (indices.isEmpty()) return false;

                return ManualPropositionalStrategyHelper.handleProofByCases(indices.getFirst(), this);
            }
            case MODUS_PONENS -> {
                List<Integer> indices = getIndexOfArityN(command);
                if (indices.isEmpty()) return false;

                return ManualPropositionalInferenceRuleHelper.handleModusPonens(indices.getFirst(), indices.get(1));
            }
            case MODUS_TOLLENS -> {
                List<Integer> indices = getIndexOfArityN(command);
                if (indices.isEmpty()) return false;

                return ManualPropositionalInferenceRuleHelper.handleModusTollens(indices.getFirst(), indices.get(1));
            }
            case HYPOTHETICAL_SYLLOGISM -> {
                List<Integer> indices = getIndexOfArityN(command);
                if (indices.isEmpty()) return false;

                return ManualPropositionalInferenceRuleHelper.handleHypotheticalSyllogism(indices.getFirst(), indices.get(1));
            }
            case DISJUNCTIVE_SYLLOGISM -> {
                List<Integer> indices = getIndexOfArityN(command);
                if (indices.isEmpty()) return false;

                return ManualPropositionalInferenceRuleHelper.handleDisjunctiveSyllogism(indices.getFirst(), indices.get(1));
            }
            case CONSTRUCTIVE_DILEMMA -> {
                List<Integer> indices = getIndexOfArityN(command);
                if (indices.isEmpty()) return false;

                return ManualPropositionalInferenceRuleHelper.handleConstructiveDilemma(indices.getFirst(), indices.get(1), indices.get(2));
            }
            case DESTRUCTIVE_DILEMMA -> {
                List<Integer> indices = getIndexOfArityN(command);
                if (indices.isEmpty()) return false;

                return ManualPropositionalInferenceRuleHelper.handleDestructiveDilemma(indices.getFirst(), indices.get(1), indices.get(2));
            }
            case ABSORPTION -> {
                List<Integer> indices = getIndexOfArityN(command);
                if (indices.isEmpty()) return false;

                return ManualPropositionalInferenceRuleHelper.handleAbsorption(indices.getFirst());
            }
            case TRANSPOSITION -> {
                List<Integer> indices = getIndexOfArityN(command);
                if (indices.isEmpty()) return false;

                return ManualPropositionalInferenceRuleHelper.handleTransposition(indices.getFirst());
            }
            case MATERIAL_EQUIVALENCE -> {
                List<Integer> indices = getIndexOfArityN(command);
                if (indices.isEmpty()) return false;

                return ManualPropositionalInferenceRuleHelper.handleMaterialEquivalence(indices.getFirst());
            }
            case MATERIAL_IMPLICATION -> {
                List<Integer> indices = getIndexOfArityN(command);
                if (indices.isEmpty()) return false;

                return ManualPropositionalInferenceRuleHelper.handleMaterialImplication(indices.getFirst());
            }
            case IMPLICATION_INTRODUCTION -> {
                List<Integer> indices = getIndexOfArityN(command);
                if (indices.isEmpty()) return false;

                return ManualPropositionalInferenceRuleHelper.handleImplicationIntroduction(indices.getFirst(), indices.get(1));
            }
            case IMPLICATION_ELIMINATION -> {
                List<Integer> indices = getIndexOfArityN(command);
                if (indices.isEmpty()) return false;

                return ManualPropositionalInferenceRuleHelper.handleImplicationSimplification(indices.getFirst());
            }
            case EQUIVALENCE_INTRODUCTION -> {
                List<Integer> indices = getIndexOfArityN(command);
                if (indices.isEmpty()) return false;

                return ManualPropositionalInferenceRuleHelper.handleEquivalenceIntroduction(indices.getFirst(), indices.get(1));
            }
            case EQUIVALENCE_SIMPLIFICATION -> {
                List<Integer> indices = getIndexOfArityN(command);
                if (indices.isEmpty()) return false;

                return ManualPropositionalInferenceRuleHelper.handleEquivalenceSimplification(indices.getFirst());
            }
            case CONJUNCTION_INTRODUCTION -> {
                List<Integer> indices = getIndexOfArityN(command);
                if (indices.isEmpty()) return false;

                return ManualPropositionalInferenceRuleHelper.handleConjunctionIntroduction(indices.getFirst(), indices.get(1));
            }
            case CONJUNCTION_ELIMINATION -> {
                List<Integer> indices = getIndexOfArityN(command);
                if (indices.isEmpty()) return false;

                return ManualPropositionalInferenceRuleHelper.handleConjunctionElimination(indices.getFirst());
            }
            case DISJUNCTION_INTRODUCTION -> {
                List<Integer> indices = getIndexOfArityN(command);
                if (indices.isEmpty()) return false;


            }
            case DISJUNCTION_ELIMINATION -> {
                List<Integer> indices = getIndexOfArityN(command);
                if (indices.isEmpty()) return false;

                return ManualPropositionalInferenceRuleHelper.handleDisjunctionElimination(indices.getFirst(), indices.get(1));
            }
            case DISJUNCTION_SIMPLIFICATION -> {
                List<Integer> indices = getIndexOfArityN(command);
                if (indices.isEmpty()) return false;

                return ManualPropositionalInferenceRuleHelper.handleDisjunctionSimplification(indices.getFirst());
            }
            case DEMORGAN -> {
                List<Integer> indices = getIndexOfArityN(command);
                if (indices.isEmpty()) return false;

                return ManualPropositionalInferenceRuleHelper.handleDeMorgan(indices.getFirst());
            }
            case CONTRADICTION -> {
                List<Integer> indices = new ArrayList<>();
                int argumentCount = arguments.size();

                int currentIndex = 0;
                while (currentIndex < argumentCount) {
                    String currentArgument = arguments.getFirst();
                    String type = getArgumentType(currentArgument);
                    int index = getArgumentIndex(currentArgument);

                    if (!type.equals(kbName)) {
                        ManualPropositionalInferenceRuleHelper.addKBError(command);
                    }

                    indices.add(index);
                    currentIndex++;
                }

                isProven = handleContradiction(indices);
            }
            case CHANGE_STATE -> {
                int index = getIntegerIndex(command);
                return handleChangeState(index);
            }
            case DONE -> {
                //TODO Look into case of Disjunction (not all goals need to be in the KB)
                for (AST goal : goals) {
                    boolean found = false;
                    for (AST kb : knowledgeBase) {
                        if (kb.isEquivalentTo(goal)) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        ErrorHelper.add("Proof could not be completed! Goal '"  + goal + "' is not in the Knowledge Base!");
                        isProven = false;
                        return false;
                    }
                }

                for (ManualPropositionalProof proof : stateGoals) {
                    if (!proof.isProven()) {
                        ErrorHelper.add("Proof could not be completed! State '"  + proof.getStateIndex() + "' has not been proven!");
                        isProven = false;
                        return false;
                    }
                }
                isProven = true;
                return true;
            }
            case ERROR -> {
                ErrorHelper.add("Command " + commandString + " does not exist!");
                return false;
            }
        }
        return false;
    }

    private boolean handleChangeState(int index) {
        if (index > ManualPropositionalProofStates.getCurrentStateIndex()) {
            ManualPropositionalInferenceRuleHelper.addStateError(index);
            return false;
        }
        ManualPropositionalProofStates.getState(index).prove();
        return true;
    }

    private boolean handleContradiction(List<Integer> indices) {
        int indexCount = indices.size();

        if (indexCount == 1) {
            AST ast = knowledgeBase.get(indices.getFirst());
            if (PropositionalLogicHelper.getOutermostOperation(ast) == LogicalOperator.CONJUNCTION) {
                AST left = ast.getSubtree(0);
                AST right = ast.getSubtree(1);
                left.negate();

                if (left.isEquivalentTo(right)) return true;
            }

            ErrorHelper.add("Cannot derive a Contradiction from " + ast + "!");
            return false;
        }
        else if (indexCount == 2) {
            AST ast1 = knowledgeBase.get(indices.getFirst());
            AST ast2 = knowledgeBase.get(indices.get(1));
            ast1.negate();

            if (ast1.isEquivalentTo(ast2)) return true;

            ErrorHelper.add("Cannot derive a Contradiction from " + ast1 + " and " + ast2 + "!");
            return false;
        }

        return false;
    }

    private void printState() {
        OutputDevice.writeToConsole("State: " + stateIndex);
        OutputDevice.writeNewLine();

        if (!knowledgeBase.isEmpty()) OutputDevice.writeNumberedToConsole(knowledgeBase, 1, kbName);
        if (!goals.isEmpty()) OutputDevice.writeNumberedToConsole(goals, 1, goalName);
        if (!childStates.isEmpty()) OutputDevice.writeNumberedStateToConsole(childStates, 1, stateName);
    }

    private String getArgumentType(String argument) {
        StringBuilder type = new StringBuilder();

        int idx = 0;
        while (idx < argument.length()) {
            if ('0' < argument.charAt(idx) && argument.charAt(idx) < '9') {
                break;
            }
            else type.append(argument.charAt(idx));
            idx++;
        }

        return type.toString();
    }

    private int getArgumentIndex(String argument) {
        StringBuilder index = new StringBuilder();

        int idx = 0;
        while (idx < argument.length()) {
            if ('A' < argument.charAt(idx) && argument.charAt(idx) < 'z') {
                idx++;
                continue;
            }
            else index.append(argument.charAt(idx));
            idx++;
        }

        return Integer.parseInt(index.toString()) - 1;
    }

}
