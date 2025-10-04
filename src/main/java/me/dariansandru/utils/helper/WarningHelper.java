package me.dariansandru.utils.helper;

import java.util.ArrayList;
import java.util.List;

public abstract class WarningHelper {

    private static final List<String> warnings = new ArrayList<>();
    private static int size = 0;

    public static boolean notEmpty() {
        return size != 0;
    }

    public static void add(String warningMessage) {
        warnings.add(warningMessage);
        size++;
    }

    public static void add(String warningMessage, int line, int position) {
        warnings.add("Line " + line + ": Position " + position + ": " + warningMessage + "!");
        size++;
    }

    public static void add(String warningMessage, int line) {
        warnings.add("Line " + ": " + warningMessage + "!");
        size++;
    }

    public static void print() {
        if (size == 0) return;

        System.out.println("Warnings detected: " + size);
        int index = 1;

        while (index <= size) {
            System.out.println(index + ". " + warnings.get(index - 1));
            index++;
        }
    }

    public static void printAndReset() {
        print();

        warnings.clear();
        size = 0;
    }

}
