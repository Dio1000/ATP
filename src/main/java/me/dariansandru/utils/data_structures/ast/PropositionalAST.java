package me.dariansandru.utils.data_structures.ast;

import me.dariansandru.domain.logical_operator.Negation;
import me.dariansandru.domain.predicate.Predicate;
import me.dariansandru.domain.signature.PropositionalSignature;
import me.dariansandru.tokenizer.Token;
import me.dariansandru.tokenizer.Tokenizer;
import me.dariansandru.tokenizer.Type;
import me.dariansandru.utils.data_structures.ast.exception.ASTNodeException;
import me.dariansandru.utils.factory.PropositionalPredicateFactory;
import me.dariansandru.utils.helper.ErrorHelper;
import me.dariansandru.utils.helper.WarningHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PropositionalAST implements AST {

    private final String formulaString;
    private final PropositionalASTNode root;
    private PropositionalASTNode currentNode;
    private int currentChildIndex;
    private boolean validated = false;

    public PropositionalAST(String formulaString) {
        this.formulaString = formulaString;
        this.root = new PropositionalASTNode(null);

        this.root.addChild();
        this.currentNode = (PropositionalASTNode) this.root.getChildren().getFirst();
        this.currentChildIndex = 0;
    }

    @Override
    public String toString() {
        return formulaString;
    }

    @Override
    public AST copy() {
        return new PropositionalAST(formulaString);
    }

    @Override
    public AST simplify() {
        return null;
    }

    @Override
    public boolean validate(int line) {
        Tokenizer tokenizer = new Tokenizer(new PropositionalSignature());
        List<Token> tokens = tokenizer.tokenize(formulaString);
        boolean invalid = false;

        for (Token token : tokens) {
            int position = token.position();

            if (token.type() == Type.SEPARATOR) {
                if ("(".equals(token.lexeme())) {
                    currentNode.addChild();
                    currentChildIndex = currentNode.getChildren().size() - 1;
                    currentNode = (PropositionalASTNode) currentNode.getChildren().get(currentChildIndex);
                    currentChildIndex = 0;
                    continue;
                }
                else if (")".equals(token.lexeme())) {
                    moveUp();
                    continue;
                }
            }

            Predicate predicate;
            try {
                predicate = PropositionalPredicateFactory.createPredicate(token);
            }
            catch (Exception e) {
                ErrorHelper.add(e.getMessage());
                invalid = true;
                continue;
            }

            int arity = predicate.getArity();
            switch (arity) {
                case 0 -> {
                    if (currentNode.isEmpty()) {
                        currentNode.setKey(predicate);
                        moveUp();
                    }
                    else {
                        invalid = true;
                        ErrorHelper.add("Unexpected proposition " + token.lexeme() + " at this position!", line, position);
                    }
                }
                case 1 -> {
                    if (currentNode.isEmpty()) {
                        currentNode.setKey(predicate);
                        ensureChildren(currentNode, 1);
                        enterChildAtCurrentIndex();
                    }
                    else {
                        invalid = true;
                        ErrorHelper.add(token.lexeme() + " is a unary operator used in an invalid position!", line, position);
                    }
                }
                case 2 -> {
                    if (currentNode.isEmpty()) {
                        currentNode.setKey(predicate);
                        ensureChildren(currentNode, 2);
                        enterChildAtCurrentIndex();
                    }
                    else {
                        invalid = true;
                        ErrorHelper.add(token.lexeme() + " is a binary operator used in an invalid position!", line, position);
                    }
                }

                case -1 -> {
                    if (currentNode.isEmpty()) {
                        currentNode.setKey(predicate);
                        ensureChildren(currentNode, 2);
                        enterChildAtCurrentIndex();
                    }
                    else {
                        Predicate currentPredicate = (Predicate) currentNode.getKey();
                        String currentRepresentation = currentPredicate.getRepresentation();

                        if (Objects.equals(token.lexeme(), currentRepresentation)) {
                            ensureChildren(currentNode, currentNode.getChildren().size() + 1);
                            currentChildIndex = currentNode.getChildren().size() - 1;
                            currentNode = (PropositionalASTNode) currentNode.getChildren().get(currentChildIndex);
                        }
                        else {
                            WarningHelper.add("Suggested parentheses around operands of logical operator " + token.lexeme(), line, position);
                            Predicate oldOp = (Predicate) currentNode.getKey();

                            PropositionalASTNode newOpNode = new PropositionalASTNode(oldOp);

                            List<ASTNode> oldChildren = new ArrayList<>(currentNode.getChildren());
                            currentNode.getChildren().clear();
                            for (ASTNode child : oldChildren) {
                                child.setParent(newOpNode);
                                newOpNode.getChildren().add(child);
                            }
                            currentNode.setKey(predicate);

                            newOpNode.setParent(currentNode);
                            currentNode.getChildren().add(newOpNode);
                            currentNode.addChild();

                            currentChildIndex = 1;
                            enterChildAtCurrentIndex();
                        }
                    }
                }
                default -> {
                    invalid = true;
                    ErrorHelper.add("Unsupported arity: " + arity + " for token " + token.lexeme(), line, position);
                }
            }
        }

        boolean valid = (currentNode.equals(root)) && !invalid;

        //TODO This is a glue fix, change it maybe? Update: Don't know if needed anymore, will test later.
        try {
            Predicate negatedPredicate = (Predicate) currentNode.getKey();
            while (Objects.equals(negatedPredicate.getRepresentation(), new Negation().getRepresentation())) {
                if (currentNode.getParent() != null) currentNode = (PropositionalASTNode) currentNode.getParent();
                else break;
            }
            if (currentNode.equals(root)) valid = true;
        }
        catch (Exception ignored) {

        }

        if (!valid) {
            ErrorHelper.add(formulaString + " is not a well-formed formula!");
        }
        return valid;
    }

    @Override
    public Object evaluate() {
        return null;
    }

    @Override
    public boolean isEquivalentTo(AST other) {
        return false;
    }

    private void ensureChildren(PropositionalASTNode node, int count) {
        while (node.getChildren().size() < count) {
            node.addChild();
        }
    }

    private void enterChildAtCurrentIndex() {
        List<ASTNode> children = currentNode.getChildren();
        if (currentChildIndex >= children.size()) {
            ensureChildren(currentNode, currentChildIndex + 1);
            children = currentNode.getChildren();
        }
        currentNode = (PropositionalASTNode) children.get(currentChildIndex);
        currentChildIndex = 0;
    }

    @Override
    public void moveLeft() {
        if (currentNode.getParent() == null) {
            throw new ASTNodeException("Current node has no parent; cannot move left.");
        }
        PropositionalASTNode parent = (PropositionalASTNode) currentNode.getParent();
        int idx = parent.getChildren().indexOf(currentNode);
        if (idx <= 0) {
            throw new ASTNodeException("Already at the leftmost sibling.");
        }
        currentNode = (PropositionalASTNode) parent.getChildren().get(idx - 1);
        currentChildIndex = 0;
    }

    @Override
    public void moveRight() {
        enterChildAtCurrentIndex();
    }

    @Override
    public void moveUp() {
        if (currentNode.getParent() == null) {
            throw new ASTNodeException("Current node does not have a parent.");
        }

        PropositionalASTNode parentNode = (PropositionalASTNode) currentNode.getParent();
        int cameFromIndex = parentNode.getChildren().indexOf(currentNode);
        currentNode = parentNode;
        currentChildIndex = cameFromIndex + 1;

        while (currentNode.getKey() instanceof Predicate predicate && predicate.getArity() == 1) {
            if (currentNode.getParent() == null) {
                break;
            }
            parentNode = (PropositionalASTNode) currentNode.getParent();
            cameFromIndex = parentNode.getChildren().indexOf(currentNode);
            currentNode = parentNode;
            currentChildIndex = cameFromIndex + 1;
        }
    }

}
