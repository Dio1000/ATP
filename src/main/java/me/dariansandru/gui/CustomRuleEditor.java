package me.dariansandru.gui;

import me.dariansandru.domain.data_structures.ast.AST;
import me.dariansandru.domain.data_structures.ast.PropositionalAST;
import me.dariansandru.domain.language.UniverseOfDiscourse;
import me.dariansandru.domain.language.signature.Signature;
import me.dariansandru.domain.language.signature.SignatureFactory;
import me.dariansandru.domain.proof.automated_proof.PropositionalProof;
import me.dariansandru.domain.proof.inference_rules.custom.CustomPropositionalInferenceRule;
import me.dariansandru.utils.helper.PropositionalLogicHelper;
import me.dariansandru.utils.loader.PropositionalLogicLoader;

import javax.swing.*;
import javax.swing.border.*;
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
import java.util.concurrent.atomic.AtomicBoolean;

public class CustomRuleEditor {

    // ── Palette (shared with PropositionalProofGUIController) ─────────────────
    private static final Color BG_DEEP     = new Color(0x0F1117);
    private static final Color BG_SURFACE  = new Color(0x1A1D27);
    private static final Color BG_RAISED   = new Color(0x22263A);
    private static final Color ACCENT      = new Color(0x6C63FF);
    private static final Color ACCENT_SOFT = new Color(0x3D3880);
    private static final Color SUCCESS     = new Color(0x3DDC84);
    private static final Color WARN        = new Color(0xFF8C00);
    private static final Color DANGER      = new Color(0xFF5C6A);
    private static final Color TEXT_PRI    = new Color(0xECEFF4);
    private static final Color TEXT_SEC    = new Color(0x8A8FA8);
    private static final Color TEXT_DIM    = new Color(0x565A72);
    private static final Color BORDER      = new Color(0x2E3350);

    // ── Fonts ─────────────────────────────────────────────────────────────────
    private static final Font FONT_TITLE   = new Font("SansSerif", Font.BOLD,  14);
    private static final Font FONT_LABEL   = new Font("SansSerif", Font.PLAIN, 12);
    private static final Font FONT_LABEL_B = new Font("SansSerif", Font.BOLD,  12);
    private static final Font FONT_MONO    = new Font("Monospaced", Font.PLAIN, 13);
    private static final Font FONT_SMALL   = new Font("SansSerif", Font.PLAIN, 10);
    private static final Font FONT_BTN     = new Font("SansSerif", Font.BOLD,  12);

    // ── State ─────────────────────────────────────────────────────────────────
    private JFrame frame;
    private JLabel statusLabel;                 // BUG FIX #1: stored field, not getComponent()

    private final DefaultListModel<RuleDefinition> ruleModel = new DefaultListModel<>();
    private JList<RuleDefinition> ruleList;

    private JTextField ruleNameField;
    private JTextArea  antecedentsArea;
    private JTextArea  conclusionArea;

    private JLabel validationBadge;
    private JLabel validationDetail;

    private JButton updateRuleBtn;
    private JButton validateRuleBtn;

    private final List<RuleDefinition> rules = new ArrayList<>();
    private RuleDefinition currentRule;
    private boolean dirty = false;             // BUG FIX #6: dirty tracking

    private String  currentPackageName;
    private boolean isEditingPackage;
    private File    currentPackageDirectory;

    private final PropositionalLogicLoader loader = new PropositionalLogicLoader();

    // BUG FIX #2: one shared executor — never leak a new one per validation call
    private final ExecutorService validatorExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "rule-validator");
        t.setDaemon(true);
        return t;
    });
    private Future<?> pendingValidation;

    // ═════════════════════════════════════════════════════════════════════════
    public CustomRuleEditor() {
        this.currentPackageName = "new_package";
        this.isEditingPackage   = false;
        initialize();
    }

    public CustomRuleEditor(String packageName) {
        this.currentPackageName = packageName;
        this.isEditingPackage   = true;
        initialize();
        loadPackage(packageName);
    }

    // =========================================================================
    // Frame construction
    // =========================================================================
    private void initialize() {
        frame = new JFrame("Custom Rule Editor"
                + (isEditingPackage ? "  —  " + currentPackageName : "  —  New Package"));
        frame.setSize(1160, 780);
        frame.setMinimumSize(new Dimension(900, 560));
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.getContentPane().setBackground(BG_DEEP);
        frame.setLayout(new BorderLayout());

        frame.addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                if (confirmDiscard("close")) {
                    validatorExecutor.shutdownNow();
                    frame.dispose();
                }
            }
        });

        frame.add(buildTitleBar(),  BorderLayout.NORTH);
        frame.add(buildBody(),      BorderLayout.CENTER);
        frame.add(buildStatusBar(), BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    // ── Title bar ─────────────────────────────────────────────────────────────
    private JPanel buildTitleBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(BG_DEEP);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
                BorderFactory.createEmptyBorder(12, 18, 12, 18)));

        JLabel title = new JLabel("⚙  Custom Inference Rule Editor");
        title.setFont(FONT_TITLE);
        title.setForeground(TEXT_PRI);

        bar.add(title,         BorderLayout.WEST);
        bar.add(buildToolbar(), BorderLayout.EAST);
        return bar;
    }

    private JPanel buildToolbar() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        p.setOpaque(false);

        JButton newPkgBtn  = mkBtn("New Package",  new Color(0x444968));
        JButton saveBtn    = mkBtn("Save Package",  SUCCESS);
        JButton importBtn  = mkBtn("Import .atpf",  new Color(0x00BCD4));
        JButton exportBtn  = mkBtn("Export Package", new Color(0x9C27B0));
        JButton newRuleBtn = mkBtn("+ New Rule",    ACCENT);
        JButton deleteBtn  = mkBtn("Delete Rule",   DANGER);

        newPkgBtn .addActionListener(e -> newPackage());
        saveBtn   .addActionListener(e -> savePackage());
        importBtn .addActionListener(e -> importRules());
        exportBtn .addActionListener(e -> exportPackage());
        newRuleBtn.addActionListener(e -> createNewRule());
        deleteBtn .addActionListener(e -> deleteSelectedRule());

        p.add(newPkgBtn); p.add(saveBtn);
        p.add(Box.createHorizontalStrut(10));
        p.add(importBtn); p.add(exportBtn);
        p.add(Box.createHorizontalStrut(10));
        p.add(newRuleBtn); p.add(deleteBtn);
        return p;
    }

    // ── Body: rule list + editor ──────────────────────────────────────────────
    private JSplitPane buildBody() {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                buildRuleList(), buildEditor());
        split.setDividerSize(6);
        split.setResizeWeight(0.28);
        split.setBackground(BG_DEEP);
        split.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return split;
    }

    // ── Rule list (left) ──────────────────────────────────────────────────────
    private JPanel buildRuleList() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(BG_SURFACE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));

        JLabel hdr = new JLabel("Rules in Package");
        hdr.setFont(FONT_LABEL_B);
        hdr.setForeground(TEXT_PRI);

        ruleList = new JList<>(ruleModel);
        ruleList.setBackground(BG_DEEP);
        ruleList.setForeground(TEXT_PRI);
        ruleList.setSelectionBackground(ACCENT_SOFT);
        ruleList.setSelectionForeground(TEXT_PRI);
        ruleList.setFixedCellHeight(44);
        ruleList.setCellRenderer(new RuleCellRenderer());  // BUG FIX #4

        ruleList.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            RuleDefinition sel = ruleList.getSelectedValue();
            if (sel == null || sel == currentRule) return;
            // BUG FIX #6: warn about unsaved edits before switching
            if (dirty && !confirmDiscard("switch rules")) {
                ruleList.setSelectedValue(currentRule, false);
                return;
            }
            loadRuleIntoEditor(sel);
        });

        JScrollPane scroll = styledScroll(ruleList);
        panel.add(hdr,    BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);

        JLabel hint = new JLabel("Select a rule to edit it");
        hint.setFont(FONT_SMALL);
        hint.setForeground(TEXT_DIM);
        panel.add(hint, BorderLayout.SOUTH);
        return panel;
    }

    // ── Editor (right) ────────────────────────────────────────────────────────
    private JPanel buildEditor() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(BG_SURFACE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(16, 16, 12, 16)));

        // ── Name row ──
        JPanel nameRow = new JPanel(new BorderLayout(10, 0));
        nameRow.setBackground(BG_SURFACE);

        JLabel nameLabel = new JLabel("Rule name");
        nameLabel.setFont(FONT_LABEL_B);
        nameLabel.setForeground(TEXT_SEC);
        nameLabel.setPreferredSize(new Dimension(90, -1));

        ruleNameField = styledField();
        ruleNameField.setFont(FONT_MONO);

        validationBadge  = new JLabel("");
        validationBadge.setFont(FONT_LABEL_B);
        validationDetail = new JLabel("Not validated");
        validationDetail.setFont(FONT_SMALL);
        validationDetail.setForeground(TEXT_DIM);

        JPanel validRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        validRow.setOpaque(false);
        validRow.add(validationBadge);
        validRow.add(validationDetail);

        nameRow.add(nameLabel,   BorderLayout.WEST);
        nameRow.add(ruleNameField, BorderLayout.CENTER);
        nameRow.add(validRow,    BorderLayout.EAST);

        // ── Antecedents ──
        JPanel antPanel = new JPanel(new BorderLayout(0, 4));
        antPanel.setBackground(BG_SURFACE);
        antPanel.add(sectionLabel("Antecedents", "one formula per line  ·  e.g.  A -> B"), BorderLayout.NORTH);
        antecedentsArea = styledArea();
        antPanel.add(styledScroll(antecedentsArea), BorderLayout.CENTER);

        // ── Conclusion ──
        JPanel concPanel = new JPanel(new BorderLayout(0, 4));
        concPanel.setBackground(BG_SURFACE);
        concPanel.add(sectionLabel("Conclusion", "a single formula"), BorderLayout.NORTH);
        conclusionArea = styledArea();
        conclusionArea.setRows(4);
        concPanel.add(styledScroll(conclusionArea), BorderLayout.CENTER);

        // ── Content split ──
        JSplitPane content = new JSplitPane(JSplitPane.VERTICAL_SPLIT, antPanel, concPanel);
        content.setResizeWeight(0.65);
        content.setDividerSize(5);
        content.setBackground(BG_SURFACE);

        // ── Bottom button row ──
        validateRuleBtn = mkBtn("Validate Rule", WARN);
        updateRuleBtn   = mkBtn("Save Rule",     ACCENT);
        validateRuleBtn.addActionListener(e -> validateCurrentRule());
        updateRuleBtn  .addActionListener(e -> updateCurrentRule());

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setOpaque(false);
        btnRow.add(validateRuleBtn);
        btnRow.add(updateRuleBtn);

        panel.add(nameRow,  BorderLayout.NORTH);
        panel.add(content,  BorderLayout.CENTER);
        panel.add(btnRow,   BorderLayout.SOUTH);

        // Wire dirty tracking
        DocumentListener dl = new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { markDirty(); }
            public void removeUpdate(DocumentEvent e)  { markDirty(); }
            public void changedUpdate(DocumentEvent e) { markDirty(); }
        };
        ruleNameField   .getDocument().addDocumentListener(dl);
        antecedentsArea .getDocument().addDocumentListener(dl);
        conclusionArea  .getDocument().addDocumentListener(dl);

        showEmptyState();
        return panel;
    }

    // ── Status bar ────────────────────────────────────────────────────────────
    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(BG_DEEP);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER),
                BorderFactory.createEmptyBorder(6, 18, 6, 18)));

        statusLabel = new JLabel("Ready  —  create or open a package to begin.");
        statusLabel.setFont(FONT_SMALL);
        statusLabel.setForeground(TEXT_DIM);
        bar.add(statusLabel, BorderLayout.WEST);

        JLabel hint = new JLabel("Validate before saving  ·  .atpf format");
        hint.setFont(FONT_SMALL);
        hint.setForeground(TEXT_DIM);
        bar.add(hint, BorderLayout.EAST);
        return bar;
    }

    // =========================================================================
    // Business logic
    // =========================================================================

    private void newPackage() {
        if (dirty && !confirmDiscard("create a new package")) return;
        if (!rules.isEmpty()) {
            int c = JOptionPane.showConfirmDialog(frame,
                    "Discard the current package and start fresh?",
                    "New Package", JOptionPane.YES_NO_OPTION);
            if (c != JOptionPane.YES_OPTION) return;
        }
        String name = JOptionPane.showInputDialog(frame,
                "Package name:", "new_package");
        if (name == null || name.isBlank()) return;

        rules.clear();
        ruleModel.clear();
        currentRule = null;
        currentPackageName = name.trim().replace(' ', '_');
        isEditingPackage   = false;
        currentPackageDirectory = null;
        frame.setTitle("Custom Rule Editor  —  " + currentPackageName);
        showEmptyState();
        setStatus("New package: " + currentPackageName, TEXT_SEC);
    }

    private void createNewRule() {
        if (dirty && !confirmDiscard("create a new rule")) return;
        RuleDefinition r = new RuleDefinition("new_rule", new ArrayList<>(), null);
        rules.add(r);
        ruleModel.addElement(r);
        ruleList.setSelectedValue(r, true);
        loadRuleIntoEditor(r);
        ruleNameField.requestFocusInWindow();
        ruleNameField.selectAll();
        setStatus("New rule created — fill in the details and save.", TEXT_SEC);
    }

    private void loadRuleIntoEditor(RuleDefinition rule) {
        currentRule = rule;
        dirty = false;

        ruleNameField.setText(rule.name);

        StringBuilder sb = new StringBuilder();
        for (AST a : rule.antecedents) sb.append(a.toString()).append('\n');
        antecedentsArea.setText(sb.toString().stripTrailing());

        conclusionArea.setText(rule.conclusion != null ? rule.conclusion.toString() : "");

        refreshValidationBadge(rule);
        updateRuleBtn.setEnabled(true);
        validateRuleBtn.setEnabled(true);
    }

    private void showEmptyState() {
        currentRule = null;
        dirty = false;
        ruleNameField.setText("");
        antecedentsArea.setText("");
        conclusionArea.setText("");
        validationBadge .setText("");
        validationDetail.setText("No rule selected");
        validationDetail.setForeground(TEXT_DIM);
        updateRuleBtn.setEnabled(false);
        validateRuleBtn.setEnabled(false);
    }

    private void markDirty() {
        if (!dirty) {
            dirty = true;
            if (currentRule != null) {
                validationBadge .setText("●");
                validationBadge .setForeground(WARN);
                validationDetail.setText("Unsaved changes");
                validationDetail.setForeground(WARN);
            }
        }
    }

    private void updateCurrentRule() {
        if (currentRule == null) { setStatus("No rule selected.", DANGER); return; }

        String name = ruleNameField.getText().trim();
        if (name.isBlank()) { setStatus("Rule name cannot be empty.", DANGER); return; }

        // BUG FIX #5: identity-safe duplicate check (works after list model updates)
        for (RuleDefinition r : rules) {
            if (r != currentRule && r.name.equals(name)) {
                setStatus("A rule named '" + name + "' already exists.", DANGER); return;
            }
        }

        List<AST> antecedents = new ArrayList<>();
        for (String line : antecedentsArea.getText().split("\n")) {
            String t = line.trim();
            if (t.isEmpty()) continue;
            PropositionalAST ast = new PropositionalAST(t, true);
            if (!ast.isValid()) { setStatus("Invalid antecedent: " + t, DANGER); return; }
            antecedents.add(ast);
        }
        if (antecedents.isEmpty()) { setStatus("At least one antecedent is required.", DANGER); return; }

        String concText = conclusionArea.getText().trim();
        if (concText.isBlank()) { setStatus("Conclusion cannot be empty.", DANGER); return; }
        PropositionalAST conclusion = new PropositionalAST(concText, true);
        if (!conclusion.isValid()) { setStatus("Invalid conclusion: " + concText, DANGER); return; }

        currentRule.name        = name;
        currentRule.antecedents = antecedents;
        currentRule.conclusion  = conclusion;
        currentRule.isValidated = false;
        currentRule.validationError = null;
        dirty = false;

        // Refresh list display
        int idx = ruleModel.indexOf(currentRule);
        ruleModel.set(idx, currentRule);
        ruleList.setSelectedIndex(idx);

        refreshValidationBadge(currentRule);
        setStatus("Rule saved: " + name + " — validate it to confirm correctness.", SUCCESS);
    }

    private void validateCurrentRule() {
        if (currentRule == null) { setStatus("No rule selected.", DANGER); return; }
        if (currentRule.antecedents.isEmpty() || currentRule.conclusion == null) {
            setStatus("Save the rule first before validating.", DANGER); return;
        }

        // Cancel any in-flight validation
        if (pendingValidation != null && !pendingValidation.isDone())
            pendingValidation.cancel(true);

        currentRule.isValidating    = true;
        currentRule.isValidated     = false;
        currentRule.validationError = null;
        refreshValidationBadge(currentRule);
        setStatus("Validating '" + currentRule.name + "' ...", WARN);

        final RuleDefinition target = currentRule;

        // BUG FIX #2: submit to the shared executor
        pendingValidation = validatorExecutor.submit(() -> {
            boolean ok   = false;
            String  err  = null;
            try {
                ok = runProofWithTimeout(target);
            } catch (Exception ex) {
                err = ex.getMessage();
            }
            final boolean finalOk  = ok;
            final String  finalErr = err;
            SwingUtilities.invokeLater(() -> {
                target.isValidating    = false;
                target.isValidated     = finalOk;
                target.validationError = finalErr;
                int idx = ruleModel.indexOf(target);
                if (idx >= 0) ruleModel.set(idx, target);
                if (target == currentRule) refreshValidationBadge(target);
                if (finalOk)
                    setStatus("\u2713 '" + target.name + "' is valid.", SUCCESS);
                else
                    setStatus("\u2717 '" + target.name + "' could not be proven: " + (finalErr != null ? finalErr : "proof failed"), DANGER);
            });
        });
    }

    /** Builds the implication and attempts a proof with a 1-second timeout. */
    private boolean runProofWithTimeout(RuleDefinition rule) throws Exception {
        AST implication = PropositionalLogicHelper.buildImplication(
                rule.antecedents, rule.conclusion);

        Signature signature = SignatureFactory.createSignature(UniverseOfDiscourse.PROPOSITIONS);
        PropositionalProof proof = new PropositionalProof(
                signature, new ArrayList<>(), List.of(implication));

        // BUG FIX #2: submit inline future — no extra ExecutorService created
        Future<Boolean> future = validatorExecutor.submit(proof::proveWithoutPrinting);
        try {
            return future.get(1, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            throw new Exception("Timed out after 1 s — rule may be too complex to verify automatically");
        } catch (ExecutionException e) {
            throw new Exception(e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
        }
    }

    private void deleteSelectedRule() {
        RuleDefinition sel = ruleList.getSelectedValue();
        if (sel == null) { setStatus("No rule selected.", DANGER); return; }
        int c = JOptionPane.showConfirmDialog(frame,
                "Delete rule '" + sel.name + "'? This cannot be undone.",
                "Delete Rule", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (c != JOptionPane.YES_OPTION) return;
        rules.remove(sel);
        ruleModel.removeElement(sel);
        showEmptyState();
        setStatus("Deleted rule: " + sel.name, DANGER);
    }

    private void savePackage() {
        if (rules.isEmpty()) { setStatus("No rules to save.", DANGER); return; }

        boolean hasUnvalidated = rules.stream().anyMatch(r -> !r.isValidated);
        if (hasUnvalidated) {
            int c = JOptionPane.showConfirmDialog(frame,
                    "Some rules have not been validated. Save anyway?",
                    "Unvalidated Rules", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (c != JOptionPane.YES_OPTION) return;
        }

        File dir = resolvePackageDir(currentPackageName);
        if (!dir.exists()) dir.mkdirs();

        File ruleFile = new File(dir, currentPackageName + ".atpf");
        try (PrintWriter w = new PrintWriter(new FileWriter(ruleFile))) {
            writeRulesToWriter(w);
        } catch (IOException e) {
            setStatus("Save failed: " + e.getMessage(), DANGER); return;
        }

        writeMetaFile(dir, null);
        currentPackageDirectory = dir;
        dirty = false;
        setStatus("Saved " + rules.size() + " rule(s) to '" + currentPackageName + "'", SUCCESS);
    }

    private void importRules() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Import .atpf Rule File");
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Custom Rule Files (*.atpf)", "atpf"));
        if (fc.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION) return;

        File src = fc.getSelectedFile();
        int imported = loadRulesFromFile(src);
        setStatus("Imported " + imported + " rule(s) from " + src.getName(), SUCCESS);
    }

    private void exportPackage() {
        if (rules.isEmpty()) { setStatus("No rules to export.", DANGER); return; }

        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Choose Export Directory");
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (fc.showSaveDialog(frame) != JFileChooser.APPROVE_OPTION) return;

        File exportRoot = new File(fc.getSelectedFile(), currentPackageName);
        if (!exportRoot.exists()) exportRoot.mkdirs();

        File ruleFile = new File(exportRoot, currentPackageName + ".atpf");
        try (PrintWriter w = new PrintWriter(new FileWriter(ruleFile))) {
            writeRulesToWriter(w);
        } catch (IOException e) {
            setStatus("Export failed: " + e.getMessage(), DANGER); return;
        }

        // BUG FIX #4: write template meta first, then overwrite with existing if present
        writeMetaFile(exportRoot, currentPackageDirectory);
        setStatus("Exported package to " + exportRoot.getAbsolutePath(), SUCCESS);
    }

    // =========================================================================
    // Package I/O helpers
    // =========================================================================

    private void loadPackage(String packageName) {
        File dir = resolvePackageDir(packageName);
        currentPackageDirectory = dir;

        if (!dir.isDirectory()) { setStatus("Package not found: " + packageName, DANGER); return; }

        File ruleFile = new File(dir, packageName + ".atpf");
        if (!ruleFile.exists()) { setStatus("Rule file not found: " + packageName + ".atpf", DANGER); return; }

        rules.clear();
        ruleModel.clear();
        int loaded = loadRulesFromFile(ruleFile);
        // Mark loaded rules as validated (they were saved by this tool previously)
        rules.forEach(r -> r.isValidated = true);
        ruleModel.clear();
        rules.forEach(ruleModel::addElement);

        if (loaded > 0) ruleList.setSelectedIndex(0);
        frame.setTitle("Custom Rule Editor  —  " + packageName);
        setStatus("Loaded " + loaded + " rule(s) from '" + packageName + "'", SUCCESS);
    }

    /**
     * Parses rules from a .atpf file and appends them to the rule list.
     * Returns the number of rules imported.
     *
     * BUG FIX #3: the original outer loop did `index++` unconditionally at the end,
     * which skipped the line immediately after every "end" token.  Fixed by only
     * advancing past "end" inside the inner loop.
     */
    private int loadRulesFromFile(File file) {
        List<String> lines;
        try {
            lines = loader.getLines(file.getPath());
        } catch (Exception e) {
            setStatus("Cannot read file: " + e.getMessage(), DANGER);
            return 0;
        }
        if (lines == null || lines.isEmpty()) return 0;

        int imported = 0;
        int i = 0;
        while (i < lines.size()) {
            String line = lines.get(i).trim();
            if (line.startsWith("rule=")) {
                String ruleName = line.substring(5).trim();
                List<String> block = new ArrayList<>();
                block.add(line);
                i++;                              // move past "rule=..." line
                while (i < lines.size()) {
                    String cur = lines.get(i).trim();
                    block.add(cur);
                    i++;                          // consume this line
                    if (cur.equals("end")) break; // stop after "end"
                }
                // i now points at the line after "end" — correct, no extra advance
                try {
                    CustomPropositionalInferenceRule cr =
                            loader.loadCustomRule(block, ruleName);
                    if (cr != null && cr.conclusion() != null) {
                        RuleDefinition rd = new RuleDefinition(
                                cr.name(), cr.antecedents(), cr.conclusion());
                        rules.add(rd);
                        ruleModel.addElement(rd);
                        imported++;
                    }
                } catch (Exception ex) {
                    System.err.println("Skipping malformed rule '" + ruleName + "': " + ex.getMessage());
                }
            } else {
                i++; // blank line or unrecognised — skip
            }
        }
        return imported;
    }

    private void writeRulesToWriter(PrintWriter w) {
        for (RuleDefinition r : rules) {
            w.println("rule=" + r.name);
            for (AST a : r.antecedents) w.println("a: " + a.toString());
            w.println("c: " + r.conclusion.toString());
            w.println("end");
            w.println();
        }
    }

    /**
     * Writes a meta file to targetDir.
     * BUG FIX #4: always writes the blank template first, then copies the existing
     * meta from sourceDir if one exists (previously the order was reversed, meaning
     * the template could silently overwrite a real existing meta file).
     */
    private void writeMetaFile(File targetDir, File sourceDir) {
        File metaFile = new File(targetDir, "meta");
        // Step 1: write the blank template
        try (PrintWriter w = new PrintWriter(new FileWriter(metaFile))) {
            w.println("Checked:");
            w.println("Sound:");
            w.println("Consistent:");
            w.println("Refutation Complete:");
            w.println("Correct:");
        } catch (IOException e) {
            System.err.println("Warning: could not write meta file: " + e.getMessage());
        }
        // Step 2: overwrite with real existing meta if present
        if (sourceDir != null) {
            File sourceMeta = new File(sourceDir, "meta");
            if (sourceMeta.exists() && !sourceMeta.getAbsolutePath()
                    .equals(metaFile.getAbsolutePath())) {
                try {
                    Files.copy(sourceMeta.toPath(), metaFile.toPath(),
                            StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    System.err.println("Warning: could not copy meta file: " + e.getMessage());
                }
            }
        }
    }

    private File resolvePackageDir(String packageName) {
        return new File("files/customRules/" + packageName);
    }

    // =========================================================================
    // UI helpers
    // =========================================================================

    private boolean confirmDiscard(String action) {
        if (!dirty) return true;
        int c = JOptionPane.showConfirmDialog(frame,
                "You have unsaved changes. Discard them and " + action + "?",
                "Unsaved Changes", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        return c == JOptionPane.YES_OPTION;
    }

    private void refreshValidationBadge(RuleDefinition r) {
        if (r.isValidating) {
            validationBadge .setText("⏳");
            validationBadge .setForeground(WARN);
            validationDetail.setText("Validating…");
            validationDetail.setForeground(WARN);
        } else if (r.isValidated) {
            validationBadge .setText("✓");
            validationBadge .setForeground(SUCCESS);
            validationDetail.setText("Valid");
            validationDetail.setForeground(SUCCESS);
        } else if (r.validationError != null) {
            validationBadge .setText("✗");
            validationBadge .setForeground(DANGER);
            validationDetail.setText(r.validationError);
            validationDetail.setForeground(DANGER);
        } else {
            validationBadge .setText("○");
            validationBadge .setForeground(TEXT_DIM);
            validationDetail.setText("Not validated");
            validationDetail.setForeground(TEXT_DIM);
        }
    }

    // BUG FIX #1: stored field — setStatus never does fragile getComponent() lookups
    private void setStatus(String msg, Color color) {
        statusLabel.setText(msg);
        statusLabel.setForeground(color);
    }

    // ── Widget factories ──────────────────────────────────────────────────────
    private JButton mkBtn(String text, Color bg) {
        JButton btn = new JButton(text) {
            private boolean hover = false;
            {
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { hover = true;  repaint(); }
                    public void mouseExited (MouseEvent e) { hover = false; repaint(); }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hover ? bg.brighter() : bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BTN);
        btn.setForeground(TEXT_PRI);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        return btn;
    }

    private JTextField styledField() {
        JTextField f = new JTextField();
        f.setBackground(BG_RAISED);
        f.setForeground(TEXT_PRI);
        f.setCaretColor(ACCENT);
        f.setFont(FONT_LABEL);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        f.setSelectionColor(ACCENT_SOFT);
        return f;
    }

    private JTextArea styledArea() {
        JTextArea a = new JTextArea();
        a.setBackground(BG_DEEP);
        a.setForeground(TEXT_PRI);
        a.setCaretColor(ACCENT);
        a.setFont(FONT_MONO);
        a.setLineWrap(false);
        a.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        a.setSelectionColor(ACCENT_SOFT);
        return a;
    }

    private JScrollPane styledScroll(JComponent c) {
        JScrollPane sp = new JScrollPane(c);
        sp.setBorder(BorderFactory.createLineBorder(BORDER));
        sp.setBackground(BG_DEEP);
        sp.getViewport().setBackground(BG_DEEP);
        sp.getVerticalScrollBar().setPreferredSize(new Dimension(6, 0));
        return sp;
    }

    private JPanel sectionLabel(String title, String hint) {
        JPanel p = new JPanel(new BorderLayout(10, 0));
        p.setBackground(BG_SURFACE);
        p.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        JLabel t = new JLabel(title);
        t.setFont(FONT_LABEL_B);
        t.setForeground(TEXT_PRI);
        JLabel h = new JLabel(hint);
        h.setFont(FONT_SMALL);
        h.setForeground(TEXT_DIM);
        p.add(t, BorderLayout.WEST);
        p.add(h, BorderLayout.EAST);
        return p;
    }

    // =========================================================================
    // Cell renderer  (BUG FIX #4 — no misused system icons)
    // =========================================================================
    private class RuleCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(
                JList<?> list, Object value, int index,
                boolean isSelected, boolean hasFocus) {

            RuleDefinition r = (RuleDefinition) value;

            JPanel row = new JPanel(new BorderLayout(8, 0));
            row.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
            row.setBackground(isSelected ? ACCENT_SOFT
                    : (index % 2 == 0 ? BG_RAISED : BG_SURFACE));

            JLabel name = new JLabel(r.name);
            name.setFont(FONT_LABEL_B);
            name.setForeground(isSelected ? TEXT_PRI : TEXT_PRI);

            JLabel badge = new JLabel();
            badge.setFont(FONT_SMALL);
            if (r.isValidating) {
                badge.setText("⏳");
                badge.setForeground(WARN);
            } else if (r.isValidated) {
                badge.setText("✓ valid");
                badge.setForeground(SUCCESS);
            } else if (r.validationError != null) {
                badge.setText("✗ failed");
                badge.setForeground(DANGER);
            } else {
                badge.setText("○ unvalidated");
                badge.setForeground(TEXT_DIM);
            }

            row.add(name,  BorderLayout.CENTER);
            row.add(badge, BorderLayout.EAST);
            return row;
        }
    }

    // =========================================================================
    // Rule data class
    // =========================================================================
    private static class RuleDefinition {
        String      name;
        List<AST>   antecedents;
        AST         conclusion;
        boolean     isValidated    = false;
        boolean     isValidating   = false;
        String      validationError = null;

        RuleDefinition(String name, List<AST> antecedents, AST conclusion) {
            this.name        = name;
            this.antecedents = antecedents != null ? new ArrayList<>(antecedents) : new ArrayList<>();
            this.conclusion  = conclusion;
        }

        @Override public String toString() { return name; }
    }

    // =========================================================================
    // Entry point
    // =========================================================================
    public static void main(String[] args) {
        SwingUtilities.invokeLater(CustomRuleEditor::new);
    }
}