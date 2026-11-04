package com.campus.lostfound.service;

import com.campus.lostfound.model.AppNotification;
import com.campus.lostfound.model.Claim;
import com.campus.lostfound.model.User;
import com.campus.lostfound.util.DateFormats;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Builds the notification feed from the two claim perspectives a user can have:
 * claims they submitted, and claims raised against items they reported.
 */
public class NotificationService {


    private final ClaimService claimService;

    public NotificationService() {
        this(new ClaimService());
    }

    public NotificationService(ClaimService claimService) {
        this.claimService = claimService;
    }

    public List<AppNotification> buildFor(User user) {
        List<AppNotification> notifications = new ArrayList<AppNotification>();
        if (user == null) {
            return notifications;
        }

        for (Claim claim : claimService.claimsOf(user.getUserId())) {
            notifications.add(forClaimant(claim));
        }
        for (Claim claim : claimService.claimsOnMyReports(user.getUserId())) {
            notifications.add(forReporter(claim));
        }

        Collections.sort(notifications, new Comparator<AppNotification>() {
            @Override
            public int compare(AppNotification left, AppNotification right) {
                Timestamp a = left.getSortTime();
                Timestamp b = right.getSortTime();
                if (a == null && b == null) {
                    return 0;
                }
                if (a == null) {
                    return 1;
                }
                if (b == null) {
                    return -1;
                }
                return b.compareTo(a);
            }
        });
        return notifications;
    }

    private AppNotification forClaimant(Claim claim) {
        String item = "\"" + claim.getItemTitle() + "\"";
        String title;
        String message;
        switch (claim.getStatus()) {
            case APPROVED:
                title = "Claim approved";
                message = "Your claim for " + item
                        + " was approved. Collect it from the finder.";
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
            case PENDING:
            default:
                title = "Claim submitted";
                message = "Your claim for " + item + " is awaiting verification.";
                break;
        }
        return new AppNotification(title, message, claim.getStatus().getLabel(),
                timeLabel(claim), sortTime(claim));
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
                message = "The claim by " + claimant + " for " + item
                        + " was rejected.";
                break;
            case RECOVERED:
                title = "Handover complete";
                message = item + " was handed over to " + claimant + ".";
                break;
            case PENDING:
            default:
                title = "New claim on your report";
                message = claimant + " claimed " + item
                        + ". Waiting for staff verification.";
                break;
        }
        return new AppNotification(title, message, claim.getStatus().getLabel(),
                timeLabel(claim), sortTime(claim));
    }

    private String suffix(String remarks) {
        return remarks == null || remarks.trim().isEmpty()
                ? "" : " " + remarks.trim();
    }

    private Timestamp sortTime(Claim claim) {
        return claim.getVerifiedAt() != null
                ? claim.getVerifiedAt() : claim.getSubmittedAt();
    }

    private String timeLabel(Claim claim) {
        Timestamp time = sortTime(claim);
        return DateFormats.dateTime(time);
    }
}
