package me.dariansandru.utils.manual;

import java.util.List;

public record ManualEntry(String name, String commandName, List<String> aliases, int arity, String description, String inference) {

}
