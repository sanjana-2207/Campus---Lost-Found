package com.campus.lostfound.ui;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.table.JTableHeader;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.RenderingHints;
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

    // ---------------- Theme palette (light / dark) ----------------
    private static final java.util.prefs.Preferences PREFS =
            java.util.prefs.Preferences.userNodeForPackage(UIStyle.class);

    public static Color NAVY;
    public static Color NAVY_SOFT;
    public static Color ACCENT;
    public static Color ACCENT_DARK;
    public static Color ACCENT_LIGHT;
    public static Color BG;
    public static Color CARD;
    public static Color BORDER;
    public static Color TEXT;
    public static Color MUTED;
    public static Color SUCCESS;
    public static Color WARNING;
    public static Color DANGER;
    public static Color ORANGE;
    public static Color SIDEBAR_TOP;
    public static Color SIDEBAR_BOTTOM;

    private static boolean darkTheme;

    static {
        applyTheme(PREFS.getBoolean("darkTheme", false));
    }

    public static boolean isDark() {
        return darkTheme;
    }

    /** Switches the palette. Screens created afterwards use the new colours. */
    public static void applyTheme(boolean dark) {
        darkTheme = dark;
        if (dark) {
            NAVY = new Color(0x1E1033);
            NAVY_SOFT = new Color(0x2E1065);
            ACCENT = new Color(0x7C3AED);
            ACCENT_DARK = new Color(0xC4B5FD);
            ACCENT_LIGHT = new Color(0x2E1065);
            BG = new Color(0x120A1F);
            CARD = new Color(0x1E1330);
            BORDER = new Color(0x33244A);
            TEXT = new Color(0xF3E8FF);
            MUTED = new Color(0xA99BB4);
            SUCCESS = new Color(0x34D399);
            WARNING = new Color(0xFBBF24);
            DANGER = new Color(0xF87171);
            ORANGE = new Color(0xFFAE00);
            SIDEBAR_TOP = new Color(0x3B0F70);
            SIDEBAR_BOTTOM = new Color(0x4C1D95);
        } else {
            NAVY = new Color(0x4C1D95);
            NAVY_SOFT = new Color(0x5B21B6);
            ACCENT = new Color(0x6D28D9);
            ACCENT_DARK = new Color(0x4C1D95);
            ACCENT_LIGHT = new Color(0xF3E8FF);
            BG = new Color(0xF7F5FC);
            CARD = Color.WHITE;
            BORDER = new Color(0xE9E4F2);
            TEXT = new Color(0x111827);
            MUTED = new Color(0x6B7280);
            SUCCESS = new Color(0x16A34A);
            WARNING = new Color(0xD97706);
            DANGER = new Color(0xDC2626);
            ORANGE = new Color(0xFFAE00);
            SIDEBAR_TOP = new Color(0x6123B9);
            SIDEBAR_BOTTOM = new Color(0x7B25D1);
        }
        try {
            PREFS.putBoolean("darkTheme", dark);
        } catch (SecurityException ignored) {
            // Theme persistence is optional.
        }
    }

    // ---------------- Fonts ----------------
    private static final String FAMILY = "Arial";
    public static final Font FONT = new Font(FAMILY, Font.PLAIN, 13);
    public static final Font FONT_SMALL = new Font(FAMILY, Font.PLAIN, 12);
    public static final Font FONT_BOLD = new Font(FAMILY, Font.BOLD, 13);
    public static final Font FONT_LABEL = new Font(FAMILY, Font.BOLD, 12);
    public static final Font TITLE = new Font(FAMILY, Font.BOLD, 24);
    public static final Font SUBTITLE = new Font(FAMILY, Font.PLAIN, 13);
    public static final Font VALUE = new Font(FAMILY, Font.BOLD, 26);
    public static final Font HERO = new Font(FAMILY, Font.BOLD, 30);
    public static final Font SECTION = new Font(FAMILY, Font.BOLD, 18);
    public static final Font CARD_TITLE = new Font(FAMILY, Font.BOLD, 15);
    public static final Font METRIC_CAPTION = new Font(FAMILY, Font.BOLD, 10);

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
        UIManager.put("Panel.background", BG);
        UIManager.put("Panel.foreground", TEXT);
        UIManager.put("OptionPane.background", CARD);
        UIManager.put("OptionPane.messageForeground", TEXT);
        UIManager.put("TabbedPane.background", CARD);
        UIManager.put("TabbedPane.foreground", TEXT);
        UIManager.put("TabbedPane.selected", ACCENT_LIGHT);
        UIManager.put("TabbedPane.contentAreaColor", CARD);
        UIManager.put("TextField.background", CARD);
        UIManager.put("TextField.foreground", TEXT);
        UIManager.put("TextField.caretForeground", TEXT);
        UIManager.put("ComboBox.background", CARD);
        UIManager.put("ComboBox.foreground", TEXT);
        UIManager.put("CheckBox.background", CARD);
        UIManager.put("CheckBox.foreground", TEXT);
        UIManager.put("RadioButton.background", CARD);
        UIManager.put("RadioButton.foreground", TEXT);
        UIManager.put("Spinner.background", CARD);
        UIManager.put("Spinner.foreground", TEXT);
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

    /** Small "← Back" link shown below each page headline. */
    public static JButton backLink(final Runnable action) {
        JButton button = new JButton("\u2190 Back");
        button.setFont(FONT_BOLD);
        button.setForeground(ACCENT_DARK);
        button.setBackground(CARD);
        button.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.addActionListener(event -> {
            if (action != null) {
                action.run();
            }
        });
        return button;
    }

    // ---------------- Containers ----------------

    /** White rounded card with a light border and internal padding. */
    public static JPanel card() {
        JPanel panel = new RoundedPanel(CARD, BORDER, 16);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        return panel;
    }

    /** FINDIT dashboard stat: icon block, label and a large live value. */
    public static JPanel finditStatCard(JLabel value, String label,
                                        String icon, Color accent) {
        JPanel panel = new RoundedPanel(CARD, BORDER, 16);
        panel.setLayout(new BorderLayout(14, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JLabel iconLabel = new JLabel(icon, SwingConstants.CENTER);
        iconLabel.setOpaque(true);
        iconLabel.setBackground(blend(accent, CARD, 0.84f));
        iconLabel.setForeground(accent);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        iconLabel.setPreferredSize(new Dimension(44, 44));
        iconLabel.setBorder(BorderFactory.createLineBorder(
                blend(accent, CARD, 0.62f)));

        JPanel info = new JPanel(new BorderLayout(0, 2));
        info.setOpaque(false);

        JLabel caption = new JLabel(label);
        caption.setFont(FONT_SMALL);
        caption.setForeground(MUTED);

        value.setFont(VALUE.deriveFont(22f));
        value.setForeground(TEXT);

        info.add(caption, BorderLayout.NORTH);
        info.add(value, BorderLayout.CENTER);

        panel.add(iconLabel, BorderLayout.WEST);
        panel.add(info, BorderLayout.CENTER);
        return panel;
    }

    /** FINDIT dashboard quick-action card. */
    public static JPanel quickCard(String icon, String title, String description,
                                   Color accent, Runnable action) {
        final JPanel card = new RoundedPanel(CARD, BORDER, 16);
        card.setLayout(new BorderLayout(14, 0));
        card.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel iconLabel = new JLabel(icon, SwingConstants.CENTER);
        iconLabel.setOpaque(true);
        iconLabel.setBackground(blend(accent, CARD, 0.84f));
        iconLabel.setForeground(accent);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
        iconLabel.setPreferredSize(new Dimension(46, 46));
        iconLabel.setBorder(BorderFactory.createLineBorder(
                blend(accent, CARD, 0.62f)));

        JPanel texts = new JPanel();
        texts.setOpaque(false);
        texts.setLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(CARD_TITLE);
        titleLabel.setForeground(TEXT);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel descLabel = new JLabel("<html><body style='width:190px'>"
                + description + "</body></html>");
        descLabel.setFont(FONT_SMALL);
        descLabel.setForeground(MUTED);
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        texts.add(titleLabel);
        texts.add(Box.createVerticalStrut(2));
        texts.add(descLabel);

        JLabel arrow = new JLabel("\u2192");
        arrow.setFont(new Font(FAMILY, Font.BOLD, 18));
        arrow.setForeground(blend(accent, MUTED, 0.45f));

        card.add(iconLabel, BorderLayout.WEST);
        card.add(texts, BorderLayout.CENTER);
        card.add(arrow, BorderLayout.EAST);

        installCardListeners(card, new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent event) {
                card.setBackground(blend(CARD, accent, 0.06f));
                card.repaint();
            }

            @Override
            public void mouseExited(MouseEvent event) {
                Point onCard = SwingUtilities.convertPoint(
                        (Component) event.getSource(), event.getPoint(), card);
                if (!card.contains(onCard)) {
                    card.setBackground(CARD);
                    card.repaint();
                }
            }

            @Override
            public void mouseClicked(MouseEvent event) {
                Point onCard = SwingUtilities.convertPoint(
                        (Component) event.getSource(), event.getPoint(), card);
                if (card.contains(onCard) && action != null) {
                    action.run();
                }
            }
        });
        return card;
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

    /**
     * Stat card that behaves like a landing-page card: hand cursor, hover
     * highlight and a click action. Used by counters that open a screen
     * (e.g. the claims counter on the dashboard).
     */
    public static JPanel statCard(final JLabel value, String caption, Color stripe,
                                  final Runnable action) {
        final JPanel panel = statCard(value, caption, stripe);
        panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        final Color restBackground = panel.getBackground();
        final Color hoverBackground = blend(CARD, ACCENT, 0.06f);
        MouseAdapter adapter = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent event) {
                panel.setBackground(hoverBackground);
            }

            @Override
            public void mouseExited(MouseEvent event) {
                Point onCard = SwingUtilities.convertPoint(
                        (Component) event.getSource(), event.getPoint(), panel);
                if (!panel.contains(onCard)) {
                    panel.setBackground(restBackground);
                }
            }

            @Override
            public void mouseClicked(MouseEvent event) {
                Point onCard = SwingUtilities.convertPoint(
                        (Component) event.getSource(), event.getPoint(), panel);
                if (panel.contains(onCard) && action != null) {
                    action.run();
                }
            }
        };
        installCardListeners(panel, adapter);
        return panel;
    }

    // ---------------- Home page ----------------

    /** Mixes {@code overlay} into {@code base}; used for soft pastel accents. */
    public static Color blend(Color base, Color overlay, float amount) {
        float a = Math.max(0f, Math.min(1f, amount));
        int r = Math.round(base.getRed() + (overlay.getRed() - base.getRed()) * a);
        int g = Math.round(base.getGreen() + (overlay.getGreen() - base.getGreen()) * a);
        int b = Math.round(base.getBlue() + (overlay.getBlue() - base.getBlue()) * a);
        return new Color(r, g, b);
    }

    /** Emoji label that uses Windows' emoji font (Arial renders boxes). */
    public static JLabel emoji(String emoji, int size) {
        JLabel label = new JLabel(emoji);
        label.setFont(new Font("Segoe UI Emoji", Font.PLAIN, size));
        return label;
    }

    /** Small painted magnifier glyph (Arial has no reliable search symbol). */
    public static JComponent magnifier(final int size, final Color color) {
        JComponent icon = new JComponent() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g2 = (Graphics2D) graphics.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.setStroke(new java.awt.BasicStroke(
                        Math.max(1.6f, size / 9f),
                        java.awt.BasicStroke.CAP_ROUND,
                        java.awt.BasicStroke.JOIN_ROUND));
                int diameter = Math.round(size * 0.62f);
                g2.drawOval(1, 1, diameter, diameter);
                int start = Math.round(diameter * 0.86f);
                g2.drawLine(start, start, size - 2, size - 2);
                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(size, size);
            }

            @Override
            public Dimension getMinimumSize() {
                return getPreferredSize();
            }

            @Override
            public Dimension getMaximumSize() {
                return getPreferredSize();
            }
        };
        icon.setOpaque(false);
        return icon;
    }

    /** Small uppercase pill, used above the home-page welcome sentence. */
    public static JLabel pill(String text, Color foreground, Color background) {
        JLabel label = new JLabel(text);
        label.setFont(new Font(FAMILY, Font.BOLD, 10));
        label.setForeground(foreground);
        label.setOpaque(true);
        label.setBackground(background);
        label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(blend(foreground, Color.WHITE, 0.65f)),
                BorderFactory.createEmptyBorder(4, 12, 4, 12)));
        return label;
    }

    public static JLabel sectionHeading(String text) {
        JLabel label = new JLabel(text);
        label.setFont(SECTION);
        label.setForeground(TEXT);
        return label;
    }

    /**
     * Pastel home-page metric: a large coloured number over a small caption.
     * Mirrors the soft feature tiles used by the Grade Guide landing page.
     */
    public static JPanel metricCard(JLabel value, String caption,
                                    Color background, Color accent) {
        return metricCard(value, caption, background, accent, null);
    }

    /**
     * Metric that can also be clicked. Used when a counter opens a screen
     * (e.g. "Claims awaiting verification" opens the claims screen).
     */
    public static JPanel metricCard(final JLabel value, String caption,
                                    Color background, Color accent,
                                    final Runnable action) {
        final JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(background);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(blend(accent, Color.WHITE, 0.72f)),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));

        value.setFont(VALUE);
        value.setForeground(accent);
        value.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel captionLabel = new JLabel("<html><body style='width:130px'>"
                + caption + "</body></html>");
        captionLabel.setFont(METRIC_CAPTION);
        captionLabel.setForeground(blend(accent, TEXT, 0.45f));
        captionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(value);
        panel.add(Box.createVerticalStrut(6));
        panel.add(captionLabel);

        if (action != null) {
            panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            installCardListeners(panel, metricHover(panel, background,
                    blend(background, accent, 0.10f), action));
        }
        return panel;
    }

    private static MouseAdapter metricHover(final JPanel panel,
                                            final Color background,
                                            final Color hoverBackground,
                                            final Runnable action) {
        return new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent event) {
                panel.setBackground(hoverBackground);
            }

            @Override
            public void mouseExited(MouseEvent event) {
                Point onCard = SwingUtilities.convertPoint(
                        (Component) event.getSource(), event.getPoint(), panel);
                if (!panel.contains(onCard)) {
                    panel.setBackground(background);
                }
            }

            @Override
            public void mouseClicked(MouseEvent event) {
                Point onCard = SwingUtilities.convertPoint(
                        (Component) event.getSource(), event.getPoint(), panel);
                if (panel.contains(onCard) && action != null) {
                    action.run();
                }
            }
        };
    }

    /**
     * Grade Guide style feature tile: a white icon block, a bold title, a
     * supporting line and a right arrow on a pastel card. The whole card is
     * clickable and lifts its border on hover.
     */
    public static JPanel featureCard(String icon, String title, String subtitle,
                                     Color background, Color hoverBackground,
                                     Color accent, Runnable action) {
        final JPanel card = new JPanel(new BorderLayout(14, 0));
        final Border restBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(blend(accent, Color.WHITE, 0.74f)),
                BorderFactory.createEmptyBorder(16, 18, 16, 18));
        final Border activeBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(blend(accent, Color.WHITE, 0.20f)),
                BorderFactory.createEmptyBorder(16, 18, 16, 18));

        card.setBackground(background);
        card.setBorder(restBorder);
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel iconLabel = new JLabel(icon, SwingConstants.CENTER);
        iconLabel.setFont(new Font(FAMILY, Font.BOLD, 15));
        iconLabel.setForeground(accent);
        iconLabel.setOpaque(true);
        iconLabel.setBackground(CARD);
        iconLabel.setPreferredSize(new Dimension(42, 42));
        iconLabel.setMinimumSize(new Dimension(42, 42));
        iconLabel.setMaximumSize(new Dimension(42, 42));
        iconLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(blend(accent, CARD, 0.62f)),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)));

        JPanel texts = new JPanel();
        texts.setOpaque(false);
        texts.setLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(CARD_TITLE);
        titleLabel.setForeground(TEXT);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("<html><body style='width:180px'>"
                + subtitle + "</body></html>");
        subtitleLabel.setFont(FONT_SMALL);
        subtitleLabel.setForeground(MUTED);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        texts.add(titleLabel);
        texts.add(Box.createVerticalStrut(3));
        texts.add(subtitleLabel);

        JPanel textWrap = new JPanel(new GridBagLayout());
        textWrap.setOpaque(false);
        GridBagConstraints textConstraints = new GridBagConstraints();
        textConstraints.weightx = 1;
        textConstraints.fill = GridBagConstraints.HORIZONTAL;
        textConstraints.anchor = GridBagConstraints.WEST;
        textWrap.add(texts, textConstraints);

        JLabel arrow = new JLabel("\u2192");
        arrow.setFont(new Font(FAMILY, Font.BOLD, 18));
        arrow.setForeground(blend(accent, MUTED, 0.45f));

        card.add(iconLabel, BorderLayout.WEST);
        card.add(textWrap, BorderLayout.CENTER);
        card.add(arrow, BorderLayout.EAST);

        installCardListeners(card, new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent event) {
                card.setBackground(hoverBackground);
                card.setBorder(activeBorder);
            }

            @Override
            public void mouseExited(MouseEvent event) {
                Point onCard = SwingUtilities.convertPoint(
                        (Component) event.getSource(), event.getPoint(), card);
                if (!card.contains(onCard)) {
                    card.setBackground(background);
                    card.setBorder(restBorder);
                }
            }

            @Override
            public void mouseClicked(MouseEvent event) {
                Point onCard = SwingUtilities.convertPoint(
                        (Component) event.getSource(), event.getPoint(), card);
                if (card.contains(onCard) && action != null) {
                    action.run();
                }
            }
        });
        return card;
    }

    // ---------------- Buttons ----------------

    public static JButton primaryButton(String text) {
        return button(text, ACCENT, Color.WHITE, ACCENT_DARK);
    }

    public static JButton orangeButton(String text) {
        return button(text, ORANGE, Color.WHITE, new Color(0xF59E00));
    }

    public static JButton secondaryButton(String text) {
        return button(text, ACCENT_LIGHT, ACCENT_DARK, blend(ACCENT_LIGHT, ACCENT, 0.22f));
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

    // ---------------- Landing-page action cards ----------------

    /**
     * Clickable action card in the style of a modern landing page: a small
     * badge, a bold title, a description and an arrow, with a hover highlight
     * (background + border darken, hand cursor).
     *
     * <p>Used for the two entry choices on the landing screens:
     * <b>"I lost an item"</b> and <b>"I found an item"</b>.</p>
     *
     * @param badge          short uppercase pill, e.g. {@code "LOST"}
     * @param title          card headline, e.g. {@code "I lost an item"}
     * @param description    one or two lines of supporting text
     * @param textWidth      wrap width of the description in pixels
     * @param accent         colour of the badge text and the arrow
     * @param background     card background at rest
     * @param hoverBackground card background while the pointer is over it
     * @param border         card border at rest
     * @param hoverBorder    card border while the pointer is over it
     * @param action         runs when the card is clicked
     */
    public static JPanel actionCard(String badge, String title, String description,
                                    int textWidth, Color accent, Color background,
                                    Color hoverBackground, Color border, Color hoverBorder,
                                    Runnable action) {
        return actionCard(badge, title, description, textWidth, accent, background,
                hoverBackground, border, hoverBorder, action, false);
    }

    /**
     * Compact variant: when {@code compact} is true the card collapses into a
     * single-row tile (badge + title + arrow, description not rendered) for
     * landing pages that need small boxes.
     */
    public static JPanel actionCard(String badge, String title, String description,
                                    int textWidth, Color accent, Color background,
                                    Color hoverBackground, Color border, Color hoverBorder,
                                    final Runnable action, boolean compact) {
        if (compact) {
            return actionTile(badge, title, accent, background, hoverBackground,
                    border, hoverBorder, action);
        }
        final JPanel card = new JPanel(new BorderLayout(0, 6));
        final Border restBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(border),
                BorderFactory.createEmptyBorder(11, 13, 11, 13));
        final Border activeBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(hoverBorder),
                BorderFactory.createEmptyBorder(11, 13, 11, 13));

        card.setBackground(background);
        card.setBorder(restBorder);
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // --- top row: badge pill + arrow --------------------------------
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);

        JLabel badgeLabel = new JLabel(badge);
        badgeLabel.setFont(new Font(FAMILY, Font.BOLD, 10));
        badgeLabel.setForeground(accent);
        badgeLabel.setOpaque(true);
        badgeLabel.setBackground(CARD);
        badgeLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(border),
                BorderFactory.createEmptyBorder(2, 8, 2, 8)));

        JLabel arrow = new JLabel("\u2192");   // rightwards arrow
        arrow.setFont(new Font(FAMILY, Font.BOLD, 17));
        arrow.setForeground(accent);

        topRow.add(badgeLabel, BorderLayout.WEST);
        topRow.add(arrow, BorderLayout.EAST);
        card.add(topRow, BorderLayout.NORTH);

        // --- texts -------------------------------------------------------
        JPanel texts = new JPanel();
        texts.setOpaque(false);
        texts.setLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(FONT_BOLD.deriveFont(15f));
        titleLabel.setForeground(TEXT);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel descLabel = new JLabel("<html><body style='width:" + textWidth + "px'>"
                + description + "</body></html>");
        descLabel.setFont(FONT_SMALL);
        descLabel.setForeground(MUTED);
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        texts.add(titleLabel);
        texts.add(Box.createVerticalStrut(3));
        texts.add(descLabel);
        card.add(texts, BorderLayout.CENTER);

        // --- hover + click ------------------------------------------------
        MouseAdapter adapter = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent event) {
                card.setBackground(hoverBackground);
                card.setBorder(activeBorder);
            }

            @Override
            public void mouseExited(MouseEvent event) {
                // Events also fire when the pointer moves between child
                // components: only reset once the pointer really left the card.
                Point onCard = SwingUtilities.convertPoint(
                        (Component) event.getSource(), event.getPoint(), card);
                if (!card.contains(onCard)) {
                    card.setBackground(background);
                    card.setBorder(restBorder);
                }
            }

            @Override
            public void mouseClicked(MouseEvent event) {
                Point onCard = SwingUtilities.convertPoint(
                        (Component) event.getSource(), event.getPoint(), card);
                if (card.contains(onCard) && action != null) {
                    action.run();
                }
            }
        };
        installCardListeners(card, adapter);
        return card;
    }

    /**
     * One-row landing-page tile: badge pill, title and arrow in a single line
     * with tight padding, same hover/click behaviour as {@link #actionCard}.
     */
    private static JPanel actionTile(String badge, String title, Color accent,
                                     final Color background, Color hoverBackground,
                                     Color border, Color hoverBorder,
                                     final Runnable action) {
        final JPanel tile = new JPanel(new BorderLayout(8, 0));
        final Border restBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(border),
                BorderFactory.createEmptyBorder(5, 9, 5, 9));
        final Border activeBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(hoverBorder),
                BorderFactory.createEmptyBorder(5, 9, 5, 9));

        tile.setBackground(background);
        tile.setBorder(restBorder);
        tile.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel badgeLabel = new JLabel(badge);
        badgeLabel.setFont(new Font(FAMILY, Font.BOLD, 9));
        badgeLabel.setForeground(accent);
        badgeLabel.setOpaque(true);
        badgeLabel.setBackground(CARD);
        badgeLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(border),
                BorderFactory.createEmptyBorder(1, 6, 1, 6)));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(FONT_BOLD);
        titleLabel.setForeground(TEXT);

        JLabel arrow = new JLabel("\u2192");
        arrow.setFont(new Font(FAMILY, Font.BOLD, 13));
        arrow.setForeground(accent);

        tile.add(badgeLabel, BorderLayout.WEST);
        tile.add(titleLabel, BorderLayout.CENTER);
        tile.add(arrow, BorderLayout.EAST);

        MouseAdapter adapter = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent event) {
                tile.setBackground(hoverBackground);
                tile.setBorder(activeBorder);
            }

            @Override
            public void mouseExited(MouseEvent event) {
                Point onTile = SwingUtilities.convertPoint(
                        (Component) event.getSource(), event.getPoint(), tile);
                if (!tile.contains(onTile)) {
                    tile.setBackground(background);
                    tile.setBorder(restBorder);
                }
            }

            @Override
            public void mouseClicked(MouseEvent event) {
                Point onTile = SwingUtilities.convertPoint(
                        (Component) event.getSource(), event.getPoint(), tile);
                if (tile.contains(onTile) && action != null) {
                    action.run();
                }
            }
        };
        installCardListeners(tile, adapter);
        // BoxLayout grows components up to their maximum size; without this cap
        // the tile would stretch across the whole dashboard row.
        tile.setMaximumSize(tile.getPreferredSize());
        return tile;
    }

    /**
     * Mouse events do not bubble in Swing, so the adapter is attached to the
     * card and to every component inside it — otherwise hovering over a label
     * would stop highlighting the card.
     */
    private static void installCardListeners(Component component, MouseAdapter adapter) {
        component.addMouseListener(adapter);
        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                installCardListeners(child, adapter);
            }
        }
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

    public static void styleSpinner(JSpinner spinner) {
        spinner.setFont(FONT);
        spinner.setBackground(CARD);
        spinner.setForeground(TEXT);
        spinner.setBorder(inputBorder());
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JTextField field = ((JSpinner.DefaultEditor) editor).getTextField();
            field.setFont(FONT);
            field.setForeground(TEXT);
            field.setBackground(CARD);
            field.setCaretColor(TEXT);
            field.setBorder(BorderFactory.createEmptyBorder(5, 6, 5, 6));
        }
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
        header.setBackground(blend(CARD, TEXT, 0.04f));
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
        // Cap the height so a short form does not stretch its first field to
        // fill all the spare room in a BoxLayout.
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                panel.getPreferredSize().height));
        return panel;
    }

    /** Flat panel that paints a rounded FINDIT-style card background. */
    private static final class RoundedPanel extends JPanel {

        private static final long serialVersionUID = 1L;

        private final int arc;
        private final Color borderColor;

        RoundedPanel(Color fill, Color borderColor, int arc) {
            this.arc = arc;
            this.borderColor = borderColor;
            setOpaque(false);
            setBackground(fill);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
            if (borderColor != null) {
                g2.setColor(borderColor);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
            }
            g2.dispose();
            super.paintComponent(graphics);
        }
    }
}
