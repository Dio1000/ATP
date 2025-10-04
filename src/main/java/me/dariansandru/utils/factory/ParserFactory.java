package me.dariansandru.utils.factory;

import me.dariansandru.domain.UniverseOfDiscourse;
import me.dariansandru.domain.signature.*;
import me.dariansandru.parser.parsers.*;

public class ParserFactory {

    public static FormulaParser createParser(Signature signature) {
        UniverseOfDiscourse universeOfDiscourse = signature.getUniverseOfDiscourse();
        return switch (universeOfDiscourse) {
            case PROPOSITIONS -> new PropositionalParser();
            case INTEGER_NUMBERS -> new IntegerParser();
            case RATIONAL_NUMBERS -> new RationalParser();
            case REAL_NUMBERS -> new RealParser();
            case STRINGS -> new StringParser();

            default -> throw new IllegalArgumentException("Unknown or unsupported universe: " + universeOfDiscourse);
        };
    }
}
