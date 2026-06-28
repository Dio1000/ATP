package me.dariansandru.gui;

import me.dariansandru.domain.data_structures.ast.AST;
import me.dariansandru.domain.data_structures.ast.PropositionalAST;
import me.dariansandru.domain.language.UniverseOfDiscourse;
import me.dariansandru.domain.language.signature.Signature;
import me.dariansandru.domain.language.signature.SignatureFactory;
import me.dariansandru.domain.proof.automated_proof.PropositionalProof;
import me.dariansandru.domain.proof.inference_rules.InferenceRule;
import me.dariansandru.domain.proof.inference_rules.custom.CustomPropositionalInferenceRule;
import me.dariansandru.domain.proof.inference_rules.propositional.ContradictionRule;
import me.dariansandru.domain.proof.manual_proof.ManualPropositionalProof;
import me.dariansandru.domain.proof.manual_proof.ManualPropositionalProofStates;
import me.dariansandru.domain.proof.manual_proof.helper.ManualPropositionalInferenceRuleHelper;
import me.dariansandru.domain.proof.manual_proof.helper.ManualPropositionalStrategyHelper;
import me.dariansandru.domain.proof.Strategy;
import me.dariansandru.utils.helper.KnowledgeBaseRegistry;
import me.dariansandru.utils.helper.ProofTextHelper;
import me.dariansandru.utils.loader.PropositionalLogicLoader;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GUIControllerActions {

    private final PropositionalProofGUIController controller;
    private final GUIComponents components;

    private JButton createProofButton;
    private JButton loadPackageButton;
    private JButton createEditorButton;
    private JButton automateButton;
    private JTextField stateIndexField;
    private JPanel automatedControlPanel;
    private JPanel automatedContainerPanel;
    private JSplitPane topSplit;
    private JPanel rightContentHolder;
    private JLabel statusLabel;
    private boolean automatedOutputVisible = false;

    public GUIControllerActions(PropositionalProofGUIController controller) {
        this.controller = controller;
        this.components = controller.getComponents();
    }

    public void setCreateProofButton(JButton button) {
        this.createProofButton = button;
    }

    public void setLoadPackageButton(JButton button) {
        this.loadPackageButton = button;
    }

    public void setCreateEditorButton(JButton button) {
        this.createEditorButton = button;
    }

    public void setAutomateButton(JButton button) {
        this.automateButton = button;
    }

    public void setStateIndexField(JTextField field) {
        this.stateIndexField = field;
    }

    public void setAutomatedControlPanel(JPanel panel) {
        this.automatedControlPanel = panel;
    }

    public void setAutomatedContainerPanel(JPanel panel) {
        this.automatedContainerPanel = panel;
    }

    public void setTopSplit(JSplitPane split) {
        this.topSplit = split;
    }

    public void setRightContentHolder(JPanel holder) {
        this.rightContentHolder = holder;
    }

    public void setStatusLabel(JLabel label) {
        this.statusLabel = label;
    }

    public void setStatus(String message, Color color) {
        if (this.statusLabel != null) {
            this.statusLabel.setText(message);
            this.statusLabel.setForeground(color);
        }
    }

    public void updateStateDisplay() {
        ManualPropositionalProof currentProof = this.controller.getCurrentProof();
        if (currentProof == null || !this.controller.isProofCreated()) {
            return;
        }
        try {
            this.controller.getStateDisplayArea().setText(currentProof.getStateText());
            this.controller.getStateDisplayArea().setCaretPosition(0);
        }
        catch (Exception exception) {
            this.controller.getStateDisplayArea().setText("Error retrieving state: " + exception.getMessage());
        }
    }

    public void refreshKnowledgeBaseCheckboxes() {
        ManualPropositionalProof currentProof = this.controller.getCurrentProof();
        if (currentProof == null) {
            return;
        }

        List<AST> knowledgeBase = currentProof.getKnowledgeBase();
        JPanel checkboxPanel = this.controller.getKnowledgeBaseCheckboxPanel();
        if (checkboxPanel == null) {
            return;
        }

        checkboxPanel.removeAll();
        this.controller.getKnowledgeBaseCheckboxes().clear();

        for (int index = 0; index < knowledgeBase.size(); index++) {
            JPanel rowPanel = this.components.buildFormulaRow(index + 1, knowledgeBase.get(index), true, true);
            checkboxPanel.add(rowPanel);
        }

        checkboxPanel.revalidate();
        checkboxPanel.repaint();

        Container ancestor = SwingUtilities.getAncestorOfClass(JScrollPane.class, checkboxPanel);
        if (ancestor != null) {
            ancestor.revalidate();
            ancestor.repaint();
        }
    }

    public void refreshGoalCheckboxes() {
        JPanel checkboxPanel = this.controller.getGoalCheckboxPanel();
        if (checkboxPanel == null) {
            return;
        }

        checkboxPanel.removeAll();
        this.controller.getGoalCheckboxes().clear();

        List<AST> goalListData = this.controller.getGoalListData();
        for (int index = 0; index < goalListData.size(); index++) {
            checkboxPanel.add(this.components.buildFormulaRow(index + 1, goalListData.get(index), true, false));
        }
        for (JCheckBox checkBox : this.controller.getGoalCheckboxes()) {
            checkBox.setEnabled(this.controller.isProofCreated());
        }

        checkboxPanel.revalidate();
        checkboxPanel.repaint();
    }

    public void refreshKnowledgeBaseEntriesDisplay() {
        JPanel checkboxPanel = this.controller.getKnowledgeBaseCheckboxPanel();
        if (checkboxPanel == null) {
            return;
        }

        checkboxPanel.removeAll();
        this.controller.getKnowledgeBaseCheckboxes().clear();
        List<AST> knowledgeBaseEntries = this.controller.getKnowledgeBaseEntries();
        for (int index = 0; index < knowledgeBaseEntries.size(); index++) {
            JPanel rowPanel = this.components.buildFormulaRow(index + 1, knowledgeBaseEntries.get(index), false, true);
            checkboxPanel.add(rowPanel);
        }
        checkboxPanel.revalidate();
        checkboxPanel.repaint();
    }

    public void refreshCheckboxes() {
        this.controller.getGoalListData().clear();
        this.controller.getGoalModel().clear();

        ManualPropositionalProof currentProof = this.controller.getCurrentProof();
        List<AST> goals = currentProof.getGoals();
        if (goals != null) {
            this.controller.getGoalListData().addAll(goals);
            goals.forEach(this.controller.getGoalModel()::addElement);
        }

        this.controller.getSelectedKnowledgeBaseFormulas().clear();
        this.controller.getSelectedGoals().clear();

        this.refreshGoalCheckboxes();
        this.refreshKnowledgeBaseCheckboxes();
        this.updateStateDisplay();
    }

    public void clearRulesPanel() {
        JPanel rulesPanel = this.controller.getRulesPanel();
        if (rulesPanel == null) {
            return;
        }

        rulesPanel.removeAll();
        JLabel hintLabel = new JLabel("Select KB formulas or goals to see applicable rules");
        hintLabel.setFont(GUITheme.LABEL_FONT);
        hintLabel.setForeground(GUITheme.TEXT_DIM);
        hintLabel.setBorder(BorderFactory.createEmptyBorder(20, 14, 20, 14));
        hintLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        rulesPanel.add(hintLabel);
        rulesPanel.revalidate();
        rulesPanel.repaint();
    }

    public void clearSelections() {
        for (JCheckBox checkBox : this.controller.getKnowledgeBaseCheckboxes()) {
            checkBox.setSelected(false);
        }
        for (JCheckBox checkBox : this.controller.getGoalCheckboxes()) {
            checkBox.setSelected(false);
        }
        this.controller.getSelectedKnowledgeBaseFormulas().clear();
        this.controller.getSelectedGoals().clear();
        this.updateSelectedFormulas();
    }

    public void updateSelectedFormulas() {
        this.controller.getSelectedKnowledgeBaseFormulas().clear();
        this.controller.getSelectedGoals().clear();

        ManualPropositionalProof currentProof = this.controller.getCurrentProof();
        if (currentProof != null) {
            List<AST> knowledgeBase = currentProof.getKnowledgeBase();
            List<JCheckBox> knowledgeBaseCheckboxes = this.controller.getKnowledgeBaseCheckboxes();
            for (int index = 0; index < knowledgeBaseCheckboxes.size() && index < knowledgeBase.size(); index++) {
                if (knowledgeBaseCheckboxes.get(index).isSelected()) {
                    this.controller.getSelectedKnowledgeBaseFormulas().add(knowledgeBase.get(index));
                }
            }
        }

        List<AST> liveGoals = (currentProof != null) ? currentProof.getGoals() : this.controller.getGoalListData();
        List<JCheckBox> goalCheckboxes = this.controller.getGoalCheckboxes();
        for (int index = 0; index < goalCheckboxes.size() && index < liveGoals.size(); index++) {
            if (goalCheckboxes.get(index).isSelected()) {
                this.controller.getSelectedGoals().add(liveGoals.get(index));
            }
        }

        if (currentProof != null && this.controller.isProofCreated() &&
                (!this.controller.getSelectedKnowledgeBaseFormulas().isEmpty() || !this.controller.getSelectedGoals().isEmpty())) {
            this.updateApplicableItems();
        }
        else {
            this.clearRulesPanel();
        }
    }

    public void updateApplicableItems() {
        List<InferenceRule> allRules = new ArrayList<>();
        ManualPropositionalProof currentProof = this.controller.getCurrentProof();

        if (!this.controller.getSelectedKnowledgeBaseFormulas().isEmpty()) {
            ManualPropositionalInferenceRuleHelper helper =
                    new ManualPropositionalInferenceRuleHelper(currentProof.getKnowledgeBase());
            allRules.addAll(helper.applicableRules(this.controller.getSelectedKnowledgeBaseFormulas()));

            ContradictionRule contradictionRule = new ContradictionRule();
            allRules.add(contradictionRule);

            for (InferenceRule customRule : this.controller.getCustomRules()) {
                try {
                    List<AST> selectedCopy = new ArrayList<>(this.controller.getSelectedKnowledgeBaseFormulas());
                    AST goal = this.controller.getSelectedKnowledgeBaseFormulas().get(0);
                    if (customRule.canInference(selectedCopy, goal)) {
                        allRules.add(customRule);
                    }
                }
                catch (Exception exception) {
                    System.err.println("Error checking custom rule: " + customRule.name());
                    exception.printStackTrace();
                }
            }
        }

        List<Strategy> strategies = new ArrayList<>();
        for (AST goal : this.controller.getSelectedGoals()) {
            if (goal.isContradiction()) {
                continue;
            }
            strategies.addAll(this.getApplicableStrategies(goal));
        }

        this.displayRulesAndStrategies(allRules, strategies);
    }

    private List<Strategy> getApplicableStrategies(AST ast) {
        ManualPropositionalStrategyHelper helper =
                new ManualPropositionalStrategyHelper(null, null, null, List.of(ast));
        List<Strategy> output = new ArrayList<>();
        if (helper.handleImplicationStrategy(0)) {
            output.add(Strategy.IMPLICATION_STRATEGY);
        }
        if (helper.handleConjunctionStrategy(0, null)) {
            output.add(Strategy.CONJUNCTION_STRATEGY);
        }
        if (helper.handleDisjunctionStrategy(0)) {
            output.add(Strategy.DISJUNCTION_STRATEGY);
        }
        if (helper.handleEquivalenceStrategy(0, null)) {
            output.add(Strategy.EQUIVALENCE_STRATEGY);
        }
        if (helper.handleNegationStrategy(0)) {
            output.add(Strategy.NEGATION_STRATEGY);
        }
        return output;
    }

    private void displayRulesAndStrategies(List<InferenceRule> rules, List<Strategy> strategies) {
        JPanel rulesPanel = this.controller.getRulesPanel();
        if (rulesPanel == null) {
            return;
        }

        rulesPanel.removeAll();

        if (rules.isEmpty() && strategies.isEmpty()) {
            JLabel emptyLabel = new JLabel("No applicable rules for the selected formulas");
            emptyLabel.setFont(GUITheme.LABEL_FONT);
            emptyLabel.setForeground(GUITheme.TEXT_DIM);
            emptyLabel.setBorder(BorderFactory.createEmptyBorder(20, 12, 20, 12));
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            rulesPanel.add(emptyLabel);
        }
        else {
            if (!rules.isEmpty()) {
                rulesPanel.add(this.components.buildSectionDivider("INFERENCE RULES"));
                for (InferenceRule rule : rules) {
                    boolean isCustom = this.controller.getCustomRules().contains(rule);
                    rulesPanel.add(this.components.buildRuleRow(rule.name(), isCustom,
                            () -> this.applyRule(rule)));
                }
            }
            if (!strategies.isEmpty()) {
                rulesPanel.add(this.components.buildSectionDivider("PROOF STRATEGIES"));
                for (Strategy strategy : strategies) {
                    rulesPanel.add(this.components.buildRuleRow(
                            strategy.name().replace('_', ' '), false,
                            () -> this.applyStrategy(strategy)));
                }
            }
        }

        rulesPanel.revalidate();
        rulesPanel.repaint();
    }

    private void applyRule(InferenceRule rule) {
        ManualPropositionalProof currentProof = this.controller.getCurrentProof();
        if (currentProof == null || !this.controller.isProofCreated()) {
            this.setStatus("No active proof, create one first.", GUITheme.INTENT_DANGER);
            return;
        }

        String ruleName = rule.name();
        Map<String, CommandInfo> commandMap = this.controller.getCommandMap();

        if (this.controller.getCustomRules().contains(rule)) {
            try {
                List<AST> knowledgeBase = currentProof.getKnowledgeBase();
                List<AST> selectedCopy = new ArrayList<>(this.controller.getSelectedKnowledgeBaseFormulas());
                AST goal = this.controller.getSelectedKnowledgeBaseFormulas().get(0);
                List<AST> result = rule.inference(selectedCopy, goal);

                if (result != null && !result.isEmpty()) {
                    for (AST ast : result) {
                        if (!knowledgeBase.contains(ast)) {
                            knowledgeBase.add(ast);
                            KnowledgeBaseRegistry.addObtainedFrom(ast.toString(), "Custom Rule: " + ruleName);
                        }
                    }
                    this.refreshKnowledgeBaseCheckboxes();
                    this.updateStateDisplay();
                    this.clearSelections();
                    this.setStatus("Applied custom rule: " + ruleName, GUITheme.INTENT_POSITIVE);
                }
                else {
                    this.setStatus("Custom rule could not be applied.", GUITheme.INTENT_DANGER);
                }
            }
            catch (Exception exception) {
                this.setStatus("Error applying custom rule: " + exception.getMessage(), GUITheme.INTENT_DANGER);
                exception.printStackTrace();
            }
            return;
        }

        String key = PropositionalProofGUIController.convertToMapKey(ruleName);
        CommandInfo commandInfo = commandMap.get(key);
        if (commandInfo == null) {
            this.setStatus("Rule '" + ruleName + "' is not mapped to a command.", GUITheme.INTENT_DANGER);
            return;
        }

        int selectedCount = this.controller.getSelectedKnowledgeBaseFormulas().size();
        if (commandInfo.fixedArity) {
            if (selectedCount != commandInfo.arity) {
                this.setStatus(String.format("'%s' needs exactly %d formula(s), %d selected.",
                        ruleName, commandInfo.arity, selectedCount), GUITheme.INTENT_DANGER);
                return;
            }
        }
        else {
            if (selectedCount < 1) {
                this.setStatus("Select at least one formula.", GUITheme.INTENT_DANGER);
                return;
            }
            if (commandInfo.arity > 0 && selectedCount > commandInfo.arity) {
                this.setStatus(String.format("'%s' takes at most %d formula(s), %d selected.",
                        ruleName, commandInfo.arity, selectedCount), GUITheme.INTENT_DANGER);
                return;
            }
        }

        List<AST> knowledgeBase = currentProof.getKnowledgeBase();
        StringBuilder commandBuilder = new StringBuilder(commandInfo.command).append("(");
        for (int index = 0; index < this.controller.getSelectedKnowledgeBaseFormulas().size(); index++) {
            int knowledgeBaseIndex = knowledgeBase.indexOf(this.controller.getSelectedKnowledgeBaseFormulas().get(index));
            if (index > 0) {
                commandBuilder.append(", ");
            }
            commandBuilder.append("KB").append(knowledgeBaseIndex + 1);
        }
        commandBuilder.append(")");

        try {
            if (currentProof.executeCommand(commandBuilder.toString())) {
                this.refreshKnowledgeBaseCheckboxes();
                this.updateStateDisplay();
                this.clearSelections();
                this.setStatus("Applied: " + ruleName, GUITheme.INTENT_POSITIVE);
            }
            else {
                this.setStatus("Rule could not be applied.", GUITheme.INTENT_DANGER);
            }
        }
        catch (Exception exception) {
            this.setStatus("Error: " + exception.getMessage(), GUITheme.INTENT_DANGER);
        }
    }

    private void applyStrategy(Strategy strategy) {
        ManualPropositionalProof currentProof = this.controller.getCurrentProof();
        if (currentProof == null || !this.controller.isProofCreated()) {
            this.setStatus("No active proof, create one first.", GUITheme.INTENT_DANGER);
            return;
        }
        if (this.controller.getSelectedGoals().isEmpty()) {
            this.setStatus("Select at least one goal to apply a strategy.", GUITheme.INTENT_DANGER);
            return;
        }

        String key = PropositionalProofGUIController.convertToMapKey(strategy.name());
        CommandInfo commandInfo = this.controller.getCommandMap().get(key);
        if (commandInfo == null) {
            this.setStatus("Strategy '" + strategy.name() + "' is not mapped.", GUITheme.INTENT_DANGER);
            return;
        }

        List<AST> liveGoals = currentProof.getGoals();

        if (liveGoals.size() == 1 && liveGoals.getFirst().isContradiction()) {
            this.setStatus("Goal is already a contradiction.", GUITheme.TEXT_SECONDARY);
            return;
        }

        List<AST> selectedGoalsCopy = new ArrayList<>(this.controller.getSelectedGoals());

        for (AST goal : selectedGoalsCopy) {
            int index = liveGoals.indexOf(goal);
            if (index < 0) {
                boolean found = false;
                for (int i = 0; i < liveGoals.size(); i++) {
                    if (goal.isEquivalentTo(liveGoals.get(i))) {
                        index = i;
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    this.setStatus("Goal not found in current proof state.", GUITheme.INTENT_DANGER);
                    continue;
                }
            }

            String command = commandInfo.command + "(G" + (index + 1) + ")";
            try {
                if (currentProof.executeCommand(command)) {
                    this.refreshCheckboxes();
                    this.setStatus("Strategy applied: " + strategy.name().replace('_', ' '),
                            GUITheme.INTENT_POSITIVE);
                }
                else {
                    this.setStatus("Strategy could not be applied to: " + goal, GUITheme.INTENT_DANGER);
                }
            }
            catch (Exception exception) {
                this.setStatus("Error applying strategy: " + exception.getMessage(), GUITheme.INTENT_DANGER);
            }
        }
        this.clearSelections();
    }

    public void addToKnowledgeBase() {
        if (this.controller.isProofCreated()) {
            this.setStatus("Proof already created, reset to modify the knowledge base.", GUITheme.INTENT_DANGER);
            return;
        }
        String text = this.controller.getKnowledgeBaseInputField().getText().trim();
        if (text.isEmpty()) {
            return;
        }

        try {
            PropositionalAST ast = new PropositionalAST(text, true);
            if (!ast.isValid()) {
                this.setStatus("Formula: " + ast + " is not valid!", GUITheme.TEXT_SECONDARY);
                this.controller.getKnowledgeBaseInputField().setText("");
                return;
            }
            this.controller.registerAtoms(List.of(ast));
            this.controller.getKnowledgeBaseEntries().add(ast);
            this.refreshKnowledgeBaseEntriesDisplay();
            this.controller.getKnowledgeBaseInputField().setText("");
            this.setStatus("Added to knowledge base: " + ast, GUITheme.TEXT_SECONDARY);
        }
        catch (Exception exception) {
            this.setStatus("Invalid formula: " + exception.getMessage(), GUITheme.INTENT_DANGER);
        }
    }

    public void addToGoal() {
        if (this.controller.isProofCreated()) {
            this.setStatus("Proof already created, reset to modify goals.", GUITheme.INTENT_DANGER);
            return;
        }
        String text = this.controller.getGoalInputField().getText().trim();
        if (text.isEmpty()) {
            return;
        }

        try {
            AST ast = new PropositionalAST(text, true);
            this.controller.registerAtoms(List.of(ast));
            this.controller.getGoalListData().add(ast);
            this.controller.getGoalModel().addElement(ast);
            this.refreshGoalCheckboxes();
            this.controller.getGoalInputField().setText("");
            this.setStatus("Goal added: " + ast, GUITheme.TEXT_SECONDARY);
        }
        catch (Exception exception) {
            this.setStatus("Invalid formula: " + exception.getMessage(), GUITheme.INTENT_DANGER);
        }
    }

    public void openBatchFormulaDialog(boolean isKnowledgeBase) {
        JDialog dialog = new JDialog(this.controller.getMainFrame(),
                isKnowledgeBase ? "Add Knowledge Base Formulas" : "Add Goal Formulas", true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this.controller.getMainFrame());
        dialog.setLayout(new BorderLayout(10, 10));

        JTextArea textArea = new JTextArea();
        textArea.setFont(GUITheme.MONOSPACED_FONT);
        textArea.setBackground(GUITheme.BACKGROUND_DEEP);
        textArea.setForeground(GUITheme.TEXT_PRIMARY);
        textArea.setCaretColor(GUITheme.INTENT_PRIMARY);

        JScrollPane scrollPane = new JScrollPane(textArea);

        JButton addButton = this.components.createButton("Add All", GUITheme.INTENT_PRIMARY);
        addButton.addActionListener(event -> {
            String text = textArea.getText().trim();
            if (text.isEmpty()) {
                dialog.dispose();
                return;
            }

            String[] lines = text.split("\\R");
            List<AST> parsedFormulas = new ArrayList<>();
            List<String> errors = new ArrayList<>();

            for (String line : lines) {
                String trimmedLine = line.trim();
                if (trimmedLine.isEmpty()) {
                    continue;
                }
                try {
                    AST ast = new PropositionalAST(trimmedLine, true);
                    this.controller.registerAtoms(List.of(ast));
                    parsedFormulas.add(ast);
                }
                catch (Exception exception) {
                    errors.add(trimmedLine + " -> " + exception.getMessage());
                }
            }

            if (isKnowledgeBase) {
                this.controller.getKnowledgeBaseEntries().addAll(parsedFormulas);
                for (AST entry : this.controller.getKnowledgeBaseEntries()) {
                    KnowledgeBaseRegistry.addObtainedFrom(entry.toString(), "Hypothesis");
                }
                this.refreshKnowledgeBaseEntriesDisplay();
                this.setStatus("Added " + parsedFormulas.size() + " KB formulas", GUITheme.INTENT_POSITIVE);
            }
            else {
                this.controller.getGoalListData().addAll(parsedFormulas);
                for (AST entry : parsedFormulas) {
                    KnowledgeBaseRegistry.addObtainedFrom(entry.toString(), "Hypothesis");
                }
                this.refreshGoalCheckboxes();
                this.setStatus("Added " + parsedFormulas.size() + " goals", GUITheme.INTENT_POSITIVE);
            }

            if (!errors.isEmpty()) {
                JOptionPane.showMessageDialog(dialog,
                        String.join("\n", errors),
                        "Some formulas failed",
                        JOptionPane.WARNING_MESSAGE);
            }
            dialog.dispose();
        });

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBackground(GUITheme.BACKGROUND_SURFACE);
        bottomPanel.add(addButton);

        dialog.add(scrollPane, BorderLayout.CENTER);
        dialog.add(bottomPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    public void createProof() {
        if (this.controller.getGoalListData().isEmpty()) {
            this.setStatus("Please add at least one goal before creating a proof.", GUITheme.INTENT_DANGER);
            return;
        }
        try {
            for (AST ast : this.controller.getKnowledgeBaseEntries()) {
                this.controller.registerAtoms(List.of(ast));
            }
            for (AST ast : this.controller.getGoalListData()) {
                this.controller.registerAtoms(List.of(ast));
            }

            List<AST> kbCopy = new ArrayList<>(this.controller.getKnowledgeBaseEntries());

            for (AST ast : this.controller.getKnowledgeBaseEntries()) {
                KnowledgeBaseRegistry.addObtainedFrom(ast.toString(), "Hypothesis");
            }
            for (AST ast : this.controller.getGoalListData()) {
                KnowledgeBaseRegistry.addObtainedFrom(ast.toString(), "Hypothesis");
            }

            ManualPropositionalProof proof = new ManualPropositionalProof(
                    kbCopy,
                    new ArrayList<>(this.controller.getGoalListData()),
                    null, 1);

            this.controller.setCurrentProof(proof);
            this.controller.setProofCreated(true);
            ManualPropositionalProofStates.addOriginalState(proof);

            this.controller.getKnowledgeBaseInputField().setEnabled(false);
            this.controller.getGoalInputField().setEnabled(false);

            this.refreshKnowledgeBaseCheckboxes();
            this.refreshGoalCheckboxes();
            this.updateStateDisplay();
            if (this.createProofButton != null) {
                this.createProofButton.setEnabled(false);
            }
            this.setStatus("Proof created, select KB formulas or goals to apply rules.", GUITheme.INTENT_POSITIVE);

        }
        catch (Exception exception) {
            this.setStatus("Error creating proof: " + exception.getMessage(), GUITheme.INTENT_DANGER);
        }
    }

    public void reset() {
        this.controller.getKnowledgeBaseEntries().clear();
        this.controller.getKnowledgeBaseInputField().setText("");
        this.controller.getKnowledgeBaseInputField().setEnabled(true);

        this.controller.getGoalListData().clear();
        this.controller.getGoalModel().clear();
        this.controller.getGoalInputField().setText("");
        this.controller.getGoalInputField().setEnabled(true);

        this.controller.setProofCreated(false);
        this.controller.setCurrentProof(null);

        this.controller.getSelectedKnowledgeBaseFormulas().clear();
        this.controller.getSelectedGoals().clear();

        this.refreshGoalCheckboxes();
        this.refreshKnowledgeBaseEntriesDisplay();

        this.controller.getStateDisplayArea().setText("");
        this.controller.getAutomatedProofOutputArea().setText("");
        this.clearRulesPanel();

        ProofTextHelper.clear();
        if (this.createProofButton != null) {
            this.createProofButton.setEnabled(true);
        }
        this.setStatus("Reset, ready for a new proof.", GUITheme.TEXT_SECONDARY);
    }

    public void handleDone() {
        ManualPropositionalProof currentProof = this.controller.getCurrentProof();
        if (currentProof == null || !this.controller.isProofCreated()) {
            this.setStatus("No active proof.", GUITheme.INTENT_DANGER);
            return;
        }
        try {
            boolean success = currentProof.executeCommand("done");
            if (success) {
                if (currentProof.getStateIndex() != 1) {
                    ManualPropositionalProof parent = currentProof.getParent();
                    if (parent != null) {
                        this.controller.setCurrentProof(parent);
                        this.refreshKnowledgeBaseCheckboxes();
                        this.updateStateDisplay();
                        this.clearSelections();
                    }
                    this.setStatus("State completed, returned to parent state.", GUITheme.TEXT_SECONDARY);
                }
                else {
                    this.updateStateDisplay();
                    this.setStatus("Proof complete!", GUITheme.INTENT_POSITIVE);
                    JOptionPane.showMessageDialog(this.controller.getMainFrame(),
                            "Proof completed successfully!", "Done", JOptionPane.INFORMATION_MESSAGE);
                    this.reset();
                }
            }
            else {
                this.setStatus("Cannot complete, some goals or sub-states are unresolved.", GUITheme.INTENT_DANGER);
            }
        }
        catch (Exception exception) {
            this.setStatus("Error: " + exception.getMessage(), GUITheme.INTENT_DANGER);
        }
    }

    public void handleChangeState() {
        ManualPropositionalProof currentProof = this.controller.getCurrentProof();
        if (currentProof == null || !this.controller.isProofCreated()) {
            this.setStatus("No active proof.", GUITheme.INTENT_DANGER);
            return;
        }
        try {
            int index = Integer.parseInt(this.stateIndexField.getText().trim());
            if (index < 1) {
                this.setStatus("State index must be at least 1.", GUITheme.INTENT_DANGER);
                return;
            }

            ManualPropositionalProof newState = currentProof.getState(index);
            if (newState != null) {
                this.controller.setCurrentProof(newState);
                this.refreshCheckboxes();
                this.clearSelections();
                this.setStatus("Switched to state " + index, GUITheme.TEXT_SECONDARY);
            }
            else {
                this.setStatus("State " + index + " does not exist.", GUITheme.INTENT_DANGER);
            }
        }
        catch (NumberFormatException exception) {
            this.setStatus("Invalid state index, enter a number.", GUITheme.INTENT_DANGER);
        }
        catch (Exception exception) {
            this.setStatus("Error changing state: " + exception.getMessage(), GUITheme.INTENT_DANGER);
        }
    }

    public void showPackageLoaderDialog() {
        String customRulesPath = "files/customRules";
        File directory = new File(customRulesPath);

        if (!directory.exists() || !directory.isDirectory()) {
            JOptionPane.showMessageDialog(this.controller.getMainFrame(),
                    "Custom rules directory not found: " + customRulesPath,
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        File[] subDirectories = directory.listFiles(File::isDirectory);
        if (subDirectories == null || subDirectories.length == 0) {
            JOptionPane.showMessageDialog(this.controller.getMainFrame(),
                    "No packages found in " + customRulesPath,
                    "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        List<String> packageNames = new ArrayList<>();
        for (File subDirectory : subDirectories) {
            if (!subDirectory.getName().equals("meta")) {
                packageNames.add(subDirectory.getName());
            }
        }

        if (packageNames.isEmpty()) {
            JOptionPane.showMessageDialog(this.controller.getMainFrame(),
                    "No valid packages found (excluding meta)",
                    "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String selectedPackage = (String) JOptionPane.showInputDialog(
                this.controller.getMainFrame(),
                "Select a package to load custom rules from:",
                "Load Custom Rules Package",
                JOptionPane.QUESTION_MESSAGE,
                null,
                packageNames.toArray(),
                this.controller.getLoadedPackage() != null ? this.controller.getLoadedPackage() : packageNames.get(0)
        );

        if (selectedPackage != null && !selectedPackage.isEmpty()) {
            this.loadPackage(selectedPackage);
        }
    }

    private void loadPackage(String packageName) {
        String customRulesPath = "files/customRules/" + packageName;
        File packageDirectory = new File(customRulesPath);

        if (!packageDirectory.exists() || !packageDirectory.isDirectory()) {
            this.setStatus("Package not found: " + packageName, GUITheme.INTENT_DANGER);
            return;
        }

        this.controller.getCustomRules().clear();
        int loadedCount = 0;
        PropositionalLogicLoader loader = this.controller.getLoader();

        File[] files = packageDirectory.listFiles((directory, name) -> name.endsWith(".atpf"));
        if (files != null) {
            for (File file : files) {
                try {
                    List<String> lines = loader.getLines(file.getPath());
                    if (lines != null && !lines.isEmpty()) {
                        loadedCount += this.parseCustomRules(lines);
                    }
                }
                catch (Exception exception) {
                    System.err.println("Error loading custom rule file: " + file.getName());
                    exception.printStackTrace();
                }
            }
        }

        this.controller.setLoadedPackage(packageName);
        this.setStatus("Loaded " + loadedCount + " custom rules from package: " + packageName, GUITheme.INTENT_POSITIVE);

        if (this.controller.isProofCreated()) {
            this.updateApplicableItems();
        }
    }

    private int parseCustomRules(List<String> lines) {
        int count = 0;
        int index = 0;
        PropositionalLogicLoader loader = this.controller.getLoader();

        while (index < lines.size()) {
            String line = lines.get(index).trim();
            if (line.startsWith("rule=")) {
                String ruleName = line.substring(5).trim();
                List<String> ruleLines = new ArrayList<>();
                ruleLines.add(line);
                index++;
                while (index < lines.size()) {
                    String currentLine = lines.get(index).trim();
                    ruleLines.add(currentLine);
                    if (currentLine.equals("end")) {
                        break;
                    }
                    index++;
                }

                try {
                    CustomPropositionalInferenceRule customRule = loader.loadCustomRule(ruleLines, ruleName);
                    if (customRule != null && customRule.conclusion() != null) {
                        this.controller.getCustomRules().add(customRule);
                        count++;
                    }
                }
                catch (Exception exception) {
                    System.err.println("Error loading custom rule: " + ruleName);
                    exception.printStackTrace();
                }
            }
            index++;
        }
        return count;
    }

    public void openRuleEditor() {
        String[] options = {"Create New Package", "Edit Existing Package"};
        int choice = JOptionPane.showOptionDialog(
                this.controller.getMainFrame(),
                "Do you want to create a new package or edit an existing one?",
                "Rule Editor",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (choice == 0) {
            String packageName = JOptionPane.showInputDialog(this.controller.getMainFrame(), "Enter package name:");
            if (packageName != null && !packageName.trim().isEmpty()) {
                SwingUtilities.invokeLater(() -> new CustomRuleEditor(packageName.trim()));
            }
        }
        else if (choice == 1) {
            String customRulesPath = "files/customRules";
            File directory = new File(customRulesPath);

            if (!directory.exists() || !directory.isDirectory()) {
                JOptionPane.showMessageDialog(this.controller.getMainFrame(),
                        "Custom rules directory not found", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            File[] subDirectories = directory.listFiles(File::isDirectory);
            if (subDirectories == null || subDirectories.length == 0) {
                JOptionPane.showMessageDialog(this.controller.getMainFrame(),
                        "No packages found", "Information", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            List<String> packageNames = new ArrayList<>();
            for (File subDirectory : subDirectories) {
                if (!subDirectory.getName().equals("meta")) {
                    packageNames.add(subDirectory.getName());
                }
            }

            if (packageNames.isEmpty()) {
                JOptionPane.showMessageDialog(this.controller.getMainFrame(),
                        "No valid packages found", "Information", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            String selectedPackage = (String) JOptionPane.showInputDialog(
                    this.controller.getMainFrame(),
                    "Select a package to edit:",
                    "Edit Package",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    packageNames.toArray(),
                    packageNames.getFirst()
            );

            if (selectedPackage != null && !selectedPackage.isEmpty()) {
                SwingUtilities.invokeLater(() -> new CustomRuleEditor(selectedPackage));
            }
        }
    }

    public void runAutomatedProof() {
        if (this.controller.getGoalListData().isEmpty() &&
                (this.controller.getCurrentProof() == null || this.controller.getCurrentProof().getGoals().isEmpty())) {
            this.setStatus("Please add at least one goal before automating.", GUITheme.INTENT_DANGER);
            return;
        }

        this.revealAutomatedOutputPanel();

        try {
            this.controller.getAutomatedProofOutputArea().setText("Running automated proof...\n\n");
            Signature signature = SignatureFactory.createSignature(UniverseOfDiscourse.PROPOSITIONS);

            List<AST> currentKB;
            List<AST> currentGoals;

            if (this.controller.isProofCreated() && this.controller.getCurrentProof() != null) {
                currentKB = new ArrayList<>(this.controller.getCurrentProof().getKnowledgeBase());
                currentGoals = new ArrayList<>(this.controller.getCurrentProof().getGoals());
            } else {
                currentKB = new ArrayList<>(this.controller.getKnowledgeBaseEntries());
                currentGoals = new ArrayList<>(this.controller.getGoalListData());
            }

            PropositionalProof automatedProof = new PropositionalProof(
                    signature,
                    currentKB,
                    currentGoals
            );

            boolean result = automatedProof.proveWithoutPrinting();

            if (result) {
                this.controller.setCachedFormalProof(automatedProof.getFormalString());
                this.controller.setCachedIndentedProof(automatedProof.getIndentedString());
                this.controller.setShowingFormal(true);

                JButton switchViewButton = this.controller.getSwitchViewButton();
                if (switchViewButton == null) {
                    switchViewButton = this.components.createButton("Show Indented Proof", GUITheme.INTENT_NEUTRAL);
                    switchViewButton.addActionListener(event -> {
                        this.controller.setShowingFormal(!this.controller.isShowingFormal());
                        this.updateAutomatedDisplay();
                    });
                    this.controller.setSwitchViewButton(switchViewButton);
                    this.automatedControlPanel.add(switchViewButton);
                    this.automatedControlPanel.revalidate();
                    this.automatedControlPanel.repaint();
                }
                else {
                    switchViewButton.setVisible(true);
                }

                this.updateAutomatedDisplay();
                this.setStatus("Automated proof completed successfully!", GUITheme.INTENT_POSITIVE);
            }
            else {
                this.controller.getAutomatedProofOutputArea().setText("Proof could not be completed automatically.\n");
                this.controller.getAutomatedProofOutputArea().append("Try manual proof or check your formulas.\n");
                this.setStatus("Automated proof failed.", GUITheme.INTENT_DANGER);
                JButton switchViewButton = this.controller.getSwitchViewButton();
                if (switchViewButton != null) {
                    switchViewButton.setVisible(false);
                }
            }
        }
        catch (Exception exception) {
            this.controller.getAutomatedProofOutputArea().setText("Error during automated proof:\n");
            this.controller.getAutomatedProofOutputArea().append(exception.getMessage());
            this.setStatus("Error: " + exception.getMessage(), GUITheme.INTENT_DANGER);
            exception.printStackTrace();
            JButton switchViewButton = this.controller.getSwitchViewButton();
            if (switchViewButton != null) {
                switchViewButton.setVisible(false);
            }
        }
    }

    private void updateAutomatedDisplay() {
        JTextArea outputArea = this.controller.getAutomatedProofOutputArea();
        if (outputArea == null) {
            return;
        }

        if (this.controller.isShowingFormal()) {
            outputArea.setText("--- FORMAL PROOF ---\n\n" + this.controller.getCachedFormalProof());
            JButton switchViewButton = this.controller.getSwitchViewButton();
            if (switchViewButton != null) {
                switchViewButton.setText("Show Indented Proof");
            }
        }
        else {
            outputArea.setText("--- INDENTED PROOF ---\n\n" + this.controller.getCachedIndentedProof());
            JButton switchViewButton = this.controller.getSwitchViewButton();
            if (switchViewButton != null) {
                switchViewButton.setText("Show Formal Proof");
            }
        }

        outputArea.setCaretPosition(0);
    }

    private void revealAutomatedOutputPanel() {
        if (this.automatedOutputVisible) {
            return;
        }
        this.automatedOutputVisible = true;

        this.rightContentHolder.removeAll();

        JSplitPane mainRightSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, this.topSplit, this.automatedContainerPanel);
        mainRightSplit.setDividerSize(6);
        mainRightSplit.setResizeWeight(0.70);
        mainRightSplit.setBackground(GUITheme.BACKGROUND_DEEP);

        this.rightContentHolder.add(mainRightSplit, BorderLayout.CENTER);
        this.rightContentHolder.revalidate();
        this.rightContentHolder.repaint();
    }
}