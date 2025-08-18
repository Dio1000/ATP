package me.dariansandru.controller;

import me.dariansandru.domain.UniverseOfDiscourse;
import me.dariansandru.domain.proof.Proof;
import me.dariansandru.domain.proof.inference_rules.propositional.ModusPonens;
import me.dariansandru.domain.proof.inference_rules.propositional.ModusTollens;
import me.dariansandru.domain.signature.Signature;
import me.dariansandru.domain.signature.SignatureFactory;
import me.dariansandru.io.InputDevice;
import me.dariansandru.io.OutputDevice;
import me.dariansandru.parser.Parser;
import me.dariansandru.parser.parsers.FormulaParser;
import me.dariansandru.parser.parsers.ParserFactory;
import me.dariansandru.parser.parsers.PropositionalParser;
import me.dariansandru.utils.data_structures.ast.AST;
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
        if (!validSyntax) {
            ErrorHelper.print();
            return;
        }
        WarningHelper.print();
        OutputDevice.writeToConsole("Syntax validated successfully!");

        List<AST> knowledgeBaseAST = ((PropositionalParser) parser).parseAndGetASTs(knowledgeBase);
        List<AST> goalsAST = ((PropositionalParser) parser).parseAndGetASTs(goals);

        ModusTollens modusPonens = new ModusTollens();
        modusPonens.canInference(knowledgeBaseAST.get(0), knowledgeBaseAST.get(1));
        System.out.println("HERE " + modusPonens.inference(knowledgeBaseAST.get(0), knowledgeBaseAST.get(1)));

        Proof proof = new Proof(signature, knowledgeBaseAST, goalsAST);
        proof.prove();
    }
}
