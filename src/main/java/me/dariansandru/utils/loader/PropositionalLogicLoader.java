package me.dariansandru.utils.loader;

import me.dariansandru.domain.data_structures.ast.AST;
import me.dariansandru.domain.data_structures.ast.PropositionalAST;
import me.dariansandru.domain.proof.inference_rules.CustomPropositionalInferenceRule;
import me.dariansandru.io.InputDevice;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PropositionalLogicLoader implements Loader {

    @Override
    public List<String> getLines(String path) {
        return InputDevice.read(path);
    }

    @Override
    public CustomPropositionalInferenceRule loadCustomRule(List<String> lines, String ruleName) {
        List<AST> antecedents = new ArrayList<>();
        AST conclusion = null;

        int index = 0;
        while (index < lines.size()) {
            String line = lines.get(index).trim();

            if (line.equals(ruleName)) {
                index++;
                while (index < lines.size()) {
                    line = lines.get(index).trim();

                    if (line.equals("end")) {
                        break;
                    }

                    if (line.startsWith("a:")) {
                        String[] parts = line.split(":", 2);
                        PropositionalAST ast = new PropositionalAST(parts[1].trim(), true);
                        antecedents.add(ast);
                    }
                    else if (line.startsWith("c:")) {
                        String[] parts = line.split(":", 2);
                        conclusion = new PropositionalAST(parts[1].trim(), true);
                    }

                    index++;
                }
                break;
            }
            else index++;
        }

        String parsedRuleName = ruleName.contains("=") ? ruleName.split("=")[1].trim() : ruleName;
        antecedents.sort(Comparator.comparingInt(AST::getLength));
        return new CustomPropositionalInferenceRule(parsedRuleName, antecedents, conclusion);
    }


}
