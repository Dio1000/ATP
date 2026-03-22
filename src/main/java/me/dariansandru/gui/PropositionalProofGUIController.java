package me.dariansandru.gui;

import me.dariansandru.domain.data_structures.ast.AST;
import me.dariansandru.domain.data_structures.ast.PropositionalAST;
import me.dariansandru.domain.proof.inference_rules.InferenceRule;
import me.dariansandru.domain.proof.manual_proof.ManualPropositionalProof;
import me.dariansandru.domain.proof.manual_proof.helper.ManualPropositionalInferenceRuleHelper;
import me.dariansandru.domain.proof.Strategy;
import me.dariansandru.utils.global.GlobalAtomID;
import me.dariansandru.utils.helper.KnowledgeBaseRegistry;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PropositionalProofGUIController {

    private JFrame frame;
    private ManualPropositionalProof currentProof;
    private ManualPropositionalInferenceRuleHelper ruleHelper;
    private boolean proofCreated = false;

    private List<AST> kbListData = new ArrayList<>();
    private List<AST> goalListData = new ArrayList<>();

    private DefaultListModel<AST> goalModel = new DefaultListModel<>();
    private JList<AST> goalList = new JList<>(goalModel);

    private List<JCheckBox> kbCheckboxes = new ArrayList<>();
    private List<JCheckBox> goalCheckboxes = new ArrayList<>();
    private JPanel kbCheckboxPanel;
    private JPanel goalCheckboxPanel;

    private JPanel rulesPanel;
    private JTextArea stateDisplayArea;

    private JTextField kbField;
    private JTextField goalField;

    private JButton addKbBtn;
    private JButton addGoalBtn;
    private JButton createProofBtn;
    private JButton resetBtn;
    private JButton doneBtn;
    private JButton changeStateBtn;
    private JTextField stateIndexField;

    private List<AST> currentlySelectedFormulas = new ArrayList<>();
    private List<AST> selectedGoals = new ArrayList<>();
    private Map<String, CommandInfo> commandMap = new HashMap<>();

    public PropositionalProofGUIController() {
        initializeCommandMap();
        initialize();
    }

    private void initializeCommandMap() {
        commandMap.put("IMPLICATION_STRATEGY", new CommandInfo("implstr", 1, true));
        commandMap.put("EQUIVALENCE_STRATEGY", new CommandInfo("eqstr", 1, true));
        commandMap.put("CONJUNCTION_STRATEGY", new CommandInfo("constr", 1, true));
        commandMap.put("DISJUNCTION_STRATEGY", new CommandInfo("disstr", 1, true));
        commandMap.put("NEGATION_STRATEGY", new CommandInfo("negstr", 1, true));
        commandMap.put("CONTRAPOSITIVE_STRATEGY", new CommandInfo("contrapos", 1, true));

        commandMap.put("ABSORPTION", new CommandInfo("absor", 1, true));
        commandMap.put("CONJUNCTION INTRODUCTION", new CommandInfo("conintro", -1, false));
        commandMap.put("CONJUNCTION ELIMINATION", new CommandInfo("conelim", 1, true));
        commandMap.put("CONSTRUCTIVE DILEMMA", new CommandInfo("constrdil", 3, true));
        commandMap.put("DESTRUCTIVE DILEMMA", new CommandInfo("destrdil", 3, true));
        commandMap.put("DISJUNCTION INTRODUCTION", new CommandInfo("disintro", -1, false));
        commandMap.put("DISJUNCTION ELIMINATION", new CommandInfo("diselim", 2, true));
        commandMap.put("DISJUNCTIVE SYLLOGISM", new CommandInfo("dissyll", 2, true));
        commandMap.put("EQUIVALENCE INTRODUCTION", new CommandInfo("eqintro", 2, true));
        commandMap.put("EQUIVALENCE SIMPLIFICATION", new CommandInfo("eqelim", 1, true));
        commandMap.put("HYPOTHETICAL SYLLOGISM", new CommandInfo("hypsyll", 2, true));
        commandMap.put("IMPLICATION INTRODUCTION", new CommandInfo("implintro", 2, true));
        commandMap.put("IMPLICATION ELIMINATION", new CommandInfo("implsimpl", 1, true));
        commandMap.put("MODUS PONENS", new CommandInfo("modpon", 2, true));
        commandMap.put("MODUS TOLLENS", new CommandInfo("modtol", 2, true));
        commandMap.put("PROOF BY CASES", new CommandInfo("cases", 1, true));

        commandMap.put("DISJUNCTION SIMPLIFICATION", new CommandInfo("dissimpl", 1, true));
        commandMap.put("MATERIAL EQUIVALENCE", new CommandInfo("mateq", 1, true));
        commandMap.put("MATERIAL IMPLICATION", new CommandInfo("matimpl", 1, true));
        commandMap.put("DEMORGAN", new CommandInfo("demorgan", 1, true));
        commandMap.put("TRANSPOSITION", new CommandInfo("trans", 1, true));

        commandMap.put("CONTRADICTION", new CommandInfo("contr", 2, true));
    }

    private void initialize() {
        frame = new JFrame("Manual Propositional Proof System");
        frame.setSize(1400, 800);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplitPane.setDividerSize(8);

        JPanel leftPanel = new JPanel(new BorderLayout(0, 10));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel kbPanel = createKBWithCheckboxesPanel();
        leftPanel.add(kbPanel, BorderLayout.CENTER);

        JPanel goalPanel = createGoalPanel();
        goalPanel.setPreferredSize(new Dimension(0, 200));
        leftPanel.add(goalPanel, BorderLayout.SOUTH);

        mainSplitPane.setLeftComponent(leftPanel);

        JPanel rightPanel = new JPanel(new BorderLayout());

        rulesPanel = new JPanel();
        rulesPanel.setLayout(new BoxLayout(rulesPanel, BoxLayout.Y_AXIS));
        JScrollPane rulesScroll = new JScrollPane(rulesPanel);
        rulesScroll.setBorder(new TitledBorder("Applicable Rules"));
        rulesScroll.setPreferredSize(new Dimension(500, 400));

        stateDisplayArea = new JTextArea();
        stateDisplayArea.setEditable(false);
        stateDisplayArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane stateScroll = new JScrollPane(stateDisplayArea);
        stateScroll.setBorder(new TitledBorder("Current State"));
        stateScroll.setPreferredSize(new Dimension(500, 350));

        rightPanel.add(rulesScroll, BorderLayout.NORTH);
        rightPanel.add(stateScroll, BorderLayout.CENTER);

        mainSplitPane.setRightComponent(rightPanel);
        mainSplitPane.setResizeWeight(0.6);

        frame.add(mainSplitPane, BorderLayout.CENTER);

        JPanel inputPanel = createInputPanel();
        frame.add(inputPanel, BorderLayout.NORTH);

        JPanel controlPanel = createControlPanel();
        frame.add(controlPanel, BorderLayout.SOUTH);

        frame.setVisible(true);

        refreshGoalCheckboxes();
    }

    private JPanel createKBWithCheckboxesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Knowledge Base"));

        kbCheckboxPanel = new JPanel();
        kbCheckboxPanel.setLayout(new BoxLayout(kbCheckboxPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(kbCheckboxPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createGoalPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Goals"));

        goalCheckboxPanel = new JPanel();
        goalCheckboxPanel.setLayout(new BoxLayout(goalCheckboxPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(goalCheckboxPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createInputPanel() {
        JPanel inputPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel kbInputPanel = new JPanel(new BorderLayout(5, 5));
        kbField = new JTextField();
        addKbBtn = new JButton("Add to KB");
        kbInputPanel.add(new JLabel("Add to Knowledge Base:"), BorderLayout.WEST);
        kbInputPanel.add(kbField, BorderLayout.CENTER);
        kbInputPanel.add(addKbBtn, BorderLayout.EAST);

        JPanel goalInputPanel = new JPanel(new BorderLayout(5, 5));
        goalField = new JTextField();
        addGoalBtn = new JButton("Add Goal");
        goalInputPanel.add(new JLabel("Add Goal:"), BorderLayout.WEST);
        goalInputPanel.add(goalField, BorderLayout.CENTER);
        goalInputPanel.add(addGoalBtn, BorderLayout.EAST);

        inputPanel.add(kbInputPanel);
        inputPanel.add(goalInputPanel);

        addKbBtn.addActionListener(e -> addToKB());
        addGoalBtn.addActionListener(e -> addToGoal());
        kbField.addActionListener(e -> addToKB());
        goalField.addActionListener(e -> addToGoal());

        return inputPanel;
    }

    private JPanel createControlPanel() {
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        createProofBtn = new JButton("Create Proof");
        resetBtn = new JButton("Reset");
        doneBtn = new JButton("Done");
        changeStateBtn = new JButton("Change State");
        stateIndexField = new JTextField(5);

        createProofBtn.addActionListener(e -> createProof());
        resetBtn.addActionListener(e -> reset());
        doneBtn.addActionListener(e -> handleDone());
        changeStateBtn.addActionListener(e -> handleChangeState());

        controlPanel.add(createProofBtn);
        controlPanel.add(resetBtn);
        controlPanel.add(new JLabel("State Index:"));
        controlPanel.add(stateIndexField);
        controlPanel.add(changeStateBtn);
        controlPanel.add(doneBtn);

        return controlPanel;
    }

    private void refreshKBCheckboxes() {
        validateCheckboxes(kbCheckboxPanel, kbCheckboxes, kbListData);
    }

    private void validateCheckboxes(JPanel kbCheckboxPanel, List<JCheckBox> kbCheckboxes, List<AST> kbListData) {
        kbCheckboxPanel.removeAll();
        kbCheckboxes.clear();

        for (int i = 0; i < kbListData.size(); i++) {
            AST ast = kbListData.get(i);
            JPanel formulaPanel = new JPanel(new BorderLayout(5, 0));
            formulaPanel.setBorder(BorderFactory.createEmptyBorder(4, 5, 4, 5));

            JCheckBox checkBox = new JCheckBox();
            checkBox.addActionListener(e -> updateSelectedFormulas());
            kbCheckboxes.add(checkBox);

            JLabel formulaLabel = new JLabel((i + 1) + ". " + ast.toString());
            formulaLabel.setFont(new Font("Monospaced", Font.PLAIN, 12));

            formulaPanel.add(checkBox, BorderLayout.WEST);
            formulaPanel.add(formulaLabel, BorderLayout.CENTER);

            kbCheckboxPanel.add(formulaPanel);
        }

        kbCheckboxPanel.revalidate();
        kbCheckboxPanel.repaint();
    }

    private void refreshGoalCheckboxes() {
        if (goalCheckboxPanel == null) return;

        validateCheckboxes(goalCheckboxPanel, goalCheckboxes, goalListData);
    }

    private void updateSelectedFormulas() {
        currentlySelectedFormulas.clear();
        selectedGoals.clear();

        for (int i = 0; i < kbCheckboxes.size(); i++) {
            if (kbCheckboxes.get(i).isSelected()) {
                currentlySelectedFormulas.add(kbListData.get(i));
            }
        }

        for (int i = 0; i < goalCheckboxes.size(); i++) {
            if (goalCheckboxes.get(i).isSelected()) {
                selectedGoals.add(goalListData.get(i));
            }
        }

        if (currentProof != null && proofCreated && (!currentlySelectedFormulas.isEmpty() || !selectedGoals.isEmpty())) {
            updateApplicableItems();
        }
        else {
            clearRulesPanel();
        }
    }

    private void updateApplicableItems() {
        if (ruleHelper == null) {
            ruleHelper = new ManualPropositionalInferenceRuleHelper(kbListData);
        }

        List<InferenceRule> applicableRules = new ArrayList<>();
        if (!currentlySelectedFormulas.isEmpty()) {
            applicableRules = ruleHelper.applicableRules(currentlySelectedFormulas);
        }

        List<Strategy> applicableStrategies = new ArrayList<>();
        for (AST goal : selectedGoals) {
            applicableStrategies.addAll(getApplicableStrategies(goal));
        }

        displayRulesAndStrategies(applicableRules, applicableStrategies);
    }

    private List<Strategy> getApplicableStrategies(AST ast) {
        ManualPropositionalInferenceRuleHelper helper = new ManualPropositionalInferenceRuleHelper(List.of(ast));
        return helper.applicableStrategies(ast);
    }

    private void displayRulesAndStrategies(List<InferenceRule> rules, List<Strategy> strategies) {
        rulesPanel.removeAll();

        if (rules.isEmpty() && strategies.isEmpty()) {
            rulesPanel.add(new JLabel("No applicable rules or strategies for the selected formulas"));
        }
        else {
            if (!rules.isEmpty()) {
                JLabel rulesHeader = new JLabel("<html><b>Inference Rules:</b></html>");
                rulesHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
                rulesPanel.add(rulesHeader);
                rulesPanel.add(Box.createVerticalStrut(5));

                for (InferenceRule rule : rules) {
                    addRuleButton(rule);
                }
                rulesPanel.add(Box.createVerticalStrut(10));
            }

            if (!strategies.isEmpty()) {
                JLabel strategiesHeader = new JLabel("<html><b>Strategies:</b></html>");
                strategiesHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
                rulesPanel.add(strategiesHeader);
                rulesPanel.add(Box.createVerticalStrut(5));

                for (Strategy strategy : strategies) {
                    addStrategyButton(strategy);
                }
            }
        }

        refreshRulesPanel();
    }

    private void addRuleButton(InferenceRule rule) {
        JPanel rulePanel = new JPanel(new BorderLayout(10, 5));
        rulePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        JPanel ruleInfoPanel = new JPanel(new GridLayout(2, 1));
        JLabel ruleNameLabel = new JLabel("<html><b>" + rule.name() + "</b></html>");
        JLabel ruleDescLabel = new JLabel(getRuleDescription(rule));
        ruleDescLabel.setFont(ruleDescLabel.getFont().deriveFont(Font.ITALIC, 10));
        ruleInfoPanel.add(ruleNameLabel);
        ruleInfoPanel.add(ruleDescLabel);

        JButton applyBtn = new JButton("Apply");
        applyBtn.addActionListener(e -> applyRule(rule));
        applyBtn.setPreferredSize(new Dimension(100, 40));

        rulePanel.add(ruleInfoPanel, BorderLayout.CENTER);
        rulePanel.add(applyBtn, BorderLayout.EAST);

        rulesPanel.add(rulePanel);
        rulesPanel.add(Box.createVerticalStrut(5));
    }

    private void addStrategyButton(Strategy strategy) {
        JPanel strategyPanel = new JPanel(new BorderLayout(10, 5));
        strategyPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        JPanel strategyInfoPanel = new JPanel(new GridLayout(2, 1));
        JLabel strategyNameLabel = new JLabel("<html><b>" + strategy.name() + "</b></html>");
        JLabel strategyDescLabel = new JLabel(getStrategyDescription(strategy));
        strategyDescLabel.setFont(strategyDescLabel.getFont().deriveFont(Font.ITALIC, 10));
        strategyInfoPanel.add(strategyNameLabel);
        strategyInfoPanel.add(strategyDescLabel);

        JButton applyBtn = new JButton("Apply");
        applyBtn.addActionListener(e -> applyStrategy(strategy));
        applyBtn.setPreferredSize(new Dimension(100, 40));

        strategyPanel.add(strategyInfoPanel, BorderLayout.CENTER);
        strategyPanel.add(applyBtn, BorderLayout.EAST);

        rulesPanel.add(strategyPanel);
        rulesPanel.add(Box.createVerticalStrut(5));
    }

    private String getRuleDescription(InferenceRule rule) {
        String ruleName = rule.name().toUpperCase();

        return switch (ruleName) {
            case "MODUS PONENS" -> "If P → Q and P, then Q";
            case "MODUS TOLLENS" -> "If P → Q and ¬Q, then ¬P";
            case "HYPOTHETICAL SYLLOGISM" -> "If P → Q and Q → R, then P → R";
            case "DISJUNCTIVE SYLLOGISM" -> "If P ∨ Q and ¬P, then Q";
            case "CONSTRUCTIVE DILEMMA" -> "If (P → Q) ∧ (R → S) and P ∨ R, then Q ∨ S";
            case "DESTRUCTIVE DILEMMA" -> "If (P → Q) ∧ (R → S) and ¬Q ∨ ¬S, then ¬P ∨ ¬R";
            case "ABSORPTION" -> "If P → Q, then P → (P ∧ Q)";
            case "TRANSPOSITION" -> "If P → Q, then ¬Q → ¬P";
            case "MATERIAL EQUIVALENCE" -> "If P ↔ Q, then (P → Q) ∧ (Q → P)";
            case "MATERIAL IMPLICATION" -> "If P → Q, then ¬P ∨ Q";
            case "DEMORGAN" -> "DeMorgan's laws: ¬(P ∧ Q) ↔ (¬P ∨ ¬Q), ¬(P ∨ Q) ↔ (¬P ∧ ¬Q)";
            case "CONJUNCTION INTRODUCTION" -> "From P and Q, derive P ∧ Q";
            case "CONJUNCTION ELIMINATION" -> "From P ∧ Q, derive P or Q";
            case "DISJUNCTION INTRODUCTION" -> "From P, derive P ∨ Q";
            case "DISJUNCTION ELIMINATION" -> "From P ∨ Q, P → R, Q → R, derive R";
            case "IMPLICATION INTRODUCTION" -> "From Q (assuming P), derive P → Q";
            case "IMPLICATION ELIMINATION" -> "From P → Q and P, derive Q";
            default -> "Apply this inference rule";
        };
    }

    private String getStrategyDescription(Strategy strategy) {
        return switch (strategy) {
            case IMPLICATION_STRATEGY -> "For goal P → Q, assume P and prove Q";
            case EQUIVALENCE_STRATEGY -> "For goal P ↔ Q, prove P → Q and Q → P";
            case CONJUNCTION_STRATEGY -> "For goal P ∧ Q, prove P and Q separately";
            case DISJUNCTION_STRATEGY -> "For goal P ∨ Q, prove either P or Q";
            case NEGATION_STRATEGY -> "For goal ¬P, assume P and derive contradiction";
            default -> "Apply this strategy";
        };
    }

    private void applyRule(InferenceRule rule) {
        if (currentProof == null || !proofCreated) {
            JOptionPane.showMessageDialog(frame, "No active proof. Please create a proof first.");
            return;
        }

        String ruleName = rule.name();
        CommandInfo cmdInfo = commandMap.get(ruleName.toUpperCase());

        if (cmdInfo == null) {
            JOptionPane.showMessageDialog(frame, String.format("Rule '%s' is not yet mapped to a command.", ruleName));
            return;
        }

        int selectedCount = currentlySelectedFormulas.size();
        if (cmdInfo.fixedArity) {
            if (selectedCount != cmdInfo.arity) {
                JOptionPane.showMessageDialog(frame, String.format("Rule '%s' requires exactly %d formula(s). You selected %d.",
                                ruleName, cmdInfo.arity, selectedCount));
                return;
            }
        }
        else {
            if (cmdInfo.arity > 0 && selectedCount > cmdInfo.arity) {
                JOptionPane.showMessageDialog(frame, String.format("Rule '%s' requires at most %d formula(s). You selected %d.",
                                ruleName, cmdInfo.arity, selectedCount));
                return;
            }
            if (selectedCount < 1) {
                JOptionPane.showMessageDialog(frame, "Please select at least one formula.");
                return;
            }
        }
        StringBuilder commandBuilder = new StringBuilder(cmdInfo.command);
        commandBuilder.append("(");

        for (int i = 0; i < currentlySelectedFormulas.size(); i++) {
            int index = kbListData.indexOf(currentlySelectedFormulas.get(i));
            if (i > 0) commandBuilder.append(", ");
            commandBuilder.append("KB").append(index + 1);
        }
        commandBuilder.append(")");

        String command = commandBuilder.toString();
        try {
            boolean success = currentProof.executeCommand(command);
            if (success) {
                refreshKBCheckboxes();
                updateStateDisplay();

                for (JCheckBox cb : kbCheckboxes) {
                    cb.setSelected(false);
                }
                updateSelectedFormulas();

                JOptionPane.showMessageDialog(frame, "Rule applied successfully!");
            }
            else {
                JOptionPane.showMessageDialog(frame, "Failed to apply rule.");
            }
        }
        catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Error applying rule: " + ex.getMessage());
        }
    }

    private void applyStrategy(Strategy strategy) {
        if (currentProof == null || !proofCreated) {
            JOptionPane.showMessageDialog(frame, "No active proof. Please create a proof first.");
            return;
        }

        if (selectedGoals.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Please select at least one goal to apply strategy.");
            return;
        }

        String strategyName = strategy.name();
        CommandInfo cmdInfo = commandMap.get(strategyName);

        if (cmdInfo == null) {
            JOptionPane.showMessageDialog(frame, String.format("Strategy '%s' is not yet mapped to a command.", strategyName));
            return;
        }

        for (AST goal : selectedGoals) {
            int goalIndex = goalListData.indexOf(goal);
            String command = cmdInfo.command + "(G" + (goalIndex + 1) + ")";

            try {
                boolean success = currentProof.executeCommand(command);
                if (success) {
                    updateStateDisplay();
                }
                else {
                    JOptionPane.showMessageDialog(frame, String.format("Failed to apply strategy to goal: %s", goal));
                }
            }
            catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Error applying strategy: " + ex.getMessage());
            }
        }

        for (JCheckBox cb : goalCheckboxes) {
            cb.setSelected(false);
        }
        updateSelectedFormulas();
        JOptionPane.showMessageDialog(frame, "Strategies applied successfully!");
    }

    private void clearRulesPanel() {
        rulesPanel.removeAll();
        rulesPanel.add(new JLabel("Select KB formulas or goals to see applicable rules/strategies"));
        refreshRulesPanel();
    }

    private void refreshRulesPanel() {
        rulesPanel.revalidate();
        rulesPanel.repaint();
    }

    private void addToKB() {
        if (proofCreated) {
            JOptionPane.showMessageDialog(frame, "Cannot modify KB after proof creation. Please reset first.");
            return;
        }

        String text = kbField.getText().trim();
        if (text.isEmpty()) return;

        try {
            AST ast = new PropositionalAST(text, true);
            registerAtoms(List.of(ast));
            kbListData.add(ast);
            refreshKBCheckboxes();
            kbField.setText("");
        }
        catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Invalid formula: " + ex.getMessage());
        }
    }

    private void addToGoal() {
        if (proofCreated) {
            JOptionPane.showMessageDialog(frame, "Cannot modify goals after proof creation. Please reset first.");
            return;
        }

        String text = goalField.getText().trim();
        if (text.isEmpty()) return;

        try {
            AST ast = new PropositionalAST(text, true);
            registerAtoms(List.of(ast));
            goalListData.add(ast);
            goalModel.addElement(ast);
            refreshGoalCheckboxes();
            goalField.setText("");
        }
        catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Invalid formula: " + ex.getMessage());
        }
    }

    private void createProof() {
        if (kbListData.isEmpty() || goalListData.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Please add at least one KB formula and one goal");
            return;
        }

        try {
            for (AST ast : kbListData) {
                KnowledgeBaseRegistry.addObtainedFrom(ast.toString(), "Hypothesis");
            }
            for (AST ast : goalListData) {
                KnowledgeBaseRegistry.addObtainedFrom(ast.toString(), "Hypothesis");
            }

            currentProof = new ManualPropositionalProof(new ArrayList<>(kbListData), new ArrayList<>(goalListData), null, 1);
            ruleHelper = new ManualPropositionalInferenceRuleHelper(kbListData);
            proofCreated = true;

            addKbBtn.setEnabled(false);
            addGoalBtn.setEnabled(false);
            kbField.setEnabled(false);
            goalField.setEnabled(false);

            updateStateDisplay();

            JOptionPane.showMessageDialog(frame, "Proof created successfully!");

        }
        catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Error creating proof: " + ex.getMessage());
        }
    }

    private void reset() {
        kbListData.clear();
        goalListData.clear();

        goalModel.clear();

        refreshKBCheckboxes();
        refreshGoalCheckboxes();

        proofCreated = false;
        currentProof = null;

        addKbBtn.setEnabled(true);
        addGoalBtn.setEnabled(true);
        kbField.setEnabled(true);
        goalField.setEnabled(true);

        stateDisplayArea.setText("");
        clearRulesPanel();

        JOptionPane.showMessageDialog(frame, "Reset complete.");
    }

    private void handleDone() {
        if (currentProof == null || !proofCreated) {
            JOptionPane.showMessageDialog(frame, "No active proof");
            return;
        }

        try {
            boolean success = currentProof.executeCommand("done");

            if (success) {
                updateStateDisplay();
                JOptionPane.showMessageDialog(frame, "Proof completed successfully!");
            }
            else {
                JOptionPane.showMessageDialog(frame, "Cannot complete proof.");
            }
        }
        catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage());
        }
    }

    private void handleChangeState() {
        if (currentProof == null || !proofCreated) {
            JOptionPane.showMessageDialog(frame, "No active proof");
            return;
        }

        try {
            int stateIndex = Integer.parseInt(stateIndexField.getText().trim());
            String command = "chstate(S" + stateIndex + ")";

            boolean success = currentProof.executeCommand(command);

            if (success) {
                updateStateDisplay();
                JOptionPane.showMessageDialog(frame, "Changed to state " + stateIndex);
            }
            else {
                JOptionPane.showMessageDialog(frame, "Failed to change state.");
            }
        }
        catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(frame, "Invalid state index");
        }
        catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage());
        }
    }

    private void updateStateDisplay() {
        if (currentProof == null || !proofCreated) return;

        try {
            String stateText = currentProof.getStateText();
            stateDisplayArea.setText(stateText);
            refreshKBCheckboxes();
        }
        catch (Exception e) {
            stateDisplayArea.setText("Error getting state: " + e.getMessage());
        }
    }

    private void registerAtoms(List<AST> asts) {
        for (AST ast : asts) {
            String str = ast.toString();
            for (char c : str.toCharArray()) {
                if (Character.isUpperCase(c)) {
                    GlobalAtomID.addAtomId(String.valueOf(c));
                }
            }
        }
    }

    private static class CommandInfo {
        String command;
        int arity;
        boolean fixedArity;

        CommandInfo(String command, int arity, boolean fixedArity) {
            this.command = command;
            this.arity = arity;
            this.fixedArity = fixedArity;
        }
    }
}