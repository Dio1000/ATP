package me.dariansandru;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.util.SystemInfo;
import me.dariansandru.controller.LogicController;
import me.dariansandru.gui.GUIController;
import me.dariansandru.utils.helper.KnowledgeBaseRegistry;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {
//        if (SystemInfo.isMacOS) {
//            System.setProperty( "apple.laf.useScreenMenuBar", "true" );
//            System.setProperty( "apple.awt.application.name", "Automated Theorem Prover" );
//            System.setProperty( "apple.awt.application.appearance", "NSAppearanceNameDarkAqua" );
//        }
//
//        FlatDarkLaf.setup();
//        SwingUtilities.invokeLater(GUIController::new);

        LogicController logicController = new LogicController("files/input.txt");
        logicController.run();
    }
}

//TODO Implement tokenizer for functions.
//TODO Check notation and arity of predicates / functions in all universes of discourse besides Propositional Logic
//TODO More Exceptions
//TODO Change reflexivity package classes to actually use reflexivity.

//TODO Look into why Disjunction assumption and conclusion prints twice.