package me.dariansandru.domain.proof.manual_proof;

import me.dariansandru.domain.LogicalOperator;
import me.dariansandru.domain.data_structures.ast.AST;
import me.dariansandru.domain.data_structures.ast.PropositionalAST;
import me.dariansandru.io.InputDevice;
import me.dariansandru.io.OutputDevice;
import me.dariansandru.parser.command.Command;
import me.dariansandru.parser.command.CommandParser;
import me.dariansandru.utils.flyweight.LogicalOperatorFlyweight;
import me.dariansandru.utils.helper.ErrorHelper;
import me.dariansandru.utils.helper.KnowledgeBaseRegistry;
import me.dariansandru.utils.helper.PropositionalLogicHelper;
import me.dariansandru.utils.helper.WarningHelper;
import me.dariansandru.utils.manual.Manual;

import java.util.ArrayList;
import java.util.List;

public class ManualPropositionalProof {

    private final List<AST> knowledgeBase;
    private final List<AST> goals;
    private final List<ManualPropositionalProof> stateGoals = new ArrayList<>();
    private boolean isProven = false;

    private final String kbName = "KB";
    private final String goalName = "G";
    private final String stateName = "S";

    // private final List<String> assumptions = new ArrayList<>();
    // private final List<String> conclusions = new ArrayList<>();
    private List<String> arguments = new ArrayList<>();

    private final List<ManualPropositionalProof> childStates = new ArrayList<>();
    private final ManualPropositionalProof parent;
    private final int stateIndex;

    public ManualPropositionalProof(List<AST> knowledgeBase, List<AST> goals, ManualPropositionalProof parent, int index) {
        this.knowledgeBase = knowledgeBase;
        this.goals = goals;
        this.parent = parent;
        this.stateIndex = index;
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

    private void addHypothesis() {
        for (AST ast : knowledgeBase) {
            KnowledgeBaseRegistry.addObtainedFrom(ast.toString(), "Hypothesis");
        }
        for (AST ast : goals) {
            KnowledgeBaseRegistry.addObtainedFrom(ast.toString(), "Hypothesis");
        }
    }

    public void prove() {
        addHypothesis();

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
            addGoalError(command);
            return -1;
        }

        if (index >= goals.size()) {
            addOutOfBoundsError(index);
            return -1;
        }

        return index;
    }

    private int getIndexOfArityOne(Command command) {
        String argument = arguments.getFirst();
        String type = getArgumentType(argument);
        int index = getArgumentIndex(argument);

        if (!type.equals(kbName)) {
            addKBError(command);
            return -1;
        }

        if (index >= knowledgeBase.size()) {
            addOutOfBoundsError(index);
            return -1;
        }

        return index;
    }

    private List<Integer> getIndexOfArityTwo(Command command) {
        String argument1 = arguments.getFirst();
        String type1 = getArgumentType(argument1);
        int index1 = getArgumentIndex(argument1);

        String argument2 = arguments.get(1);
        String type2 = getArgumentType(argument2);
        int index2 = getArgumentIndex(argument2);

        if (!type1.equals(kbName) || !type2.equals(kbName)) {
            addKBError(command);
            return new ArrayList<>();
        }

        if (index1 >= knowledgeBase.size()) {
            addOutOfBoundsError(index1);
            return new ArrayList<>();
        }
        else if (index2 >= knowledgeBase.size()) {
            addOutOfBoundsError(index2);
            return new ArrayList<>();
        }

        return List.of(index1, index2);
    }

    private List<Integer> getIndexOfArityThree(Command command) {
        String argument1 = arguments.getFirst();
        String type1 = getArgumentType(argument1);
        int index1 = getArgumentIndex(argument1);

        String argument2 = arguments.get(1);
        String type2 = getArgumentType(argument2);
        int index2 = getArgumentIndex(argument2);

        String argument3 = arguments.get(2);
        String type3 = getArgumentType(argument3);
        int index3 = getArgumentIndex(argument3);

        if (!type1.equals(kbName) || !type2.equals(kbName) || !type3.equals(kbName)) {
            addKBError(command);
            return new ArrayList<>();
        }

        if (index1 >= knowledgeBase.size()) {
            addOutOfBoundsError(index1);
            return new ArrayList<>();
        }
        else if (index2 >= knowledgeBase.size()) {
            addOutOfBoundsError(index2);
            return new ArrayList<>();
        }
        else if (index3 >= knowledgeBase.size()) {
            addOutOfBoundsError(index3);
            return new ArrayList<>();
        }

        return List.of(index1, index2, index3);
    }

    private boolean handlePropositionalLogicCommand(String commandString, Command command) {
        arguments = CommandParser.getArguments(commandString);

        switch (command) {
            case IMPLICATION_STRATEGY -> {
                int index = getIndexOfStrategy(command);
                if (index == -1) return false;

                return handleImplicationStrategy(index);
            }
            case EQUIVALENCE_STRATEGY -> {
                int index = getIndexOfStrategy(command);
                if (index == -1) return false;

                return handleEquivalenceStrategy(index);
            }
            case CONJUNCTION_STRATEGY -> {
                int index = getIndexOfStrategy(command);
                if (index == -1) return false;

                return handleConjunctionStrategy(index);
            }
            case DISJUNCTION_STRATEGY -> {
                int index = getIndexOfStrategy(command);
                if (index == -1) return false;

                return handleDisjunctionStrategy(index);
            }
            case NEGATION_STRATEGY -> {
                int index = getIndexOfStrategy(command);
                if (index == -1) return false;

                return handleNegationStrategy(index);
            }
            case PROOF_BY_CASES -> {
                int index = getIndexOfArityOne(command);
                if (index == -1) return false;

                return handleProofByCases(index);
            }
            case MODUS_PONENS -> {
                List<Integer> indices = getIndexOfArityTwo(command);
                if (indices.isEmpty()) return false;

                return handleModusPonens(indices.getFirst(), indices.get(1));
            }
            case MODUS_TOLLENS -> {
                List<Integer> indices = getIndexOfArityTwo(command);
                if (indices.isEmpty()) return false;

                return handleModusTollens(indices.getFirst(), indices.get(1));
            }
            case HYPOTHETICAL_SYLLOGISM -> {
                List<Integer> indices = getIndexOfArityTwo(command);
                if (indices.isEmpty()) return false;

                return handleHypotheticalSyllogism(indices.getFirst(), indices.get(1));
            }
            case DISJUNCTIVE_SYLLOGISM -> {
                List<Integer> indices = getIndexOfArityTwo(command);
                if (indices.isEmpty()) return false;

                return handleDisjunctiveSyllogism(indices.getFirst(), indices.get(1));
            }
            case CONSTRUCTIVE_DILEMMA -> {
                List<Integer> indices = getIndexOfArityThree(command);
                if (indices.isEmpty()) return false;

                return handleConstructiveDilemma(indices.getFirst(), indices.get(1), indices.get(2));
            }
            case DESTRUCTIVE_DILEMMA -> {
                List<Integer> indices = getIndexOfArityThree(command);
                if (indices.isEmpty()) return false;

                return handleDestructiveDilemma(indices.getFirst(), indices.get(1), indices.get(2));
            }
            case ABSORPTION -> {
                int index = getIndexOfArityOne(command);
                if (index == -1) return false;

                return handleAbsorption(index);
            }
            case TRANSPOSITION -> {
                int index = getIndexOfArityOne(command);
                if (index == -1) return false;

                return handleTransposition(index);
            }
            case MATERIAL_EQUIVALENCE -> {
                int index = getIndexOfArityOne(command);
                if (index == -1) return false;

                return handleMaterialEquivalence(index);
            }
            case MATERIAL_IMPLICATION -> {
                int index = getIndexOfArityOne(command);
                if (index == -1) return false;

                return handleMaterialImplication(index);
            }
            case IMPLICATION_INTRODUCTION -> {
                List<Integer> indices = getIndexOfArityTwo(command);
                if (indices.isEmpty()) return false;

                return handleImplicationIntroduction(indices.getFirst(), indices.get(1));
            }
            case IMPLICATION_ELIMINATION -> {
                int index = getIndexOfArityOne(command);
                if (index == -1) return false;

                return handleImplicationSimplification(index);
            }
            case EQUIVALENCE_INTRODUCTION -> {
                List<Integer> indices = getIndexOfArityTwo(command);
                if (indices.isEmpty()) return false;

                return handleEquivalenceIntroduction(indices.getFirst(), indices.get(1));
            }
            case EQUIVALENCE_SIMPLIFICATION -> {
                int index = getIndexOfArityOne(command);
                if (index == -1) return false;

                return handleEquivalenceSimplification(index);
            }
            case CONJUNCTION_INTRODUCTION -> {
                List<Integer> indices = getIndexOfArityTwo(command);
                if (indices.isEmpty()) return false;

                return handleConjunctionIntroduction(indices.getFirst(), indices.get(1));
            }
            case CONJUNCTION_ELIMINATION -> {
                int index = getIndexOfArityOne(command);
                if (index == -1) return false;

                return handleConjunctionElimination(index);
            }
            case DISJUNCTION_INTRODUCTION -> {

            }
            case DISJUNCTION_ELIMINATION -> {
                List<Integer> indices = getIndexOfArityTwo(command);
                if (indices.isEmpty()) return false;

                return handleDisjunctionElimination(indices.getFirst(), indices.get(1));
            }
            case DISJUNCTION_SIMPLIFICATION -> {
                int index = getIndexOfArityOne(command);
                if (index == -1) return false;

                return handleDisjunctionSimplification(index);
            }
            case DEMORGAN -> {
                int index = getIndexOfArityOne(command);
                if (index == -1) return false;

                return handleDeMorgan(index);
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
                        addKBError(command);
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
        System.out.println(index);
        ManualPropositionalProofStates.getState(index).prove();
        return true;
    }

    private boolean handleImplicationStrategy(int index) {
        AST ast = goals.get(index);
        if (PropositionalLogicHelper.getOutermostOperation(ast) != LogicalOperator.IMPLICATION) {
            ErrorHelper.add("Cannot apply command on " + ast +
                    ". Outermost logical operator is not '" + LogicalOperatorFlyweight.getImplicationString() + "'!");
            return false;
        }

        AST newAST1 = ast.getSubtree(0);
        AST newAST2 = ast.getSubtree(1);
        if (!containsEntry(newAST1)) knowledgeBase.add(newAST1);

        goals.remove(ast);
        goals.add(newAST2);

        KnowledgeBaseRegistry.addObtainedFrom(newAST1.toString(), List.of(ast.toString()), "Implication Strategy");
        KnowledgeBaseRegistry.addObtainedFrom(newAST2.toString(), List.of(ast.toString()), "Implication Strategy");
        return true;
    }

    private boolean handleEquivalenceStrategy(int index) {
        AST ast = goals.get(index);
        if (PropositionalLogicHelper.getOutermostOperation(ast) != LogicalOperator.EQUIVALENCE) {
            ErrorHelper.add("Cannot apply command on " + ast +
                    ". Outermost logical operator is not '" + LogicalOperatorFlyweight.getEquivalenceString() + "'!");
            return false;
        }

        goals.remove(ast);
        AST left = ast.getSubtree(0);
        AST right = ast.getSubtree(1);
        AST newAST1 = new PropositionalAST(left + " " + LogicalOperatorFlyweight.getImplicationString() + " " + right, true);
        AST newAST2 = new PropositionalAST(right + " " + LogicalOperatorFlyweight.getImplicationString() + " " + left, true);

        createNewStates(newAST1, newAST2);

        KnowledgeBaseRegistry.addObtainedFrom(newAST1.toString(), List.of(ast.toString()), "Equivalence Strategy");
        KnowledgeBaseRegistry.addObtainedFrom(newAST2.toString(), List.of(ast.toString()), "Equivalence Strategy");
        return true;
    }

    private boolean handleConjunctionStrategy(int index) {
        AST ast = goals.get(index);
        if (PropositionalLogicHelper.getOutermostOperation(ast) != LogicalOperator.CONJUNCTION) {
            ErrorHelper.add("Cannot apply command on " + ast +
                    ". Outermost logical operator is not '" + LogicalOperatorFlyweight.getConjunctionString() + "'!");
            return false;
        }

        AST newAST1 = ast.getSubtree(0);
        AST newAST2 = ast.getSubtree(1);
        goals.remove(ast);

        createNewStates(newAST1, newAST2);

        KnowledgeBaseRegistry.addObtainedFrom(newAST1.toString(), List.of(ast.toString()), "Conjunction Strategy");
        KnowledgeBaseRegistry.addObtainedFrom(newAST2.toString(), List.of(ast.toString()), "Conjunction Strategy");
        return true;
    }

    private void createNewStates(AST newAST1, AST newAST2) {
        List<AST> newGoals1 = new ArrayList<>();
        copyGoals(newGoals1, goals);
        newGoals1.add(newAST1);
        ManualPropositionalProof newState1 = new ManualPropositionalProof(knowledgeBase, newGoals1, this, this.stateIndex + 1);

        List<AST> newGoals2 = new ArrayList<>();
        copyGoals(newGoals2, goals);
        newGoals2.add(newAST2);
        ManualPropositionalProof newState2 = new ManualPropositionalProof(knowledgeBase, newGoals2, this, this.stateIndex + 2);

        childStates.add(newState1);
        childStates.add(newState2);

        stateGoals.add(newState1);
        stateGoals.add(newState2);

        ManualPropositionalProofStates.addState(newState1, stateIndex + 1);
        ManualPropositionalProofStates.addState(newState2, stateIndex + 2);
    }

    private void createNewKBStates(AST newAST1, AST newAST2) {
        List<AST> newKB1 = new ArrayList<>();
        List<AST> newGoals1 = new ArrayList<>();
        copyGoals(newGoals1, goals);
        copyKB(newKB1, knowledgeBase);

        newKB1.add(newAST1);
        ManualPropositionalProof newState1 = new ManualPropositionalProof(newKB1, newGoals1, this, this.stateIndex + 1);

        List<AST> newKB2 = new ArrayList<>();
        copyKB(newKB2, knowledgeBase);
        List<AST> newGoals2 = new ArrayList<>();
        copyGoals(newGoals2, goals);

        newKB2.add(newAST2);
        ManualPropositionalProof newState2 = new ManualPropositionalProof(newKB2, newGoals2, this, this.stateIndex + 2);

        childStates.add(newState1);
        childStates.add(newState2);

        stateGoals.add(newState1);
        stateGoals.add(newState2);

        ManualPropositionalProofStates.addState(newState1, stateIndex + 1);
        ManualPropositionalProofStates.addState(newState2, stateIndex + 2);
    }

    private boolean handleDisjunctionStrategy(int index) {
        return false;
    }

    private boolean handleNegationStrategy(int index) {
        AST ast = goals.get(index);
        ast.negate();
        AST contradictionAST = new PropositionalAST(true);
        if (!containsEntry(ast)) knowledgeBase.add(ast);
        goals.remove(ast);
        goals.add(contradictionAST);

        if (PropositionalLogicHelper.getOutermostOperation(ast) != LogicalOperator.NEGATION) {
            KnowledgeBaseRegistry.addObtainedFrom(ast.toString(), List.of(), "Proof of Negation");
            KnowledgeBaseRegistry.addObtainedFrom(contradictionAST.toString(), List.of(), "Proof of Negation");
        }
        else {
            KnowledgeBaseRegistry.addObtainedFrom(ast.toString(), List.of(), "Proof by Contradiction");
            KnowledgeBaseRegistry.addObtainedFrom(contradictionAST.toString(), List.of(), "Proof by Contradiction");
        }
        return true;
    }

    private boolean handleProofByCases(int index) {
        AST ast = knowledgeBase.get(index);
        knowledgeBase.remove(ast);

        if (PropositionalLogicHelper.getOutermostOperation(ast) == LogicalOperator.DISJUNCTION) {
            AST left = ast.getSubtree(0);
            AST right = ast.getSubtree(1);
            createNewKBStates(left, right);

            KnowledgeBaseRegistry.addObtainedFrom(left.toString(), List.of(ast.toString()), "Proof by Cases");
            KnowledgeBaseRegistry.addObtainedFrom(right.toString(), List.of(ast.toString()), "Proof by Cases");

            goals.removeFirst();
            return true;
        }

        ErrorHelper.add("Cannot apply Proof by Cases on " + ast + "!");
        return false;
    }

    private boolean handleModusPonens(int index1, int index2) {
        AST ast1 = knowledgeBase.get(index1);
        AST ast2 = knowledgeBase.get(index2);

        if (PropositionalLogicHelper.getOutermostOperation(ast1) == LogicalOperator.IMPLICATION) {
            if (canApplyModusPonens(ast1, ast2)) return true;
        }
        else if (PropositionalLogicHelper.getOutermostOperation(ast2) == LogicalOperator.IMPLICATION) {
            if (canApplyModusPonens(ast2, ast1)) return true;
        }

        ErrorHelper.add("Cannot apply Modus Ponens on " + ast1 + " and " + ast2 + "!");
        return false;
    }

    private boolean canApplyModusPonens(AST ast1, AST ast2) {
        AST antecedent1 = ast1.getSubtree(0);
        AST conclusion1 = ast1.getSubtree(1);

        if (antecedent1.isEquivalentTo(ast2)) {
            KnowledgeBaseRegistry.addObtainedFrom(conclusion1.toString(), List.of(antecedent1.toString(), ast1.toString()), "Modus Ponens");
            if (!containsEntry(conclusion1)) knowledgeBase.add(conclusion1);
            return true;
        }
        return false;
    }

    private boolean handleModusTollens(int index1, int index2) {
        AST ast1 = knowledgeBase.get(index1);
        AST ast2 = knowledgeBase.get(index2);

        if (PropositionalLogicHelper.getOutermostOperation(ast1) == LogicalOperator.IMPLICATION) {
            if (canApplyModusTollens(ast1, ast2)) return true;
        }
        else if (PropositionalLogicHelper.getOutermostOperation(ast2) == LogicalOperator.IMPLICATION) {
            if (canApplyModusTollens(ast2, ast1)) return true;
        }

        ErrorHelper.add("Cannot apply Modus Tollens on " + ast1 + " and " + ast2 + "!");
        return false;
    }

    private boolean canApplyModusTollens(AST ast1, AST ast2) {
        AST antecedent1 = ast1.getSubtree(0);
        AST conclusion1 = ast1.getSubtree(1);
        conclusion1.negate();

        if (conclusion1.isEquivalentTo(ast2)) {
            antecedent1.negate();
            KnowledgeBaseRegistry.addObtainedFrom(antecedent1.toString(), List.of(conclusion1.toString(), ast1.toString()), "Modus Tollens");
            if (!containsEntry(antecedent1)) knowledgeBase.add(antecedent1);
            return true;
        }
        return false;
    }

    private boolean handleHypotheticalSyllogism(int index1, int index2) {
        AST ast1 = knowledgeBase.get(index1);
        AST ast2 = knowledgeBase.get(index2);

        if (PropositionalLogicHelper.getOutermostOperation(ast1) != LogicalOperator.IMPLICATION ||
            PropositionalLogicHelper.getOutermostOperation(ast2) != LogicalOperator.IMPLICATION) {
            ErrorHelper.add("Cannot apply Hypothetical Syllogism on " + ast1 + " and " + ast2 + "!");
            return false;
        }

        AST antecedent1 = ast1.getSubtree(0);
        AST conclusion1 = ast1.getSubtree(1);
        AST antecedent2 = ast2.getSubtree(0);
        AST conclusion2 = ast2.getSubtree(1);

        if (conclusion1.isEquivalentTo(antecedent2)) {
            AST newAST = new PropositionalAST(antecedent1.toString() + " " + LogicalOperatorFlyweight.getImplicationString() + " " + conclusion2.toString(), true);
            KnowledgeBaseRegistry.addObtainedFrom(newAST.toString(), List.of(ast1.toString(), ast2.toString()), "Hypothetical Syllogism");
            if (!containsEntry(newAST)) knowledgeBase.add(newAST);
            return true;
        }
        else if (conclusion2.isEquivalentTo(antecedent1)) {
            AST newAST = new PropositionalAST(antecedent2 + " " + LogicalOperatorFlyweight.getImplicationString() + " " + conclusion1, true);
            KnowledgeBaseRegistry.addObtainedFrom(newAST.toString(), List.of(ast1.toString(), ast2.toString()), "Hypothetical Syllogism");
            if (!containsEntry(newAST)) knowledgeBase.add(newAST);
            return true;
        }

        ErrorHelper.add("Cannot apply Hypothetical Syllogism on " + ast1 + " and " + ast2 + "!");
        return false;
    }

    private boolean handleDisjunctiveSyllogism(int index1, int index2) {
        AST ast1 = knowledgeBase.get(index1);
        AST ast2 = knowledgeBase.get(index2);

        if (PropositionalLogicHelper.getOutermostOperation(ast1) != LogicalOperator.DISJUNCTION &&
            PropositionalLogicHelper.getOutermostOperation(ast2) != LogicalOperator.DISJUNCTION) {
            ErrorHelper.add("Cannot apply Disjunctive Syllogism on " + ast1 + " and " + ast2 + "!");
            return false;
        }

        if (PropositionalLogicHelper.getOutermostOperation(ast1) == LogicalOperator.DISJUNCTION) {
            if (canApplyDisjunctiveSyllogism(ast2, ast1, ast1.toString(), ast2.toString())) return true;
        }
        else if (PropositionalLogicHelper.getOutermostOperation(ast2) == LogicalOperator.DISJUNCTION) {
            if (canApplyDisjunctiveSyllogism(ast1, ast2, ast1.toString(), ast2.toString())) return true;
        }

        ErrorHelper.add("Cannot apply Disjunctive Syllogism on " + ast1 + " and " + ast2 + "!");
        return false;
    }

    private boolean canApplyDisjunctiveSyllogism(AST ast1, AST ast2, String string, String string2) {
        AST left = ast2.getSubtree(0);
        AST leftCopy = new PropositionalAST(left.toString(), true);

        AST right = ast2.getSubtree(1);
        AST rightCopy = new PropositionalAST(right.toString(), true);

        left.negate();
        right.negate();

        if (left.isEquivalentTo(ast1)) {
            KnowledgeBaseRegistry.addObtainedFrom(rightCopy.toString(), List.of(string, string2), "Disjunctive Syllogism");
            if (!containsEntry(rightCopy)) knowledgeBase.add(rightCopy);
            return true;
        }
        else if (right.isEquivalentTo(ast1)) {
            KnowledgeBaseRegistry.addObtainedFrom(leftCopy.toString(), List.of(string, string2), "Disjunctive Syllogism");
            if (!containsEntry(left)) knowledgeBase.add(leftCopy);
            return true;
        }
        return false;
    }

    private boolean handleConstructiveDilemma(int index1, int index2, int index3) {
        AST ast1 = knowledgeBase.get(index1);
        AST ast2 = knowledgeBase.get(index2);
        AST ast3 = knowledgeBase.get(index3);

        if (PropositionalLogicHelper.getOutermostOperation(ast1) == LogicalOperator.IMPLICATION &&
            PropositionalLogicHelper.getOutermostOperation(ast2) == LogicalOperator.IMPLICATION &&
            PropositionalLogicHelper.getOutermostOperation(ast3) == LogicalOperator.DISJUNCTION) {
            if (canApplyConstructiveSyllogism(ast1, ast2, ast3, ast2.toString(), ast3.toString())) return true;
        }
        else if (PropositionalLogicHelper.getOutermostOperation(ast1) == LogicalOperator.IMPLICATION &&
                PropositionalLogicHelper.getOutermostOperation(ast2) == LogicalOperator.DISJUNCTION &&
                PropositionalLogicHelper.getOutermostOperation(ast3) == LogicalOperator.IMPLICATION) {
            if (canApplyConstructiveSyllogism(ast1, ast3, ast2, ast2.toString(), ast3.toString())) return true;
        }
        else if (PropositionalLogicHelper.getOutermostOperation(ast1) == LogicalOperator.DISJUNCTION &&
                PropositionalLogicHelper.getOutermostOperation(ast2) == LogicalOperator.IMPLICATION &&
                PropositionalLogicHelper.getOutermostOperation(ast3) == LogicalOperator.IMPLICATION) {
            AST antecedent1 = ast2.getSubtree(0);
            AST conclusion1 = ast2.getSubtree(1);
            AST antecedent2 = ast3.getSubtree(0);
            AST conclusion2 = ast3.getSubtree(1);
            AST left = ast1.getSubtree(0);
            AST right = ast1.getSubtree(1);

            if (antecedent1.isEquivalentTo(left) && antecedent2.isEquivalentTo(right) ||
                    antecedent1.isEquivalentTo(right) && antecedent2.isEquivalentTo(left)) {
                AST newAST = new PropositionalAST(conclusion1.toString() + " " + LogicalOperatorFlyweight.getDisjunctionString() + " " + conclusion2.toString(), true);
                KnowledgeBaseRegistry.addObtainedFrom(newAST.toString(), List.of(ast1.toString(), ast2.toString(), ast3.toString()), "Constructive Dilemma");
                if (!containsEntry(newAST)) knowledgeBase.add(newAST);
                return true;
            }
        }
        ErrorHelper.add("Cannot apply Constructive Dilemma on " + ast1 + ", " + ast2 + " and " + ast3 + "!");
        return false;
    }

    private boolean canApplyConstructiveSyllogism(AST ast1, AST ast2, AST ast3, String string, String string2) {
        AST antecedent1 = ast1.getSubtree(0);
        AST conclusion1 = ast1.getSubtree(1);
        AST antecedent2 = ast2.getSubtree(0);
        AST conclusion2 = ast2.getSubtree(1);
        AST left = ast3.getSubtree(0);
        AST right = ast3.getSubtree(1);

        if (antecedent1.isEquivalentTo(left) && antecedent2.isEquivalentTo(right) ||
                antecedent1.isEquivalentTo(right) && antecedent2.isEquivalentTo(left)) {
            AST newAST = new PropositionalAST(conclusion1.toString() + " " + LogicalOperatorFlyweight.getDisjunctionString() + " " + conclusion2.toString(), true);
            KnowledgeBaseRegistry.addObtainedFrom(newAST.toString(), List.of(ast1.toString(), string, string2), "Constructive Dilemma");
            if (!containsEntry(newAST)) knowledgeBase.add(newAST);
            return true;
        }
        return false;
    }

    private boolean handleDestructiveDilemma(int index1, int index2, int index3) {
        AST ast1 = knowledgeBase.get(index1);
        AST ast2 = knowledgeBase.get(index2);
        AST ast3 = knowledgeBase.get(index3);

        if (PropositionalLogicHelper.getOutermostOperation(ast1) == LogicalOperator.IMPLICATION &&
                PropositionalLogicHelper.getOutermostOperation(ast2) == LogicalOperator.IMPLICATION &&
                PropositionalLogicHelper.getOutermostOperation(ast3) == LogicalOperator.DISJUNCTION) {
            if (canApplyDestructiveDilemma(ast1, ast2, ast3, ast2.toString(), ast3.toString())) return true;
        }
        else if (PropositionalLogicHelper.getOutermostOperation(ast1) == LogicalOperator.IMPLICATION &&
                PropositionalLogicHelper.getOutermostOperation(ast2) == LogicalOperator.DISJUNCTION &&
                PropositionalLogicHelper.getOutermostOperation(ast3) == LogicalOperator.IMPLICATION) {
            if (canApplyDestructiveDilemma(ast1, ast3, ast2, ast2.toString(), ast3.toString())) return true;
        }
        else if (PropositionalLogicHelper.getOutermostOperation(ast1) == LogicalOperator.DISJUNCTION &&
                PropositionalLogicHelper.getOutermostOperation(ast2) == LogicalOperator.IMPLICATION &&
                PropositionalLogicHelper.getOutermostOperation(ast3) == LogicalOperator.IMPLICATION) {
            AST antecedent1 = ast2.getSubtree(0);
            AST conclusion1 = ast2.getSubtree(1);
            conclusion1.negate();

            AST antecedent2 = ast3.getSubtree(0);
            AST conclusion2 = ast3.getSubtree(1);
            conclusion2.negate();

            AST left = ast1.getSubtree(0);
            AST right = ast1.getSubtree(1);

            if (conclusion1.isEquivalentTo(left) && conclusion2.isEquivalentTo(right) ||
                    conclusion1.isEquivalentTo(right) && conclusion2.isEquivalentTo(left)) {
                antecedent1.negate();
                antecedent2.negate();

                AST newAST = new PropositionalAST(antecedent1 + " " + LogicalOperatorFlyweight.getDisjunctionString() + " " + antecedent2, true);
                KnowledgeBaseRegistry.addObtainedFrom(newAST.toString(), List.of(ast1.toString(), ast2.toString(), ast3.toString()), "Destructive Dilemma");
                if (!containsEntry(newAST)) knowledgeBase.add(newAST);
                return true;
            }
        }
        ErrorHelper.add("Cannot apply Destructive Dilemma on " + ast1 + ", " + ast2 + " and " + ast3 + "!");
        return false;
    }

    private boolean canApplyDestructiveDilemma(AST ast1, AST ast2, AST ast3, String string, String string2) {
        AST antecedent1 = ast1.getSubtree(0);
        AST conclusion1 = ast1.getSubtree(1);
        conclusion1.negate();

        AST antecedent2 = ast2.getSubtree(0);
        AST conclusion2 = ast2.getSubtree(1);
        conclusion2.negate();

        AST left = ast3.getSubtree(0);
        AST right = ast3.getSubtree(1);

        if (conclusion1.isEquivalentTo(left) && conclusion2.isEquivalentTo(right) ||
                conclusion1.isEquivalentTo(right) && conclusion2.isEquivalentTo(left)) {
            antecedent1.negate();
            antecedent2.negate();

            AST newAST = new PropositionalAST(antecedent1 + " " + LogicalOperatorFlyweight.getDisjunctionString() + " " + antecedent2, true);
            KnowledgeBaseRegistry.addObtainedFrom(newAST.toString(), List.of(ast1.toString(), string, string2), "Destructive Dilemma");
            if (!containsEntry(newAST)) knowledgeBase.add(newAST);
            return true;
        }
        return false;
    }

    private boolean handleAbsorption(int index) {
        AST ast = knowledgeBase.get(index);

        if (PropositionalLogicHelper.getOutermostOperation(ast) != LogicalOperator.IMPLICATION) {
            ErrorHelper.add("Cannot apply Absorption on " + ast + " !");
            return false;
        }

        AST antecedent = ast.getSubtree(0);
        AST conclusion = ast.getSubtree(1);
        AST newAST = new PropositionalAST(antecedent + " " + LogicalOperatorFlyweight.getImplicationString() + " ("
                            + antecedent + " " + LogicalOperatorFlyweight.getConjunctionString() + " " + conclusion + ")", true);
        KnowledgeBaseRegistry.addObtainedFrom(newAST.toString(), List.of(ast.toString()), "Absorption");
        if (!containsEntry(newAST)) knowledgeBase.add(newAST);
        return true;
    }

    private boolean handleTransposition(int index) {
       AST ast = knowledgeBase.get(index);

        if (PropositionalLogicHelper.getOutermostOperation(ast) != LogicalOperator.IMPLICATION) {
            ErrorHelper.add("Cannot apply Transposition on " + ast + " !");
            return false;
        }

        AST antecedent = ast.getSubtree(0);
        AST conclusion = ast.getSubtree(1);
        antecedent.negate();
        conclusion.negate();

        AST newAST = new PropositionalAST(conclusion + " " + LogicalOperatorFlyweight.getImplicationString() + " " + antecedent, true);
        KnowledgeBaseRegistry.addObtainedFrom(newAST.toString(), List.of(ast.toString()), "Transposition");
        if (!containsEntry(newAST)) knowledgeBase.add(newAST);
        return true;
    }

    private boolean handleMaterialEquivalence(int index) {
        AST ast = knowledgeBase.get(index);

        if (PropositionalLogicHelper.getOutermostOperation(ast) != LogicalOperator.EQUIVALENCE) {
            ErrorHelper.add("Cannot apply Material Equivalence on " + ast + "!");
            return false;
        }

        AST left = ast.getSubtree(0);
        AST right = ast.getSubtree(1);
        AST newAST = new PropositionalAST("(" + left + " " + LogicalOperatorFlyweight.getImplicationString() + " " + right + ") " +
                    LogicalOperatorFlyweight.getConjunctionString() + "(" + right + " " + LogicalOperatorFlyweight.getImplicationString() + " " + left + ")", true);
        KnowledgeBaseRegistry.addObtainedFrom(newAST.toString(), List.of(ast.toString()), "Material Equivalence");
        if (!containsEntry(newAST)) knowledgeBase.add(newAST);
        return true;
    }

    private boolean handleMaterialImplication(int index) {
        AST ast = knowledgeBase.get(index);

        if (PropositionalLogicHelper.getOutermostOperation(ast) != LogicalOperator.IMPLICATION) {
            ErrorHelper.add("Cannot apply Material Implication on " + ast + "!");
            return false;
        }

        AST antecedent = ast.getSubtree(0);
        AST conclusion = ast.getSubtree(1);
        antecedent.negate();
        AST newAST = new PropositionalAST(antecedent + " " + LogicalOperatorFlyweight.getDisjunctionString() + " " + conclusion, true);
        KnowledgeBaseRegistry.addObtainedFrom(newAST.toString(), List.of(ast.toString()), "Material Equivalence");
        if (!containsEntry(newAST)) knowledgeBase.add(newAST);
        return true;
    }

    private boolean handleImplicationIntroduction(int index1, int index2) {
        AST ast1 = knowledgeBase.get(index1);
        AST ast2 = knowledgeBase.get(index2);

        AST ast = new PropositionalAST(ast1 + " " + LogicalOperatorFlyweight.getImplicationString() + " " + ast2, true);
        KnowledgeBaseRegistry.addObtainedFrom(ast.toString(), List.of(ast1.toString(), ast2.toString()), "Implication Introduction");
        if (!containsEntry(ast)) knowledgeBase.add(ast);
        return true;
    }

    private boolean handleImplicationSimplification(int index) {
        AST ast = knowledgeBase.get(index);

        if (PropositionalLogicHelper.getOutermostOperation(ast) != LogicalOperator.IMPLICATION) {
            ErrorHelper.add("Cannot apply Implication Simplification on " + ast + "!");
            return false;
        }

        AST ast1 = ast.getSubtree(0);
        AST ast2 = ast.getSubtree(1);
        KnowledgeBaseRegistry.addObtainedFrom(ast1.toString(), List.of(ast.toString()), "Implication Simplification");
        KnowledgeBaseRegistry.addObtainedFrom(ast2.toString(), List.of(ast.toString()), "Implication Simplification");

        if (!containsEntry(ast1)) knowledgeBase.add(ast1);
        if (!containsEntry(ast2)) knowledgeBase.add(ast2);
        return true;
    }

    private boolean handleEquivalenceIntroduction(int index1, int index2) {
        AST ast1 = knowledgeBase.get(index1);
        AST ast2 = knowledgeBase.get(index2);

        if (PropositionalLogicHelper.getOutermostOperation(ast1) == LogicalOperator.IMPLICATION &&
            PropositionalLogicHelper.getOutermostOperation(ast2) == LogicalOperator.IMPLICATION) {
            AST antecedent1 = ast1.getSubtree(0);
            AST conclusion1 = ast1.getSubtree(1);

            AST antecedent2 = ast2.getSubtree(0);
            AST conclusion2 = ast2.getSubtree(1);

            if (antecedent1.isEquivalentTo(conclusion2) && antecedent2.isEquivalentTo(conclusion1)) {
                AST ast = new PropositionalAST(antecedent1 + " " + LogicalOperatorFlyweight.getEquivalenceString() + " " + conclusion1, true);
                KnowledgeBaseRegistry.addObtainedFrom(ast.toString(), List.of(ast1.toString(), ast2.toString()), "Equivalence Introduction");
                if (!containsEntry(ast)) knowledgeBase.add(ast);
                return true;
            }
        }
        ErrorHelper.add("Cannot apply Equivalence Simplification on " + ast1 + " and " + ast2 + "!");
        return false;
    }

    private boolean handleEquivalenceSimplification(int index) {
        AST ast = knowledgeBase.get(index);

        if (PropositionalLogicHelper.getOutermostOperation(ast) != LogicalOperator.EQUIVALENCE) {
            ErrorHelper.add("Cannot apply Equivalence Simplification on " + ast + "!");
            return false;
        }

        AST ast1 = ast.getSubtree(0);
        AST ast2 = ast.getSubtree(1);
        KnowledgeBaseRegistry.addObtainedFrom(ast1.toString(), List.of(ast.toString()), "Equivalence Simplification");
        KnowledgeBaseRegistry.addObtainedFrom(ast2.toString(), List.of(ast.toString()), "Equivalence Simplification");

        if (!containsEntry(ast1)) knowledgeBase.add(ast1);
        if (!containsEntry(ast2)) knowledgeBase.add(ast2);
        return true;
    }

    private boolean handleConjunctionIntroduction(int index1, int index2) {
        AST ast1 = knowledgeBase.get(index1);
        AST ast2 = knowledgeBase.get(index2);

        AST ast = new PropositionalAST(ast1 + " " + LogicalOperatorFlyweight.getConjunctionString() + " " + ast2, true);
        KnowledgeBaseRegistry.addObtainedFrom(ast.toString(), List.of(ast1.toString(), ast2.toString()), "Conjunction Introduction");
        if (!containsEntry(ast)) knowledgeBase.add(ast);
        return true;
    }

    private boolean handleDisjunctionElimination(int index1, int index2) {
        AST ast1 = knowledgeBase.get(index1);
        AST ast2 = knowledgeBase.get(index2);

        if (PropositionalLogicHelper.getOutermostOperation(ast1) == LogicalOperator.DISJUNCTION) {
            AST left = ast1.getSubtree(0);
            AST right = ast1.getSubtree(1);

            AST negatedLeft = new PropositionalAST(left.toString(), true);
            AST negatedRight = new PropositionalAST(right.toString(), true);
            negatedLeft.negate();
            negatedRight.negate();

            if (negatedLeft.isEquivalentTo(ast2)) {
                KnowledgeBaseRegistry.addObtainedFrom(right.toString(), List.of(ast1.toString(), ast2.toString()), "Disjunction Elimination");
                if (!containsEntry(right)) knowledgeBase.add(right);
                return true;
            }
            else if (negatedRight.isEquivalentTo(ast2)) {
                KnowledgeBaseRegistry.addObtainedFrom(left.toString(), List.of(ast1.toString(), ast2.toString()), "Disjunction Elimination");
                if (!containsEntry(left)) knowledgeBase.add(left);
                return true;
            }
        }
        else if (PropositionalLogicHelper.getOutermostOperation(ast2) == LogicalOperator.DISJUNCTION) {
            AST left = ast2.getSubtree(0);
            AST right = ast2.getSubtree(1);

            AST negatedLeft = new PropositionalAST(left.toString(), true);
            AST negatedRight = new PropositionalAST(right.toString(), true);
            negatedLeft.negate();
            negatedRight.negate();

            if (negatedLeft.isEquivalentTo(ast1)) {
                KnowledgeBaseRegistry.addObtainedFrom(right.toString(), List.of(ast1.toString(), ast2.toString()), "Disjunction Elimination");
                if (!containsEntry(right)) knowledgeBase.add(right);
                return true;
            }
            else if (negatedRight.isEquivalentTo(ast1)) {
                KnowledgeBaseRegistry.addObtainedFrom(left.toString(), List.of(ast1.toString(), ast2.toString()), "Disjunction Elimination");
                if (!containsEntry(left)) knowledgeBase.add(left);
                return true;
            }
        }

        ErrorHelper.add("Cannot apply Disjunction Elimination on " + ast1 + " and " + ast2 + "!");
        return false;
    }

    private boolean handleConjunctionElimination(int index) {
        AST ast = knowledgeBase.get(index);

        if (PropositionalLogicHelper.getOutermostOperation(ast) != LogicalOperator.CONJUNCTION) {
            ErrorHelper.add("Cannot apply Conjunction Elimination on " + ast + "!");
            return false;
        }

        AST ast1 = ast.getSubtree(0);
        AST ast2 = ast.getSubtree(1);
        KnowledgeBaseRegistry.addObtainedFrom(ast1.toString(), List.of(ast.toString()), "Conjunction Elimination");
        KnowledgeBaseRegistry.addObtainedFrom(ast2.toString(), List.of(ast.toString()), "Conjunction Elimination");

        if (!containsEntry(ast1)) knowledgeBase.add(ast1);
        if (!containsEntry(ast2)) knowledgeBase.add(ast2);
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

    private boolean handleDisjunctionSimplification(int index) {
        AST ast = knowledgeBase.get(index);

        if (PropositionalLogicHelper.getOutermostOperation(ast) == LogicalOperator.DISJUNCTION) {
            AST left = ast.getSubtree(0);
            AST right = ast.getSubtree(1);

            if (left.isEquivalentTo(right)) {
                KnowledgeBaseRegistry.addObtainedFrom(left.toString(), List.of(ast.toString()), "Disjunction Simplification");
                if (!containsEntry(left)) knowledgeBase.add(left);
                return true;
            }
        }

        ErrorHelper.add("Cannot apply Disjunction Simplification on " + ast + "!");
        return false;
    }

    private boolean handleDeMorgan(int index) {
        AST copyAst = knowledgeBase.get(index);
        AST ast = new PropositionalAST(copyAst.toString(), true);

        if (PropositionalLogicHelper.getOutermostOperation(ast) == LogicalOperator.NEGATION) {
            ast.negate();
            if (PropositionalLogicHelper.getOutermostOperation(ast) == LogicalOperator.CONJUNCTION) {
                AST left = ast.getSubtree(0);
                AST right = ast.getSubtree(1);
                left.negate();
                right.negate();

                AST newAST = new PropositionalAST(left + " " + LogicalOperatorFlyweight.getDisjunctionString() + " " + right, true);
                KnowledgeBaseRegistry.addObtainedFrom(newAST.toString(), List.of(ast.toString()), "DeMorgan");
                if (!containsEntry(newAST)) knowledgeBase.add(newAST);
                return true;
            }
            else if (PropositionalLogicHelper.getOutermostOperation(ast) == LogicalOperator.DISJUNCTION) {
                AST left = ast.getSubtree(0);
                AST right = ast.getSubtree(1);
                left.negate();
                right.negate();

                AST newAST = new PropositionalAST(left + " " + LogicalOperatorFlyweight.getConjunctionString() + " " + right, true);
                KnowledgeBaseRegistry.addObtainedFrom(newAST.toString(), List.of(ast.toString()), "DeMorgan");
                if (!containsEntry(newAST)) knowledgeBase.add(newAST);
                return true;
            }
            else if (PropositionalLogicHelper.getOutermostOperation(ast) == LogicalOperator.IMPLICATION) {
                AST left = ast.getSubtree(0);
                AST right = ast.getSubtree(1);
                right.negate();

                AST newAST = new PropositionalAST(left + " " + LogicalOperatorFlyweight.getConjunctionString() + " " + right, true);
                KnowledgeBaseRegistry.addObtainedFrom(newAST.toString(), List.of(ast.toString()), "DeMorgan");
                if (!containsEntry(newAST)) knowledgeBase.add(newAST);
                return true;
            }
        }

        ErrorHelper.add("Cannot apply DeMorgan on " + ast + "!");
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

    private void addKBError(Command command) {
        if (command.getArity() == 1) ErrorHelper.add("Command '" + command + "' requires the argument to be from the Knowledge Base!");
        else ErrorHelper.add("Command '" + command + "' requires the arguments to be from the Knowledge Base!");
    }

    private void addGoalError(Command command) {
        if (command.getArity() == 1) ErrorHelper.add("Command '" + command + "' requires the argument to be from the Goals!");
        else ErrorHelper.add("Command '" + command + "' requires the arguments to be from the Goals!");
    }

    private void addOutOfBoundsError(int index) {
        if (knowledgeBase.size() == 1) ErrorHelper.add("Knowledge Base has " + knowledgeBase.size() + " entry, index " + (index + 1) + " is out of bounds!");
        else ErrorHelper.add("Knowledge Base has " + knowledgeBase.size() + " entries, index " + (index + 1) + " is out of bounds!");
    }

    private void copyGoals(List<AST> newGoals, List<AST> oldGoals) {
        newGoals.addAll(oldGoals);
    }

    private void copyKB(List<AST> newKB, List<AST> oldKB) {
        newKB.addAll(oldKB);
    }

    private boolean containsEntry(AST entry) {
        for (AST ast : knowledgeBase) {
            if (ast.isEquivalentTo(entry)) return true;
        }
        return false;
    }

}
