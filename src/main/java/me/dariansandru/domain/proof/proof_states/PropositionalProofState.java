package me.dariansandru.domain.proof.proof_states;

import me.dariansandru.domain.data_structures.ast.ASTNode;
import me.dariansandru.domain.data_structures.ast.PropositionalASTNode;
import me.dariansandru.domain.language.LogicalOperator;
import me.dariansandru.domain.proof.ProofStep;
import me.dariansandru.domain.proof.Strategy;
import me.dariansandru.domain.proof.SubGoal;
import me.dariansandru.domain.proof.inference_rules.InferenceRule;
import me.dariansandru.domain.proof.inference_rules.propositional.ConjunctionIntroduction;
import me.dariansandru.domain.proof.inference_rules.propositional.ContradictionRule;
import me.dariansandru.domain.proof.inference_rules.propositional.DisjunctionElimination;
import me.dariansandru.domain.proof.inference_rules.propositional.PropositionalInferenceRule;
import me.dariansandru.domain.data_structures.ast.AST;
import me.dariansandru.domain.data_structures.ast.PropositionalAST;
import me.dariansandru.io.OutputDevice;
import me.dariansandru.utils.global.GlobalFlags;
import me.dariansandru.utils.global.GlobalTimer;
import me.dariansandru.utils.helper.KnowledgeBaseRegistry;
import me.dariansandru.utils.helper.ProofTextHelper;
import me.dariansandru.utils.helper.PropositionalLogicHelper;

import java.util.*;

public class PropositionalProofState implements ProofState {

    private final List<AST> knowledgeBase;
    private final List<String> knowledgeBaseStrings = new ArrayList<>();
    private final List<AST> goals;

    private final List<InferenceRule> inferenceRules;

    private final Set<String> appliedExpansionRules = new HashSet<>();
    private final Set<String> conjunctionInputs = new HashSet<>();

    private int currentConjunctionLevel = 0;
    private boolean levelCompleted = false;

    private final List<String> activeSubGoals = new ArrayList<>();
    private final List<AST> allSubGoals = new ArrayList<>();

    boolean isVisited = false;
    boolean childrenInConjunction = false;

    boolean isProven = false;
    boolean expanded = false;

    private ProofState parent;
    private final List<ProofState> children = new ArrayList<>();

    private int currentChildIndex = 0;
    private int stateIndex = 0;

    private final List<InferenceRule> expansionRules = new ArrayList<>();
    private final List<InferenceRule> derivationRules = new ArrayList<>();

    public PropositionalProofState(List<AST> knowledgeBase,
                                   List<AST> goals,
                                   List<InferenceRule> inferenceRules) {
        this.knowledgeBase = knowledgeBase;
        this.goals = goals;
        this.inferenceRules = inferenceRules;
        categoriseRules(inferenceRules);

        for (AST ast : knowledgeBase) {
            conjunctionInputs.add(ast.toString());
        }
    }

    private void categoriseRules(List<InferenceRule> rules) {
        for (InferenceRule rule : rules) {
            String ruleName = rule.name();
            if (isExpansionRule(ruleName)) expansionRules.add(rule);
            else derivationRules.add(rule);
        }
    }

    @Override
    public List<AST> getKnowledgeBase() {
        return knowledgeBase;
    }

    @Override
    public List<AST> getGoals() {
        return goals;
    }

    @Override
    public AST getGoal() {
        return goals.getFirst();
    }

    @Override
    public List<InferenceRule> getInferenceRules() {
        return inferenceRules;
    }

    @Override
    public void prove() {
        GlobalTimer.setStartTime();
        proofSetup();

        if (!children.isEmpty()) proveChildren();
        if (isProven) return;

        int expansionPass = 0;
        int maxExpansionPasses = 4;

        while (!isProven && expansionPass < maxExpansionPasses) {
            currentConjunctionLevel = ConjunctionIntroduction.LEVEL_ATOMS;
            levelCompleted = false;
            boolean changed = applyExpansionRules();

            if (changed) applyDerivationRules();
            if (containsGoal()) {
                printProof();
                this.isProven = true;
                GlobalTimer.setEndTime();
                break;
            }
            if (goals.getFirst().isContradiction() && containsContradiction()) {
                printProof();
                this.isProven = true;
                GlobalTimer.setEndTime();
                break;
            }
            SubGoal root = new SubGoal(goals.getFirst(), PropositionalInferenceRule.HYPOTHESIS, goals.getFirst());
            expandSubGoal(root);

            expansionPass++;
        }
    }

    private void printProof() {
        if (!GlobalFlags.executionFlag.equals("automated")) {
            ProofTextHelper.getProofText(goals.getFirst().toString());
            ProofTextHelper.buildFormalProof(goals.getFirst().toString());
            return;
        }

        OutputDevice.writeToConsole("");
        if (GlobalFlags.indentedProofFlag) {
            ProofTextHelper.getProofText(goals.getFirst().toString());
            ProofTextHelper.print();
            OutputDevice.writeToConsole("");
        }
        if (GlobalFlags.formalProofFlag) {
            ProofTextHelper.buildFormalProof(goals.getFirst().toString());
            ProofTextHelper.printFormalProof();
            OutputDevice.writeToConsole("");
        }
        isProven = true;
    }

    public String getIndentedProofString() {
        return ProofTextHelper.getProofString();
    }

    public String getFormalProofString() {
        return ProofTextHelper.getFormalProofString();
    }

    @Override
    public boolean simplify() {
        if (!(goals.getFirst() instanceof PropositionalAST goal)) return false;
        return goals.size() != 1 || !goal.isAtomic();
    }

    @Override
    public boolean isVisited() {
        return isVisited;
    }

    @Override
    public boolean areChildrenInConjunction() {
        return childrenInConjunction;
    }

    @Override
    public boolean isProven() {
        return isProven;
    }

    @Override
    public List<ProofState> getChildren() {
        return children;
    }

    @Override
    public void addChild(ProofState proofState) {
        children.add(proofState);
        proofState.addParent(this);
    }

    @Override
    public ProofState getParent() {
        return parent;
    }

    @Override
    public void addParent(ProofState proofState) {
        this.parent = proofState;
    }

    @Override
    public void setStateIndex(int index) {
        this.stateIndex = index;
    }

    @Override
    public void setProven() {
        this.isProven = true;
    }

    @Override
    public int getStateIndex() {
        return this.stateIndex;
    }

    private boolean applyExpansionRules() {
        boolean changed = false;

        for (InferenceRule rule : expansionRules) {
            String ruleName = rule.name();

            if (ruleName.equals("Conjunction Introduction")) {
                if (currentConjunctionLevel == ConjunctionIntroduction.LEVEL_ATOMS && !levelCompleted) {
                    List<AST> atoms = extractAtoms();
                    if (!atoms.isEmpty()) {
                        List<AST> derived = rule.inference(atoms, getGoal());
                        changed = addDerived(derived);
                        levelCompleted = true;
                    }
                }

                if (currentConjunctionLevel == ConjunctionIntroduction.LEVEL_SIMPLE_CONJUNCTIONS && !levelCompleted) {
                    List<AST> atoms = extractAtoms();
                    List<AST> simpleConjunctions = extractSimpleConjunctions();
                    List<AST> combined = new ArrayList<>();
                    combined.addAll(atoms);
                    combined.addAll(simpleConjunctions);

                    if (!combined.isEmpty()) {
                        List<AST> derived = rule.inference(combined, getGoal());
                        changed = addDerived(derived);
                        levelCompleted = true;
                    }
                }

                if (currentConjunctionLevel == ConjunctionIntroduction.LEVEL_ALL && !levelCompleted) {
                    List<AST> candidates = new ArrayList<>();
                    for (AST ast : knowledgeBase) {
                        String astStr = ast.toString();
                        if (!conjunctionInputs.contains(astStr)) {
                            candidates.add(ast);
                        }
                    }

                    if (!candidates.isEmpty()) {
                        List<AST> derived = rule.inference(candidates, getGoal());
                        changed = addDerived(derived);
                        for (AST ast : candidates) conjunctionInputs.add(ast.toString());
                        levelCompleted = true;
                    }
                }

                if (levelCompleted && currentConjunctionLevel < ConjunctionIntroduction.LEVEL_ALL) {
                    currentConjunctionLevel++;
                    levelCompleted = false;
                    return changed || applyExpansionRules();
                }
            }
            else {
                List<AST> derived = rule.inference(knowledgeBase, getGoal());
                changed = addDerived(derived) || changed;
            }
            appliedExpansionRules.add(ruleName);
        }
        return changed;
    }

    private boolean addDerived(List<AST> derived) {
        boolean changed = false;
        for (AST ast : derived) {
            String astStr = ast.toString();
            if (!knowledgeBaseStrings.contains(astStr)) {
                knowledgeBase.add(ast);
                knowledgeBaseStrings.add(astStr);

                conjunctionInputs.add(astStr);
                changed = true;
            }
        }
        return changed;
    }

    private List<AST> extractAtoms() {
        List<AST> atoms = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        for (AST ast : knowledgeBase) {
            String astStr = ast.toString();
            if (seen.contains(astStr)) continue;
            if (isAtom(ast)) {
                atoms.add(ast);
                seen.add(astStr);
            }
        }
        return atoms;
    }

    private List<AST> extractSimpleConjunctions() {
        List<AST> simple = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        for (AST ast : knowledgeBase) {
            String astStr = ast.toString();
            if (seen.contains(astStr)) continue;
            if (isSimpleConjunction(ast)) {
                simple.add(ast);
                seen.add(astStr);
            }
        }
        return simple;
    }

    private boolean isAtom(AST ast) {
        if (ast == null) return false;
        String str = ast.toString();
        return str.matches("^[A-Z][0-9]*$") || str.equals("Contradiction") || str.equals("Tautology");
    }

    private boolean isSimpleConjunction(AST ast) {
        ConjunctionIntroduction conjunctionIntroduction = new ConjunctionIntroduction();
        return conjunctionIntroduction.isSimpleConjunction(ast);
    }

    private void applyDerivationRules() {
        for (InferenceRule rule : derivationRules) {
            List<AST> derived = rule.inference(knowledgeBase, getGoal());
            for (AST ast : derived) {
                String astStr = ast.toString();
                if (!knowledgeBaseStrings.contains(astStr)) {
                    knowledgeBase.add(ast);
                    knowledgeBaseStrings.add(astStr);
                }
            }
        }
    }

    private void proofSetup() {
        if (!knowledgeBase.isEmpty()) {
            for (AST ast : knowledgeBase) {
                String astString = ast.toString();
                if (!knowledgeBaseStrings.contains(astString)) knowledgeBaseStrings.add(astString);
                conjunctionInputs.add(astString);
            }
        }
        for (AST ast : knowledgeBase) {
            if (KnowledgeBaseRegistry.isStrategy(ast.toString())) continue;
            KnowledgeBaseRegistry.addObtainedFrom(ast.toString(), "Hypothesis");
        }
        for (AST ast : goals) {
            if (KnowledgeBaseRegistry.isStrategy(ast.toString())) continue;
            KnowledgeBaseRegistry.addObtainedFrom(ast.toString(), "Hypothesis");
        }
    }

    private void expandSubGoal(SubGoal subGoal) {
        if (subGoal.getGoal().isContradiction()) return;

        if (activeSubGoals.contains(subGoal.getGoal().toString())) return;
        activeSubGoals.add(subGoal.getGoal().toString());

        if (containsGoalDirectly(subGoal.getGoal())) {
            this.isProven = true;
            ProofTextHelper.getProofText(subGoal);
            return;
        }

        if (containsSubGoal(subGoal)) {
            this.isProven = true;
            if (addSubGoalToKnowledgeBase(subGoal)) return;
            ProofTextHelper.getProofText(subGoal);
            return;
        }

        AST ast = subGoal.getGoal();
        allSubGoals.add(ast);
        while (subGoal.hasMoreOtherGoals()) {
            AST otherGoal = subGoal.getCurrentOtherGoal();
            processGoal(subGoal, otherGoal);
            subGoal.incrementOtherGoalIndex();
            if (!isProven) return;
        }
    }

    private boolean containsGoalDirectly(AST goal) {
        String goalStr = goal.toString();
        return knowledgeBaseStrings.contains(goalStr);
    }

    private void processGoal(SubGoal parent, AST goal) {
        if (goal.isContradiction()) {
            ContradictionRule rule = new ContradictionRule();
            if (processSubGoals(parent, rule.getSubGoals(knowledgeBase, goal))) return;
        }

        for (InferenceRule rule : inferenceRules) {
            DisjunctionElimination disjunctionElimination = new DisjunctionElimination();
            if (Objects.equals(rule.name(), disjunctionElimination.name())) {
                if (processSubGoals(parent, rule.getSubGoals(knowledgeBase, goals.getFirst()))) return;
            }
            if (processSubGoals(parent, rule.getSubGoals(knowledgeBase, goal))) return;
        }
    }

    private boolean processSubGoals(SubGoal subGoal, List<SubGoal> subGoals) {
        if (!subGoals.isEmpty()) {
            subGoal.addChildren(subGoals);

            for (SubGoal child : subGoals) {
                expandSubGoal(child);
                if (isProven) return true;
            }
        }
        return false;
    }

    private void proveChildren() {
        this.isProven = childrenInConjunction;

        while (currentChildIndex < children.size()) {
            ProofState child = children.get(currentChildIndex);
            if (!child.isProven()) child.prove();

            if (childrenInConjunction) {
                if (!child.isProven()) {
                    this.isProven = false;
                    return;
                }
            }
            else {
                if (child.isProven()) {
                    this.isProven = true;
                    return;
                }
            }
            currentChildIndex++;
        }
    }

    public List<Strategy> notifyProof() {
        List<Strategy> strategies = new ArrayList<>();
        if (isProven) return List.of(Strategy.NO_STRATEGY);
        if (expanded) return List.of(Strategy.NO_STRATEGY);

        expanded = true;
        if (PropositionalLogicHelper.getOutermostOperation(this.goals.getFirst()) == LogicalOperator.NEGATION) {
            this.childrenInConjunction = true;
            strategies.add(Strategy.NEGATION_STRATEGY);
            return strategies;
        }
        if (!simplify()) return List.of(Strategy.NO_STRATEGY);

        if (this.goals.size() != 1) {
            this.childrenInConjunction = true;
            strategies.add(Strategy.CONJUNCTION_STRATEGY);
        }

        for (AST ast : knowledgeBase) {
            if (PropositionalLogicHelper.getOutermostOperation(ast) == LogicalOperator.DISJUNCTION) {
                strategies.add(Strategy.PROOF_BY_CASES);
            }
        }

        switch (PropositionalLogicHelper.getOutermostOperation(this.goals.getFirst())) {
            case IMPLICATION -> {
                this.childrenInConjunction = true;
                strategies.add(Strategy.IMPLICATION_STRATEGY);
            }
            case EQUIVALENCE -> {
                this.childrenInConjunction = true;
                strategies.add(Strategy.EQUIVALENCE_STRATEGY);
            }
            case CONJUNCTION -> {
                this.childrenInConjunction = true;
                strategies.add(Strategy.CONJUNCTION_STRATEGY);
            }
            case DISJUNCTION -> {
                this.childrenInConjunction = false;
                strategies.add(Strategy.DISJUNCTION_STRATEGY);
            }
            case NEGATION -> {
                this.childrenInConjunction = true;
                strategies.add(Strategy.NEGATION_STRATEGY);
            }
            default -> {
                this.childrenInConjunction = false;
                return List.of(Strategy.NO_STRATEGY);
            }
        }
        return strategies;
    }

    private boolean containsGoal() {
        for (AST ast : knowledgeBase) {
            if (ast.isEquivalentTo(goals.getFirst())) {
                if (!ast.toString().equals(goals.getFirst().toString())) KnowledgeBaseRegistry.addEntry(
                        goals.getFirst().toString(), "Since " + ast + " is equivalent to the goal (" + goals.getFirst() + "), we derive the goal",
                        List.of(ast.toString()));
                return true;
            }
        }
        return false;
    }

    private boolean containsContradiction() {
        for (AST ast : knowledgeBase) {
            if (ast.isContradiction()) {
                KnowledgeBaseRegistry.addEntry("Contradiction", "We derive a contradiction from " + ast,
                        List.of(ast.toString()));
            }
        }

        for (int i = 0; i < knowledgeBase.size(); i++) {
            for (int j = i + 1; j < knowledgeBase.size(); j++) {
                AST first = knowledgeBase.get(i);
                AST second = knowledgeBase.get(j);
                if (isNegationOf(first, second)) {
                    KnowledgeBaseRegistry.addEntry("Contradiction", "We derive a contradiction from " + first + " and " + second,
                            List.of(first.toString(), second.toString()));
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isNegationOf(AST first, AST second) {
        if (first == null || second == null) return false;

        try {
            PropositionalAST negatedFirst = new PropositionalAST(first.toString(), true);
            negatedFirst.negate();

            PropositionalAST negatedSecond = new PropositionalAST(second.toString(), true);
            negatedSecond.negate();

            return first.isEquivalentTo(negatedSecond) || second.isEquivalentTo(negatedFirst);
        }
        catch (Exception e) {
            return false;
        }
    }

    private boolean containsSubGoal(SubGoal subGoal) {
        String goalStr = subGoal.getGoal().toString();
        if (knowledgeBaseStrings.contains(goalStr)) return true;

        for (AST ast : knowledgeBase) {
            if (subGoal.getOtherGoals() != null) {
                for (AST other : subGoal.getOtherGoals()) {
                    String otherStr = other.toString();
                    if (knowledgeBaseStrings.contains(otherStr)) return true;
                    try {
                        if (ast.isEquivalentTo(other)) return true;
                    }
                    catch (Exception ignored) {
                    }
                }
            }
            try {
                if (ast.isEquivalentTo(subGoal.getGoal())) return true;
            }
            catch (Exception ignored) {
            }
        }
        return false;
    }

    private boolean containsConjunctionContradiction() {
        for (AST ast : knowledgeBase) {
            if (PropositionalLogicHelper.getOutermostOperation(ast) == LogicalOperator.CONJUNCTION) {
                try {
                    PropositionalAST left = (PropositionalAST) ast.getSubtree(0);
                    PropositionalAST right = (PropositionalAST) ast.getSubtree(1);

                    PropositionalAST negatedLeft = new PropositionalAST(left.toString(), true);
                    PropositionalAST negatedRight = new PropositionalAST(right.toString(), true);
                    negatedLeft.negate();
                    negatedRight.negate();

                    if (left.isEquivalentTo(negatedRight) && right.isEquivalentTo(negatedLeft)) {
                        ProofTextHelper.getProofTextContradiction(ast);
                        return true;
                    }
                } catch (Exception ignored) {
                }
            }
        }
        return false;
    }

    private boolean addSubGoalToKnowledgeBase(SubGoal subGoal) {
        if (subGoal == null) return false;

        SubGoal current = subGoal;
        while (current != null && current.getParent() != null) {
            if ("Contradiction".equals(current.getParent().getGoal().toString())) break;
            current = current.getParent();
        }
        if (current == null || current.getParent() == null) return false;

        for (InferenceRule inferenceRule : inferenceRules) {
            List<AST> potentialEntries = inferenceRule.inference(knowledgeBase, current.getGoal());

            for (AST ast : potentialEntries) {
                String astStr = ast.toString();
                if (!knowledgeBaseStrings.contains(astStr)) {
                    knowledgeBase.add(ast);
                    knowledgeBaseStrings.add(astStr);
                }
            }
        }
        return containsConjunctionContradiction();
    }

    private boolean isExpansionRule(String ruleName) {
        return ruleName.equals("Conjunction Elimination") ||
                ruleName.equals("Conjunction Introduction") ||
                ruleName.equals("Disjunction Elimination") ||
                ruleName.equals("Disjunction Introduction") ||
                ruleName.equals("Material Implication") ||
                ruleName.equals("Material Equivalence") ||
                ruleName.equals("De Morgan") ||
                ruleName.equals("Transposition") ||
                ruleName.equals("Proof by Cases") ||
                ruleName.equals("Disjunction Simplification") ||
                ruleName.equals("Absorption") ||
                ruleName.equals("Equivalence Introduction") ||
                ruleName.equals("Equivalence Elimination") ||
                ruleName.equals("Implication Introduction") ||
                ruleName.equals("Implication Simplification");
    }

    public void setUnproven() {
        this.isProven = false;
    }
}