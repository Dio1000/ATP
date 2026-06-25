package me.dariansandru.gui;

import me.dariansandru.domain.data_structures.ast.AST;
import me.dariansandru.domain.data_structures.ast.PropositionalAST;
import me.dariansandru.domain.language.UniverseOfDiscourse;
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
import me.dariansandru.domain.language.signature.Signature;
import me.dariansandru.utils.global.GlobalAtomID;
import me.dariansandru.utils.helper.KnowledgeBaseRegistry;
import me.dariansandru.utils.loader.PropositionalLogicLoader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Main proof workbench window.
 *
 * Layout changes in this revision:
 *   1. "Load Package" and "Rule Editor" moved out of their own strip and into the
 *      header of the "Applicable Rules & Strategies" panel (top-right corner),
 *      since they're both about extending the rule set used during proof, not
 *      proof-state navigation.
 *   2. "Create Proof", "Reset", "Done" moved to the LEFT side of the automate
 *      control row, sharing the same horizontal strip as "Automate" (which sits
 *      on the right). This consolidates all primary proof-lifecycle actions
 *      onto a single row instead of splitting them across the title bar and a
 *      separate bottom strip.
 *   3. Color palette consolidated. Previously each button used an arbitrarily
 *      chosen accent (purple, pink, teal, orange, info-blue, danger-red, etc.)
 *      with no semantic consistency. Buttons are now grouped by INTENT using
 *      exactly four tones:
 *        - NEUTRAL (slate)   -> default / navigational actions (Reset, Go to
 *                               State, Load Package, Rule Editor, "+")
 *        - PRIMARY (violet)  -> the main forward action of a panel (Create
 *                               Proof, Apply)
 *        - POSITIVE (green)  -> successful completion (Done)
 *        - ATTENTION (amber) -> triggers a potentially slow/heavy operation
 *                               (Automate)
 *      Custom-rule markers keep their own dedicated amber-adjacent tone so they
 *      stay visually distinguishable from built-in rules, but every button's
 *      color now maps to one of these four intents instead of being chosen
 *      arbitrarily per-feature.
 */
public class PropositionalProofGUIController {

    // ── Palette: backgrounds ─────────────────────────────────────────────────
    private static final Color BACKGROUND_DEEP    = new Color(0x0F1117);
    private static final Color BACKGROUND_SURFACE = new Color(0x1A1D27);
    private static final Color BACKGROUND_RAISED  = new Color(0x22263A);
    private static final Color BACKGROUND_HOVER   = new Color(0x2A2F48);

    // ── Palette: semantic intents (consolidated from 8 arbitrary accents to 4) ─
    private static final Color INTENT_NEUTRAL   = new Color(0x3A3F58); // slate — default / navigation
    private static final Color INTENT_PRIMARY   = new Color(0x6C63FF); // violet — main forward action
    private static final Color INTENT_POSITIVE  = new Color(0x3DDC84); // green — success / completion
    private static final Color INTENT_ATTENTION = new Color(0xE0A22D); // amber — heavy/slow operation
    private static final Color INTENT_DANGER    = new Color(0xE0526B); // muted red — destructive

    // Rule-row accents (kept distinct from button intents, used only for the
    // small dot markers in the rule list, not for buttons)
    private static final Color RULE_BUILTIN_DOT = INTENT_PRIMARY;
    private static final Color RULE_CUSTOM_DOT  = INTENT_ATTENTION;

    private static final Color TEXT_PRIMARY     = new Color(0xECEFF4);
    private static final Color TEXT_SECONDARY   = new Color(0x8A8FA8);
    private static final Color TEXT_DIM         = new Color(0x565A72);
    private static final Color BORDER_COLOR     = new Color(0x2E3350);
    private static final Color CHECKBOX_FOREGROUND = new Color(0xB0B4CC);

    private static final Font TITLE_FONT       = new Font("SansSerif", Font.BOLD, 15);
    private static final Font LABEL_FONT       = new Font("SansSerif", Font.PLAIN, 12);
    private static final Font LABEL_BOLD_FONT  = new Font("SansSerif", Font.BOLD, 12);
    private static final Font MONOSPACED_FONT  = new Font("Monospaced", Font.PLAIN, 12);
    private static final Font SMALL_FONT       = new Font("SansSerif", Font.PLAIN, 10);
    private static final Font SECTION_FONT     = new Font("SansSerif", Font.BOLD, 11);
    private static final Font BUTTON_FONT      = new Font("SansSerif", Font.BOLD, 12);

    private JFrame mainFrame;
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

    // Collapsible "Automated Proof Output" support: the panel is built once
    // but only attached to the visible tree after the first Automate click.
    private JSplitPane topSplit;
    private JPanel automatedContainerPanel;
    private JPanel rightContentHolder;
    private boolean automatedOutputVisible = false;

    private JButton createProofButton;
    private JButton resetButton;
    private JButton doneButton;
    private JButton changeStateButton;
    private JTextField stateIndexField;
    private JButton rulesButton;
    private JButton automateButton;
    private JButton loadPackageButton;
    private JButton createEditorButton;

    private JLabel statusLabel;

    private final List<AST> selectedKnowledgeBaseFormulas = new ArrayList<>();
    private final List<AST> selectedGoals = new ArrayList<>();
    private final Map<String, CommandInfo> commandMap = new HashMap<>();
    private final List<InferenceRule> customRules = new ArrayList<>();
    private final PropositionalLogicLoader loader = new PropositionalLogicLoader();
    private String loadedPackage = null;

    public PropositionalProofGUIController() {
        this.initializeCommandMap();
        this.installLookAndFeel();
        this.initialize();
    }

    private void installLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (Exception ignored) {
        }

        UIManager.put("Panel.background", BACKGROUND_SURFACE);
        UIManager.put("ScrollPane.background", BACKGROUND_SURFACE);
        UIManager.put("Viewport.background", BACKGROUND_SURFACE);
        UIManager.put("TextArea.background", BACKGROUND_DEEP);
        UIManager.put("TextArea.foreground", TEXT_PRIMARY);
        UIManager.put("TextField.background", BACKGROUND_RAISED);
        UIManager.put("TextField.foreground", TEXT_PRIMARY);
        UIManager.put("TextField.caretForeground", INTENT_PRIMARY);
        UIManager.put("TextField.selectionBackground", new Color(0x3D3880));
        UIManager.put("CheckBox.background", BACKGROUND_RAISED);
        UIManager.put("CheckBox.foreground", CHECKBOX_FOREGROUND);
        UIManager.put("ScrollBar.thumb", BACKGROUND_RAISED);
        UIManager.put("ScrollBar.track", BACKGROUND_DEEP);
        UIManager.put("OptionPane.background", BACKGROUND_SURFACE);
        UIManager.put("OptionPane.messageForeground", TEXT_PRIMARY);
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

    private static String convertToMapKey(String name) {
        return name.toUpperCase().replace(' ', '_');
    }

    private void initialize() {
        this.mainFrame = new JFrame("Propositional Proof System");
        this.mainFrame.setSize(1440, 860);
        this.mainFrame.setMinimumSize(new Dimension(1100, 650));
        this.mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.mainFrame.setLayout(new BorderLayout());
        this.mainFrame.getContentPane().setBackground(BACKGROUND_DEEP);

        this.mainFrame.add(this.buildTitleBar(), BorderLayout.NORTH);
        this.mainFrame.add(this.buildMainArea(), BorderLayout.CENTER);
        this.mainFrame.add(this.buildStatusBar(), BorderLayout.SOUTH);

        this.mainFrame.setVisible(true);
        this.refreshGoalCheckboxes();
    }

    // ── Title bar: app title + Create Proof / Reset / Done + Rules ────────────
    private JPanel buildTitleBar() {
        JPanel titleBarPanel = new JPanel(new BorderLayout());
        titleBarPanel.setBackground(BACKGROUND_DEEP);
        titleBarPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
                BorderFactory.createEmptyBorder(14, 20, 14, 20)));

        JLabel titleLabel = new JLabel("Propositional Proof Assistant");
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(TEXT_PRIMARY);

        JPanel leftCluster = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        leftCluster.setOpaque(false);
        leftCluster.add(titleLabel);
        leftCluster.add(this.buildProofActionsCluster());
        titleBarPanel.add(leftCluster, BorderLayout.WEST);

        this.rulesButton = this.createButton("Rules", INTENT_NEUTRAL);
        this.rulesButton.addActionListener(event -> {
            RulesGUI rulesGui = new RulesGUI();
            rulesGui.show();
        });
        titleBarPanel.add(this.rulesButton, BorderLayout.EAST);

        return titleBarPanel;
    }

    /** Create Proof / Reset / Done cluster, placed on the LEFT of the title bar next to the app title. */
    private JPanel buildProofActionsCluster() {
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        controlPanel.setOpaque(false);

        this.createProofButton = this.createButton("Create Proof", INTENT_PRIMARY);
        this.resetButton       = this.createButton("Reset", INTENT_NEUTRAL);
        this.doneButton        = this.createButton("Done", INTENT_POSITIVE);

        this.createProofButton.addActionListener(event -> this.createProof());
        this.resetButton.addActionListener(event -> this.reset());
        this.doneButton.addActionListener(event -> this.handleDone());

        controlPanel.add(this.createProofButton);
        controlPanel.add(this.resetButton);
        controlPanel.add(this.doneButton);

        return controlPanel;
    }

    private JSplitPane buildMainArea() {
        JSplitPane outerSplitPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, this.buildLeftPanel(), this.buildRightPanel());
        outerSplitPanel.setDividerSize(6);
        outerSplitPanel.setResizeWeight(0.40);
        outerSplitPanel.setBackground(BACKGROUND_DEEP);
        outerSplitPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return outerSplitPanel;
    }

    private JPanel buildLeftPanel() {
        JPanel leftPanel = new JPanel(new BorderLayout(0, 10));
        leftPanel.setBackground(BACKGROUND_DEEP);
        leftPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 6));

        JSplitPane leftVerticalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, this.buildKnowledgeBasePanel(), this.buildGoalPanel());
        leftVerticalSplit.setDividerSize(6);
        leftVerticalSplit.setResizeWeight(0.70);
        leftVerticalSplit.setBackground(BACKGROUND_DEEP);

        leftPanel.add(leftVerticalSplit, BorderLayout.CENTER);
        return leftPanel;
    }

    private JPanel buildKnowledgeBasePanel() {
        JPanel knowledgeBasePanel = new JPanel(new BorderLayout(0, 8));
        knowledgeBasePanel.setBackground(BACKGROUND_SURFACE);
        knowledgeBasePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)));
        knowledgeBasePanel.add(this.buildSectionHeader("Knowledge Base", ""), BorderLayout.NORTH);

        this.knowledgeBaseCheckboxPanel = new JPanel();
        this.knowledgeBaseCheckboxPanel.setLayout(new BoxLayout(this.knowledgeBaseCheckboxPanel, BoxLayout.Y_AXIS));
        this.knowledgeBaseCheckboxPanel.setBackground(BACKGROUND_DEEP);

        JScrollPane scrollPane = this.createStyledScrollPane(this.knowledgeBaseCheckboxPanel);
        knowledgeBasePanel.add(scrollPane, BorderLayout.CENTER);
        knowledgeBasePanel.add(this.buildFormulaInputBar(true), BorderLayout.SOUTH);

        return knowledgeBasePanel;
    }

    private JPanel buildGoalPanel() {
        JPanel goalPanel = new JPanel(new BorderLayout(0, 8));
        goalPanel.setBackground(BACKGROUND_SURFACE);
        goalPanel.setPreferredSize(new Dimension(0, 220));
        goalPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)));

        goalPanel.add(this.buildSectionHeader("Goals", ""), BorderLayout.NORTH);

        this.goalCheckboxPanel = new JPanel();
        this.goalCheckboxPanel.setLayout(new BoxLayout(this.goalCheckboxPanel, BoxLayout.Y_AXIS));
        this.goalCheckboxPanel.setBackground(BACKGROUND_DEEP);

        JScrollPane scrollPane = this.createStyledScrollPane(this.goalCheckboxPanel);
        goalPanel.add(scrollPane, BorderLayout.CENTER);
        goalPanel.add(this.buildFormulaInputBar(false), BorderLayout.SOUTH);

        return goalPanel;
    }

    private JPanel buildFormulaInputBar(boolean isKnowledgeBase) {
        JPanel inputBar = new JPanel(new BorderLayout(6, 0));
        inputBar.setBackground(BACKGROUND_SURFACE);
        inputBar.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));

        String placeholderText = isKnowledgeBase ? "Enter Knowledge Base formula..." : "Enter goal formula...";
        JTextField textField = this.createStyledTextField(placeholderText);

        if (isKnowledgeBase) {
            this.knowledgeBaseInputField = textField;
            textField.addActionListener(event -> this.addToKnowledgeBase());
        }
        else {
            this.goalInputField = textField;
            textField.addActionListener(event -> this.addToGoal());
        }

        JButton addButton = this.createButton("+", INTENT_NEUTRAL);
        addButton.setPreferredSize(new Dimension(36, 30));
        addButton.addActionListener(event -> this.openBatchFormulaDialog(isKnowledgeBase));

        inputBar.add(textField, BorderLayout.CENTER);
        inputBar.add(addButton, BorderLayout.EAST);

        return inputBar;
    }

    private JPanel buildRightPanel() {
        // Rules Container - Applicable Rules & Strategies
        JPanel rulesContainerPanel = new JPanel(new BorderLayout(0, 0));
        rulesContainerPanel.setBackground(BACKGROUND_DEEP);

        this.rulesPanel = new JPanel();
        this.rulesPanel.setLayout(new BoxLayout(this.rulesPanel, BoxLayout.Y_AXIS));
        this.rulesPanel.setBackground(BACKGROUND_DEEP);

        JScrollPane rulesScrollPane = this.createStyledScrollPane(this.rulesPanel);
        JPanel rulesWrapper = this.wrapInSection(rulesScrollPane, "Applicable Rules & Strategies", "");
        rulesContainerPanel.add(rulesWrapper, BorderLayout.CENTER);
        // "Load Package" and "Rule Editor" sit directly underneath this panel.
        rulesContainerPanel.add(this.buildRuleSourceActions(), BorderLayout.SOUTH);

        // State Container - Proof State
        this.stateDisplayArea = new JTextArea();
        this.stateDisplayArea.setEditable(false);
        this.stateDisplayArea.setFont(MONOSPACED_FONT);
        this.stateDisplayArea.setBackground(BACKGROUND_DEEP);
        this.stateDisplayArea.setForeground(new Color(0xA3E6B0));
        this.stateDisplayArea.setCaretColor(INTENT_PRIMARY);
        this.stateDisplayArea.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        this.stateDisplayArea.setLineWrap(true);
        this.stateDisplayArea.setWrapStyleWord(true);

        JPanel stateContainerPanel = new JPanel(new BorderLayout(0, 8));
        stateContainerPanel.setBackground(BACKGROUND_DEEP);

        JPanel stateWrapper = this.wrapInSection(this.createStyledScrollPane(this.stateDisplayArea), "Proof State", "");
        JPanel stateControlPanel = this.buildStateControlPanel();

        stateContainerPanel.add(stateWrapper, BorderLayout.CENTER);
        stateContainerPanel.add(stateControlPanel, BorderLayout.SOUTH);

        // Top Split - Rules and State (equal size). This is the ONLY thing shown
        // until Automate is clicked for the first time, so it gets the full
        // vertical space of the right panel by default.
        this.topSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, rulesContainerPanel, stateContainerPanel);
        this.topSplit.setDividerSize(6);
        this.topSplit.setResizeWeight(0.50);
        this.topSplit.setBackground(BACKGROUND_DEEP);

        // Automated Proof Output panel — built once, but NOT attached to the
        // visible tree until the user clicks "Automate" for the first time.
        this.automatedProofOutputArea = new JTextArea();
        this.automatedProofOutputArea.setEditable(false);
        this.automatedProofOutputArea.setFont(MONOSPACED_FONT);
        this.automatedProofOutputArea.setBackground(BACKGROUND_DEEP);
        this.automatedProofOutputArea.setForeground(TEXT_PRIMARY);
        this.automatedProofOutputArea.setCaretColor(INTENT_PRIMARY);
        this.automatedProofOutputArea.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        this.automatedProofOutputArea.setLineWrap(true);
        this.automatedProofOutputArea.setWrapStyleWord(true);

        JPanel automatedWrapper = this.wrapInSection(this.createStyledScrollPane(this.automatedProofOutputArea), "Automated Proof Output", "");
        this.automatedContainerPanel = new JPanel(new BorderLayout(0, 8));
        this.automatedContainerPanel.setBackground(BACKGROUND_DEEP);
        this.automatedContainerPanel.add(automatedWrapper, BorderLayout.CENTER);

        // Right panel root: a single content slot that holds EITHER just
        // topSplit (default), OR a topSplit/automatedOutput split (after
        // Automate has been clicked once). Swapped at runtime in
        // showAutomatedOutputPanel(), never rebuilt from scratch.
        this.rightContentHolder = new JPanel(new BorderLayout());
        this.rightContentHolder.setBackground(BACKGROUND_DEEP);
        this.rightContentHolder.add(this.topSplit, BorderLayout.CENTER);

        JPanel rightRoot = new JPanel(new BorderLayout(0, 8));
        rightRoot.setBackground(BACKGROUND_DEEP);
        rightRoot.add(this.rightContentHolder, BorderLayout.CENTER);
        // The Automate trigger strip is always visible at the bottom, regardless
        // of whether the output panel itself is showing yet.
        rightRoot.add(this.buildAutomatePanel(), BorderLayout.SOUTH);

        return rightRoot;
    }

    /**
     * Reveals the "Automated Proof Output" panel the first time Automate is
     * run. Before this is called, the right side shows only Rules + Proof
     * State (each gets the full available height). After this is called,
     * the content holder is swapped to a Rules+State / Automated-Output
     * split, and subsequent automate runs just update the text area without
     * touching the layout again.
     */
    private void revealAutomatedOutputPanel() {
        if (this.automatedOutputVisible) {
            return;
        }
        this.automatedOutputVisible = true;

        this.rightContentHolder.removeAll();

        JSplitPane mainRightSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, this.topSplit, this.automatedContainerPanel);
        mainRightSplit.setDividerSize(6);
        mainRightSplit.setResizeWeight(0.70);
        mainRightSplit.setBackground(BACKGROUND_DEEP);

        this.rightContentHolder.add(mainRightSplit, BorderLayout.CENTER);
        this.rightContentHolder.revalidate();
        this.rightContentHolder.repaint();
    }

    private JPanel buildAutomatePanel() {
        JPanel automatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        automatePanel.setBackground(BACKGROUND_SURFACE);
        automatePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));

        this.automateButton = this.createButton("Automate", INTENT_ATTENTION);
        this.automateButton.addActionListener(event -> this.runAutomatedProof());

        automatePanel.add(this.automateButton);
        return automatePanel;
    }

    /** Action row shown directly under the "Applicable Rules & Strategies" panel. */
    private JPanel buildRuleSourceActions() {
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        actions.setBackground(BACKGROUND_SURFACE);
        actions.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));

        this.loadPackageButton  = this.createButton("Load Package", INTENT_ATTENTION);
        this.createEditorButton = this.createButton("Rule Editor", INTENT_PRIMARY);

        this.loadPackageButton.addActionListener(event -> this.showPackageLoaderDialog());
        this.createEditorButton.addActionListener(event -> this.openRuleEditor());

        actions.add(this.loadPackageButton);
        actions.add(this.createEditorButton);
        return actions;
    }

    private JPanel buildStateControlPanel() {
        JPanel stateControlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        stateControlPanel.setBackground(BACKGROUND_SURFACE);
        stateControlPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));

        JLabel stateLabel = new JLabel("State:");
        stateLabel.setForeground(TEXT_SECONDARY);
        stateLabel.setFont(LABEL_FONT);

        this.stateIndexField = this.createStyledTextField("");
        this.stateIndexField.setPreferredSize(new Dimension(52, 28));
        this.stateIndexField.setHorizontalAlignment(JTextField.CENTER);

        this.changeStateButton = this.createButton("Go to State", INTENT_NEUTRAL);
        this.changeStateButton.addActionListener(event -> this.handleChangeState());

        stateControlPanel.add(stateLabel);
        stateControlPanel.add(this.stateIndexField);
        stateControlPanel.add(this.changeStateButton);

        return stateControlPanel;
    }

    private void showPackageLoaderDialog() {
        String customRulesPath = "files/customRules";
        File directory = new File(customRulesPath);

        if (!directory.exists() || !directory.isDirectory()) {
            JOptionPane.showMessageDialog(this.mainFrame,
                    "Custom rules directory not found: " + customRulesPath,
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        File[] subDirectories = directory.listFiles(File::isDirectory);
        if (subDirectories == null || subDirectories.length == 0) {
            JOptionPane.showMessageDialog(this.mainFrame,
                    "No packages found in " + customRulesPath,
                    "Information",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        List<String> packageNames = new ArrayList<>();
        for (File subDirectory : subDirectories) {
            if (!subDirectory.getName().equals("meta")) {
                packageNames.add(subDirectory.getName());
            }
        }

        if (packageNames.isEmpty()) {
            JOptionPane.showMessageDialog(this.mainFrame,
                    "No valid packages found (excluding meta)",
                    "Information",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String selectedPackage = (String) JOptionPane.showInputDialog(
                this.mainFrame,
                "Select a package to load custom rules from:",
                "Load Custom Rules Package",
                JOptionPane.QUESTION_MESSAGE,
                null,
                packageNames.toArray(),
                this.loadedPackage != null ? this.loadedPackage : packageNames.get(0)
        );

        if (selectedPackage != null && !selectedPackage.isEmpty()) {
            this.loadPackage(selectedPackage);
        }
    }

    private void loadPackage(String packageName) {
        String customRulesPath = "files/customRules/" + packageName;
        File packageDirectory = new File(customRulesPath);

        if (!packageDirectory.exists() || !packageDirectory.isDirectory()) {
            this.setStatus("Package not found: " + packageName, INTENT_DANGER);
            return;
        }

        this.customRules.clear();
        int loadedCount = 0;

        File[] files = packageDirectory.listFiles((directory, name) -> name.endsWith(".atpf"));
        if (files != null) {
            for (File file : files) {
                try {
                    List<String> lines = this.loader.getLines(file.getPath());
                    if (lines != null && !lines.isEmpty()) {
                        int count = this.parseCustomRules(lines);
                        loadedCount += count;
                    }
                }
                catch (Exception exception) {
                    System.err.println("Error loading custom rule file: " + file.getName());
                    exception.printStackTrace();
                }
            }
        }

        this.loadedPackage = packageName;
        this.setStatus("Loaded " + loadedCount + " custom rules from package: " + packageName, INTENT_POSITIVE);

        if (this.proofCreated) {
            this.updateApplicableItems();
        }
    }

    private int parseCustomRules(List<String> lines) {
        int count = 0;
        int index = 0;

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
                    CustomPropositionalInferenceRule customRule = this.loader.loadCustomRule(ruleLines, ruleName);
                    if (customRule != null && customRule.conclusion() != null) {
                        this.customRules.add(customRule);
                        count++;
                        System.out.println("Loaded custom rule: " + customRule.name() + " with " +
                                customRule.antecedents().size() + " antecedents");
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

    private void runAutomatedProof() {
        if (this.knowledgeBaseEntries.isEmpty() || this.goalListData.isEmpty()) {
            this.setStatus("Please add knowledge base formulas and goals before automating.", INTENT_DANGER);
            return;
        }

        // Reveal the output panel now that we know there's something to show.
        this.revealAutomatedOutputPanel();

        try {
            this.automatedProofOutputArea.setText("Running automated proof...\n\n");

            Signature signature = SignatureFactory.createSignature(UniverseOfDiscourse.PROPOSITIONS);

            PropositionalProof automatedProof = new PropositionalProof(
                    signature,
                    new ArrayList<>(this.knowledgeBaseEntries),
                    new ArrayList<>(this.goalListData)
            );

            boolean result = automatedProof.proveWithoutPrinting();

            if (result) {
                this.automatedProofOutputArea.append("Proof completed successfully!\n\n");
                this.automatedProofOutputArea.append("Proof steps:\n");
                this.automatedProofOutputArea.append("----------------------------------------\n");

                java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
                java.io.PrintStream originalOut = System.out;
                java.io.PrintStream printStream = new java.io.PrintStream(outputStream);
                System.setOut(printStream);

                automatedProof.printProof();

                System.out.flush();
                System.setOut(originalOut);

                String proofText = outputStream.toString();
                this.automatedProofOutputArea.append(proofText);

                this.setStatus("Automated proof completed successfully!", INTENT_POSITIVE);
            }
            else {
                this.automatedProofOutputArea.append("Proof could not be completed automatically.\n");
                this.automatedProofOutputArea.append("Try manual proof or check your formulas.\n");
                this.setStatus("Automated proof failed.", INTENT_DANGER);
            }

        }
        catch (Exception exception) {
            this.automatedProofOutputArea.append("Error during automated proof:\n");
            this.automatedProofOutputArea.append(exception.getMessage());
            this.setStatus("Error: " + exception.getMessage(), INTENT_DANGER);
            exception.printStackTrace();
        }
    }

    private void addToKnowledgeBase() {
        if (this.proofCreated) {
            this.setStatus("Proof already created, reset to modify the knowledge base.", INTENT_DANGER);
            return;
        }
        String text = this.knowledgeBaseInputField.getText().trim();
        if (text.isEmpty()) {
            return;
        }
        try {
            PropositionalAST ast = new PropositionalAST(text, true);
            if (!ast.isValid()) {
                this.setStatus("Formula: " + ast + " is not valid!", TEXT_SECONDARY);
                this.knowledgeBaseInputField.setText("");
                return;
            }
            this.registerAtoms(List.of(ast));
            this.knowledgeBaseEntries.add(ast);
            this.refreshKnowledgeBaseEntriesDisplay();
            this.knowledgeBaseInputField.setText("");
            this.setStatus("Added to knowledge base: " + ast, TEXT_SECONDARY);
        }
        catch (Exception exception) {
            this.setStatus("Invalid formula: " + exception.getMessage(), INTENT_DANGER);
        }
    }

    private void openBatchFormulaDialog(boolean isKnowledgeBase) {
        JDialog dialog = new JDialog(this.mainFrame, isKnowledgeBase ? "Add Knowledge Base Formulas" : "Add Goal Formulas", true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this.mainFrame);
        dialog.setLayout(new BorderLayout(10, 10));

        JTextArea textArea = new JTextArea();
        textArea.setFont(MONOSPACED_FONT);
        textArea.setBackground(BACKGROUND_DEEP);
        textArea.setForeground(TEXT_PRIMARY);
        textArea.setCaretColor(INTENT_PRIMARY);

        JScrollPane scrollPane = new JScrollPane(textArea);

        JButton addButton = this.createButton("Add All", INTENT_PRIMARY);
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
                    this.registerAtoms(List.of(ast));
                    parsedFormulas.add(ast);
                }
                catch (Exception exception) {
                    errors.add(trimmedLine + " -> " + exception.getMessage());
                }
            }

            if (isKnowledgeBase) {
                this.knowledgeBaseEntries.addAll(parsedFormulas);
                for (AST entry : this.knowledgeBaseEntries) {
                    KnowledgeBaseRegistry.addObtainedFrom(entry.toString(), "Hypothesis");
                }
                this.refreshKnowledgeBaseEntriesDisplay();
                this.setStatus("Added " + parsedFormulas.size() + " KB formulas", INTENT_POSITIVE);
            }
            else {
                this.goalListData.addAll(parsedFormulas);
                for (AST entry : parsedFormulas) {
                    KnowledgeBaseRegistry.addObtainedFrom(entry.toString(), "Hypothesis");
                }
                this.refreshGoalCheckboxes();
                this.setStatus("Added " + parsedFormulas.size() + " goals", INTENT_POSITIVE);
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
        bottomPanel.setBackground(BACKGROUND_SURFACE);
        bottomPanel.add(addButton);

        dialog.add(scrollPane, BorderLayout.CENTER);
        dialog.add(bottomPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private void refreshKnowledgeBaseEntriesDisplay() {
        this.knowledgeBaseCheckboxPanel.removeAll();
        this.knowledgeBaseCheckboxes.clear();
        for (int i = 0; i < this.knowledgeBaseEntries.size(); i++) {
            JPanel row = this.buildFormulaRow(i + 1, this.knowledgeBaseEntries.get(i), false, true);
            this.knowledgeBaseCheckboxPanel.add(row);
        }
        this.knowledgeBaseCheckboxPanel.revalidate();
        this.knowledgeBaseCheckboxPanel.repaint();
    }

    private void createProof() {
        if (this.goalListData.isEmpty()) {
            this.setStatus("Please add at least one goal before creating a proof.", INTENT_DANGER);
            return;
        }
        try {
            for (AST ast : this.knowledgeBaseEntries) {
                KnowledgeBaseRegistry.addObtainedFrom(ast.toString(), "Hypothesis");
            }
            for (AST ast : this.goalListData) {
                KnowledgeBaseRegistry.addObtainedFrom(ast.toString(), "Hypothesis");
            }

            this.currentProof = new ManualPropositionalProof(
                    new ArrayList<>(this.knowledgeBaseEntries),
                    new ArrayList<>(this.goalListData),
                    null, 1);
            this.proofCreated = true;
            ManualPropositionalProofStates.addOriginalState(this.currentProof);

            this.knowledgeBaseInputField.setEnabled(false);
            this.goalInputField.setEnabled(false);

            this.refreshKnowledgeBaseCheckboxes();
            this.refreshGoalCheckboxes();
            this.updateStateDisplay();
            this.createProofButton.setEnabled(false);
            this.setStatus("Proof created - select KB formulas or goals to apply rules.", INTENT_POSITIVE);

        }
        catch (Exception exception) {
            this.setStatus("Error creating proof: " + exception.getMessage(), INTENT_DANGER);
        }
    }

    private void refreshKnowledgeBaseCheckboxes() {
        if (this.currentProof == null) {
            return;
        }

        List<AST> knowledgeBase = this.currentProof.getKnowledgeBase();
        this.knowledgeBaseCheckboxPanel.removeAll();
        this.knowledgeBaseCheckboxes.clear();

        for (int i = 0; i < knowledgeBase.size(); i++) {
            JPanel row = this.buildFormulaRow(i + 1, knowledgeBase.get(i), true, true);
            this.knowledgeBaseCheckboxPanel.add(row);
        }

        this.knowledgeBaseCheckboxPanel.revalidate();
        this.knowledgeBaseCheckboxPanel.repaint();

        Container ancestor = SwingUtilities.getAncestorOfClass(JScrollPane.class, this.knowledgeBaseCheckboxPanel);
        if (ancestor != null) {
            ancestor.revalidate();
            ancestor.repaint();
        }
    }

    private void reset() {
        this.knowledgeBaseEntries.clear();
        this.knowledgeBaseInputField.setText("");
        this.knowledgeBaseInputField.setEnabled(true);

        this.goalListData.clear();
        this.goalModel.clear();
        this.goalInputField.setText("");
        this.goalInputField.setEnabled(true);

        this.proofCreated = false;
        this.currentProof = null;

        this.selectedKnowledgeBaseFormulas.clear();
        this.selectedGoals.clear();

        this.refreshGoalCheckboxes();
        this.refreshKnowledgeBaseEntriesDisplay();

        this.stateDisplayArea.setText("");
        this.automatedProofOutputArea.setText("");
        this.clearRulesPanel();

        this.createProofButton.setEnabled(true);
        this.setStatus("Reset, ready for a new proof.", TEXT_SECONDARY);
    }

    private void refreshGoalCheckboxes() {
        if (this.goalCheckboxPanel == null) {
            return;
        }
        this.goalCheckboxPanel.removeAll();
        this.goalCheckboxes.clear();

        for (int i = 0; i < this.goalListData.size(); i++) {
            this.goalCheckboxPanel.add(this.buildFormulaRow(i + 1, this.goalListData.get(i), true, false));
        }
        for (JCheckBox checkBox : this.goalCheckboxes) {
            checkBox.setEnabled(this.proofCreated);
        }

        this.goalCheckboxPanel.revalidate();
        this.goalCheckboxPanel.repaint();
    }

    private void addToGoal() {
        if (this.proofCreated) {
            this.setStatus("Proof already created - reset to modify goals.", INTENT_DANGER);
            return;
        }
        String text = this.goalInputField.getText().trim();
        if (text.isEmpty()) {
            return;
        }
        try {
            AST ast = new PropositionalAST(text, true);
            this.registerAtoms(List.of(ast));
            this.goalListData.add(ast);
            this.goalModel.addElement(ast);
            this.refreshGoalCheckboxes();
            this.goalInputField.setText("");
            this.setStatus("Goal added: " + ast, TEXT_SECONDARY);
        }
        catch (Exception exception) {
            this.setStatus("Invalid formula: " + exception.getMessage(), INTENT_DANGER);
        }
    }

    private void updateSelectedFormulas() {
        this.selectedKnowledgeBaseFormulas.clear();
        this.selectedGoals.clear();

        if (this.currentProof != null) {
            List<AST> knowledgeBase = this.currentProof.getKnowledgeBase();
            for (int i = 0; i < this.knowledgeBaseCheckboxes.size() && i < knowledgeBase.size(); i++) {
                if (this.knowledgeBaseCheckboxes.get(i).isSelected()) {
                    this.selectedKnowledgeBaseFormulas.add(knowledgeBase.get(i));
                }
            }
        }

        List<AST> liveGoals = (this.currentProof != null) ? this.currentProof.getGoals() : this.goalListData;
        for (int i = 0; i < this.goalCheckboxes.size() && i < liveGoals.size(); i++) {
            if (this.goalCheckboxes.get(i).isSelected()) {
                this.selectedGoals.add(liveGoals.get(i));
            }
        }

        if (this.currentProof != null && this.proofCreated && (!this.selectedKnowledgeBaseFormulas.isEmpty() || !this.selectedGoals.isEmpty())) {
            this.updateApplicableItems();
        }
        else {
            this.clearRulesPanel();
        }
    }

    private void updateApplicableItems() {
        List<InferenceRule> allRules = new ArrayList<>();

        if (!this.selectedKnowledgeBaseFormulas.isEmpty()) {
            ManualPropositionalInferenceRuleHelper helper =
                    new ManualPropositionalInferenceRuleHelper(this.currentProof.getKnowledgeBase());
            allRules.addAll(helper.applicableRules(this.selectedKnowledgeBaseFormulas));

            ContradictionRule contradictionRule = new ContradictionRule();
            allRules.add(contradictionRule);

            for (InferenceRule customRule : this.customRules) {
                try {
                    List<AST> selectedCopy = new ArrayList<>(this.selectedKnowledgeBaseFormulas);
                    AST goal = this.selectedKnowledgeBaseFormulas.get(0);

                    System.out.println(selectedCopy + " " + goal);
                    if (customRule.canInference(selectedCopy, goal)) {
                        allRules.add(customRule);
                        System.out.println("Custom rule applicable: " + customRule.name());
                    }
                }
                catch (Exception exception) {
                    System.err.println("Error checking custom rule: " + customRule.name());
                    exception.printStackTrace();
                }
            }
        }

        List<Strategy> strategies = new ArrayList<>();
        for (AST goal : this.selectedGoals) {
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
        this.rulesPanel.removeAll();

        if (rules.isEmpty() && strategies.isEmpty()) {
            JLabel emptyLabel = new JLabel("No applicable rules for the selected formulas");
            emptyLabel.setFont(LABEL_FONT);
            emptyLabel.setForeground(TEXT_DIM);
            emptyLabel.setBorder(BorderFactory.createEmptyBorder(20, 12, 20, 12));
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            this.rulesPanel.add(emptyLabel);
        }
        else {
            if (!rules.isEmpty()) {
                this.rulesPanel.add(this.buildSectionDivider("INFERENCE RULES"));
                for (InferenceRule rule : rules) {
                    boolean isCustom = this.customRules.contains(rule);
                    this.rulesPanel.add(this.buildRuleRow(
                            rule.name(),
                            isCustom,
                            () -> this.applyRule(rule)
                    ));
                }
            }
            if (!strategies.isEmpty()) {
                this.rulesPanel.add(this.buildSectionDivider("PROOF STRATEGIES"));
                for (Strategy strategy : strategies) {
                    this.rulesPanel.add(this.buildRuleRow(
                            this.formatStrategyName(strategy),
                            false,
                            () -> this.applyStrategy(strategy)
                    ));
                }
            }
        }

        this.rulesPanel.revalidate();
        this.rulesPanel.repaint();
    }

    private JPanel buildSectionDivider(String label) {
        JPanel dividerPanel = new JPanel(new BorderLayout(8, 0));
        dividerPanel.setBackground(BACKGROUND_DEEP);
        dividerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        dividerPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 4, 8));

        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(SECTION_FONT);
        labelComponent.setForeground(TEXT_DIM);

        JSeparator separator = new JSeparator();
        separator.setForeground(BORDER_COLOR);

        dividerPanel.add(labelComponent, BorderLayout.WEST);
        dividerPanel.add(separator, BorderLayout.CENTER);
        return dividerPanel;
    }

    private String formatStrategyName(Strategy strategy) {
        return strategy.name().replace('_', ' ');
    }

    private void applyRule(InferenceRule rule) {
        if (this.currentProof == null || !this.proofCreated) {
            this.setStatus("No active proof, create one first.", INTENT_DANGER);
            return;
        }

        String ruleName = rule.name();

        if (this.customRules.contains(rule)) {
            try {
                List<AST> knowledgeBase = this.currentProof.getKnowledgeBase();
                List<AST> selectedCopy = new ArrayList<>(this.selectedKnowledgeBaseFormulas);
                AST goal = this.selectedKnowledgeBaseFormulas.get(0);

                List<AST> result = rule.inference(selectedCopy, goal);

                if (result != null && !result.isEmpty()) {
                    for (AST ast : result) {
                        if (!knowledgeBase.contains(ast)) {
                            knowledgeBase.add(ast);
                            KnowledgeBaseRegistry.addObtainedFrom(ast.toString(), "Custom Rule: " + ruleName);
                            System.out.println("Added to KB: " + ast + " from " + ruleName);
                        }
                    }
                    this.refreshKnowledgeBaseCheckboxes();
                    this.updateStateDisplay();
                    this.clearSelections();
                    this.setStatus("Applied custom rule: " + ruleName, INTENT_POSITIVE);
                }
                else {
                    this.setStatus("Custom rule could not be applied.", INTENT_DANGER);
                }
            }
            catch (Exception exception) {
                this.setStatus("Error applying custom rule: " + exception.getMessage(), INTENT_DANGER);
                exception.printStackTrace();
            }
            return;
        }

        CommandInfo commandInfo = this.commandMap.get(convertToMapKey(ruleName));

        if (commandInfo == null) {
            this.setStatus("Rule '" + ruleName + "' is not mapped to a command.", INTENT_DANGER);
            return;
        }

        int selectedCount = this.selectedKnowledgeBaseFormulas.size();
        if (commandInfo.fixedArity) {
            if (selectedCount != commandInfo.arity) {
                this.setStatus(String.format("'%s' needs exactly %d formula(s), %d selected.", ruleName, commandInfo.arity, selectedCount), INTENT_DANGER);
                return;
            }
        }
        else {
            if (selectedCount < 1) {
                this.setStatus("Select at least one formula.", INTENT_DANGER);
                return;
            }
            if (commandInfo.arity > 0 && selectedCount > commandInfo.arity) {
                this.setStatus(String.format("'%s' takes at most %d formula(s), %d selected.", ruleName, commandInfo.arity, selectedCount), INTENT_DANGER);
                return;
            }
        }

        List<AST> knowledgeBase = this.currentProof.getKnowledgeBase();
        StringBuilder commandBuilder = new StringBuilder(commandInfo.command).append("(");
        for (int i = 0; i < this.selectedKnowledgeBaseFormulas.size(); i++) {
            int index = knowledgeBase.indexOf(this.selectedKnowledgeBaseFormulas.get(i));
            if (i > 0) {
                commandBuilder.append(", ");
            }
            commandBuilder.append("KB").append(index + 1);
        }
        commandBuilder.append(")");

        try {
            if (this.currentProof.executeCommand(commandBuilder.toString())) {
                this.refreshKnowledgeBaseCheckboxes();
                this.updateStateDisplay();
                this.clearSelections();
                this.setStatus("Applied: " + ruleName, INTENT_POSITIVE);
            }
            else {
                this.setStatus("Rule could not be applied.", INTENT_DANGER);
            }
        }
        catch (Exception exception) {
            this.setStatus("Error: " + exception.getMessage(), INTENT_DANGER);
        }
    }

    private void applyStrategy(Strategy strategy) {
        if (this.currentProof == null || !this.proofCreated) {
            this.setStatus("No active proof - create one first.", INTENT_DANGER);
            return;
        }
        if (this.selectedGoals.isEmpty()) {
            this.setStatus("Select at least one goal to apply a strategy.", INTENT_DANGER);
            return;
        }

        CommandInfo commandInfo = this.commandMap.get(convertToMapKey(strategy.name()));
        if (commandInfo == null) {
            this.setStatus("Strategy '" + strategy.name() + "' is not mapped.", INTENT_DANGER);
            return;
        }

        List<AST> liveGoals = this.currentProof.getGoals();

        for (AST goal : this.selectedGoals) {
            int index = (liveGoals != null) ? liveGoals.indexOf(goal) : -1;
            if (index < 0) {
                this.setStatus("Goal not found in current proof state.", INTENT_DANGER);
                continue;
            }

            String command = commandInfo.command + "(G" + (index + 1) + ")";
            try {
                if (this.currentProof.executeCommand(command)) {
                    this.refreshCheckboxes();
                    this.setStatus("Strategy applied: " + this.formatStrategyName(strategy), INTENT_POSITIVE);
                }
                else {
                    this.setStatus("Strategy could not be applied to: " + goal, INTENT_DANGER);
                }
            }
            catch (Exception exception) {
                this.setStatus("Error applying strategy: " + exception.getMessage(), INTENT_DANGER);
            }
        }

        this.clearSelections();
    }

    private void clearSelections() {
        for (JCheckBox checkBox : this.knowledgeBaseCheckboxes) {
            checkBox.setSelected(false);
        }
        for (JCheckBox checkBox : this.goalCheckboxes) {
            checkBox.setSelected(false);
        }
        this.selectedKnowledgeBaseFormulas.clear();
        this.selectedGoals.clear();
        this.updateSelectedFormulas();
    }

    private void clearRulesPanel() {
        this.rulesPanel.removeAll();
        JLabel hintLabel = new JLabel("Select KB formulas or goals to see applicable rules");
        hintLabel.setFont(LABEL_FONT);
        hintLabel.setForeground(TEXT_DIM);
        hintLabel.setBorder(BorderFactory.createEmptyBorder(20, 14, 20, 14));
        hintLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.rulesPanel.add(hintLabel);
        this.rulesPanel.revalidate();
        this.rulesPanel.repaint();
    }

    private void handleDone() {
        if (this.currentProof == null || !this.proofCreated) {
            this.setStatus("No active proof.", INTENT_DANGER);
            return;
        }
        try {
            boolean success = this.currentProof.executeCommand("done");
            if (success) {
                if (this.currentProof.getStateIndex() != 1) {
                    ManualPropositionalProof parent = this.currentProof.getParent();
                    if (parent != null) {
                        this.currentProof = parent;
                        this.refreshKnowledgeBaseCheckboxes();
                        this.updateStateDisplay();
                        this.clearSelections();
                    }
                    this.setStatus("State completed - returned to parent state.", TEXT_SECONDARY);
                }
                else {
                    this.updateStateDisplay();
                    this.setStatus("Proof complete!", INTENT_POSITIVE);
                    JOptionPane.showMessageDialog(this.mainFrame, "Proof completed successfully!", "Done", JOptionPane.INFORMATION_MESSAGE);
                    this.reset();
                }
            }
            else {
                this.setStatus("Cannot complete - some goals or sub-states are unresolved.", INTENT_DANGER);
            }
        }
        catch (Exception exception) {
            this.setStatus("Error: " + exception.getMessage(), INTENT_DANGER);
        }
    }

    private void handleChangeState() {
        if (this.currentProof == null || !this.proofCreated) {
            this.setStatus("No active proof.", INTENT_DANGER);
            return;
        }
        try {
            int index = Integer.parseInt(this.stateIndexField.getText().trim());
            if (index < 1) {
                this.setStatus("State index must be at least 1.", INTENT_DANGER);
                return;
            }

            ManualPropositionalProof newState = this.currentProof.getState(index);
            if (newState != null) {
                this.currentProof = newState;
                this.refreshCheckboxes();
                this.clearSelections();
                this.setStatus("Switched to state " + index, TEXT_SECONDARY);
            }
            else {
                this.setStatus("State " + index + " does not exist.", INTENT_DANGER);
            }
        }
        catch (NumberFormatException exception) {
            this.setStatus("Invalid state index - enter a number.", INTENT_DANGER);
        }
        catch (Exception exception) {
            this.setStatus("Error changing state: " + exception.getMessage(), INTENT_DANGER);
        }
    }

    private void openRuleEditor() {
        String[] options = {"Create New Package", "Edit Existing Package"};
        int choice = JOptionPane.showOptionDialog(
                this.mainFrame,
                "Do you want to create a new package or edit an existing one?",
                "Rule Editor",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (choice == 0) {
            String packageName = JOptionPane.showInputDialog(this.mainFrame, "Enter package name:");
            if (packageName != null && !packageName.trim().isEmpty()) {
                SwingUtilities.invokeLater(() -> new CustomRuleEditor(packageName.trim()));
            }
        }
        else if (choice == 1) {
            String customRulesPath = "files/customRules";
            File directory = new File(customRulesPath);

            if (!directory.exists() || !directory.isDirectory()) {
                JOptionPane.showMessageDialog(this.mainFrame, "Custom rules directory not found", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            File[] subDirectories = directory.listFiles(File::isDirectory);
            if (subDirectories == null || subDirectories.length == 0) {
                JOptionPane.showMessageDialog(this.mainFrame, "No packages found", "Information", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            List<String> packageNames = new ArrayList<>();
            for (File subDirectory : subDirectories) {
                if (!subDirectory.getName().equals("meta")) {
                    packageNames.add(subDirectory.getName());
                }
            }

            if (packageNames.isEmpty()) {
                JOptionPane.showMessageDialog(this.mainFrame, "No valid packages found", "Information", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            String selectedPackage = (String) JOptionPane.showInputDialog(
                    this.mainFrame,
                    "Select a package to edit:",
                    "Edit Package",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    packageNames.toArray(),
                    packageNames.get(0)
            );

            if (selectedPackage != null && !selectedPackage.isEmpty()) {
                SwingUtilities.invokeLater(() -> new CustomRuleEditor(selectedPackage));
            }
        }
    }

    private void refreshCheckboxes() {
        this.goalListData.clear();
        this.goalModel.clear();

        List<AST> goals = this.currentProof.getGoals();
        if (goals != null) {
            this.goalListData.addAll(goals);
            goals.forEach(this.goalModel::addElement);
        }

        this.selectedKnowledgeBaseFormulas.clear();
        this.selectedGoals.clear();

        this.refreshGoalCheckboxes();
        this.refreshKnowledgeBaseCheckboxes();
        this.updateStateDisplay();
    }

    private void updateStateDisplay() {
        if (this.currentProof == null || !this.proofCreated) {
            return;
        }
        try {
            this.stateDisplayArea.setText(this.currentProof.getStateText());
            this.stateDisplayArea.setCaretPosition(0);
        }
        catch (Exception exception) {
            this.stateDisplayArea.setText("Error retrieving state: " + exception.getMessage());
        }
    }

    private void setStatus(String message, Color color) {
        this.statusLabel.setText(message);
        this.statusLabel.setForeground(color);
    }

    private void registerAtoms(List<AST> asts) {
        for (AST ast : asts) {
            for (char character : ast.toString().toCharArray()) {
                if (Character.isUpperCase(character)) {
                    GlobalAtomID.addAtomId(String.valueOf(character));
                }
            }
        }
    }

    private JPanel buildStatusBar() {
        JPanel statusBarPanel = new JPanel(new BorderLayout());
        statusBarPanel.setBackground(BACKGROUND_DEEP);
        statusBarPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR),
                BorderFactory.createEmptyBorder(6, 18, 6, 18)));

        this.statusLabel = new JLabel("Ready, add knowledge base formulas and goals to begin.");
        this.statusLabel.setFont(SMALL_FONT);
        this.statusLabel.setForeground(TEXT_DIM);
        statusBarPanel.add(this.statusLabel, BorderLayout.WEST);

        JLabel hintLabel = new JLabel("Select KB formulas or goals to see applicable rules");
        hintLabel.setFont(SMALL_FONT);
        hintLabel.setForeground(TEXT_DIM);
        statusBarPanel.add(hintLabel, BorderLayout.EAST);

        return statusBarPanel;
    }

    private JPanel buildSectionHeader(String title, String icon) {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BACKGROUND_SURFACE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(LABEL_BOLD_FONT);
        titleLabel.setForeground(TEXT_PRIMARY);

        JSeparator separator = new JSeparator();
        separator.setForeground(BORDER_COLOR);
        separator.setBackground(BORDER_COLOR);

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(separator, BorderLayout.SOUTH);
        return headerPanel;
    }

    private JPanel wrapInSection(JComponent innerComponent, String title, String icon) {
        JPanel wrapperPanel = new JPanel(new BorderLayout(0, 8));
        wrapperPanel.setBackground(BACKGROUND_SURFACE);
        wrapperPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)));
        wrapperPanel.add(this.buildSectionHeader(title, icon), BorderLayout.NORTH);
        wrapperPanel.add(innerComponent, BorderLayout.CENTER);
        return wrapperPanel;
    }

    private JScrollPane createStyledScrollPane(JComponent view) {
        JScrollPane scrollPane = new JScrollPane(view);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        scrollPane.setBackground(BACKGROUND_DEEP);
        scrollPane.getViewport().setBackground(BACKGROUND_DEEP);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        scrollPane.getVerticalScrollBar().setBackground(BACKGROUND_DEEP);
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(6, 0));
        return scrollPane;
    }

    private JTextField createStyledTextField(String placeholder) {
        JTextField textField = new JTextField() {
            @Override
            protected void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                if (this.getText().isEmpty() && !placeholder.isEmpty() && !this.hasFocus()) {
                    Graphics2D graphics2D = (Graphics2D) graphics;
                    graphics2D.setColor(TEXT_DIM);
                    graphics2D.setFont(this.getFont().deriveFont(Font.ITALIC));
                    Insets insets = this.getInsets();
                    graphics2D.drawString(placeholder, insets.left + 2, this.getHeight() / 2 + graphics2D.getFontMetrics().getAscent() / 2 - 1);
                }
            }
        };
        textField.setBackground(BACKGROUND_RAISED);
        textField.setForeground(TEXT_PRIMARY);
        textField.setCaretColor(INTENT_PRIMARY);
        textField.setFont(MONOSPACED_FONT);
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        textField.setSelectionColor(new Color(0x3D3880));
        return textField;
    }

    private JButton createButton(String text, Color backgroundColor) {
        JButton button = new JButton(text) {
            private boolean isHovering = false;
            {
                this.addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent event) {
                        isHovering = true;
                        repaint();
                    }
                    public void mouseExited(MouseEvent event) {
                        isHovering = false;
                        repaint();
                    }
                });
            }
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D graphics2D = (Graphics2D) graphics.create();
                graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color fillColor = this.isHovering ? backgroundColor.brighter() : backgroundColor;
                if (!this.isEnabled()) {
                    fillColor = BACKGROUND_HOVER;
                }
                graphics2D.setColor(fillColor);
                graphics2D.fill(new RoundRectangle2D.Float(0, 0, this.getWidth(), this.getHeight(), 8, 8));
                graphics2D.dispose();
                super.paintComponent(graphics);
            }
        };
        button.setFont(BUTTON_FONT);
        button.setForeground(TEXT_PRIMARY);
        button.setBackground(backgroundColor);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(6, 16, 6, 16));
        return button;
    }

    /** Compact variant used for secondary actions tucked into panel headers. */
    private JButton createSmallButton(String text, Color backgroundColor) {
        JButton button = this.createButton(text, backgroundColor);
        button.setFont(SMALL_FONT.deriveFont(Font.BOLD, 11f));
        button.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        return button;
    }

    private JButton createSmallApplyButton() {
        JButton button = this.createButton("Apply", INTENT_PRIMARY);
        button.setFont(SMALL_FONT.deriveFont(Font.BOLD));
        button.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        button.setPreferredSize(new Dimension(64, 24));
        button.setMaximumSize(new Dimension(64, 24));
        return button;
    }

    private JPanel buildFormulaRow(int index, AST ast, boolean checkable, boolean isKnowledgeBase) {
        Color rowBackground = isKnowledgeBase ? BACKGROUND_RAISED : new Color(0x1E2A35);
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setBackground(rowBackground);
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
                BorderFactory.createEmptyBorder(5, 8, 5, 10)));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));

        JCheckBox checkBox = new JCheckBox();
        checkBox.setBackground(rowBackground);
        checkBox.setForeground(CHECKBOX_FOREGROUND);
        checkBox.setEnabled(checkable);

        JLabel numberLabel = new JLabel(String.valueOf(index));
        numberLabel.setFont(SMALL_FONT);
        numberLabel.setForeground(TEXT_DIM);
        numberLabel.setPreferredSize(new Dimension(22, -1));

        JLabel formulaLabel = new JLabel(ast.toString());
        formulaLabel.setFont(MONOSPACED_FONT);
        formulaLabel.setForeground(TEXT_PRIMARY);

        JPanel textPanel = new JPanel(new GridLayout(checkable && isKnowledgeBase ? 2 : 1, 1, 0, 1));
        textPanel.setBackground(rowBackground);
        textPanel.add(formulaLabel);

        if (checkable && isKnowledgeBase) {
            String source = KnowledgeBaseRegistry.getObtainedFrom(ast.toString());
            JLabel sourceLabel = new JLabel(source != null ? source : "-");
            sourceLabel.setFont(SMALL_FONT);
            sourceLabel.setForeground(INTENT_PRIMARY.brighter());
            textPanel.add(sourceLabel);
        }

        JPanel leftPanel = new JPanel(new BorderLayout(4, 0));
        leftPanel.setBackground(rowBackground);
        leftPanel.add(checkBox, BorderLayout.WEST);
        leftPanel.add(numberLabel, BorderLayout.CENTER);

        row.add(leftPanel, BorderLayout.WEST);
        row.add(textPanel, BorderLayout.CENTER);

        if (checkable) {
            if (isKnowledgeBase) {
                this.knowledgeBaseCheckboxes.add(checkBox);
            }
            else {
                this.goalCheckboxes.add(checkBox);
            }
            checkBox.addActionListener(event -> this.updateSelectedFormulas());
        }
        return row;
    }

    private JPanel buildRuleRow(String name, boolean isCustom, Runnable action) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setBackground(BACKGROUND_SURFACE);
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        Color dotColor = isCustom ? RULE_CUSTOM_DOT : RULE_BUILTIN_DOT;
        JLabel dot = new JLabel(isCustom ? "\u25C6" : "\u25CF");
        dot.setForeground(dotColor);
        dot.setFont(new Font("SansSerif", Font.PLAIN, 10));
        dot.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 6));

        JLabel label = new JLabel(name);
        label.setFont(LABEL_FONT);
        label.setForeground(isCustom ? RULE_CUSTOM_DOT : TEXT_PRIMARY);

        JPanel namePanel = new JPanel(new BorderLayout());
        namePanel.setBackground(BACKGROUND_SURFACE);
        namePanel.add(dot, BorderLayout.WEST);
        namePanel.add(label, BorderLayout.CENTER);

        JButton applyButton = this.createSmallApplyButton();
        applyButton.addActionListener(event -> action.run());

        row.add(namePanel, BorderLayout.CENTER);
        row.add(applyButton, BorderLayout.EAST);
        return row;
    }

    private static class CommandInfo {
        final String command;
        final int arity;
        final boolean fixedArity;

        CommandInfo(String command, int arity, boolean fixedArity) {
            this.command = command;
            this.arity = arity;
            this.fixedArity = fixedArity;
        }
    }
}