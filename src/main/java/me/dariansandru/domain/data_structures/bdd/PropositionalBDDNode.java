package me.dariansandru.domain.data_structures.bdd;

import me.dariansandru.domain.data_structures.ast.PropositionalAST;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PropositionalBDDNode {

    private boolean isLeaf;
    private PropositionalAST value;

    private PropositionalBDDNode parent;
    private PropositionalBDDNode left;
    private PropositionalBDDNode right;

    private int depth;
    private boolean isLeftChild = false;
    private int uniqueIntegerRepresentation = -1;
    private int uniqueIntegerRepresentationLength;

    private int uniqueIntegerRepresentationOfValue = -1;
    private int uniqueIntegerRepresentationOfValueLength;

    public PropositionalBDDNode(PropositionalAST value) {
        this.isLeaf = value.isTautology() || value.isContradiction();
        this.value = value;

        this.parent = null;
        this.left = null;
        this.right = null;
    }

    public PropositionalBDDNode() {
        this.isLeaf = false;
        this.value = null;

        this.parent = null;
        this.left = null;
        this.right = null;
    }

    public void addLeftChild(PropositionalAST value) {
        PropositionalBDDNode node = new PropositionalBDDNode();

        node.value = value;
        node.setParent(this);
        node.setDepth(this.depth + 1);
        node.setLeaf();
        isLeftChild = true;

        this.left = node;
    }

    public void addRightChild(PropositionalAST value) {
        PropositionalBDDNode node = new PropositionalBDDNode();

        node.value = value;
        node.setParent(this);
        node.setDepth(this.depth + 1);
        node.setLeaf();
        isLeftChild = false;

        this.right = node;
    }

    public boolean isLeaf() {
        return isLeaf;
    }

    public PropositionalAST getValue() {
        return value;
    }

    public PropositionalBDDNode getParent() {
        return parent;
    }

    public void setParent(PropositionalBDDNode node) {
        if (this.parent == null) this.parent = node;
    }

    public PropositionalBDDNode getLeft() {
        return left;
    }

    public void setLeaf() {
        this.isLeaf = value.isTautology() || value.isContradiction();
    }

    public PropositionalBDDNode getRight() {
        return right;
    }

    public int getDepth() {
        if (parent == null) return 0;
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public List<Integer> getTruthValuesOfParents() {
        List<Integer> truthValues = new ArrayList<>();
        PropositionalBDDNode node = this;
        while (node.parent != null) {
            truthValues.addFirst(this.isLeftChild ? 0 : 1);
            node = node.parent;
        }

        return truthValues;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PropositionalBDDNode other = (PropositionalBDDNode) obj;

        if (this.isLeaf != other.isLeaf) return false;
        if (!Objects.equals(this.value.toString(), other.value.toString())) return false;

        return Objects.equals(this.left, other.left)
                && Objects.equals(this.right, other.right);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isLeaf, value, left, right);
    }

    public int getUniqueIntegerRepresentation() {
        if (uniqueIntegerRepresentation != -1) return uniqueIntegerRepresentation;

        int representation = getRepresentationOfValue();

        int parentUniqueRepresentation = this.getParent() == null ? 0 : this.getParent().getUniqueIntegerRepresentationOfValue();
        int parentUniqueRepresentationLength = parentUniqueRepresentation == 0 ? 1 : this.getParent().getUniqueIntegerRepresentationOfValueLength();
        representation *= (int) Math.pow(10, parentUniqueRepresentationLength);
        representation += parentUniqueRepresentation;

        int representationCopy = representation;
        int representationLength = 0;

        while (representationCopy != 0) {
            representationLength++;
            representationCopy /= 10;
        }

        this.uniqueIntegerRepresentation = representation;
        this.uniqueIntegerRepresentationLength = representationLength;
        return representation;
    }

    public int getRepresentationOfValue() {
        if (uniqueIntegerRepresentationOfValue != -1) return uniqueIntegerRepresentationOfValue;

        int representation = 0;
        if (this.value.isTautology()) representation += 2;
        else if (this.value.isContradiction()) representation += 1;
        else {
            String valueString = this.value.toString();
            for (int i = 0 ; i < this.value.toString().length() ; i++) {
                representation += valueString.charAt(i);
            }
        }

        int length = 0;
        int representationCopy = representation;
        while (representationCopy != 0) {
            length++;
            representationCopy /= 10;
        }

        uniqueIntegerRepresentationOfValue = representation;
        uniqueIntegerRepresentationOfValueLength = length;

        return representation;
    }

    public int getUniqueIntegerRepresentationLength() {
        return this.uniqueIntegerRepresentationLength;
    }

    public int getUniqueIntegerRepresentationOfValue() {
        return uniqueIntegerRepresentationOfValue;
    }

    public int getUniqueIntegerRepresentationOfValueLength() {
        return uniqueIntegerRepresentationOfValueLength;
    }
}
