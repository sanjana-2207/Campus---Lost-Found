package com.campus.lostfound.ui;

import com.campus.lostfound.model.Item;
import com.campus.lostfound.model.MatchResult;
import com.campus.lostfound.service.ClaimService;
import com.campus.lostfound.service.ItemService;
import com.campus.lostfound.service.MatchService;
import com.campus.lostfound.util.Async;
import com.campus.lostfound.util.DateFormats;

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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * FINDIT-style Possible Matches screen. The pairs come from the SQL matching
 * view; double-clicking a row opens the found report so an owner can claim it.
 */
public class MatchesPanel extends JPanel implements Refreshable {

    private static final long serialVersionUID = 1L;

    private final MatchService matchService;
    private final ItemService itemService;
    private final ClaimService claimService;
    private final Runnable onBack;

    private final ListTableModel tableModel = new ListTableModel(new String[]{
            "Lost report", "Found report", "Category", "Location",
            "Lost date", "Found date", "Score", "Confidence"});
    private final JTable table = new JTable(tableModel);
    private final List<MatchResult> currentMatches = new ArrayList<MatchResult>();
    private final JLabel resultLabel = new JLabel(" ");

    public MatchesPanel(MatchService matchService, ItemService itemService,
                        ClaimService claimService, Runnable onBack) {
        this.matchService = matchService;
        this.itemService = itemService;
        this.claimService = claimService;
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

        JLabel title = UIStyle.heading("Possible Matches");
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel subtitle = UIStyle.subtitle(
                "Lost and found reports that look like the same item.");
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        text.add(title);
        text.add(Box.createVerticalStrut(2));
        text.add(subtitle);
        text.add(Box.createVerticalStrut(8));
        text.add(UIStyle.backLink(onBack));
        header.add(text, BorderLayout.WEST);

        JButton refresh = UIStyle.secondaryButton("Refresh");
        refresh.addActionListener(event -> refreshData());
        header.add(refresh, BorderLayout.EAST);
        return header;
    }

    private JComponent buildBody() {
        JPanel card = UIStyle.card();
        card.setLayout(new BorderLayout(0, 10));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel heading = new JLabel("Suggested matches");
        heading.setFont(UIStyle.FONT_BOLD.deriveFont(15f));
        heading.setForeground(UIStyle.TEXT);
        resultLabel.setFont(UIStyle.FONT_SMALL);
        resultLabel.setForeground(UIStyle.MUTED);
        top.add(heading, BorderLayout.WEST);
        top.add(resultLabel, BorderLayout.EAST);
        card.add(top, BorderLayout.NORTH);

        UIStyle.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setPreferredWidth(220);
        table.getColumnModel().getColumn(1).setPreferredWidth(220);
        table.getColumnModel().getColumn(2).setPreferredWidth(110);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        table.getColumnModel().getColumn(4).setPreferredWidth(95);
        table.getColumnModel().getColumn(5).setPreferredWidth(95);
        table.getColumnModel().getColumn(6).setPreferredWidth(60);
        table.getColumnModel().getColumn(7).setPreferredWidth(90);
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2) {
                    openSelectedMatch();
                }
            }
        });

        card.add(UIStyle.scroll(table), BorderLayout.CENTER);

        JLabel hint = UIStyle.muted(
                "Double-click a match to open the found report and claim it.");
        card.add(hint, BorderLayout.SOUTH);
        return card;
    }

    @Override
    public void refreshData() {
        Async.run(matchService::findMatches, matches -> {
            currentMatches.clear();
            currentMatches.addAll(matches);

            List<Object[]> rows = new ArrayList<Object[]>();
            for (MatchResult match : matches) {
                rows.add(new Object[]{
                        "#" + match.getLostItemId() + "  " + match.getLostTitle(),
                        "#" + match.getFoundItemId() + "  " + match.getFoundTitle(),
                        match.getCategoryName(),
                        match.getLocationName(),
                        DateFormats.date(match.getLostDate()),
                        DateFormats.date(match.getFoundDate()),
                        match.getScore(),
                        match.getConfidence()});
            }
            tableModel.setRows(rows);
            resultLabel.setText(matches.size()
                    + (matches.size() == 1 ? " match" : " matches"));
        }, error -> Async.showError(this, error, "Could not load possible matches"));
    }

    private void openSelectedMatch() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Select a match first.", "Nothing selected",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        if (modelRow < 0 || modelRow >= currentMatches.size()) {
            return;
        }
        MatchResult match = currentMatches.get(modelRow);
        Item item = itemService.findById(match.getFoundItemId());
        if (item == null) {
            JOptionPane.showMessageDialog(this,
                    "That found report no longer exists.", "Not found",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (ItemDetailDialog.open(this, item, claimService, itemService)) {
            refreshData();
        }
    }
}
