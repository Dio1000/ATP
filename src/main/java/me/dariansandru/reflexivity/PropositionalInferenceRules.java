package me.dariansandru.reflexivity;

import me.dariansandru.domain.proof.inference_rules.custom.CustomPropositionalInferenceRule;
import me.dariansandru.domain.proof.inference_rules.InferenceRule;
import me.dariansandru.domain.proof.inference_rules.propositional.*;
import me.dariansandru.utils.global.GlobalFlags;
import me.dariansandru.utils.loader.PropositionalLogicLoader;
import me.dariansandru.utils.helper.ErrorHelper;

import java.util.ArrayList;
import java.util.List;

public class PropositionalInferenceRules implements InferenceRules {

    @Override
    public List<InferenceRule> get() {
        PropositionalLogicLoader loader = new PropositionalLogicLoader();
        List<String> lines = loader.getLines(GlobalFlags.rulesFilePath);
        List<InferenceRule> rules = new ArrayList<>();

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("//")) continue;

            switch (line) {
                case "ModusPonens" -> rules.add(new ModusPonens());
                case "ModusTollens" -> rules.add(new ModusTollens());
                case "ConjunctionElimination" -> rules.add(new ConjunctionElimination());
                case "ConjunctionIntroduction" -> rules.add(new ConjunctionIntroduction());
                case "DisjunctionElimination" -> rules.add(new DisjunctionElimination());
                case "DisjunctionIntroduction" -> rules.add(new DisjunctionIntroduction());
                case "EquivalenceIntroduction" -> rules.add(new EquivalenceIntroduction());
                case "EquivalenceElimination" -> rules.add(new EquivalenceElimination());
                case "ImplicationIntroduction" -> rules.add(new ImplicationIntroduction());
                case "ImplicationElimination" -> rules.add(new ImplicationElimination());
                case "DeMorgan" -> rules.add(new DeMorgan());
                case "ContradictionRule" -> rules.add(new ContradictionRule());
                case "MaterialImplication" -> rules.add(new MaterialImplication());
                case "HypotheticalSyllogism" -> rules.add(new HypotheticalSyllogism());
                case "Absorption" -> rules.add(new Absorption());
                case "ConstructiveDilemma" -> rules.add(new ConstructiveDilemma());
                case "DestructiveDilemma" -> rules.add(new DestructiveDilemma());
                case "DisjunctiveSyllogism" -> rules.add(new DisjunctiveSyllogism());
                default -> ErrorHelper.add("Unknown classical rule in " + GlobalFlags.rulesFilePath + ": " + line);
            }
        }

        return rules;
    }

    @Override
    public List<InferenceRule> getCustom(String path) {
        PropositionalLogicLoader loader = new PropositionalLogicLoader();
        List<String> lines = loader.getLines(path);

        List<InferenceRule> inferenceRules = new ArrayList<>();
        for (String line : lines) {
            if (line.startsWith("rule")) {
                CustomPropositionalInferenceRule rule = loader.loadCustomRule(lines, line);
                rule.createPathToAtomsMap();
                inferenceRules.add(rule);
            }
        }

        return inferenceRules;
    }
}