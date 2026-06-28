package me.dariansandru.domain.language.interpretation;

import me.dariansandru.domain.language.interpretation.exception.InvalidInterpretationException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Partial Interpretation with the property that not all atoms from the
 * interpretation are assigned a truth value. Thus, this structure is useful
 * when building BDDs, helping in checking which atoms in the formula
 * are redundant (An atom is redundant if, for any interpretation of a formula,
 * the truth value is the same regardless of the atom).
 * For example: A -> (B OR C), and the Partial Interpretation
 * {A - True, B - True, C - unassigned}, the formula becomes True -> (True or C)
 * But since (True or C) is True, regardless of the truth value of C, the formula
 * becomes: True -> True, which is True. This means that the truth value of the
 * formula, when A is true and B is true, is the same regardless of C.
 */
public class PropositionalPartialInterpretation implements Interpretation {

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
        if (value == null || value == "") return -1;
        Integer integer = interpretation.get(value);

        if (integer == 1) return true;
        else if (integer == 0) return false;
        else return -1;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (String key : interpretation.keySet()) {
            builder.append(key).append("-> ").append(interpretation.get(key)).append(" ");
        }
        return builder.append("\n").toString();
    }

}
