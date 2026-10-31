package com.campus.lostfound.dao;

import com.campus.lostfound.model.Claim;
import com.campus.lostfound.model.ClaimStatus;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

/** Data access contract for ownership claims. */
public interface ClaimDAO extends GenericDAO<Claim> {

    /** All claims with item title, claimant name and verifier name resolved. */
    List<Claim> findAllDetailed();

    /** Claims raised against one item. */
    List<Claim> findByItem(int itemId);

    /** Claims submitted by one user. */
    List<Claim> findByClaimant(int claimantId);

    /**
     * True when this user already has an <em>active</em> claim on this item.
     * Rejected claims do not block a fresh claim.
     */
    boolean existsActiveFor(int itemId, int claimantId);

    /** Transaction-aware variant used while the item row is locked. */
    boolean existsActiveFor(Connection connection, int itemId, int claimantId);

    /** Inserts a claim inside a caller-managed transaction. */
    int insert(Connection connection, Claim claim);

    /**
     * Updates the claim status inside a transaction owned by the caller, but
     * only when the row still has {@code expectedStatus}. A zero return value
     * means somebody else changed the claim first (optimistic lock).
     *
     * <p>A null verifier keeps the previous verifier columns untouched.</p>
     */
    boolean updateStatus(Connection connection, int claimId, ClaimStatus expectedStatus,
                         ClaimStatus newStatus, Integer verifierId, String remarks);

    /**
     * Rejects every other PENDING claim on the same item. Called after one claim
     * is approved so that competing claims cannot stay pending forever.
     *
     * @return number of claims that were rejected
     */
    int rejectOtherPending(Connection connection, int itemId, int approvedClaimId,
                           int verifierId, String remarks);

    /** Claim status -> count, used by the reports screen. */
    Map<ClaimStatus, Integer> countByStatus();

    /** Number of claims still waiting for verification. */
    int countPending();
}
