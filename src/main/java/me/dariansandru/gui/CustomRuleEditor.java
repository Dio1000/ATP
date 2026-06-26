package me.dariansandru.gui;

import me.dariansandru.domain.data_structures.ast.AST;
import me.dariansandru.domain.data_structures.ast.PropositionalAST;
import me.dariansandru.domain.language.UniverseOfDiscourse;
import me.dariansandru.domain.language.signature.Signature;
import me.dariansandru.domain.language.signature.SignatureFactory;
import me.dariansandru.domain.proof.automated_proof.PropositionalProof;
import me.dariansandru.domain.proof.inference_rules.custom.CustomPropositionalInferenceRule;
import me.dariansandru.utils.loader.PropositionalLogicLoader;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class CustomRuleEditor {

    private static final Color BACKGROUND_DEEP = new Color(0x0F1117);
    private static final Color BACKGROUND_SURFACE = new Color(0x1A1D27);
    private static final Color BACKGROUND_RAISED = new Color(0x22263A);
    private static final Color ACCENT_COLOR = new Color(0x6C63FF);
    private static final Color ACCENT_SOFT_COLOR = new Color(0x3D3880);
    private static final Color SUCCESS_COLOR = new Color(0x3DDC84);
    private static final Color WARNING_COLOR = new Color(0xFF8C00);
    private static final Color DANGER_COLOR = new Color(0xFF5C6A);
    private static final Color TEXT_PRIMARY_COLOR = new Color(0xECEFF4);
    private static final Color TEXT_SECONDARY_COLOR = new Color(0x8A8FA8);
    private static final Color TEXT_DIM_COLOR = new Color(0x565A72);
    private static final Color BORDER_COLOR = new Color(0x2E3350);

    private static final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 14);
    private static final Font LABEL_FONT = new Font("SansSerif", Font.PLAIN, 12);
    private static final Font LABEL_BOLD_FONT = new Font("SansSerif", Font.BOLD, 12);
    private static final Font MONOSPACED_FONT = new Font("Monospaced", Font.PLAIN, 13);
    private static final Font SMALL_FONT = new Font("SansSerif", Font.PLAIN, 10);
    private static final Font BUTTON_FONT = new Font("SansSerif", Font.BOLD, 12);

    private JFrame mainFrame;
    private JLabel statusLabel;

    private final DefaultListModel<RuleDefinition> ruleListModel = new DefaultListModel<>();
    private JList<RuleDefinition> ruleList;

    private JTextField ruleNameField;
    private JTextArea antecedentsTextArea;
    private JTextArea conclusionTextArea;

    private JLabel validationBadgeLabel;
    private JLabel validationDetailLabel;

    private JButton updateRuleButton;
    private JButton validateRuleButton;

    private final List<RuleDefinition> rulesList = new ArrayList<>();
    private RuleDefinition currentRule;
    private boolean isDirty = false;

    private String currentPackageName;
    private boolean isEditingPackage;
    private File currentPackageDirectory;

    private final PropositionalLogicLoader logicLoader = new PropositionalLogicLoader();

    private final ExecutorService validationExecutor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "rule-validator");
        thread.setDaemon(true);
        return thread;
    });

    private final ExecutorService proofExecutor = Executors.newCachedThreadPool(runnable -> {
        Thread thread = new Thread(runnable, "proof-worker");
        thread.setDaemon(true);
        return thread;
    });

    private Future<?> pendingValidationTask;
    private volatile Future<Boolean> pendingProofFuture;
    private long validationRunId = 0;

    public CustomRuleEditor() {
        this.currentPackageName = "new_package";
        this.isEditingPackage = false;
        this.initialize();
    }

    public CustomRuleEditor(String packageName) {
        this.currentPackageName = packageName;
        this.isEditingPackage = true;
        this.initialize();
        this.loadPackage(packageName);
    }

    private void initialize() {
        this.mainFrame = new JFrame("Custom Rule Editor" + (this.isEditingPackage ? "  —  " + this.currentPackageName : "  —  New Package"));
        this.mainFrame.setSize(1160, 780);
        this.mainFrame.setMinimumSize(new Dimension(900, 560));
        this.mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.mainFrame.getContentPane().setBackground(BACKGROUND_DEEP);
        this.mainFrame.setLayout(new BorderLayout());

        this.mainFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                if (CustomRuleEditor.this.confirmDiscard("close")) {
                    CustomRuleEditor.this.validationExecutor.shutdownNow();
                    CustomRuleEditor.this.proofExecutor.shutdownNow();
                    CustomRuleEditor.this.mainFrame.dispose();
                }
            }
        });

        this.mainFrame.add(this.buildTitleBar(), BorderLayout.NORTH);
        this.mainFrame.add(this.buildBody(), BorderLayout.CENTER);
        this.mainFrame.add(this.buildStatusBar(), BorderLayout.SOUTH);

        this.mainFrame.setVisible(true);
    }

    private JPanel buildTitleBar() {
        JPanel titleBarPanel = new JPanel(new BorderLayout());
        titleBarPanel.setBackground(BACKGROUND_DEEP);
        titleBarPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
                BorderFactory.createEmptyBorder(12, 18, 12, 18)));

        JLabel titleLabel = new JLabel("⚙  Custom Inference Rule Editor");
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(TEXT_PRIMARY_COLOR);

        titleBarPanel.add(titleLabel, BorderLayout.WEST);
        titleBarPanel.add(this.buildToolbar(), BorderLayout.EAST);
        return titleBarPanel;
    }

    private JPanel buildToolbar() {
        JPanel toolbarPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        toolbarPanel.setOpaque(false);

        JButton newPackageButton = this.createButton("New Package", new Color(0x444968));
        JButton savePackageButton = this.createButton("Save Package", SUCCESS_COLOR);
        JButton importButton = this.createButton("Import .atpf", new Color(0x00BCD4));
        JButton exportButton = this.createButton("Export Package", new Color(0x9C27B0));
        JButton newRuleButton = this.createButton("+ New Rule", ACCENT_COLOR);
        JButton deleteRuleButton = this.createButton("Delete Rule", DANGER_COLOR);

        newPackageButton.addActionListener(event -> this.createNewPackage());
        savePackageButton.addActionListener(event -> this.savePackage());
        importButton.addActionListener(event -> this.importRules());
        exportButton.addActionListener(event -> this.exportPackage());
        newRuleButton.addActionListener(event -> this.createNewRule());
        deleteRuleButton.addActionListener(event -> this.deleteSelectedRule());

        toolbarPanel.add(newPackageButton);
        toolbarPanel.add(savePackageButton);
        toolbarPanel.add(Box.createHorizontalStrut(10));
        toolbarPanel.add(importButton);
        toolbarPanel.add(exportButton);
        toolbarPanel.add(Box.createHorizontalStrut(10));
        toolbarPanel.add(newRuleButton);
        toolbarPanel.add(deleteRuleButton);
        return toolbarPanel;
    }

    private JSplitPane buildBody() {
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, this.buildRuleList(), this.buildEditor());
        splitPane.setDividerSize(6);
        splitPane.setResizeWeight(0.28);
        splitPane.setBackground(BACKGROUND_DEEP);
        splitPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return splitPane;
    }

    private JPanel buildRuleList() {
        JPanel ruleListPanel = new JPanel(new BorderLayout(0, 8));
        ruleListPanel.setBackground(BACKGROUND_SURFACE);
        ruleListPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));

        JLabel headerLabel = new JLabel("Rules in Package");
        headerLabel.setFont(LABEL_BOLD_FONT);
        headerLabel.setForeground(TEXT_PRIMARY_COLOR);

        this.ruleList = new JList<>(this.ruleListModel);
        this.ruleList.setBackground(BACKGROUND_DEEP);
        this.ruleList.setForeground(TEXT_PRIMARY_COLOR);
        this.ruleList.setSelectionBackground(ACCENT_SOFT_COLOR);
        this.ruleList.setSelectionForeground(TEXT_PRIMARY_COLOR);
        this.ruleList.setFixedCellHeight(44);
        this.ruleList.setCellRenderer(new RuleCellRenderer());

        this.ruleList.addListSelectionListener(event -> {
            if (event.getValueIsAdjusting()) {
                return;
            }
            RuleDefinition selectedRule = this.ruleList.getSelectedValue();
            if (selectedRule == null || selectedRule == this.currentRule) {
                return;
            }
            if (this.isDirty && !this.confirmDiscard("switch rules")) {
                this.ruleList.setSelectedValue(this.currentRule, false);
                return;
            }
            this.loadRuleIntoEditor(selectedRule);
        });

        JScrollPane scrollPane = this.createStyledScrollPane(this.ruleList);
        ruleListPanel.add(headerLabel, BorderLayout.NORTH);
        ruleListPanel.add(scrollPane, BorderLayout.CENTER);

        JLabel hintLabel = new JLabel("Select a rule to edit it");
        hintLabel.setFont(SMALL_FONT);
        hintLabel.setForeground(TEXT_DIM_COLOR);
        ruleListPanel.add(hintLabel, BorderLayout.SOUTH);
        return ruleListPanel;
    }

    private JPanel buildEditor() {
        JPanel editorPanel = new JPanel(new BorderLayout(0, 12));
        editorPanel.setBackground(BACKGROUND_SURFACE);
        editorPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createEmptyBorder(16, 16, 12, 16)));

        JPanel nameRowPanel = new JPanel(new BorderLayout(10, 0));
        nameRowPanel.setBackground(BACKGROUND_SURFACE);

        JLabel nameLabel = new JLabel("Rule name");
        nameLabel.setFont(LABEL_BOLD_FONT);
        nameLabel.setForeground(TEXT_SECONDARY_COLOR);
        nameLabel.setPreferredSize(new Dimension(90, -1));

        this.ruleNameField = this.createStyledTextField();
        this.ruleNameField.setFont(MONOSPACED_FONT);

        this.validationBadgeLabel = new JLabel("");
        this.validationBadgeLabel.setFont(LABEL_BOLD_FONT);
        this.validationDetailLabel = new JLabel("Not validated");
        this.validationDetailLabel.setFont(SMALL_FONT);
        this.validationDetailLabel.setForeground(TEXT_DIM_COLOR);

        JPanel validationRowPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        validationRowPanel.setOpaque(false);
        validationRowPanel.add(this.validationBadgeLabel);
        validationRowPanel.add(this.validationDetailLabel);

        nameRowPanel.add(nameLabel, BorderLayout.WEST);
        nameRowPanel.add(this.ruleNameField, BorderLayout.CENTER);
        nameRowPanel.add(validationRowPanel, BorderLayout.EAST);

        JPanel antecedentsPanel = new JPanel(new BorderLayout(0, 4));
        antecedentsPanel.setBackground(BACKGROUND_SURFACE);
        antecedentsPanel.add(this.createSectionLabel("Antecedents", "one formula per line  ·  e.g.  A -> B"), BorderLayout.NORTH);
        this.antecedentsTextArea = this.createStyledTextArea();
        antecedentsPanel.add(this.createStyledScrollPane(this.antecedentsTextArea), BorderLayout.CENTER);

        JPanel conclusionPanel = new JPanel(new BorderLayout(0, 4));
        conclusionPanel.setBackground(BACKGROUND_SURFACE);
        conclusionPanel.add(this.createSectionLabel("Conclusion", "a single formula"), BorderLayout.NORTH);
        this.conclusionTextArea = this.createStyledTextArea();
        this.conclusionTextArea.setRows(4);
        conclusionPanel.add(this.createStyledScrollPane(this.conclusionTextArea), BorderLayout.CENTER);

        JSplitPane contentSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, antecedentsPanel, conclusionPanel);
        contentSplitPane.setResizeWeight(0.65);
        contentSplitPane.setDividerSize(5);
        contentSplitPane.setBackground(BACKGROUND_SURFACE);

        this.validateRuleButton = this.createButton("Validate Rule", WARNING_COLOR);
        this.updateRuleButton = this.createButton("Save Rule", ACCENT_COLOR);
        this.validateRuleButton.addActionListener(event -> this.validateCurrentRule());
        this.updateRuleButton.addActionListener(event -> this.updateCurrentRule());

        JPanel buttonRowPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttonRowPanel.setOpaque(false);
        buttonRowPanel.add(this.validateRuleButton);
        buttonRowPanel.add(this.updateRuleButton);

        editorPanel.add(nameRowPanel, BorderLayout.NORTH);
        editorPanel.add(contentSplitPane, BorderLayout.CENTER);
        editorPanel.add(buttonRowPanel, BorderLayout.SOUTH);

        DocumentListener documentListener = new DocumentListener() {
            public void insertUpdate(DocumentEvent event) { CustomRuleEditor.this.markDirty(); }
            public void removeUpdate(DocumentEvent event) { CustomRuleEditor.this.markDirty(); }
            public void changedUpdate(DocumentEvent event) { CustomRuleEditor.this.markDirty(); }
        };
        this.ruleNameField.getDocument().addDocumentListener(documentListener);
        this.antecedentsTextArea.getDocument().addDocumentListener(documentListener);
        this.conclusionTextArea.getDocument().addDocumentListener(documentListener);

        this.showEmptyState();
        return editorPanel;
    }

    private JPanel buildStatusBar() {
        JPanel statusBarPanel = new JPanel(new BorderLayout());
        statusBarPanel.setBackground(BACKGROUND_DEEP);
        statusBarPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR),
                BorderFactory.createEmptyBorder(6, 18, 6, 18)));

        this.statusLabel = new JLabel("Ready: Create or open a package to begin.");
        this.statusLabel.setFont(SMALL_FONT);
        this.statusLabel.setForeground(TEXT_DIM_COLOR);
        statusBarPanel.add(this.statusLabel, BorderLayout.WEST);

        JLabel hintLabel = new JLabel("Validate before saving  ·  .atpf format");
        hintLabel.setFont(SMALL_FONT);
        hintLabel.setForeground(TEXT_DIM_COLOR);
        statusBarPanel.add(hintLabel, BorderLayout.EAST);
        return statusBarPanel;
    }

    private void createNewPackage() {
        if (this.isDirty && !this.confirmDiscard("create a new package")) {
            return;
        }
        if (!this.rulesList.isEmpty()) {
            int choice = JOptionPane.showConfirmDialog(this.mainFrame, "Discard the current package and start fresh?", "New Package", JOptionPane.YES_NO_OPTION);
            if (choice != JOptionPane.YES_OPTION) {
                return;
            }
        }
        String packageName = JOptionPane.showInputDialog(this.mainFrame, "Package name:", "new_package");
        if (packageName == null || packageName.isBlank()) {
            return;
        }

        this.rulesList.clear();
        this.ruleListModel.clear();
        this.currentRule = null;
        this.currentPackageName = packageName.trim().replace(' ', '_');
        this.isEditingPackage = false;
        this.currentPackageDirectory = null;
        this.mainFrame.setTitle("Custom Rule Editor: " + this.currentPackageName);
        this.showEmptyState();
        this.setStatus("New package: " + this.currentPackageName, TEXT_SECONDARY_COLOR);
    }

    private void createNewRule() {
        if (this.isDirty && !this.confirmDiscard("create a new rule")) {
            return;
        }
        RuleDefinition newRule = new RuleDefinition("NewRule", new ArrayList<>(), null);
        this.rulesList.add(newRule);
        this.ruleListModel.addElement(newRule);
        this.ruleList.setSelectedValue(newRule, true);
        this.loadRuleIntoEditor(newRule);
        this.ruleNameField.requestFocusInWindow();
        this.ruleNameField.selectAll();
        this.setStatus("New rule created: Fill in the details and save.", TEXT_SECONDARY_COLOR);
    }

    private void loadRuleIntoEditor(RuleDefinition rule) {
        this.currentRule = rule;
        this.isDirty = false;

        this.ruleNameField.setText(rule.name);

        StringBuilder stringBuilder = new StringBuilder();
        for (AST ast : rule.antecedents) {
            stringBuilder.append(ast.toString()).append('\n');
        }
        this.antecedentsTextArea.setText(stringBuilder.toString().stripTrailing());

        this.conclusionTextArea.setText(rule.conclusion != null ? rule.conclusion.toString() : "");

        this.refreshValidationBadge(rule);
        this.updateRuleButton.setEnabled(true);
        this.validateRuleButton.setEnabled(true);
    }

    private void showEmptyState() {
        this.currentRule = null;
        this.isDirty = false;
        this.ruleNameField.setText("");
        this.antecedentsTextArea.setText("");
        this.conclusionTextArea.setText("");
        this.validationBadgeLabel.setText("");
        this.validationDetailLabel.setText("No rule selected");
        this.validationDetailLabel.setForeground(TEXT_DIM_COLOR);
        this.updateRuleButton.setEnabled(false);
        this.validateRuleButton.setEnabled(false);
    }

    private void markDirty() {
        if (!this.isDirty) {
            this.isDirty = true;
            if (this.currentRule != null) {
                this.validationBadgeLabel.setForeground(WARNING_COLOR);
                this.validationDetailLabel.setText("Unsaved changes");
                this.validationDetailLabel.setForeground(WARNING_COLOR);
            }
        }
    }

    private void updateCurrentRule() {
        if (this.currentRule == null) {
            this.setStatus("No rule selected.", DANGER_COLOR);
            return;
        }

        String ruleName = this.ruleNameField.getText().trim();
        if (ruleName.isBlank()) {
            this.setStatus("Rule name cannot be empty.", DANGER_COLOR);
            return;
        }

        for (RuleDefinition rule : this.rulesList) {
            if (rule != this.currentRule && rule.name.equals(ruleName)) {
                this.setStatus("A rule named '" + ruleName + "' already exists.", DANGER_COLOR);
                return;
            }
        }

        List<AST> antecedents = new ArrayList<>();
        for (String line : this.antecedentsTextArea.getText().split("\n")) {
            String trimmedLine = line.trim();
            if (trimmedLine.isEmpty()) {
                continue;
            }
            PropositionalAST ast = new PropositionalAST(trimmedLine, true);
            if (!ast.isValid()) {
                this.setStatus("Invalid antecedent: " + trimmedLine, DANGER_COLOR);
                return;
            }
            antecedents.add(ast);
        }
        if (antecedents.isEmpty()) {
            this.setStatus("At least one antecedent is required.", DANGER_COLOR);
            return;
        }

        String conclusionText = this.conclusionTextArea.getText().trim();
        if (conclusionText.isBlank()) {
            this.setStatus("Conclusion cannot be empty.", DANGER_COLOR);
            return;
        }
        PropositionalAST conclusion = new PropositionalAST(conclusionText, true);
        if (!conclusion.isValid()) {
            this.setStatus("Invalid conclusion: " + conclusionText, DANGER_COLOR);
            return;
        }

        this.currentRule.name = ruleName;
        this.currentRule.antecedents = antecedents;
        this.currentRule.conclusion = conclusion;
        this.currentRule.isValidated = false;
        this.currentRule.validationError = null;
        this.isDirty = false;

        int index = this.ruleListModel.indexOf(this.currentRule);
        this.ruleListModel.set(index, this.currentRule);
        this.ruleList.setSelectedIndex(index);

        this.refreshValidationBadge(this.currentRule);
        this.setStatus("Rule saved: " + ruleName + ": validate it to confirm correctness.", SUCCESS_COLOR);
    }

    private void validateCurrentRule() {
        if (this.currentRule == null) {
            this.setStatus("No rule selected.", DANGER_COLOR);
            return;
        }
        if (this.currentRule.antecedents.isEmpty() || this.currentRule.conclusion == null) {
            this.setStatus("Save the rule first before validating.", DANGER_COLOR);
            return;
        }

        if (this.pendingValidationTask != null && !this.pendingValidationTask.isDone()) {
            this.pendingValidationTask.cancel(true);
        }
        if (this.pendingProofFuture != null && !this.pendingProofFuture.isDone()) {
            this.pendingProofFuture.cancel(true);
        }

        this.currentRule.isValidating = true;
        this.currentRule.isValidated = false;
        this.currentRule.validationError = null;
        this.refreshValidationBadge(this.currentRule);
        this.setStatus("Validating '" + this.currentRule.name + "'...", WARNING_COLOR);

        final RuleDefinition targetRule = this.currentRule;
        final long currentRunId = this.validationRunId++;

        this.pendingValidationTask = this.validationExecutor.submit(() -> {
            boolean isValid = false;
            String errorMessage = null;
            try {
                isValid = this.runProofWithTimeout(targetRule);
            }
            catch (Exception exception) {
                errorMessage = exception.getMessage();
            }

            final boolean finalIsValid = isValid;
            final String finalErrorMessage = errorMessage;

            SwingUtilities.invokeLater(() -> {
                if (currentRunId != CustomRuleEditor.this.validationRunId) {
                    return;
                }
                targetRule.isValidating = false;
                targetRule.isValidated = finalIsValid;
                targetRule.validationError = finalErrorMessage;

                int index = CustomRuleEditor.this.ruleListModel.indexOf(targetRule);
                if (index >= 0) {
                    CustomRuleEditor.this.ruleListModel.set(index, targetRule);
                }
                if (targetRule == CustomRuleEditor.this.currentRule) {
                    CustomRuleEditor.this.refreshValidationBadge(targetRule);
                }
                if (finalIsValid) {
                    CustomRuleEditor.this.setStatus("\u2713 '" + targetRule.name + "' is valid.", SUCCESS_COLOR);
                }
                else {
                    CustomRuleEditor.this.setStatus("\u2717 '" + targetRule.name + "' could not be proven: " + (finalErrorMessage != null ? finalErrorMessage : "proof failed"), DANGER_COLOR);
                }
            });
        });
    }

    private boolean runProofWithTimeout(RuleDefinition rule) throws Exception {
        Signature signature = SignatureFactory.createSignature(UniverseOfDiscourse.PROPOSITIONS);

        PropositionalProof proof = new PropositionalProof(
                signature,
                new ArrayList<>(rule.antecedents),
                List.of(rule.conclusion));

        Future<Boolean> future = this.proofExecutor.submit(proof::prove);
        this.pendingProofFuture = future;

        try {
            Boolean result = future.get(1, TimeUnit.SECONDS);
            return result != null && result;
        }
        catch (TimeoutException exception) {
            future.cancel(true);
            throw new Exception("Timed out after 1s: Rule may be too complex");
        }
        catch (ExecutionException exception) {
            throw new Exception("Proof execution error: " + exception.getCause().getMessage());
        }
        catch (InterruptedException exception) {
            future.cancel(true);
            Thread.currentThread().interrupt();
            throw new Exception("Proof execution interrupted");
        }
    }

    private void deleteSelectedRule() {
        RuleDefinition selectedRule = this.ruleList.getSelectedValue();
        if (selectedRule == null) {
            this.setStatus("No rule selected.", DANGER_COLOR);
            return;
        }
        int choice = JOptionPane.showConfirmDialog(
                this.mainFrame,
                "Delete rule '" + selectedRule.name + "'? This cannot be undone.",
                "Delete Rule",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }
        this.rulesList.remove(selectedRule);
        this.ruleListModel.removeElement(selectedRule);
        this.showEmptyState();
        this.setStatus("Deleted rule: " + selectedRule.name, DANGER_COLOR);
    }

    private void savePackage() {
        if (this.rulesList.isEmpty()) {
            this.setStatus("No rules to save.", DANGER_COLOR);
            return;
        }

        boolean hasUnvalidated = this.rulesList.stream().anyMatch(rule -> !rule.isValidated);
        if (hasUnvalidated) {
            int choice = JOptionPane.showConfirmDialog(
                    this.mainFrame,
                    "Some rules have not been validated. Save anyway?",
                    "Unvalidated Rules",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (choice != JOptionPane.YES_OPTION) {
                return;
            }
        }

        File packageDirectory = this.resolvePackageDirectory(this.currentPackageName);
        if (!packageDirectory.exists()) {
            packageDirectory.mkdirs();
        }

        File ruleFile = new File(packageDirectory, this.currentPackageName + ".atpf");
        try (PrintWriter writer = new PrintWriter(new FileWriter(ruleFile))) {
            this.writeRulesToWriter(writer);
        }
        catch (IOException exception) {
            this.setStatus("Save failed: " + exception.getMessage(), DANGER_COLOR);
            return;
        }

        this.writeMetaFile(packageDirectory, null);
        this.currentPackageDirectory = packageDirectory;
        this.isDirty = false;
        this.setStatus("Saved " + this.rulesList.size() + " rule(s) to '" + this.currentPackageName + "'", SUCCESS_COLOR);
    }

    private void importRules() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Import .atpf Rule File");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Custom Rule Files (*.atpf)", "atpf"));
        if (fileChooser.showOpenDialog(this.mainFrame) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File sourceFile = fileChooser.getSelectedFile();
        int importedCount = this.loadRulesFromFile(sourceFile);
        this.setStatus("Imported " + importedCount + " rule(s) from " + sourceFile.getName(), SUCCESS_COLOR);
    }

    private void exportPackage() {
        if (this.rulesList.isEmpty()) {
            this.setStatus("No rules to export.", DANGER_COLOR);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Choose Export Directory");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (fileChooser.showSaveDialog(this.mainFrame) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File exportRoot = new File(fileChooser.getSelectedFile(), this.currentPackageName);
        if (!exportRoot.exists()) {
            exportRoot.mkdirs();
        }

        File ruleFile = new File(exportRoot, this.currentPackageName + ".atpf");
        try (PrintWriter writer = new PrintWriter(new FileWriter(ruleFile))) {
            this.writeRulesToWriter(writer);
        }
        catch (IOException exception) {
            this.setStatus("Export failed: " + exception.getMessage(), DANGER_COLOR);
            return;
        }

        this.writeMetaFile(exportRoot, this.currentPackageDirectory);
        this.setStatus("Exported package to " + exportRoot.getAbsolutePath(), SUCCESS_COLOR);
    }

    private void loadPackage(String packageName) {
        File packageDirectory = this.resolvePackageDirectory(packageName);
        this.currentPackageDirectory = packageDirectory;

        if (!packageDirectory.isDirectory()) {
            this.setStatus("Package not found: " + packageName, DANGER_COLOR);
            return;
        }

        File ruleFile = new File(packageDirectory, packageName + ".atpf");
        if (!ruleFile.exists()) {
            this.setStatus("Rule file not found: " + packageName + ".atpf", DANGER_COLOR);
            return;
        }

        this.rulesList.clear();
        this.ruleListModel.clear();
        int loadedCount = this.loadRulesFromFile(ruleFile);
        this.rulesList.forEach(rule -> rule.isValidated = true);
        this.ruleListModel.clear();
        this.rulesList.forEach(this.ruleListModel::addElement);

        if (loadedCount > 0) {
            this.ruleList.setSelectedIndex(0);
        }
        this.mainFrame.setTitle("Custom Rule Editor:  " + packageName);
        this.setStatus("Loaded " + loadedCount + " rule(s) from '" + packageName + "'", SUCCESS_COLOR);
    }

    private int loadRulesFromFile(File file) {
        List<String> lines;
        try {
            lines = this.logicLoader.getLines(file.getPath());
        }
        catch (Exception exception) {
            this.setStatus("Cannot read file: " + exception.getMessage(), DANGER_COLOR);
            return 0;
        }
        if (lines == null || lines.isEmpty()) {
            return 0;
        }

        int importedCount = 0;
        int index = 0;
        while (index < lines.size()) {
            String line = lines.get(index).trim();
            if (line.startsWith("rule=")) {
                String ruleName = line.substring(5).trim();
                List<String> ruleBlock = new ArrayList<>();
                ruleBlock.add(line);
                index++;
                while (index < lines.size()) {
                    String currentLine = lines.get(index).trim();
                    ruleBlock.add(currentLine);
                    index++;
                    if (currentLine.equals("end")) {
                        break;
                    }
                }
                try {
                    CustomPropositionalInferenceRule customRule = this.logicLoader.loadCustomRule(ruleBlock, ruleName);
                    if (customRule != null && customRule.conclusion() != null) {
                        RuleDefinition ruleDefinition = new RuleDefinition(
                                customRule.name(),
                                customRule.antecedents(),
                                customRule.conclusion());
                        this.rulesList.add(ruleDefinition);
                        this.ruleListModel.addElement(ruleDefinition);
                        importedCount++;
                    }
                }
                catch (Exception exception) {
                    System.err.println("Skipping malformed rule '" + ruleName + "': " + exception.getMessage());
                }
            }
            else {
                index++;
            }
        }
        return importedCount;
    }

    private void writeRulesToWriter(PrintWriter writer) {
        for (RuleDefinition rule : this.rulesList) {
            writer.println("rule=" + rule.name);
            for (AST antecedent : rule.antecedents) {
                writer.println("a: " + antecedent.toString());
            }
            writer.println("c: " + rule.conclusion.toString());
            writer.println("end");
            writer.println();
        }
    }

    private void writeMetaFile(File targetDirectory, File sourceDirectory) {
        File metaFile = new File(targetDirectory, "meta");
        try (PrintWriter writer = new PrintWriter(new FileWriter(metaFile))) {
            writer.println("Checked:");
            writer.println("Sound:");
            writer.println("Consistent:");
            writer.println("Refutation Complete:");
            writer.println("Correct:");
        }
        catch (IOException exception) {
            System.err.println("Warning: could not write meta file: " + exception.getMessage());
        }
        if (sourceDirectory != null) {
            File sourceMetaFile = new File(sourceDirectory, "meta");
            if (sourceMetaFile.exists() && !sourceMetaFile.getAbsolutePath().equals(metaFile.getAbsolutePath())) {
                try {
                    Files.copy(sourceMetaFile.toPath(), metaFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
                catch (IOException exception) {
                    System.err.println("Warning: could not copy meta file: " + exception.getMessage());
                }
            }
        }
    }

    private File resolvePackageDirectory(String packageName) {
        return new File("files/customRules/" + packageName);
    }

    private boolean confirmDiscard(String action) {
        if (!this.isDirty) {
            return true;
        }
        int choice = JOptionPane.showConfirmDialog(
                this.mainFrame,
                "You have unsaved changes. Discard them and " + action + "?",
                "Unsaved Changes",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        return choice == JOptionPane.YES_OPTION;
    }

    private void refreshValidationBadge(RuleDefinition rule) {
        if (rule.isValidating) {
            this.validationBadgeLabel.setForeground(WARNING_COLOR);
            this.validationDetailLabel.setText("Validating…");
            this.validationDetailLabel.setForeground(WARNING_COLOR);
        }
        else if (rule.isValidated) {
            this.validationBadgeLabel.setForeground(SUCCESS_COLOR);
            this.validationDetailLabel.setText("Valid");
            this.validationDetailLabel.setForeground(SUCCESS_COLOR);
        }
        else if (rule.validationError != null) {
            this.validationBadgeLabel.setForeground(DANGER_COLOR);
            this.validationDetailLabel.setText(rule.validationError);
            this.validationDetailLabel.setForeground(DANGER_COLOR);
        }
        else {
            this.validationBadgeLabel.setForeground(TEXT_DIM_COLOR);
            this.validationDetailLabel.setText("Not validated");
            this.validationDetailLabel.setForeground(TEXT_DIM_COLOR);
        }
    }

    private void setStatus(String message, Color color) {
        this.statusLabel.setText(message);
        this.statusLabel.setForeground(color);
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
                graphics2D.setColor(isHovering ? backgroundColor.brighter() : backgroundColor);
                graphics2D.fill(new RoundRectangle2D.Float(0, 0, this.getWidth(), this.getHeight(), 8, 8));
                graphics2D.dispose();
                super.paintComponent(graphics);
            }
        };
        button.setFont(BUTTON_FONT);
        button.setForeground(TEXT_PRIMARY_COLOR);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        return button;
    }

    private JTextField createStyledTextField() {
        JTextField textField = new JTextField();
        textField.setBackground(BACKGROUND_RAISED);
        textField.setForeground(TEXT_PRIMARY_COLOR);
        textField.setCaretColor(ACCENT_COLOR);
        textField.setFont(LABEL_FONT);
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        textField.setSelectionColor(ACCENT_SOFT_COLOR);
        return textField;
    }

    private JTextArea createStyledTextArea() {
        JTextArea textArea = new JTextArea();
        textArea.setBackground(BACKGROUND_DEEP);
        textArea.setForeground(TEXT_PRIMARY_COLOR);
        textArea.setCaretColor(ACCENT_COLOR);
        textArea.setFont(MONOSPACED_FONT);
        textArea.setLineWrap(false);
        textArea.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        textArea.setSelectionColor(ACCENT_SOFT_COLOR);
        return textArea;
    }

    private JScrollPane createStyledScrollPane(JComponent component) {
        JScrollPane scrollPane = new JScrollPane(component);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        scrollPane.setBackground(BACKGROUND_DEEP);
        scrollPane.getViewport().setBackground(BACKGROUND_DEEP);
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(6, 0));
        return scrollPane;
    }

    private JPanel createSectionLabel(String title, String hint) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(BACKGROUND_SURFACE);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(LABEL_BOLD_FONT);
        titleLabel.setForeground(TEXT_PRIMARY_COLOR);
        JLabel hintLabel = new JLabel(hint);
        hintLabel.setFont(SMALL_FONT);
        hintLabel.setForeground(TEXT_DIM_COLOR);
        panel.add(titleLabel, BorderLayout.WEST);
        panel.add(hintLabel, BorderLayout.EAST);
        return panel;
    }

    private static class RuleCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(
                JList<?> list,
                Object value,
                int index,
                boolean isSelected,
                boolean hasFocus) {

            RuleDefinition rule = (RuleDefinition) value;

            JPanel rowPanel = new JPanel(new BorderLayout(8, 0));
            rowPanel.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
            rowPanel.setBackground(isSelected ? ACCENT_SOFT_COLOR : (index % 2 == 0 ? BACKGROUND_RAISED : BACKGROUND_SURFACE));

            JLabel nameLabel = new JLabel(rule.name);
            nameLabel.setFont(LABEL_BOLD_FONT);
            nameLabel.setForeground(TEXT_PRIMARY_COLOR);

            JLabel badgeLabel = this.createBadgeLabel(rule);

            rowPanel.add(nameLabel, BorderLayout.CENTER);
            rowPanel.add(badgeLabel, BorderLayout.EAST);
            return rowPanel;
        }

        private JLabel createBadgeLabel(RuleDefinition rule) {
            JLabel badgeLabel = new JLabel();
            badgeLabel.setFont(SMALL_FONT);
            if (rule.isValidating) {
                badgeLabel.setText("⏳");
                badgeLabel.setForeground(WARNING_COLOR);
            }
            else if (rule.isValidated) {
                badgeLabel.setText("✓ valid");
                badgeLabel.setForeground(SUCCESS_COLOR);
            }
            else if (rule.validationError != null) {
                badgeLabel.setText("✗ failed");
                badgeLabel.setForeground(DANGER_COLOR);
            }
            else {
                badgeLabel.setText("○ unvalidated");
                badgeLabel.setForeground(TEXT_DIM_COLOR);
            }
            return badgeLabel;
        }
    }

    private static class RuleDefinition {
        String name;
        List<AST> antecedents;
        AST conclusion;
        boolean isValidated = false;
        boolean isValidating = false;
        String validationError = null;

        RuleDefinition(String name, List<AST> antecedents, AST conclusion) {
            this.name = name;
            this.antecedents = antecedents != null ? new ArrayList<>(antecedents) : new ArrayList<>();
            this.conclusion = conclusion;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }

    public static void main(String[] arguments) {
        SwingUtilities.invokeLater(CustomRuleEditor::new);
    }
}