package me.dariansandru.parser;

import me.dariansandru.domain.UniverseOfDiscourse;
import me.dariansandru.utils.helper.ErrorHelper;
import me.dariansandru.utils.helper.WarningHelper;

import java.util.ArrayList;
import java.util.List;

public abstract class Parser {

    public static boolean parseValidInput(List<String> lines) {
        int index = 0;
        boolean universeFlag = false;
        boolean kbFlag = false;
        boolean goalsFlag = false;

        for (String line : lines) {
            switch (line) {
                case "Universe:" -> {
                    UniverseOfDiscourse universeOfDiscourse = UniverseOfDiscourse.getFromString(lines.get(index + 1));
                    if (universeOfDiscourse == UniverseOfDiscourse.UNKNOWN)
                        ErrorHelper.add(lines.get(index + 1) + " is not a valid Universe of Discourse!");
                    universeFlag = true;
                }
                case "KB:" -> {
                    if (index + 1 > lines.size() || lines.get(index + 1).equals("Goals:"))
                        WarningHelper.add("No Knowledge Base entries were provided!");
                    kbFlag = true;
                }
                case "Goals:" -> {
                    if (index + 1 >= lines.size() || lines.get(index + 1).equals("KB:"))
                        ErrorHelper.add("No goals were provided!");
                    goalsFlag = true;
                }
            }
            index++;
        }

        if (!universeFlag) ErrorHelper.add("Universe of Discourse not provided!");
        if (!kbFlag) ErrorHelper.add("Knowledge Base not provided!");
        if (!goalsFlag) ErrorHelper.add("Goals not provided!");

        return universeFlag && kbFlag && goalsFlag;
    }

    public static List<String> getKBLines(List<String> lines) {
        List<String> kbLines = new ArrayList<>();
        boolean inKB = false;

        int index = 0;
        while (index < lines.size()) {
            String line = lines.get(index);

            if (line.equals("KB:")) {
                inKB = true;
                index++;
                continue;
            }
            else if (line.equals("Goals:")) inKB = false;

            if (inKB) {
                if (!line.startsWith("(") && !line.endsWith(")")) kbLines.add("(" + line + ")");
                else kbLines.add(line);
            }
            index++;
        }

        return kbLines;
    }

    public static List<String> getGoalsLines(List<String> lines) {
        List<String> goalLines = new ArrayList<>();
        boolean inGoals = false;

        int index = 0;
        while (index < lines.size()) {
            String line = lines.get(index);

            if (line.equals("Goals:")) {
                inGoals = true;
                index++;
                continue;
            }
            else if (line.equals("KB")) inGoals = false;

            if (inGoals) {
                if (!line.startsWith("(") && !line.endsWith(")")) goalLines.add("(" + line + ")");
                else goalLines.add(line);
            }
            index++;
        }

        return goalLines;
    }

    public static UniverseOfDiscourse getUniverseOfDiscourse(List<String> lines) {
        int index = 0;
        for (String line : lines) {
            if (line.equals("Universe:")) {
                return UniverseOfDiscourse.getFromString(lines.get(index + 1));
            }
            index++;
        }

        return UniverseOfDiscourse.UNKNOWN;
    }

}