package me.dariansandru.utils.helper;

import java.util.ArrayList;
import java.util.List;

public abstract class ErrorHelper {

    private static List<String> errors = new ArrayList<>();
    private static int size = 0;

    public static boolean notEmpty() {
        return size != 0;
    }

    public static void add(String errorMessage) {
        errors.add(errorMessage);
        size++;
    }

    public static void add(String errorMessage, int line, int position) {
        errors.add("Line " + line + ": Position " + position + ": " + errorMessage + "!");
        size++;
    }

    public static void add(String errorMessage, int line) {
        errors.add("Line " + line + ": " + errorMessage + "!");
        size++;
    }

    public static void print() {
        if (size == 0) return;

        System.out.println("Errors detected: " + size);
        int index = 1;

        while (index <= size) {
            System.out.println(index + ". " + errors.get(index - 1));
            index++;
        }
    }

}
