package me.dariansandru.test;

import me.dariansandru.domain.data_structures.ast.AST;
import me.dariansandru.domain.data_structures.ast.PropositionalAST;
import me.dariansandru.domain.language.UniverseOfDiscourse;
import me.dariansandru.domain.language.signature.Signature;
import me.dariansandru.domain.language.signature.SignatureFactory;
import me.dariansandru.domain.proof.automated_proof.PropositionalProof;
import me.dariansandru.io.OutputDevice;
import me.dariansandru.utils.global.GlobalAtomID;

import java.util.ArrayList;
import java.util.List;

public abstract class TestPropositionalProofs {

    private static class ProofTestCase {

        List<AST> knowledgeBase;
        List<AST> goals;

        ProofTestCase(List<AST> knowledgeBase, List<AST> goals) {
            this.knowledgeBase = knowledgeBase;
            this.goals = goals;
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
        GlobalAtomID.addAtomId("S");
        GlobalAtomID.addAtomId("T");
        GlobalAtomID.addAtomId("A");
        GlobalAtomID.addAtomId("B");
        GlobalAtomID.addAtomId("C");
        GlobalAtomID.addAtomId("D");
        GlobalAtomID.addAtomId("E");
        GlobalAtomID.addAtomId("I");
        GlobalAtomID.addAtomId("M");
        GlobalAtomID.addAtomId("W");
        GlobalAtomID.addAtomId("A1");
        GlobalAtomID.addAtomId("A2");
        GlobalAtomID.addAtomId("A3");
        GlobalAtomID.addAtomId("A4");
        GlobalAtomID.addAtomId("A5");
        GlobalAtomID.addAtomId("A6");
        GlobalAtomID.addAtomId("A7");
        GlobalAtomID.addAtomId("A8");
        GlobalAtomID.addAtomId("A9");
        GlobalAtomID.addAtomId("A10");
    }

    private static void runTests() {
        Object[][] testData = getTestData();

        List<ProofTestCase> parsedTests = new ArrayList<>();

        for (Object[] test : testData) {
            String[] knowledgeBaseStrings = (String[]) test[0];
            String[] goalStrings = (String[]) test[1];

            parsedTests.add(new ProofTestCase(
                    createASTList(knowledgeBaseStrings),
                    createASTList(goalStrings)
            ));
        }

        int passed = 0;

        for (int index = 0; index < parsedTests.size(); index++) {
            ProofTestCase testCase = parsedTests.get(index);

            Signature signature = SignatureFactory.createSignature(UniverseOfDiscourse.PROPOSITIONS);
            PropositionalProof proof = new PropositionalProof(signature, new ArrayList<>(testCase.knowledgeBase), new ArrayList<>(testCase.goals));

            proof.proveWithoutPrinting();
            boolean result = proof.isProven();

            String knowledgeBaseString = getListString(testCase.knowledgeBase);
            String goalString = getListString(testCase.goals);

            if (result) {
                passed++;
                OutputDevice.writeToConsole("Test " + (index + 1) + " passed:");
                OutputDevice.writeToConsole("KB: " + knowledgeBaseString + " | Goals: " + goalString + " | Proven: true");
            }
            else {
                OutputDevice.writeToConsole("Test " + (index + 1) + " failed:");
                OutputDevice.writeToConsole("KB: " + knowledgeBaseString + " | Goals: " + goalString + " | Proven: false");
            }
            OutputDevice.writeToConsole("");
        }

        if (passed == parsedTests.size()) OutputDevice.writeToConsole("All tests passed!");
        else OutputDevice.writeToConsole(passed + "/" + parsedTests.size() + " passed");
    }

    private static Object[][] getTestData() {
        return new Object[][]{
                {
                        new String[]{"P", "P -> Q"},
                        new String[]{"Q"}
                },
                {
                        new String[]{"P -> Q", "Q -> R", "P"},
                        new String[]{"R"}
                },
                {
                        new String[]{"P -> Q", "Q -> R", "R -> S", "P"},
                        new String[]{"S"}
                },
                {
                        new String[]{"P", "P -> (Q -> R)"},
                        new String[]{"Q -> R"}
                },
                {
                        new String[]{"P", "!P"},
                        new String[]{"Contradiction"}
                },
                {
                        new String[]{"P", "P -> Q", "!Q"},
                        new String[]{"Contradiction"}
                },
                {
                        new String[]{"(A AND W) -> P", "!A -> I", "!W -> M", "!P", "E -> (!I AND !M)"},
                        new String[]{"!E"}
                },
                {
                        new String[]{"P -> (Q -> R)", "P", "Q"},
                        new String[]{"R"}
                },
                {
                        new String[]{"P OR Q", "P -> R", "Q -> R"},
                        new String[]{"R"}
                },
                {
                        new String[]{"P -> Q", "!Q"},
                        new String[]{"!P"}
                },
                {
                        new String[]{"P", "Q"},
                        new String[]{"P AND Q"}
                },
                {
                        new String[]{"P", "Q", "R"},
                        new String[]{"P AND Q AND R"}
                },
                {
                        new String[]{"P"},
                        new String[]{"P OR Q"}
                },
                {
                        new String[]{"P AND Q"},
                        new String[]{"(P AND Q) OR R"}
                },
                {
                        new String[]{"!(P AND Q)"},
                        new String[]{"!P OR !Q"}
                },
                {
                        new String[]{"!(P OR Q)"},
                        new String[]{"!P AND !Q"}
                },
                {
                        new String[]{"P -> Q", "Q -> P"},
                        new String[]{"P <-> Q"}
                },
                {
                        new String[]{"P <-> Q"},
                        new String[]{"P -> Q"}
                },
                {
                        new String[]{"P <-> Q"},
                        new String[]{"Q -> P"}
                },
                {
                        new String[]{"(A AND B) OR (C AND D)", "!(A AND B)"},
                        new String[]{"C AND D"}
                },
                {
                        new String[]{"P -> Q", "R -> S", "P OR R"},
                        new String[]{"Q OR S"}
                },
                {
                        new String[]{"P -> Q", "R -> S", "!Q"},
                        new String[]{"!P"}
                },
                {
                        new String[]{"P -> Q", "R -> S", "!Q OR !S"},
                        new String[]{"!P OR !R"}
                },
                {
                        new String[]{"P -> Q"},
                        new String[]{"!P OR Q"}
                },
                {
                        new String[]{"P -> Q"},
                        new String[]{"P -> (P AND Q)"}
                },
                {
                        new String[]{"P -> Q", "Q -> R"},
                        new String[]{"P -> R"}
                },
                {
                        new String[]{"P -> Q", "Q -> R", "R -> S"},
                        new String[]{"P -> S"}
                },
                {
                        new String[]{
                                "A1 -> A2", "A2 -> A3", "A3 -> A4", "A4 -> A5",
                                "A5 -> A6", "A6 -> A7", "A7 -> A8", "A8 -> A9", "A9 -> A10"
                        },
                        new String[]{"A1 -> A10"}
                },
                {
                        new String[]{"!!P"},
                        new String[]{"P"}
                },
                {
                        new String[]{"!(!P AND !Q)"},
                        new String[]{"P OR Q"}
                },
                {
                        new String[]{
                                "P -> Q",
                                "Q -> R",
                                "R -> S",
                                "P"
                        },
                        new String[]{"S"}
                },
                {
                        new String[]{
                                "P OR Q",
                                "P -> R",
                                "Q -> R"
                        },
                        new String[]{"R"}
                },
                {
                        new String[]{
                                "P -> Q",
                                "P -> !Q"
                        },
                        new String[]{"!P"}
                },
                {
                        new String[]{"P AND (Q OR R)"},
                        new String[]{"(P AND Q) OR (P AND R)"}
                },
                {
                        new String[]{"(P AND Q) OR (P AND R)"},
                        new String[]{"P AND (Q OR R)"}
                },
                {
                        new String[]{"(P -> Q) -> P"},
                        new String[]{"P"}
                },
                {
                        new String[]{"P", "P -> Q", "P -> R"},
                        new String[]{"Q", "R"}
                },
                {
                        new String[]{
                                "P AND Q",
                                "(P AND Q) -> R",
                                "R -> S"
                        },
                        new String[]{"S", "P", "Q"}
                },
                {
                        new String[]{"P -> Q"},
                        new String[]{"!Q -> !P"}
                },
                {
                        new String[]{"(P AND Q) -> R", "!R"},
                        new String[]{"!(P AND Q)"}
                },
                {
                        new String[]{
                                "(P AND Q) -> (R OR S)",
                                "(R OR S) -> T",
                                "P AND Q"
                        },
                        new String[]{"T"}
                },
                {
                        new String[]{
                                "(A AND W) -> P",
                                "!A -> I",
                                "!W -> M",
                                "!P",
                                "E -> (!I AND !M)"
                        },
                        new String[]{"!E"}
                },
                {
                        new String[]{
                                "P -> Q",
                                "Q -> R",
                                "R -> S",
                                "S -> T",
                                "P"
                        },
                        new String[]{"T"}
                },
                {
                        new String[]{
                                "P -> Q",
                                "R -> S",
                                "P AND R"
                        },
                        new String[]{"Q AND S"}
                },
                {
                        new String[]{
                                "P -> Q",
                                "R -> S",
                                "P OR R"
                        },
                        new String[]{"Q OR S"}
                },
                {
                        new String[]{"!P OR Q"},
                        new String[]{"P -> Q"}
                },
                {
                        new String[]{"P -> Q", "Q -> P"},
                        new String[]{"P <-> Q"}
                },
                {
                        new String[]{"!(P <-> Q)"},
                        new String[]{"(P AND !Q) OR (!P AND Q)"}
                },
                {
                        new String[]{"(P AND Q) -> R"},
                        new String[]{"P -> (Q -> R)"}
                },
                {
                        new String[]{"P -> (Q -> R)"},
                        new String[]{"(P AND Q) -> R"}
                },
                {
                        new String[]{
                                "(P AND Q) OR (R AND S)",
                                "!(P AND Q)"
                        },
                        new String[]{"R AND S"}
                },
                {
                        new String[]{
                                "P -> Q",
                                "Q -> R",
                                "R -> S",
                                "S -> T",
                                "P OR Q",
                                "!R"
                        },
                        new String[]{"T"}
                }
        };
    }

    private static List<AST> createASTList(String[] formulas) {
        List<AST> asts = new ArrayList<>();
        for (String formula : formulas) {
            if (!formula.isEmpty()) asts.add(new PropositionalAST(formula, true));
        }
        return asts;
    }

    private static String getListString(List<AST> asts) {
        if (asts.isEmpty()) {
            return "{}";
        }

        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < asts.size(); index++) {
            builder.append(asts.get(index));
            if (index < asts.size() - 1) {
                builder.append(", ");
            }
        }
        return builder.toString();
    }
}