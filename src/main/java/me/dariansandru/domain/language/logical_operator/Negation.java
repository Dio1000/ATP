package me.dariansandru.domain.language.logical_operator;

import me.dariansandru.domain.language.Notation;
import me.dariansandru.domain.language.UniverseOfDiscourse;
import me.dariansandru.domain.language.predicate.Predicate;

public class Negation implements Predicate {

    public Negation() {

    }

    @Override
    public UniverseOfDiscourse getUniverseOfDiscourse() {
        return UniverseOfDiscourse.PROPOSITIONS;
    }

    @Override
    public int getArity() {
        return 1;
    }

    @Override
    public String getRepresentation() {
        return "!";
    }

    @Override
    public Notation getNotation() {
        return Notation.PREFIX;
    }

    @Override
    public boolean evaluate(Object... args) {
        if (args.length != 1)
            throw new IllegalStateException("Negation requires exactly 1 argument!");
        if (!(args[0] instanceof Predicate operand))
            throw new IllegalStateException("Negation argument must be a predicate with arity 0!");

        if (operand.getArity() != 0)
            throw new IllegalStateException("Negation argument must be a proposition (arity 0)!");

        return !operand.evaluate();
    }
}
