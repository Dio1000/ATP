package me.dariansandru.domain.language.function;

import me.dariansandru.domain.language.UniverseOfDiscourse;

/*
Implementing this interface allows the user to extend the system with a new type of function.
This is helpful when implementing new Signatures, all of each have their own functions.
This interface allows describing of a function, by providing the arity, representation
and a method that evaluates any number of arguments.
Note: Functions return any type of object, whereas predicates return booleans.
 */
public interface Function {
    UniverseOfDiscourse getUniverseOfDiscourse();
    int getArity();
    String getRepresentation();
    Object evaluate(Object... args);
}
