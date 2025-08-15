package me.dariansandru.parser.parsers;

import me.dariansandru.domain.signature.PropositionalSignature;
import me.dariansandru.domain.signature.Signature;
import me.dariansandru.utils.data_structures.ast.PropositionalAST;

import java.util.List;

public class PropositionalParser implements FormulaParser {

    PropositionalSignature propositionalSignature = new PropositionalSignature();

    @Override
    public Signature getSignature() {
        return propositionalSignature;
    }

    @Override
    public boolean parseEntry(String line, int index) {
        PropositionalAST ast = new PropositionalAST(line);
        return ast.validate(index);
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

}
