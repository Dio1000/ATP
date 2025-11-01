package me.dariansandru.domain.proof.inference_rules;

import me.dariansandru.domain.data_structures.ast.*;
import me.dariansandru.domain.language.LogicalOperator;
import me.dariansandru.domain.language.predicate.Predicate;
import me.dariansandru.domain.proof.SubGoal;
import me.dariansandru.domain.proof.helper.Direction;
import me.dariansandru.domain.proof.helper.Path;
import me.dariansandru.utils.helper.PropositionalLogicHelper;

import java.util.*;

public class CustomPropositionalInferenceRule implements InferenceRule {

    private final String name;
    private final List<AST> antecedents;
    private final AST conclusion;

    private final List<Map<String, Path>> pathToAtomList = new ArrayList<>();
    private final Set<String> atoms = new HashSet<>();
    private final Map<String, String> atomVariableMap = new HashMap<>();

    private final List<List<AST>> usedEntries = new ArrayList<>();
    private final List<AST> derived = new ArrayList<>();

    public CustomPropositionalInferenceRule(String name, List<AST> antecedents, AST conclusion) {
        this.name = name;
        this.antecedents = antecedents;
        this.conclusion = conclusion;
    }

    public String name() {
        return name;
    }

    public List<AST> antecedents() {
        return antecedents;
    }

    public AST conclusion() {
        return conclusion;
    }

    @Override
    public boolean canInference(List<AST> kb, AST goal) {
        resetState();

        for (AST antecedent : antecedents) {
            atoms.addAll(PropositionalLogicHelper.getAtoms(antecedent));
        }
        createPathToAtomsMap();

        Map<Integer, List<Integer>> matches = new HashMap<>();
        for (int i = 0; i < antecedents.size(); i++) {
            for (int j = 0; j < kb.size(); j++) {
                if (antecedents.get(i).hasSameStructure(kb.get(j))) {
                    matches.computeIfAbsent(i, k -> new ArrayList<>()).add(j);
                }
            }
        }

        int[] matchedTo = new int[kb.size()];
        Arrays.fill(matchedTo, -1);
        if (!canMatchAll(matches, antecedents.size(), kb.size(), matchedTo)) return false;

        boolean found = false;
        List<List<AST>> potentialEntries = new ArrayList<>();

        int[] currentAssignment = new int[antecedents.size()];
        Arrays.fill(currentAssignment, -1);
        boolean[] usedKb = new boolean[kb.size()];

        generateUniqueAssignments(matches, 0, currentAssignment, usedKb, kb, potentialEntries);
        for (List<AST> entry : potentialEntries) {
            entry.sort(Comparator.comparingInt(AST::getLength));
        }

        for (List<AST> entry : potentialEntries) {
            if (usedEntries.contains(entry)) continue;
            if (!found && isInferenceRuleApplicable(entry)) {
                found = true;
                usedEntries.add(entry);
            }
        }

        return found;
    }

    private boolean isInferenceRuleApplicable(List<AST> kbSubset) {
        if (kbSubset.size() < antecedents.size()) return false;
        return checkVariablesConsistent(kbSubset);
    }

    private boolean checkVariablesConsistent(List<AST> kbSubset) {
        atomVariableMap.clear();
        for (String atom : atoms) {
            atomVariableMap.put(atom, null);
        }

        for (int i = 0; i < kbSubset.size(); i++) {
            AST candidate = kbSubset.get(i);
            Map<String, Path> atomPath = pathToAtomList.get(i);

            for (String key : atomPath.keySet()) {
                PropositionalAST variableAST = getVariable(candidate, atomPath.get(key));
                if (variableAST == null) return false;

                String variableASTString = Objects.requireNonNull(variableAST).toString();
                if (atomVariableMap.get(key) == null) atomVariableMap.put(key, variableASTString);
                else if (!atomVariableMap.get(key).equals(variableASTString)) return false;
            }
        }

        for (String key : atomVariableMap.keySet()) {
            if (atomVariableMap.get(key) == null) return false;
        }
        return true;
    }

    private PropositionalAST getVariable(AST ast, Path path) {
        PropositionalASTNode node = (PropositionalASTNode) ast.getRoot();
        for (Direction direction : path.directions()) {
            if (node.getKey() instanceof Predicate predicate) {
                if (PropositionalLogicHelper.getLogicalOperator(predicate) != direction.operator()) {
                    return null;
                }
                node = (PropositionalASTNode) node.getChildren().get(direction.child());
            }
        }
        return new PropositionalAST(node);
    }

    public AST buildConclusion() {
        Map<String, Path> conclusionAtomPath = buildPathMap(conclusion);
        PropositionalAST conclusionAST = new PropositionalAST(conclusion.toString(), true);
        Set<String> conclusionAtoms = PropositionalLogicHelper.getAtoms(conclusionAST);

        for (String atom : conclusionAtoms) {
            PropositionalASTNode node = (PropositionalASTNode) conclusionAST.getRoot();
            Path path = conclusionAtomPath.get(atom);

            PropositionalASTNode parent = null;
            int childIndex = -1;

            for (Direction direction : path.directions()) {
                if (!(node.getKey() instanceof Predicate predicate)) break;

                LogicalOperator operator = PropositionalLogicHelper.getLogicalOperator(predicate);
                if (operator != direction.operator()) {
                    return null;
                }

                parent = node;
                childIndex = direction.child();
                node = (PropositionalASTNode) node.getChildren().get(childIndex);
            }

            if (parent != null) {
                PropositionalAST replacementAST = new PropositionalAST(atomVariableMap.get(atom), true);
                parent.getChildren().set(childIndex, (PropositionalASTNode) replacementAST.getRoot());
            }
            else {
                conclusionAST = new PropositionalAST(atomVariableMap.get(atom), true);
            }
        }

        return conclusionAST;
    }

    @Override
    public List<AST> inference(List<AST> kb, AST goal) {
        try {
            if (canInference(kb, goal)) {
                AST result = buildConclusion();
                return result != null ? List.of(result) : List.of();
            }
            return List.of();
        }
        finally {
            resetState();
        }
    }

    @Override
    public List<SubGoal> getSubGoals(List<AST> knowledgeBase, AST... asts) {
        return List.of();
    }

    @Override
    public String getText(SubGoal subGoal) {
        return "";
    }

    private boolean canMatchAll(Map<Integer, List<Integer>> matches, int antecedentSize, int kbSize, int[] matchedTo) {
        boolean[] visited = new boolean[kbSize];
        for (int i = 0; i < antecedentSize; i++) {
            Arrays.fill(visited, false);
            if (!DFSMatch(i, matches, visited, matchedTo)) return false;
        }
        return true;
    }

    private boolean DFSMatch(int index, Map<Integer, List<Integer>> matches, boolean[] visited, int[] matchedTo) {
        if (!matches.containsKey(index)) return false;
        for (int idx : matches.get(index)) {
            if (visited[idx]) continue;
            visited[idx] = true;

            if (matchedTo[idx] == -1 || DFSMatch(matchedTo[idx], matches, visited, matchedTo)) {
                matchedTo[idx] = index;
                return true;
            }
        }
        return false;
    }

    private void generateUniqueAssignments(Map<Integer, List<Integer>> matches, int antecedentIndex,
                                           int[] currentAssignment, boolean[] usedKb, List<AST> kb,
                                           List<List<AST>> result) {
        if (antecedentIndex == currentAssignment.length) {
            List<AST> assignment = new ArrayList<>();
            for (int index : currentAssignment) assignment.add(kb.get(index));

            boolean duplicate = result.stream().anyMatch(list -> {
                Set<AST> set1 = new HashSet<>(list);
                Set<AST> set2 = new HashSet<>(assignment);
                return set1.equals(set2);
            });
            if (!duplicate) result.add(assignment);
            return;
        }

        if (!matches.containsKey(antecedentIndex)) return;
        for (int index : matches.get(antecedentIndex)) {
            if (usedKb[index]) continue;

            currentAssignment[antecedentIndex] = index;
            usedKb[index] = true;
            generateUniqueAssignments(matches, antecedentIndex + 1, currentAssignment, usedKb, kb, result);
            usedKb[index] = false;
            currentAssignment[antecedentIndex] = -1;
        }
    }

    public void createPathToAtomsMap() {
        pathToAtomList.clear();
        for (AST antecedent : antecedents) {
            pathToAtomList.add(buildPathMap(antecedent));
        }
    }

    public Map<String, Path> buildPathMap(AST ast) {
        PropositionalASTNode root = (PropositionalASTNode) ast.getRoot();
        if (root.getChildren().isEmpty()) {
            Map<String, Path> map = new HashMap<>();
            map.put(ast.toString(), new Path(List.of()));
            return map;
        }
        return getAllPaths(root);
    }

    public static Map<String, Path> getAllPaths(PropositionalASTNode root) {
        Map<String, Path> pathMap = new LinkedHashMap<>();
        collectPathsToAtoms(root, new ArrayList<>(), pathMap);
        return pathMap;
    }

    private static void collectPathsToAtoms(PropositionalASTNode node,
                                            List<Direction> currentPath,
                                            Map<String, Path> pathMap) {
        if (node == null) return;
        if (node.getChildren().isEmpty() && node.getKey() != null) {
            String atom = node.getKey().toString();
            pathMap.put(atom, new Path(new ArrayList<>(currentPath)));
        }

        List<ASTNode> children = node.getChildren();
        for (int index = 0; index < children.size(); index++) {
            PropositionalASTNode child = (PropositionalASTNode) children.get(index);
            LogicalOperator operator = null;
            if (node.getKey() instanceof Predicate predicate) {
                operator = PropositionalLogicHelper.getLogicalOperator(predicate);
            }

            currentPath.add(new Direction(operator, index));
            collectPathsToAtoms(child, currentPath, pathMap);
            currentPath.removeLast();
        }
    }

    private void resetState() {
        pathToAtomList.clear();
        atoms.clear();
        atomVariableMap.clear();
        derived.clear();
    }
}
