package me.dariansandru.domain.language.function;

import me.dariansandru.domain.language.UniverseOfDiscourse;

public interface Function {
    UniverseOfDiscourse getUniverseOfDiscourse();
    int getArity();
    String getRepresentation();
    Object evaluate(Object... args);
}
