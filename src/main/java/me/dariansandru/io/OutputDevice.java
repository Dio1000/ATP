package me.dariansandru.io;

import me.dariansandru.io.exception.OutputException;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class OutputDevice {

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

    public static void writeToConsole(String message, int num) {
        while (num != 0) {
            System.out.println(message);
            num--;
        }
    }

    public static void writeToConsole(String message1, String message2, int num) {
        while (num != 0) {
            System.out.println(message1);
            num--;
        }
        System.out.println(message2);
    }

}
