package me.dariansandru.gui;

import me.dariansandru.domain.data_structures.ast.AST;
import me.dariansandru.utils.helper.KnowledgeBaseRegistry;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

public class GUIComponents {

    private final PropositionalProofGUIController controller;

    public GUIComponents(PropositionalProofGUIController controller) {
        this.controller = controller;
    }

    public JFrame buildMainFrame() {
        JFrame mainFrame = new JFrame("Propositional Proof System");
        mainFrame.setSize(1440, 860);
        mainFrame.setMinimumSize(new Dimension(1100, 650));
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setLayout(new BorderLayout());
        mainFrame.getContentPane().setBackground(GUITheme.BACKGROUND_DEEP);

        mainFrame.add(this.buildTitleBar(), BorderLayout.NORTH);
        mainFrame.add(this.buildMainArea(), BorderLayout.CENTER);
        mainFrame.add(this.buildStatusBar(), BorderLayout.SOUTH);

        return mainFrame;
    }

    private JPanel buildTitleBar() {
        JPanel titleBarPanel = new JPanel(new BorderLayout());
        titleBarPanel.setBackground(GUITheme.BACKGROUND_DEEP);
        titleBarPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, GUITheme.BORDER_COLOR),
                BorderFactory.createEmptyBorder(14, 20, 14, 20)));

        JLabel titleLabel = new JLabel("Propositional Proof Assistant");
        titleLabel.setFont(GUITheme.TITLE_FONT);
        titleLabel.setForeground(GUITheme.TEXT_PRIMARY);

        JPanel leftCluster = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        leftCluster.setOpaque(false);
        leftCluster.add(titleLabel);
        leftCluster.add(this.buildProofActionsCluster());
        titleBarPanel.add(leftCluster, BorderLayout.WEST);

        JButton rulesButton = this.createButton("Rules", GUITheme.INTENT_NEUTRAL);
        rulesButton.addActionListener(event -> {
            RulesGUI rulesGui = new RulesGUI();
            rulesGui.show();
        });
        titleBarPanel.add(rulesButton, BorderLayout.EAST);

        return titleBarPanel;
    }

    private JPanel buildProofActionsCluster() {
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        controlPanel.setOpaque(false);

        JButton createProofButton = this.createButton("Create Proof", GUITheme.INTENT_PRIMARY);
        JButton resetButton = this.createButton("Reset", GUITheme.INTENT_NEUTRAL);
        JButton doneButton = this.createButton("Done", GUITheme.INTENT_POSITIVE);

        createProofButton.addActionListener(event -> this.controller.getActions().createProof());
        resetButton.addActionListener(event -> this.controller.getActions().reset());
        doneButton.addActionListener(event -> this.controller.getActions().handleDone());

        controlPanel.add(createProofButton);
        controlPanel.add(resetButton);
        controlPanel.add(doneButton);

        this.controller.getActions().setCreateProofButton(createProofButton);

        return controlPanel;
    }

    private JSplitPane buildMainArea() {
        JSplitPane outerSplitPanel = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                this.buildLeftPanel(),
                this.buildRightPanel());
        outerSplitPanel.setDividerSize(6);
        outerSplitPanel.setResizeWeight(0.40);
        outerSplitPanel.setBackground(GUITheme.BACKGROUND_DEEP);
        outerSplitPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return outerSplitPanel;
    }

    private JPanel buildLeftPanel() {
        JPanel leftPanel = new JPanel(new BorderLayout(0, 10));
        leftPanel.setBackground(GUITheme.BACKGROUND_DEEP);
        leftPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 6));

        JSplitPane leftVerticalSplit = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                this.buildKnowledgeBasePanel(),
                this.buildGoalPanel());
        leftVerticalSplit.setDividerSize(6);
        leftVerticalSplit.setResizeWeight(0.70);
        leftVerticalSplit.setBackground(GUITheme.BACKGROUND_DEEP);

        leftPanel.add(leftVerticalSplit, BorderLayout.CENTER);
        return leftPanel;
    }

    private JPanel buildKnowledgeBasePanel() {
        JPanel knowledgeBasePanel = new JPanel(new BorderLayout(0, 8));
        knowledgeBasePanel.setBackground(GUITheme.BACKGROUND_SURFACE);
        knowledgeBasePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GUITheme.BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)));
        knowledgeBasePanel.add(this.buildSectionHeader("Knowledge Base", ""), BorderLayout.NORTH);

        JPanel knowledgeBaseCheckboxPanel = new JPanel();
        knowledgeBaseCheckboxPanel.setLayout(new BoxLayout(knowledgeBaseCheckboxPanel, BoxLayout.Y_AXIS));
        knowledgeBaseCheckboxPanel.setBackground(GUITheme.BACKGROUND_DEEP);
        this.controller.setKnowledgeBaseCheckboxPanel(knowledgeBaseCheckboxPanel);

        JScrollPane scrollPane = this.createStyledScrollPane(knowledgeBaseCheckboxPanel);
        knowledgeBasePanel.add(scrollPane, BorderLayout.CENTER);
        knowledgeBasePanel.add(this.buildFormulaInputBar(true), BorderLayout.SOUTH);

        return knowledgeBasePanel;
    }

    private JPanel buildGoalPanel() {
        JPanel goalPanel = new JPanel(new BorderLayout(0, 8));
        goalPanel.setBackground(GUITheme.BACKGROUND_SURFACE);
        goalPanel.setPreferredSize(new Dimension(0, 220));
        goalPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GUITheme.BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)));

        goalPanel.add(this.buildSectionHeader("Goals", ""), BorderLayout.NORTH);

        JPanel goalCheckboxPanel = new JPanel();
        goalCheckboxPanel.setLayout(new BoxLayout(goalCheckboxPanel, BoxLayout.Y_AXIS));
        goalCheckboxPanel.setBackground(GUITheme.BACKGROUND_DEEP);
        this.controller.setGoalCheckboxPanel(goalCheckboxPanel);

        JScrollPane scrollPane = this.createStyledScrollPane(goalCheckboxPanel);
        goalPanel.add(scrollPane, BorderLayout.CENTER);
        goalPanel.add(this.buildFormulaInputBar(false), BorderLayout.SOUTH);

        return goalPanel;
    }

    private JPanel buildFormulaInputBar(boolean isKnowledgeBase) {
        JPanel inputBar = new JPanel(new BorderLayout(6, 0));
        inputBar.setBackground(GUITheme.BACKGROUND_SURFACE);
        inputBar.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));

        String placeholderText = isKnowledgeBase ? "Enter Knowledge Base formula..." : "Enter goal formula...";
        JTextField textField = this.createStyledTextField(placeholderText);

        if (isKnowledgeBase) {
            this.controller.setKnowledgeBaseInputField(textField);
            textField.addActionListener(event -> this.controller.getActions().addToKnowledgeBase());
        }
        else {
            this.controller.setGoalInputField(textField);
            textField.addActionListener(event -> this.controller.getActions().addToGoal());
        }

        JButton addButton = this.createButton("+", GUITheme.INTENT_NEUTRAL);
        addButton.setPreferredSize(new Dimension(36, 30));
        addButton.addActionListener(event -> this.controller.getActions().openBatchFormulaDialog(isKnowledgeBase));

        inputBar.add(textField, BorderLayout.CENTER);
        inputBar.add(addButton, BorderLayout.EAST);

        return inputBar;
    }

    public JPanel buildRightPanel() {
        JPanel rulesContainerPanel = new JPanel(new BorderLayout(0, 0));
        rulesContainerPanel.setBackground(GUITheme.BACKGROUND_DEEP);

        JPanel rulesPanel = new JPanel();
        rulesPanel.setLayout(new BoxLayout(rulesPanel, BoxLayout.Y_AXIS));
        rulesPanel.setBackground(GUITheme.BACKGROUND_DEEP);
        this.controller.setRulesPanel(rulesPanel);

        JScrollPane rulesScrollPane = this.createStyledScrollPane(rulesPanel);
        JPanel rulesWrapper = this.wrapInSection(rulesScrollPane, "Applicable Rules & Strategies", "");
        rulesContainerPanel.add(rulesWrapper, BorderLayout.CENTER);
        rulesContainerPanel.add(this.buildRuleSourceActions(), BorderLayout.SOUTH);

        JTextArea stateDisplayArea = new JTextArea();
        stateDisplayArea.setEditable(false);
        stateDisplayArea.setFont(GUITheme.MONOSPACED_FONT);
        stateDisplayArea.setBackground(GUITheme.BACKGROUND_DEEP);
        stateDisplayArea.setForeground(new Color(0xA3E6B0));
        stateDisplayArea.setCaretColor(GUITheme.INTENT_PRIMARY);
        stateDisplayArea.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        stateDisplayArea.setLineWrap(true);
        stateDisplayArea.setWrapStyleWord(true);
        this.controller.setStateDisplayArea(stateDisplayArea);

        JPanel stateContainerPanel = new JPanel(new BorderLayout(0, 8));
        stateContainerPanel.setBackground(GUITheme.BACKGROUND_DEEP);

        JPanel stateWrapper = this.wrapInSection(this.createStyledScrollPane(stateDisplayArea), "Proof State", "");
        JPanel stateControlPanel = this.buildStateControlPanel();

        stateContainerPanel.add(stateWrapper, BorderLayout.CENTER);
        stateContainerPanel.add(stateControlPanel, BorderLayout.SOUTH);

        JSplitPane topSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, rulesContainerPanel, stateContainerPanel);
        topSplit.setDividerSize(6);
        topSplit.setResizeWeight(0.50);
        topSplit.setBackground(GUITheme.BACKGROUND_DEEP);
        this.controller.getActions().setTopSplit(topSplit);

        JTextArea automatedProofOutputArea = new JTextArea();
        automatedProofOutputArea.setEditable(false);
        automatedProofOutputArea.setFont(GUITheme.MONOSPACED_FONT);
        automatedProofOutputArea.setBackground(GUITheme.BACKGROUND_DEEP);
        automatedProofOutputArea.setForeground(GUITheme.TEXT_PRIMARY);
        automatedProofOutputArea.setCaretColor(GUITheme.INTENT_PRIMARY);
        automatedProofOutputArea.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        automatedProofOutputArea.setLineWrap(true);
        automatedProofOutputArea.setWrapStyleWord(true);
        this.controller.setAutomatedProofOutputArea(automatedProofOutputArea);

        JPanel automatedWrapper = this.wrapInSection(this.createStyledScrollPane(automatedProofOutputArea), "Automated Proof Output", "");
        JPanel automatedContainerPanel = new JPanel(new BorderLayout(0, 8));
        automatedContainerPanel.setBackground(GUITheme.BACKGROUND_DEEP);
        automatedContainerPanel.add(automatedWrapper, BorderLayout.CENTER);
        this.controller.getActions().setAutomatedContainerPanel(automatedContainerPanel);

        JPanel automatedControlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        automatedControlPanel.setBackground(GUITheme.BACKGROUND_SURFACE);
        automatedControlPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, GUITheme.BORDER_COLOR),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        automatedContainerPanel.add(automatedControlPanel, BorderLayout.SOUTH);
        this.controller.getActions().setAutomatedControlPanel(automatedControlPanel);

        JPanel rightContentHolder = new JPanel(new BorderLayout());
        rightContentHolder.setBackground(GUITheme.BACKGROUND_DEEP);
        rightContentHolder.add(topSplit, BorderLayout.CENTER);
        this.controller.getActions().setRightContentHolder(rightContentHolder);

        JPanel rightRoot = new JPanel(new BorderLayout(0, 8));
        rightRoot.setBackground(GUITheme.BACKGROUND_DEEP);
        rightRoot.add(rightContentHolder, BorderLayout.CENTER);
        rightRoot.add(this.buildAutomatePanel(), BorderLayout.SOUTH);

        return rightRoot;
    }

    private JPanel buildRuleSourceActions() {
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        actionsPanel.setBackground(GUITheme.BACKGROUND_SURFACE);
        actionsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, GUITheme.BORDER_COLOR),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));

        JButton loadPackageButton = this.createButton("Load Package", GUITheme.INTENT_NEUTRAL);
        JButton createEditorButton = this.createButton("Rule Editor", GUITheme.INTENT_NEUTRAL);

        loadPackageButton.addActionListener(event -> this.controller.getActions().showPackageLoaderDialog());
        createEditorButton.addActionListener(event -> this.controller.getActions().openRuleEditor());

        actionsPanel.add(loadPackageButton);
        actionsPanel.add(createEditorButton);

        this.controller.getActions().setLoadPackageButton(loadPackageButton);
        this.controller.getActions().setCreateEditorButton(createEditorButton);

        return actionsPanel;
    }

    private JPanel buildStateControlPanel() {
        JPanel stateControlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        stateControlPanel.setBackground(GUITheme.BACKGROUND_SURFACE);
        stateControlPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, GUITheme.BORDER_COLOR),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));

        JLabel stateLabel = new JLabel("State:");
        stateLabel.setForeground(GUITheme.TEXT_SECONDARY);
        stateLabel.setFont(GUITheme.LABEL_FONT);

        JTextField stateIndexField = this.createStyledTextField("");
        stateIndexField.setPreferredSize(new Dimension(52, 28));
        stateIndexField.setHorizontalAlignment(JTextField.CENTER);
        this.controller.getActions().setStateIndexField(stateIndexField);

        JButton changeStateButton = this.createButton("Go to State", GUITheme.INTENT_NEUTRAL);
        changeStateButton.addActionListener(event -> this.controller.getActions().handleChangeState());

        stateControlPanel.add(stateLabel);
        stateControlPanel.add(stateIndexField);
        stateControlPanel.add(changeStateButton);

        return stateControlPanel;
    }

    private JPanel buildAutomatePanel() {
        JPanel automatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        automatePanel.setBackground(GUITheme.BACKGROUND_SURFACE);
        automatePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, GUITheme.BORDER_COLOR),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));

        JButton automateButton = this.createButton("Automate", GUITheme.INTENT_ATTENTION);
        automateButton.addActionListener(event -> this.controller.getActions().runAutomatedProof());

        automatePanel.add(automateButton);
        this.controller.getActions().setAutomateButton(automateButton);

        return automatePanel;
    }

    public JPanel buildStatusBar() {
        JPanel statusBarPanel = new JPanel(new BorderLayout());
        statusBarPanel.setBackground(GUITheme.BACKGROUND_DEEP);
        statusBarPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, GUITheme.BORDER_COLOR),
                BorderFactory.createEmptyBorder(6, 18, 6, 18)));

        JLabel statusLabel = new JLabel("Ready, add knowledge base formulas and goals to begin.");
        statusLabel.setFont(GUITheme.SMALL_FONT);
        statusLabel.setForeground(GUITheme.TEXT_DIM);
        statusBarPanel.add(statusLabel, BorderLayout.WEST);
        this.controller.getActions().setStatusLabel(statusLabel);

        JLabel hintLabel = new JLabel("Select KB formulas or goals to see applicable rules");
        hintLabel.setFont(GUITheme.SMALL_FONT);
        hintLabel.setForeground(GUITheme.TEXT_DIM);
        statusBarPanel.add(hintLabel, BorderLayout.EAST);

        return statusBarPanel;
    }

    public JPanel buildSectionHeader(String title, String icon) {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(GUITheme.BACKGROUND_SURFACE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(GUITheme.LABEL_BOLD_FONT);
        titleLabel.setForeground(GUITheme.TEXT_PRIMARY);

        JSeparator separator = new JSeparator();
        separator.setForeground(GUITheme.BORDER_COLOR);
        separator.setBackground(GUITheme.BORDER_COLOR);

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(separator, BorderLayout.SOUTH);
        return headerPanel;
    }

    public JPanel wrapInSection(JComponent innerComponent, String title, String icon) {
        JPanel wrapperPanel = new JPanel(new BorderLayout(0, 8));
        wrapperPanel.setBackground(GUITheme.BACKGROUND_SURFACE);
        wrapperPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GUITheme.BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)));
        wrapperPanel.add(this.buildSectionHeader(title, icon), BorderLayout.NORTH);
        wrapperPanel.add(innerComponent, BorderLayout.CENTER);
        return wrapperPanel;
    }

    public JScrollPane createStyledScrollPane(JComponent viewComponent) {
        JScrollPane scrollPane = new JScrollPane(viewComponent);
        scrollPane.setBorder(BorderFactory.createLineBorder(GUITheme.BORDER_COLOR, 1));
        scrollPane.setBackground(GUITheme.BACKGROUND_DEEP);
        scrollPane.getViewport().setBackground(GUITheme.BACKGROUND_DEEP);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setBackground(GUITheme.BACKGROUND_DEEP);
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(6, 0));
        return scrollPane;
    }

    public JTextField createStyledTextField(String placeholderText) {
        JTextField textField = new JTextField() {
            @Override
            protected void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                if (this.getText().isEmpty() && !placeholderText.isEmpty() && !this.hasFocus()) {
                    Graphics2D graphics2D = (Graphics2D) graphics;
                    graphics2D.setColor(GUITheme.TEXT_DIM);
                    graphics2D.setFont(this.getFont().deriveFont(Font.ITALIC));
                    Insets insets = this.getInsets();
                    graphics2D.drawString(placeholderText, insets.left + 2, this.getHeight() / 2 + graphics2D.getFontMetrics().getAscent() / 2 - 1);
                }
            }
        };
        textField.setBackground(GUITheme.BACKGROUND_RAISED);
        textField.setForeground(GUITheme.TEXT_PRIMARY);
        textField.setCaretColor(GUITheme.INTENT_PRIMARY);
        textField.setFont(GUITheme.MONOSPACED_FONT);
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GUITheme.BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        textField.setSelectionColor(new Color(0x3D3880));
        return textField;
    }

    public JButton createButton(String buttonText, Color backgroundColor) {
        JButton button = new JButton(buttonText) {
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
                Color fillColor = isHovering ? backgroundColor.brighter() : backgroundColor;
                if (!this.isEnabled()) {
                    fillColor = GUITheme.BACKGROUND_HOVER;
                }
                graphics2D.setColor(fillColor);
                graphics2D.fill(new RoundRectangle2D.Float(0, 0, this.getWidth(), this.getHeight(), 8, 8));
                graphics2D.dispose();
                super.paintComponent(graphics);
            }
        };
        button.setFont(GUITheme.BUTTON_FONT);
        button.setForeground(GUITheme.TEXT_PRIMARY);
        button.setBackground(backgroundColor);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(6, 16, 6, 16));
        return button;
    }

    public JButton createSmallApplyButton() {
        JButton button = this.createButton("Apply", GUITheme.INTENT_PRIMARY);
        button.setFont(GUITheme.SMALL_FONT.deriveFont(Font.BOLD));
        button.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        button.setPreferredSize(new Dimension(64, 24));
        button.setMaximumSize(new Dimension(64, 24));
        return button;
    }

    public JPanel buildFormulaRow(int rowIndex, AST formulaAst, boolean isCheckable, boolean isKnowledgeBase) {
        Color rowBackground = isKnowledgeBase ? GUITheme.BACKGROUND_RAISED : new Color(0x1E2A35);
        JPanel rowPanel = new JPanel(new BorderLayout(8, 0));
        rowPanel.setBackground(rowBackground);
        rowPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, GUITheme.BORDER_COLOR),
                BorderFactory.createEmptyBorder(5, 8, 5, 10)));
        rowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));

        JCheckBox checkBox = new JCheckBox();
        checkBox.setBackground(rowBackground);
        checkBox.setForeground(GUITheme.CHECKBOX_FOREGROUND);
        checkBox.setEnabled(isCheckable);

        JLabel numberLabel = new JLabel(String.valueOf(rowIndex));
        numberLabel.setFont(GUITheme.SMALL_FONT);
        numberLabel.setForeground(GUITheme.TEXT_DIM);
        numberLabel.setPreferredSize(new Dimension(22, -1));

        JLabel formulaLabel = new JLabel(formulaAst.toString());
        formulaLabel.setFont(GUITheme.MONOSPACED_FONT);
        formulaLabel.setForeground(GUITheme.TEXT_PRIMARY);

        JPanel textPanel = new JPanel(new GridLayout(isCheckable && isKnowledgeBase ? 2 : 1, 1, 0, 1));
        textPanel.setBackground(rowBackground);
        textPanel.add(formulaLabel);

        if (isCheckable && isKnowledgeBase) {
            String source = KnowledgeBaseRegistry.getObtainedFrom(formulaAst.toString());
            JLabel sourceLabel = new JLabel(source);
            sourceLabel.setFont(GUITheme.SMALL_FONT);
            sourceLabel.setForeground(GUITheme.INTENT_PRIMARY.brighter());
            textPanel.add(sourceLabel);
        }

        JPanel leftPanel = new JPanel(new BorderLayout(4, 0));
        leftPanel.setBackground(rowBackground);
        leftPanel.add(checkBox, BorderLayout.WEST);
        leftPanel.add(numberLabel, BorderLayout.CENTER);

        rowPanel.add(leftPanel, BorderLayout.WEST);
        rowPanel.add(textPanel, BorderLayout.CENTER);

        if (isCheckable) {
            if (isKnowledgeBase) {
                this.controller.getKnowledgeBaseCheckboxes().add(checkBox);
            }
            else {
                this.controller.getGoalCheckboxes().add(checkBox);
            }
            checkBox.addActionListener(event -> this.controller.getActions().updateSelectedFormulas());
        }
        return rowPanel;
    }

    public JPanel buildRuleRow(String ruleName, boolean isCustom, Runnable action) {
        JPanel rowPanel = new JPanel(new BorderLayout(8, 0));
        rowPanel.setBackground(GUITheme.BACKGROUND_SURFACE);
        rowPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, GUITheme.BORDER_COLOR),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        rowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        Color dotColor = isCustom ? GUITheme.RULE_CUSTOM_DOT : GUITheme.RULE_BUILTIN_DOT;
        JLabel dotLabel = new JLabel(isCustom ? "\u25C6" : "\u25CF");
        dotLabel.setForeground(dotColor);
        dotLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));
        dotLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 6));

        JLabel nameLabel = new JLabel(ruleName);
        nameLabel.setFont(GUITheme.LABEL_FONT);
        nameLabel.setForeground(isCustom ? GUITheme.RULE_CUSTOM_DOT : GUITheme.TEXT_PRIMARY);

        JPanel namePanel = new JPanel(new BorderLayout());
        namePanel.setBackground(GUITheme.BACKGROUND_SURFACE);
        namePanel.add(dotLabel, BorderLayout.WEST);
        namePanel.add(nameLabel, BorderLayout.CENTER);

        JButton applyButton = this.createSmallApplyButton();
        applyButton.addActionListener(event -> action.run());

        rowPanel.add(namePanel, BorderLayout.CENTER);
        rowPanel.add(applyButton, BorderLayout.EAST);
        return rowPanel;
    }

    public JPanel buildSectionDivider(String labelText) {
        JPanel dividerPanel = new JPanel(new BorderLayout(8, 0));
        dividerPanel.setBackground(GUITheme.BACKGROUND_DEEP);
        dividerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        dividerPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 4, 8));

        JLabel labelComponent = new JLabel(labelText);
        labelComponent.setFont(GUITheme.SECTION_FONT);
        labelComponent.setForeground(GUITheme.TEXT_DIM);

        JSeparator separator = new JSeparator();
        separator.setForeground(GUITheme.BORDER_COLOR);

        dividerPanel.add(labelComponent, BorderLayout.WEST);
        dividerPanel.add(separator, BorderLayout.CENTER);
        return dividerPanel;
    }
}