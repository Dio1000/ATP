package me.dariansandru.domain.language.signature;

import me.dariansandru.domain.language.UniverseOfDiscourse;
import me.dariansandru.domain.language.function.Function;
import me.dariansandru.domain.language.predicate.Predicate;

import java.util.List;

/**
Implementing this method allows for the creation of a new Signature.
This is useful when implementing a new Universe of Discourse, each having their own Signature.
Signatures store the predicates and functions of a Universe of Discourse.
 */
public interface Signature {
    List<Predicate> getPredicates();
    List<Function> getFunctions();
    UniverseOfDiscourse getUniverseOfDiscourse();
}
