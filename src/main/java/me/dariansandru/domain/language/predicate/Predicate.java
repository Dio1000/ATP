package me.dariansandru.domain.language.predicate;

import me.dariansandru.domain.language.Notation;
import me.dariansandru.domain.language.UniverseOfDiscourse;

/*
Implementing this interface allows the user to create a new type of Predicate.
This is useful when creating a new Signature, which requires their own list of predicates.
The interface allows describing of a predicate, by providing the arity, representation,
notation (Prefix, Infix, Postfix) and a method that evaluates any number of arguments.
Note: Predicates return boolean values, whereas functions return any type of object.
*/
public interface Predicate {
    UniverseOfDiscourse getUniverseOfDiscourse();
    int getArity();
    String getRepresentation();
    Notation getNotation();
    boolean evaluate(Object... args);
}
