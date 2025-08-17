package me.dariansandru.controller;

import me.dariansandru.domain.UniverseOfDiscourse;
import me.dariansandru.domain.signature.Signature;
import me.dariansandru.domain.signature.SignatureFactory;
import me.dariansandru.io.InputDevice;
import me.dariansandru.parser.Parser;
import me.dariansandru.parser.parsers.FormulaParser;
import me.dariansandru.parser.parsers.ParserFactory;
import me.dariansandru.reflexivity.PropositionalInferenceRules;
import me.dariansandru.utils.helper.ErrorHelper;
import me.dariansandru.utils.helper.WarningHelper;

import java.util.List;

public class LogicController {

    private final String inputFile;

    public LogicController(String inputFile) {
        this.inputFile = inputFile;
    }

    public void run() {
        List<String> lines = InputDevice.read(inputFile);
        if (!Parser.parseValidInput(lines)) return;
        UniverseOfDiscourse universeOfDiscourse = Parser.getUniverseOfDiscourse(lines);
        Signature signature = SignatureFactory.createSignature(universeOfDiscourse);
        FormulaParser parser = ParserFactory.createParser(signature);

        List<String> knowledgeBase = Parser.getKBLines(lines);
        List<String> goals = Parser.getGoalsLines(lines);

        boolean validSyntax = parser.parse(knowledgeBase) && parser.parse(goals);
        if (validSyntax) {
            System.out.println("Syntax validation complete!");
            WarningHelper.print();
        }
        else {
            ErrorHelper.print();
            return;
        }


    }
}
