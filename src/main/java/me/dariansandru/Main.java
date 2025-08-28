package me.dariansandru;

import me.dariansandru.controller.LogicController;

public class Main {
    public static void main(String[] args) {
        String inputText = "files/input.txt";
        LogicController logicController = new LogicController(inputText);
        logicController.run();
    }
}

//TODO Implement tokenizer for functions.
//TODO Check notation and arity of predicates / functions in all universes of discourse besides Propositional Logic
//TODO More Exceptions
//TODO Change reflexivity package classes to actually use reflexivity.