package me.dariansandru.domain.data_structures.ast;

import java.util.List;

/**
Implementing this interface allows the user to create a new type of AST Node.
This is to be used for helping add new Universes of Discourse (e.g. Integers, Strings),
aiding in the creation of new types of AST, which require different parsing, validation
and other operations.
 **/
public interface ASTNode {
    Object getKey();
    void setKey(Object key);

    boolean isEmpty();

    ASTNode getParent();
    void setParent(ASTNode parent);

    List<ASTNode> getChildren();
    void addChild();
}
