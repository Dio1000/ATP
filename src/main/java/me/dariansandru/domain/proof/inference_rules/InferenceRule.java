package me.dariansandru.domain.proof.inference_rules;

import me.dariansandru.domain.formula.Formula;

public interface InferenceRule {
    Formula inference(Formula... formulas);
}
