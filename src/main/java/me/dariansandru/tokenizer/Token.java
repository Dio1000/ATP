package me.dariansandru.tokenizer;

public record Token(String lexeme, Type type, int position) {

    @Override
    public String toString() {
        return "Token: Lexeme - " + lexeme + "; Type: " + type + "; Position: " + position;
    }
}
