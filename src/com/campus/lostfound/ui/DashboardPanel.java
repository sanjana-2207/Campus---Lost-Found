package com.campus.lostfound.ui;

import com.campus.lostfound.model.Item;
import com.campus.lostfound.service.ItemService;
import com.campus.lostfound.service.ReportService;
import com.campus.lostfound.service.ReportSummary;
import com.campus.lostfound.util.Async;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

/**
 * Landing screen: four live counters plus the ten most recent reports.
 */
public class DashboardPanel extends JPanel implements Refreshable {

    private static final long serialVersionUID = 1L;

    private final ItemService itemService;
    private final ReportService reportService;

    private final JLabel lostValue = new JLabel("0");
    private final JLabel foundValue = new JLabel("0");
    private final JLabel pendingValue = new JLabel("0");
    private final JLabel recoveredValue = new JLabel("0");
    private final JLabel rateLabel = new JLabel("Recovery rate 0%");

    private final ListTableModel tableModel = new ListTableModel(new String[]{
            "ID", "Type", "Title", "Category", "Location", "Date", "Status", "Reported by"});

    public DashboardPanel(ItemService itemService, ReportService reportService) {
        this.itemService = itemService;
        this.reportService = reportService;

        setLayout(new BorderLayout(0, 16));
        setBackground(UIStyle.BG);

        add(buildHeader(), BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(0, 16));
        body.setOpaque(false);
        body.add(buildCounters(), BorderLayout.NORTH);
        body.add(buildRecentCard(), BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);
    }

    private JComponent buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

        JLabel title = UIStyle.heading("Dashboard");
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel subtitle = UIStyle.subtitle(
                "A live snapshot of lost, found, claimed and recovered items on campus.");
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        text.add(title);
        text.add(Box.createVerticalStrut(4));
        text.add(subtitle);
        header.add(text, BorderLayout.WEST);

        rateLabel.setFont(UIStyle.FONT_BOLD);
        rateLabel.setForeground(UIStyle.SUCCESS);
        rateLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        header.add(rateLabel, BorderLayout.EAST);

        return header;
    }

    private JComponent buildCounters() {
        JPanel row = new JPanel(new GridLayout(1, 4, 14, 0));
        row.setOpaque(false);

        row.add(UIStyle.statCard(lostValue, "Lost item reports", UIStyle.WARNING));
        row.add(UIStyle.statCard(foundValue, "Found item reports", UIStyle.ACCENT));
        row.add(UIStyle.statCard(pendingValue, "Claims awaiting verification", UIStyle.DANGER));
        row.add(UIStyle.statCard(recoveredValue, "Items recovered", UIStyle.SUCCESS));

        return row;
    }

    private JComponent buildRecentCard() {
        JPanel card = UIStyle.card();
        card.setLayout(new BorderLayout(0, 10));

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);

        JLabel title = new JLabel("Recent reports");
        title.setFont(UIStyle.FONT_BOLD.deriveFont(15f));
        title.setForeground(UIStyle.TEXT);
        titleRow.add(title, BorderLayout.WEST);

        JLabel hint = UIStyle.muted("Ten most recent reports across all categories");
        titleRow.add(hint, BorderLayout.EAST);

        card.add(titleRow, BorderLayout.NORTH);

        JTable table = new JTable(tableModel);
        UIStyle.styleTable(table);
        table.getColumnModel().getColumn(0).setPreferredWidth(45);
        table.getColumnModel().getColumn(1).setPreferredWidth(60);
        table.getColumnModel().getColumn(2).setPreferredWidth(280);
        table.getColumnModel().getColumn(3).setPreferredWidth(120);
        table.getColumnModel().getColumn(4).setPreferredWidth(120);
        table.getColumnModel().getColumn(5).setPreferredWidth(100);
        table.getColumnModel().getColumn(6).setPreferredWidth(90);
        table.getColumnModel().getColumn(6).setCellRenderer(new StatusCellRenderer());
        table.getColumnModel().getColumn(7).setPreferredWidth(140);

        card.add(UIStyle.scroll(table), BorderLayout.CENTER);
        return card;
    }

    @Override
    public void refreshData() {
        Async.run(() -> new Snapshot(reportService.buildSummary(), itemService.recent(10)),
                snapshot -> {
                    ReportSummary summary = snapshot.summary;
                    lostValue.setText(String.valueOf(summary.getTotalLost()));
                    foundValue.setText(String.valueOf(summary.getTotalFound()));
                    pendingValue.setText(String.valueOf(summary.getPendingClaims()));
                    recoveredValue.setText(String.valueOf(summary.getTotalRecovered()));
                    rateLabel.setText(String.format("Recovery rate %.1f%%  ·  %d open items",
                            summary.getRecoveryRate(), summary.getTotalOpen()));

                    List<Object[]> rows = new ArrayList<Object[]>();
                    for (Item item : snapshot.recent) {
                        rows.add(new Object[]{
                                item.getItemId(),
                                item.getItemType().getLabel(),
                                item.getTitle(),
                                item.getCategoryName(),
                                item.getLocationName(),
                                item.getEventDate(),
                                item.getStatus().getLabel(),
                                item.getReporterName()});
                    }
                    tableModel.setRows(rows);
                },
                error -> Async.showError(this, error, "Could not load the dashboard"));
    }

    /** Immutable result of the two background queries. */
    private static final class Snapshot {
        private final ReportSummary summary;
        private final List<Item> recent;

        private Snapshot(ReportSummary summary, List<Item> recent) {
            this.summary = summary;
            this.recent = recent;
        }
    }
}
