package com.campus.lostfound.ui;

import com.campus.lostfound.exception.ValidationException;
import com.campus.lostfound.model.Item;
import com.campus.lostfound.model.ItemStatus;
import com.campus.lostfound.model.ItemType;
import com.campus.lostfound.service.ClaimService;
import com.campus.lostfound.service.ItemService;
import com.campus.lostfound.util.DateFormats;
import com.campus.lostfound.util.ImageStore;
import com.campus.lostfound.util.SessionContext;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Modal dialog showing every detail of one report.
 *
 * <p>If the signed-in user is allowed to claim the item, the dialog also hosts
 * the proof-of-ownership form. Returns {@code true} when a claim was submitted
 * so the search screen can refresh itself.</p>
 */
public class ItemDetailDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private final Item item;
    private final ClaimService claimService;
    private final ItemService itemService;
    private boolean claimSubmitted;

    private final JLabel messageLabel = new JLabel(" ");

    private ItemDetailDialog(Window owner, Item item, ClaimService claimService,
                             ItemService itemService) {
        super(owner, "Item #" + item.getItemId() + " details",
                ModalityType.APPLICATION_MODAL);
        this.item = item;
        this.claimService = claimService;
        this.itemService = itemService;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(700, 680);
        setLocationRelativeTo(owner);

        JPanel content = new JPanel(new BorderLayout(0, 14));
        content.setBackground(UIStyle.CARD);
        content.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        content.add(buildTop(), BorderLayout.NORTH);
        content.add(buildDetails(), BorderLayout.CENTER);
        content.add(buildClaimArea(), BorderLayout.SOUTH);

        setContentPane(content);
    }

    /** Shows the dialog and reports whether a claim was filed. */
    public static boolean open(Component parent, Item item, ClaimService claimService,
                               ItemService itemService) {
        Window owner = SwingUtilities.getWindowAncestor(parent);
        ItemDetailDialog dialog = new ItemDetailDialog(owner, item, claimService, itemService);
        dialog.setVisible(true);
        return dialog.claimSubmitted;
    }

    // ==================================================================
    //  Layout
    // ==================================================================

    private JComponent buildTop() {
        JPanel top = new JPanel(new BorderLayout(0, 6));
        top.setOpaque(false);

        JLabel type = new JLabel(item.getItemType().getLabel().toUpperCase() + " ITEM");
        type.setFont(UIStyle.FONT_LABEL);
        type.setForeground(item.getItemType().name().equals("LOST")
                ? UIStyle.WARNING : UIStyle.ACCENT_DARK);
        top.add(type, BorderLayout.NORTH);

        JLabel title = new JLabel("<html><body style='width:400px'>"
                + escape(item.getTitle()) + "</body></html>");
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setForeground(UIStyle.TEXT);
        top.add(title, BorderLayout.CENTER);

        ImageIcon photo = ImageStore.loadForItem(item.getItemId(), 180, 140);
        if (photo != null) {
            JLabel photoLabel = new JLabel(photo);
            photoLabel.setBorder(BorderFactory.createLineBorder(UIStyle.BORDER));
            top.add(photoLabel, BorderLayout.EAST);
        }
        return top;
    }

    private JComponent buildDetails() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 12));
        wrapper.setOpaque(false);

        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(3, 0, 3, 14);
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        row = addRow(grid, constraints, row, "Category", item.getCategoryName());
        row = addRow(grid, constraints, row, "Location", item.getLocationName()
                + (item.getLocation() == null || item.getLocation().getBuilding() == null
                        || item.getLocation().getBuilding().isEmpty()
                        ? "" : "  (" + item.getLocation().getBuilding() + ")"));
        row = addRow(grid, constraints, row, "Date", DateFormats.date(item.getEventDate()));
        row = addRow(grid, constraints, row, "Reported at", DateFormats.dateTime(item.getCreatedAt()));
        row = addRow(grid, constraints, row, "Reported by", item.getReporterName()
                + (item.getReportedBy() == null ? "" : "  ·  " + item.getReportedBy().getEmail()));
        row = addRow(grid, constraints, row, "Contact", item.getReportedBy() == null
                ? "-" : item.getReportedBy().getPhone());

        wrapper.add(grid, BorderLayout.NORTH);

        JPanel description = new JPanel(new BorderLayout(0, 4));
        description.setOpaque(false);
        description.add(UIStyle.fieldLabel("Description"), BorderLayout.NORTH);

        JTextArea descriptionArea = new JTextArea(item.getDescription());
        descriptionArea.setEditable(false);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setFont(UIStyle.FONT);
        descriptionArea.setForeground(UIStyle.TEXT);
        descriptionArea.setBackground(UIStyle.blend(UIStyle.CARD, UIStyle.TEXT, 0.06f));
        descriptionArea.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        description.add(descriptionArea, BorderLayout.CENTER);

        JTextArea extraArea = new JTextArea(item.getSpecificDetails());
        extraArea.setEditable(false);
        extraArea.setLineWrap(true);
        extraArea.setWrapStyleWord(true);
        extraArea.setFont(UIStyle.FONT);
        extraArea.setForeground(UIStyle.MUTED);
        extraArea.setBackground(UIStyle.blend(UIStyle.CARD, UIStyle.TEXT, 0.06f));
        extraArea.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        JPanel textHolder = new JPanel(new BorderLayout(0, 8));
        textHolder.setOpaque(false);
        textHolder.add(description, BorderLayout.NORTH);
        textHolder.add(extraArea, BorderLayout.CENTER);

        wrapper.add(textHolder, BorderLayout.CENTER);
        return wrapper;
    }

    private JComponent buildClaimArea() {
        JPanel area = new JPanel(new BorderLayout(0, 8));
        area.setOpaque(false);
        area.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIStyle.BORDER));

        boolean ownItem = SessionContext.isLoggedIn()
                && item.getReportedBy() != null
                && item.getReportedBy().getUserId() == SessionContext.getCurrentUser().getUserId();
        boolean foundItem = item.getItemType() == ItemType.FOUND;
        boolean open = item.getStatus() == ItemStatus.OPEN
                || item.getStatus() == ItemStatus.MATCHED;
        boolean claimable = (foundItem && open && !ownItem)
                || (item.getItemType() == ItemType.LOST && open);

        if (!claimable) {
            String reason;
            if (ownItem && foundItem) {
                reason = "You reported this item, so you cannot claim it.";
            } else {
                reason = "This item is no longer open for claims.";
            }
            JLabel note = UIStyle.muted(reason);
            note.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
            area.add(note, BorderLayout.NORTH);
            return area;
        }

        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Found your item?");
        title.setFont(UIStyle.FONT_BOLD.deriveFont(14f));
        title.setForeground(UIStyle.TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel subtitle = UIStyle.muted(
                "Claim this item by providing proof of ownership.");
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        heading.add(title);
        heading.add(Box.createVerticalStrut(2));
        heading.add(subtitle);
        area.add(heading, BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(0, 8));
        body.setOpaque(false);
        body.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JTextArea proofArea = UIStyle.textArea();
        proofArea.setRows(3);
        proofArea.setToolTipText("Mention colour, marks, contents or anything only the owner knows.");
        proofArea.setPreferredSize(new Dimension(400, 70));

        JScrollPane scroll = new JScrollPane(proofArea);
        scroll.setBorder(BorderFactory.createLineBorder(UIStyle.BORDER));

        body.add(UIStyle.fieldLabel(
                "Proof of ownership *  (colour, marks, contents, when and where you lost it)"),
                BorderLayout.NORTH);
        body.add(scroll, BorderLayout.CENTER);

        JPanel actions = new JPanel(new BorderLayout(10, 0));
        actions.setOpaque(false);

        messageLabel.setFont(UIStyle.FONT_SMALL);
        messageLabel.setForeground(UIStyle.DANGER);
        actions.add(messageLabel, BorderLayout.CENTER);

        JButton submit = UIStyle.primaryButton("Submit ownership claim");
        submit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                submitClaim(proofArea.getText());
            }
        });
        actions.add(submit, BorderLayout.EAST);

        body.add(actions, BorderLayout.SOUTH);
        area.add(body, BorderLayout.CENTER);

        area.add(Box.createVerticalStrut(4), BorderLayout.SOUTH);
        return area;
    }

    private int addRow(JPanel panel, GridBagConstraints constraints, int row,
                       String label, String value) {
        constraints.gridy = row;
        constraints.gridx = 0;
        constraints.weightx = 0;
        JLabel name = new JLabel(label);
        name.setFont(UIStyle.FONT_LABEL);
        name.setForeground(UIStyle.MUTED);
        panel.add(name, constraints);

        constraints.gridx = 1;
        constraints.weightx = 1;
        JLabel text = new JLabel(value == null || value.isEmpty() ? "-" : value);
        text.setFont(UIStyle.FONT);
        text.setForeground(UIStyle.TEXT);
        panel.add(text, constraints);

        return row + 1;
    }

    // ==================================================================
    //  Action
    // ==================================================================

    private void submitClaim(String proof) {
        messageLabel.setText(" ");
        Item target = item;
        if (item.getItemType() == ItemType.LOST) {
            target = itemService.findClaimableMatchForLost(
                    item, SessionContext.getCurrentUser());
            if (target == null) {
                messageLabel.setText(
                        "No matching found item is available to claim yet.");
                return;
            }
        }
        try {
            claimService.submitClaim(target, SessionContext.getCurrentUser(), proof);
            claimSubmitted = true;

            JOptionPane.showMessageDialog(this,
                    "Your claim has been submitted"
                    + (target.getItemId() == item.getItemId()
                        ? ".\n" : " for \"" + target.getTitle() + "\".\n")
                    + "Security staff will verify it and update the status.",
                    "Claim submitted", JOptionPane.INFORMATION_MESSAGE);
            dispose();

        } catch (ValidationException ex) {
            messageLabel.setText(ex.getMessage());
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Database error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
