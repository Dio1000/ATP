package me.dariansandru.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RulesGUI {

    private static final Color BACKGROUND_DEEP = new Color(0x0A0C10);
    private static final Color BACKGROUND_SURFACE = new Color(0x14161C);
    private static final Color BACKGROUND_RAISED = new Color(0x1E2230);
    private static final Color ACCENT_COLOR = new Color(0x7C6FFF);
    private static final Color ACCENT_LIGHT_COLOR = new Color(0x9B8FFF);
    private static final Color TEXT_PRIMARY_COLOR = new Color(0xE8EDF5);
    private static final Color TEXT_SECONDARY_COLOR = new Color(0x8A8FA8);
    private static final Color BORDER_COLOR = new Color(0x2A2F42);

    private final JFrame mainFrame;
    private final DefaultListModel<RuleInfo> ruleListModel = new DefaultListModel<>();
    private final JList<RuleInfo> ruleList = new JList<>(this.ruleListModel);
    private final JTextArea detailTextArea = new JTextArea();
    private final JLabel ruleCountLabel = new JLabel();
    private final List<RuleInfo> allRules = new ArrayList<>();

    public RulesGUI() {
        this.mainFrame = new JFrame("Rules Reference");
        this.mainFrame.setSize(1100, 750);
        this.mainFrame.setLocationRelativeTo(null);
        this.mainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.mainFrame.setMinimumSize(new Dimension(800, 500));

        this.initializeRules();
        this.initializeUI();
    }

    public void show() {
        this.mainFrame.setVisible(true);
    }

    private void initializeUI() {
        this.mainFrame.setLayout(new BorderLayout(0, 0));
        this.mainFrame.getContentPane().setBackground(BACKGROUND_DEEP);

        this.mainFrame.add(this.buildTopBar(), BorderLayout.NORTH);
        this.mainFrame.add(this.buildMainArea(), BorderLayout.CENTER);
        this.mainFrame.add(this.buildStatusBar(), BorderLayout.SOUTH);

        this.loadRules(this.allRules);
        this.updateRuleCount();

        if (!this.allRules.isEmpty()) {
            this.ruleList.setSelectedIndex(0);
            this.showRule(this.allRules.getFirst());
        }
    }

    private JPanel buildTopBar() {
        JPanel topBarPanel = new JPanel(new BorderLayout(12, 0));
        topBarPanel.setBackground(BACKGROUND_SURFACE);
        topBarPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftPanel.setOpaque(false);

        JButton backButton = this.createStyledButton("Back", ACCENT_COLOR, Color.WHITE);
        backButton.addActionListener(event -> this.mainFrame.dispose());

        JLabel titleLabel = new JLabel("Rule Book");
        titleLabel.setForeground(TEXT_PRIMARY_COLOR);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));

        leftPanel.add(backButton);
        leftPanel.add(titleLabel);

        JPanel rightPanel = new JPanel(new BorderLayout(8, 0));
        rightPanel.setOpaque(false);

        JTextField searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(280, 34));
        searchField.setBackground(BACKGROUND_RAISED);
        searchField.setForeground(TEXT_PRIMARY_COLOR);
        searchField.setCaretColor(ACCENT_COLOR);
        searchField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));

        searchField.getDocument().addDocumentListener((SimpleDocumentListener) event ->
                this.filterRules(searchField.getText())
        );

        rightPanel.add(searchField, BorderLayout.CENTER);

        topBarPanel.add(leftPanel, BorderLayout.WEST);
        topBarPanel.add(rightPanel, BorderLayout.EAST);

        return topBarPanel;
    }

    private JSplitPane buildMainArea() {
        this.ruleList.setBackground(BACKGROUND_SURFACE);
        this.ruleList.setForeground(TEXT_PRIMARY_COLOR);
        this.ruleList.setSelectionBackground(ACCENT_COLOR);
        this.ruleList.setSelectionForeground(Color.WHITE);
        this.ruleList.setFont(new Font("SansSerif", Font.PLAIN, 14));
        this.ruleList.setFixedCellHeight(42);
        this.ruleList.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));

        this.ruleList.setCellRenderer((list, value, index, isSelected, hasFocus) -> {
            RuleInfo rule = (RuleInfo) value;
            JPanel panel = new JPanel(new BorderLayout(8, 0));
            panel.setOpaque(true);
            panel.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

            JLabel nameLabel = new JLabel(rule.name());
            nameLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
            nameLabel.setForeground(isSelected ? Color.WHITE : TEXT_PRIMARY_COLOR);

            String arityDisplay = rule.arity() == -1 ? "n" : String.valueOf(rule.arity());
            JLabel arityLabel = new JLabel(" /" + arityDisplay);
            arityLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
            arityLabel.setForeground(isSelected ? Color.WHITE : TEXT_SECONDARY_COLOR);

            panel.add(nameLabel, BorderLayout.WEST);
            panel.add(arityLabel, BorderLayout.EAST);

            if (isSelected) {
                panel.setBackground(ACCENT_COLOR);
                panel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 3, 0, 0, ACCENT_LIGHT_COLOR),
                        BorderFactory.createEmptyBorder(10, 11, 10, 14)
                ));
            }
            else {
                panel.setBackground(index % 2 == 0 ? BACKGROUND_SURFACE : BACKGROUND_RAISED);
            }

            return panel;
        });

        this.ruleList.addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                this.showRule(this.ruleList.getSelectedValue());
            }
        });

        JScrollPane leftScrollPane = new JScrollPane(this.ruleList);
        leftScrollPane.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_COLOR));
        leftScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        leftScrollPane.setBackground(BACKGROUND_SURFACE);

        this.detailTextArea.setEditable(false);
        this.detailTextArea.setBackground(BACKGROUND_DEEP);
        this.detailTextArea.setForeground(TEXT_PRIMARY_COLOR);
        this.detailTextArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        this.detailTextArea.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        this.detailTextArea.setLineWrap(true);
        this.detailTextArea.setWrapStyleWord(true);

        JScrollPane rightScrollPane = new JScrollPane(this.detailTextArea);
        rightScrollPane.setBorder(BorderFactory.createEmptyBorder());
        rightScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        rightScrollPane.setBackground(BACKGROUND_DEEP);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftScrollPane, rightScrollPane);
        splitPane.setDividerLocation(350);
        splitPane.setDividerSize(2);
        splitPane.setBackground(BACKGROUND_DEEP);

        return splitPane;
    }

    private JPanel buildStatusBar() {
        JPanel statusBarPanel = new JPanel(new BorderLayout());
        statusBarPanel.setBackground(BACKGROUND_SURFACE);
        statusBarPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR),
                BorderFactory.createEmptyBorder(6, 16, 6, 16)
        ));

        this.ruleCountLabel.setForeground(TEXT_SECONDARY_COLOR);
        this.ruleCountLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));

        statusBarPanel.add(this.ruleCountLabel, BorderLayout.WEST);

        return statusBarPanel;
    }

    private void updateRuleCount() {
        int total = this.allRules.size();
        int shown = this.ruleListModel.size();
        if (total == shown) {
            this.ruleCountLabel.setText(String.format("%d rules available", total));
        }
        else {
            this.ruleCountLabel.setText(String.format("Showing %d of %d rules", shown, total));
        }
    }

    private void showRule(RuleInfo rule) {
        if (rule == null) {
            this.detailTextArea.setText("Select a rule to view details");
            return;
        }

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("Rule: ").append(rule.name()).append("\n");
        if (rule.arity() == -1) {
            stringBuilder.append("Arity: n").append("\n");
        }
        else {
            stringBuilder.append("Arity: ").append(rule.arity()).append("\n");
        }
        stringBuilder.append("\n");
        stringBuilder.append("Derivation:\n");
        stringBuilder.append(this.formatDerivation(rule.example()));

        this.detailTextArea.setText(stringBuilder.toString());
        this.detailTextArea.setCaretPosition(0);
    }

    private String formatDerivation(String raw) {
        String[] lines = raw.split("\n");
        StringBuilder stringBuilder = new StringBuilder();

        for (String line : lines) {
            if (line.trim().isEmpty()) {
                continue;
            }

            if (line.contains("─") || line.contains("----") || line.matches("^[─\\-]+$")) {
                stringBuilder.append("  ").append("─".repeat(Math.min(line.length(), 40))).append("\n");
            }
            else {
                stringBuilder.append("  ").append(line).append("\n");
            }
        }
        return stringBuilder.toString();
    }

    private void filterRules(String text) {
        String query = text.toLowerCase().trim();

        if (query.isEmpty()) {
            this.loadRules(this.allRules);
        }
        else {
            List<RuleInfo> filteredRules = this.allRules.stream()
                    .filter(rule ->
                            rule.name().toLowerCase().contains(query) ||
                                    String.valueOf(rule.arity()).contains(query))
                    .collect(Collectors.toList());
            this.loadRules(filteredRules);
        }
        this.updateRuleCount();
    }

    private void loadRules(List<RuleInfo> rules) {
        this.ruleListModel.clear();
        rules.forEach(this.ruleListModel::addElement);
    }

    private JButton createStyledButton(String text, Color backgroundColor, Color foregroundColor) {
        JButton button = new JButton(text);
        button.setBackground(backgroundColor);
        button.setForeground(foregroundColor);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(new Font("SansSerif", Font.BOLD, 13));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent event) {
                button.setBackground(ACCENT_LIGHT_COLOR);
            }

            public void mouseExited(java.awt.event.MouseEvent event) {
                button.setBackground(backgroundColor);
            }
        });

        return button;
    }

    private void initializeRules() {
        this.addRule("Implication Strategy", 1,
                "Goal: P -> Q\nAssume P\nDerive Q\n----\nP -> Q");

        this.addRule("Equivalence Strategy", 1,
                "Goal: P <-> Q\nProve P -> Q\nProve Q -> P\n----\nP <-> Q");

        this.addRule("Conjunction Strategy", 1,
                "Goal: P AND Q\nProve P\nProve Q\n----\nP AND Q");

        this.addRule("Disjunction Strategy", 1,
                "Goal: P OR Q\nProve P (or Q)\n----\nP OR Q");

        this.addRule("Negation Strategy", 1,
                "Goal: !P\nAssume P\nDerive contradiction\n----\n!P");

        this.addRule("Contrapositive Strategy", 1,
                "Goal: P -> Q\nProve !Q -> !P\n----\nP -> Q");

        this.addRule("Absorption", 1,
                "P AND (P OR Q)\n----\nP");

        this.addRule("Conjunction Introduction", -1,
                "P\nQ\n----\nP AND Q");

        this.addRule("Conjunction Elimination", 1,
                "P AND Q\n----\nP");

        this.addRule("Constructive Dilemma", 3,
                "P -> Q\nR -> S\nP OR R\n----\nQ OR S");

        this.addRule("Destructive Dilemma", 3,
                "P -> Q\nR -> S\n!Q OR !S\n----\n!P OR !R");

        this.addRule("Disjunction Introduction", -1,
                "P\n----\nP OR Q");

        this.addRule("Disjunction Elimination", 1,
                "P OR Q\nP -> R\nQ -> R\n----\nR");

        this.addRule("Disjunctive Syllogism", 2,
                "P OR Q\n!P\n----\nQ");

        this.addRule("Equivalence Introduction", 2,
                "P <-> Q\nP\n----\nQ");

        this.addRule("Equivalence Elimination", 1,
                "P <-> Q\n----\nP -> Q");

        this.addRule("Hypothetical Syllogism", 2,
                "P -> Q\nQ -> R\n----\nP -> R");

        this.addRule("Implication Introduction", 2,
                "P -> Q\nP\n----\nQ");

        this.addRule("Implication Elimination", 1,
                "P -> Q\n----\n!Q -> !P");

        this.addRule("Modus Ponens", 2,
                "P -> Q\nP\n----\nQ");

        this.addRule("Modus Tollens", 2,
                "P -> Q\n!Q\n----\n!P");

        this.addRule("Proof by Cases", 1,
                "P OR Q\nP -> R\nQ -> R\n----\nR");

        this.addRule("Disjunction Simplification", 1,
                "P OR Q\n----\nP");

        this.addRule("Material Equivalence", 1,
                "P <-> Q\n----\n(P -> Q) AND (Q -> P)");

        this.addRule("Material Implication", 1,
                "P -> Q\n----\n!P OR Q");

        this.addRule("De Morgan", 1,
                "!(P AND Q)\n----\n!P OR !Q");

        this.addRule("Transposition", 1,
                "P -> Q\n----\n!Q -> !P");

        this.addRule("Contradiction", 2,
                "P\n!P\n----\n⊥");
    }

    private void addRule(String name, int arity, String example) {
        this.allRules.add(new RuleInfo(name, arity, example));
    }

    private record RuleInfo(String name, int arity, String example) {}

    private interface SimpleDocumentListener extends javax.swing.event.DocumentListener {
        void update(javax.swing.event.DocumentEvent event);

        @Override
        default void insertUpdate(javax.swing.event.DocumentEvent event) {
            this.update(event);
        }

        @Override
        default void removeUpdate(javax.swing.event.DocumentEvent event) {
            this.update(event);
        }

        @Override
        default void changedUpdate(javax.swing.event.DocumentEvent event) {
            this.update(event);
        }
    }
}