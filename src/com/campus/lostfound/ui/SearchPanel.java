package com.campus.lostfound.ui;

import com.campus.lostfound.model.Category;
import com.campus.lostfound.model.Item;
import com.campus.lostfound.model.ItemFilter;
import com.campus.lostfound.model.ItemStatus;
import com.campus.lostfound.model.ItemType;
import com.campus.lostfound.model.Location;
import com.campus.lostfound.service.ClaimService;
import com.campus.lostfound.service.ItemService;
import com.campus.lostfound.util.Async;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Search and filter screen.
 *
 * <p>All filtering happens in SQL through {@link ItemFilter}; the table only
 * renders the rows that come back. Double-clicking a row opens the detail dialog
 * where a claim can be filed.</p>
 */
public class SearchPanel extends JPanel implements Refreshable {

    private static final long serialVersionUID = 1L;

    private final ItemService itemService;
    private final ClaimService claimService;

    private final JTextField keywordField = UIStyle.textField();
    private final JComboBox<String> categoryBox = UIStyle.comboBox();
    private final JComboBox<String> locationBox = UIStyle.comboBox();
    private final JComboBox<String> typeBox = UIStyle.comboBox();
    private final JComboBox<String> statusBox = UIStyle.comboBox();

    private final Map<String, Integer> categoryIds = new HashMap<String, Integer>();
    private final Map<String, Integer> locationIds = new HashMap<String, Integer>();
    private final Map<String, ItemStatus> statusValues = new HashMap<String, ItemStatus>();

    private final ListTableModel tableModel = new ListTableModel(new String[]{
            "ID", "Type", "Title", "Category", "Location", "Date", "Status", "Reported by"});
    private final JTable table = new JTable(tableModel);
    private final JLabel resultLabel = new JLabel(" ");
    private final List<Item> currentItems = new ArrayList<Item>();

    private boolean lookupsLoaded;
    private boolean loading;

    public SearchPanel(ItemService itemService, ClaimService claimService) {
        this.itemService = itemService;
        this.claimService = claimService;

        setLayout(new BorderLayout(0, 16));
        setBackground(UIStyle.BG);

        typeBox.addItem("All types");
        typeBox.addItem("Lost");
        typeBox.addItem("Found");

        statusBox.addItem("Any status");
        for (ItemStatus status : ItemStatus.values()) {
            statusBox.addItem(status.getLabel());
            statusValues.put(status.getLabel(), status);
        }

        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);
    }

    // ==================================================================
    //  Layout
    // ==================================================================

    private JComponent buildHeader() {
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel title = UIStyle.heading("Search and claim");
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel subtitle = UIStyle.subtitle(
                "Filter by category, location and status, then open a report to claim it.");
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        header.add(title);
        header.add(Box.createVerticalStrut(4));
        header.add(subtitle);
        return header;
    }

    private JComponent buildBody() {
        JPanel body = new JPanel(new BorderLayout(0, 14));
        body.setOpaque(false);
        body.add(buildFilters(), BorderLayout.NORTH);
        body.add(buildResultsCard(), BorderLayout.CENTER);
        return body;
    }

    private JComponent buildFilters() {
        JPanel card = UIStyle.card();
        card.setLayout(new BorderLayout(0, 12));

        JPanel firstRow = new JPanel(new BorderLayout(10, 0));
        firstRow.setOpaque(false);
        firstRow.add(UIStyle.labeled("Search by keyword (title or description)", keywordField),
                BorderLayout.CENTER);

        JPanel buttons = new JPanel();
        buttons.setOpaque(false);
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));

        JButton search = UIStyle.primaryButton("Search");
        search.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                runSearch();
            }
        });

        JButton reset = UIStyle.secondaryButton("Reset");
        reset.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                resetFilters();
            }
        });

        buttons.add(Box.createHorizontalGlue());
        buttons.add(search);
        buttons.add(Box.createHorizontalStrut(8));
        buttons.add(reset);
        firstRow.add(buttons, BorderLayout.EAST);

        JPanel secondRow = new JPanel(new GridLayout(1, 4, 12, 0));
        secondRow.setOpaque(false);
        secondRow.add(UIStyle.labeled("Category", categoryBox));
        secondRow.add(UIStyle.labeled("Location", locationBox));
        secondRow.add(UIStyle.labeled("Report type", typeBox));
        secondRow.add(UIStyle.labeled("Status", statusBox));

        card.add(firstRow, BorderLayout.NORTH);
        card.add(secondRow, BorderLayout.CENTER);

        keywordField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                runSearch();
            }
        });
        return card;
    }

    private JComponent buildResultsCard() {
        JPanel card = UIStyle.card();
        card.setLayout(new BorderLayout(0, 10));

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);

        JLabel title = new JLabel("Results");
        title.setFont(UIStyle.FONT_BOLD.deriveFont(15f));
        title.setForeground(UIStyle.TEXT);

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.X_AXIS));
        left.add(title);
        left.add(Box.createHorizontalStrut(12));
        resultLabel.setFont(UIStyle.FONT_SMALL);
        resultLabel.setForeground(UIStyle.MUTED);
        left.add(resultLabel);
        titleRow.add(left, BorderLayout.WEST);

        JButton view = UIStyle.secondaryButton("View details / claim");
        view.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                openSelectedRow();
            }
        });
        titleRow.add(view, BorderLayout.EAST);
        card.add(titleRow, BorderLayout.NORTH);

        UIStyle.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setPreferredWidth(45);
        table.getColumnModel().getColumn(1).setPreferredWidth(55);
        table.getColumnModel().getColumn(2).setPreferredWidth(300);
        table.getColumnModel().getColumn(3).setPreferredWidth(115);
        table.getColumnModel().getColumn(4).setPreferredWidth(115);
        table.getColumnModel().getColumn(5).setPreferredWidth(95);
        table.getColumnModel().getColumn(6).setPreferredWidth(85);
        table.getColumnModel().getColumn(6).setCellRenderer(new StatusCellRenderer());
        table.getColumnModel().getColumn(7).setPreferredWidth(140);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2) {
                    openSelectedRow();
                }
            }
        });

        card.add(UIStyle.scroll(table), BorderLayout.CENTER);
        return card;
    }

    // ==================================================================
    //  Behaviour
    // ==================================================================

    @Override
    public void refreshData() {
        if (loading) {
            return;
        }
        loading = true;
        final ItemFilter filter = buildFilter();

        Async.run(() -> new LookupSnapshot(
                        lookupsLoaded ? null : itemService.categories(),
                        lookupsLoaded ? null : itemService.locations(),
                        itemService.search(filter)),
                snapshot -> {
                    loading = false;
                    if (snapshot.categories != null && snapshot.locations != null) {
                        applyLookups(snapshot.categories, snapshot.locations);
                    }
                    applyItems(snapshot.items);
                },
                error -> {
                    loading = false;
                    Async.showError(this, error, "Could not load the reports");
                });
    }

    private void applyLookups(List<Category> categories, List<Location> locations) {
        categoryBox.removeAllItems();
        categoryIds.clear();
        categoryBox.addItem("All categories");
        for (Category category : categories) {
            categoryBox.addItem(category.getName());
            categoryIds.put(category.getName(), category.getCategoryId());
        }

        locationBox.removeAllItems();
        locationIds.clear();
        locationBox.addItem("All locations");
        for (Location location : locations) {
            locationBox.addItem(location.getName());
            locationIds.put(location.getName(), location.getLocationId());
        }
        lookupsLoaded = true;
    }

    private void runSearch() {
        if (loading) {
            return;
        }
        loading = true;
        final ItemFilter filter = buildFilter();

        Async.run(() -> itemService.search(filter), items -> {
            loading = false;
            applyItems(items);
        }, error -> {
            loading = false;
            Async.showError(this, error, "Search failed");
        });
    }

    private ItemFilter buildFilter() {
        ItemFilter filter = new ItemFilter().setKeyword(keywordField.getText());

        String category = (String) categoryBox.getSelectedItem();
        if (category != null && categoryIds.containsKey(category)) {
            filter.setCategoryId(categoryIds.get(category));
        }
        String location = (String) locationBox.getSelectedItem();
        if (location != null && locationIds.containsKey(location)) {
            filter.setLocationId(locationIds.get(location));
        }
        if ("Lost".equals(typeBox.getSelectedItem())) {
            filter.setItemType(ItemType.LOST);
        } else if ("Found".equals(typeBox.getSelectedItem())) {
            filter.setItemType(ItemType.FOUND);
        }
        String status = (String) statusBox.getSelectedItem();
        if (status != null && statusValues.containsKey(status)) {
            filter.setStatus(statusValues.get(status));
        }
        return filter;
    }

    private void applyItems(List<Item> items) {
        currentItems.clear();
        currentItems.addAll(items);

        List<Object[]> rows = new ArrayList<Object[]>();
        for (Item item : items) {
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
        resultLabel.setText(items.size() + (items.size() == 1 ? " report" : " reports")
                + " found");
    }

    /** Background result: lookups are null when they were already loaded. */
    private static final class LookupSnapshot {
        private final List<Category> categories;
        private final List<Location> locations;
        private final List<Item> items;

        private LookupSnapshot(List<Category> categories, List<Location> locations,
                               List<Item> items) {
            this.categories = categories;
            this.locations = locations;
            this.items = items;
        }
    }

    private void resetFilters() {
        keywordField.setText("");
        if (categoryBox.getItemCount() > 0) {
            categoryBox.setSelectedIndex(0);
        }
        if (locationBox.getItemCount() > 0) {
            locationBox.setSelectedIndex(0);
        }
        typeBox.setSelectedIndex(0);
        statusBox.setSelectedIndex(0);
        runSearch();
    }

    private void openSelectedRow() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Select a report in the table first.", "Nothing selected",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int modelRow = table.convertRowIndexToModel(viewRow);
        if (modelRow < 0 || modelRow >= currentItems.size()) {
            return;
        }

        Item item = currentItems.get(modelRow);
        boolean claimed = ItemDetailDialog.open(this, item, claimService);
        if (claimed) {
            runSearch();
        }
    }
}
