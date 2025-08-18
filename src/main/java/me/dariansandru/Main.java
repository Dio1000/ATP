package me.dariansandru;

import me.dariansandru.controller.LogicController;

public class Main {
    public static void main(String[] args) {
        String inputText = "files/input.txt";
        LogicController logicController = new LogicController(inputText);
        logicController.run();
    }
}

//TODO? Refactor Signatures such that they use reflexivity.
//TODO? Refactor Proof to be an interface.
//TODO Implement tokenizer for functions.
//TODO Check notation and arity of predicates / functions in all universes of discourse besides Propositional Logic
//TODO if the AST parsing throws an error catch it and invalidate the formula.
//TODO Change reflexivity package classes to actually use reflexivity.
//TODO lines are parsed twice, change this (in LogicController)