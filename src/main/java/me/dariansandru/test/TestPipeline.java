package me.dariansandru.test;

import me.dariansandru.io.OutputDevice;

public abstract class TestPipeline {

    public static void test() {
        OutputDevice.writeToConsole("Starting Proposition Creation Test!");
        TestPropositionalCreation.test();
        OutputDevice.writeToConsole("");

        OutputDevice.writeToConsole("Starting Propositional Inference Rule Test!");
        TestPropositionalInferenceRules.test();
        OutputDevice.writeToConsole("");

        OutputDevice.writeToConsole("Starting Propositional Proof Test!");
        TestPropositionalProofs.test();
        OutputDevice.writeToConsole("");
    }
}
