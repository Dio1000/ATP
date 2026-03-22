package me.dariansandru.meta;

public abstract class MetaData {
    public static String INFERENCE_RULES_PATH = "./files/customRules/";
    public static String INFERENCE_RULES_META = "meta";
    public static String INFERENCE_RULES_FILE_FORMAT = ".atpf";

    public static String getInferenceRulesPath(String packageName) {
        return INFERENCE_RULES_PATH + packageName + "/";
    }

    public static String getInferenceRulesFilePath(String packageName) {
        return getInferenceRulesPath(packageName) + packageName + INFERENCE_RULES_FILE_FORMAT;
    }

    public static String getInferenceRulesMetaPath(String packageName) {
        return getInferenceRulesPath(packageName) + INFERENCE_RULES_META;
    }
}
