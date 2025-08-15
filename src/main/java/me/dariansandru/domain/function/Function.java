package me.dariansandru.domain.function;

import me.dariansandru.domain.UniverseOfDiscourse;

public interface Function {
    UniverseOfDiscourse getUniverseOfDiscourse();
    int getArity();
    String getRepresentation();
    Object evaluate(Object... args);
}
