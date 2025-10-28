package me.dariansandru.domain.language.predicate;

import me.dariansandru.domain.language.Notation;
import me.dariansandru.domain.language.UniverseOfDiscourse;

public interface Predicate {
    UniverseOfDiscourse getUniverseOfDiscourse();
    int getArity();
    String getRepresentation();
    Notation getNotation();
    boolean evaluate(Object... args);
}
