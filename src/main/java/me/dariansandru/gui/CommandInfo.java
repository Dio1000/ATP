package me.dariansandru.gui;

class CommandInfo {
    final String command;
    final int arity;
    final boolean fixedArity;

    CommandInfo(String command, int arity, boolean fixedArity) {
        this.command = command;
        this.arity = arity;
        this.fixedArity = fixedArity;
    }
}