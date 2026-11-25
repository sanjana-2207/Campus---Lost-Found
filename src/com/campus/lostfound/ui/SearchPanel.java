package com.campus.lostfound.ui;

import com.campus.lostfound.model.Category;
import com.campus.lostfound.model.Item;
import com.campus.lostfound.model.ItemFilter;
import com.campus.lostfound.model.ItemType;
import com.campus.lostfound.model.Location;
import com.campus.lostfound.service.ClaimService;
import com.campus.lostfound.service.ItemService;
import com.campus.lostfound.util.Async;
import com.campus.lostfound.util.DateFormats;
import com.campus.lostfound.util.ImageStore;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerDateModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
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
    private final Runnable onBack;

    private final JTextField keywordField = UIStyle.textField();
    private final JComboBox<String> categoryBox = UIStyle.comboBox();
    private final JComboBox<String> locationBox = UIStyle.comboBox();
    private final JComboBox<String> dateModeBox = UIStyle.comboBox();
    private final JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
    private final JComboBox<String> timeModeBox = UIStyle.comboBox();
    private final JSpinner timeSpinner = new JSpinner(new SpinnerDateModel());

    private final Map<String, Integer> categoryIds = new HashMap<String, Integer>();
    private final Map<String, Integer> locationIds = new HashMap<String, Integer>();

    private final ListTableModel tableModel = new ListTableModel(new String[]{
            "ID", "Photo", "Title", "Category", "Location", "Date", "Reported at",
            "Reported by"});
    private final JTable table = new JTable(tableModel);
    private final JLabel resultLabel = new JLabel(" ");
    private final List<Item> currentItems = new ArrayList<Item>();

    private boolean lookupsLoaded;
    private boolean loading;

    public SearchPanel(ItemService itemService, ClaimService claimService,
                       Runnable onBack) {
        this.itemService = itemService;
        this.claimService = claimService;
        this.onBack = onBack;

        setLayout(new BorderLayout(0, 16));
        setBackground(UIStyle.BG);

        dateModeBox.addItem("Any date");
        dateModeBox.addItem("On");
        dateModeBox.addItem("Before");
        dateModeBox.addItem("After");
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "dd-MM-yyyy"));
        dateSpinner.setValue(Date.from(
                LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()));
        dateSpinner.setEnabled(false);
        dateModeBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                dateSpinner.setEnabled(!"Any date".equals(dateModeBox.getSelectedItem()));
            }
        });

        timeModeBox.addItem("Any time");
        timeModeBox.addItem("After");
        timeModeBox.addItem("Before");
        timeSpinner.setEditor(new JSpinner.DateEditor(timeSpinner, "hh:mm a"));
        timeSpinner.setValue(new Date());
        timeSpinner.setEnabled(false);
        UIStyle.styleSpinner(dateSpinner);
        UIStyle.styleSpinner(timeSpinner);
        timeModeBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                timeSpinner.setEnabled(!"Any time".equals(timeModeBox.getSelectedItem()));
            }
        });

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

        JLabel title = UIStyle.heading("Found Items");
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel subtitle = UIStyle.subtitle(
                "Search and claim items reported on campus. Filter by keyword, "
                        + "category, location, date and time.");
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        header.add(title);
        header.add(Box.createVerticalStrut(2));
        header.add(subtitle);
        header.add(Box.createVerticalStrut(8));
        header.add(UIStyle.backLink(onBack));
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
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JPanel firstRow = new JPanel(new BorderLayout(10, 0));
        firstRow.setOpaque(false);
        firstRow.setAlignmentX(Component.LEFT_ALIGNMENT);
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

        JPanel secondRow = new JPanel(new GridLayout(1, 2, 12, 0));
        secondRow.setOpaque(false);
        secondRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        secondRow.add(UIStyle.labeled("Category", categoryBox));
        secondRow.add(UIStyle.labeled("Location", locationBox));

        JPanel dateControls = new JPanel(new GridLayout(1, 2, 10, 0));
        dateControls.setOpaque(false);
        dateControls.add(dateModeBox);
        dateControls.add(dateSpinner);

        JPanel timeControls = new JPanel(new GridLayout(1, 2, 10, 0));
        timeControls.setOpaque(false);
        timeControls.add(timeModeBox);
        timeControls.add(timeSpinner);

        JPanel dateRow = new JPanel(new GridLayout(1, 2, 12, 0));
        dateRow.setOpaque(false);
        dateRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        dateRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));
        dateRow.add(UIStyle.labeled("Date", dateControls));
        dateRow.add(UIStyle.labeled("Time", timeControls));

        card.add(firstRow);
        card.add(Box.createVerticalStrut(12));
        card.add(secondRow);
        card.add(Box.createVerticalStrut(12));
        card.add(dateRow);

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
        table.setRowHeight(56);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setPreferredWidth(45);
        table.getColumnModel().getColumn(1).setPreferredWidth(70);
        table.getColumnModel().getColumn(1).setCellRenderer(new PhotoCellRenderer());
        table.getColumnModel().getColumn(2).setPreferredWidth(240);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        table.getColumnModel().getColumn(4).setPreferredWidth(100);
        table.getColumnModel().getColumn(5).setPreferredWidth(90);
        table.getColumnModel().getColumn(6).setPreferredWidth(140);
        table.getColumnModel().getColumn(7).setPreferredWidth(120);

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
        // Search and claim is for claimable found items only. Lost reports have
        // nothing to hand over, and reserved/recovered items are no longer
        // offered for new claims.
        ItemFilter filter = new ItemFilter()
                .setKeyword(keywordField.getText())
                .setItemType(ItemType.FOUND)
                .setClaimableOnly(true);

        String category = (String) categoryBox.getSelectedItem();
        if (category != null && categoryIds.containsKey(category)) {
            filter.setCategoryId(categoryIds.get(category));
        }
        String location = (String) locationBox.getSelectedItem();
        if (location != null && locationIds.containsKey(location)) {
            filter.setLocationId(locationIds.get(location));
        }
        String dateMode = (String) dateModeBox.getSelectedItem();
        if (dateMode != null && !"Any date".equals(dateMode)) {
            LocalDate chosen = selectedDate();
            if ("On".equals(dateMode)) {
                filter.setFromDate(chosen).setToDate(chosen);
            } else if ("Before".equals(dateMode)) {
                filter.setToDate(chosen);
            } else if ("After".equals(dateMode)) {
                filter.setFromDate(chosen);
            }
        }
        String timeMode = (String) timeModeBox.getSelectedItem();
        if ("After".equals(timeMode)) {
            filter.setFromTime(selectedTime());
        } else if ("Before".equals(timeMode)) {
            filter.setToTime(selectedTime());
        }
        return filter;
    }

    private LocalDate selectedDate() {
        Date value = (Date) dateSpinner.getValue();
        return value.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private LocalTime selectedTime() {
        Date value = (Date) timeSpinner.getValue();
        return value.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
    }

    private void applyItems(List<Item> items) {
        currentItems.clear();
        currentItems.addAll(items);

        List<Object[]> rows = new ArrayList<Object[]>();
        for (Item item : items) {
            rows.add(new Object[]{
                    item.getItemId(),
                    ImageStore.loadForItem(item.getItemId(), 56, 44),
                    item.getTitle(),
                    item.getCategoryName(),
                    item.getLocationName(),
                    DateFormats.date(item.getEventDate()),
                    DateFormats.dateTime(item.getCreatedAt()),
                    item.getReporterName()});
        }
        tableModel.setRows(rows);
        resultLabel.setText(items.size() + (items.size() == 1 ? " item" : " items")
                + " available to claim");
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

    /** Used by the top-bar global search. The shell refreshes this screen next. */
    public void searchKeyword(String keyword) {
        keywordField.setText(keyword == null ? "" : keyword);
    }

    private void resetFilters() {
        keywordField.setText("");
        if (categoryBox.getItemCount() > 0) {
            categoryBox.setSelectedIndex(0);
        }
        if (locationBox.getItemCount() > 0) {
            locationBox.setSelectedIndex(0);
        }
        dateModeBox.setSelectedIndex(0);
        dateSpinner.setValue(Date.from(
                LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()));
        dateSpinner.setEnabled(false);
        timeModeBox.setSelectedIndex(0);
        timeSpinner.setValue(new Date());
        timeSpinner.setEnabled(false);
        UIStyle.styleSpinner(dateSpinner);
        UIStyle.styleSpinner(timeSpinner);
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
        boolean claimed = ItemDetailDialog.open(this, item, claimService, itemService);
        if (claimed) {
            runSearch();
        }
    }
}
