package me.dariansandru.domain.data_structures.bdd;

import me.dariansandru.domain.data_structures.ast.PropositionalAST;
import me.dariansandru.domain.language.interpretation.PropositionalPartialInterpretation;
import me.dariansandru.utils.helper.PropositionalLogicHelper;

import java.util.*;

public class PropositionalBDDBuilder {

    private PropositionalBDDNode root;
    private final PropositionalAST ast;

    private List<String> atomList = new ArrayList<>();
    private final List<String> atomStringList = new ArrayList<>();
    private int nodeCount = 0;

    private boolean isBuilt = false;

    private int[] uniqueArrayRepresentation;
    private int index = 0;

    public PropositionalBDDBuilder(PropositionalAST ast) {
        this.ast = ast;
        this.root = null;
    }

    public void buildBDD() {
        if (isBuilt) return;
        if (ast.isContradiction() || ast.isTautology()) return;

        Set<String> atoms = PropositionalLogicHelper.getAtoms(ast);
        atomList = atoms.stream().sorted().distinct().toList();
        atomStringList.addAll(atomList);

        this.root = new PropositionalBDDNode(new PropositionalAST(atomList.getFirst(), true, 0));

        nodeCount = 1;
        buildRecursive(root);

        uniqueArrayRepresentation = new int[nodeCount];
        buildArray();
        isBuilt = true;
    }

    private void buildRecursive(PropositionalBDDNode node) {
        if (node.isLeaf()) return;

        int depth = node.getDepth();
        int numAtoms = atomList.size();

        List<Integer> truthValueListTrue = new ArrayList<>(Collections.nCopies(numAtoms, -1));
        List<Integer> truthValueListFalse = new ArrayList<>(Collections.nCopies(numAtoms, -1));

        List<Integer> parentsTruthValues = node.getTruthValuesOfParents();
        for (int i = 0; i < depth; i++) {
            truthValueListTrue.set(i, parentsTruthValues.get(i));
            truthValueListFalse.set(i, parentsTruthValues.get(i));
        }

        truthValueListTrue.set(depth, 1);
        truthValueListFalse.set(depth, 0);
        PropositionalPartialInterpretation partialInterpretationTrue =
                new PropositionalPartialInterpretation(atomStringList, truthValueListTrue);
        PropositionalPartialInterpretation partialInterpretationFalse =
                new PropositionalPartialInterpretation(atomStringList, truthValueListFalse);

        PropositionalAST astLeft = ast.evaluatePartial(partialInterpretationTrue);
        PropositionalAST astRight = ast.evaluatePartial(partialInterpretationFalse);

        if (astLeft.isTautology() && astRight.isTautology()) {
            if (depth == 0) {
                node.addLeftChild(new PropositionalAST(false));
                node.addRightChild(new PropositionalAST(false));
                nodeCount += 2;
            }
            node.setValue(new PropositionalAST(false));
            return;
        }
        else if (astLeft.isContradiction() && astRight.isContradiction()) {
            if (depth == 0) {
                node.addLeftChild(new PropositionalAST(true));
                node.addRightChild(new PropositionalAST(true));
                nodeCount += 2;
            }
            node.setValue(new PropositionalAST(true));
            return;
        }

        if (astLeft.isTautology() || astLeft.isContradiction()) {
            nodeCount++;
            node.addLeftChild(astLeft);
        }
        else {
            nodeCount++;
            node.addLeftChild(new PropositionalAST(atomList.get(depth + 1), true));
        }

        if (astRight.isTautology() || astRight.isContradiction()) {
            nodeCount++;
            node.addRightChild(astRight);
        }
        else {
            nodeCount++;
            node.addRightChild(new PropositionalAST(atomList.get(depth + 1), true));
        }

        buildRecursive(node.getLeft());
        buildRecursive(node.getRight());
    }

    private void buildArray() {
        uniqueArrayRepresentation[index] = this.root.getUniqueIntegerRepresentation();
        buildArrayRecursive(root.getLeft());
        buildArrayRecursive(root.getRight());
    }

    private void buildArrayRecursive(PropositionalBDDNode node) {
        if (node == null) return;

        index++;
        uniqueArrayRepresentation[index] = node.getUniqueIntegerRepresentation();

        buildArrayRecursive(node.getLeft());
        buildArrayRecursive(node.getRight());
    }

    public PropositionalBDDNode getRoot() {
        return root;
    }

    public PropositionalAST getAst() {
        return ast;
    }

    public int[] getUniqueArrayRepresentation() {
        return uniqueArrayRepresentation;
    }

    public int getUniqueArrayLength() {
        return index;
    }

    public boolean isTautology() {
        return this.getRoot().getRight().getValue().isTautology()
                && this.getRoot().getLeft().getValue().isTautology();
    }

    public boolean isContradiction() {
        return this.getRoot().getRight().getValue().isContradiction()
                && this.getRoot().getLeft().getValue().isContradiction();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PropositionalBDDBuilder other = (PropositionalBDDBuilder) obj;

        int[] array1 = this.getUniqueArrayRepresentation();
        int[] array2 = other.getUniqueArrayRepresentation();
        int length1 = this.getUniqueArrayLength();
        int length2 = other.getUniqueArrayLength();

        if (length1 != length2) return false;

        for (int i = 0; i < length1; i++) {
            if (array1[i] != array2[i]) return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(ast, root, atomList, atomStringList);
    }
}
