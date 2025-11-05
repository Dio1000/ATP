package me.dariansandru;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.util.SystemInfo;
import me.dariansandru.controller.LogicController;
import me.dariansandru.domain.data_structures.ast.AST;
import me.dariansandru.domain.data_structures.ast.PropositionalAST;
import me.dariansandru.domain.language.interpretation.PropositionalPartialInterpretation;
import me.dariansandru.domain.proof.inference_rules.CustomPropositionalInferenceRule;
import me.dariansandru.gui.GUIController;
import me.dariansandru.utils.helper.PropositionalLogicHelper;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;
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
            
        }
        else {
            throw new IllegalStateException("Argument: " + args[0] + " could not be found!");
        }
    }
}

// -- GENERAL --
//TODO Look into how collections are kept (inference rules, universes, enums in general).
//TODO Create more error messages for AST parsing.
//TODO Maybe there will be the need for different KBRegistry handling because of stronger equivalency?
//TODO Look into custom inference rules, refactor to add all derived formulas in one cycle.

// -- BUGS --

// -- NEW FEATURES --
//TODO Create a new way to output proofs because some lines do not get printed in Automated Proofs.
//TODO Add more inference rules for Automated Proving (from Manual Proving) and fix old ones. (Look at Natural Deduction to find missing rules)
//TODO Add creation of custom propositional inference rules packages.