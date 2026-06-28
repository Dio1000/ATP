package me.dariansandru.domain.data_structures.ast;

import me.dariansandru.domain.language.predicate.Predicate;
import me.dariansandru.domain.data_structures.ast.exception.ASTNodeException;

import java.util.ArrayList;
import java.util.List;

/**
 * Propositional Abstract Syntax Tree (AST) Node represents the structure
 * of a singular node withing the AST. It stores the relevant data that makes
 * the traversal of the tree possible, that being the parent of each node (for
 * upward traversal), the children (for downward traversal) and the key.
 */
public class PropositionalASTNode implements ASTNode {

    private ASTNode parent;
    private final List<ASTNode> children;
    private Predicate key;

    public PropositionalASTNode(Predicate key) {
        this.parent = null;
        this.children = new ArrayList<>();
        this.key = key;
    }

    @Override
    public Object getKey() {
        return key;
    }

    @Override
    public void setKey(Object key) {
        if (!(key instanceof Predicate)) {
            throw new ASTNodeException("PropositionalAST Node must be a Predicate!");
        }
        this.key = (Predicate) key;
    }

    @Override
    public boolean isEmpty() {
        return key == null;
    }

    @Override
    public ASTNode getParent() {
        return parent;
    }

    @Override
    public void setParent(ASTNode parent) {
        this.parent = parent;
    }

    @Override
    public List<ASTNode> getChildren() {
        return children;
    }

    @Override
    public void addChild() {
        PropositionalASTNode newChild = new PropositionalASTNode(null);
        newChild.setParent(this);
        children.add(newChild);
    }

}
