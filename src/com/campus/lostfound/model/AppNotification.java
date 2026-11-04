package com.campus.lostfound.model;

import java.sql.Timestamp;

/**
 * A read-only notification derived from claim activity. FINDIT does not store a
 * separate notifications table, so the list is rebuilt from the claims the user
 * is involved in.
 */
public class AppNotification {

    private final String title;
    private final String message;
    private final String status;
    private final String timeLabel;
    private final Timestamp sortTime;

    public AppNotification(String title, String message, String status,
                           String timeLabel, Timestamp sortTime) {
        this.title = title;
        this.message = message;
        this.status = status;
        this.timeLabel = timeLabel;
        this.sortTime = sortTime;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public String getStatus() {
        return status;
    }

    public String getTimeLabel() {
        return timeLabel;
    }

    public Timestamp getSortTime() {
        return sortTime;
    }
}
