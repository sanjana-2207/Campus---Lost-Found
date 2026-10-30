package com.campus.lostfound.ui;

import com.campus.lostfound.exception.ValidationException;
import com.campus.lostfound.model.Claim;
import com.campus.lostfound.model.ClaimStatus;
import com.campus.lostfound.service.ClaimService;
import com.campus.lostfound.util.SessionContext;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;

/**
 * Claim verification screen.
 *
 * <p>Staff and administrators see two tabs: every claim, and their own. Students
 * only see their own claims, which also keeps other students' proof text
 * private.</p>
 */
public class ClaimsPanel extends JPanel implements Refreshable {

    private static final long serialVersionUID = 1L;

    private final ClaimService claimService;

    private final ListTableModel allModel = new ListTableModel(new String[]{
            "ID", "Item", "Claimant", "Submitted", "Status", "Verified by", "Remarks"});
    private final ListTableModel myModel = new ListTableModel(new String[]{
            "ID", "Item", "Submitted", "Status", "Remarks", "Proof"});

    private final JTable allTable = new JTable(allModel);
    private final JTable myTable = new JTable(myModel);

    private final List<Claim> allClaims = new ArrayList<Claim>();
    private final List<Claim> myClaims = new ArrayList<Claim>();

    private final JButton approveButton = UIStyle.successButton("Approve");
    private final JButton rejectButton = UIStyle.dangerButton("Reject");
    private final JButton recoverButton = UIStyle.primaryButton("Mark recovered");

    public ClaimsPanel(ClaimService claimService) {
        this.claimService = claimService;

        setLayout(new BorderLayout(0, 16));
        setBackground(UIStyle.BG);

        add(buildHeader(), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UIStyle.FONT_BOLD);
        if (SessionContext.isVerifier()) {
            tabs.addTab("All claims", buildAllClaimsPanel());
        }
        tabs.addTab("My claims", buildMyClaimsPanel());
        add(tabs, BorderLayout.CENTER);

        wireActions();
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

        JLabel title = UIStyle.heading("Ownership claims");
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel subtitle = UIStyle.subtitle(SessionContext.isVerifier()
                ? "Verify proof of ownership, then hand the item over and mark it recovered."
                : "Track the claims you have submitted and their verification status.");
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        text.add(title);
        text.add(Box.createVerticalStrut(4));
        text.add(subtitle);
        header.add(text, BorderLayout.WEST);

        JButton refresh = UIStyle.secondaryButton("Refresh");
        refresh.addActionListener(event -> refreshData());
        header.add(refresh, BorderLayout.EAST);

        return header;
    }

    private JComponent buildAllClaimsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(UIStyle.BG);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 0, 0, 0));

        JPanel card = UIStyle.card();
        card.setLayout(new BorderLayout(0, 10));

        JLabel hint = UIStyle.muted(
                "Approve verifies the owner and reserves the item; mark recovered "
                + "records the physical handover.");
        card.add(hint, BorderLayout.NORTH);

        UIStyle.styleTable(allTable);
        allTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        allTable.getColumnModel().getColumn(0).setPreferredWidth(45);
        allTable.getColumnModel().getColumn(1).setPreferredWidth(240);
        allTable.getColumnModel().getColumn(2).setPreferredWidth(140);
        allTable.getColumnModel().getColumn(3).setPreferredWidth(130);
        allTable.getColumnModel().getColumn(4).setPreferredWidth(95);
        allTable.getColumnModel().getColumn(4).setCellRenderer(new StatusCellRenderer());
        allTable.getColumnModel().getColumn(5).setPreferredWidth(140);
        allTable.getColumnModel().getColumn(6).setPreferredWidth(260);

        card.add(UIStyle.scroll(allTable), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        actions.add(approveButton);
        actions.add(rejectButton);
        actions.add(recoverButton);
        card.add(actions, BorderLayout.SOUTH);

        panel.add(card, BorderLayout.CENTER);
        return panel;
    }

    private JComponent buildMyClaimsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(UIStyle.BG);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 0, 0, 0));

        JPanel card = UIStyle.card();
        card.setLayout(new BorderLayout(0, 10));

        card.add(UIStyle.muted(
                "A claim stays pending until staff verify your proof of ownership."),
                BorderLayout.NORTH);

        UIStyle.styleTable(myTable);
        myTable.getColumnModel().getColumn(0).setPreferredWidth(45);
        myTable.getColumnModel().getColumn(1).setPreferredWidth(280);
        myTable.getColumnModel().getColumn(2).setPreferredWidth(130);
        myTable.getColumnModel().getColumn(3).setPreferredWidth(95);
        myTable.getColumnModel().getColumn(3).setCellRenderer(new StatusCellRenderer());
        myTable.getColumnModel().getColumn(4).setPreferredWidth(240);
        myTable.getColumnModel().getColumn(5).setPreferredWidth(300);

        card.add(UIStyle.scroll(myTable), BorderLayout.CENTER);
        panel.add(card, BorderLayout.CENTER);
        return panel;
    }

    private void wireActions() {
        allTable.getSelectionModel().addListSelectionListener(event -> updateButtons());
        updateButtons();

        approveButton.addActionListener(event -> decide(true));
        rejectButton.addActionListener(event -> decide(false));
        recoverButton.addActionListener(event -> markRecovered());
    }

    // ==================================================================
    //  Data
    // ==================================================================

    @Override
    public void refreshData() {
        try {
            if (SessionContext.isVerifier()) {
                allClaims.clear();
                allClaims.addAll(claimService.allClaims());

                List<Object[]> allRows = new ArrayList<Object[]>();
                for (Claim claim : allClaims) {
                    allRows.add(new Object[]{
                            claim.getClaimId(),
                            "#" + claim.getItem().getItemId() + "  " + claim.getItemTitle(),
                            claim.getClaimantName(),
                            claim.getSubmittedAt(),
                            claim.getStatus().getLabel(),
                            claim.getVerifierName(),
                            claim.getRemarks()});
                }
                allModel.setRows(allRows);
            }

            if (SessionContext.isLoggedIn()) {
                myClaims.clear();
                myClaims.addAll(claimService.claimsOf(
                        SessionContext.getCurrentUser().getUserId()));

                List<Object[]> myRows = new ArrayList<Object[]>();
                for (Claim claim : myClaims) {
                    myRows.add(new Object[]{
                            claim.getClaimId(),
                            "#" + claim.getItem().getItemId() + "  " + claim.getItemTitle(),
                            claim.getSubmittedAt(),
                            claim.getStatus().getLabel(),
                            claim.getRemarks(),
                            claim.getProof()});
                }
                myModel.setRows(myRows);
            }

            updateButtons();

        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Could not load claims", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateButtons() {
        Claim claim = selectedClaim();
        boolean pending = claim != null && claim.getStatus() == ClaimStatus.PENDING;
        boolean approved = claim != null && claim.getStatus() == ClaimStatus.APPROVED;

        approveButton.setEnabled(pending);
        rejectButton.setEnabled(pending);
        recoverButton.setEnabled(approved);
    }

    private Claim selectedClaim() {
        int viewRow = allTable.getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        int modelRow = allTable.convertRowIndexToModel(viewRow);
        return modelRow >= 0 && modelRow < allClaims.size() ? allClaims.get(modelRow) : null;
    }

    // ==================================================================
    //  Actions
    // ==================================================================

    private void decide(boolean approve) {
        Claim claim = selectedClaim();
        if (claim == null) {
            return;
        }

        String title = (approve ? "Approve" : "Reject") + " claim #" + claim.getClaimId();
        String remarks = (String) JOptionPane.showInputDialog(this,
                "Remarks for " + claim.getClaimantName() + " (optional):",
                title, JOptionPane.PLAIN_MESSAGE, null, null,
                approve ? "Ownership verified." : "Description does not match the item.");

        if (remarks == null) {
            return; // user cancelled
        }

        try {
            if (approve) {
                claimService.approveClaim(claim, SessionContext.getCurrentUser(), remarks);
                JOptionPane.showMessageDialog(this,
                        "Claim approved. The item is now reserved for "
                        + claim.getClaimantName() + ".",
                        "Claim approved", JOptionPane.INFORMATION_MESSAGE);
            } else {
                claimService.rejectClaim(claim, SessionContext.getCurrentUser(), remarks);
                JOptionPane.showMessageDialog(this,
                        "Claim rejected. The item stays available for other claimants.",
                        "Claim rejected", JOptionPane.INFORMATION_MESSAGE);
            }
            refreshData();

        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Not allowed", JOptionPane.WARNING_MESSAGE);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Transaction failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void markRecovered() {
        Claim claim = selectedClaim();
        if (claim == null) {
            return;
        }

        int choice = JOptionPane.showConfirmDialog(this,
                "Confirm that the item was physically handed over to "
                + claim.getClaimantName() + "?",
                "Mark recovered", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            claimService.markRecovered(claim, SessionContext.getCurrentUser());
            JOptionPane.showMessageDialog(this,
                    "Item marked as recovered. The dashboard counters were updated.",
                    "Recovery recorded", JOptionPane.INFORMATION_MESSAGE);
            refreshData();

        } catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Not allowed", JOptionPane.WARNING_MESSAGE);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Transaction failed", JOptionPane.ERROR_MESSAGE);
        }
    }
}
