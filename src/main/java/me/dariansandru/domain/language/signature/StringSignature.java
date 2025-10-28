package me.dariansandru.domain.language.signature;

import me.dariansandru.domain.language.UniverseOfDiscourse;
import me.dariansandru.domain.language.function.Function;
import me.dariansandru.domain.language.predicate.Predicate;

import java.util.List;

public class StringSignature implements Signature{
    @Override
    public List<Predicate> getPredicates() {
        return List.of();
    }

    @Override
    public List<Function> getFunctions() {
        return List.of();
    }

    @Override
    public UniverseOfDiscourse getUniverseOfDiscourse() {
        return null;
    }
}
