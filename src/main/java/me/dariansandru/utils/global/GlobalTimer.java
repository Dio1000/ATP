package me.dariansandru.utils.global;

public abstract class GlobalTimer {
    private static long startTime;
    private static long endTime;

    public static void setStartTime() {
        startTime = System.nanoTime();
    }

    public static void setEndTime() {
        endTime = System.nanoTime();
    }

    public static double getExecutionTime() {
        double executionTime = (endTime - startTime) / 1_000_000.0;

        startTime = 0;
        endTime = 0;
        return executionTime;
    }
}
