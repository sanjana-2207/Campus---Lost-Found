package com.campus.web.service;

import com.campus.web.model.AppNotification;
import com.campus.web.model.Claim;
import com.campus.web.model.ClaimStatus;
import com.campus.web.model.User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class NotificationService {

    private final ClaimService claims;

    public NotificationService(ClaimService claims) {
        this.claims = claims;
    }

    public List<AppNotification> buildFor(User user) {
        List<AppNotification> notifications = new ArrayList<>();
        if (user == null) {
            return notifications;
        }
        for (Claim claim : claims.claimsOf(user.getId())) {
            notifications.add(forClaimant(claim));
        }
        for (Claim claim : claims.claimsForReporter(user.getId())) {
            notifications.add(forReporter(claim));
        }
        notifications.sort(Comparator.comparing(
                AppNotification::getTime,
                Comparator.nullsLast(Comparator.reverseOrder())));
        return notifications;
    }

    private AppNotification forClaimant(Claim claim) {
        String item = "\"" + claim.getItemTitle() + "\"";
        String title;
        String message;
        switch (claim.getStatus()) {
            case APPROVED:
                title = "Claim approved";
                message = "Your claim for " + item + " was approved. Collect it from the finder.";
                break;
            case REJECTED:
                title = "Claim rejected";
                message = "Your claim for " + item + " was rejected."
                        + suffix(claim.getRemarks());
                break;
            case RECOVERED:
                title = "Item recovered";
                message = "You received " + item + ". The claim is complete.";
                break;
            default:
                title = "Claim submitted";
                message = "Your claim for " + item + " is awaiting verification.";
                break;
        }
        return new AppNotification(title, message, claim.getStatus().getLabel(),
                time(claim));
    }

    private AppNotification forReporter(Claim claim) {
        String item = "\"" + claim.getItemTitle() + "\"";
        String claimant = claim.getClaimantName();
        String title;
        String message;
        switch (claim.getStatus()) {
            case APPROVED:
                title = "Action needed: confirm handover";
                message = claimant + " was verified for " + item
                        + ". Confirm the handover from Claims.";
                break;
            case REJECTED:
                title = "Claim rejected";
                message = "The claim by " + claimant + " for " + item + " was rejected.";
                break;
            case RECOVERED:
                title = "Handover complete";
                message = item + " was handed over to " + claimant + ".";
                break;
            default:
                title = "New claim on your report";
                message = claimant + " claimed " + item + ". Waiting for staff verification.";
                break;
        }
        return new AppNotification(title, message, claim.getStatus().getLabel(),
                time(claim));
    }

    private LocalDateTime time(Claim claim) {
        return claim.getVerifiedAt() != null
                ? claim.getVerifiedAt() : claim.getSubmittedAt();
    }

    private String suffix(String remarks) {
        return remarks == null || remarks.isBlank() ? "" : " " + remarks.trim();
    }
}
