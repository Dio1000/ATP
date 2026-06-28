package me.dariansandru.utils.helper;

import me.dariansandru.domain.data_structures.ast.AST;
import me.dariansandru.domain.data_structures.ast.PropositionalAST;
import me.dariansandru.domain.language.UniverseOfDiscourse;
import me.dariansandru.domain.language.signature.Signature;
import me.dariansandru.domain.language.signature.SignatureFactory;
import me.dariansandru.domain.proof.automated_proof.PropositionalProof;
import me.dariansandru.io.OutputDevice;
import me.dariansandru.utils.global.GlobalTimer;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.util.ArrayList;
import java.util.List;

public class MemoryTracker {

    public static void resetPeak() {
        for (MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans()) {
            pool.resetPeakUsage();
        }
    }

    public static double getPeakMemoryMB() {
        long peakMemory = 0;
        for (MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans()) {
            if (pool.getType() == MemoryType.HEAP) peakMemory += pool.getPeakUsage().getUsed();
        }
        return peakMemory / (1024.0 * 1024.0);
    }

    public static void testAST(String formula, int times) {
        warmup();
        double totalTime = 0;
        double totalMemory = 0;

        for (int i = 0; i < times; i++) {
            System.gc();
            try { Thread.sleep(50); } catch (InterruptedException ignored) {}
            MemoryTracker.resetPeak();
            GlobalTimer.setStartTime();

            new PropositionalAST(formula, true);

            GlobalTimer.setEndTime();
            totalTime += GlobalTimer.getExecutionTime();
            totalMemory += MemoryTracker.getPeakMemoryMB();
        }

        OutputDevice.writeToConsole("AST created (" + times + " times).");
        OutputDevice.writeToConsole("Average Peak memory usage: " + (totalMemory / times) + " MB");
        OutputDevice.writeToConsole("Average Execution time: " + (totalTime / times) + " ms");
        OutputDevice.writeToConsole("");
    }

    public static void testBDD(String formula, int times) {
        warmup();
        double totalTime = 0;
        double totalMemory = 0;

        for (int i = 0; i < times; i++) {
            System.gc();
            try { Thread.sleep(50); } catch (InterruptedException ignored) {}
            MemoryTracker.resetPeak();
            GlobalTimer.setStartTime();

            PropositionalAST ast = new PropositionalAST(formula, true);
            ast.buildBDD();

            GlobalTimer.setEndTime();
            totalTime += GlobalTimer.getExecutionTime();
            totalMemory += MemoryTracker.getPeakMemoryMB();
        }

        OutputDevice.writeToConsole("AST & BDD created (" + times + " times).");
        OutputDevice.writeToConsole("Average Peak memory usage: " + (totalMemory / times) + " MB");
        OutputDevice.writeToConsole("Average Execution time: " + (totalTime / times) + " ms");
        OutputDevice.writeToConsole("");
    }

    public static void testProof(String[] kbStrings, String[] goalStrings) {
        warmup();
        System.gc();
        try { Thread.sleep(50); } catch (InterruptedException ignored) {}
        MemoryTracker.resetPeak();

        List<AST> kb = new ArrayList<>();
        for (String s : kbStrings) kb.add(new PropositionalAST(s, true));

        List<AST> goals = new ArrayList<>();
        for (String s : goalStrings) goals.add(new PropositionalAST(s, true));

        Signature signature = SignatureFactory.createSignature(UniverseOfDiscourse.PROPOSITIONS);

        GlobalTimer.setStartTime();
        PropositionalProof proof = new PropositionalProof(signature, kb, goals);
        proof.proveWithoutPrinting();
        GlobalTimer.setEndTime();

        double peakMem = MemoryTracker.getPeakMemoryMB();

        OutputDevice.writeToConsole("Proof completed.");
        OutputDevice.writeToConsole("Peak memory usage: " + peakMem + " MB");
        OutputDevice.writeToConsole("Execution time: " + GlobalTimer.getExecutionTime() + " ms");
    }

    public static void testProofBatch(String[][] kbs, String[][] goals, int times) {
        for (int i = 0; i < kbs.length; i++) {
            double totalTime = 0;
            double totalMemory = 0;

            for (int j = 0; j < times; j++) {
                System.gc();
                try { Thread.sleep(50); } catch (InterruptedException ignored) {}
                MemoryTracker.resetPeak();
                GlobalTimer.setStartTime();

                List<AST> kbList = new ArrayList<>();
                for (String s : kbs[i]) kbList.add(new PropositionalAST(s, true));
                List<AST> goalList = new ArrayList<>();
                for (String s : goals[i]) goalList.add(new PropositionalAST(s, true));

                Signature sig = SignatureFactory.createSignature(UniverseOfDiscourse.PROPOSITIONS);
                PropositionalProof proof = new PropositionalProof(sig, kbList, goalList);
                proof.proveWithoutPrinting();

                GlobalTimer.setEndTime();
                totalTime += GlobalTimer.getExecutionTime();
                totalMemory += MemoryTracker.getPeakMemoryMB();
            }

            OutputDevice.writeToConsole("Proof Test " + (i + 1) + " (KB Size: " + kbs[i].length + "):");
            OutputDevice.writeToConsole("Average Peak memory usage: " + (totalMemory / times) + " MB");
            OutputDevice.writeToConsole("Average Execution time: " + (totalTime / times) + " ms\n");
        }
    }

    public static void runBenchmarks() {
        String[][] kbs = {
                {"P -> Q", "P"},
                {"P OR Q", "P -> R", "Q -> R"},
                {
                        "A1 -> A2", "A2 -> A3", "A3 -> A4", "A4 -> A5", "A5 -> A6",
                        "A6 -> A7", "A7 -> A8", "A8 -> A9", "A9 -> A10", "A10 -> A11",
                        "A11 -> A12", "A12 -> A13", "A13 -> A14", "A14 -> A15", "A15 -> A16",
                        "A16 -> A17", "A17 -> A18", "A18 -> A19", "A19 -> A20", "A20 -> A21",
                        "A21 -> A22", "A22 -> A23", "A23 -> A24", "A24 -> A25", "A25 -> A26",
                        "A26 -> A27", "A27 -> A28", "A28 -> A29", "A29 -> A30", "A30 -> A31",
                        "A31 -> A32", "A32 -> A33", "A33 -> A34", "A34 -> A35", "A35 -> A36",
                        "A36 -> A37", "A37 -> A38", "A38 -> A39", "A39 -> A40", "A40 -> A41",
                        "A41 -> A42", "A42 -> A43", "A43 -> A44", "A44 -> A45", "A45 -> A46",
                        "A46 -> A47", "A47 -> A48", "A48 -> A49", "A49 -> A50", "A1"
                },
                {"(A AND W) -> P", "!A -> I", "!W -> M", "!P", "E -> (!I AND !M)"}
        };

        String[][] goals = {
                {"Q"},
                {"R"},
                {"A50"},
                {"!E"}
        };

        MemoryTracker.testProofBatch(kbs, goals, 10);
    }
    
    public static void runExpandedBenchmarks() {
        String[] kb25 = new String[25];
        for(int i=0; i<25; i++) kb25[i] = "A" + (i+1) + " -> A" + (i+2);
        kb25[24] = "A1";

        String[] kb50 = new String[50];
        for(int i=0; i<50; i++) kb50[i] = "A" + (i+1) + " -> A" + (i+2);
        kb50[49] = "A1";

        String[][] kbs = {
                {"P", "P -> Q"},
                {"P -> Q", "Q -> R", "P"},
                {"P -> Q", "Q -> R", "R -> S", "P"},
                kb25,
                kb50
        };

        String[][] goals = {
                {"Q"}, {"R"}, {"S"}, {"A26"}, {"A51"}
        };

        MemoryTracker.testProofBatch(kbs, goals, 10);
    }

    private static void warmup() {
        for (int i = 0; i < 5000; i++) {
            PropositionalAST dummy = new PropositionalAST("(A -> B) -> C", true);
            dummy.buildBDD();
        }
    }
}