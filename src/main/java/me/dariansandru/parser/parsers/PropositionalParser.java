package me.dariansandru.parser.parsers;

import me.dariansandru.domain.signature.PropositionalSignature;
import me.dariansandru.domain.signature.Signature;
import me.dariansandru.utils.data_structures.ast.AST;
import me.dariansandru.utils.data_structures.ast.PropositionalAST;

import java.util.ArrayList;
import java.util.List;

public class PropositionalParser implements FormulaParser {

    private final PropositionalSignature propositionalSignature = new PropositionalSignature();
    private final List<AST> astList = new ArrayList<>();

    @Override
    public Signature getSignature() {
        return propositionalSignature;
    }

    @Override
    public boolean parseEntry(String line, int index) {
        PropositionalAST ast = new PropositionalAST(line);
        boolean valid = ast.validate(index);
        if (valid) astList.add(ast);

        return valid;
    }

    @Override
    public boolean parse(List<String> lines) {
        boolean valid = true;
        int index = 1;

        for (String line : lines) {
            if (!parseEntry(line, index)) valid = false;
            index++;
        }

        return valid;
    }

    @Override
    public List<AST> getASTList() {
        return new ArrayList<>(astList);
    }

    public List<AST> parseAndGetASTs(List<String> lines) {
        astList.clear();
        parse(lines);
        return getASTList();
    }
}
