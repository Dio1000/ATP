package me.dariansandru.parser.parsers;

import me.dariansandru.domain.signature.Signature;
import me.dariansandru.utils.data_structures.ast.AST;

import java.util.List;

public class RationalParser implements FormulaParser{

    @Override
    public Signature getSignature() {
        return null;
    }

    @Override
    public boolean parseEntry(String line, int index) {
        return false;
    }

    @Override
    public boolean parse(List<String> lines) {
        return false;
    }

    @Override
    public List<AST> getASTList() {
        return List.of();
    }

}
