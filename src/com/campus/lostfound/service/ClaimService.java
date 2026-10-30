package com.campus.lostfound.service;

import com.campus.lostfound.config.DBConnection;
import com.campus.lostfound.dao.ClaimDAO;
import com.campus.lostfound.dao.ItemDAO;
import com.campus.lostfound.dao.JdbcClaimDAO;
import com.campus.lostfound.dao.JdbcItemDAO;
import com.campus.lostfound.exception.DataAccessException;
import com.campus.lostfound.exception.ValidationException;
import com.campus.lostfound.model.Claim;
import com.campus.lostfound.model.ClaimStatus;
import com.campus.lostfound.model.Item;
import com.campus.lostfound.model.ItemStatus;
import com.campus.lostfound.model.Role;
import com.campus.lostfound.model.User;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Ownership claim workflow.
 *
 * <p><b>This class is the transaction showcase of the project.</b> Approving a
 * claim runs three statements that must all succeed or all fail:</p>
 * <ol>
 *   <li>the claim becomes APPROVED,</li>
 *   <li>the item becomes CLAIMED,</li>
 *   <li>every competing pending claim on that item becomes REJECTED.</li>
 * </ol>
 * <p>If any statement fails, {@code rollback()} restores the previous state, so
 * the database can never show an approved claim against an open item.</p>
 */
public class ClaimService {

    private final ClaimDAO claimDAO;
    private final ItemDAO itemDAO;

    public ClaimService() {
        this(new JdbcClaimDAO(), new JdbcItemDAO());
    }

    public ClaimService(ClaimDAO claimDAO, ItemDAO itemDAO) {
        this.claimDAO = claimDAO;
        this.itemDAO = itemDAO;
    }

    // ==================================================================
    //  Claimant side
    // ==================================================================

    /** Files a new PENDING claim after checking the business rules. */
    public Claim submitClaim(Item item, User claimant, String proof) {
        if (item == null) {
            throw new ValidationException("Select an item to claim.");
        }
        if (claimant == null) {
            throw new ValidationException("You must be signed in to claim an item.");
        }
        if (item.getReportedBy() != null
                && item.getReportedBy().getUserId() == claimant.getUserId()) {
            throw new ValidationException("You cannot claim an item that you reported yourself.");
        }
        if (item.getStatus() != ItemStatus.OPEN && item.getStatus() != ItemStatus.MATCHED) {
            throw new ValidationException(
                    "This item is already claimed or recovered, so no new claim can be filed.");
        }
        if (proof == null || proof.trim().length() < 15) {
            throw new ValidationException(
                    "Describe the item in at least 15 characters (colour, marks, contents) "
                    + "so the staff can verify that it is yours.");
        }
        if (claimDAO.existsFor(item.getItemId(), claimant.getUserId())) {
            throw new ValidationException("You have already submitted a claim for this item.");
        }

        Claim claim = new Claim(item, claimant, proof.trim());
        claimDAO.insert(claim);
        return claim;
    }

    public List<Claim> claimsOf(int claimantId) {
        return claimDAO.findByClaimant(claimantId);
    }

    // ==================================================================
    //  Verifier side (STAFF / ADMIN)
    // ==================================================================

    /** APPROVED + item CLAIMED + competing claims REJECTED, atomically. */
    public void approveClaim(Claim claim, User verifier, String remarks) {
        requireVerifier(verifier);
        if (claim == null || claim.getStatus() != ClaimStatus.PENDING) {
            throw new ValidationException("Only a pending claim can be approved.");
        }

        final String note = isBlank(remarks) ? "Ownership verified by staff." : remarks.trim();
        final int itemId = claim.getItem().getItemId();

        runInTransaction(new TransactionWork() {
            @Override
            public void execute(Connection connection) {
                claimDAO.updateStatus(connection, claim.getClaimId(),
                        ClaimStatus.APPROVED, verifier.getUserId(), note);
                itemDAO.updateStatus(connection, itemId, ItemStatus.CLAIMED);
                claimDAO.rejectOtherPending(connection, itemId, claim.getClaimId(),
                        verifier.getUserId(),
                        "Closed automatically: another claim was approved for this item.");
            }
        });
    }

    /** Rejects a pending claim; the item stays available for other claimants. */
    public void rejectClaim(Claim claim, User verifier, String remarks) {
        requireVerifier(verifier);
        if (claim == null || claim.getStatus() != ClaimStatus.PENDING) {
            throw new ValidationException("Only a pending claim can be rejected.");
        }
        final String note = isBlank(remarks) ? "Ownership could not be verified." : remarks.trim();

        runInTransaction(new TransactionWork() {
            @Override
            public void execute(Connection connection) {
                claimDAO.updateStatus(connection, claim.getClaimId(),
                        ClaimStatus.REJECTED, verifier.getUserId(), note);
            }
        });
    }

    /** Marks an approved claim as physically handed over to the owner. */
    public void markRecovered(Claim claim, User verifier) {
        requireVerifier(verifier);
        if (claim == null || claim.getStatus() != ClaimStatus.APPROVED) {
            throw new ValidationException("Approve the claim before marking the item as recovered.");
        }
        final int itemId = claim.getItem().getItemId();

        runInTransaction(new TransactionWork() {
            @Override
            public void execute(Connection connection) {
                claimDAO.updateStatus(connection, claim.getClaimId(),
                        ClaimStatus.RECOVERED, verifier.getUserId(),
                        "Item handed over to the owner.");
                itemDAO.updateStatus(connection, itemId, ItemStatus.RECOVERED);
            }
        });
    }

    public List<Claim> allClaims() {
        return claimDAO.findAllDetailed();
    }

    public int pendingCount() {
        return claimDAO.countPending();
    }

    // ==================================================================
    //  Transaction plumbing
    // ==================================================================

    /** One unit of work that participates in a caller-owned transaction. */
    private interface TransactionWork {
        void execute(Connection connection);
    }

    private void runInTransaction(TransactionWork work) {
        Connection connection = null;
        try {
            connection = DBConnection.getInstance().getConnection();
            connection.setAutoCommit(false);

            work.execute(connection);
            connection.commit();

        } catch (SQLException ex) {
            rollbackQuietly(connection);
            throw new DataAccessException(
                    "The claim could not be completed and was cancelled. " + ex.getMessage(), ex);
        } catch (RuntimeException ex) {
            // Any DAO failure rolls the whole unit of work back.
            rollbackQuietly(connection);
            throw ex;
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                } catch (SQLException ignored) {
                    // Closing anyway.
                }
                DBConnection.closeQuietly(connection);
            }
        }
    }

    private void rollbackQuietly(Connection connection) {
        if (connection != null) {
            try {
                connection.rollback();
            } catch (SQLException ignored) {
                // The original failure is the useful one.
            }
        }
    }

    private void requireVerifier(User verifier) {
        if (verifier == null
                || (verifier.getRole() != Role.STAFF && verifier.getRole() != Role.ADMIN)) {
            throw new ValidationException("Only staff or an administrator can verify claims.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
