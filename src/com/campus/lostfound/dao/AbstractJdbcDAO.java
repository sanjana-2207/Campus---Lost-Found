package com.campus.lostfound.dao;

import com.campus.lostfound.config.DBConnection;
import com.campus.lostfound.exception.DataAccessException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Base class for all JDBC DAOs.
 *
 * <p><b>Inheritance in the persistence layer:</b> connection handling, parameter
 * binding, date conversion and error wrapping are written once here and reused by
 * every concrete DAO.</p>
 */
public abstract class AbstractJdbcDAO<T> implements GenericDAO<T> {

    /** Opens a connection or fails with a user-friendly message. */
    protected Connection openConnection() {
        try {
            return DBConnection.getInstance().getConnection();
        } catch (SQLException ex) {
            throw new DataAccessException(
                    "Cannot connect to MySQL. Check that the server is running and that "
                    + "db.properties has the correct user name and password.", ex);
        }
    }

    /** Converts a checked {@link SQLException} into an unchecked application error. */
    protected DataAccessException wrap(String action, SQLException ex) {
        return new DataAccessException(action + " (" + ex.getMessage() + ")", ex);
    }

    protected void rollbackQuietly(Connection connection) {
        if (connection != null) {
            try {
                connection.rollback();
            } catch (SQLException ignored) {
                // The original exception is more useful than the rollback failure.
            }
        }
    }

    /** Closes a JDBC resource without throwing (used after a transaction). */
    protected void closeQuietly(AutoCloseable resource) {
        if (resource != null) {
            try {
                resource.close();
            } catch (Exception ignored) {
                // Nothing useful can be done here.
            }
        }
    }

    /**
     * Binds a positional parameter list to a PreparedStatement.
     * Handles {@link LocalDate} and enum constants so callers can pass domain
     * objects instead of JDBC types.
     */
    protected void bind(PreparedStatement statement, List<Object> parameters)
            throws SQLException {
        for (int i = 0; i < parameters.size(); i++) {
            Object value = parameters.get(i);
            int index = i + 1;

            if (value == null) {
                statement.setNull(index, java.sql.Types.NULL);
            } else if (value instanceof LocalDate) {
                statement.setDate(index, java.sql.Date.valueOf((LocalDate) value));
            } else if (value instanceof Enum) {
                statement.setString(index, ((Enum<?>) value).name());
            } else {
                statement.setObject(index, value);
            }
        }
    }

    /** Null-safe conversion from java.sql.Date to java.time.LocalDate. */
    protected LocalDate toLocalDate(java.sql.Date date) {
        return date == null ? null : date.toLocalDate();
    }
}
