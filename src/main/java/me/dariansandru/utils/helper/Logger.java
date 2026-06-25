package me.dariansandru.utils.helper;

import me.dariansandru.domain.proof.Strategy;
import me.dariansandru.domain.proof.inference_rules.InferenceRule;
import me.dariansandru.io.OutputDevice;

import java.util.ArrayList;
import java.util.List;

public abstract class Logger {
    public static List<InferenceRule> inferenceRules = new ArrayList<>();
    public static List<Strategy> strategies = new ArrayList<>();

    public static void addRule(InferenceRule inferenceRule) {
        inferenceRules.add(inferenceRule);
    }

    public static void addStrategy(Strategy strategy) {
        strategies.add(strategy);
    }

    public static void showLog() {
        OutputDevice.writeToConsole("\nStrategies:");
        for (Strategy strategy : strategies) OutputDevice.writeToConsole(strategy.toString());

        OutputDevice.writeToConsole("Rules:");
        for (InferenceRule inferenceRule : inferenceRules) OutputDevice.writeToConsole(inferenceRule.toString());
    }
}
