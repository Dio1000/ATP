package me.dariansandru.controller;

import me.dariansandru.domain.language.UniverseOfDiscourse;
import me.dariansandru.domain.data_structures.ast.PropositionalAST;
import me.dariansandru.domain.proof.manual_proof.ManualPropositionalProof;
import me.dariansandru.domain.proof.manual_proof.ManualPropositionalProofStates;
import me.dariansandru.domain.proof.automated_proof.PropositionalProof;
import me.dariansandru.domain.language.signature.Signature;
import me.dariansandru.domain.language.signature.SignatureFactory;
import me.dariansandru.io.InputDevice;
import me.dariansandru.io.OutputDevice;
import me.dariansandru.parser.Parser;
import me.dariansandru.parser.parsers.FormulaParser;
import me.dariansandru.utils.factory.ParserFactory;
import me.dariansandru.domain.data_structures.ast.AST;
import me.dariansandru.utils.helper.ErrorHelper;
import me.dariansandru.utils.helper.ProofTextHelper;
import me.dariansandru.utils.helper.PropositionalLogicHelper;
import me.dariansandru.utils.helper.WarningHelper;
import me.dariansandru.utils.loader.LoaderPackageHelper;

import java.util.ArrayList;
import java.util.List;

public class LogicController {

    private final Signature signature;
    private boolean valid;

    private List<AST> knowledgeBaseAST = new ArrayList<>();
    private List<AST> goalsAST = new ArrayList<>();

    public LogicController(String inputFile) {
        List<String> lines = InputDevice.read(inputFile);
        valid = Parser.parseValidInput(lines);
        if (!valid) {
            this.signature = null;
            WarningHelper.printAndReset();
            ErrorHelper.printAndReset();
            return;
        }
        
        UniverseOfDiscourse universeOfDiscourse = Parser.getUniverseOfDiscourse(lines);
        this.signature = SignatureFactory.createSignature(universeOfDiscourse);
        FormulaParser parser = ParserFactory.createParser(signature);

        List<String> knowledgeBase = Parser.getKBLines(lines);
        List<String> goals = Parser.getGoalsLines(lines);
        knowledgeBaseAST = parser.parseAndGetASTs(knowledgeBase);
        goalsAST = parser.parseAndGetASTs(goals);

        String packageName = Parser.getPackageName(lines);
        LoaderPackageHelper.setPackageName(packageName);

        if (WarningHelper.notEmpty()) WarningHelper.printAndReset();
        if (ErrorHelper.notEmpty()) {
            valid = false;
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

        if (cannotBeProven()) {
            OutputDevice.writeToConsole("This cannot be proven!");
            return;
        }
        else OutputDevice.writeToConsole("Starting proof processing...");

        // TODO Logic Controller should be generic
        PropositionalProof proof = new PropositionalProof(signature, knowledgeBaseAST, goalsAST);
        proof.proveWithoutPrinting();
        proof.prove();
    }

    public void manualRun() {
        if (!valid) return;
        ProofTextHelper.clear();

        if (cannotBeProven()) {
            OutputDevice.writeToConsole("This cannot be proven!");
            return;
        }
        else OutputDevice.writeToConsole("Starting proof processing...");

        // TODO Logic Controller should be generic
        ManualPropositionalProof proof = new ManualPropositionalProof(knowledgeBaseAST, goalsAST, null, 1);
        ManualPropositionalProofStates.addState(proof, 1);
        proof.prove();
    }

    private boolean cannotBeProven() {
        PropositionalAST ast = (PropositionalAST) PropositionalLogicHelper.buildImplication(knowledgeBaseAST, goalsAST.getFirst());
        ast.buildBDD();
        return !ast.getBuilder().isTautology();
    }
}
