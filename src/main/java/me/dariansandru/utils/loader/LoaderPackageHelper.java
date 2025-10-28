package me.dariansandru.utils.loader;

public class LoaderPackageHelper {
    private static String packageName;

    public LoaderPackageHelper(String packageName) {
        LoaderPackageHelper.packageName = packageName;
    }

    public static String getPackageName() {
        return packageName;
    }

    public static void setPackageName(String packageName) {
        LoaderPackageHelper.packageName = packageName;
    }
}
