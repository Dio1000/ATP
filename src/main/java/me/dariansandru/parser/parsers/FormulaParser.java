package me.dariansandru.parser.parsers;

import me.dariansandru.domain.signature.Signature;
import me.dariansandru.utils.data_structures.ast.AST;

import java.util.List;

public interface FormulaParser {
    Signature getSignature();
    boolean parseEntry(String line, int index);
    boolean parse(List<String> lines);

    List<AST> getASTList();
}
