package me.dariansandru.utils.loader;

import me.dariansandru.domain.proof.inference_rules.InferenceRule;
import me.dariansandru.domain.proof.inference_rules.custom.CustomPropositionalInferenceRule;
import me.dariansandru.domain.proof.inference_rules.helper.CustomInferenceRuleHelper;
import me.dariansandru.meta.MetaData;
import me.dariansandru.reflexivity.PropositionalInferenceRules;
import me.dariansandru.utils.factory.PropositionalInferenceRuleFactory;

import java.util.List;

public class LoaderPackageHelper {
    private static String packageName;

    public LoaderPackageHelper(String packageName) {
        LoaderPackageHelper.packageName = packageName;
    }

    public static String getPackageName() {
        if (packageName == null) return "Classical";
        return packageName;
    }

    public static void setPackageName(String packageName) {
        LoaderPackageHelper.packageName = packageName;
    }

    public static void checkProperties() {
        String packagePath = MetaData.getInferenceRulesPath(packageName);
        CustomInferenceRuleHelper.checkPropositionalProperties(packagePath);
    }
}
