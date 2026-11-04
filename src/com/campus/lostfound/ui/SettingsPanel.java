package com.campus.lostfound.ui;

import com.campus.lostfound.exception.ValidationException;
import com.campus.lostfound.model.User;
import com.campus.lostfound.service.AuthService;
import com.campus.lostfound.util.SessionContext;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Settings screen - a direct port of the FINDIT settings page
 * (finditcampus.com/settings.html): page heading with a back link, a Profile
 * Information card, a Change Password card and an Account card, using the same
 * copy, form layout and purple accent colours as the original page.
 */
public class SettingsPanel extends JPanel implements Refreshable {

    private static final long serialVersionUID = 1L;

    // ---------------- theme-aware settings palette ----------------
    private static Color PAGE_BG;
    private static Color PURPLE;
    private static Color PURPLE_DARK;
    private static Color PURPLE_LIGHT;
    private static Color PURPLE_GRADIENT_END;
    private static Color FOCUS_BORDER;
    private static Color INPUT_BORDER;
    private static Color HEADING_GRAY;
    private static Color CARD_LINE;
    private static Color RESET_TEXT;
    private static Color RESET_LINE;
    private static Color RESET_HOVER;
    private static Color LOGOUT_BG;
    private static Color LOGOUT_TEXT;
    private static Color LOGOUT_HOVER;
    private static Color CARD_BG;
    private static Color CARD_SHADOW;

    private static void applyThemePalette() {
        PAGE_BG = UIStyle.BG;
        PURPLE = UIStyle.ACCENT;
        PURPLE_DARK = UIStyle.ACCENT_DARK;
        PURPLE_LIGHT = UIStyle.ACCENT_LIGHT;
        PURPLE_GRADIENT_END = UIStyle.ACCENT;
        FOCUS_BORDER = UIStyle.ACCENT;
        INPUT_BORDER = UIStyle.BORDER;
        HEADING_GRAY = UIStyle.MUTED;
        CARD_LINE = UIStyle.BORDER;
        RESET_TEXT = UIStyle.TEXT;
        RESET_LINE = UIStyle.BORDER;
        RESET_HOVER = UIStyle.ACCENT_LIGHT;
        LOGOUT_BG = UIStyle.blend(UIStyle.CARD, UIStyle.DANGER, 0.12f);
        LOGOUT_TEXT = UIStyle.DANGER;
        LOGOUT_HOVER = UIStyle.blend(UIStyle.CARD, UIStyle.DANGER, 0.22f);
        CARD_BG = UIStyle.CARD;
        CARD_SHADOW = UIStyle.isDark()
                ? new Color(0x0B0614) : new Color(0xE7E3F1);
    }

    private static final Font H1 = new Font("Segoe UI", Font.BOLD, 30);
    private static final Font H2 = new Font("Segoe UI", Font.BOLD, 21);
    private static final Font BODY = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font LABEL_FONT = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font INPUT_FONT = new Font("Segoe UI", Font.PLAIN, 15);
    private static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 15);

    private static final int INPUT_HEIGHT = 40;
    private static final int CARD_RADIUS = 18;
    private static final int CARD_MAX_WIDTH = 1000;

    private static final String CAMPUS_NAME = "Campus College of Engineering and Technology";

    private final AuthService authService;
    private final Runnable onBack;
    private final Runnable onProfileSaved;
    private final Runnable onLogout;

    // Profile form
    private final JTextField nameField = inputField();
    private final JTextField emailField = inputField();
    private final JTextField phoneField = inputField();
    private final JComboBox<String> genderBox = genderBox();
    private final JTextField studentIdField = readonlyField();
    private final JTextField campusField = readonlyField();
    private final JTextField roleField = readonlyField();
    private final JPanel roleGroup;

    // Password form
    private final JPasswordField currentPwField = passwordField();
    private final JPasswordField newPasswordField = passwordField();
    private final JPasswordField confirmPasswordField = passwordField();

    public SettingsPanel(AuthService authService, Runnable onBack,
                         Runnable onProfileSaved, Runnable onLogout) {
        applyThemePalette();
        this.authService = authService;
        this.onBack = onBack;
        this.onProfileSaved = onProfileSaved;
        this.onLogout = onLogout;

        setLayout(new BorderLayout());
        setBackground(PAGE_BG);

        campusField.setText(CAMPUS_NAME);
        roleGroup = formGroup("Account Role", roleField);
        roleGroup.setVisible(SessionContext.isAdmin());

        JPanel page = new JPanel();
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.setBackground(PAGE_BG);
        page.setBorder(BorderFactory.createEmptyBorder(30, 25, 40, 25));

        page.add(buildPageHeading());
        page.add(Box.createVerticalStrut(30));
        page.add(buildProfileCard());
        page.add(Box.createVerticalStrut(25));
        page.add(buildPasswordCard());
        page.add(Box.createVerticalStrut(25));
        page.add(buildAccountCard());

        JScrollPane scroll = new JScrollPane(page);
        scroll.setBorder(null);
        scroll.setBackground(PAGE_BG);
        scroll.getViewport().setBackground(PAGE_BG);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scroll, BorderLayout.CENTER);
    }

    // ==================================================================
    //  Page heading
    // ==================================================================

    private JPanel buildPageHeading() {
        JPanel heading = new JPanel();
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        heading.setOpaque(false);

        JLabel title = new JLabel("Settings");
        title.setFont(H1);
        title.setForeground(UIStyle.TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel(
                "Manage your REC Lost & Found account and profile information.");
        subtitle.setFont(BODY);
        subtitle.setForeground(HEADING_GRAY);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        heading.add(title);
        heading.add(Box.createVerticalStrut(8));
        heading.add(subtitle);
        heading.add(Box.createVerticalStrut(8));
        heading.add(UIStyle.backLink(onBack));

        stretch(heading);
        return heading;
    }

    // ==================================================================
    //  Card 1 - Profile Information
    // ==================================================================

    private JPanel buildProfileCard() {
        JPanel card = roundedCard(null);

        card.add(cardHeading("\uD83D\uDC64", "Profile Information",
                "Update your personal information."));
        card.add(Box.createVerticalStrut(25));

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);

        form.add(formRow(
                formGroup("Full Name", nameField),
                formGroup("Email Address", emailField)));
        form.add(Box.createVerticalStrut(20));
        form.add(formRow(
                formGroup("Phone Number", phoneField),
                formGroup("Gender", genderBox)));
        form.add(Box.createVerticalStrut(20));
        form.add(formGroup("Student ID", studentIdField));
        form.add(Box.createVerticalStrut(20));
        form.add(formGroup("Campus / College", campusField));
        form.add(Box.createVerticalStrut(20));
        form.add(roleGroup);
        form.add(Box.createVerticalStrut(15));

        JPanel buttonRow = new JPanel();
        buttonRow.setLayout(new BoxLayout(buttonRow, BoxLayout.X_AXIS));
        buttonRow.setOpaque(false);

        JButton save = new GradientButton("Save Profile");
        save.addActionListener(event -> saveProfile());
        JButton reset = new FlatButton("Reset", CARD_BG, RESET_TEXT,
                RESET_HOVER, RESET_LINE);
        reset.addActionListener(event -> refreshData());

        buttonRow.add(save);
        buttonRow.add(Box.createHorizontalStrut(12));
        buttonRow.add(reset);
        stretch(buttonRow);

        form.add(buttonRow);
        stretch(form);
        card.add(form);

        finishCard(card);
        return card;
    }

    // ==================================================================
    //  Card 2 - Change Password
    // ==================================================================

    private JPanel buildPasswordCard() {
        JPanel card = roundedCard(null);

        card.add(cardHeading("\uD83D\uDD10", "Change Password",
                "Update your account password securely."));
        card.add(Box.createVerticalStrut(25));

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);

        form.add(formGroup("Current Password", currentPwField));
        form.add(Box.createVerticalStrut(20));
        form.add(formRow(
                formGroup("New Password", newPasswordField),
                formGroup("Confirm New Password", confirmPasswordField)));
        form.add(Box.createVerticalStrut(20));

        JButton change = new FlatButton("Change Password",
                PURPLE, Color.WHITE, PURPLE_DARK, null);
        change.addActionListener(event -> changePassword());
        stretch(change);
        form.add(change);

        stretch(form);
        card.add(form);

        finishCard(card);
        return card;
    }

    // ==================================================================
    //  Card 3 - Account
    // ==================================================================

    private JPanel buildAccountCard() {
        JPanel card = roundedCard(CARD_LINE);

        card.add(cardHeading("\u2699\uFE0F", "Account",
                "Manage your REC Lost & Found account."));
        card.add(Box.createVerticalStrut(25));

        JPanel actions = new JPanel(new BorderLayout());
        actions.setOpaque(false);

        JButton logout = new FlatButton("Logout",
                LOGOUT_BG, LOGOUT_TEXT, LOGOUT_HOVER, null);
        logout.addActionListener(event -> onLogout.run());

        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.X_AXIS));
        right.setOpaque(false);
        right.add(logout);

        actions.add(right, BorderLayout.EAST);
        stretch(actions);
        card.add(actions);

        finishCard(card);
        return card;
    }

    // ==================================================================
    //  Actions
    // ==================================================================

    private void saveProfile() {
        User user = SessionContext.getCurrentUser();
        if (user == null) {
            return;
        }
        String gender = genderBox.getSelectedIndex() <= 0
                ? null : (String) genderBox.getSelectedItem();
        try {
            authService.updateProfile(user, nameField.getText(),
                    emailField.getText(), phoneField.getText(), gender);
            JOptionPane.showMessageDialog(this, "Your profile has been saved.",
                    "Settings", JOptionPane.INFORMATION_MESSAGE);
            onProfileSaved.run();
        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Could not save profile", JOptionPane.ERROR_MESSAGE);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Database error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void changePassword() {
        User user = SessionContext.getCurrentUser();
        if (user == null) {
            return;
        }
        try {
            authService.changePassword(user.getUserId(),
                    new String(currentPwField.getPassword()),
                    new String(newPasswordField.getPassword()),
                    new String(confirmPasswordField.getPassword()));
            JOptionPane.showMessageDialog(this, "Your password has been changed.",
                    "Change Password", JOptionPane.INFORMATION_MESSAGE);
            currentPwField.setText("");
            newPasswordField.setText("");
            confirmPasswordField.setText("");
        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Could not change password", JOptionPane.ERROR_MESSAGE);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Database error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Reloads every field from the signed-in user (called on each visit). */
    @Override
    public void refreshData() {
        User user = SessionContext.getCurrentUser();
        if (user == null) {
            return;
        }
        nameField.setText(nullToEmpty(user.getFullName()));
        emailField.setText(nullToEmpty(user.getEmail()));
        phoneField.setText(nullToEmpty(user.getPhone()));
        studentIdField.setText(nullToEmpty(user.getStudentId()));
        roleField.setText(user.getRole().getLabel());

        genderBox.setSelectedIndex(0);
        if (user.getGender() != null) {
            for (int i = 0; i < genderBox.getItemCount(); i++) {
                if (user.getGender().equals(genderBox.getItemAt(i))) {
                    genderBox.setSelectedIndex(i);
                    break;
                }
            }
        }

        currentPwField.setText("");
        newPasswordField.setText("");
        confirmPasswordField.setText("");
    }

    // ==================================================================
    //  Layout helpers
    // ==================================================================

    /** White rounded card (18px radius + soft shadow), FINDIT settings-card. */
    private static JPanel roundedCard(final Color stroke) {
        JPanel panel = new JPanel() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                double arc = CARD_RADIUS * 2.0;
                // Soft drop shadow (0 7px 25px rgba(0,0,0,0.06) approximation).
                g2.setColor(CARD_SHADOW);
                g2.fill(new RoundRectangle2D.Double(0, 4, w, h, arc, arc));
                g2.setColor(CARD_BG);
                g2.fill(new RoundRectangle2D.Double(0, 0, w, h, arc, arc));
                if (stroke != null) {
                    g2.setColor(stroke);
                    g2.setStroke(new java.awt.BasicStroke(1f));
                    g2.draw(new RoundRectangle2D.Double(0.5, 0.5, w - 1, h - 1,
                            arc, arc));
                }
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);
        return panel;
    }

    /** Icon + title + description row with the bottom divider (card-heading). */
    private JPanel cardHeading(String emoji, String title, String description) {
        JPanel head = new JPanel(new BorderLayout(16, 0));
        head.setOpaque(false);

        JPanel icon = new JPanel(new BorderLayout()) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(PURPLE_LIGHT);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(),
                        28, 28));
                g2.dispose();
            }
        };
        icon.setOpaque(false);
        icon.setPreferredSize(new Dimension(52, 52));
        icon.setMaximumSize(new Dimension(52, 52));

        JLabel iconLabel = new JLabel(emoji);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        iconLabel.setForeground(PURPLE_DARK);
        iconLabel.setHorizontalAlignment(JLabel.CENTER);
        icon.add(iconLabel, BorderLayout.CENTER);

        JPanel texts = new JPanel();
        texts.setLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));
        texts.setOpaque(false);

        JLabel h2 = new JLabel(title);
        h2.setFont(H2);
        h2.setForeground(UIStyle.TEXT);
        h2.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel p = new JLabel(description);
        p.setFont(BODY);
        p.setForeground(HEADING_GRAY);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);

        texts.add(h2);
        texts.add(Box.createVerticalStrut(5));
        texts.add(p);

        head.add(icon, BorderLayout.WEST);
        head.add(texts, BorderLayout.CENTER);
        head.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, CARD_LINE),
                BorderFactory.createEmptyBorder(0, 0, 22, 0)));
        stretch(head);
        return head;
    }

    private JPanel formGroup(String label, JComponent field) {
        JPanel group = new JPanel(new BorderLayout(0, 8));
        group.setOpaque(false);

        JLabel caption = new JLabel(label);
        caption.setFont(LABEL_FONT);
        caption.setForeground(UIStyle.TEXT);

        group.add(caption, BorderLayout.NORTH);
        group.add(field, BorderLayout.CENTER);
        return group;
    }

    private JPanel formRow(JComponent left, JComponent right) {
        JPanel row = new JPanel(new GridLayout(1, 2, 20, 0));
        row.setOpaque(false);
        row.add(left);
        row.add(right);
        return row;
    }

    /** Prevents BoxLayout from growing the component past its natural height. */
    private static void stretch(JComponent component) {
        component.setAlignmentX(Component.LEFT_ALIGNMENT);
        component.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                component.getPreferredSize().height));
    }

    /** Caps a card at the FINDIT container width and keeps it at natural height. */
    private static void finishCard(JPanel card) {
        card.setMaximumSize(new Dimension(CARD_MAX_WIDTH,
                card.getPreferredSize().height));
    }

    // ==================================================================
    //  Input helpers (FINDIT form styles)
    // ==================================================================

    private static JTextField inputField() {
        JTextField field = new JTextField();
        field.setFont(INPUT_FONT);
        field.setForeground(UIStyle.TEXT);
        field.setBackground(UIStyle.CARD);
        field.setBorder(roundedBorder(UIStyle.BORDER));
        field.setPreferredSize(new Dimension(
                field.getPreferredSize().width, INPUT_HEIGHT));
        installFocusBorder(field);
        return field;
    }

    private static JTextField readonlyField() {
        JTextField field = inputField();
        field.setEditable(false);
        field.setBackground(UIStyle.blend(UIStyle.CARD, UIStyle.TEXT, 0.08f));
        field.setForeground(UIStyle.MUTED);
        return field;
    }

    private static JPasswordField passwordField() {
        JPasswordField field = new JPasswordField();
        field.setFont(INPUT_FONT);
        field.setForeground(UIStyle.TEXT);
        field.setBackground(UIStyle.CARD);
        field.setBorder(roundedBorder(UIStyle.BORDER));
        field.setPreferredSize(new Dimension(
                field.getPreferredSize().width, INPUT_HEIGHT));
        installFocusBorder(field);
        return field;
    }

    private static JComboBox<String> genderBox() {
        JComboBox<String> box = new JComboBox<String>(new String[]{
                "Select Gender", "Male", "Female", "Other",
                "Prefer not to say"});
        box.setFont(INPUT_FONT);
        box.setBackground(UIStyle.CARD);
        box.setForeground(UIStyle.TEXT);
        box.setBorder(roundedBorder(UIStyle.BORDER));
        box.setPreferredSize(new Dimension(
                box.getPreferredSize().width, INPUT_HEIGHT));
        installFocusBorder(box);
        return box;
    }

    /** Input focus ring: border turns #7c3aed while the field has focus. */
    private static void installFocusBorder(final JComponent component) {
        component.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent event) {
                component.setBorder(roundedBorder(FOCUS_BORDER));
            }

            @Override
            public void focusLost(FocusEvent event) {
                component.setBorder(roundedBorder(INPUT_BORDER));
            }
        });
    }

    /** 10px-radius outline, the FINDIT input/select border. */
    private static Border roundedBorder(final Color color) {
        return new AbstractBorder() {
            private static final long serialVersionUID = 1L;

            @Override
            public void paintBorder(Component c, Graphics g, int x, int y,
                                    int width, int height) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.setStroke(new java.awt.BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Double(x + 0.5, y + 0.5,
                        width - 1.0, height - 1.0, 20, 20));
                g2.dispose();
            }

            @Override
            public Insets getBorderInsets(Component c) {
                return new Insets(6, 12, 6, 12);
            }
        };
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    // ==================================================================
    //  Buttons (FINDIT save/password/logout styles)
    // ==================================================================

    /** Solid rounded button with a hover tint (reset / password / logout). */
    private static final class FlatButton extends JButton {
        private static final long serialVersionUID = 1L;

        private final Color rest;
        private final Color hover;
        private final Color line;

        FlatButton(String text, Color background, Color foreground,
                   Color hoverBackground, Color lineColor) {
            super(text);
            this.rest = background;
            this.hover = hoverBackground;
            this.line = lineColor;
            setBackground(background);
            setForeground(foreground);
            setFont(BUTTON_FONT);
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setBorder(BorderFactory.createEmptyBorder(13, 24, 13, 24));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent event) {
                    setBackground(hover);
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent event) {
                    setBackground(rest);
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground() == null ? rest : getBackground());
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(),
                    20, 20));
            if (line != null) {
                g2.setColor(line);
                g2.setStroke(new java.awt.BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Double(0.5, 0.5, getWidth() - 1,
                        getHeight() - 1, 20, 20));
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Gradient save button (linear-gradient 135deg #6d28d9 -> #9333ea). */
    private static final class GradientButton extends JButton {
        private static final long serialVersionUID = 1L;

        private final Color start;
        private final Color end;
        private final Color hoverStart;
        private final Color hoverEnd;
        private boolean hovered;

        GradientButton(String text) {
            this(text, PURPLE, PURPLE_GRADIENT_END);
        }

        GradientButton(String text, Color start, Color end) {
            super(text);
            this.start = start;
            this.end = end;
            this.hoverStart = start.darker();
            this.hoverEnd = end.darker();
            setForeground(Color.WHITE);
            setFont(BUTTON_FONT);
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setBorder(BorderFactory.createEmptyBorder(13, 24, 13, 24));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent event) {
                    hovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent event) {
                    hovered = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            Color from = hovered ? hoverStart : start;
            Color to = hovered ? hoverEnd : end;
            g2.setPaint(new GradientPaint(0, 0, from, getWidth(), getHeight(), to));
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(),
                    20, 20));
            g2.dispose();
            super.paintComponent(g);
        }
    }

}
