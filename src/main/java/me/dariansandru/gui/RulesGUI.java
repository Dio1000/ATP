package me.dariansandru.gui;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RulesGUI {

    private static final Color BG_DEEP = new Color(0x0A0C10);
    private static final Color BG_SURFACE = new Color(0x14161C);
    private static final Color BG_RAISED = new Color(0x1E2230);
    private static final Color ACCENT = new Color(0x7C6FFF);
    private static final Color ACCENT_LIGHT = new Color(0x9B8FFF);
    private static final Color TEXT_PRI = new Color(0xE8EDF5);
    private static final Color TEXT_SEC = new Color(0x8A8FA8);
    private static final Color BORDER = new Color(0x2A2F42);

    private final JFrame frame;
    private final DefaultListModel<RuleInfo> model = new DefaultListModel<>();
    private final JList<RuleInfo> list = new JList<>(model);
    private final JTextArea detailArea = new JTextArea();
    private final JLabel ruleCountLabel = new JLabel();
    private final List<RuleInfo> allRules = new ArrayList<>();

    public RulesGUI() {
        frame = new JFrame("Rules Reference");
        frame.setSize(1100, 750);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setMinimumSize(new Dimension(800, 500));

        initRules();
        initUI();
    }

    public void show() {
        frame.setVisible(true);
    }

    private void initUI() {
        frame.setLayout(new BorderLayout(0, 0));
        frame.getContentPane().setBackground(BG_DEEP);

        frame.add(buildTopBar(), BorderLayout.NORTH);
        frame.add(buildMain(), BorderLayout.CENTER);
        frame.add(buildStatusBar(), BorderLayout.SOUTH);

        load(allRules);
        updateRuleCount();

        if (!allRules.isEmpty()) {
            list.setSelectedIndex(0);
            show(allRules.getFirst());
        }
    }

    private JPanel buildTopBar() {
        JPanel p = new JPanel(new BorderLayout(12, 0));
        p.setBackground(BG_SURFACE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);

        JButton back = createStyledButton("Back", ACCENT, Color.WHITE);
        back.addActionListener(e -> frame.dispose());

        JLabel title = new JLabel("Rule Book");
        title.setForeground(TEXT_PRI);
        title.setFont(new Font("SansSerif", Font.BOLD, 20));

        left.add(back);
        left.add(title);

        JPanel right = new JPanel(new BorderLayout(8, 0));
        right.setOpaque(false);

        JTextField search = new JTextField();
        search.setPreferredSize(new Dimension(280, 34));
        search.setBackground(BG_RAISED);
        search.setForeground(TEXT_PRI);
        search.setCaretColor(ACCENT);
        search.setFont(new Font("SansSerif", Font.PLAIN, 13));
        search.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));

        search.getDocument().addDocumentListener((SimpleDocListener) e ->
                filter(search.getText())
        );

        right.add(search, BorderLayout.CENTER);

        p.add(left, BorderLayout.WEST);
        p.add(right, BorderLayout.EAST);

        return p;
    }

    private JSplitPane buildMain() {
        list.setBackground(BG_SURFACE);
        list.setForeground(TEXT_PRI);
        list.setSelectionBackground(ACCENT);
        list.setSelectionForeground(Color.WHITE);
        list.setFont(new Font("SansSerif", Font.PLAIN, 14));
        list.setFixedCellHeight(42);
        list.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));

        list.setCellRenderer((l, v, i, sel, foc) -> {
            JPanel panel = new JPanel(new BorderLayout(8, 0));
            panel.setOpaque(true);
            panel.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

            JLabel nameLabel = new JLabel(v.name());
            nameLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
            nameLabel.setForeground(sel ? Color.WHITE : TEXT_PRI);

            String arityDisplay = v.arity() == -1 ? "n" : String.valueOf(v.arity());
            JLabel arityLabel = new JLabel(" /" + arityDisplay);
            arityLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
            arityLabel.setForeground(sel ? Color.WHITE : TEXT_SEC);

            panel.add(nameLabel, BorderLayout.WEST);
            panel.add(arityLabel, BorderLayout.EAST);

            if (sel) {
                panel.setBackground(ACCENT);
                panel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 3, 0, 0, ACCENT_LIGHT),
                        BorderFactory.createEmptyBorder(10, 11, 10, 14)
                ));
            } else {
                panel.setBackground(i % 2 == 0 ? BG_SURFACE : BG_RAISED);
            }

            return panel;
        });

        list.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                show(list.getSelectedValue());
            }
        });

        JScrollPane leftScroll = new JScrollPane(list);
        leftScroll.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER));
        leftScroll.getVerticalScrollBar().setUnitIncrement(16);
        leftScroll.setBackground(BG_SURFACE);

        detailArea.setEditable(false);
        detailArea.setBackground(BG_DEEP);
        detailArea.setForeground(TEXT_PRI);
        detailArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        detailArea.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        detailArea.setLineWrap(true);
        detailArea.setWrapStyleWord(true);

        JScrollPane rightScroll = new JScrollPane(detailArea);
        rightScroll.setBorder(BorderFactory.createEmptyBorder());
        rightScroll.getVerticalScrollBar().setUnitIncrement(16);
        rightScroll.setBackground(BG_DEEP);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftScroll, rightScroll);
        split.setDividerLocation(350);
        split.setDividerSize(2);
        split.setBackground(BG_DEEP);

        return split;
    }

    private JPanel buildStatusBar() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG_SURFACE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER),
                BorderFactory.createEmptyBorder(6, 16, 6, 16)
        ));

        ruleCountLabel.setForeground(TEXT_SEC);
        ruleCountLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));

        p.add(ruleCountLabel, BorderLayout.WEST);

        return p;
    }

    private void updateRuleCount() {
        int total = allRules.size();
        int shown = model.size();
        if (total == shown) {
            ruleCountLabel.setText(String.format("%d rules available", total));
        } else {
            ruleCountLabel.setText(String.format("Showing %d of %d rules", shown, total));
        }
    }

    private void show(RuleInfo r) {
        if (r == null) {
            detailArea.setText("Select a rule to view details");
            return;
        }

        StringBuilder sb = new StringBuilder();

        sb.append("Rule: ").append(r.name()).append("\n");
        if (r.arity() == -1) sb.append("Arity: n").append("\n");
        else sb.append("Arity: ").append(r.arity()).append("\n");
        sb.append("\n");
        sb.append("Derivation:\n");
        sb.append(formatDerivation(r.example()));

        detailArea.setText(sb.toString());
        detailArea.setCaretPosition(0);
    }

    private String formatDerivation(String raw) {
        String[] lines = raw.split("\n");
        StringBuilder sb = new StringBuilder();

        for (String line : lines) {
            if (line.trim().isEmpty()) continue;

            if (line.contains("─") || line.contains("----") || line.matches("^[─\\-]+$")) {
                sb.append("  ").append("─".repeat(Math.min(line.length(), 40))).append("\n");
            } else {
                sb.append("  ").append(line).append("\n");
            }
        }
        return sb.toString();
    }

    private void filter(String text) {
        String q = text.toLowerCase().trim();

        if (q.isEmpty()) {
            load(allRules);
        } else {
            List<RuleInfo> filtered = allRules.stream()
                    .filter(r ->
                            r.name().toLowerCase().contains(q) ||
                                    String.valueOf(r.arity()).contains(q))
                    .collect(Collectors.toList());
            load(filtered);
        }
        updateRuleCount();
    }

    private void load(List<RuleInfo> rules) {
        model.clear();
        rules.forEach(model::addElement);
    }

    private JButton createStyledButton(String text, Color bg, Color fg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setFont(new Font("SansSerif", Font.BOLD, 13));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

        b.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                b.setBackground(ACCENT_LIGHT);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                b.setBackground(bg);
            }
        });

        return b;
    }

    private void initRules() {
        addRule("Implication Strategy", 1,
                "Goal: P -> Q\nAssume P\nDerive Q\n----\nP -> Q");

        addRule("Equivalence Strategy", 1,
                "Goal: P <-> Q\nProve P -> Q\nProve Q -> P\n----\nP <-> Q");

        addRule("Conjunction Strategy", 1,
                "Goal: P AND Q\nProve P\nProve Q\n----\nP AND Q");

        addRule("Disjunction Strategy", 1,
                "Goal: P OR Q\nProve P (or Q)\n----\nP OR Q");

        addRule("Negation Strategy", 1,
                "Goal: !P\nAssume P\nDerive contradiction\n----\n!P");

        addRule("Contrapositive Strategy", 1,
                "Goal: P -> Q\nProve !Q -> !P\n----\nP -> Q");

        addRule("Absorption", 1,
                "P AND (P OR Q)\n----\nP");

        addRule("Conjunction Introduction", -1,
                "P\nQ\n----\nP AND Q");

        addRule("Conjunction Elimination", 1,
                "P AND Q\n----\nP");

        addRule("Constructive Dilemma", 3,
                "P -> Q\nR -> S\nP OR R\n----\nQ OR S");

        addRule("Destructive Dilemma", 3,
                "P -> Q\nR -> S\n!Q OR !S\n----\n!P OR !R");

        addRule("Disjunction Introduction", -1,
                "P\n----\nP OR Q");

        addRule("Disjunction Elimination", 1,
                "P OR Q\nP -> R\nQ -> R\n----\nR");

        addRule("Disjunctive Syllogism", 2,
                "P OR Q\n!P\n----\nQ");

        addRule("Equivalence Introduction", 2,
                "P <-> Q\nP\n----\nQ");

        addRule("Equivalence Elimination", 1,
                "P <-> Q\n----\nP -> Q");

        addRule("Hypothetical Syllogism", 2,
                "P -> Q\nQ -> R\n----\nP -> R");

        addRule("Implication Introduction", 2,
                "P -> Q\nP\n----\nQ");

        addRule("Implication Elimination", 1,
                "P -> Q\n----\n!Q -> !P");

        addRule("Modus Ponens", 2,
                "P -> Q\nP\n----\nQ");

        addRule("Modus Tollens", 2,
                "P -> Q\n!Q\n----\n!P");

        addRule("Proof by Cases", 1,
                "P OR Q\nP -> R\nQ -> R\n----\nR");

        addRule("Disjunction Simplification", 1,
                "P OR Q\n----\nP");

        addRule("Material Equivalence", 1,
                "P <-> Q\n----\n(P -> Q) AND (Q -> P)");

        addRule("Material Implication", 1,
                "P -> Q\n----\n!P OR Q");

        addRule("De Morgan", 1,
                "!(P AND Q)\n----\n!P OR !Q");

        addRule("Transposition", 1,
                "P -> Q\n----\n!Q -> !P");

        addRule("Contradiction", 2,
                "P\n!P\n----\n⊥");
    }

    private void addRule(String name, int arity, String example) {
        allRules.add(new RuleInfo(name, arity, example));
    }

    private record RuleInfo(String name, int arity, String example) {}

    private interface SimpleDocListener extends javax.swing.event.DocumentListener {
        void update(javax.swing.event.DocumentEvent e);

        @Override
        default void insertUpdate(javax.swing.event.DocumentEvent e) {
            update(e);
        }

        @Override
        default void removeUpdate(javax.swing.event.DocumentEvent e) {
            update(e);
        }

        @Override
        default void changedUpdate(javax.swing.event.DocumentEvent e) {
            update(e);
        }
    }
}

/*
private static class CommandInfo {
        final String  command;
        final int     arity;
        final boolean fixedArity;

        CommandInfo(String command, int arity, boolean fixedArity) {
            this.command    = command;
            this.arity      = arity;
            this.fixedArity = fixedArity;
        }
    }
 */