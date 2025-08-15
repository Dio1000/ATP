package me.dariansandru.domain.predicate;

import me.dariansandru.domain.Notation;
import me.dariansandru.domain.UniverseOfDiscourse;

public interface Predicate {
    UniverseOfDiscourse getUniverseOfDiscourse();
    int getArity();
    String getRepresentation();
    Notation getNotation();
    boolean evaluate(Object... args);
}
