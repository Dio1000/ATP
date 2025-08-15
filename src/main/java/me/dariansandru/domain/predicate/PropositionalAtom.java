package me.dariansandru.domain.predicate;

import me.dariansandru.domain.Notation;
import me.dariansandru.domain.UniverseOfDiscourse;

public class PropositionalAtom implements Predicate {
    private final String name;
    private final boolean truthValue;

    public PropositionalAtom(String name, boolean truthValue) {
        this.name = name;
        this.truthValue = truthValue;
    }

    @Override
    public UniverseOfDiscourse getUniverseOfDiscourse() {
        return UniverseOfDiscourse.PROPOSITIONS;
    }

    @Override
    public int getArity() {
        return 0;
    }

    @Override
    public String getRepresentation() {
        return name;
    }

    @Override
    public Notation getNotation() {
        return Notation.PREFIX;
    }

    @Override
    public boolean evaluate(Object... args) {
        return truthValue;
    }
}
