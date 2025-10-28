package me.dariansandru.domain.language.signature;

import me.dariansandru.domain.language.UniverseOfDiscourse;
import me.dariansandru.domain.language.function.Function;
import me.dariansandru.domain.language.predicate.Predicate;

import java.util.List;

public interface Signature {
    List<Predicate> getPredicates();
    List<Function> getFunctions();
    UniverseOfDiscourse getUniverseOfDiscourse();
}
