package me.dariansandru.test;

import me.dariansandru.domain.data_structures.ast.AST;
import me.dariansandru.domain.data_structures.ast.PropositionalAST;
import me.dariansandru.domain.proof.inference_rules.InferenceRule;
import me.dariansandru.domain.proof.inference_rules.propositional.*;
import me.dariansandru.io.OutputDevice;
import me.dariansandru.utils.global.GlobalAtomID;

import java.util.ArrayList;
import java.util.List;

public abstract class TestPropositionalInferenceRules {

    private static class InferenceTestCase {

        List<AST> inputs;
        AST goal;
        InferenceRule rule;
        List<AST> expected;

        InferenceTestCase(List<AST> inputs, AST goal, InferenceRule rule, List<AST> expected) {
            this.inputs = inputs;
            this.goal = goal;
            this.rule = rule;
            this.expected = expected;
        }
    }

    private static class FailedTest {

        int index;
        String from;
        String goal;
        String obtained;
        String expected;
        String rule;

        FailedTest(int index, String from, String goal, String obtained, String expected, String rule) {
            this.index = index;
            this.from = from;
            this.goal = goal;
            this.obtained = obtained;
            this.expected = expected;
            this.rule = rule;
        }
    }

    public static void test() {
        registerAtoms();
        runTests();
    }

    private static void registerAtoms() {
        GlobalAtomID.addAtomId("P");
        GlobalAtomID.addAtomId("Q");
        GlobalAtomID.addAtomId("R");
        GlobalAtomID.addAtomId("T");
    }

    private static void runTests() {
        Object[][] tests = getObjects();

        List<InferenceTestCase> parsedTests = new ArrayList<>();
        List<FailedTest> failedTests = new ArrayList<>();

        for (Object[] test : tests) {
            InferenceRule rule = (InferenceRule) test[0];

            String[] inputStrings = (String[]) test[1];
            String goalString = (String) test[2];
            String[] expectedStrings = (String[]) test[3];

            AST goal = null;
            if (!goalString.isBlank()) goal = new PropositionalAST(goalString, true);
            parsedTests.add(new InferenceTestCase(createASTList(inputStrings), goal, rule, createASTList(expectedStrings)));
        }

        int passed = 0;
        for (int i = 0; i < parsedTests.size(); i++) {
            InferenceTestCase test = parsedTests.get(i);
            List<AST> output = test.rule.inference(test.inputs, test.goal);
            boolean ok = sameOutput(output, test.expected);

            String from = getListString(test.inputs);
            String goal = test.goal == null ? "{}" : test.goal.toString();
            String obtained = getListString(output);
            String expected = getListString(test.expected);
            String ruleName = test.rule.name();

            if (ok) {
                passed++;
                OutputDevice.writeToConsole("Test " + (i + 1) + " passed:");
                OutputDevice.writeToConsole("From " + from + ", goal " + goal + ", by rule " + ruleName + ", we obtained " + obtained);
            }
            else {
                OutputDevice.writeToConsole("Test " + (i + 1) + " failed:");
                OutputDevice.writeToConsole("From " + from + ", goal " + goal + ", by rule " + ruleName + ", we obtained " + obtained
                );
                failedTests.add(new FailedTest(i + 1, from, goal, obtained, expected, ruleName));
            }
            OutputDevice.writeToConsole("");
        }

        if (passed == parsedTests.size()) OutputDevice.writeToConsole("All tests passed!");
        else {
            OutputDevice.writeToConsole(passed + "/" + parsedTests.size() + " passed");
            OutputDevice.writeToConsole("\nFailed tests:");

            for (FailedTest failed : failedTests) {
                OutputDevice.writeToConsole("Test " + failed.index + " | rule: " + failed.rule + "\nFrom: " + failed.from + "\nGoal: " + failed.goal + "\nExpected: " + failed.expected + "\nGot: " + failed.obtained + "\n");
            }
        }
    }

    private static Object[][] getObjects() {
        Absorption absorption = new Absorption();

        ConjunctionIntroduction conjunctionIntroduction = new ConjunctionIntroduction();
        ConjunctionElimination conjunctionElimination = new ConjunctionElimination();

        DisjunctionIntroduction disjunctionIntroduction = new DisjunctionIntroduction();
        DisjunctionElimination disjunctionElimination = new DisjunctionElimination();
        DisjunctiveSyllogism disjunctiveSyllogism = new DisjunctiveSyllogism();

        ModusPonens modusPonens = new ModusPonens();
        ModusTollens modusTollens = new ModusTollens();
        HypotheticalSyllogism hypotheticalSyllogism = new HypotheticalSyllogism();

        DeMorgan deMorgan = new DeMorgan();

        EquivalenceElimination equivalenceElimination = new EquivalenceElimination();
        EquivalenceIntroduction equivalenceIntroduction = new EquivalenceIntroduction();

        MaterialImplication materialImplication = new MaterialImplication();

        ConstructiveDilemma constructiveDilemma = new ConstructiveDilemma();
        DestructiveDilemma destructiveDilemma = new DestructiveDilemma();

        return new Object[][]{

                {
                        conjunctionIntroduction,
                        new String[]{"P", "Q"},
                        "",
                        new String[]{"P AND Q"}
                },

                {
                        conjunctionIntroduction,
                        new String[]{"P AND Q", "R OR T"},
                        "",
                        new String[]{"(P AND Q) AND (R OR T)"}
                },

                {
                        conjunctionIntroduction,
                        new String[]{"P -> Q", "Q -> R"},
                        "",
                        new String[]{"(P -> Q) AND (Q -> R)"}
                },

                {
                        conjunctionIntroduction,
                        new String[]{"!(P OR Q)", "R"},
                        "",
                        new String[]{"!(P OR Q) AND R"}
                },

                {
                        conjunctionIntroduction,
                        new String[]{"(P AND Q) -> R", "R -> T"},
                        "",
                        new String[]{"((P AND Q) -> R) AND (R -> T)"}
                },



                {
                        conjunctionElimination,
                        new String[]{"P AND Q"},
                        "",
                        new String[]{"P", "Q"}
                },

                {
                        conjunctionElimination,
                        new String[]{"(P OR Q) AND (R -> T)"},
                        "",
                        new String[]{"P OR Q", "R -> T"}
                },

                {
                        conjunctionElimination,
                        new String[]{"(P AND Q) AND (R AND T)"},
                        "",
                        new String[]{"P AND Q", "R AND T"}
                },

                {
                        conjunctionElimination,
                        new String[]{"!(P OR Q) AND (R <-> T)"},
                        "",
                        new String[]{"!(P OR Q)", "R <-> T"}
                },

                {
                        conjunctionElimination,
                        new String[]{"((P -> Q) OR R) AND !T"},
                        "",
                        new String[]{"(P -> Q) OR R", "!T"}
                },



                {
                        disjunctionIntroduction,
                        new String[]{"P"},
                        "P OR Q",
                        new String[]{"P OR Q"}
                },

                {
                        disjunctionIntroduction,
                        new String[]{"Q"},
                        "P OR Q",
                        new String[]{"P OR Q"}
                },

                {
                        disjunctionIntroduction,
                        new String[]{"P AND Q"},
                        "(P AND Q) OR R",
                        new String[]{"(P AND Q) OR R"}
                },

                {
                        disjunctionIntroduction,
                        new String[]{"R -> T"},
                        "(R -> T) OR (P AND Q)",
                        new String[]{"(R -> T) OR (P AND Q)"}
                },

                {
                        disjunctionIntroduction,
                        new String[]{"!(P OR Q)"},
                        "!(P OR Q) OR (R AND T)",
                        new String[]{"!(P OR Q) OR (R AND T)"}
                },



                {
                        disjunctionElimination,
                        new String[]{"P OR P"},
                        "",
                        new String[]{"P"}
                },

                {
                        disjunctionElimination,
                        new String[]{"(P AND Q) OR (P AND Q)"},
                        "",
                        new String[]{"P AND Q"}
                },

                {
                        disjunctionElimination,
                        new String[]{"!(P OR Q) OR !(P OR Q)"},
                        "",
                        new String[]{"!(P OR Q)"}
                },



                {
                        modusPonens,
                        new String[]{"P", "P -> Q"},
                        "",
                        new String[]{"Q"}
                },

                {
                        modusPonens,
                        new String[]{"P AND Q", "(P AND Q) -> R"},
                        "",
                        new String[]{"R"}
                },

                {
                        modusPonens,
                        new String[]{"!(P OR Q)", "!(P OR Q) -> R"},
                        "",
                        new String[]{"R"}
                },

                {
                        modusPonens,
                        new String[]{"P <-> Q", "(P <-> Q) -> (R AND T)"},
                        "",
                        new String[]{"R AND T"}
                },

                {
                        modusPonens,
                        new String[]{"(P -> Q) AND R", "((P -> Q) AND R) -> T"},
                        "",
                        new String[]{"T"}
                },



                {
                        modusTollens,
                        new String[]{"!Q", "P -> Q"},
                        "",
                        new String[]{"!P"}
                },

                {
                        modusTollens,
                        new String[]{"!(R AND T)", "P -> (R AND T)"},
                        "",
                        new String[]{"!P"}
                },

                {
                        modusTollens,
                        new String[]{"!(P OR Q)", "R -> (P OR Q)"},
                        "",
                        new String[]{"!R"}
                },

                {
                        modusTollens,
                        new String[]{"!(P <-> Q)", "R -> (P <-> Q)"},
                        "",
                        new String[]{"!R"}
                },

                {
                        modusTollens,
                        new String[]{"!(R -> T)", "(P AND Q) -> (R -> T)"},
                        "",
                        new String[]{"!(P AND Q)"}
                },



                {
                        deMorgan,
                        new String[]{"!(P AND Q)"},
                        "",
                        new String[]{"!P OR !Q"}
                },

                {
                        deMorgan,
                        new String[]{"!(P OR Q)"},
                        "",
                        new String[]{"!P AND !Q"}
                },

                {
                        deMorgan,
                        new String[]{"!((P AND Q) AND R)"},
                        "",
                        new String[]{"!(P AND Q) OR !R"}
                },

                {
                        deMorgan,
                        new String[]{"!((P OR Q) OR R)"},
                        "",
                        new String[]{"!(P OR Q) AND !R"}
                },

                {
                        deMorgan,
                        new String[]{"!((P -> Q) AND (R -> T))"},
                        "",
                        new String[]{"!(P -> Q) OR !(R -> T)"}
                },

                {
                        deMorgan,
                        new String[]{"!((P <-> Q) OR (R AND T))"},
                        "",
                        new String[]{"!(P <-> Q) AND !(R AND T)"}
                },



                {
                        equivalenceIntroduction,
                        new String[]{"P -> Q", "Q -> P"},
                        "",
                        new String[]{"P <-> Q"}
                },

                {
                        equivalenceIntroduction,
                        new String[]{"(P AND Q) -> R", "R -> (P AND Q)"},
                        "",
                        new String[]{"(P AND Q) <-> R"}
                },

                {
                        equivalenceIntroduction,
                        new String[]{"!(P OR Q) -> R", "R -> !(P OR Q)"},
                        "",
                        new String[]{"!(P OR Q) <-> R"}
                },

                {
                        equivalenceIntroduction,
                        new String[]{"(P -> Q) -> (R -> T)", "(R -> T) -> (P -> Q)"},
                        "",
                        new String[]{"(P -> Q) <-> (R -> T)"}
                },

                {
                        equivalenceIntroduction,
                        new String[]{"(P AND Q) -> (R OR T)", "(R OR T) -> (P AND Q)"},
                        "",
                        new String[]{"(P AND Q) <-> (R OR T)"}
                },



                {
                        equivalenceElimination,
                        new String[]{"P <-> Q"},
                        "",
                        new String[]{"P -> Q", "Q -> P"}
                },

                {
                        equivalenceElimination,
                        new String[]{"(P AND Q) <-> (R OR T)"},
                        "",
                        new String[]{"(P AND Q) -> (R OR T)", "(R OR T) -> (P AND Q)"}
                },

                {
                        equivalenceElimination,
                        new String[]{"!(P OR Q) <-> (R AND T)"},
                        "",
                        new String[]{"!(P OR Q) -> (R AND T)", "(R AND T) -> !(P OR Q)"}
                },

                {
                        equivalenceElimination,
                        new String[]{"(P -> Q) <-> !(R AND T)"},
                        "",
                        new String[]{"(P -> Q) -> !(R AND T)", "!(R AND T) -> (P -> Q)"}
                },



                {
                        materialImplication,
                        new String[]{"P -> Q"},
                        "",
                        new String[]{"!P OR Q"}
                },

                {
                        materialImplication,
                        new String[]{"!P -> Q"},
                        "",
                        new String[]{"P OR Q"}
                },

                {
                        materialImplication,
                        new String[]{"(P AND Q) -> R"},
                        "",
                        new String[]{"!(P AND Q) OR R"}
                },

//                {
//                        materialImplication,
//                        new String[]{"(P OR Q) -> (R AND T)"},
//                        "",
//                        new String[]{"!(P OR Q) OR (R AND T)"}
//                },

//                {
//                        materialImplication,
//                        new String[]{"!(P AND Q) -> !(R OR T)"},
//                        "",
//                        new String[]{"!(!(P AND Q)) OR !(R OR T)"}
//                },

                {
                        materialImplication,
                        new String[]{"(P <-> Q) -> (R -> T)"},
                        "",
                        new String[]{"!(P <-> Q) OR (R -> T)"}
                },



                {
                        constructiveDilemma,
                        new String[]{"P -> Q", "R -> T", "P OR R"},
                        "",
                        new String[]{"Q OR T"}
                },

                {
                        constructiveDilemma,
                        new String[]{"(P AND Q) -> R", "T -> Q", "(P AND Q) OR T"},
                        "",
                        new String[]{"R OR Q"}
                },

                {
                        constructiveDilemma,
                        new String[]{"!(P OR Q) -> R", "(R AND T) -> Q", "!(P OR Q) OR (R AND T)"},
                        "",
                        new String[]{"R OR Q"}
                },



                {
                        destructiveDilemma,
                        new String[]{"P -> Q", "R -> T", "!Q OR !T"},
                        "",
                        new String[]{"!P OR !R"}
                },

                {
                        destructiveDilemma,
                        new String[]{"(P AND Q) -> R", "T -> Q", "!R OR !Q"},
                        "",
                        new String[]{"!(P AND Q) OR !T"}
                },

//                {
//                        destructiveDilemma,
//                        new String[]{"!(P OR Q) -> R", "(R AND T) -> Q", "!R OR !Q"},
//                        "",
//                        new String[]{"!(!(P OR Q)) OR !(R AND T)"}
//                },



                {
                        disjunctiveSyllogism,
                        new String[]{"P OR Q", "!P"},
                        "",
                        new String[]{"Q"}
                },

                {
                        disjunctiveSyllogism,
                        new String[]{"P OR Q", "!Q"},
                        "",
                        new String[]{"P"}
                },

                {
                        disjunctiveSyllogism,
                        new String[]{"(P AND Q) OR R", "!(P AND Q)"},
                        "",
                        new String[]{"R"}
                },

//                {
//                        disjunctiveSyllogism,
//                        new String[]{"!(P OR Q) OR (R -> T)", "!(!(P OR Q))"},
//                        "",
//                        new String[]{"R -> T"}
//                },

                {
                        disjunctiveSyllogism,
                        new String[]{"(P <-> Q) OR (R AND T)", "!(R AND T)"},
                        "",
                        new String[]{"P <-> Q"}
                },



                {
                        absorption,
                        new String[]{"P -> Q"},
                        "P -> (P AND Q)",
                        new String[]{"P -> (P AND Q)"}
                },

                {
                        absorption,
                        new String[]{"(P AND Q) -> R"},
                        "(P AND Q) -> ((P AND Q) AND R)",
                        new String[]{"(P AND Q) -> ((P AND Q) AND R)"}
                },

                {
                        absorption,
                        new String[]{"!(P OR Q) -> T"},
                        "!(P OR Q) -> (!(P OR Q) AND T)",
                        new String[]{"!(P OR Q) -> (!(P OR Q) AND T)"}
                },



                {
                        hypotheticalSyllogism,
                        new String[]{"P -> Q", "Q -> R"},
                        "",
                        new String[]{"P -> R"}
                },

                {
                        hypotheticalSyllogism,
                        new String[]{"(P AND Q) -> R", "R -> T"},
                        "",
                        new String[]{"(P AND Q) -> T"}
                },

                {
                        hypotheticalSyllogism,
                        new String[]{"!(P OR Q) -> R", "R -> (T AND Q)"},
                        "",
                        new String[]{"!(P OR Q) -> (T AND Q)"}
                },

                {
                        hypotheticalSyllogism,
                        new String[]{"(P <-> Q) -> (R -> T)", "(R -> T) -> !(P AND Q)"},
                        "",
                        new String[]{"(P <-> Q) -> !(P AND Q)"}
                }
        };
    }

    private static List<AST> createASTList(String[] formulas) {
        List<AST> asts = new ArrayList<>();
        for (String formula : formulas) {
            asts.add(new PropositionalAST(formula, true));
        }
        return asts;
    }

    private static boolean sameOutput(List<AST> first, List<AST> second) {
        if (first.size() != second.size()) return false;

        boolean[] matched = new boolean[second.size()];
        for (AST astFirst : first) {
            boolean found = false;
            for (int i = 0; i < second.size(); i++) {
                if (!matched[i] && astFirst.isEquivalentTo(second.get(i))) {
                    matched[i] = true;
                    found = true;
                    break;
                }
            }
            if (!found) return false;
        }

        for (boolean match : matched) if (!match) return false;
        return true;
    }

    private static String getListString(List<AST> asts) {
        if (asts.isEmpty()) return "{}";

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < asts.size(); i++) {
            builder.append(asts.get(i));
            if (i < asts.size() - 1) builder.append(", ");
        }
        return builder.toString();
    }
}