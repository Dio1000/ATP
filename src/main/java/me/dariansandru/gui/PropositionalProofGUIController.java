package me.dariansandru.gui;

import me.dariansandru.domain.data_structures.ast.AST;
import me.dariansandru.domain.data_structures.ast.PropositionalAST;
import me.dariansandru.domain.proof.inference_rules.InferenceRule;
import me.dariansandru.domain.proof.manual_proof.ManualPropositionalProof;
import me.dariansandru.utils.global.GlobalAtomID;
import me.dariansandru.utils.loader.PropositionalLogicLoader;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PropositionalProofGUIController {

    private final JFrame mainFrame;
    private ManualPropositionalProof currentProof;
    private boolean proofCreated = false;

    private final List<AST> knowledgeBaseEntries = new ArrayList<>();
    private final List<AST> goalListData = new ArrayList<>();
    private final DefaultListModel<AST> goalModel = new DefaultListModel<>();

    private final List<JCheckBox> knowledgeBaseCheckboxes = new ArrayList<>();
    private final List<JCheckBox> goalCheckboxes = new ArrayList<>();

    private JPanel knowledgeBaseCheckboxPanel;
    private JPanel goalCheckboxPanel;
    private JTextField knowledgeBaseInputField;
    private JTextField goalInputField;

    private JPanel rulesPanel;
    private JTextArea stateDisplayArea;
    private JTextArea automatedProofOutputArea;

    private final GUIComponents components;
    private final GUIControllerActions actions;

    private final List<AST> selectedKnowledgeBaseFormulas = new ArrayList<>();
    private final List<AST> selectedGoals = new ArrayList<>();
    private final Map<String, CommandInfo> commandMap = new HashMap<>();
    private final List<InferenceRule> customRules = new ArrayList<>();
    private final PropositionalLogicLoader loader = new PropositionalLogicLoader();
    private String loadedPackage = null;

    private String cachedFormalProof = "";
    private String cachedIndentedProof = "";
    private boolean isShowingFormal = true;
    private JButton switchViewButton;

    public PropositionalProofGUIController() {
        this.initializeCommandMap();
        this.installLookAndFeel();
        this.components = new GUIComponents(this);
        this.actions = new GUIControllerActions(this);
        this.mainFrame = this.components.buildMainFrame();
        this.mainFrame.setVisible(true);
        this.refreshGoalCheckboxes();
        warmup();
    }

    public JFrame getMainFrame() { return mainFrame; }
    public List<AST> getKnowledgeBaseEntries() { return knowledgeBaseEntries; }
    public List<AST> getGoalListData() { return goalListData; }
    public DefaultListModel<AST> getGoalModel() { return goalModel; }
    public List<JCheckBox> getKnowledgeBaseCheckboxes() { return knowledgeBaseCheckboxes; }
    public List<JCheckBox> getGoalCheckboxes() { return goalCheckboxes; }
    public JPanel getKnowledgeBaseCheckboxPanel() { return knowledgeBaseCheckboxPanel; }
    public void setKnowledgeBaseCheckboxPanel(JPanel panel) { this.knowledgeBaseCheckboxPanel = panel; }
    public JPanel getGoalCheckboxPanel() { return goalCheckboxPanel; }
    public void setGoalCheckboxPanel(JPanel panel) { this.goalCheckboxPanel = panel; }
    public JTextField getKnowledgeBaseInputField() { return knowledgeBaseInputField; }
    public void setKnowledgeBaseInputField(JTextField field) { this.knowledgeBaseInputField = field; }
    public JTextField getGoalInputField() { return goalInputField; }
    public void setGoalInputField(JTextField field) { this.goalInputField = field; }
    public JPanel getRulesPanel() { return rulesPanel; }
    public void setRulesPanel(JPanel panel) { this.rulesPanel = panel; }
    public JTextArea getStateDisplayArea() { return stateDisplayArea; }
    public void setStateDisplayArea(JTextArea area) { this.stateDisplayArea = area; }
    public JTextArea getAutomatedProofOutputArea() { return automatedProofOutputArea; }
    public void setAutomatedProofOutputArea(JTextArea area) { this.automatedProofOutputArea = area; }
    public List<AST> getSelectedKnowledgeBaseFormulas() { return selectedKnowledgeBaseFormulas; }
    public List<AST> getSelectedGoals() { return selectedGoals; }
    public Map<String, CommandInfo> getCommandMap() { return commandMap; }
    public List<InferenceRule> getCustomRules() { return customRules; }
    public PropositionalLogicLoader getLoader() { return loader; }
    public String getLoadedPackage() { return loadedPackage; }
    public void setLoadedPackage(String pkg) { this.loadedPackage = pkg; }
    public ManualPropositionalProof getCurrentProof() { return currentProof; }
    public void setCurrentProof(ManualPropositionalProof proof) { this.currentProof = proof; }
    public boolean isProofCreated() { return proofCreated; }
    public void setProofCreated(boolean created) { this.proofCreated = created; }
    public JButton getSwitchViewButton() { return switchViewButton; }
    public void setSwitchViewButton(JButton button) { this.switchViewButton = button; }
    public String getCachedFormalProof() { return cachedFormalProof; }
    public void setCachedFormalProof(String proof) { this.cachedFormalProof = proof; }
    public String getCachedIndentedProof() { return cachedIndentedProof; }
    public void setCachedIndentedProof(String proof) { this.cachedIndentedProof = proof; }
    public boolean isShowingFormal() { return isShowingFormal; }
    public void setShowingFormal(boolean formal) { this.isShowingFormal = formal; }
    public List<InferenceRule> getCustomRulesList() { return customRules; }

    public GUIComponents getComponents() { return components; }
    public GUIControllerActions getActions() { return actions; }

    private void installLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        GUITheme.applyLookAndFeel();
    }

    private void initializeCommandMap() {
        this.commandMap.put("IMPLICATION_STRATEGY", new CommandInfo("implstr", 1, true));
        this.commandMap.put("EQUIVALENCE_STRATEGY", new CommandInfo("eqstr", 1, true));
        this.commandMap.put("CONJUNCTION_STRATEGY", new CommandInfo("constr", 1, true));
        this.commandMap.put("DISJUNCTION_STRATEGY", new CommandInfo("disstr", 1, true));
        this.commandMap.put("NEGATION_STRATEGY", new CommandInfo("negstr", 1, true));
        this.commandMap.put("CONTRAPOSITIVE_STRATEGY", new CommandInfo("contrapos", 1, true));
        this.commandMap.put("ABSORPTION", new CommandInfo("absor", 1, true));
        this.commandMap.put("CONJUNCTION_INTRODUCTION", new CommandInfo("conintro", -1, false));
        this.commandMap.put("CONJUNCTION_ELIMINATION", new CommandInfo("conelim", 1, true));
        this.commandMap.put("CONSTRUCTIVE_DILEMMA", new CommandInfo("constrdil", 3, true));
        this.commandMap.put("DESTRUCTIVE_DILEMMA", new CommandInfo("destrdil", 3, true));
        this.commandMap.put("DISJUNCTION_INTRODUCTION", new CommandInfo("disintro", -1, false));
        this.commandMap.put("DISJUNCTION_ELIMINATION", new CommandInfo("diselim", 1, true));
        this.commandMap.put("DISJUNCTIVE_SYLLOGISM", new CommandInfo("dissyll", 2, true));
        this.commandMap.put("EQUIVALENCE_INTRODUCTION", new CommandInfo("eqintro", 2, true));
        this.commandMap.put("EQUIVALENCE_ELIMINATION", new CommandInfo("eqelim", 1, true));
        this.commandMap.put("HYPOTHETICAL_SYLLOGISM", new CommandInfo("hypsyll", 2, true));
        this.commandMap.put("IMPLICATION_INTRODUCTION", new CommandInfo("implintro", 2, true));
        this.commandMap.put("IMPLICATION_ELIMINATION", new CommandInfo("implsimpl", 1, true));
        this.commandMap.put("MODUS_PONENS", new CommandInfo("modpon", 2, true));
        this.commandMap.put("MODUS_TOLLENS", new CommandInfo("modtol", 2, true));
        this.commandMap.put("PROOF_BY_CASES", new CommandInfo("cases", 1, true));
        this.commandMap.put("DISJUNCTION_SIMPLIFICATION", new CommandInfo("dissimpl", 1, true));
        this.commandMap.put("MATERIAL_EQUIVALENCE", new CommandInfo("mateq", 1, true));
        this.commandMap.put("MATERIAL_IMPLICATION", new CommandInfo("matimpl", 1, true));
        this.commandMap.put("DEMORGAN", new CommandInfo("demorgan", 1, true));
        this.commandMap.put("TRANSPOSITION", new CommandInfo("trans", 1, true));
        this.commandMap.put("CONTRADICTION", new CommandInfo("contr", 2, true));
    }

    static String convertToMapKey(String name) {
        return name.toUpperCase().replace(' ', '_');
    }

    private void refreshGoalCheckboxes() {
        if (this.goalCheckboxPanel == null) return;
        this.goalCheckboxPanel.removeAll();
        this.goalCheckboxes.clear();
        for (int i = 0; i < this.goalListData.size(); i++) {
            this.goalCheckboxPanel.add(this.components.buildFormulaRow(i + 1, this.goalListData.get(i), true, false));
        }
        for (JCheckBox checkBox : this.goalCheckboxes) {
            checkBox.setEnabled(this.proofCreated);
        }
        this.goalCheckboxPanel.revalidate();
        this.goalCheckboxPanel.repaint();
    }

    void registerAtoms(List<AST> asts) {
        for (AST ast : asts) {
            for (char character : ast.toString().toCharArray()) {
                if (Character.isUpperCase(character)) {
                    GlobalAtomID.addAtomId(String.valueOf(character));
                }
            }
        }
    }

    private void warmup() {
        for (int i = 0; i < 5000; i++) {
            PropositionalAST dummy = new PropositionalAST("(A -> B) -> C", true);
            dummy.buildBDD();
        }
    }
}