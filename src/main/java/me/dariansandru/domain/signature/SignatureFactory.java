package me.dariansandru.domain.signature;

import me.dariansandru.domain.UniverseOfDiscourse;

public class SignatureFactory {

    public static Signature createSignature(UniverseOfDiscourse universe) {
        return switch (universe) {
            case PROPOSITIONS -> new PropositionalSignature();
            case INTEGER_NUMBERS -> new IntegerSignature();
            case RATIONAL_NUMBERS -> new RationalSignature();
            case REAL_NUMBERS -> new RealSignature();
            case STRINGS -> new StringSignature();

            default -> throw new IllegalArgumentException("Unknown or unsupported universe: " + universe);
        };
    }

}
