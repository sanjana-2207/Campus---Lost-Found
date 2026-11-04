package com.campus.web.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AppNotification {

    private static final DateTimeFormatter TIME =
            DateTimeFormatter.ofPattern("dd-MM-yyyy, hh:mm a");

    private final String title;
    private final String message;
    private final String status;
    private final LocalDateTime time;

    public AppNotification(String title, String message, String status,
                           LocalDateTime time) {
        this.title = title;
        this.message = message;
        this.status = status;
        this.time = time;
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

    public LocalDateTime getTime() {
        return time;
    }

    public String getTimeLabel() {
        return time == null ? "" : time.format(TIME);
    }
}
