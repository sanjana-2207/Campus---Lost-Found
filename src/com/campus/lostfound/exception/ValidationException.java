package com.campus.lostfound.exception;

/**
 * Thrown when user input fails a business rule (empty title, future date,
 * duplicate claim, wrong password, ...).
 *
 * <p>This is a <b>business</b> error, not a technical one, so the message is
 * safe to show directly in the user interface.</p>
 */
public class ValidationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ValidationException(String message) {
        super(message);
    }
}
