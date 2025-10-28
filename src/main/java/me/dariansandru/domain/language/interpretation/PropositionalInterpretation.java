package me.dariansandru.domain.language.interpretation;

import me.dariansandru.domain.language.interpretation.exception.InvalidInterpretationException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PropositionalInterpretation implements Interpretation {

    private final Map<String, Boolean> interpretation = new HashMap<>();

    public PropositionalInterpretation(List<String> atoms, List<Boolean> truthValues) {
        if (atoms.isEmpty() || truthValues.isEmpty())
            throw new InvalidInterpretationException("An interpretation cannot have an empty list of atoms or truth values!");
        if (atoms.size() != truthValues.size())
            throw new InvalidInterpretationException("The number of atoms must be equal to the number of truth values!");

        for (int index = 0 ; index < atoms.size() ; index++) {
            interpretation.put(atoms.get(index), truthValues.get(index));
        }
    }

    @Override
    public Object getValue(Object value) {
        return interpretation.get(value);
    }
}
