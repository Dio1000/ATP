package me.dariansandru.domain.language.interpretation;

import me.dariansandru.domain.language.interpretation.exception.InvalidInterpretationException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PropositionalPartialInterpretation implements Interpretation{

    private final Map<String, Integer> interpretation = new HashMap<>();

    public PropositionalPartialInterpretation(List<String> atoms, List<Integer> truthValues) {
        if (atoms.isEmpty() || truthValues.isEmpty())
            throw new InvalidInterpretationException("An interpretation cannot have an empty list of atoms or truth values!");

        for (int index = 0 ; index < atoms.size() ; index++) {
            interpretation.put(atoms.get(index), truthValues.get(index));
        }
    }

    @Override
    public Object getValue(Object value) {
        Integer integer = interpretation.get(value);
        if (integer == 1) return true;
        else if (integer == 0) return false;
        else return -1;
    }

}
