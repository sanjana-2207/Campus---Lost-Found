package com.campus.lostfound.util;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/** One place for the dd-MM-yyyy date style used across the application. */
public final class DateFormats {

    public static final DateTimeFormatter DATE =
            DateTimeFormatter.ofPattern("dd-MM-yyyy");
    public static final DateTimeFormatter DATE_TIME =
            DateTimeFormatter.ofPattern("dd-MM-yyyy, hh:mm a");
    public static final DateTimeFormatter MONTH =
            DateTimeFormatter.ofPattern("MM-yyyy");

    private DateFormats() {
    }

    public static String date(LocalDate date) {
        return date == null ? "-" : date.format(DATE);
    }

    public static String dateTime(Timestamp timestamp) {
        return timestamp == null ? "" : timestamp.toLocalDateTime().format(DATE_TIME);
    }

    /** Converts a stored "yyyy-MM" monthly key to "MM-yyyy". */
    public static String month(String storedKey) {
        if (storedKey == null || storedKey.length() < 7) {
            return storedKey == null ? "" : storedKey;
        }
        return storedKey.substring(5, 7) + "-" + storedKey.substring(0, 4);
    }
}
