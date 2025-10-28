package me.dariansandru.gui;

import me.dariansandru.controller.LogicController;
import me.dariansandru.utils.helper.ProofTextHelper;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GUIController extends JFrame {
    private final JTextArea outputArea;
    private final ButtonGroup universeGroup;

    private final List<String> kbEntries = new ArrayList<>();
    private final List<String> goalEntries = new ArrayList<>();
    private final DefaultListModel<String> kbListModel = new DefaultListModel<>();
    private final DefaultListModel<String> goalListModel = new DefaultListModel<>();
    private final JList<String> kbList;
    private final JList<String> goalList;

    public GUIController() {
        setTitle("Automated Theorem Prover");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1600, 1000);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel inputPanel = new JPanel(new BorderLayout(10, 10));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Input Parameters"));

        JPanel universePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        universePanel.setBorder(BorderFactory.createTitledBorder("Select Universe"));

        universeGroup = new ButtonGroup();
        String[] universes = {"Propositions", "Integer Numbers", "Real Numbers", "Strings", "Sets"};

        for (String universe : universes) {
            JRadioButton radioButton = new JRadioButton(universe);
            radioButton.setActionCommand(universe);
            radioButton.setFont(new Font("Cambria Math", Font.PLAIN, 16));
            universeGroup.add(radioButton);
            universePanel.add(radioButton);
        }
        universeGroup.getElements().nextElement().setSelected(true);

        JPanel kbGoalsPanel = new JPanel(new GridLayout(1, 2, 15, 0));

        JPanel kbPanel = new JPanel(new BorderLayout(5, 5));
        kbPanel.setBorder(BorderFactory.createTitledBorder("Knowledge Base"));
        kbPanel.setPreferredSize(new Dimension(600, 400));

        JPanel kbInputPanel = new JPanel(new BorderLayout(5, 5));
        JTextField kbInputField = new JTextField();
        kbInputField.setFont(new Font("Cambria Math", Font.PLAIN, 16));

        JButton addKbButton = new JButton("Add KB Entry");
        addKbButton.addActionListener(e -> {
            String entry = kbInputField.getText().trim();
            if (!entry.isEmpty()) {
                kbEntries.add(entry);
                kbListModel.addElement("KB" + kbEntries.size() + ": " + entry);
                kbInputField.setText("");
            }
        });
        kbInputPanel.add(kbInputField, BorderLayout.CENTER);
        kbInputPanel.add(addKbButton, BorderLayout.EAST);

        kbList = new JList<>(kbListModel);
        kbList.setFont(new Font("Cambria Math", Font.PLAIN, 16));
        kbList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane kbScroll = new JScrollPane(kbList);

        JPanel kbControlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton deleteKbButton = new JButton("Delete Selected");
        deleteKbButton.addActionListener(e -> {
            int selectedIndex = kbList.getSelectedIndex();
            if (selectedIndex != -1) {
                kbEntries.remove(selectedIndex);
                kbListModel.remove(selectedIndex);
                refreshKbList();
            }
        });

        JButton clearKbButton = new JButton("Clear All");
        clearKbButton.addActionListener(e -> {
            kbEntries.clear();
            kbListModel.clear();
        });
        kbControlPanel.add(deleteKbButton);
        kbControlPanel.add(clearKbButton);

        kbPanel.add(kbInputPanel, BorderLayout.NORTH);
        kbPanel.add(kbScroll, BorderLayout.CENTER);
        kbPanel.add(kbControlPanel, BorderLayout.SOUTH);

        JPanel goalsPanel = new JPanel(new BorderLayout(5, 5));
        goalsPanel.setBorder(BorderFactory.createTitledBorder("Goals"));
        goalsPanel.setPreferredSize(new Dimension(600, 400));

        JPanel goalInputPanel = new JPanel(new BorderLayout(5, 5));
        JTextField goalInputField = new JTextField();
        goalInputField.setFont(new Font("Cambria Math", Font.PLAIN, 16));
        JButton addGoalButton = new JButton("Add Goal");
        addGoalButton.addActionListener(e -> {
            String entry = goalInputField.getText().trim();
            if (!entry.isEmpty()) {
                goalEntries.add(entry);
                goalListModel.addElement("Goal" + goalEntries.size() + ": " + entry);
                goalInputField.setText("");
            }
        });
        goalInputPanel.add(goalInputField, BorderLayout.CENTER);
        goalInputPanel.add(addGoalButton, BorderLayout.EAST);

        goalList = new JList<>(goalListModel);
        goalList.setFont(new Font("Cambria Math", Font.PLAIN, 16));
        goalList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane goalScroll = new JScrollPane(goalList);

        JPanel goalControlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton deleteGoalButton = new JButton("Delete Selected");
        deleteGoalButton.addActionListener(e -> {
            int selectedIndex = goalList.getSelectedIndex();
            if (selectedIndex != -1) {
                goalEntries.remove(selectedIndex);
                goalListModel.remove(selectedIndex);
                refreshGoalList();
            }
        });

        JButton clearGoalButton = new JButton("Clear All");
        clearGoalButton.addActionListener(e -> {
            goalEntries.clear();
            goalListModel.clear();
        });
        goalControlPanel.add(deleteGoalButton);
        goalControlPanel.add(clearGoalButton);

        goalsPanel.add(goalInputPanel, BorderLayout.NORTH);
        goalsPanel.add(goalScroll, BorderLayout.CENTER);
        goalsPanel.add(goalControlPanel, BorderLayout.SOUTH);

        kbGoalsPanel.add(kbPanel);
        kbGoalsPanel.add(goalsPanel);

        inputPanel.add(universePanel, BorderLayout.NORTH);
        inputPanel.add(kbGoalsPanel, BorderLayout.CENTER);

        JPanel outputPanel = new JPanel(new BorderLayout());
        outputPanel.setBorder(BorderFactory.createTitledBorder("Proof Output"));
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Cambria Math", Font.PLAIN, 16));
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);
        outputArea.setText("Proof output will appear here...");

        JScrollPane outputScroll = new JScrollPane(outputArea);
        outputPanel.add(outputScroll, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        JButton proveButton = createStyledButton("Run Proof", 140, 45);
        proveButton.addActionListener(e -> runProver());
        buttonPanel.add(proveButton);

        JButton clearButton = createStyledButton("Clear Output", 140, 45);
        clearButton.addActionListener(e -> outputArea.setText(""));
        buttonPanel.add(clearButton);

        JButton clearAllButton = createStyledButton("Clear All", 140, 45);
        clearAllButton.addActionListener(e -> clearAll());
        buttonPanel.add(clearAllButton);

        mainPanel.add(inputPanel, BorderLayout.NORTH);
        mainPanel.add(outputPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        getContentPane().add(mainPanel);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void clearAll() {
        kbEntries.clear();
        goalEntries.clear();

        kbListModel.clear();
        goalListModel.clear();
        outputArea.setText("");

        universeGroup.clearSelection();
        universeGroup.getElements().nextElement().setSelected(true);
    }

    private void refreshKbList() {
        kbListModel.clear();
        for (int i = 0; i < kbEntries.size(); i++) {
            kbListModel.addElement("KB" + (i + 1) + ": " + kbEntries.get(i));
        }
    }

    private void refreshGoalList() {
        goalListModel.clear();
        for (int i = 0; i < goalEntries.size(); i++) {
            goalListModel.addElement("Goal" + (i + 1) + ": " + goalEntries.get(i));
        }
    }

    private JButton createStyledButton(String text, int width, int height) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 16));
        button.setPreferredSize(new Dimension(width, height));
        return button;
    }

    private void runProver() {
        outputArea.setText("");
        String universe = universeGroup.getSelection().getActionCommand();

        if (goalEntries.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please add at least one goal", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Universe:\n").append(universe).append("\n\n");
        sb.append("KB:\n");
        for (String entry : kbEntries) {
            sb.append(entry).append("\n");
        }
        sb.append("\nGoals:\n");
        for (String goal : goalEntries) {
            sb.append(goal).append("\n");
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("files/input.txt"))) {
            writer.write(sb.toString());
        }
        catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error writing input.txt: " + ex.getMessage(),
                    "File Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        outputArea.setText("Processing proof...");

        LogicController logicController = new LogicController("files/input.txt");
        logicController.automatedRun();

        try {
            String proof = ProofTextHelper.getProofString();
            outputArea.setText(proof);
            outputArea.setCaretPosition(0);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            outputArea.setText("Error during proof execution: " + ex.getMessage());
        }

//        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
//            @Override
//            protected Void doInBackground() throws Exception {
//                LogicController logicController = new LogicController("files/input.txt");
//                logicController.automatedRun();
//                return null;
//            }
//
//            @Override
//            protected void done() {
//                try {
//                    get();
//                    String proof = ProofTextHelper.getProofString();
//                    outputArea.setText(proof);
//                    outputArea.setCaretPosition(0);
//                }
//                catch (Exception ex) {
//                    ex.printStackTrace();
//                    outputArea.setText("Error during proof execution: " + ex.getMessage());
//                }
//            }
//        };

       //worker.execute();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(GUIController::new);
    }
}