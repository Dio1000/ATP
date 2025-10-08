package me.dariansandru.controller;

import me.dariansandru.domain.UniverseOfDiscourse;
import me.dariansandru.domain.data_structures.ast.PropositionalAST;
import me.dariansandru.domain.proof.manual_proof.ManualPropositionalProof;
import me.dariansandru.domain.proof.manual_proof.ManualPropositionalProofStates;
import me.dariansandru.domain.proof.proofs.PropositionalProof;
import me.dariansandru.domain.signature.Signature;
import me.dariansandru.domain.signature.SignatureFactory;
import me.dariansandru.io.InputDevice;
import me.dariansandru.io.OutputDevice;
import me.dariansandru.parser.Parser;
import me.dariansandru.parser.parsers.FormulaParser;
import me.dariansandru.utils.factory.ParserFactory;
import me.dariansandru.domain.data_structures.ast.AST;
import me.dariansandru.utils.helper.ErrorHelper;
import me.dariansandru.utils.helper.ProofTextHelper;
import me.dariansandru.utils.helper.WarningHelper;

import java.util.ArrayList;
import java.util.List;

public class LogicController {

    private final Signature signature;
    private final boolean valid;

    private List<AST> knowledgeBaseAST = new ArrayList<>();
    private List<AST> goalsAST = new ArrayList<>();

    public LogicController(String inputFile) {
        List<String> lines = InputDevice.read(inputFile);
        valid = Parser.parseValidInput(lines);
        if (!valid) {
            this.signature = null;
            return;
        }

        UniverseOfDiscourse universeOfDiscourse = Parser.getUniverseOfDiscourse(lines);
        this.signature = SignatureFactory.createSignature(universeOfDiscourse);
        FormulaParser parser = ParserFactory.createParser(signature);

        List<String> knowledgeBase = Parser.getKBLines(lines);
        List<String> goals = Parser.getGoalsLines(lines);
        knowledgeBaseAST = parser.parseAndGetASTs(knowledgeBase);
        goalsAST = parser.parseAndGetASTs(goals);

        if (WarningHelper.notEmpty()) WarningHelper.printAndReset();
        if (ErrorHelper.notEmpty()) {
            OutputDevice.writeToConsole("Could not validate syntax!");
            ErrorHelper.print();
            return;
        }
        OutputDevice.writeToConsole("Syntax validated!");

        // TRIGGER WARNING! VOODOO
        List<AST> voodooKnowledgeBaseAST = new ArrayList<>();
        List<AST> voodooGoalAST = new ArrayList<>();

        voodooKnowledgeBaseAST.add(new PropositionalAST("A", true));
        voodooGoalAST.add(new PropositionalAST("A", true));

        PropositionalProof voodooProof = new PropositionalProof(signature, voodooKnowledgeBaseAST, voodooGoalAST);
        voodooProof.proveWithoutPrinting();
        // VOODOO ENDED
    }

    public void automatedRun() {
        if (!valid) return;
        ProofTextHelper.clear();

        // TODO Logic Controller should be general
        PropositionalProof proof = new PropositionalProof(signature, knowledgeBaseAST, goalsAST);
        proof.proveWithoutPrinting();
        proof.prove();
    }

    public void manualRun() {
        if (!valid) return;
        ProofTextHelper.clear();

        // TODO Logic Controller should be general
        ManualPropositionalProof proof = new ManualPropositionalProof(knowledgeBaseAST, goalsAST, null, 1);
        ManualPropositionalProofStates.addState(proof, 1);
        proof.prove();
    }
}
