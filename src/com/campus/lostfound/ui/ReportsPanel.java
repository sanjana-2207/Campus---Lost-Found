package com.campus.lostfound.ui;

import com.campus.lostfound.model.ClaimStatus;
import com.campus.lostfound.model.Item;
import com.campus.lostfound.model.ItemStatus;
import com.campus.lostfound.service.ReportService;
import com.campus.lostfound.service.ReportSummary;
import com.campus.lostfound.util.Async;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * Analytics screen: category, status and monthly summaries, plus CSV export.
 */
public class ReportsPanel extends JPanel implements Refreshable {

    private static final long serialVersionUID = 1L;

    private final ReportService reportService;

    private final JLabel totalValue = new JLabel("0");
    private final JLabel recoveryValue = new JLabel("0%");
    private final JLabel pendingValue = new JLabel("0");
    private final JLabel claimsValue = new JLabel("0");

    private final ListTableModel categoryModel =
            new ListTableModel(new String[]{"Category", "Reports"});
    private final ListTableModel statusModel =
            new ListTableModel(new String[]{"Item status", "Reports"});
    private final ListTableModel claimModel =
            new ListTableModel(new String[]{"Claim status", "Claims"});
    private final ListTableModel monthlyModel =
            new ListTableModel(new String[]{"Month", "Lost", "Found"});

    private ReportSummary summary;

    public ReportsPanel(ReportService reportService) {
        this.reportService = reportService;

        setLayout(new BorderLayout(0, 16));
        setBackground(UIStyle.BG);

        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);
    }

    // ==================================================================
    //  Layout
    // ==================================================================

    private JComponent buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

        JLabel title = UIStyle.heading("Reports and analytics");
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel subtitle = UIStyle.subtitle(
                "Aggregated with SQL GROUP BY queries and exportable for the weekly review.");
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        text.add(title);
        text.add(Box.createVerticalStrut(4));
        text.add(subtitle);
        header.add(text, BorderLayout.WEST);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);

        JButton refresh = UIStyle.secondaryButton("Refresh");
        refresh.addActionListener(event -> refreshData());

        JButton exportSummary = UIStyle.primaryButton("Export summary");
        exportSummary.addActionListener(event -> exportSummary());

        JButton exportItems = UIStyle.secondaryButton("Export item list");
        exportItems.addActionListener(event -> exportItems());

        buttons.add(exportItems);
        buttons.add(exportSummary);
        buttons.add(refresh);
        header.add(buttons, BorderLayout.EAST);

        return header;
    }

    private JComponent buildBody() {
        JPanel body = new JPanel(new BorderLayout(0, 14));
        body.setOpaque(false);

        JPanel counters = new JPanel(new GridLayout(1, 4, 14, 0));
        counters.setOpaque(false);
        counters.add(UIStyle.statCard(totalValue, "Total reports", UIStyle.ACCENT));
        counters.add(UIStyle.statCard(recoveryValue, "Recovery rate", UIStyle.SUCCESS));
        counters.add(UIStyle.statCard(pendingValue, "Pending claims", UIStyle.WARNING));
        counters.add(UIStyle.statCard(claimsValue, "Total claims", UIStyle.DANGER));
        body.add(counters, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(2, 2, 14, 14));
        grid.setOpaque(false);
        grid.add(card("Reports by category", categoryModel));
        grid.add(card("Reports by status", statusModel));
        grid.add(card("Claims by status", claimModel));
        grid.add(card("Monthly trend", monthlyModel));
        body.add(grid, BorderLayout.CENTER);

        return body;
    }

    private JComponent card(String title, ListTableModel model) {
        JPanel card = UIStyle.card();
        card.setLayout(new BorderLayout(0, 8));

        JLabel heading = new JLabel(title);
        heading.setFont(UIStyle.FONT_BOLD.deriveFont(14f));
        heading.setForeground(UIStyle.TEXT);
        card.add(heading, BorderLayout.NORTH);

        JTable table = new JTable(model);
        UIStyle.styleTable(table);
        table.setAutoCreateRowSorter(false);
        card.add(UIStyle.scroll(table), BorderLayout.CENTER);

        return card;
    }

    // ==================================================================
    //  Data
    // ==================================================================

    @Override
    public void refreshData() {
        Async.run(reportService::buildSummary, loaded -> {
            summary = loaded;

            totalValue.setText(String.valueOf(summary.getTotalItems()));
            recoveryValue.setText(String.format("%.1f%%", summary.getRecoveryRate()));
            pendingValue.setText(String.valueOf(summary.getPendingClaims()));
            claimsValue.setText(String.valueOf(summary.getTotalClaims()));

            List<Object[]> categories = new ArrayList<Object[]>();
            for (Map.Entry<String, Integer> entry : summary.getByCategory().entrySet()) {
                categories.add(new Object[]{entry.getKey(), entry.getValue()});
            }
            categoryModel.setRows(categories);

            List<Object[]> statuses = new ArrayList<Object[]>();
            for (ItemStatus status : ItemStatus.values()) {
                Integer count = summary.getByStatus().get(status);
                statuses.add(new Object[]{status.getLabel(), count == null ? 0 : count});
            }
            statusModel.setRows(statuses);

            List<Object[]> claims = new ArrayList<Object[]>();
            for (ClaimStatus status : ClaimStatus.values()) {
                Integer count = summary.getClaimsByStatus().get(status);
                claims.add(new Object[]{status.getLabel(), count == null ? 0 : count});
            }
            claimModel.setRows(claims);

            List<Object[]> months = new ArrayList<Object[]>();
            TreeSet<String> allMonths = new TreeSet<String>();
            allMonths.addAll(summary.getMonthlyLost().keySet());
            allMonths.addAll(summary.getMonthlyFound().keySet());
            for (String month : allMonths) {
                months.add(new Object[]{
                        month,
                        valueOrZero(summary.getMonthlyLost(), month),
                        valueOrZero(summary.getMonthlyFound(), month)});
            }
            monthlyModel.setRows(months);

        }, error -> Async.showError(this, error, "Could not build the report"));
    }

    private int valueOrZero(Map<String, Integer> map, String key) {
        Integer value = map.get(key);
        return value == null ? 0 : value;
    }

    // ==================================================================
    //  Export
    // ==================================================================

    private void exportSummary() {
        if (summary == null) {
            JOptionPane.showMessageDialog(this,
                    "The report is still loading. Please wait a moment and try again.",
                    "Export not ready", JOptionPane.WARNING_MESSAGE);
            return;
        }
        File file = chooseFile("lost-and-found-summary.csv");
        if (file == null) {
            return;
        }
        final ReportSummary snapshot = summary;
        Async.run(() -> {
            try {
                reportService.exportSummary(file, snapshot);
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
            return Boolean.TRUE;
        }, ignored -> JOptionPane.showMessageDialog(this,
                        "Summary exported to:\n" + file.getAbsolutePath(),
                        "Export complete", JOptionPane.INFORMATION_MESSAGE),
                error -> showExportError(error));
    }

    private void exportItems() {
        File file = chooseFile("lost-and-found-items.csv");
        if (file == null) {
            return;
        }
        Async.run(() -> {
            try {
                List<Item> items = reportService.allItems();
                reportService.exportItems(file, items);
                return items.size();
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }, count -> JOptionPane.showMessageDialog(this,
                        count + " reports exported to:\n" + file.getAbsolutePath(),
                        "Export complete", JOptionPane.INFORMATION_MESSAGE),
                error -> showExportError(error));
    }

    private void showExportError(Throwable error) {
        Throwable cause = error instanceof UncheckedIOException && error.getCause() != null
                ? error.getCause() : error;
        if (cause instanceof IOException) {
            JOptionPane.showMessageDialog(this,
                    "Could not write the file: " + cause.getMessage(),
                    "Export failed", JOptionPane.ERROR_MESSAGE);
        } else {
            Async.showError(this, cause, "Could not export the reports");
        }
    }

    private File chooseFile(String defaultName) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save CSV");
        chooser.setSelectedFile(new File(defaultName));
        chooser.setFileFilter(new FileNameExtensionFilter("CSV files (*.csv)", "csv"));

        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        File selected = chooser.getSelectedFile();
        if (!selected.getName().toLowerCase().endsWith(".csv")) {
            selected = new File(selected.getParentFile(), selected.getName() + ".csv");
        }
        // Check the destination before starting a background write.
        File parent = selected.getParentFile();
        if (parent == null || !parent.exists() || !parent.canWrite()) {
            JOptionPane.showMessageDialog(this,
                    "The selected folder does not exist or is not writable.",
                    "Export failed", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        if (selected.exists() && !selected.canWrite()) {
            JOptionPane.showMessageDialog(this,
                    "That file is read-only. Choose another name.",
                    "Export failed", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        return selected;
    }
}
