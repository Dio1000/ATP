package me.dariansandru.parser.parsers;

import me.dariansandru.domain.language.signature.Signature;
import me.dariansandru.domain.data_structures.ast.AST;

import java.util.List;

public interface FormulaParser {
    Signature getSignature();

    void parseEntry(String line, int index);
    void parse(List<String> lines);

    List<AST> getASTList();
    List<AST> parseAndGetASTs(List<String> lines);
}
