package me.dariansandru.utils.manual;

import me.dariansandru.io.InputDevice;

import java.util.ArrayList;
import java.util.List;

public class PropositionalLogicManual implements Manual {

    private final List<ManualEntry> entries = new ArrayList<>();
    private final String fileRoot = "files/man/propositional/";

    private final String nameString = "name:";
    private final String commandNameString = "commandName:";
    private final String aliasesString = "aliases:";
    private final String arityString = "arity:";
    private final String descriptionString = "description:";
    private final String inferenceString = "inference:";

    public PropositionalLogicManual() {
        buildEntries();
    }

    @Override
    public List<ManualEntry> getEntries() {
        return entries;
    }

    @Override
    public void buildEntries() {
        buildEntry("ModusPonens");
        buildEntry("ModusTollens");
    }

    @Override
    public ManualEntry buildEntry(String fileName) {
        String filePath = fileRoot + "/" + fileName;
        List<String> lines = InputDevice.read(filePath);

        String name = "";
        String commandName = "";
        List<String> aliases = new ArrayList<>();
        int arity = -1;
        String description = "";
        String inference = "";

        for (String line : lines) {
            if (line.startsWith(nameString)) name = line.substring(nameString.length()).trim();
            else if (line.startsWith(commandNameString)) commandName = line.substring(commandNameString.length()).trim();
            else if (line.startsWith(aliasesString)) aliases = handleAliases(line);
            else if (line.startsWith(arityString)) arity = handleArity(line);
            else if (line.startsWith(descriptionString)) description = line.substring(descriptionString.length()).trim();
            else if (line.startsWith(inferenceString)) inference = handleInference(line);
            else new ManualEntry("Error", "err", List.of(), -1, "Error", "Error");
        }

        ManualEntry manualEntry = new ManualEntry(name, commandName, aliases,
                arity, description, inference);
        entries.add(manualEntry);
        return manualEntry;
    }

    @Override
    public ManualEntry getEntryByName(String name) {
        for (ManualEntry entry : entries) {
            if (entry.name().equals(name)) return entry;
        }

        return new ManualEntry("Error", "err", List.of(), -1, "Error", "Error");
    }

    private List<String> handleAliases(String line) {
        line = line.substring(aliasesString.length()).trim();
        List<String> parts = List.of(line.split(","));

        List<String> parsedParts = new ArrayList<>();
        for (String part : parts) {
            parsedParts.add(part.trim());
        }

        return parsedParts;
    }

    private int handleArity(String line) {
        line = line.substring(arityString.length()).trim();
        return Integer.parseInt(line);
    }

    private String handleInference(String line) {
        line = line.substring(inferenceString.length()).trim();
        List<String> parts = List.of(line.split(";"));
        List<String> premises = new ArrayList<>();
        List<String> derived = new ArrayList<>();

        List<String> premiseParts = List.of(parts.getFirst().split(","));
        List<String> derivedParts = List.of(parts.get(1).split(","));

        int max = -1;
        for (String premisePart : premiseParts) {
            if (premisePart.length() > max) max = premisePart.length();
            premises.add(premisePart.trim());
        }

        for (String derivedPart : derivedParts) {
            if (derivedPart.length() > max) max = derivedPart.length();
            derived.add(derivedPart.trim());
        }
        StringBuilder builder = new StringBuilder();
        String separator = "-".repeat(max);

        for (String premise : premises) {
            builder.append(premise).append("\n");
        }
        builder.append(separator).append("\n");
        for (String _derived : derived) {
            builder.append(_derived).append("\n");
        }

        return builder.toString();
    }

}
