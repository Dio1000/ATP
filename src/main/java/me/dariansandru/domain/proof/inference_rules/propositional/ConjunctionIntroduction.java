package me.dariansandru.domain.proof.inference_rules.propositional;

import me.dariansandru.domain.data_structures.ast.ASTNode;
import me.dariansandru.domain.data_structures.ast.PropositionalASTNode;
import me.dariansandru.domain.language.LogicalOperator;
import me.dariansandru.domain.proof.SubGoal;
import me.dariansandru.domain.proof.inference_rules.InferenceRule;
import me.dariansandru.domain.data_structures.ast.AST;
import me.dariansandru.domain.data_structures.ast.PropositionalAST;
import me.dariansandru.utils.flyweight.LogicalOperatorFlyweight;
import me.dariansandru.utils.helper.KnowledgeBaseRegistry;
import me.dariansandru.utils.helper.PropositionalLogicHelper;

import java.util.*;

public class ConjunctionIntroduction implements InferenceRule {

    private final List<AST> derived = new ArrayList<>();
    private final Set<String> usedCombinations = new HashSet<>();
    private final Set<String> derivedStrings = new HashSet<>();
    private final Set<String> existingFormulas = new HashSet<>();

    public static final int LEVEL_ATOMS = 0;
    public static final int LEVEL_SIMPLE_CONJUNCTIONS = 1;
    public static final int LEVEL_ALL = 2;

    private final Set<String> allAtoms = new HashSet<>();
    private List<AST> atomFormulas = new ArrayList<>();
    private List<AST> simpleConjunctionFormulas = new ArrayList<>();

    @Override
    public String name() {
        return "Conjunction Introduction";
    }

    @Override
    public boolean canInference(List<AST> asts, AST goal) {
        derived.clear();
        usedCombinations.clear();
        derivedStrings.clear();
        allAtoms.clear();
        atomFormulas.clear();
        simpleConjunctionFormulas.clear();
        existingFormulas.clear();

        for (AST ast : asts) {
            existingFormulas.add(ast.toString());
            allAtoms.addAll(PropositionalLogicHelper.getAtoms(ast));
        }

        for (AST ast : asts) {
            if (isAtom(ast)) atomFormulas.add(ast);
            else if (isSimpleConjunction(ast)) simpleConjunctionFormulas.add(ast);
        }

        Set<String> seenAtoms = new HashSet<>();
        List<AST> uniqueAtoms = new ArrayList<>();
        for (AST ast : atomFormulas) {
            String string = ast.toString();
            if (!seenAtoms.contains(string)) {
                uniqueAtoms.add(ast);
                seenAtoms.add(string);
            }
        }
        atomFormulas = uniqueAtoms;

        Set<String> seenSimple = new HashSet<>();
        List<AST> uniqueSimple = new ArrayList<>();
        for (AST ast : simpleConjunctionFormulas) {
            String string = ast.toString();
            if (!seenSimple.contains(string)) {
                uniqueSimple.add(ast);
                seenSimple.add(string);
            }
        }
        simpleConjunctionFormulas = uniqueSimple;

        if (!atomFormulas.isEmpty()) combineFormulas(atomFormulas);
        if (!derived.isEmpty()) return true;

        if (!simpleConjunctionFormulas.isEmpty()) {
            List<AST> combined = new ArrayList<>();
            combined.addAll(atomFormulas);
            combined.addAll(simpleConjunctionFormulas);
            combineFormulas(combined);
        }
        if (!derived.isEmpty()) return true;

        combineFormulas(asts);
        return !derived.isEmpty();
    }

    private boolean isAtom(AST ast) {
        if (ast == null) return false;
        String string = ast.toString();
        return string.matches("^[A-Z][0-9]*$") || string.equals("Contradiction") || string.equals("Tautology");
    }

    public boolean isSimpleConjunction(AST ast) {
        if (!(ast instanceof PropositionalAST pAst)) return false;

        LogicalOperator op = PropositionalLogicHelper.getOutermostOperation(pAst);
        if (op != LogicalOperator.CONJUNCTION) return false;

        PropositionalASTNode root = (PropositionalASTNode) pAst.getRoot();
        for (ASTNode child : root.getChildren()) {
            PropositionalAST childAst = new PropositionalAST((PropositionalASTNode) child);
            if (!isAtom(childAst)) return false;
        }
        return true;
    }

    private void combineFormulas(List<AST> formulas) {
        for (int i = 0; i < formulas.size(); i++) {
            for (int j = 0; j < formulas.size(); j++) {
                if (i == j) continue;

                AST first = formulas.get(i);
                AST second = formulas.get(j);
                if (isRedundantConjunction(first, second)) continue;

                String combinationKey = generateCombinationKey(first, second);
                if (usedCombinations.contains(combinationKey)) continue;

                PropositionalAST conjunction = PropositionalLogicHelper.buildFormula((PropositionalAST) first, (PropositionalAST) second,
                        LogicalOperatorFlyweight.getConjunctionString());
                String conjunctionString = conjunction.toString();

                if (existingFormulas.contains(conjunctionString) || derivedStrings.contains(conjunctionString)) continue;
                KnowledgeBaseRegistry.addEntry(conjunctionString, "From " + first + " and " + second + ", by " + name() + ", we derive " + conjunction,
                        List.of(first.toString(), second.toString()));

                derived.add(conjunction);
                derivedStrings.add(conjunctionString);
                usedCombinations.add(combinationKey);
            }
        }
    }

    private boolean isRedundantConjunction(AST first, AST second) {
        if (isConjunction(first)) {
            Set<String> firstAtoms = PropositionalLogicHelper.getAtoms(first);
            Set<String> secondAtoms = PropositionalLogicHelper.getAtoms(second);
            if (firstAtoms.containsAll(secondAtoms)) return true;
        }
        if (isConjunction(second)) {
            Set<String> firstAtoms = PropositionalLogicHelper.getAtoms(first);
            Set<String> secondAtoms = PropositionalLogicHelper.getAtoms(second);
            return secondAtoms.containsAll(firstAtoms);
        }
        return false;
    }

    private boolean isConjunction(AST ast) {
        if (!(ast instanceof PropositionalAST pAst)) return false;
        return PropositionalLogicHelper.getOutermostOperation(pAst) == LogicalOperator.CONJUNCTION;
    }

    @Override
    public List<AST> inference(List<AST> asts, AST goal) {
        derived.clear();
        usedCombinations.clear();
        derivedStrings.clear();
        if (canInference(asts, goal)) return derived;
        return new ArrayList<>();
    }

    @Override
    public List<SubGoal> getSubGoals(List<AST> knowledgeBase, AST... asts) {
        if (asts.length != 1) return List.of();

        AST goal = asts[0];
        if (!(goal instanceof PropositionalAST propositionalGoal)) return List.of();

        LogicalOperator outermostOperation = PropositionalLogicHelper.getOutermostOperation(propositionalGoal);
        if (!Objects.equals(outermostOperation.toString(), LogicalOperatorFlyweight.getConjunctionString())) return List.of();

        List<SubGoal> subGoals = new ArrayList<>();
        AST left = propositionalGoal.getSubtree(0);
        AST right = propositionalGoal.getSubtree(1);

        subGoals.add(new SubGoal(left, PropositionalInferenceRule.CONJUNCTION_INTRODUCTION, goal));
        subGoals.add(new SubGoal(right, PropositionalInferenceRule.CONJUNCTION_INTRODUCTION, goal));
        return subGoals;
    }

    @Override
    public String getText(SubGoal subGoal) {
        return "To prove " + subGoal.getFormula() + ", we need to prove " + subGoal.getGoal();
    }

    private String generateCombinationKey(AST first, AST second) {
        return first.toString() + "|" + second.toString();
    }
}