package me.dariansandru.reflexivity;

import me.dariansandru.domain.proof.inference_rules.InferenceRule;

import java.util.List;

/*
Implementing this interface allows the user to extend the system with a new set
of Inference Rules. This is useful when creating a Signature for a new Universe
of Discourse, since every Signature owns a set of Inference Rules that can be applied
on the Formulas. The implemented class would be able to return all the inference rules
that belong to a Signature, as well as the custom rules.
 */
public interface InferenceRules {
    List<InferenceRule> get();
    List<InferenceRule> getCustom(String path);
}
