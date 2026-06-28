package me.dariansandru.utils.global;

public abstract class GlobalFlags {
    public static String executionFlag;

    public static boolean printTreeFlag;
    public static final String printTreeFlagString = "--printTree";

    public static boolean indentedProofFlag;
    public static final String indentedProofFlagString = "--indentedProof";

    public static boolean formalProofFlag;
    public static final String formalProofFlagString = "--formalProof";

    public static String inputFilePath = "files/input.txt";
    public static String outputFilePath = null;
    public static String rulesFilePath = "files/rules.txt";
    public static boolean outputToConsole = true;

    private static final String inputPrefix = "input=";
    private static final String outputPrefix = "output=";
    private static final String rulesPrefix = "rules=";

    public static void getFlags(String[] args) {
        if (args == null || args.length <= 1) return;
        String mode = args[0];
        executionFlag = mode;

        for (int i = 1; i < args.length; i++) {
            String arg = args[i];

            if (arg.startsWith(inputPrefix)) {
                String path = arg.substring(inputPrefix.length()).trim();
                if (!path.isEmpty()) inputFilePath = path;
                continue;
            }

            if (arg.startsWith(outputPrefix)) {
                String path = arg.substring(outputPrefix.length()).trim();
                if (!path.isEmpty()) {
                    outputFilePath = path;
                    outputToConsole = false;
                }
                continue;
            }

            if (arg.startsWith(rulesPrefix)) {
                String path = arg.substring(rulesPrefix.length()).trim();
                if (!path.isEmpty()) rulesFilePath = path;
                continue;
            }

            switch (arg) {
                case printTreeFlagString -> {
                    if ("test".equals(mode)) printTreeFlag = true;
                    else System.err.println("Warning: " + printTreeFlagString + " is only allowed in 'test' mode. Ignoring.");
                }
                case indentedProofFlagString -> {
                    if ("automated".equals(mode) || "automate".equals(mode)) indentedProofFlag = true;
                    else System.err.println("Warning: " + indentedProofFlagString + " is only allowed in 'automated' mode. Ignoring.");

                }
                case formalProofFlagString -> {
                    if ("automated".equals(mode) || "automate".equals(mode)) formalProofFlag = true;
                    else System.err.println("Warning: " + formalProofFlagString + " is only allowed in 'automated' mode. Ignoring.");
                }
                default -> {
                    if (!arg.startsWith("-")) System.err.println("Warning: Unrecognized argument '" + arg + "'.");
                }
            }
        }
    }

    public static void reset() {
        printTreeFlag = false;
        indentedProofFlag = false;
        formalProofFlag = false;
        inputFilePath = "files/input.txt";
        outputFilePath = null;
        rulesFilePath = "files/rules.txt";
        outputToConsole = true;
    }
}