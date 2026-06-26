package me.dariansandru.domain.language.interpretation;

/*
Implementing this interface allows the user to create a new type of Interpretation.
This is useful when creating new Universes of Discourse. The new formulas require
their own special interpretations, which hold unique types of values.
 */
public interface Interpretation {
    Object getValue(Object value);
}
