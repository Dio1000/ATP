package me.dariansandru.domain.logical_operator;

import me.dariansandru.domain.Notation;
import me.dariansandru.domain.UniverseOfDiscourse;
import me.dariansandru.domain.predicate.Predicate;

public class Implication implements Predicate {

    public Implication() {

    }

    @Override
    public UniverseOfDiscourse getUniverseOfDiscourse() {
        return UniverseOfDiscourse.PROPOSITIONS;
    }

    @Override
    public int getArity() {
        return 2;
    }

    @Override
    public String getRepresentation() {
        return "->";
    }

    @Override
    public Notation getNotation() {
        return Notation.INFIX;
    }

    @Override
    public boolean evaluate(Object... args) {
        if (args.length != 2)
            throw new IllegalStateException("Implication requires 2 arguments!");
        if (!(args[0] instanceof Predicate left) || !(args[1] instanceof Predicate right))
            throw new IllegalStateException("Implication arguments must be predicates with arity 0!");

        if (left.getArity() != 0 || right.getArity() != 0)
            throw new IllegalStateException("Implication arguments must be propositions (arity 0)!");

        return !left.evaluate() || right.evaluate();
    }
}
