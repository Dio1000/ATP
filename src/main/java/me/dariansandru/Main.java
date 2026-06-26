package me.dariansandru;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.util.SystemInfo;
import me.dariansandru.controller.LogicController;
import me.dariansandru.gui.PropositionalProofGUIController;
import me.dariansandru.test.TestPipeline;
import me.dariansandru.utils.global.GlobalFlags;

import javax.swing.*;

public class Main {

    static final String guiString = "gui";
    static final String automatedString = "automated";
    static final String manualString = "manual";
    static final String testString = "test";
    static final String inputFile = "files/input.txt";

    public static void main(String[] args) {

        if (args.length == 0) throw new IllegalStateException("No arguments were provided!");

        GlobalFlags.getFlags(args);
        final String argument = args[0];
        GlobalFlags.executionFlag = argument;

        switch (argument) {
            case guiString -> {
                if (SystemInfo.isMacOS) {
                    System.setProperty("apple.laf.useScreenMenuBar", "true");
                    System.setProperty("apple.awt.application.name", "Automated Theorem Prover");
                    System.setProperty("apple.awt.application.appearance", "NSAppearanceNameDarkAqua");
                }

                FlatDarkLaf.setup();
                SwingUtilities.invokeLater(PropositionalProofGUIController::new);
            }
            case automatedString -> {
                LogicController logicController = new LogicController(inputFile);
                logicController.automatedRun();
            }
            case manualString -> {
                LogicController logicController = new LogicController(inputFile);
                logicController.manualRun();
            }
            case testString -> TestPipeline.test();
            case null, default -> throw new IllegalStateException("Argument: " + args[0] + " could not be found!");
        }
    }
}