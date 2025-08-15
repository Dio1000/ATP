package me.dariansandru.io;

import me.dariansandru.io.exception.InputException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class InputDevice {

    public static List<String> read(String fileName) throws InputException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (!line.isEmpty() && !line.startsWith("//")) lines.add(line.trim());
            }
        }
        catch (IOException e) {
            throw new InputException("Could not read from file: " + fileName, e);
        }

        return lines;
    }

}
