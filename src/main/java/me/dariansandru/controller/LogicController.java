package me.dariansandru.controller;

import me.dariansandru.domain.UniverseOfDiscourse;
import me.dariansandru.domain.proof.proofs.PropositionalProof;
import me.dariansandru.domain.signature.Signature;
import me.dariansandru.domain.signature.SignatureFactory;
import me.dariansandru.io.InputDevice;
import me.dariansandru.io.OutputDevice;
import me.dariansandru.parser.Parser;
import me.dariansandru.parser.parsers.FormulaParser;
import me.dariansandru.parser.parsers.ParserFactory;
import me.dariansandru.parser.parsers.PropositionalParser;
import me.dariansandru.domain.data_structures.ast.AST;
import me.dariansandru.utils.helper.ErrorHelper;
import me.dariansandru.utils.helper.ProofTextHelper;
import me.dariansandru.utils.helper.WarningHelper;

import java.util.List;

public class LogicController {

    private final String inputFile;

    public LogicController(String inputFile) {
        this.inputFile = inputFile;
    }

    public void run() {
        ProofTextHelper.clear();

        List<String> lines = InputDevice.read(inputFile);
        if (!Parser.parseValidInput(lines)) return;

        UniverseOfDiscourse universeOfDiscourse = Parser.getUniverseOfDiscourse(lines);
        Signature signature = SignatureFactory.createSignature(universeOfDiscourse);
        FormulaParser parser = ParserFactory.createParser(signature);

        List<String> knowledgeBase = Parser.getKBLines(lines);
        List<String> goals = Parser.getGoalsLines(lines);

        List<AST> knowledgeBaseAST = ((PropositionalParser) parser).parseAndGetASTs(knowledgeBase);
        List<AST> goalsAST = ((PropositionalParser) parser).parseAndGetASTs(goals);

        if (WarningHelper.notEmpty()) WarningHelper.print();
        if (ErrorHelper.notEmpty()) {
            OutputDevice.writeToConsole("Could not validate syntax!");
            ErrorHelper.print();
            return;
        }
        OutputDevice.writeToConsole("Syntax validated!");

        PropositionalProof proof = new PropositionalProof(signature, knowledgeBaseAST, goalsAST);
        proof.prove();
    }
}
