package me.dariansandru.parser.parsers;

import me.dariansandru.domain.signature.Signature;

import java.util.List;

public interface FormulaParser {
    Signature getSignature();
    boolean parseEntry(String line, int index);
    boolean parse(List<String> lines);
}
