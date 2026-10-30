package com.campus.lostfound.ui;

import com.campus.lostfound.exception.ValidationException;
import com.campus.lostfound.model.Role;
import com.campus.lostfound.model.User;
import com.campus.lostfound.service.AuthService;
import com.campus.lostfound.util.SessionContext;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Login and registration screen.
 *
 * <p>Uses a {@link CardLayout} so the sign-in form and the registration form
 * share the same window without opening dialogs.</p>
 */
public class LoginFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private final AuthService authService = new AuthService();

    private final CardLayout formCards = new CardLayout();
    private final JPanel formHolder = new JPanel(formCards);

    // Sign in
    private JTextField loginEmail;
    private JPasswordField loginPassword;
    private JLabel loginMessage;

    // Register
    private JTextField registerName;
    private JTextField registerEmail;
    private JTextField registerPhone;
    private JPasswordField registerPassword;
    private JPasswordField registerConfirm;
    private JComboBox<Role> registerRole;
    private JLabel registerMessage;

    public LoginFrame() {
        setTitle("Campus Lost & Found - Sign in");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(960, 660);
        setLocationRelativeTo(null);
        setResizable(false);

        setLayout(new BorderLayout());
        add(new BrandPanel(), BorderLayout.WEST);
        add(buildFormArea(), BorderLayout.CENTER);
    }

    // ==================================================================
    //  Layout
    // ==================================================================

    private JComponent buildFormArea() {
        formHolder.setBackground(UIStyle.CARD);

        JPanel loginCard = new JPanel();
        loginCard.setBackground(UIStyle.CARD);
        loginCard.setBorder(BorderFactory.createEmptyBorder(46, 56, 40, 56));
        loginCard.setLayout(new BoxLayout(loginCard, BoxLayout.Y_AXIS));

        JLabel heading = UIStyle.heading("Welcome back");
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginCard.add(heading);
        loginCard.add(Box.createVerticalStrut(6));

        JLabel subtitle = UIStyle.subtitle(
                "Sign in to report, search, match and recover campus items.");
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginCard.add(subtitle);
        loginCard.add(Box.createVerticalStrut(26));

        loginEmail = UIStyle.textField();
        loginEmail.setText("arjun@campus.edu");
        loginCard.add(field("E-mail address", loginEmail));
        loginCard.add(Box.createVerticalStrut(14));

        loginPassword = new JPasswordField();
        UIStyle.styleInput(loginPassword);
        loginPassword.setText("student123");
        loginPassword.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                doLogin();
            }
        });
        loginCard.add(field("Password", loginPassword));
        loginCard.add(Box.createVerticalStrut(10));

        loginMessage = messageLabel();
        loginCard.add(loginMessage);
        loginCard.add(Box.createVerticalStrut(8));

        JButton signIn = UIStyle.primaryButton("Sign in");
        signIn.setAlignmentX(Component.LEFT_ALIGNMENT);
        signIn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        signIn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                doLogin();
            }
        });
        loginCard.add(signIn);
        loginCard.add(Box.createVerticalStrut(16));

        JButton toRegister = linkButton("New here? Create an account");
        toRegister.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                formCards.show(formHolder, "register");
            }
        });
        loginCard.add(toRegister);
        loginCard.add(Box.createVerticalStrut(22));

        JLabel hint = new JLabel("<html><b>Demo accounts</b> &nbsp; "
                + "student: arjun@campus.edu / student123 &nbsp;|&nbsp; "
                + "staff: security@campus.edu / staff123</html>");
        hint.setFont(UIStyle.FONT_SMALL);
        hint.setForeground(UIStyle.MUTED);
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginCard.add(hint);

        formHolder.add(loginCard, "login");
        formHolder.add(buildRegisterCard(), "register");
        formCards.show(formHolder, "login");
        return formHolder;
    }

    private JComponent buildRegisterCard() {
        JPanel card = new JPanel();
        card.setBackground(UIStyle.CARD);
        card.setBorder(BorderFactory.createEmptyBorder(34, 56, 30, 56));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel heading = UIStyle.heading("Create your account");
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(heading);
        card.add(Box.createVerticalStrut(4));

        JLabel subtitle = UIStyle.subtitle("One account for reporting, searching and claiming.");
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(subtitle);
        card.add(Box.createVerticalStrut(18));

        registerName = UIStyle.textField();
        card.add(field("Full name", registerName));
        card.add(Box.createVerticalStrut(10));

        registerEmail = UIStyle.textField();
        card.add(field("E-mail address", registerEmail));
        card.add(Box.createVerticalStrut(10));

        registerPhone = UIStyle.textField();
        card.add(field("Phone number", registerPhone));
        card.add(Box.createVerticalStrut(10));

        JPanel passwordRow = new JPanel(new GridLayout(1, 2, 12, 0));
        passwordRow.setOpaque(false);
        passwordRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        passwordRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));

        registerPassword = new JPasswordField();
        UIStyle.styleInput(registerPassword);
        passwordRow.add(field("Password", registerPassword));

        registerConfirm = new JPasswordField();
        UIStyle.styleInput(registerConfirm);
        registerConfirm.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                doRegister();
            }
        });
        passwordRow.add(field("Confirm password", registerConfirm));
        card.add(passwordRow);
        card.add(Box.createVerticalStrut(10));

        JPanel roleRow = new JPanel(new GridLayout(1, 2, 12, 0));
        roleRow.setOpaque(false);
        roleRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        roleRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));

        registerRole = UIStyle.comboBox();
        registerRole.addItem(Role.STUDENT);
        registerRole.addItem(Role.STAFF);
        roleRow.add(field("Account type", registerRole));

        JPanel spacer = new JPanel();
        spacer.setOpaque(false);
        roleRow.add(spacer);
        card.add(roleRow);
        card.add(Box.createVerticalStrut(10));

        registerMessage = messageLabel();
        card.add(registerMessage);
        card.add(Box.createVerticalStrut(8));

        JPanel actions = new JPanel(new GridLayout(1, 2, 12, 0));
        actions.setOpaque(false);
        actions.setAlignmentX(Component.LEFT_ALIGNMENT);
        actions.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JButton create = UIStyle.primaryButton("Create account");
        create.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                doRegister();
            }
        });
        actions.add(create);

        JButton back = UIStyle.secondaryButton("Back to sign in");
        back.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                formCards.show(formHolder, "login");
            }
        });
        actions.add(back);

        card.add(actions);
        card.add(Box.createVerticalStrut(12));

        JLabel note = new JLabel("Administrator accounts are created by the college IT cell.");
        note.setFont(UIStyle.FONT_SMALL);
        note.setForeground(UIStyle.MUTED);
        note.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(note);

        return card;
    }

    /** Label above a field, wrapped so BoxLayout keeps the width. */
    private JComponent field(String label, JComponent input) {
        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));
        panel.add(UIStyle.fieldLabel(label), BorderLayout.NORTH);
        panel.add(input, BorderLayout.CENTER);
        return panel;
    }

    private JLabel messageLabel() {
        JLabel label = new JLabel(" ");
        label.setFont(UIStyle.FONT_SMALL);
        label.setForeground(UIStyle.DANGER);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JButton linkButton(String text) {
        JButton button = new JButton(text);
        button.setFont(UIStyle.FONT_BOLD);
        button.setForeground(UIStyle.ACCENT);
        button.setBackground(UIStyle.CARD);
        button.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        return button;
    }

    // ==================================================================
    //  Actions
    // ==================================================================

    private void doLogin() {
        loginMessage.setText(" ");
        try {
            User user = authService.login(loginEmail.getText(),
                    new String(loginPassword.getPassword()));

            SessionContext.login(user);
            MainFrame mainFrame = new MainFrame();
            mainFrame.setVisible(true);
            dispose();

        } catch (ValidationException ex) {
            loginMessage.setText(ex.getMessage());
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Connection problem", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doRegister() {
        registerMessage.setText(" ");
        try {
            Role role = (Role) registerRole.getSelectedItem();
            User user = authService.register(
                    registerName.getText(),
                    registerEmail.getText(),
                    registerPhone.getText(),
                    new String(registerPassword.getPassword()),
                    new String(registerConfirm.getPassword()),
                    role);

            loginEmail.setText(user.getEmail());
            loginPassword.setText("");
            loginMessage.setText(" ");
            formCards.show(formHolder, "login");

            JOptionPane.showMessageDialog(this,
                    "Account created. You can now sign in with your e-mail and password.",
                    "Registration successful", JOptionPane.INFORMATION_MESSAGE);

        } catch (ValidationException ex) {
            registerMessage.setText(ex.getMessage());
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Connection problem", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ==================================================================
    //  Brand panel (custom painted)
    // ==================================================================

    private static class BrandPanel extends JPanel {

        private static final long serialVersionUID = 1L;

        BrandPanel() {
            setPreferredSize(new Dimension(410, 0));
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(BorderFactory.createEmptyBorder(64, 44, 48, 44));

            JLabel mark = new JLabel("REC");
            mark.setFont(new Font("Segoe UI", Font.BOLD, 13));
            mark.setForeground(new Color(0x8FD3DE));
            mark.setAlignmentX(Component.LEFT_ALIGNMENT);
            add(mark);
            add(Box.createVerticalStrut(18));

            JLabel title = new JLabel("<html>Campus<br>Lost &amp; Found</html>");
            title.setFont(new Font("Segoe UI", Font.BOLD, 34));
            title.setForeground(Color.WHITE);
            title.setAlignmentX(Component.LEFT_ALIGNMENT);
            add(title);
            add(Box.createVerticalStrut(14));

            JLabel tagline = new JLabel("<html>A single desk for every lost bottle,<br>"
                    + "ID card, key and calculator on campus.</html>");
            tagline.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            tagline.setForeground(new Color(0xC9D6E2));
            tagline.setAlignmentX(Component.LEFT_ALIGNMENT);
            add(tagline);
            add(Box.createVerticalStrut(30));

            add(bullet("Report lost and found items in seconds"));
            add(bullet("Search and filter by category and location"));
            add(bullet("Automatic match suggestions"));
            add(bullet("Verified ownership claims and recovery tracking"));

            add(Box.createVerticalGlue());

            JLabel footer = new JLabel("Java  ·  Swing  ·  JDBC  ·  MySQL");
            footer.setFont(new Font("Segoe UI", Font.BOLD, 12));
            footer.setForeground(new Color(0x8FD3DE));
            footer.setAlignmentX(Component.LEFT_ALIGNMENT);
            add(footer);
        }

        private JComponent bullet(String text) {
            JLabel label = new JLabel("<html><body style='width:290px'>&#9679;&nbsp; "
                    + text + "</body></html>");
            label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            label.setForeground(new Color(0xDCE6EF));
            label.setBorder(BorderFactory.createEmptyBorder(0, 0, 9, 0));
            label.setAlignmentX(Component.LEFT_ALIGNMENT);
            return label;
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setPaint(new GradientPaint(0, 0, UIStyle.NAVY,
                    0, getHeight(), UIStyle.ACCENT_DARK));
            g2.fillRect(0, 0, getWidth(), getHeight());

            g2.setColor(new Color(255, 255, 255, 16));
            g2.fillOval(-120, getHeight() - 260, 420, 420);
            g2.setColor(new Color(255, 255, 255, 12));
            g2.fillOval(getWidth() - 150, -120, 320, 320);
            g2.dispose();
        }
    }
}
