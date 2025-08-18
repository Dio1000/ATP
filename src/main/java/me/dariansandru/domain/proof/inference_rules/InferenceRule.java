package me.dariansandru.domain.proof.inference_rules;

import me.dariansandru.domain.formula.Formula;
import me.dariansandru.utils.data_structures.ast.AST;

public interface InferenceRule {
    String getName();
    boolean canInference(AST... asts);
    AST inference(AST... asts);
}
