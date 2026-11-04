package com.campus.lostfound.ui;

import com.campus.lostfound.model.User;
import com.campus.lostfound.service.ReportService;
import com.campus.lostfound.util.Async;
import com.campus.lostfound.util.SessionContext;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Scrollable;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.RenderingHints;

/**
 * FINDIT-style dashboard: purple welcome hero, live stat cards and quick
 * actions. Lost-item reporting stays out of scope; the lost counters remain as
 * analytics for the reports screen.
 */
public class DashboardPanel extends JPanel implements Refreshable {

    private static final long serialVersionUID = 1L;

    public interface HomeNavigator {
        void openReport();

        void openFoundItems();

        void openMatches();

        void openMyReports();

        void openNotifications();

        void openClaims();

        void openReports();
    }

    private final ReportService reportService;
    private final HomeNavigator navigator;

    private final JLabel lostValue = new JLabel("0");
    private final JLabel foundValue = new JLabel("0");
    private final JLabel pendingValue = new JLabel("0");
    private final JLabel recoveredValue = new JLabel("0");
    private final JLabel rateValue = new JLabel("0%");

    public DashboardPanel(ReportService reportService, HomeNavigator navigator) {
        this.reportService = reportService;
        this.navigator = navigator;

        setLayout(new BorderLayout());
        setBackground(UIStyle.BG);
        add(buildScrollablePage(), BorderLayout.CENTER);
    }

    private JComponent buildScrollablePage() {
        JPanel page = new HomePage();
        page.setOpaque(false);
        page.setLayout(new BorderLayout());

        JPanel column = new JPanel();
        column.setOpaque(false);
        column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));
        column.setBorder(BorderFactory.createEmptyBorder(2, 2, 10, 2));

        JComponent hero = buildHero();
        hero.setAlignmentX(Component.LEFT_ALIGNMENT);
        column.add(hero);
        column.add(Box.createVerticalStrut(20));

        JComponent stats = buildStats();
        stats.setAlignmentX(Component.LEFT_ALIGNMENT);
        column.add(stats);
        column.add(Box.createVerticalStrut(26));

        JComponent heading = buildSectionHeading();
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        column.add(heading);
        column.add(Box.createVerticalStrut(14));

        JComponent actions = buildQuickActions();
        actions.setAlignmentX(Component.LEFT_ALIGNMENT);
        column.add(actions);
        column.add(Box.createVerticalGlue());

        page.add(column, BorderLayout.CENTER);

        JScrollPane scroll = new JScrollPane(page);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        return scroll;
    }

    // ==================================================================
    //  Hero
    // ==================================================================

    private JComponent buildHero() {
        JPanel hero = new JPanel(new BorderLayout(24, 0)) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g2 = (Graphics2D) graphics.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, new Color(0x5420AD),
                        getWidth(), getHeight(), new Color(0x9333D8)));
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 22, 22);
                g2.dispose();
                super.paintComponent(graphics);
            }
        };
        hero.setOpaque(false);
        hero.setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));

        JPanel texts = new JPanel();
        texts.setOpaque(false);
        texts.setLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));

        JPanel tag = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        tag.setOpaque(true);
        tag.setBackground(new Color(255, 255, 255, 40));
        tag.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 80)),
                BorderFactory.createEmptyBorder(4, 7, 4, 7)));
        tag.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel tagIcon = UIStyle.emoji("\uD83C\uDF93", 11);
        tagIcon.setForeground(Color.WHITE);
        JLabel tagText = new JLabel("REC LOST & FOUND");
        tagText.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 10));
        tagText.setForeground(Color.WHITE);
        tag.add(tagIcon);
        tag.add(tagText);

        JLabel title = new JLabel("Welcome back, " + firstName() + "!");
        title.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 30));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel(
                "Find what you've lost or help someone recover their belongings.");
        subtitle.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 14));
        subtitle.setForeground(new Color(0xF4EAFF));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel hint = new JLabel("Choose an option below to get started.");
        hint.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 12));
        hint.setForeground(new Color(0xE9DAF8));
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);

        texts.add(tag);
        texts.add(Box.createVerticalStrut(12));
        texts.add(title);
        texts.add(Box.createVerticalStrut(6));
        texts.add(subtitle);
        texts.add(Box.createVerticalStrut(10));
        texts.add(hint);

        hero.add(texts, BorderLayout.CENTER);
        hero.add(buildHeroCircle(), BorderLayout.EAST);

        hero.setMaximumSize(new Dimension(Integer.MAX_VALUE, hero.getPreferredSize().height));
        return hero;
    }

    private JComponent buildHeroCircle() {
        JPanel circle = new JPanel(new BorderLayout()) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g2 = (Graphics2D) graphics.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 36));
                g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
                g2.dispose();
                super.paintComponent(graphics);
            }
        };
        circle.setOpaque(false);
        circle.setPreferredSize(new Dimension(132, 132));
        circle.setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));

        JPanel center = new JPanel(new java.awt.GridBagLayout());
        center.setOpaque(false);
        JLabel glyph = UIStyle.emoji("\uD83D\uDD0D", 48);
        glyph.setForeground(Color.WHITE);
        center.add(glyph);
        circle.add(center, BorderLayout.CENTER);
        return circle;
    }

    private String firstName() {
        User user = SessionContext.getCurrentUser();
        if (user == null || user.getFullName() == null
                || user.getFullName().trim().isEmpty()) {
            return "there";
        }
        return user.getFullName().trim().split("\\s+")[0];
    }

    // ==================================================================
    //  Stats
    // ==================================================================

    private JComponent buildStats() {
        JPanel row = new JPanel(new GridLayout(1, 5, 14, 0));
        row.setOpaque(false);

        row.add(UIStyle.finditStatCard(lostValue, "Lost Items",
                "\uD83D\uDD0E", new Color(0xC2410C)));
        row.add(UIStyle.finditStatCard(foundValue, "Found Items",
                "\uD83D\uDCE6", UIStyle.ACCENT));
        row.add(UIStyle.finditStatCard(pendingValue, "Claims Awaiting",
                "\uD83E\uDD1D", new Color(0x7E22CE)));
        row.add(UIStyle.finditStatCard(recoveredValue, "Items Recovered",
                "\u2705", UIStyle.SUCCESS));
        row.add(UIStyle.finditStatCard(rateValue, "Recovery Rate",
                "\uD83D\uDCC8", new Color(0xA16207)));

        row.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                row.getPreferredSize().height));
        return row;
    }

    // ==================================================================
    //  Quick actions
    // ==================================================================

    private JComponent buildSectionHeading() {
        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));

        JLabel title = UIStyle.sectionHeading("What would you like to do?");
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel subtitle = UIStyle.subtitle(
                "Search, match and track the items you care about.");
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        heading.add(title);
        heading.add(Box.createVerticalStrut(4));
        heading.add(subtitle);
        heading.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                heading.getPreferredSize().height));
        return heading;
    }

    private JComponent buildQuickActions() {
        JPanel grid = new JPanel(new GridLayout(2, 2, 16, 16));
        grid.setOpaque(false);

        grid.add(UIStyle.quickCard("\uD83D\uDCE6", "Report Found Items",
                "Register an item you picked up on campus.",
                UIStyle.ACCENT, new Runnable() {
                    @Override
                    public void run() {
                        navigator.openReport();
                    }
                }));
        grid.add(UIStyle.quickCard("\uD83D\uDD0D", "Search and Claims",
                "Find reported items and claim ownership.",
                new Color(0x0284C7), new Runnable() {
                    @Override
                    public void run() {
                        navigator.openFoundItems();
                    }
                }));
        grid.add(UIStyle.quickCard("\uD83E\uDD1D", "Ownership Claims",
                "Track claims and confirm item handovers.",
                UIStyle.SUCCESS, new Runnable() {
                    @Override
                    public void run() {
                        navigator.openClaims();
                    }
                }));
        grid.add(UIStyle.quickCard("\uD83D\uDCCA", "Reports & Analytics",
                "Recovery rate, categories and monthly trends.",
                new Color(0xD97706), new Runnable() {
                    @Override
                    public void run() {
                        navigator.openReports();
                    }
                }));

        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                grid.getPreferredSize().height));
        return grid;
    }

    @Override
    public void refreshData() {
        Async.run(() -> reportService.buildSummary(),
                summary -> {
                    lostValue.setText(String.valueOf(summary.getTotalLost()));
                    foundValue.setText(String.valueOf(summary.getTotalFound()));
                    pendingValue.setText(String.valueOf(summary.getPendingClaims()));
                    recoveredValue.setText(String.valueOf(summary.getTotalRecovered()));
                    rateValue.setText(String.format("%.1f%%", summary.getRecoveryRate()));
                },
                error -> Async.showError(this, error, "Could not load the dashboard"));
    }

    /** Transparent page so the shell background shows through. */
    private static final class HomePage extends JPanel implements Scrollable {

        private static final long serialVersionUID = 1L;

        HomePage() {
            setOpaque(false);
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect,
                                              int orientation, int direction) {
            return 18;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect,
                                               int orientation, int direction) {
            return Math.max(40, visibleRect.height - 40);
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return getParent() instanceof javax.swing.JViewport
                    && getPreferredSize().height < getParent().getHeight();
        }
    }
}
