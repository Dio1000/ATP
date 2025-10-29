package me.dariansandru.domain.data_structures.bdd;

import me.dariansandru.domain.data_structures.ast.AST;
import me.dariansandru.domain.data_structures.ast.PropositionalAST;
import me.dariansandru.domain.language.interpretation.PropositionalPartialInterpretation;
import me.dariansandru.utils.helper.PropositionalLogicHelper;

import java.util.*;

public class PropositionalBDDBuilder {

    private PropositionalBDDNode root;
    private final PropositionalAST ast;
    private List<AST> atomList = new ArrayList<>();
    private final List<String> atomStringList = new ArrayList<>();

    public PropositionalBDDBuilder(PropositionalAST ast) {
        this.ast = ast;
        this.root = null;
    }

    public void buildBDD() {
        Set<AST> atoms = PropositionalLogicHelper.getAtoms(ast);
        atomList = atoms.stream()
                .sorted(Comparator.comparing(AST::toString)).distinct().toList();
        for (AST astString : atomList) atomStringList.add(astString.toString());

        this.root = new PropositionalBDDNode(new PropositionalAST(atomList.getFirst().toString(), true));
        buildRecursive(root);
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
        if (astLeft.isTautology() || astLeft.isContradiction())
            node.addLeftChild(astLeft);
        else
            node.addLeftChild(new PropositionalAST(atomList.get(depth + 1).toString(), true));

        PropositionalAST astRight = ast.evaluatePartial(partialInterpretationFalse);
        if (astRight.isTautology() || astRight.isContradiction())
            node.addRightChild(astRight);
        else
            node.addRightChild(new PropositionalAST(atomList.get(depth + 1).toString(), true));

        buildRecursive(node.getLeft());
        buildRecursive(node.getRight());
    }

    public PropositionalBDDNode getRoot() {
        return root;
    }

    public PropositionalAST getAst() {
        return ast;
    }

}
