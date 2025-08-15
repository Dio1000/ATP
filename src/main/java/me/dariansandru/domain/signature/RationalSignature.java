package me.dariansandru.domain.signature;

import me.dariansandru.domain.UniverseOfDiscourse;
import me.dariansandru.domain.function.Function;
import me.dariansandru.domain.predicate.Predicate;

import java.util.List;

public class RationalSignature implements Signature{
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
