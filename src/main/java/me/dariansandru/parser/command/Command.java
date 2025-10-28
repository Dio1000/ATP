package me.dariansandru.parser.command;

import me.dariansandru.domain.data_structures.BiMap;
import me.dariansandru.utils.helper.PropositionalLogicHelper;

import java.util.ArrayList;
import java.util.List;

public enum Command {

    // -- PROPOSITIONAL LOGIC --

    // Strategies
    IMPLICATION_STRATEGY("implstr", 1, true),
    EQUIVALENCE_STRATEGY("eqstr", 1, true),
    CONJUNCTION_STRATEGY("constr", 1, true),
    DISJUNCTION_STRATEGY("disstr", 1, true),
    NEGATION_STRATEGY("negstr", 1, true),
    CONTRAPOSITIVE_STRATEGY("contrapos", 1, true),

    // Rules of Inference
    ABSORPTION("absor", 1, true),
    CONJUNCTION_INTRODUCTION("conintro", PropositionalLogicHelper.getNArityConstant(), false),
    CONJUNCTION_ELIMINATION("conelim", 1, true),
    CONSTRUCTIVE_DILEMMA("constrdil", 3, true),
    DESTRUCTIVE_DILEMMA("destrdil", 3, true),
    DISJUNCTION_INTRODUCTION("disintro", PropositionalLogicHelper.getNArityConstant(), false),
    DISJUNCTION_ELIMINATION("diselim", 2, true),
    DISJUNCTIVE_SYLLOGISM("dissyll", 2, true),
    EQUIVALENCE_INTRODUCTION("eqintro", 2, true),
    EQUIVALENCE_SIMPLIFICATION("eqelim", 1, true),
    HYPOTHETICAL_SYLLOGISM("hypsyll", 2, true),
    IMPLICATION_INTRODUCTION("implintro", 2, true),
    IMPLICATION_ELIMINATION("implsimpl", 1, true),
    MODUS_PONENS("modpon", 2, true),
    MODUS_TOLLENS("modtol", 2, true),
    PROOF_BY_CASES("cases", 1, true),

    // Rules of Replacement
    DISJUNCTION_SIMPLIFICATION("dissimpl", 1, true),
    MATERIAL_EQUIVALENCE("mateq", 1, true),
    MATERIAL_IMPLICATION("matimpl", 1, true),
    DEMORGAN("demorgan", 1, true),
    TRANSPOSITION("trans", 1, true),

    // Other
    DONE("done", 0, true),
    ERROR("error", -1, true),
    CONTRADICTION("contr", 2, false),
    CHANGE_STATE("chstate", 1, true);

    private final String commandString;
    private final int arity;
    private final boolean fixed;

    private static final List<Command> allCommands = new ArrayList<>();
    private static final List<String> allCommandStrings = new ArrayList<>();
    private static final BiMap stringCommandBiMap = new BiMap();

    Command(String commandString, int arity, boolean fixed) {
        this.commandString = commandString;
        this.arity = arity;
        this.fixed = fixed;
    }

    static {
        allCommands.addAll(List.of(Command.values()));
        allCommandStrings.add(List.of(Command.values()).toString());

        for (Command command : Command.values()) {
            stringCommandBiMap.put(command.commandString, command);
        }
    }

    @Override
    public String toString() {
        return commandString;
    }

    public int getArity() {
        return arity;
    }

    public boolean isFixed() {
        return fixed;
    }

    public static List<Command> getAllCommands() {
        return allCommands;
    }

    public static List<String> getAllCommandStrings() {
        return allCommandStrings;
    }

    public static boolean contains(String commandString) {
        return allCommandStrings.getFirst().contains(commandString);
    }

    public static Command getFromString(String commandString) {
        Command result = (Command) stringCommandBiMap.get(commandString);
        return result != null ? result : ERROR;
    }
}
