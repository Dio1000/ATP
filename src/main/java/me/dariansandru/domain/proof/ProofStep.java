package me.dariansandru.domain.proof;

public class ProofStep {
    private final String text;
    private final int indent;

    public ProofStep(String text, int indent) {
        this.text = text;
        this.indent = indent;
    }

    public String getText() {
        return text;
    }

    public int getIndent() {
        return indent;
    }
}
