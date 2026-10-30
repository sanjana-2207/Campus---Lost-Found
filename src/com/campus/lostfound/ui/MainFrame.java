package com.campus.lostfound.ui;

import com.campus.lostfound.model.User;
import com.campus.lostfound.service.ClaimService;
import com.campus.lostfound.service.ItemService;
import com.campus.lostfound.service.MatchService;
import com.campus.lostfound.service.ReportService;
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
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Application shell: header, navigation sidebar and a {@link CardLayout} of
 * screens.
 *
 * <p>Screens are created once and reused. Whenever one becomes visible,
 * {@link MainFrame} calls {@link Refreshable#refreshData()} so the tables always
 * show the current database contents.</p>
 */
public class MainFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel content = new JPanel(cardLayout);
    private final Map<String, JComponent> cards = new LinkedHashMap<String, JComponent>();
    private final Map<String, JButton> navButtons = new LinkedHashMap<String, JButton>();

    // Services are created once and shared by every screen (composition).
    private final ItemService itemService = new ItemService();
    private final ClaimService claimService = new ClaimService();
    private final MatchService matchService = new MatchService();
    private final ReportService reportService = new ReportService();

    public MainFrame() {
        setTitle("Campus Lost & Found");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 800);
        setMinimumSize(new Dimension(1120, 700));
        setLocationRelativeTo(null);

        content.setBackground(UIStyle.BG);
        content.setBorder(BorderFactory.createEmptyBorder(22, 24, 22, 24));

        registerCard("dashboard", "Dashboard", new DashboardPanel(itemService, reportService));
        registerCard("report", "Report an item", new ReportItemPanel(itemService));
        registerCard("search", "Search and claim", new SearchPanel(itemService, claimService));
        registerCard("matches", "Match suggestions",
                new MatchesPanel(matchService, itemService, claimService));
        registerCard("claims", "Ownership claims", new ClaimsPanel(claimService));
        registerCard("reports", "Reports and analytics", new ReportsPanel(reportService));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildSidebar(), BorderLayout.WEST);
        add(content, BorderLayout.CENTER);

        showCard("dashboard");
    }

    // ==================================================================
    //  Shell construction
    // ==================================================================

    private void registerCard(String key, String navigationLabel, JComponent panel) {
        cards.put(key, panel);
        JButton button = UIStyle.navButton(navigationLabel);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                showCard(key);
            }
        });
        navButtons.put(key, button);
        content.add(panel, key);
    }

    private JComponent buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIStyle.NAVY);
        header.setBorder(BorderFactory.createEmptyBorder(13, 24, 13, 24));
        header.setPreferredSize(new Dimension(0, 68));

        JPanel branding = new JPanel();
        branding.setOpaque(false);
        branding.setLayout(new BoxLayout(branding, BoxLayout.Y_AXIS));

        JLabel name = new JLabel("Campus Lost & Found");
        name.setFont(new Font("Segoe UI", Font.BOLD, 17));
        name.setForeground(Color.WHITE);
        name.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel tagline = new JLabel("Recover what matters, faster.");
        tagline.setFont(UIStyle.FONT_SMALL);
        tagline.setForeground(new Color(0x8FA9C0));
        tagline.setAlignmentX(Component.LEFT_ALIGNMENT);

        branding.add(name);
        branding.add(Box.createVerticalStrut(2));
        branding.add(tagline);
        header.add(branding, BorderLayout.WEST);

        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.X_AXIS));

        User user = SessionContext.getCurrentUser();
        String who = user == null
                ? "Not signed in"
                : user.getFullName() + "  ·  " + user.getRole().getLabel();
        JLabel userLabel = new JLabel(who);
        userLabel.setFont(UIStyle.FONT_BOLD);
        userLabel.setForeground(new Color(0xDCE6EF));

        JButton logout = UIStyle.secondaryButton("Sign out");
        logout.setFont(UIStyle.FONT_SMALL.deriveFont(Font.BOLD));
        logout.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                signOut();
            }
        });

        right.add(userLabel);
        right.add(Box.createHorizontalStrut(18));
        right.add(logout);
        header.add(right, BorderLayout.EAST);

        return header;
    }

    private JComponent buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setBackground(UIStyle.NAVY);
        sidebar.setPreferredSize(new Dimension(226, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(18, 0, 16, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));

        JLabel menu = new JLabel("MENU");
        menu.setFont(new Font("Segoe UI", Font.BOLD, 11));
        menu.setForeground(new Color(0x6E8CA8));
        menu.setBorder(BorderFactory.createEmptyBorder(0, 26, 10, 0));
        menu.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(menu);

        for (JButton button : navButtons.values()) {
            sidebar.add(button);
        }

        sidebar.add(Box.createVerticalGlue());

        JLabel footer = new JLabel("<html>v1.0 &nbsp;·&nbsp; MySQL<br>"
                + "OOPS + DBMS mini project</html>");
        footer.setFont(UIStyle.FONT_SMALL);
        footer.setForeground(new Color(0x6E8CA8));
        footer.setBorder(BorderFactory.createEmptyBorder(0, 26, 0, 0));
        footer.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(footer);

        return sidebar;
    }

    // ==================================================================
    //  Navigation
    // ==================================================================

    private void showCard(String key) {
        cardLayout.show(content, key);

        for (Map.Entry<String, JButton> entry : navButtons.entrySet()) {
            boolean active = entry.getKey().equals(key);
            JButton button = entry.getValue();
            button.setBackground(active ? UIStyle.NAVY_SOFT : UIStyle.NAVY);
            button.setForeground(active ? Color.WHITE : new Color(0xC9D6E2));
            button.setBorder(active
                    ? BorderFactory.createCompoundBorder(
                            BorderFactory.createMatteBorder(0, 4, 0, 0, UIStyle.ACCENT),
                            BorderFactory.createEmptyBorder(12, 22, 12, 16))
                    : BorderFactory.createEmptyBorder(12, 26, 12, 16));
        }

        JComponent card = cards.get(key);
        if (card instanceof Refreshable) {
            try {
                ((Refreshable) card).refreshData();
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(),
                        "Could not load data", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void signOut() {
        int choice = JOptionPane.showConfirmDialog(this,
                "Sign out of the application?", "Sign out",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            SessionContext.logout();
            dispose();
            new LoginFrame().setVisible(true);
        }
    }
}
