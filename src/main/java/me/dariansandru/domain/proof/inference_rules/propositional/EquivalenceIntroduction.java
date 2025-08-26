package me.dariansandru.domain.proof.inference_rules.propositional;

import me.dariansandru.domain.proof.inference_rules.InferenceRule;
import me.dariansandru.utils.data_structures.ast.AST;

import java.util.List;

public class EquivalenceIntroduction implements InferenceRule {
    @Override
    public String getName() {
        return "";
    }

    @Override
    public boolean canInference(List<AST> asts) {
        return false;
    }

    @Override
    public AST inference(List<AST> asts) {
        return null;
    }
}
