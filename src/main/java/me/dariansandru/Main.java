package me.dariansandru;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.util.SystemInfo;
import me.dariansandru.controller.LogicController;
import me.dariansandru.domain.data_structures.ast.AST;
import me.dariansandru.domain.data_structures.ast.PropositionalAST;
import me.dariansandru.domain.proof.inference_rules.InferenceRule;
import me.dariansandru.domain.proof.inference_rules.helper.CustomInferenceRuleHelper;
import me.dariansandru.domain.proof.manual_proof.helper.ManualPropositionalInferenceRuleHelper;
import me.dariansandru.gui.GUIController;
import me.dariansandru.gui.PropositionalProofGUIController;
import me.dariansandru.utils.global.GlobalAtomID;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Main {

    static final String guiString = "gui";
    static final String automatedString = "automated";
    static final String manualString = "manual";
    static final String testString = "test";
    static final String inputFile = "files/input.txt";

    public static void main(String[] args) {

        if (args.length == 0) throw new IllegalStateException("No arguments were provided!");
        final String argument = args[0];

        if (args.length == 2) throw new IllegalStateException("Too many arguments were provided!");
        else if (Objects.equals(argument, guiString)) {
            if (SystemInfo.isMacOS) {
                System.setProperty( "apple.laf.useScreenMenuBar", "true" );
                System.setProperty( "apple.awt.application.name", "Automated Theorem Prover" );
                System.setProperty( "apple.awt.application.appearance", "NSAppearanceNameDarkAqua" );
            }

            FlatDarkLaf.setup();
            SwingUtilities.invokeLater(PropositionalProofGUIController::new);
        }
        else if (Objects.equals(argument, automatedString)) {
            LogicController logicController = new LogicController(inputFile);
            logicController.automatedRun();
        }
        else if (Objects.equals(argument, manualString)) {
            LogicController logicController = new LogicController(inputFile);
            logicController.manualRun();
        }
        else if (Objects.equals(argument, testString)) {
            PropositionalAST ast1 = new PropositionalAST("A -> B", true);
            PropositionalAST ast2 = new PropositionalAST("A", true);
            List<AST> asts = new ArrayList<>();
            asts.add(ast1);
            asts.add(ast2);
            GlobalAtomID.addAtomId("A");
            GlobalAtomID.addAtomId("B");

            ManualPropositionalInferenceRuleHelper helper = new ManualPropositionalInferenceRuleHelper(new ArrayList<AST>());
            List<InferenceRule> inferenceRules = helper.applicableRules(asts);
            for (InferenceRule inferenceRule : inferenceRules) System.out.println(inferenceRule.name());
        }
        else {
            throw new IllegalStateException("Argument: " + args[0] + " could not be found!");
        }
    }
}

// -- GENERAL --

// -- BUGS --
//TODO Implication Strategy does not work in GUI.
//TODO Make GUI look more beautiful.

// -- NEW FEATURES --
//TODO Create a new way to output proofs because some lines do not get printed in Automated Proofs.
//TODO Add more inference rules for Automated Proving (from Manual Proving) and fix old ones. (Look at Natural Deduction to find missing rules)
//TODO Add creation of custom propositional inference rules packages.