package me.dariansandru.utils.factory;

import me.dariansandru.domain.language.logical_operator.*;
import me.dariansandru.domain.language.predicate.Predicate;
import me.dariansandru.domain.language.predicate.PropositionalAtom;
import me.dariansandru.tokenizer.Token;
import me.dariansandru.tokenizer.Type;

public class PropositionalPredicateFactory {

    public static Predicate createPredicate(Token token) {
        String lexeme = token.lexeme();
        Type type = token.type();

        String conjunction = new Conjunction().getRepresentation();
        String disjunction = new Disjunction().getRepresentation();
        String implication = new Implication().getRepresentation();
        String equivalence = new Equivalence().getRepresentation();
        String negation = new Negation().getRepresentation();

        switch (type) {
            case LOGICAL_OPERATOR -> {
                if (lexeme.equals(conjunction)) return new Conjunction();
                else if (lexeme.equals(disjunction)) return new Disjunction();
                else if (lexeme.equals(implication)) return new Implication();
                else if (lexeme.equals(equivalence)) return new Equivalence();
                else if (lexeme.equals(negation)) return new Negation();
                else throw new IllegalStateException(lexeme + " is not the representation of any logical operator!");
            }
            case PREDICATE -> {
                if (lexeme.matches("^[A-Z][0-9]*$")) {
                    return new PropositionalAtom(lexeme, false);
                }
                throw new IllegalArgumentException("Invalid propositional atom: " + lexeme);
            }
            case FUNCTION -> throw new UnsupportedOperationException("Function predicates are not supported in Propositional Logic.");
            default -> throw new IllegalStateException(type + " is not a valid type!");
        }
    }
}
