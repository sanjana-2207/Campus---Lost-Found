package com.campus.web.service;

import com.campus.web.model.Claim;
import com.campus.web.model.ClaimStatus;
import com.campus.web.model.Item;
import com.campus.web.model.ItemStatus;
import com.campus.web.model.ItemType;
import com.campus.web.model.User;
import com.campus.web.repository.ClaimRepository;
import com.campus.web.repository.ItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ClaimService {

    private static final int MIN_PROOF_LENGTH = 15;

    private final ClaimRepository claims;
    private final ItemRepository items;

    public ClaimService(ClaimRepository claims, ItemRepository items) {
        this.claims = claims;
        this.items = items;
    }

    public List<Claim> claimsOf(Long userId) {
        return claims.findByClaimantIdOrderBySubmittedAtDesc(userId);
    }

    public List<Claim> claimsForReporter(Long reporterId) {
        return claims.findForReporter(reporterId);
    }

    public List<Claim> allClaims() {
        return claims.findAllByOrderBySubmittedAtDesc();
    }

    public long pendingCount() {
        return claims.countByStatus(ClaimStatus.PENDING);
    }

    public long actionableCount(User user) {
        if (user == null) {
            return 0;
        }
        long count = 0;
        if (user.isVerifier()) {
            count += claims.countByStatus(ClaimStatus.PENDING);
        }
        for (Claim claim : claims.findForReporter(user.getId())) {
            if (claim.getStatus() == ClaimStatus.APPROVED) {
                count++;
            }
        }
        return count;
    }

    @Transactional
    public Claim submitClaim(Item item, User claimant, String proof) {
        if (item == null) {
            throw new IllegalArgumentException("Select an item to claim.");
        }
        if (item.getType() != ItemType.FOUND) {
            throw new IllegalArgumentException("Only found items can be claimed.");
        }
        if (item.getReporter() != null
                && item.getReporter().getId().equals(claimant.getId())) {
            throw new IllegalArgumentException("You cannot claim an item you reported yourself.");
        }
        if (item.getStatus() != ItemStatus.OPEN
                && item.getStatus() != ItemStatus.MATCHED) {
            throw new IllegalArgumentException("This item is no longer open for claims.");
        }
        String cleanProof = proof == null ? "" : proof.trim();
        if (cleanProof.length() < MIN_PROOF_LENGTH) {
            throw new IllegalArgumentException(
                    "Describe the item in at least " + MIN_PROOF_LENGTH + " characters.");
        }
        boolean active = claims.existsByItemIdAndClaimantIdAndStatusIn(
                item.getId(), claimant.getId(),
                List.of(ClaimStatus.PENDING, ClaimStatus.APPROVED, ClaimStatus.RECOVERED));
        if (active) {
            throw new IllegalArgumentException("You already have an active claim for this item.");
        }

        Claim claim = new Claim();
        claim.setItem(item);
        claim.setClaimant(claimant);
        claim.setProof(cleanProof);
        claim.setStatus(ClaimStatus.PENDING);
        claim.setSubmittedAt(LocalDateTime.now());
        return claims.save(claim);
    }

    @Transactional
    public void approve(Long claimId, User verifier, String remarks) {
        requireVerifier(verifier);
        Claim claim = claims.findById(claimId)
                .orElseThrow(() -> new IllegalArgumentException("Claim not found."));
        if (claim.getStatus() != ClaimStatus.PENDING) {
            throw new IllegalArgumentException("Only a pending claim can be approved.");
        }
        Item item = claim.getItem();
        if (item.getStatus() != ItemStatus.OPEN
                && item.getStatus() != ItemStatus.MATCHED) {
            throw new IllegalArgumentException("This item is no longer open.");
        }

        claim.setStatus(ClaimStatus.APPROVED);
        claim.setVerifiedBy(verifier);
        claim.setVerifiedAt(LocalDateTime.now());
        claim.setRemarks(cleanRemarks(remarks, "Ownership verified."));
        item.setStatus(ItemStatus.CLAIMED);

        for (Claim other : claims.findByItemIdAndStatusAndIdNot(
                item.getId(), ClaimStatus.PENDING, claim.getId())) {
            other.setStatus(ClaimStatus.REJECTED);
            other.setVerifiedBy(verifier);
            other.setVerifiedAt(LocalDateTime.now());
            other.setRemarks("Closed automatically: another claim was approved.");
            claims.save(other);
        }
        claims.save(claim);
        items.save(item);
    }

    @Transactional
    public void reject(Long claimId, User verifier, String remarks) {
        requireVerifier(verifier);
        Claim claim = claims.findById(claimId)
                .orElseThrow(() -> new IllegalArgumentException("Claim not found."));
        if (claim.getStatus() != ClaimStatus.PENDING) {
            throw new IllegalArgumentException("Only a pending claim can be rejected.");
        }
        claim.setStatus(ClaimStatus.REJECTED);
        claim.setVerifiedBy(verifier);
        claim.setVerifiedAt(LocalDateTime.now());
        claim.setRemarks(cleanRemarks(remarks, "Ownership could not be verified."));
        claims.save(claim);
    }

    @Transactional
    public void confirmHandover(Long claimId, User reporter) {
        Claim claim = claims.findById(claimId)
                .orElseThrow(() -> new IllegalArgumentException("Claim not found."));
        if (claim.getStatus() != ClaimStatus.APPROVED) {
            throw new IllegalArgumentException("Only an approved claim can be confirmed.");
        }
        Item item = claim.getItem();
        if (item.getReporter() == null
                || !item.getReporter().getId().equals(reporter.getId())) {
            throw new IllegalArgumentException(
                    "Only the person who reported the item can confirm the handover.");
        }
        if (item.getStatus() != ItemStatus.CLAIMED) {
            throw new IllegalArgumentException("This item is not reserved for a verified owner.");
        }

        String note = "Owner collected the item; handover confirmed by the finder.";
        if (claim.getRemarks() != null && !claim.getRemarks().isBlank()) {
            note = claim.getRemarks() + " | " + note;
        }
        claim.setStatus(ClaimStatus.RECOVERED);
        claim.setVerifiedAt(LocalDateTime.now());
        claim.setRemarks(note);
        item.setStatus(ItemStatus.RECOVERED);
        claims.save(claim);
        items.save(item);
    }

    private void requireVerifier(User user) {
        if (user == null || !user.isVerifier()) {
            throw new IllegalArgumentException("Only staff can verify claims.");
        }
    }

    private String cleanRemarks(String remarks, String fallback) {
        String note = remarks == null || remarks.isBlank() ? fallback : remarks.trim();
        return note.length() > 255 ? note.substring(0, 255) : note;
    }
}
