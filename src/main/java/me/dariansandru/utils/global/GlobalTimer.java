package me.dariansandru.utils.global;

public abstract class GlobalTimer {
    private static long startTime;
    private static long endTime;
    private static long proofStartTime;
    private static long proofEndTime;

    public static synchronized void setStartTime() {
        startTime = System.nanoTime();
    }

    public static synchronized void setEndTime() {
        endTime = System.nanoTime();
    }

    public static synchronized void setProofTestStartTime() {
        proofStartTime = System.nanoTime();
    }

    public static synchronized void setProofEndTime() {
        proofEndTime = System.nanoTime();
    }

    public static double getExecutionTime() {
        double executionTime = (endTime - startTime - (proofEndTime - proofStartTime)) / 1_000_000.0;

        startTime = 0;
        endTime = 0;
        return executionTime;
    }
}
