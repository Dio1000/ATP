package me.dariansandru.utils.global;

public abstract class GlobalFlags {
    public static String executionFlag;

    public static boolean printTreeFlag;
    public static final String printTreeFlagString = "--printTree";

    public static boolean indentedProofFlag;
    public static final String indentedProofFlagString = "--indentedProof";

    public static boolean formalProofFlag;
    public static final String formalProofFlagString = "--formalProof";

    public static void getFlags(String[] args) {
        if (args == null || args.length <= 1) return;
        String mode = args[0];

        for (int i = 1; i < args.length; i++) {
            String arg = args[i];

            switch (arg) {
                case printTreeFlagString -> {
                    if ("test".equals(mode)) printTreeFlag = true;
                    else System.err.println("Warning: " + printTreeFlagString + " is only allowed in 'test' mode. Ignoring.");
                }
                case indentedProofFlagString -> {
                    if ("automated".equals(mode)) indentedProofFlag = true;
                    else System.err.println("Warning: " + indentedProofFlagString + " is only allowed in 'automated' mode. Ignoring.");
                }
                case formalProofFlagString -> {
                    if ("automated".equals(mode)) formalProofFlag = true;
                    else System.err.println("Warning: " + formalProofFlagString + " is only allowed in 'automated' mode. Ignoring.");
                }
                default -> System.err.println("Warning: Unrecognized flag '" + arg + "'.");
            }
        }
    }
}
