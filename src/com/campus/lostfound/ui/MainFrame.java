package com.campus.lostfound.ui;

import com.campus.lostfound.model.User;
import com.campus.lostfound.service.AuthService;
import com.campus.lostfound.service.ClaimService;
import com.campus.lostfound.service.ItemService;
import com.campus.lostfound.service.MatchService;
import com.campus.lostfound.service.ReportService;
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
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * REC Lost & Found application shell: branded top bar, a horizontal navigation
 * row and a {@link CardLayout} of screens. There is no sidebar.
 */
public class MainFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel content = new JPanel(cardLayout);
    private final Map<String, JComponent> cards = new LinkedHashMap<String, JComponent>();
    private final Deque<String> history = new ArrayDeque<String>();
    private String currentKey;
    private boolean navigatingBack;

    // Services are created once and shared by every screen (composition).
    private final ItemService itemService = new ItemService();
    private final ClaimService claimService = new ClaimService();
    private final ReportService reportService = new ReportService();
    private final MatchService matchService = new MatchService();
    private final AuthService authService = new AuthService();

    private JLabel headerUserLabel;
    private JLabel headerRoleLabel;
    private Avatar headerAvatar;
    private JLabel notificationBadge;
    private JTextField globalSearchField;

    public MainFrame() {
        setTitle("REC Lost & Found");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1360, 840);
        setMinimumSize(new Dimension(1180, 720));
        setLocationRelativeTo(null);

        content.setBackground(UIStyle.BG);
        content.setBorder(BorderFactory.createEmptyBorder(22, 26, 22, 26));

        registerScreens();

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIStyle.CARD);
        header.add(buildTopbar(), BorderLayout.NORTH);

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(UIStyle.BG);
        main.add(header, BorderLayout.NORTH);
        main.add(content, BorderLayout.CENTER);

        add(main, BorderLayout.CENTER);

        showCard("home");
    }

    // ==================================================================
    //  Screens
    // ==================================================================

    private void registerScreens() {
        registerCard("home", new DashboardPanel(reportService,
                new DashboardPanel.HomeNavigator() {
                    @Override
                    public void openReport() {
                        MainFrame.this.openReport();
                    }

                    @Override
                    public void openFoundItems() {
                        showCard("found");
                    }

                    @Override
                    public void openMatches() {
                        showCard("matches");
                    }

                    @Override
                    public void openMyReports() {
                        showCard("myreports");
                    }

                    @Override
                    public void openNotifications() {
                        showCard("notifications");
                    }

                    @Override
                    public void openClaims() {
                        showCard("claims");
                    }

                    @Override
                    public void openReports() {
                        showCard("reports");
                    }
                }));

        registerCard("report", new ReportItemPanel(itemService, backAction()));
        registerCard("found", new SearchPanel(itemService, claimService, backAction()));
        registerCard("matches", new MatchesPanel(matchService, itemService,
                claimService, backAction()));
        registerCard("myreports", new MyReportsPanel(itemService, claimService,
                new Runnable() {
                    @Override
                    public void run() {
                        MainFrame.this.openReport();
                    }
                }, backAction()));
        registerCard("notifications", new NotificationsPanel(claimService, backAction()));
        registerCard("claims", new ClaimsPanel(claimService, backAction()));
        registerCard("reports", new ReportsPanel(reportService, backAction()));
        registerCard("settings", new SettingsPanel(authService,
                backAction(),
                new Runnable() {
                    @Override
                    public void run() {
                        updateHeaderUser();
                    }
                },
                new Runnable() {
                    @Override
                    public void run() {
                        signOut();
                    }
                }));
    }

    private void registerCard(String key, JComponent panel) {
        cards.put(key, panel);
        content.add(panel, key);
    }

    // ==================================================================
    //  Header
    // ==================================================================

    private JComponent buildTopbar() {
        JPanel topbar = new JPanel(new BorderLayout(24, 0));
        topbar.setBackground(UIStyle.CARD);
        topbar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UIStyle.BORDER),
                BorderFactory.createEmptyBorder(12, 26, 12, 26)));

        topbar.add(buildBrand(), BorderLayout.WEST);

        JPanel searchWrap = new JPanel(new BorderLayout());
        searchWrap.setOpaque(false);
        searchWrap.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
        searchWrap.add(buildSearchBox(), BorderLayout.CENTER);
        topbar.add(searchWrap, BorderLayout.CENTER);

        topbar.add(buildTopbarRight(), BorderLayout.EAST);
        return topbar;
    }

    private JComponent buildBrand() {
        JPanel brand = new JPanel();
        brand.setOpaque(false);
        brand.setLayout(new BoxLayout(brand, BoxLayout.Y_AXIS));

        JLabel name = new JLabel("REC Lost & Found");
        name.setFont(new Font("Arial", Font.BOLD, 21));
        name.setForeground(UIStyle.ACCENT_DARK);
        name.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel tagline = new JLabel("Campus lost & found");
        tagline.setFont(new Font("Arial", Font.PLAIN, 10));
        tagline.setForeground(UIStyle.MUTED);
        tagline.setAlignmentX(Component.LEFT_ALIGNMENT);

        brand.add(name);
        brand.add(Box.createVerticalStrut(1));
        brand.add(tagline);
        return brand;
    }

    private JComponent buildSearchBox() {
        JPanel box = new JPanel(new BorderLayout(10, 0));
        box.setBackground(UIStyle.blend(UIStyle.CARD, UIStyle.ACCENT, 0.05f));
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIStyle.BORDER),
                BorderFactory.createEmptyBorder(0, 14, 0, 14)));

        JLabel icon = UIStyle.emoji("\uD83D\uDD0D", 18);
        icon.setForeground(UIStyle.ACCENT_DARK);

        globalSearchField = new JTextField();
        globalSearchField.setFont(new Font("Arial", Font.PLAIN, 14));
        globalSearchField.setForeground(UIStyle.TEXT);
        globalSearchField.setOpaque(false);
        globalSearchField.setBorder(BorderFactory.createEmptyBorder());
        globalSearchField.setToolTipText("Search found items and press Enter");
        installPlaceholder(globalSearchField, "Search found items...");
        globalSearchField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                String text = globalSearchField.getText().trim();
                if (text.isEmpty() || text.startsWith("Search ")) {
                    return;
                }
                SearchPanel found = (SearchPanel) cards.get("found");
                found.searchKeyword(text);
                showCard("found");
            }
        });

        box.add(icon, BorderLayout.WEST);
        box.add(globalSearchField, BorderLayout.CENTER);
        box.setPreferredSize(new Dimension(480, 44));
        return box;
    }

    private JComponent buildTopbarRight() {
        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.X_AXIS));

        JButton themeButton = UIStyle.secondaryButton(
                UIStyle.isDark() ? "Light" : "Dark");
        themeButton.setFont(new Font("Arial", Font.BOLD, 11));
        themeButton.setToolTipText(UIStyle.isDark()
                ? "Switch to light theme" : "Switch to dark theme");
        themeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                switchTheme();
            }
        });

        JPanel bell = new JPanel(null);
        bell.setOpaque(false);
        bell.setPreferredSize(new Dimension(50, 44));
        bell.setMaximumSize(new Dimension(50, 44));

        JLabel bellButton = UIStyle.emoji("\uD83D\uDD14", 18);
        bellButton.setBounds(0, 0, 44, 44);
        bellButton.setHorizontalAlignment(SwingConstants.CENTER);
        bellButton.setForeground(UIStyle.ACCENT_DARK);
        bellButton.setBackground(UIStyle.ACCENT_LIGHT);
        bellButton.setOpaque(true);
        bellButton.setBorder(BorderFactory.createEmptyBorder());
        bellButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        bellButton.setToolTipText("Notifications");
        bellButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                showCard("notifications");
            }
        });

        notificationBadge = new JLabel("0", SwingConstants.CENTER);
        notificationBadge.setFont(new Font("Arial", Font.BOLD, 9));
        notificationBadge.setForeground(Color.WHITE);
        notificationBadge.setBackground(UIStyle.DANGER);
        notificationBadge.setOpaque(true);
        notificationBadge.setBounds(28, 0, 20, 18);
        notificationBadge.setVisible(false);

        bell.add(bellButton);
        bell.add(notificationBadge);

        User user = SessionContext.getCurrentUser();
        String initial = user == null || user.getFullName() == null
                || user.getFullName().isEmpty()
                ? "U" : user.getFullName().substring(0, 1).toUpperCase();
        headerAvatar = new Avatar(initial);

        JPanel userInfo = new JPanel();
        userInfo.setOpaque(false);
        userInfo.setLayout(new BoxLayout(userInfo, BoxLayout.Y_AXIS));

        headerUserLabel = new JLabel(user == null ? "Not signed in"
                : user.getFullName());
        headerUserLabel.setFont(new Font("Arial", Font.BOLD, 13));
        headerUserLabel.setForeground(UIStyle.TEXT);
        headerUserLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        headerRoleLabel = new JLabel(user == null ? ""
                : user.getRole().getLabel());
        headerRoleLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        headerRoleLabel.setForeground(UIStyle.MUTED);
        headerRoleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        userInfo.add(headerUserLabel);
        userInfo.add(headerRoleLabel);


        JButton home = UIStyle.secondaryButton("Home");
        home.setFont(new Font("Arial", Font.BOLD, 11));
        home.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                showCard("home");
            }
        });

        JButton settings = UIStyle.secondaryButton("Settings");
        settings.setFont(new Font("Arial", Font.BOLD, 11));
        settings.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                showCard("settings");
            }
        });

        JButton logout = UIStyle.secondaryButton("Logout");
        logout.setFont(new Font("Arial", Font.BOLD, 11));
        logout.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                signOut();
            }
        });

        right.add(themeButton);
        right.add(Box.createHorizontalStrut(10));
        right.add(bell);
        right.add(Box.createHorizontalStrut(14));
        right.add(headerAvatar);
        right.add(Box.createHorizontalStrut(9));
        right.add(userInfo);
        right.add(Box.createHorizontalStrut(14));
        right.add(home);
        right.add(Box.createHorizontalStrut(8));
        right.add(settings);
        right.add(Box.createHorizontalStrut(8));
        right.add(logout);
        return right;
    }

    private void installPlaceholder(final JTextField field, final String placeholder) {
        field.setText(placeholder);
        field.setForeground(UIStyle.MUTED);
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent event) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(UIStyle.TEXT);
                }
            }

            @Override
            public void focusLost(FocusEvent event) {
                if (field.getText().trim().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(UIStyle.MUTED);
                }
            }
        });
    }

    private void updateHeaderUser() {
        User user = SessionContext.getCurrentUser();
        if (user == null) {
            return;
        }
        if (headerUserLabel != null) {
            headerUserLabel.setText(user.getFullName());
        }
        if (headerRoleLabel != null) {
            headerRoleLabel.setText(user.getRole().getLabel());
        }
        if (headerAvatar != null) {
            headerAvatar.setText(user.getFullName() == null
                    || user.getFullName().isEmpty()
                    ? "U" : user.getFullName().substring(0, 1).toUpperCase());
            headerAvatar.repaint();
        }
    }

    // ==================================================================
    //  Navigation
    // ==================================================================

    private Runnable backAction() {
        return new Runnable() {
            @Override
            public void run() {
                goBack();
            }
        };
    }

    public void openReport() {
        showCard("report");
    }

    private void showCard(String key) {
        if (key == null) {
            return;
        }
        if (!navigatingBack && currentKey != null && !currentKey.equals(key)) {
            history.push(currentKey);
        }
        navigatingBack = false;
        currentKey = key;

        cardLayout.show(content, key);

        JComponent card = cards.get(key);
        if (card instanceof Refreshable) {
            try {
                ((Refreshable) card).refreshData();
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(),
                        "Could not load data", JOptionPane.ERROR_MESSAGE);
            }
        }
        refreshNotificationBadge();
    }

    private void goBack() {
        if (history.isEmpty()) {
            showCard("home");
            return;
        }
        String previous = history.pop();
        navigatingBack = true;
        showCard(previous);
    }


    private void refreshNotificationBadge() {
        final User user = SessionContext.getCurrentUser();
        if (user == null || notificationBadge == null) {
            return;
        }
        Async.run(() -> claimService.actionableCount(user), count -> {
            int value = count == null ? 0 : count.intValue();
            notificationBadge.setText(value > 99 ? "99+" : String.valueOf(value));
            notificationBadge.setVisible(value > 0);
        }, error -> {
            // A badge failure must never block navigation.
        });
    }

    private void switchTheme() {
        String target = currentKey == null ? "home" : currentKey;
        UIStyle.applyTheme(!UIStyle.isDark());
        UIStyle.applyGlobalDefaults();
        MainFrame replacement = new MainFrame();
        replacement.setVisible(true);
        replacement.showCard(target);
        dispose();
    }

    private void signOut() {
        int choice = JOptionPane.showConfirmDialog(this,
                "Sign out of REC Lost & Found?", "Logout",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            SessionContext.logout();
            dispose();
            new LoginFrame().setVisible(true);
        }
    }

    // ==================================================================
    //  Small custom components
    // ==================================================================

    private static final class Avatar extends JLabel {

        private static final long serialVersionUID = 1L;

        Avatar(String initial) {
            super(initial);
            setHorizontalAlignment(SwingConstants.CENTER);
            setFont(new Font("Arial", Font.BOLD, 17));
            setForeground(Color.WHITE);
            setPreferredSize(new Dimension(40, 40));
            setMinimumSize(new Dimension(40, 40));
            setMaximumSize(new Dimension(40, 40));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(UIStyle.ACCENT);
            g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
            g2.dispose();
            super.paintComponent(graphics);
        }
    }

}
