package me.dariansandru.domain.proof.inference_rules.helper;

import me.dariansandru.domain.data_structures.ast.AST;
import me.dariansandru.domain.data_structures.ast.PropositionalAST;
import me.dariansandru.domain.proof.automated_proof.PropositionalProof;
import me.dariansandru.domain.proof.inference_rules.InferenceRule;
import me.dariansandru.domain.proof.inference_rules.custom.CustomPropositionalInferenceRule;
import me.dariansandru.io.OutputDevice;
import me.dariansandru.meta.MetaData;
import me.dariansandru.reflexivity.PropositionalInferenceRules;
import me.dariansandru.utils.global.GlobalAtomID;
import me.dariansandru.utils.helper.WarningHelper;

import java.util.ArrayList;
import java.util.List;

public abstract class CustomInferenceRuleHelper {

    public static void checkPropositionalProperties(String packageName) {
        String packagePath = MetaData.getInferenceRulesFilePath(packageName);
        List<InferenceRule> inferenceRules = new PropositionalInferenceRules().getCustom(packagePath);
        boolean sound = true;
        boolean consistent = false;
        boolean complete;
        boolean correct;

        for (InferenceRule inferenceRule : inferenceRules) {
            if (!isSound((CustomPropositionalInferenceRule) inferenceRule)) {
                sound = false;
                WarningHelper.add("Inference rule: " + inferenceRule + " is not sound!");
            }
        }

        if (!sound) {
            WarningHelper.add("The given system is not sound because it has unsound inference rules!");
            WarningHelper.add("The given system is not consistent because it is not sound!");
        }
        else consistent = true;

        complete = isComplete(inferenceRules) && sound;
        if (!complete && !sound) {
            WarningHelper.add("The given system is not complete because it is not sound!");
        }
        else if (!complete) {
            WarningHelper.add("The given system is not complete because it cannot emulate resolution!");
        }

        correct = sound && complete;
        if (!correct) {
            if (sound) WarningHelper.add("The given system is not correct because it is not complete!");
            else WarningHelper.add("The given system is not correct because it is neither sound nor complete!");
        }

        String metaPath = MetaData.getInferenceRulesMetaPath(packageName);
        String builder = "Checked: true" +
                "\nSound: " + sound +
                "\nConsistent: " + consistent +
                "\nRefutation Complete: " + complete +
                "\nCorrect: " + correct;
        OutputDevice.write(List.of(builder), metaPath);
    }

    private static boolean isSound(CustomPropositionalInferenceRule inferenceRule) {
        return inferenceRule.isTautology();
    }

    private static boolean isComplete(List<InferenceRule> inferenceRules) {
        return resolutionCheck(inferenceRules)
                && contradictionCheck(inferenceRules);
                //&& explosionCheck(inferenceRules);
    }

    private static boolean resolutionCheck(List<InferenceRule> inferenceRules) {
        GlobalAtomID.addAtomId("A");
        GlobalAtomID.addAtomId("B");
        GlobalAtomID.addAtomId("X");

        List<AST> kb = List.of(new PropositionalAST("!A -> X", true), new PropositionalAST("X -> B", true));
        List<AST> goal = List.of(new PropositionalAST("!A -> B", true));
        PropositionalProof proof = new PropositionalProof(inferenceRules, kb, goal);

        return proof.proveWithoutPrinting();
    }

    private static boolean contradictionCheck(List<InferenceRule> rules) {
        GlobalAtomID.addAtomId("A");

        List<AST> kb = new ArrayList<>();
        kb.add(new PropositionalAST("A", true));
        kb.add(new PropositionalAST("!A", true));

        List<AST> goal = List.of(new PropositionalAST(true));

        PropositionalProof proof = new PropositionalProof(rules, kb, goal);
        return proof.proveWithoutPrinting();
    }

    private static boolean explosionCheck(List<InferenceRule> rules) {
        GlobalAtomID.addAtomId("A");

        List<AST> kb = new ArrayList<>();
        kb.add(new PropositionalAST(true));

        List<AST> goal = List.of(new PropositionalAST("A", true));

        PropositionalProof proof = new PropositionalProof(rules, kb, goal);
        return proof.proveWithoutPrinting();
    }
}
