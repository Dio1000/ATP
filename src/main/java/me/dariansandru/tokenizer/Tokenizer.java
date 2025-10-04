package me.dariansandru.tokenizer;

import me.dariansandru.domain.function.Function;
import me.dariansandru.domain.predicate.Predicate;
import me.dariansandru.domain.signature.PropositionalSignature;
import me.dariansandru.domain.signature.Signature;
import me.dariansandru.parser.command.Command;
import me.dariansandru.utils.flyweight.LogicalOperatorFlyweight;
import me.dariansandru.utils.helper.ErrorHelper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Tokenizer {
    private final Signature signature;
    private final List<Predicate> predicates;
    private final List<Function> functions;

    public Tokenizer(Signature signature) {
        this.signature = signature;
        this.predicates = signature.getPredicates();
        this.functions = signature.getFunctions();
    }

    public List<Token> tokenize(String line) {
        List<Token> tokens = new ArrayList<>();
        String input = line.trim();

        List<Matchable> matchableList = new ArrayList<>();

        for (Predicate predicate : predicates) {
            matchableList.add(new Matchable(predicate.getRepresentation(), Type.PREDICATE));
        }
        for (Function function : functions) {
            matchableList.add(new Matchable(function.getRepresentation(), Type.FUNCTION));
        }
        for (Command command : Command.getAllCommands()) {
            matchableList.add(new Matchable(command.toString(), Type.COMMAND));
        }

        matchableList.add(new Matchable("(", Type.SEPARATOR));
        matchableList.add(new Matchable(")", Type.SEPARATOR));
        matchableList.add(new Matchable(",", Type.SEPARATOR));

        matchableList.add(new Matchable(LogicalOperatorFlyweight.getConjunctionString(), Type.LOGICAL_OPERATOR));
        matchableList.add(new Matchable(LogicalOperatorFlyweight.getDisjunctionString(), Type.LOGICAL_OPERATOR));
        matchableList.add(new Matchable(LogicalOperatorFlyweight.getImplicationString(), Type.LOGICAL_OPERATOR));
        matchableList.add(new Matchable(LogicalOperatorFlyweight.getEquivalenceString(), Type.LOGICAL_OPERATOR));
        matchableList.add(new Matchable(LogicalOperatorFlyweight.getNegationString(), Type.LOGICAL_OPERATOR));

        matchableList.sort(Comparator.comparingInt((Matchable m) -> m.lexeme.length()).reversed());

        int index = 0;
        while (index < input.length()) {
            char c = input.charAt(index);

            if (Character.isWhitespace(c)) {
                index++;
                continue;
            }

            boolean matched = false;
            for (Matchable m : matchableList) {
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
                if (signature instanceof PropositionalSignature) tokens.add(new Token(lex, Type.PREDICATE, index));
                else tokens.add(new Token(lex, Type.IDENTIFIER, index));
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
