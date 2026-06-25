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

        for (AST ast : asts) {
            allAtoms.addAll(PropositionalLogicHelper.getAtoms(ast));
        }

        for (AST ast : asts) {
            if (isAtom(ast)) {
                atomFormulas.add(ast);
            }
            else if (isSimpleConjunction(ast)) {
                simpleConjunctionFormulas.add(ast);
            }
        }

        Set<String> seenAtoms = new HashSet<>();
        List<AST> uniqueAtoms = new ArrayList<>();
        for (AST ast : atomFormulas) {
            String str = ast.toString();
            if (!seenAtoms.contains(str)) {
                uniqueAtoms.add(ast);
                seenAtoms.add(str);
            }
        }
        atomFormulas = uniqueAtoms;

        Set<String> seenSimple = new HashSet<>();
        List<AST> uniqueSimple = new ArrayList<>();
        for (AST ast : simpleConjunctionFormulas) {
            String str = ast.toString();
            if (!seenSimple.contains(str)) {
                uniqueSimple.add(ast);
                seenSimple.add(str);
            }
        }
        simpleConjunctionFormulas = uniqueSimple;

        Set<String> goalAtoms = goal != null ? PropositionalLogicHelper.getAtoms(goal) : new HashSet<>();

        if (!atomFormulas.isEmpty()) {
            combineFormulas(atomFormulas, goalAtoms);
        }
        if (!derived.isEmpty()) {
            return true;
        }

        if (!simpleConjunctionFormulas.isEmpty()) {
            List<AST> combined = new ArrayList<>();
            combined.addAll(atomFormulas);
            combined.addAll(simpleConjunctionFormulas);
            combineFormulas(combined, goalAtoms);
        }

        if (!derived.isEmpty()) {
            return true;
        }
        combineFormulas(asts, goalAtoms);

        return !derived.isEmpty();
    }

    private boolean isAtom(AST ast) {
        if (ast == null) return false;
        String str = ast.toString();
        return str.matches("^[A-Z][0-9]*$") ||
                str.equals("Contradiction") ||
                str.equals("Tautology");
    }

    private boolean isSimpleConjunction(AST ast) {
        if (ast == null || !(ast instanceof PropositionalAST pAst)) return false;

        LogicalOperator op = PropositionalLogicHelper.getOutermostOperation(pAst);
        if (op != LogicalOperator.CONJUNCTION) return false;

        PropositionalASTNode root = (PropositionalASTNode) pAst.getRoot();
        for (ASTNode child : root.getChildren()) {
            PropositionalAST childAst = new PropositionalAST((PropositionalASTNode) child);
            if (!isAtom(childAst)) return false;
        }
        return true;
    }

    private void combineFormulas(List<AST> formulas, Set<String> goalAtoms) {
        for (int i = 0; i < formulas.size(); i++) {
            for (int j = 0; j < formulas.size(); j++) {
                if (i == j) continue;

                AST first = formulas.get(i);
                AST second = formulas.get(j);

                if (isRedundantConjunction(first, second)) continue;

                String combinationKey = generateCombinationKey(first, second);
                if (usedCombinations.contains(combinationKey)) continue;

                if (!goalAtoms.isEmpty()) {
                    Set<String> firstAtoms = PropositionalLogicHelper.getAtoms(first);
                    Set<String> secondAtoms = PropositionalLogicHelper.getAtoms(second);
                    boolean firstRelevant = firstAtoms.stream().anyMatch(goalAtoms::contains);
                    boolean secondRelevant = secondAtoms.stream().anyMatch(goalAtoms::contains);
                    if (!firstRelevant && !secondRelevant) continue;
                }

                PropositionalAST conjunction = PropositionalLogicHelper.buildFormula(
                        (PropositionalAST) first,
                        (PropositionalAST) second,
                        LogicalOperatorFlyweight.getConjunctionString()
                );

                String conjStr = conjunction.toString();
                if (derivedStrings.contains(conjStr)) continue;

                KnowledgeBaseRegistry.addEntry(
                        conjStr,
                        "From " + first + " and " + second + ", by " + name() + ", we derive " + conjunction,
                        List.of(first.toString(), second.toString())
                );

                derived.add(conjunction);
                derivedStrings.add(conjStr);
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
        if (!(goal instanceof PropositionalAST pGoal)) return List.of();

        LogicalOperator outermostOp = PropositionalLogicHelper.getOutermostOperation(pGoal);
        if (!Objects.equals(outermostOp.toString(), LogicalOperatorFlyweight.getConjunctionString())) {
            return List.of();
        }

        List<SubGoal> subGoals = new ArrayList<>();
        AST left = pGoal.getSubtree(0);
        AST right = pGoal.getSubtree(1);

        subGoals.add(new SubGoal(left, PropositionalInferenceRule.CONJUNCTION_INTRODUCTION, goal));
        subGoals.add(new SubGoal(right, PropositionalInferenceRule.CONJUNCTION_INTRODUCTION, goal));

        return subGoals;
    }

    @Override
    public String getText(SubGoal subGoal) {
        return "To prove " + subGoal.getFormula() + ", we need to prove " + subGoal.getGoal();
    }

    private String generateCombinationKey(AST first, AST second) {
        String s1 = first.toString();
        String s2 = second.toString();
        if (s1.compareTo(s2) <= 0) {
            return s1 + "|" + s2;
        }
        else {
            return s2 + "|" + s1;
        }
    }
}