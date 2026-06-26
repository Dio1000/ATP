package me.dariansandru.gui;

import javax.swing.*;
import java.awt.*;

public final class GUITheme {

    private GUITheme() {}

    public static final Color BACKGROUND_DEEP = new Color(0x0F1117);
    public static final Color BACKGROUND_SURFACE = new Color(0x1A1D27);
    public static final Color BACKGROUND_RAISED = new Color(0x22263A);
    public static final Color BACKGROUND_HOVER = new Color(0x2A2F48);

    public static final Color INTENT_NEUTRAL = new Color(0x3A3F58);
    public static final Color INTENT_PRIMARY = new Color(0x6C63FF);
    public static final Color INTENT_POSITIVE = new Color(0x3DDC84);
    public static final Color INTENT_ATTENTION = new Color(0xE0A22D);
    public static final Color INTENT_DANGER = new Color(0xE0526B);

    public static final Color RULE_BUILTIN_DOT = INTENT_PRIMARY;
    public static final Color RULE_CUSTOM_DOT = INTENT_ATTENTION;

    public static final Color TEXT_PRIMARY = new Color(0xECEFF4);
    public static final Color TEXT_SECONDARY = new Color(0x8A8FA8);
    public static final Color TEXT_DIM = new Color(0x565A72);
    public static final Color BORDER_COLOR = new Color(0x2E3350);
    public static final Color CHECKBOX_FOREGROUND = new Color(0xB0B4CC);

    public static final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 15);
    public static final Font LABEL_FONT = new Font("SansSerif", Font.PLAIN, 12);
    public static final Font LABEL_BOLD_FONT = new Font("SansSerif", Font.BOLD, 12);
    public static final Font MONOSPACED_FONT = new Font("Monospaced", Font.PLAIN, 12);
    public static final Font SMALL_FONT = new Font("SansSerif", Font.PLAIN, 10);
    public static final Font SECTION_FONT = new Font("SansSerif", Font.BOLD, 11);
    public static final Font BUTTON_FONT = new Font("SansSerif", Font.BOLD, 12);

    public static void applyLookAndFeel() {
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
}