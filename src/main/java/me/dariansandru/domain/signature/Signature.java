package me.dariansandru.domain.signature;

import me.dariansandru.domain.UniverseOfDiscourse;
import me.dariansandru.domain.function.Function;
import me.dariansandru.domain.predicate.Predicate;

import java.util.List;

public interface Signature {
    List<Predicate> getPredicates();
    List<Function> getFunctions();
    UniverseOfDiscourse getUniverseOfDiscourse();
}
