package me.dariansandru.tokenizer;

import me.dariansandru.domain.function.Function;
import me.dariansandru.domain.logical_operator.*;
import me.dariansandru.domain.predicate.Predicate;
import me.dariansandru.domain.signature.Signature;
import me.dariansandru.utils.helper.ErrorHelper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Tokenizer {
    private final List<Predicate> predicates;
    private final List<Function> functions;

    public Tokenizer(Signature signature) {
        this.predicates = signature.getPredicates();
        this.functions = signature.getFunctions();
    }

    public List<Token> tokenize(String line) {
        List<Token> tokens = new ArrayList<>();
        String input = line.trim();

        List<Matchable> matchables = new ArrayList<>();

        for (Predicate p : predicates) {
            matchables.add(new Matchable(p.getRepresentation(), Type.PREDICATE));
        }
        for (Function f : functions) {
            matchables.add(new Matchable(f.getRepresentation(), Type.FUNCTION));
        }

        matchables.add(new Matchable("(", Type.SEPARATOR));
        matchables.add(new Matchable(")", Type.SEPARATOR));
        matchables.add(new Matchable(",", Type.SEPARATOR));

        matchables.add(new Matchable(new Conjunction().getRepresentation(), Type.LOGICAL_OPERATOR));
        matchables.add(new Matchable(new Disjunction().getRepresentation(), Type.LOGICAL_OPERATOR));
        matchables.add(new Matchable(new Implication().getRepresentation(), Type.LOGICAL_OPERATOR));
        matchables.add(new Matchable(new Equivalence().getRepresentation(), Type.LOGICAL_OPERATOR));
        matchables.add(new Matchable(new Negation().getRepresentation(), Type.LOGICAL_OPERATOR));

        matchables.sort(Comparator.comparingInt((Matchable m) -> m.lexeme.length()).reversed());

        int index = 0;
        while (index < input.length()) {
            char c = input.charAt(index);

            if (Character.isWhitespace(c)) {
                index++;
                continue;
            }

            boolean matched = false;
            for (Matchable m : matchables) {
                String lex = m.lexeme;
                if (input.startsWith(lex, index)) {
                    tokens.add(new Token(lex, m.type, index));
                    index += lex.length();
                    matched = true;
                    break;
                }
            }
            if (matched) continue;

            if (Character.isLetterOrDigit(c)) {
                int start = index;
                while (index < input.length() && Character.isLetterOrDigit(input.charAt(index))) {
                    index++;
                }
                String lex = input.substring(start, index);
                tokens.add(new Token(lex, Type.PREDICATE, index));
            }
            else {
                ErrorHelper.add("Unknown symbol at position " + index + ": " + c);
                index++;
            }
        }

        return tokens;
    }

    private static class Matchable {
        String lexeme;
        Type type;

        Matchable(String lexeme, Type type) {
            this.lexeme = lexeme;
            this.type = type;
        }
    }
}
