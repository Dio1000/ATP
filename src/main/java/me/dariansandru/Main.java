package me.dariansandru;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.util.SystemInfo;
import me.dariansandru.controller.LogicController;
import me.dariansandru.gui.GUIController;
import me.dariansandru.utils.manual.PropositionalLogicManual;

import javax.swing.*;
import java.util.Objects;

public class Main {

    public static void main(String[] args) {
        String guiString = "gui";
        String automatedString = "automated";
        String manualString = "manual";
        String testString = "test";
        String inputFile = "files/input.txt";

        if (args.length == 0) throw new IllegalStateException("No arguments were provided!");
        else if (args.length == 2) throw new IllegalStateException("Too many arguments were provided!");
        else if (Objects.equals(args[0], guiString)) {
            if (SystemInfo.isMacOS) {
                System.setProperty( "apple.laf.useScreenMenuBar", "true" );
                System.setProperty( "apple.awt.application.name", "Automated Theorem Prover" );
                System.setProperty( "apple.awt.application.appearance", "NSAppearanceNameDarkAqua" );
            }

            FlatDarkLaf.setup();
            SwingUtilities.invokeLater(GUIController::new);
        }
        else if (Objects.equals(args[0], automatedString)) {
            LogicController logicController = new LogicController(inputFile);
            logicController.automatedRun();
        }
        else if (Objects.equals(args[0], manualString)) {
            LogicController logicController = new LogicController(inputFile);
            logicController.manualRun();
        }
        else if (Objects.equals(args[0], testString)) {
            PropositionalLogicManual manual = new PropositionalLogicManual();
            System.out.println(manual.getEntries().get(1).inference());
        }
        else {
            throw new IllegalStateException("Argument: " + args[0] + " could not be found!");
        }
    }
}

// -- GENERAL --
//TODO Implement tokenizer for functions.
//TODO Check notation and arity of predicates / functions in all universes of discourse besides Propositional Logic.
//TODO Change reflexivity package classes to actually use reflexivity.
//TODO Look into how collections are kept (inference rules, universes, enums in general).
//TODO Change arity of Conjunction introduction

// -- BUGS --
//TODO Look into why Disjunction assumption and conclusion prints twice.
//TODO Fix Proven / Not Proven text for States in Interactive Proving.

// -- NEW FEATURES --
//TODO Create a new way to output proofs.
//TODO Add more inference rules for Automated Proving (from Manual Proving) and fix old ones.