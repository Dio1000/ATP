package me.dariansandru.domain.proof.inference_rules;

import me.dariansandru.domain.formula.Formula;
import me.dariansandru.utils.data_structures.ast.AST;

import java.util.List;

public interface InferenceRule {
    String getName();
    boolean canInference(List<AST> asts);
    AST inference(List<AST> asts);
}
