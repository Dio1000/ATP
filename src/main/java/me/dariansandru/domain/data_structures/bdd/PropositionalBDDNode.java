package me.dariansandru.domain.data_structures.bdd;

import me.dariansandru.domain.data_structures.ast.PropositionalAST;
import me.dariansandru.utils.global.GlobalAtomID;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PropositionalBDDNode {

    private final int id;

    private boolean isLeaf;
    private PropositionalAST value;

    private PropositionalBDDNode parent;
    private PropositionalBDDNode left;
    private PropositionalBDDNode right;

    private int depth;
    private boolean isLeftChild = false;

    public PropositionalBDDNode(PropositionalAST value) {
        this.id = GlobalAtomID.getAtomId(value.toString());
        this.value = value;
        this.isLeaf = value.isTautology() || value.isContradiction();
        this.parent = null;
        this.left = null;
        this.right = null;
        this.depth = 0;
    }

    public PropositionalBDDNode() {
        this.id = 0;
        this.value = null;
        this.isLeaf = false;
        this.parent = null;
        this.left = null;
        this.right = null;
        this.depth = 0;
    }

    public void addLeftChild(PropositionalAST value) {
        PropositionalBDDNode node = new PropositionalBDDNode(value);
        node.parent = this;
        node.depth = this.depth + 1;
        node.isLeftChild = true;
        node.setLeaf();
        this.left = node;
    }

    public void addRightChild(PropositionalAST value) {
        PropositionalBDDNode node = new PropositionalBDDNode(value);
        node.parent = this;
        node.depth = this.depth + 1;
        node.isLeftChild = false;
        node.setLeaf();
        this.right = node;
    }

    public int getId() {
        return id;
    }

    public boolean isLeaf() {
        return isLeaf;
    }

    public PropositionalAST getValue() {
        return value;
    }

    public void setValue(PropositionalAST value) {
        this.value = value;
        setLeaf();
    }

    public PropositionalBDDNode getParent() {
        return parent;
    }

    public PropositionalBDDNode getLeft() {
        return left;
    }

    public PropositionalBDDNode getRight() {
        return right;
    }

    public int getDepth() {
        return parent == null ? 0 : depth;
    }

    private void setLeaf() {
        this.isLeaf = value.isTautology() || value.isContradiction();
    }

    public List<Integer> getTruthValuesOfParents() {
        List<Integer> truthValues = new ArrayList<>();
        PropositionalBDDNode node = this;

        while (node.parent != null) {
            truthValues.addFirst(node.isLeftChild ? 1 : 0);
            node = node.parent;
        }
        return truthValues;
    }

    public int getUniqueIntegerRepresentation() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PropositionalBDDNode other = (PropositionalBDDNode) obj;

        return isLeaf == other.isLeaf
                && Objects.equals(value, other.value)
                && Objects.equals(left, other.left)
                && Objects.equals(right, other.right);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isLeaf, value, left, right);
    }
}