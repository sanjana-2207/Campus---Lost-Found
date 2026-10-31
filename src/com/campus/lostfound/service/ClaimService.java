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
import com.campus.lostfound.model.ItemType;
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

    private static final int MIN_PROOF_LENGTH = 15;
    private static final int MAX_PROOF_LENGTH = 2000;
    private static final int MAX_REMARKS_LENGTH = 255;

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

    /**
     * Files a new PENDING claim after checking the business rules.
     *
     * <p>Only FOUND reports can be claimed: a LOST report describes an item its
     * reporter is still looking for, so there is nothing to hand over. The item
     * row is locked while the claim is inserted, which closes the race where two
     * students claim the same item at the same moment.</p>
     */
    public Claim submitClaim(Item item, User claimant, String proof) {
        if (item == null) {
            throw new ValidationException("Select an item to claim.");
        }
        if (claimant == null) {
            throw new ValidationException("You must be signed in to claim an item.");
        }
        if (item.getItemType() != ItemType.FOUND) {
            throw new ValidationException(
                    "Only found items can be claimed. This report describes a lost item.");
        }
        if (proof == null || proof.trim().length() < MIN_PROOF_LENGTH) {
            throw new ValidationException(
                    "Describe the item in at least " + MIN_PROOF_LENGTH
                    + " characters (colour, marks, contents) "
                    + "so the staff can verify that it is yours.");
        }
        if (proof.trim().length() > MAX_PROOF_LENGTH) {
            throw new ValidationException(
                    "Keep the proof of ownership under " + MAX_PROOF_LENGTH + " characters.");
        }

        final String cleanedProof = proof.trim();
        final Claim claim = new Claim(item, claimant, cleanedProof);

        runInTransaction(new TransactionWork() {
            @Override
            public void execute(Connection connection) {
                Item locked = itemDAO.findByIdForUpdate(connection, item.getItemId());
                if (locked == null) {
                    throw new ValidationException("This item report no longer exists.");
                }
                if (locked.getItemType() != ItemType.FOUND) {
                    throw new ValidationException(
                            "Only found items can be claimed. This report describes a lost item.");
                }
                if (locked.getReportedBy() != null
                        && locked.getReportedBy().getUserId() == claimant.getUserId()) {
                    throw new ValidationException(
                            "You cannot claim an item that you reported yourself.");
                }
                if (locked.getStatus() != ItemStatus.OPEN
                        && locked.getStatus() != ItemStatus.MATCHED) {
                    throw new ValidationException(
                            "This item is already claimed or recovered, so no new claim can be filed.");
                }
                if (claimDAO.existsActiveFor(connection, item.getItemId(),
                        claimant.getUserId())) {
                    throw new ValidationException(
                            "You already have an active claim for this item.");
                }
                claimDAO.insert(connection, claim);
            }
        });
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

        final String note = cleanRemarks(remarks, "Ownership verified by staff.");
        final int itemId = claim.getItem().getItemId();

        runInTransaction(new TransactionWork() {
            @Override
            public void execute(Connection connection) {
                Item locked = itemDAO.findByIdForUpdate(connection, itemId);
                if (locked == null) {
                    throw new ValidationException("This item report no longer exists.");
                }
                if (locked.getStatus() != ItemStatus.OPEN
                        && locked.getStatus() != ItemStatus.MATCHED) {
                    throw new ValidationException(
                            "This item is already claimed or recovered. Refresh the list.");
                }
                if (!claimDAO.updateStatus(connection, claim.getClaimId(),
                        ClaimStatus.PENDING, ClaimStatus.APPROVED,
                        verifier.getUserId(), note)) {
                    throw new ValidationException(
                            "This claim was already processed by somebody else. Refresh the list.");
                }
                if (!itemDAO.updateStatus(connection, itemId,
                        locked.getStatus(), ItemStatus.CLAIMED)) {
                    throw new ValidationException(
                            "The item changed while the claim was being approved. Refresh the list.");
                }
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
        final String note = cleanRemarks(remarks, "Ownership could not be verified.");

        runInTransaction(new TransactionWork() {
            @Override
            public void execute(Connection connection) {
                if (!claimDAO.updateStatus(connection, claim.getClaimId(),
                        ClaimStatus.PENDING, ClaimStatus.REJECTED,
                        verifier.getUserId(), note)) {
                    throw new ValidationException(
                            "This claim was already processed by somebody else. Refresh the list.");
                }
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
                Item locked = itemDAO.findByIdForUpdate(connection, itemId);
                if (locked == null) {
                    throw new ValidationException("This item report no longer exists.");
                }
                if (locked.getStatus() != ItemStatus.CLAIMED) {
                    throw new ValidationException(
                            "This item is not in the claimed state. Refresh the list.");
                }
                if (!claimDAO.updateStatus(connection, claim.getClaimId(),
                        ClaimStatus.APPROVED, ClaimStatus.RECOVERED,
                        verifier.getUserId(), "Item handed over to the owner.")) {
                    throw new ValidationException(
                            "This claim was already processed by somebody else. Refresh the list.");
                }
                if (!itemDAO.updateStatus(connection, itemId,
                        ItemStatus.CLAIMED, ItemStatus.RECOVERED)) {
                    throw new ValidationException(
                            "The item changed while recovery was being recorded. Refresh the list.");
                }
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
                    "The claim could not be completed and was cancelled. "
                    + "Please try again or contact the administrator.", ex);
        } catch (RuntimeException ex) {
            // Any DAO failure rolls the whole unit of work back.
            rollbackQuietly(connection);
            throw ex;
        } finally {
            // Returning the connection to the pool resets auto-commit and rolls
            // back anything that was not committed.
            DBConnection.closeQuietly(connection);
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

    private String cleanRemarks(String remarks, String fallback) {
        String cleaned = isBlank(remarks) ? fallback : remarks.trim();
        if (cleaned.length() > MAX_REMARKS_LENGTH) {
            throw new ValidationException(
                    "Keep the remarks under " + MAX_REMARKS_LENGTH + " characters.");
        }
        return cleaned;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
