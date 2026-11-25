package com.campus.lostfound.ui;

import com.campus.lostfound.model.Item;
import com.campus.lostfound.service.ClaimService;
import com.campus.lostfound.service.ItemService;
import com.campus.lostfound.util.Async;
import com.campus.lostfound.util.DateFormats;
import com.campus.lostfound.util.ImageStore;
import com.campus.lostfound.util.SessionContext;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * FINDIT-style My Reports screen: every item the signed-in user has reported,
 * with its current status and the same detail dialog used elsewhere.
 */
public class MyReportsPanel extends JPanel implements Refreshable {

    private static final long serialVersionUID = 1L;

    private final ItemService itemService;
    private final ClaimService claimService;
    private final Runnable onReport;
    private final Runnable onBack;

    private final ListTableModel tableModel = new ListTableModel(new String[]{
            "ID", "Photo", "Type", "Title", "Category", "Location", "Date",
            "Reported at", "Status"});
    private final JTable table = new JTable(tableModel);
    private final List<Item> currentItems = new ArrayList<Item>();
    private final JLabel resultLabel = new JLabel(" ");

    public MyReportsPanel(ItemService itemService, ClaimService claimService,
                          Runnable onReport, Runnable onBack) {
        this.itemService = itemService;
        this.claimService = claimService;
        this.onReport = onReport;
        this.onBack = onBack;

        setLayout(new BorderLayout(0, 16));
        setBackground(UIStyle.BG);

        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);
    }

    private JComponent buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

        JLabel title = UIStyle.heading("My Reports");
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel subtitle = UIStyle.subtitle(
                "Items you have reported and their current status.");
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        text.add(title);
        text.add(Box.createVerticalStrut(2));
        text.add(subtitle);
        text.add(Box.createVerticalStrut(8));
        text.add(UIStyle.backLink(onBack));
        header.add(text, BorderLayout.WEST);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);

        JButton report = UIStyle.primaryButton("Report Found Item");
        report.addActionListener(event -> {
            if (onReport != null) {
                onReport.run();
            }
        });

        JButton refresh = UIStyle.secondaryButton("Refresh");
        refresh.addActionListener(event -> refreshData());

        buttons.add(report);
        buttons.add(refresh);
        header.add(buttons, BorderLayout.EAST);
        return header;
    }

    private JComponent buildBody() {
        JPanel card = UIStyle.card();
        card.setLayout(new BorderLayout(0, 10));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel heading = new JLabel("Reported items");
        heading.setFont(UIStyle.FONT_BOLD.deriveFont(15f));
        heading.setForeground(UIStyle.TEXT);
        resultLabel.setFont(UIStyle.FONT_SMALL);
        resultLabel.setForeground(UIStyle.MUTED);
        top.add(heading, BorderLayout.WEST);
        top.add(resultLabel, BorderLayout.EAST);
        card.add(top, BorderLayout.NORTH);

        UIStyle.styleTable(table);
        table.setRowHeight(56);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setPreferredWidth(45);
        table.getColumnModel().getColumn(1).setPreferredWidth(70);
        table.getColumnModel().getColumn(1).setCellRenderer(new PhotoCellRenderer());
        table.getColumnModel().getColumn(2).setPreferredWidth(60);
        table.getColumnModel().getColumn(3).setPreferredWidth(250);
        table.getColumnModel().getColumn(4).setPreferredWidth(105);
        table.getColumnModel().getColumn(5).setPreferredWidth(100);
        table.getColumnModel().getColumn(6).setPreferredWidth(90);
        table.getColumnModel().getColumn(7).setPreferredWidth(145);
        table.getColumnModel().getColumn(8).setPreferredWidth(85);
        table.getColumnModel().getColumn(8).setCellRenderer(new StatusCellRenderer());

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2) {
                    openSelectedReport();
                }
            }
        });

        card.add(UIStyle.scroll(table), BorderLayout.CENTER);

        JLabel hint = UIStyle.muted(
                "Double-click a report to see its details.");
        card.add(hint, BorderLayout.SOUTH);
        return card;
    }

    @Override
    public void refreshData() {
        if (SessionContext.getCurrentUser() == null) {
            currentItems.clear();
            tableModel.setRows(new ArrayList<Object[]>());
            resultLabel.setText("Not signed in");
            return;
        }
        final int userId = SessionContext.getCurrentUser().getUserId();
        Async.run(() -> itemService.reportsBy(userId), items -> {
            currentItems.clear();
            currentItems.addAll(items);

            List<Object[]> rows = new ArrayList<Object[]>();
            for (Item item : items) {
                rows.add(new Object[]{
                        item.getItemId(),
                        ImageStore.loadForItem(item.getItemId(), 56, 44),
                        item.getItemType().getLabel(),
                        item.getTitle(),
                        item.getCategoryName(),
                        item.getLocationName(),
                        DateFormats.date(item.getEventDate()),
                        DateFormats.dateTime(item.getCreatedAt()),
                        item.getStatus().getLabel()});
            }
            tableModel.setRows(rows);
            resultLabel.setText(items.size()
                    + (items.size() == 1 ? " report" : " reports"));
        }, error -> Async.showError(this, error, "Could not load your reports"));
    }

    private void openSelectedReport() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Select a report first.", "Nothing selected",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        if (modelRow < 0 || modelRow >= currentItems.size()) {
            return;
        }
        ItemDetailDialog.open(this, currentItems.get(modelRow), claimService, itemService);
    }
}
