package me.dariansandru.domain.language.logical_operator;

import me.dariansandru.domain.language.Notation;
import me.dariansandru.domain.language.UniverseOfDiscourse;
import me.dariansandru.domain.language.predicate.Predicate;

public class Disjunction implements Predicate{

    public Disjunction() {

    }

    @Override
    public UniverseOfDiscourse getUniverseOfDiscourse() {
        return UniverseOfDiscourse.PROPOSITIONS;
    }

    @Override
    public int getArity() {
        return -1;
    }

    @Override
    public String getRepresentation() {
        return "OR";
    }

    @Override
    public Notation getNotation() {
        return Notation.INFIX;
    }

    @Override
    public boolean evaluate(Object... args) {
        if (args.length != 2) {
            throw new IllegalStateException("Disjunction only takes 2 arguments!");
        }
        if (!(args[0] instanceof Predicate left) || !(args[1] instanceof Predicate right)) {
            throw new IllegalStateException("Disjunction takes 2 predicate arguments!");
        }

        if (left.getArity() != 0 || right.getArity() != 0) {
            throw new IllegalStateException("Disjunction arguments must be propositions (arity 0)!");
        }

        return left.evaluate() || right.evaluate();
    }
}
