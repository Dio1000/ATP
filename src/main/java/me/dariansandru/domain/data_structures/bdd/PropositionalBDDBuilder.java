package me.dariansandru.domain.data_structures.bdd;

import jdk.incubator.vector.IntVector;
import jdk.incubator.vector.VectorOperators;
import jdk.incubator.vector.VectorSpecies;
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

    static final VectorSpecies<Integer> SPECIES = IntVector.SPECIES_PREFERRED;
    private int[] uniqueArrayRepresentation;
    private int index = 0;

    private final int VECTOR_SIZE_THRESHOLD = 15;

    public PropositionalBDDBuilder(PropositionalAST ast) {
        this.ast = ast;
        this.root = null;
    }

    public void buildBDD() {
        if (isBuilt) return;
        if (ast.isContradiction() || ast.isTautology()) return;
        
        Set<String> atoms = PropositionalLogicHelper.getAtoms(ast);
        atomList = atoms.stream()
                .sorted().distinct().toList();
        atomStringList.addAll(atomList);

        this.root = new PropositionalBDDNode(new PropositionalAST(atomList.getFirst(), true));

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
        if (astLeft.isTautology() || astLeft.isContradiction()) {
            nodeCount++;
            node.addLeftChild(astLeft);
        }
        else {
            nodeCount++;
            node.addLeftChild(new PropositionalAST(atomList.get(depth + 1), true));
        }

        PropositionalAST astRight = ast.evaluatePartial(partialInterpretationFalse);
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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PropositionalBDDBuilder other = (PropositionalBDDBuilder) obj;

        int[] array1 = this.getUniqueArrayRepresentation();
        int[] array2 = other.getUniqueArrayRepresentation();
        int arrayLength1 = this.getUniqueArrayLength();
        int arrayLength2 = other.getUniqueArrayLength();

        if (arrayLength1 != arrayLength2) return false;
        if (arrayLength1 < SPECIES.length() || arrayLength1 < VECTOR_SIZE_THRESHOLD) {
            return Objects.equals(this.root, other.root) &&
                    Objects.equals(this.atomStringList, other.atomStringList) &&
                    Arrays.equals(array1, array2);
        }

        int index = 0;
        for (; index < SPECIES.loopBound(arrayLength1); index += SPECIES.length()) {
            var vector1 = IntVector.fromArray(SPECIES, array1, index);
            var vector2 = IntVector.fromArray(SPECIES, array2, index);

            if (vector1.compare(VectorOperators.NE, vector2).anyTrue()) {
                return false;
            }
        }
        for (; index < arrayLength1; index++) {
            if (array1[index] != array2[index]) return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(ast, root, atomList, atomStringList);
    }
}
