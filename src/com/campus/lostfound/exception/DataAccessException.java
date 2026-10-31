package com.campus.lostfound.exception;

/**
 * Unchecked wrapper around {@link java.sql.SQLException}.
 *
 * <p>Keeping SQL exceptions unchecked means the user interface never has to
 * catch database errors; it only has to show a friendly message. The original
 * exception is always kept as the cause for debugging.</p>
 */
public class DataAccessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DataAccessException(String message) {
        super(message);
    }

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
