package com.campus.lostfound.model;

import java.sql.Timestamp;

/**
 * An ownership claim raised by a claimant against a found item.
 *
 * <p>A claim is an <b>association object</b>: it links a {@link User} to an
 * {@link Item} and carries the verification workflow (PENDING -> APPROVED or
 * REJECTED -> RECOVERED).</p>
 */
public class Claim {

    private int claimId;
    private Item item;
    private User claimant;
    private String proof;
    private ClaimStatus status = ClaimStatus.PENDING;
    private Timestamp submittedAt;
    private User verifiedBy;
    private Timestamp verifiedAt;
    private String remarks;

    public Claim() {
    }

    public Claim(Item item, User claimant, String proof) {
        this.item = item;
        this.claimant = claimant;
        this.proof = proof;
    }

    public int getClaimId() {
        return claimId;
    }

    public void setClaimId(int claimId) {
        this.claimId = claimId;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public User getClaimant() {
        return claimant;
    }

    public void setClaimant(User claimant) {
        this.claimant = claimant;
    }

    public String getProof() {
        return proof;
    }

    public void setProof(String proof) {
        this.proof = proof;
    }

    public ClaimStatus getStatus() {
        return status;
    }

    public void setStatus(ClaimStatus status) {
        this.status = status == null ? ClaimStatus.PENDING : status;
    }

    public Timestamp getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(Timestamp submittedAt) {
        this.submittedAt = submittedAt;
    }

    public User getVerifiedBy() {
        return verifiedBy;
    }

    public void setVerifiedBy(User verifiedBy) {
        this.verifiedBy = verifiedBy;
    }

    public Timestamp getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(Timestamp verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    // Null-safe helpers used by the Swing table model.
    public String getItemTitle() {
        return item == null ? "" : item.getTitle();
    }

    public String getClaimantName() {
        return claimant == null ? "" : claimant.getFullName();
    }

    public String getVerifierName() {
        return verifiedBy == null ? "" : verifiedBy.getFullName();
    }
}
