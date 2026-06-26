package me.dariansandru.parser.parsers;

import me.dariansandru.domain.language.signature.Signature;
import me.dariansandru.domain.data_structures.ast.AST;

import java.util.List;

/*
Implementing this interface allows the user to extend the system with a new FormulaParser.
This is useful when creating new Universes of Discourse, since they have their own formulas,
thus requiring unique, specialised parsers.
The methods include getting the Signature that the Formula belongs to, parsing an entry
and parsing a whole list of lines (that contain entries), and parsing and fetching ASTs
from a list of lines.
 */
public interface FormulaParser {
    Signature getSignature();

    void parseEntry(String line, int index);
    void parse(List<String> lines);

    List<AST> getASTList();
    List<AST> parseAndGetASTs(List<String> lines);
}
