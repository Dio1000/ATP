package me.dariansandru.domain.logical_operator;

import me.dariansandru.domain.Notation;
import me.dariansandru.domain.UniverseOfDiscourse;
import me.dariansandru.domain.predicate.Predicate;

public class Conjunction implements Predicate {

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
        return "AND";
    }

    @Override
    public Notation getNotation() {
        return Notation.INFIX;
    }

    @Override
    public boolean evaluate(Object... args) {
        if (args.length != 2) {
            throw new IllegalStateException("Conjunction only takes 2 arguments!");
        }
        if (!(args[0] instanceof Predicate left) || !(args[1] instanceof Predicate right)) {
            throw new IllegalStateException("Conjunction takes 2 predicate arguments!");
        }

        if (left.getArity() != 0 || right.getArity() != 0) {
            throw new IllegalStateException("Conjunction arguments must be propositions (arity 0)!");
        }

        return left.evaluate() && right.evaluate();
    }

}
