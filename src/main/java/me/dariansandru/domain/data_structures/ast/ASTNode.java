package me.dariansandru.domain.data_structures.ast;

import java.util.List;

public interface ASTNode {
    Object getKey();
    void setKey(Object key);

    boolean isEmpty();

    ASTNode getParent();
    void setParent(ASTNode parent);

    List<ASTNode> getChildren();
    void addChild();
}
