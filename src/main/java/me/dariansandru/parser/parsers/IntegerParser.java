package me.dariansandru.parser.parsers;

import me.dariansandru.domain.signature.Signature;

import java.util.List;

public class IntegerParser implements FormulaParser {

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
}
