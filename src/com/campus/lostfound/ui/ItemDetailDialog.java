package com.campus.lostfound.ui;

import com.campus.lostfound.exception.ValidationException;
import com.campus.lostfound.model.Item;
import com.campus.lostfound.model.ItemStatus;
import com.campus.lostfound.service.ClaimService;
import com.campus.lostfound.util.SessionContext;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
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
import java.awt.Color;
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
    private boolean claimSubmitted;

    private final JLabel messageLabel = new JLabel(" ");

    private ItemDetailDialog(Window owner, Item item, ClaimService claimService) {
        super(owner, "Item #" + item.getItemId() + " details",
                ModalityType.APPLICATION_MODAL);
        this.item = item;
        this.claimService = claimService;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(600, 660);
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
    public static boolean open(Component parent, Item item, ClaimService claimService) {
        Window owner = SwingUtilities.getWindowAncestor(parent);
        ItemDetailDialog dialog = new ItemDetailDialog(owner, item, claimService);
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

        JLabel title = new JLabel("<html><body style='width:480px'>"
                + escape(item.getTitle()) + "</body></html>");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(UIStyle.TEXT);
        top.add(title, BorderLayout.CENTER);

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
                        ? "" : "  (" + item.getLocation().getBuilding() + ")"));
        row = addRow(grid, constraints, row, "Date", String.valueOf(item.getEventDate()));
        row = addRow(grid, constraints, row, "Status", item.getStatus().getLabel());
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
        descriptionArea.setBackground(new Color(0xF8FAFC));
        descriptionArea.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        description.add(descriptionArea, BorderLayout.CENTER);

        JTextArea extraArea = new JTextArea(item.getSpecificDetails());
        extraArea.setEditable(false);
        extraArea.setLineWrap(true);
        extraArea.setWrapStyleWord(true);
        extraArea.setFont(UIStyle.FONT);
        extraArea.setForeground(UIStyle.MUTED);
        extraArea.setBackground(new Color(0xF8FAFC));
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
        boolean claimable = (item.getStatus() == ItemStatus.OPEN
                || item.getStatus() == ItemStatus.MATCHED) && !ownItem;

        if (!claimable) {
            JLabel note = UIStyle.muted(ownItem
                    ? "You reported this item, so you cannot claim it."
                    : "This item is no longer open for claims.");
            note.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
            area.add(note, BorderLayout.NORTH);
            return area;
        }

        JPanel heading = new JPanel(new BorderLayout());
        heading.setOpaque(false);
        JLabel title = new JLabel("Is this yours?");
        title.setFont(UIStyle.FONT_BOLD.deriveFont(14f));
        title.setForeground(UIStyle.TEXT);
        heading.add(title, BorderLayout.WEST);
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
        try {
            claimService.submitClaim(item, SessionContext.getCurrentUser(), proof);
            claimSubmitted = true;

            JOptionPane.showMessageDialog(this,
                    "Your claim has been submitted.\n"
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
