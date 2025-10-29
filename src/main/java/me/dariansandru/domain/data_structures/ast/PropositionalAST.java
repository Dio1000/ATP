package me.dariansandru.domain.data_structures.ast;

import me.dariansandru.domain.data_structures.bdd.PropositionalBDDBuilder;
import me.dariansandru.domain.language.LogicalOperator;
import me.dariansandru.domain.language.interpretation.Interpretation;
import me.dariansandru.domain.language.interpretation.PropositionalInterpretation;
import me.dariansandru.domain.language.interpretation.PropositionalPartialInterpretation;
import me.dariansandru.domain.language.interpretation.exception.InvalidInterpretationException;
import me.dariansandru.domain.language.predicate.Predicate;
import me.dariansandru.domain.language.signature.PropositionalSignature;
import me.dariansandru.tokenizer.Token;
import me.dariansandru.tokenizer.Tokenizer;
import me.dariansandru.tokenizer.Type;
import me.dariansandru.domain.data_structures.ast.exception.ASTException;
import me.dariansandru.domain.data_structures.ast.exception.ASTNodeException;
import me.dariansandru.utils.factory.PropositionalPredicateFactory;
import me.dariansandru.utils.flyweight.LogicalOperatorFlyweight;
import me.dariansandru.utils.helper.ErrorHelper;
import me.dariansandru.utils.helper.PropositionalLogicHelper;
import me.dariansandru.utils.helper.WarningHelper;

import java.util.*;

public class PropositionalAST implements AST {

    private final String formulaString;
    private PropositionalASTNode root;
    private PropositionalASTNode currentNode;

    private int currentChildIndex;
    private final boolean isContradiction;
    private boolean isTautology;

    private final PropositionalBDDBuilder builder;

    public PropositionalAST(String formulaString) {
        this.formulaString = formulaString;
        this.root = new PropositionalASTNode(null);
        this.isContradiction = false;
        this.isTautology = false;

        this.root.addChild();
        this.currentNode = (PropositionalASTNode) this.root.getChildren().getFirst();
        this.currentChildIndex = 0;

        this.checkContradiction();
        this.checkTautology();

        builder = new PropositionalBDDBuilder(this);
    }

    public PropositionalAST(String formulaString, boolean shouldValidate) {
        this.formulaString = formulaString;
        this.root = new PropositionalASTNode(null);
        this.isContradiction = false;
        this.isTautology = false;

        this.root.addChild();
        this.currentNode = (PropositionalASTNode) this.root.getChildren().getFirst();
        this.currentChildIndex = 0;

        if (shouldValidate) this.validate(0);

        this.checkContradiction();
        this.checkTautology();

        builder = new PropositionalBDDBuilder(this);
    }

    public PropositionalAST(PropositionalASTNode node) {
        this.formulaString = "";
        this.isContradiction = false;
        this.isTautology = false;

        if (node != null && node.getKey() == null && !node.getChildren().isEmpty()) {
            this.root = node;
        }
        else {
            this.root = new PropositionalASTNode(null);
            this.root.addChild();
            this.root.getChildren().set(0, node);
            if (node != null) node.setParent(this.root);
        }

        if (!this.root.getChildren().isEmpty()) {
            this.currentNode = (PropositionalASTNode) this.root.getChildren().getFirst();
        }
        else {
            this.currentNode = this.root;
        }
        this.currentChildIndex = 0;

        this.checkContradiction();
        this.checkTautology();

        builder = new PropositionalBDDBuilder(this);
    }

    public PropositionalAST(boolean isContradiction) {
        this.formulaString = null;
        this.root = null;
        this.isContradiction = isContradiction;
        this.isTautology = !isContradiction;
        builder = new PropositionalBDDBuilder(this);
    }

    @Override
    public String toString() {
        // TODO: Find a way to also be able to print the formula itself, even if it is a contradiction / tautology
        if (this.isContradiction) return "Contradiction";
        if (this.isTautology) return "Tautology";

        PropositionalASTNode formula = getFormulaNode();
        if (formula == null || formula.getKey() == null) return "";

        String string = buildString(formula);

        Predicate predicate = (Predicate) formula.getKey();
        int arity = predicate.getArity();

        if (arity == 0 || arity == 1) return string;
        return stripOuter(string);
    }

    private String buildString(PropositionalASTNode node) {
        if (node == null || node.getKey() == null) {
            return "";
        }

        Predicate predicate = (Predicate) node.getKey();
        int arity = predicate.getArity();

        switch (arity) {
            case 0 -> {
                return predicate.getRepresentation();
            }
            case 1 -> {
                PropositionalASTNode child = (PropositionalASTNode) node.getChildren().getFirst();
                String childStr = buildString(child);

                if (child.getKey() instanceof Predicate childPred && childPred.getArity() != 0) {
                    return predicate.getRepresentation() + childStr;
                }
                return predicate.getRepresentation() + childStr;
            }
            case 2 -> {
                PropositionalASTNode left = (PropositionalASTNode) node.getChildren().get(0);
                PropositionalASTNode right = (PropositionalASTNode) node.getChildren().get(1);
                return "(" + buildString(left) + " " + predicate.getRepresentation() + " " + buildString(right) + ")";
            }
            case -1 -> {
                StringJoiner joiner = new StringJoiner(" " + predicate.getRepresentation() + " ");
                for (ASTNode child : node.getChildren()) {
                    joiner.add(buildString((PropositionalASTNode) child));
                }
                return "(" + joiner + ")";
            }
            default -> {
                return "<?>";
            }
        }
    }

    private String stripOuter(String string) {
        if (string.length() < 2) return string;
        if (string.charAt(0) != '(' || string.charAt(string.length() - 1) != ')') return string;

        int d = 0;
        for (int i = 0; i < string.length() - 1; i++) {
            char character = string.charAt(i);
            if (character == '(') d++;
            else if (character == ')') d--;
            if (d == 0 && i < string.length() - 1) return string;
        }
        return string.substring(1, string.length() - 1);
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
        int position = 0;

        int currentIndex = 0;
        try{
            for (Token token : tokens) {
                position = token.position();

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
                            ErrorHelper.add("Unexpected proposition " + token.lexeme() + " at this position", line, position);
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
                            ErrorHelper.add(token.lexeme() + " is a unary operator used in an invalid position", line, position);
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
                            ErrorHelper.add(token.lexeme() + " is a binary operator used in an invalid position", line, position);
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
                                String newLexeme = tokens.get(currentIndex - 2).lexeme();
                                int newPosition = tokens.get(currentIndex - 2).position();

                                WarningHelper.add("Suggested parentheses around operands of logical operator " + newLexeme, line, newPosition);
                                Predicate oldOp = (Predicate) currentNode.getKey();

                                PropositionalASTNode newOpNode = new PropositionalASTNode(oldOp);

                                List<ASTNode> oldChildren = new ArrayList<>(currentNode.getChildren());
                                currentNode.getChildren().clear();
                                for (ASTNode child : oldChildren) {
                                    PropositionalASTNode childCopy = cloneNode((PropositionalASTNode) child);
                                    childCopy.setParent(newOpNode);
                                    newOpNode.getChildren().add(childCopy);
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
                currentIndex++;
            }

            boolean valid = (currentNode.equals(root)) && !invalid;
            if (!valid) {
                if (currentNode.getParent() != null) ErrorHelper.add("Expected closing parentheses ')' at this position!", line, position);
                ErrorHelper.add(formulaString + " is not a well-formed formula!");
            }

            return valid;
        }
        catch (Exception e) {
            ErrorHelper.add("Could not validate formula: " + formulaString);
            throw new ASTException("Could not validate formula: " + formulaString);
        }
    }

    @Override
    public void negate() {
        if (this.isAtomic()) {
            if (Objects.equals(((Predicate)this.root.getChildren().getFirst().getKey()).getRepresentation(), LogicalOperatorFlyweight.getNegationString())) {
                root = (PropositionalASTNode) this.root.getChildren().getFirst().getChildren().getFirst();
                return;
            }

            PropositionalASTNode negationNode = new PropositionalASTNode(LogicalOperatorFlyweight.getNegation());
            negationNode.getChildren().add(root.getChildren().getFirst());
            root.setParent(negationNode);
            root = negationNode;
            root.setParent(null);
            return;
        }

        LogicalOperator operator = PropositionalLogicHelper.getOutermostOperation(this);
        if (operator == LogicalOperator.NEGATION) {
            PropositionalASTNode negNode = (PropositionalASTNode) root.getChildren().getFirst();
            PropositionalASTNode inner = (PropositionalASTNode) negNode.getChildren().getFirst();

            inner.setParent(root);
            root.getChildren().set(0, inner);
        }
        else {
            PropositionalASTNode negationNode = new PropositionalASTNode(LogicalOperatorFlyweight.getNegation());
            negationNode.getChildren().add(root);
            root.setParent(negationNode);
            root = negationNode;
            root.setParent(null);
        }
    }

    @Override
    public Object evaluate(Interpretation interpretation) {
        if (!(interpretation instanceof PropositionalInterpretation))
            throw new InvalidInterpretationException("Interpretation provided to the Propositional AST is not an instance of PropositionalInterpretation!");
        return evaluateRecursive(this, interpretation);
    }

    public boolean evaluateRecursive(PropositionalAST node, Interpretation interpretation) {
        LogicalOperator operator = PropositionalLogicHelper.getOutermostOperation(node);
        if (operator == LogicalOperator.IMPLICATION) {
            boolean argument1 = evaluateRecursive((PropositionalAST) node.getSubtree(0), interpretation);
            boolean argument2 = evaluateRecursive((PropositionalAST) node.getSubtree(1), interpretation);
            return !argument1 || argument2;
        }
        else if (operator == LogicalOperator.EQUIVALENCE) {
            boolean argument1 = evaluateRecursive((PropositionalAST) node.getSubtree(0), interpretation);
            boolean argument2 = evaluateRecursive((PropositionalAST) node.getSubtree(1), interpretation);
            return argument1 == argument2;
        }
        else if (operator == LogicalOperator.CONJUNCTION) {
            List<Boolean> arguments = new ArrayList<>();
            int childrenNumber = ((PropositionalASTNode) node.getRoot()).getChildren().size();

            for (int index = 0; index < childrenNumber; index++) {
                arguments.add(evaluateRecursive((PropositionalAST) node.getSubtree(index), interpretation));
            }

            for (int index = 0; index < childrenNumber; index++) {
                if (!arguments.get(index)) {
                    return false;
                }
            }
            return true;
        }
        else if (operator == LogicalOperator.DISJUNCTION) {
            List<Boolean> arguments = new ArrayList<>();
            int childrenNumber = ((PropositionalASTNode) node.getRoot()).getChildren().size();

            for (int index = 0; index < childrenNumber; index++) {
                arguments.add(evaluateRecursive((PropositionalAST) node.getSubtree(index), interpretation));
            }

            for (int index = 0; index < childrenNumber; index++) {
                if (arguments.get(index)) {
                    return true;
                }
            }
            return false;
        }
        else if (operator == LogicalOperator.NEGATION) {
            return !evaluateRecursive((PropositionalAST) node.getSubtree(0), interpretation);
        }
        else {
            String atomString = node.toString();
            return (boolean) interpretation.getValue(atomString);
        }
    }

    public PropositionalAST evaluatePartial(PropositionalPartialInterpretation interpretation) {
        return evaluatePartialRecursive(this, interpretation);
    }

    public PropositionalAST evaluatePartialRecursive(PropositionalAST node, PropositionalPartialInterpretation interpretation) {
        LogicalOperator operator = PropositionalLogicHelper.getOutermostOperation(node);
        if (operator == LogicalOperator.IMPLICATION) {
            PropositionalAST argument1 = evaluatePartialRecursive((PropositionalAST) node.getSubtree(0), interpretation);
            PropositionalAST argument2 = evaluatePartialRecursive((PropositionalAST) node.getSubtree(1), interpretation);

            if (isContradictionOrTautology(argument1) && isContradictionOrTautology(argument2)) {
                boolean boolean1 = argument1.isTautology;
                boolean boolean2 = argument2.isTautology;

                return !boolean1 || boolean2 ? new PropositionalAST(false) : new PropositionalAST(true);
            }
            else if (isContradictionOrTautology(argument1) && !isContradictionOrTautology(argument2)){
                boolean boolean1 = argument1.isTautology;

                return boolean1 ? argument2 : new PropositionalAST(false);
            }
            else if (!isContradictionOrTautology(argument1) && isContradictionOrTautology(argument2)){
                boolean boolean2 = argument2.isTautology;

                return boolean2 ? new PropositionalAST(false) : argument1;
            }
            else {
                return formString(argument1, argument2, LogicalOperatorFlyweight.getImplicationString());
            }
        }
        else if (operator == LogicalOperator.EQUIVALENCE) {
            PropositionalAST argument1 = evaluatePartialRecursive((PropositionalAST) node.getSubtree(0), interpretation);
            PropositionalAST argument2 = evaluatePartialRecursive((PropositionalAST) node.getSubtree(1), interpretation);
            if (isContradictionOrTautology(argument1) && isContradictionOrTautology(argument2)) {
                boolean boolean1 = argument1.isTautology;
                boolean boolean2 = argument2.isTautology;

                return boolean1 == boolean2 ? new PropositionalAST(false) : new PropositionalAST(true);
            }
            else if (isContradictionOrTautology(argument1) && !isContradictionOrTautology(argument2)){
                return argument2;
            }
            else if (!isContradictionOrTautology(argument1) && isContradictionOrTautology(argument2)){
                return argument1;
            }
            else {
                return formString(argument1, argument2, LogicalOperatorFlyweight.getEquivalenceString());
            }
        }
        else if (operator == LogicalOperator.CONJUNCTION) {
            List<PropositionalAST> arguments = new ArrayList<>();
            int childrenNumber = ((PropositionalASTNode) node.getRoot()).getChildren().size();

            for (int index = 0; index < childrenNumber ; index++) {
                arguments.add(evaluatePartialRecursive((PropositionalAST) node.getSubtree(index), interpretation));
            }

            boolean allTautology = true;
            boolean existsContradiction = false;

            StringBuilder conjunctionBuilder = new StringBuilder();
            for (int index = 0 ; index < childrenNumber ; index++) {
                PropositionalAST ast = arguments.get(index);
                if (ast.isContradiction) {
                    allTautology = false;
                    existsContradiction = true;
                }
                else if (!isContradictionOrTautology(ast)) {
                    conjunctionBuilder.append(ast).append(" ").append(LogicalOperatorFlyweight.getConjunctionString()).append(" ");
                    allTautology = false;
                }
            }

            if (!conjunctionBuilder.isEmpty()) {
                int lengthToRemove = LogicalOperatorFlyweight.getConjunctionString().length() + 1;
                conjunctionBuilder.setLength(conjunctionBuilder.length() - lengthToRemove);
            }

            if (allTautology) return new PropositionalAST(false);
            else if (existsContradiction) return new PropositionalAST(true);
            else return new PropositionalAST(conjunctionBuilder.toString(), true);
        }
        else if (operator == LogicalOperator.DISJUNCTION) {
            List<PropositionalAST> arguments = new ArrayList<>();
            int childrenNumber = ((PropositionalASTNode) node.getRoot()).getChildren().size();

            for (int index = 0; index < childrenNumber ; index++) {
                arguments.add(evaluatePartialRecursive((PropositionalAST) node.getSubtree(index), interpretation));
            }

            boolean existsTautology = false;
            boolean allContradiction = true;

            StringBuilder disjunctionBuilder = new StringBuilder();
            for (int index = 0 ; index < childrenNumber ; index++) {
                PropositionalAST ast = arguments.get(index);
                if (ast.isTautology) {
                    allContradiction = false;
                    existsTautology = true;
                }
                else if (!isContradictionOrTautology(ast)) {
                    disjunctionBuilder.append(ast).append(" ").append(LogicalOperatorFlyweight.getDisjunctionString()).append(" ");
                    allContradiction = false;
                }
            }

            if (!disjunctionBuilder.isEmpty()) {
                int lengthToRemove = LogicalOperatorFlyweight.getDisjunctionString().length() + 1;
                disjunctionBuilder.setLength(disjunctionBuilder.length() - lengthToRemove);
            }

            if (allContradiction) return new PropositionalAST(true);
            else if (existsTautology) return new PropositionalAST(false);
            else return new PropositionalAST(disjunctionBuilder.toString(), true);
        }
        else if (operator == LogicalOperator.NEGATION) {
            PropositionalAST ast = evaluatePartialRecursive((PropositionalAST) node.getSubtree(0), interpretation);
            if (ast.isTautology) return new PropositionalAST(true);
            else if (ast.isContradiction) return new PropositionalAST(false);
            else {
                String argumentString = ast.toString();
                if (PropositionalLogicHelper.getOutermostOperation(ast) != LogicalOperator.NOT_A_LOGICAL_OPERATOR) {
                    argumentString = "!(" + argumentString + ")";
                    return new PropositionalAST(argumentString, true);
                }
                else {
                    argumentString = "!" + argumentString;
                    return new PropositionalAST(argumentString, true);
                }
            }
        }
        else {
            String atomString = node.toString();
            Object value = interpretation.getValue(atomString);

            if (value instanceof Boolean) {
                if ((Boolean) value) return new PropositionalAST(false);
                else return new PropositionalAST(true);
            }

            else return new PropositionalAST(atomString, true);
        }
    }

    public void buildBDD() {
        builder.buildBDD();
    }

    public PropositionalBDDBuilder getBuilder() {
        return builder;
    }

    private PropositionalAST formString(PropositionalAST argument1, PropositionalAST argument2, String symbol) {
        String argument1String = argument1.toString();
        String argument2String = argument2.toString();

        LogicalOperator operator1 = PropositionalLogicHelper.getOutermostOperation(argument1);
        LogicalOperator operator2 = PropositionalLogicHelper.getOutermostOperation(argument2);

        if (operator1 != LogicalOperator.NOT_A_LOGICAL_OPERATOR && operator1 != LogicalOperator.NEGATION) {
            argument1String = "(" + argument1 + ")";
        }
        if (operator2 != LogicalOperator.NOT_A_LOGICAL_OPERATOR && operator2 != LogicalOperator.NEGATION) {
            argument2String = "(" + argument2 + ")";
        }
        return new PropositionalAST(argument1String + " " + symbol + " " + argument2String, true);
    }

    private boolean isContradictionOrTautology(PropositionalAST ast) {
        return ast.isTautology || ast.isContradiction;
    }

    @Override
    public void moveLeft() {
        if (currentNode.getParent() == null) {
            throw new ASTNodeException("Current node has no parent, cannot move left.");
        }
        PropositionalASTNode parent = (PropositionalASTNode) currentNode.getParent();

        int index = parent.getChildren().indexOf(currentNode);
        if (index <= 0) {
            throw new ASTNodeException("Already at the leftmost sibling.");
        }

        currentNode = (PropositionalASTNode) parent.getChildren().get(index - 1);
        currentChildIndex = 0;
    }

    @Override
    public void moveRight() {
        enterChildAtCurrentIndex();
    }

    @Override
    public void moveUp() {
        if (currentNode.getParent() == null) {
            return;
        }

        PropositionalASTNode parent = (PropositionalASTNode) currentNode.getParent();
        int childIndex = parent.getChildren().indexOf(currentNode);
        currentNode = parent;

        while (currentNode.getKey() instanceof Predicate predicate &&
                predicate.getArity() == 1 &&
                currentNode.getParent() != null) {
            parent = (PropositionalASTNode) currentNode.getParent();
            childIndex = parent.getChildren().indexOf(currentNode);
            currentNode = parent;
        }

        currentChildIndex = childIndex + 1;
        if (currentChildIndex >= currentNode.getChildren().size()) {
            currentChildIndex = currentNode.getChildren().size();
        }
    }

    @Override
    public Object getRoot() {
        if (root != null && root.getKey() == null && !root.getChildren().isEmpty()) {
            return root.getChildren().getFirst();
        }
        return this.root;
    }

    @Override
    public AST getSubtree(int childIndex) {
        if (root == null) {
            throw new IllegalStateException("Root is null");
        }
        PropositionalASTNode effectiveRoot = root;

        if (root.getKey() == null && root.getChildren().size() == 1) {
            effectiveRoot = (PropositionalASTNode) root.getChildren().getFirst();
        }

        if (effectiveRoot.getChildren().isEmpty()) {
            if (childIndex != 0) {
                throw new IndexOutOfBoundsException("Leaf node has no children");
            }
            return new PropositionalAST(cloneNode(effectiveRoot));
        }

        if (childIndex < 0 || childIndex >= effectiveRoot.getChildren().size()) {
//            throw new IndexOutOfBoundsException(
//                    "Child index out of range: " + childIndex + " (node has " + effectiveRoot.getChildren().size() + " children)"
//            );
            PropositionalASTNode childNode = (PropositionalASTNode) effectiveRoot.getChildren().get(childIndex - 1);
            PropositionalASTNode clone = cloneNode(childNode);

            return new PropositionalAST(clone);
        }

        PropositionalASTNode childNode = (PropositionalASTNode) effectiveRoot.getChildren().get(childIndex);
        PropositionalASTNode clone = cloneNode(childNode);

        return new PropositionalAST(clone);
    }

    @Override
    public boolean isEquivalentTo(AST other) {
        if (other == null || !(other.getRoot() instanceof PropositionalASTNode)) {
            return false;
        }
        return areNodesEquivalent((PropositionalASTNode) this.getRoot(), (PropositionalASTNode) other.getRoot());
    }

    @Override
    public boolean isEmpty() {
        return this.formulaString.isEmpty();
    }

    @Override
    public boolean isContradiction() {
        return isContradiction;
    }

    @Override
    public void checkContradiction() {
    }

    public boolean isTautology() {
        return isTautology;
    }

    //TODO Look further into this
    @Override
    public void checkTautology() {
        if (PropositionalLogicHelper.getOutermostOperation(this) == LogicalOperator.IMPLICATION) {
            AST left = this.getSubtree(0);
            AST right = this.getSubtree(1);

            this.isTautology = left.isEquivalentTo(right);
            return;
        }

        if (PropositionalLogicHelper.getOutermostOperation(this) == LogicalOperator.DISJUNCTION) {

        }

    }

    private boolean areNodesEquivalent(PropositionalASTNode node1, PropositionalASTNode node2) {
        if (node1 == null && node2 == null) {
            return true;
        }
        if (node1 == null || node2 == null) {
            return false;
        }

        Object key1 = node1.getKey();
        Object key2 = node2.getKey();

        if (key1 == null && key2 != null) return false;
        if (key1 != null && key2 == null) return false;

        if (key1 != null) {
            String representation1 = (key1 instanceof Predicate p1) ? p1.getRepresentation() : key1.toString();
            String representation2 = (key2 instanceof Predicate p2) ? p2.getRepresentation() : key2.toString();
            if (!representation1.equals(representation2)) {
                return false;
            }
        }

        if (node1.getChildren().size() != node2.getChildren().size()) {
            return false;
        }

        for (int i = 0; i < node1.getChildren().size(); i++) {
            PropositionalASTNode c1 = (PropositionalASTNode) node1.getChildren().get(i);
            PropositionalASTNode c2 = (PropositionalASTNode) node2.getChildren().get(i);
            if (!areNodesEquivalent(c1, c2)) return false;
        }
        return true;
    }

    @Override
    public boolean hasSameStructure(AST other) {
        return hasSameStructureRecursive(this, other);
    }

    private boolean hasSameStructureRecursive(AST pattern, AST target) {
        LogicalOperator patternOperator = PropositionalLogicHelper.getOutermostOperation(pattern);
        LogicalOperator targetOperator  = PropositionalLogicHelper.getOutermostOperation(target);

        if (patternOperator == LogicalOperator.NOT_A_LOGICAL_OPERATOR) return true;
        if (patternOperator != targetOperator) return false;

        PropositionalASTNode nodePattern = (PropositionalASTNode) pattern.getRoot();
        PropositionalASTNode nodeTarget  = (PropositionalASTNode) target.getRoot();

        if (nodePattern.getChildren().size() > nodeTarget.getChildren().size())
            return false;

        for (int i = 0; i < nodePattern.getChildren().size(); i++) {
            AST subtreePattern = pattern.getSubtree(i);
            AST subtreeTarget  = target.getSubtree(i);

            if (!hasSameStructureRecursive(subtreePattern, subtreeTarget))
                return false;
        }

        return true;
    }

    private void ensureChildren(PropositionalASTNode node, int count) {
        while (node.getChildren().size() < count) {
            node.addChild();
        }
    }

    private void enterChildAtCurrentIndex() {
        Predicate predicate = (Predicate) currentNode.getKey();
        if (predicate != null && predicate.getArity() == 0) {
            return;
        }

        List<ASTNode> children = currentNode.getChildren();
        if (currentChildIndex >= children.size()) {
            ensureChildren(currentNode, currentChildIndex + 1);
            children = currentNode.getChildren();
        }
        currentNode = (PropositionalASTNode) children.get(currentChildIndex);
        currentChildIndex = 0;
    }

    private PropositionalASTNode cloneNode(PropositionalASTNode node) {
        PropositionalASTNode copy = new PropositionalASTNode((Predicate) node.getKey());
        for (ASTNode child : node.getChildren()) {
            PropositionalASTNode childCopy = cloneNode((PropositionalASTNode) child);
            childCopy.setParent(copy);
            copy.getChildren().add(childCopy);
        }
        return copy;
    }

    public boolean isAtomic() {
        if (root == null) return false;

        PropositionalASTNode effectiveRoot = root;
        if (effectiveRoot.getKey() == null && effectiveRoot.getChildren().size() == 1) {
            effectiveRoot = (PropositionalASTNode) effectiveRoot.getChildren().getFirst();
        }
        if (effectiveRoot.getKey() == null) return false;

        Predicate predicate = (Predicate) effectiveRoot.getKey();
        if (predicate.getArity() == 0) return true;

        if (predicate.getRepresentation().equals(LogicalOperatorFlyweight.getNegationString())
                && effectiveRoot.getChildren().size() == 1) {
            PropositionalASTNode child = (PropositionalASTNode) effectiveRoot.getChildren().getFirst();
            if (child.getKey() instanceof Predicate childPredicate) {
                return childPredicate.getArity() == 0;
            }
        }

        return false;
    }

    private PropositionalASTNode getFormulaNode() {
        if (root.getKey() != null) return root;
        if (root.getChildren().isEmpty()) return null;
        return (PropositionalASTNode) root.getChildren().getFirst();
    }

}