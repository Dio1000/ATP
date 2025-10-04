package me.dariansandru.parser.parsers;

import me.dariansandru.domain.signature.Signature;
import me.dariansandru.domain.data_structures.ast.AST;

import java.util.List;

public class IntegerParser implements FormulaParser {

    @Override
    public Signature getSignature() {
        return null;
    }

    @Override
    public void parseEntry(String line, int index) {
    }

    @Override
    public void parse(List<String> lines) {
    }

    @Override
    public List<AST> getASTList() {
        return List.of();
    }

    @Override
    public List<AST> parseAndGetASTs(List<String> lines) {
        return List.of();
    }
}
