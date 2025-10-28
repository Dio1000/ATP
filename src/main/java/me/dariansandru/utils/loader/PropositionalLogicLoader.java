package me.dariansandru.utils.loader;

import me.dariansandru.domain.data_structures.ast.AST;
import me.dariansandru.domain.data_structures.ast.PropositionalAST;
import me.dariansandru.domain.proof.inference_rules.CustomPropositionalInferenceRule;
import me.dariansandru.io.InputDevice;

import java.util.ArrayList;
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
            String line = lines.get(index);
            StringBuilder builder = new StringBuilder();
            builder.append("rule='").append(ruleName).append("'");

            if (line.contentEquals(builder)) {
                while (line.equals("end")) {
                    line = lines.get(index);

                    if (line.startsWith("a:")) {
                        String[] parts = line.split(":");
                        PropositionalAST ast = new PropositionalAST(parts[1].trim(), true);
                        antecedents.add(ast);
                    }
                    else if (line.startsWith("c:")) {
                        String[] parts = line.split(":");
                        conclusion = new PropositionalAST(parts[1].trim(), true);
                    }
                    index++;
                }
            }

            index++;
        }

        return new CustomPropositionalInferenceRule(ruleName.split("=")[1].trim(), antecedents, conclusion);
    }

}
