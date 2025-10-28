package me.dariansandru.domain.proof.manual_proof.helper;

import me.dariansandru.domain.language.LogicalOperator;
import me.dariansandru.domain.data_structures.ast.AST;
import me.dariansandru.domain.data_structures.ast.PropositionalAST;
import me.dariansandru.parser.command.Command;
import me.dariansandru.utils.flyweight.LogicalOperatorFlyweight;
import me.dariansandru.utils.helper.ErrorHelper;
import me.dariansandru.utils.helper.KnowledgeBaseRegistry;
import me.dariansandru.utils.helper.PropositionalLogicHelper;

import java.util.ArrayList;
import java.util.List;

public class ManualPropositionalInferenceRuleHelper {

    private List<AST> knowledgeBase = new ArrayList<>();

    public ManualPropositionalInferenceRuleHelper(List<AST> knowledgeBase) {
        this.knowledgeBase = knowledgeBase;
    }

    public void loadData(List<AST> knowledgeBase) {
        this.knowledgeBase = knowledgeBase;
    }

    public boolean handleModusPonens(int index1, int index2) {
        AST ast1 = knowledgeBase.get(index1);
        AST ast2 = knowledgeBase.get(index2);

        if (PropositionalLogicHelper.getOutermostOperation(ast1) == LogicalOperator.IMPLICATION) {
            if (canApplyModusPonens(ast1, ast2)) return true;
        } else if (PropositionalLogicHelper.getOutermostOperation(ast2) == LogicalOperator.IMPLICATION) {
            if (canApplyModusPonens(ast2, ast1)) return true;
        }

        ErrorHelper.add("Cannot apply Modus Ponens on " + ast1 + " and " + ast2 + "!");
        return false;
    }

    private boolean canApplyModusPonens(AST ast1, AST ast2) {
        AST antecedent1 = ast1.getSubtree(0);
        AST conclusion1 = ast1.getSubtree(1);

        if (antecedent1.isEquivalentTo(ast2)) {
            KnowledgeBaseRegistry.addObtainedFrom(conclusion1.toString(),
                    List.of(antecedent1.toString(), ast1.toString()), "Modus Ponens");
            if (!containsEntry(conclusion1)) knowledgeBase.add(conclusion1);
            return true;
        }
        return false;
    }

    public boolean handleModusTollens(int index1, int index2) {
        AST ast1 = knowledgeBase.get(index1);
        AST ast2 = knowledgeBase.get(index2);

        if (PropositionalLogicHelper.getOutermostOperation(ast1) == LogicalOperator.IMPLICATION) {
            if (canApplyModusTollens(ast1, ast2)) return true;
        } else if (PropositionalLogicHelper.getOutermostOperation(ast2) == LogicalOperator.IMPLICATION) {
            if (canApplyModusTollens(ast2, ast1)) return true;
        }

        ErrorHelper.add("Cannot apply Modus Tollens on " + ast1 + " and " + ast2 + "!");
        return false;
    }

    private boolean canApplyModusTollens(AST ast1, AST ast2) {
        AST antecedent1 = ast1.getSubtree(0);
        AST conclusion1 = ast1.getSubtree(1);
        conclusion1.negate();

        if (conclusion1.isEquivalentTo(ast2)) {
            antecedent1.negate();
            KnowledgeBaseRegistry.addObtainedFrom(antecedent1.toString(),
                    List.of(conclusion1.toString(), ast1.toString()), "Modus Tollens");
            if (!containsEntry(antecedent1)) knowledgeBase.add(antecedent1);
            return true;
        }
        return false;
    }

    public boolean handleHypotheticalSyllogism(int index1, int index2) {
        AST ast1 = knowledgeBase.get(index1);
        AST ast2 = knowledgeBase.get(index2);

        if (PropositionalLogicHelper.getOutermostOperation(ast1) != LogicalOperator.IMPLICATION ||
                PropositionalLogicHelper.getOutermostOperation(ast2) != LogicalOperator.IMPLICATION) {
            ErrorHelper.add("Cannot apply Hypothetical Syllogism on " + ast1 + " and " + ast2 + "!");
            return false;
        }

        AST antecedent1 = ast1.getSubtree(0);
        AST conclusion1 = ast1.getSubtree(1);
        AST antecedent2 = ast2.getSubtree(0);
        AST conclusion2 = ast2.getSubtree(1);

        if (conclusion1.isEquivalentTo(antecedent2)) {
            AST newAST = new PropositionalAST(
                    antecedent1 + " " + LogicalOperatorFlyweight.getImplicationString() + " " + conclusion2, true);
            KnowledgeBaseRegistry.addObtainedFrom(newAST.toString(), List.of(ast1.toString(), ast2.toString()), "Hypothetical Syllogism");
            if (!containsEntry(newAST)) knowledgeBase.add(newAST);
            return true;
        } else if (conclusion2.isEquivalentTo(antecedent1)) {
            AST newAST = new PropositionalAST(
                    antecedent2 + " " + LogicalOperatorFlyweight.getImplicationString() + " " + conclusion1, true);
            KnowledgeBaseRegistry.addObtainedFrom(newAST.toString(), List.of(ast1.toString(), ast2.toString()), "Hypothetical Syllogism");
            if (!containsEntry(newAST)) knowledgeBase.add(newAST);
            return true;
        }

        ErrorHelper.add("Cannot apply Hypothetical Syllogism on " + ast1 + " and " + ast2 + "!");
        return false;
    }

    public boolean handleDisjunctiveSyllogism(int index1, int index2) {
        AST ast1 = knowledgeBase.get(index1);
        AST ast2 = knowledgeBase.get(index2);

        if (PropositionalLogicHelper.getOutermostOperation(ast1) != LogicalOperator.DISJUNCTION &&
                PropositionalLogicHelper.getOutermostOperation(ast2) != LogicalOperator.DISJUNCTION) {
            ErrorHelper.add("Cannot apply Disjunctive Syllogism on " + ast1 + " and " + ast2 + "!");
            return false;
        }

        if (PropositionalLogicHelper.getOutermostOperation(ast1) == LogicalOperator.DISJUNCTION) {
            if (canApplyDisjunctiveSyllogism(ast2, ast1, ast1.toString(), ast2.toString())) return true;
        }
        else if (PropositionalLogicHelper.getOutermostOperation(ast2) == LogicalOperator.DISJUNCTION) {
            if (canApplyDisjunctiveSyllogism(ast1, ast2, ast1.toString(), ast2.toString())) return true;
        }

        ErrorHelper.add("Cannot apply Disjunctive Syllogism on " + ast1 + " and " + ast2 + "!");
        return false;
    }

    public boolean canApplyDisjunctiveSyllogism(AST ast1, AST ast2, String string, String string2) {
        AST left = ast2.getSubtree(0);
        AST leftCopy = new PropositionalAST(left.toString(), true);

        AST right = ast2.getSubtree(1);
        AST rightCopy = new PropositionalAST(right.toString(), true);

        left.negate();
        right.negate();

        if (left.isEquivalentTo(ast1)) {
            KnowledgeBaseRegistry.addObtainedFrom(rightCopy.toString(), List.of(string, string2), "Disjunctive Syllogism");
            if (!containsEntry(rightCopy)) knowledgeBase.add(rightCopy);
            return true;
        }
        else if (right.isEquivalentTo(ast1)) {
            KnowledgeBaseRegistry.addObtainedFrom(leftCopy.toString(), List.of(string, string2), "Disjunctive Syllogism");
            if (!containsEntry(left)) knowledgeBase.add(leftCopy);
            return true;
        }
        return false;
    }

    public boolean handleConstructiveDilemma(int index1, int index2, int index3) {
        AST ast1 = knowledgeBase.get(index1);
        AST ast2 = knowledgeBase.get(index2);
        AST ast3 = knowledgeBase.get(index3);

        if (PropositionalLogicHelper.getOutermostOperation(ast1) == LogicalOperator.IMPLICATION &&
                PropositionalLogicHelper.getOutermostOperation(ast2) == LogicalOperator.IMPLICATION &&
                PropositionalLogicHelper.getOutermostOperation(ast3) == LogicalOperator.DISJUNCTION) {
            if (canApplyConstructiveSyllogism(ast1, ast2, ast3, ast2.toString(), ast3.toString())) return true;
        }
        else if (PropositionalLogicHelper.getOutermostOperation(ast1) == LogicalOperator.IMPLICATION &&
                PropositionalLogicHelper.getOutermostOperation(ast2) == LogicalOperator.DISJUNCTION &&
                PropositionalLogicHelper.getOutermostOperation(ast3) == LogicalOperator.IMPLICATION) {
            if (canApplyConstructiveSyllogism(ast1, ast3, ast2, ast2.toString(), ast3.toString())) return true;
        }
        else if (PropositionalLogicHelper.getOutermostOperation(ast1) == LogicalOperator.DISJUNCTION &&
                PropositionalLogicHelper.getOutermostOperation(ast2) == LogicalOperator.IMPLICATION &&
                PropositionalLogicHelper.getOutermostOperation(ast3) == LogicalOperator.IMPLICATION) {
            AST antecedent1 = ast2.getSubtree(0);
            AST conclusion1 = ast2.getSubtree(1);
            AST antecedent2 = ast3.getSubtree(0);
            AST conclusion2 = ast3.getSubtree(1);
            AST left = ast1.getSubtree(0);
            AST right = ast1.getSubtree(1);

            if (antecedent1.isEquivalentTo(left) && antecedent2.isEquivalentTo(right) ||
                    antecedent1.isEquivalentTo(right) && antecedent2.isEquivalentTo(left)) {
                AST newAST = new PropositionalAST(conclusion1.toString() + " " + LogicalOperatorFlyweight.getDisjunctionString() + " " + conclusion2.toString(), true);
                KnowledgeBaseRegistry.addObtainedFrom(newAST.toString(), List.of(ast1.toString(), ast2.toString(), ast3.toString()), "Constructive Dilemma");
                if (!containsEntry(newAST)) knowledgeBase.add(newAST);
                return true;
            }
        }
        ErrorHelper.add("Cannot apply Constructive Dilemma on " + ast1 + ", " + ast2 + " and " + ast3 + "!");
        return false;
    }

    public boolean canApplyConstructiveSyllogism(AST ast1, AST ast2, AST ast3, String string, String string2) {
        AST antecedent1 = ast1.getSubtree(0);
        AST conclusion1 = ast1.getSubtree(1);
        AST antecedent2 = ast2.getSubtree(0);
        AST conclusion2 = ast2.getSubtree(1);
        AST left = ast3.getSubtree(0);
        AST right = ast3.getSubtree(1);

        if (antecedent1.isEquivalentTo(left) && antecedent2.isEquivalentTo(right) ||
                antecedent1.isEquivalentTo(right) && antecedent2.isEquivalentTo(left)) {
            AST newAST = new PropositionalAST(conclusion1.toString() + " " + LogicalOperatorFlyweight.getDisjunctionString() + " " + conclusion2.toString(), true);
            KnowledgeBaseRegistry.addObtainedFrom(newAST.toString(), List.of(ast1.toString(), string, string2), "Constructive Dilemma");
            if (!containsEntry(newAST)) knowledgeBase.add(newAST);
            return true;
        }
        return false;
    }

    public boolean handleDestructiveDilemma(int index1, int index2, int index3) {
        AST ast1 = knowledgeBase.get(index1);
        AST ast2 = knowledgeBase.get(index2);
        AST ast3 = knowledgeBase.get(index3);

        if (PropositionalLogicHelper.getOutermostOperation(ast1) == LogicalOperator.IMPLICATION &&
                PropositionalLogicHelper.getOutermostOperation(ast2) == LogicalOperator.IMPLICATION &&
                PropositionalLogicHelper.getOutermostOperation(ast3) == LogicalOperator.DISJUNCTION) {
            if (canApplyDestructiveDilemma(ast1, ast2, ast3, ast2.toString(), ast3.toString())) return true;
        }
        else if (PropositionalLogicHelper.getOutermostOperation(ast1) == LogicalOperator.IMPLICATION &&
                PropositionalLogicHelper.getOutermostOperation(ast2) == LogicalOperator.DISJUNCTION &&
                PropositionalLogicHelper.getOutermostOperation(ast3) == LogicalOperator.IMPLICATION) {
            if (canApplyDestructiveDilemma(ast1, ast3, ast2, ast2.toString(), ast3.toString())) return true;
        }
        else if (PropositionalLogicHelper.getOutermostOperation(ast1) == LogicalOperator.DISJUNCTION &&
                PropositionalLogicHelper.getOutermostOperation(ast2) == LogicalOperator.IMPLICATION &&
                PropositionalLogicHelper.getOutermostOperation(ast3) == LogicalOperator.IMPLICATION) {
            AST antecedent1 = ast2.getSubtree(0);
            AST conclusion1 = ast2.getSubtree(1);
            conclusion1.negate();

            AST antecedent2 = ast3.getSubtree(0);
            AST conclusion2 = ast3.getSubtree(1);
            conclusion2.negate();

            AST left = ast1.getSubtree(0);
            AST right = ast1.getSubtree(1);

            if (conclusion1.isEquivalentTo(left) && conclusion2.isEquivalentTo(right) ||
                    conclusion1.isEquivalentTo(right) && conclusion2.isEquivalentTo(left)) {
                antecedent1.negate();
                antecedent2.negate();

                AST newAST = new PropositionalAST(antecedent1 + " " + LogicalOperatorFlyweight.getDisjunctionString() + " " + antecedent2, true);
                KnowledgeBaseRegistry.addObtainedFrom(newAST.toString(), List.of(ast1.toString(), ast2.toString(), ast3.toString()), "Destructive Dilemma");
                if (!containsEntry(newAST)) knowledgeBase.add(newAST);
                return true;
            }
        }
        ErrorHelper.add("Cannot apply Destructive Dilemma on " + ast1 + ", " + ast2 + " and " + ast3 + "!");
        return false;
    }

    public boolean canApplyDestructiveDilemma(AST ast1, AST ast2, AST ast3, String string, String string2) {
        AST antecedent1 = ast1.getSubtree(0);
        AST conclusion1 = ast1.getSubtree(1);
        conclusion1.negate();

        AST antecedent2 = ast2.getSubtree(0);
        AST conclusion2 = ast2.getSubtree(1);
        conclusion2.negate();

        AST left = ast3.getSubtree(0);
        AST right = ast3.getSubtree(1);

        if (conclusion1.isEquivalentTo(left) && conclusion2.isEquivalentTo(right) ||
                conclusion1.isEquivalentTo(right) && conclusion2.isEquivalentTo(left)) {
            antecedent1.negate();
            antecedent2.negate();

            AST newAST = new PropositionalAST(antecedent1 + " " + LogicalOperatorFlyweight.getDisjunctionString() + " " + antecedent2, true);
            KnowledgeBaseRegistry.addObtainedFrom(newAST.toString(), List.of(ast1.toString(), string, string2), "Destructive Dilemma");
            if (!containsEntry(newAST)) knowledgeBase.add(newAST);
            return true;
        }
        return false;
    }

    public boolean handleAbsorption(int index) {
        AST ast = knowledgeBase.get(index);

        if (PropositionalLogicHelper.getOutermostOperation(ast) != LogicalOperator.IMPLICATION) {
            ErrorHelper.add("Cannot apply Absorption on " + ast + " !");
            return false;
        }

        AST antecedent = ast.getSubtree(0);
        AST conclusion = ast.getSubtree(1);
        AST newAST = new PropositionalAST(antecedent + " " + LogicalOperatorFlyweight.getImplicationString() + " ("
                + antecedent + " " + LogicalOperatorFlyweight.getConjunctionString() + " " + conclusion + ")", true);
        KnowledgeBaseRegistry.addObtainedFrom(newAST.toString(), List.of(ast.toString()), "Absorption");
        if (!containsEntry(newAST)) knowledgeBase.add(newAST);
        return true;
    }

    public boolean handleTransposition(int index) {
        AST ast = knowledgeBase.get(index);

        if (PropositionalLogicHelper.getOutermostOperation(ast) != LogicalOperator.IMPLICATION) {
            ErrorHelper.add("Cannot apply Transposition on " + ast + " !");
            return false;
        }

        AST antecedent = ast.getSubtree(0);
        AST conclusion = ast.getSubtree(1);
        antecedent.negate();
        conclusion.negate();

        AST newAST = new PropositionalAST(conclusion + " " + LogicalOperatorFlyweight.getImplicationString() + " " + antecedent, true);
        KnowledgeBaseRegistry.addObtainedFrom(newAST.toString(), List.of(ast.toString()), "Transposition");
        if (!containsEntry(newAST)) knowledgeBase.add(newAST);
        return true;
    }

    public boolean handleMaterialEquivalence(int index) {
        AST ast = knowledgeBase.get(index);

        if (PropositionalLogicHelper.getOutermostOperation(ast) != LogicalOperator.EQUIVALENCE) {
            ErrorHelper.add("Cannot apply Material Equivalence on " + ast + "!");
            return false;
        }

        AST left = ast.getSubtree(0);
        AST right = ast.getSubtree(1);
        AST newAST = new PropositionalAST("(" + left + " " + LogicalOperatorFlyweight.getImplicationString() + " " + right + ") " +
                LogicalOperatorFlyweight.getConjunctionString() + "(" + right + " " + LogicalOperatorFlyweight.getImplicationString() + " " + left + ")", true);
        KnowledgeBaseRegistry.addObtainedFrom(newAST.toString(), List.of(ast.toString()), "Material Equivalence");
        if (!containsEntry(newAST)) knowledgeBase.add(newAST);
        return true;
    }

    public boolean handleMaterialImplication(int index) {
        AST ast = knowledgeBase.get(index);

        if (PropositionalLogicHelper.getOutermostOperation(ast) != LogicalOperator.IMPLICATION) {
            ErrorHelper.add("Cannot apply Material Implication on " + ast + "!");
            return false;
        }

        AST antecedent = ast.getSubtree(0);
        AST conclusion = ast.getSubtree(1);
        antecedent.negate();
        AST newAST = new PropositionalAST(antecedent + " " + LogicalOperatorFlyweight.getDisjunctionString() + " " + conclusion, true);
        KnowledgeBaseRegistry.addObtainedFrom(newAST.toString(), List.of(ast.toString()), "Material Equivalence");
        if (!containsEntry(newAST)) knowledgeBase.add(newAST);
        return true;
    }

    public boolean handleImplicationIntroduction(int index1, int index2) {
        AST ast1 = knowledgeBase.get(index1);
        AST ast2 = knowledgeBase.get(index2);

        AST ast = new PropositionalAST(ast1 + " " + LogicalOperatorFlyweight.getImplicationString() + " " + ast2, true);
        KnowledgeBaseRegistry.addObtainedFrom(ast.toString(), List.of(ast1.toString(), ast2.toString()), "Implication Introduction");
        if (!containsEntry(ast)) knowledgeBase.add(ast);
        return true;
    }

    public boolean handleImplicationSimplification(int index) {
        AST ast = knowledgeBase.get(index);

        if (PropositionalLogicHelper.getOutermostOperation(ast) != LogicalOperator.IMPLICATION) {
            ErrorHelper.add("Cannot apply Implication Simplification on " + ast + "!");
            return false;
        }

        AST ast1 = ast.getSubtree(0);
        AST ast2 = ast.getSubtree(1);
        KnowledgeBaseRegistry.addObtainedFrom(ast1.toString(), List.of(ast.toString()), "Implication Simplification");
        KnowledgeBaseRegistry.addObtainedFrom(ast2.toString(), List.of(ast.toString()), "Implication Simplification");

        if (!containsEntry(ast1)) knowledgeBase.add(ast1);
        if (!containsEntry(ast2)) knowledgeBase.add(ast2);
        return true;
    }

    public boolean handleEquivalenceIntroduction(int index1, int index2) {
        AST ast1 = knowledgeBase.get(index1);
        AST ast2 = knowledgeBase.get(index2);

        if (PropositionalLogicHelper.getOutermostOperation(ast1) == LogicalOperator.IMPLICATION &&
                PropositionalLogicHelper.getOutermostOperation(ast2) == LogicalOperator.IMPLICATION) {
            AST antecedent1 = ast1.getSubtree(0);
            AST conclusion1 = ast1.getSubtree(1);

            AST antecedent2 = ast2.getSubtree(0);
            AST conclusion2 = ast2.getSubtree(1);

            if (antecedent1.isEquivalentTo(conclusion2) && antecedent2.isEquivalentTo(conclusion1)) {
                AST ast = new PropositionalAST(antecedent1 + " " + LogicalOperatorFlyweight.getEquivalenceString() + " " + conclusion1, true);
                KnowledgeBaseRegistry.addObtainedFrom(ast.toString(), List.of(ast1.toString(), ast2.toString()), "Equivalence Introduction");
                if (!containsEntry(ast)) knowledgeBase.add(ast);
                return true;
            }
        }
        ErrorHelper.add("Cannot apply Equivalence Simplification on " + ast1 + " and " + ast2 + "!");
        return false;
    }

    public boolean handleEquivalenceSimplification(int index) {
        AST ast = knowledgeBase.get(index);

        if (PropositionalLogicHelper.getOutermostOperation(ast) != LogicalOperator.EQUIVALENCE) {
            ErrorHelper.add("Cannot apply Equivalence Simplification on " + ast + "!");
            return false;
        }

        AST ast1 = ast.getSubtree(0);
        AST ast2 = ast.getSubtree(1);
        KnowledgeBaseRegistry.addObtainedFrom(ast1.toString(), List.of(ast.toString()), "Equivalence Simplification");
        KnowledgeBaseRegistry.addObtainedFrom(ast2.toString(), List.of(ast.toString()), "Equivalence Simplification");

        if (!containsEntry(ast1)) knowledgeBase.add(ast1);
        if (!containsEntry(ast2)) knowledgeBase.add(ast2);
        return true;
    }

    public boolean handleConjunctionIntroduction(List<Integer> indices) {
        StringBuilder builder = new StringBuilder();
        List<String> from = new ArrayList<>();

        int index = 0;
        while (index < indices.size()) {
            AST ast = knowledgeBase.get(index);
            if (index == indices.size() - 1) builder.append(ast);
            else builder.append(ast).append(" ").append(LogicalOperatorFlyweight.getConjunctionString()).append(" ");

            from.add(ast.toString());
            index++;
        }

        AST newAST = new PropositionalAST(builder.toString(), true);

        KnowledgeBaseRegistry.addObtainedFrom(newAST.toString(), from, "Conjunction Introduction");
        if (!containsEntry(newAST)) knowledgeBase.add(newAST);
        return true;
    }

    public boolean handleDisjunctionElimination(int index1, int index2) {
        AST ast1 = knowledgeBase.get(index1);
        AST ast2 = knowledgeBase.get(index2);

        if (PropositionalLogicHelper.getOutermostOperation(ast1) == LogicalOperator.DISJUNCTION) {
            AST left = ast1.getSubtree(0);
            AST right = ast1.getSubtree(1);

            AST negatedLeft = new PropositionalAST(left.toString(), true);
            AST negatedRight = new PropositionalAST(right.toString(), true);
            negatedLeft.negate();
            negatedRight.negate();

            if (negatedLeft.isEquivalentTo(ast2)) {
                KnowledgeBaseRegistry.addObtainedFrom(right.toString(), List.of(ast1.toString(), ast2.toString()), "Disjunction Elimination");
                if (!containsEntry(right)) knowledgeBase.add(right);
                return true;
            }
            else if (negatedRight.isEquivalentTo(ast2)) {
                KnowledgeBaseRegistry.addObtainedFrom(left.toString(), List.of(ast1.toString(), ast2.toString()), "Disjunction Elimination");
                if (!containsEntry(left)) knowledgeBase.add(left);
                return true;
            }
        }
        else if (PropositionalLogicHelper.getOutermostOperation(ast2) == LogicalOperator.DISJUNCTION) {
            AST left = ast2.getSubtree(0);
            AST right = ast2.getSubtree(1);

            AST negatedLeft = new PropositionalAST(left.toString(), true);
            AST negatedRight = new PropositionalAST(right.toString(), true);
            negatedLeft.negate();
            negatedRight.negate();

            if (negatedLeft.isEquivalentTo(ast1)) {
                KnowledgeBaseRegistry.addObtainedFrom(right.toString(), List.of(ast1.toString(), ast2.toString()), "Disjunction Elimination");
                if (!containsEntry(right)) knowledgeBase.add(right);
                return true;
            }
            else if (negatedRight.isEquivalentTo(ast1)) {
                KnowledgeBaseRegistry.addObtainedFrom(left.toString(), List.of(ast1.toString(), ast2.toString()), "Disjunction Elimination");
                if (!containsEntry(left)) knowledgeBase.add(left);
                return true;
            }
        }

        ErrorHelper.add("Cannot apply Disjunction Elimination on " + ast1 + " and " + ast2 + "!");
        return false;
    }

    public boolean handleConjunctionElimination(int index) {
        AST ast = knowledgeBase.get(index);

        if (PropositionalLogicHelper.getOutermostOperation(ast) != LogicalOperator.CONJUNCTION) {
            ErrorHelper.add("Cannot apply Conjunction Elimination on " + ast + "!");
            return false;
        }

        AST ast1 = ast.getSubtree(0);
        AST ast2 = ast.getSubtree(1);
        KnowledgeBaseRegistry.addObtainedFrom(ast1.toString(), List.of(ast.toString()), "Conjunction Elimination");
        KnowledgeBaseRegistry.addObtainedFrom(ast2.toString(), List.of(ast.toString()), "Conjunction Elimination");

        if (!containsEntry(ast1)) knowledgeBase.add(ast1);
        if (!containsEntry(ast2)) knowledgeBase.add(ast2);
        return true;
    }

    public boolean handleDisjunctionSimplification(int index) {
        AST ast = knowledgeBase.get(index);

        if (PropositionalLogicHelper.getOutermostOperation(ast) == LogicalOperator.DISJUNCTION) {
            AST left = ast.getSubtree(0);
            AST right = ast.getSubtree(1);

            if (left.isEquivalentTo(right)) {
                KnowledgeBaseRegistry.addObtainedFrom(left.toString(), List.of(ast.toString()), "Disjunction Simplification");
                if (!containsEntry(left)) knowledgeBase.add(left);
                return true;
            }
        }

        ErrorHelper.add("Cannot apply Disjunction Simplification on " + ast + "!");
        return false;
    }

    public boolean handleDeMorgan(int index) {
        AST copyAst = knowledgeBase.get(index);
        AST ast = new PropositionalAST(copyAst.toString(), true);

        if (PropositionalLogicHelper.getOutermostOperation(ast) == LogicalOperator.NEGATION) {
            ast.negate();
            if (PropositionalLogicHelper.getOutermostOperation(ast) == LogicalOperator.CONJUNCTION) {
                AST left = ast.getSubtree(0);
                AST right = ast.getSubtree(1);
                left.negate();
                right.negate();

                AST newAST = new PropositionalAST(left + " " + LogicalOperatorFlyweight.getDisjunctionString() + " " + right, true);
                KnowledgeBaseRegistry.addObtainedFrom(newAST.toString(), List.of(ast.toString()), "DeMorgan");
                if (!containsEntry(newAST)) knowledgeBase.add(newAST);
                return true;
            }
            else if (PropositionalLogicHelper.getOutermostOperation(ast) == LogicalOperator.DISJUNCTION) {
                AST left = ast.getSubtree(0);
                AST right = ast.getSubtree(1);
                left.negate();
                right.negate();

                AST newAST = new PropositionalAST(left + " " + LogicalOperatorFlyweight.getConjunctionString() + " " + right, true);
                KnowledgeBaseRegistry.addObtainedFrom(newAST.toString(), List.of(ast.toString()), "DeMorgan");
                if (!containsEntry(newAST)) knowledgeBase.add(newAST);
                return true;
            }
            else if (PropositionalLogicHelper.getOutermostOperation(ast) == LogicalOperator.IMPLICATION) {
                AST left = ast.getSubtree(0);
                AST right = ast.getSubtree(1);
                right.negate();

                AST newAST = new PropositionalAST(left + " " + LogicalOperatorFlyweight.getConjunctionString() + " " + right, true);
                KnowledgeBaseRegistry.addObtainedFrom(newAST.toString(), List.of(ast.toString()), "DeMorgan");
                if (!containsEntry(newAST)) knowledgeBase.add(newAST);
                return true;
            }
        }

        ErrorHelper.add("Cannot apply DeMorgan on " + ast + "!");
        return false;
    }

    public void addKBError(Command command) {
        if (command.getArity() == 1) ErrorHelper.add("Command '" + command + "' requires the argument to be from the Knowledge Base!");
        else ErrorHelper.add("Command '" + command + "' requires the arguments to be from the Knowledge Base!");
    }

    public void addGoalError(Command command) {
        if (command.getArity() == 1) ErrorHelper.add("Command '" + command + "' requires the argument to be from the Goals!");
        else ErrorHelper.add("Command '" + command + "' requires the arguments to be from the Goals!");
    }

    public void addOutOfBoundsError(int index) {
        if (knowledgeBase.size() == 1) ErrorHelper.add("Knowledge Base has " + knowledgeBase.size() + " entry, index " + (index + 1) + " is out of bounds!");
        else ErrorHelper.add("Knowledge Base has " + knowledgeBase.size() + " entries, index " + (index + 1) + " is out of bounds!");
    }

    public void addStateError(int index) {
        ErrorHelper.add("Cannot change to state " + index + ". State does not exist!");
    }

    public boolean containsEntry(AST entry) {
        for (AST ast : knowledgeBase) {
            if (ast.isEquivalentTo(entry)) return true;
        }
        return false;
    }
}

