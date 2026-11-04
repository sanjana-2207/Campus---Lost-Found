package com.campus.lostfound.ui;

import com.campus.lostfound.exception.ValidationException;
import com.campus.lostfound.model.Role;
import com.campus.lostfound.service.AuthService;
import com.campus.lostfound.util.Async;
import com.campus.lostfound.util.SessionContext;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
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
    private JLabel registerMessage;

    public LoginFrame() {
        setTitle("REC Lost & Found - Sign in");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1040, 700);
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
        formHolder.setBackground(UIStyle.BG);

        JPanel loginCard = new JPanel();
        loginCard.setBackground(UIStyle.BG);
        loginCard.setBorder(BorderFactory.createEmptyBorder(46, 56, 40, 56));
        loginCard.setLayout(new BoxLayout(loginCard, BoxLayout.Y_AXIS));

        JLabel logo = new JLabel("REC Lost & Found");
        logo.setFont(new Font("Arial", Font.BOLD, 24));
        logo.setForeground(UIStyle.ACCENT_DARK);
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginCard.add(logo);
        loginCard.add(Box.createVerticalStrut(14));

        JLabel heading = UIStyle.heading("Sign In");
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginCard.add(heading);
        loginCard.add(Box.createVerticalStrut(6));

        JLabel subtitle = UIStyle.subtitle("Login to your REC Lost & Found account.");
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
        card.setBackground(UIStyle.BG);
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

        JLabel roleNote = new JLabel("<html><b>Student account.</b> Staff accounts are "
                + "issued by the lost-and-found desk.</html>");
        roleNote.setFont(UIStyle.FONT_SMALL);
        roleNote.setForeground(UIStyle.MUTED);
        roleNote.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(roleNote);
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
        loginMessage.setText("Signing in...");
        final String email = loginEmail.getText();
        final String password = new String(loginPassword.getPassword());

        Async.run(() -> authService.login(email, password), user -> {
            loginMessage.setText(" ");
            SessionContext.login(user);
            MainFrame mainFrame = new MainFrame();
            mainFrame.setVisible(true);
            dispose();
        }, error -> {
            if (error instanceof ValidationException) {
                loginMessage.setText(error.getMessage());
            } else {
                loginMessage.setText(" ");
                Async.showError(this, error, "Connection problem");
            }
        });
    }

    private void doRegister() {
        registerMessage.setText("Creating account...");
        final String name = registerName.getText();
        final String email = registerEmail.getText();
        final String phone = registerPhone.getText();
        final String password = new String(registerPassword.getPassword());
        final String confirm = new String(registerConfirm.getPassword());

        Async.run(() -> authService.register(name, email, phone, password, confirm,
                Role.STUDENT), user -> {
            registerMessage.setText(" ");
            loginEmail.setText(user.getEmail());
            loginPassword.setText("");
            loginMessage.setText(" ");
            formCards.show(formHolder, "login");

            JOptionPane.showMessageDialog(this,
                    "Account created. You can now sign in with your e-mail and password.",
                    "Registration successful", JOptionPane.INFORMATION_MESSAGE);
        }, error -> {
            if (error instanceof ValidationException) {
                registerMessage.setText(error.getMessage());
            } else {
                registerMessage.setText(" ");
                Async.showError(this, error, "Connection problem");
            }
        });
    }

    // ==================================================================
    //  Brand panel (custom painted) Ã¢â‚¬â€ also the landing-screen shortcuts
    // ==================================================================

    private class BrandPanel extends JPanel {

        private static final long serialVersionUID = 1L;

        BrandPanel() {
            setPreferredSize(new Dimension(410, 0));
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(BorderFactory.createEmptyBorder(48, 44, 40, 44));

            JLabel mark = new JLabel("REC Lost & Found");
            mark.setFont(new Font("Arial", Font.BOLD, 16));
            mark.setForeground(new Color(0xFFD98A));
            mark.setAlignmentX(Component.LEFT_ALIGNMENT);
            add(mark);
            add(Box.createVerticalStrut(16));

            JLabel title = new JLabel("<html>Welcome Back to<br>"
                    + "<span style='color:#FFAE00'>REC Lost &amp; Found</span></html>");
            title.setFont(new Font("Arial", Font.BOLD, 30));
            title.setForeground(Color.WHITE);
            title.setAlignmentX(Component.LEFT_ALIGNMENT);
            add(title);
            add(Box.createVerticalStrut(14));

            JLabel tagline = new JLabel("<html><body style='width:240px'>"
                    + "Sign in to find your lost items, report found belongings "
                    + "and help your campus community.</body></html>");
            tagline.setFont(new Font("Arial", Font.PLAIN, 14));
            tagline.setForeground(new Color(0xF4EAFF));
            tagline.setAlignmentX(Component.LEFT_ALIGNMENT);
            add(tagline);
            add(Box.createVerticalStrut(20));

            add(benefit("\uD83D\uDD0D", "Find your lost items"));
            add(Box.createVerticalStrut(8));
            add(benefit("\uD83D\uDCE6", "Report found items"));
            add(Box.createVerticalStrut(8));
            add(benefit("\uD83E\uDD1D", "Connect with your campus"));

            add(Box.createVerticalGlue());

            JLabel footer = new JLabel("REC Lost & Found");
            footer.setFont(new Font("Arial", Font.BOLD, 12));
            footer.setForeground(new Color(0xE9DAF8));
            footer.setAlignmentX(Component.LEFT_ALIGNMENT);
            add(footer);
        }

        private JComponent benefit(String icon, String text) {
            JPanel panel = new JPanel(new BorderLayout(12, 0));
            panel.setBackground(new Color(255, 255, 255, 34));
            panel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(255, 255, 255, 24)),
                    BorderFactory.createEmptyBorder(11, 14, 11, 14)));
            panel.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

            JLabel iconLabel = new JLabel(icon);
            iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 17));
            iconLabel.setForeground(new Color(0xFFD98A));
            iconLabel.setPreferredSize(new Dimension(26, 20));

            JLabel textLabel = new JLabel(text);
            textLabel.setFont(new Font("Arial", Font.PLAIN, 13));
            textLabel.setForeground(Color.WHITE);

            panel.add(iconLabel, BorderLayout.WEST);
            panel.add(textLabel, BorderLayout.CENTER);
            return panel;
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setPaint(new GradientPaint(0, 0, new Color(0x5420AD),
                    getWidth(), getHeight(), new Color(0x8730D1)));
            g2.fillRect(0, 0, getWidth(), getHeight());

            g2.setColor(new Color(255, 255, 255, 16));
            g2.fillOval(-120, getHeight() - 260, 420, 420);
            g2.setColor(new Color(255, 255, 255, 12));
            g2.fillOval(getWidth() - 150, -120, 320, 320);
            g2.dispose();
        }
    }
}
