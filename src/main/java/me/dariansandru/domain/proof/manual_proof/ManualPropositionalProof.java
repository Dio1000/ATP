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

import java.util.ArrayList;
import java.util.List;

public class ManualPropositionalProof {

    private final List<AST> knowledgeBase;
    private final List<AST> goals;
    private boolean isProven = false;

    private final String kbName = "KB";
    private final String goalName = "G";

    private final List<String> assumptions = new ArrayList<>();
    private final List<String> conclusions = new ArrayList<>();
    private List<String> arguments = new ArrayList<>();

    public ManualPropositionalProof(List<AST> knowledgeBase, List<AST> goals) {
        this.knowledgeBase = knowledgeBase;
        this.goals = goals;
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
        }
        OutputDevice.writeToConsole("Proof completed!");
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
                String argument1 = arguments.getFirst();
                String type1 = getArgumentType(argument1);
                int index1 = getArgumentIndex(argument1);

                String argument2 = arguments.get(1);
                String type2 = getArgumentType(argument2);
                int index2 = getArgumentIndex(argument2);

                if (!type1.equals(kbName) || !type2.equals(kbName)) {
                    addKBError(command);
                }

                return handleImplicationIntroduction(index1, index2);
            }
            case IMPLICATION_ELIMINATION -> {
                String argument = arguments.getFirst();
                String type = getArgumentType(argument);
                int index = getArgumentIndex(argument);

                if (!type.equals(kbName)) {
                    addKBError(command);
                }

                return handleImplicationSimplification(index);
            }
            case EQUIVALENCE_INTRODUCTION -> {
                String argument1 = arguments.getFirst();
                String type1 = getArgumentType(argument1);
                int index1 = getArgumentIndex(argument1);

                String argument2 = arguments.get(1);
                String type2 = getArgumentType(argument2);
                int index2 = getArgumentIndex(argument2);

                if (!type1.equals(kbName) || !type2.equals(kbName)) {
                    addKBError(command);
                }

                return handleEquivalenceIntroduction(index1, index2);
            }
            case EQUIVALENCE_ELIMINATION -> {
                String argument = arguments.getFirst();
                String type = getArgumentType(argument);
                int index = getArgumentIndex(argument);

                if (!type.equals(kbName)) {
                    addKBError(command);
                }

                return handleEquivalenceSimplification(index);
            }
            case CONJUNCTION_INTRODUCTION -> {
                String argument1 = arguments.getFirst();
                String type1 = getArgumentType(argument1);
                int index1 = getArgumentIndex(argument1);

                String argument2 = arguments.get(1);
                String type2 = getArgumentType(argument2);
                int index2 = getArgumentIndex(argument2);

                if (!type1.equals(kbName) || !type2.equals(kbName)) {
                    addKBError(command);
                }

                return handleConjunctionIntroduction(index1, index2);
            }
            case CONJUNCTION_ELIMINATION -> {
                String argument = arguments.getFirst();
                String type = getArgumentType(argument);
                int index = getArgumentIndex(argument);

                if (!type.equals(kbName)) {
                    addKBError(command);
                }

                return handleConjunctionElimination(index);
            }
            case DISJUNCTION_INTRODUCTION -> {

            }
            case DISJUNCTION_ELIMINATION -> {

            }
            case DEMORGAN -> {

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

    private boolean handleImplicationStrategy(int index) {
        AST ast = goals.get(index);
        if (PropositionalLogicHelper.getOutermostOperation(ast) != LogicalOperator.IMPLICATION) {
            ErrorHelper.add("Cannot apply command on " + ast +
                    ". Outermost logical operator is not '" + LogicalOperatorFlyweight.getImplicationString() + "'!");
            return false;
        }

        AST newAST1 = ast.getSubtree(0);
        AST newAST2 = ast.getSubtree(1);
        knowledgeBase.add(newAST1);

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

        AST newAST1 = ast.getSubtree(0);
        AST newAST2 = ast.getSubtree(1);
        goals.add(newAST1);
        goals.add(newAST2);
        goals.remove(ast);

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
        goals.add(newAST1);
        goals.add(newAST2);
        goals.remove(ast);

        KnowledgeBaseRegistry.addObtainedFrom(newAST1.toString(), List.of(ast.toString()), "Conjunction Strategy");
        KnowledgeBaseRegistry.addObtainedFrom(newAST2.toString(), List.of(ast.toString()), "Conjunction Strategy");
        return true;
    }

    private boolean handleDisjunctionStrategy(int index) {
        return false;
    }

    private boolean handleNegationStrategy(int index) {
        AST ast = goals.get(index);
        ast.negate();
        AST contradictionAST = new PropositionalAST(true);
        knowledgeBase.add(ast);
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

    private boolean handleModusPonens(int index1, int index2) {
        AST ast1 = knowledgeBase.get(index1);
        AST ast2 = knowledgeBase.get(index2);

        if (PropositionalLogicHelper.getOutermostOperation(ast1) == LogicalOperator.IMPLICATION) {
            AST antecedent1 = ast1.getSubtree(0);
            AST conclusion1 = ast1.getSubtree(1);

            if (antecedent1.isEquivalentTo(ast2)) {
                KnowledgeBaseRegistry.addObtainedFrom(conclusion1.toString(), List.of(antecedent1.toString(), ast1.toString()), "Modus Ponens");
                knowledgeBase.add(conclusion1);
                return true;
            }
        }
        else if (PropositionalLogicHelper.getOutermostOperation(ast2) == LogicalOperator.IMPLICATION) {
            AST antecedent2 = ast2.getSubtree(0);
            AST conclusion2 = ast2.getSubtree(1);

            if (antecedent2.isEquivalentTo(ast1)) {
                KnowledgeBaseRegistry.addObtainedFrom(conclusion2.toString(), List.of(antecedent2.toString(), ast2.toString()), "Modus Ponens");
                knowledgeBase.add(conclusion2);
                return true;
            }
        }

        ErrorHelper.add("Cannot apply Modus Ponens on " + ast1 + " and " + ast2 + "!");
        return false;
    }

    private boolean handleModusTollens(int index1, int index2) {
        AST ast1 = knowledgeBase.get(index1);
        AST ast2 = knowledgeBase.get(index2);

        if (PropositionalLogicHelper.getOutermostOperation(ast1) == LogicalOperator.IMPLICATION) {
            AST antecedent1 = ast1.getSubtree(0);
            AST conclusion1 = ast1.getSubtree(1);
            conclusion1.negate();

            if (conclusion1.isEquivalentTo(ast2)) {
                antecedent1.negate();
                KnowledgeBaseRegistry.addObtainedFrom(antecedent1.toString(), List.of(conclusion1.toString(), ast1.toString()), "Modus Tollens");
                knowledgeBase.add(antecedent1);
                return true;
            }
        }
        else if (PropositionalLogicHelper.getOutermostOperation(ast2) == LogicalOperator.IMPLICATION) {
            AST antecedent2 = ast2.getSubtree(0);
            AST conclusion2 = ast2.getSubtree(1);
            conclusion2.negate();

            if (conclusion2.isEquivalentTo(ast1)) {
                antecedent2.negate();
                KnowledgeBaseRegistry.addObtainedFrom(antecedent2.toString(), List.of(conclusion2.toString(), ast2.toString()), "Modus Tollens");
                knowledgeBase.add(antecedent2);
                return true;
            }
        }

        ErrorHelper.add("Cannot apply Modus Tollens on " + ast1 + " and " + ast2 + "!");
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
            knowledgeBase.add(newAST);
            return true;
        }
        else if (conclusion2.isEquivalentTo(antecedent1)) {
            AST newAST = new PropositionalAST(antecedent2.toString() + " " + LogicalOperatorFlyweight.getImplicationString() + " " + conclusion1.toString(), true);
            KnowledgeBaseRegistry.addObtainedFrom(newAST.toString(), List.of(ast1.toString(), ast2.toString()), "Hypothetical Syllogism");
            knowledgeBase.add(newAST);
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
            AST left = ast1.getSubtree(0);
            AST leftCopy = new PropositionalAST(left.toString(), true);

            AST right = ast1.getSubtree(1);
            AST rightCopy = new PropositionalAST(right.toString(), true);

            left.negate();
            right.negate();

            if (left.isEquivalentTo(ast2)) {
                KnowledgeBaseRegistry.addObtainedFrom(rightCopy.toString(), List.of(ast1.toString(), ast2.toString()), "Disjunctive Syllogism");
                knowledgeBase.add(rightCopy);
                return true;
            }
            else if (right.isEquivalentTo(ast2)) {
                KnowledgeBaseRegistry.addObtainedFrom(leftCopy.toString(), List.of(ast1.toString(), ast2.toString()), "Disjunctive Syllogism");
                knowledgeBase.add(leftCopy);
                return true;
            }
        }
        else if (PropositionalLogicHelper.getOutermostOperation(ast2) == LogicalOperator.DISJUNCTION) {
            AST left = ast2.getSubtree(0);
            AST leftCopy = new PropositionalAST(left.toString(), true);

            AST right = ast2.getSubtree(1);
            AST rightCopy = new PropositionalAST(right.toString(), true);

            left.negate();
            right.negate();

            if (left.isEquivalentTo(ast1)) {
                KnowledgeBaseRegistry.addObtainedFrom(rightCopy.toString(), List.of(ast1.toString(), ast2.toString()), "Disjunctive Syllogism");
                knowledgeBase.add(rightCopy);
                return true;
            }
            else if (right.isEquivalentTo(ast1)) {
                KnowledgeBaseRegistry.addObtainedFrom(leftCopy.toString(), List.of(ast1.toString(), ast2.toString()), "Disjunctive Syllogism");
                knowledgeBase.add(leftCopy);
                return true;
            }
        }

        ErrorHelper.add("Cannot apply Disjunctive Syllogism on " + ast1 + " and " + ast2 + "!");
        return false;
    }

    private boolean handleConstructiveDilemma(int index1, int index2, int index3) {
        AST ast1 = knowledgeBase.get(index1);
        AST ast2 = knowledgeBase.get(index2);
        AST ast3 = knowledgeBase.get(index3);

        if (PropositionalLogicHelper.getOutermostOperation(ast1) == LogicalOperator.IMPLICATION &&
            PropositionalLogicHelper.getOutermostOperation(ast2) == LogicalOperator.IMPLICATION &&
            PropositionalLogicHelper.getOutermostOperation(ast3) == LogicalOperator.DISJUNCTION) {
            AST antecedent1 = ast1.getSubtree(0);
            AST conclusion1 = ast1.getSubtree(1);
            AST antecedent2 = ast2.getSubtree(0);
            AST conclusion2 = ast2.getSubtree(1);
            AST left = ast3.getSubtree(0);
            AST right = ast3.getSubtree(1);

            if (antecedent1.isEquivalentTo(left) && antecedent2.isEquivalentTo(right) ||
                    antecedent1.isEquivalentTo(right) && antecedent2.isEquivalentTo(left)) {
                AST newAST = new PropositionalAST(conclusion1.toString() + " " + LogicalOperatorFlyweight.getDisjunctionString() + " " + conclusion2.toString(), true);
                KnowledgeBaseRegistry.addObtainedFrom(newAST.toString(), List.of(ast1.toString(), ast2.toString(), ast3.toString()), "Constructive Dilemma");
                knowledgeBase.add(newAST);
                return true;
            }
        }
        else if (PropositionalLogicHelper.getOutermostOperation(ast1) == LogicalOperator.IMPLICATION &&
                PropositionalLogicHelper.getOutermostOperation(ast2) == LogicalOperator.DISJUNCTION &&
                PropositionalLogicHelper.getOutermostOperation(ast3) == LogicalOperator.IMPLICATION) {
            AST antecedent1 = ast1.getSubtree(0);
            AST conclusion1 = ast1.getSubtree(1);
            AST antecedent2 = ast3.getSubtree(0);
            AST conclusion2 = ast3.getSubtree(1);
            AST left = ast2.getSubtree(0);
            AST right = ast2.getSubtree(1);

            if (antecedent1.isEquivalentTo(left) && antecedent2.isEquivalentTo(right) ||
                    antecedent1.isEquivalentTo(right) && antecedent2.isEquivalentTo(left)) {
                AST newAST = new PropositionalAST(conclusion1.toString() + " " + LogicalOperatorFlyweight.getDisjunctionString() + " " + conclusion2.toString(), true);
                KnowledgeBaseRegistry.addObtainedFrom(newAST.toString(), List.of(ast1.toString(), ast2.toString(), ast3.toString()), "Constructive Dilemma");
                knowledgeBase.add(newAST);
                return true;
            }
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
                knowledgeBase.add(newAST);
                return true;
            }
        }
        ErrorHelper.add("Cannot apply Constructive Dilemma on " + ast1 + ", " + ast2 + " and " + ast3 + "!");
        return false;
    }

    private boolean handleDestructiveDilemma(int index1, int index2, int index3) {
        AST ast1 = knowledgeBase.get(index1);
        AST ast2 = knowledgeBase.get(index2);
        AST ast3 = knowledgeBase.get(index3);

        if (PropositionalLogicHelper.getOutermostOperation(ast1) == LogicalOperator.IMPLICATION &&
                PropositionalLogicHelper.getOutermostOperation(ast2) == LogicalOperator.IMPLICATION &&
                PropositionalLogicHelper.getOutermostOperation(ast3) == LogicalOperator.DISJUNCTION) {
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

                AST newAST = new PropositionalAST(antecedent1.toString() + " " + LogicalOperatorFlyweight.getDisjunctionString() + " " + antecedent2.toString(), true);
                KnowledgeBaseRegistry.addObtainedFrom(newAST.toString(), List.of(ast1.toString(), ast2.toString(), ast3.toString()), "Destructive Dilemma");
                knowledgeBase.add(newAST);
                return true;
            }
        }
        else if (PropositionalLogicHelper.getOutermostOperation(ast1) == LogicalOperator.IMPLICATION &&
                PropositionalLogicHelper.getOutermostOperation(ast2) == LogicalOperator.DISJUNCTION &&
                PropositionalLogicHelper.getOutermostOperation(ast3) == LogicalOperator.IMPLICATION) {
            AST antecedent1 = ast1.getSubtree(0);
            AST conclusion1 = ast1.getSubtree(1);
            conclusion1.negate();

            AST antecedent2 = ast3.getSubtree(0);
            AST conclusion2 = ast3.getSubtree(1);
            conclusion2.negate();

            AST left = ast2.getSubtree(0);
            AST right = ast2.getSubtree(1);

            if (conclusion1.isEquivalentTo(left) && conclusion2.isEquivalentTo(right) ||
                    conclusion1.isEquivalentTo(right) && conclusion2.isEquivalentTo(left)) {
                antecedent1.negate();
                antecedent2.negate();

                AST newAST = new PropositionalAST(antecedent1.toString() + " " + LogicalOperatorFlyweight.getDisjunctionString() + " " + antecedent2.toString(), true);
                KnowledgeBaseRegistry.addObtainedFrom(newAST.toString(), List.of(ast1.toString(), ast2.toString(), ast3.toString()), "Destructive Dilemma");
                knowledgeBase.add(newAST);
                return true;
            }
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

                AST newAST = new PropositionalAST(antecedent1.toString() + " " + LogicalOperatorFlyweight.getDisjunctionString() + " " + antecedent2.toString(), true);
                KnowledgeBaseRegistry.addObtainedFrom(newAST.toString(), List.of(ast1.toString(), ast2.toString(), ast3.toString()), "Destructive Dilemma");
                knowledgeBase.add(newAST);
                return true;
            }
        }
        ErrorHelper.add("Cannot apply Destructive Dilemma on " + ast1 + ", " + ast2 + " and " + ast3 + "!");
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
        knowledgeBase.add(newAST);
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
        knowledgeBase.add(newAST);
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
        knowledgeBase.add(newAST);
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
        knowledgeBase.add(newAST);
        return true;
    }

    private boolean handleImplicationIntroduction(int index1, int index2) {
        AST ast1 = knowledgeBase.get(index1);
        AST ast2 = knowledgeBase.get(index2);

        AST ast = new PropositionalAST(ast1 + " " + LogicalOperatorFlyweight.getImplicationString() + " " + ast2, true);
        KnowledgeBaseRegistry.addObtainedFrom(ast.toString(), List.of(ast1.toString(), ast2.toString()), "Implication Introduction");
        knowledgeBase.add(ast);
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

        knowledgeBase.add(ast1);
        knowledgeBase.add(ast2);
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
                knowledgeBase.add(ast);
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

        knowledgeBase.add(ast1);
        knowledgeBase.add(ast2);
        return true;
    }

    private boolean handleConjunctionIntroduction(int index1, int index2) {
        AST ast1 = knowledgeBase.get(index1);
        AST ast2 = knowledgeBase.get(index2);

        AST ast = new PropositionalAST(ast1 + " " + LogicalOperatorFlyweight.getConjunctionString() + " " + ast2, true);
        KnowledgeBaseRegistry.addObtainedFrom(ast.toString(), List.of(ast1.toString(), ast2.toString()), "Conjunction Introduction");
        knowledgeBase.add(ast);
        return true;
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

        knowledgeBase.add(ast1);
        knowledgeBase.add(ast2);
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
        OutputDevice.writeNumberedToConsole(knowledgeBase, 1, kbName);
        OutputDevice.writeNumberedToConsole(goals, 1, goalName);
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

}
