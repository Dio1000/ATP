package me.dariansandru.parser.command;

import me.dariansandru.domain.signature.UniversalSignature;
import me.dariansandru.tokenizer.Token;
import me.dariansandru.tokenizer.Tokenizer;
import me.dariansandru.tokenizer.Type;
import me.dariansandru.utils.helper.ErrorHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class CommandParser {

    private static final List<String> arguments = new ArrayList<>();
    private static Command currentCommand;

    public static boolean parse(String command) {
        arguments.clear();
        Tokenizer tokenizer = new Tokenizer(new UniversalSignature());
        List<Token> tokens = tokenizer.tokenize(command);
        int tokenSize = tokens.size();

        int index = 0;
        while (index < tokenSize) {
            Token token = tokens.get(index);
            if (index == 0 && token.type() != Type.COMMAND) {
                ErrorHelper.add("Command " + token.lexeme() + " does not exist!");
                break;
            }
            if (token.type() == Type.COMMAND) {
                if (!Command.contains(token.lexeme())) {
                    ErrorHelper.add("Command: " + token.lexeme() + " does not exist!");
                }
                currentCommand = Command.getFromString(token.lexeme());
            }
            else if (Objects.equals(token.lexeme(), "(") && token.type() == Type.SEPARATOR) {
                if (index == tokenSize - 1) {
                    ErrorHelper.add("Command cannot end with '(' separator, use ')' instead!");
                }
                else {
                    Token previousToken = tokens.get(index - 1);
                    if (index + 1 >= tokenSize) {
                        ErrorHelper.add("Command cannot end with '(' separator, use ')' instead!");
                        break;
                    }
                    Token nextToken = tokens.get(index + 1);

                    if (previousToken.type() != Type.COMMAND) {
                        ErrorHelper.add(token.position() + ": Unexpected separator '" + token.lexeme() + "' at this position!");
                    }
                    if (nextToken.type() != Type.IDENTIFIER) {
                        ErrorHelper.add(token.position() + ": Expected identifier after separator '" + token.lexeme() + "' at this position!");
                    }
                }

            }
            else if (token.type() == Type.IDENTIFIER) {
                index = parseIdentifier(tokens, index);
                if (index != tokenSize - 1) {
                    ErrorHelper.add("Command must end with a ')' separator!");
                }
            }
            else {
                ErrorHelper.add(token.position() + ": Unexpected token at this position!");
            }

            index++;
        }

        return !ErrorHelper.notEmpty();
    }
    
    private static int parseIdentifier(List<Token> tokens, int index) {
        int tokenSize = tokens.size();
        int identifierCount = 0;

        Token token = tokens.get(index);
        if (tokens.get(index - 2).type() != Type.COMMAND) {
            ErrorHelper.add(token.position() + ": Expected command before argument '" + token.lexeme() + "'!");
        }
        int tokenArity = Command.getFromString(tokens.get(index - 2).lexeme()).getArity();

        while (index < tokenSize) {
            token = tokens.get(index);
            Token previousToken = tokens.get(index - 1);

            if (index + 1 >= tokenSize) {
                if (!Objects.equals(token.lexeme(), ")")) {
                    ErrorHelper.add("Expected ')' at the end of the command!");
                }
                break;
            }
            Token nextToken = tokens.get(index + 1);

            if (token.type() == Type.IDENTIFIER) {
                arguments.add(token.lexeme());
                identifierCount++;
            }
            else if (Objects.equals(token.lexeme(), ",") && token.type() == Type.SEPARATOR)  {
                if (previousToken.type() != Type.IDENTIFIER || nextToken.type() != Type.IDENTIFIER) {
                    ErrorHelper.add(token.position() + ": Comma separator expected between two identifiers!");
                }
            }
            else if (Objects.equals(token.lexeme(), ")") && token.type() == Type.SEPARATOR)  {
                if (previousToken.type() != Type.IDENTIFIER) {
                    ErrorHelper.add(token.position() + ": Expected argument before ')'!");
                }
            }
            else {
                ErrorHelper.add(token.position() + ": Unexpected token at this position!");
            }
            index++;
        }

        if (!currentCommand.isFixed() && identifierCount > tokenArity) {
            ErrorHelper.add("Expected a maximum of " + tokenArity + " arguments but received " + identifierCount + "!");
        }
        else if (!currentCommand.isFixed() && identifierCount == 0) {
            ErrorHelper.add("Expected at least one argument and a maximum of " + tokenArity);
        }
        else if (currentCommand.isFixed() && identifierCount != tokenArity) {
            if (tokenArity == 1) ErrorHelper.add("Expected " + tokenArity + " argument but received " + identifierCount + "!");
            else ErrorHelper.add("Expected " + tokenArity + " arguments but received " + identifierCount + "!");
        }
        
        return index;
    }

    public static List<String> getArguments(String command) {
        return arguments;
    }

    public static Command getCurrentCommand() {
        return currentCommand;
    }

}
