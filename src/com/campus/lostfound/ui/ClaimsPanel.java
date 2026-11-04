package com.campus.lostfound.ui;

import com.campus.lostfound.exception.ValidationException;
import com.campus.lostfound.model.Claim;
import com.campus.lostfound.model.ClaimStatus;
import com.campus.lostfound.service.ClaimService;
import com.campus.lostfound.util.Async;
import com.campus.lostfound.util.DateFormats;
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
    private final Runnable onBack;

    private final ListTableModel allModel = new ListTableModel(new String[]{
            "ID", "Item", "Claimant", "Submitted", "Status", "Verified by",
            "Proof of ownership", "Remarks"});
    private final ListTableModel myModel = new ListTableModel(new String[]{
            "ID", "Item", "Submitted", "Status", "Remarks", "Proof"});
    private final ListTableModel reportedModel = new ListTableModel(new String[]{
            "ID", "Item", "Claimant", "Submitted", "Claim status", "Remarks"});

    private final JTable allTable = new JTable(allModel);
    private final JTable myTable = new JTable(myModel);
    private final JTable reportedTable = new JTable(reportedModel);

    private final List<Claim> allClaims = new ArrayList<Claim>();
    private final List<Claim> myClaims = new ArrayList<Claim>();
    private final List<Claim> reportedClaims = new ArrayList<Claim>();

    private final JButton approveButton = UIStyle.successButton("Approve");
    private final JButton rejectButton = UIStyle.dangerButton("Reject");
    private final JButton recoverButton = UIStyle.primaryButton("Mark recovered");
    private final JButton confirmButton =
            UIStyle.successButton("Confirm owner received item");

    public ClaimsPanel(ClaimService claimService, Runnable onBack) {
        this.claimService = claimService;
        this.onBack = onBack;

        setLayout(new BorderLayout(0, 16));
        setBackground(UIStyle.BG);

        add(buildHeader(), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UIStyle.FONT_BOLD);
        if (SessionContext.isVerifier()) {
            tabs.addTab("All claims", buildAllClaimsPanel());
        }
        tabs.addTab("My claims", buildMyClaimsPanel());
        tabs.addTab("My reported items", buildReportedClaimsPanel());
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

        JLabel title = UIStyle.heading("Claims & Handovers");
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel subtitle = UIStyle.subtitle(SessionContext.isVerifier()
                ? "Verify proof of ownership; finders confirm the final handover."
                : "Track your claims, and confirm handovers for items you reported.");
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

    private JComponent buildAllClaimsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(UIStyle.BG);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 0, 0, 0));

        JPanel card = UIStyle.card();
        card.setLayout(new BorderLayout(0, 10));

        JLabel hint = UIStyle.muted(
                "Approve verifies the owner and reserves the item. The finder confirms "
                + "the physical handover from My reported items; mark recovered is a "
                + "staff override.");
        card.add(hint, BorderLayout.NORTH);

        UIStyle.styleTable(allTable);
        allTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        allTable.getColumnModel().getColumn(0).setPreferredWidth(45);
        allTable.getColumnModel().getColumn(1).setPreferredWidth(240);
        allTable.getColumnModel().getColumn(2).setPreferredWidth(140);
        allTable.getColumnModel().getColumn(3).setPreferredWidth(130);
        allTable.getColumnModel().getColumn(4).setPreferredWidth(95);
        allTable.getColumnModel().getColumn(4).setCellRenderer(new StatusCellRenderer());
        allTable.getColumnModel().getColumn(5).setPreferredWidth(130);
        allTable.getColumnModel().getColumn(6).setPreferredWidth(300);
        allTable.getColumnModel().getColumn(7).setPreferredWidth(220);

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

    private JComponent buildReportedClaimsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(UIStyle.BG);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 0, 0, 0));

        JPanel card = UIStyle.card();
        card.setLayout(new BorderLayout(0, 10));

        card.add(UIStyle.muted(
                "After staff approve a claim on an item you reported, confirm the "
                + "physical handover here. The item then leaves Search and claim and "
                + "stays only in Reports and analytics."),
                BorderLayout.NORTH);

        UIStyle.styleTable(reportedTable);
        reportedTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        reportedTable.getColumnModel().getColumn(0).setPreferredWidth(45);
        reportedTable.getColumnModel().getColumn(1).setPreferredWidth(260);
        reportedTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        reportedTable.getColumnModel().getColumn(3).setPreferredWidth(130);
        reportedTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        reportedTable.getColumnModel().getColumn(4).setCellRenderer(new StatusCellRenderer());
        reportedTable.getColumnModel().getColumn(5).setPreferredWidth(280);

        card.add(UIStyle.scroll(reportedTable), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        actions.add(confirmButton);
        card.add(actions, BorderLayout.SOUTH);

        panel.add(card, BorderLayout.CENTER);
        return panel;
    }

    private void wireActions() {
        allTable.getSelectionModel().addListSelectionListener(event -> updateButtons());
        reportedTable.getSelectionModel().addListSelectionListener(event -> updateButtons());
        updateButtons();

        approveButton.addActionListener(event -> decide(true));
        rejectButton.addActionListener(event -> decide(false));
        recoverButton.addActionListener(event -> markRecovered());
        confirmButton.addActionListener(event -> confirmHandover());
    }

    // ==================================================================
    //  Data
    // ==================================================================

    @Override
    public void refreshData() {
        final boolean verifier = SessionContext.isVerifier();
        final boolean loggedIn = SessionContext.isLoggedIn();
        final int userId = loggedIn ? SessionContext.getCurrentUser().getUserId() : 0;

        Async.run(() -> new Snapshot(
                        verifier ? claimService.allClaims() : new ArrayList<Claim>(),
                        loggedIn ? claimService.claimsOf(userId) : new ArrayList<Claim>(),
                        loggedIn ? claimService.claimsOnMyReports(userId)
                                : new ArrayList<Claim>()),
                snapshot -> {
                    allClaims.clear();
                    allClaims.addAll(snapshot.all);
                    myClaims.clear();
                    myClaims.addAll(snapshot.mine);
                    reportedClaims.clear();
                    reportedClaims.addAll(snapshot.reported);

                    List<Object[]> allRows = new ArrayList<Object[]>();
                    for (Claim claim : allClaims) {
                        allRows.add(new Object[]{
                                claim.getClaimId(),
                                "#" + claim.getItem().getItemId() + "  " + claim.getItemTitle(),
                                claim.getClaimantName(),
                                DateFormats.dateTime(claim.getSubmittedAt()),
                                claim.getStatus().getLabel(),
                                claim.getVerifierName(),
                                claim.getProof(),
                                claim.getRemarks()});
                    }
                    allModel.setRows(allRows);

                    List<Object[]> myRows = new ArrayList<Object[]>();
                    for (Claim claim : myClaims) {
                        myRows.add(new Object[]{
                                claim.getClaimId(),
                                "#" + claim.getItem().getItemId() + "  " + claim.getItemTitle(),
                                DateFormats.dateTime(claim.getSubmittedAt()),
                                claim.getStatus().getLabel(),
                                claim.getRemarks(),
                                claim.getProof()});
                    }
                    myModel.setRows(myRows);

                    List<Object[]> reportedRows = new ArrayList<Object[]>();
                    for (Claim claim : reportedClaims) {
                        reportedRows.add(new Object[]{
                                claim.getClaimId(),
                                "#" + claim.getItem().getItemId() + "  " + claim.getItemTitle(),
                                claim.getClaimantName(),
                                DateFormats.dateTime(claim.getSubmittedAt()),
                                claim.getStatus().getLabel(),
                                claim.getRemarks()});
                    }
                    reportedModel.setRows(reportedRows);
                    updateButtons();
                },
                error -> Async.showError(this, error, "Could not load claims"));
    }

    /** Immutable result of the three background queries. */
    private static final class Snapshot {
        private final List<Claim> all;
        private final List<Claim> mine;
        private final List<Claim> reported;

        private Snapshot(List<Claim> all, List<Claim> mine, List<Claim> reported) {
            this.all = all;
            this.mine = mine;
            this.reported = reported;
        }
    }

    private void updateButtons() {
        Claim claim = selectedClaim();
        boolean pending = claim != null && claim.getStatus() == ClaimStatus.PENDING;
        boolean approved = claim != null && claim.getStatus() == ClaimStatus.APPROVED;

        approveButton.setEnabled(pending);
        rejectButton.setEnabled(pending);
        recoverButton.setEnabled(approved);

        Claim reported = selectedReportedClaim();
        confirmButton.setEnabled(reported != null
                && reported.getStatus() == ClaimStatus.APPROVED);
    }

    private Claim selectedClaim() {
        int viewRow = allTable.getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        int modelRow = allTable.convertRowIndexToModel(viewRow);
        return modelRow >= 0 && modelRow < allClaims.size() ? allClaims.get(modelRow) : null;
    }

    private Claim selectedReportedClaim() {
        int viewRow = reportedTable.getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        int modelRow = reportedTable.convertRowIndexToModel(viewRow);
        return modelRow >= 0 && modelRow < reportedClaims.size()
                ? reportedClaims.get(modelRow) : null;
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

        setActionsBusy(true);
        Async.run(() -> {
            if (approve) {
                claimService.approveClaim(claim, SessionContext.getCurrentUser(), remarks);
            } else {
                claimService.rejectClaim(claim, SessionContext.getCurrentUser(), remarks);
            }
            return Boolean.TRUE;
        }, ignored -> {
            setActionsBusy(false);
            JOptionPane.showMessageDialog(this,
                    approve
                            ? "Claim approved. The item is now reserved for "
                              + claim.getClaimantName() + "."
                            : "Claim rejected. The item stays available for other claimants.",
                    approve ? "Claim approved" : "Claim rejected",
                    JOptionPane.INFORMATION_MESSAGE);
            refreshData();
        }, error -> {
            setActionsBusy(false);
            showActionError(error);
        });
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

        setActionsBusy(true);
        Async.run(() -> {
            claimService.markRecovered(claim, SessionContext.getCurrentUser());
            return Boolean.TRUE;
        }, ignored -> {
            setActionsBusy(false);
            JOptionPane.showMessageDialog(this,
                    "Item marked as recovered. The dashboard counters were updated.",
                    "Recovery recorded", JOptionPane.INFORMATION_MESSAGE);
            refreshData();
        }, error -> {
            setActionsBusy(false);
            showActionError(error);
        });
    }

    /**
     * Finder-side final step: after staff verified the owner, the person who
     * reported the found item confirms the physical handover. This hides the
     * item from Search and claim (it becomes RECOVERED) while it remains in the
     * reports and analytics aggregates.
     */
    private void confirmHandover() {
        Claim claim = selectedReportedClaim();
        if (claim == null) {
            return;
        }
        if (claim.getStatus() != ClaimStatus.APPROVED) {
            JOptionPane.showMessageDialog(this,
                    "Only an approved claim can be confirmed as handed over.",
                    "Not ready", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int choice = JOptionPane.showConfirmDialog(this,
                "Confirm that you handed over \"#" + claim.getItem().getItemId()
                + "  " + claim.getItemTitle() + "\" to " + claim.getClaimantName() + "?",
                "Confirm handover", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        setActionsBusy(true);
        Async.run(() -> {
            claimService.confirmHandover(claim, SessionContext.getCurrentUser());
            return Boolean.TRUE;
        }, ignored -> {
            setActionsBusy(false);
            JOptionPane.showMessageDialog(this,
                    "Handover confirmed. The item is now archived for reports and "
                    + "will no longer appear in Search and claim.",
                    "Handover confirmed", JOptionPane.INFORMATION_MESSAGE);
            refreshData();
        }, error -> {
            setActionsBusy(false);
            showActionError(error);
        });
    }

    private void setActionsBusy(boolean busy) {
        approveButton.setEnabled(!busy);
        rejectButton.setEnabled(!busy);
        recoverButton.setEnabled(!busy);
        confirmButton.setEnabled(!busy);
        if (!busy) {
            updateButtons();
        }
    }

    private void showActionError(Throwable error) {
        if (error instanceof ValidationException) {
            JOptionPane.showMessageDialog(this, error.getMessage(),
                    "Not allowed", JOptionPane.WARNING_MESSAGE);
        } else {
            Async.showError(this, error, "Transaction failed");
        }
    }
}
