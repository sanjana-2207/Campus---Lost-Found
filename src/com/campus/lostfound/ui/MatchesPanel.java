package com.campus.lostfound.ui;

import com.campus.lostfound.model.Item;
import com.campus.lostfound.model.MatchResult;
import com.campus.lostfound.service.ClaimService;
import com.campus.lostfound.service.ItemService;
import com.campus.lostfound.service.MatchService;
import com.campus.lostfound.util.Async;

import javax.swing.BorderFactory;
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Shows the pairs produced by the SQL matching view.
 *
 * <p>Selecting a row explains the score in plain English, which makes the
 * feature easy to demonstrate: every match has a reason.</p>
 */
public class MatchesPanel extends JPanel implements Refreshable {

    private static final long serialVersionUID = 1L;

    private final MatchService matchService;
    private final ItemService itemService;
    private final ClaimService claimService;

    private final ListTableModel tableModel = new ListTableModel(new String[]{
            "Score", "Confidence", "Lost report", "Found report", "Category", "Location",
            "Lost date", "Found date", "Days"});
    private final JTable table = new JTable(tableModel);
    private final JLabel explanation = new JLabel("Select a match to see why it was suggested.");
    private final List<MatchResult> matches = new ArrayList<MatchResult>();

    public MatchesPanel(MatchService matchService, ItemService itemService,
                        ClaimService claimService) {
        this.matchService = matchService;
        this.itemService = itemService;
        this.claimService = claimService;

        setLayout(new BorderLayout(0, 16));
        setBackground(UIStyle.BG);

        add(buildHeader(), BorderLayout.NORTH);
        add(buildCard(), BorderLayout.CENTER);
    }

    private JComponent buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

        JLabel title = UIStyle.heading("Match suggestions");
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel subtitle = UIStyle.subtitle(
                "Lost and found reports with the same category and location, "
                + "scored by date proximity.");
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        text.add(title);
        text.add(Box.createVerticalStrut(4));
        text.add(subtitle);
        header.add(text, BorderLayout.WEST);

        JButton refresh = UIStyle.secondaryButton("Refresh matches");
        refresh.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                refreshData();
            }
        });
        header.add(refresh, BorderLayout.EAST);

        return header;
    }

    private JComponent buildCard() {
        JPanel card = UIStyle.card();
        card.setLayout(new BorderLayout(0, 10));

        JLabel note = UIStyle.muted(
                "Double-click a row to open the found item and submit a claim.");
        card.add(note, BorderLayout.NORTH);

        UIStyle.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setPreferredWidth(55);
        table.getColumnModel().getColumn(1).setPreferredWidth(85);
        table.getColumnModel().getColumn(2).setPreferredWidth(230);
        table.getColumnModel().getColumn(3).setPreferredWidth(230);
        table.getColumnModel().getColumn(4).setPreferredWidth(110);
        table.getColumnModel().getColumn(5).setPreferredWidth(110);
        table.getColumnModel().getColumn(6).setPreferredWidth(95);
        table.getColumnModel().getColumn(7).setPreferredWidth(95);
        table.getColumnModel().getColumn(8).setPreferredWidth(50);
        table.getColumnModel().getColumn(0).setCellRenderer(new StatusCellRenderer());

        table.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                showExplanation();
            }
        });
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2) {
                    openFoundItem();
                }
            }
        });

        card.add(UIStyle.scroll(table), BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout(12, 0));
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createEmptyBorder(6, 2, 0, 2));

        explanation.setFont(UIStyle.FONT_SMALL);
        explanation.setForeground(UIStyle.MUTED);
        footer.add(explanation, BorderLayout.CENTER);

        JButton open = UIStyle.primaryButton("Open found item");
        open.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                openFoundItem();
            }
        });
        footer.add(open, BorderLayout.EAST);

        card.add(footer, BorderLayout.SOUTH);
        return card;
    }

    @Override
    public void refreshData() {
        Async.run(matchService::findMatches, results -> {
            matches.clear();
            matches.addAll(results);

            List<Object[]> rows = new ArrayList<Object[]>();
            for (MatchResult match : results) {
                rows.add(new Object[]{
                        match.getScore(),
                        match.getConfidence(),
                        "#" + match.getLostItemId() + "  " + match.getLostTitle(),
                        "#" + match.getFoundItemId() + "  " + match.getFoundTitle(),
                        match.getCategoryName(),
                        match.getLocationName(),
                        match.getLostDate(),
                        match.getFoundDate(),
                        match.getDaysApart()});
            }
            tableModel.setRows(rows);

            explanation.setText(results.isEmpty()
                    ? "No matches right now. Report more items or relax the filters."
                    : results.size() + " suggested matches. Select a row for the reason.");
        }, error -> Async.showError(this, error, "Could not load the matches"));
    }

    private void showExplanation() {
        MatchResult match = selectedMatch();
        if (match != null) {
            explanation.setText(matchService.explain(match));
        }
    }

    private MatchResult selectedMatch() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        return modelRow >= 0 && modelRow < matches.size() ? matches.get(modelRow) : null;
    }

    private void openFoundItem() {
        MatchResult match = selectedMatch();
        if (match == null) {
            JOptionPane.showMessageDialog(this,
                    "Select a match in the table first.", "Nothing selected",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        final int foundItemId = match.getFoundItemId();
        Async.run(() -> itemService.findById(foundItemId), foundItem -> {
            if (foundItem == null) {
                JOptionPane.showMessageDialog(this,
                        "That found report no longer exists.", "Not found",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            boolean claimed = ItemDetailDialog.open(this, foundItem, claimService);
            if (claimed) {
                refreshData();
            }
        }, error -> Async.showError(this, error, "Could not open the found report"));
    }
}
