package me.dariansandru.domain.proof.inference_rules;

import me.dariansandru.domain.data_structures.ast.AST;
import me.dariansandru.domain.proof.SubGoal;

import java.util.*;

public record CustomPropositionalInferenceRule(String name, List<AST> antecedents,
                                               AST conclusion) implements InferenceRule {

    @Override
    public boolean canInference(List<AST> kb, AST goal) {
        if (!conclusion.hasSameStructure(goal)) return false;

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
        int antecedentCount = antecedents.size();

        int[] currentAssignment = new int[antecedentCount];
        Arrays.fill(currentAssignment, -1);
        boolean[] usedKb = new boolean[kb.size()];

        generateUniqueAssignments(matches, 0, currentAssignment, usedKb, kb, potentialEntries);
        for (List<AST> entry : potentialEntries) {
            System.out.println(entry);
            if (!found && isInferenceRuleApplicable(entry)) found = true;
        }
        return found;
    }

    private boolean isInferenceRuleApplicable(List<AST> kbSubset) {
        if (kbSubset.size() < antecedents.size()) return false;

        return checkVariablesConsistent(kbSubset);
    }

    private boolean checkVariablesConsistent(List<AST> kbSubset) {

        return true;
    }

    @Override
    public List<AST> inference(List<AST> kb, AST goal) {
        if (canInference(kb, goal)) return List.of(conclusion);
        return List.of();
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
            for (int index : currentAssignment) {
                assignment.add(kb.get(index));
            }

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

}
