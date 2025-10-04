package me.dariansandru.io;

import me.dariansandru.domain.data_structures.ast.AST;
import me.dariansandru.io.exception.OutputException;
import me.dariansandru.utils.helper.KnowledgeBaseRegistry;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class OutputDevice {

    private static final String indentation = "     ";

    public static void write(List<String> lines, String fileName) throws OutputException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            for (String line : lines) {
                writer.write(line + "\n");
            }
        }
        catch (IOException e) {
            throw new OutputException("Could not write to file: " + fileName, e);
        }
    }

    public static void writeToConsole(String message) {
        System.out.println(message);
    }

    public static void writeNewLine() {
        System.out.println();
    }

    public static void writeToConsole(String message, int num) {
        while (num != 0) {
            System.out.print(message);
            num--;
        }
    }

    public static void writeToConsole(String message1, String message2, int num) {
        while (num > 0) {
            System.out.print(message1);
            num--;
        }
        System.out.println(message2);
    }

    public static void writeIndentedToConsole(String message, int num) {
        writeToConsole(indentation, num);
        System.out.println(message);
    }

    public static void writeNumberedToConsole(List<AST> list, int startIndex, String prefix) {
        StringBuilder builder = new StringBuilder();
        for (AST string : list) {
            builder.append(prefix).append(startIndex).append(". ").append(KnowledgeBaseRegistry.getObtainedFrom(string.toString())).append("\n");
            startIndex++;
        }

        System.out.println(builder);
    }

}
