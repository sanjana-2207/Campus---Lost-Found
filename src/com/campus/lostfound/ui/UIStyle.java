package com.campus.lostfound.ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.table.JTableHeader;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Single source of truth for the visual language of the application:
 * colours, fonts, buttons, cards, fields and tables.
 *
 * <p>Keeping these values in one class is the reason every screen looks
 * consistent, and it lets the whole theme be changed in one place.</p>
 */
public final class UIStyle {

    // ---------------- Palette ----------------
    public static final Color NAVY = new Color(0x132A3E);
    public static final Color NAVY_SOFT = new Color(0x1D3D56);
    public static final Color ACCENT = new Color(0x2C7DA0);
    public static final Color ACCENT_DARK = new Color(0x1F5C78);
    public static final Color ACCENT_LIGHT = new Color(0xE7F1F7);
    public static final Color BG = new Color(0xF2F5F8);
    public static final Color CARD = Color.WHITE;
    public static final Color BORDER = new Color(0xDCE3EA);
    public static final Color TEXT = new Color(0x1F2A37);
    public static final Color MUTED = new Color(0x6B7A8C);
    public static final Color SUCCESS = new Color(0x2E7D4F);
    public static final Color WARNING = new Color(0xC77D00);
    public static final Color DANGER = new Color(0xC0392B);

    // ---------------- Fonts ----------------
    private static final String FAMILY = "Segoe UI";
    public static final Font FONT = new Font(FAMILY, Font.PLAIN, 13);
    public static final Font FONT_SMALL = new Font(FAMILY, Font.PLAIN, 12);
    public static final Font FONT_BOLD = new Font(FAMILY, Font.BOLD, 13);
    public static final Font FONT_LABEL = new Font(FAMILY, Font.BOLD, 12);
    public static final Font TITLE = new Font(FAMILY, Font.BOLD, 24);
    public static final Font SUBTITLE = new Font(FAMILY, Font.PLAIN, 13);
    public static final Font VALUE = new Font(FAMILY, Font.BOLD, 26);

    private UIStyle() {
    }

    /** Applies the theme to the standard dialogs Swing creates internally. */
    public static void applyGlobalDefaults() {
        UIManager.put("Label.font", FONT);
        UIManager.put("Button.font", FONT_BOLD);
        UIManager.put("TextField.font", FONT);
        UIManager.put("PasswordField.font", FONT);
        UIManager.put("ComboBox.font", FONT);
        UIManager.put("TextArea.font", FONT);
        UIManager.put("Table.font", FONT);
        UIManager.put("TableHeader.font", FONT_LABEL);
        UIManager.put("TabbedPane.font", FONT_BOLD);
        UIManager.put("OptionPane.messageFont", FONT);
        UIManager.put("OptionPane.buttonFont", FONT_BOLD);
        UIManager.put("Spinner.font", FONT);
        UIManager.put("CheckBox.font", FONT);
        UIManager.put("RadioButton.font", FONT);
        UIManager.put("TitledBorder.font", FONT_LABEL);
    }

    // ---------------- Labels ----------------

    public static JLabel heading(String text) {
        JLabel label = new JLabel(text);
        label.setFont(TITLE);
        label.setForeground(TEXT);
        return label;
    }

    public static JLabel subtitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(SUBTITLE);
        label.setForeground(MUTED);
        return label;
    }

    public static JLabel fieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_LABEL);
        label.setForeground(MUTED);
        return label;
    }

    public static JLabel muted(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_SMALL);
        label.setForeground(MUTED);
        return label;
    }

    // ---------------- Containers ----------------

    /** White rounded card with a light border and internal padding. */
    public static JPanel card() {
        JPanel panel = new JPanel();
        panel.setBackground(CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(16, 18, 16, 18)));
        return panel;
    }

    /** Card with an accent stripe on the left, used by the dashboard counters. */
    public static JPanel statCard(JLabel value, String caption, Color stripe) {
        JPanel panel = new JPanel();
        panel.setLayout(new java.awt.BorderLayout(0, 2));
        panel.setBackground(CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, stripe),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER),
                        BorderFactory.createEmptyBorder(14, 16, 14, 16))));

        value.setFont(VALUE);
        value.setForeground(TEXT);

        JLabel captionLabel = new JLabel(caption);
        captionLabel.setFont(FONT_SMALL);
        captionLabel.setForeground(MUTED);

        panel.add(value, java.awt.BorderLayout.NORTH);
        panel.add(captionLabel, java.awt.BorderLayout.CENTER);
        return panel;
    }

    // ---------------- Buttons ----------------

    public static JButton primaryButton(String text) {
        return button(text, ACCENT, Color.WHITE, ACCENT_DARK);
    }

    public static JButton secondaryButton(String text) {
        return button(text, CARD, TEXT, new Color(0xEDF2F6));
    }

    public static JButton dangerButton(String text) {
        return button(text, DANGER, Color.WHITE, new Color(0xA32B1F));
    }

    public static JButton successButton(String text) {
        return button(text, SUCCESS, Color.WHITE, new Color(0x1F5E3A));
    }

    private static JButton button(String text, Color background, Color foreground,
                                  final Color hover) {
        final JButton button = new JButton(text);
        button.setFont(FONT_BOLD);
        button.setBackground(background);
        button.setForeground(foreground);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(9, 16, 9, 16));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        final Color normal = background;
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent event) {
                if (button.isEnabled()) {
                    button.setBackground(hover);
                }
            }

            @Override
            public void mouseExited(MouseEvent event) {
                button.setBackground(normal);
            }
        });
        return button;
    }

    /** A borderless navigation button used in the left sidebar. */
    public static JButton navButton(String text) {
        JButton button = new JButton(text);
        button.setFont(FONT_BOLD);
        button.setHorizontalAlignment(JButton.LEFT);
        button.setBackground(NAVY);
        button.setForeground(new Color(0xC9D6E2));
        button.setBorder(BorderFactory.createEmptyBorder(12, 22, 12, 16));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    // ---------------- Inputs ----------------

    public static JTextField textField() {
        JTextField field = new JTextField();
        styleInput(field);
        return field;
    }

    public static JTextArea textArea() {
        JTextArea area = new JTextArea();
        area.setFont(FONT);
        area.setForeground(TEXT);
        area.setBackground(CARD);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        return area;
    }

    public static void styleInput(JComponent component) {
        component.setFont(FONT);
        component.setForeground(TEXT);
        component.setBackground(CARD);
        component.setBorder(inputBorder());
        component.setPreferredSize(new Dimension(component.getPreferredSize().width, 34));
    }

    public static <T> JComboBox<T> comboBox() {
        JComboBox<T> box = new JComboBox<T>();
        box.setFont(FONT);
        box.setBackground(CARD);
        box.setForeground(TEXT);
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(2, 6, 2, 4)));
        box.setPreferredSize(new Dimension(box.getPreferredSize().width, 34));
        return box;
    }

    private static Border inputBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(6, 9, 6, 9));
    }

    // ---------------- Tables ----------------

    public static void styleTable(JTable table) {
        table.setRowHeight(30);
        table.setFont(FONT);
        table.setForeground(TEXT);
        table.setBackground(CARD);
        table.setSelectionBackground(ACCENT_LIGHT);
        table.setSelectionForeground(TEXT);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(0xEDF1F5));
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_LABEL);
        header.setBackground(new Color(0xF7F9FB));
        header.setForeground(MUTED);
        header.setPreferredSize(new Dimension(0, 34));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));
        header.setReorderingAllowed(false);
    }

    public static JScrollPane scroll(JComponent content) {
        JScrollPane pane = new JScrollPane(content);
        pane.setBorder(BorderFactory.createLineBorder(BORDER));
        pane.getViewport().setBackground(CARD);
        pane.setBackground(CARD);
        return pane;
    }

    /** Wraps a component with vertical spacing, used by BoxLayout forms. */
    public static JPanel labeled(String text, JComponent field) {
        JPanel panel = new JPanel(new java.awt.BorderLayout(0, 4));
        panel.setOpaque(false);
        panel.add(fieldLabel(text), java.awt.BorderLayout.NORTH);
        panel.add(field, java.awt.BorderLayout.CENTER);
        return panel;
    }
}
