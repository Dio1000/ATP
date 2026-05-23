package me.dariansandru.test;

import me.dariansandru.domain.data_structures.ast.PropositionalAST;
import me.dariansandru.io.OutputDevice;
import me.dariansandru.utils.global.GlobalAtomID;

import java.util.ArrayList;
import java.util.List;

public abstract class TestPropositionalCreation {

    public static void test() {
        registerAtoms();
        createBasicFormulas();
    }

    private static void registerAtoms() {
        GlobalAtomID.addAtomId("P");
        GlobalAtomID.addAtomId("Q");
        GlobalAtomID.addAtomId("R");
        GlobalAtomID.addAtomId("T");
    }

    public static void createBasicFormulas() {

        Object[][] tests = {
                {"P", true},
                {"Q", true},
                {"R", true},
                {"T", true},

                {"P AND Q", true},
                {"P OR Q", true},
                {"P -> Q", true},
                {"P <-> Q", true},

                {"!P", true},
                {"!!P", true},
                {"!!!P", true},
                {"!Q AND P", true},
                {"!(P AND Q)", true},
                {"!(P OR Q)", true},
                {"!P OR Q", true},

                {"P AND Q AND R", true},
                {"P OR Q OR R", true},
                {"(P AND Q) AND R", true},
                {"P AND (Q AND R)", true},
                {"(P OR Q) OR R", true},
                {"P OR (Q OR R)", true},

                {"P AND Q OR R", true},
                {"P OR Q AND R", true},
                {"(P AND Q) OR (R AND T)", true},
                {"(P OR Q) AND (R OR T)", true},

                {"P -> Q -> R", true},
                {"(P -> Q) -> R", true},
                {"P -> (Q -> R)", true},
                {"P -> Q AND R", true},
                {"P AND Q -> R", true},

                {"P <-> Q <-> R", true},
                {"(P <-> Q) <-> R", true},
                {"P <-> (Q <-> R)", true},

                {"!(P AND Q) -> (R OR T)", true},
                {"(P -> Q) AND (R -> T)", true},
                {"(P <-> Q) OR (!R AND T)", true},
                {"!((P OR Q) AND (R OR T))", true},

                {"((((P))))", true},
                {"((((P AND Q))))", true},
                {"!((((P OR Q))))", true},

                {"P AND (Q)", true},
                {"(P) AND Q", true},
                {"((P))", true},

                {"!P AND !Q OR R -> T", true},
                {"((P -> Q) <-> (!R OR T))", true},
                {"!(P <-> Q) AND (R -> T)", true},

                {"P AND", false},
                {"AND P Q", false},
                {"P OR OR Q", false},
                {"-> P Q", false},
                {"P <->", false},
                {"<-> P Q", false},

                {"", false},
                {" ", false},
                {"P AND (", false},
                {"(P AND Q", false},
                {"P OR ) Q", false},
                {")P(", false},
                {"(P -> Q))", false},

                {"P AND AND Q", false},
                {"P OR OR OR Q", false},
                {"P -> -> Q", false},

                {"P ! Q", false},
                {"! AND P", false},
                {"P !AND Q", false},

                {"A XOR B", false},
                {"P NAND Q", false},
                {"(P Q)", false}
        };

        int totalTests = tests.length;
        int passed = 0;

        List<String> failed = new ArrayList<>();

        for (int i = 0; i < tests.length; i++) {
            String formula = (String) tests[i][0];
            boolean expected = (boolean) tests[i][1];

            PropositionalAST ast = new PropositionalAST(formula, true);
            boolean actual = ast.isValid();

            if (actual == expected) {
                passed++;
                OutputDevice.writeToConsole("Test " + (i + 1) + " passed! '" + formula + "' -> " + (actual ? "valid" : "invalid"));
            }
            else {
                failed.add("Test " + (i + 1) + ": '" + formula + "' was " + (actual ? "valid" : "invalid") + " but expected " + (expected ? "valid" : "invalid"));
            }
        }

        if (failed.isEmpty()) {
            OutputDevice.writeToConsole("All tests passed!");
        }
        else {
            OutputDevice.writeToConsole(passed + " out of " + totalTests + " passed!");
            OutputDevice.writeToConsole("");

            OutputDevice.writeToConsole("FAILED TESTS:");
            for (String f : failed) {
                OutputDevice.writeToConsole(f);
            }
        }
    }
}