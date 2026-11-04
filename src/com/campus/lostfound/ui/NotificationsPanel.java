package com.campus.lostfound.ui;

import com.campus.lostfound.model.AppNotification;
import com.campus.lostfound.service.ClaimService;
import com.campus.lostfound.service.NotificationService;
import com.campus.lostfound.util.Async;
import com.campus.lostfound.util.SessionContext;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

/**
 * FINDIT-style notification feed derived from claim activity.
 */
public class NotificationsPanel extends JPanel implements Refreshable {

    private static final long serialVersionUID = 1L;

    private final NotificationService notificationService;
    private final Runnable onBack;
    private final JPanel list = new JPanel();
    private final JLabel countLabel = new JLabel(" ");

    public NotificationsPanel(ClaimService claimService, Runnable onBack) {
        this.notificationService = new NotificationService(claimService);
        this.onBack = onBack;

        setLayout(new BorderLayout(0, 16));
        setBackground(UIStyle.BG);

        add(buildHeader(), BorderLayout.NORTH);

        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setBorder(BorderFactory.createEmptyBorder(2, 2, 8, 2));

        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        add(scroll, BorderLayout.CENTER);
    }

    private JComponent buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

        JLabel title = UIStyle.heading("Notifications");
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel subtitle = UIStyle.subtitle(
                "Claim updates and handovers that need your attention.");
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        text.add(title);
        text.add(Box.createVerticalStrut(2));
        text.add(subtitle);
        text.add(Box.createVerticalStrut(8));
        text.add(UIStyle.backLink(onBack));
        header.add(text, BorderLayout.WEST);

        JPanel buttons = new JPanel();
        buttons.setOpaque(false);
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));

        countLabel.setFont(UIStyle.FONT_SMALL);
        countLabel.setForeground(UIStyle.MUTED);

        JButton refresh = UIStyle.secondaryButton("Refresh");
        refresh.addActionListener(event -> refreshData());

        buttons.add(countLabel);
        buttons.add(Box.createHorizontalStrut(12));
        buttons.add(refresh);
        header.add(buttons, BorderLayout.EAST);
        return header;
    }

    @Override
    public void refreshData() {
        Async.run(() -> notificationService.buildFor(SessionContext.getCurrentUser()),
                notifications -> {
                    list.removeAll();
                    countLabel.setText(notifications.size()
                            + (notifications.size() == 1 ? " update" : " updates"));

                    if (notifications.isEmpty()) {
                        JPanel empty = UIStyle.card();
                        empty.setLayout(new BorderLayout());
                        JLabel message = new JLabel(
                                "You're all caught up. New claim activity will appear here.");
                        message.setFont(UIStyle.FONT);
                        message.setForeground(UIStyle.MUTED);
                        empty.add(message, BorderLayout.CENTER);
                        empty.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                                empty.getPreferredSize().height));
                        empty.setAlignmentX(Component.LEFT_ALIGNMENT);
                        list.add(empty);
                    } else {
                        for (AppNotification notification : notifications) {
                            JComponent card = buildNotification(notification);
                            card.setAlignmentX(Component.LEFT_ALIGNMENT);
                            list.add(card);
                            list.add(Box.createVerticalStrut(12));
                        }
                    }
                    list.revalidate();
                    list.repaint();
                },
                error -> Async.showError(this, error, "Could not load notifications"));
    }

    private JComponent buildNotification(AppNotification notification) {
        JPanel card = UIStyle.card();
        card.setLayout(new BorderLayout(14, 0));

        Color accent = accentFor(notification.getStatus());
        JLabel icon = new JLabel("\u25cf", SwingConstants.CENTER);
        icon.setFont(UIStyle.FONT_BOLD.deriveFont(18f));
        icon.setForeground(accent);
        icon.setOpaque(true);
        icon.setBackground(UIStyle.blend(accent, UIStyle.CARD, 0.84f));
        icon.setPreferredSize(new Dimension(44, 44));
        icon.setBorder(BorderFactory.createLineBorder(
                UIStyle.blend(accent, UIStyle.CARD, 0.62f)));

        JPanel texts = new JPanel();
        texts.setOpaque(false);
        texts.setLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));

        JLabel title = new JLabel(notification.getTitle());
        title.setFont(UIStyle.FONT_BOLD.deriveFont(14f));
        title.setForeground(UIStyle.TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel message = new JLabel("<html><body style='width:620px'>"
                + escape(notification.getMessage()) + "</body></html>");
        message.setFont(UIStyle.FONT_SMALL);
        message.setForeground(UIStyle.MUTED);
        message.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel time = new JLabel(notification.getTimeLabel());
        time.setFont(UIStyle.FONT_SMALL.deriveFont(10f));
        time.setForeground(UIStyle.MUTED);
        time.setAlignmentX(Component.LEFT_ALIGNMENT);

        texts.add(title);
        texts.add(Box.createVerticalStrut(3));
        texts.add(message);
        texts.add(Box.createVerticalStrut(4));
        texts.add(time);

        JLabel status = new JLabel(notification.getStatus());
        status.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 10));
        status.setForeground(accent);
        status.setOpaque(true);
        status.setBackground(UIStyle.blend(accent, UIStyle.CARD, 0.86f));
        status.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
        status.setHorizontalAlignment(SwingConstants.CENTER);

        card.add(icon, BorderLayout.WEST);
        card.add(texts, BorderLayout.CENTER);
        card.add(status, BorderLayout.EAST);

        card.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                card.getPreferredSize().height));
        return card;
    }

    private Color accentFor(String status) {
        if ("Approved".equalsIgnoreCase(status)) {
            return UIStyle.ACCENT;
        }
        if ("Rejected".equalsIgnoreCase(status)) {
            return UIStyle.DANGER;
        }
        if ("Recovered".equalsIgnoreCase(status)) {
            return UIStyle.SUCCESS;
        }
        return UIStyle.WARNING;
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
