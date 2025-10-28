package me.dariansandru.parser.parsers;

import me.dariansandru.domain.language.signature.PropositionalSignature;
import me.dariansandru.domain.language.signature.Signature;
import me.dariansandru.domain.data_structures.ast.AST;
import me.dariansandru.domain.data_structures.ast.PropositionalAST;

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
    public void parseEntry(String line, int index) {
        PropositionalAST ast = new PropositionalAST(line);
        boolean valid = ast.validate(index);
        if (valid) astList.add(ast);

    }

    @Override
    public void parse(List<String> lines) {
        int index = 1;
        for (String line : lines) {
            parseEntry(line, index);
            index++;
        }
    }

    @Override
    public List<AST> getASTList() {
        return new ArrayList<>(astList);
    }

    @Override
    public List<AST> parseAndGetASTs(List<String> lines) {
        astList.clear();
        parse(lines);
        return getASTList();
    }
}
